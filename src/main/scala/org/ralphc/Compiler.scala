package org.ralphc

import org.alephium.api.{Try, failed}
import org.alephium.api.model.{CompileContractResult, CompileScriptResult}
import org.alephium.protocol.vm.lang

object Compiler {
  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def compileScript(code: String): Try[CompileScriptResult] = {
    lang.Compiler
      .compileTxScriptFull(code)
      .map(p => CompileScriptResult.from(p._1, p._2))
      .left
      .map(error => failed(error.toString))
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def compileContract(code: String): Try[CompileContractResult] = {
    lang.Compiler
      .compileContractFull(code)
      .map(p => CompileContractResult.from(p._1, p._2))
      .left
      .map(error => failed(error.toString))
  }
}
