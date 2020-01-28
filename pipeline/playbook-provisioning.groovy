/*
*	gitRepo
*	gitBranch
*	gitCreds
*	scriptType
*	playbook
*	playbookTags
*/

node ('master') {
	scriptsDirectory    = "${scriptType}"
	playbook            = "${playbook}"
	playbookTags        = "${playbookTags}"
    
	date				= new Date()
	println date
	
	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"
	
	stage('Checkout') {
		checkout()
	}
	
	dir(scriptsDirectory) {
		stage('Playbook Execution') {
			ansiColor('xterm') {
				ansiblePlaybook(
					playbook        : "${playbook}',
					tags            : "${playbookTags}"
					colorized       : true
				)
			}
		}
	}
}
def checkout() {
	checkout([
		$class								: 'GitSCM', 
		branches							: [[name	: gitBranch ]], 
		doGenerateSubmoduleConfigurations	: false, 
		clearWorkspace						: true,
		extensions							: [[$class	: 'CleanCheckout' ],[
			$class					: 'SubmoduleOption', 
			disableSubmodules		: false,
			parentCredentials		: true,
			recursiveSubmodules		: true,
			reference				: '',
			trackingSubmodules		: false]],
		submoduleCfg				: [],
		userRemoteConfigs			: [[credentialsId: gitCreds, url: gitRepo]]])
}
