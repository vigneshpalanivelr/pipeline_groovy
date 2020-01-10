/*
1) Add GitHub Credentials in the Jenkins Credentials	(Name : gitCreds)
2) Create a Freestyle Job					(Name : Master-Job)
3) It Generates 2 Seed Jobs
	1. admin-view
	2. admin-seed-job
*/

def pipelineGroovyStack		=	"https://github.com/vigneshpalanivelr/pipeline_groovy.git"
def pipelineGroovyBranch	=	"master"
def GitCreds			= 	"gitCreds"
def groovyPath			=	"jobs/**/*.groovy"

pipelineJob("admin-seed-job") {
	description('Job To Create All Admin Seed Jobs')
	parameters {
		choiceParam("job_dsl_repo"	, [pipelineGroovyStack]	, "Job DSL Repo")
		choiceParam("job_dsl_branch"	, [pipelineGroovyBranch], "Job DSL Branch")
		choiceParam("job_dsl_repo_cred"	, [GitCreds]		, "Job DSL Cred")
		choiceParam("job_dsl_path"	, [groovyPath]		, "Location of Job DSL Groovy Script")
	}
	scm {
        	git {
			branch("\$job_dsl_branch")
			remote {
				name("origin")
 				url("\$job_dsl_repo")
				credentials(\$job_dsl_repo_cred)
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
