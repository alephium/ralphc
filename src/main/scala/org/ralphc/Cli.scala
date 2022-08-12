package org.ralphc

import java.util.concurrent.Callable
import picocli.CommandLine.{Command, Option}

@Command(name = "ralphc", mixinStandardHelpOptions = true, version = Array("ralphc 1.5.0-rc4"), description = Array("compiler ralph language."))
class Cli extends Callable[Unit] {
  @Option(names = Array("-f"))
  val files: Array[String] = Array.empty

  @Option(names = Array("-d", "--debug"), defaultValue = "false", description = Array("debug mode"))
  var debug: Boolean = false

  def _debug(): Unit = {
    if (debug) {
      pprint.pprintln(files)
    }
  }

  def error(msg: String): Unit = {
    _debug()
    pprint.pprintln(msg)
  }

  def ok(msg: String): Unit = {
    _debug()
    pprint.pprintln(msg)
  }

  def print[M, O](msg: M, other: O): Unit = {
    if (debug) {
      pprint.pprintln(files)
      pprint.pprintln(other)
    }
    pprint.pprintln(msg)
  }

  override def call(): Unit = {

    files.foreach(path => {
      Parser
        .Parser(path)
        .fold(
          deps => error(deps.mkString("|")),
          value =>
            value._1 match {
              case 1 =>
                Compiler.compileScript(value._2).fold(err => print(err.detail, value), (ret => print(ret.bytecodeTemplate, value)))
              case 2 =>
                Compiler.compileContract(value._2).fold(err => print(err.detail, value), (ret => print(ret.bytecode, value)))
              case _ => pprint.pprintln("type option error!")
            }
        )
    })
  }
}
