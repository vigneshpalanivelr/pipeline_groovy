/*
*	hostname
*	SVC_ACC
*	python2
*	python3
*	git-core
*	pip2
*	pip3
*	ansible

*	installPlan
*/

node ('master') {
	withCredentials([usernamePassword(credentialsId: SVC_ACC, usernameVariable:'SVC_USER', passwordVariable: 'SVC_PASS')]) {
	stage("Log Dir's") {
		println "Checking : Log Directory /usr/local/sbin/custome-scripts/logs/"
		sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -d '/usr/local/sbin/custome-scripts/logs/' ]]; then echo "Found : Log Path"; else sudo mkdir -p /usr/local/sbin/custome-scripts/logs/; fi'"
	}
	stage("Install Python2&3") {
		if (python2) {
			println "Checking : python2"
			sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -f '/bin/python' ]]; then echo "Found : Python"; elif [[ -f '/bin/python2' ]]; then echo "Found : Python2"; else if [[ $installPlan == 'false' ]]; then echo "Required : python2"; elif [[ $installPlan == 'true' ]]; then sudo yum install -y python2; fi;fi | sudo tee -a /usr/local/sbin/logs/postDeployment_log.txt'"
		}
		if (python3) {
			println "Checking : python3"
			sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'for i in `ls -R /bin/python3*`; do if [[ "$i" == *python3* ]]; then echo "$i : Found"; else if [[ $installPlan == 'false' ]]; then echo "Required : python3"; elif [[ $installPlan == 'true' ]]; then sudo yum install -y python3; fi;fi; done | sudo tee -a /usr/local/sbin/postDeployment_log.txt'"
		}
	}
	stage("Install git-core") {
		if (git-core) {
			println "Checking : python2"
			sh "sshpass -p '${SVC_PASS}' ssh -l '${SVC_USER}' -o StricthostKeyChecking=no $hostname 'if  [[ -f '/usr/bin/git' ]]; then echo "Found : Git"; else if [[ $installPlan == 'false' ]]; then echo "Required : Git"; elif [[ $installPlan == 'true' ]]; then sudo yum install -y git-core; fi;fi | sudo tee -a /usr/local/sbin/logs/postDeployment_log.txt'"
		}
	}	
}
