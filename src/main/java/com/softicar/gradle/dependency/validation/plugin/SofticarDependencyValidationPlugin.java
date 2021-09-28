package com.softicar.gradle.dependency.validation.plugin;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.logging.Logger;
import org.gradle.util.internal.VersionNumber;

/**
 * This Gradle plug-in analyzes and validates resolved dependencies, in order to
 * deliberately fail the build if the validation yields a negative result.
 * <p>
 * This plug-in is currently solely concerned with the validation of dependency
 * conflict resolutions. Particularly, for any given library with at least two
 * conflicting versions in the classpath dependencies, this plug-in ensures that
 * the latest version (or an even newer one) among all encountered versions is
 * used. That is, accidental downgrades, e.g. by using Gradle's "force"
 * mechanism in a resolution strategy, are avoided.
 * <p>
 * The following table exemplifies cases in which this plug-in would (not) fail
 * the build:
 *
 * <pre>
 * | Classpath | Enforced | Build  |
 * | Versions  | Version  | Fails  |
 * +-----------+----------+--------+
 * | 1.1, 1.2  | 1.1      | yes    |
 * | 1.1, 1.2  | 1.2      | no     |
 * | 1.1, 1.2  | 1.3      | no     |
 * </pre>
 *
 * The underlying assumption is that dependency downgrades are very likely to
 * crash the dependent code, and that we hence always want to use the highest
 * version of a dependency (among all versions transitively required by
 * artifacts in the classpath).
 *
 * @author Alexander Schmidt
 */
public class SofticarDependencyValidationPlugin implements Plugin<Project> {

	// A blacklist of configurations to avoid warnings like the following, when executing the task of this plug-in:
	// "The <configuration-name> configuration has been deprecated for resolution. This will fail with an error in Gradle <gradle-version>."
	// TODO After each upgrade to a new Gradle version, check if we still need this kind of blacklisting.
	private static final Set<String> UNRESOLVABLE_CONFIGURATIONS = Set.of("archives", "default");

	@Override
	public void apply(Project project) {

		DependencyConflictResolutionValidator validator = new DependencyConflictResolutionValidator(project);

		// This registers the validation for each dependency resolution.
		project.getConfigurations().all(configuration -> {
			configuration.getResolutionStrategy().eachDependency(validator::validate);
		});

		// This task explicitly triggers all dependency resolutions.
		project.getTasks().register("softicarDependencyValidation").configure(task -> {
			task.doLast(dummy -> {
				project.getConfigurations().all(configuration -> {
					if (isResolvable(configuration)) {
						configuration.resolve();
					}
				});
				validator.printResultsAndAbortBuildIfNecessary();
			});
		});
	}

	private boolean isResolvable(Configuration configuration) {

		return configuration.isCanBeResolved() && !UNRESOLVABLE_CONFIGURATIONS.contains(configuration.getName());
	}

	private static class DependencyConflictResolutionValidator {

		private final Project project;
		private final Logger logger;
		private final Map<String, DependencyVersionCollector> map;

		public DependencyConflictResolutionValidator(Project project) {

			this.project = Objects.requireNonNull(project);
			this.logger = Objects.requireNonNull(project.getLogger());
			this.map = new TreeMap<>();
		}

		synchronized public void validate(DependencyResolveDetails details) {

			ModuleVersionSelector requested = details.getRequested();
			String requestedVersion = requested.getVersion();
			String requestedName = getModuleName(requested);

			ModuleVersionSelector target = details.getTarget();
			String targetVersion = target.getVersion();
			String targetName = getModuleName(target);

			assertEqualModuleNames(requestedName, targetName);

			map
				.computeIfAbsent(requestedName, dummy -> new DependencyVersionCollector(project))
				.addRequestedVersion(requestedVersion)
				.setResolvedVersion(targetVersion);
		}

		synchronized public void printResultsAndAbortBuildIfNecessary() {

			Map<String, DependencyVersionCollector> map = createDependencyConflictResolutionMap();
			if (!map.isEmpty()) {
				logger.quiet(String.format("Dependencies with conflicting versions: %s", map.size()));
			}

			map.forEach(this::printConflictResolution);
			map.forEach(this::assertValidResolvedVersion);
		}

		private void printConflictResolution(String key, DependencyVersionCollector versionCollector) {

			String message = String
				.format(//
					"%s -- %s versions: %s -- resolved: %s",
					key,
					versionCollector.getRequestedVersionsCount(),
					versionCollector.getRequestedVersionsString(),
					versionCollector.getResolvedVersion());
			logger.quiet(message);
		}

		private Map<String, DependencyVersionCollector> createDependencyConflictResolutionMap() {

			Map<String, DependencyVersionCollector> filteredMap = new TreeMap<>();
			for (Entry<String, DependencyVersionCollector> entry: map.entrySet()) {
				if (entry.getValue().getRequestedVersionsCount() > 1) {
					filteredMap.put(entry.getKey(), entry.getValue());
				}
			}
			return filteredMap;
		}

		private String getModuleName(ModuleVersionSelector selector) {

			return selector.getGroup() + ":" + selector.getName();
		}

		private void assertEqualModuleNames(String requestedModuleName, String resolvedModuleName) {

			if (!requestedModuleName.equals(resolvedModuleName)) {
				throw new GradleException(
					"Build aborted: %s does not work properly when a module name was replaced. (not implemented)"
						.formatted(SofticarDependencyValidationPlugin.class.getSimpleName()));
			}
		}

		private void assertValidResolvedVersion(String moduleName, DependencyVersionCollector versionCollector) {

			VersionNumber resolvedVersionNumber = versionCollector.getResolvedVersionNumber();
			VersionNumber highestRequestedVersionNumber = versionCollector.getHighestRequestedVersionNumber();

			if (resolvedVersionNumber.compareTo(highestRequestedVersionNumber) < 0) {
				throw new GradleException(
					String
						.format(//
							"Dependency validation error in: %s\n"//
									+ "Encountered an unexpected downgrade of '%s' from [%s] to [%s].",
							project.getName(),
							moduleName,
							highestRequestedVersionNumber,
							resolvedVersionNumber));
			}
		}
	}

	private static class DependencyVersionCollector {

		private final Project project;
		private final Set<String> requestedVersions;
		private String resolvedVersion;

		public DependencyVersionCollector(Project project) {

			this.project = project;
			this.requestedVersions = new TreeSet<>();
			this.resolvedVersion = null;
		}

		public DependencyVersionCollector addRequestedVersion(String version) {

			requestedVersions.add(version);
			return this;
		}

		public DependencyVersionCollector setResolvedVersion(String version) {

			if (this.resolvedVersion != null && !this.resolvedVersion.equals(version)) {
				throw new GradleException(
					String
						.format(//
							"Dependency validation error in: %s\n"//
									+ "Unexpectedly encountered several resolved versions: [%s], [%s]",
							project.getName(),
							this.resolvedVersion,
							version));
			}
			this.resolvedVersion = version;
			return this;
		}

		public String getResolvedVersion() {

			return resolvedVersion;
		}

		public VersionNumber getResolvedVersionNumber() {

			return VersionNumber.parse(resolvedVersion);
		}

		public String getRequestedVersionsString() {

			return "[" + requestedVersions.stream().collect(Collectors.joining("], [")) + "]";
		}

		public VersionNumber getHighestRequestedVersionNumber() {

			return requestedVersions.stream().map(VersionNumber::parse).max(VersionNumber::compareTo).orElse(null);
		}

		public int getRequestedVersionsCount() {

			return requestedVersions.size();
		}
	}
}
