/*
*	gitRepo
*	gitBranch
*	gitCreds
*	awsAccount
*	tfstateBucket
*	tfstateBucketPrefixEBS
*	tfstateBucketPrefixEBSA

*	resource_name
*	ebs_volume_count
*	ebs_az

*	includeEBS
*	includeEBSAttach
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectoryEBS		= "modules/all_modules/${tfstateBucketPrefixEBS}"
	terraformDirectoryEBSAttach	= "modules/all_modules/${tfstateBucketPrefixEBSA}"
	
	global_tfvars				= "../../../variables/global_vars.tfvars"
	ebs_tfvars		    		= "../../../variables/ebs_volume_vars.tfvars"
	
	date						= new Date()
	println date

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"

	stage('Checkout') {
		checkout()
		if (includeEBS == 'true') {
			dir(terraformDirectoryEBS) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Remote State Init') {
						terraform_init(tfstateBucketPrefixEBS, resource_name, 'ebs-volumes')
					}
					stage('Terraform Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Plan') {
						approval()
					}
					stage('Terraform Apply') {
						terraform_apply()
					}
				}
			}
		}
		if (includeEBSAttach == 'true') {
			dir(terraformDirectoryEBSAttach) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Remote State Init') {
						terraform_init(tfstateBucketPrefixEBSA, resource_name, 'ebs-attach')
					}
					stage('Terraform Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Plan') {
						approval()
					}
					stage('Terraform Apply') {
						terraform_apply()
					}
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Remote State Init') {
						terraform_init(tfstateBucketPrefixEBSA, resource_name, 'ebs-attach')
					}
					stage('Plan Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve Destroy') {
						approval()
					}
					stage('Destroy') {
						terraform_destroy()
					}
				}
			}
		}
		if (includeEBS == 'true') {
			dir(terraformDirectoryEBS) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Remote State Init') {
						terraform_init(tfstateBucketPrefixEBS, resource_name, 'ebs-volumes')
					}
					stage('Plan Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve Destroy') {
						approval()
					}
					stage('Destroy') {
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
	env.TF_VAR_aws_account_num			= "${awsAccount}"
	env.TF_VAR_ebs_volume_count			= "${ebs_volume_count}"
	env.TF_VAR_ebs_az					= "${ebs_az}"
	env.TF_VAR_resource_name			= "${resource_name}"
	env.TF_VAR_ebs_key_state_prefix		= "${tfstateBucketPrefixEBS}"
}

def terraform_init(module, tfstatename, stack) {
	withEnv(["GIT_ASKPASS=${WORKSPACE}/askp-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'STASH_USERNAME', passwordVariable: 'STASH_PASSWORD')]) {
			sh "terraform init -no-color -input=false -upgrade=true -backend=true -force-copy -backend-config='bucket=${tfstateBucket}' -backend-config='key=${module}/${tfstatename}-${stack}.tfstate'"
		}
	}
}

def terraform_plan(global_tfvars,ec2_eni_tfvars) {
	sh "terraform plan -no-color -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${ebs_tfvars}"
}

def terraform_apply() {
	sh "terraform apply -no-color -input=false tfplan"
}

def terraform_plan_destroy(global_tfvars,ec2_eni_tfvars) {
    sh "terraform plan -destroy -no-color -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${ebs_tfvars}"
}

def terraform_destroy() {
    sh "terraform apply -no-color -input=false tfdestroy"
}
