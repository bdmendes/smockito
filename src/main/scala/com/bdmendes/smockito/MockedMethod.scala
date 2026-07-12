package com.bdmendes.smockito

/** The internal representation of a method to mock. The compiler synthesizes conversions from
  * regular function types to this type, for up to 22 parameters, via implicit conversions.
  */
into opaque type MockedMethod[A <: Tuple, R] = Pack[A] => R

extension [A <: Tuple, R](mockedMethod: MockedMethod[A, R])
  private inline def tupled: A => R = (args: A) => mockedMethod(pack(args))
  private inline def packed: Pack[A] => R = mockedMethod

object MockedMethod:
  // We may use `TupledFunction` from the standard library once it goes stable. See
  // https://docs.scala-lang.org/scala3/reference/experimental/tupled-function.html

  // scalafmt: { maxColumn = 240 }

  given conv00[R]: Conversion[() => R, MockedMethod[EmptyTuple, R]] = f => (_: Unit) => f()

  given conv01[A, R]: Conversion[A => R, MockedMethod[Tuple1[A], R]] = f => t => f(t)

  given conv02[A1, A2, R]: Conversion[(A1, A2) => R, MockedMethod[(A1, A2), R]] = f => t => f(t._1, t._2)

  given conv03[A1, A2, A3, R]: Conversion[(A1, A2, A3) => R, MockedMethod[(A1, A2, A3), R]] = f => t => f(t._1, t._2, t._3)

  given conv04[A1, A2, A3, A4, R]: Conversion[(A1, A2, A3, A4) => R, MockedMethod[(A1, A2, A3, A4), R]] = f => t => f(t._1, t._2, t._3, t._4)

  given conv05[A1, A2, A3, A4, A5, R]: Conversion[(A1, A2, A3, A4, A5) => R, MockedMethod[(A1, A2, A3, A4, A5), R]] = f => t => f(t._1, t._2, t._3, t._4, t._5)

  given conv06[A1, A2, A3, A4, A5, A6, R]: Conversion[(A1, A2, A3, A4, A5, A6) => R, MockedMethod[(A1, A2, A3, A4, A5, A6), R]] = f => t => f(t._1, t._2, t._3, t._4, t._5, t._6)

  given conv07[A1, A2, A3, A4, A5, A6, A7, R]: Conversion[(A1, A2, A3, A4, A5, A6, A7) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7), R]] = f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7)

  given conv08[A1, A2, A3, A4, A5, A6, A7, A8, R]: Conversion[(A1, A2, A3, A4, A5, A6, A7, A8) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8), R]] = f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8)

  given conv09[A1, A2, A3, A4, A5, A6, A7, A8, A9, R]: Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9), R]] = f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9)

  given conv10[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R]: Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10), R]] =
    f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10)

  given conv11[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R]: Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11), R]] =
    f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11)

  given conv12[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R]: Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12), R]] =
    f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12)

  given conv13[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R]: Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13), R]] =
    f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13)

  given conv14[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, R]: Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14), R]] =
    f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14)

  given conv15[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15), R]] =
    f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15)

  given conv16[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16), R]] =
    f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16)

  given conv17[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17), R]] =
    f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17)

  given conv18[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18), R]
  ] = f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18)

  given conv19[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19), R]
  ] = f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19)

  given conv20[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20), R]
  ] = f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19, t._20)

  given conv21[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21), R]
  ] = f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19, t._20, t._21)

  given conv22[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22), R]
  ] = f => t => f(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19, t._20, t._21, t._22)
