package com.bdmendes.smockito

/** The internal representation of a method to mock.
  */
opaque type MockedMethod[A <: Tuple, R] = Pack[A] => R

extension [A <: Tuple, R](mockedMethod: MockedMethod[A, R])
  def tupled: A => R = (args: A) => mockedMethod(pack(args))
  def packed: Pack[A] => R = mockedMethod

object MockedMethod:
  // We may use `TupledFunction` from the standard library once it goes stable. See
  // https://docs.scala-lang.org/scala3/reference/experimental/tupled-function.html

  // scalafmt: { maxColumn = 150 }

  given conv00[R]: Conversion[() => R, MockedMethod[EmptyTuple, R]] = f => (_: Unit) => f()

  given conv01[A, R]: Conversion[A => R, MockedMethod[Tuple1[A], R]] = f => (arg: A) => f(arg)

  given conv02[A1, A2, R]: Conversion[(A1, A2) => R, MockedMethod[(A1, A2), R]] = f => f.tupled

  given conv03[A1, A2, A3, R]: Conversion[(A1, A2, A3) => R, MockedMethod[(A1, A2, A3), R]] = f => f.tupled

  given conv04[A1, A2, A3, A4, R]: Conversion[(A1, A2, A3, A4) => R, MockedMethod[(A1, A2, A3, A4), R]] = f => f.tupled

  given conv05[A1, A2, A3, A4, A5, R]: Conversion[(A1, A2, A3, A4, A5) => R, MockedMethod[(A1, A2, A3, A4, A5), R]] = f => f.tupled

  given conv06[A1, A2, A3, A4, A5, A6, R]: Conversion[(A1, A2, A3, A4, A5, A6) => R, MockedMethod[(A1, A2, A3, A4, A5, A6), R]] = f => f.tupled

  given conv07[A1, A2, A3, A4, A5, A6, A7, R]: Conversion[(A1, A2, A3, A4, A5, A6, A7) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7), R]] =
    f => f.tupled

  given conv08[A1, A2, A3, A4, A5, A6, A7, A8, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8), R]] = f => f.tupled

  given conv09[A1, A2, A3, A4, A5, A6, A7, A8, A9, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9), R]] = f => f.tupled

  given conv10[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10), R]] = f => f.tupled

  given conv11[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11), R]] =
    f => f.tupled

  given conv12[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12), R]] =
    f => f.tupled

  given conv13[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13), R]
  ] = f => f.tupled

  given conv14[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14), R]
  ] = f => f.tupled

  given conv15[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15), R]
  ] = f => f.tupled

  given conv16[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16), R]
  ] = f => f.tupled

  given conv17[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17), R]
  ] = f => f.tupled

  given conv18[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18), R]
  ] = f => f.tupled

  given conv19[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19), R]
  ] = f => f.tupled

  given conv20[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20), R]
  ] = f => f.tupled

  given conv21[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21), R]
  ] = f => f.tupled

  given conv22[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22), R]
  ] = f => f.tupled
