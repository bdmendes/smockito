package com.bdmendes.smockito

import Mock.mapper.*
import com.bdmendes.smockito.Smockito.SmockitoException.*
import com.bdmendes.smockito.Smockito.SmockitoMode
import com.bdmendes.smockito.internal.meta.*
import com.bdmendes.smockito.pack
import java.lang.reflect.Method
import org.mockito.*
import scala.compiletime.*
import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag
import scala.util.Try

/** A `Mock` represents a type mocked by Mockito. See [[Smockito.mock]] for more information.
  */
opaque type Mock[+T] <: T = T

private[smockito] trait MockSyntax:

  val mode: SmockitoMode

  extension [T](mock: Mock[T])

    private inline def matching[A <: Tuple, R](methods: Iterable[Method]): List[Method] =
      // Get all methods that may correspond to our types.
      // Due to erasure, we might get extra matches.
      val argClasses = mapTuple[A, ClassTag[?]](ct).map(_.runtimeClass)
      val returnClass = summonInline[ClassTag[R]].runtimeClass
      methods
        .filter { method =>
          method.getReturnType.isAssignableFrom(returnClass) &&
          argClasses.length == method.getParameterTypes.length &&
          argClasses.zip(method.getParameterTypes).forall((a, b) => b.isAssignableFrom(a))
        }
        .toList

    private inline def assertMethodExists[A <: Tuple, R](): Unit =
      if mode == SmockitoMode.Strict then
        inline erasedValue[A] match
          case EmptyTuple =>
            ()
          case _ =>
            val methods = summonInline[ClassTag[T]].runtimeClass.getMethods
            if matching[A, R](methods).isEmpty then
              throw UnknownMethod

    private inline def assertStubbedBefore[A <: Tuple, R](): Unit =
      if mode == SmockitoMode.Strict then
        val invocations = Mockito.mockingDetails(mock).getStubbings.asScala.map(_.getInvocation)
        if matching[A, R](invocations.map(_.getMethod)).isEmpty then
          throw UnstubbedMethod

    /** Sets up a stub for a method. Refer to [[Smockito]] for a usage example.
      *
      * @param method
      *   the method to mock.
      * @param stub
      *   the stub implementation.
      * @return
      *   the mocked type.
      */
    inline def on[A <: Tuple, R1, R2 <: R1](method: Mock[T] ?=> MockedMethod[A, R1])(
        stub: PartialFunction[Pack[A], R2]
    ): Mock[T] =
      assertMethodExists[A, R1]()
      Mockito
        .when(
          method(using mock).tupled(Tuple.fromArray(mapTuple[A, Any](anyMatcher)).asInstanceOf[A])
        )
        .thenAnswer { invocation =>
          val arguments = invocation.getArguments

          // If this stub is invoked with nulls, assume we are in the process of setting up
          // a stub override (i.e. using ArgumentMatchers.any ~ null).
          // Assuming a method won't receive null should not be a problem in the Scala world.
          if arguments.nonEmpty && arguments.forall(_ == null) then
            if mode == SmockitoMode.Strict then
              throw AlreadyStubbedMethod(invocation.getMethod)
            else
              null
          else
            stub.applyOrElse(
              pack(Tuple.fromArray(arguments).asInstanceOf[A]),
              _ => throw UnexpectedArguments(invocation.getMethod, arguments)
            )
        }
      mock

    /** Yields the captured arguments received by a stubbed method, in chronological order. Refer to
      * [[Smockito]] for a usage example.
      *
      * @param method
      *   the mocked method.
      * @return
      *   the received arguments.
      */
    inline def calls[A <: Tuple, R](method: Mock[T] ?=> MockedMethod[A, R]): List[Pack[A]] =
      inline erasedValue[A] match
        case _: EmptyTuple =>
          error("`calls` is not available for nullary methods. Use `times` instead.")

        case _ =>
          assertStubbedBefore[A, R]()
          val argCaptors = mapTuple[A, ArgumentCaptor[?]](captor)
          val _ =
            method(using Mockito.verify(mock, Mockito.atLeast(0)))
              .tupled
              .apply(Tuple.fromArray(argCaptors.map(_.capture())).asInstanceOf[A])
          argCaptors
            .map(_.getAllValues.toArray)
            .transpose
            .toList
            .map(args => pack(Tuple.fromArray(args).asInstanceOf[A]))

    /** Yields the number of times a stub was called. Refer to [[Smockito]] for a usage example.
      *
      * @param method
      *   the mocked method.
      * @return
      *   the number of calls to the stub.
      */
    inline def times[A <: Tuple, R](method: Mock[T] ?=> MockedMethod[A, R]): Int =
      assertStubbedBefore[A, R]()
      inline erasedValue[A] match
        case _: EmptyTuple =>
          // Unfortunately, Mockito does not expose a reliable API for this use case.
          // We have to check all matching invocations, minding type erasure, and manually
          // validate the result.
          val invocations =
            matching[EmptyTuple, R](
              Mockito.mockingDetails(mock).getInvocations.asScala.map(_.getMethod)
            ).size
          val validInvocations = (invocations to 1 by -1).find { count =>
            Try(
              method(using Mockito.verify(mock, Mockito.times(count)))
                .tupled
                .apply(EmptyTuple.asInstanceOf[A])
            ).isSuccess
          }
          validInvocations.getOrElse(0)
        case _: (h *: t) =>
          // We do a little trick here: capturing the first argument is enough for counting the
          // number of calls.
          val cap = mapTuple[h *: EmptyTuple, ArgumentCaptor[?]](captor).head
          val _ =
            method(using Mockito.verify(mock, Mockito.atLeast(0)))
              .tupled
              .apply(Tuple.fromArray(cap.capture() +: mapTuple[t, Any](anyMatcher)).asInstanceOf[A])
          cap.getAllValues.size

    /** Sets up a stub for a method that calls the respective method of a real instance. At a high
      * level, this desugars to:
      *
      * {{{
      *   mock.on(it.someMethod)(realInstance.someMethod)
      * }}}
      *
      * @param method
      *   the mocked method.
      * @param realInstance
      *   the real instance.
      * @return
      *   the mocked type.
      */
    inline def forward[A <: Tuple, R](
        method: Mock[T] ?=> MockedMethod[A, R],
        realInstance: T
    ): Mock[T] =
      val realMethod = method(using realInstance.asInstanceOf[Mock[T]]).packed
      mock.on(method)(PartialFunctionProxy(realMethod))

object Mock:

  private[smockito] object mapper:
    lazy val anyMatcher = [X] => (_: ClassTag[X]) ?=> ArgumentMatchers.any[X]()
    lazy val captor = [X] => (_: ClassTag[X]) ?=> ArgumentCaptor.captor[X]()
    lazy val ct = [X] => (x: ClassTag[X]) ?=> x

    class PartialFunctionProxy[A <: Tuple, R](f: Pack[A] => R) extends PartialFunction[Pack[A], R]:
      override def apply(args: Pack[A]): R = f(args)
      override def isDefinedAt(x: Pack[A]): Boolean = true

  private[smockito] def apply[T](using ct: ClassTag[T]): Mock[T] =
    Mockito.mock(ct.runtimeClass.asInstanceOf[Class[T]])
