
--
-- beCPG becpg_dimdate
--
  	


DROP TABLE IF EXISTS `becpg_property`; 
DROP TABLE IF EXISTS `becpg_datalist`; 
DROP TABLE IF EXISTS `becpg_entity`;
DROP TABLE IF EXISTS `becpg_instance`; 
DROP TABLE IF EXISTS `becpg_batch`;
DROP TABLE IF EXISTS `becpg_dimdate`;

CREATE TABLE `becpg_dimdate` (
   `id` int(8) NOT NULL AUTO_INCREMENT,
   `Date` date UNIQUE NOT NULL,
   `Year` int(6) NOT NULL,
   `Quarter` int(6) NOT NULL,
   `Month` int(6) NOT NULL,
   `Week` int(6) NOT NULL,
   `Day` int(6) NOT NULL,
   `WeekDay` int(6) NOT NULL,
   `NQuarter` varchar(7) NOT NULL, 
   `NMonth` varchar(15) NOT NULL, 
   `NMonth4L` varchar(4) NOT NULL, 
   `NWeek` varchar(11) NOT NULL, 
   `NDay` varchar(15) NOT NULL, 
   `NWeekDay` varchar(15) NOT NULL, 
  PRIMARY KEY ( `id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;


DELIMITER $$

DROP PROCEDURE IF EXISTS `antDIM_TIME`$$ 
CREATE PROCEDURE `antDIM_TIME` () 
BEGIN
	

SET lc_time_names = 'fr_FR';

SELECT '2010-01-01' INTO @ds; 
SELECT '2030-01-01' INTO @de; 

WHILE (@ds <= @de) DO 

INSERT INTO becpg_dimdate  ( 
     id, 
     Date, 
     Year,
     Quarter, 
     Month, 
     Week, 
     Day, 
     WeekDay, 
     NQuarter, 
     NMonth, 
     NMonth4L, 
     NWeek, 
     NDay, 
     NWeekDay
) 
SELECT year (@ds) * 10000 + month (@ds) * 100 + day (@ds) as id, 
     (@ds) Date, 
     year(@ds) Year,
     quarter(@ds) Quarter, 
     month(@ds) Month, 
     week(@ds) Week, 
     RIGHT (concat ('0', day (@ds)), 2) Day, 
     weekday (@ds) WeekDay, 
     concat ( 'Q', quarter (@ds ),'/', year (@ds)) NQuarter, 
     monthname (@ds) NMonth, 
     LEFT (monthname(@ds), 4) NMonth4L, 
     concat ( 'Week', week (@ds) ,'/', year (@ds)) NWeek, 
     concat (RIGHT (concat ('0', day (@ds)), 2),' ', monthname(@ds)) NDay, 
    dayname(@ds) NWeekDay;

set @ds = DATE_ADD(@ds, INTERVAL 1 DAY); 

END WHILE;

END$$

DELIMITER ; 


CALL antDIM_TIME();


 CREATE TABLE `becpg_batch` (
 	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
   `batch_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (`id`)
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;



--
-- Instances
--  becpg_instance
--
  

 CREATE TABLE `becpg_instance` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `tenant_username`  TEXT NOT NULL,
  `tenant_password` TEXT NOT NULL,
  `tenant_name` TEXT NOT NULL,
  `instance_name` TEXT NOT NULL,
  `instance_url` TEXT NOT NULL,
  `last_imported` DATETIME  NULL,
  `batch_id`   BIGINT(20) NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;


--
-- Entity Fact Table
--   becpg_entity
-- 

CREATE TABLE `becpg_entity` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `entity_id` VARCHAR(62) NOT NULL, 
  `entity_type` TEXT NOT NULL,
  `entity_name` TEXT NOT NULL,
  `instance_id`  BIGINT(20) NOT NULL,
  `batch_id`   BIGINT(20) NOT NULL,
   PRIMARY KEY (`id`),
   FOREIGN KEY (instance_id) REFERENCES becpg_instance(`id`) ON DELETE CASCADE,
   FOREIGN KEY (batch_id) REFERENCES becpg_batch(`id`) ON DELETE CASCADE
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;
  
 CREATE INDEX entity_id_ix ON becpg_entity(entity_id);
  
--
--
-- DataList Fact table 
--   becpg_datalist
--   
 
 CREATE TABLE `becpg_datalist` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `datalist_id` VARCHAR(62) NOT NULL, 
  `entity_fact_id` BIGINT(20) NOT NULL,
  `datalist_name` TEXT NOT NULL,
  `item_type` TEXT NOT NULL,
  `instance_id`  BIGINT(20) NOT NULL,
  `batch_id`   BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (entity_fact_id) REFERENCES becpg_entity(`id`) ON DELETE CASCADE,
  FOREIGN KEY (instance_id) REFERENCES becpg_instance(`id`) ON DELETE CASCADE,
  FOREIGN KEY (batch_id) REFERENCES becpg_batch(`id`) ON DELETE CASCADE
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;
 
  
  CREATE INDEX datalist_id_ix ON becpg_datalist(datalist_id); 
  
--
-- Properties
--  becpg_property
--
  

 CREATE TABLE `becpg_property` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `entity_id`  BIGINT(20) DEFAULT  NULL,
  `datalist_id`  BIGINT(20) DEFAULT NULL,
  `prop_name` TEXT NOT NULL,
  `prop_id` TEXT DEFAULT NULL,
  `string_value` TEXT DEFAULT NULL,
  `date_value` int(8) DEFAULT NULL,
  `double_value` DOUBLE DEFAULT NULL,
  `boolean_value` BIT DEFAULT NULL,
  `long_value` BIGINT DEFAULT NULL,
  `float_value` FLOAT DEFAULT NULL,
  `batch_id`   BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
   FOREIGN KEY (date_value) REFERENCES becpg_dimdate(`id`) ON DELETE CASCADE,
   FOREIGN KEY (entity_id) REFERENCES becpg_entity(`id`) ON DELETE CASCADE,
   FOREIGN KEY (datalist_id) REFERENCES becpg_datalist(`id`) ON DELETE CASCADE,
   FOREIGN KEY (batch_id) REFERENCES becpg_batch(`id`) ON DELETE CASCADE
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;


  
  
  
  
--
-- Entity Type Table
--   becpg_product
-- 
DROP TABLE IF EXISTS `becpg_entity_type`;

CREATE TABLE `becpg_entity_type` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`entity_type`  TEXT NOT NULL ,
	`entity_label` TEXT NOT NULL,
	 PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;

SET character_set_client = utf8;

insert into becpg_entity_type(entity_type,entity_label) values ("bcpg:product","Produit");
insert into becpg_entity_type(entity_type,entity_label) values ("bcpg:finishedProduct","Produit fini");
insert into becpg_entity_type(entity_type,entity_label) values ("bcpg:semiFinishedProduct","Produit semi fini");
insert into becpg_entity_type(entity_type,entity_label) values ("bcpg:localSemiFinishedProduct","Produit semi fini local");
insert into becpg_entity_type(entity_type,entity_label) values ("bcpg:rawMaterial","Matière première");
insert into becpg_entity_type(entity_type,entity_label) values ("bcpg:packagingKit",'Kit d''emballage');
insert into becpg_entity_type(entity_type,entity_label) values ("bcpg:packagingMaterial","Emballage");


--
-- Product state Table
--   becpg_product_state
-- 
DROP TABLE IF EXISTS `becpg_product_state`;

CREATE TABLE `becpg_product_state` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`product_state`  TEXT NOT NULL ,
	`product_label` TEXT NOT NULL,
	 PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;

SET character_set_client = utf8;

insert into becpg_product_state(product_state,product_label) values ("ToValidate","En développement");
insert into becpg_product_state(product_state,product_label) values ("Valid","Validé");
insert into becpg_product_state(product_state,product_label) values ("Refused","Refusé");
insert into becpg_product_state(product_state,product_label) values ("Archived","Archivé");



ANALYZE TABLE becpg_property,becpg_datalist,becpg_entity;