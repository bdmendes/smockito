package com.bdmendes.smockito

import org.mockito.Mockito

/** A `Spy` is a copied instance, for interception purposes. See [[Smockito.spy]] for more
  * information.
  */
opaque type Spy[+T] <: Mock[T] = Mock[T]

private object Spy:

  def apply[T](realInstance: T): Spy[T] = Mockito.spy(realInstance).asInstanceOf[Mock[T]]
