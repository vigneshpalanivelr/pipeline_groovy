/*
*	hostname
*	SVC_ACC
*	scriptHomeDir
*	scriptsDir
*	logsDir

*	python2
*	python3
*	git-core
*	pip2
*	pip3
*	ansible

*	installPlan
*/

node ('master') {
	def installationLog = "installation.log"
	
	withCredentials([usernamePassword(credentialsId: SVC_ACC, usernameVariable:'SVC_USER', passwordVariable: 'SVC_PASS')]) {
		stage("Log Dir's") {
			println "Checking : Log Directory ${scriptHomeDir}${scriptsDir}${logsDir}"
			sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -d '${scriptHomeDir}${scriptsDir}${logsDir}' ]]; then echo Found : Log Path; else sudo mkdir -p ${scriptHomeDir}${scriptsDir}${logsDir}; fi'"
		}
		stage("Install Python2&3") {
			if (python2) {
				println "Checking : python2"
				sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -f '/bin/python' ]]; then echo Found : Python; elif [[ -f '/bin/python2' ]]; then echo Found : Python2; else if [[ $installPlan == 'false' ]]; then echo Required : python2; elif [[ $installPlan == 'true' ]]; then sudo yum install -y python2; fi;fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
			}
			if (python3) {
				println "Checking : python3"
				sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'for i in `ls -R /bin/python3*`; do if [[ \$i == *python3* ]]; then echo \$i : Found; else if [[ $installPlan == 'false' ]]; then echo Required : python3; elif [[ $installPlan == 'true' ]]; then sudo yum install -y python3; fi;fi; done | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
			}
		}
		stage("Install git-core") {
			if (${git-core}) {
				println "Checking : python2"
				sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -f '/usr/bin/git' ]]; then echo Found : Git; else if [[ $installPlan == 'false' ]]; then echo Required : Git; elif [[ $installPlan == 'true' ]]; then sudo yum install -y git-core; fi;fi | sudo tee -a ${scriptHomeDir}${scriptsDir}${logsDir}${installationLog}'"
			}
		}
	}
}
