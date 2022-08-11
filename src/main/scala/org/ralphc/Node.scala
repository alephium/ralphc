package org.ralphc

import scala.collection.mutable

class Node(val path: String) {
  var ty                        = 1
  var code                      = ""
  var Deps: mutable.Set[String] = mutable.Set.empty
}

case class LightNode(path: String, code: String, var status: Int)
