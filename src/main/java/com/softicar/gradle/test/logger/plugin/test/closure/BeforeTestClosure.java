package com.softicar.gradle.test.logger.plugin.test.closure;

import groovy.lang.Closure;
import java.util.function.Consumer;
import org.gradle.api.tasks.testing.TestDescriptor;

public class BeforeTestClosure extends Closure<Object> {

	private final Consumer<TestDescriptor> consumer;

	public BeforeTestClosure(Object owner, Consumer<TestDescriptor> consumer) {

		super(owner);
		this.consumer = consumer;
	}

	/**
	 * This method is called by the Groovy API, via reflection. Do not remove or
	 * rename it.
	 */
	public void doCall(TestDescriptor descriptor) {

		consumer.accept(descriptor);
	}
}
