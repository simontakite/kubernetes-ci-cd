#!/usr/bin/env groovy

import groovy.json.JsonBuilder

def call(channel = '#dh-ops') {
  name = "${env.PROJECT_NAME}:${env.DOCKER_TAG}"
  link = "<${env.RUN_DISPLAY_URL}|${currentBuild.displayName}>"
  message =  "Deployed `${name}` (${link}) to *${env.APP_ENV}*"

  gitLink = env.GIT_URL
  if (gitLink.contains('github.com')) {
    gitLink = gitLink.replace('.git', '') + "/commit/${env.GIT_COMMIT}"
  }

  actions = [
    linkButton(':git: Git', gitLink),
    linkButton(':jenkins: Jenkins', env.RUN_DISPLAY_URL.replace('http:', 'https:'))
  ]

  if (env.APP_URL != null && env.APP_URL != "null") {
    message += "\n${env.APP_URL}"
  }

  if (env.APP_ENV) {
    if (env.APP_ENV.contains(':kubernetes:')) {
      if (env.DEPLOY_INFO) message += "\n```${env.DEPLOY_INFO}```"
      kubeDash = (env.APP_ENV.contains('DMZ') ? 'dmz.nrk.cloud' : 'int.nrk.cloud')
      if (env.KUBE_NAMESPACE && env.KUBE_APP) {
        actions.push(linkButton(
          ':kubernetes: Kubernetes',
          "http://${kubeDash}/#!/deployment/${env.KUBE_NAMESPACE}/${env.KUBE_APP}?namespace=${env.KUBE_NAMESPACE}"
        ))
      }
    }
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

@NonCPS
def bitbucketCommitUrl(gitLink, rev) {
  def m = gitLink =~ /^(.+)\/scm\/(.+)\/(.+)\.git$/
  if (m.matches()) {
    (nix, base, project, repo) = m[0]
    return "${base}/projects/${project}/repos/${repo}/commits/${rev}"
  }
  return gitLink
}
