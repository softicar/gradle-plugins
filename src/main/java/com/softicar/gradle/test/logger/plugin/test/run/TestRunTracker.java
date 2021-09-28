package com.softicar.gradle.test.logger.plugin.test.run;

import com.softicar.gradle.test.logger.plugin.test.method.TestMethodName;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.gradle.api.tasks.testing.TestDescriptor;

public class TestRunTracker {

	private volatile int startedTests;
	private volatile Map<TestMethodName, Integer> testIndexMap;
	private volatile Map<TestMethodName, Long> testStartTimestampMap;
	private volatile Map<TestMethodName, Integer> testRunCounterMap;

	public TestRunTracker() {

		this.startedTests = 0;
		this.testIndexMap = new TreeMap<>();
		this.testStartTimestampMap = new TreeMap<>();
		this.testRunCounterMap = new TreeMap<>();
	}

	public synchronized TestRunMetrics start(TestDescriptor descriptor) {

		TestMethodName methodName = new TestMethodName(descriptor);
		startedTests++;
		testIndexMap.put(methodName, startedTests);
		testStartTimestampMap.put(methodName, System.currentTimeMillis());
		testRunCounterMap.compute(methodName, (dummy, runs) -> runs == null? 1 : runs + 1);
		return new TestRunMetrics(startedTests, testIndexMap.size());
	}

	public synchronized FinishedTestRunMetrics end(TestDescriptor descriptor) {

		TestMethodName methodName = new TestMethodName(descriptor);
		Integer index = testIndexMap.remove(methodName);
		Long startTime = testStartTimestampMap.remove(methodName);
		long duration = System.currentTimeMillis() - startTime;
		return new FinishedTestRunMetrics(index, testIndexMap.size(), duration);
	}

	public synchronized Map<TestMethodName, Integer> getRedundantTestRunMap() {

		return testRunCounterMap//
			.entrySet()
			.stream()
			.filter(entry -> entry.getValue() > 1)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
}
