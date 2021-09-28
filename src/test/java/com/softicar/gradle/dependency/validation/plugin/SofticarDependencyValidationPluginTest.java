package com.softicar.gradle.dependency.validation.plugin;

import com.softicar.gradle.AbstractGradlePluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

public class SofticarDependencyValidationPluginTest extends AbstractGradlePluginTest {

	@Before
	public void setup() {

		appendToFile("build.gradle", """
				plugins {
					id 'com.softicar.gradle.java.library'
					id 'com.softicar.gradle.dependency.validation'
				}
				check.dependsOn softicarDependencyValidation
				""");
	}

	@Test
	public void testSuccessfulBuild() {

		GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("softicarDependencyValidation")
			.withPluginClasspath()
			.build();
	}

	@Test
	public void testUnintendedDowngrade() {

		appendToFile("build.gradle", """
				repositories {
					mavenCentral()
				}
				dependencies {
					implementation "commons-codec:commons-codec:1.14"
					implementation "org.apache.poi:poi:4.1.2" // depends on commons-codec:commons-codec:1.13
				}
				configurations.all {
					resolutionStrategy {
						failOnVersionConflict()
						force "commons-codec:commons-codec:1.13"
					}
				}
				""");

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("softicarDependencyValidation")
			.withPluginClasspath()
			.buildAndFail();

		assertContains("Encountered an unexpected downgrade of 'commons-codec:commons-codec' from [1.14.0] to [1.13.0].", result.getOutput());
	}
}
