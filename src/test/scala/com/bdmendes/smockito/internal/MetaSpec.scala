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

    assert(!hasRejection("abortInvalidEntry(target.charAt))"))
    assert(!hasRejection("abortInvalidEntry(target.charAt(_: Int))"))
    assert(!hasRejection("abortInvalidEntry((pos: Int) => target.charAt(pos))"))
    assert(hasRejection("abortInvalidEntry(0)"))
    assert(hasRejection("abortInvalidEntry(() => true)"))
    assert(hasRejection("abortInvalidEntry((_: String) => true)"))

private object MetaSpec:
  val target: String = "Some string"

  private inline def abortInvalidEntry[T, F[_]](inline expr: Any): Unit =
    ${
      abortOnInvalidMethodSelection[T, F]('expr)
    }
