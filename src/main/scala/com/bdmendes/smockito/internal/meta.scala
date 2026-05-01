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

  def matchedMethodName[T: Type, F[_], A <: Tuple: Type](expr: Expr[F[T] ?=> Any])(using
      q: Quotes
  ): Expr[String] =
    import q.reflect.*

    val targetType = TypeRepr.of[T]
    lazy val typeName = targetType.show(using Printer.TypeReprShortCode)

    val expectedArity: Int =
      TypeRepr.of[A].dealias match
        case t if t =:= TypeRepr.of[EmptyTuple] =>
          0
        case AppliedType(_, args) =>
          args.length
        case _ =>
          report.errorAndAbort("Could not determine expected arity")

    def findAndCheck(term: Term): Option[String] =
      term match
        case s @ Select(prefix, _) if prefix.tpe <:< targetType =>
          val actualArity = s.symbol.paramSymss.filterNot(_.exists(_.isType)).map(_.length).sum
          if actualArity != expectedArity then
            val plural = Option.when(actualArity != 1)("s").getOrElse("")
            report.errorAndAbort(
              s"Method ${s.symbol.name} in $typeName has $actualArity parameter$plural " +
                s"but received function expects $expectedArity"
            )
          Some(s.symbol.name)
        case Lambda(_, body) =>
          findAndCheck(body)
        case Apply(fn, args) =>
          findAndCheck(fn).orElse(args.iterator.flatMap(findAndCheck).nextOption())
        case TypeApply(fn, _) =>
          findAndCheck(fn)
        case Block(_, e) =>
          findAndCheck(e)
        case Inlined(_, _, body) =>
          findAndCheck(body)
        case _ =>
          None

    findAndCheck(expr.asTerm) match
      case Some(methodName) =>
        Expr(methodName)
      case None =>
        report.errorAndAbort(s"Expected selection of a mockable method of $typeName")
