package com.softicar.gradle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Base class for SoftiCAR Gradle plugin tests.
 * <p>
 * <b>IMPORTANT:</b> Before executing derived test classes in Eclipse, run the
 * command <code>./gradlew assemble</code> on this project to update the class
 * path. See: <a
 * href=https://docs.gradle.org/current/userguide/testing_gradle_plugins.html#ide_integration>Testing
 * Gradle Plugins</a>
 *
 * @author Oliver Richers
 */
public class AbstractGradlePluginTest extends Assert {

	public @Rule TemporaryFolder testProjectDir = new TemporaryFolder();

	public AbstractGradlePluginTest() {

		Locale.setDefault(Locale.US);
	}

	protected File mkdirs(String path) {

		File folder = new File(testProjectDir.getRoot(), path);
		folder.mkdirs();
		return folder;
	}

	protected void appendToFile(String path, String lines) {

		appendToFile(new File(testProjectDir.getRoot(), path), lines);
	}

	protected void appendToFile(File file, String lines) {

		try {
			try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
				writer.println(lines);
			}
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	protected void assertContains(String expectedText, String fulltext) {

		if (!fulltext.contains(expectedText)) {
			fail("Expected the text '%s' in the following:\n".formatted(expectedText) + fulltext);
		}
	}

	protected void assertMissing(String unexpectedText, String fulltext) {

		if (fulltext.contains(unexpectedText)) {
			fail("Unexpectedly encountered the text '%s' in the following:\n".formatted(unexpectedText) + fulltext);
		}
	}

	protected String listFiles(File root) {

		StringBuilder output = new StringBuilder();
		listFiles(root, ".", output);
		return output.toString();
	}

	protected void listFiles(File root, String path, StringBuilder output) {

		output.append(path).append("\n");

		Optional//
			.ofNullable(root.listFiles())
			.map(Stream::of)
			.orElse(Stream.empty())
			.sorted(Comparator.comparing(File::getName))
			.forEach(child -> listFiles(child, path + "/" + child.getName(), output));
	}
}
