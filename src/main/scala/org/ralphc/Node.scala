package org.ralphc

import scala.collection.mutable

case class Node(path: String, compile: Compile[String], deps: Option[mutable.Set[String]])

case class LightNode(code: Compile[String], var status: Option[Unit])

sealed abstract class Compile[A] {
  def get: A

  def map[C](script: A => C, contract: A => C): C = this match {
    case Script(s)   => script(s)
    case Contract(c) => contract(c)
  }
}

final case class Script[A](code: A) extends Compile[A] {
  def get: A = code
}

final case class Contract[A](code: A) extends Compile[A] {
  def get: A = code
}
