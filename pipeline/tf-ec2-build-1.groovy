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
*	tfstateBucketPrefixEBSA
*	tfstateBucketPrefixEC2

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
*	includeEBSAttach
*	includeEC2
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectorySG			= "modules/all_modules/${tfstateBucketPrefixSG}"
	terraformDirectorySGRule		= "modules/all_modules/${tfstateBucketPrefixSGR}/${instance_name}-sg"
	terraformDirectoryENI			= "modules/all_modules/${tfstateBucketPrefixENI}"
	terraformDirectoryEBS			= "modules/all_modules/${tfstateBucketPrefixEBS}"
	terraformDirectoryEBSAttach		= "modules/all_modules/${tfstateBucketPrefixEBSA}"
	terraformDirectoryEC2			= "modules/all_modules/${tfstateBucketPrefixEC2}"
    
	global_tfvars					= "../../../variables/global_vars.tfvars"
	sg_tfvars						= "../../../variables/sg_vars.tfvars"
	global_sg_rule_tfvars			= "../../../../variables/global_vars.tfvars"
	sg_rule_tfvars					= "../../../../variables/sg_vars.tfvars"
	ec2_eni_tfvars					= "../../../variables/ec2_eni_vars.tfvars"
	ebs_tfvars						= "../../../variables/ebs_volume_vars.tfvars"
	ec2_tfvars						= "../../../variables/ec2_instance_vars.tfvars"
	
	date                    		= new Date()
	println date

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"

	stage('Approval') {
		approval()
	}

	stage('Checkout') {
		checkout()
		if (includeSG == 'true') {
			dir(terraformDirectorySG) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('SG Init') {
						terraform_init(tfstateBucketPrefixSG,'sg')
					}
					stage('SG Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,sg_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve SG Plan') {
						approval()
					}
					stage('SG Apply') {
						terraform_apply()
					}
				}
			}
		}
		if (includeSGRule == 'true') {
			dir(terraformDirectorySGRule) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('SGR Init') {
						terraform_init(tfstateBucketPrefixSGR,'sg-rule')
					}
					stage('SGR Plan') {
						set_env_variables()
						terraform_plan(global_sg_rule_tfvars,sg_rule_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve SGR Plan') {
						approval()
					}
					stage('SGR Apply') {
						terraform_apply()
					}
				}
			}
		}
		if (includeENI == 'true') {
			dir(terraformDirectoryENI) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('ENI Init') {
						terraform_init(tfstateBucketPrefixENI,'eni')
					}
					stage('ENI Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,ec2_eni_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve ENI Plan') {
						approval()
					}
					stage('ENI Apply') {
						terraform_apply()
					}
				}
			}
		}
		if (includeEBS == 'true') {
            dir(terraformDirectoryEBS) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('EBS Init') {
						terraform_init(tfstateBucketPrefixEBS,'ebs-volumes')
					}
					stage('EBS Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve EBS Plan') {
						approval()
					}
					stage('EBS Apply') {
						terraform_apply()
					}
				}
			}
		}
		if (includeEC2 == 'true') {
            dir(terraformDirectoryEC2) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('EC2 Init') {
						terraform_init(tfstateBucketPrefixEC2,'ec2-instance')
					}
					stage('EC2 Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,ec2_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve EC2 Plan') {
						approval()
					}
					stage('EC2 Apply') {
						terraform_apply()
					}
				}
			}
		}
		if (includeEBSAttach == 'true') {
            dir(terraformDirectoryEBSAttach) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('EBS-A Init') {
						terraform_init(tfstateBucketPrefixEBSA,'ebs-attach')
					}
					stage('EBS-A Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve EBS-A Plan') {
						approval()
					}
					stage('EBS-A Apply') {
						terraform_apply()
					}
				}
			}
		}
		# Destroy Starts
		if (includeEBSAttach == 'true') {
            dir(terraformDirectoryEBSAttach) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('EBS-A D Init') {
						terraform_init(tfstateBucketPrefixEBSA,'ebs-attach')
					}
					stage('Plan EBS-A Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve EBS-A Destroy') {
						approval()
					}
					stage('EBS-A Destroy') {
						terraform_destroy()
					}
				}
            }
        }
		if (includeEC2 == 'true') {
            dir(terraformDirectoryEC2) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('EC2-D Init') {
						terraform_init(tfstateBucketPrefixEC2,'ec2-instance')
					}
					stage('Plan EC2-D') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ec2_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve EC2-D') {
						approval()
					}
					stage('EC2-D Destroy') {
						terraform_destroy()
					}
				}
            }
        }
		if (includeEBS == 'true') {
            dir(terraformDirectoryEBS) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('EBS-D Init') {
						terraform_init(tfstateBucketPrefixEBS,'ebs-volumes')
					}
					stage('Plan EBS-D') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ebs_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve EBS-D') {
						approval()
					}
					stage('EBS Destroy') {
						terraform_destroy()
					}
				}
            }
        }
		if (includeENI == 'true') {
			dir(terraformDirectoryENI) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('ENI Init') {
						terraform_init(tfstateBucketPrefixENI,'eni')
					}
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
		if (includeSGRule == 'true') {
			dir(terraformDirectorySGRule) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('SG-R Init') {
						terraform_init(tfstateBucketPrefixSGR,'sg-rule')
					}
					stage('Plan SG-R Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_sg_rule_tfvars,sg_rule_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve SG-R Destroy') {
						approval()
					}
					stage('SG-R Destroy') {
						terraform_destroy()
					}
				}
			}
		}
		if (includeSG == 'true') {
			dir(terraformDirectorySG) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('SG Init') {
						terraform_init(tfstateBucketPrefixSG,'sg')
					}
					stage('Plan SG Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,sg_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve SG Destroy') {
						approval()
					}
					stage('SG Destroy') {
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
	env.TF_VAR_aws_account_num			= "${awsAccount}"
	env.TF_VAR_ec2_instance_name		= "${instance_name}"
	env.TF_VAR_resource_name			= "${instance_name}"
	env.TF_VAR_ebs_volume_count			= "${ebs_volume_count}"
	env.TF_VAR_aws_vpc_name				= "${vpc_name}"
	env.TF_VAR_vpc_subnet_name			= "${subnet}"
	env.TF_VAR_sg_group_name			= "${sg_group_name}"
	env.TF_VAR_ec2_sg_name				= "${sg_group_name}"
	env.TF_VAR_ec2_instance_type		= "${instance_type}"
	env.TF_VAR_ec2_az					= "${AZ}"
	env.TF_VAR_ebs_az					= "${AZ}"
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
