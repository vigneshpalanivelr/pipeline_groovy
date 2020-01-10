node('master') {
	stage('Clone Repository') {
		checkout([
			$class					: 'GitSCM',
			branches				: [[name: jobDSL_branch]],
			doGenerateSubmoduleConfigurations	: false,
			extensions				: [],
			submoduleCfg				: [],
			userRemoteConfigs			: [[credentialsId: jobDSL_creds, url: jobDSL_repo]]
		])
	}
	stage('Process DSL') {
		jobDsl targets : jobDSL_path
	}
}
