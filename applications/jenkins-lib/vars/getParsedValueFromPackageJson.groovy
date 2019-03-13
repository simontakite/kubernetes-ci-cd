#!/usr/bin/env groovy

def call(pathToParse) {
    if (fileExists('package.json')) {
        return sh(returnStdout: true, script: "cat package.json | jq -r '${pathToParse}'").trim()
    }
    return 'Cannot find `package.json`'
}