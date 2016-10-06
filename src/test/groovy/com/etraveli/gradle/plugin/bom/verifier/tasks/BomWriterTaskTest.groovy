package com.etraveli.gradle.plugin.bom.verifier.tasks

import com.etraveli.gradle.plugin.bom.verifier.BomVerifierPluginExtension
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BomWriterTaskTest extends Specification {

  final static String PROJECT_NAME = 'bomWriterTaskTest'

  @Rule
  final TemporaryFolder testProjectDir = new TemporaryFolder()
  File buildFile
  File bomFile

  def setup() {
    testProjectDir.newFolder('api')
    testProjectDir.newFolder('impl')
    buildFile = testProjectDir.newFile('build.gradle')
    testProjectDir.newFile('settings.gradle') << """
            rootProject.name='$PROJECT_NAME'
            include 'api'
            include 'impl'

            """
    bomFile = testProjectDir.newFile('runtime.bom') << """com.beust:jcommander:1.27
org.beanshell:bsh:2.0b4
org.hamcrest:hamcrest-core:1.1
org.mockito:mockito-core:1.9.5
org.objenesis:objenesis:1.0
org.testng:testng:6.8.8"""
  }

  def "Default file gets written in default build directory path if not overridden via extension"() {
    given:
    buildFile << """
            plugins {
              id 'java'
              id 'com.etraveli.bom-verifier'
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomWriterTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    new File(testProjectDir.root.absolutePath + "/$PROJECT_NAME" + ".bom").exists()
  }

  def "Default file gets written in specified directory path if overridden via extension"() {
    given:
    def overriddenFileLocation = testProjectDir.root.absolutePath + "/bom"
    buildFile << """
            plugins {
              id 'java'
              id 'com.etraveli.bom-verifier'
            }
            ${BomVerifierPluginExtension.NAME} {
              bomFilePath = "$overriddenFileLocation"
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomWriterTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    new File(overriddenFileLocation + "/" + PROJECT_NAME + ".bom").exists()
  }

  def "Custom file gets written in default directory path if overridden via extension"() {
    given:
    def overriddenFileName = 'customFileName.bupp'
    buildFile << """
            plugins {
              id 'java'
              id 'com.etraveli.bom-verifier'
            }
            ${BomVerifierPluginExtension.NAME} {
              bomFileName = "$overriddenFileName"
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomWriterTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    new File(testProjectDir.root.absolutePath + "/$overriddenFileName").exists()
  }

  def "Custom file gets written in custom directory path if overridden via extension"() {
    given:
    def overriddenFileLocation = testProjectDir.root.absolutePath + "/bom"
    def overriddenFileName = 'customFileName.bupp'
    buildFile << """
            plugins {
              id 'java'
              id 'com.etraveli.bom-verifier'
            }
            ${BomVerifierPluginExtension.NAME} {
              bomFilePath = "$overriddenFileLocation"
              bomFileName = "$overriddenFileName"
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomWriterTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    new File(overriddenFileLocation + "/" + overriddenFileName).exists()
  }

  def "Runtime dependencies get written to BOM file"() {
    given:
    buildFile << """
            plugins {
              id 'java'
              id 'com.etraveli.bom-verifier'
            }
            repositories {
              mavenCentral()
              jcenter()
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
      .withArguments(BomWriterTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    def theNewBomFile = new File(testProjectDir.root.absolutePath + "/$PROJECT_NAME" + ".bom")
    theNewBomFile.exists()
    (bomFile.text.tokenize('\n') - theNewBomFile.text.tokenize('\n')).isEmpty()
  }

  def "Writing BOM file does overwrite file and not append"() {
    def shouldBeOverwritten = testProjectDir.newFile('shouldNotAppend.bom') << """com.beust:jcommander:1.27
org.beanshell:bsh:2.0b4
org.hamcrest:hamcrest-core:1.1
org.mockito:mockito-core:1.9.5
org.objenesis:objenesis:1.0
org.testng:testng:6.8.8"""
    given:
    buildFile << """
            plugins {
              id 'java'
              id 'com.etraveli.bom-verifier'
            }
            repositories {
              mavenCentral()
              jcenter()
            }
            bomVerifier {
              bomFileName = 'bipp.bom'
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
      .withArguments(BomWriterTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    shouldBeOverwritten.readLines().size() == 6
  }

  def "Customized configuration dependencies get written to BOM file"() {
    def customBom = testProjectDir.newFile('custom.bom') << """org.apache.commons:commons-lang3:3.4"""
    given:
    buildFile << """
            plugins {
              id 'java'
              id 'com.etraveli.bom-verifier'
            }
            repositories {
              mavenCentral()
              jcenter()
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
      .withArguments(BomWriterTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    def theNewBomFile = new File(testProjectDir.root.absolutePath + "/$PROJECT_NAME" + ".bom")
    theNewBomFile.exists()
    (customBom.text.tokenize('\n') - theNewBomFile.text.tokenize('\n')).isEmpty()
  }

  def "Project dependency versions should not get written to BOM file by default"() {
    given:
    buildFile << """
            plugins {
              id 'java'
              id 'com.etraveli.bom-verifier'
            }
            repositories {
              mavenCentral()
              jcenter()
            }

            project(':api') {
              apply plugin: 'java'
              version = '1.0'
              dependencies {
                compile 'org.apache.commons:commons-lang3:3.4'
              }
            }

            project(':impl') {
              apply plugin: 'java'
              version = '1.0'
              dependencies {
                compile project(':api')
              }
            }

            dependencies {
              compileOnly 'org.jdom:jdom2:2.0.6'
              compile project(':impl')
              compile 'org.testng:testng:6.8.8'
              runtime 'org.mockito:mockito-core:1.9.5'
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomWriterTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    def theNewBomFile = new File(testProjectDir.root.absolutePath + "/$PROJECT_NAME" + ".bom")
    theNewBomFile.exists()
    theNewBomFile.readLines().contains("org.apache.commons:commons-lang3:3.4")
    theNewBomFile.readLines().contains("bomWriterTaskTest:api:*")
    theNewBomFile.readLines().contains("bomWriterTaskTest:impl:*")
  }

  def "Project dependency versions should only get written to BOM file when configured to be written"() {
    given:
    buildFile << """
            plugins {
              id 'java'
              id 'com.etraveli.bom-verifier'
            }
            repositories {
              mavenCentral()
              jcenter()
            }
            bomVerifier {
              includeProjectDependencyVersions = true
            }

            project(':api') {
              apply plugin: 'java'
              version = '1.0'
              dependencies {
                compile 'org.apache.commons:commons-lang3:3.4'
              }
            }

            project(':impl') {
              apply plugin: 'java'
              version = '1.0'
              dependencies {
                compile project(':api')
              }
            }

            dependencies {
              compileOnly 'org.jdom:jdom2:2.0.6'
              compile project(':impl')
              compile 'org.testng:testng:6.8.8'
              runtime 'org.mockito:mockito-core:1.9.5'
            }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments(BomWriterTask.NAME, '--stacktrace')
      .withPluginClasspath()
      .build()

    then:
    def theNewBomFile = new File(testProjectDir.root.absolutePath + "/$PROJECT_NAME" + ".bom")
    theNewBomFile.exists()
    theNewBomFile.readLines().contains("org.apache.commons:commons-lang3:3.4")
    theNewBomFile.readLines().contains("bomWriterTaskTest:api:1.0")
    theNewBomFile.readLines().contains("bomWriterTaskTest:impl:1.0")
  }
}
