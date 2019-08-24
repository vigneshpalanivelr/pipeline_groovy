node('master') {
	stage('Clone & Checkout') {
		checkout([$class: 'GitSCM', branches: [[name: gitBranch ]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: gitCreds, url: gitRepo]]])
	}
}
