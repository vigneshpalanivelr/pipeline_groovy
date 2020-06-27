/*
*	gitRepo
*	gitBranch
*	gitCreds
*	awsAccount
*	tfstateBucket
*	tfstateBucketPrefix

*	r53_zone_name
*	vpc_name

*	includeR53Zone
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectory	= "modules/all_modules/${tfstateBucketPrefix}"
	
	global_tfvars   	= "../../../variables/global_vars.tfvars"
	r53_tfvars			= "../../../variables/r53_zone_vars.tfvars"
	
	date 				= new Date()
	println date

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"

	stage('Checkout') {
		checkout()
		//Create Route53 Zone
		if (includeR53Zone == 'true') {
			dir(terraformDirectory) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('R53 Zone Plan') {
						terraform_init(tfstateBucketPrefix, r53_zone_name, 'r53-zone')
						set_env_variables()
						terraform_plan(global_tfvars,r53_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('R53 Zone Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//################################################################################################################
		//Destroy Pipeline starts
		//################################################################################################################
		//Destroy Route53 Zone
		if (includeR53Zone == 'true') {
			dir(terraformDirectory) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('R53 Zone Destroy Plan') {
						terraform_init(tfstateBucketPrefix, r53_zone_name, 'r53-zone')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,r53_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('R53 Zone Destroy') {
						approval()
						terraform_destroy()
					}
				}
			}
		}
	}
}

def approval() {
	timeout(time: 5, unit: 'DAYS') {
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
	env.TF_VAR_r53_zone_name    = "${r53_zone_name}"
	env.TF_VAR_vpc_name			= "${vpc_name}"
}

def terraform_init(module, tfstatename, stack) {
	withEnv(["GIT_ASKPASS=${WORKSPACE}/askp-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'STASH_USERNAME', passwordVariable: 'STASH_PASSWORD')]) {
			wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
				sh "terraform init -input=false -upgrade=true -backend=true -force-copy -backend-config='bucket=${tfstateBucket}' -backend-config='key=${module}/${tfstatename}-${stack}.tfstate'"
			}
		}
	}
}

def terraform_plan(global_tfvars,r53_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform plan -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${r53_tfvars}"
	}
}

def terraform_apply() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform apply -input=false tfplan"
	}
}

def terraform_plan_destroy(global_tfvars,r53_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform plan -destroy -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${r53_tfvars}"
	}
}

def terraform_destroy() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform apply -input=false tfdestroy"
	}
}
