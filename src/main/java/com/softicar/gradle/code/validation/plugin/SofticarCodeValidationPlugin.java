package com.softicar.gradle.code.validation.plugin;

import com.softicar.gradle.AbstractSofticarProjectPlugin;
import com.softicar.gradle.java.library.plugin.SofticarJavaLibraryPlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.JavaExec;

/**
 * This plug-in adds the task <i>codeValidation</i> to the project.
 *
 * @author Oliver Richers
 */
public class SofticarCodeValidationPlugin extends AbstractSofticarProjectPlugin {

	@Override
	public void apply(Project project) {

		requirePlugin(project, SofticarJavaLibraryPlugin.getId());

		SofticarCodeValidationSettings.create(project);

		project.getTasks().register("softicarCodeValidation", JavaExec.class).configure(task -> {
			task.dependsOn(project.getTasks().named("classes"));
			task.dependsOn(project.getTasks().named("testClasses"));

			JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
			task.setClasspath(javaExtension.getSourceSets().getByName("main").getRuntimeClasspath());

			SofticarCodeValidationSettings settings = SofticarCodeValidationSettings.get(project);
			task.getMainClass().set(settings.getValidationEntryPointClass());
			task.setArgs(settings.getArguments());
		});
	}
}
