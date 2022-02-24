package chisel3.experimental.hierarchy.core

/*
// Marker Trait
trait IsLensable

// Wrapper Class
final case class Lense[T](proxy: Proxy[T, IsContext])

// Underlying Classes; For now, just use IsContext's until proven otherwise

// Typeclass Trait
trait Lensify[B]  {
  type R
  def apply[P](value: V, hierarchy: Lense[A]): R
}

object Lensify {
  // I believe lensing on isLookupable should be illegal; don't define this?!?
  implicit def isLookupable[L <: IsLookupable] = new Lensify[L] {
    type R = L
    def apply[C](b: L, hierarchy: Lense[C]): L = ??? //ERROR!! Cannot do this!!
  }
  implicit def IsContext[I <: IsContext] = new Lensify[Hierarchy[I]] {
    type R = Lense[I]
    def apply[C](b: Hierarchy[I], hierarchy: Lense[C]): Lense[I] = Lense(b.proxy)
  }
}
*/



//TODO: will need to handle lensing nested Contextuals with another typeclass
// TODO: nested contextuals
//final case class ContextualLense[T, V](value: V, parent: Proxy[T, IsContext])
  // TODO: nested contextuals
  //implicit def isContextual[I <: IsContextual] = new Lensify[Contextual[I], Edit[I]] {
  //  def lensify[C](b: Contextual[I], hierarchy: Lense[C]): Edit[I] = Edit(b.value)
  //}