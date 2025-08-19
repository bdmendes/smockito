package com.bdmendes.smockito

import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

private class DefaultAnswer extends Answer[Any]:

  override def answer(invocation: InvocationOnMock): Any =
    val method = invocation.getMethod
    if method.getName.contains("$default$") then
      // Java does not have default arguments. In bytecode, scalac injects a synthesized
      // `<method-name>$default$<param-pos>` method.
      invocation.callRealMethod()
    else if invocation.getRawArguments.forall(_ != null) then
      // The user might be relying on a real implementation, e.g. an adapter that dispatches to a
      // method that is stubbed.
      try invocation.callRealMethod()
      catch _ => null
    else
      null
