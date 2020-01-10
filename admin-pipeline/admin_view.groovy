nestedView('seed-jobs') {
	views {
		listView('seed-jobs') {
			views {
				description('All Seed Jobs')
				jobs {
					regex('/admin-*')
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
					regex('/rds-*')
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
