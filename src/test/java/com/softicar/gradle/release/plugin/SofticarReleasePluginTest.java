package com.softicar.gradle.release.plugin;

import com.softicar.gradle.AbstractGradlePluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

public class SofticarReleasePluginTest extends AbstractGradlePluginTest {

	@Before
	public void setup() {

		appendToFile("build.gradle", """
				plugins {
					id 'com.softicar.gradle.java.library'
					id 'com.softicar.gradle.release'
				}
				group = 'com.example'
				version = '1.2.3'
				""");
		appendToFile("settings.gradle", """
				rootProject.name = 'foo'
				""");
	}

	@Test
	public void testWithoutGitFolder() {

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("release")
			.withPluginClasspath()
			.buildAndFail();

		assertContains("did not find a `.git` folder.", result.getOutput());
	}

	@Test
	public void testTasks() {

		mkdirs(".git");

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("tasks")
			.withPluginClasspath()
			.build();

		assertContains("Release tasks", result.getOutput());
	}
}
