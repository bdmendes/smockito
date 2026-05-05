package com.bdmendes.smockito

import java.lang.reflect.Method
import scala.reflect.ClassTag

/** The trait to mix in for using Smockito facilities in a specification. It includes the [[mock]]
  * and [[spy]] methods for creating mocks, and the various syntax extensions on them, such as
  * [[on]], [[calls]] and [[times]]. Those extensions expect method references via
  * [[https://docs.scala-lang.org/scala3/reference/contextual/context-functions.html context functions]]
  * with a [[Mock]] context parameter, retrievable via the [[it]] method.
  */
trait Smockito extends MockSyntax:

  /** Creates a [[Mock]] instance of `T`. A `Mock[T]` is the compile time representation of an
    * instance of `T` mocked by Mockito, erased at runtime, whose default answer is to throw.
    *
    * @tparam T
    *   the type to mock.
    * @return
    *   the mock instance.
    */
  def mock[T: ClassTag]: Mock[T] = Mock.apply

  /** Creates a [[Spy]] instance of `T`. A `Spy[T]` is the compile time representation of a real
    * instance of `T` copied by Mockito for spying purposes, erased at runtime.
    *
    * @tparam T
    *   the type of the real instance to spy on.
    * @return
    *   the spy instance.
    */
  def spy[T: ClassTag](realInstance: T): Spy[T] = Spy.apply(realInstance)

  /** Retrieves the mock in scope. This is the recommended way to refer to a mock available in
    * context, as is the case when using methods of [[Mock]].
    *
    * @param mock
    *   the mock in scope.
    * @tparam T
    *   the mocked type.
    * @return
    *   the mock in scope.
    */
  def it[T](using mock: Mock[T]): T = mock

object Smockito:

  private def describeMethod(method: Method): String =
    s"The method ${method.getName} of class ${method.getDeclaringClass.getName}"

  sealed abstract class SmockitoException private[smockito] (msg: String) extends Exception(msg)

  object SmockitoException:

    case class UnexpectedArguments private[smockito] (method: Method, arguments: Array[Object])
        extends SmockitoException(
          s"${describeMethod(method)} received unexpected arguments: " +
            s"(${arguments.mkString(", ")})."
        )

    case class UnexpectedCallNumber private[smockito] (callNumber: Int)
        extends SmockitoException(
          s"The method was called an unexpected number of times: $callNumber."
        )

    case class UnstubbedMethod private[smockito] (method: Method, arguments: Array[Object])
        extends SmockitoException(
          s"${describeMethod(method)} is not stubbed " +
            s"and was called with arguments: (${arguments.mkString(", ")})."
        )

    case class UnexpectedType private[smockito] (value: Any, expected: Class[?])
        extends SmockitoException(
          s"Expected a ${expected.getName}, but got $value which is of type " +
            s"${value.getClass.getName}."
        )
