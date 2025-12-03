package com.bdmendes.smockito

import Mock.mapper.*
import com.bdmendes.smockito.Smockito.SmockitoException.*
import com.bdmendes.smockito.internal.meta.*
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicInteger
import org.mockito.*
import org.mockito.exceptions.base.MockitoAssertionError
import org.mockito.stubbing.Answer
import scala.compiletime.*
import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

/** A `Mock` represents a type mocked by Mockito. See [[Smockito.mock]] for more information.
  */
opaque type Mock[+T] <: T = T

private trait MockSyntax:

  extension [T](mock: Mock[T])

    private inline def matching[A <: Tuple, R](methods: Iterable[Method]): List[Method] =
      // Get all methods that may correspond to our types.
      // Due to erasure, we might get extra matches.
      val argClasses = mapTuple[A, ClassTag[?]](ct).map(_.runtimeClass)
      val returnClass = summonInline[ClassTag[R]].runtimeClass
      methods
        .filter: method =>
          method.getReturnType.isAssignableFrom(returnClass) &&
            argClasses.length == method.getParameterTypes.length &&
            argClasses.zip(method.getParameterTypes).forall((a, b) => b.isAssignableFrom(a))
        .toList

    private inline def assertMethodExists[A <: Tuple, R](): Unit =
      val methods = summonInline[ClassTag[T]].runtimeClass.getMethods
      if matching[A, R](methods).isEmpty then
        throw UnknownMethod

    private inline def verifies(f: => Any): Boolean =
      // Sometimes we need to resort to Mockito verifications with mode different than `atLeast(0)`.
      // In those cases, we must be careful not to swallow all exceptions.
      try
        f
        true
      catch
        case _: MockitoAssertionError =>
          false

    /** Sets up a stub for a method, based on the received tupled arguments.
      *
      * @param method
      *   the method to mock.
      * @param stub
      *   the stub implementation, based on the received arguments.
      * @return
      *   the mocked type.
      */
    inline def on[A <: Tuple, R1, R2 <: R1](method: Mock[T] ?=> MockedMethod[A, R1])(
        stub: PartialFunction[Pack[A], R2]
    ): Mock[T] =
      assertMethodExists[A, R1]()
      val answer: Answer[R2] =
        invocation =>
          val arguments = invocation.getRawArguments
          stub.applyOrElse(
            pack(Tuple.fromArray(arguments).asInstanceOf[A]),
            _ => throw UnexpectedArguments(invocation.getMethod, arguments)
          )
      method(using Mockito.doAnswer(answer).when(mock)).tupled(
        Tuple.fromArray(mapTuple[A, Any](anyMatcher)).asInstanceOf[A]
      )
      mock

    /** Sets up a stub that delegates to the real implementation of this method. Useful when you
      * want to preserve an adapter method’s behavior while stubbing a method lower in the hierarchy
      * later with [[on]]. Otherwise, use with care.
      *
      * Notice that, if the real implementation interacts with a class value that is not available
      * in the mocking context, the stub will throw with a [[java.lang.NullPointerException]].
      * Similarly, if this method is abstract on the mocked type, this will throw at set up time.
      *
      * @param method
      *   the method whose real implementation shall be called.
      * @return
      *   the mocked type.
      */
    inline def real[A <: Tuple, R](method: Mock[T] ?=> MockedMethod[A, R]): Mock[T] =
      assertMethodExists[A, R]()
      method(using Mockito.doCallRealMethod().when(mock)).tupled(
        Tuple.fromArray(mapTuple[A, Any](anyMatcher)).asInstanceOf[A]
      )
      mock

    /** Yields the captured arguments received by a stubbed method, in chronological order. If you
      * only need to reason about the number of interactions, [[times]] is more efficient.
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
          assertMethodExists[A, R]()
          val argCaptors = mapTuple[A, ArgumentCaptor[?]](captor)
          val _ =
            method(using Mockito.verify(mock, Mockito.atLeast(0))).tupled(
              Tuple.fromArray(argCaptors.map(_.capture())).asInstanceOf[A]
            )
          argCaptors
            .map(_.getAllValues.toArray)
            .transpose
            .toList
            .map(args => pack(Tuple.fromArray(args).asInstanceOf[A]))

    /** Yields the number of times a stub was called. If you need the exact arguments, see
      * [[calls]].
      *
      * @param method
      *   the mocked method.
      * @return
      *   the number of calls to the stub.
      */
    inline def times[A <: Tuple, R](method: Mock[T] ?=> MockedMethod[A, R]): Int =
      assertMethodExists[A, R]()
      inline erasedValue[A] match
        case _: EmptyTuple =>
          // Unfortunately, Mockito does not expose a reliable API for this use case.
          // We have to check all matching invocations, minding type erasure, and manually
          // validate the result.
          val invocations =
            matching[EmptyTuple, R](
              Mockito.mockingDetails(mock).getInvocations.asScala.map(_.getMethod)
            ).size
          val validInvocations = (invocations to 1 by -1).find: count =>
            verifies:
              method(using Mockito.verify(mock, Mockito.times(count))).tupled(
                EmptyTuple.asInstanceOf[A]
              )
          validInvocations.getOrElse(0)
        case _: (h *: t) =>
          // We do a little trick here: capturing the first argument is enough for counting the
          // number of calls.
          val cap = mapTuple[h *: EmptyTuple, ArgumentCaptor[?]](captor).head
          val _ =
            method(using Mockito.verify(mock, Mockito.atLeast(0))).tupled(
              Tuple.fromArray(cap.capture() +: mapTuple[t, Any](anyMatcher)).asInstanceOf[A]
            )
          cap.getAllValues.size

    /** Sets up a stub for a method that calls the respective method of a real instance. At a high
      * level, this desugars to:
      *
      * {{{
      *   mock.on(it.someMethod)(realInstance.someMethod)
      * }}}
      *
      * If you need to forward multiple methods, consider using a [[Spy]] instead.
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

    /** Sets up a stub for a method that behaves differently based on the call number. For the
      * version operating only on the expected set of inputs, see [[on]].
      *
      * @param method
      *   the method to mock.
      * @param stub
      *   the stub implementation, based on the call number of the respective method, starting at 1.
      * @return
      *   the mocked type.
      */
    inline def onCall[A <: Tuple, R1, R2 <: R1](method: Mock[T] ?=> MockedMethod[A, R1])(
        stub: Int => Pack[A] => R2
    ): Mock[T] =
      val callCount = AtomicInteger(0)
      val f = (args: Pack[A]) => stub(callCount.incrementAndGet())(args)
      mock.on(method)(PartialFunctionProxy(f))

    /** Whether the last invocation of method `a` happened before the last invocation of method `b`,
      * provided both methods were called at least once. Same as `calledAfter(b, a)`.
      *
      * @param a
      *   the method that is expected to be called first.
      * @param b
      *   the method that is expected to be called after `a`.
      * @return
      *   Whether `a` was called before `b`.
      */
    inline def calledBefore[A1 <: Tuple, R1, A2 <: Tuple, R2](
        a: Mock[T] ?=> MockedMethod[A1, R1],
        b: Mock[T] ?=> MockedMethod[A2, R2]
    ): Boolean =
      assertMethodExists[A1, R1]()
      assertMethodExists[A2, R2]()
      val ordered = Mockito.inOrder(mock)
      verifies:
        a(using ordered.verify(mock, Mockito.atLeastOnce)).tupled(
          Tuple.fromArray(mapTuple[A1, Any](anyMatcher)).asInstanceOf[A1]
        )
        b(using ordered.verify(mock, Mockito.atLeastOnce)).tupled(
          Tuple.fromArray(mapTuple[A2, Any](anyMatcher)).asInstanceOf[A2]
        )

    /** Whether the last invocation of method `a` happened after the last invocation of method `b`,
      * provided both methods were called at least once. Same as `calledBefore(b, a)`.
      *
      * @param a
      *   the method that is expected to be called after `b`.
      * @param b
      *   the method that is expected to be called first.
      * @return
      *   Whether `a` was called after `b`.
      */
    inline def calledAfter[A1 <: Tuple, R1, A2 <: Tuple, R2](
        a: Mock[T] ?=> MockedMethod[A1, R1],
        b: Mock[T] ?=> MockedMethod[A2, R2]
    ): Boolean = calledBefore(b, a)

private object Mock:

  object mapper:
    lazy val anyMatcher = [X] => (_: ClassTag[X]) ?=> ArgumentMatchers.any[X]
    lazy val captor = [X] => (_: ClassTag[X]) ?=> ArgumentCaptor.captor[X]()
    lazy val ct = [X] => (x: ClassTag[X]) ?=> x

    class PartialFunctionProxy[A <: Tuple, R](f: Pack[A] => R) extends PartialFunction[Pack[A], R]:
      override def apply(args: Pack[A]): R = f(args)
      override def isDefinedAt(x: Pack[A]): Boolean = true

  def apply[T](using ct: ClassTag[T]): Mock[T] =
    Mockito.mock(
      ct.runtimeClass.asInstanceOf[Class[T]],
      Mockito.withSettings().defaultAnswer(DefaultAnswer())
    )
