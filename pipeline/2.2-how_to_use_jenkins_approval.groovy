node('master'){
  stage('Approval - 2') {
    approval()
  }
  stage('echo') {
    echo "Approved"
  }
}

def approval() {
  timeout(time: 15, unit: 'SECONDS'){
    // Every input step has an unique ID. It is used in the generated URL to proceed or abort.
    input(id: "Deploy Gate", message: "Deploy ?", ok: 'Deploy')
  }
}
