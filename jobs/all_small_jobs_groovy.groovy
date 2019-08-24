def gitRepo		=	"https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def gitBranch		=	"master"
def gitCreds		=	"GitCred"

pipelineJob("1.1-how_to_use_jenkins_credentials_build_job") {
	description ('Explains how to use Credentials for Build Jobs')
	logRotator(-1,-1)
	parameters {
		choiceParam("job_dsl_repo"	, [gitRepo]	, "Job DSL Repo")
		choiceParam("job_dsl_branch"	, [gitBranch]	, "Job DSL Branch")
		choiceParam("job_dsl_repo_cred"	, [gitCreds]	, "Job DSL Cred")
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/1.1-how_to_use_jenkins_credentials.groovy'))
			sandbox()
		}
	}
}

pipelineJob("2.1-how_to_use_jenkins_approval_build_job") {
	description('Explains how to use Jenins Approval for Build Jobs')
	logRotator(-1,-1)
	parameters{
		choiceParam("job_dsl_repo"	, [gitRepo]	, "Job DSL Repo")
		choiceParam("job_dsl_branch"	, [gitBranch]	, "Job DSL Branch")
		choiceParam("job_dsl_repo_cred"	, [gitCreds]	, "Job DSL Cred")
        }
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/2.1-how_to_use_jenkins_approval.groovy'))
                        sandbox()
                }
	}
}

pipelineJob("3.1-how_to_git_clone_checkout_build_job") {
	description('Explains how to use Jenins Approval for Build Jobs')
	logRotator(-1,-1)
	parameters{
		choiceParam("job_dsl_repo"	, [gitRepo]	, "Job DSL Repo")
		choiceParam("job_dsl_branch"	, [gitBranch]	, "Job DSL Branch")
		choiceParam("job_dsl_repo_cred"	, [gitCreds]	, "Job DSL Cred")
        }
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/3.1-how_to_git_clone_checkout.groovy'))
                        sandbox()
                }
	}
}
