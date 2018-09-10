/*

The Poll script below:
https://stackoverflow.com/a/35241752

It will require relaxed security settings. This is the how-to:

Install
https://wiki.jenkins.io/display/JENKINS/Script+Security+Plugin

Visit:
https://jenkins.jx.cicd.avtalsbanken.net/scriptApproval/

Add the following signatures under "Signatures already approved:" (as of Jenkins ver. 2.126)
method hudson.scm.PollingResult hasChanges
method jenkins.model.Jenkins getItemByFullName java.lang.String
method jenkins.triggers.SCMTriggerItem poll hudson.model.TaskListener
staticField hudson.model.TaskListener NULL
staticMethod jenkins.model.Jenkins getInstance
*/
@NonCPS boolean poll(String job) {
  Jenkins.instance.getItemByFullName(job).poll(TaskListener.NULL).hasChanges()
}

def UPSTREAM_GIT_REPOS = ['/robertgartman/weblib']
def UPSTREAM_GIT_BRANCHES = ['master', "$BRANCH_NAME"].unique()


pipeline {
    agent {
      label "jenkins-maven"
    }
    environment {
      ORG               = 'robertgartman'
      APP_NAME          = 'webservice'
      isStale = false;
    }
    stages {

      stage('Build stale upstream artifacts') {
        steps {
          script {
            // Cover all the upstream repos
            UPSTREAM_GIT_REPOS.each { repo ->
              // Cover master branch and current branch
              UPSTREAM_GIT_BRANCHES.each { branch ->
                def jenkinsJob = repo+'/'+branch
                echo 'Checking if stale: ' + jenkinsJob
                if (poll(jenkinsJob)) {
                  isStale = true;
                  build job:jenkinsJob, propagate: false, wait: false
                }
              }
            }
          }
        }
      }

      stage('CI Build and push snapshot') {
        when {
          branch 'PR-*'
          equals expected: false, actual: isStale
        }
        environment {
          PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
          PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
          HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
        }
        steps {
          container('maven') {
            sh "mvn versions:set -DnewVersion=$PREVIEW_VERSION"
            sh "mvn install"
            sh 'export VERSION=$PREVIEW_VERSION && skaffold build -f skaffold.yaml'


            sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:$PREVIEW_VERSION"
          }

          dir ('./charts/preview') {
           container('maven') {
             sh "make preview"
             sh "jx preview --app $APP_NAME --dir ../.."
           }
          }
        }
      }
      stage('Build Release') {
        when {
          branch 'master'
          equals expected: false, actual: isStale
        }
        steps {
          container('maven') {
            // ensure we're not on a detached head
            sh "git checkout master"
            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            // so we can retrieve the version in later steps
            sh "echo \$(jx-release-version) > VERSION"
            sh "mvn versions:set -DnewVersion=\$(cat VERSION)"
          }
          dir ('./charts/webservice') {
            container('maven') {
              sh "make tag"
            }
          }
          container('maven') {
            sh 'mvn clean deploy'

            sh 'export VERSION=`cat VERSION` && skaffold build -f skaffold.yaml'


            sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:\$(cat VERSION)"
          }
        }
      }
      stage('Promote to Environments') {
        when {
          branch 'master'
          equals expected: false, actual: isStale
        }
        steps {
          dir ('./charts/webservice') {
            container('maven') {
              sh 'jx step changelog --version v\$(cat ../../VERSION)'

              // release the helm chart
              sh 'jx step helm release'

              // promote through all 'Auto' promotion Environments
              sh 'jx promote -b --all-auto --timeout 1h --version \$(cat ../../VERSION)'
            }
          }
        }
      }
    }
    post {
        always {
            cleanWs()
        }
        failure {
            input """Pipeline failed. 
We will keep the build pod around to help you diagnose any failures. 

Select Proceed or Abort to terminate the build pod"""
        }
    }
  }
