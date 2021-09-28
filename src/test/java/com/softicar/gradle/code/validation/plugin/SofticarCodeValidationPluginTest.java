package com.softicar.gradle.code.validation.plugin;

import com.softicar.gradle.AbstractGradlePluginTest;
import java.io.File;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

public class SofticarCodeValidationPluginTest extends AbstractGradlePluginTest {

	@Before
	public void setup() {

		appendToFile("build.gradle", """
				plugins {
					id 'com.softicar.gradle.java.library'
					id 'com.softicar.gradle.code.validation'
				}
				""");
	}

	@Test
	public void testWithoutValidationEntryPointDefinition() {

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("softicarCodeValidation")
			.withPluginClasspath()
			.buildAndFail();

		assertContains("Could not create task ':softicarCodeValidation'.", result.getOutput());
		assertContains("Entry point class for code validation not defined.", result.getOutput());
	}

	@Test
	public void testWithMissingValidationEntryPointClass() {

		appendToFile("build.gradle", """
				softicarCodeValidationSettings {
					validationEntryPointClass = "com.example.Validator"
				}
				""");

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("softicarCodeValidation")
			.withPluginClasspath()
			.buildAndFail();

		assertContains("Task :softicarCodeValidation FAILED", result.getOutput());
		assertContains("com.example.Validator", result.getOutput());
	}

	@Test
	public void testWithFailingValidation() {

		appendToFile("build.gradle", """
				softicarCodeValidationSettings {
					validationEntryPointClass = "com.example.Validator"
				}
				""");

		File sourceFolder = mkdirs("src/main/java/com/example");
		appendToFile(new File(sourceFolder, "Validator.java"), """
				package com.example;
				public class Validator {
					public static void main(String[] args) {
						throw new RuntimeException("Expected validation failure!");
					}
				}
				""");

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("softicarCodeValidation")
			.withPluginClasspath()
			.buildAndFail();

		assertContains("Task :softicarCodeValidation FAILED", result.getOutput());
		assertContains("java.lang.RuntimeException: Expected validation failure!", result.getOutput());
	}

	@Test
	public void testWithSuccessfulValidation() {

		appendToFile("build.gradle", """
				softicarCodeValidationSettings {
					validationEntryPointClass = "com.example.Validator"
					arguments = ["foo", "bar"]
				}
				""");

		File sourceFolder = mkdirs("src/main/java/com/example");
		appendToFile(new File(sourceFolder, "Validator.java"), """
				package com.example;
				public class Validator {
					public static void main(String[] args) {
						System.out.println("validation arguments: %s".formatted(java.util.List.of(args)));
					}
				}
				""");

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("softicarCodeValidation")
			.withPluginClasspath()
			.build();

		assertContains("validation arguments: [foo, bar]", result.getOutput());
	}
}
