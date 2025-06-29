package com.bdmendes.smockito

import org.mockito.Mockito

import scala.reflect.ClassTag

trait Smockito:
  def mock[T](using ct: ClassTag[T]): Mock[T] = Mock(
    Mockito.mock(ct.runtimeClass.asInstanceOf[Class[T]])
  )
