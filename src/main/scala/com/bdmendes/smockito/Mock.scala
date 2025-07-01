package com.bdmendes.smockito

import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import scala.reflect.ClassTag

opaque type Mock[T] = T

extension [T](mock: Mock[T])

  def on[A1, A2, R1, R2](
      method: T => A1 => R1
  )(using A1 =:= A2, R1 =:= R2)(stub: PartialFunction[A2, R2]): Mock[T] =
    Mockito
      .when(method(mock).apply(ArgumentMatchers.any()))
      .thenAnswer { invocation =>
        val argument = invocation.getArgument[A1](0)
        stub.applyOrElse(
          argument,
          _ =>
            throw new IllegalArgumentException(
              s"Mocked method received unexpected parameters: $argument"
            )
        )
      }
    mock

object Mock:

  given [T]: Conversion[Mock[T], T] with
    def apply(mock: Mock[T]): T = mock

  private[smockito] def apply[T](using ct: ClassTag[T]): Mock[T] =
    Mockito.mock(ct.runtimeClass.asInstanceOf[Class[T]])
