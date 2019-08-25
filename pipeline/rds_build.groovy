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
	def terraformDirectoryRDS	= "modules/all_modules/rds_module"
	global_tfvars   		= "../../../global_vars.tfvars"
	rds_tfvars      		= "../../../${db_engine}.tfvars"
	
	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASSWORD}\" ;;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"
	
	stage('Approve before Start'){
		approval()
	}
	stage('Checkout') {
		checkout()
		if (createInstance == 'true'){
			dir(terraformDirectoryRDS){
				stage('Remote State Init') {
					terraform_init()
				}
				if (terraformApplyPlan == 'plan') {
					stage('Terraform Plan'){
						set_env_variables()
						withEnv(["TF_VAR_db_password=${db_password}"]){
                	                		terraform_plan(global_tfvars,rds_tfvars)
						}
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Plan Approve & Apply'){
						set_env_variables()
						terraform_plan(global_tfvars,rds_tfvars)
						approval()
						terraform_apply()
	               	                }
				}
				if (terraformApplyPlan == 'plan-destroy') {
					stage('Plan Destroy'){
						set_env_variables()
                                                terraform_plan_destroy()
                                        }
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve & Destroy'){
                                                approval()
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
	withEnv(["TF_VAR_db_password=${db_password}"]){
		env.TF_VAR_db_engine            = "${db_engine}"
		env.TF_VAR_db_family            = "${db_family}"
		env.TF_VAR_db_engine_version    = "${db_engine_version}"
		env.TF_VAR_db_instance_class    = "${db_instance_class}"
		env.TF_VAR_db_identifier        = "${db_identifier}"
		env.TF_VAR_db_name              = "${db_name}"
		env.TF_VAR_db_username          = "${db_username}"
		env.TF_VAR_db_allocated_storage = "${db_allocated_storage}"
		env.TF_VAR_db_multi_az          = "${db_multi_az}"
		env.TF_VAR_db_R53_name          = "${db_R53_name}"
	}
}

def terraform_init() {
	withEnv(["GIT_ASKPASS=${WORKSPACE}/askp-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'STASH_USERNAME', passwordVariable: 'STASH_PASSWORD')]) {
			sh "terraform init -no-color -input=false -upgrade=true -backend=true -force-copy -backend-config='bucket=${tfstateBucket}' -backend-config='workspace_key_prefix=${tfstateBucketPrefix}' -backend-config='key=rds_module.tfstate'"
		}
	}
}

def terraform_plan(global_tfvars,rds_tfvars) {
	sh "terraform plan -no-color -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${rds_tfvars}"
}

def terraform_apply() {
	sh "terraform apply -no-color -input=false tfplan"
}

def terraform_plan_destroy() {
        sh "terraform plan -destroy -no-color -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${rds_tfvars}"
}

def terraform_destroy() {
        sh "terraform apply -no-color -input=false tfdestroy"
}
