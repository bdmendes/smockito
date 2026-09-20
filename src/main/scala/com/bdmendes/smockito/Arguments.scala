package com.bdmendes.smockito

import scala.compiletime.erasedValue

/** Arguments, as required by stubs and yield by verifications, are of the most convenient shape to
  * the caller: a scalar for single argument methods and a special named tuple of arguments for
  * multi-argument methods, which also retains positional access.
  */
type Arguments[N <: Tuple, A <: Tuple] =
  A match
    case EmptyTuple =>
      Unit
    case Tuple1[h] =>
      h
    case Tuple =>
      Arguments.NamedArguments[N, A]

object Arguments:

  opaque type NamedArguments[N <: Tuple, +A <: Tuple] >: A <: NamedTuple.NamedTuple[N, A] =
    NamedTuple.NamedTuple[N, A]

  // Named arguments retain positional access.
  extension [N <: Tuple, A <: Tuple](args: NamedArguments[N, A])
    inline def _1: Tuple.Elem[A, 0] = args.toTuple(0)
    inline def _2: Tuple.Elem[A, 1] = args.toTuple(1)
    inline def _3: Tuple.Elem[A, 2] = args.toTuple(2)
    inline def _4: Tuple.Elem[A, 3] = args.toTuple(3)
    inline def _5: Tuple.Elem[A, 4] = args.toTuple(4)
    inline def _6: Tuple.Elem[A, 5] = args.toTuple(5)
    inline def _7: Tuple.Elem[A, 6] = args.toTuple(6)
    inline def _8: Tuple.Elem[A, 7] = args.toTuple(7)
    inline def _9: Tuple.Elem[A, 8] = args.toTuple(8)
    inline def _10: Tuple.Elem[A, 9] = args.toTuple(9)
    inline def _11: Tuple.Elem[A, 10] = args.toTuple(10)
    inline def _12: Tuple.Elem[A, 11] = args.toTuple(11)
    inline def _13: Tuple.Elem[A, 12] = args.toTuple(12)
    inline def _14: Tuple.Elem[A, 13] = args.toTuple(13)
    inline def _15: Tuple.Elem[A, 14] = args.toTuple(14)
    inline def _16: Tuple.Elem[A, 15] = args.toTuple(15)
    inline def _17: Tuple.Elem[A, 16] = args.toTuple(16)
    inline def _18: Tuple.Elem[A, 17] = args.toTuple(17)
    inline def _19: Tuple.Elem[A, 18] = args.toTuple(18)
    inline def _20: Tuple.Elem[A, 19] = args.toTuple(19)
    inline def _21: Tuple.Elem[A, 20] = args.toTuple(20)
    inline def _22: Tuple.Elem[A, 21] = args.toTuple(21)

  private[smockito] inline def toTuple[N <: Tuple, A <: Tuple](x: Arguments[N, A]): A =
    inline erasedValue[A] match
      case _: EmptyTuple =>
        EmptyTuple.asInstanceOf[A]
      case _: Tuple1[?] =>
        Tuple1(x).asInstanceOf[A]
      case _: Tuple =>
        x.asInstanceOf[A]

  private[smockito] def apply[N <: Tuple, A <: Tuple](x: A): Arguments[N, A] =
    x match
      case x: EmptyTuple =>
        ()
      case x: Tuple1[?] =>
        x._1
      case x: Tuple =>
        NamedTuple[N, A](x)
