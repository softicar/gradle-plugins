package com.softicar.gradle.test.logger.plugin.test.failure;

import java.util.List;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;

public class TestFailure {

	private final TestDescriptor descriptor;
	private final List<Throwable> exceptions;

	public TestFailure(TestDescriptor descriptor, TestResult result) {

		this.descriptor = descriptor;
		this.exceptions = result.getExceptions();
	}

	public TestDescriptor getDescriptor() {

		return descriptor;
	}

	public List<Throwable> getExceptions() {

		return exceptions;
	}
}
