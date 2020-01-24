def terraformRepo				= "https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def terraformBranch				= "master"
def gitCreds					= "gitCreds"
def awsAccount					= "210315133748"
def tfStateBucket				= "terraform-tfstate-mumba-1"
def tfStateBucketPrefixRDS		= "rds_module"
def tfStateBucketPrefixR53		= "r53_module"
def tfStateBucketPrefixR53ac	= "r53ac_module"
def tfStateBucketPrefixKMS		= "kms_module"
def tfStateBucketPrefixENI		= "eni_module"
def tfStateBucketPrefixEBS		= "ebs_module"

// RDS DB Build Generic Job
pipelineJob('tf-rds-db-build-1-job') {
	description('''Building AWS RDS Instances 1) PostgreSQL 2) Oracle 3) MySQL 4) MariaDB <br><br>Instructions for Creating:
	<br>&emsp 1) Creates Master Instance&emsp&emsp&emsp&emsp&emsp&emsp&emsp&emsp&ensp TF-STATE : InstanceId.tfstate
	<br>&emsp 2) Creates Master Route53 DNS&emsp&emsp&emsp&emsp&emsp&emsp&nbsp TF-STATE : Route53-dns.tfstate
	<br>&emsp 3) Creates Slave Instance&emsp&emsp&emsp&emsp&emsp&emsp&emsp&emsp&emsp&nbsp TF-STATE : InstanceId.tfstate
	<br>&emsp 4) Creates Replica Route53 DNS&emsp&emsp&emsp&emsp&emsp&emsp TF-STATE : Route53-dns.tfstate
	<br>Instructions for Destroying (Imp : Reverse Order):
	<br>&emsp 4) Destroy Replica Route53 DNS&emsp&emsp&emsp&emsp&emsp&emsp TF-STATE : Route53-dns.tfstate
	<br>&emsp 3) Destroy Slave Instance&emsp&emsp&emsp&emsp&emsp&emsp&emsp&emsp&emsp&nbsp TF-STATE : InstanceId.tfstate
	<br>&emsp 2) Destroy Master Route53 DNS&emsp&emsp&emsp&emsp&emsp &emsp TF-STATE : Route53-dns.tfstate
	<br>&emsp 1) Destroy Master Instance&emsp&emsp&emsp&emsp&emsp&emsp&emsp&emsp&ensp&nbsp TF-STATE : InstanceId.tfstate
	''')
	logRotator(-1,-1)
	parameters{
		choiceParam('gitRepo'					, [terraformRepo]				, '')
		choiceParam('gitBranch'					, [terraformBranch]				, '')
		choiceParam('gitCreds'					, [gitCreds]					, '')
		choiceParam('awsAccount'				, [awsAccount]					, '')
		choiceParam('tfstateBucket'				, [tfStateBucket]				, 'TF State Bucket'             	)
		choiceParam('tfstateBucketPrefixRDS'	, [tfStateBucketPrefixRDS]		, 'TF State Bucket Prefix - RDS'	)
		choiceParam('tfstateBucketPrefixDNS'	, [tfStateBucketPrefixR53ac]	, 'TF State Bucket Prefix - DNS'	)
		stringParam('db_family'					, 'postgres9.6,oracle-se1-11.2'	, '')
		stringParam('db_engine'					, 'postgres,oracle-se1'			, '')
		stringParam('db_engine_version'			, '9.6.11,11.2.0.4.v21'			, '')
		choiceParam('db_instance_class'			, ['db.t2.small','db.t2.micro']	, '')
		stringParam('db_identifier'				, 'test-instance'				, '''Name : name-(pgsql|oracle|mysql|mariadb)-rds + rr<br>
		TF-STATE : Statefile for Instance<br>
		db_identifier.tfstate''')
		choiceParam('db_name'					, ['DBNAME']					, '')
		choiceParam('db_username'				, ['Administrator']				, '')
		nonStoredPasswordParam('db_password'	, 'Do you think that you can see !!')
		choiceParam('db_allocated_storage'		, ['10']						, 'in GBs'						)
		choiceParam('db_multi_az'				, ['false','true']				, '')
		choiceParam('db_apply_changes'			, ['true','false']				, '')
		choiceParam('db_availability_zone'		, ['ap-south-1a','ap-south-1c']	, 'either a or b'				)
		choiceParam('db_action'					, ['master','replica','promote'	,'promote-as-master'], ''		)
		choiceParam('includeInstance'			, ['true','false']				, '')
		stringParam('db_source_identifier'		, 'test-instance'				, 'source instance to replicate')
		stringParam('db_route53_name'			, 'test-instance'				, '''TF-STATE : Statefile for Route53 Name<br>
		db_route53_name-dns.tfstate''')
		choiceParam('includeInstanceDNS'		, ['false','true']				, '')
		choiceParam('terraformApplyPlan'		, ['plan','apply','plan-destroy','destroy']	, '''
		<br>&emsp plan&emsp&emsp&emsp&emsp: only plan to create 
		<br>&emsp apply&emsp&emsp&emsp&ensp: will apply above plan 
		<br>&emsp plan-destroy&nbsp&nbsp: only plan to destroy
		<br>&emsp destroy&emsp&emsp&ensp&nbsp: will apply above plan-destroy''') 
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/tf-rds-db-build-1.groovy'))
			sandbox()
		}
	}
}

// Route53 Zone Creation
pipelineJob('tf-route53-zone-build-1-job') {
	description('Building AWS Route53 Zone Creation')
	logRotator(-1,-1)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]			, '')
		choiceParam('gitBranch'				, [terraformBranch]			, '')
		choiceParam('gitCreds'				, [gitCreds]				, '')
		choiceParam('awsAccount'			, [awsAccount]				, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]			, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'	, [tfStateBucketPrefixR53]	, 'TF State Bucket Prefix'      )
		stringParam('r53_zone_name'			, 'vignesh-private.zone.com', '')
		stringParam('vpc_name'				, 'Default_VPC'				, '')
		choiceParam('includeR53Zone'		, ['true','false']			, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/tf-route53-zone-build-1.groovy'))
			sandbox()
		}
	}
}

// Route53 A-record and CNAME Creation
pipelineJob('tf-route53ac-record-build-1-job') {
	description('Building AWS Route53 Record Creation')
	logRotator(-1,-1)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		choiceParam('gitBranch'				, [terraformBranch]				, '')
		choiceParam('gitCreds'				, [gitCreds]					, '')
		choiceParam('awsAccount'			, [awsAccount]					, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]				, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'	, [tfStateBucketPrefixR53ac]	, 'TF State Bucket Prefix'      )
		stringParam('r53_zone_name'			, 'vignesh-private-zone'		, 'zone name'					)
		stringParam('r53_record_name'		, 'postgres-r53,ec2-r53'		, 'route53 name'				)
		stringParam('r53_records'			, ''							, 'ip-address | end-point'		)
		choiceParam('r53_record_type'		, ['A','CNAME']					, 'A : ip-address | CNAME : end-point')
		choiceParam('includeR53acRecord'	, ['true','false']				, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/tf-route53ac-record-build-1.groovy'))
			sandbox()
		}
	}
}

// AWS KMS Key Creation
pipelineJob('tf-kms-key-build-1-job') {
	description('Building AWS KMS key creation')
	logRotator(-1,-1)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		choiceParam('gitBranch'				, [terraformBranch]				, '')
		choiceParam('gitCreds'				, [gitCreds]					, '')
		choiceParam('awsAccount'			, [awsAccount]					, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]				, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'	, [tfStateBucketPrefixKMS]		, 'TF State Bucket Prefix'      )
		stringParam('kms_key_name'			, 'custome-key'					, '')
		choiceParam('includeKMSKey'			, ['true','false']				, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/tf-kms-key-build-1.groovy'))
			sandbox()
		}
	}
}

// AWS ENI Creation
pipelineJob('tf-eni-build-1-job') {
	description('Building AWS ENI creation')
	logRotator(-1,-1)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		choiceParam('gitBranch'				, [terraformBranch]				, '')
		choiceParam('gitCreds'				, [gitCreds]					, '')
		choiceParam('awsAccount'			, [awsAccount]					, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]				, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'	, [tfStateBucketPrefixENI]		, 'TF State Bucket Prefix'      )
		stringParam('subnet'				, 'default-1'					, 'Subnet Name'					)
		stringParam('instance_name'			, 'test-instance'				, '')
		choiceParam('includeENI'			, ['true','false']				, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/tf-eni-build-1.groovy'))
			sandbox()
		}
	}
}

// AWS EBS Creation
pipelineJob('tf-ebs-build-1-job') {
	description('Building AWS EBS Volume creation')
	logRotator(-1,-1)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		choiceParam('gitBranch'				, [terraformBranch]				, '')
		choiceParam('gitCreds'				, [gitCreds]					, '')
		choiceParam('awsAccount'			, [awsAccount]					, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]				, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'	, [tfStateBucketPrefixEBS]		, 'TF State Bucket Prefix'      )
		stringParam('ebs_name'				, ''							, '')
		choiceParam('ebs_availability_zone'	, ['ap-south-1a','ap-south-1c']	, 'EBS Availability Zone'		)
		stringParam('ebs_size'				, ''							, 'in GB')
		choiceParam('ebs_type'				, ['gp2','standard','io1','sc1','st1']		, '')
		choiceParam('includeEBS'			, ['true','false']				, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/tf-ebs-build-1.groovy'))
			sandbox()
		}
	}
}
