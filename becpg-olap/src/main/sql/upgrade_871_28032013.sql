
UPDATE becpg_dimdate SET NDay = concat ( concat (RIGHT (concat ('0', day), 2),'/', RIGHT(concat ('0', month), 2)),'/',year);

UPDATE becpg_dimdate SET NWeek = concat ( 'S', week ,'/', year);
