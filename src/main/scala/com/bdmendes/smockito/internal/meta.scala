package com.bdmendes.smockito.internal

import scala.compiletime.*
import scala.reflect.ClassTag

object meta:

  inline def mapTuple[T <: Tuple, R: ClassTag](inline f: [X] => () => R): Array[R] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Array.empty
      case _: (h *: t) =>
        f[h]() +: mapTuple[t, R](f)

  inline def summonClassTags[T <: Tuple]: Array[ClassTag[?]] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Array.empty
      case _: (h *: t) =>
        summonInline[ClassTag[h]] +: summonClassTags[t]
