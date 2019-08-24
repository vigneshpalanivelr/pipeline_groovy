node('master'){
  stage('Approval - 2') {
    approval()
  }
}

def approval() {
  timeout(time: 15, unit: 'SECONDS'){
    // Every input step has an unique ID. It is used in the generated URL to proceed or abort.
    input id: 'Approval - 2', message: 'You want to Continue ?', ok: 'Continue'
  }
}
