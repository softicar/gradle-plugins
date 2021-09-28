package com.softicar.gradle.ivy.publish.plugin;

import org.gradle.api.Project;

public class SofticarIvyPublishSettings {

	private static final String SETTINGS_NAME = "softicarIvyPublishSettings";
	public String ivyUploadUrl = "";
	public String ivyUploadUsername = "";
	public String ivyUploadPassword = "";

	public SofticarIvyPublishSettings validate() {

		if (ivyUploadUrl.isBlank()) {
			throw new RuntimeException(//
				"No `ivyUploadUrl` defined. For example: %s { ivyUploadUrl = 'sftp://somehost:22/path/to/repo' }".formatted(SETTINGS_NAME));
		}
		return this;
	}

	public boolean hasUploadCredentials() {

		return !ivyUploadUsername.isBlank();
	}

	public static SofticarIvyPublishSettings get(Project project) {

		return project.getExtensions().getByType(SofticarIvyPublishSettings.class);
	}

	public static SofticarIvyPublishSettings create(Project project) {

		return project.getExtensions().create(SETTINGS_NAME, SofticarIvyPublishSettings.class);
	}
}
