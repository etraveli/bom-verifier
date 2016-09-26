package com.etraveli.gradle.plugin.bom.verifier.tasks

import com.etraveli.gradle.plugin.bom.verifier.BomVerifierPluginExtension
import com.etraveli.gradle.plugin.bom.verifier.tools.ConfigurationDependenciesResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class BomWriterTask extends DefaultTask {

  final static String NAME = 'writeNewBom'

  @TaskAction
  def writeBom() {
    BomVerifierPluginExtension extension = project.extensions.getByType(BomVerifierPluginExtension.class)
    Set deps = getConfigurationDependencies(extension)
    writeDependenciesToBomFile(extension, deps)
  }

  private static Set getConfigurationDependencies(BomVerifierPluginExtension extension) {
    return new ConfigurationDependenciesResolver(extension.includeProjectDependencyVersions).resolve(extension.configuration)
  }

  private static void writeDependenciesToBomFile(BomVerifierPluginExtension extension, Set deps) {
    File bomFile = createBomFile(extension)
    bomFile.withWriter { w ->
      deps.toList().sort().each { dep ->
        w << "$dep\n"
      }
    }
  }

  private static File createBomFile(BomVerifierPluginExtension extension) {
    new File(extension.bomFilePath).mkdirs()
    return new File(extension.bomFilePath + "/" + extension.bomFileName)
  }
}
