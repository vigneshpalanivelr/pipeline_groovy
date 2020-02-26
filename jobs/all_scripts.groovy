def scriptsRepo     = "https://github.com/vigneshpalanivelr/all_scripts.git"
def scriptsBranch   = "master"
def gitCreds        = "gitCreds"
def SVC_ACC			= "SVC_ACC"

// RDS DB Build Generic Job
pipelineJob('playbook-provisioning-job') {
	logRotator (-1,-1)
	parameters {
		choiceParam('gitRepo'					, [scriptsRepo]								, '')
		choiceParam('gitBranch'					, [scriptsBranch]							, '')
		choiceParam('gitCreds'					, [gitCreds]								, '')
		choiceParam('scriptType'				, ['select','ansible','python','pgsql']		, '')
		stringParam('playbook'					, 'site.yml'								, '')
		stringParam('inventory'					, 'inventory'								, '')
		stringParam('extras'					, ''										, '')
		stringParam('playbookTags'				, ''										, '')
    }
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/playbook-provisioning.groovy'))
			sandbox()
		}
	}
}

//post-provision-installation
pipelineJob("post-provision-installation") {
	description ('Installing Python | Git | ')
	logRotator(-1,-1)
	parameters {
		stringParam('hostname'	, ''			, 'New EC2 IP')
		choiceParam('SVC_ACC'	, [SVC_ACC]		, '')
		booleanParam('python2'	, true			, '')
		booleanParam('git-core'	, true			, '')
		booleanParam('pip2'		, true			, '')
		booleanParam('ansible'	, true			, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/post-provision-installation.groovy'))
			sandbox()
		}
	}
}
