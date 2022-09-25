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
              // v1.5.0^
              // if (line.trim.indexOf("TxContract") == 0 || line.trim.indexOf("Contract") == 0) compile = Contract(codes)
              // if (line.trim.indexOf("TxScript") == 0) compile = Script(codes)
            }
          }
        }
      }
      compile = Mix(codes)
      files += (path -> Node(
        path,
        compile.map(_ => codes, _ => codes, _ => codes),
        Option.when(deps.nonEmpty)(deps)
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
            .fold[Option[Unit]](Some())(node =>
              node.deps.fold[Option[Unit]] {
                dfsNodes += (node.path -> LightNode(node.compile, Some()))
                Some()
              }(deps => {
                val lightNode = LightNode(node.compile, None)
                dfsNodes += (node.path -> lightNode)
                deps
                  .map(path => dfs(path))
                  .find(_.isEmpty)
                  .fold[Option[Unit]] {
                    lightNode.status = Some()
                    Some()
                  }(_ => None)
              })
            )
        )(node => node.status)
    }

    dfs(absolutePath).fold[Either[Array[String], (Compile[String], String)]](Left(dfsNodes.keys.toArray))(_ =>
      Right(
        (
          files(absolutePath).compile,
          dfsNodes.values
            .map(node => node.code.fold(c => c)(c => c)(c => c))
            .fold("")(_ + _)
        )
      )
    )
  }

  def getFile(file: File): Array[File] = {
    val files = file
      .listFiles()
      .filter(!_.isDirectory)
      .filter(t => t.toString.endsWith(".ral"))
    files ++ file.listFiles().filter(_.isDirectory).flatMap(getFile)
  }

  def project(rootPath: String): String = getFile(new File(rootPath)).map(Source.fromFile(_).mkString).mkString

}
