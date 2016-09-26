package com.etraveli.gradle.plugin.bom.verifier

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration;

class BomVerifierPluginExtension {

  final static String NAME = 'bomVerifier'

  String bomFilePath
  String bomFileName
  Configuration configuration
  boolean includeProjectDependencyVersions = false

  BomVerifierPluginExtension(final Project project) {
    this.bomFilePath = project.projectDir.absolutePath
    this.bomFileName = project.name + ".bom"
    this.configuration = project.configurations.runtime
  }
}
