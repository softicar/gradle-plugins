package com.softicar.gradle.test.logger.plugin;

import com.softicar.gradle.AbstractGradlePluginTest;
import java.io.File;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

public class SofticarTestLoggerPluginTest extends AbstractGradlePluginTest {

	@Before
	public void setup() {

		appendToFile("build.gradle", """
				plugins {
					id 'com.softicar.gradle.java.library'
					id 'com.softicar.gradle.test.logger'
				}
				repositories {
					mavenCentral()
				}
				dependencies {
					testImplementation "junit:junit:4.12"
				}
				""");
	}

	@Test
	public void testWithSuccessfulTest() {

		File testFolder = mkdirs("src/test/java/com/example");
		appendToFile(new File(testFolder, "SomeTest.java"), """
				package com.example;

				import org.junit.Test;

				public class SomeTest {
					@Test
					public void test() {
						// nothing to do
					}
				}
				""");

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("-Pcom.softicar.test.logger.verbose=false", "test")
			.withPluginClasspath()
			.build();

		assertMissing("Test #1 (running: 1)  test  [com.example.SomeTest]  > started", result.getOutput());
		assertMissing("Test #1 (running: 0)  test  [com.example.SomeTest]  > SUCCESS", result.getOutput());
	}

	@Test
	public void testWithSuccessfulTestAndVerboseMode() {

		File testFolder = mkdirs("src/test/java/com/example");
		appendToFile(new File(testFolder, "SomeTest.java"), """
				package com.example;

				import org.junit.Test;

				public class SomeTest {
					@Test
					public void test() {
						// nothing to do
					}
				}
				""");

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("-Pcom.softicar.test.logger.verbose=true", "test")
			.withPluginClasspath()
			.build();

		assertContains("Test #1 (running: 1)  test  [com.example.SomeTest]  > started", result.getOutput());
		assertContains("Test #1 (running: 0)  test  [com.example.SomeTest]  > SUCCESS", result.getOutput());
	}

	@Test
	public void testWithFailingTest() {

		File testFolder = mkdirs("src/test/java/com/example");
		appendToFile(new File(testFolder, "SomeTest.java"), """
				package com.example;

				import org.junit.Assert;
				import org.junit.Test;

				public class SomeTest {
					@Test
					public void goodTest() {
						// success
					}
					@Test
					public void badTest() {
						Assert.fail("intended failure");
					}
				}
				""");

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("-Pcom.softicar.test.logger.verbose=false", "test")
			.withPluginClasspath()
			.buildAndFail();

		String output = result.getOutput();

		assertMissing("(running: 1)  goodTest  [com.example.SomeTest]  > started", output);
		assertMissing("(running: 0)  goodTest  [com.example.SomeTest]  > SUCCESS", output);
		assertMissing("(running: 1)  badTest  [com.example.SomeTest]  > started", output);
		assertMissing("(running: 0)  badTest  [com.example.SomeTest]  > FAILURE", output);

		assertContainsFailureDetails(output);
		assertContainsFailureSummary(output);
	}

	@Test
	public void testWithFailingTestAndVerboseMode() {

		File testFolder = mkdirs("src/test/java/com/example");
		appendToFile(new File(testFolder, "SomeTest.java"), """
				package com.example;

				import org.junit.Assert;
				import org.junit.Test;

				public class SomeTest {
					@Test
					public void goodTest() {
						// success
					}
					@Test
					public void badTest() {
						Assert.fail("intended failure");
					}
				}
				""");

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("-Pcom.softicar.test.logger.verbose=true", "test")
			.withPluginClasspath()
			.buildAndFail();

		String output = result.getOutput();

		assertContains("(running: 1)  goodTest  [com.example.SomeTest]  > started", output);
		assertContains("(running: 0)  goodTest  [com.example.SomeTest]  > SUCCESS", output);
		assertContains("(running: 1)  badTest  [com.example.SomeTest]  > started", output);
		assertContains("(running: 0)  badTest  [com.example.SomeTest]  > FAILURE", output);

		assertContainsFailureDetails(output);
		assertContainsFailureSummary(output);
	}

	private void assertContainsFailureSummary(String output) {

		assertContains("Summary of 1 failed test(s):", output);
		assertContains("badTest  [com.example.SomeTest]", output);
	}

	private void assertContainsFailureDetails(String output) {

		assertContains("Details of 1 failed test(s):", output);
		assertContains("Failure #1 >  badTest  [com.example.SomeTest]", output);
		assertContains("java.lang.AssertionError: intended failure", output);
		assertContains("at org.junit.Assert.fail(Assert.java:88)", output);
		assertContains("at com.example.SomeTest.badTest(SomeTest.java:", output);
	}
}
