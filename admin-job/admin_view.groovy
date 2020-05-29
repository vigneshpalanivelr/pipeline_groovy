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
		listView('rds-database') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-rds-.*/)
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
		listView('route53') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-r53.*/)
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
		listView('kms-keys') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-kms.*/)
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
		listView('security-group') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-sg.*/)
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
		listView('network-interface') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-eni.*/)
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
		listView('block-devices') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-ebs.*/)
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
		listView('ec2-compute') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-ec2.*/)
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
		listView('cloud-watch') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-cw.*/)
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
		listView('s3-storage') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-s3.*/)
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
		listView('sns-notification') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-sns.*/)
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
		listView('lambda') {
			views {
				description('All Terraform Jobs')
				jobs {
					regex(/terraform-lambda.*/)
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
		listView('z-practice') {
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
