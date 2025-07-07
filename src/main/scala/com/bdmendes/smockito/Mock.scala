package com.bdmendes.smockito

import Mock.*
import com.bdmendes.smockito.Smockito.SmockitoException.*
import java.lang.reflect.Method
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.exceptions.misusing.*
import scala.Tuple.Size
import scala.compiletime.*
import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

/** A `Mock` represents a type mocked by Mockito. See [[Smockito.mock]] for more information.
  */
opaque type Mock[T] = T

private[smockito] trait MockSyntax:

  extension [T](mock: Mock[T])(using ct: ClassTag[T])

    private inline def matchingMethods[A <: Tuple, R: ClassTag]: List[Method] =
      val invocations = Mockito.mockingDetails(mock).getStubbings.asScala.map(_.getInvocation)
      val argClasses = summonClassTags[A].map(_.runtimeClass)
      val returnClass = summon[ClassTag[R]].runtimeClass
      invocations
        .map(_.getMethod)
        .filter { method =>
          returnClass == method.getReturnType && argClasses.sameElements(method.getParameterTypes)
        }
        .toList

    /** Sets up a stub for a method. Refer to [[Smockito]] for a usage example.
      *
      * @param method
      *   the method to mock.
      * @param strict
      *   whether to throw if the method was already stubbed.
      * @param stub
      *   the stub implementation.
      * @return
      *   the mocked type.
      */
    inline def on[A1 <: Tuple, A2 <: Tuple, R1: ClassTag, R2: ClassTag](
        method: Mock[T] ?=> MockedMethod[A1, R1],
        strict: Boolean = true
    )(using A1 =:= A2, R1 =:= R2, ValueOf[Size[A1]])(stub: PartialFunction[A2, R2]): Mock[T] =
      val args = Tuple.fromArray(mapTuple[A1, Any](anyMatcher)).asInstanceOf[A1]
      try
        Mockito
          .when(method(using mock).tupled.apply(args))
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
      catch
        case _: InvalidUseOfMatchersException | _: MissingMethodInvocationException =>
          throw NotAMethodOnType
        case e =>
          throw e
      mock

    /** Yields the captured arguments received by a stubbed method, in chronological order, in the
      * form of a tuple with the same shape as the method arguments. Refer to [[Smockito]] for a
      * usage example.
      *
      * @param method
      *   the mocked method.
      * @param strict
      *   whether to throw if the method was not stubbed.
      * @return
      *   the received arguments.
      */
    inline def calls[A <: Tuple: ClassTag, R: ClassTag](
        method: Mock[T] ?=> MockedMethod[A, R],
        strict: Boolean = true
    )(using ValueOf[Size[A]]): List[A] =
      if strict && matchingMethods[A, R].isEmpty then
        throw UnstubbedMethod

      inline erasedValue[A] match
        case _: EmptyTuple =>
          // Unfortunately, Mockito does not expose a reliable API for this use case.
          // As such, we may yield a value higher than the actual number of calls to this method.
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
              .tupled
              .apply(Tuple.fromArray(argCaptors.map(_.capture())).asInstanceOf[A])
          argCaptors
            .map(_.getAllValues.toArray)
            .transpose
            .map(Tuple.fromArray(_).asInstanceOf[A])
            .toList

    /** Yields the number of times a stub was called. Refer to [[Smockito]] for a usage example.
      *
      * @param method
      *   the mocked method.
      * @param strict
      *   whether to throw if the method was not stubbed.
      * @return
      *   the number of calls to the stub.
      */
    inline def times[A <: Tuple: ClassTag, R: ClassTag](
        method: Mock[T] ?=> MockedMethod[A, R],
        strict: Boolean = true
    )(using ValueOf[Size[A]]): Int =
      inline erasedValue[A] match
        case _: EmptyTuple =>
          mock.calls[A, R](method, strict).size
        case _: (h *: t) =>
          if strict && matchingMethods[A, R].isEmpty then
            throw UnstubbedMethod

          // We do a little trick here: capturing the first argument is enough for counting the
          // number of calls.
          val cap = mapTuple[h *: EmptyTuple, ArgumentCaptor[?]](captor).head
          val _ =
            method(using Mockito.verify(mock, atLeast(0)))
              .tupled
              .apply(Tuple.fromArray(cap.capture() +: mapTuple[t, Any](anyMatcher)).asInstanceOf[A])
          cap.getAllValues.size

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
        f[h]() +: mapTuple[t, R](f)

  private[smockito] inline def summonClassTags[T <: Tuple]: Array[ClassTag[?]] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Array.empty
      case _: (h *: t) =>
        summonInline[ClassTag[h]] +: summonClassTags[t]

  private[smockito] def apply[T](using ct: ClassTag[T]): Mock[T] =
    Mockito.mock(ct.runtimeClass.asInstanceOf[Class[T]])

  given [T]: Conversion[Mock[T], T] with
    def apply(mock: Mock[T]): T = mock
