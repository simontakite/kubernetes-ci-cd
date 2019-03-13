#!/usr/bin/env groovy

def call(channel = '#dh-ops') {
  def name = "${env.PROJECT_NAME}@${env.MODULE_PACKAGE_VERSION}"
  def link = "<${env.RUN_DISPLAY_URL}|build ${currentBuild.displayName}>"

  slackSend color:   'good',
            channel: channel,
            message: "Deployed `${name}` (${link}) to *${env.MODULE_PACKAGE_REPOSITORY}*\n" +
                     "URL: ${env.MODULE_PACKAGE_URL}"
}
