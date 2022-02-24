// SPDX-License-Identifier: Apache-2.0

package chisel3.experimental.hierarchy.core

// Wrapper Class
case class Context[+C <: IsContext](context: Option[C]) {
  def get = context.get
}

// Typeclass
trait Contexter[V, C <: IsContext]  {
  //type C
  type R = Context[C]
  def apply[P](value: V, hierarchy: Hierarchy[P]): R
}

// Default Typeclass Implementations
object Contexter {
  implicit def isHierarchical[V] = new Contexter[V, IsContext] {
    def apply[P](value: V, hierarchy: Hierarchy[P]): R = {
      hierarchy.proxy.lookupContext
    }
  }
}
