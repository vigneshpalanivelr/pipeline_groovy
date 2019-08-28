def terraformRepo       		= "https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def terraformBranch     		= "master"
def gitCreds            		= "GitCred"
def terraformTFstateBucket		= "terraform-tfstate-mumbai"
def terraformTFstateBucketPrefix	= "rds_module"

// RDS Build Generic Job
pipelineJob('tf-1-rds-build-generic-job') {
        description('Explains how to use Jenins Approval for Build Jobs')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'                   , [terraformRepo]       	, '')
                choiceParam('gitBranch'                 , [terraformBranch]     	, '')
                choiceParam('gitCreds'                  , [gitCreds]            	, '')
                choiceParam('tfstateBucket'             , [terraformTFstateBucket]      , 'TF State Bucket'             )
                choiceParam('tfstateBucketPrefix'       , [terraformTFstateBucketPrefix], 'TF State Bucket Prefix'      )
		stringParam('db_family'                 , 'postgres9.6,oracle-se1-11.2'	, '')
		stringParam('db_engine'                 , 'postgres,oracle-se1'		, '')
                stringParam('db_engine_version'         , '9.6.11,11.2.0.4.v21'		, '')
                choiceParam('db_instance_class'         , ['db.t2.small']       	, '')
                stringParam('db_identifier'             , 'test-instance'       	, '')
                choiceParam('db_name'                   , ['DBNAME']			, '')
                choiceParam('db_username'               , ['Administrator']     	, '')
                nonStoredPasswordParam('db_password'    , 'Do you think that you can see !!')
                choiceParam('db_allocated_storage'      , ['10','20']				, 'in GBs')
                choiceParam('db_multi_az'               , ['false','true']      	, '')
                choiceParam('createInstance'            , ['true','false']      	, '')
                stringParam('db_R53_name'               , 'R53_Name'            	, '')
                choiceParam('createInstanceDNS'         , ['true','false']      	, '')
                choiceParam('terraformApplyPlan'        , ['plan','apply','plan-destroy','destroy']	, '')
        }
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/tf-1-rds-build-generic.groovy'))
                        sandbox()
                }
        }
}

// RDS Build mssql Job
pipelineJob('tf-2-rds-build-generic-job') {
        description('Explains how to use Jenins Approval for Build Jobs')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'                   , [terraformRepo]               , '')
                choiceParam('gitBranch'                 , [terraformBranch]             , '')
                choiceParam('gitCreds'                  , [gitCreds]                    , '')
                choiceParam('tfstateBucket'             , [terraformTFstateBucket]      , 'TF State Bucket'             )
                choiceParam('tfstateBucketPrefix'       , [terraformTFstateBucketPrefix], 'TF State Bucket Prefix'      )
                stringParam('db_family'                 , 'sqlserver-ex-11.0' 		, '')
                stringParam('db_engine'                 , 'sqlserver-ex'		, '')
                stringParam('db_engine_version'         , '14.00.1000.169.v1'		, '')
                choiceParam('db_instance_class'         , ['db.t2.small']               , '')
                stringParam('db_identifier'             , 'test-instance'               , '')
                choiceParam('db_name'                   , ['','DBNAME']                 , '')
                choiceParam('db_username'               , ['Administrator']             , '')
                nonStoredPasswordParam('db_password'    , 'Do you think that you can see !!')
                choiceParam('db_allocated_storage'      , ['20','10']			, 'in GBs')
                choiceParam('db_multi_az'               , ['false','true']              , '')
                choiceParam('createInstance'            , ['true','false']              , '')
                stringParam('db_R53_name'               , 'R53_Name'                    , '')
                choiceParam('createInstanceDNS'         , ['true','false']              , '')
                choiceParam('terraformApplyPlan'        , ['plan','apply','plan-destroy','destroy']     , '')
        }
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/tf-2-rds-build-generic.groovy'))
                        sandbox()
                }
        }
}
