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
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
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
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
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
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
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
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
				}
			}
		}
		listView('sg') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/tf-sg.*/)
				}
				columns {
					status()
					weather()
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
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
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
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
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
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
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
				}
			}
		}
		listView('cw') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/tf-cw.*/)
				}
				columns {
					status()
					weather()
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
				}
			}
		}
		listView('s3') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/tf-s3.*/)
				}
				columns {
					status()
					weather()
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
				}
			}
		}
		listView('sns') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/tf-sns.*/)
				}
				columns {
					status()
					weather()
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
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
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
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
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
				}
			}
		}
	}
}
nestedView('playbooks') {
	views {
		listView('ansible-playbooks') {
			views {
				description('Ansible Playbooks')
				jobs {
					regex(/playbook-.*job/)
				}
				columns {
					status()
					weather()
					buildButton()
					name()
					lastSuccess()
					lastFailure()
					lastDuration()
				}
			}
		}
	}
}
