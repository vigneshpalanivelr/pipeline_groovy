/*
*	hostname
*	SVC_ACC
*	scriptHomeDir
*	scriptsDir
*	logsDir
*	rpmDir

*	wget
*	epel
*	python2
*	python3
*	git
*	ansible
*	epelRepo
*	epel7RPM
*	pythonPipPack

*	installPlan
*/

node ('master') {
    sh "ssh-keygen -R ${hostname}"
	def installationLog = "installation.log"
	
	
	withCredentials([usernamePassword(credentialsId: SVC_ACC, usernameVariable:'SVC_USER', passwordVariable: 'SVC_PASS')]) {
		stage("Log Dir's") {
			println "Checking : Log Directory ${scriptHomeDir}${scriptsDir}${logsDir}"
			sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -d '${scriptHomeDir}${scriptsDir}${logsDir}' ]]; then echo Found : Log Path; else sudo mkdir -p ${scriptHomeDir}${scriptsDir}${logsDir}; fi'"
		}
		stage("Install wget") {
			if (wget == 'true') {
				println "Checking : wget"
				//sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -f '/usr/bin/wget' ]]; then echo Found : wget; else if [[ $installPlan == 'false' ]]; then echo Required : wget; elif [[ $installPlan == 'true' ]]; then sudo yum install -y wget; fi;fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
				sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if rpm -qa | grep wget*; then echo Found : wget; else if [[ $installPlan == 'false' ]]; then echo Required : wget; elif [[ $installPlan == 'true' ]]; then sudo yum install -y wget; fi;fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
			}
		}
		stage("Install epel-release") {
			if (epel == 'true') {
				println "Checking : epel-release"
				//sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if ! rpm -qa | grep epel-release; then sudo rpm -ivh ${scriptHomeDir}${scriptsDir}${rpmDir}${epel7RPM}; else sudo wget -c -P ${scriptHomeDir}${scriptsDir}${rpmDir} ${epelRepo}${epel7RPM}; sudo yum install -y epel-release; fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
				sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if rpm -qa | grep epel-release*; then echo \"Found : epel-release\"; sudo yum install -y epel-release; else sudo wget -c -P ${scriptHomeDir}${scriptsDir}${rpmDir} ${epelRepo}${epel7RPM}; sudo rpm -ivh ${scriptHomeDir}${scriptsDir}${rpmDir}${epel7RPM}; fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
			}
		}
		stage("Install Python2&3") {
			//println "Downloading Pip"
			//sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'sudo curl ${pythonPipPack} -o ${scriptHomeDir}${scriptsDir}${rpmDir}get-pip.py | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
			
			if (python2 == 'true') {
				println "Checking : python2"
				//sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -f '/bin/python' ]]; then echo Found : Python; elif [[ -f '/bin/python2' ]]; then echo Found : Python2; else if [[ $installPlan == 'false' ]]; then echo Required : python2; elif [[ $installPlan == 'true' ]]; then sudo yum install -y python2; fi;fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
				//sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'sudo python2 ${scriptHomeDir}${scriptsDir}${rpmDir}get-pip.py | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
				sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'sudo yum install -y python && sudo yum install -y python-pip | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
			}
			if (python3 == 'true') {
				println "Checking : python3"
				//sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'for i in `ls -R /bin/python3*`; do if [[ \$i == *python3* ]]; then echo Found : \$i; else if [[ $installPlan == 'false' ]]; then echo Required : python3; elif [[ $installPlan == 'true' ]]; then sudo yum install -y python3; fi;fi; done | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
				//sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'sudo python3 ${scriptHomeDir}${scriptsDir}${rpmDir}get-pip.py | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
				sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'sudo yum install -y python3 && sudo yum install -y python3-pip | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
			}
		}
		stage("Install git-core") {
			if (git == 'true') {
				println "Checking : Git"
				//sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -f '/usr/bin/git' ]]; then echo Found : Git; else if [[ $installPlan == 'false' ]]; then echo Required : Git; elif [[ $installPlan == 'true' ]]; then sudo yum install -y git-core; fi;fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
				sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if rpm -qa | grep git*; then echo Found : Git; else if [[ $installPlan == 'false' ]]; then echo Required : Git; elif [[ $installPlan == 'true' ]]; then sudo yum install -y git-core; fi;fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
			}
		}
		stage("Install Ansible") {
			if (ansible == 'true') {
				println "Checking : Ansible-Playbook"
				//sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -f '/usr/bin/ansible' ]]; then echo Found : Ansible; else if [[ $installPlan == 'false' ]]; then echo Required : Ansible; elif [[ $installPlan == 'true' ]]; then sudo yum install -y ansible; fi;fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
				sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if rpm -qa | grep ansible*; then echo Found : Ansible; else if [[ $installPlan == 'false' ]]; then echo Required : Ansible; elif [[ $installPlan == 'true' ]]; then sudo yum install -y ansible; fi;fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
			}
		}
	}
}
