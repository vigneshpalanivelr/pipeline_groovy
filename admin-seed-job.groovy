/*
1)	Create a Freestyle Job
2)	Add all the JOB DSL scripts
*/

def pipelineGroovyStack		=	"https://github.com/vigneshpalanivelr/pipeline_groovy.git"
def pipelineGroovyBranch	=	"master"
def GitCred			= 	"GitCred"
def groovyPath			=	"jobs/**/*.groovy"

job("admin-seed-job") {
	description('Job To Create All Admin Seed Jobs')
	parameters {
		choiceParam("job_dsl_repo"	, [pipelineGroovyStack]	, "Job DSL Repo")
		choiceParam("job_dsl_branch"	, [pipelineGroovyBranch], "Job DSL Branch")
		choiceParam("job_dsl_repo_cred"	, [GitCred]		, "Job DSL Cred")
		choiceParam("job_dsl_path"	, [groovyPath]		, "Location of Job DSL Groovy Script")
	}
	scm {
        	git {
			branch("\$job_dsl_branch")
			remote {
				name("origin")
 				url("\$job_dsl_repo")
				credentials(GitCred)
			}
		}
    }
	steps {
		dsl {
			external("\$job_dsl_path")
			ignoreExisting(false)
			removeAction("DELETE")
			removeViewAction("DELETE")
			lookupStrategy("JENKINS_ROOT")
		}
	}
}
