package org.ralphc

import java.io.File
import java.nio.file.Paths
import scala.collection.immutable
import scala.collection.mutable
import scala.io.Source
import scala.util.Using

object Parser {
  var files: mutable.Map[String, Node] = mutable.Map.empty // all file node

  def parser(path: String): Unit = {
    if (!files.contains(path)) {
      val dir                       = path.substring(0, path.lastIndexOf(File.separator))
      val deps: mutable.Set[String] = mutable.Set.empty
      var compile: Compile[String]  = Script("")
      var codes                     = ""
      Using(Source.fromFile(path)) { source =>
        {
          val lines = source.getLines()
          for (line <- lines) {
            if (line.trim.indexOf("import") == 0) {
              var depPath = line.trim.split("\\s+").last
              depPath = depPath.replace("\"", "")
              val path          = Paths.get(dir)
              val canonicalPath = path.resolve(depPath).toFile.getCanonicalPath
              deps += canonicalPath
            } else {
              codes += line
              if (line.trim.indexOf("TxContract") == 0 || line.trim.indexOf("Contract") == 0) compile = Contract(codes)
              if (line.trim.indexOf("TxScript") == 0) compile = Script(codes)
            }
          }
        }
      }
      files += (path -> Node(
        path,
        compile match {
          case Contract(_) => Contract(codes)
          case Script(_)   => Script(codes)
        },
        deps
      ))
      deps.foreach(parser)
    }
  }

  def Parser(path: String): Either[Array[String], (Compile[String], String)] = {
    var dfsNodes: immutable.ListMap[String, LightNode] = immutable.ListMap.empty
    val absolutePath                                   = new File(path).getCanonicalPath
    parser(absolutePath)

    def dfs(path: String): Option[Unit] = {
      dfsNodes
        .get(path)
        .fold[Option[Unit]](
          files
            .get(path)
            .map(node => {
              if (node.deps.nonEmpty) {
                dfsNodes += (node.path -> LightNode(node.compile, None))
                for (path <- node.deps) {
                  if (dfs(path).isEmpty) {
                    return None
                  }
                }
                dfsNodes.get(node.path).foreach(node => node.status = Some())
              } else {
                dfsNodes += (node.path -> LightNode(node.compile, Some()))
              }
            })
        )(node => node.status)
    }

    dfs(absolutePath).fold[Either[Array[String], (Compile[String], String)]](Left(dfsNodes.keys.toArray))(_ =>
      Right(
        (
          files(absolutePath).compile,
          dfsNodes.values
            .map(node => node.code.map(s => s, c => c))
            .fold("")(_ + _)
        )
      )
    )
  }
}
