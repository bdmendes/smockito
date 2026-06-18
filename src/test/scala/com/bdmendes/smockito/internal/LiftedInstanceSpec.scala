package com.bdmendes.smockito.internal

class LiftedInstanceSpec extends munit.FunSuite:

  // For conciseness.
  type D = Int

  // scalafmt: { maxColumn = 240 }

  test("invoke the real function for 0 arguments"):
    val f = LiftedInstance(() => 1)
    assertEquals(f(), 1)

  test("invoke the real function for 1 argument"):
    val f = LiftedInstance((x: D) => x + 1)
    assertEquals(f(1), 2)

  test("invoke the real function for 2 arguments"):
    val f = LiftedInstance((x1: D, x2: D) => x1 + x2)
    assertEquals(f(1, 2), 3)
    assertEquals(f.tupled((1, 2)), 3)

  test("invoke the real function for 3 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D) => x1 + x2 + x3)
    assertEquals(f(1, 2, 3), 6)
    assertEquals(f.tupled((1, 2, 3)), 6)

  test("invoke the real function for 4 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D) => x1 + x2 + x3 + x4)
    assertEquals(f(1, 2, 3, 4), 10)
    assertEquals(f.tupled((1, 2, 3, 4)), 10)

  test("invoke the real function for 5 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D) => x1 + x2 + x3 + x4 + x5)
    assertEquals(f(1, 2, 3, 4, 5), 15)
    assertEquals(f.tupled((1, 2, 3, 4, 5)), 15)

  test("invoke the real function for 6 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D) => x1 + x2 + x3 + x4 + x5 + x6)
    assertEquals(f(1, 2, 3, 4, 5, 6), 21)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6)), 21)

  test("invoke the real function for 7 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7)
    assertEquals(f(1, 2, 3, 4, 5, 6, 7), 28)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7)), 28)

  test("invoke the real function for 8 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8)
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8), 36)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8)), 36)

  test("invoke the real function for 9 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9)
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9), 45)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9)), 45)

  test("invoke the real function for 10 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10)
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 55)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10)), 55)

  test("invoke the real function for 11 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11)
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), 66)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)), 66)

  test("invoke the real function for 12 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12)
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), 78)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)), 78)

  test("invoke the real function for 13 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D, x13: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13)
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), 91)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)), 91)

  test("invoke the real function for 14 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D, x13: D, x14: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14)
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14), 105)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)), 105)

  test("invoke the real function for 15 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D, x13: D, x14: D, x15: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15)
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), 120)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)), 120)

  test("invoke the real function for 16 arguments"):
    val f = LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D, x13: D, x14: D, x15: D, x16: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16)
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), 136)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)), 136)

  test("invoke the real function for 17 arguments"):
    val f =
      LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D, x13: D, x14: D, x15: D, x16: D, x17: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17
      )
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17), 153)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17)), 153)

  test("invoke the real function for 18 arguments"):
    val f =
      LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D, x13: D, x14: D, x15: D, x16: D, x17: D, x18: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 + x18
      )
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18), 171)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18)), 171)

  test("invoke the real function for 19 arguments"):
    val f =
      LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D, x13: D, x14: D, x15: D, x16: D, x17: D, x18: D, x19: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 + x18 + x19
      )
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19), 190)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19)), 190)

  test("invoke the real function for 20 arguments"):
    val f =
      LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D, x13: D, x14: D, x15: D, x16: D, x17: D, x18: D, x19: D, x20: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 + x18 + x19 + x20
      )
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20), 210)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)), 210)

  test("invoke the real function for 21 arguments"):
    val f =
      LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D, x13: D, x14: D, x15: D, x16: D, x17: D, x18: D, x19: D, x20: D, x21: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 + x18 + x19 + x20 + x21
      )
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21), 231)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21)), 231)

  test("invoke the real function for 22 arguments"):
    val f =
      LiftedInstance((x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D, x13: D, x14: D, x15: D, x16: D, x17: D, x18: D, x19: D, x20: D, x21: D, x22: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 + x18 + x19 + x20 + x21 + x22
      )
    assertEquals(f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22), 253)
    assertEquals(f.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)), 253)

  test("not corrupt the instance if it extends Function"):
    class Wrapper extends Function0[String]:
      def foo = "foo"
      def apply(): String = "hello"

    val wrapper = LiftedInstance[Wrapper](new Wrapper)
    assertEquals(wrapper(), "hello")
    assertEquals(wrapper.foo, "foo")
