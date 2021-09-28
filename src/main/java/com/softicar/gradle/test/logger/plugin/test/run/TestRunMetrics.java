package com.softicar.gradle.test.logger.plugin.test.run;

public class TestRunMetrics {

	private final int index;
	private final int runningCount;

	public TestRunMetrics(int index, int runningCount) {

		this.index = index;
		this.runningCount = runningCount;
	}

	public int getIndex() {

		return index;
	}

	public int getRunningCount() {

		return runningCount;
	}
}
