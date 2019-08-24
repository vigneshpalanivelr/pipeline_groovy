node('master') {
	stage('Clone & Checkout') {
		checkout([
			$class: 'GitSCM', 
			branches: [[name: gitBranch ]], 
			doGenerateSubmoduleConfigurations: false, 
			extensions: [[$class: 'CleanCheckout'], [
				$class: 'SubmoduleOption', 
				disableSubmodules: false, 
				parentCredentials: true, 
				recursiveSubmodules: true, 
				reference: '', trackingSubmodules: false]], 
			submoduleCfg: [], 
			userRemoteConfigs: [[credentialsId: gitCreds, url: gitRepo], []]])
	}
}
