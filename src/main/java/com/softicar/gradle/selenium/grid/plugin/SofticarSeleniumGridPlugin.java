package com.softicar.gradle.selenium.grid.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;

/**
 * This Gradle plug-in is applied to root projects to help execute test tasks
 * comprising Selenium based unit tests.
 * <p>
 * This plug-in:
 * <ul>
 * <li>provides basic configuration and environmental settings,</li>
 * <li>facilitates parallel execution of tests tasks on a single Selenium grid,
 * and</li>
 * <li>triggers the shutdown of the Selenium grid after the build (regardless of
 * whether the build was successful, failed, or aborted).</li>
 * </ul>
 *
 * @author Alexander Schmidt
 */
public class SofticarSeleniumGridPlugin implements Plugin<Project> {

	/**
	 * The name of the file which contains the shutdown script to terminate the
	 * Selenium grid.
	 */
	private static final String SELENIUM_GRID_SHUTDOWN_SCRIPT = "shutdown-grid.sh";

	/**
	 * The ID which uniquely identifies the disposable Selenium grid instance.
	 */
	private static final String SELENIUM_GRID_UUID = UUID.randomUUID().toString();

	/**
	 * The absolute path to the temporary directory in which temporary files
	 * related to the Selenium grid instance shall be stored.
	 */
	private static final String SELENIUM_GRID_TEMPORARY_DIRECTORY = new File(//
		System.getProperty("java.io.tmpdir"),
		"selenium-grid-" + SELENIUM_GRID_UUID).getAbsolutePath();

	/**
	 * The number of seconds to wait for the grid shutdown command to terminate.
	 */
	private static final int SHUTDOWN_TIMEOUT_SECONDS = 20;

	/**
	 * The prefix of the system properties to propagate (forward) from the
	 * Gradle build JVM to the Gradle test execution worker JVMs.
	 */
	private static final String SYSTEM_PROPERTY_PROPAGATION_PREFIX = "com.softicar.testing.selenium";

	// ---------------------------------------------------------------- //

	private final SystemPropertyHelper systemPropertyHelper;
	private Project rootProject;

	public SofticarSeleniumGridPlugin() {

		this.systemPropertyHelper = new SystemPropertyHelper(SYSTEM_PROPERTY_PROPAGATION_PREFIX);
		this.rootProject = null;
	}

	@Override
	public void apply(Project rootProject) {

		this.rootProject = rootProject;

		assertGradleDaemonDisabled(rootProject);

		rootProject.allprojects(subProject -> {
			subProject.afterEvaluate(project -> {
				if (project.getTasks().findByName("test") != null) {
					configureTestTask(project.getTasks().named("test", Test.class));
				}
			});
		});

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));
	}

	private void configureTestTask(TaskProvider<Test> testTask) {

		testTask.configure(test -> {
			Integer workerThreadCount = determineWorkerThreadCount();
			test.systemProperty("com.softicar.testing.selenium.grid.deferred.shutdown", "true");
			test.systemProperty("com.softicar.testing.selenium.grid.shutdown.script", SELENIUM_GRID_SHUTDOWN_SCRIPT);
			test.systemProperty("com.softicar.testing.selenium.grid.temporary.directory", SELENIUM_GRID_TEMPORARY_DIRECTORY);
			test.systemProperty("com.softicar.testing.selenium.grid.uuid", SELENIUM_GRID_UUID);
			test.systemProperty("com.softicar.testing.selenium.grid.worker.thread.count", workerThreadCount.toString());
			test.setMaxParallelForks(workerThreadCount);
			systemPropertyHelper.proparateTo(test);
		});
	}

	private Boolean isGradleDaemonEnabled(Project project) {

		return Optional.ofNullable(project.getProperties().get("org.gradle.daemon")).map(value -> value.equals("true")).orElse(true);
	}

	private void assertGradleDaemonDisabled(Project project) {

		if (isGradleDaemonEnabled(project)) {
			StringBuilder message = new StringBuilder();
			message.append("BUILD ABORTED - the Gradle daemon must be disabled for builds of this project (details below)\n");
			message.append("During builds of this project, unit tests are executed on a Selenium grid which is started on-demand,\n");
			message.append("and which runs on local Docker containers. The grid must be be stopped after the build has concluded\n");
			message.append("(regardless if successful, failed, or aborted). To stop the grid, a JVM shutdown hook is used.\n");
			message.append("JVM shutdown hooks would not be triggered if the Gradle daemon was used. As a consequnce, to make sure\n");
			message.append("that the grid is stopped after the build, the Gradle daemon must not be used when building this project.");
			throw new GradleException(message.toString());
		}
	}

	private Integer determineWorkerThreadCount() {

		return systemPropertyHelper//
			.getIntegerValue("com.softicar.testing.selenium.grid.worker.thread.count")
			.orElse(rootProject.getGradle().getStartParameter().getMaxWorkerCount());
	}

	private static class SystemPropertyHelper {

		private final String propagatedPropertyPrefix;

		public SystemPropertyHelper(String propagatedPropertyPrefix) {

			this.propagatedPropertyPrefix = Objects.requireNonNull(propagatedPropertyPrefix);
		}

		public Optional<String> getStringValue(String property) {

			return Optional//
				.ofNullable(System.getProperties().get(property))
				.map(this::getValueAsString);
		}

		public Optional<Integer> getIntegerValue(String property) {

			return getStringValue(property).map(Integer::parseInt);
		}

		public void proparateTo(Test targetTest) {

			getPropertyStringMap(System.getProperties())
				.entrySet()
				.stream()
				.filter(entry -> entry.getKey().startsWith(propagatedPropertyPrefix))
				.forEach(entry -> setSystemProperty(targetTest, entry));
		}

		private Map<String, String> getPropertyStringMap(Properties properties) {

			return properties//
				.entrySet()
				.stream()
				.collect(
					Collectors
						.toMap(//
							entry -> entry.getKey().toString(),
							entry -> Optional.ofNullable(entry.getValue()).map(value -> value.toString()).orElse(null)));
		}

		private void setSystemProperty(Test targetTest, Entry<String, ?> propertyEntry) {

			String property = propertyEntry.getKey();
			String value = getValueAsString(propertyEntry.getValue());
			if (value != null) {
				targetTest.systemProperty(property, value);
			}
		}

		private String getValueAsString(Object value) {

			return Optional.ofNullable(value).map(Object::toString).orElse(null);
		}
	}

	/**
	 * Shuts down the Selenium grid, and perform cleanup operations.
	 */
	private static class ShutdownHandler implements Runnable {

		@Override
		public void run() {

			try {
				File temporaryDirectory = new File(SELENIUM_GRID_TEMPORARY_DIRECTORY);
				File shutdownScript = new File(temporaryDirectory, SELENIUM_GRID_SHUTDOWN_SCRIPT);

				if (shutdownScript.exists()) {
					Process process = invokeShutdownProcess(shutdownScript);
					String processOutput = captureOutput(process);
					boolean terminated = process.waitFor(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
					int exitValue = process.exitValue();
					assertShutdownSuccessful(shutdownScript, processOutput, terminated, exitValue);
				}

				deleteDirectory(temporaryDirectory);
			} catch (Exception exception) {
				dumpShutdownLogException(exception);
				throw new RuntimeException(exception);
			}
		}

		private Process invokeShutdownProcess(File shutdownScriptFile) throws IOException {

			ProcessBuilder processBuilder = new ProcessBuilder("sh", shutdownScriptFile.getAbsolutePath());
			processBuilder.directory(new File("."));
			processBuilder.redirectErrorStream(true);
			return processBuilder.start();
		}

		private String captureOutput(Process process) throws IOException {

			StringBuilder output = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line + "\n");
				}
			}
			return output.toString();
		}

		private void assertShutdownSuccessful(File shutdownScript, String processOutput, boolean terminated, int exitValue) {

			if (!terminated || exitValue != 0) {
				String message = "Grid shutdown script failed:\n";
				message += "Shutdown script was: '" + shutdownScript.getAbsolutePath() + "'\n";
				message += "Exit value was: " + exitValue + "\n";
				if (!terminated) {
					message += "Timeout encountered.\n";
				}
				message += "Output was:\n";
				message += ">>>>\n";
				message += processOutput + "\n";
				message += "<<<<\n";
				throw new RuntimeException(message);
			}
		}

		/**
		 * Writes the {@link String} representation of the given
		 * {@link Exception} to a file.
		 *
		 * @param exception
		 *            the {@link Exception} to dump (never <i>null</i>)
		 */
		private void dumpShutdownLogException(Exception exception) {

			try {
				List<String> lines = Arrays
					.asList(//
						String.format("Caught an Exception while processing the %s runtime shutdown hook:", SofticarSeleniumGridPlugin.class.getSimpleName()),
						exception.toString());
				File shutdownExceptionLogFile = new File(SELENIUM_GRID_TEMPORARY_DIRECTORY + "-failed-shutdown.log");
				Files.write(shutdownExceptionLogFile.toPath(), lines);
			} catch (IOException shutdownLogFileIoException) {
				throw new RuntimeException(shutdownLogFileIoException);
			}
		}

		private void deleteDirectory(File directory) {

			for (File file: directory.listFiles()) {
				file.delete();
			}
			directory.delete();
		}
	}
}
