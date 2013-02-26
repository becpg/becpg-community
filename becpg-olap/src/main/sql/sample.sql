

DELETE FROM becpg_instance
						 
INSERT INTO `becpg_instance` (`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`) VALUES ( "admin", "becpg", "default","demo","http://82.237.72.111:8080/alfresco/service");
INSERT INTO `becpg_instance` (`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`) VALUES ( "admin@agrostis.biz", "becpg", "agrostis.biz","matthieu","http://localhost:8080/alfresco/service");
INSERT INTO `becpg_instance` (`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`) VALUES ( "admin@demo.becpg.fr", "becpg", "demo.becpg.fr","matthieu","http://localhost:8080/alfresco/service");


					select
    `compoList`.`productHierarchy1` as `c0`,
    avg(`projectTask`.`tlDuration`) as `m0`
from
    (select
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy1",prop.string_value,NULL)) as productHierarchy1,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy2",prop.string_value,NULL)) as productHierarchy2,
								entity.id as id
							from
								 becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
								 prop.prop_name = "bcpg:productHierarchy1" or prop.prop_name = "bcpg:productHierarchy2" and entity.entity_type = "pjt:project"
							group by id) as `compoList`,
    (select
					datalist.id as id,
					datalist.datalist_id as noderef,
					datalist.entity_fact_id as entity_fact_id,
					MAX(IF(prop.prop_name = "pjt:tlTaskName",prop.string_value,NULL)) as tlTaskName,
					MAX(IF(prop.prop_name = "pjt:tlDuration",prop.long_value,NULL)) as tlDuration,
					MAX(IF(prop.prop_name = "pjt:tlStart",prop.date_value,NULL)) as tlStart,
					MAX(IF(prop.prop_name = "pjt:tlEnd",prop.date_value,NULL)) as tlEnd,
					MAX(IF(prop.prop_name = "pjt:tlState",prop.string_value,NULL)) as tlState,
					datalist.instance_id as instance_id
				from
					becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
				where
					datalist.datalist_name = "taskList" and datalist.item_type = "pjt:taskList"
				group by 
					id) as `projectTask`
where
    `projectTask`.`entity_fact_id` = `compoList`.`id`
group by
    `compoList`.`productHierarchy1`