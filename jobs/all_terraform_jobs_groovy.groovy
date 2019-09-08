def terraformRepo       		= "https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def terraformBranch     		= "master"
def gitCreds            		= "GitCred"
def tfStateBucket			= "terraform-tfstate-mumbai"
def tfStateBucketPrefixRDS		= "rds_module"
def tfStateBucketPrefixR53		= "r53_module"
def tfStateBucketPrefixR53ac		= "r53ac_module"

// RDS DB Build Generic Job
pipelineJob('tf-1-rds-db-build-job') {
        description('Explains how to use Jenins Approval for Build Jobs')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'                   , [terraformRepo]       	, '')
                choiceParam('gitBranch'                 , [terraformBranch]     	, '')
                choiceParam('gitCreds'                  , [gitCreds]            	, '')
                choiceParam('tfstateBucket'             , [tfStateBucket]      		, 'TF State Bucket'             )
                choiceParam('tfstateBucketPrefix'       , [tfStateBucketPrefixRDS]	, 'TF State Bucket Prefix'      )
		stringParam('db_family'                 , 'postgres9.6,oracle-se1-11.2'	, '')
		stringParam('db_engine'                 , 'postgres,oracle-se1'		, '')
                stringParam('db_engine_version'         , '9.6.11,11.2.0.4.v21'		, '')
                choiceParam('db_instance_class'         , ['db.t2.small']       	, '')
                stringParam('db_identifier'             , 'test-instance'       	, '')
                choiceParam('db_name'                   , ['DBNAME']			, '')
                choiceParam('db_username'               , ['Administrator']     	, '')
                nonStoredPasswordParam('db_password'    , 'Do you think that you can see !!')
                choiceParam('db_allocated_storage'      , ['10']			, 'in GBs')
                choiceParam('db_multi_az'               , ['false','true']      	, '')
                choiceParam('createInstance'            , ['true','false']      	, '')
                stringParam('db_R53_name'               , 'R53_Name'            	, '')
                choiceParam('createInstanceDNS'         , ['true','false']      	, '')
                choiceParam('terraformApplyPlan'        , ['plan','apply','plan-destroy','destroy']	, '')
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
        description('Explains how to use Jenins Approval for Build Jobs')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'                   , [terraformRepo]       	, '')
                choiceParam('gitBranch'                 , [terraformBranch]     	, '')
                choiceParam('gitCreds'                  , [gitCreds]            	, '')
                choiceParam('tfstateBucket'             , [tfStateBucket]      		, 'TF State Bucket'             )
                choiceParam('tfstateBucketPrefix'       , [tfStateBucketPrefixR53]	, 'TF State Bucket Prefix'      )
		stringParam('r53_zone_name'		, 'vignesh-private-zone'	, '')
                stringParam('vpc_name'			, 'Default_VPC'			, '')
		choiceParam('createR53Zone'		, ['true','false']      	, '')
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
        description('Explains how to use Jenins Approval for Build Jobs')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'                   , [terraformRepo]       	, '')
                choiceParam('gitBranch'                 , [terraformBranch]     	, '')
                choiceParam('gitCreds'                  , [gitCreds]            	, '')
                choiceParam('tfstateBucket'             , [tfStateBucket]      		, 'TF State Bucket'             )
                choiceParam('tfstateBucketPrefix'       , [tfStateBucketPrefixR53ac]	, 'TF State Bucket Prefix'      )
		stringParam('r53_zone_name'		, 'vignesh-private-zone'	, '')
		stringParam('r53_record_name'		, 'postgres-r53,ec2-r53'	, '')
		stringParam('r53_records'		, ''				, 'ip-address | end-point')
		choiceParam('r53_record_type'		, ['A','CNAME']      		, 'A : ip-address<br>CNAME : end-point')
		choiceParam('createR53acRecord'		, ['true','false']      	, '')
		choiceParam('terraformApplyPlan'        , ['plan','apply','plan-destroy','destroy']	, '')
	}
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/tf-1-route53ac-record-build.groovy'))
                        sandbox()
                }
        }
}
