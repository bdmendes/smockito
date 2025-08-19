package com.bdmendes.smockito

import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

private class DefaultAnswer extends Answer[Any]:

  override def answer(invocation: InvocationOnMock): Any =
    val method = invocation.getMethod
    if method.getName.contains("$default$") then
      // Java does not have default arguments. In bytecode, scalac injects a synthetized
      // `<method-name>$default$<param-pos>` method.
      invocation.callRealMethod()
    else
      null
