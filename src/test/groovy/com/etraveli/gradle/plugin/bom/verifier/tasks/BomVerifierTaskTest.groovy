package com.etraveli.gradle.plugin.bom.verifier.tasks

import com.etraveli.gradle.plugin.bom.verifier.BomVerifierPluginExtension
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat
import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class BomVerifierTaskTest extends Specification {

  final static String PROJECT_NAME = 'bomVerifierTaskTest'

  @Rule
  final TemporaryFolder testProjectDir = new TemporaryFolder()
  File buildFile

  def setup() {
    buildFile = testProjectDir.newFile('build.gradle')
    testProjectDir.newFile('settings.gradle') << """
            rootProject.name='$PROJECT_NAME'
            """
  }

  def "Verifying against non-existing BOM file should throw missing BOM file exception"() {
    given:
    buildFile << """
            plugins {
              id 'java'
              id 'bom-verifier'
            }
            repositories {
              mavenCentral()
            }
            dependencies {
              compileOnly 'org.jdom:jdom2:2.0.6'
              compile 'org.testng:testng:6.8.8'
              runtime 'org.mockito:mockito-core:1.9.5'
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomVerifierTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .buildAndFail()

    then:
    result.task(":$BomVerifierTask.NAME").outcome == FAILED
    result.output.contains('Run task writeNewBom to create BOM file')
  }

  def "When verifying matching dependencies against BOM file then verification should pass"() {
    given:
    def File bom = testProjectDir.newFile("${PROJECT_NAME}.bom") << """com.beust:jcommander:1.27
org.beanshell:bsh:2.0b4
org.hamcrest:hamcrest-core:1.1
org.mockito:mockito-core:1.9.5
org.objenesis:objenesis:1.0
org.testng:testng:6.8.8"""

    buildFile << """
            plugins {
              id 'java'
              id 'bom-verifier'
            }
            ${BomVerifierPluginExtension.NAME} {
              bomFilePath = "${testProjectDir.getRoot().absolutePath}"
            }
            repositories {
              mavenCentral()
            }
            dependencies {
              compileOnly 'org.jdom:jdom2:2.0.6'
              compile 'org.testng:testng:6.8.8'
              runtime 'org.mockito:mockito-core:1.9.5'
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomVerifierTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    result.task(":$BomVerifierTask.NAME").outcome == SUCCESS
  }

  def "When verifying matching dependencies using custom configuration against BOM file then verification should pass"() {
    given:
    def bom = testProjectDir.newFile("${PROJECT_NAME}.bom") << """org.apache.commons:commons-lang3:3.4"""

    buildFile << """
            plugins {
              id 'java'
              id 'bom-verifier'
            }
            repositories {
              maven {
                name = 'artifactory.etraveli.net'
                url = "https://artifactory.etraveli.net/artifactory/remote-repos/"
              }
            }
            sourceSets {
              custom
            }
            bomVerifier {
              configuration = configurations.customCompile
            }
            dependencies {
              compileOnly 'org.jdom:jdom2:2.0.6'
              customCompile 'org.apache.commons:commons-lang3:3.4'
              compile 'org.testng:testng:6.8.8'
              runtime 'org.mockito:mockito-core:1.9.5'
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomVerifierTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    result.task(":$BomVerifierTask.NAME").outcome == SUCCESS
  }

  def "When verifying non-matching dependencies against BOM file then verification should fail"() {
    given:
    def File bom = testProjectDir.newFile("${PROJECT_NAME}.bom") << """com.beust:jcommander:1.27
org.beanshell:bsh:2.0b4
org.hamcrest:hamcrest-core:1.1
org.mockito:mockito-core:1.9.5
org.objenesis:objenesis:1.1
org.testng:testng:6.8.8"""

    buildFile << """
            plugins {
              id 'java'
              id 'bom-verifier'
            }
            ${BomVerifierPluginExtension.NAME} {
              bomFilePath = "${testProjectDir.getRoot().absolutePath}"
            }
            repositories {
              mavenCentral()
            }
            dependencies {
              compileOnly 'org.jdom:jdom2:2.0.6'
              compile 'org.testng:testng:6.8.8'
              runtime 'org.mockito:mockito-core:1.9.5'
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomVerifierTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .buildAndFail()

    then:
    result.task(":$BomVerifierTask.NAME").outcome == FAILED
  }

  def "When dependencies are missing in configuration then they should be logged in build failure message"() {
    given:
    def File bom = testProjectDir.newFile("${PROJECT_NAME}.bom") << """com.beust:jcommander:1.27
org.beanshell:bsh:2.0b4
org.hamcrest:hamcrest-core:1.1
org.mockito:mockito-core:1.9.5
org.objenesis:objenesis:1.0
org.testng:testng:6.8.8"""

    buildFile << """
            plugins {
              id 'java'
              id 'bom-verifier'
            }
            ${BomVerifierPluginExtension.NAME} {
              bomFilePath = "${testProjectDir.getRoot().absolutePath}"
            }
            repositories {
              mavenCentral()
            }
            dependencies {
              compileOnly 'org.jdom:jdom2:2.0.6'
              compile 'org.testng:testng:6.8.8'
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomVerifierTask.NAME)
      .withPluginClasspath()
      .buildAndFail()

    then:
    assertThat(result.output).containsSequence(['Declared in BOM, but not in configuration',
                                                'org.hamcrest:hamcrest-core:1.1',
                                                'org.mockito:mockito-core:1.9.5',
                                                'org.objenesis:objenesis:1.0',
                                                'Run task writeNewBom to create new BOM file'])
    !result.output.contains('Declared in configuration, but not in BOM')
  }

  def "When dependencies are missing in BOM then they should be logged in build failure message"() {
    given:
    def File bom = testProjectDir.newFile("${PROJECT_NAME}.bom") << """com.beust:jcommander:1.27
org.beanshell:bsh:2.0b4
org.hamcrest:hamcrest-core:1.1
org.mockito:mockito-core:1.9.5
org.testng:testng:6.8.8"""

    buildFile << """
            plugins {
              id 'java'
              id 'bom-verifier'
            }
            ${BomVerifierPluginExtension.NAME} {
              bomFilePath = "${testProjectDir.getRoot().absolutePath}"
            }
            repositories {
              mavenCentral()
            }
            dependencies {
              compileOnly 'org.jdom:jdom2:2.0.6'
              compile 'org.testng:testng:6.8.8'
              runtime 'org.mockito:mockito-core:1.9.5'
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomVerifierTask.NAME)
      .withPluginClasspath()
      .buildAndFail()

    then:
    assertThat(result.output).containsSequence(['Declared in configuration, but not in BOM',
                                                'org.objenesis:objenesis:1.0',
                                                'Run task writeNewBom to create new BOM file'])
    !result.output.contains('Declared in BOM, but not in configuration')
  }

  def "When dependencies are missing in both configuration and BOM then they should both be logged in build failure message"() {
    given:
    def File bom = testProjectDir.newFile("${PROJECT_NAME}.bom") << """com.beust:jcommander:1.27
org.beanshell:bsh:2.0b4
org.hamcrest:hamcrest-core:1.1
org.mockito:mockito-core:1.9.5
junit:junit:4.12
org.testng:testng:6.8.8"""

    buildFile << """
            plugins {
              id 'java'
              id 'bom-verifier'
            }
            ${BomVerifierPluginExtension.NAME} {
              bomFilePath = "${testProjectDir.getRoot().absolutePath}"
            }
            repositories {
              mavenCentral()
            }
            dependencies {
              compileOnly 'org.jdom:jdom2:2.0.6'
              compile 'org.testng:testng:6.8.8'
              runtime 'org.mockito:mockito-core:1.9.5'
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomVerifierTask.NAME)
      .withPluginClasspath()
      .buildAndFail()

    then:
    assertThat(result.output).containsSequence(['Declared in BOM, but not in configuration',
                                                'junit:junit:4.12',
                                                'Declared in configuration, but not in BOM',
                                                'org.objenesis:objenesis:1.0',
                                                'Run task writeNewBom to create new BOM file'])
  }
}
