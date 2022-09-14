package org.ralphc

import org.alephium.api.{Try, failed}
import org.alephium.api.model.{CompileContractResult, CompileScriptResult, CompileProjectResult}
import org.alephium.protocol.vm.lang.CompilerOptions
import org.alephium.protocol.vm.lang

object Compiler {
  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def compileScript(code: String, compilerOptions: CompilerOptions = CompilerOptions.Default): Try[CompileScriptResult] = {
    lang.Compiler
      .compileTxScriptFull(code, compilerOptions = compilerOptions)
      .map(p => CompileScriptResult.from(p._1, p._2, p._3))
      .left
      .map(error => failed(error.toString))
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def compileContract(code: String, compilerOptions: CompilerOptions = CompilerOptions.Default): Try[CompileContractResult] = {
    lang.Compiler
      .compileContractFull(code, compilerOptions = compilerOptions)
      .map(p => CompileContractResult.from(p._1, p._2, p._3))
      .left
      .map(error => failed(error.toString))
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def compileProject(code: String, compilerOptions: CompilerOptions = CompilerOptions.Default): Try[CompileProjectResult] = {
    lang.Compiler
      .compileProject(code, compilerOptions = compilerOptions)
      .map(p => CompileProjectResult.from(p._1, p._2))
      .left
      .map(error => failed(error.toString))
  }
}
