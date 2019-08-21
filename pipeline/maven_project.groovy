node ('master') {
	writeFile(file:	"git-askpass-${BUILD_TAG}", text:"#!/bin/bash\ncase \"\$1\" in \nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) echo \"\${STASH_PASSWORD}\" ;;\nesac")
	sh "chmod a+x git-askpass-${BUILD_TAG}"
	withEnv(["GIT_ASKPASS=${WORKSPACE}/git-askpass-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: 'GitCred',passwordVariable: 'STASH_PASSWORD',usernameVariable: 'STASH_USERNAME')]) {
		echo STASH_PASSWORD
		sh 'echo $STASH_USERNAME'
		}
	}
}
node ('master') {
	withCredentials([usernamePassword(credentialsId: 'GitCred',passwordVariable: 'STASH_PASSWORD',usernameVariable: 'STASH_USERNAME')]) {
	echo STASH_PASSWORD
	sh 'echo $STASH_USERNAME'
	}
}
