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
}
catch(err) { 
    // timeout reached or input false
    echo "${err}"
    cause = err.causes.get(0)
    echo "${cause}"
    if (cause instanceof org.jenkinsci.plugins.workflow.steps.TimeoutStepExecution.ExceededTimeout) {
      currentBuild.result = 'ABORTED'
      println('ExceededTimeout!')
      error('ExceededTimeout!') 
    } 
    else {
      error('error inside FlowInterruptedException: ' + err)
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
