package com.softicar.gradle.java.library.plugin;

import com.softicar.gradle.AbstractGradlePluginTest;
import java.io.File;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

public class SofticarJavaLibraryPluginTest extends AbstractGradlePluginTest {

	@Before
	public void setup() {

		appendToFile("build.gradle", """
				plugins {
					id 'com.softicar.gradle.java.library'
				}
				group = 'com.example'
				version = '1.2.3'
				""");
		appendToFile("settings.gradle", """
				rootProject.name = 'foo'
				""");
	}

	@Test
	public void test() {

		File sourceFolder = mkdirs("src/main/java");
		appendToFile(new File(sourceFolder, "Test.java"), """
				public class Test {
					public static void main(String[] args) {}
				}
				""");

		GradleRunner//
			.create()
			.withProjectDir(testProjectDir.getRoot())
			.withArguments("clean", "build")
			.withPluginClasspath()
			.build();

		File jarFile = new File(testProjectDir.getRoot(), "build/libs/foo-1.2.3.jar");
		assertTrue(jarFile.exists());
	}
}
