package com.etraveli.gradle.plugin.bom.verifier.exceptions

import org.gradle.api.GradleException

class DependencyMismatchException extends GradleException {
  DependencyMismatchException(String message) {
    super(message)
  }
}
