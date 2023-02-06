package com.softicar.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

public abstract class AbstractSofticarProjectPlugin implements Plugin<Project> {

	protected void requirePlugin(Project project, String pluginId) {

		if (!project.getPluginManager().hasPlugin(pluginId)) {
			throw new RuntimeException("%s requires plugin '%s'".formatted(getClass().getSimpleName(), pluginId));
		}
	}

	protected void applyManifest(TaskProvider<Jar> jarTaskProvider) {

		jarTaskProvider.get().doFirst(new ApplyManifestAction());
	}

	protected JavaPluginExtension JavaPluginExtension(Project project) {

		return project.getExtensions().getByType(JavaPluginExtension.class);
	}

	private class ApplyManifestAction implements Action<Task> {

		@Override
		public void execute(Task task) {

			Jar jarTask = (Jar) task;
			Attributes attributes = jarTask.getManifest().getAttributes();
			attributes.put("Implementation-Title", jarTask.getProject().getName());
			attributes.put("Implementation-Version", jarTask.getProject().getVersion());
			attributes.put("Built-By", System.getProperty("user.name"));
			attributes.put("Built-JDK", System.getProperty("java.version"));
			attributes.put("Built-Gradle", jarTask.getProject().getGradle().getGradleVersion());
		}
	}
}
