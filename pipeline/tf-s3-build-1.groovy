/*
*	gitRepo
*	gitBranch
*	gitCreds
*	tfstateBucket
*	tfstateBucketPrefixS3
*	tfstateBucketPrefixS3L

*	s3_bucket_name
*	s3_log_bucket_name
*	s3_versioning

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

	stage('Approval') {
		approval()
	}

	stage('Checkout') {
		checkout()
		if (includeS3LogBucket == 'true') {
			dir(terraformDirectoryS3Log) {
				stage('Remote State Init') {
					terraform_init(tfstateBucketPrefixS3L,s3_log_bucket_name)
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan S3 Log Bucket') {
						set_env_variables()
						terraform_plan(global_tfvars,s3_storage_tfvars)
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
					stage('Plan Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,s3_storage_tfvars)
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
		if (includeS3Bucket == 'true') {
			dir(terraformDirectoryS3) {
				stage('Remote State Init') {
					terraform_init(tfstateBucketPrefixS3,s3_bucket_name)
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('Terraform Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,s3_storage_tfvars)
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
					stage('Plan Destroy') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,s3_storage_tfvars)
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
	timeout(time: 1, unit: 'MINUTES') {
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
	env.TF_VAR_s3_bucket_name		= "${s3_bucket_name}"
	env.TF_VAR_s3_log_bucket_name	= "${s3_log_bucket_name}"
	env.TF_VAR_s3_versioning		= "${s3_versioning}"
}

def terraform_init(tfstateBucketPrefix,s3_bucket_name) {
	withEnv(["GIT_ASKPASS=${WORKSPACE}/askp-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'STASH_USERNAME', passwordVariable: 'STASH_PASSWORD')]) {
			sh "terraform init -no-color -input=false -upgrade=true -backend=true -force-copy -backend-config='bucket=${tfstateBucket}' -backend-config='key=${tfstateBucketPrefix}/${s3_bucket_name}-bucket.tfstate'"
		}
	}
}

def terraform_plan(global_tfvars,s3_storage_tfvars) {
	sh "terraform plan -no-color -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${s3_storage_tfvars}"
}

def terraform_apply() {
	sh "terraform apply -no-color -input=false tfplan"
}

def terraform_plan_destroy(global_tfvars,s3_storage_tfvars) {
        sh "terraform plan -destroy -no-color -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${s3_storage_tfvars}"
}

def terraform_destroy() {
        sh "terraform apply -no-color -input=false tfdestroy"
}
