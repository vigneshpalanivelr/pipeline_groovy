/*
*	gitRepo
*	gitBranch
*	gitCreds
*	tfstateBucket
*	tfstateBucketPrefix

*	db_family
*	db_engine
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

	local variable	: Use def keyword
	global variable	: No def keyword
	
	sqlserver-ex	: Engine sqlserver-ex does not support encryption at rest
	sqlserver-web 	: DBName must be null for engine
*/

node('master') {

	terraformDirectoryRDS	= "modules/all_modules/${tfstateBucketPrefix}"
	global_tfvars   	= "../../../variables/global_vars.tfvars"
	rds_tfvars      	= "../../../variables/rds.tfvars"
	db_rds 			= (db_engine		=~ /[a-zA-Z]+/)[0]
	db_engine_major_version = (db_engine_version	=~ /\d+.\d+/)[0]
	date 			= new Date()

	println date
	
	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASSWORD}\" ;;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"
	
	stage('Approval'){
		approval()
	}
	stage('Checkout') {
		checkout()
		if (createInstance == 'true'){
			dir(terraformDirectoryRDS){
				stage('Remote State Init') {
					terraform_init()
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan'){
						withEnv(["TF_VAR_db_password=${db_password}"]){
							set_env_variables()
                		               		terraform_plan(global_tfvars,rds_tfvars)
						}
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Plan'){
						approval()
					}
					stage('Terraform Apply'){
						terraform_apply()
					}
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy'){
                                                withEnv(["TF_VAR_db_password=${db_password}"]){
							set_env_variables()
	        	                        	terraform_plan_destroy(global_tfvars,rds_tfvars)
						}
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve Destroy'){
                                                approval()
                                	}
					stage(' Destroy'){
                                                terraform_destroy()
                                	}
				}
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

def set_env_variables() {
	env.TF_VAR_db_family            	= "${db_family}"
	env.TF_VAR_db_engine            	= "${db_engine}"
	env.TF_VAR_db_engine_version    	= "${db_engine_version}"
	env.TF_VAR_db_instance_class    	= "${db_instance_class}"
	env.TF_VAR_db_identifier        	= "${db_identifier}"
	env.TF_VAR_db_name              	= "${db_name}"
	env.TF_VAR_db_username          	= "${db_username}"
	env.TF_VAR_db_allocated_storage 	= "${db_allocated_storage}"
	env.TF_VAR_db_multi_az          	= "${db_multi_az}"
	env.TF_VAR_db_R53_name          	= "${db_R53_name}"
	env.TF_VAR_db_rds			= "${db_rds}"
	env.TF_VAR_db_engine_major_version	= "${db_engine_major_version}"
}

def terraform_init() {
	withEnv(["GIT_ASKPASS=${WORKSPACE}/askp-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'STASH_USERNAME', passwordVariable: 'STASH_PASSWORD')]) {
			sh "terraform init -no-color -input=false -upgrade=true -backend=true -force-copy -backend-config='bucket=${tfstateBucket}' -backend-config='key=${tfstateBucketPrefix}/${db_identifier}-${db_rds}-rds.tfstate'"
		}
	}
}

def terraform_plan(global_tfvars,rds_tfvars) {
	sh "terraform plan -no-color -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${rds_tfvars}"
}

def terraform_apply() {
	sh "terraform apply -no-color -input=false tfplan"
}

def terraform_plan_destroy(global_tfvars,rds_tfvars) {
        sh "terraform plan -destroy -no-color -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${rds_tfvars}"
}

def terraform_destroy() {
        sh "terraform apply -no-color -input=false tfdestroy"
}
