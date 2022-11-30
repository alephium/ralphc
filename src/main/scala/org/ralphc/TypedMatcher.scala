package org.ralphc

object TypedMatcher {

  def matcher(input: String): Option[Struct[String]] = {
    val abstractContractMatcher = """[\S\s]*Abstract\s+Contract\s+([A-Z][a-zA-Z0-9]*)[\S\s]*""".r
    val contractMatcher         = """[\S\s]*Contract\s+([A-Z][a-zA-Z0-9]*)[\S\s]*""".r
    val interfaceMatcher        = """[\S\s]*Interface\s+([A-Z][a-zA-Z0-9]*)[\S\s]*""".r
    val scriptMatcher           = """[\S\s]*TxScript\s+([A-Z][a-zA-Z0-9]*)[\S\s]*""".r

    input match {
      case abstractContractMatcher(name) => Some(AbstractContract(name))
      case contractMatcher(name)         => Some(Contract(name))
      case interfaceMatcher(name)        => Some(Interface(name))
      case scriptMatcher(name)           => Some(Script(name))
      case _ => {
        pprint.pprintln(input)
        pprint.pprintln("no match")
        None
      }
    }
  }
}

sealed abstract class Struct[T] {
  def getName: T
}

final case class Contract[T](name: T) extends Struct[T] {
  override def getName: T = name
}

final case class AbstractContract[T](name: T) extends Struct[T] {
  override def getName: T = name
}

final case class Interface[T](name: T) extends Struct[T] {
  override def getName: T = name
}

final case class Script[T](name: T) extends Struct[T] {
  override def getName: T = name
}
