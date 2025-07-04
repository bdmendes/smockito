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
