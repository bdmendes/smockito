package com.bdmendes.smockito.internal

import com.bdmendes.smockito.*
import scala.compiletime.*
import scala.quoted.*
import scala.reflect.ClassTag

private[smockito] object meta:

  inline def mapTuple[T <: Tuple, R: ClassTag](inline f: [X] => (ClassTag[X]) ?=> R): Array[R] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Array.empty
      case _: (h *: t) =>
        f[h](using summonInline[ClassTag[h]]) +: mapTuple[t, R](f)

  def abortOnInvalidMethodSelection[T: Type, A <: Tuple: Type, R: Type](
      method: Expr[Mock[T] ?=> MockedMethod[A, R]]
  )(using q: Quotes): Expr[Unit] =
    import q.reflect.*

    val mockType = TypeRepr.of[T]

    // Walk the tree looking for any Select whose prefix has type T (or subtype).
    // That's the signature of `it.someMethod` (where `it: T`), as opposed to
    // an arbitrary lambda whose body has no such selection.
    def hasSelectOnMock(term: Term): Boolean =
      term match
        case Select(prefix, _) if prefix.tpe <:< mockType =>
          true
        case Lambda(_, body) =>
          hasSelectOnMock(body)
        case Apply(fn, args) =>
          hasSelectOnMock(fn) || args.exists(hasSelectOnMock)
        case TypeApply(fn, _) =>
          hasSelectOnMock(fn)
        case Typed(inner, _) =>
          hasSelectOnMock(inner)
        case Block(stmts, expr) =>
          stmts.exists:
            case t: Term =>
              hasSelectOnMock(t)
            case _ =>
              false
          || hasSelectOnMock(expr)
        case Inlined(_, _, body) =>
          hasSelectOnMock(body)
        case _ =>
          false

    if !hasSelectOnMock(method.asTerm) then
      report.errorAndAbort(
        "Smockito expects a direct method reference via `it` (e.g. `it.someMethod`), found unrelated expression"
      )

    '{}
