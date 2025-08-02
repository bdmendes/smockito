package com.bdmendes.smockito.internal

import _root_.com.bdmendes.smockito.internal.meta.mapTuple
import scala.reflect.ClassTag

class MetaSpec extends munit.FunSuite:

  test("map over tuples"):
    val f = [X] => (x: ClassTag[X]) ?=> x.runtimeClass.getSimpleName

    assert(mapTuple[(String, Int, Long), String](f).sameElements(Array("String", "int", "long")))
