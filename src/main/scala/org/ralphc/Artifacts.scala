package org.ralphc

import org.alephium.api.model.CompileProjectResult
import org.alephium.protocol.Hash
import org.alephium.ralph.CompilerOptions
import org.alephium.util.AVector

case class CodeInfo(
    sourceCodeHash: String,
    var bytecodeDebugPatch: CompileProjectResult.Patch,
    var codeHashDebug: Hash,
    var warnings: AVector[String]
)

final case class Artifacts(option: CompilerOptions, infos: Map[String, CodeInfo])
