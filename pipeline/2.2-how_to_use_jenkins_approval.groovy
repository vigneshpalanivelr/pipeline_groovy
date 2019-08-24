node('master'){
  stage('Approval - 2') {
    echo "Approval waiting"
    approval()
    echo "Approval done"
  }
  stage('echo') {
    echo "Approved"
  }
}

def approval() {
  timeout(time: 15, unit: 'SECONDS'){
    // Every input step has an unique ID. It is used in the generated URL to proceed or abort.
    input(id: 'Proceed1', message: 'Was this successful?', parameters: [
      [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Please confirm you agree with this']
      ])
  }
}
