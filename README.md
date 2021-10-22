![GitHub](https://img.shields.io/github/license/Prevent-DEV/com.softicar.gradle.plugins)
![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/Prevent-DEV/com.softicar.gradle.plugins/Continuous%20Integration/main)

# SoftiCAR Gradle Plugins

The _SoftiCAR Gradle Plugins_ repository is a collection of [Gradle](https://gradle.org/) plugins used by many SoftiCAR Java projects. It comprises the following plugins:

* SoftiCAR Code Validation Plugin
* SoftiCAR Dependency Validation Plugin
* SoftiCAR Java Library Plugin
* SoftiCAR Selenium Grid Plugin
* SoftiCAR Test Logger Plugin

For Gradle multi-projects, a given plugin is either applied to the root project or to the subprojects individually.

## Building and Development

To build this repository, a [JDK 15+](https://adoptopenjdk.net/) installation is required. Building is done using the [gradlew](https://docs.gradle.org/current/userguide/gradle_wrapper.html) command.

```
./gradlew clean build
```

For development, a recent [Eclipse IDE for Java Development](https://www.eclipse.org/downloads/packages/) is required. Clone the repository into the *Eclipse* workspace using the *Git* command line client and import it as *Existing Gradle Project*.

Please read the [contribution guidelines](CONTRIBUTING.md) for this repository and keep our [code of conduct](CODE_OF_CONDUCT.md) in mind.

## SoftiCAR Code Validation Plugin

This plugin enables a project to execute self-contained code validation logic, i.e. code validation logic that is implemented in the project itself or in one of its dependencies.

#### Usage

For example, in the `build.gradle` of the root project write this:
```gradle
plugins {
	// the code validation plugin requires the java library plugin
	id 'com.softicar.gradle.java.library'
	// we usually don't want to apply the plugin to the root project (apply false)
	id 'com.softicar.gradle.code.validation' apply false
}
subprojects {
	apply plugin: 'com.softicar.gradle.java.library'
	apply plugin: 'com.softicar.gradle.code.validation'
	softicarCodeValidationSettings {
		validationEntryPointClass = "com.example.Validator"
		arguments = ["--some-parameter", "12345"]
	}
	check.dependsOn softicarCodeValidation
}
```

## SoftiCAR Dependency Validation Plugin

This plugin is useful to validate manual dependency conflict resolutions, e.g. by failing the build in case of accidential downgrades of dependencies. See the Javadoc of the plugin class for more details.

#### Usage

For example, in the _build.gradle_ of the root project write this:
```gradle
plugins {
	// we usually don't want to apply the plugin to the root project (apply false)
	id 'com.softicar.gradle.dependency.validation' apply false
}
subprojects{
	configurations.all {
		resolutionStrategy {
			failOnVersionConflict()
			force ...
		}
	}
	apply plugin: 'com.softicar.gradle.dependency.validation'
	check.dependsOn softicarDependencyValidation
}
```

## SoftiCAR Java Library Plugin

This plugin applies the Gradle Java Library plug-in and applies some tweaks.
* It configures the manifest of the generated jar-File.
* It silences some Javadoc compiler diagnostics.

#### Usage

For example, in the _build.gradle_ of the root project write this:
```gradle
plugins {
	id 'com.softicar.gradle.java.library'
}
subprojects {
	apply plugin: 'com.softicar.gradle.java.library'
}
```

## SoftiCAR Selenium Grid Plugin

This plugin is useful for projects executing unit tests based on the [Selenium](https://www.selenium.dev/) framework. See the Javadoc of the plugin class for more details.

#### Usage

To use this plugin, the Gradle daemon must be disabled, e.g. in _gradle.properties_:
```
org.gradle.daemon=false
```

This plugin is applied to the root project directly, e.g. in _build.gradle_:
```gradle
plugins {
	id 'com.softicar.gradle.selenium.grid'
}
```

## SoftiCAR Test Logger Plugin

This plugin can be used to debug problems (e.g. concerning performance or determinism) during test execution.

#### Usage

For example, in the _build.gradle_ of the root project write this:
```gradle
plugins {
	// the Java plug-in provides the `test` task that the plugin binds to
	id 'com.softicar.gradle.java.library'
	// we usually don't want to apply the plugin to the root project (apply false)
	id 'com.softicar.gradle.test.logger' apply false
}
subprojects {
	apply plugin: 'com.softicar.gradle.java.library'
	apply plugin: 'com.softicar.gradle.test.logger'
}
```

And then execute _Gradle_ with the following parameter:
```
./gradlew -Pcom.softicar.test.logger.enabled=true check
```
