def terraformRepo       	= "https://github.com/vigneshpalanivelr/terraform_practice_codes.git"
def terraformBranch     	= "master"
def gitCreds            	= "GitCred"
def terraformTFstateBucket	= "terraform-tfstate-mumbai"
def terraformTFstateBucketPrefix= "rds_module"

pipelineJob('rds_build_job') {
        description('Explains how to use Jenins Approval for Build Jobs')
        logRotator(-1,-1)
        parameters{
                choiceParam('gitRepo'           	, [terraformRepo]       	, 'Job DSL Repo')
                choiceParam('gitBranch'         	, [terraformBranch]     	, 'Job DSL Branch')
                choiceParam('gitCreds'          	, [gitCreds]            	, 'Job DSL Cred')
		choiceParam('tfstateBucket'		, [terraformTFstateBucket]	, 'TF State Bucket')
		choiceParam('tfstateBucketPrefix'	, [terraformTFstateBucketPrefix], 'TF State Bucket Prefix')
                choiceParam('db_engine'	        	, ['postgres','oracle','mssql','mariadb','mysql','aurora']      , 'db type')
                stringParam('db_family'         	, 'db_family'           	, 'db engine_family')
		stringParam('db_engine_version' 	, 'db_version'          	, 'db engine_version')
                choiceParam('db_instance_class'		, ['db.t2.small']		, 'db instance type')
		stringParam('db_identifier'	    	, 'instance_name'       	, 'db instance name')
		stringParam('db_name'			, 'db_name'          		, 'db name')
		stringParam('db_username'		, 'db_username'        		, 'db username')
		stringParam('db_password'		, 'db_password'        		, 'db password')
		stringParam('db_allocated_storage'	, 'db_storage'			, 'db_allocated_storage')
		choiceParam('db_multi_az'		, ['true','false']		, 'db_multi_az')
		choiceParam('createInstance'		, ['true','false']		, 'create_db_instance')
		stringParam('db_R53_name'		, 'R53_Name'			, 'db_route53_name')
		choiceParam('createInstanceDNS'		, ['true','false']		, 'creating_db_instance_dns')
		choiceParam('terraformApplyPlan'	, ['true','false']		, 'terraform_apply_plan')
        }
        definition {
                cps {
                        script(readFileFromWorkspace('pipeline/rds_build.groovy'))
                        sandbox()
                }
        }
}
