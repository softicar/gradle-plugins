package com.softicar.gradle.test.logger.plugin.test.run;

public class FinishedTestRunMetrics extends TestRunMetrics {

	private final long duration;

	public FinishedTestRunMetrics(int index, int runningCount, long duration) {

		super(index, runningCount);
		this.duration = duration;
	}

	public long getDuration() {

		return duration;
	}
}
