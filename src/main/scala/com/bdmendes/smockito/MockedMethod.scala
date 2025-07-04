package com.bdmendes.smockito

class MockedMethod[A <: Tuple, R] private[smockito] (private[smockito] val underlying: A => R)

object MockedMethod:
  // We may use `TupledFunction` from the standard library once it goes stable. See
  // https://docs.scala-lang.org/scala3/reference/experimental/tupled-function.html

  given conv0[R]: Conversion[() => R, MockedMethod[EmptyTuple, R]] with

    def apply(f: () => R): MockedMethod[EmptyTuple, R] =
      new MockedMethod({ (arg: EmptyTuple) =>
        f()
      })

  given conv1[A, R]: Conversion[A => R, MockedMethod[Tuple1[A], R]] with

    def apply(f: A => R) =
      new MockedMethod({ (arg: Tuple1[A]) =>
        f(arg._1)
      })

  given conv2[A1, A2, R]: Conversion[(A1, A2) => R, MockedMethod[(A1, A2), R]] with
    def apply(f: (A1, A2) => R) = new MockedMethod(f.tupled)

  given conv3[A1, A2, A3, R]: Conversion[(A1, A2, A3) => R, MockedMethod[(A1, A2, A3), R]] with
    def apply(f: (A1, A2, A3) => R) = new MockedMethod(f.tupled)

  given conv4[A1, A2, A3, A4, R]
      : Conversion[(A1, A2, A3, A4) => R, MockedMethod[(A1, A2, A3, A4), R]] with
    def apply(f: (A1, A2, A3, A4) => R) = new MockedMethod(f.tupled)

  given conv5[A1, A2, A3, A4, A5, R]
      : Conversion[(A1, A2, A3, A4, A5) => R, MockedMethod[(A1, A2, A3, A4, A5), R]] with
    def apply(f: (A1, A2, A3, A4, A5) => R) = new MockedMethod(f.tupled)

  given conv6[A1, A2, A3, A4, A5, A6, R]
      : Conversion[(A1, A2, A3, A4, A5, A6) => R, MockedMethod[(A1, A2, A3, A4, A5, A6), R]] with
    def apply(f: (A1, A2, A3, A4, A5, A6) => R) = new MockedMethod(f.tupled)

  given conv7[A1, A2, A3, A4, A5, A6, A7, R]
      : Conversion[(A1, A2, A3, A4, A5, A6, A7) => R, MockedMethod[(A1, A2, A3, A4, A5, A6, A7), R]]
  with
    def apply(f: (A1, A2, A3, A4, A5, A6, A7) => R) = new MockedMethod(f.tupled)

  given conv8[A1, A2, A3, A4, A5, A6, A7, A8, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8), R]
  ] with
    def apply(f: (A1, A2, A3, A4, A5, A6, A7, A8) => R) = new MockedMethod(f.tupled)

  given conv9[A1, A2, A3, A4, A5, A6, A7, A8, A9, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9), R]
  ] with
    def apply(f: (A1, A2, A3, A4, A5, A6, A7, A8, A9) => R) = new MockedMethod(f.tupled)

  given conv10[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R]: Conversion[
    (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => R,
    MockedMethod[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10), R]
  ] with
    def apply(f: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => R) = new MockedMethod(f.tupled)
