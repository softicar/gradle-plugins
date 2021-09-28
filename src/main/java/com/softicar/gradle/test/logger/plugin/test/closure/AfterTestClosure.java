package com.softicar.gradle.test.logger.plugin.test.closure;

import groovy.lang.Closure;
import java.util.function.BiConsumer;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;

public class AfterTestClosure extends Closure<Object> {

	private final BiConsumer<TestDescriptor, TestResult> consumer;

	public AfterTestClosure(Object owner, BiConsumer<TestDescriptor, TestResult> consumer) {

		super(owner);
		this.consumer = consumer;
	}

	/**
	 * This method is called by the Groovy API, via reflection. Do not remove or
	 * rename it.
	 */
	public void doCall(TestDescriptor descriptor, TestResult result) {

		consumer.accept(descriptor, result);
	}
}
