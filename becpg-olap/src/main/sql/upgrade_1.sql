ALTER TABLE `becpg_entity` ADD `is_last_version` BIT DEFAULT FALSE;
ALTER TABLE `becpg_datalist` ADD `is_last_version` BIT DEFAULT FALSE;


 UPDATE 	becpg_entity as entity2 SET entity2.is_last_version = TRUE WHERE
  entity2.id IN (
 	SELECT entity_id  FROM  becpg_property AS prop 	WHERE prop.prop_name = "cm:modified" AND 
 		(prop.date_value = (
					SELECT MAX(prop2.date_value) FROM becpg_property AS prop2
					WHERE prop.prop_name = "cm:modified" AND prop2.entity_id = prop.entity_id
					) OR  prop.date_value IS NULL)
	);
					

UPDATE 	becpg_datalist as entity2 SET entity2.is_last_version = TRUE WHERE
  entity2.id IN (
 	SELECT datalist_id  FROM  becpg_property AS prop 	WHERE prop.prop_name = "cm:modified" AND 
 		(prop.date_value = (
					SELECT MAX(prop2.date_value) FROM becpg_property AS prop2
					WHERE prop.prop_name = "cm:modified" AND prop2.datalist_id = prop.datalist_id
					) OR  prop.date_value IS NULL)
	);
			
		
//TODO add created = NULL