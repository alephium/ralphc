package org.ralphc

import java.util.concurrent.Callable
import picocli.CommandLine.{Command, Option}
import org.alephium.protocol.vm.lang.CompilerOptions

@Command(name = "ralphc", mixinStandardHelpOptions = true, version = Array("ralphc 1.5.0-rc7"), description = Array("compiler ralph language."))
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

  @Option(names = Array("--ir"), defaultValue = "false", description = Array("Ignore readonly check warning"))
  var ignoreReadonlyCheckWarnings: Boolean = false

  @Option(names = Array("--ip"), defaultValue = "false", description = Array("Ignore unused private functions warning"))
  var ignoreUnusedPrivateFunctionsWarnings: Boolean = false

  @Option(names = Array("--ie"), defaultValue = "false", description = Array("Ignore external call check warning"))
  var ignoreExternalCallCheckWarnings: Boolean = false

  @Option(names = Array("-w", "--warning"), defaultValue = "false", description = Array("Consider warnings as errors"))
  var warningAsError: Boolean = false

  def debug[O](values: O*): Unit = {
    if (debug) {
      values.foreach(pprint.pprintln(_))
    }
  }

  def error[T, O](msg: T, other: O): Int = {
    pprint.pprintln(s"error: \n $msg ")
    pprint.pprintln(other)
    -1
  }

  def warning[T, O](msg: T, other: O): Int = {
    pprint.pprintln(s"warning: \n $msg")
    pprint.pprintln(other)
    if (warningAsError) {
      -1
    } else {
      0
    }
  }

  def ok[T, O](msg: T, other: O): Int = {
    pprint.pprintln(msg)
    pprint.pprintln(other)
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
      ignoreReadonlyCheckWarnings = ignoreReadonlyCheckWarnings,
      ignoreUnusedPrivateFunctionsWarnings = ignoreUnusedPrivateFunctionsWarnings,
      ignoreExternalCallCheckWarnings = ignoreExternalCallCheckWarnings
    )
    debug(compilerOptions, s"warningAsError: $warningAsError", s"files: ${files.mkString(",")}")

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
