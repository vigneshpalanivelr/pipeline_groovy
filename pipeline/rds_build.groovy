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
	
	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASSWORD}\" ;;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"
	
	stage('Approve before Start'){
		approval()
	}
	stage('Checkout') {
		checkout()
		if (createInstance == true){
			stage('RDS Instance remote state') {
				terraform_init()
			}
		}
	}
}

def approval() {
	timeout(time: 15, unit: 'SECONDS') {
		input(
			id: 'Approval', message: 'Shall I Continue ?', parameters: [[
				$class:	'BooleanParameterDefinition', defaultValue: true, description: 'default to tick', name: 'Please confirm to proceed'
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

def terraform_init() {
	withEnv(["GIT_ASKPASS=${WORKSPACE}/askp-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'STASH_USERNAME', passwordVariable: 'STASH_PASSWORD')]) {
			sh "terraform init -backend=true -backend-config='bucket=${terraformTFstateBucket}' -backend-config='workspace_key_prefix=${terraformTFstateBucketPrefix}' -backend-config='key=rds_module.tfstate'"
		}
	}
}
