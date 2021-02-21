//Ref : https://www.scmtechblog.net/2017/05/colour-formatting-jenkins-console.html
def terraformRepo					= "https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def packerRepo                      = "https://github.com/vigneshpalanivelr/all_scripts.git"
def gitCreds						= "gitCreds"
def awsAccount						= "495710143902"
 
def tfStateBucket					= "terraform-tfstate-mumbai-1"
def logBucket						= "terraform-tfstate-mumba-1-bucket-1"

def tfStateBucketPrefixS3			= "s3_module"
def tfStateBucketPrefixS3Log		= "s3_log_module"
def tfStateBucketPrefixRDS			= "rds_module"
def tfStateBucketPrefixRDSRR		= "rds_replica_module"
def tfStateBucketPrefixR53			= "r53_module"
def tfStateBucketPrefixR53ac		= "r53ac_module"
def tfStateBucketPrefixR53alias		= "r53alias_module"
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
def tfStateBucketPrefixLB			= "load_balancer_module"
def tfstateBucketPrefixASG          = "asg_module/auto_scaling_group"
def tfstateBucketPrefixASGLT        = "asg_module/auto_scaling_lt"

def lambda_functions_list           = ['select','ec2_stop_scheduler','ec2_ss_delete_scheduler','rds_stop_scheduler','rds_ss_delete_scheduler','ec2_instance_profile_checker', 'ec2_volume_eni_checker', 'iam_access_key_checker']

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
		stringParam('db_master_identifier'		, 'test-rds'					, '''Name : name-(pgsql|oracle|mysql|mariadb)-rds + rr<br>
		TF-STATE : Statefile for Instance<br>
		db_identifier.tfstate''')
		stringParam('db_slave_identifier'		, 'test-rds-rr'					, '')
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

// Route53 Record  Creation A - CNAME - ALIAS
pipelineJob('terraform-r53-ac-record-job') {
	description('Building AWS Route53 Record Creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'					, [terraformRepo]					, '')
		stringParam('gitBranch'					, 'master'							, '')
		choiceParam('gitCreds'					, [gitCreds]						, '')
		choiceParam('awsAccount'				, [awsAccount]						, '')
		choiceParam('tfstateBucket'				, [tfStateBucket]					, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefix'		, [tfStateBucketPrefixR53ac]		, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixALIAS'	, [tfStateBucketPrefixR53alias]		, 'TF State Bucket Prefix'      )
		stringParam('r53_zone_name'				, 'vignesh-private.zone.com'		, '')
		stringParam('r53_record_name'			, 'test-instance-ec2-r53'			, '')
		stringParam('r53_records'				, '---'								, 'ip-address (NA : ALIAS)'		)
		choiceParam('r53_overwrite'				, ['true','false']					, '')
		choiceParam('r53_record_type'			, ['A','CNAME']						, 'A/CNAME : ip-address | A : end-point/dns')
		choiceParam('alias_for'					, ['load-balancer','rds-instance']	, '')
		stringParam('resource_name'				, 'test-alb | test-rds'				, 'RDS / ALB')
		choiceParam('includeR53acRecord'		, ['true','false']					, '')
		choiceParam('includeR53AliasRecord'		, ['true','false']					, '')
		choiceParam('terraformApplyPlan'		, ['plan','apply','plan-destroy','destroy']	, '')
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
		choiceParam('lambda_function'		, lambda_functions_list			, '')
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

// AWS LB Creation
pipelineJob('terraform-lb-job') {
	description('Building AWS ALB/NLB creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'					, [terraformRepo]				, '')
		stringParam('gitBranch'					, 'master'						, '')
		choiceParam('gitCreds'					, [gitCreds]					, '')
		choiceParam('awsAccount'				, [awsAccount]					, '')
		choiceParam('tfstateBucket'				, [tfStateBucket]				, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefixSG'		, [tfStateBucketPrefixSG]		, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixSGR'	, [tfStateBucketPrefixSGRule]	, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixLB'		, [tfStateBucketPrefixLB]		, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixALIAS'	, [tfStateBucketPrefixR53alias]		, 'TF State Bucket Prefix'      )
		stringParam('vpc_name'					, 'default-vpc'					, '')
		stringParam('lb_name'					, 'test-alb'					, '')
		choiceParam('lb_type'					, ['application','network']		, '')
		choiceParam('lb_is_internal'			, ['true']						, '')
		stringParam('lb_sg_name'				, 'test-alb'					, '')
		choiceParam('includeSG'					, ['true','false']				, '')
		choiceParam('includeSGRule'				, ['true','false']				, '')
		choiceParam('includeLB'					, ['true','false']				, '')
		stringParam('lis_port'					, '80'							, '')
		choiceParam('lis_protocol'				, ['HTTP','HTTPS']				, '')
		choiceParam('lis_response_type'			, ['fixed-response','forward','redirect']	, '')
		choiceParam('includeLBLis'				, ['true','false']				, '')
		stringParam('tg_name'					, 'test-alb-tg'					, '')
		stringParam('tg_port'					, '8080'						, '')
		choiceParam('tg_protocol'				, ['HTTP','HTTPS']				, '')
		choiceParam('tg_target_type'			, ['instance','ip','lambda']	, '')
		choiceParam('includeLBTG'				, ['true','false']				, '')
		stringParam('ec2_name'					, 'test-instance-rhel-7'		, '')
		stringParam('tg_attach_name'			, 'test-alb-tg-attach'			, '')
		choiceParam('includeLBTGA'				, ['true','false']				, '')
		stringParam('rule_target_dns'			, 'test-instance-ec2-r53'		, '')
		choiceParam('rule_response_type'		, ['forward','redirect','fixed-response']	, '')
		choiceParam('includeR53AliasRecord'		, ['true','false']				, '')
		choiceParam('includeLBRule'				, ['true','false']				, '')
		choiceParam('terraformApplyPlan'		, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-lb-pipeline.groovy'))
			sandbox()
		}
	}
}

pipelineJob('packer-build-ami') {
	description('Building AMI')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'					, [packerRepo]					, '')
		stringParam('gitBranch'					, 'master'						, '')
		choiceParam('gitCreds'					, [gitCreds]					, '')
		choiceParam('awsAccount'				, [awsAccount]					, '')
		choiceParam('packerDir'					, ['packer/templateFile']		, '')
		choiceParam('packerTempFile'			, ['packer.json']				, '')
		choiceParam('packerVarFile'				, ['packer-vars.json']			, '')
		choiceParam('packerLogLevel'			, ['select', '', '0', '1']		, '')
		choiceParam('packerLogFile'				, ['packer-logs.log']			, '')
		stringParam('packer_ami_name'			, 'jenkins-ami-custom'			, '')
		stringParam('source_ami_name'			, 'RHEL-7.7_HVM'				, '')
		stringParam('source_ami_owner'			, '309956199498'				, '')
		stringParam('packer_instance_type'		, 't2.micro'					, '')
		stringParam('vpc_name'					, 'default-vpc'					, '')
		choiceParam('subnet_name'				, ['default-subnet-1','default-subnet-2','default-subnet-3'], '')
		stringParam('security_group_name'		, 'default-ec2-sg'				, '')
		choiceParam('RHEL'						, ['7','8','6']					, '')
		stringParam('packerRepo'				, packerRepo					, '')
		stringParam('packerBranch'				, 'testing'						, '')
		stringParam('pipModules'				, 'pip,pip2,pip2.7,pip3,pip3.6'	, '')
		stringParam('PG_MAJOR'					, '9.6'							, '')
		stringParam('PG_MINOR'					, '6'							, '')
		stringParam('packerVersion'				, '1.6.0'						, '')
		stringParam('tfVersion'					, '0.12.7'						, '')
		stringParam('group_name'				, 'root_group'					, '')
		stringParam('username'					, 'vignesh'						, '')
		stringParam('password'					, 'vignesh'						, '')
		booleanParam('pgsql'					, true							, '')
		booleanParam('packer'					, true							, '')
		booleanParam('terraform'				, true							, '')
		booleanParam('CloudWatch'				, true							, '')
		booleanParam('CloudInit'				, true							, '')
		booleanParam('jenkins'					, true							, '')
		booleanParam('jenkinsPlugins'			, true							, '')
		booleanParam('pythonModules'			, true							, '')
		booleanParam('createGroup'				, true							, '')
		booleanParam('createUser'				, true							, '')
		booleanParam('addSudoers'				, true							, '')
		choiceParam('includeAMIBuild'			, ['true','false']				, '')
		stringParam('amiId'						, 'ami-'						, '')
		stringParam('kmsAlias'					, 'aws/ebs'						, '')
		stringParam('subnetId'					, 'subnet-5ddcf635'				, '')
		stringParam('sgId'						, 'sg-0ca60bf03afe214ab'		, '')
		choiceParam('includeAMIEncrypt'			, ['true','false']				, '')
		choiceParam('deleteAMI'					, ['false','true']				, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/aws-linux-rhel7-ami.groovy'))
			sandbox()
		}
	}
}

// AWS ASG Creation
pipelineJob('terraform-asg-job') {
	description('Building AWS ASG creation')
	logRotator(100,100)
	parameters{
		choiceParam('gitRepo'					, [terraformRepo]					, '')
		stringParam('gitBranch'					, 'master'							, '')
		choiceParam('gitCreds'					, [gitCreds]						, '')
		choiceParam('awsAccount'				, [awsAccount]						, '')
		choiceParam('tfstateBucket'				, [tfStateBucket]					, 'TF State Bucket'             )
		choiceParam('tfstateBucketPrefixSG'		, [tfStateBucketPrefixSG]			, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixSGR'	, [tfStateBucketPrefixSGRule]		, 'TF State Bucket Prefix'      )
		choiceParam('tfstateBucketPrefixASGLT'	, [tfstateBucketPrefixASGLT]		, 'TF State Bucket Prefix'		)
		choiceParam('tfstateBucketPrefixASG'	, [tfstateBucketPrefixASG]			, 'TF State Bucket Prefix'      )
		stringParam('vpc_name'					, 'default-vpc'						, '')
		stringParam('asg_lt_name'				, 'test-asg-lt'						, '')
		choiceParam('asg_lt_instance_type'		, ['t2.micro']						, '')
		stringParam('asg_lt_sg_name'			, 'test-instance'					, 'name + sg (by default)'		)
		stringParam('ami_regex'					, 'RHEL-7.7'						, '')
		stringParam('ami_owner_id'				, '309956199498,734555027572'		, '''309956199498 : RHEL<br>734555027572 : CentOS''')
		choiceParam('root_user'					, ['vignesh']						, 'Login Cred')
		choiceParam('root_passwd'				, ['vignesh']						, 'Login Cred')
		stringParam('asg_min_size'				, '1'								, '')
		stringParam('asg_max_size'				, '1'								, '')
		stringParam('asg_desired_capacity'		, '1'								, '')
		choiceParam('asg_health_check_type'		, ['EC2','ELB']						, '')
		choiceParam('includeSG'					, ['true','false']					, '')
		choiceParam('includeSGRule'				, ['true','false']					, '')
		choiceParam('includeASGLT'				, ['true','false']					, '')
		choiceParam('includeASG'				, ['true','false']					, '')
		choiceParam('terraformApplyPlan'		, ['plan','apply','plan-destroy','destroy']	, '')
	}
	definition {
		cps {
			script(readFileFromWorkspace('pipeline/terraform-asg-pipeline.groovy'))
			sandbox()
		}
	}
}