nestedView('seed-jobs') {
	views {
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
nestedView('terraform-jobs') {
	views {
		nestedView('rds') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex('/rds-*-deploy')
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
