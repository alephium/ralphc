package org.ralphc

import org.alephium.ralph.CompilerOptions

class CodeInfo(sourceCodeHash: String, bytecodeDebugPatch: String, codeHashDebug: String, warnings: Array[String])

case class Artifacts(option: CompilerOptions, infos: Map[String, CodeInfo], artifactFileName: ".project.json")

//contracts
//artifacts
