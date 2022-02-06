def call(){
    allStages()
}

def allStages(){
    stageCleanBuild()
    stageCleanTest()
    stagePackage()
    stageSonar()
    stageQualityGate()
    stageUploadNexus()
}

def stageCleanBuild(){
    stage("Paso 1: Build && Test"){
        sh "echo 'Build && Test!'"
        sh "mvn clean compile -e"
        // code
    }
}

def stageCleanTest(){
    stage("Paso 2: Testear"){
        sh "echo 'Test Code!'"
        // Run Maven on a Unix agent.
        sh "mvn clean test -e"
    }
}

def stagePackage(){
    stage("Paso 3: Build .Jar"){
        sh "echo 'Build .Jar!'"
        // Run Maven on a Unix agent.
        sh "mvn clean package -e"
    }
}

def stageSonar(){
    def projectKey="${GIT_REPO_NAME}'-'${GIT_BRANCH}'-'${env.BUILD_NUMBER}"
    def projectName="${GIT_REPO_NAME}'-'${GIT_BRANCH}'-'${env.BUILD_NUMBER}"
    stage("Paso 4: Análisis SonarQube"){
        withSonarQubeEnv('sonarqube') {
        sh "echo 'Calling sonar Service in another docker container!'"
        // Run Maven on a Unix agent to execute Sonar.
        sh "mvn clean verify sonar:sonar -Dsonar.projectKey='${projectKey}' -Dsonar.projectName='${projectName}'"
        }
    }
}

def stageQualityGate(){
  stage("Paso 4 1/2: Revisar Sonar - Quality Gate"){
    timeout(time: 1, unit: 'MINUTES') {
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
                println(qg)
                slackSend color: 'danger', message: "[${JOB_NAME}] [${BUILD_TAG}] Revisión Sonar fallida ${qg.status}", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-jenkins-slack'
                error "Pipeline aborted due to quality gate failure: ${qg.status}"
            }
        }
       sh "echo 'Quality Gate Passed'"
  }
}

def stageUploadNexus(){
    stage("Paso 5: Subir Nexus"){
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
                    filePath: 'build/LaboratorioM3-ID-0.0.1.jar'
                ]
            ],
                mavenCoordinate: [
                    artifactId: 'LaboratorioM3-ID',
                    groupId: 'com.laboratorioM3',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
    }
}

return this;