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
    val receivedReturnType = TypeRepr.of[R]
    val receivedParamTypes = TypeRepr.of[A].typeArgs

    def methodSignature(t: TypeRepr): (List[TypeRepr], TypeRepr) =
      // Concatenate the parameter lists into a single one, as one needs to do
      // in manual eta-expansion for curried methods.
      t match
        case MethodType(_, params, ret) =>
          val (retParams, result) = methodSignature(ret)
          (params ++ retParams, result)
        case _ =>
          // Values.
          (Nil, t)

    def normalize(t: TypeRepr): TypeRepr =
      // Desugar for varargs, compiled to a Seq.
      t.widenTermRefByName.widenByName match
        case AppliedType(tycon, arg :: Nil) if tycon.typeSymbol == defn.RepeatedParamClass =>
          TypeRepr.of[Seq].appliedTo(arg)
        case t =>
          t

    def isCompatible(actual: TypeRepr, expected: TypeRepr): Boolean =
      expected.dealias match
        case TypeBounds(low, high) =>
          normalize(low) <:< actual && actual <:< normalize(high)
        case expected =>
          normalize(expected) =:= actual

    def targetsType(term: Term): Boolean =
      // Analyse the instance type via the `it` method argument, or directly
      // if it is referenced in another fashion.
      term match
        case Apply(_, List(arg)) =>
          // In the case of super class methods such as `toString`, the type argument of `it`
          // widens and so does the resulting expression, so check for an upper bound instead.
          arg.tpe <:< targetType && targetType <:< term.tpe
        case _ =>
          term.tpe <:< targetType

    def showTypes(ts: List[TypeRepr]): String = ts.map(_.show).mkString("(", ", ", ")")

    def checkAndReturn(sym: Symbol, methodType: TypeRepr): Option[String] =
      // Eta-expansion in Scala has its quirks, such as capturing contextual arguments,
      // effectively returning a function whose shape does not exist in the class byte code.
      // This hints the user to eta-expand manually at compile time.
      val (params, methodReturn) = methodSignature(methodType)
      val methodParamTypes = params.map(normalize)
      if !methodParamTypes.corresponds(receivedParamTypes)(isCompatible) then
        report.errorAndAbort(
          s"Method ${sym.name} in ${targetType.show} expects ${showTypes(methodParamTypes)} " +
            s"but received function expects ${showTypes(receivedParamTypes.map(normalize))}"
        )
      if !(receivedReturnType <:< methodReturn) then
        report.errorAndAbort(
          s"Method ${sym.name} in ${targetType.show} returns ${methodReturn.show} " +
            s"but received function returns ${receivedReturnType.show}"
        )
      Some(sym.name)

    def findAndCheck(term: Term): Option[String] =
      term match
        // Method selection.
        case tapp @ TypeApply(s @ Select(prefix, _), _) if targetsType(prefix) =>
          checkAndReturn(s.symbol, normalize(tapp.tpe))
        case s @ Select(prefix, _) if targetsType(prefix) =>
          checkAndReturn(s.symbol, normalize(prefix.tpe.memberType(s.symbol)))
        // Parent AST nodes. Particularly relevant is the implicit conversion step
        // via the `Conversion` typeclass application to lift functions to `MockedMethod`.
        case Lambda(_, body) =>
          findAndCheck(body)
        case Apply(fn, args) =>
          (fn :: args).iterator.flatMap(findAndCheck).nextOption()
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
