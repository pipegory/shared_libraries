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

    rama = GIT_BRANCH
    if(rama.contains('develop')){
        //stageCreateRelease()
    }
}

def stageCleanBuild(){
    env.TAREA = "Paso 1: Build && Test";
    stage("$env.TAREA"){
        sh "echo 'Build && Test!'"
        sh "mvn clean compile -e"
        // code
    }
}

def stageCleanTest(){
    env.TAREA = "Paso 2: Testear"
    stage("$env.TAREA"){
        sh "echo 'Test Code!'"
        // Run Maven on a Unix agent.
        sh "mvn clean test -e"
    }
}

def stagePackage(){
    env.TAREA = "Paso 3: Build .Jar"
    stage("$env.TAREA"){
        sh "echo 'Build .Jar!'"
        // Run Maven on a Unix agent.
        sh "mvn clean package -e"
    }
}

def stageSonar(){
    def projectKey="${GIT_REPO_NAME}'-'${GIT_BRANCH}'-'${env.BUILD_NUMBER}"
    def projectName="${GIT_REPO_NAME}'-'${GIT_BRANCH}'-'${env.BUILD_NUMBER}"
    env.TAREA = "Paso 4: Análisis SonarQube"
    stage("$env.TAREA"){
        withSonarQubeEnv('sonarqube') {
        sh "echo 'Calling sonar Service in another docker container!'"
        // Run Maven on a Unix agent to execute Sonar.
        sh "mvn clean verify sonar:sonar -Dsonar.projectKey='${projectKey}' -Dsonar.projectName='${projectName}'"
        }
    }
}

def stageQualityGate(){
    env.TAREA = "Paso 4 1/2: Revisar Sonar - Quality Gate"
    stage("$env.TAREA"){
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
    env.TAREA = "Paso 5: Subir Nexus"
    stage("$env.TAREA"){
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

def stageCreateRelease(){
    stage('Checkout') {
        checkout([
            $class: 'GitSCM',
            branches: scm.branches,
            extensions: scm.extensions + [[$class: 'LocalBranch'], [$class: 'WipeWorkspace']],
            userRemoteConfigs: [[credentialsId: 'jenkins-git-user', url: 'https://github.com/pipegory/ejemplo_gradle.git']],
            doGenerateSubmoduleConfigurations: false
        ])
    }
    stage('Git Merge Develop'){
        withCredentials([
            gitUsernamePassword(credentialsId: 'jenkins-git-user', gitToolName: 'Default')
        ]) {
            sh '''
                git fetch -p
                git checkout develop
                git checkout -b release-v1-0-0
                git push origin release-v1-0-0
            '''
        }
    }
}

return this;
