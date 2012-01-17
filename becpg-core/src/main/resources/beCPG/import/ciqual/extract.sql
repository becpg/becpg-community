-- product hierachy 1
-- "VALUES";"Produit de la mer"

SELECT CONCAT('"VALUES";"',ORIGGPFR,'"') FROM `CIQUAL`.`FOOD_GROUPS` WHERE ORIGGPCD not like '%.%';

-- product hierachy 2
-- "VALUES";"Produit de la mer - Poisson";"Produit de la mer";"Poisson"

SELECT  CONCAT('"VALUES";"',hierachy1.ORIGGPFR,' - ',hierachy2.ORIGGPFR,'";"',hierachy1.ORIGGPFR,'";"',hierachy2.ORIGGPFR,'"')
 FROM FOOD_GROUPS as hierachy1, FOOD_GROUPS as hierachy2 WHERE hierachy1.ORIGGPCD not like '%.%'   AND hierachy2.ORIGGPCD like CONCAT(hierachy1.ORIGGPCD,'.%');


-- Nutriment
-- "VALUES";"Glucides";"g";"Nutriment";"Groupe 1"

SELECT CONCAT('"VALUES";"',ORIGCPNM,'";"',C_ORIGCPNMABR,'"') from COMPONENT


-- Raw material
-- "COLUMNS";"bcpg:code";"cm:name";"bcpg:legalName";"bcpg:productHierarchy1";"bcpg:productHierarchy2"
SELECT CONCAT('"VALUES";"MP',FOOD.ORIGFDCD,'";"',FOOD.ORIGFDNM,'";"',FOOD.ORIGFDNM,'";"',hierachy1.ORIGGPFR,'";"',hierachy2.ORIGGPFR,'"' ) from FOOD, FOOD_GROUPS as hierachy1, FOOD_GROUPS as hierachy2 
WHERE FOOD.ORIGGPCD = hierachy2.ORIGGPCD and  hierachy1.ORIGGPCD like SUBSTRING_INDEX(hierachy2.ORIGGPCD,".",1); 

-- NutList
-- "COLUMNS";"code";"bcpg:nutListNut";"bcpg:nutListValue";"bcpg:nutListUnit";"bcpg:nutListMini";"bcpg:nutListMaxi";bcpg:nutListGroup;

SELECT CONCAT('"VALUES";"MP',COMPILED_DATA.ORIGFDCD,'";"',COMPONENT.ORIGCPNM,'";"',COMPILED_DATA.SELVAL,'";"',COMPILED_DATA.UNIT,'";"',IFNULL(COMPILED_DATA.MIN,0),'";"',IFNULL(COMPILED_DATA.MAX,0))  FROM COMPILED_DATA,COMPONENT  WHERE COMPONENT.ORIGCPCD = COMPILED_DATA.ORIGCPCD;
