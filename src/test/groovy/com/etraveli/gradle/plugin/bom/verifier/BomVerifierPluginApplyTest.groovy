package com.etraveli.gradle.plugin.bom.verifier

import com.etraveli.gradle.plugin.bom.verifier.tasks.BomVerifierTask
import com.etraveli.gradle.plugin.bom.verifier.tasks.BomWriterTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import spock.lang.Specification

class BomVerifierPluginApplyTest extends Specification {

  @Test
  def "Tasks are added to project when Java plugin is available"() {
    Project project = ProjectBuilder.builder().build();
    project.pluginManager.apply 'java'
    project.pluginManager.apply 'com.etraveli.bom-verifier'

    expect:
    project.pluginManager.hasPlugin('com.etraveli.bom-verifier')
    project.tasks.getByName(BomWriterTask.NAME)
    project.tasks.getByName(BomVerifierTask.NAME)
  }

  @Test
  def "Java plugins Check task depends on BOM verification task"() {
    Project project = ProjectBuilder.builder().build();
    project.pluginManager.apply 'java'
    project.pluginManager.apply 'com.etraveli.bom-verifier'

    expect:
    project.pluginManager.hasPlugin('com.etraveli.bom-verifier')
    project.check.getDependsOn().contains(BomVerifierTask.NAME)
  }

  @Test
  def "Tasks are not added to project when Java plugin is not available"() {
    Project project = ProjectBuilder.builder().build();
    project.pluginManager.apply 'com.etraveli.bom-verifier'

    expect:
    project.pluginManager.hasPlugin('com.etraveli.bom-verifier')
    project.tasks.findByName(BomWriterTask.NAME) == null
    project.tasks.findByName(BomVerifierTask.NAME) == null
  }
}
