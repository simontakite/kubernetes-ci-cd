#!/usr/bin/env groovy

/*
 * pushStatic uploads static assets to Akamai NetStorage. The content will
 * be available through Akamai CDN: https://static.nrk.no/dh/project-name/
 *
 * @param root directory is `/dh` by default
 * @param subdirectory is the project name by default
*/
def call(root = 'dh', subdirectory = env.PROJECT_NAME) {
  if (env.BRANCH_NAME == 'master') {
    sh "mkdir -p .collected_assets/${subdirectory}"
    docker.image(env.DOCKER_IMAGE).withRun("--name=${env.PROJECT_NAME}-${env.DOCKER_TAG} --stop-timeout=1", "/bin/sleep 1000") {
      sh "docker cp ${env.PROJECT_NAME}-${env.DOCKER_TAG}:/usr/src/app/build/static .collected_assets/${subdirectory}/"
    }
    sshagent(['akamai-netstorage']) {
      sh "cd .collected_assets; scp -r -o HostKeyAlgorithms=+ssh-dss -o StrictHostKeyChecking=no ${subdirectory} sshacs@Statiknrkno.upload.akamai.com:/${root}/"
    }
    sh 'rm -rf .collected_assets'
    env.STATIC_PUSHED = 1
    env.STATIC_HOST = "https://static.nrk.no/${root}/${subdirectory}"
  } else {
    echo "Not on master branch - skipping static upload to Akamai NetStorage"
  }
}
