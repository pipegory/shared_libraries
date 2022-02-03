def call(){
  pipeline {
      agent any
      environment {
          NEXUS_USER         = credentials('NEXUS-USER')
          NEXUS_PASSWORD     = credentials('NEXUS-PASS')
      }
    //   parameters {
    //         choice choices: ['maven'], description: 'Prueba', name: 'compileTool'
    //   }
      stages {
          stage("Pipeline"){
              steps {
                  script{
                      sh "env"
                      env.TAREA = ""
                        maven.call();
                  }
              }
              post{
                success{
                    slackSend color: 'good', message: "[Ignacio] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                }
                failure{
                    slackSend color: 'danger', message: "[Ignacio] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida en stage [${env.TAREA}]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                }
            }
          }
      }
  }
}
return this;
