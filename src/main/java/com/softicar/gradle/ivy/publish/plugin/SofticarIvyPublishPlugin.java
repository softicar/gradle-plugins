package com.softicar.gradle.ivy.publish.plugin;

import com.softicar.gradle.AbstractSofticarProjectPlugin;
import com.softicar.gradle.java.library.plugin.SofticarJavaLibraryPlugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.ivy.IvyPublication;
import org.gradle.api.publish.ivy.plugins.IvyPublishPlugin;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

public class SofticarIvyPublishPlugin extends AbstractSofticarProjectPlugin {

	@Override
	public void apply(Project project) {

		requirePlugin(project, SofticarJavaLibraryPlugin.getId());

		SofticarIvyPublishSettings.create(project);

		project.getPluginManager().apply(IvyPublishPlugin.class);

		project.getTasks().create("sourcesJar", Jar.class, task -> {
			task.dependsOn(project.getTasks().named("classes"));
			task.from(JavaPluginExtension(project).getSourceSets().getByName("main").getAllSource());
			task.getArchiveClassifier().set("sources");
		});

		project.getTasks().create("javadocJar", Jar.class, task -> {
			task.dependsOn(project.getTasks().named("javadoc"));
			task.from(project.getTasks().named("javadoc", Javadoc.class).get().getDestinationDir());
			task.getArchiveClassifier().set("javadoc");
		});

		applyManifest(project.getTasks().named("sourcesJar", Jar.class));
		applyManifest(project.getTasks().named("javadocJar", Jar.class));

		// add ivy upload repository
		project.afterEvaluate(this::addIvyUploadRepository);

		// add sources and Javadoc artifacts to publishing
		project.getExtensions().configure(PublishingExtension.class, publishing -> {
			publishing.publications(publications -> {
				publications.create("ivy", IvyPublication.class, ivy -> {
					ivy.from(project.getComponents().getByName("java"));

					// Due to a defect in Gradle, we need to use `afterEvaluate` here.
					// (see https://github.com/gradle/gradle/issues/8707)
					project.afterEvaluate(afterEvaluate -> {
						ivy.getConfigurations().create("sources");
						ivy.getConfigurations().create("javadoc");

						ivy.artifact(project.getTasks().getByName("sourcesJar"), it -> {
							it.setType("source");
							it.setConf("sources");
						});

						ivy.artifact(project.getTasks().getByName("javadocJar"), it -> {
							it.setType("javadoc");
							it.setConf("javadoc");
						});
					});
				});
			});
		});
	}

	private void addIvyUploadRepository(Project project) {

		project.getExtensions().configure(PublishingExtension.class, publishing -> {
			publishing.repositories(repositories -> {
				repositories.ivy(ivy -> {
					SofticarIvyPublishSettings settings = SofticarIvyPublishSettings.get(project).validate();
					ivy.setUrl(settings.ivyUploadUrl);
					if (settings.hasUploadCredentials()) {
						ivy.credentials(credentials -> {
							credentials.setUsername(settings.ivyUploadUsername);
							credentials.setPassword(settings.ivyUploadPassword);
						});
					}
				});
			});
		});
	}
}
