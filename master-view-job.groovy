pipeline {
    agent any
    options {
        buildDiscarder logRotator(
                artifactDaysToKeepStr   : '',
                artifactNumToKeepStr    : '',
                daysToKeepStr           : '',
                numToKeepStr            : ''
        )
    }
    parameters {
        choice(name: 'jobDSL_repo'	    , choices: 		['https://github.com/vigneshpalanivelr/pipeline_groovy.git']	, description: 'Job DSL Repo')
        string(name: 'jobDSL_branch'	, defaultValue: 'master'							                        	, description: 'Job DSL Branch')
        choice(name: 'jobDSL_creds' 	, choices: 		['gitCreds']							                        , description: 'Job DSL Cred')
        choice(name: 'jobDSL_path'	    , choices: 		['admin-job/admin_view.groovy']					            	, description: 'Location of Job DSL View Groovy Script')
    }
	stages {
	    stage('Clean Workspace') {
			steps {
				script {
                    cleanWs()
				}
			}
        }
		stage('Clone Repo') {
			steps {
				script {
					try {
						checkout([
							$class					            : 'GitSCM',
							branches				            : [[name: jobDSL_branch]],
							doGenerateSubmoduleConfigurations	: false,
							extensions                          : [],
							submoduleCfg				        : [],
							userRemoteConfigs			        : [[credentialsId: jobDSL_creds, url: jobDSL_repo]]
						])
					}
					catch(Exception Error) {
						echo Error
						exit(100)
					}
				}
			}
		}
		stage('Execute View Groovy') {
			steps {
				script {
                    jobDsl removedJobAction: 'DELETE', removedViewAction: 'DELETE', targets : jobDSL_path
				}
			}
        }
	}
}
