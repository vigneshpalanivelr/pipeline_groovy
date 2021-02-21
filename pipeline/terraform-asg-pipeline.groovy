/*
*	gitRepo
*	gitBranch
*	gitCreds
*	awsAccount
*	tfstateBucket
*	tfstateBucketPrefixSG
*	tfstateBucketPrefixSGR
*	tfstateBucketPrefixASGLTLT
*	tfstateBucketPrefixASGLT

*	vpc_name
*	asg_name
*	asg_lt_name
*	asg_lt_sg_name
*	asg_lt_instance_type
*	ami_regex
*	ami_owner_id
*	root_user
*	root_passwd
*	asg_min_size
*	asg_max_size
*	asg_desired_capacity
*	asg_health_check_type

*	includeSG
*	includeSGRule
*	includeASGLT
*	includeASG
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectorySG			= "modules/all_modules/${tfstateBucketPrefixSG}"
	terraformDirectorySGRule		= "modules/all_modules/${tfstateBucketPrefixSGR}/${asg_lt_sg_name}-sg"
	terraformDirectoryASGLT			= "modules/all_modules/${tfstateBucketPrefixASGLT}"
	terraformDirectoryASG			= "modules/all_modules/${tfstateBucketPrefixASG}"
    
	global_tfvars					= "../../../../variables/global_vars.tfvars"
	sg_tfvars						= "../../../variables/sg_vars.tfvars"
	sg_rule_tfvars					= "../../../../variables/sg_vars.tfvars"
	asg_tfvars						= "../../../../variables/asg_vars.tfvars"
	
	date                    		= new Date()
	println date

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"

	stage('Checkout') {
		checkout()
		if (includeSG == 'true') {
			dir(terraformDirectorySG) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('SG Init Plan') {
						terraform_init(tfstateBucketPrefixSG, asg_lt_sg_name, 'sg')
						set_env_variables()
						terraform_plan(global_tfvars,sg_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('SG Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		if (includeSGRule == 'true') {
			dir(terraformDirectorySGRule) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('SG-R Init Apply') {
						terraform_init(tfstateBucketPrefixSGR, asg_lt_sg_name, 'sg-rule')
						set_env_variables()
						terraform_plan(global_tfvars,sg_rule_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('SG-R Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		if (includeASGLT == 'true') {
			dir(terraformDirectoryASGLT) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('ASG Init Plan') {
						terraform_init(tfstateBucketPrefixASGLT, asg_lt_name, 'asg-lt')
						set_env_variables()
						terraform_plan(global_tfvars,asg_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('ASG Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		if (includeASG == 'true') {
            dir(terraformDirectoryASG) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('ASG Init Plan') {
						terraform_init(tfstateBucketPrefixASG, asg_lt_name, 'asg')
						set_env_variables()
						terraform_plan(global_tfvars,asg_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('ASG Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		// ###################################################
		// Destroy Starts 
		// ###################################################
		if (includeASG == 'true') {
            dir(terraformDirectoryASG) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('ASG-D Init Apply') {
						terraform_init(tfstateBucketPrefixASG, asg_lt_name, 'asg')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,asg_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('ASG-D Destroy') {
						approval()
						terraform_destroy()
					}
				}
            }
        }
		if (includeASGLT == 'true') {
			dir(terraformDirectoryASGLT) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('ASG-LT-D Init Plan') {
						terraform_init(tfstateBucketPrefixASGLT, asg_lt_name, 'asg-lt')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,asg_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('ASG-LT-D Destroy') {
						approval()
						terraform_destroy()
					}
				}
			}
		}
		if (includeSGRule == 'true') {
			dir(terraformDirectorySGRule) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('SG-R Init Plan') {
						terraform_init(tfstateBucketPrefixSGR, asg_lt_sg_name, 'sg-rule')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,sg_rule_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('SG-R Destroy') {
						approval()
						terraform_destroy()
					}
				}
			}
		}
		if (includeSG == 'true') {
			dir(terraformDirectorySG) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('SG Init Plan') {
						terraform_init(tfstateBucketPrefixSG, asg_lt_sg_name, 'sg')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,sg_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('SG Destroy') {
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
	env.TF_VAR_aws_account_num			= "${awsAccount}"
	env.TF_VAR_aws_vpc_name				= "${vpc_name}"
	env.TF_VAR_sg_group_name			= "${asg_lt_sg_name}"
	env.TF_VAR_resource_name			= "${asg_lt_sg_name}"
	env.TF_VAR_asg_lt_sg_name			= "${asg_lt_sg_name}"
	env.TF_VAR_asg_lt_name				= "${asg_lt_name}"
	env.TF_VAR_asg_lt_instance_type		= "${asg_lt_instance_type}"
	env.TF_VAR_ami_regex				= "${ami_regex}"
	env.TF_VAR_ami_owner_id				= "${ami_owner_id}"
	env.TF_VAR_root_user				= "${root_user}"
	env.TF_VAR_root_passwd				= "${root_passwd}"
	env.TF_asg_name						= "${asg_name}"
	env.TF_VAR_asg_min_size				= "${asg_min_size}"
	env.TF_VAR_asg_max_size				= "${asg_max_size}"
	env.TF_VAR_asg_desired_capacity     = "${asg_desired_capacity}"
	env.TF_VAR_asg_health_check_type    = "${asg_health_check_type}"
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

def terraform_plan(global_tfvars,any_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform plan -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${any_tfvars}"
	}
}

def terraform_apply() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform apply -input=false tfplan"
	}
}

def terraform_plan_destroy(global_tfvars,any_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
    	sh "terraform plan -destroy -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${any_tfvars}"
    }
}

def terraform_destroy() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
    	sh "terraform apply -input=false tfdestroy"
    }
}
