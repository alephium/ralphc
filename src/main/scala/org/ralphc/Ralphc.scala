package org.ralphc

import picocli.CommandLine

object Main extends App {
  val cli = new CommandLine(new Cli)
  if (args.isEmpty) {
    System.exit(cli.execute("-h"))
  } else {
    System.exit(cli.execute(args: _*))
  }
}
