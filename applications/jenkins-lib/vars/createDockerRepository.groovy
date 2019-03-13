#!/usr/bin/env groovy

import groovy.json.JsonBuilder

/*
 * Create repository in the Docker Trusted Registry (DTR)
 *
 * @param repository name
 * @param namespace for the repository
 * @param credentials for the repository
 */
def createRepository(repository, namespace, credentials = 'dtr-credentials') {
  def payload = new JsonBuilder([
    name : repository,
    shortDescription : "",
    visibility : "public",
    scanOnPush : false
  ]).toString()

  def response = httpRequest(
    authentication: credentials,
    validResponseCodes: '201',
    url: "${env.DTR_API}/repositories/${namespace}",
    httpMode: 'POST',
    contentType: 'APPLICATION_JSON',
    requestBody: payload
  )
}

/*
 * Check if repository exists in the Docker Trusted Registry (DTR)
 *
 * @param repository name
 * @param namespace for the repository, e.g. historieutvikling
 * @param credentials for the repository, e.g. dtr-credentials
 */
def repositoryExists(repository, namespace, credentials) {
  def response = httpRequest(
    authentication: credentials,
    validResponseCodes: '200,404',
    url: "${env.DTR_API}/repositories/${namespace}/${repository}"
  )
  return response.getStatus() == 200
}

/*
 * createDockerRepository creates a repository in the Docker Trusted Registry
 * if it doesn't already exist. API docs: https://dtr.nrk.no/docs/api
 *
 * @param repository name
 * @param namespace defaults to historieutvikling
 * @param credentials defaults to dtr-credentials
 */
def call(repository, namespace = 'historieutvikling', credentials = 'dtr-credentials') {
  if(!repositoryExists(repository, namespace, credentials)) {
    createRepository(repository, namespace, credentials)
  }
}
