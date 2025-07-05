package com.bdmendes.smockito

import Mock.*
import org.mockito.*
import org.mockito.Mockito.*
import scala.Tuple.Size
import scala.compiletime.*
import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

opaque type Mock[T] = T

extension [T](mock: Mock[T])

  inline def on[A1 <: Tuple, A2 <: Tuple, R1, R2](
      method: Mock[T] ?=> MockedMethod[A1, R1]
  )(using A1 =:= A2, R1 =:= R2, ValueOf[Size[A1]])(stub: PartialFunction[A2, R2]): Mock[T] =
    val args = Tuple.fromArray(mapTuple[A1, Any](anyMatcher)).asInstanceOf[A1]
    Mockito
      .when(method(using mock).underlying.apply(args))
      .thenAnswer { invocation =>
        val arguments = Tuple.fromArray(invocation.getArguments).asInstanceOf[A2]
        stub.applyOrElse(
          arguments,
          _ =>
            throw new IllegalArgumentException(
              s"Mocked method received unexpected arguments: $arguments"
            )
        )
      }
    mock

  inline def calls[A <: Tuple: ClassTag, R: ClassTag](
      method: Mock[T] ?=> MockedMethod[A, R]
  ): List[A] =
    inline erasedValue[A] match
      case _: EmptyTuple =>
        // Unfortunately, Mockito does not expose a reliable API for this use case.
        // This is not resilient against multiple no-arg methods on the same class with the same
        // return type.
        val invocations = Mockito.mockingDetails(mock).getInvocations
        val matchingInvocations =
          invocations
            .asScala
            .count { invocation =>
              val mockitoMethod = invocation.getMethod
              mockitoMethod.getReturnType == summon[ClassTag[R]].runtimeClass &&
              mockitoMethod.getParameterCount == 0
            }
        List.fill(matchingInvocations)(EmptyTuple.asInstanceOf[A])

      case _ =>
        val argCaptors = mapTuple[A, ArgumentCaptor[?]](captor)
        val _ =
          method(using Mockito.verify(mock, atLeast(0)))
            .underlying
            .apply(Tuple.fromArray(argCaptors.map(_.capture())).asInstanceOf[A])
        argCaptors
          .map(_.getAllValues.toArray)
          .transpose
          .map(Tuple.fromArray(_).asInstanceOf[A])
          .toList

object Mock:

  private[smockito] lazy val anyMatcher = [X] => () => ArgumentMatchers.any[X]()
  private[smockito] lazy val captor = [X] => () => ArgumentCaptor.captor[X]()

  private[smockito] inline def mapTuple[T <: Tuple, R: ClassTag](
      inline f: [X] => () => R
  ): Array[R] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Array.empty
      case _: (h *: t) =>
        val head = f[h]()
        val tail = mapTuple[t, R](f)
        head +: tail

  private[smockito] def apply[T](using ct: ClassTag[T]): Mock[T] =
    Mockito.mock(ct.runtimeClass.asInstanceOf[Class[T]])

  given [T]: Conversion[Mock[T], T] with
    def apply(mock: Mock[T]): T = mock
