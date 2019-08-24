def terraformRepo       	= "https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def terraformBranch     	= "master"
def gitCreds            	= "GitCred"
def terraformTFstateBucket	= "terraform-tfstate-mumbai"
def terraformTFstateBucketPrefix= "rds_module"

pipelineJob('rds_build_job') {
        description('Explains how to use Jenins Approval for Build Jobs')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'           	, [terraformRepo]	, '')
                choiceParam('gitBranch'         	, [terraformBranch]	, '')
                choiceParam('gitCreds'          	, [gitCreds]		, '')
		choiceParam('tfstateBucket'		, [terraformTFstateBucket]	, 'TF State Bucket'		, '')
		choiceParam('tfstateBucketPrefix'	, [terraformTFstateBucketPrefix], 'TF State Bucket Prefix'	, '')
                choiceParam('db_engine'	        	, ['postgres','oracle','mssql','mariadb','mysql','aurora']	, '')
                stringParam('db_family'         	, 'db_family'		, '')
		stringParam('db_engine_version' 	, 'db_version'		, '')
                choiceParam('db_instance_class'		, ['db.t2.small']	, '')
		stringParam('db_identifier'	    	, 'instance_name'	, '')
		stringParam('db_name'			, 'db_name'		, '')
		choiceParam('db_username'		, ['Administrator']	, '')
		stringParam('db_password'		, 'db_password'        	, 'Do you think that you can see !!')
		stringParam('db_allocated_storage'	, '5'			, 'in GBs')
		choiceParam('db_multi_az'		, ['true','false']	, '')
		choiceParam('createInstance'		, ['true','false']	, '')
		stringParam('db_R53_name'		, 'R53_Name'		, '')
		choiceParam('createInstanceDNS'		, ['true','false']	, '')
		choiceParam('terraformApplyPlan'	, ['true','false']	, '')
        }
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/rds_build.groovy'))
                        sandbox()
                }
        }
}
