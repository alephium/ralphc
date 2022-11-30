package org.ralphc

import org.alephium.api.model.CompileProjectResult
import org.alephium.protocol.Hash
import org.alephium.ralph.CompilerOptions
import org.alephium.util.AVector
import java.nio.file.Path

case class CodeInfo(
    sourceCodeHash: String,
    var bytecodeDebugPatch: CompileProjectResult.Patch,
    var codeHashDebug: Hash,
    var warnings: AVector[String]
)

case class Artifacts(compilerOptionsUsed: CompilerOptions, infos: Map[String, CodeInfo])

final case class MetaInfo(name: String, sourcePath: Path, ArtifactPath: Path, codeInfo: CodeInfo)
