package com.bdmendes.smockito

import org.mockito.AdditionalAnswers
import org.mockito.Mockito
import org.mockito.exceptions.base.MockitoException
import scala.reflect.ClassTag

/** A `Spy` is a mock whose default answer is to forward all method calls to a real instance, unless
  * stubbed otherwise.
  */
opaque type Spy[+T] <: Mock[T] = Mock[T]

private object Spy:

  def apply[T](realInstance: T)(using ct: ClassTag[T]): Spy[T] =
    try
      Mockito.spy(realInstance).asInstanceOf[Mock[T]]
    catch
      case _: MockitoException =>
        // A "delegating mock" is less powerful than a spy, since it cannot record internal
        // interactions, but it can be used as a fallback when the type cannot be spied.
        Mockito
          .mock(
            ct.runtimeClass.asInstanceOf[Class[T]],
            Mockito.withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(realInstance))
          )
          .asInstanceOf[Mock[T]]
