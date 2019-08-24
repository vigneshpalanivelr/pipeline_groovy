/*
*	gitRepo
*	gitBranch
*	gitCreds
*	tfstateBucket
*	tfstateBucketPrefix

*	db_engine
*	db_family
*	db_engine_version
*	db_instance_class
*	db_identifier
*	db_name
*	db_username
*	db_password
*	db_allocated_storage
*	db_multi_az
*	db_R53_name

*	createInstance
*	createInstanceDNS
*	terraformApplyPlan
*/

node('master') {
	def terraformDirectory	= "modules/all_modules/rds_module"
	
	withFile(file: "git-askpass-${BUILD_TAG}",text:"#!/bin/bash\ncase \"\$1\" in\nUsername*) echo \"${STASH_USERNAME}\" ;;\nPassword*) \"${STASH_PASSWORD}\" ;;\nesac")
	sh "chmod a+x git-askpass-${BUILD_TAG}"
	
	stage('Approve before Start'){
		approval()
	}
	stage('Checkout') {
		checkout()
	}
}

def approval() {
	timeout(time: 15, unit: 'SECONDS') {
		input(
			id: 'Approval',
			message: 'Shall I Continue ?',
			parameters:	[[
				$class:	'BooleanParameterDefinition', 
				defaultValue: true, 
				description: 'default to tick', 
				name: 'Please confirm to proceed'
			]]
		)
	}
}

def checkout() {
	checkout([
		$class: 'GitSCM', 
		branches: [[name: gitBranch ]], 
		doGenerateSubmoduleConfigurations: false, 
		clearWorkspace: true,
		extensions: [
			[$class: 'CleanCheckout'], [
			$class: 'SubmoduleOption', 
			disableSubmodules: false, 
			parentCredentials: true, 
			recursiveSubmodules: true, 
			reference: '', trackingSubmodules: false]], 
		submoduleCfg: [], 
		userRemoteConfigs: [[credentialsId: gitCreds, url: gitRepo]]
	])
}
