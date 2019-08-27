def terraformRepo       	= "https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def terraformBranch     	= "master"
def gitCreds            	= "GitCred"
def terraformTFstateBucket	= "terraform-tfstate-mumbai"
def terraformTFstateBucketPrefix= "rds_module"

// RDS Build Job
pipelineJob('rds_build_job') {
        description('Explains how to use Jenins Approval for Build Jobs')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'           	, [terraformRepo]	, '')
                choiceParam('gitBranch'         	, [terraformBranch]	, '')
                choiceParam('gitCreds'          	, [gitCreds]		, '')
		choiceParam('tfstateBucket'		, [terraformTFstateBucket]	, 'TF State Bucket'		)
		choiceParam('tfstateBucketPrefix'	, [terraformTFstateBucketPrefix], 'TF State Bucket Prefix'	)
                choiceParam('db_engine'	        	, ['postgres','oracle','mssql','mariadb','mysql','aurora']	,'')
                stringParam('db_family'         	, 'postgres,oracle-se1'	, '')
		stringParam('db_engine_version' 	, '9.6.11,11.2.0.4.v21'	, '')
                choiceParam('db_instance_class'		, ['db.t2.small']	, '')
		stringParam('db_identifier'	    	, 'test-instance'	, '')
		stringParam('db_name'			, 'DBNAME'		, '')
		choiceParam('db_username'		, ['Administrator']	, '')
		nonStoredPasswordParam('db_password'	, 'Do you think that you can see !!')
		stringParam('db_allocated_storage'	, '10'			, 'in GBs')
		choiceParam('db_multi_az'		, ['false','true']	, '')
		choiceParam('createInstance'		, ['true','false']	, '')
		stringParam('db_R53_name'		, 'R53_Name'		, '')
		choiceParam('createInstanceDNS'		, ['true','false']	, '')
		choiceParam('terraformApplyPlan'	, ['plan','apply','plan-destroy','destroy']	, '')
        }
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/rds_build.groovy'))
                        sandbox()
                }
        }
}

// RDS Build Generic Job
pipelineJob('rds_build_gen_job') {
        description('Explains how to use Jenins Approval for Build Jobs')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'                   , [terraformRepo]       , '')
                choiceParam('gitBranch'                 , [terraformBranch]     , '')
                choiceParam('gitCreds'                  , [gitCreds]            , '')
                choiceParam('tfstateBucket'             , [terraformTFstateBucket]      , 'TF State Bucket'             )
                choiceParam('tfstateBucketPrefix'       , [terraformTFstateBucketPrefix], 'TF State Bucket Prefix'      )
                choiceParam('db_rds'			, ['postgres','oracle','mssql','mariadb','mysql','aurora']      ,'')
		stringParam('db_engine'                 , 'postgres,oracle-se1'	, '')
                stringParam('db_engine_version'         , '9.6.11,11.2.0.4.v21'	, '')
		stringParam('db_engine_major_version'	, '9.6,11.2'		, '')
                choiceParam('db_instance_class'         , ['db.t2.small']       , '')
                stringParam('db_identifier'             , 'test-instance'       , '')
                stringParam('db_name'                   , 'DBNAME'		, '')
                choiceParam('db_username'               , ['Administrator']     , '')
                nonStoredPasswordParam('db_password'    , 'Do you think that you can see !!')
                stringParam('db_allocated_storage'      , '10'			, 'in GBs')
                choiceParam('db_multi_az'               , ['false','true']      , '')
                choiceParam('createInstance'            , ['true','false']      , '')
                stringParam('db_R53_name'               , 'R53_Name'            , '')
                choiceParam('createInstanceDNS'         , ['true','false']      , '')
                choiceParam('terraformApplyPlan'        , ['plan','apply','plan-destroy','destroy']     , '')
        }
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/rds_build_gen.groovy'))
                        sandbox()
                }
        }
}
