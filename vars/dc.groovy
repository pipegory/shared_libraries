def call(){
    allStages()
}

def allStages(){
    stageDownloadNexus()
    stageRunJar()
    stageRunTest()
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

def stageRunTest(){
    env.TAREA="Paso 8: Curl Springboot Gradle sleep 20"
    stage("$env.TAREA"){
        sh "nohup bash mvnw spring-boot:run &"
        sh "sleep 20"
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

return this;