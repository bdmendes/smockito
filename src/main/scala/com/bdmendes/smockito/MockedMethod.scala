package com.bdmendes.smockito

class MockedMethod[A <: Tuple, R] private[smockito] (private[smockito] val underlying: A => R)
    extends AnyVal

object MockedMethod:
  // We may use `TupledFunction` from the standard library once it goes stable. See
  // https://docs.scala-lang.org/scala3/reference/experimental/tupled-function.html

  // scalafmt: { maxColumn = 150 }

  given conv0[R]: Conversion[() => R, MockedMethod[EmptyTuple, R]] =
    f =>
      val tupled = (arg: EmptyTuple) => f()
      MockedMethod(tupled)

  given conv1[A, R]: Conversion[A => R, MockedMethod[Tuple1[A], R]] =
    f =>
      val tupled = (arg: Tuple1[A]) => f(arg._1)
      MockedMethod(tupled)

  given conv2[A1, A2, R]: Conversion[(A1, A2) => R, MockedMethod[(A1, A2), R]] = f => MockedMethod(f.tupled)

  given conv3[A1, A2, A3, R]: Conversion[(A1, A2, A3) => R, MockedMethod[(A1, A2, A3), R]] = f => MockedMethod(f.tupled)

  given conv4[A1, A2, A3, A4, R]: Conversion[(A1, A2, A3, A4) => R, MockedMethod[(A1, A2, A3, A4), R]] = f => MockedMethod(f.tupled)

  given conv5[A1, A2, A3, A4, A5, R]: Conversion[(A1, A2, A3, A4, A5) => R, MockedMethod[(A1, A2, A3, A4, A5), R]] = f => MockedMethod(f.tupled)

  given conv6[A1, A2, A3, A4, A5, A6, R]: Conversion[(A1, A2, A3, A4, A5, A6) => R, MockedMethod[(A1, A2, A3, A4, A5, A6), R]] =
    f => MockedMethod(f.tupled)

  given conv7[A1, A2, A3, A4, A5, A6, A7, R]: Conversion[(A1, A2, A3, A4, A5, A6, A7) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7), R]] =
    f => MockedMethod(f.tupled)

  given conv8[A1, A2, A3, A4, A5, A6, A7, A8, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8), R]] = f => MockedMethod(f.tupled)

  given conv9[A1, A2, A3, A4, A5, A6, A7, A8, A9, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9), R]] = f => MockedMethod(f.tupled)

  given conv10[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10), R]] =
    f => MockedMethod(f.tupled)

  given conv11[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11), R]] =
    f => MockedMethod(f.tupled)

  given conv12[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12), R]] =
    f => MockedMethod(f.tupled)

  given conv13[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13), R]
  ] = f => MockedMethod(f.tupled)

  given conv14[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14), R]
  ] = f => MockedMethod(f.tupled)

  given conv15[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15), R]
  ] = f => MockedMethod(f.tupled)

  given conv16[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16), R]
  ] = f => MockedMethod(f.tupled)

  given conv17[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17), R]
  ] = f => MockedMethod(f.tupled)

  given conv18[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18), R]
  ] = f => MockedMethod(f.tupled)

  given conv19[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19), R]
  ] = f => MockedMethod(f.tupled)

  given conv20[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20), R]
  ] = f => MockedMethod(f.tupled)

  given conv21[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21), R]
  ] = f => MockedMethod(f.tupled)

  given conv22[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22), R]
  ] = f => MockedMethod(f.tupled)
