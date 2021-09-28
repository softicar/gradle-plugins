package com.softicar.gradle.ivy.publish.plugin;

import com.softicar.gradle.AbstractGradlePluginTest;
import java.io.File;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

public class SofticarIvyPublishPluginTest extends AbstractGradlePluginTest {

	@Before
	public void setup() {

		appendToFile("build.gradle", """
				plugins {
					id 'com.softicar.gradle.java.library'
					id 'com.softicar.gradle.ivy.publish'
				}
				""");
	}

	@Test
	public void testWithoutIvyUploadUrl() {

		BuildResult result = GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("tasks")
			.withPluginClasspath()
			.buildAndFail();

		assertContains("No `ivyUploadUrl` defined.", result.getOutput());
	}

	@Test
	public void testWithLocalUploadUrl() {

		File publishingFolder = mkdirs("published");

		// build.gradle
		appendToFile("build.gradle", """
				group = 'com.example'
				version = '1.2.3'
				softicarIvyPublishSettings {
					ivyUploadUrl = '%s'
				}
				""".formatted(publishingFolder.toURI()));

		// settings.gradle
		appendToFile("settings.gradle", "rootProject.name = 'foo'");

		// create test class
		File sourceFolder = mkdirs("src/main/java");
		appendToFile(new File(sourceFolder, "Test.java"), """
				public class Test {
					public static void main(String[] args) {}
				}
				""");

		GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("clean", "build", "publish")
			.withPluginClasspath()
			.build();

		assertEquals("""
				.
				./com.example
				./com.example/foo
				./com.example/foo/1.2.3
				./com.example/foo/1.2.3/foo-1.2.3-javadoc.jar
				./com.example/foo/1.2.3/foo-1.2.3-javadoc.jar.sha1
				./com.example/foo/1.2.3/foo-1.2.3-javadoc.jar.sha256
				./com.example/foo/1.2.3/foo-1.2.3-javadoc.jar.sha512
				./com.example/foo/1.2.3/foo-1.2.3-sources.jar
				./com.example/foo/1.2.3/foo-1.2.3-sources.jar.sha1
				./com.example/foo/1.2.3/foo-1.2.3-sources.jar.sha256
				./com.example/foo/1.2.3/foo-1.2.3-sources.jar.sha512
				./com.example/foo/1.2.3/foo-1.2.3.jar
				./com.example/foo/1.2.3/foo-1.2.3.jar.sha1
				./com.example/foo/1.2.3/foo-1.2.3.jar.sha256
				./com.example/foo/1.2.3/foo-1.2.3.jar.sha512
				./com.example/foo/1.2.3/foo-1.2.3.module
				./com.example/foo/1.2.3/foo-1.2.3.module.sha1
				./com.example/foo/1.2.3/foo-1.2.3.module.sha256
				./com.example/foo/1.2.3/foo-1.2.3.module.sha512
				./com.example/foo/1.2.3/ivy-1.2.3.xml
				./com.example/foo/1.2.3/ivy-1.2.3.xml.sha1
				./com.example/foo/1.2.3/ivy-1.2.3.xml.sha256
				./com.example/foo/1.2.3/ivy-1.2.3.xml.sha512
				""", listFiles(publishingFolder));
	}
}
