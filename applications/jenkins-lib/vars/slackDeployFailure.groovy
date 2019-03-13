#!/usr/bin/env groovy

def call(channel = '#dh-ops') {
  def name = "${env.PROJECT_NAME}:${env.DOCKER_TAG}"
  def link = "<${env.RUN_DISPLAY_URL}|${currentBuild.displayName}>"
  def message = "Deployment failed for `${name}` (${link})"
  if (env.APP_ENV != null) message += " to *${env.APP_ENV}*"
  slackSend color: 'warning', channel: channel, message: message
}
