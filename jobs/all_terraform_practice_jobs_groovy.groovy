def terraformRepo	=	"https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def terraformBranch	=	"master"
def gitCreds		=	"GitCreds"

pipelineJob("practice-1.1-how-to-use-jenkins-credentials-build-job") {
	description ('Explains how to use Credentials for Build Jobs')
	logRotator(-1,-1)
	parameters {
		choiceParam("gitRepo"	, [terraformRepo]	, "Job DSL Repo")
		choiceParam("gitBranch"	, [terraformBranch]	, "Job DSL Branch")
		choiceParam("gitCreds"	, [gitCreds]		, "Job DSL Cred")
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/1.1-how_to_use_jenkins_credentials.groovy'))
			sandbox()
		}
	}
}

pipelineJob("practice-2.1-how-to-use-jenkins-approval-build-job") {
	description('Explains how to use Jenins Approval for Build Jobs')
	logRotator(-1,-1)
	parameters{
		choiceParam("gitRepo"	, [terraformRepo]	, "Job DSL Repo")
		choiceParam("gitBranch"	, [terraformBranch]	, "Job DSL Branch")
		choiceParam("gitCreds"	, [gitCreds]		, "Job DSL Cred")
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/2.1-how_to_use_jenkins_approval.groovy'))
			sandbox()
		}
	}
}

pipelineJob("practice-3.1-how-to-git-clone-checkout-build-job") {
	description('Explains how to use Jenins Approval for Build Jobs')
	logRotator(-1,-1)
	parameters{
		choiceParam("gitRepo"	, [terraformRepo]	, "Job DSL Repo")
		choiceParam("gitBranch"	, [terraformBranch]	, "Job DSL Branch")
		choiceParam("gitCreds"	, [gitCreds]		, "Job DSL Cred")
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/3.1-how_to_git_clone_checkout.groovy'))
			sandbox()
		}
	}
}
//============================== Testing ============================
pipelineJob("test") {
	description('Explains how to use Jenins Approval for Build Jobs')
	logRotator(-1,-1)
	parameters{extendedChoice(
		name			: "favorite_letters",
		type			: "PT_CHECKBOX",
		multiSelectDelimiter	: " ", 
		value			: """a, b, c, d, e, f""",
		defaultValue		: "d, f",
		description		: "Select your favorite letter(s)"
	)}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/3.1-how_to_git_clone_checkout.groovy'))
			sandbox()
		}
	}
}
