#!/usr/bin/env groovy
/*
 * Preparation step to set useful environmental variables that can
 * be used in the Pipeline steps. Inspired by the Kurator libs.
 */
def call(projectName = null, teamName = 'historieutvikling') {
  def git_remote = sh(returnStdout: true, script: 'git ls-remote --get-url')
  env.TEAM_NAME = teamName
  env.DTR_API = "https://${env.DOCKER_REGISTRY}/api/v0"
  env.PROJECT_NAME = projectName ? projectName : git_remote.split('/')[-1].split("\\.")[0]
  env.DOCKER_REPO = "${env.TEAM_NAME}/${env.PROJECT_NAME}"
  env.DOCKER_TAG = gitTag() ? gitTag() : gitRevision()
  env.DOCKER_IMAGE = "${env.DOCKER_REGISTRY}/${env.DOCKER_REPO}:${env.DOCKER_TAG}"
  env.MODULE_PACKAGE_VERSION = getParsedValueFromPackageJson('.version')
  env.MODULE_PACKAGE_NAME = getParsedValueFromPackageJson('.name')
  env.MODULE_PACKAGE_REPOSITORY = "https://npmjs.com"
  env.MODULE_PACKAGE_URL = "${env.MODULE_PACKAGE_REPOSITORY}/package/${env.MODULE_PACKAGE_NAME}"
  env.NODE_ENV = "production"
}
