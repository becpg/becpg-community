	
--
-- beCPG becpg_dimdate
--
  		

DROP TABLE IF EXISTS `becpg_dimdate`;

CREATE TABLE `becpg_dimdate` (
   `dimdate_id` int(8) NOT NULL AUTO_INCREMENT,
   `Date` date UNIQUE NOT NULL,
   `Year` int(6) NOT NULL,
   `Quarter` int(6) NOT NULL,
   `Month` int(6) NOT NULL,
   `Week` int(6) NOT NULL,
   `Day` int(6) NOT NULL,
   `WeekDay` int(6) NOT NULL,
   `NQuarter` varchar(7) NOT NULL, 
   `NMonth` varchar(15) NOT NULL, 
   `NMonth3L` varchar(3) NOT NULL, 
   `NWeek` varchar(11) NOT NULL, 
   `NDay` varchar(15) NOT NULL, 
   `NWeekDay` varchar(15) NOT NULL, 
  PRIMARY KEY ( `dimdate_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DELIMITER $$

DROP PROCEDURE IF EXISTS `antDIM_TIME`$$ 
CREATE PROCEDURE `antDIM_TIME` () 
BEGIN
	

SET lc_time_names = 'fr_FR';

SELECT '2010-01-01' INTO @ds; 
SELECT '2030-01-01' INTO @de; 

WHILE (@ds <= @de) DO 

INSERT INTO becpg_dimdate  ( 
     dimdate_id, 
     Date, 
     Year,
     Quarter, 
     Month, 
     Week, 
     Day, 
     WeekDay, 
     NQuarter, 
     NMonth, 
     NMonth3L, 
     NWeek, 
     NDay, 
     NWeekDay
) 
SELECT year (@ds) * 10000 + month (@ds) * 100 + day (@ds) as dimdate_id, 
     (@ds) Date, 
     year(@ds) Year,
     quarter(@ds) Quarter, 
     month(@ds) Month, 
     week(@ds) Week, 
     RIGHT (concat ('0', day (@ds)), 2) Day, 
     weekday (@ds) WeekDay, 
     concat ( 'Q', quarter (@ds ),'/', year (@ds)) NQuarter, 
     monthname (@ds) NMonth, 
     LEFT (monthname(@ds), 3) NMonth3L, 
     concat ( 'Week', week (@ds) ,'/', year (@ds)) NWeek, 
     concat (RIGHT (concat ('0', day (@ds)), 2),' ', monthname(@ds)) NDay, 
    dayname(@ds) NWeekDay;

set @ds = DATE_ADD(@ds, INTERVAL 1 DAY); 

END WHILE;

END$$

DELIMITER ; 


CALL antDIM_TIME();


--
-- Product Type Table
--   becpg_product
-- 
DROP TABLE IF EXISTS `becpg_product_type`;

CREATE TABLE `becpg_product_type` (
	`product_type_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`product_type`  TEXT NOT NULL ,
	`product_label` TEXT NOT NULL,
	 PRIMARY KEY (`product_type_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

SET character_set_client = utf8;

insert into becpg_product_type(product_type,product_label) values ("product","Produit");
insert into becpg_product_type(product_type,product_label) values ("finishedProduct","Produit fini");
insert into becpg_product_type(product_type,product_label) values ("semiFinishedProduct","Produit semi fini");
insert into becpg_product_type(product_type,product_label) values ("localSemiFinishedProduct","Produit semi fini local");
insert into becpg_product_type(product_type,product_label) values ("rawMaterial","Matière première");
insert into becpg_product_type(product_type,product_label) values ("packagingKit",'Kit d''emballage');
insert into becpg_product_type(product_type,product_label) values ("packagingMaterial","Emballage");


--
-- Product state Table
--   becpg_product_state
-- 
DROP TABLE IF EXISTS `becpg_product_state`;

CREATE TABLE `becpg_product_state` (
	`product_state_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`product_state`  TEXT NOT NULL ,
	`product_label` TEXT NOT NULL,
	 PRIMARY KEY (`product_state_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

SET character_set_client = utf8;

insert into becpg_product_state(product_state,product_label) values ("ToValidate","A validé");
insert into becpg_product_state(product_state,product_label) values ("Valid","Validé");
insert into becpg_product_state(product_state,product_label) values ("Refused","Refusé");
insert into becpg_product_state(product_state,product_label) values ("Archived","Archivé");




--
-- Product Fact Table
--   becpg_product
-- 
DROP TABLE IF EXISTS `becpg_product`;

 CREATE TABLE `becpg_product` (
  `product_fact_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `product_startEffectivity_id` int(8) NOT NULL,
  `product_endEffectivity_id` int(8) DEFAULT NULL,
  `product_createdDate_id` int(8) NOT NULL,
  `product_modifiedDate_id` int(8) DEFAULT NULL,
  `product_code` TEXT NOT NULL,
  `product_name` TEXT NOT NULL,
  `product_legalName` TEXT DEFAULT NULL,
  `product_productHierarchy1` TEXT NOT NULL,
  `product_productHierarchy2` TEXT NOT NULL,
  `product_productState` TEXT DEFAULT NULL,
  `product_type` TEXT NOT NULL,
  `product_projectedQty` INT,
  `product_unitTotalCost` FLOAT,
  `product_unitPrice` FLOAT,
  `product_nut_assoc_id` BIGINT(20) ,
  `product_allergen_assoc_id` BIGINT(20) ,
  `product_ing_assoc_id` BIGINT(20) ,
  `product_microbio_assoc_id` BIGINT(20) ,
  `product_packaging_assoc_id` BIGINT(20) ,
  `product_compo_assoc_id` BIGINT(20),
  PRIMARY KEY (`product_fact_id`)
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

  

 DROP TABLE IF EXISTS `becpg_cost`;

 CREATE TABLE `becpg_cost` (
  `cost_fact_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `cost_startEffectivity_id` int(8) NOT NULL,
  `cost_endEffectivity_id` int(8) DEFAULT NULL,
  `cost_value` FLOAT NOT NULL,
  `cost_name` TEXT NOT NULL,
  `cost_source_id` BIGINT(20) ,
  `cost_assoc_id` BIGINT(20) ,
  PRIMARY KEY (`cost_fact_id`)
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
 
  
  

  
  