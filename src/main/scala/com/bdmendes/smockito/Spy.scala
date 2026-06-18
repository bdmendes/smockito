package com.bdmendes.smockito

import com.bdmendes.smockito.internal.ForwardingFunction
import org.mockito.AdditionalAnswers
import org.mockito.Mockito
import org.mockito.exceptions.base.MockitoException
import scala.reflect.ClassTag

/** A `Spy` is a mock whose default answer is to forward all method calls to a real instance, unless
  * stubbed otherwise.
  */
opaque type Spy[+T] <: Mock[T] = Mock[T]

private object Spy:

  // scalafmt: { maxColumn = 120 }

  def apply[T](realInstance: T)(using ct: ClassTag[T]): Spy[T] =
    try
      Mockito.spy(realInstance).asInstanceOf[Mock[T]]
    catch
      case _: MockitoException =>
        // Lambdas require special treatment as they are usually synthetic classes that Mockito
        // cannot copy. We try this as a fallback as other classes can also extend Function and this
        // would be incorrect as a first attempt.
        try
          val realInstanceProxy =
            realInstance match
              case f: Function0[?] =>
                ForwardingFunction(f)
              case f: Function1[?, ?] =>
                ForwardingFunction(f)
              case f: Function2[?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function3[?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function4[?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function5[?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function6[?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function7[?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function8[?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function9[?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function10[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function11[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function12[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function13[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function14[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function15[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function16[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function17[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function18[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function19[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function20[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function21[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case f: Function22[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?] =>
                ForwardingFunction(f)
              case _ =>
                realInstance
          Mockito.spy(realInstanceProxy.asInstanceOf[T]).asInstanceOf[Mock[T]]
        catch
          case _ =>
            // A "delegating mock" is less powerful than a spy, since it cannot record internal
            // interactions, but it can be used as a fallback when the type cannot be spied.
            Mockito
              .mock(
                ct.runtimeClass.asInstanceOf[Class[T]],
                Mockito.withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(realInstance))
              )
              .asInstanceOf[Mock[T]]
