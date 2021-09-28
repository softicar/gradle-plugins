package com.softicar.gradle.test.logger.plugin.test.result;

import com.softicar.gradle.test.logger.plugin.test.failure.TestFailure;
import com.softicar.gradle.test.logger.plugin.test.method.TestMethodName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;
import org.gradle.api.tasks.testing.TestResult.ResultType;

/**
 * Accumulates results of test method executions.
 *
 * @author Alexander Schmidt
 */
public class TestResultRecorder {

	private volatile Map<TestMethodName, TestFailure> failureMap;
	private volatile List<TestMethodName> failedMethodNames;

	public TestResultRecorder() {

		this.failureMap = new TreeMap<>();
		this.failedMethodNames = new ArrayList<>();
	}

	/**
	 * Records the given {@link TestResult} for a specific test method,
	 * identified via the given {@link TestDescriptor}.
	 * <p>
	 * If invoked several times per test method, the most recently-recorded
	 * result will prevail.
	 *
	 * @param descriptor
	 *            the descriptor that identifies the test method (never null)
	 * @param result
	 *            the result of the test method run (never null)
	 */
	public synchronized void recordResult(TestDescriptor descriptor, TestResult result) {

		TestMethodName methodName = new TestMethodName(descriptor);
		if (result.getResultType() == ResultType.FAILURE) {
			TestFailure previous = failureMap.put(methodName, new TestFailure(descriptor, result));
			if (previous == null) {
				failedMethodNames.add(methodName);
			}
		} else {
			TestFailure previous = failureMap.remove(methodName);
			if (previous != null) {
				failedMethodNames.remove(methodName);
			}
		}
	}

	/**
	 * Retrieves {@link TestFailure} instances from the recorded test results.
	 * <p>
	 * If a failure and a subsequent success were recorded for a given test
	 * method, the returned {@link Collection} will not contain a
	 * {@link TestFailure} instance for that test method.
	 *
	 * @return the relevant recorded {@link TestFailure} instances (never null)
	 */
	public synchronized Collection<TestFailure> getFailures() {

		return failedMethodNames.stream().map(failureMap::get).collect(Collectors.toList());
	}

	public synchronized boolean isEmpty() {

		return failedMethodNames.isEmpty();
	}

	public synchronized int size() {

		return failedMethodNames.size();
	}
}
