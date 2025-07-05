package com.bdmendes.smockito

import scala.reflect.ClassTag

trait Smockito:

  /** Creates a [[Mock]] instance of `T`.
    *
    * @tparam T
    *   the type to mock.
    * @return
    *   the mock instance.
    */
  def mock[T: ClassTag]: Mock[T] = Mock.apply

  /** Retrieves the mock in scope.
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

  enum SmockitoException(val msg: String) extends Exception(s"${msg}\n${exceptionTrailer}"):

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
            "through a state lookup in the stub."
        )
