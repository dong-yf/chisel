// SPDX-License-Identifier: Apache-2.0

package chisel3.experimental.hierarchy.core
import scala.collection.mutable.HashMap
import scala.reflect.runtime.universe.TypeTag
import scala.language.experimental.macros
import chisel3.internal.sourceinfo.{DefinitionTransform, InstanceTransform}
import java.util.IdentityHashMap

/** Represents a view of a proto from a specific hierarchical path */
trait Wrapper[+P] {
  //def _lookup[B](name: String): Any => Any = that.asInstanceOf[Any => Any]

  /** Used by Chisel's internal macros. DO NOT USE in your normal Chisel code!!!
    * Instead, mark the field you are accessing with [[@public]]
    *
    * Given a selector function (that) which selects a member from the original, return the
    *   corresponding member from the hierarchy.
    *
    * Our @instantiable and @public macros generate the calls to this apply method
    *
    * By calling this function, we summon the proper Lookupable typeclass from our implicit scope.
    *
    * @param that a user-specified lookup function
    * @param lookup typeclass which contains the correct lookup function, based on the types of A and B
    * @param macroGenerated a value created in the macro, to make it harder for users to use this API
    */
  def _lookup[B](
    that: P => B
  )(
    implicit lookupable: Lookupable[B],
    macroGenerated:      chisel3.internal.MacroGenerated
  ): lookupable.H = {
    // TODO: Call to 'that' should be replaced with shapeless to enable deserialized Underlying
    val protoValue = that(proto)
    //println(s"$proxy(${proxy.identity}): protoValue=$protoValue, visited=${proxy.visited(protoValue)}: ")
    val cachedValue = proxy.retrieveMe(protoValue)
    cachedValue match {
      case None if proxy.nVisits(protoValue) > 1 => // Second time around cycle is done, error!
        val debug = protoValue match {
          case w: Wrapper[_] => w.proxy.debug
          case other => proxy.debug + s"/$other"
        }
        throw new Exception(s"Cycle! $debug")
      case None if proxy.nVisits(protoValue) > 0 => // A cycle is found, go around again to print debug info
        val debug = protoValue match {
          case w: Wrapper[_] => w.proxy.debug
          case other => proxy.debug + s"/$other"
        }
        println(s"Cycle! $debug")
      case other => other
    }
    cachedValue match {
      case None =>
        //println(s"- $proxy(${proxy.identity}): missedCache")
        proxy.visit(protoValue)
        val retValue = lookupable.apply(this, protoValue)
        proxy.cacheMe(protoValue, retValue)
        //println(s"- $proxy(${proxy.identity}): returned with $retValue")
        retValue
      case Some(x: lookupable.H) =>
        //println(s"- $proxy(${proxy.identity}): cache hit with $x")
        x
    }
  }
  def debug = proxy.debug
  def identity = proxy.identity

  //def query(path: String) = proxy.query(path)

  /*
  def _nameLookup[B](
    name: String
  )(
    implicit lookupable: Lookupable[B],
    macroGenerated:      chisel3.internal.MacroGenerated
  ): lookupable.H = {
    // TODO: Call to 'that' should be replaced with shapeless to enable deserialized Underlying
    val protoValue = that(proto)
    proxy.retrieveMeAsWrapper(protoValue).orElse(proxy.retrieveMe(protoValue)).orElse {
      val retValue = lookupable.apply(this, protoValue)
      proxy.cacheMe(protoValue, retValue)
      Some(retValue)
    }.get.asInstanceOf[lookupable.H]
  }
   */

  /** Useful to view underlying proxy as another type it is representing
    *
    * @return proxy with a different type
    */
  def proxyAs[T]: Proxy[P] with T = proxy.asInstanceOf[Proxy[P] with T]

  /** Determine whether proxy proto is of type provided.
    *
    * @note IMPORTANT: this function requires summoning a TypeTag[B], which will fail if B is an inner class.
    * @note IMPORTANT: this function IGNORES type parameters, akin to normal type erasure.
    * @note IMPORTANT: this function relies on Java reflection for proxy proto, but Scala reflection for provided type
    *
    * E.g. isA[List[Int]] will return true, even if proxy proto is of type List[String]
    * @return Whether proxy proto is of provided type (with caveats outlined above)
    */
  def isA[B: TypeTag]: Boolean = {
    val tptag = implicitly[TypeTag[B]]
    // drop any type information for the comparison, because the proto will not have that information.
    val name = tptag.tpe.toString.takeWhile(_ != '[')
    //println("Start")
    val x = superClasses.contains(name)
    //println("End")
    x
  }

  /** @return Return the proxy Definition[P] of this Hierarchy[P] */
  //def toSetter: Setter[P] = proxy.toSetter

  /** @return Underlying proxy representing a proto in viewed from a hierarchical path */
  private[chisel3] def proxy: Proxy[P]

  /** @return Underlying proto, which is the actual underlying object we are representing */
  private[chisel3] def proto: P = proxy.proto

  private lazy val superClasses = Wrapper.calculateSuperClasses(proto.getClass())
}

object Wrapper {

  // This code handles a special-case where, within an mdoc context, the type returned from
  //  scala reflection (typetag) looks different than when returned from java reflection.
  //  This function detects this case and reshapes the string to match.
  private def modifyReplString(clz: String): String = {
    if (clz != null) {
      clz.split('.').toList match {
        case "repl" :: "MdocSession" :: app :: rest => s"$app.this." + rest.mkString(".")
        case other                                  => clz
      }
    } else clz
  }

  // Nested objects stick a '$' at the end of the object name, but this does not show up in the scala reflection type string
  // E.g.
  // object Foo {
  //   object Bar {
  //     class Baz()
  //   }
  // }
  // Scala type will be Foo.Bar.Baz
  // Java type will be Foo.Bar$.Baz
  private def modifyNestedObjects(clz: String): String = {
    if (clz != null) { clz.replace("$", "") }
    else clz
  }

  private def calculateSuperClasses(clz: Class[_]): Set[String] = {
    //println(clz)
    if (clz != null) {
      Set(modifyNestedObjects(modifyReplString(clz.getCanonicalName()))) ++
        clz.getInterfaces().flatMap(i => calculateSuperClasses(i)) ++
        calculateSuperClasses(clz.getSuperclass())
    } else {
      Set.empty[String]
    }
  }
}
