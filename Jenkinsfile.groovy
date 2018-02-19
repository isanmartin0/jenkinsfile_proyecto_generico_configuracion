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

    //Parallet project configuration (PPC) properties
    def branchPPC = 'master'
    def credentialsIdPPC = 'f8692545-6ab0-479b-aac6-02f66050aab4'
    def relativeTargetDirPPC = '/tmp/configs/PPC/'
    def isPPCJenkinsFile = false
    def isPPCJenkinsYaml = false
    def isPPCOpenshiftTemplate = false
    def isPPCApplicationDevProperties = false
    def isPPCApplicationUatProperties = false
    def isPPCApplicationProdProperties = false
    def jenkinsFilePathPPC = relativeTargetDirPPC + 'Jenkinsfile'
    def jenkinsYamlPathPPC = relativeTargetDirPPC + 'Jenkins.yml'
    def openshiftTemplatePathPPC = relativeTargetDirPPC + 'kube/template.yaml'
    def applicationDevPropertiesPathPPC = relativeTargetDirPPC + 'configuration_profiles/dev/application-dev.properties'
    def applicationUatPropertiesPathPPC = relativeTargetDirPPC + 'configuration_profiles/uat/application-uat.properties'
    def applicationProdPropertiesPathPPC = relativeTargetDirPPC + 'configuration_profiles/prod/application-prod.properties'
    def jenknsFilePipelinePPC

    //Generic project configuration properties
    def gitDefaultProjectConfigurationPath='https://github.com/isanmartin0/jenkinsfile_proyecto_generico_configuracion'
    def relativeTargetDirGenericPGC = '/tmp/configs/generic/'
    def branchGenericPGC = 'master'
    def credentialsIdGenericPGC = 'f8692545-6ab0-479b-aac6-02f66050aab4'
    def jenkinsYamlGenericPath = relativeTargetDirGenericPGC + 'Jenkins.yml'
    def openshiftTemplateGenericPath = relativeTargetDirGenericPGC + 'kube/template.yaml'
    def isGenericJenkinsYaml = false

    def pom
    def projectURL
    def artifactId
    def groupId


    echo "BEGIN GENERIC CONFIGURATION PROJECT (PGC)"

    node('maven') {

        //sleep 10
        checkout scm

        stage('Detect Parallel project configuration (PPC)') {

            pom = readMavenPom()
            projectURL = pom.url
            artifactId = pom.artifactId
            groupId = pom.groupId
            /*
            if (groupId == null || "".equals(groupId)) {
                groupId = pom.parent.groupId
            }

            if (groupId != null) {
                groupId = groupId.trim()
            }


*/

            try {
                def parallelConfigurationProject = utils.getParallelConfigurationProjectURL(projectURL, artifactId)

                echo "Parallel configuration project ${parallelConfigurationProject} searching"

                retry (3)
                        {
                            checkout([$class                           : 'GitSCM',
                                      branches                         : [[name: branchPPC]],
                                      doGenerateSubmoduleConfigurations: false,
                                      extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                           relativeTargetDir: relativeTargetDirPPC]],
                                      submoduleCfg                     : [],
                                      userRemoteConfigs                : [[credentialsId: credentialsIdPPC,
                                                                           url          : parallelConfigurationProject]]])
                        }
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
                echo 'There is an error on retrieving parallel project configuration'
                def exc_message = exc.message
                echo "${exc_message}"
            }
        }

        //Set to force the execution. Remove after tests
        isPPCJenkinsFile = false
        isPPCJenkinsYaml = false
        isPPCOpenshiftTemplate = true


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

                if (isPPCJenkinsYaml && isPPCOpenshiftTemplate) {
                    //The generic pipeline will use Jenkins.yml and template of the parallel project configuration


                } else {
                    //The generic pipeline will use generic Jenkins.yml or generic Openshift template
                    //We need load this elements

                    echo "Generic configuration project loading"

                    retry (3) {
                        checkout([$class                           : 'GitSCM',
                                  branches                         : [[name: branchGenericPGC]],
                                  doGenerateSubmoduleConfigurations: false,
                                  extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                       relativeTargetDir: relativeTargetDirGenericPGC]],
                                  submoduleCfg                     : [],
                                  userRemoteConfigs                : [[credentialsId: credentialsIdGenericPGC,
                                                                       url          : gitDefaultProjectConfigurationPath]]])
                    }

                    echo "Generic configuration project loaded"


                    if (isPPCJenkinsYaml) {
                        //Take parameters of the parallel project configuration (PPC)
                        params = readYaml  file: jenkinsYamlPathPPC
                        echo "Using Jenkins.yml from parallel project configuration (PPC)"
                    } else {
                        //Take the generic parameters
                        params = readYaml  file: jenkinsYamlGenericPath
                        echo "Using Jenkins.yml from generic project"
                    }

                    assert params.openshift.templatePath?.trim()


                    if (isPPCOpenshiftTemplate) {
                        //The template is provided by parallel project configuration (PPC)
                        params.openshift.templatePath = relativeTargetDirPPC + params.openshift.templatePath
                        echo "Template provided by parallel project configuration (PPC)"
                    } else {
                        //The tamplate is provided by generic configuration
                        params.openshift.templatePath = relativeTargetDirGenericPGC + params.openshift.templatePath
                        echo "Template provided by generic configuration project"
                    }

                    echo "params.openshift.templatePath: ${params.openshift.templatePath}"
                }

            }


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

                artifactoryRepoURL = (branchType == 'master' || branchType == 'release')  ? artifactoryReleasesURL : artifactorySnapshotsURL


            }

            stage ('Prepare profiles') {
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

                        echo "groupId: ${groupId}"
                        echo "artifactId: ${artifactId}"

                        def sonar_project_key = groupId + ":" + artifactId + "-" + branchNameHY
                        def sonar_project_name = artifactId + "-" + branchNameHY

                        echo "sonar_project_key: ${sonar_project_key}"
                        echo "sonar_project_name: ${sonar_project_name}"

                        sh "${mavenCmd} sonar:sonar -Dsonar.host.url=${sonarQube} ${mavenProfile} -Dsonar.projectKey=${sonar_project_key} -Dsonar.projectName=${sonar_project_name}"
                    }
                } else {
                    echo "Skipping Running SonarQube..."
                }


                def continuar = input message: 'Waiting for user approval',
                        parameters: [choice(name: 'Continue and deploy?', choices: 'No\nYes', description: 'Choose "Yes" if you want to deploy this build')]

                if (continuar == 'No') {
                    currentBuild.rawBuild.result = Result.ABORTED
                    throw new hudson.AbortException('Guess what!')
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

    } // end of node


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

    echo "END GENERIC CONFIGURATION PROJECT (PGC)"

} //end of method

return this;

