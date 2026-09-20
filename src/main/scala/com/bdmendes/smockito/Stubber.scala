package com.bdmendes.smockito

import Mock.mapper.anyMatcher
import Mock.unwrap
import com.bdmendes.smockito.Smockito.SmockitoException.*
import com.bdmendes.smockito.internal.meta
import java.util.concurrent.atomic.AtomicInteger
import org.mockito.Mockito
import org.mockito.stubbing.Answer

class Stubber[T <: AnyRef, N <: Tuple, A <: Tuple, R](
    private val mock: Mock[T],
    private val method: Mock[T] ?=> MockedMethod[A, R],
    private val info: meta.MatchedMethodInfo
):

  inline def apply(
      inline stub: Mock[T] ?=> PartialFunction[Int, PartialFunction[Arguments[N, A], R]]
  ): Mock[T] =
    val callCount = AtomicInteger(0)
    val answer: Answer[R] =
      invocation =>
        val call = callCount.incrementAndGet()
        val f = stub(using mock).applyOrElse(call, _ => throw UnexpectedCallNumber(call))
        val arguments = unwrap[A](invocation.getRawArguments, info.parameterTypes)
        f.applyOrElse(
          Arguments[N, A](Tuple.fromArray(arguments).asInstanceOf[A]),
          _ => throw UnexpectedArguments(invocation.getMethod, arguments)
        )
    val target = method(using Mockito.doAnswer(answer).when(mock))
    target.tupled(Tuple.fromArray(meta.mapTuple[A, Any](anyMatcher)).asInstanceOf[A])
    mock

object Stubber:

  class SimpleStubber[T <: AnyRef, N <: Tuple, A <: Tuple, R](
      private[smockito] val stubber: Stubber[T, N, A, R]
  ):

    inline def apply(inline stub: Mock[T] ?=> PartialFunction[Arguments[N, A], R]): Mock[T] =
      stubber(PartialFunction.fromFunction(_ => stub))

  // Support arguments as raw tuples as a fallback.
  extension [T <: AnyRef, N <: Tuple, A <: Tuple, R](stubber: Stubber[T, N, A, R])

    inline def apply(
        inline stub: Mock[T] ?=> PartialFunction[Int, PartialFunction[A, R]]
    ): Mock[T] =
      stubber(stub.andThen(_.compose[Arguments[N, A]](args => Arguments.toTuple[N, A](args))))

  extension [T <: AnyRef, N <: Tuple, A <: Tuple, R](stubber: SimpleStubber[T, N, A, R])

    inline def apply(inline stub: Mock[T] ?=> PartialFunction[A, R]): Mock[T] =
      stubber(stub.compose[Arguments[N, A]](args => Arguments.toTuple[N, A](args)))
