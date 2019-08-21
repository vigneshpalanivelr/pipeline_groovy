job("Jenkins_Maven_Sonar_Artifactory_Email_Build_Job") {
	parameters {
		choiceParam("job_dsl_repo"	, ["https://github.com/vigneshpalanivelr/terraform_practice_codes.git"]	, "Job DSL Repo")
		choiceParam("job_dsl_branch"	, ["master"]								, "Job DSL Branch")
		choiceParam("job_dsl_repo_cred"	, ["GitCred"]								, "Job DSL Cred")
	}
	scm {
        	git {
			branch("\$job_dsl_branch")
			remote {
			name("origin")
                	url("\$job_dsl_repo")
                	credentials("GitCred")
            		}
        	}
    	}
}
