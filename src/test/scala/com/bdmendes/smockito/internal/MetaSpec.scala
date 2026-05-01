package com.bdmendes.smockito.internal

import meta.*
import scala.reflect.ClassTag

class MetaSpec extends munit.FunSuite:

  test("map over tuples"):
    val f = [X] => (x: ClassTag[X]) ?=> x.runtimeClass.getSimpleName

    assert(mapTuple[(String, Int, Long), String](f).sameElements(Array("String", "int", "long")))
