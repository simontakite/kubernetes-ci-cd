#!/usr/bin/env groovy

/*
 * kubeDeploy is to be called from a Jenkins Pipeline
 *
 * @param environment should be one of dmz', 'int', 'test'
 * @param kubeConfigurationPath defaults to .ci
 * @param customVars to override environment variables (KUBE_*)
*/
def call(environment, kubeConfigurationPath = ".ci", customVars = [:]) {
  def kubeCredentialsId = 'kube-int'

  if (env.PROJECT_NAME.contains('dh-fotballvm-2018') ||
      env.PROJECT_NAME.contains('dh-kultur-spesial-hnio')) {
    // Avoid breaking projects already deployed with dh- prefix in ingress
    env.KUBE_APP = env.PROJECT_NAME
  } else {
    env.KUBE_APP = env.PROJECT_NAME.replaceFirst(/^dh-/, '')
  }

  // Append normalized suffix to branches
  if(env.BRANCH_NAME && env.BRANCH_NAME != 'master') {
    def suffix = normalize(env.BRANCH_NAME)
    env.KUBE_APP += '-' + suffix
    // Maximum 63 characters for labels in Kubernetes
    if (env.KUBE_APP.length() > 63) {
      env.KUBE_APP = env.KUBE_APP[0..62]
    }
  }

  switch(environment) {
    case 'dmz':
      kubeCredentialsId = 'kube-dmz'
      env.KUBE_NAMESPACE = 'dh'
      env.KUBE_INGRESS_HOST = 'dh.nrk.no'
      break

    case 'gke':
      kubeCredentialsId = 'kube-gke'
      env.KUBE_NAMESPACE = 'dh'
      env.KUBE_INGRESS_HOST = 'dh.google.nrk.cloud'
      break

    case 'int':
      env.KUBE_NAMESPACE = 'dh'
      env.KUBE_INGRESS_HOST = 'dh.kubeint.nrk.no'
      break

    case 'test':
      env.KUBE_NAMESPACE = 'dh-test'
      env.KUBE_INGRESS_HOST = 'dh-test.kubeint.nrk.no'
      break

    case 'stage':
      kubeCredentialsId = 'kube-dmz'
      env.KUBE_NAMESPACE = 'dh-stage'
      env.KUBE_INGRESS_HOST = 'dh-stage.nrk.no'
      break

    default:
      error("Environment $environment is not supported by kubeDeploy")
  }

  // May be overridden by customVars
  env.KUBE_INGRESS_PATH = "/${env.KUBE_APP}"

  // Locate Kubernetes deployment files
  def candidateFiles = findFiles(glob: kubeConfigurationPath + '/*.yaml')
  def deploymentFiles = [ ]

  // Add files determined to be relevant for this deployment, i.e. skip
  // any environment specific files not matching current environment
  def otherEnvironments = [ 'int', 'test', 'stage', 'dmz', 'gke' ]
  otherEnvironments.removeElement(environment)
  if (environment == 'gke') { otherEnvironments.removeElement('dmz') }
  for (i=0; i < candidateFiles.size(); i++) {
    if(!candidateFiles[i].name.matches('.*(' + otherEnvironments.join('|') + ').*')) {
      deploymentFiles.push(candidateFiles[i].name)
    }
  }

  assertTrue(deploymentFiles.size() > 0, "No deployment files in ${kubeConfigurationPath}")

  // Allow overriding variables
  if (customVars) {
    for (kv in mapToList(customVars)) {
      env[kv[0]] = kv[1]
    }
  }

  env.BASE_URL = "https://${env.KUBE_INGRESS_HOST}${env.KUBE_INGRESS_PATH}"
  if (env.STATIC_PUSHED == null) env.STATIC_HOST = env.BASE_URL
  env.APP_ENV = ':kubernetes: ' + environment.toUpperCase()

  archiveArtifacts artifacts: kubeConfigurationPath + '/*.yaml'

  withCredentials([file(credentialsId: kubeCredentialsId, variable: 'KUBECONFIG_FILE')]) {
    def appHasIngress = false
    for (i = 0; i < deploymentFiles.size(); i++) {
      def yamlFile = kubeConfigurationPath + '/' + deploymentFiles[i]
      if (yamlFile.contains("ingress")) { appHasIngress = true }
      sh "envsubst < ${yamlFile} > ${yamlFile}.apply"
      sh "perl -pi -e 's/value: (\\d+)/value: \"\$1\"/g' ${yamlFile}.apply"
      if (yamlFile.contains("deployment") && env.DOCKER_REGISTRY == 'eu.gcr.io') {
        echo 'Add GCR image pull secret to deployment file'
        sh "grep -q imagePullSecrets ${yamlFile}.apply || perl -pi -e 's/(\\s+)containers:/\$1imagePullSecrets:\\n\$1- name: gcr-historieutvikling\\n\$1containers:/g' ${yamlFile}.apply"
      }
      sh "kubectl --kubeconfig ${KUBECONFIG_FILE} apply -f ${yamlFile}.apply"

    }

    if (appHasIngress == true) {
      env.APP_URL = env.BASE_URL
    } else {
      env.APP_URL = null
    }
    // kubectl rollout status will block until all pods are ready
    sh "kubectl --kubeconfig ${KUBECONFIG_FILE} rollout status deploy/$KUBE_APP -n $KUBE_NAMESPACE"

    // Get pod information for Slack notification
    env.DEPLOY_INFO = sh(returnStdout: true, script: "kubectl --kubeconfig ${KUBECONFIG_FILE} get pods -n $KUBE_NAMESPACE --selector=app=$KUBE_APP --field-selector=status.phase=Running -o=custom-columns=NAME:.metadata.name,IP:.status.podIP,NODE:.spec.nodeName | sed 's/.felles.ds.nrk.no//' | sed 's/.dmz.nrk.no//' | sed 's/gke-dh-kubernetes-default-pool-//'")
  }

}

/*
 * Workaround for annoyingly buggy Groovy support in Jenkins, see:
 * https://stackoverflow.com/questions/40159258
 */
@NonCPS
List<List<?>> mapToList(Map map) {
  return map.collect { it ->
    [it.key, it.value]
  }
}
