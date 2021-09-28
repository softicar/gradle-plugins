package com.softicar.gradle.java.library.plugin;

import com.softicar.gradle.AbstractSofticarProjectPlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;

public class SofticarJavaLibraryPlugin extends AbstractSofticarProjectPlugin {

	public static String getId() {

		return "com.softicar.gradle.java.library";
	}

	@Override
	public void apply(Project project) {

		project.getPluginManager().apply(JavaLibraryPlugin.class);

		// disable overly-restrictive Javadoc compiler
		project.getTasks().withType(Javadoc.class).forEach(javadocTask -> {
			((CoreJavadocOptions) javadocTask.getOptions()).addStringOption("Xdoclint:none", "-quiet");
		});

		// configure Jar manifest
		applyManifest(project.getTasks().named("jar", Jar.class));
	}
}
