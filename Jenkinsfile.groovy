#!/usr/bin/groovy
@Library('msa-cicd-jenkins-shared-libs')_
import com.evobanco.Utils

def utils = new com.evobanco.Utils()

def artifactorySnapshotsURL = 'https://digitalservices.evobanco.com/artifactory/libs-snapshot-local'
def artifactoryReleasesURL = 'https://digitalservices.evobanco.com/artifactory/libs-release-local'
def sonarQube = 'http://sonarqube:9000'
def openshiftURL = 'https://openshift.grupoevo.corp:8443'
def openshiftCredential = 'openshift'
def registry = '172.20.253.34'
def artifactoryCredential = 'artifactory-token'
def jenkinsNamespace = 'cicd'
def mavenCmd = 'mvn -U -B -s /opt/evo-maven-settings/evo-maven-settings.xml'
def mavenProfile = ''
def springProfile = ''
def params
def envLabel
def branchName
def branchNameHY
def branchType
def artifactoryRepoURL


def runJenkinsfile() {
    node('maven') {
        echo "BEGIN...(PGC)"

        checkout scm

        stage('Prepare') {
            echo "Prepare stage (PGC)"

            setDisplayName()

            echo "${currentBuild.displayName}"

            branchName = utils.getBranch()
            echo "We are on branch ${branchName}"
            branchType = utils.getBranchType(branchName)
            echo "This branch is a ${branchType} branch"
            branchNameHY = branchName.replace("/", "-").replace(".", "-")
            echo "Branch name processed: ${branchName}"
        }

        echo "END (PGC)"
    }
}

return this;

