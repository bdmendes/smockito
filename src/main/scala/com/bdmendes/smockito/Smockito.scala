package com.bdmendes.smockito

import scala.reflect.ClassTag

/** [[https://github.com/bdmendes/smockito Smockito]] is a tiny framework-agnostic facade for
  * `Mockito`.
  *
  * {{{
  *   trait Specification extends Smockito:
  *      // Chain stubs to set up a mock instance.
  *      val repository = mock[Repository[User]]
  *         .on(it.exists)(args => mockUsers.map(_.username).contains(args._1))
  *         .on(() => it.get)(_ => mockUsers)
  *         .on(it.getWith) { case (start, end) =>
  *           mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
  *         } // Mock[Repository[T]]
  *
  *      // A `Mock[T]` is effectively a `T`, both at compile and runtimes.
  *      assert(repository.getWith("john", "doe") == User("johndoe")
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
  * Method stubs are set up with [[Mock.on]]. Besides the method to mock, it requires a
  * [[PartialFunction]] to handle the expected inputs, represented as a well-typed tuple, with the
  * same shape as the mocked method arguments, that one may destructure.
  *
  * [[Mock.calls]] provides the captured arguments of all the past invocations of a stubbed method,
  * in chronological order, in the form of a tuple with the same shape as the method arguments, à la
  * `scalamock`. If one only cares about the number of times a stub was called, [[Mock.times]] is
  * more efficient.
  *
  * Besides the obvious type safety differences at compile time, [[Smockito]] is more opinionated
  * than `Mockito` regarding the way one is allowed to set up mocks at runtime:
  *   - It will throw when a stub override is provided, discouraging a change of behaviour during
  *     the lifetime of a mock. The stub should be unique, predictable and handle all relevant cases
  *     upfront.
  *   - It will throw when `calls` or `times` is called on an unstubbed method.
  *
  * [[Mock]] is interoperable with all [[org.mockito.Mockito]] APIs.
  */
trait Smockito:

  /** Creates a [[Mock]] instance of `T`.
    *
    * A `Mock[T]` is the [[Smockito]] compile time representation of a type mocked by Mockito,
    * erased at runtime. As such, after you set up method stubs, you may pass a mock anywhere a `T`
    * is needed.
    *
    * See [[Mock.on]], [[Mock.calls]] and [[Mock.times]] for the methods available on `Mock`.
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

  enum SmockitoException(val msg: String) extends Exception(s"$msg\n$exceptionTrailer"):

    case NotAMethodOnType[T](ct: ClassTag[T])
        extends SmockitoException(
          s"The received method does not exist on ${ct.toString()}. " +
            "Are you performing eta-expansion correctly?"
        )

    case UnstubbedMethod
        extends SmockitoException(
          s"The received method was not stubbed, so you cannot reason about it. " +
            "Are you performing eta-expansion correctly? " +
            "Did you forget to set up the stub first?"
        )

    case AlreadyStubbedMethod
        extends SmockitoException(
          s"The received method already has a stub. If you need to perform a different action " +
            "on a subsequent invocation, replace the mock or reflect that intent " +
            "through a state lookup in the stub.\n" +
            "Mind that Smockito identifies stubbed methods by signature. If you previously mocked " +
            "a different method with the same signature, this behaviour might be undesired. " +
            "In that case, you may disable this check at the call site."
        )
