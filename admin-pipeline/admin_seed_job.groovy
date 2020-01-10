node('master') {
        stage('Clone & Checkout') {
                checkout([
                        $class: 'GitSCM',
                        branches: [[name: jobDSL_branch ]],
                        doGenerateSubmoduleConfigurations: false,
                        clearWorkspace: true,
                        extensions: [[$class: 'CleanCheckout'], [
                                $class: 'SubmoduleOption',
                                disableSubmodules: false,
                                parentCredentials: true,
                                recursiveSubmodules: true,
                                reference: '', trackingSubmodules: false]],
                        submoduleCfg: [],
                        userRemoteConfigs: [[credentialsId: jobDSL_creds, url: jobDSL_repo]]])
        }
	stage('Process DSL') {
		jobDsl targets : jobDSL_scripts
	}
}
