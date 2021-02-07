/*
*	gitRepo
*	gitBranch
*	gitCreds

*	awsAccount
*	packerDir
*	packerVarFile
*	packerTempFile
*	packerLogLevel
*	packerLogFile

*	packer_ami_name
*	source_ami_name
*	source_ami_owner
*	vpc_name
*	subnet_name
*	security_group_name
*	packer_instance_type
*	RHEL
*	packerRepo
*	packerBranch
*	pipModules
*	PG_MAJOR
*	PG_MINOR
*	packerVersion
*	tfVersion
*	group_name
*	username
*	password

*	includeAMIBuild
*	ami_id
*	deleteAMI
*/

node ('master'){
	stage('Checkout') {
		checkout()
	}
	stage('Hardening') {
		if (pgsql) {
			ins_pgsql = "pgsql_install"
		}
		if (packer) {
			ins_packer = "packer_install"
		}
		if (terraform) {
			ins_tf = "terraform_install"
		}
		if (CloudWatch) {
			cre_cw = "configure_cw"
		}
		if (CloudInit) {
			set_ci = "setup_cloud_init"
		}
		if (jenkins) {
			ins_jenkins = "jenkins_install"
		}
		if (jenkinsPlugins) {
			jenkins_plugin = "jenkins_plugin"
		}
		if (pythonModules) {
			python_modules = "python_modules"
		}
		if (createGroup) {
			cre_grp = "create_group"
		}
		if (createUser) {
			cre_usr = "create_user"
		}
		if (addSudoers) {
			add_sudo = "add_sudoers"
		}
	}
	if (includeAMIBuild == 'true') {
		stage('Build AMI') {
			try {
				wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
					sourceAMI = buildAMI(awsAccount, packerDir, packerVarFile, packerTempFile, packerLogLevel, packerLogFile)
				}
			}
			catch(Exception Error) {
				currentBuild.result = "FAILURE"
				destroyPackerinstance()
				print(Error.getMessage())
				error("--------------- Failure Building AMI ---------------")
			}
			sourceAMIName = getAMIName(sourceAMI)
		}
	}
	if (includeAMIBuild == 'true' && includeAMIEncrypt == 'true') {
		stage('Encrypt AMI') {
			instance   = createEC2(sourceAMI, 'alias/'+kmsAlias, subnetId, sgId)
			encryptAMI(instance)
		}
	}
	else if (includeAMIEncrypt == 'true') {
		stage('Encrypt AMI') {
			instance   = createEC2(amiId, 'alias/'+kmsAlias, subnetId, sgId)
			encryptAMI(instance)
		}
	}	
	if (includeAMIBuild == 'true' && deleteAMI == 'true' ) {
		stage('Delete AMI') {
			deleteAMI(sourceAMI)
		}
	}
	else if (deleteAMI == 'true') {
		stage('Delete AMI') {
			deleteAMI(ami_id)
		}
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

def buildAMI(awsAccount, packerDir, packerVarFile, packerTempFile, packerLogLevel, packerLogFile) {
	writeFile(file:	"git-askpass-${BUILD_TAG}", text:"#!/bin/bash\ncase \"\$1\" in \nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) echo \"\${STASH_PASSWORD}\" ;;\nesac")
	sh "chmod a+x git-askpass-${BUILD_TAG}"
	withEnv(["GIT_ASKPASS=${WORKSPACE}/git-askpass-${BUILD_TAG}"]) {
		withCredentials([usernamePassword(credentialsId: 'gitCreds',passwordVariable: 'STASH_PASSWORD',usernameVariable: 'STASH_USERNAME')]) {
			def awsAMI
			echo "--------------- Building AWS AMI ---------------"
			//echo "packer validate -var-file=${packerVarFile} -var \"packer_instance_type=${packer_instance_type}\" -var \"packer_ami_name=${packer_ami_name}\" -var \"source_ami_name=${source_ami_name}\" -var \"source_ami_owner=${source_ami_owner}\" -var \"vpc_name=${vpc_name}\" -var \"subnet_name=${subnet_name}\" -var \"security_group_name=${security_group_name}\" ${packerTempFile}"
			dir(packerDir) {
				sh """packer validate -var-file=${packerVarFile}                       \
								   -var "packer_instance_type=${packer_instance_type}" \
								   -var "packer_ami_name=${packer_ami_name}"           \
								   -var "source_ami_name=${source_ami_name}"           \
								   -var "source_ami_owner=${source_ami_owner}"         \
								   -var "vpc_name=${vpc_name}"                         \
								   -var "subnet_name=${subnet_name}"                   \
								   -var "security_group_name=${security_group_name}"   \
								   -var "STASH_USERNAME=${STASH_USERNAME}"             \
								   -var "STASH_PASSWORD=${STASH_PASSWORD}"             \
								   -var "RHEL=${RHEL}"                                 \
								   -var "gitRepo=${packerRepo}"                        \
								   -var "gitBranch=${packerBranch}"                    \
								   -var "pipModules=${pipModules}"                     \
								   -var "PG_MAJOR=${PG_MAJOR}"                         \
								   -var "PG_MINOR=${PG_MINOR}"                         \
								   -var "packerVersion=${packerVersion}"               \
								   -var "tfVersion=${tfVersion}"                       \
								   -var "group_name=${group_name}"                     \
								   -var "username=${username}"                         \
								   -var "password=${password}"                         \
								   -var "ins_pgsql=${ins_pgsql}"                       \
								   -var "ins_packer=${ins_packer}"                     \
								   -var "ins_tf=${ins_tf}"                             \
								   -var "cre_cw=${cre_cw}"                             \
								   -var "set_ci=${set_ci}"                             \
								   -var "ins_jenkins=${ins_jenkins}"                   \
								   -var "jenkins_plugin=${jenkins_plugin}"             \
								   -var "python_modules=${python_modules}"             \
								   -var "cre_grp=${cre_grp}"                           \
								   -var "cre_usr=${cre_usr}"                           \
								   -var "add_sudo=${add_sudo}"                         \
					  ${packerTempFile}"""
				sh """set -o pipefail;                                                 \
					  packer build -var-file=${packerVarFile}                          \
								   -var "packer_instance_type=${packer_instance_type}" \
								   -var "packer_ami_name=${packer_ami_name}"           \
								   -var "source_ami_name=${source_ami_name}"           \
								   -var "source_ami_owner=${source_ami_owner}"         \
								   -var "vpc_name=${vpc_name}"                         \
								   -var "subnet_name=${subnet_name}"                   \
								   -var "security_group_name=${security_group_name}"   \
								   -var "STASH_USERNAME=${STASH_USERNAME}"             \
								   -var "STASH_PASSWORD=${STASH_PASSWORD}"             \
								   -var "RHEL=${RHEL}"                                 \
								   -var "gitRepo=${packerRepo}"                        \
								   -var "gitBranch=${packerBranch}"                    \
								   -var "pipModules=${pipModules}"                     \
								   -var "PG_MAJOR=${PG_MAJOR}"                         \
								   -var "PG_MINOR=${PG_MINOR}"                         \
								   -var "packerVersion=${packerVersion}"               \
								   -var "tfVersion=${tfVersion}"                       \
								   -var "group_name=${group_name}"                     \
								   -var "username=${username}"                         \
								   -var "password=${password}"                         \
								   -var "ins_pgsql=${ins_pgsql}"                       \
								   -var "ins_packer=${ins_packer}"                     \
								   -var "ins_tf=${ins_tf}"                             \
								   -var "cre_cw=${cre_cw}"                             \
								   -var "set_ci=${set_ci}"                             \
								   -var "ins_jenkins=${ins_jenkins}"                   \
								   -var "jenkins_plugin=${jenkins_plugin}"             \
								   -var "python_modules=${python_modules}"             \
								   -var "cre_grp=${cre_grp}"                           \
								   -var "cre_usr=${cre_usr}"                           \
								   -var "add_sudo=${add_sudo}"                         \
					  ${packerTempFile} | tee -a ${packerLogFile}"""
			}
			awsAMI = sh(script: "egrep --only-matching --regexp='ami-.{17}' ${packerDir}/${packerLogFile} | tail -1", returnStdout: true).trim()
			return awsAMI
		}
	}
}

def destroyPackerinstance() {
	instanceId = sh(script: "egrep --only-matching --regexp='i-.(\\w){17}' ${packerDir}/${packerLogFile} | head -1", returnStdout: true).trim()
	echo "--------------- AWS Packer EC2 Instance : Destroyed ---------------"
	sh "aws ec2 terminate-instances --instance-ids ${instanceId}"
}

def getAMIName(sourceAMI) {
	amiName = sh(script: "aws ec2 describe-images --image-ids ${sourceAMI} --query 'Images[*].{Name:Name}' --output text", returnStdout: true).trim()
	echo "--------------- Un-Encrypted AMI Creation : Completed ---------------"
	echo "Un-Encrypted AMI ID   : ${sourceAMI}"
	echo "Un-Encrypted AMI Name : ${amiName}"
	return amiName
}

def deleteAMI(ami_id) {
	echo "--------------- Deleting AMI : Completed ---------------"
	sh "aws ec2 deregister-image --image-id ${ami_id}"
}

def createEC2(AMI, KMSAlias, subnet_id, sg_id ) {
	//AMI         = sh(script: """aws ec2 describe-images --region ${aws_region} --filters "Name=owner-id, Values=${awsAccount}" --query 'sort_by(Images, &CreationDate)[].ImageId' --output text""", returnStdout: true).trim()
	KMSkey      = sh(script: """aws kms list-aliases --query "Aliases[?AliasName=='${KMSAlias}'].AliasArn" --output text""", returnStdout: true).trim()
	EC2id       = sh(script: """aws ec2 run-instances --image-id ${AMI} --instance-type t2.micro --region ap-south-1 --subnet-id ${subnet_id} --security-group-ids ${sg_id} --count 1 --block-device-mappings '[{"DeviceName": "/dev/sda1", "Ebs": {"DeleteOnTermination": true, "KmsKeyId":"${KMSkey}", "Encrypted" : true}}]' --query "Instances[].InstanceId" --output text""", returnStdout: true).trim()
	
	instanceID  = ""
	while(EC2id != instanceID) {
		instanceID = sh(script: "aws ec2 describe-instance-status --instance-ids ${EC2id} --filters Name=instance-state-name,Values=running Name=instance-status.reachability,Values=passed --query 'InstanceStatuses[].InstanceId[]' --output text", returnStdout: true).trim()
		echo "--------------- Waiting for EC2 : In-Progress ---------------"
		sh 'sleep 30'
	}
	echo "EC2 is UP : ${instanceID}"
	echo "--------------- Waiting for EC2 : Completed ---------------"
	return EC2id
}

def encryptAMI(EC2id) {
	datetime     = sh(script: """echo -e \$(date +'%Y-%m-%d-%H-%M-%S')""", returnStdout: true).trim()
	encyptingAMI = sh(script: """aws ec2 create-image --instance-id ${EC2id} --name jenkins-ami-custom-${datetime} --no-reboot --output text""", returnStdout: true).trim()
	
	amiID        = ""
	while(encyptingAMI != amiID) {
		amiID = sh(script: """aws ec2 describe-images --region ap-south-1 --image-ids ${encyptingAMI} --owners self --filters Name=state,Values=available --query "Images[].ImageId[]" --output text""", returnStdout: true).trim()
		echo "--------------- Encypting AMI : In-Progress ---------------"
		sh 'sleep 60'
	}
	echo "Encrypted AMI ID   : ${amiID}"
	echo "--------------- Encypting AMI : Completed ---------------"
}