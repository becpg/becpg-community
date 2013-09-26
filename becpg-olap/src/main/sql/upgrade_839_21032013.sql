ALTER TABLE `becpg_entity` ADD `is_last_version` BIT DEFAULT FALSE;
ALTER TABLE `becpg_datalist` ADD `is_last_version` BIT DEFAULT FALSE;

UPDATE becpg_entity SET is_last_version = FALSE;
UPDATE becpg_datalist SET is_last_version = FALSE;

UPDATE becpg_entity  SET is_last_version = TRUE WHERE
 id IN (
 	SELECT MAX(entity.id) as id  FROM  (SELECT * FROM becpg_entity) AS entity GROUP BY entity.entity_id
);

CREATE TEMPORARY TABLE `temp_becpg_datalist` (
  `id` BIGINT(20) NOT NULL,
  `datalist_id` VARCHAR(62) NOT NULL, 
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;

  
INSERT INTO `temp_becpg_datalist` (id,datalist_id) SELECT id,datalist_id FROM becpg_datalist;

UPDATE becpg_datalist as t1 INNER JOIN
(
  SELECT MAX(datalist.id) as id  FROM  temp_becpg_datalist AS datalist 
  GROUP BY datalist.datalist_id
) as t2 
ON  t1.id = t2.id
SET t1.is_last_version = TRUE; 


DROP TABLE temp_becpg_datalist;