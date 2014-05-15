--
-- Title:      Upgrade to V1.6 - Rename Assocs
-- Database:   MySQL
-- Author:    matthieu

INSERT IGNORE INTO alf_qname (ns_id,version,local_name) SELECT id,0, 'labelingLabelingTemplate' FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/pack/1.0' ;
INSERT IGNORE INTO alf_qname (ns_id,version,local_name) SELECT id,0, 'productMicrobioCriteriaRef' FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0';
INSERT IGNORE INTO alf_qname (ns_id,version,local_name) SELECT id,0, 'precautionOfUseRef' FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0';
INSERT IGNORE INTO alf_qname (ns_id,version,local_name) SELECT id,0, 'subsidiaryRef' FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0';
INSERT IGNORE INTO alf_qname (ns_id,version,local_name) SELECT id,0, 'trademarkRef' FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0';
INSERT IGNORE INTO alf_qname (ns_id,version,local_name) SELECT id,0, 'storageConditionsRef' FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0';

UPDATE alf_node_assoc
   SET type_qname_id = (SELECT id  FROM alf_qname    WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/pack/1.0') and local_name = 'labelingLabelingTemplate')
   WHERE  type_qname_id = (SELECT id  FROM alf_qname   WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/pack/1.0') and local_name = 'labelingTemplate');

UPDATE alf_node_assoc
   SET type_qname_id = (SELECT id  FROM alf_qname  WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0') and local_name = 'productMicrobioCriteriaRef')
   WHERE  type_qname_id = (SELECT id  FROM alf_qname  WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0') and local_name = 'productMicrobioCriteria') ;
   
UPDATE alf_node_assoc
   SET type_qname_id = (SELECT id  FROM alf_qname  WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0') and local_name = 'precautionOfUseRef')
   WHERE  type_qname_id = (SELECT id  FROM alf_qname  WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0') and local_name = 'precautionOfUse') ;
   
UPDATE alf_node_assoc
   SET type_qname_id = (SELECT id  FROM alf_qname  WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0') and local_name = 'storageConditionsRef')
   WHERE  type_qname_id = (SELECT id  FROM alf_qname  WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0') and local_name = 'storageConditions') ;
   
UPDATE alf_node_assoc
   SET type_qname_id = (SELECT id  FROM alf_qname  WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0') and local_name = 'subsidiaryRef')
   WHERE  type_qname_id = (SELECT id  FROM alf_qname  WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0') and local_name = 'subsidiary') ;
   
UPDATE alf_node_assoc
   SET type_qname_id = (SELECT id FROM alf_qname  WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0') and local_name = 'trademarkRef')
   WHERE  type_qname_id = (SELECT id  FROM alf_qname  WHERE  ns_id =  (SELECT id FROM alf_namespace WHERE uri = 'http://www.bcpg.fr/model/becpg/1.0') and local_name = 'trademark') ;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.bcpg.plm.db-V1.6-Rename-assoc';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.bcpg.plm.db-V1.6-Rename-assoc', 'Manually executed script upgrade V1.6 to Rename assocs',
     0, 6022, -1, 6033, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
