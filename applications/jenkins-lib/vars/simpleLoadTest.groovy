#!/usr/bin/env groovy

/*
 * simpleGatlingTest runs a simple HTTP load test
 *
 * @param url to test against
 * @param users to inject, default 10
 * @param ramp duration in seconds, default 60
*/
def call(url, users = 10, ramp = 60) {
    echo "Executing simple load test with Gatling Tool"
    sh "JAVA_OPTS=\"-Dusers=$users -Dduration=$ramp -Durl=$url\" /opt/gatling/bin/gatling.sh -s SimpleLoadTest -rf ${env.WORKSPACE}/results"
    gatlingArchive()
}
