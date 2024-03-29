![GitHub](https://img.shields.io/github/license/softicar/gradle-plugins)
![workflow](https://img.shields.io/github/actions/workflow/status/softicar/gradle-plugins/continuous-integration.yml?branch=main)

# SoftiCAR Gradle Plugins

This repository provides a collection of [Gradle](https://gradle.org/) plugins that are used by various SoftiCAR Java projects. It comprises the following plugins:

* SoftiCAR Code Validation Plugin
* SoftiCAR Dependency Validation Plugin
* SoftiCAR Java Library Plugin
* SoftiCAR Selenium Grid Plugin
* SoftiCAR Test Logger Plugin

For Gradle multi-projects, a given plugin is either applied to the root project or to the subprojects individually.

## 1 Plugins

This repository provides the plugins described below.

In the examples, replace `X.Y.Z` with a [release](../../releases) version number.

### 1.1 SoftiCAR Code Validation Plugin

This plugin enables a project to execute self-contained code validation logic, i.e. code validation logic that is implemented in the project itself or in one of its dependencies.

**Usage**

For example, in the `build.gradle` of the root project write this:

```gradle
plugins {
    // the code validation plugin requires the java library plugin
    id 'com.softicar.gradle.java.library' version 'X.Y.Z'
    // we usually don't want to apply the plugin to the root project (apply false)
    id 'com.softicar.gradle.code.validation' version 'X.Y.Z' apply false
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

### 1.2 SoftiCAR Dependency Validation Plugin

This plugin is useful to validate manual dependency conflict resolutions, e.g. by failing the build in case of accidential downgrades of dependencies. See the Javadoc of the plugin class for more details.

**Usage**

For example, in the _build.gradle_ of the root project write this:

```gradle
plugins {
    // we usually don't want to apply the plugin to the root project (apply false)
    id 'com.softicar.gradle.dependency.validation' version 'X.Y.Z' apply false
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

### 1.3 SoftiCAR Java Library Plugin

This plugin applies the Gradle Java Library plug-in and applies some tweaks.

* It configures the manifest of the generated jar-File.
* It silences some Javadoc compiler diagnostics.

**Usage**

For example, in the _build.gradle_ of the root project write this:

```gradle
plugins {
    id 'com.softicar.gradle.java.library' version 'X.Y.Z'
}
subprojects {
    apply plugin: 'com.softicar.gradle.java.library'
}
```

### 1.4 SoftiCAR Selenium Grid Plugin

This plugin is useful for projects executing unit tests based on the [Selenium](https://www.selenium.dev/) framework. See the Javadoc of the plugin class for more details.

**Usage**

To use this plugin, the Gradle daemon must be disabled, e.g. in _gradle.properties_:

```
org.gradle.daemon=false
```

This plugin is applied to the root project directly, e.g. in _build.gradle_:

```gradle
plugins {
    id 'com.softicar.gradle.selenium.grid' version 'X.Y.Z'
}
```

### 1.5 SoftiCAR Test Logger Plugin

This plugin can be used to debug problems (e.g. concerning performance or determinism) during test execution.

**Usage**

For example, in the _build.gradle_ of the root project write this:

```gradle
plugins {
    // the Java plug-in provides the `test` task that the plugin binds to
    id 'com.softicar.gradle.java.library' version 'X.Y.Z'
    // we usually don't want to apply the plugin to the root project (apply false)
    id 'com.softicar.gradle.test.logger' version 'X.Y.Z' apply false
}
subprojects {
    apply plugin: 'com.softicar.gradle.java.library'
    apply plugin: 'com.softicar.gradle.test.logger'
}
```

And then execute _Gradle_ with the following parameter:

```
./gradlew -Pcom.softicar.test.logger.verbose=true check
```

## 2 Building and Development

To build this repository, a [JDK 15+](https://adoptopenjdk.net/) installation is required. Building is done using the [gradlew](https://docs.gradle.org/current/userguide/gradle_wrapper.html) command.

```
./gradlew clean build
```

For development, a recent [Eclipse IDE for Java Development](https://www.eclipse.org/downloads/packages/) is required. Clone the repository into the *Eclipse* workspace using the *Git* command line client and import it as *Existing Gradle Project*.

To publish this Gradle plug-in to a local repository for testing purposes, manipulate the build files as follows:

1. Modify the `build.gradle` of the root project as follows:

   1. Define a local Maven repository:

          publishing {
              repositories {
                  maven {
                      name = 'localPluginRepository'
                      url = System.properties['user.home'] + '/local-plugin-repository'
                  }
              }
          }

   1. Run:

          ./gradlew clean publish -Pversion=X.Y.Z

1. Choose another project to build with the locally-released plugin, modify its `build.gradle` and `settings.gradle` files, and build it:

   1. In `build.gradle`, add distinct plugins as described in the sections below, e.g.:

          plugins {
              id 'com.softicar.gradle.java.library' version 'X.Y.Z'
              id 'com.softicar.gradle.code.validation' version 'X.Y.Z' apply false
          }

          subprojects {
               apply plugin: 'com.softicar.gradle.code.validation'
          }

   1. At the very top of `settings.gradle`, add:

          pluginManagement {
              repositories {
                  maven {
                      url System.properties['user.home'] + '/local-plugin-repository'
                  }
                  gradlePluginPortal()
              }
          }

   1. Run:

          ./gradlew clean build

1. When you're done testing, revert the above manipulations. Make sure to _not_ add them to a PR.

## 3 Releases and Versioning

Releases of this repository follow the [Semantic Versioning](https://semver.org/) principle.

```
     1.2.3
    /  |  \
major  |  patch
     minor
```

1. If there was an **API break** since the previous release, the **major version** is incremented: `1.2.3 -> 2.0.0` -- API breaks include:
   - Incompatible changes to existing Java code which is part of the API; most notably: changes to (or removal of) `public`/`protected` classes/fields/methods/signatures
   - Changes in the behavior of existing Java code (except fixes of defective behavior)
   - *Any* change to a database table
   - Fundamental changes to the behavior or style of the UI
1. If there was **no API break** but a **new feature** was added, the **minor version** is incremented: `1.2.3 -> 1.3.0`
1. If there was **no API break** and **no new feature** was added, the **patch version** is incremented: `1.2.3 -> 1.2.4`
   - e.g. when *only* defects were fixed

## 4 Contributing

Please read the [contribution guidelines](CONTRIBUTING.md) for this repository and keep our [code of conduct](CODE_OF_CONDUCT.md) in mind.
