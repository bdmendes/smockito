package com.bdmendes.smockito

import Mock.mapper.*
import com.bdmendes.smockito.Smockito.SmockitoException.*
import com.bdmendes.smockito.internal.DefaultAnswer
import com.bdmendes.smockito.internal.meta
import org.mockito.*
import org.mockito.exceptions.base.MockitoAssertionError
import scala.compiletime.*
import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

/** A `Mock` represents a type mocked by Mockito, with a default answer strategy of throwing on
  * unstubbed methods.
  */
opaque type Mock[+T <: AnyRef] <: T = T

private trait MockSyntax:

  extension [T <: AnyRef](mock: Mock[T])

    private[smockito] transparent inline def validateAndRetrieveMethodInfo[A <: Tuple, R](
        inline method: Mock[T] ?=> MockedMethod[A, R]
    ) =
      ${
        meta.matchedMethodInfo[T, Mock, A, R]('method)
      }

    private inline def verifies(f: => Any): Boolean =
      // Sometimes we need to resort to Mockito verifications with mode different than `atLeast(0)`.
      // In those cases, we must be careful not to swallow all exceptions.
      try
        f
        true
      catch
        case _: MockitoAssertionError =>
          false

    /** Sets up a stub for a method that behaves differently based on the call number and the
      * received arguments. This will override any previous stubs for the same method.
      *
      * The call number is local to this stub. If the method is restubbed between calls, it may
      * differ from the total invocation count reported by [[times]].
      *
      * @param method
      *   the method to mock.
      * @return
      *   a setup accepting a stub based on its call number, starting at 1.
      * @see
      *   [[on]] for the version that only considers the expected set of inputs.
      */
    transparent inline def onCall[A <: Tuple, R](inline method: Mock[T] ?=> MockedMethod[A, R]) =
      inline validateAndRetrieveMethodInfo(method) match
        case info: meta.MatchedMethodInfo =>
          new Stubber[T, info.ParameterNames, A, R](mock, method, info)

    /** Sets up a stub for a method, based on the received arguments. This will override any
      * previous stubs for the same method.
      *
      * @param method
      *   the method to mock.
      * @return
      *   a setup accepting a stub based on the received arguments.
      * @see
      *   [[onCall]] for the version that also takes the call number into account.
      */
    transparent inline def on[A <: Tuple, R](inline method: Mock[T] ?=> MockedMethod[A, R]) =
      inline validateAndRetrieveMethodInfo(method) match
        case info: meta.MatchedMethodInfo =>
          new Stubber.SimpleStubber[T, info.ParameterNames, A, R](
            new Stubber[T, info.ParameterNames, A, R](mock, method, info)
          )

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
    inline def real[A <: Tuple, R](inline method: Mock[T] ?=> MockedMethod[A, R]): Mock[T] =
      val _ = validateAndRetrieveMethodInfo(method)
      val target = method(using Mockito.doCallRealMethod().when(mock))
      target.tupled(Tuple.fromArray(meta.mapTuple[A, Any](anyMatcher)).asInstanceOf[A])
      mock

    /** Yields the arguments received by a method, per invocation, in chronological order.
      *
      * @param method
      *   the mocked method.
      * @return
      *   the received arguments.
      * @see
      *   [[times]] for the version that only counts the number of calls, for efficiency.
      */
    transparent inline def calls[A <: Tuple, R](inline method: Mock[T] ?=> MockedMethod[A, R]) =
      inline erasedValue[A] match
        case _: EmptyTuple =>
          error("`calls` is not available for nullary methods; use `times` instead")
        case _ =>
          inline validateAndRetrieveMethodInfo(method) match
            case info: meta.MatchedMethodInfo =>
              val argCaptors = meta.mapTuple[A, ArgumentCaptor[?]](captor)
              val target = method(using Mockito.verify(mock, Mockito.atLeast(0)))
              target.tupled(Tuple.fromArray(argCaptors.map(_.capture())).asInstanceOf[A])
              argCaptors
                .map(_.getAllValues.toArray)
                .transpose
                .toList
                .map[Arguments[info.ParameterNames, A]](args =>
                  Arguments[info.ParameterNames, A](
                    Tuple.fromArray(Mock.unwrap[A](args, info.parameterTypes)).asInstanceOf[A]
                  )
                )

    /** Yields the number of times a method was called.
      *
      * @param method
      *   the mocked method.
      * @return
      *   the number of calls to the method.
      * @see
      *   [[calls]] if you need the exact received arguments per invocation.
      */
    inline def times[A <: Tuple, R](inline method: Mock[T] ?=> MockedMethod[A, R]): Int =
      val _ = validateAndRetrieveMethodInfo(method)
      inline erasedValue[A] match
        case _: EmptyTuple =>
          // Mockito has no reliable API for this use case, so we have to resort to inspecting the
          // mock's invocations and considering all possible matches.
          val returnClass = summonInline[ClassTag[R]].runtimeClass
          val possiblyMatching =
            Mockito
              .mockingDetails(mock)
              .getInvocations
              .asScala
              .filter(invocation =>
                invocation.getMethod.getParameters.isEmpty &&
                  invocation.getMethod.getReturnType.isAssignableFrom(returnClass)
              )
              .size
          val validInvocations = (possiblyMatching to 1 by -1).find: count =>
            verifies:
              val target = method(using Mockito.verify(mock, Mockito.times(count)))
              target.tupled(EmptyTuple.asInstanceOf[A])
          validInvocations.getOrElse(0)
        case _: (h *: t) =>
          // Non-nullary methods may be overloaded, so we resort to a little trick here: we capture
          // the first argument only, which is enough for counting the number of calls.
          val cap = meta.mapTuple[h *: EmptyTuple, ArgumentCaptor[?]](captor).head
          val target = method(using Mockito.verify(mock, Mockito.atLeast(0)))
          target.tupled(
            Tuple.fromArray(cap.capture() +: meta.mapTuple[t, Any](anyMatcher)).asInstanceOf[A]
          )
          cap.getAllValues.size

    /** Sets up a stub for a method that calls the respective method of a real instance.
      *
      * At a high level, this desugars to:
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
        inline method: Mock[T] ?=> MockedMethod[A, R],
        realInstance: T
    ): Mock[T] =
      val realMethod = method(using realInstance.asInstanceOf[Mock[T]]).tupled
      Stubber.apply(mock.on(method))(PartialFunction.fromFunction(realMethod))

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
        inline a: Mock[T] ?=> MockedMethod[A1, R1],
        inline b: Mock[T] ?=> MockedMethod[A2, R2]
    ): Boolean =
      val _ = validateAndRetrieveMethodInfo(a)
      val _ = validateAndRetrieveMethodInfo(b)
      val ordered = Mockito.inOrder(mock)
      verifies:
        val targetA = a(using ordered.verify(mock, Mockito.atLeastOnce))
        targetA.tupled(Tuple.fromArray(meta.mapTuple[A1, Any](anyMatcher)).asInstanceOf[A1])
        val targetB = b(using ordered.verify(mock, Mockito.atLeastOnce))
        targetB.tupled(Tuple.fromArray(meta.mapTuple[A2, Any](anyMatcher)).asInstanceOf[A2])

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
        inline a: Mock[T] ?=> MockedMethod[A1, R1],
        inline b: Mock[T] ?=> MockedMethod[A2, R2]
    ): Boolean = calledBefore(b, a)

private object Mock:

  inline def unwrap[A <: Tuple](
      arguments: Array[Object],
      parameterTypes: IndexedSeq[meta.MethodParameterType],
      index: Int = 0,
      needsCloning: Boolean = true
  ): Array[Object] =
    // By-name parameters are compiled as nullary functions, hence the special treatment.
    if needsCloning then
      unwrap[A](arguments.clone(), parameterTypes, index, false)
    else
      inline erasedValue[A] match
        case _: EmptyTuple =>
          arguments
        case _: (h *: t) =>
          val unwrapped =
            arguments(index) match
              case f: Function0[?] =>
                parameterTypes(index) match
                  case meta.MethodParameterType.ByName =>
                    f.apply()
                  case meta.MethodParameterType.Regular =>
                    f
              case other =>
                other
          val typeCheckedValue =
            if unwrapped != null then
              try
                unwrapped.asInstanceOf[h]
              catch
                case _: ClassCastException =>
                  val expected = summonInline[ClassTag[h]].runtimeClass
                  throw UnexpectedType(unwrapped, expected)
            else
              unwrapped
          arguments.update(index, typeCheckedValue.asInstanceOf[Object])
          unwrap[t](arguments, parameterTypes, index + 1, false)

  object mapper:
    lazy val anyMatcher = [X] => (_: ClassTag[X]) ?=> ArgumentMatchers.any[X]().asInstanceOf[X]

    lazy val captor =
      [X] => (_: ClassTag[X]) ?=> ArgumentCaptor.captor[X]().asInstanceOf[ArgumentCaptor[X]]

  def apply[T <: AnyRef](using ct: ClassTag[T]): Mock[T] =
    Mockito.mock(
      ct.runtimeClass.asInstanceOf[Class[T]],
      Mockito.withSettings().defaultAnswer(DefaultAnswer)
    )
