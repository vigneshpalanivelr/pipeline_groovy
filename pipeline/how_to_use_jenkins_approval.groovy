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
    // echo "${err}"
    cause = err.causes.get(0)
    // echo "${cause}"
    if (cause instanceof org.jenkinsci.plugins.workflow.support.steps.input.Rejection){
        def user = err.getCauses()[0].getUser()
        if('SYSTEM' == user.toString()) {
            // SYSTEM   means timeout 
            // USERNAME means aborted (any)
            didTimeout = true
            echo "Aborted by: [${user}]"
        }
        else {
            userInput = false
            echo "Aborted by: [${user}]"
        }
    }
    else {
        println('error inside FlowInterruptedException: ' + err)
    }
}

node {
    if (didTimeout) {
        echo "No input was received : TIMEOUT"
        currentBuild.result = 'FAILURE'
    } 
    else if (userInput == false) {
        echo "Input received as : ABORT"
        currentBuild.result = 'ABORTED'
    }
    else {
        echo "Build Status : SUCCESS"
    } 
}
/*
Ref : https://support.cloudbees.com/hc/en-us/articles/226554067-Pipeline-How-to-add-an-input-step-with-timeout-that-continues-if-timeout-is-reached-using-a-default-value
Ref : https://support.cloudbees.com/hc/en-us/articles/230922428-Pipeline-How-to-add-an-input-step-that-continues-if-aborted-using-value
Ref : https://support.cloudbees.com/hc/en-us/articles/207406207-Avoid-script-approvals-with-a-Jenkins-Pipeline-Groovy-script
*/
