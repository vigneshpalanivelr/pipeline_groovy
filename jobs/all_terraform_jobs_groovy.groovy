def terraformRepo       		= "https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def terraformBranch     		= "master"
def gitCreds            		= "GitCred"
def tfStateBucket			= "terraform-tfstate-mumbai"
def tfStateBucketPrefixRDS		= "rds_module"
def tfStateBucketPrefixR53		= "r53_module"
def tfStateBucketPrefixR53ac		= "r53ac_module"
def tfStateBucketPrefixKMS		= "kms_module"

// RDS DB Build Generic Job
pipelineJob('tf-1-rds-db-build-job') {
        description('''Building AWS RDS Instances 
		    <br>&emsp 1) PostgreSQL <br>&emsp 2) Oracle <br>&emsp 3) MySQL <br>&emsp 4) MariaDB <br>
		    <br>Instructions :
		    <br>&emsp 1) Creates Master or Master + Slave Instance&emsp TF-STATE : InstanceId.tfstate
		    <br>&emsp 2) Creates Master Route53 Name&emsp&emsp&emsp&emsp&emsp &emsp TF-STATE : Route53-dns.tfstate
		    <br>&emsp 3) Creates Replica Route53 Name&emsp&emsp&emsp&emsp&emsp&emsp TF-STATE : Route53-dns.tfstate''')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'                   , [terraformRepo]       	, '')
                choiceParam('gitBranch'                 , [terraformBranch]     	, '')
                choiceParam('gitCreds'                  , [gitCreds]            	, '')
                choiceParam('tfstateBucket'             , [tfStateBucket]      		, 'TF State Bucket'             	)
                choiceParam('tfstateBucketPrefixRDS'	, [tfStateBucketPrefixRDS]	, 'TF State Bucket Prefix - RDS'	)
		choiceParam('tfstateBucketPrefixDNS'    , [tfStateBucketPrefixR53ac]	, 'TF State Bucket Prefix - DNS'	)
		stringParam('db_family'                 , 'postgres9.6,oracle-se1-11.2'	, '')
		stringParam('db_engine'                 , 'postgres,oracle-se1'		, '')
                stringParam('db_engine_version'         , '9.6.11,11.2.0.4.v21'		, '')
                choiceParam('db_instance_class'         , ['db.t2.small']       	, '')
                stringParam('db_identifier'             , 'test-instance'       	, '''TF-STATE : Statefile for Instance<br>
			    db_identifier.tfstate''')
                choiceParam('db_name'                   , ['DBNAME']			, '')
                choiceParam('db_username'               , ['Administrator']     	, '')
                nonStoredPasswordParam('db_password'    , 'Do you think that you can see !!')
                choiceParam('db_allocated_storage'      , ['10']			, 'in GBs')
                choiceParam('db_multi_az'               , ['false','true']      	, '')
		choiceParam('db_read_replica'		, ['true','false']      	, '')
                choiceParam('db_apply_changes'		, ['true','false']      	, '')
		choiceParam('includeInstance'		, ['true','false']      	, '')
		stringParam('db_route53_name'		, 'test-instance'		, '''TF-STATE : Statefile for Route53 Name<br>
			    db_route53_name-dns.tfstate''')
                choiceParam('includeInstanceDNS'	, ['false','true']      	, '')
                choiceParam('terraformApplyPlan'        , ['plan','apply','plan-destroy','destroy']	, '''
			    <br>&emsp plan&emsp&emsp&emsp&emsp: only plan to create 
			    <br>&emsp apply&emsp&emsp&emsp&ensp: will apply above plan 
			    <br>&emsp plan-destroy&nbsp&nbsp: only plan to destroy
			    <br>&emsp destroy&emsp&emsp&ensp&nbsp: will apply above plan-destroy''') 
        }
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/tf-1-rds-db-build.groovy'))
                        sandbox()
                }
        }
}

// Route53 Zone Creation
pipelineJob('tf-1-route53-zone-build-job') {
        description('Building AWS Route53 Zone Creation')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'                   , [terraformRepo]       	, '')
                choiceParam('gitBranch'                 , [terraformBranch]     	, '')
                choiceParam('gitCreds'                  , [gitCreds]            	, '')
                choiceParam('tfstateBucket'             , [tfStateBucket]      		, 'TF State Bucket'             )
                choiceParam('tfstateBucketPrefix'       , [tfStateBucketPrefixR53]	, 'TF State Bucket Prefix'      )
		stringParam('r53_zone_name'		, 'vignesh-private.zone.com'	, '')
                stringParam('vpc_name'			, 'Default_VPC'			, '')
		choiceParam('includeR53Zone'		, ['true','false']      	, '')
		choiceParam('terraformApplyPlan'        , ['plan','apply','plan-destroy','destroy']	, '')
	}
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/tf-1-route53-zone-build.groovy'))
                        sandbox()
                }
        }
}

// Route53 A-record and CNAME Creation
pipelineJob('tf-1-route53ac-record-build-job') {
        description('Building AWS Route53 Record Creation')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'                   , [terraformRepo]       	, '')
                choiceParam('gitBranch'                 , [terraformBranch]     	, '')
                choiceParam('gitCreds'                  , [gitCreds]            	, '')
                choiceParam('tfstateBucket'             , [tfStateBucket]      		, 'TF State Bucket'             )
                choiceParam('tfstateBucketPrefix'       , [tfStateBucketPrefixR53ac]	, 'TF State Bucket Prefix'      )
		stringParam('r53_zone_name'		, 'vignesh-private-zone'	, 'zone name')
		stringParam('r53_record_name'		, 'postgres-r53,ec2-r53'	, 'route53 name')
		stringParam('r53_records'		, ''				, 'ip-address | end-point')
		choiceParam('r53_record_type'		, ['A','CNAME']      		, 'A : ip-address | CNAME : end-point')
		choiceParam('includeR53acRecord'	, ['true','false']      	, '')
		choiceParam('terraformApplyPlan'        , ['plan','apply','plan-destroy','destroy']	, '')
	}
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/tf-1-route53ac-record-build.groovy'))
                        sandbox()
                }
        }
}

// AWS KMS Key Creation
pipelineJob('tf-1-kms-key-build-job') {
        description('Building AWS KMS key creation')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'                   , [terraformRepo]       	, '')
                choiceParam('gitBranch'                 , [terraformBranch]     	, '')
                choiceParam('gitCreds'                  , [gitCreds]            	, '')
                choiceParam('tfstateBucket'             , [tfStateBucket]      		, 'TF State Bucket'             )
                choiceParam('tfstateBucketPrefix'       , [tfStateBucketPrefixKMS]	, 'TF State Bucket Prefix'      )
		stringParam('kms_key_name'		, 'custome-key'			, '')
		choiceParam('includeKMSKey'		, ['true','false']      	, '')
		choiceParam('terraformApplyPlan'        , ['plan','apply','plan-destroy','destroy']	, '')
	}
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/tf-1-kms-key-build.groovy'))
                        sandbox()
                }
        }
}
