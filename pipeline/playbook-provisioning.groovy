/*
*	gitRepo
*	gitBranch
*	gitCreds

*	scriptType
*	playbook
*	inventory

*	pgsqlInstall
*	listInstall
*	packerInstall
*	tfInstall
*	mountVolumes
*	configCW
*	setupCI
*	createGroup
*	groupName
*	createUser
*	username
*	password
*	addSudoers

*	pgsqlUnInstall
*	listUnInstall
*	packerUnInstall
*	tfUnInstall
*	removeSudoers
*	deleteUser
*	deleteGroup
*/

node ('master') {
	ansiColor('xterm') {
		scriptsDirectory    = "${scriptType}"
		playbook            = "${playbook}"
		inventory			= "${inventory}"
		
		date				= new Date()
		println date
		
		writeFile(file: "askp-${BUILD_TAG}",text:"#!/bin/bash/\ncase \"\$1\" in\nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) \"\${STASH_PASWORD}\";;\nesac")
		sh "chmod a+x askp-${BUILD_TAG}"
		
		stage('Checkout') {
			checkout()
		}
		
		stage('Playbook Execution') {
			if (pgsqlInstall 	== 'true') {
				extras 			= "-e ins_pgsql='pgsql_install'"
				playbookTags	= "pgsql_install"
				plybkExecution(extras,playbookTags,'PgSQL Installation')
			}
			if (listInstall 	== 'true') {
				extras 			= "-e ins_all='list_install'"
				playbookTags	= "list_install"
				plybkExecution(extras,playbookTags,'Group Installation')
			}
			if (packerInstall 	== 'true') {
				extras 			= "-e ins_packer='packer_install'"
				playbookTags	= "packer_install"
				plybkExecution(extras,playbookTags,'Packer Installation')
			}
			if (tfInstall 		== 'true') {
				extras 			= "-e ins_tf='terraform_install'"
				playbookTags	= "terraform_install"
				plybkExecution(extras,playbookTags,'Terraform Installation')
			}
			if (mountVolumes 	== 'true') {
				extras 			= "-e mount='mount_volumes'"
				playbookTags	= "mount_volumes"
				plybkExecution(extras,playbookTags,'Mount Voulmes')
			}
			if (configCW		== 'true') {
				extras 			= "-e cre_cw='configure_cw'"
				playbookTags	= "configure_cw"
				plybkExecution(extras,playbookTags,'Configure Cloud Watch')
			}
			if (setupCI			== 'true') {
				extras 			= "-e set_ci='setup_cloud_init'"
				playbookTags	= "setup_cloud_init"
				plybkExecution(extras,playbookTags,'Set Cloud Init')
			}
			if (createGroup		== 'true') {
				extras 			= "-e group_name='" + groupName + "' -e cre_grp='create_group'"
				playbookTags	= "create_group"
				plybkExecution(extras,playbookTags,'Create Group')
			}
			if (createUser		== 'true') {
				extras 			= "-e group_name='" + groupName + "' -e username='" + username + "' -e password='" + password + "' -e tag_group='yes' -e action='create_user' -e cre_usr='create_user'"
				playbookTags	= "create_user"
				plybkExecution(extras,playbookTags,'Create User')
			}
			if (addSudoers		== 'true') {
				extras 			= "-e group_name='" + groupName + "' -e add_sudo='add_sudoers'"
				playbookTags	= "add_sudoers"
				plybkExecution(extras,playbookTags,'Add Sudoers')
			}
			/*
			####################################
			Remove or Delete or Revoke Playbooks
			####################################
			*/
			if (pgsqlUnInstall 	== 'true') {
				extras 			= "-e uin_pgsql='pgsql_uninstall'"
				playbookTags	= "pgsql_uninstall"
				plybkExecution(extras,playbookTags,'PgSQL Un-Installation')
			}
			if (listUnInstall 	== 'true') {
				extras 			= "-e uin_all='list_uninstall'"
				playbookTags	= "list_uninstall"
				plybkExecution(extras,playbookTags,'Group Un-Installation')
			}
			if (packerUnInstall == 'true') {
				extras 			= "-e uin_packer='packer_uninstall'"
				playbookTags	= "packer_uninstall"
				plybkExecution(extras,playbookTags,'Packer Un-Installation')
			}
			if (tfUnInstall 	== 'true') {
				extras 			= "-e uin_tf='terraform_uninstall'"
				playbookTags	= "terraform_uninstall"
				plybkExecution(extras,playbookTags,'Terraform Un-Installation')
			}
			if (removeSudoers	== 'true') {
				extras 			= "-e group_name='" + groupName + "' -e del_sudo='remove_sudoers'"
				playbookTags	= "remove_sudoers"
				plybkExecution(extras,playbookTags,'Remove Sudoers')
			}
			if (deleteUser		== 'true') {
				extras 			= "-e username='" + username + "' -e del_usr='delete_user'"
				playbookTags	= "delete_user"
				plybkExecution(extras,playbookTags,'Delete User')
			}
			if (deleteGroup		== 'true') {
				extras 			= "-e group_name='" + groupName + "' -e del_grp='delete_group'"
				playbookTags	= "delete_group"
				plybkExecution(extras,playbookTags,'Delete Group')
			}
		}
	}
}

def checkout() {
	checkout([
		$class								: 'GitSCM', 
		branches							: [[name	: gitBranch ]], 
		doGenerateSubmoduleConfigurations	: false, 
		clearWorkspace						: true,
		extensions							: [[$class	: 'CleanCheckout' ],[
			$class					: 'SubmoduleOption', 
			disableSubmodules		: false,
			parentCredentials		: true,
			recursiveSubmodules		: true,
			reference				: '',
			trackingSubmodules		: false]],
		submoduleCfg				: [],
		userRemoteConfigs			: [[credentialsId: gitCreds, url: gitRepo]]])
}

def plybkExecution(extras,playbookTags,stageName) {
	dir(scriptsDirectory) {
		/*
		#Playbook execution without using plugin block
		if (extraVars) {
			stage(stageName) {
				sh "/usr/bin/ansible-playbook ${playbook} --inventory ${inventory} --tags=${playbookTags} --extra-vars '${extraVars}'"
			}
		}
		*/
		if (extras) {
			stage(stageName + ' with ExVars') {
				ansiblePlaybook(
					playbook        : "${playbook}",
					inventory		: "${inventory}",
					tags            : playbookTags,
					extras			: extras,
					credentialsId	: "${SVC_ACC}",
					colorized       : true
				)
			}
		}
		else {
			stage(stageName + ' without ExVars') {
				ansiblePlaybook(
					playbook        : "${playbook}",
					inventory		: "${inventory}",
					tags            : playbookTags,
					credentialsId	: "${SVC_ACC}",
					colorized       : true
				)
			}
		}
	}
}
