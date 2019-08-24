node('master') {
	stage('Clone & Checkout') {
		checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: gitCreds, url: gitRepo]]])
	}
}
