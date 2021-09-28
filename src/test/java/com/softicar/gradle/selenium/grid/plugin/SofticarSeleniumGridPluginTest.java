package com.softicar.gradle.selenium.grid.plugin;

import com.softicar.gradle.AbstractGradlePluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

public class SofticarSeleniumGridPluginTest extends AbstractGradlePluginTest {

	@Before
	public void setup() {

		appendToFile("build.gradle", """
				plugins {
					id 'base'
					id 'com.softicar.gradle.selenium.grid'
				}
				""");
	}

	@Test
	public void testFailsWithActiveDaemon() {

		appendToFile("gradle.properties", "org.gradle.daemon=true");

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("softicarCodeValidation")
			.withPluginClasspath()
			.buildAndFail();

		assertContains("Failed to apply plugin 'com.softicar.gradle.selenium.grid'.", result.getOutput());
		assertContains("the Gradle daemon must be disabled for builds of this project", result.getOutput());
	}

	@Test
	public void testSuccessfulBuild() {

		appendToFile("gradle.properties", "org.gradle.daemon=false");

		GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("build")
			.withPluginClasspath()
			.build();
	}

	// TODO implement more tests
}
