#!/usr/bin/groovy
import com.evobanco.Utils




def runGenericJenkinsfile() {

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


    def branchPPC = 'master'
    def credentialsIdPPC = 'f8692545-6ab0-479b-aac6-02f66050aab4'
    def relativeTargetDirPPC = '/ppc/configs/'
    def isPPCJenkinsFile = false
    def isPPCJenkinsYaml = false
    def isPPCOpenshiftTemplate = false
    def isPPCApplicationDevProperties = false
    def isPPCApplicationUatProperties = false
    def isPPCApplicationProdProperties = false
    def jenkinsFilePathPPC = '/ppc/configs/Jenkinsfile'
    def jenkinsYamlPathPPC = '/ppc/configs/Jenkins.yml'
    def openshiftTemplatePathPPC = '/ppc/configs/kube/template.yaml'
    def applicationDevPropertiesPathPPC = '/ppc/configs/configuration_profiles/dev/application-dev.properties'
    def applicationUatPropertiesPathPPC = '/ppc/configs/configuration_profiles/uat/application-uat.properties'
    def applicationProdPropertiesPathPPC = '/ppc/configs/configuration_profiles/prod/application-prod.properties'
    def jenknsFilePipelinePPC

    def gitDefaultProjectConfigurationPath='https://github.com/isanmartin0/jenkinsfile_proyecto_generico_configuracion'
    def relativeTargetDirGenericPGC = '/generic/configs/'
    def branchGenericPGC = 'master'
    def credentialsIdGenericPGC = 'f8692545-6ab0-479b-aac6-02f66050aab4'
    def jenkinsYamlGenericPath = '/generic/configs/Jenkins.yml'
    def openshiftTemplateGenericPath = '/generic/configs/kube/template.yaml'


    node('maven') {
        echo "BEGIN GENERIC CONFIGURATION PROJECT (PGC)"
        sleep 10
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

                echo "Generic configuration project loading"

                checkout([$class                           : 'GitSCM',
                          branches                         : [[name: branchGenericPGC]],
                          doGenerateSubmoduleConfigurations: false,
                          extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                               relativeTargetDir: relativeTargetDirGenericPGC]],
                          submoduleCfg                     : [],
                          userRemoteConfigs                : [[credentialsId: credentialsIdGenericPGC,
                                                               url          : gitDefaultProjectConfigurationPath]]])


                // Jenkins.yml
                isGenericJenkinsYaml = fileExists jenkinsYamlGenericPath

                if(isGenericJenkinsYaml) {
                    echo "Generic configuration project Jenkins.yml found"
                } else {
                    echo "Generic configuration project Jenkins.yml not found"
                }



                echo "Parallel configuration project ${parallelConfigurationProject} searching"

                checkout([$class                           : 'GitSCM',
                          branches                         : [[name: branchPPC]],
                          doGenerateSubmoduleConfigurations: false,
                          extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                               relativeTargetDir: relativeTargetDirPPC]],
                          submoduleCfg                     : [],
                          userRemoteConfigs                : [[credentialsId: credentialsIdPPC,
                                                               url          : parallelConfigurationProject]]])

                echo "Parallel configuration project ${parallelConfigurationProject} exits"

                // Jenkinsfile
                isPPCJenkinsFile = fileExists jenkinsFilePathPPC

                if (isPPCJenkinsFile) {
                    echo "Parallel configuration project Jenkinsfile found"
                } else {
                    echo "Parallel configuration project Jenkinsfile not found"
                }


                // Jenkins.yml
                isPPCJenkinsYaml = fileExists jenkinsYamlPathPPC

                if (isPPCJenkinsYaml) {
                    echo "Parallel configuration project Jenkins.yml found"
                } else {
                    echo "Parallel configuration project Jenkins.yml not found"
                }

                // Openshift template (template.yaml)
                isPPCOpenshiftTemplate = fileExists openshiftTemplatePathPPC

                if (isPPCOpenshiftTemplate) {
                    echo "Parallel configuration project Openshift template found"
                } else {
                    echo "Parallel configuration project Openshift template not found"
                }

                //application-dev.properties
                isPPCApplicationDevProperties = fileExists applicationDevPropertiesPathPPC

                if (isPPCApplicationDevProperties) {
                    echo "Parallel configuration project profile application-dev.properties found"
                } else {
                    echo "Parallel configuration project profile application-dev.properties not found"
                }

                //application-uat.properties
                isPPCApplicationUatProperties = fileExists applicationUatPropertiesPathPPC

                if (isPPCApplicationUatProperties) {
                    echo "Parallel configuration project profile application-uat.properties found"
                } else {
                    echo "Parallel configuration project profile application-uat.properties not found"
                }


                //application-prod.properties
                isPPCApplicationProdProperties = fileExists applicationProdPropertiesPathPPC

                if (isPPCApplicationProdProperties) {
                    echo "Parallel configuration project profile application-prod.properties found"
                } else {
                    echo "Parallel configuration project profile application-prod.properties not found"
                }


                echo "isPPCJenkinsFile : ${isPPCJenkinsFile}"
                echo "isPPCJenkinsYaml : ${isPPCJenkinsYaml}"
                echo "isPPCOpenshiftTemplate : ${isPPCOpenshiftTemplate}"
                echo "isPPCApplicationDevProperties : ${isPPCApplicationDevProperties}"
                echo "isPPCApplicationUatProperties : ${isPPCApplicationUatProperties}"
                echo "isPPCApplicationProdProperties : ${isPPCApplicationProdProperties}"

            }
            catch (exc) {
                echo 'Something failed, I should sound the klaxons!'
            }
        }

        isPPCJenkinsFile = false
        isPPCJenkinsYaml = false


        if (isPPCJenkinsFile) {

            stage('Switch to parallel configuration project Jenkinsfile') {

                echo "Loading Jenkinsfile from Parallel Configuration Project (PPC)"

                jenknsFilePipelinePPC = load jenkinsFilePathPPC

                echo "Jenkinsfile from Parallel Configuration Project (PPC) loaded"

                echo "Executing Jenkinsfile from Parallel Configuration Project (PPC)"

                jenknsFilePipelinePPC.runPPCJenkinsfile()
            }


        } else {
            echo "Executing Jenkinsfile from Generic Configuration Project (PPC)"

            stage('Load pipeline configuration') {

                if (isPPCJenkinsYaml) {

                } else {
                    echo "Loading Generic Configuration Project (PGC) Jenkins.yml"
                    params = readYaml  file: jenkinsYamlGenericPath
                    echo "Generic Configuration Project (PGC) Jenkins.yml loaded"
                }

            }
        }



        echo "END (PGC)"
    }

}

return this;

