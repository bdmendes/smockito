package com.bdmendes.smockito.internal;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

public class MockitoAgent {
	public static void premain(String agentArgs, Instrumentation inst) {
		try {
			Class<?> agentClass = Class.forName("org.mockito.internal.PremainAttach");
			Method premainMethod = agentClass.getMethod("premain", String.class, Instrumentation.class);
			premainMethod.invoke(null, agentArgs, inst);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load Mockito agent", e);
		}
	}
}
