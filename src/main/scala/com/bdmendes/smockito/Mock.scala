package com.bdmendes.smockito

import org.mockito.Mockito
import scala.reflect.ClassTag

/** A `Mock` wraps an instance mocked by [[Mockito]]. One should work with this
  * type to be able to eta-expand on methods of the underlying type in order to
  * set up mocked methods and inspect respective calls in an expressive way.
  *
  * A `Mock[T]` is implicitly converted to a `T` in any call site where a `T` is
  * required. If interacting with a raw [[Mockito]] API, you may need to
  * explicitly call `underlying`.
  *
  * @param underlying
  *   the underlying instance.
  * @tparam T
  *   the instance type.
  */
class Mock[T] private (val underlying: T)

object Mock:

  given [T]: Conversion[Mock[T], T] with
    def apply(mock: Mock[T]): T = mock.underlying

  private[smockito] def apply[T](using ct: ClassTag[T]): Mock[T] =
    new Mock(Mockito.mock(ct.runtimeClass.asInstanceOf[Class[T]]))
