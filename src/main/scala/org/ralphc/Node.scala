package org.ralphc

import scala.collection.mutable

case class Node(path: String, compile: Compile[String], deps: Option[mutable.Set[String]])

case class LightNode(code: Compile[String], var status: Option[Unit])

sealed abstract class Compile[A] {
  def get: A

  @inline final def map[C](script: A => C, contract: A => C, mix: A => C): Compile[C] = this match {
    case Script(s)   => Script(script(s))
    case Contract(c) => Contract(contract(c))
    case Mix(c)      => Mix(mix(c))
  }

  @inline final def fold[C](fs: A => C)(fc: A => C)(fm: A => C): C = this match {
    case Script(s)   => fs(s)
    case Contract(c) => fc(c)
    case Mix(c)      => fm(c)
  }
}

final case class Script[A](code: A) extends Compile[A] {
  def get: A = code
}

final case class Contract[A](code: A) extends Compile[A] {
  def get: A = code
}

final case class Mix[A](code: A) extends Compile[A] {
  def get: A = code
}
