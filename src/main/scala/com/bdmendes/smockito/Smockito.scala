package com.bdmendes.smockito

import scala.reflect.ClassTag

/** Creates a [[Mock]] instance of `T`.
  *
  * A `Mock[T]` is the [[Smockito]] compile time representation of a type mocked by Mockito, erased
  * at runtime. As such, after you set up method stubs, you may pass a mock anywhere a `T` is
  * needed.
  *
  * @tparam T
  *   the type to mock.
  * @return
  *   the mock instance.
  */
def mock[T: ClassTag]: Mock[T] = Mock.apply

/** Creates a [[Spy]] instance of `T`.
  *
  * A `Spy[T]` is the [[Smockito]] compile time representation of a type spied by Mockito, erased at
  * runtime. Keep in mind that Mockito spies are copies of real instances, so any method side
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
