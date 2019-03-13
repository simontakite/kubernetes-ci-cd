#!/usr/bin/env groovy

/*
 * staticDeploy currently deploys configuration to the
 * `dh-static` nginx instance.
 *
 * @param subdomain of the application on Mesos DMZ
*/
def call(subdomain = env.PROJECT_NAME) {
  if (env.BRANCH_NAME == 'master') {
    echo "staticDeploy() is deprecated - use pushStatic() instead"
  } else {
    echo "Not on master branch - skipping static setup for ${subdomain}"
  }
}
