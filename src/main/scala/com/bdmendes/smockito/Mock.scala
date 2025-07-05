package com.bdmendes.smockito

import Mock.*
import com.bdmendes.smockito.Smockito.SmockitoException.*
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.exceptions.misusing.*
import scala.Tuple.Size
import scala.compiletime.*
import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

opaque type Mock[T] = T

extension [T](mock: Mock[T])(using ct: ClassTag[T])

  private inline def searchStub[A <: Tuple, R: ClassTag](onExists: Boolean => Unit): Unit =
    val invocations = Mockito.mockingDetails(mock).getStubbings.asScala.map(_.getInvocation)
    val argClasses = summonClassTags[A].map(_.runtimeClass)
    val returnClass = summon[ClassTag[R]].runtimeClass
    val exists =
      invocations.exists { invocation =>
        val mockitoMethod = invocation.getMethod
        returnClass == mockitoMethod.getReturnType &&
        argClasses.sameElements(mockitoMethod.getParameterTypes)
      }
    onExists(exists)

  inline def on[A1 <: Tuple, A2 <: Tuple, R1: ClassTag, R2: ClassTag](
      method: Mock[T] ?=> MockedMethod[A1, R1],
      strict: Boolean = true
  )(using A1 =:= A2, R1 =:= R2, ValueOf[Size[A1]])(stub: PartialFunction[A2, R2]): Mock[T] =
    if strict then
      searchStub[A1, R1] { exists =>
        if exists then
          throw AlreadyStubbedMethod
      }

    val args = Tuple.fromArray(mapTuple[A1, Any](anyMatcher)).asInstanceOf[A1]
    try
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
    catch
      case _: InvalidUseOfMatchersException | _: MissingMethodInvocationException =>
        throw NotAMethodOnType(ct)
      case e =>
        throw e
    mock

  inline def calls[A <: Tuple: ClassTag, R: ClassTag](
      method: Mock[T] ?=> MockedMethod[A, R],
      strict: Boolean = true
  )(using ValueOf[Size[A]]): List[A] =
    if strict then
      searchStub[A, R](exists =>
        if !exists then
          throw UnstubbedMethod
      )

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
            .underlying
            .apply(Tuple.fromArray(argCaptors.map(_.capture())).asInstanceOf[A])
        argCaptors
          .map(_.getAllValues.toArray)
          .transpose
          .map(Tuple.fromArray(_).asInstanceOf[A])
          .toList

  inline def times[A <: Tuple: ClassTag, R: ClassTag](
      method: Mock[T] ?=> MockedMethod[A, R],
      strict: Boolean = true
  )(using ValueOf[Size[A]]): Int =
    inline erasedValue[A] match
      case _: EmptyTuple =>
        mock.calls[A, R](method, strict).size
      case _: (h *: t) =>
        if strict then
          searchStub[A, R](exists =>
            if !exists then
              throw UnstubbedMethod
          )

        // We do a little trick here: capturing the first argument is enough for counting the
        // number of calls.
        val cap = mapTuple[h *: EmptyTuple, ArgumentCaptor[?]](captor).head
        val _ =
          method(using Mockito.verify(mock, atLeast(0)))
            .underlying
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
