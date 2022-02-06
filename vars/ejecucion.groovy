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
                        def ejecucionIC = load 'ic.groovy'
                        def ejecucionDC = load 'dc.groovy'
                        String[] str
                        str        = GIT_BRANCH.split('/')
                        rama = str[1]
                        if (rama.contains('feature') ) {
                            sh "echo 'rama feature IC'"
                            ejecucionIC.call()
                        }
                        else {
                            sh "echo 'rama 2' ${str[1]}  ' DC'"
                            ejecucionDC.call()
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
