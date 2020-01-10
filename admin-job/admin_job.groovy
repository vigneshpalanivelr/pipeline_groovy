/*
1) Add GitHub Credentials in the Jenkins Credentials	(Name : gitCreds)
2) Create a Freestyle Job for 2 jobs

*	jobDSL_repo
*	jobDSL_branch
*	jobDSL_creds
*	jobDSL_path
*/

def pipelineGroovyStack		=	"https://github.com/vigneshpalanivelr/pipeline_groovy.git"
def pipelineGroovyBranch	=	"master"
def GitCreds			= 	"gitCreds"
def seedJobDSL			=	"admin-pipeline/admin_seed_job.groovy"
def jobDSL			=	"jobs/**/*.groovy"

//Admin Seed Job
pipelineJob("admin-seed-job") {
	description('Job To Create All Admin Seed Jobs')
	parameters {
		choiceParam("jobDSL_repo"	, [pipelineGroovyStack]	, "Job DSL Repo")
		choiceParam("jobDSL_branch"	, [pipelineGroovyBranch], "Job DSL Branch")
		choiceParam("jobDSL_creds"	, [GitCreds]		, "Job DSL Cred")
		choiceParam("jobDSL_path"	, [seedJobDSL]		, "Location of Job DSL Groovy Script")
		choiceParam("jobDSL_scripts"	, [jobDSL]		, "Location of Job DSL Groovy Script")
	}
	definition {
		cps {
			script(readFileFromWorkspace(seedJobDSL))
			sandbox()
		}
	}
}
