package com.bdmendes.smockito.internal

import Meta.*
import MetaSpec.*
import scala.reflect.ClassTag

class MetaSpec extends munit.FunSuite:

  test("map over tuples"):
    val f = [X] => (x: ClassTag[X]) ?=> x.runtimeClass.getSimpleName

    assert(mapTuple[(String, Int, Long), String](f).sameElements(Array("String", "int", "long")))

  test("retrieve matched method name"):
    inline def hasRejection(expr: String): Boolean =
      compileErrors(expr).contains("Expected selection of a mockable method")

    assertEquals(matchedMethodEntry[String, Id](target.charAt), "charAt")
    assertEquals(matchedMethodEntry[String, Id](target.charAt(_: Int)), "charAt")
    assertEquals(matchedMethodEntry[String, Id]((pos: Int) => target.charAt(pos)), "charAt")

    assert(hasRejection("matchedMethodEntry[String, Id](0)"))
    assert(hasRejection("matchedMethodEntry[Int, Id](target.charAt)"))
    assert(hasRejection("matchedMethodEntry[String, Id](() => true)"))
    assert(hasRejection("matchedMethodEntry[String, Id]((_: String) => true)"))

private object MetaSpec:
  opaque type Id[+T] <: T = T

  val target: Id[String] = "Some string"

  private inline def matchedMethodEntry[T, F[_]](inline expr: Any): String =
    ${
      matchedMethodName[T, F, Tuple1[?], Char]('expr)
    }
