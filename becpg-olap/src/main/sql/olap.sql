
--
-- beCPG becpg_dimdate
--
  		

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


--
-- Entity Fact Table
--   becpg_entity
-- 
DROP TABLE IF EXISTS `becpg_entity`;

CREATE TABLE `becpg_entity` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `entity_id` VARCHAR(62) NOT NULL, 
  `entity_type` TEXT NOT NULL,
  `entity_name` TEXT NOT NULL,
   PRIMARY KEY (`id`)
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;
  
--
--
-- DataList Fact table 
--   becpg_datalist
--   
 DROP TABLE IF EXISTS `becpg_datalist`; 
 
 CREATE TABLE `becpg_datalist` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `datalist_id` VARCHAR(62) NOT NULL, 
  `entity_fact_id` BIGINT(20) NOT NULL,
  `datalist_name` TEXT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`entity_fact_id`) REFERENCES becpg_entity(`id`) ON DELETE CASCADE
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;
 
  
--
-- Properties
--  becpg_property
--
 DROP TABLE IF EXISTS `becpg_property`; 
  

 CREATE TABLE `becpg_property` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `fact_id`  BIGINT(20) NOT NULL,
  `prop_name` TEXT NOT NULL,
  `string_value` TEXT DEFAULT NULL,
  `date_value` int(8) DEFAULT NULL,
  `double_value` DOUBLE DEFAULT NULL,
  `boolean_value` BIT DEFAULT NULL,
  `long_value` BIGINT DEFAULT NULL,
  `float_value` FLOAT DEFAULT NULL,
  PRIMARY KEY (`id`),
   FOREIGN KEY (date_value) REFERENCES becpg_dimdate(`id`) ON DELETE CASCADE
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;

  