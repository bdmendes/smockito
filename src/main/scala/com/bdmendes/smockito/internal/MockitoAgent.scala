package com.bdmendes.smockito.internal

import java.lang.instrument.Instrumentation
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object MockitoAgent:

  def method =
    // Unfortunately, we cannot be sure that Mockito exists in the class path in
    // some complicated build configurations. It's best not to load this agent
    // in that case, but nevertheless we want to be flexible.
    Try {
      val mockito = Class.forName("org.mockito.internal.PremainAttach")
      mockito.getMethod("premain", classOf[String], classOf[Instrumentation])
    }

  def premain(args: String, instrumentation: Instrumentation): Unit =
    method match
      case Failure(e) =>
        println(s"Smockito could not start Mockito agent: ${e.getMessage}")
      case Success(m) =>
        // Mockito already checks for repeated calls. We can invoke this blindly.
        m.invoke(null, args, instrumentation)
