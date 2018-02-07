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

node('maven') {
    checkout([$class: 'GitSCM', branches: [[name: 'feature/jenkinsfile']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: '/tmp/configs']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '4b18ea85-c50b-40f4-9a81-e89e44e20178', url: 'https://github.com/jucaf/innocv-worldcoo']]])
    echo "ESTE ES EL DEFAULT"
    sleep 10
    checkout scm
    params = readYaml  file: '/tmp/configs/Jenkins.yml'
    stage('Prepare') {
        setDisplayName()

        assert params.openshift.templatePath?.trim()

        branchName = utils.getBranch()
        echo "We are on branch ${branchName}"
        branchType = utils.getBranchType(branchName)
        echo "This branch is a ${branchType} branch"
        branchNameHY = branchName.replace("/", "-").replace(".", "-")
        echo "Branch name processed: ${branchName}"

        artifactoryRepoURL = (branchType == 'master' || branchType == 'release')  ? artifactoryReleasesURL : artifactorySnapshotsURL

        switch (branchType) {
            case 'feature':
                envLabel="dev"
                if (params.maven.profileFeature) {
                    mavenProfile = "-P${params.maven.profileFeature}"
                    springProfile = params.spring.profileFeature
                }
                break
            case 'develop':
                envLabel="dev"
                if (params.maven.profileDevelop) {
                    mavenProfile = "-P${params.maven.profileDevelop}"
                    springProfile = params.spring.profileDevelop
                }
                break
            case 'release':
                envLabel="uat"
                if (params.maven.profileRelease) {
                    mavenProfile = "-P${params.maven.profileRelease}"
                    springProfile = params.spring.profileRelease
                }
                break
            case 'master':
                envLabel="pro"
                if (params.maven.profileMaster) {
                    mavenProfile = "-P${params.maven.profileMaster}"
                    springProfile = params.spring.profileMaster
                }
                break
            case 'hotfix':
                envLabel="uat"
                if (params.maven.profileHotfix) {
                    mavenProfile = "-P${params.maven.profileHotfix}"
                    springProfile = params.spring.profileHotfix
                }
                break
        }
    }

    if (branchName != 'master')
    {
        if (branchType in params.testing.predeploy.checkstyle) {
            stage('Checkstyle') {
                echo "Running Checkstyle artifact..."
                sh "${mavenCmd} checkstyle:check -DskipTests=true ${mavenProfile}"
            }
        } else {
            echo "Skipping Checkstyle..."
        }

        stage('Build') {
            echo "Building artifact..."
            sh "${mavenCmd} package -DskipTests=true -Dcheckstyle.skip=true ${mavenProfile}"
        }

        if (branchType in params.testing.predeploy.unitTesting) {
            stage('Unit Tests') {
                echo "Running unit tests..."
                sh "${mavenCmd} verify -Dcheckstyle.skip=true ${mavenProfile}"
            }
        } else {
            echo "Skipping unit tests..."
        }

        if (branchType in params.testing.predeploy.sonarQube) {
            stage('SonarQube') {
                echo "Running SonarQube..."
                sh "${mavenCmd} sonar:sonar -Dsonar.host.url=${sonarQube} ${mavenProfile}"
            }
        } else {
            echo "Skipping Running SonarQube..."
        }

        stage('Artifact Deploy') {
            echo "Deploying artifact to Artifactory..."
            sh "${mavenCmd} deploy -DskipTests=true -Dcheckstyle.skip=true ${mavenProfile}"
        }
    }

    stage('OpenShift Build') {
        echo "Building image on OpenShift..."

        openshiftCheckAndCreateProject {
            oseCredential = openshiftCredential
            cloudURL = openshiftURL
            environment = envLabel
            jenkinsNS = jenkinsNamespace
            artCredential = artifactoryCredential
            template = params.openshift.templatePath
            branchHY = branchNameHY
            branch_type = branchType
            dockerRegistry = registry
        }

        openshiftBuildProject {
            artCredential = artifactoryCredential
            snapshotRepoUrl = artifactorySnapshotsURL
            repoUrl = artifactoryRepoURL
            javaOpts = ''
            springProfileActive = springProfile
            bc = params.openshift.buildConfigName
            is = params.openshift.imageStreamName
            branchHY = branchNameHY
            branch_type = branchType
        }
    }
}

def deploy = 'Yes'

if (branchType in params.confirmDeploy) {
    stage('Decide on Deploying') {
        deploy = input message: 'Waiting for user approval',
            parameters: [choice(name: 'Continue and deploy?', choices: 'No\nYes', description: 'Choose "Yes" if you want to deploy this build')]
    }
}

if (deploy == 'Yes') {
    node {
        checkout scm
        stage('OpenShift Deploy') {
            echo "Deploying on OpenShift..."

            openshiftDeployProject {
                branchHY = branchNameHY
                branch_type = branchType
            }
        }
    }

    def tasks = [:]

    if (branchType in params.testing.postdeploy.smokeTesting) {
        tasks["smoke"] = {
            stage('Smoke Tests') {
                echo "Running smoke tests..."
                //sh 'bzt testing/smoke.yml'
            }
        }
    } else {
        echo "Skipping smoke tests..."
    }

    if (branchType in params.testing.postdeploy.acceptanceTesting) {
        tasks["acceptance"] = {
            stage('Acceptance Tests') {
                echo "Running acceptance tests..."
                //sh 'bzt testing/acceptance.yml'
            }
        }
    } else {
        echo "Skipping acceptance tests..."
    }

    if (branchType in params.testing.postdeploy.securityTesting) {
        tasks["security"] = {
            stage('Security Tests') {
                echo "Running security tests..."
                //sh 'bzt testing/security.yml'
            }
        }
    } else {
        echo "Skipping security tests..."
    }

    node('maven') { //taurus
        checkout scm
        parallel tasks
    }

    if (branchType in params.testing.postdeploy.performanceTesting) {
        node('maven') { //taurus
            checkout scm
            stage('Performance Tests') {
                echo "Running performance tests..."
                //sh 'bzt testing/performance.yml'
            }
        }
    } else {
        echo "Skipping performance tests..."
    }
}

stage('Notification') {
    echo "Sending Notifications..."

    /*
    if (currentBuild.result != 'SUCCESS') {
        slackSend channel: '#ops-room', color: '#FF0000', message: "The pipeline ${currentBuild.fullDisplayName} has failed."
        hipchatSend (color: 'RED', notify: true, message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        emailext (
            subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
            body: """<p>FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
              <p>Check console output at "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>""",
            recipientProviders: [[$class: 'DevelopersRecipientProvider']]
          )
    }
    */
}