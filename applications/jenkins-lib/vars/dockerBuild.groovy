#!/usr/bin/env groovy

/*
 * dockerBuild builds and pushes the Docker image to the registry.
 * The image is tagged with short form of revision and 'latest'.
 *
 * @param name of the image, e.g. `historieutvikling/foo-app`
 * @param registry defaults to https://dtr.nrk.no
 * @param credentials defaults to dtr-credentials
*/
def call(imageName = "${env.TEAM_NAME}/${env.PROJECT_NAME}",
         registry = "https://${env.DOCKER_REGISTRY}",
         credentials = 'dtr-credentials') {

  // Create the repository on DTR
  if (registry == 'https://dtr.nrk.no') {
    def nameParts = imageName.split('/')
    def namespace = nameParts[0]
    def repository = nameParts[1]
    createDockerRepository(repository, namespace, credentials)
  }

  if (registry == 'https://eu.gcr.io' && credentials == 'dtr-credentials') {
    credentials = 'gcr:historieutvikling'
  }

  // Build the Docker image and push to registry
  docker.withRegistry(registry, credentials) {
    withCredentials([string(credentialsId: 'npmjs-read', variable: 'NPM_TOKEN'),
                     string(credentialsId: 'git-ssh-key', variable: 'GIT_SSH_KEY')]) {
      sh "docker build -t $imageName . --build-arg NPM_TOKEN=${env.NPM_TOKEN} --build-arg GIT_SSH_KEY=${env.GIT_SSH_KEY} --build-arg BASE_URL=${env.BASE_URL}"
      def image = docker.image imageName
      image.push env.DOCKER_TAG
      image.push 'latest'
    }
  }
}
