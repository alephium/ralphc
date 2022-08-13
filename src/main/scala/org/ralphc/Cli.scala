package org.ralphc

import java.util.concurrent.Callable
import picocli.CommandLine.{Command, Option}

@Command(name = "ralphc", mixinStandardHelpOptions = true, version = Array("ralphc 1.5.0-rc4"), description = Array("compiler ralph language."))
class Cli extends Callable[Int] {
  @Option(names = Array("-f"))
  val files: Array[String] = Array.empty

  @Option(names = Array("-d", "--debug"), defaultValue = "false", description = Array("debug mode"))
  var debug: Boolean = false

  def _print[O](other: O): Int = {
    if (debug) {
      pprint.pprintln(files)
      pprint.pprintln(other)
    }
    0
  }

  def error[O](msg: String, other: O): Int = {
    pprint.pprintln(s"error: \n $msg")
    _print(other)
    -1
  }

  def ok[O](msg: String, other: O): Int = {
    pprint.pprintln(msg)
    _print(other)
  }

  def print[O](msg: Either[String, String], other: O): Int = {
    msg.fold(ok(_, other), error(_, other))
  }

  override def call(): Int = {
    val rets = for {
      path <- files
      ret = Parser
        .Parser(path)
        .fold(
          deps => error("circular dependency：\n" + deps.mkString("\n"), path),
          value => value._1.fold(_ => Compiler.compileScript(value._2).fold(err => error(err.detail, value), ret => ok(ret.bytecodeTemplate, value)))
                                (_ => Compiler.compileContract(value._2).fold(err => error(err.detail, value), ret => ok(ret.bytecode, value)))
        )
    } yield ret
    rets.sum
  }
}
