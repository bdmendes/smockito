package com.bdmendes.smockito.internal

import java.lang.instrument.Instrumentation
import org.mockito.internal.PremainAttach

object MockitoAgent:

  def premain(args: String, instrumentation: Instrumentation): Unit =
    PremainAttach.premain(args, instrumentation)
