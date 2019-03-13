#!/usr/bin/env groovy

def call(channel = '#dh-ops') {
  def name = "${env.PROJECT_NAME}:${env.DOCKER_TAG}"
  def link = "<${env.RUN_DISPLAY_URL}|${currentBuild.displayName}>"

  slackSend color: 'warning',
                   channel: channel,
                   message: "Build failed: `${name}` (${link})"
}
