/*
*	gitRepo
*	gitBranch
*	gitCreds
*	awsAccount
*	tfstateBucket
*	tfStateBucketPrefixCW

*	resource_name

*	includeCW
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectoryCW		= "modules/all_modules/${tfstateBucketPrefixEC2CW}/cw_ec2"
	
	global_tfvars				= "../../../variables/global_vars.tfvars"
	cw_tfvars					= "../../../../variables/cw_vars.tfvars"
	
	date						= new Date()
	println date

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"

	stage('Approval') {
		approval()
	}

	stage('Checkout') {
		checkout()
		if (includeCW == 'true') {
			dir(terraformDirectoryCW) {
				stage('CW Init') {
					terraform_init(tfStateBucketPrefixCW + '/cw_ec2','cw-alarm')
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('CW Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,cw_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('CW Apply') {
						approval()
						terraform_apply()
					}
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,cw_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('CW Destroy') {
						approval()
						terraform_destroy()
					}
				}
			}
		}
	}
}

def approval() {
	timeout(time: 1, unit: 'MINUTES') {
		input(
			id: 'Approval', message: 'Shall i continue ?', parameters: [[
				$class:	'BooleanParameterDefinition', defaultValue: true, description: 'default to tick', name: 'Please confirm to proceed']]
		)
	}
}

def checkout() {
	checkout([
		$class                            : 'GitSCM', 
		branches                          : [[name		: gitBranch ]], 
		doGenerateSubmoduleConfigurations : false, 
		clearWorkspace                    : true,
		extensions                        : [[$class	: 'CleanCheckout' ],[
			$class					: 'SubmoduleOption', 
			disableSubmodules		: false,
			parentCredentials		: true,
			recursiveSubmodules		: true,
			reference				: '',
			trackingSubmodules		: false]],
		submoduleCfg				: [],
		userRemoteConfigs			: [[credentialsId: gitCreds, url: gitRepo]]])
}

def set_env_variables() {
	env.TF_VAR_aws_account_num	= "${awsAccount}"
	env.TF_VAR_resource_name	= "${resource_name}"
}

def terraform_init(module,stack) {
	withEnv(["GIT_ASKPASS=${WORKSPACE}/askp-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'STASH_USERNAME', passwordVariable: 'STASH_PASSWORD')]) {
			sh "terraform init -no-color -input=false -upgrade=true -backend=true -force-copy -backend-config='bucket=${tfstateBucket}' -backend-config='key=${module}/${resource_name}-${stack}.tfstate'"
		}
	}
}

def terraform_plan(global_tfvars,cw_tfvars) {
	sh "terraform plan -no-color -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${cw_tfvars}"
}

def terraform_apply() {
	sh "terraform apply -no-color -input=false tfplan"
}

def terraform_plan_destroy(global_tfvars,cw_tfvars) {
    sh "terraform plan -destroy -no-color -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${cw_tfvars}"
}

def terraform_destroy() {
    sh "terraform apply -no-color -input=false tfdestroy"
}
