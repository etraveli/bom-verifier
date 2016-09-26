package com.etraveli.gradle.plugin.bom.verifier.tools

import org.gradle.api.artifacts.*

class ConfigurationDependenciesResolver {

  final boolean includeProjectDependencyVersions

  ConfigurationDependenciesResolver(boolean includeProjectDependencyVersions) {
    this.includeProjectDependencyVersions = includeProjectDependencyVersions
  }

  Set<String> resolve(Configuration configuration) {
    def deps = [] as Set
    addDeclaredDependencies(configuration, deps)
    addResolvedDependencies(configuration, deps)
    if (!includeProjectDependencyVersions) {
      useWildCardForProjectDependencyVersions(configuration, deps)
    }
    deps
  }

  private void addDeclaredDependencies(Configuration configuration, deps) {
    configuration.allDependencies.withType(ExternalDependency).each { Dependency dependency ->
      deps << createDependencyKey(dependency.group, dependency.name, dependency.version)
    }
  }

  String createDependencyKey(String group, String name, String version) {
    return (group ?: '') + ':' + name + ':' + version
  }

  private void addResolvedDependencies(Configuration configuration, deps) {
    configuration.resolvedConfiguration.firstLevelModuleDependencies.each { ResolvedDependency resolved ->
      addTransitiveDependencies(resolved, deps)
    }
  }

  void addTransitiveDependencies(ResolvedDependency parent, Set deps) {
    deps << createDependencyKey(parent.moduleGroup, parent.moduleName, parent.moduleVersion)
    if (parent.children.size() > 0) {
      parent.children.each { ResolvedDependency child ->
        addTransitiveDependencies(child, deps)
      }
    }
  }

  private void useWildCardForProjectDependencyVersions(Configuration configuration, deps) {
    configuration.allDependencies.withType(ProjectDependency).each { ProjectDependency prjDep ->
      deps.remove(createDependencyKey(prjDep.group, prjDep.name, prjDep.version))
      deps << createDependencyKey(prjDep.group, prjDep.name, '*')
      if (prjDep.projectConfiguration) {
        useWildCardForProjectDependencyVersions(prjDep.projectConfiguration, deps)
      }
    }
  }
}
