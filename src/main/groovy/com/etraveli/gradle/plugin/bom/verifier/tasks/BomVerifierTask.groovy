package com.etraveli.gradle.plugin.bom.verifier.tasks

import com.etraveli.gradle.plugin.bom.verifier.BomVerifierPluginExtension
import com.etraveli.gradle.plugin.bom.verifier.exceptions.DependencyMismatchException
import com.etraveli.gradle.plugin.bom.verifier.tools.ConfigurationDependenciesResolver
import org.gradle.api.DefaultTask
import org.gradle.api.resources.MissingResourceException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

class BomVerifierTask extends DefaultTask {

  final static String NAME = 'verifyBom'

  @TaskAction
  def verifyBom() {
    def extension = project.extensions.getByType(BomVerifierPluginExtension.class)
    def bomDefinedDependencies = readDependenciesFromBom(extension)
    def resolvedDependencies = new ConfigurationDependenciesResolver(extension.includeProjectDependencyVersions).resolve(extension.configuration)

    def inBomNotInConfiguration = (bomDefinedDependencies - resolvedDependencies).join("\n")
    def inConfigurationNotInBom = (resolvedDependencies - bomDefinedDependencies).join("\n")

    if (!(inBomNotInConfiguration).empty || !(inConfigurationNotInBom).empty) {
      def errorMessage = 'Dependencies differ from BOM!'

      if (!inBomNotInConfiguration.empty) {
        errorMessage += '\n\nDeclared in BOM, but not in configuration:\n\n' + inBomNotInConfiguration
      }

      if (!inConfigurationNotInBom.empty) {
        errorMessage += '\n\nDeclared in configuration, but not in BOM:\n\n' + inConfigurationNotInBom
      }

      errorMessage += '\n\nRun task writeNewBom to create new BOM file.'

      throw new TaskExecutionException(this, new DependencyMismatchException(errorMessage))
    }
  }

  private List<String> readDependenciesFromBom(BomVerifierPluginExtension extension) {
    def bomFile = new File(extension.bomFilePath + "/" + extension.bomFileName)

    if (!bomFile.exists()) {
      throw new TaskExecutionException(this,
                                       new MissingResourceException('BOM file does not exist when trying to read ' +
                                                                    bomFile.absolutePath +
                                                                    '.\nRun task ' + BomWriterTask.NAME +
                                                                    ' to create BOM file.'))
    }

    def bomDefinedDependencies = bomFile.readLines()
    bomDefinedDependencies
  }

}
