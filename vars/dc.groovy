def call(){
    allStages()
}

def allStages(){
    stageDownloadNexus()
    stageRunJar()
    stageRunTest()
    // gitMergeMaster()
    // gitMergeDevelop()
    // gitTagMaster()
}

def stageDownloadNexus(){
    env.TAREA = "Paso 6: Descargar Nexus"
    stage("$env.TAREA"){
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/laboratorioM3/LaboratorioM3-ID/0.0.1/LaboratorioM3-ID-0.0.1.jar" -O'
    }
}

def stageRunJar(){
    env.TAREA = "Paso 7: Levantar Artefacto Jar"
    stage("$env.TAREA"){
        sh 'nohup java -jar LaboratorioM3-ID-0.0.1.jar --server.port=7788 & >/dev/null'
    }
}

def stageRunTest(){
    env.TAREA="Paso 8: Ejecución Test URLs"
    stage("$env.TAREA"){
        sh "nohup bash mvnw spring-boot:run &"
        sh "sleep 20"
        sh "ls"
        validaStatus('http://localhost:7788/rest/mscovid/test?msg=testing')
        validaStatus('http://localhost:7788/rest/mscovid/estadoMundial')
        validaStatus('http://localhost:7788/rest/mscovid/estadoPais?pais=chile')
    }
}

def gitMergeMaster(){
    echo "GIT BRANCH ${GIT_BRANCH}"
    stage('Checkout') {
        checkout([
            $class: 'GitSCM',
            branches: scm.branches,
            extensions: scm.extensions + [[$class: 'LocalBranch'], [$class: 'WipeWorkspace']],
            userRemoteConfigs: [[credentialsId: 'jenkins-git-user', url: 'https://github.com/DiplomadoDevOps2021/ms-iclab.git']],
            doGenerateSubmoduleConfigurations: false
        ])
    }
    stage('Git Merge Master'){
        withCredentials([
            gitUsernamePassword(credentialsId: 'jenkins-git-user', gitToolName: 'Default')
        ]) {
            sh '''
                git config --global user.email "ordenes.ignacio@gmail.com"
                git config --global user.name "Jenkins Pipeline"
                git fetch -p
                git checkout release-v1-0-0; git pull origin release-v1-0-0
                git checkout main
                git pull origin main
                git merge release-v1-0-0;
                git push origin main
            '''
        }
    }
}

def gitTagMaster(){
    echo "GIT BRANCH ${GIT_BRANCH}"
    stage('Checkout') {
        checkout([
            $class: 'GitSCM',
            branches: scm.branches,
            extensions: scm.extensions + [[$class: 'LocalBranch'], [$class: 'WipeWorkspace']],
            userRemoteConfigs: [[credentialsId: 'jenkins-git-user', url: 'https://github.com/DiplomadoDevOps2021/ms-iclab.git']],
            doGenerateSubmoduleConfigurations: false
        ])
    }
    stage('Git Tag Master'){
        withCredentials([
            gitUsernamePassword(credentialsId: 'jenkins-git-user', gitToolName: 'Default')
        ]) {
            sh '''
                git config --global user.email "ordenes.ignacio@gmail.com"
                git config --global user.name "Jenkins Pipeline"
                git fetch -p
                git checkout main
                git tag -a release-v1-0-0 -m "release-v1-0-0";
                git push origin --tags
            '''
        }
    }
}

def gitMergeDevelop(){
    stage('Checkout') {
        checkout([
            $class: 'GitSCM',
            branches: scm.branches,
            extensions: scm.extensions + [[$class: 'LocalBranch'], [$class: 'WipeWorkspace']],
            userRemoteConfigs: [[credentialsId: 'jenkins-git-user', url: 'https://github.com/DiplomadoDevOps2021/ms-iclab.git']],
            doGenerateSubmoduleConfigurations: false
        ])
    }
    stage('Git Merge Develop'){
        withCredentials([
            gitUsernamePassword(credentialsId: 'jenkins-git-user', gitToolName: 'Default')
        ]) {
            sh '''
                git fetch -p
                git checkout release-v1-0-0; git pull origin release-v1-0-0
                git checkout develop; git pull origin develop
                git merge release-v1-0-0;
                git push origin develop
            '''
        }
    }
}



def validaStatus(url){
    String status = sh(script: "curl -sLI -w '%{http_code}' $url -o /dev/null", returnStdout: true)
    echo "status: ${status}"
    if (status != "200")
    {
        error "El endpoint responde código: ${status}"
    }
}

return this;
