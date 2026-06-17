package com.bdmendes.smockito

class MockedMethodSpec extends munit.FunSuite:

  // For conciseness.
  type D = Int

  test("invoke the real method for 0 arguments"):
    val method: MockedMethod[EmptyTuple, D] = () => 1
    assertEquals(method.packed(()), 1)
    assertEquals(method.tupled(EmptyTuple), 1)

  test("invoke the real method for 1 argument"):
    val method: MockedMethod[Tuple1[D], D] = (x: D) => x + 1
    assertEquals(method.packed(1), 2)
    assertEquals(method.tupled(Tuple1(1)), 2)

  test("invoke the real method for 2 arguments"):
    val method: MockedMethod[(D, D), D] = (x: D, y: D) => x + y
    assertEquals(method.packed((1, 2)), 3)
    assertEquals(method.tupled((1, 2)), 3)

  test("invoke the real method for 3 arguments"):
    val method: MockedMethod[(D, D, D), D] = (x1: D, x2: D, x3: D) => x1 + x2 + x3
    assertEquals(method.packed((1, 2, 3)), 6)
    assertEquals(method.tupled((1, 2, 3)), 6)

  test("invoke the real method for 4 arguments"):
    val method: MockedMethod[(D, D, D, D), D] = (x1: D, x2: D, x3: D, x4: D) => x1 + x2 + x3 + x4
    assertEquals(method.packed((1, 2, 3, 4)), 10)
    assertEquals(method.tupled((1, 2, 3, 4)), 10)

  test("invoke the real method for 5 arguments"):
    val method: MockedMethod[(D, D, D, D, D), D] =
      (x1: D, x2: D, x3: D, x4: D, x5: D) => x1 + x2 + x3 + x4 + x5
    assertEquals(method.packed((1, 2, 3, 4, 5)), 15)
    assertEquals(method.tupled((1, 2, 3, 4, 5)), 15)

  test("invoke the real method for 6 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D), D] =
      (x1: D, x2: D, x3: D, x4: D, x5: D, x6: D) => x1 + x2 + x3 + x4 + x5 + x6
    assertEquals(method.packed((1, 2, 3, 4, 5, 6)), 21)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6)), 21)

  test("invoke the real method for 7 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D), D] =
      (x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D) => x1 + x2 + x3 + x4 + x5 + x6 + x7
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7)), 28)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7)), 28)

  test("invoke the real method for 8 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D), D] =
      (x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7, 8)), 36)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7, 8)), 36)

  test("invoke the real method for 9 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D), D] =
      (x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9)), 45)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9)), 45)

  test("invoke the real method for 10 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D), D] =
      (x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10)), 55)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10)), 55)

  test("invoke the real method for 11 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D), D] =
      (x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)), 66)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)), 66)

  test("invoke the real method for 12 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (x1: D, x2: D, x3: D, x4: D, x5: D, x6: D, x7: D, x8: D, x9: D, x10: D, x11: D, x12: D) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)), 78)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)), 78)

  test("invoke the real method for 13 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (
          x1: D,
          x2: D,
          x3: D,
          x4: D,
          x5: D,
          x6: D,
          x7: D,
          x8: D,
          x9: D,
          x10: D,
          x11: D,
          x12: D,
          x13: D
      ) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)), 91)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)), 91)

  test("invoke the real method for 14 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (
          x1: D,
          x2: D,
          x3: D,
          x4: D,
          x5: D,
          x6: D,
          x7: D,
          x8: D,
          x9: D,
          x10: D,
          x11: D,
          x12: D,
          x13: D,
          x14: D
      ) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)), 105)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)), 105)

  test("invoke the real method for 15 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (
          x1: D,
          x2: D,
          x3: D,
          x4: D,
          x5: D,
          x6: D,
          x7: D,
          x8: D,
          x9: D,
          x10: D,
          x11: D,
          x12: D,
          x13: D,
          x14: D,
          x15: D
      ) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)), 120)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)), 120)

  test("invoke the real method for 16 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (
          x1: D,
          x2: D,
          x3: D,
          x4: D,
          x5: D,
          x6: D,
          x7: D,
          x8: D,
          x9: D,
          x10: D,
          x11: D,
          x12: D,
          x13: D,
          x14: D,
          x15: D,
          x16: D
      ) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)), 136)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)), 136)

  test("invoke the real method for 17 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (
          x1: D,
          x2: D,
          x3: D,
          x4: D,
          x5: D,
          x6: D,
          x7: D,
          x8: D,
          x9: D,
          x10: D,
          x11: D,
          x12: D,
          x13: D,
          x14: D,
          x15: D,
          x16: D,
          x17: D
      ) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17
    assertEquals(method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17)), 153)
    assertEquals(method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17)), 153)

  test("invoke the real method for 18 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (
          x1: D,
          x2: D,
          x3: D,
          x4: D,
          x5: D,
          x6: D,
          x7: D,
          x8: D,
          x9: D,
          x10: D,
          x11: D,
          x12: D,
          x13: D,
          x14: D,
          x15: D,
          x16: D,
          x17: D,
          x18: D
      ) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 +
          x18
    assertEquals(
      method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18)),
      171
    )
    assertEquals(
      method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18)),
      171
    )

  test("invoke the real method for 19 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (
          x1: D,
          x2: D,
          x3: D,
          x4: D,
          x5: D,
          x6: D,
          x7: D,
          x8: D,
          x9: D,
          x10: D,
          x11: D,
          x12: D,
          x13: D,
          x14: D,
          x15: D,
          x16: D,
          x17: D,
          x18: D,
          x19: D
      ) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 +
          x18 + x19
    assertEquals(
      method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19)),
      190
    )
    assertEquals(
      method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19)),
      190
    )

  test("invoke the real method for 20 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (
          x1: D,
          x2: D,
          x3: D,
          x4: D,
          x5: D,
          x6: D,
          x7: D,
          x8: D,
          x9: D,
          x10: D,
          x11: D,
          x12: D,
          x13: D,
          x14: D,
          x15: D,
          x16: D,
          x17: D,
          x18: D,
          x19: D,
          x20: D
      ) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 +
          x18 + x19 + x20
    assertEquals(
      method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)),
      210
    )
    assertEquals(
      method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)),
      210
    )

  test("invoke the real method for 21 arguments"):
    val method: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (
          x1: D,
          x2: D,
          x3: D,
          x4: D,
          x5: D,
          x6: D,
          x7: D,
          x8: D,
          x9: D,
          x10: D,
          x11: D,
          x12: D,
          x13: D,
          x14: D,
          x15: D,
          x16: D,
          x17: D,
          x18: D,
          x19: D,
          x20: D,
          x21: D
      ) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 +
          x18 + x19 + x20 + x21
    assertEquals(
      method.packed((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21)),
      231
    )
    assertEquals(
      method.tupled((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21)),
      231
    )

  test("invoke the real method for 22 arguments"):
    val method
        : MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (
          x1: D,
          x2: D,
          x3: D,
          x4: D,
          x5: D,
          x6: D,
          x7: D,
          x8: D,
          x9: D,
          x10: D,
          x11: D,
          x12: D,
          x13: D,
          x14: D,
          x15: D,
          x16: D,
          x17: D,
          x18: D,
          x19: D,
          x20: D,
          x21: D,
          x22: D
      ) =>
        x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 +
          x18 + x19 + x20 + x21 + x22
    assertEquals(
      method.packed(
        (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)
      ),
      253
    )
    assertEquals(
      method.tupled(
        (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)
      ),
      253
    )
