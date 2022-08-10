package org.ralphc

import scala.io.Source
import scala.util.Using
import java.util.concurrent.Callable
import picocli.CommandLine.{Command, Option, Parameters}

@Command(name = "ralphc", mixinStandardHelpOptions = true, version = Array("ralphc 1.4.4"), description = Array("compiler ralph language."))
class Cli extends Callable[Unit] {
  @Option(names = Array("-f"))
  val files: Array[String] = null

  @Option(names = Array("-t", "--type"), description = Array("compile type; 1:script, 2:contract, 3: assert"))
  var ty = 1

  @Option(names = Array("-d", "--debug"), defaultValue = "false", description = Array("debug mode"))
  var debug: Boolean = false

  def _debug(): Unit = {
    if (debug) {
      pprint.pprintln(ty)
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

  override def call(): Unit = {
    if (files != null) {
      val codes = files.fold("")((code, e) =>
        if (e.nonEmpty) {
          Using(Source.fromFile(e)) { source => source.mkString + code }.get
        } else {
          code
        }
      )
      ty match {
        case 1 =>
          Compiler.compileScript(codes).left.map(err => error(err.detail)).map(ret => ok(ret.bytecodeTemplate))
        case 2 =>
          Compiler.compileContract(codes).left.map(err => error(err.detail)).map(ret => ok(ret.bytecode))
        case _ => pprint.pprintln("type option error!")
      }
    } else {
      pprint.pprintln("no file")
    }
  }
}
