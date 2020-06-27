/*
*	gitRepo
*	gitBranch
*	gitCreds
*	awsAccount
*	tfstateBucket
*	tfstateBucketPrefixSG
*	tfstateBucketPrefixSGR
*	tfstateBucketPrefixLB
*	tfstateBucketPrefixALIAS

*	vpc_name
*	lb_name
*	lb_type
*	lb_is_internal
*	lb_sg_name

*	lis_port
*	lis_protocol
*	lis_response_type

*	tg_name
*	tg_port
*	tg_protocol
*	tg_target_type

*	ec2_name
*	tg_attach_name

*	rule_target_dns
*	rule_response_type

*	includeSG
*	includeSGRule
*	includeLB
*	includeLBLis
*	includeLBTG
*	includeLBTGA
*	includeR53AliasRecord
*	includeLBRule
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectorySG		= "modules/all_modules/${tfstateBucketPrefixSG}"
	terraformDirectorySGRule	= "modules/all_modules/${tfstateBucketPrefixSGR}/${lb_sg_name}-sg"
	terraformDirectoryLB		= "modules/all_modules/${tfstateBucketPrefixLB}/load_balancer/"
	terraformDirectoryLBLis		= "modules/all_modules/${tfstateBucketPrefixLB}/load_balancer_listener/"
	terraformDirectoryLBTG		= "modules/all_modules/${tfstateBucketPrefixLB}/load_balancer_tg/"
	terraformDirectoryLBTGA		= "modules/all_modules/${tfstateBucketPrefixLB}/load_balancer_tg_attachment/"
	terraformDirectoryLBRule	= "modules/all_modules/${tfstateBucketPrefixLB}/load_balancer_listener_rule/"
	terraformDirectoryR53Alias	= "modules/all_modules/${tfstateBucketPrefixALIAS}/"
	
	global_tfvars				= "../../../variables/global_vars.tfvars"
	sg_tfvars					= "../../../variables/sg_vars.tfvars"
	lb_tfvars					= "../../../variables/lb_vars.tfvars"
	global_2_tfvars				= "../../../../variables/global_vars.tfvars"
	sg_2_tfvars					= "../../../../variables/sg_vars.tfvars"
	lb_2_tfvars					= "../../../../variables/lb_vars.tfvars"
	
	date						= new Date()
	println date

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"

	stage('Checkout') {
		checkout()
		//Create Load-Balancer SG
		if (includeSG == 'true') {
			dir(terraformDirectorySG) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixSG, lb_sg_name, "sg")
						set_env_variables()
						terraform_plan(global_tfvars, sg_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//Create Load-Balancer SG Rules
		if (includeSGRule == 'true') {
			dir(terraformDirectorySGRule) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixSGR, lb_sg_name, "sg-rule")
						set_env_variables()
						terraform_plan(global_2_tfvars, sg_2_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//Create Load-Balancer
		if (includeLB == 'true') {
			dir(terraformDirectoryLB) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixLB, lb_name, "lb")
						set_env_variables()
						terraform_plan(global_2_tfvars,lb_2_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//Create Load-Balancer Listener
		if (includeLBLis == 'true') {
			dir(terraformDirectoryLBLis) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixLB, lb_name + '-' + lis_port + '-' + lis_protocol, "lb-listener")
						set_env_variables()
						terraform_plan(global_2_tfvars,lb_2_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//Create Load-Balancer Target Group
		if (includeLBTG == 'true') {
			dir(terraformDirectoryLBTG) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixLB, lb_name + '-' + tg_name, "lb-tg")
						set_env_variables()
						terraform_plan(global_2_tfvars,lb_2_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//Create Load-Balancer Target Group Attachment 
		if (includeLBTGA == 'true') {
			dir(terraformDirectoryLBTGA) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixLB, lb_name + '-' + tg_attach_name, "lb-tg-attach")
						set_env_variables()
						terraform_plan(global_2_tfvars,lb_2_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//Create Load-Balancer DNS 
		if (includeR53AliasRecord == 'true') {
			dir(terraformDirectoryR53Alias) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixLB, rule_target_dns, "alias-dns")
						set_env_variables()
						terraform_plan(global_tfvars, lb_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//Create Load-Balancer Listener Rule
		if (includeLBRule == 'true') {
			dir(terraformDirectoryLBRule) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixLB, lb_name + '-' + rule_target_dns + '-' + tg_name, "lb-lis-rule")
						set_env_variables()
						terraform_plan(global_2_tfvars,lb_2_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//################################################################################################################
		//Destroy Pipeline starts
		//################################################################################################################
		//Destroy Load-Balancer Listener Rule
		if (includeLBRule == 'true') {
			dir(terraformDirectoryLBRule) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						terraform_init(tfstateBucketPrefixLB, lb_name + '-' + rule_target_dns + '-' + tg_name, "lb-lis-rule")
						set_env_variables()
						terraform_plan_destroy(global_2_tfvars,lb_2_tfvars)
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
		//Destroy Load-Balancer DNS 
		if (includeR53AliasRecord == 'true') {
			dir(terraformDirectoryR53Alias) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						terraform_init(tfstateBucketPrefixLB, rule_target_dns, "alias-dns")
						set_env_variables()
						terraform_plan_destroy(global_tfvars,lb_tfvars)
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
		//Destroy Load-Balancer Target Group Attachment 
		if (includeLBTGA == 'true') {
			dir(terraformDirectoryLBTGA) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Terraform Plan') {
						terraform_init(tfstateBucketPrefixLB, lb_name + '-' + tg_attach_name, "lb-tg-attach")
						set_env_variables()
						terraform_plan_destroy(global_2_tfvars,lb_2_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//Destroy Load-Balancer Target Group
		if (includeLBTG == 'true') {
			dir(terraformDirectoryLBTG) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						terraform_init(tfstateBucketPrefixLB, lb_name + '-' + tg_name, "lb-tg")
						set_env_variables()
						terraform_plan_destroy(global_2_tfvars,lb_2_tfvars)
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
		//Destroy Load-Balancer Listener
		if (includeLBLis == 'true') {
			dir(terraformDirectoryLBLis) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						terraform_init(tfstateBucketPrefixLB, lb_name + '-' + lis_port + '-' + lis_protocol, "lb-listener")
						set_env_variables()
						terraform_plan_destroy(global_2_tfvars,lb_2_tfvars)
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
		//Destroy Load-Balancer
		if (includeLB == 'true') {
			dir(terraformDirectoryLB) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						terraform_init(tfstateBucketPrefixLB, lb_name, "lb")
						set_env_variables()
						terraform_plan_destroy(global_2_tfvars,lb_2_tfvars)
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
		//Destroy Load-Balancer SG Rues
		if (includeSGRule == 'true') {
			dir(terraformDirectorySGRule) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						terraform_init(tfstateBucketPrefixSGR, lb_sg_name, "sg-rule")
						set_env_variables()
						terraform_plan_destroy(global_2_tfvars,sg_2_tfvars)
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
		//Destroy Load-Balancer SG
		if (includeSG == 'true') {
			dir(terraformDirectorySG) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						terraform_init(tfstateBucketPrefixSG, lb_sg_name, "sg")
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
	env.TF_VAR_aws_vpc_name			= "${vpc_name}"
	env.TF_VAR_aws_account_num		= "${awsAccount}"
	env.TF_VAR_lb_name		   		= "${lb_name}"
	env.TF_VAR_lb_type				= "${lb_type}"
	env.TF_VAR_lb_is_internal		= "${lb_is_internal}"
	env.TF_VAR_sg_group_name		= "${lb_sg_name}"
	env.TF_VAR_resource_name		= "${lb_sg_name}"
	env.TF_VAR_lb_port				= "${lis_port}"
	env.TF_VAR_lb_protocol			= "${lis_protocol}"
	env.TF_VAR_lb_response_type		= "${lis_response_type}"
	env.TF_VAR_lb_tg_name			= "${tg_name}"
	env.TF_VAR_lb_tg_port			= "${tg_port}"
	env.TF_VAR_lb_tg_protocol		= "${tg_protocol}"
	env.TF_VAR_lb_tg_target_type	= "${tg_target_type}"
	env.TF_VAR_ec2_name       		= "${ec2_name}"
	env.TF_VAR_rule_target_dns		= "${rule_target_dns}"
	env.TF_VAR_rule_response_type	= "${rule_response_type}"
	env.TF_VAR_r53_record_name		= "${rule_target_dns}"
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

def terraform_plan(global_tfvars,resource_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform plan -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${resource_tfvars}"
	}
}

def terraform_apply() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform apply -input=false tfplan"
	}
}

def terraform_plan_destroy(global_tfvars,resource_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
    	sh "terraform plan -destroy -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${resource_tfvars}"
    }
}

def terraform_destroy() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
    	sh "terraform apply -input=false tfdestroy"
    }
}