#!/usr/bin/env groovy

/*
 * gitTag returns the tag of the HEAD commit
 * @return the git tag (if any)
*/
def call() {
  sh(returnStdout: true, script: "git tag --points-at HEAD").trim()
}
