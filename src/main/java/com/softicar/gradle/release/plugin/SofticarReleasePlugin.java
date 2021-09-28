package com.softicar.gradle.release.plugin;

import com.softicar.gradle.AbstractSofticarProjectPlugin;
import com.softicar.gradle.java.library.plugin.SofticarJavaLibraryPlugin;
import java.io.File;
import java.io.IOException;
import net.researchgate.release.ReleaseExtension;
import net.researchgate.release.ReleasePlugin;
import org.gradle.api.Project;

public class SofticarReleasePlugin extends AbstractSofticarProjectPlugin {

	@Override
	public void apply(Project project) {

		requirePlugin(project, SofticarJavaLibraryPlugin.getId());

		if (!isRootProject(project)) {
			throw new RuntimeException("%s: plugin should only be applied to the root project.".formatted(getClass().getSimpleName()));
		}

		if (!hasGit(project)) {
			throw new RuntimeException("%s: did not find a `.git` folder.".formatted(getClass().getSimpleName()));
		}

		project.getPlugins().apply(ReleasePlugin.class);

		project.getExtensions().configure(ReleaseExtension.class, release -> {
			release.setTagTemplate("$name-$version");
		});
	}

	private boolean isRootProject(Project project) {

		return project == project.getRootProject();
	}

	private static boolean hasGit(Project project) {

		try {
			return new File(project.getRootProject().getRootDir().getCanonicalFile(), ".git").isDirectory();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}
