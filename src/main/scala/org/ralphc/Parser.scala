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
      val node = new Node(path)
      val dir  = path.substring(0, path.lastIndexOf(File.separator))
      Using(Source.fromFile(path)) { source =>
        source
          .getLines()
          .foreach(line => {
            if (line.trim.indexOf("import") == 0) {
              var depPath = line.trim.split("\\s+").last
              depPath = depPath.replace("\"", "")
              val path          = Paths.get(dir)
              val canonicalPath = path.resolve(depPath).toFile.getCanonicalPath
              node.Deps += canonicalPath
            } else {
              if (line.trim.indexOf("TxContract") == 0 || line.trim.indexOf("Contract") == 0) node.ty = 2
              if (line.trim.indexOf("TxScript") == 0) node.ty = 1
              node.code += line
            }
          })
      }
      files += (path -> node)
      node.Deps.foreach(parser)
    }
  }

  def Parser(path: String): Either[Array[String], (Int, String)] = {
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
              if (node.Deps.nonEmpty) {
                dfsNodes += (node.path -> LightNode(node.path, node.code, 0))
                for (path <- node.Deps) {
                  if (dfs(path).isEmpty) {
                    return None
                  }
                }
                dfsNodes.get(node.path).foreach(node => node.status = 1)
              } else {
                dfsNodes += (node.path -> LightNode(node.path, node.code, 1))
              }
            })
        )(node => if (node.status == 0) None else { Some() })
    }

    dfs(absolutePath).fold[Either[Array[String], (Int, String)]](Left(dfsNodes.keys.toArray))(_ =>
      Right((files(absolutePath).ty, dfsNodes.values.map(_.code).fold("")(_ + _)))
    )
  }
}
