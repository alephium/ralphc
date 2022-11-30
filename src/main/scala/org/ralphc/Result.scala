package org.ralphc

import org.alephium.api.model.CompileResult
import org.alephium.protocol.Hash
import org.alephium.util.AVector

final case class ScriptResult(
    version: String,
    name: String,
    bytecodeTemplate: String,
    fieldsSig: CompileResult.FieldsSig,
    functions: AVector[CompileResult.FunctionSig]
) extends CompileResult.Versioned

final case class ContractResult(
    version: String,
    name: String,
    bytecode: String,
    codeHash: Hash,
    fieldsSig: CompileResult.FieldsSig,
    eventsSig: AVector[CompileResult.EventSig],
    functions: AVector[CompileResult.FunctionSig]
) extends CompileResult.Versioned
