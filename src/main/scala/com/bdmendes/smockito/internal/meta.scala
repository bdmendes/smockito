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

  def matchedMethodName[T: Type, F[_], A <: Tuple: Type, R: Type](expr: Expr[F[T] ?=> Any])(using
      q: Quotes
  ): Expr[String] =
    import q.reflect.*

    given Printer[TypeRepr] = Printer.TypeReprShortCode

    val targetType = TypeRepr.of[T]
    val returnType = TypeRepr.of[R]

    lazy val receivedParamTypes: List[TypeRepr] =
      TypeRepr.of[A].dealias match
        case t if t =:= TypeRepr.of[EmptyTuple] =>
          Nil
        case AppliedType(_, args) =>
          args
        case _ =>
          report.errorAndAbort("Could not determine received parameter types")

    def methodSignature(t: TypeRepr): (List[TypeRepr], TypeRepr) =
      t match
        case MethodType(_, params, ret) =>
          val (retParams, result) = methodSignature(ret)
          (params ++ retParams, result)
        case PolyType(_, _, ret) =>
          methodSignature(ret)
        case _ =>
          (Nil, t)

    def normalize(t: TypeRepr): TypeRepr =
      t.widenTermRefByName.widenByName.dealias match
        case AppliedType(tycon, arg :: Nil) if tycon.typeSymbol == defn.RepeatedParamClass =>
          TypeRepr.of[Seq].appliedTo(arg).dealias
        case t =>
          t

    def isCompatible(actual: TypeRepr, expected: TypeRepr): Boolean =
      expected.dealias match
        case TypeBounds(low, high) =>
          normalize(low) <:< actual && actual <:< normalize(high)
        case expected =>
          normalize(expected) <:< actual

    def showTypes(ts: List[TypeRepr]): String = ts.map(_.show).mkString("(", ", ", ")")

    def checkAndReturn(sym: Symbol, methodType: TypeRepr): Option[String] =
      val (params, methodReturn) = methodSignature(methodType)
      val methodParamTypes = params.map(normalize)
      if !methodParamTypes.corresponds(receivedParamTypes)(isCompatible) then
        report.errorAndAbort(
          s"Method ${sym.name} in ${targetType.show} expects ${showTypes(methodParamTypes)} " +
            s"but received function expects ${showTypes(receivedParamTypes.map(normalize))}"
        )
      if !(returnType <:< methodReturn) then
        report.errorAndAbort(
          s"Method ${sym.name} in ${targetType.show} returns ${methodReturn.show} " +
            s"but received function returns ${returnType.show}"
        )
      Some(sym.name)

    def findAndCheck(term: Term): Option[String] =
      term match
        case tapp @ TypeApply(s @ Select(prefix, _), _) if prefix.tpe <:< targetType =>
          checkAndReturn(s.symbol, normalize(tapp.tpe))
        case s @ Select(prefix, _) if prefix.tpe <:< targetType =>
          checkAndReturn(s.symbol, normalize(prefix.tpe.memberType(s.symbol)))
        case Lambda(_, body) =>
          findAndCheck(body)
        case Apply(fn, args) =>
          (fn :: args).iterator.flatMap(findAndCheck).nextOption()
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
        report.errorAndAbort(s"Expected selection of a mockable method of ${targetType.show}")
