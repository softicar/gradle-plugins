package com.softicar.gradle.code.validation.plugin;

import java.util.List;
import java.util.Optional;
import org.gradle.api.Project;

/**
 * Configuration extension for the {@link SofticarCodeValidationPlugin}.
 *
 * @author Oliver Richers
 */
public abstract class SofticarCodeValidationSettings {

	private static final String SETTINGS_NAME = "softicarCodeValidationSettings";

	private String validationEntryPointClass;
	private List<String> arguments;

	public void setValidationEntryPointClass(String validationEntryPointClass) {

		this.validationEntryPointClass = validationEntryPointClass;
	}

	public String getValidationEntryPointClass() {

		if (validationEntryPointClass == null) {
			throw new RuntimeException("Entry point class for code validation not defined.");
		}
		return validationEntryPointClass;
	}

	public void setArguments(List<String> arguments) {

		this.arguments = arguments;
	}

	public List<String> getArguments() {

		return Optional.ofNullable(arguments).orElse(List.of());
	}

	public static SofticarCodeValidationSettings get(Project project) {

		return project.getExtensions().getByType(SofticarCodeValidationSettings.class);
	}

	public static SofticarCodeValidationSettings create(Project project) {

		return project.getExtensions().create(SETTINGS_NAME, SofticarCodeValidationSettings.class);
	}
}
