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
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectorySG		= "modules/all_modules/${tfstateBucketPrefixSG}"
	terraformDirectorySGRule	= "modules/all_modules/${tfstateBucketPrefixSGR}"
	terraformDirectoryENI		= "modules/all_modules/${tfstateBucketPrefixENI}"
	terraformDirectoryEBS		= "modules/all_modules/${tfstateBucketPrefixEBS}"
	terraformDirectoryEC2		= "modules/all_modules/${tfstateBucketPrefixEC2}"
    
	global_tfvars           	= "../../../variables/global_vars.tfvars"
	sg_tfvars					= "../../../variables/sg_vars.tfvars"
	sg_rule_tfvars				= "../../../../variables/sg_vars.tfvars"
	ec2_eni_tfvars				= "../../../variables/ec2_eni_vars.tfvars"
	ebs_tfvars					= "../../../variables/ebs_volume_vars.tfvars"
	ec2_tfvars					= "../../../variables/ec2_instance_vars.tfvars"
	
	date                    	= new Date()
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
				stage('Remote State Init') {
					terraform_init(tfstateBucketPrefixSG,'sg')
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform SG Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,sg_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve SG Plan') {
						approval()
					}
					stage('Terraform SG Apply') {
						terraform_apply()
					}
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
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
		if (includeSGRule == 'true') {
			dir(terraformDirectorySGRule) {
				stage('Remote State Init') {
					terraform_init(tfstateBucketPrefixSGR,'sg-rule')
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform SG Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,sg_rule_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve SG Plan') {
						approval()
					}
					stage('Terraform SG Apply') {
						terraform_apply()
					}
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan SG Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,sg_rule_tfvars)
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
		if (includeENI == 'true') {
			dir(terraformDirectoryENI) {
				stage('Remote State Init') {
					terraform_init(tfstateBucketPrefixENI,'eni')
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
					terraform_init(tfstateBucketPrefixEBS,'ebs-volumes')
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
		if (includeEC2 == 'true') {
            dir(terraformDirectoryEC2) {
                stage('Remote State Init') {
					terraform_init(tfstateBucketPrefixEC2,'ec2-instance')
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform EC2 Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,ec2_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve EC2 Plan') {
						approval()
					}
					stage('Terraform EC2 Apply') {
						terraform_apply()
					}
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan EC2 Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,ec2_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('Approve EC2 Destroy') {
						approval()
					}
					stage('EC2 Destroy') {
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
