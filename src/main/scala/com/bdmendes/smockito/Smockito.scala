package com.bdmendes.smockito

import java.lang.reflect.Method
import scala.reflect.ClassTag

/** [[https://github.com/bdmendes/smockito Smockito]] is a tiny framework-agnostic facade for
  * `Mockito`.
  *
  * {{{
  *   abstract class Repository[T](val name: String):
  *      def get: List[T]
  *      def exists(username: String): Boolean
  *      def getWith(startsWith: String, endsWith: String): List[T]
  *
  *   case class User(username: String)
  *
  *   class RepositorySpecification extends Smockito:
  *      // Chain stubs to set up a mock instance.
  *      val repository = mock[Repository[User]]
  *          .on(() => it.get)(_ => List(User("johndoe")))
  *          .on(it.getWith) {
  *              case ("john", "doe") => User("johndoe")
  *          } // Mock[Repository[User]]
  *
  *      // A `Mock[T]` is effectively a `T`, both at compile and runtime.
  *      assert(repository.getWith("john", "doe") == User("johndoe"))
  *
  *      // Observe the past method interactions.
  *      assert(repository.calls(it.getWith) == List(("john", "doe")))
  *      assert(repository.times(it.getWith) == 1)
  * }}}
  *
  * All methods on [[Mock]] require an
  * [[https://docs.scala-lang.org/scala3/book/fun-eta-expansion.html eta-expanded method]] as the
  * first argument. The [[it]] shorthand is a terse way of capturing the mocked type in context.
  *
  * Method stubs are set up with [[on]]. Besides the method to mock, it requires a
  * [[PartialFunction]] to handle the expected inputs, represented as a well-typed tuple, with the
  * same shape as the mocked method arguments, that one may destructure.
  *
  * [[calls]] provides the captured arguments of all the past invocations of a stubbed method, in
  * chronological order, in the form of a tuple with the same shape as the method arguments, à la
  * `scalamock`. If one only cares about the number of times a stub was called, [[times]] is more
  * efficient.
  *
  * [[Mock]] is interoperable with all [[org.mockito.Mockito]] APIs.
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

  private lazy val exceptionTrailer =
    s"Please review the documentation at https://github.com/bdmendes/smockito. " +
      "If you think this is a bug, please open an issue with a minimal reproducible example."

  private def describeMethod(method: Method): String =
    s"The method ${method.getName} of class ${method.getDeclaringClass.getName}"

  sealed trait SmockitoException(val msg: String) extends Exception:
    override def getMessage(): String = s"$msg\n$exceptionTrailer"

  object SmockitoException:

    case object UnstubbedMethod
        extends SmockitoException(
          s"The received method was not stubbed, so you cannot reason about it. " +
            "Are you performing eta-expansion correctly? " +
            "Did you forget to set up the stub first?"
        )

    case class AlreadyStubbedMethod(method: Method)
        extends SmockitoException(
          s"${describeMethod(
              method
            )} is already stubbed. If you need to perform a different action " +
            "on a subsequent invocation, replace the mock or reflect that intent " +
            "through a state lookup in the stub."
        )

    case class UnexpectedArguments(method: Method, arguments: Array[Object])
        extends SmockitoException(
          s"${describeMethod(method)} received unexpected arguments: (${arguments.mkString(
              ", "
            )}). " + "Did you forget to handle this case at the stub?"
        )
