#!/usr/bin/env groovy

def call(channel = '#dh-ops') {
  def name = "${env.PROJECT_NAME}"
  def link = "<${env.RUN_DISPLAY_URL}|build ${currentBuild.displayName}>"

  slackSend color: 'warning',
            channel: channel,
            message: "Deploy failure for `${name}` (${link})"
}
