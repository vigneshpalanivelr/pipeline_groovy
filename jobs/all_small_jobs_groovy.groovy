def jobDslRepo		=	"https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def jobDslBranch	=	"master"
def jobDslRepoCred	=	"GitCred"

pipelineJob("how_to_use_jenkins_credentials_build_job") {
	description ('Explains how to use Credentials for Build Jobs')
	logRotator(-1,-1)
	parameters {
		choiceParam("job_dsl_repo"	, [jobDslRepo]		, "Job DSL Repo")
		choiceParam("job_dsl_branch"	, [jobDslBranch]	, "Job DSL Branch")
		choiceParam("job_dsl_repo_cred"	, [jobDslRepoCred]	, "Job DSL Cred")
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/how_to_use_jenkins_credentials.groovy'))
			//sandbox()
		}
	}
}

pipelineJob("how_to_use_jenkins_approval_build_job") {
	description('Explains how to use Jenins Approval for Build Jobs')
	logRotator(-1,-1)
	parameters{
                choiceParam("job_dsl_repo"      , [jobDslRepo]          , "Job DSL Repo")
                choiceParam("job_dsl_branch"    , [jobDslBranch]        , "Job DSL Branch")
                choiceParam("job_dsl_repo_cred" , [jobDslRepoCred]      , "Job DSL Cred")
        }
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/how_to_use_jenkins_approval.groovy'))
                        //sandbox()
                }
	}
}
