package com.bdmendes.smockito

import org.mockito.Mockito
import scala.reflect.ClassTag

opaque type Mock[T] = T

object Mock:

  given [T]: Conversion[Mock[T], T] with
    def apply(mock: Mock[T]): T = mock

  private[smockito] def apply[T](using ct: ClassTag[T]): Mock[T] =
    Mockito.mock(ct.runtimeClass.asInstanceOf[Class[T]])
