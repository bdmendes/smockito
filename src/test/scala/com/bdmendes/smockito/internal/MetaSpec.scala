package com.bdmendes.smockito.internal

import MetaSpec.*
import meta.*
import scala.reflect.ClassTag

class MetaSpec extends munit.FunSuite:

  test("map over tuples"):
    val f = [X] => (x: ClassTag[X]) ?=> x.runtimeClass.getSimpleName

    assert(mapTuple[(String, Int, Long), String](f).sameElements(Array("String", "int", "long")))

  test("abort on invalid method selection"):
    inline def hasRejection(expr: String): Boolean =
      compileErrors(expr).contains("got unrelated expression")

    // The compiler can't see that `target` and `abortInvalidEntry` are being referenced below,
    // so force one of them to suppress the warning.
    val _ = target

    assert(!hasRejection("abortInvalidEntry[String, Id](target.charAt)"))
    assert(!hasRejection("abortInvalidEntry[String, Id](target.charAt(_: Int))"))
    assert(!hasRejection("abortInvalidEntry[String, Id]((pos: Int) => target.charAt(pos))"))
    assert(hasRejection("abortInvalidEntry[String, Id](0)"))
    assert(hasRejection("abortInvalidEntry[Int, Id](target.charAt)"))
    assert(hasRejection("abortInvalidEntry[String, Id](() => true)"))
    assert(hasRejection("abortInvalidEntry[String, Id]((_: String) => true)"))

private object MetaSpec:
  opaque type Id[+T] <: T = T

  val target: Id[String] = "Some string"

  private inline def abortInvalidEntry[T, F[_]](inline expr: Any): Unit =
    ${
      abortOnInvalidMethodSelection[T, F, Tuple1[?]]('expr)
    }
