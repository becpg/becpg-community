-- product hierachy 1
-- "VALUES";"Produit de la mer"

SELECT CONCAT('"VALUES";"',ORIGGPFR,'"') FROM `CIQUAL`.`FOOD_GROUPS` WHERE ORIGGPCD not like '%.%';

-- product hierachy 2
-- "VALUES";"Produit de la mer - Poisson";"Produit de la mer";"Poisson"

SELECT  CONCAT('"VALUES";"',hierachy1.ORIGGPFR,' - ',hierachy2.ORIGGPFR,'";"',hierachy1.ORIGGPFR,'";"',hierachy2.ORIGGPFR,'"')
 FROM FOOD_GROUPS as hierachy1, FOOD_GROUPS as hierachy2 WHERE hierachy1.ORIGGPCD not like '%.%'   AND hierachy2.ORIGGPCD like CONCAT(hierachy1.ORIGGPCD,'.%');


-- Nutriment
-- "VALUES";"Glucides";"g";"Nutriment";"Groupe 1"
"COLUMNS";"cm:name";"bcpg:nutUnit";"bcpg:nutType";"bcpg:nutGroup";"bcpg:legalName";"bcpg:legalName_fr";"bcpg:legalName_en"
SELECT CONCAT('"VALUES";"',ORIGCPNM,'";"',C_ORIGCPNMABR,'";"',ORIGCPNM,'";"',ORIGCPNM,'";"',ENGCPNAM,'"') from COMPONENT


-- Raw material
-- "COLUMNS";"bcpg:code";"cm:name";"bcpg:legalName";"bcpg:productHierarchy1";"bcpg:productHierarchy2"
SELECT CONCAT('"VALUES";"MP',REPLACE(FOOD.ORIGFDCD,'"',"'"),'";"',REPLACE(FOOD.ORIGFDNM,"\"","''"),'";"',REPLACE(FOOD.ORIGFDNM,"\"","''"),'";"',REPLACE(FOOD.ORIGFDNM,"\"","''"),'";"',REPLACE(FOOD.ENGFDNAM,"\"","''"),'";"',hierachy1.ORIGGPFR,'";"',hierachy2.ORIGGPFR,'"' ) from FOOD, FOOD_GROUPS as hierachy1, FOOD_GROUPS as hierachy2 
WHERE FOOD.ORIGGPCD = hierachy2.ORIGGPCD and  hierachy1.ORIGGPCD like SUBSTRING_INDEX(hierachy2.ORIGGPCD,".",1) and (hierachy1.ORIGGPFR 
not in ("Boulangerie-viennoiserie","Pâtisseries et biscuits","Céréales petit déjeuner et barres céréalières","Plats composés","Sandwichs") or hierachy2.ORIGGPFR in("Préparations pour pâtisseries")) ; 

-- Finished product
-- "COLUMNS";"bcpg:code";"cm:name";"bcpg:legalName";"bcpg:productHierarchy1";"bcpg:productHierarchy2"
SELECT CONCAT('"VALUES";"PF',REPLACE(FOOD.ORIGFDCD,'"',"'"),'";"',FOOD.ORIGFDNM,'";"',FOOD.ORIGFDNM,'";"',REPLACE(FOOD.ORIGFDNM,"\"","''"),'";"',REPLACE(FOOD.ENGFDNAM,"\"","''"),'";"',hierachy1.ORIGGPFR,'";"',hierachy2.ORIGGPFR,'"' ) from FOOD, FOOD_GROUPS as hierachy1, FOOD_GROUPS as hierachy2 
WHERE FOOD.ORIGGPCD = hierachy2.ORIGGPCD and  hierachy1.ORIGGPCD like SUBSTRING_INDEX(hierachy2.ORIGGPCD,".",1) and (hierachy1.ORIGGPFR  in ("Boulangerie-viennoiserie","Pâtisseries et biscuits","Céréales petit déjeuner et barres céréalières","Plats composés","Sandwichs") and hierachy2.ORIGGPFR not in("Préparations pour pâtisseries") ) ; 



-- NutList
-- "COLUMNS";"code";"bcpg:nutListNut";"bcpg:nutListValue";"bcpg:nutListUnit";"bcpg:nutListMini";"bcpg:nutListMaxi";bcpg:nutListGroup;

SELECT CONCAT('"VALUES";"MP',COMPILED_DATA.ORIGFDCD,'";"',COMPONENT.ORIGCPNM,'";"',REPLACE(COMPILED_DATA.SELVAL,",","."),'";"',COMPILED_DATA.UNIT,'";"',IFNULL(COMPILED_DATA.MIN,0),'";"',IFNULL(COMPILED_DATA.MAX,0),'"')  
FROM COMPILED_DATA,COMPONENT  WHERE COMPONENT.ORIGCPCD = COMPILED_DATA.ORIGCPCD 
AND COMPILED_DATA.ORIGFDCD IN (SELECT FOOD.ORIGFDCD from FOOD, FOOD_GROUPS as hierachy1, FOOD_GROUPS as hierachy2 
WHERE FOOD.ORIGGPCD = hierachy2.ORIGGPCD and  hierachy1.ORIGGPCD like SUBSTRING_INDEX(hierachy2.ORIGGPCD,".",1) 
and (hierachy1.ORIGGPFR not in ("Boulangerie-viennoiserie","Pâtisseries et biscuits","Céréales petit déjeuner et barres céréalières","Plats composés","Sandwichs")
or hierachy2.ORIGGPFR in("Préparations pour pâtisseries") )
);


SELECT CONCAT('"VALUES";"PF',COMPILED_DATA.ORIGFDCD,'";"',COMPONENT.ORIGCPNM,'";"',REPLACE(COMPILED_DATA.SELVAL,",","."),'";"',COMPILED_DATA.UNIT,'";"',IFNULL(COMPILED_DATA.MIN,0),'";"',IFNULL(COMPILED_DATA.MAX,0),'"')  
FROM COMPILED_DATA,COMPONENT  WHERE COMPONENT.ORIGCPCD = COMPILED_DATA.ORIGCPCD 
AND COMPILED_DATA.ORIGFDCD IN (SELECT FOOD.ORIGFDCD from FOOD, FOOD_GROUPS as hierachy1, FOOD_GROUPS as hierachy2 
WHERE FOOD.ORIGGPCD = hierachy2.ORIGGPCD and  hierachy1.ORIGGPCD like SUBSTRING_INDEX(hierachy2.ORIGGPCD,".",1) 
and (hierachy1.ORIGGPFR  in ("Boulangerie-viennoiserie","Pâtisseries et biscuits","Céréales petit déjeuner et barres céréalières","Plats composés","Sandwichs")
and hierachy2.ORIGGPFR not in("Préparations pour pâtisseries") )
);
