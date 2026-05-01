package com.bdmendes.smockito.internal

import scala.compiletime.*
import scala.quoted.*
import scala.reflect.ClassTag

object meta:

  inline def mapTuple[T <: Tuple, R: ClassTag](inline f: [X] => (ClassTag[X]) ?=> R): Array[R] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Array.empty
      case _: (h *: t) =>
        f[h](using summonInline[ClassTag[h]]) +: mapTuple[t, R](f)

  def abortOnInvalidMethodSelection[T: Type, F[_]](expr: Expr[F[T] ?=> Any])(using
      q: Quotes
  ): Expr[Unit] =
    import q.reflect.*

    val targetType = TypeRepr.of[T]

    // Walk the tree looking for any Select whose prefix has type T (or subtype).
    // That's the signature of `it.someMethod` (where `it: T`), as opposed to
    // an arbitrary lambda whose body has no such selection.
    def hasSelectOnF(term: Term): Boolean =
      term match
        case Select(prefix, _) if prefix.tpe <:< targetType =>
          true
        case Lambda(_, body) =>
          hasSelectOnF(body)
        case Apply(fn, args) =>
          hasSelectOnF(fn) || args.exists(hasSelectOnF)
        case TypeApply(fn, _) =>
          hasSelectOnF(fn)
        case Block(_, expr) =>
          hasSelectOnF(expr)
        case Inlined(_, _, body) =>
          hasSelectOnF(body)
        case _ =>
          false

    if !hasSelectOnF(expr.asTerm) then
      report.errorAndAbort(
        "Smockito expects a direct method reference via `it` (e.g. `it.foo`), got unrelated expression"
      )

    '{}
