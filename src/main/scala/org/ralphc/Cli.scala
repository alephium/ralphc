package org.ralphc

import java.util.concurrent.Callable
import picocli.CommandLine.{Command, Option}
import org.alephium.protocol.vm.lang.CompilerOptions

@Command(name = "ralphc", mixinStandardHelpOptions = true, version = Array("ralphc 1.5.0-rc7"), description = Array("compiler ralph language."))
class Cli extends Callable[Int] {
  @Option(names = Array("-f"))
  val files: Array[String] = Array.empty

  @Option(names = Array("-d", "--debug"), defaultValue = "false", description = Array("debug mode"))
  var debug: Boolean = false

  @Option(names = Array("--ic"), defaultValue = "false", description = Array("ignore unused constants warning"))
  var ignoreUnusedConstantsWarnings: Boolean = false

  @Option(names = Array("--iv"), defaultValue = "false", description = Array("ignore unused variables warning"))
  var ignoreUnusedVariablesWarnings: Boolean = false

  @Option(names = Array("--if"), defaultValue = "false", description = Array("ignore unused fields warning"))
  var ignoreUnusedFieldsWarnings: Boolean = false

  @Option(names = Array("--ir"), defaultValue = "false", description = Array("ignore readonly check warning"))
  var ignoreReadonlyCheckWarnings: Boolean = false

  @Option(names = Array("--ip"), defaultValue = "false", description = Array("ignore unused private functions warning"))
  var ignoreUnusedPrivateFunctionsWarnings: Boolean = false

  @Option(names = Array("--ie"), defaultValue = "false", description = Array("ignore external call check warning"))
  var ignoreExternalCallCheckWarnings: Boolean = false

  @Option(names = Array("--warning"), defaultValue = "false", description = Array("consider warnings as errors"))
  var warningAsError: Boolean = false

  def _print[O](other: O): Int = {
    if (debug) {
      pprint.pprintln(files)
      pprint.pprintln(other)
    }
    0
  }

  def error[T, O](msg: T, other: O): Int = {
    pprint.pprintln(s"error: \n $msg")
    _print(other)
    -1
  }

  def warning[T, O](msg: T, other: O): Int = {
    pprint.pprintln(s"warning: \n $msg")
    _print(other)
    if (warningAsError) {
      -1
    } else {
      0
    }
  }

  def ok[T, O](msg: T, other: O): Int = {
    pprint.pprintln(msg)
    _print(other)
  }

  def print[O](msg: Either[String, String], other: O): Int = {
    msg.fold(ok(_, other), error(_, other))
  }

  override def call(): Int = {
    val compilerOptions = CompilerOptions(
      ignoreUnusedConstantsWarnings = ignoreUnusedConstantsWarnings,
      ignoreUnusedVariablesWarnings = ignoreUnusedVariablesWarnings,
      ignoreUnusedFieldsWarnings = ignoreUnusedFieldsWarnings,
      ignoreReadonlyCheckWarnings = ignoreReadonlyCheckWarnings,
      ignoreUnusedPrivateFunctionsWarnings = ignoreUnusedPrivateFunctionsWarnings,
      ignoreExternalCallCheckWarnings = ignoreExternalCallCheckWarnings
    )
    _print(compilerOptions)

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
                  ret => {
                    var checkWaringAsError = 0
                    ret.scripts.foreach(script => {
                      if (script.warnings.nonEmpty) {
                        warning(script.warnings, "")
                        checkWaringAsError -= 1
                      }
                      ok(script, "")
                    })
                    ret.contracts.foreach(contract => {
                      if (contract.warnings.nonEmpty) {
                        warning(contract.warnings, "")
                        checkWaringAsError -= 1
                      }
                      ok(contract, "")
                    })
                    if (warningAsError) {
                      checkWaringAsError
                    } else {
                      0
                    }
                  }
                )
            )
        )
    } yield ret
    rets.sum
  }
}
