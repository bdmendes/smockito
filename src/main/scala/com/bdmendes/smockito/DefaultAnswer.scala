package com.bdmendes.smockito

import com.bdmendes.smockito.Smockito.SmockitoException.UnstubbedMethod
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

private object DefaultAnswer extends Answer[Any]:

  override def answer(invocation: InvocationOnMock): Any =
    val method = invocation.getMethod
    if method.getName.contains("$default$") then
      // The Scala compiler synthesizes default arguments as methods.
      try invocation.callRealMethod()
      catch _ => null
    else
      throw UnstubbedMethod(method, invocation.getRawArguments)
