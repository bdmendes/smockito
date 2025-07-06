package com.bdmendes.smockito.internal

import java.lang.instrument.Instrumentation
import org.mockito.internal.PremainAttach

object MockitoAgent:

  /** Pigs back to the inline Mockito mock maker. Referenced in the manifest as the entrypoint when
    * used as a Java agent.
    */
  def premain(args: String, intrumentation: Instrumentation): Unit =
    PremainAttach.premain(args, intrumentation)
