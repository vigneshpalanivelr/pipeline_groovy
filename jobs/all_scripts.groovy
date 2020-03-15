def scriptsRepo     = "https://github.com/vigneshpalanivelr/all_scripts.git"
def scriptsBranch   = "master"
def gitCreds        = "gitCreds"
def SVC_ACC			= "SVC_ACC"
def scriptHomeDir	= "/usr/local/sbin/"
def scriptsDir		= "custom-scripts/"
def logsDir			= "logs/"
def rpmDir			= "rpms/"

def epelRepo		= "https://dl.fedoraproject.org/pub/epel/"
def pythonPipPack	= "https://bootstrap.pypa.io/get-pip.py"

// RDS DB Build Generic Job
pipelineJob('playbook-provisioning-job') {
	logRotator (-1,-1)
	parameters {
		choiceParam('gitRepo'					, [scriptsRepo]								, '')
		choiceParam('gitBranch'					, [scriptsBranch]							, '')
		choiceParam('gitCreds'					, [gitCreds]								, '')
		choiceParam('SVC_ACC'					, [SVC_ACC]									, '')
		choiceParam('scriptType'				, ['ansible','python','pgsql']				, '')
		stringParam('playbook'					, 'site.yml'								, '')
		stringParam('inventory'					, 'inventory'								, '')
		booleanParam('pgsqlInstall'				, false										, '')
		booleanParam('listInstall'				, true										, '')
		booleanParam('packerInstall'			, true										, '')
		booleanParam('tfInstall'				, true										, '')
		booleanParam('mountVolumes'				, false										, '')
		booleanParam('configCW'					, true										, '')
		booleanParam('setupCI'					, true										, '')
		booleanParam('createGroup'				, true										, '')
		stringParam('groupName'					, 'root_group'								, '')
		booleanParam('createUser'				, true										, '')
		stringParam('username'					, 'root_user'								, '')
		passwordParam('password'				,'')
		booleanParam('addSudoers'				, true										, '')
		booleanParam('pgsqlUnInstall'			, true										, '')
		booleanParam('listUnInstall'			, true										, '')
		booleanParam('packerUnInstall'			, true										, '')
		booleanParam('tfUnInstall'				, true										, '')
		booleanParam('removeSudoers'			, true										, '')
		booleanParam('deleteUser'				, true										, '')
		booleanParam('deleteGroup'				, false										, '')
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
		stringParam('hostname'		, ''					, 'New EC2 IP')
		choiceParam('SVC_ACC'		, [SVC_ACC]				, '')
		choiceParam('scriptHomeDir'	, [scriptHomeDir]		, '')
		choiceParam('scriptsDir'	, [scriptsDir]			, '')
		choiceParam('logsDir'		, [logsDir]				, '')
		choiceParam('rpmDir'		, [rpmDir]				, '')
		booleanParam('wget'			, true					, '')
		booleanParam('epel'			, true					, '')
		booleanParam('python2'		, true					, '')
		booleanParam('python3'		, true					, '')
		booleanParam('git'			, true					, '')
		booleanParam('ansible'		, true					, '')
		stringParam('epelRepo'		, epelRepo				, '')
		choiceParam('RHEL'			, ['6','7','8']			, '')
		stringParam('pythonPipPack'	, pythonPipPack			, '')
		choiceParam('installPlan'	, ['true','false']		, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/post-provision-installation.groovy'))
			sandbox()
		}
	}
}
