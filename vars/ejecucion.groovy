def call(){
  pipeline {
      agent any
      environment {
          NEXUS_USER         = credentials('NEXUS-USER')
          NEXUS_PASSWORD     = credentials('NEXUS-PASS')
          GIT_REPO_NAME      = GIT_URL.replaceFirst(/^.*\/([^\/]+?).git$/, '$1')
      }
      stages {
          stage("Pipeline"){
              steps {
                  script{
                    rama = GIT_BRANCH
                    if (rama.contains('feature') || rama.contains('develop')) {
                        sh "echo 'rama feature IC'"
                        ic.call()
                    }
                    else {
                        if (rama.contains('release') ) {
                            sh "echo 'rama 2' ${str[1]}  ' DC'"
                            dc.call()
                        }
                        else{
                            slackSend color: 'danger', message: "[${JOB_NAME}] [${BUILD_TAG}] Rama no permitida: ${GIT_BRANCH}", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                            error "Rama ${GIT_BRANCH} no permitida/soportada"
                        }
                    }
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
