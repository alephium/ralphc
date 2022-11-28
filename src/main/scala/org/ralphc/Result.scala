package org.ralphc

import org.alephium.api.model.{CompileContractResult, CompileProjectResult, CompileResult, CompileScriptResult}
import org.alephium.protocol.Hash
import org.alephium.util.AVector

final case class CompileScriptResultSig(
    version: String,
    name: String,
    bytecodeTemplate: String,
    bytecodeDebugPatch: CompileProjectResult.Patch,
    fieldsSig: CompileResult.FieldsSig,
    functionsSig: AVector[CompileResult.FunctionSig],
    warnings: AVector[String]
) extends CompileResult.Versioned

final case class CompileContractResultSig(
    version: String,
    name: String,
    bytecode: String,
    bytecodeDebugPatch: CompileProjectResult.Patch,
    codeHash: Hash,
    codeHashDebug: Hash,
    fieldsSig: CompileResult.FieldsSig,
    functionsSig: AVector[CompileResult.FunctionSig],
    eventsSig: AVector[CompileResult.EventSig],
    warnings: AVector[String]
) extends CompileResult.Versioned

final case class CompileProjectResultSig(
    contracts: AVector[CompileContractResultSig],
    scripts: AVector[CompileScriptResultSig]
)
