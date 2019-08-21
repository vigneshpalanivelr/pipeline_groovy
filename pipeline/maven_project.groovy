node ('master') {
	writeFile(file:	"git-askpass-${BUILD_TAG}", text:"#!/bin/bash\ncase \"\$1\" in \nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) echo \"\${STASH_PASSWORD}\" ;;\nesac")
	sh "chmod a+x git-askpass-${BUILD_TAG}"
	withEnv(["GIT_ASKPASS=${WORKSPACE}/git-askpass-${BUILD_TAG}"]){
		withCredentials([usernamePassword(credentialsId: 'gitCred',passwordVariable: 'STASH_PASSWORD',usernameVariable: 'STASH_USERNAME')]) {
		echo $STASH_PASSWORD
		echo $STASH_USERNAME
		}
	}
}
