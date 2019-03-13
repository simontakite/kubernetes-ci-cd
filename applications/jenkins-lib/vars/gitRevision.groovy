#!/usr/bin/env groovy

/*
 * gitRevision returns the git revision in short or full form
 *
 * @param compact if short rev is desired (default)
 * @return the git revision
*/
def call(Boolean compact = true) {
  def args = compact ? '--short' : ''
  sh(returnStdout: true,
     script: "git rev-parse ${args} HEAD").trim()
}
