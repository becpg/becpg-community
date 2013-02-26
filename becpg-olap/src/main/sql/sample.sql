

DELETE FROM becpg_instance
						 
INSERT INTO `becpg_instance` (`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`) VALUES ( "admin", "becpg", "default","demo","http://82.237.72.111:8080/alfresco/service");
INSERT INTO `becpg_instance` (`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`) VALUES ( "admin@agrostis.biz", "becpg", "agrostis.biz","matthieu","http://localhost:8080/alfresco/service");
INSERT INTO `becpg_instance` (`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`) VALUES ( "admin@demo.becpg.fr", "becpg", "demo.becpg.fr","matthieu","http://localhost:8080/alfresco/service");

