/*
*	gitRepo
*	gitBranch
*	gitCreds
*	awsAccount
*	tfstateBucket
*	tfstateBucketPrefix
*	tfstateBucketPrefixALIAS

*	r53_zone_name
*	r53_record_name
*	r53_record_type
*	r53_records
*	r53_overwrite

*	alias_for
*	resource_name

*	includeR53acRecord
*	includeR53AliasRecord
*	terraformApplyPlan	
*/

node ('master'){
	terraformDirectory		= "modules/all_modules/${tfstateBucketPrefix}"
	terraformDirectoryALIAS	= "modules/all_modules/${tfstateBucketPrefixALIAS}"

	global_tfvars   	= "../../../variables/global_vars.tfvars"
	r53ac_tfvars		= "../../../variables/r53ac_vars.tfvars"

	date 				= new Date()
	println date

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"

	stage('Checkout') {
		checkout()
		//Create Route53 A/CNAME Record
		if (includeR53acRecord == 'true') {
			dir(terraformDirectory) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('R53 AC Plan') {
						terraform_init(tfstateBucketPrefix, r53_record_name + r53_record_type, 'dns')
						set_env_variables()
						terraform_plan(global_tfvars,r53ac_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('R53 AC Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//Create Route53 Alias Record
		if (includeR53AliasRecord == 'true') {
			dir(terraformDirectoryALIAS) {
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('R53 Alias Plan') {
						terraform_init(tfstateBucketPrefix, r53_record_name, 'alias-dns')
						set_env_variables()
						terraform_plan(global_tfvars,r53ac_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('R53 Alias Apply') {
						approval()
						terraform_apply()
					}
				}
			}
		}
		//################################################################################################################
		//Destroy Pipeline starts
		//################################################################################################################
		//Destroy Route53 Alias Record
		if (includeR53AliasRecord == 'true') {
			dir(terraformDirectoryALIAS) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('R53 AC Destroy Plan') {
						terraform_init(tfstateBucketPrefix, r53_record_name, 'alias-dns')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,r53ac_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('R53 AC Destroy Destroy') {
						approval()
						terraform_destroy()
					}
				}
			}
		}
		//Destroy Route53 A/CNAME Record
		if (includeR53acRecord == 'true') {
			dir(terraformDirectory) {
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('R53 Alias Destroy Plan') {
						terraform_init(tfstateBucketPrefix, r53_record_name + r53_record_type, 'dns')
						set_env_variables()
						terraform_plan_destroy(global_tfvars,r53ac_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('R53 Alias Destroy') {
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
	env.TF_VAR_aws_account_num		= "${awsAccount}"
	env.TF_VAR_r53_zone_name		= "${r53_zone_name}"
	env.TF_VAR_r53_record_name		= "${r53_record_name}"
	env.TF_VAR_r53_record_type		= "${r53_record_type}"
	env.TF_VAR_r53_records 			= "${r53_records}"
	env.TF_VAR_r53_overwrite		= "${r53_overwrite}"
	env.TF_VAR_alias_for			= "${alias_for}"
	env.TF_VAR_lb_name				= "${resource_name}"
	env.TF_VAR_rds_instance_name	= "${resource_name}"
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

def terraform_plan(global_tfvars,r53ac_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform plan -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${r53ac_tfvars}"
	}
}

def terraform_apply() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform apply -input=false tfplan"
	}
}

def terraform_plan_destroy(global_tfvars,r53ac_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform plan -destroy -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${r53ac_tfvars}"
	}
}

def terraform_destroy() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform apply -input=false tfdestroy"
	}
}
