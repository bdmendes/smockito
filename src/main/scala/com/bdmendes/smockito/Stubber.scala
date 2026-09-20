package com.bdmendes.smockito

import Mock.mapper.anyMatcher
import Mock.unwrap
import com.bdmendes.smockito.Smockito.SmockitoException.*
import com.bdmendes.smockito.internal.meta
import java.util.concurrent.atomic.AtomicInteger
import org.mockito.Mockito
import org.mockito.stubbing.Answer

class Stubber[T <: AnyRef, N <: Tuple, A <: Tuple, R](
    private[smockito] val mock: Mock[T],
    private[smockito] val method: Mock[T] ?=> MockedMethod[A, R],
    private[smockito] val info: meta.MatchedMethodInfo
):

  inline def apply(
      inline stub: Mock[T] ?=> PartialFunction[Int, PartialFunction[Pack[N, A], R]]
  ): Mock[T] =
    val callCount = AtomicInteger(0)
    val answer: Answer[R] =
      invocation =>
        val call = callCount.incrementAndGet()
        val f = stub(using mock).applyOrElse(call, _ => throw UnexpectedCallNumber(call))
        val arguments = unwrap[A](invocation.getRawArguments, info.parameterTypes)
        f.applyOrElse(
          Pack[N, A](Tuple.fromArray(arguments).asInstanceOf[A]),
          _ => throw UnexpectedArguments(invocation.getMethod, arguments)
        )
    val target = method(using Mockito.doAnswer(answer).when(mock))
    target.tupled(Tuple.fromArray(meta.mapTuple[A, Any](anyMatcher)).asInstanceOf[A])
    mock

object Stubber:

  // Extension fallbacks keep contextual lambda inference unambiguous.
  extension [T <: AnyRef, N <: Tuple, A <: Tuple, R](configure: Stubber[T, N, A, R])

    inline def apply(
        inline stub: Mock[T] ?=> PartialFunction[Int, PartialFunction[A, R]]
    ): Mock[T] = configure(stub.andThen(_.compose[Pack[N, A]](args => Pack.toTuple[N, A](args))))

  class On[T <: AnyRef, N <: Tuple, A <: Tuple, R](
      private[smockito] val configure: Stubber[T, N, A, R]
  ):

    inline def apply(inline stub: Mock[T] ?=> PartialFunction[Pack[N, A], R]): Mock[T] =
      configure(PartialFunction.fromFunction(_ => stub))

  extension [T <: AnyRef, N <: Tuple, A <: Tuple, R](configure: On[T, N, A, R])

    inline def apply(inline stub: Mock[T] ?=> PartialFunction[A, R]): Mock[T] =
      configure(stub.compose[Pack[N, A]](args => Pack.toTuple[N, A](args)))
