#!/usr/bin/env groovy
/*
 * Helper function to set a variable for the pipeline steps
 *
 * Expects 2 strings
 */
def call(key, value) {
  env[key] = value
}
