package com.bdmendes.smockito.internal

import java.lang.instrument.Instrumentation
import java.lang.reflect.Method

object MockitoAgent:

  private[smockito] def mockitoPremain: Method =
    val agentClass = Class.forName("org.mockito.internal.PremainAttach")
    agentClass.getMethod("premain", classOf[String], classOf[Instrumentation])

  def premain(agentArgs: String, inst: Instrumentation): Unit =
    try
      mockitoPremain.invoke(null, agentArgs, inst)
    catch
      case e: Exception =>
        throw new RuntimeException("Failed to load Mockito agent", e)
