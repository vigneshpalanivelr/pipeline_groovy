nestedView('seed-jobs') {
	views {
		listView('seed-jobs') {
			views {
				description('All Seed Jobs')
				jobs {
					regex(/admin.*/)
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
					regex(/tf-rds-.*/)
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
		listView('r53') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/tf-route.*/)
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
		listView('kms') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/tf-kms.*/)
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
		listView('eni') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/tf-eni.*/)
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
		listView('ebs') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/tf-ebs.*/)
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
		listView('ec2') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/tf-ec2.*/)
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
nestedView('practice-jobs') {
	views {
		listView('practice') {
			views {
				description('All Jenkins Practice Jobs')
				jobs {
					regex(/practice-.*/)
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
nestedView('master-jobs') {
	views {
		listView('master-jobs') {
			views {
				description('Master Jobs')
				jobs {
					regex(/master-.*job/)
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
