package com.bdmendes.smockito.internal

import com.bdmendes.smockito.Smockito.SmockitoException.UnstubbedMethod
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

private[smockito] object DefaultAnswer extends Answer[Any]:

  override def answer(invocation: InvocationOnMock): Any =
    val method = invocation.getMethod
    if method.getName.contains("$default$") then
      // The Scala compiler synthesizes default arguments as methods.
      try invocation.callRealMethod()
      catch _ => null
    else if method.getName == "toString" && method.getParameterCount == 0 then
      // Mocks are described in test failures and logs, such as when passed to other mocks.
      val settings = Mockito.mockingDetails(invocation.getMock).getMockCreationSettings
      s"Mock[${settings.getTypeToMock.getSimpleName}]"
    else
      throw UnstubbedMethod(method, invocation.getRawArguments)
