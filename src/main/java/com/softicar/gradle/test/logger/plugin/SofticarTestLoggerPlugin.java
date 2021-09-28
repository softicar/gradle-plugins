package com.softicar.gradle.test.logger.plugin;

import com.softicar.gradle.AbstractSofticarProjectPlugin;
import com.softicar.gradle.test.logger.plugin.test.closure.AfterTestClosure;
import com.softicar.gradle.test.logger.plugin.test.closure.BeforeTestClosure;
import com.softicar.gradle.test.logger.plugin.test.failure.TestFailureLogger;
import com.softicar.gradle.test.logger.plugin.test.log.TestLogPrinter;
import com.softicar.gradle.test.logger.plugin.test.result.TestResultRecorder;
import com.softicar.gradle.test.logger.plugin.test.run.FinishedTestRunMetrics;
import com.softicar.gradle.test.logger.plugin.test.run.RedundantTestRunLogger;
import com.softicar.gradle.test.logger.plugin.test.run.TestRunMetrics;
import com.softicar.gradle.test.logger.plugin.test.run.TestRunTracker;
import java.util.Collections;
import java.util.Objects;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;
import org.gradle.api.tasks.testing.logging.TestExceptionFormat;
import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.gradle.api.tasks.testing.logging.TestLoggingContainer;

/**
 * This Gradle plug-in enables verbose log output during test execution.
 * <p>
 * When a Gradle build is started with this plug-in applied to a root project,
 * execution of <i>test</i> tasks will produce verbose log output. This output
 * enables real-time monitoring of executed test methods, their result status,
 * timing information, etc.
 *
 * @author Alexander Schmidt
 */
public class SofticarTestLoggerPlugin extends AbstractSofticarProjectPlugin {

	private static final String VERBOSE_PROPERTY_NAME = "com.softicar.test.logger.verbose";

	@Override
	public void apply(Project project) {

		requirePlugin(project, "java");

		if (project.getTasks().findByName("test") != null) {
			new TestLogger(project).configureTestTask(project.getTasks().named("test", Test.class));
		}
	}

	private static class TestLogger {

		private final TestRunTracker testRunTracker;
		private final TestResultRecorder resultRecorder;
		private final Project project;
		private final Logger logger;
		private final boolean verbose;

		public TestLogger(Project project) {

			this.testRunTracker = new TestRunTracker();
			this.resultRecorder = new TestResultRecorder();
			this.project = Objects.requireNonNull(project);
			this.logger = Objects.requireNonNull(project.getLogger());
			this.verbose = isVerbose(project);
		}

		public void configureTestTask(TaskProvider<Test> testTask) {

			testTask.configure(this::configureTestLogging);
			testTask.configure(this::configureBeforeTest);
			testTask.configure(this::configureAfterTest);

			project.getGradle().buildFinished(this::executeOnBuildFinished);
		}

		private void configureTestLogging(Test testTask) {

			TestLoggingContainer testLogging = testTask.getTestLogging();
			testLogging.setEvents(Collections.singleton(TestLogEvent.FAILED));
			testLogging.setShowExceptions(true);
			testLogging.setExceptionFormat(TestExceptionFormat.FULL);
			testLogging.setShowCauses(true);
			testLogging.setShowStackTraces(true);
			testLogging.setShowStandardStreams(false);
		}

		private void configureBeforeTest(Test testTask) {

			testTask.beforeTest(new BeforeTestClosure(this, this::executeBeforeTest));
		}

		private void configureAfterTest(Test testTask) {

			testTask.afterTest(new AfterTestClosure(this, this::executeAfterTest));
		}

		private void executeBeforeTest(TestDescriptor descriptor) {

			TestRunMetrics metrics = testRunTracker.start(descriptor);
			if (verbose) {
				new TestLogPrinter(logger)
					.printStart(//
						metrics.getIndex(),
						metrics.getRunningCount(),
						descriptor.getName(),
						descriptor.getClassName());
			}
		}

		private void executeAfterTest(TestDescriptor descriptor, TestResult result) {

			FinishedTestRunMetrics metrics = testRunTracker.end(descriptor);
			if (verbose) {
				new TestLogPrinter(logger)//
					.printResult(
						metrics.getIndex(),
						metrics.getRunningCount(),
						descriptor.getName(),
						descriptor.getClassName(),
						result.getResultType(),
						metrics.getDuration());
			}
			resultRecorder.recordResult(descriptor, result);
		}

		private void executeOnBuildFinished(@SuppressWarnings("unused") BuildResult buildResult) {

			new TestFailureLogger(logger).log(resultRecorder);
			new RedundantTestRunLogger(logger).log(testRunTracker);
		}

		private boolean isVerbose(Project project) {

			return Objects.equals(project.getProperties().get(VERBOSE_PROPERTY_NAME), "true");
		}
	}
}
