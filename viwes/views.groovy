nestedView('seed-jobs') {
	views {
		listView('seed-jobs') {
			description('All Seed Jobs')
			jobs {
				name('admin-seed-job')
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
