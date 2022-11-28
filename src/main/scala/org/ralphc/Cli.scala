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
import java.nio.file.{Path, Paths}

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

  @Option(names = Array("-a", "--artifacts"), defaultValue = "artifacts", description = Array("artifacts directory"))
  var artifacts = "artifacts"

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

  val compilerOptions = CompilerOptions(
    ignoreUnusedConstantsWarnings = ignoreUnusedConstantsWarnings,
    ignoreUnusedVariablesWarnings = ignoreUnusedVariablesWarnings,
    ignoreUnusedFieldsWarnings = ignoreUnusedFieldsWarnings,
    ignoreUpdateFieldsCheckWarnings = ignoreUpdateFieldsCheckWarnings,
    ignoreUnusedPrivateFunctionsWarnings = ignoreUnusedPrivateFunctionsWarnings,
    ignoreExternalCallCheckWarnings = ignoreExternalCallCheckWarnings
  )

  def print[O](msg: Either[String, String], other: O): Int = {
    msg.fold(ok(_, other), error(_, other))
  }

  override def call(): Int = {
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
    implicit val hashWriter: Writer[Hash]                                         = StringWriter.comap[Hash](_.toHexString)
    implicit val hashReader: Reader[Hash]                                         = byteStringReader.map(Hash.from(_).get)
    implicit val compilerOptionsRW: ReadWriter[CompilerOptions]                   = macroRW
    implicit val compilePatchRW: ReadWriter[CompileProjectResult.Patch]           = readwriter[String].bimap(_.value, CompileProjectResult.Patch)
    implicit val compileResultFieldsRW: ReadWriter[CompileResult.FieldsSig]       = macroRW
    implicit val compileResultFunctionRW: ReadWriter[CompileResult.FunctionSig]   = macroRW
    implicit val compileResultEventRW: ReadWriter[CompileResult.EventSig]         = macroRW
    implicit val compileScriptResultRW: ReadWriter[CompileScriptResult]           = macroRW
    implicit val compileContractResultRW: ReadWriter[CompileContractResult]       = macroRW
    implicit val compileProjectResultRW: ReadWriter[CompileProjectResult]         = macroRW
    implicit val compileScriptResultSigRW: ReadWriter[CompileScriptResultSig]     = macroRW
    implicit val compileContractResultSigRW: ReadWriter[CompileContractResultSig] = macroRW
    implicit val compileProjectResultSigRW: ReadWriter[CompileProjectResultSig]   = macroRW
    implicit val codeInfoRW: ReadWriter[CodeInfo]                                 = macroRW
    implicit val artifactsRW: ReadWriter[Artifacts]                               = macroRW

    val scripts = ret.scripts.map(s => {
      val script = CompileScriptResultSig(
        s.version,
        s.name,
        s.bytecodeTemplate,
        s.fields,
        s.functions
      )

      val value = Parser.infos(s.name)
      value.warnings = s.warnings
      value.bytecodeDebugPatch = s.bytecodeDebugPatch
      Parser.infos.addOne(s.name, value)

      val code = write(script, 2)
      saveResultSig(code, Parser.infosPath(s.name)._2)
      script
    })
    val contracts = ret.contracts.map(c => {
      val contract = CompileContractResultSig(
        c.version,
        c.name,
        c.bytecode,
        c.codeHash,
        c.fields,
        c.events,
        c.functions
      )
      val value = Parser.infos(c.name)
      value.warnings = c.warnings
      value.bytecodeDebugPatch = c.bytecodeDebugPatch
      value.codeHashDebug = c.codeHashDebug
      Parser.infos.addOne(c.name, value)

      val code = write(contract, 2)
      saveResultSig(code, Parser.infosPath(c.name)._2)
      contract
    })

    var ast = write(CompileProjectResultSig(contracts, scripts), 2)
    debug(ast)
//    saveAst(ast)

    val artifacts = write(Artifacts(compilerOptions, Parser.infos.toMap), 2)
    saveProjectArtifacts(artifacts)

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

  def saveAst(codes: String): Unit = saveResultSig(codes, Paths.get(projectDir, "project.artifacts.json"))

  def saveProjectArtifacts(codes: String): Unit = saveResultSig(codes, Paths.get(artifactsPath().toString, ".project.json"))

  def saveResultSig(code: String, path: Path): Unit = {
    path.getParent.toFile.mkdirs()
    val writer = new PrintWriter(path.toFile)
    writer.write(code)
    writer.close()
  }

  def contractPath(): Path = Paths.get(projectDir)

  def rootPath(): Path = contractPath().getParent

  def artifactsPath(): Path = rootPath().resolve(artifacts)
}
