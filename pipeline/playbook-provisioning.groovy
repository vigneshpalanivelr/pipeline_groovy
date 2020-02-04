/*
*	gitRepo
*	gitBranch
*	gitCreds

*	scriptType
*	playbook
*	inventory
*	playbookTags
*	extraVars
*/

node ('master') {
	scriptsDirectory    = "${scriptType}"
	playbook            = "${playbook}"
	inventory			= "${inventory}"
	playbookTags        = "${playbookTags}"
	extraVars			= "${extraVars}"
    
	date				= new Date()
	println date
	
	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"
	
	stage('Checkout') {
		checkout()
	}
	
	dir(scriptsDirectory) {
		if (extraVars) {
			stage('Playbook Execution with extravars') {
				sh "/usr/bin/ansible-playbook ${playbook} --inventory ${inventory} --tags=${playbookTags} --extra-vars '${extraVars}'"
			}
		}
		else {
			stage('Playbook Execution without extravars') {
				ansiColor('xterm') {
					ansiblePlaybook(
						playbook        : "${playbook}",
						inventory		: "${inventory}"
						tags            : "${playbookTags}",
						colorized       : true
					)
				}
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
