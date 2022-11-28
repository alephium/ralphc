package org.ralphc

import java.io.PrintWriter
import java.util.concurrent.Callable
import picocli.CommandLine.{Command, Option}
import org.alephium.ralph.CompilerOptions
import org.alephium.api.model.CompileProjectResult
import org.alephium.api.model._
import org.alephium.json.Json._
import org.alephium.api.UtilJson._
import org.alephium.json.Json.ReadWriter
import org.alephium.protocol.Hash
import org.alephium.util.AVector

@Command(name = "ralphc", mixinStandardHelpOptions = true, version = Array("ralphc 1.5.4"), description = Array("compiler ralph language."))
class Cli extends Callable[Int] {
  @Option(names = Array("-f"))
  val files: Array[String] = Array.empty

  @Option(names = Array("-d", "--debug"), defaultValue = "false", description = Array("Debug mode"))
  var debug: Boolean = false

  @Option(names = Array("--ic"), defaultValue = "false", description = Array("Ignore unused constants warning"))
  var ignoreUnusedConstantsWarnings: Boolean = false

  @Option(names = Array("--iv"), defaultValue = "false", description = Array("Ignore unused variables warning"))
  var ignoreUnusedVariablesWarnings: Boolean = false

  @Option(names = Array("--if"), defaultValue = "false", description = Array("Ignore unused fields warning"))
  var ignoreUnusedFieldsWarnings: Boolean = false

  @Option(names = Array("--ir"), defaultValue = "false", description = Array("Ignore update field check warning"))
  var ignoreUpdateFieldsCheckWarnings: Boolean = false

  @Option(names = Array("--ip"), defaultValue = "false", description = Array("Ignore unused private functions warning"))
  var ignoreUnusedPrivateFunctionsWarnings: Boolean = false

  @Option(names = Array("--ie"), defaultValue = "false", description = Array("Ignore external call check warning"))
  var ignoreExternalCallCheckWarnings: Boolean = false

  @Option(names = Array("-w", "--warning"), defaultValue = "false", description = Array("Consider warnings as errors"))
  var warningAsError: Boolean = false

  @Option(names = Array("-p", "--project"), defaultValue = "", description = Array("Project path"))
  var projectDir: String = ""

  def debug[O](values: O*): Unit = {
    if (debug) {
      values.foreach(pprint.pprintln(_))
    }
  }

  def error[T, O](msg: T, other: O): Int = {
    pprint.pprintln(other)
    pprint.pprintln(s"error: \n $msg \n")
    -1
  }

  def warning[T, O](msg: T, other: O): Int = {
    pprint.pprintln(other)
    pprint.pprintln(s"warning: \n $msg \n")
    if (warningAsError) {
      -1
    } else {
      0
    }
  }

  def ok[T, O](msg: T, other: O): Int = {
    pprint.pprintln(other)
    pprint.pprintln(msg)
    0
  }

  def print[O](msg: Either[String, String], other: O): Int = {
    msg.fold(ok(_, other), error(_, other))
  }

  override def call(): Int = {
    val compilerOptions = CompilerOptions(
      ignoreUnusedConstantsWarnings = ignoreUnusedConstantsWarnings,
      ignoreUnusedVariablesWarnings = ignoreUnusedVariablesWarnings,
      ignoreUnusedFieldsWarnings = ignoreUnusedFieldsWarnings,
      ignoreUpdateFieldsCheckWarnings = ignoreUpdateFieldsCheckWarnings,
      ignoreUnusedPrivateFunctionsWarnings = ignoreUnusedPrivateFunctionsWarnings,
      ignoreExternalCallCheckWarnings = ignoreExternalCallCheckWarnings
    )
    debug(compilerOptions, s"projectDir: $projectDir", s"warningAsError: $warningAsError", s"files: ${files.mkString(",")}")

    if (projectDir.isEmpty) {
      val rets = for {
        path <- files
        ret = Parser
          .Parser(path)
          .fold(
            deps => error("circular dependency：\n" + deps.mkString("\n"), path),
            value =>
              value._1.fold(_ => Compiler.compileScript(value._2, compilerOptions).fold(err => error(err.detail, value), ret => ok(ret, value)))(_ =>
                Compiler.compileContract(value._2, compilerOptions).fold(err => error(err.detail, value), ret => ok(ret, value))
              )(_ =>
                Compiler
                  .compileProject(value._2, compilerOptions)
                  .fold(
                    err => error(err.detail, value),
                    ret => result(ret)
                  )
              )
          )
      } yield ret
      rets.sum
    } else {
      val codes = Parser.project(projectDir)
      debug(codes)
      Compiler
        .compileProject(codes, compilerOptions)
        .fold(
          err => error(err.detail, projectDir),
          ret => result(ret)
        )
    }
  }

  def result(ret: CompileProjectResult): Int = {
    implicit val hashWriter: Writer[Hash]                                       = StringWriter.comap[Hash](_.toHexString)
    implicit val hashReader: Reader[Hash]                                       = byteStringReader.map(Hash.from(_).get)
    implicit val compileResultFunctionRW: ReadWriter[CompileResult.FunctionSig] = macroRW
    implicit val compileResultEventRW: ReadWriter[CompileResult.EventSig]       = macroRW
    implicit val compilePatchRW: ReadWriter[CompileProjectResult.Patch]         = readwriter[String].bimap(_.value, CompileProjectResult.Patch)
    implicit val compileResultFieldsRW: ReadWriter[CompileResult.FieldsSig]     = macroRW
    implicit val compileScriptResultRW: ReadWriter[CompileScriptResult]         = macroRW
    implicit val compileContractResultRW: ReadWriter[CompileContractResult]     = macroRW
    implicit val compileProjectResultRW: ReadWriter[CompileProjectResult]       = macroRW

    val ast = write(ret, 2)
    saveAst(ast)
    debug(ast)
    var checkWaringAsError = 0
    val each = (warnings: AVector[String], name: String) => {
      if (warnings.nonEmpty) {
        warning(write(warnings, 2), name)
        checkWaringAsError -= 1
      }
    }
    ret.scripts.foreach(script => each(script.warnings, s"script.name: ${script.name}"))
    ret.contracts.foreach(contract => each(contract.warnings, s"contract.name: ${contract.name}"))
    if (warningAsError) {
      checkWaringAsError
    } else {
      0
    }
  }

  def saveAst(codes: String): Unit = {
    import java.nio.file.Paths
    val path   = Paths.get(projectDir, "project.artifacts.json")
    val writer = new PrintWriter(path.toFile)
    writer.write(codes)
    writer.close()
  }
}
