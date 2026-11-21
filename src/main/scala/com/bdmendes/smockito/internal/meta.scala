package com.bdmendes.smockito.internal

import java.lang.reflect.Modifier
import scala.compiletime.*
import scala.quoted.*
import scala.reflect.ClassTag

object meta:

  enum TypeType:
    case Regular
    case ByName
    case ValueClass(cls: Class[?], declared: Boolean)

    def maybeBox(value: Object): Object =
      this match
        case ValueClass(cls, declared) if declared || !cls.isInstance(value) =>
          val constructor = cls.getDeclaredConstructors.head
          constructor.setAccessible(true)
          constructor.newInstance(value)
        case _ =>
          value

    def maybeUnbox(value: Object, target: Class[?]): Object =
      this match
        case ValueClass(cls, declared)
            if cls.isInstance(value) && (declared || !target.isInstance(value)) =>
          val field = cls.getDeclaredFields.filterNot(f => Modifier.isStatic(f.getModifiers)).head
          field.setAccessible(true)
          field.get(value)
        case _ =>
          value

  trait MatchedMethodInfo:
    type ParameterNames <: Tuple
    val name: String
    val parameterTypes: IndexedSeq[TypeType]
    val resultType: TypeType

  private type MethodInfo = (
      name: String,
      paramNames: List[String],
      paramTypes: List[Expr[TypeType]],
      resultType: Expr[TypeType]
  )

  inline def mapTuple[T <: Tuple, R: ClassTag](inline f: [X] => (ClassTag[X]) ?=> R): Array[R] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Array.empty
      case _: (h *: t) =>
        f[h](using summonInline[ClassTag[h]]) +: mapTuple[t, R](f)

  inline def requireSingleType[T]: Unit =
    ${
      requireSingleTypeImpl[T]
    }

  def requireSingleTypeImpl[T: Type](using q: Quotes): Expr[Unit] =
    import q.reflect.*
    // The class tag erases these to a single class, the only one Mockito would mock.
    TypeRepr.of[T].dealias match
      case t @ (_: AndType | _: OrType) =>
        report.errorAndAbort(
          "Mocking union or intersection types such as " +
            s"${t.show(using Printer.TypeReprShortCode)} is not supported"
        )
      case _ =>
        '{
          ()
        }

  def matchedMethodInfo[T <: AnyRef: Type, F[_ <: AnyRef]: Type, A <: Tuple: Type, R: Type](
      expr: Expr[F[T] ?=> Any]
  )(using q: Quotes): Expr[MatchedMethodInfo] =
    import q.reflect.*

    given Printer[TypeRepr] = Printer.TypeReprShortCode

    val fTargetType = TypeRepr.of[F[T]]
    val rawTargetType = TypeRepr.of[T]
    val receivedReturnType = TypeRepr.of[R]
    val receivedParamTypes = TypeRepr.of[A].typeArgs

    def methodSignature(
        t: TypeRepr
    ): (paramNames: List[String], paramTypes: List[TypeRepr], returnType: TypeRepr) =
      // Concatenate the parameter lists into a single one, as one needs to do
      // in manual eta-expansion for curried methods.
      t.asMatchable match
        case MethodType(names, params, ret) =>
          val (retNames, retParams, result) = methodSignature(ret)
          (names ++ retNames, params ++ retParams, result)
        case PolyType(_, _, ret) =>
          methodSignature(ret)
        case _ =>
          // Values.
          (Nil, Nil, t)

    def normalize(t: TypeRepr, javaDefined: Boolean = false): TypeRepr =
      // Desugar for varargs, compiled to a Seq in Scala and to an array in Java.
      t.widenTermRefByName.widenByName.asMatchable match
        case AppliedType(tycon, arg :: Nil) if tycon.typeSymbol == defn.RepeatedParamClass =>
          if javaDefined then
            TypeRepr.of[Array].appliedTo(arg)
          else
            TypeRepr.of[Seq].appliedTo(arg)
        // Java members under explicit nulls.
        case FlexibleType(t) if t.typeSymbol == defn.RepeatedParamClass =>
          normalize(t, javaDefined)
        case t =>
          t

    def valueClass(t: TypeRepr, declared: TypeRepr): Expr[TypeType] =
      t.dealias
        .classSymbol
        .filter(cls =>
          cls.typeRef.baseClasses.contains(defn.AnyValClass) &&
            !(defn.AnyValClass :: defn.ScalaPrimitiveValueClasses).contains(cls)
        )
        .fold(
          '{
            TypeType.Regular
          }
        )(cls =>
          '{
            TypeType.ValueClass(
              ${
                Literal(ClassOfConstant(cls.typeRef)).asExprOf[Class[?]]
              },
              ${
                Expr(declared.dealias.classSymbol.contains(cls))
              }
            )
          }
        )

    def isCompatible(actual: TypeRepr, expected: TypeRepr): Boolean =
      expected.dealias.asMatchable match
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
          arg.tpe <:< fTargetType && fTargetType <:< term.tpe
        case _ =>
          term.tpe <:< fTargetType

    def showTypes(ts: List[TypeRepr]): String = ts.map(_.show).mkString("(", ", ", ")")

    val unmockableMethods =
      List(defn.AnyClass, defn.ObjectClass)
        .flatMap(_.declaredMethods)
        .filterNot(_.name == "toString")

    def checkAndReturn(method: Term): Option[MethodInfo] =
      // As declared in the type.
      val sym = method.symbol

      // Avoid runtime errors by flagging unmockable methods at compile time.
      if (sym :: sym.allOverriddenSymbols.toList).exists(unmockableMethods.contains) then
        report.errorAndAbort(s"Method ${sym.name} in ${rawTargetType.show} is not mockable")

      // As seen from the mock type, with the method's type arguments applied.
      val signature = methodSignature(normalize(method.tpe))

      // Eta-expansion in Scala has its quirks, such as capturing contextual arguments,
      // effectively returning a function whose shape does not exist in the class byte code.
      // This hints the user to eta-expand manually at compile time.
      val methodParamTypes = signature.paramTypes.map(normalize(_, sym.flags.is(Flags.JavaDefined)))
      if !methodParamTypes.corresponds(receivedParamTypes)(isCompatible) then
        report.errorAndAbort(
          s"Method ${sym.name} in ${rawTargetType.show} expects ${showTypes(methodParamTypes)} " +
            s"but received function expects ${showTypes(receivedParamTypes.map(normalize(_)))}"
        )
      if !(receivedReturnType <:< signature.returnType) then
        report.errorAndAbort(
          s"Method ${sym.name} in ${rawTargetType.show} returns ${signature.returnType.show} " +
            s"but received function returns ${receivedReturnType.show}"
        )

      val declared = methodSignature(sym.termRef.widen)
      val parameterTypes =
        signature
          .paramTypes
          .zip(declared.paramTypes)
          .map:
            case (_: ByNameType, _) =>
              '{
                TypeType.ByName
              }
            case (param, declaredParam) =>
              valueClass(param, declaredParam)
      val resultType = valueClass(signature.returnType.widenByName, declared.returnType.widenByName)
      Some((sym.name, signature.paramNames, parameterTypes, resultType))

    def findAndCheck(term: Term): Option[MethodInfo] =
      term match
        // Method selection.
        case tapp @ TypeApply(s @ Select(prefix, _), _)
            if targetsType(prefix) && s.symbol != Symbol.noSymbol =>
          checkAndReturn(tapp)
        case s @ Select(prefix, _) if targetsType(prefix) && s.symbol != Symbol.noSymbol =>
          checkAndReturn(s)
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
      case Some((methodName, methodParameterNames, parameterTypeExprs, resultTypeExpr)) =>
        // Singleton string types retain field names through the transparent inline API.
        val namesType =
          methodParameterNames.foldRight(TypeRepr.of[EmptyTuple]): (name, tail) =>
            TypeRepr.of[*:].appliedTo(List(ConstantType(StringConstant(name)), tail))
        namesType.asType match
          case '[n] =>
            '{
              new MatchedMethodInfo:
                type ParameterNames = n & Tuple
                val name =
                  ${
                    Expr(methodName)
                  }
                val parameterTypes =
                  IndexedSeq(
                    ${
                      Varargs(parameterTypeExprs)
                    }*
                  )
                val resultType = $resultTypeExpr
            }
      case None =>
        report.errorAndAbort(
          s"Expected direct selection of a mockable method of ${rawTargetType.show}"
        )
