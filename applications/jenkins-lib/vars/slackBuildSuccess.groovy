#!/usr/bin/env groovy

import groovy.json.JsonBuilder

def call(channel = '#dh-ops') {
  name = "${env.PROJECT_NAME}:${env.DOCKER_TAG}"
  link = "<${env.RUN_DISPLAY_URL}|${currentBuild.displayName}>"
  message =  "Successfully built `${name}` (${link})"
  gitLink = env.GIT_URL
  if (gitLink.contains('github.com')) {
    gitLink = gitLink.replace('.git', '') + "/commit/${env.DOCKER_TAG}"
  }

  actions = [
    linkButton(':git: Git', gitLink),
    linkButton(':jenkins: Jenkins', env.RUN_DISPLAY_URL.replace('http:', 'https:'))
  ]

  if (env.DOCKER_REGISTRY == 'dtr.nrk.no') {
    message += "\nUsage: `docker pull ${env.TEAM_NAME}/${env.PROJECT_NAME}:${env.DOCKER_TAG}`"
    actions.push(linkButton(
      ':docker: DTR',
      "https://dtr.nrk.no/${env.TEAM_NAME}/${env.PROJECT_NAME}"
    ))
  }

  // Build attachment
  attachment = [
    text: message,
    actions: actions,
    color: 'good',
    mrkdwn_in: [ 'text' ]
  ]

  attachments = new JsonBuilder([ attachment ]).toString()
  slackSend channel: channel, attachments: attachments
}

def linkButton(text, url) {
  button = [:]
  button.text = text
  button.type = 'button'
  button.url = url
  return button
}
