package com.softicar.gradle.test.logger.plugin.test.log;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.TestResult.ResultType;

public class TestLogPrinter {

	private final Logger logger;

	public TestLogPrinter(Logger logger) {

		this.logger = Objects.requireNonNull(logger);
	}

	public void printStart(int index, int runningCount, String methodName, String className) {

		print(//
			index,
			runningCount,
			methodName,
			className,
			"started");
	}

	public void printResult(int index, int runningCount, String methodName, String className, ResultType resultType, long duration) {

		print(//
			index,
			runningCount,
			methodName,
			className,
			String.format("%s (%.2fs)", resultType, duration / 1000d));
	}

	private void print(int index, int runningCount, String methodName, String className, String status) {

		String message = String
			.format(//
				"[%s] Test #%s (running: %s)  %s  [%s]  > %s",
				getFormattedTime(),
				index,
				runningCount,
				methodName,
				className,
				status);
		logger.lifecycle(message);
	}

	private String getFormattedTime() {

		return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	}
}
