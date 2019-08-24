node ('master') {
	stage('with git-askpass file and case statement') {
		/*
		1)	Creating a File on each build that will echo USERNAME and PASSWORD and make as executable
		2)	Setting the file as ENV variable for GIT_ASKPASS
		3)	GIT_ASKPASS will be triggered whenever the submodule is invoked with one of the argument (username | password)
		4)	Any submodule will be authenticated
		
		Ref : https://git-scm.com/docs/gitcredentials
		Ref : https://github.com/jenkinsci/git-client-plugin/commit/62872bccaf3f0e02c663acfdd85be2b38bddf1e8
		*/
		
		writeFile(file:	"git-askpass-${BUILD_TAG}", text:"#!/bin/bash\ncase \"\$1\" in \nUsername*) echo \"\${STASH_USERNAME}\" ;;\nPassword*) echo \"\${STASH_PASSWORD}\" ;;\nesac")
		sh "chmod a+x git-askpass-${BUILD_TAG}"
		withEnv(["GIT_ASKPASS=${WORKSPACE}/git-askpass-${BUILD_TAG}"]){
			/*
			The following can be used as seperate module
			1)	This will search for provided credentialsId in Jenkins Credentials page
			2)	It will set all the available resources as Environment variables
			3)	This environment later can be accessed using shell commands for authentication
			4)	This can be created using pipeline snnipet generator
			
			Ref : https://wiki.jenkins.io/display/JENKINS/Credentials+Binding+Plugin
			Ref : https://support.cloudbees.com/hc/en-us/articles/203802500-Injecting-Secrets-into-Jenkins-Build-Jobs
			*/
			withCredentials([usernamePassword(credentialsId: 'GitCred',passwordVariable: 'STASH_PASSWORD',usernameVariable: 'STASH_USERNAME')]) {
			echo STASH_PASSWORD
			sh 'echo $STASH_USERNAME'
			}
		sh 'echo $GIT_ASKPASS'
		}
	}
}
