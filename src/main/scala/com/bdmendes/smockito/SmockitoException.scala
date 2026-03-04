package com.bdmendes.smockito

import java.lang.reflect.Method

sealed abstract class SmockitoException private[smockito] (msg: String) extends Exception(msg)

object SmockitoException:

  private def describeMethod(method: Method): String =
    s"The method ${method.getName} of class ${method.getDeclaringClass.getName}"

  case class UnknownMethod private[smockito] ()
      extends SmockitoException(
        s"The received method does not match any of the mock object's methods. " +
          "Are you performing eta-expansion correctly? " +
          "Double-check if this method has contextual parameters and " +
          "they are inadvertently being captured in the spec scope, " +
          "one or more default parameters are being discarded, " +
          "or a variable number of arguments is being fixed."
      )

  case class UnexpectedArguments private[smockito] (method: Method, arguments: Array[Object])
      extends SmockitoException(
        s"${describeMethod(method)} received unexpected arguments: " +
          s"(${arguments.mkString(", ")}). " + "Did you forget to handle this case at the stub?"
      )

  case class UnexpectedCallNumber private[smockito] (callNumber: Int)
      extends SmockitoException(
        s"The method was called an unexpected number of times: $callNumber. " +
          "Did you forget to handle this call number at the stub?"
      )

  case class UnstubbedMethod private[smockito] (method: Method, arguments: Array[Object])
      extends SmockitoException(
        s"${describeMethod(method)} is not stubbed " +
          s"and was called with arguments: (${arguments.mkString(", ")}). " +
          "Did you forget to stub the method, or was it called unexpectedly?"
      )

  case class UnexpectedType private[smockito] (value: Any, expected: Class[?])
      extends SmockitoException(
        s"Expected a ${expected.getName}, but got $value which is of type " +
          s"${value.getClass.getName}. You may have defined a stub for a fixed " +
          "type parameter, a fixed number of parameters, or be hitting a " + "Smockito limitation."
      )
