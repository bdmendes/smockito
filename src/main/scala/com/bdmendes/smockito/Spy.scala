package com.bdmendes.smockito

import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import scala.reflect.ClassTag

/** A `Spy` is a mock whose default answer is to forward all method calls to a real instance, unless
  * stubbed otherwise.
  */
opaque type Spy[+T] <: Mock[T] = Mock[T]

private object Spy:

  class SpyAnswer(obj: Any) extends Answer[Any]:

    override def answer(invocation: InvocationOnMock): Any =
      invocation.getMethod.invoke(obj, invocation.getRawArguments*)

  def apply[T](realInstance: T)(using ct: ClassTag[T]): Spy[T] =
    Mockito
      .mock(
        ct.runtimeClass.asInstanceOf[Class[T]],
        Mockito.withSettings().defaultAnswer(SpyAnswer(realInstance))
      )
      .asInstanceOf[Mock[T]]
