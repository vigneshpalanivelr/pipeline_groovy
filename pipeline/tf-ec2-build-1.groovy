/*
*	gitRepo
*	gitBranch
*	gitCreds
*	awsAccount
*	tfstateBucket
*	tfstateBucketPrefix

*	instance_name
*	eni_subnet
*	ebs_name
*	ebs_availability_zone
*	ebs_size
*	ebs_type

*	includeENI
*	includeEBS
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectoryENI	= "modules/all_modules/eni_module"
    terraformDirectoryEBS	= "modules/all_modules/ebs_module"
    
	global_tfvars           = "../../../variables/global_vars.tfvars"
    ec2_eni_tfvars          = "../../../variables/ec2_eni_vars.tfvars"
	ebs_tfvars              = "../../../variables/ebs_volume_vars.tfvars"
	
    date                    = new Date()
	println date

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"

	stage('Approval') {
		approval()
	}

	stage('Checkout') {
		checkout()
		if (includeENI == 'true') {
			dir(terraformDirectoryENI) {
				stage('Remote State Init') {
					terraform_init('eni_module','eni')
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform ENI Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,ec2_eni_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve ENI Plan') {
						approval()
					}
					stage('Terraform ENI Apply') {
						terraform_apply()
					}
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan ENI Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ec2_eni_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve ENI Destroy') {
						approval()
					}
					stage('ENI Destroy') {
						terraform_destroy()
					}
				}
			}
		}
        if (includeEBS == 'true') {
            dir(terraformDirectoryEBS) {
                stage('Remote State Init') {
					terraform_init('ebs_module','ebs-volumes')
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform EBS Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve EBS Plan') {
						approval()
					}
					stage('Terraform EBS Apply') {
						terraform_apply()
					}
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan EBS Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve EBS Destroy') {
						approval()
					}
					stage('EBS Destroy') {
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
	env.TF_VAR_ebs_name					= "${ebs_name}"
	env.TF_VAR_aws_account_num			= "${awsAccount}"
	env.TF_VAR_ebs_availability_zone	= "${ebs_availability_zone}"
	env.TF_VAR_ebs_size					= "${ebs_size}"
	env.TF_VAR_ebs_type					= "${ebs_type}"
	env.TF_VAR_ebs_iops					= "${ebs_iops}"
	env.TF_VAR_ebs_encrypted			= "${ebs_encrypted}"
}

def terraform_init(module,stack) {
	withEnv(["GIT_ASKPASS=${WORKSPACE}/askp-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'STASH_USERNAME', passwordVariable: 'STASH_PASSWORD')]) {
			sh "terraform init -no-color -input=false -upgrade=true -backend=true -force-copy -backend-config='bucket=${tfstateBucket}' -backend-config='key=${module}/${instance_name}-${stack}.tfstate'"
		}
	}
}

def terraform_plan(global_tfvars,any_tfvars) {
	sh "terraform plan -no-color -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${any_tfvars}"
}

def terraform_apply() {
	sh "terraform apply -no-color -input=false tfplan"
}

def terraform_plan_destroy(global_tfvars,any_tfvars) {
    sh "terraform plan -destroy -no-color -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${any_tfvars}"
}

def terraform_destroy() {
    sh "terraform apply -no-color -input=false tfdestroy"
}
