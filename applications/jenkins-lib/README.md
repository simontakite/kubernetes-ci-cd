# Jenkins for Digital historieutvikling

This repository contains shared libraries for Jenkins.

## Usage

Jenkins is configured to automatically discover and trigger any jobs
defined in a `Jenkinsfile` located in a repository under the Bitbucket
projects `SPES` and `DHLIB`. Builds of pipelines in `master` and
`feature/*` branches are automatically triggered on push.

### Get started

  1. Add a `Jenkinsfile` to your repository in `SPES` or `DHLIB`.
  2. Push to Bitbucket to trigger the jobs in your `Jenkinsfile`.

## Jenkinsfile

```groovy
pipeline {
  agent any

  stages {
    stage('Build') {
      steps {
        dockerBuild('historieutvikling/some-application')
      }
    }

    stage('Stage') {
      steps {
        marathonDeploy('stage')
      }
    }

    stage('Deploy') {
      when { branch 'master' }
      steps {
        marathonDeploy('int')
      }
    }
  }
  post {
    always {
      deleteDir()
    }
  }
}
```

## Security

### Authentication

Active Directory is used for authentication to the Jenkins instance,
i.e. a LDAP simple bind against `felles.ds.nrk.no:636` is attempted.

### Deployment

A DMZ user is normally required for deployment in Mesos. The Jenkins
instances are whitelisted in all Mesos environments (stage, int, dmz),
and call the REST API without authentication. Thus, any user with commit
access to repositories in the projects monitored by Jenkins effectively
has access to trigger Mesos deployments through Jenkins.

## Bitbucket

Pipeline jobs are created automatically by Jenkins if and only if
a `Jenkinsfile` exists in one of the branches in the repository.

`post-receive` hooks are added automatically for all repositories
in Bitbucket projects menioned above. The hook notifies Jenkins
instance of changes in these repositories.
