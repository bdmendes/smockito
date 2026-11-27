package com.bdmendes.smockito

import com.bdmendes.smockito.Mock.mapper.*
import com.bdmendes.smockito.Smockito.SmockitoException.*
import com.bdmendes.smockito.internal.DefaultAnswer
import com.bdmendes.smockito.internal.meta
import org.mockito.*
import org.mockito.exceptions.base.MockitoAssertionError
import org.mockito.invocation.Invocation
import org.mockito.verification.VerificationMode
import scala.compiletime.*
import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag
import scala.util.Try

/** A `Mock` represents a type mocked by Mockito.
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
          new Stubber.SimpleStubber(new Stubber[T, info.ParameterNames, A, R](mock, method, info))

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
      val info = validateAndRetrieveMethodInfo(method)
      Mock.withCleanup:
        val target = method(using Mockito.doCallRealMethod().when(mock))
        target(Tuple.fromArray(mapArguments[A, Any](info)(anyMatcher)).asInstanceOf[A])
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
              Mock
                .matchingInvocations(mock, method, info)
                .iterator
                .map: invocation =>
                  val args = Mock.unwrap[A](invocation.getRawArguments, info.parameterTypes)
                  Arguments[info.ParameterNames, A](Tuple.fromArray(args).asInstanceOf[A])
                .toList

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
      val info = validateAndRetrieveMethodInfo(method)
      Mock.matchingInvocations(mock, method, info).size

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
      val stubber = mock.on(method)
      stubber(args => method(using realInstance.asInstanceOf[Mock[T]])(Arguments.toTuple(args)))

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
      val infoA = validateAndRetrieveMethodInfo(a)
      val infoB = validateAndRetrieveMethodInfo(b)
      val ordered = Mockito.inOrder(mock)
      Mock.verifies:
        val targetA = a(using ordered.verify(mock, Mockito.atLeastOnce))
        targetA(Tuple.fromArray(mapArguments[A1, Any](infoA)(anyMatcher)).asInstanceOf[A1])
        val targetB = b(using ordered.verify(mock, Mockito.atLeastOnce))
        targetB(Tuple.fromArray(mapArguments[A2, Any](infoB)(anyMatcher)).asInstanceOf[A2])

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

  inline def verifies(f: => Any): Boolean =
    // Sometimes we need to resort to Mockito verifications with mode different than `atLeast(0)`.
    // In those cases, we must be careful not to swallow all exceptions.
    try
      withCleanup(f)
      true
    catch
      case _: MockitoAssertionError =>
        false

  inline def withCleanup[T](f: => T): T =
    // A failure halfway through stubbing or verifying leaves Mockito's thread-local state dirty,
    // which would fail the next Mockito interaction on this thread, possibly in another test.
    try
      f
    catch
      case e: Throwable =>
        // Each validation clears the problem it reports, so repeat until none is left.
        val _ = Iterator.continually(Try(Mockito.validateMockitoUsage())).take(32).find(_.isSuccess)
        throw e

  inline def matchingInvocations[T <: AnyRef, A <: Tuple, R](
      mock: Mock[T],
      inline method: Mock[T] ?=> MockedMethod[A, R],
      info: meta.MatchedMethodInfo
  ): Seq[Invocation] =
    var matching: Seq[Invocation] = Seq.empty
    val collect: VerificationMode =
      data =>
        matching = data.getAllInvocations.asScala.filter(data.getTarget.matches).toSeq
        // Like Mockito's own modes, so that `verifyNoMoreInteractions` accounts for them.
        matching.foreach(_.markVerified())
    withCleanup:
      val target = method(using Mockito.verify(mock, collect))
      target(Tuple.fromArray(mapArguments[A, Any](info)(anyMatcher)).asInstanceOf[A])
    matching

  inline def unwrap[A <: Tuple](
      originalArguments: Array[Object],
      parameterTypes: IndexedSeq[meta.TypeType],
      index: Int = 0,
      arguments: Array[Object] = Array.empty,
      needsCloning: Boolean = true
  ): Array[Object] =
    if needsCloning then
      unwrap[A](originalArguments, parameterTypes, index, originalArguments.clone(), false)
    else
      inline erasedValue[A] match
        case _: EmptyTuple =>
          arguments
        case _: (h *: t) =>
          val unwrapped =
            parameterTypes(index).maybeBox(arguments(index)) match
              // By-name parameters desugar to nullary functions, so apply and cache the result.
              case f: Function0[?] if parameterTypes(index) == meta.TypeType.ByName =>
                val v = f.apply()
                originalArguments.update(index, () => v)
                v
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
          unwrap[t](originalArguments, parameterTypes, index + 1, arguments, false)

  object mapper:

    inline def mapArguments[A <: Tuple, R: ClassTag](info: meta.MatchedMethodInfo)(
        inline f: [X] => (ClassTag[X]) ?=> R
    ): Array[R] =
      val parameterTypes = info.parameterTypes.iterator
      meta.mapTuple[A, R]([X] =>
        (ct: ClassTag[X]) ?=>
          if parameterTypes.next() == meta.TypeType.ByName then
            f[() => Any]
          else
            f[X](using ct)
      )

    val anyMatcher =
      [X] =>
        (ct: ClassTag[X]) ?=>
          val cls = ct.runtimeClass
          if cls.isArray then
            // Keep the array type for Java varargs matching.
            AdditionalMatchers
              .or(ArgumentMatchers.isNull(cls), ArgumentMatchers.any(cls))
              .asInstanceOf[X]
          else
            ArgumentMatchers.any[X]().asInstanceOf[X]

  def apply[T <: AnyRef](using ct: ClassTag[T]): Mock[T] =
    Mockito.mock(
      ct.runtimeClass.asInstanceOf[Class[T]],
      Mockito.withSettings().defaultAnswer(DefaultAnswer)
    )
