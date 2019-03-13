#!/usr/bin/env groovy

def call(channel = '#dh-ops') {
  slackSend color: 'danger',
            channel: channel,
            message: "Build failed: *${currentBuild.displayName}*" +
                     "\nDetails: ${env.RUN_DISPLAY_URL}"
}
