job("admin-seed-job") {
	description('Job to create Admin Seed Jobs')
	parameters {
		choiceParam("job_dsl_repo"	, ["https://github.com/vigneshpalanivelr/pipeline_groovy.git"]	, "Job DSL Repo")
		choiceParam("job_dsl_branch"	, ["master"]							, "Job DSL Branch")
		choiceParam("job_dsl_repo_cred"	, ["GitCred"]							, "Job DSL Cred")
		choiceParam("job_dsl_path"	, ["jobs/**/*.groovy"]						, "Location of Job DSL Groovy Script")
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
