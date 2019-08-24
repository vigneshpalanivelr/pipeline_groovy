def userInput	= true
def didTimeout	= false

try {
    // change to a convenient timeout for you
    timeout(time: 15, unit: 'SECONDS') {
        userInput = input(
        id: 'Proceed1', message: 'Was this successful?', parameters: [
        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Please confirm you agree with this']
        ])
        echo "${userInput}"
    }
} catch(err) { 
    // timeout reached or input false
    def user = err.getCauses()[0].getUser()
    echo "${user}"
    if('SYSTEM' == user.toString()) {
        // SYSTEM means timeout.
        didTimeout = true
        echo "${didTimeout}"
    } else {
        userInput = false
        echo "${userInput}"
        echo "Aborted by: [${user}]"
    }
}

node {
    if (didTimeout) {
        echo "no input was received before timeout"
    } else if (userInput == true) {
        echo "this was successful"
    } else {
        echo "this was not successful"
        currentBuild.result = 'FAILURE'
    } 
}

userAborted		= false
startMillis 	= System.currentTimeMillis()
timeoutMillis	= 10000

try { 
	timeout(time: timeoutMillis, unit: 'MILLISECONDS') {
		input 'Do you approve?' 
	} 
}
catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
	cause = e.causes.get(0)
	echo "${cause}"
	echo "Aborted by " + cause.getUser().toString()
	if (cause.getUser().toString() != 'SYSTEM') {
		startMillis = System.currentTimeMillis()
	}
	else {
		endMillis = System.currentTimeMillis()
		if (endMillis - startMillis >= timeoutMillis) {
			echo "Approval timed out. Continuing with deployment."
		}
		else {
			userAborted = true
			echo "SYSTEM aborted, but looks like timeout period didn't complete. Aborting."
		}
	}
}

if (userAborted) {
	currentBuild.result = 'ABORTED'
} 
else {
	currentBuild.result = 'SUCCESS'
	echo "Firing the missiles!"
}
