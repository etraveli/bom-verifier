package com.etraveli.gradle.plugin.bom.verifier

import com.etraveli.gradle.plugin.bom.verifier.tasks.BomVerifierTask
import com.etraveli.gradle.plugin.bom.verifier.tasks.BomWriterTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class BomVerifierPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    if (project.pluginManager.hasPlugin('java')) {
      project.extensions.create(BomVerifierPluginExtension.NAME, BomVerifierPluginExtension, project)
      addTasks(project)
      addBomVerificationToCheckTask(project)
    }
  }

  private void addTasks(Project project) {
    project.task(BomVerifierTask.NAME, type: BomVerifierTask)
    project.task(BomWriterTask.NAME, type: BomWriterTask)
  }

  private void addBomVerificationToCheckTask(Project project) {
    project.check.dependsOn(BomVerifierTask.NAME)
  }
}
