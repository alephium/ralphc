package org.ralphc

import java.io.File
import java.nio.file.Paths
import scala.collection.mutable
import scala.io.Source
import org.alephium.crypto
import org.alephium.api.{Try, failed}
import org.alephium.api.model.CompileProjectResult
import org.alephium.ralph.CompilerOptions
import org.alephium.ralph
import org.alephium.protocol.Hash
import org.alephium.util.AVector

object Compiler {
  val metaInfos      = mutable.Map.empty[String, MetaInfo]
  var projectDir     = "contracts"
  var projectDirName = "contracts"
  var artifactsName  = "artifacts"

  def getFile(file: File): Array[File] = {
    val files = file
      .listFiles()
      .filter(!_.isDirectory)
      .filter(t => t.toString.endsWith(".ral"))
    files ++ file.listFiles().filter(_.isDirectory).flatMap(getFile)
  }

  def projectCodes(rootPath: String, artifactsName: String): String = {
    this.projectDir = rootPath
    this.artifactsName = artifactsName
    this.projectDirName = new File(rootPath).getName
    getFile(new File(rootPath))
      .map(file => {
        val sourceCode     = Source.fromFile(file).mkString
        val sourceCodeHash = crypto.Sha256.hash(sourceCode).toHexString
        TypedMatcher
          .matcher(sourceCode)
          .map(_.getName)
          .map(name => {
            val path       = file.toPath.toString
            val sourcePath = Paths.get(path.substring(path.indexOf(this.projectDirName)))
            val savePath   = Paths.get(path.replace(projectDirName, artifactsName) + ".json")
            val meta       = MetaInfo(name, sourcePath, savePath, CodeInfo(sourceCodeHash, CompileProjectResult.Patch(""), Hash.zero, AVector()))
            metaInfos.addOne(name, meta)
          })
        sourceCode
      })
      .mkString
  }
  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def compileProject(code: String, compilerOptions: CompilerOptions = CompilerOptions.Default): Try[CompileProjectResult] = {
    ralph.Compiler
      .compileProject(code, compilerOptions = compilerOptions)
      .map(p => CompileProjectResult.from(p._1, p._2))
      .left
      .map(error => failed(error.toString))
  }
}
