/*
*	gitRepo
*	gitBranch
*	gitCreds
*	awsAccount
*	tfstateBucket
*	tfstateBucketPrefixSG
*	tfstateBucketPrefixSGR

*	vpc_name
*	sg_group_name
*	resource_name

*	includeSG
*	includeSGRule
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectorySG		= "modules/all_modules/${tfstateBucketPrefixSG}"
	terraformDirectorySGRule	= "modules/all_modules/${tfstateBucketPrefixSGR}/${sg_group_name}-sg"
	
	global_tfvars				= "../../../variables/global_vars.tfvars"
	sg_tfvars					= "../../../variables/sg_vars.tfvars"
	rule_global_tfvars			= "../../../../variables/global_vars.tfvars"
	rule_sg_tfvars				= "../../../../variables/sg_vars.tfvars"
	
	date						= new Date()
	println date

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"

	stage('Checkout') {
		checkout()
		if (includeSG == 'true') {
			dir(terraformDirectorySG) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixSG, sg_group_name, "sg")
						set_env_variables()
						terraform_plan(global_tfvars,sg_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Plan') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		if (includeSGRule == 'true') {
			dir(terraformDirectorySGRule) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixSGR, sg_group_name, "sg-rule")
						set_env_variables()
						terraform_plan(rule_global_tfvars,rule_sg_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Plan') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		if (includeSGRule == 'true') {
			dir(terraformDirectorySGRule) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						terraform_init(tfstateBucketPrefixSGR, sg_group_name, "sg-rule")
						set_env_variables()
						terraform_plan_destroy(rule_global_tfvars,rule_sg_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve Destroy') {
						approval()
						terraform_destroy()
					}
				}
			}
		}
		if (includeSG == 'true') {
			dir(terraformDirectorySG) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						terraform_init(tfstateBucketPrefixSG, sg_group_name, "sg")
						set_env_variables()
						terraform_plan_destroy(global_tfvars,sg_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve Destroy') {
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
	env.TF_VAR_aws_vpc_name		= "${vpc_name}"
	env.TF_VAR_aws_account_num	= "${awsAccount}"
	env.TF_VAR_sg_group_name    = "${sg_group_name}"
	env.TF_VAR_resource_name	= "${resource_name}"
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

def terraform_plan(global_tfvars,sg_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform plan -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${sg_tfvars}"
	}
}

def terraform_apply() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform apply -input=false tfplan"
	}
}

def terraform_plan_destroy(global_tfvars,sg_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
    	sh "terraform plan -destroy -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${sg_tfvars}"
    }
}

def terraform_destroy() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
    	sh "terraform apply -input=false tfdestroy"
    }
}
