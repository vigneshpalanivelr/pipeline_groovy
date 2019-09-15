/*
*	gitRepo
*	gitBranch
*	gitCreds
*	tfstateBucket
*	tfstateBucketPrefix

*	db_family
*	db_engine
*	db_engine_version
*	db_instance_class
*	db_identifier
*	db_source_identifier
*	db_name
*	db_username
*	db_password
*	db_allocated_storage
*	db_multi_az
*	db_read_replica
*	db_apply_changes
*	db_route53_name

*	includeMaster
*	includeReplica
*	includeInstanceDNS
*	terraformApplyPlan
Error on pipeline
	local variable	: Use def keyword
	global variable	: No def keyword
	sqlserver-ex	: Engine sqlserver-ex does not support encryption at rest
	sqlserver-web 	: DBName must be null for engine
	sqlserver-se	: DBName must be null for engine
Recommendations and Error on Terraform
	Creating RR	: DBSubnetGroupNotAllowedFault: DbSubnetGroupName should not be specified for read replicas that are created in the same region as the master
	Creating RR	: Cannot change master user password on an RDS postgres Read Replica because it uses physical replication and therefore cannot differ from its parent.
	Creating RR	: InvalidDBInstanceState: DB Backups not supported on a read replica for engine postgres
	Recommending RR	: Change or Disable Availability zone variable in RR
	Recommending RR	: Disable Username and Database
	Destroying RR	: DB Instance FinalSnapshotIdentifier is required when a final snapshot is required
Steps
	1)	Create RDS Instance
	2)	Create RDS DNS
	3)	Destroy RDS DNS
	4)	Destroy RDS Instance
	5)	Defined all the required functions
Pending Implementation
	1)	Creating CW Alarms
	2)	Creating Application DBA user
*/

node('master') {

	terraformDirMasterRDS	= "modules/all_modules/rds_module"
	terraformDirReplicaRDS	= "modules/all_modules/rds_replica_module"
	terraformDirectoryDNS	= "modules/all_modules/rds_dns_module"

	global_tfvars		= "../../../variables/global_vars.tfvars"
	rds_tfvars		= "../../../variables/rds_vars.tfvars"
	rds_dns_tfvars		= "../../../variables/rds_dns_vars.tfvars"

	db_rds			= (db_engine		=~ /[a-zA-Z]+/)[0]
	db_engine_major_version = (db_engine_version	=~ /\d+.\d+/)[0]
	
	date			= new Date()
	println date
	

	writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASSWORD}\" ;;\nesac")
	sh "chmod a+x askp-${BUILD_TAG}"
	
	stage('Approval'){
		approval()
	}
	stage('Checkout') {
		checkout()
		//Create Master RDS Instance
		if ((includeMaster == 'true') && (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply')) {
			dir(terraformDirMasterRDS) {
				stage('RDS Init') {
					terraform_rds_init()
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('RDS Plan'){
						withEnv(["TF_VAR_db_password=${db_password}"]) {
							set_env_variables()
							terraform_plan(global_tfvars,rds_tfvars)
						}
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('RDS Approve'){
						approval()
					}
					stage('RDS Apply') {
						terraform_apply()
					}
				}
			}
		}
		//Create Replica RDS Instance
		if ((includeReplica == 'true') && (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply')) {
			dir(terraformDirReplicaRDS) {
				stage('RDS Init') {
					terraform_rds_init()
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('RDS Plan'){
						set_env_variables()
						terraform_plan(global_tfvars,rds_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('RDS Approve'){
						approval()
					}
					stage('RDS Apply') {
						terraform_apply()
					}
				}
			}
		}
		//Create RDS DNS
		if ((includeInstanceDNS == 'true') && (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply')) {
			dir(terraformDirectoryDNS) {
				stage('DNS Init') {
					terraform_dns_init()
				}
				if (terraformApplyPlan == 'plan' || terraformApplyPlan == 'apply') {
					stage('DNS Plan') {
						set_env_variables()
						terraform_plan(global_tfvars,rds_dns_tfvars)
					}
				}
				if (terraformApplyPlan == 'apply') {
					stage('DNS Approve'){
						approval()
					}
					stage('DNS Apply') {
						terraform_apply()
					}
				}
			}
		}
		//Destroy RDS DNS
		if ((includeInstanceDNS == 'true') && (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy')) {
			dir(terraformDirectoryDNS) {
				stage('DNS Init') {
					terraform_dns_init()
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('DNS Destroy Plan') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,rds_dns_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('DNS Approve') {
						approval()
					}
					stage('DNS Destroy') {
						terraform_destroy()
					}
				}
			}
		}
		//Destroy Replica RDS Instance
		if ((includeReplica == 'true') && (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy')) {
			dir(terraformDirReplicaRDS) {
				stage('RDS Init') {
					terraform_rds_init()
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('RDS Destroy Plan') {
						set_env_variables()
						terraform_plan_destroy(global_tfvars,rds_tfvars)
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('RDS Approve') {
						approval()
					}
					stage('RDS Destroy') {
						terraform_destroy()
					}
				}
			}
		}
		//Destroy Master RDS Instance
		if ((includeMaster == 'true') && (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy')) {
			dir(terraformDirMasterRDS) {
				stage('RDS Init') {
					terraform_rds_init()
				}
				if (terraformApplyPlan == 'plan-destroy' || terraformApplyPlan == 'destroy') {
					stage('RDS Destroy Plan') {
						withEnv(["TF_VAR_db_password=${db_password}"]) {
							set_env_variables()
							terraform_plan_destroy(global_tfvars,rds_tfvars)
						}
					}
				}
				if (terraformApplyPlan == 'destroy') {
					stage('RDS Approve') {
						approval()
					}
					stage('RDS Destroy') {
						terraform_destroy()
					}
				}
			}
		}
	}
}

//Common functions
def approval() {
	timeout(time: 5, unit: 'MINUTES') {
		input(
			id: 'Approval', message: 'Shall I Continue ?', parameters: [[
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
	env.TF_VAR_db_family            	= "${db_family}"
	env.TF_VAR_db_engine            	= "${db_engine}"
	env.TF_VAR_db_engine_version    	= "${db_engine_version}"
	env.TF_VAR_db_engine_major_version	= "${db_engine_major_version}"
	env.TF_VAR_db_identifier        	= "${db_identifier}"
	env.TF_VAR_db_source_identifier		= "${db_source_identifier}"
	env.TF_VAR_db_instance_class    	= "${db_instance_class}"
	env.TF_VAR_db_rds			= "${db_rds}"
	env.TF_VAR_db_name              	= "${db_name}"
	env.TF_VAR_db_username          	= "${db_username}"
	env.TF_VAR_db_allocated_storage 	= "${db_allocated_storage}"
	env.TF_VAR_db_multi_az          	= "${db_multi_az}"
	env.TF_VAR_db_read_replica		= "${db_read_replica}"
	env.TF_VAR_db_apply_immediately		= "${db_apply_changes}"
	env.TF_VAR_db_route53_name		= "${db_route53_name}"
}
def terraform_plan(global_tfvars,first_tfvars) {
	sh "terraform plan -no-color -out=tfplan -input=false -var-file=${global_tfvars} -var-file=${first_tfvars}"
}
def terraform_apply() {
	sh "terraform apply -no-color -input=false tfplan"
}
def terraform_plan_destroy(global_tfvars,first_tfvars) {
	sh "terraform plan -destroy -no-color -out=tfdestroy -input=false -var-file=${global_tfvars} -var-file=${first_tfvars}"
}
def terraform_destroy() {
	sh "terraform apply -no-color -input=false tfdestroy"
}

//RDS Instance init/load remote state function
def terraform_rds_init() {
	withEnv(["GIT_ASKPASS=${WORKSPACE}/askp-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'STASH_USERNAME', passwordVariable: 'STASH_PASSWORD')]) {
			sh "terraform init -no-color -input=false -upgrade=true -backend=true -force-copy -backend-config='bucket=${tfstateBucket}' -backend-config='key=${tfstateBucketPrefixRDS}/${db_identifier}.tfstate'"
		}
	}
}

//RDS DNS init/load remote state function
def terraform_dns_init() {
	withEnv(["GIT_ASKPASS=${WORKSPACE}/askp-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'STASH_USERNAME', passwordVariable: 'STASH_PASSWORD')]) {
			sh "terraform init -no-color -input=false -upgrade=true -backend=true -force-copy -backend-config='bucket=${tfstateBucket}' -backend-config='key=${tfstateBucketPrefixDNS}/${db_route53_name}-dns.tfstate'"
		}
	}
}
