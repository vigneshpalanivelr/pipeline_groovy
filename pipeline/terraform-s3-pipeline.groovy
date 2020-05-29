/*
*	gitRepo
*	gitBranch
*	gitCreds
*	awsAccount
*	tfstateBucket
*	tfstateBucketPrefixS3
*	tfstateBucketPrefixS3L

*	s3_bucket_name
*	s3_versioning
*	s3_log_bucket_name

*	includeS3Bucket
*	includeS3LogBucket
*	terraformApplyPlan
*/

node ('master'){
	terraformDirectoryS3	= "modules/all_modules/${tfstateBucketPrefixS3}"
	terraformDirectoryS3Log	= "modules/all_modules/${tfstateBucketPrefixS3L}"
	
	global_tfvars   	= "../../../variables/global_vars.tfvars"
	s3_storage_tfvars	= "../../../variables/s3_storage_vars.tfvars"
	
	date 				= new Date()
	println date

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"

	stage('Checkout') {
		checkout()
		//Log Bucket should be created 1st
		//This can also be used for Normal bucket without logging
		if (includeS3LogBucket == 'true') {
			dir(terraformDirectoryS3Log) {
				stage('Remote State Init') {
					terraform_init(tfstateBucketPrefixS3L, s3_log_bucket_name, 's3-log-bucket')
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Plan S3 Log Bucket') {
						set_env_variables()
						terraform_plan(global_tfvars,s3_storage_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Plan') {
						approval()
						terraform_apply()
					}
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,s3_storage_tfvars)
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
		//Normal Bucket should be created after Log Bucket
		//THis bucket with loggin enabled
		if (includeS3Bucket == 'true') {
			dir(terraformDirectoryS3) {
				stage('Remote State Init') {
					terraform_init(tfstateBucketPrefixS3, s3_bucket_name ,'s3-bucket')
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,s3_storage_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('Approve Plan') {
						approval()
						terraform_apply()
					}
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('Plan Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,s3_storage_tfvars)
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
		$class: 'GitSCM', 
		branches: [[name: gitBranch ]], 
		doGenerateSubmoduleConfigurations: false, 
		clearWorkspace: true,
		extensions: [
			[$class: 'CleanCheckout'], [
			$class: 'SubmoduleOption', 
			disableSubmodules: false, 
			parentCredentials: true, 
			recursiveSubmodules: true, 
			reference: '', trackingSubmodules: false]], 
		submoduleCfg: [], 
		userRemoteConfigs: [[credentialsId: gitCreds, url: gitRepo]]
	])
}

def set_env_variables() {
	env.TF_VAR_aws_account_num		= "${awsAccount}"
	env.TF_VAR_s3_bucket_name		= "${s3_bucket_name}"
	env.TF_VAR_s3_log_bucket_name	= "${s3_log_bucket_name}"
	env.TF_VAR_s3_versioning		= "${s3_versioning}"
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

def terraform_plan(global_tfvars,s3_storage_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform plan -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${s3_storage_tfvars}"
	}
}

def terraform_apply() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
		sh "terraform apply -input=false tfplan"
	}
}

def terraform_plan_destroy(global_tfvars,s3_storage_tfvars) {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
    	sh "terraform plan -destroy -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${s3_storage_tfvars}"
    }
}

def terraform_destroy() {
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
    	sh "terraform apply -input=false tfdestroy"
    }
}
