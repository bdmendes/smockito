package com.bdmendes.smockito

import java.lang.reflect.Method
import scala.reflect.ClassTag

/** The trait to mix in for using Smockito facilities in a specification. It includes the [[mock]]
  * and [[spy]] methods for creating mocks, and the various syntax extensions on them, such as
  * [[on]], [[calls]] and [[times]]. Those extensions expect method references via
  * [[https://docs.scala-lang.org/scala3/reference/contextual/context-functions.html context functions]]
  * with a [[Mock]] context parameter, retrievable via the [[it]] method.
  *
  * Checkout [[https://github.com/bdmendes/smockito the documentation]] for more information and
  * usage patterns.
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

  sealed abstract class SmockitoException(msg: String) extends Exception(msg)

  object SmockitoException:

    case object UnknownMethod
        extends SmockitoException(
          s"The received method does not match any of the mock object's methods. " +
            "Are you performing eta-expansion correctly? " +
            "Double-check if this method has contextual parameters and " +
            "they are inadvertently being captured in the spec scope, " +
            "or if one or more default parameters are being discarded."
        )

    case class UnexpectedArguments(method: Method, arguments: Array[Object])
        extends SmockitoException(
          s"${describeMethod(method)} received unexpected arguments: " +
            s"(${arguments.mkString(", ")}). " + "Did you forget to handle this case at the stub?"
        )

    case class UnstubbedMethod(method: Method, arguments: Array[Object])
        extends SmockitoException(
          s"${describeMethod(method)} is not stubbed " +
            s"and was called with arguments: (${arguments.mkString(", ")}). " +
            "Did you forget to stub the method, or was it called unexpectedly?"
        )
