package com.bdmendes.smockito

class MockedMethodSpec extends munit.FunSuite:

  test("spawn a MockedMethod for all arities until 22"):
    // scalafmt: { maxColumn = 150 }
    type D = Int
    val _: MockedMethod[EmptyTuple, D] = () => 1
    val _: MockedMethod[Tuple1[D], D] = (_: D) => 1
    val _: MockedMethod[(D, D), D] = (_: D, _: D) => 1
    val _: MockedMethod[(D, D, D), D] = (_: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D), D] = (_: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D), D] = (_: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D), D] = (_: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D), D] = (_: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D), D] = (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D), D] = (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D), D] = (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D), D] = (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D), D] = (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
    val _: MockedMethod[(D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, D), D] =
      (_: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D, _: D) => 1
