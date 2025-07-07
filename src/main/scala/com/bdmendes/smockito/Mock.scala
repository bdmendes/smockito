package com.bdmendes.smockito

import Mock.*
import com.bdmendes.smockito.Smockito.SmockitoException.*
import java.lang.reflect.Method
import org.mockito.*
import org.mockito.exceptions.misusing.*
import org.mockito.invocation.Invocation
import scala.Tuple.Size
import scala.compiletime.*
import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag
import scala.util.Try

/** A `Mock` represents a type mocked by Mockito. See [[Smockito.mock]] for more information.
  */
opaque type Mock[T] = T

private[smockito] trait MockSyntax:

  extension [T](mock: Mock[T])(using ct: ClassTag[T])

    private inline def matching[A <: Tuple, R: ClassTag](
        invocations: Iterable[Invocation]
    ): List[Method] =
      // Get all methods that may correspond to our types.
      // Due to erasure, we might get extra matches.
      val argClasses = summonClassTags[A].map(_.runtimeClass)
      val returnClass = summon[ClassTag[R]].runtimeClass
      invocations
        .map(_.getMethod)
        .filter { method =>
          returnClass == method.getReturnType && argClasses.sameElements(method.getParameterTypes)
        }
        .toList

    private inline def matchingStubs[A <: Tuple, R: ClassTag]: List[Method] =
      val invocations = Mockito.mockingDetails(mock).getStubbings.asScala.map(_.getInvocation)
      matching[A, R](invocations)

    /** Sets up a stub for a method. Refer to [[Smockito]] for a usage example.
      *
      * @param method
      *   the method to mock.
      * @param stub
      *   the stub implementation.
      * @return
      *   the mocked type.
      */
    inline def on[A1 <: Tuple, A2 <: Tuple, R1: ClassTag, R2: ClassTag](
        method: Mock[T] ?=> MockedMethod[A1, R1]
    )(using A1 =:= A2, R1 =:= R2, ValueOf[Size[A1]])(stub: PartialFunction[A2, R2]): Mock[T] =
      val args = Tuple.fromArray(mapTuple[A1, Any](anyMatcher)).asInstanceOf[A1]
      try
        Mockito
          .when(method(using mock).tupled.apply(args))
          .thenAnswer { invocation =>
            val arguments = invocation.getArguments

            // If this stub is invoked with nulls, assume we are in the process of setting up
            // another stub (i.e. using ArgumentMatchers.any ~ null).
            // Assuming a method won't normally receive null should not be a problem in the Scala
            // world.
            // Also, this does not collide with our verifications, as at least one argument captor
            // is used.
            if !arguments.isEmpty && matchingStubs[A1, R1].nonEmpty &&
              arguments.sameElements(Array.fill[Any](arguments.size)(null))
            then
              throw AlreadyStubbedMethod

            stub.applyOrElse(
              Tuple.fromArray(arguments).asInstanceOf[A2],
              _ => throw UnexpectedArguments(arguments)
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
      * @return
      *   the received arguments.
      */
    inline def calls[A <: Tuple: ClassTag, R: ClassTag](
        method: Mock[T] ?=> MockedMethod[A, R]
    )(using ValueOf[Size[A]]): List[A] =
      inline erasedValue[A] match
        case _: EmptyTuple =>
          // We could prevent calling this method in this case,
          // but for keeping the API homogeneous, let's allow it.
          List.fill(times[A, R](method))(EmptyTuple.asInstanceOf[A])

        case _ =>
          if matchingStubs[A, R].isEmpty then
            throw UnstubbedMethod

          val argCaptors = mapTuple[A, ArgumentCaptor[?]](captor)
          val _ =
            method(using Mockito.verify(mock, Mockito.atLeast(0)))
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
      * @return
      *   the number of calls to the stub.
      */
    inline def times[A <: Tuple: ClassTag, R: ClassTag](method: Mock[T] ?=> MockedMethod[A, R])(
        using ValueOf[Size[A]]
    ): Int =
      if matchingStubs[A, R].isEmpty then
        throw UnstubbedMethod

      inline erasedValue[A] match
        case _: EmptyTuple =>
          // Unfortunately, Mockito does not expose a reliable API for this use case.
          // We have to check all matching invocations, minding type erasure, and manually
          // validate the result.
          val invocations =
            matching[EmptyTuple, R](Mockito.mockingDetails(mock).getInvocations.asScala).size
          val validInvocations = (invocations to 1 by -1).find { count =>
            Try(
              method(using Mockito.verify(mock, Mockito.times(count)))
                .tupled
                .apply(EmptyTuple.asInstanceOf[A])
            ).isSuccess
          }
          validInvocations.getOrElse(0)
        case _: (h *: t) =>
          // We do a little trick here: capturing the first argument is enough for counting the
          // number of calls.
          val cap = mapTuple[h *: EmptyTuple, ArgumentCaptor[?]](captor).head
          val _ =
            method(using Mockito.verify(mock, Mockito.atLeast(0)))
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
