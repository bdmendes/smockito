package com.bdmendes.smockito

import Mock.anyMatchers
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import scala.Tuple.Size
import scala.compiletime.constValue
import scala.compiletime.erasedValue
import scala.compiletime.summonInline
import scala.reflect.ClassTag

opaque type Mock[T] = T

extension [T](mock: Mock[T])

  inline def on[A1 <: Tuple, A2 <: Tuple, R1, R2](
      method: Mock[T] ?=> MockedMethod[A1, R1]
  )(using A1 =:= A2, R1 =:= R2, ValueOf[Size[A1]])(stub: PartialFunction[A2, R2]): Mock[T] =
    Mockito
      .when(method(using mock).underlying.apply(Tuple.fromArray(anyMatchers[A1]).asInstanceOf[A1]))
      .thenAnswer { invocation =>
        val arguments = Tuple.fromArray(invocation.getArguments).asInstanceOf[A2]
        stub.applyOrElse(
          arguments,
          _ =>
            throw new IllegalArgumentException(
              s"Mocked method received unexpected parameters: $arguments"
            )
        )
      }
    mock

object Mock:

  private[smockito] inline def anyMatchers[T <: Tuple]: Array[Any] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Array.empty
      case _: (h *: t) =>
        val head = ArgumentMatchers.any[h]
        val tail = anyMatchers[t]
        head +: tail

  given [T]: Conversion[Mock[T], T] with
    def apply(mock: Mock[T]): T = mock

  private[smockito] def apply[T](using ct: ClassTag[T]): Mock[T] =
    Mockito.mock(ct.runtimeClass.asInstanceOf[Class[T]])
