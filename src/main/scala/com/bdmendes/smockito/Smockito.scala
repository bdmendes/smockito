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

  /** Creates a [[Mock]] instance of `T`.
    *
    * A `Mock[T]` is the [[Smockito]] compile time representation of a type mocked by Mockito,
    * erased at runtime. As such, after you set up method stubs, you may pass a mock anywhere a `T`
    * is needed.
    *
    * @tparam T
    *   the type to mock.
    * @return
    *   the mock instance.
    */
  def mock[T: ClassTag]: Mock[T] = Mock.apply

  /** Creates a [[Spy]] instance of `T`.
    *
    * A `Spy[T]` is the [[Smockito]] compile time representation of a type spied by Mockito, erased
    * at runtime. Keep in mind that Mockito spies are copies of real instances, so any method side
    * effects won't affect the original object.
    *
    * A `Spy[T]` is also a `Mock[T]`, so you may use all methods available on [[Mock]].
    *
    * If you only need to forward a few methods to a real instance, consider using the [[forward]]
    * method on a [[Mock]] instead for clarity and performance.
    *
    * @tparam T
    *   the type to spy.
    * @return
    *   the spy instance.
    */
  def spy[T](realInstance: T): Spy[T] = Spy.apply(realInstance)

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
            "type parameter, a fixed number of parameters, or be hitting a " +
            "Smockito limitation."
        )
