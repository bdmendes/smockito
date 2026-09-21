package com.bdmendes.smockito

import scala.compiletime.erasedValue

/** Arguments, as required by stubs and yielded by verifications, are of the most convenient shape
  * to the caller: a scalar for single argument methods and a named tuple for multi-argument
  * methods.
  */
type Arguments[N <: Tuple, A <: Tuple] =
  A match
    case EmptyTuple =>
      Unit
    case Tuple1[h] =>
      h
    case Tuple =>
      NamedTuple.NamedTuple[N, A]

private[smockito] object Arguments:

  inline def toTuple[N <: Tuple, A <: Tuple](x: Arguments[N, A]): A =
    inline erasedValue[A] match
      case _: EmptyTuple =>
        EmptyTuple.asInstanceOf[A]
      case _: Tuple1[?] =>
        Tuple1(x).asInstanceOf[A]
      case _: Tuple =>
        x.asInstanceOf[A]

  def apply[N <: Tuple, A <: Tuple](x: A): Arguments[N, A] =
    x match
      case x: EmptyTuple =>
        ()
      case x: Tuple1[?] =>
        x._1
      case x: Tuple =>
        NamedTuple[N, A](x)
