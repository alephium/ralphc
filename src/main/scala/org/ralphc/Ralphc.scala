package org.ralphc

import picocli.CommandLine

object Main extends App {
  System.exit(new CommandLine(new Cli).execute(args: _*))
}
