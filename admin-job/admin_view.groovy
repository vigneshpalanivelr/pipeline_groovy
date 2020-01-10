nestedView('seed-jobs') {
        views {
                listView('seed-jobs') {
                        views {
                                description('All Seed Jobs')
                                jobs {
                                        regex('/admin-*-job/')
                                }
                                columns {
                                        status()
                                        weather()
                                        name()
                                        lastSuccess()
                                        lastFailure()
                                        lastDuration()
                                        buildButton()
                                }
                        }
                }
        }
}
nestedView('terraform-jobs') {
        views {
                listView('rds') {
                        views {
                                description('All Terraform Jobs')
                                jobs {
                                        regex('/tf-rds-*-job/')
                                }
                                columns {
                                        status()
                                        weather()
                                        name()
                                        lastSuccess()
                                        lastFailure()
                                        lastDuration()
                                        buildButton()
                                }
                        }
                }
		listView('practice') {
			views {
				description('All Jenkins Practice Jobs')
				jobs {
					regex('/practice-*-job/')
				}
				columns {
					status()
					weather()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
					buildButton()
				}
			}
		}
        }
}

