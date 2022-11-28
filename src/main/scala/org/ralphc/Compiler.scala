package org.ralphc

import org.alephium.api.{Try, failed}
import org.alephium.api.model.{CompileContractResult, CompileScriptResult, CompileProjectResult}
import org.alephium.ralph.CompilerOptions
import org.alephium.ralph

object Compiler {
  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def compileScript(code: String, compilerOptions: CompilerOptions = CompilerOptions.Default): Try[CompileScriptResult] = {
    ralph.Compiler
      .compileTxScriptFull(code, compilerOptions = compilerOptions)
      .map(CompileScriptResult.from)
      .left
      .map(error => failed(error.toString))
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def compileContract(code: String, compilerOptions: CompilerOptions = CompilerOptions.Default): Try[CompileContractResult] = {
    ralph.Compiler
      .compileContractFull(code, compilerOptions = compilerOptions)
      .map(CompileContractResult.from)
      .left
      .map(error => failed(error.toString))
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
