def userInput	= true
def didTimeout	= false

try {
    // change to a convenient timeout for you
    timeout(time: 15, unit: 'SECONDS') {
        userInput = input(
        id: 'Proceed1', message: 'Was this successful?', parameters: [
        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Please confirm you agree with this']
        ])
    }
}
catch(err) { 
    // timeout reached or input false
    echo "${err}"
    cause = err.causes.get(0)
    echo "${cause}"
    if (cause instanceof org.jenkinsci.plugins.workflow.support.steps.input.Rejection){
        def user = err.getCauses()[0].getUser()
        echo "${user}"
        if('SYSTEM' == user.toString()) {
            // SYSTEM   means timeout 
            // USERNAME means aborted (any)
            didTimeout = true
        }
        else {
            userInput = false
            echo "Aborted by: [${user}]"
        }
    else {
        println('error inside FlowInterruptedException: ' + err)
    }
}

node {
    if (didTimeout) {
        echo "No input was received : TIMEOUT"
        currentBuild.result = 'ABORTED'
    } 
    else if (userInput == true) {
        echo "Successful"
    } 
    else if (userInput == false) {
        echo "Input received as ABORT"
        currentBuild.result = 'ABORTED'
    }
    else {
        echo "Build Failed"
        currentBuild.result = 'FAILURE'
    }
}
