def jobDslRepo		=	"https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def jobDslBranch	=	"master"
def jobDslRepoCred	=	"GitCred"

pipelineJob("Jenkins_Maven_Sonar_Artifactory_Email_Build_Job") {
	description ('Integrating Jenkins Maven SonarQube Artifactory Email Build Pipeline')
	logRotator(-1,-1)
	parameters {
		choiceParam("job_dsl_repo"	, [jobDslRepo]		, "Job DSL Repo")
		choiceParam("job_dsl_branch"	, [jobDslBranch]	, "Job DSL Branch")
		choiceParam("job_dsl_repo_cred"	, [jobDslRepoCred]	, "Job DSL Cred")
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/maven_project.groovy'))
			sandbox()
		}
	}
}
