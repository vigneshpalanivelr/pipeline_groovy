/*
*	gitRepo
*	gitBranch
*	gitCreds
*	awsAccount
*	tfstateBucket
*	tfstateBucketPrefixSG
*	tfstateBucketPrefixSGR
*	tfstateBucketPrefixENI
*	tfstateBucketPrefixEBS
*	tfstateBucketPrefixEC2
*	tfstateBucketPrefixEBSA
*	tfstateBucketPrefixEC2CW

*	instance_name
*	ebs_volume_count
*	vpc_name
*	sg_group_name
*	instance_type
*	AZ
*	subnet

*	includeSG
*	includeSGRule
*	includeENI
*	includeEBS
*	includeEC2
*	includeEBSAttach
*	includeCW
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectorySG			= "modules/all_modules/${tfstateBucketPrefixSG}"
	terraformDirectorySGRule		= "modules/all_modules/${tfstateBucketPrefixSGR}/${instance_name}-sg"
	terraformDirectoryENI			= "modules/all_modules/${tfstateBucketPrefixENI}"
	terraformDirectoryEBS			= "modules/all_modules/${tfstateBucketPrefixEBS}"
	terraformDirectoryEC2			= "modules/all_modules/${tfstateBucketPrefixEC2}"
	terraformDirectoryEBSAttach		= "modules/all_modules/${tfstateBucketPrefixEBSA}"
	terraformDirectoryEC2CW			= "modules/all_modules/${tfstateBucketPrefixEC2CW}/cw_ec2"
    
	global_tfvars					= "../../../variables/global_vars.tfvars"
	global_2_tfvars					= "../../../../variables/global_vars.tfvars"
	sg_tfvars						= "../../../variables/sg_vars.tfvars"
	sg_rule_tfvars					= "../../../../variables/sg_vars.tfvars"
	ec2_eni_tfvars					= "../../../variables/ec2_eni_vars.tfvars"
	ebs_tfvars						= "../../../variables/ebs_volume_vars.tfvars"
	ec2_tfvars						= "../../../variables/ec2_instance_vars.tfvars"
	cw_tfvars						= "../../../../variables/cw_vars.tfvars"
	
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
						terraform_init(tfstateBucketPrefixSG, sg_group_name, 'sg')
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
						terraform_init(tfstateBucketPrefixSGR, sg_group_name, 'sg-rule')
						set_env_variables()
						terraform_plan(global_2_tfvars,sg_rule_tfvars)
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
		if (includeENI == 'true') {
			dir(terraformDirectoryENI) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('ENI Init Plan') {
						terraform_init(tfstateBucketPrefixENI, instance_name, 'eni')
						set_env_variables()
						terraform_plan(global_tfvars,ec2_eni_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('ENI Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		if (includeEBS == 'true') {
            dir(terraformDirectoryEBS) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('EBS Init Plan') {
						terraform_init(tfstateBucketPrefixEBS, instance_name, 'ebs-volumes')
						set_env_variables()
						terraform_plan(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('EBS Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		if (includeEC2 == 'true') {
            dir(terraformDirectoryEC2) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('EC2 Init Plan') {
						terraform_init(tfstateBucketPrefixEC2, instance_name, 'ec2-instance')
						set_env_variables()
						terraform_plan(global_tfvars,ec2_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('EC2 Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		if (includeEBSAttach == 'true') {
            dir(terraformDirectoryEBSAttach) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('EBS-A Init Plan') {
						terraform_init(tfstateBucketPrefixEBSA, instance_name, 'ebs-attach')
						set_env_variables()
						terraform_plan(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('EBS-A Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		if (includeCW == 'true') {
            dir(terraformDirectoryEC2CW) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('CW Init Plan') {
						terraform_init(tfstateBucketPrefixEC2CW + '/cw_alarm_ec2', instance_name, 'cw-alarm')
						set_env_variables()
						terraform_plan(global_2_tfvars,cw_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('CW Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		// ###################################################
		// Destroy Starts 
		// ###################################################
		if (includeCW == 'true') {
            dir(terraformDirectoryEC2CW) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('CW-D Init Plan') {
						terraform_init(tfstateBucketPrefixEC2CW + '/cw_alarm_ec2', instance_name, 'cw-alarm')
						set_env_variables()
						terraform_plan_destroy(global_2_tfvars,cw_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('CW-D Destroy') {
						approval()
						terraform_destroy()
					}
				}
			}
		}
		if (includeEBSAttach == 'true') {
            dir(terraformDirectoryEBSAttach) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('EBS-A Init Plan') {
						terraform_init(tfstateBucketPrefixEBSA, instance_name, 'ebs-attach')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('EBS-A Destroy') {
						approval()
						terraform_destroy()
					}
				}
            }
        }
		if (includeEC2 == 'true') {
            dir(terraformDirectoryEC2) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('EC2-D Init Plan') {
						terraform_init(tfstateBucketPrefixEC2, instance_name, 'ec2-instance')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ec2_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('EC2-D Destroy') {
						approval()
						terraform_destroy()
					}
				}
            }
        }
		if (includeEBS == 'true') {
            dir(terraformDirectoryEBS) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('EBS-D Init Apply') {
						terraform_init(tfstateBucketPrefixEBS, instance_name, 'ebs-volumes')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('EBS-D Destroy') {
						approval()
						terraform_destroy()
					}
				}
            }
        }
		if (includeENI == 'true') {
			dir(terraformDirectoryENI) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('ENI-D Init Plan') {
						terraform_init(tfstateBucketPrefixENI, instance_name, 'eni')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ec2_eni_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('ENI-D Destroy') {
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
						terraform_init(tfstateBucketPrefixSGR, sg_group_name, 'sg-rule')
						set_env_variables()
						terraform_plan_destroy(global_2_tfvars,sg_rule_tfvars)
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
						terraform_init(tfstateBucketPrefixSG, sg_group_name, 'sg')
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
	env.TF_VAR_ec2_instance_name		= "${instance_name}"
	env.TF_VAR_resource_name			= "${instance_name}"
	env.TF_VAR_ebs_volume_count			= "${ebs_volume_count}"
	env.TF_VAR_aws_vpc_name				= "${vpc_name}"
	env.TF_VAR_vpc_subnet_name			= "${subnet}"
	env.TF_VAR_sg_group_name			= "${sg_group_name}"
	env.TF_VAR_ec2_sg_name				= "${sg_group_name}"
	env.TF_VAR_ec2_instance_type		= "${instance_type}"
	env.TF_VAR_root_user				= "${root_user}"
	env.TF_VAR_root_passwd				= "${root_passwd}"
	env.TF_VAR_ec2_az					= "${AZ}"
	env.TF_VAR_ebs_az					= "${AZ}"
	env.TF_VAR_ebs_key_state_prefix		= "${tfstateBucketPrefixEBS}"
	env.TF_VAR_ec2_ami_regex			= "${ec2_ami_regex}"
	env.TF_VAR_ec2_ami_owner_id			= "${ec2_ami_owner_id}"
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
