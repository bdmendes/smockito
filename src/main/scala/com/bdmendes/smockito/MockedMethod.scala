package com.bdmendes.smockito

/** Representation of a mocked method, implicitly converted from an eta-expanded
  * method.
  *
  * @tparam P
  *   the (possibly tupled) types of the method parameters.
  * @tparam R
  *   the return type.
  */
trait MockedMethod[P, R]:

  /** Sets up an implementation for the method for the set of arguments in the
    * domain of the given partial function. If this method is called with other
    * arguments, it will throw a [[IllegalArgumentException]].
    *
    * @param f
    *   the method implementation.
    */
  def returns(f: PartialFunction[P, R]): Unit

  /** The list of arguments this method received on each call, in chronological
    * order. Returns multiple arguments as a tuple.
    *
    * You may query the size of the result list to quickly match against the
    * number of times this method was called.
    *
    * @return
    *   the received arguments per call.
    */
  def calls: List[R]
