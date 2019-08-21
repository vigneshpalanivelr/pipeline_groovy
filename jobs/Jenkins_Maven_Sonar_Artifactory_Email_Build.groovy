def jobDslRepo		=	"https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def jobDslBranch	=	"master"
def jobDslRepoCred	=	"GitCred"

job("Jenkins_Maven_Sonar_Artifactory_Email_Build_Job") {
	parameters {
		choiceParam("job_dsl_repo"	, [jobDslRepo]		, "Job DSL Repo")
		choiceParam("job_dsl_branch"	, [jobDslBranch]	, "Job DSL Branch")
		choiceParam("job_dsl_repo_cred"	, [jobDslRepoCred]	, "Job DSL Cred")
	}
	scm {
        	git {
			branch("\$job_dsl_branch")
			remote {
			name("origin")
                	url("\$job_dsl_repo")
                	credentials(jobDslRepoCred)
            		}
        	}
    	}
}
