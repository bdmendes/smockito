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
