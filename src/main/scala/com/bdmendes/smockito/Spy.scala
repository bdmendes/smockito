package com.bdmendes.smockito

import org.mockito.Mockito

/** A `Spy` is a copied instance, for interception purposes. All methods of this instance behave as
  * the real instance, unless stubbed otherwise.
  */
opaque type Spy[+T] <: Mock[T] = Mock[T]

private object Spy:

  def apply[T](realInstance: T): Spy[T] = Mockito.spy(realInstance).asInstanceOf[Mock[T]]
