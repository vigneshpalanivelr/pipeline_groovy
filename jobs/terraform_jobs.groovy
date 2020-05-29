//Ref : https://www.scmtechblog.net/2017/05/colour-formatting-jenkins-console.html
def terraformRepo					= "https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def gitCreds						= "gitCreds"
def awsAccount						= "210315133748"
def tfStateBucket					= "terraform-tfstate-mumba-1"

def tfStateBucketPrefixS3			= "s3_module"
def tfStateBucketPrefixS3Log		= "s3_log_module"
def tfStateBucketPrefixRDS			= "rds_module"
def tfStateBucketPrefixRDSRR		= "rds_replica_module"
def tfStateBucketPrefixR53			= "r53_module"
def tfStateBucketPrefixR53ac		= "r53ac_module"
def tfStateBucketPrefixKMS			= "kms_module"
def tfStateBucketPrefixSNS			= "sns_module"
def tfStateBucketPrefixSG			= "sg_module"
def tfStateBucketPrefixSGRule		= "sg_rule_module"
def tfStateBucketPrefixENI			= "eni_module"
def tfStateBucketPrefixEBS			= "ebs_module"
def tfStateBucketPrefixEBSAttach	= "ebs_attachment_module"
def tfStateBucketPrefixEC2			= "ec2_module"
def tfStateBucketPrefixEC2CW		= "cw_module"
def tfStateBucketPrefixLambda		= "lambda_module"

// RDS DB Build Generic Job
pipelineJob('terraform-rds-db-job') {
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
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'					, [terraformRepo]				, '')
		stringParam('gitBranch'					, 'master'				        , '')
		choiceParam('gitCreds'					, [gitCreds]					, '')
		choiceParam('awsAccount'				, [awsAccount]					, '')
		choiceParam('tfstateBucket'				, [tfStateBucket]				, 'TF State Bucket'             	)
		choiceParam('tfStateBucketPrefixSG'		, [tfStateBucketPrefixSG]		, 'TF State Bucket Prefix - SG'		)
		choiceParam('tfStateBucketPrefixSGR'	, [tfStateBucketPrefixSGRule]	, 'TF State Bucket Prefix - SGR'	)
		choiceParam('tfstateBucketPrefixRDS'	, [tfStateBucketPrefixRDS]		, 'TF State Bucket Prefix - RDS'	)
		choiceParam('tfstateBucketPrefixRDSRR'	, [tfStateBucketPrefixRDSRR]	, 'TF State Bucket Prefix - RDS RR'	)
		stringParam('db_family'					, 'postgres9.6,oracle-se1-11.2'	, '')
		stringParam('db_engine'					, 'postgres,oracle-se1'			, '')
		stringParam('db_engine_version'			, '9.6.11,11.2.0.4.v21'			, '')
		choiceParam('db_instance_class'			, ['db.t2.micro','db.t2.small']	, '')
		stringParam('db_master_identifier'		, 'test-instance-rds'			, '''Name : name-(pgsql|oracle|mysql|mariadb)-rds + rr<br>
		TF-STATE : Statefile for Instance<br>
		db_identifier.tfstate''')
		stringParam('db_slave_identifier'		, 'test-instance-rds-rr'		, '')
		choiceParam('db_name'					, ['DBNAME']					, '')
		choiceParam('db_username'				, ['Administrator']				, '')
		nonStoredPasswordParam('db_password'	, 'Do you think that you can see !!')
		choiceParam('db_allocated_storage'		, ['10']						, 'in GBs'						)
		choiceParam('db_multi_az'				, ['false','true']				, '')
		choiceParam('db_apply_changes'			, ['true','false']				, '')
		stringParam('vpc_name'					, 'default-vpc'					, '')
		choiceParam('db_availability_zone'		, ['ap-south-1a','ap-south-1b','ap-south-1c'],				  '')
		stringParam('sg_group_name'				, 'test-instance-rds-sg'		, 'name + sg (by default)'		)
		choiceParam('includeSG'					, ['true','false']				, '')
		choiceParam('includeSGRule'				, ['true','false']				, '')
		choiceParam('includeInstance'			, ['master','slave','master-slave']							, '')
		choiceParam('terraformApplyPlan'		, ['plan','apply','plan-destroy','destroy']	, '''
		<br>&emsp plan&emsp&emsp&emsp&emsp: only plan to create 
		<br>&emsp apply&emsp&emsp&emsp&ensp: will apply above plan 
		<br>&emsp plan-destroy&nbsp&nbsp: only plan to destroy
		<br>&emsp destroy&emsp&emsp&ensp&nbsp: will apply above plan-destroy''') 
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-rds-db-pipeline.groovy'))
			sandbox()
		}
	}
}

// Route53 Zone Creation
pipelineJob('terraform-r53-zone-job') {
	description('Building AWS Route53 Zone Creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]			, '')
		stringParam('gitBranch'				, 'master'					, '')
		choiceParam('gitCreds'				, [gitCreds]				, '')
		choiceParam('awsAccount'			, [awsAccount]				, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]			, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'	, [tfStateBucketPrefixR53]	, 'TF State Bucket Prefix'      )
		stringParam('r53_zone_name'			, 'vignesh-private.zone.com', '')
		stringParam('vpc_name'				, 'default-vpc'				, '')
		choiceParam('includeR53Zone'		, ['true','false']			, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-route53-zone-pipeline.groovy'))
			sandbox()
		}
	}
}

// Route53 A-record and CNAME Creation
pipelineJob('terraform-r53-ac-record-job') {
	description('Building AWS Route53 Record Creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		stringParam('gitBranch'				, 'master'						, '')
		choiceParam('gitCreds'				, [gitCreds]					, '')
		choiceParam('awsAccount'			, [awsAccount]					, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]				, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'	, [tfStateBucketPrefixR53ac]	, 'TF State Bucket Prefix'      )
		stringParam('r53_zone_name'			, 'vignesh-private.zone.com'	, 'zone name'					)
		stringParam('r53_record_name'		, 'test-instance-ec2-r53'		, 'route53 name'				)
		stringParam('r53_records'			, ''							, 'ip-address | end-point'		)
		choiceParam('r53_record_type'		, ['A','CNAME']					, 'A : ip-address | CNAME : end-point')
		choiceParam('includeR53acRecord'	, ['true','false']				, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-route53-ac-record-pipeline.groovy'))
			sandbox()
		}
	}
}

// AWS S3 Bucket and S3 Log Bucket Creation
pipelineJob('terraform-s3-job') {
	description('Building AWS KMS key creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		stringParam('gitBranch'				, 'master'						, '')
		choiceParam('gitCreds'				, [gitCreds]					, '')
		choiceParam('awsAccount'			, [awsAccount]					, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]				, 'TF State Bucket'					)
		choiceParam('tfstateBucketPrefixS3'	, [tfStateBucketPrefixS3]		, 'TF State Bucket Prefix S3'		)
		choiceParam('tfstateBucketPrefixS3L', [tfStateBucketPrefixS3Log]	, 'TF State Bucket Prefix S3 Log'	)
		choiceParam('s3_versioning'			, ['true','false']				, '')
		stringParam('s3_bucket_name'		, 'terraform-tfstate-mum-bkt-1'	, '')
		choiceParam('includeS3Bucket'		, ['true','false']				, '')
		stringParam('s3_log_bucket_name'	, 'Can also used for normal bkt', '')
		choiceParam('includeS3LogBucket'	, ['true','false']				, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-s3-pipeline.groovy'))
			sandbox()
		}
	}
}

// AWS KMS Key Creation
pipelineJob('terraform-kms-key-job') {
	description('Building AWS KMS key creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		stringParam('gitBranch'				, 'master'						, '')
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
			script(readFileFromWorkspace('pipeline/terraform-kms-key-pipeline.groovy'))
			sandbox()
		}
	}
}

// AWS SNS Topic Creation
pipelineJob('terraform-sns-job') {
	description('Building AWS KMS key creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		stringParam('gitBranch'				, 'master'						, '')
		choiceParam('gitCreds'				, [gitCreds]					, '')
		choiceParam('awsAccount'			, [awsAccount]					, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]				, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'	, [tfStateBucketPrefixSNS]		, 'TF State Bucket Prefix'      )
		stringParam('sns_topic_name'		, 'test-sns-topic'				, '')
		choiceParam('sns_protocol'			, ['email','sms']				, '')
		choiceParam('sns_endpoint'			, ['vignesh1650@gmail.com']		, '')
		choiceParam('includeSNS'			, ['true','false']				, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-sns-pipeline.groovy'))
			sandbox()
		}
	}
}

// AWS SG Creation
pipelineJob('terraform-sg-job') {
	description('Building AWS SG creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		stringParam('gitBranch'				, 'master'						, '')
		choiceParam('gitCreds'				, [gitCreds]					, '')
		choiceParam('awsAccount'			, [awsAccount]					, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]				, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefixSG'	, [tfStateBucketPrefixSG]		, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixSGR', [tfStateBucketPrefixSGRule]	, 'TF State Bucket Prefix'      )
		stringParam('vpc_name'				, 'default-vpc'					, '')
		stringParam('sg_group_name'			, 'test-instance'				, 'name + sg (by default)'		)
		stringParam('resource_name'			, 'test-instance'				, 'SG Description'				)
		choiceParam('includeSG'				, ['true','false']				, '')
		choiceParam('includeSGRule'			, ['true','false']				, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-sg-pipeline.groovy'))
			sandbox()
		}
	}
}

// AWS ENI Creation
pipelineJob('terraform-eni-job') {
	description('Building AWS ENI creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		stringParam('gitBranch'				, 'master'						, '')
		choiceParam('gitCreds'				, [gitCreds]					, '')
		choiceParam('awsAccount'			, [awsAccount]					, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]				, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'	, [tfStateBucketPrefixENI]		, 'TF State Bucket Prefix'      )
		choiceParam('eni_subnet'			, ['default-subnet-1','default-subnet-2','default-subnet-3']	, 'ENI Subnet'	)
		stringParam('sg_group_name'			, 'test-instance'				, 'ENI Security Group'			)
		stringParam('resource_name'			, 'test-instance'				, '')
		choiceParam('includeENI'			, ['true','false']				, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-eni-pipeline.groovy'))
			sandbox()
		}
	}
}

// AWS EBS Creation
pipelineJob('terraform-ebs-job') {
	description('Building AWS EBS Volume creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'					, [terraformRepo]					, '')
		stringParam('gitBranch'					, 'master'							, '')
		choiceParam('gitCreds'					, [gitCreds]						, '')
		choiceParam('awsAccount'				, [awsAccount]						, '')
		choiceParam('tfstateBucket'				, [tfStateBucket]					, 'TF State Bucket'				)
		choiceParam('tfstateBucketPrefixEBS'	, [tfStateBucketPrefixEBS]			, 'TF State Bucket Prefix'		)
		choiceParam('tfstateBucketPrefixEBSA'	, [tfStateBucketPrefixEBSAttach]	, 'TF State Bucket Prefix'		)
		stringParam('resource_name'				, 'test-instance'					, '')
		stringParam('ebs_volume_count'			, '3'								, '')
		choiceParam('ebs_az'					, ['ap-south-1a','ap-south-1b','ap-south-1c']	, '')
		choiceParam('includeEBS'				, ['true','false']					, '')
		choiceParam('includeEBSAttach'			, ['true','false']					, '')
		choiceParam('terraformApplyPlan'		, ['plan','apply','plan-destroy','destroy']		, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-ebs-pipeline.groovy'))
			sandbox()
		}
	}
}

// AWS CW Alarm Configuration
pipelineJob('terraform-cw-job') {
	description('Building AWS EBS Volume creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'					, [terraformRepo]					, '')
		stringParam('gitBranch'					, 'master'							, '')
		choiceParam('gitCreds'					, [gitCreds]						, '')
		choiceParam('awsAccount'				, [awsAccount]						, '')
		choiceParam('tfstateBucket'				, [tfStateBucket]					, 'TF State Bucket'				)
		choiceParam('tfStateBucketPrefixCW'		, [tfStateBucketPrefixEC2CW]		, 'TF State Bucket Prefix'		)
		stringParam('resource_name'				, 'test-instance'					, '')
		choiceParam('cw_alarm'					, ['ec2','rds']						, '')
		choiceParam('includeCW'					, ['true','false']					, '')
		choiceParam('terraformApplyPlan'		, ['plan','apply','plan-destroy','destroy']		, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-cw-pipeline.groovy'))
			sandbox()
		}
	}
}

// AWS EC2 Creation
pipelineJob('terraform-ec2-job') {
	description('Building AWS EC2 creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'					, [terraformRepo]					, '')
		stringParam('gitBranch'					, 'master'							, '')
		choiceParam('gitCreds'					, [gitCreds]						, '')
		choiceParam('awsAccount'				, [awsAccount]						, '')
		choiceParam('tfstateBucket'				, [tfStateBucket]					, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefixSG'		, [tfStateBucketPrefixSG]			, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixSGR'	, [tfStateBucketPrefixSGRule]		, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixENI'	, [tfStateBucketPrefixENI]			, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixEBS'	, [tfStateBucketPrefixEBS]			, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixEC2'	, [tfStateBucketPrefixEC2]			, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixEBSA'	, [tfStateBucketPrefixEBSAttach]	, 'TF State Bucket Prefix'		)
		choiceParam('tfstateBucketPrefixEC2CW'	, [tfStateBucketPrefixEC2CW]		, 'TF State Bucket Prefix'      )
		stringParam('instance_name'				, 'test-instance'					, '')
		stringParam('vpc_name'					, 'default-vpc'						, '')
		stringParam('sg_group_name'				, 'test-instance'					, 'name + sg (by default)'		)
		stringParam('instance_type'				, 't2.micro'						, '')
		choiceParam('AZ'						, ['ap-south-1a','ap-south-1b','ap-south-1c']				, 'EBS | EC2')
		choiceParam('subnet'					, ['default-subnet-1','default-subnet-2','default-subnet-3'], 'ENI | EC2')
		stringParam('ebs_volume_count'			, '3'								, '')
		stringParam('ec2_ami_regex'				, 'RHEL-7.7'						, '')
		stringParam('ec2_ami_owner_id'			, '309956199498,734555027572'		, '''309956199498 : RHEL<br>734555027572 : CentOS''')
		choiceParam('root_user'					, ['vignesh']						, 'Login Cred')
		choiceParam('root_passwd'				, ['vignesh']						, 'Login Cred')
		choiceParam('includeSG'					, ['true','false']					, '')
		choiceParam('includeSGRule'				, ['true','false']					, '')
		choiceParam('includeENI'				, ['true','false']					, '')
		choiceParam('includeEBS'				, ['true','false']					, '')
		choiceParam('includeEC2'				, ['true','false']					, '')
		choiceParam('includeEBSAttach'			, ['true','false']					, '')
		choiceParam('includeCW'					, ['true','false']					, '')
		choiceParam('terraformApplyPlan'		, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-ec2-pipeline.groovy'))
			sandbox()
		}
	}
}

// AWS Lambda Creation
pipelineJob('terraform-lambda-job') {
	description('Building AWS ENI creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'				, [terraformRepo]				, '')
		stringParam('gitBranch'				, 'master'						, '')
		choiceParam('gitCreds'				, [gitCreds]					, '')
		choiceParam('awsAccount'			, [awsAccount]					, '')
		choiceParam('tfstateBucket'			, [tfStateBucket]				, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'	, [tfStateBucketPrefixLambda]	, 'TF State Bucket Prefix'      )
		stringParam('vpc_name'				, 'default-vpc'					, '')
		stringParam('sg_group_name'			, 'test-instance-sg'			, 'ENI Security Group'			)
		choiceParam('lambda_function'		, ['select','ec2_stop_scheduler','ec2_ss_delete_scheduler']	, '')
		choiceParam('includeLambda'			, ['true','false']				, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-lambda-pipeline.groovy'))
			sandbox()
		}
	}
}