package com.bdmendes.smockito

import com.bdmendes.smockito.Mock.mapper.anyMatcher
import com.bdmendes.smockito.Mock.unwrap
import com.bdmendes.smockito.Smockito.SmockitoException.*
import com.bdmendes.smockito.internal.meta
import java.util.concurrent.atomic.AtomicInteger
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import scala.annotation.publicInBinary

/** Configures a mocked method's response based on its one-based call number and arguments. Calls
  * with an unmatched call number or arguments throw an exception.
  */
class Stubber[T <: AnyRef, N <: Tuple, A <: Tuple, R] @publicInBinary private[smockito] (
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
    target(Tuple.fromArray(meta.mapTuple[A, Any](anyMatcher)).asInstanceOf[A])
    mock

object Stubber:

  class SimpleStubber[T <: AnyRef, N <: Tuple, A <: Tuple, R](
      private val stubber: Stubber[T, N, A, R]
  ):

    inline def apply(inline stub: Mock[T] ?=> PartialFunction[Arguments[N, A], R]): Mock[T] =
      stubber(PartialFunction.fromFunction(_ => stub))
