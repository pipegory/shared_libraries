def call(){
    allStages()
}

def allStages(){
    stageCleanBuild()
    stageCleanTest()
    stagePackage()
    stageSonar()
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
    stage("Paso 4: Análisis SonarQube"){
        //steps {
            withSonarQubeEnv('sonarqube') {
            sh "echo 'Calling sonar Service in another docker container!'"
            // Run Maven on a Unix agent to execute Sonar.
            def projectKey="${env.GIT_BRANCH}_${env.BUILD_NUMBER}"
            sh "echo ${projectKey}"
            def projectName="'RAMA:['${env.GIT_BRANCH}']_EJECUCIÓN_NR:['${env.BUILD_NUMBER}']'"
            sh "echo ${projectName}"
            sh "mvn clean verify sonar:sonar -Dsonar.projectKey='${projectKey}' -Dsonar.projectName='${projectName}'"
            }
        //}
        // post{
        //     //record the test results and archive the jar file.
        //     success {
        //         //archiveArtifacts artifacts:'build/*.jar'
        //         nexusPublisher nexusInstanceId: 'nexus',
        //         nexusRepositoryId: 'devops-usach-nexus',
        //         packages: [
        //             [$class: 'MavenPackage',
        //             mavenAssetList: [
        //                 [classifier: '',
        //                 extension: '.jar',
        //                 filePath: 'build/DevOpsUsach2020-0.0.1.jar']
        //                 ],
        //                 mavenCoordinate: [
        //                     artifactId: 'DevOpsUsach2020',
        //                     groupId: 'cl.devopsusach2020',
        //                     packaging: 'jar',
        //                     version: '0.0.1-feature-nexus']
        //             ]
        //         ]
        //     }
        // }
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

def stageDownloadNexus(){
    stage("Paso 6: Descargar Nexus"){

        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/laboratorioM3/LaboratorioM3-ID/0.0.1/LaboratorioM3-ID-0.0.1.jar" -O'
    }
}

def stageRunJar(){
    stage("Paso 7: Levantar Artefacto Jar"){
        sh 'nohup java -jar build/LaboratorioM3-ID-0.0.1.jar & >/dev/null'
    }
}

def stageRunSpringCurl(){
    env.TAREA="Paso 8: Curl Springboot Gradle sleep 20"
    stage("$env.TAREA"){
        sh "nohup bash mvnw spring-boot:run &"
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

return this;