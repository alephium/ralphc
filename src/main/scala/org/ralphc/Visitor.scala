package org.ralphc

import org.alephium.antlr4.ralph.{RalphParser, RalphParserBaseVisitor}
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

class Visitor() extends RalphParserBaseVisitor[Array[String]] {
  override def visitSourceFile(ctx: RalphParser.SourceFileContext): Array[String] = {
    val items = ctx.assetScript().map(_.IDENTIFIER().getSymbol.getText) ++ ctx.contract().map(_.IDENTIFIER().getSymbol.getText) ++
      ctx.txScript().map(_.IDENTIFIER().getSymbol.getText) ++ ctx.interface_().map(_.INTERFACE().getSymbol.getText)
    items.toArray
  }
}
