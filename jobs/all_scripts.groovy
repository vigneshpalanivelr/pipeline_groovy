def scriptsRepo     = "https://github.com/vigneshpalanivelr/all_scripts.git"
def scriptsBranch   = "master"
def gitCreds        = "gitCreds"

// RDS DB Build Generic Job
pipelineJob('playbook-provisioning-job') {
	logRotator (-1,-1)
	parameters {
		choiceParam('gitRepo'					, [terraformRepo]							, '')
		choiceParam('gitBranch'					, [terraformBranch]							, '')
		choiceParam('gitCreds'					, [gitCreds]								, '')
		choiceParam('scriptType'				, ['select','ansible','python','pgsql']		, '')
		stringParam('playbook'					, 'site.yml'								, '')
		stringParam('playbookTags'				, ''										, '')
    }
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/playbook-provisioning.groovy'))
			sandbox()
		}
	}
}
