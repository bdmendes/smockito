package com.bdmendes.smockito

type Pack[A <: Tuple] =
  A match
    case EmptyTuple =>
      EmptyTuple
    case Tuple1[h] =>
      h
    case Tuple =>
      A

private[smockito] def pack[A <: Tuple](x: A): Pack[A] =
  x match
    case x: EmptyTuple =>
      EmptyTuple
    case x: Tuple1[?] =>
      x._1
    case x: Tuple =>
      x
