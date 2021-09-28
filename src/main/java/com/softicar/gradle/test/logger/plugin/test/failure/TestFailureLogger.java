package com.softicar.gradle.test.logger.plugin.test.failure;

import com.softicar.gradle.test.logger.plugin.test.method.TestMethodName;
import com.softicar.gradle.test.logger.plugin.test.result.TestResultRecorder;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.gradle.api.logging.Logger;

public class TestFailureLogger {

	private final Logger logger;

	public TestFailureLogger(Logger logger) {

		this.logger = logger;
	}

	public void log(TestResultRecorder resultRecorder) {

		if (!resultRecorder.isEmpty()) {
			logDetails(resultRecorder);
			logSummary(resultRecorder);
		}
	}

	private void logSummary(TestResultRecorder resultRecorder) {

		logger.lifecycle(String.format("Summary of %s failed test(s):", resultRecorder.size()));
		logger.lifecycle("");
		resultRecorder.getFailures().stream().map(this::getTestMethodNameAsString).forEach(logger::lifecycle);
		logger.lifecycle("");
	}

	private void logDetails(TestResultRecorder resultRecorder) {

		logger.lifecycle(String.format("Details of %s failed test(s):", resultRecorder.size()));
		logger.lifecycle("");

		int counter = 0;
		for (TestFailure failure: resultRecorder.getFailures()) {
			++counter;
			logger.lifecycle(String.format("Failure #%s >  %s", counter, getTestMethodNameAsString(failure)));
			failure.getExceptions().stream().map(this::getStackTraceAsString).forEach(logger::lifecycle);
		}

		logger.lifecycle("");
	}

	private String getTestMethodNameAsString(TestFailure failure) {

		return new TestMethodName(failure.getDescriptor()).getNameString();
	}

	private String getStackTraceAsString(Throwable throwable) {

		StringWriter stringWriter = new StringWriter();
		try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
			throwable.printStackTrace(printWriter);
		}
		return stringWriter.toString();
	}
}
