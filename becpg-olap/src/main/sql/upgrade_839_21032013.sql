ALTER TABLE `becpg_entity` ADD `is_last_version` BIT DEFAULT FALSE;
ALTER TABLE `becpg_datalist` ADD `is_last_version` BIT DEFAULT FALSE;

UPDATE becpg_entity SET is_last_version = FALSE;
UPDATE becpg_datalist SET is_last_version = FALSE;

UPDATE becpg_entity  SET is_last_version = TRUE WHERE
 id IN (
 	SELECT MAX(entity.id) as id  FROM  (SELECT * FROM becpg_entity) AS entity GROUP BY entity.entity_id
);
					

UPDATE becpg_datalist SET is_last_version = TRUE WHERE
  id IN (
 		SELECT MAX(datalist.id) as id  FROM  (SELECT * FROM becpg_datalist) AS datalist GROUP BY datalist.datalist_id
);
			
