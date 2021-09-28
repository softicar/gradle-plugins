package com.softicar.gradle.test.logger.plugin.test.method;

import org.gradle.api.tasks.testing.TestDescriptor;

public class TestMethodName implements Comparable<TestMethodName> {

	private final String name;

	public TestMethodName(TestDescriptor descriptor) {

		this.name = String.format("%s  [%s]", descriptor.getName(), descriptor.getClassName());
	}

	@Override
	public int compareTo(TestMethodName other) {

		return name.compareTo(other.name);
	}

	@Override
	public String toString() {

		return getNameString();
	}

	public String getNameString() {

		return name;
	}
}
