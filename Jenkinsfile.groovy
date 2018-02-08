#!/usr/bin/groovy
@Library('msa-cicd-jenkins-shared-libs')_
import com.evobanco.Utils




def runJenkinsfile() {

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
    def isPPCJenkinsYaml = false
    def isPPCApplicationDevProperties = false
    def isPPCApplicationUatProperties = false
    def isPPCApplicationProdProperties = false


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

        stage('Detect Parallel project configuration (PPC)') {

            def pom = readMavenPom()
            def projectURL = pom.url
            def artifactId = pom.artifactId

            try {
                def parallelConfigurationProject = utils.getParallelConfigurationProjectURL(projectURL, artifactId)

                echo "Parallel configuration project ${parallelConfigurationProject} searching"

                checkout([$class                           : 'GitSCM',
                          branches                         : [[name: 'master']],
                          doGenerateSubmoduleConfigurations: false,
                          extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                               relativeTargetDir: '/tmp/configs']],
                          submoduleCfg                     : [],
                          userRemoteConfigs                : [[credentialsId: 'f8692545-6ab0-479b-aac6-02f66050aab4',
                                                               url          : parallelConfigurationProject]]])

                echo "Parallel configuration project ${parallelConfigurationProject} exits"

                // Jenkins.yml
                try {
                    params = readYaml file: '/tmp/configs/Jenkins.yml'
                    isPPCJenkinsYaml = true
                    echo "Parallel configuration project Jenkins.yml found"
                } catch (exc) {
                    echo "Parallel configuration project Jenkins.yml not found"
                }

                try {
                    readProperties file: '/tmp/configs//configuration_profiles/application-dev.properties'
                    isPPCApplicationDevProperties = true
                    echo "Parallel configuration project profile application-dev.properties found"
                } catch (exc) {
                    echo "Parallel configuration project profile application-dev.properties not found"
                }

                //application-uat.properties
                try {
                    readProperties file: '/tmp/configs//configuration_profiles/application-uat.properties'
                    isPPCApplicationUatProperties = true
                    echo "Parallel configuration project profile application-uat.properties found"
                } catch (exc) {
                    echo "Parallel configuration project profile application-uat.properties not found"
                }


                //application-prod.properties
                try {
                    readProperties file: '/tmp/configs//configuration_profiles/application-prod.properties'
                    isPPCApplicationProdProperties = true
                    echo "Parallel configuration project profile application-prod.properties found"
                } catch (exc) {
                    echo "Parallel configuration project profile application-prod.properties not found"
                }

                echo "isPPCJenkinsYaml : ${isPPCJenkinsYaml}"
                echo "isPPCApplicationDevProperties : ${isPPCApplicationDevProperties}"
                echo "isPPCApplicationUatProperties : ${isPPCApplicationUatProperties}"
                echo "isPPCApplicationProdProperties : ${isPPCApplicationProdProperties}"

            }
            catch (exc) {
                echo 'Something failed, I should sound the klaxons!'
            }
        }

        echo "END (PGC)"
    }
}

return this;

