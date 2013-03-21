

DELETE FROM becpg_instance
						 
INSERT INTO `becpg_instance` (`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`) VALUES ( "admin", "becpg", "default","demo","http://localhost:8080/alfresco/service");
INSERT INTO `becpg_instance` (`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`) VALUES ( "admin@demo.becpg.fr", "becpg", "demo.becpg.fr","matthieu","http://localhost:8080/alfresco/service");

					
-- SELECT {[Measures].[Avancement]} ON COLUMNS,
-- 	{ [Désignation.Projet par famille].[Nom projet].Members 
-- 	, CurrentDateMember([Date de modification.Date par mois],'[Date \de \mo\dificatio\n\.Date par \moi\s]\.[yyyy]\.[mmmm]').Lag(2)  
-- 	: CurrentDateMember([Date de modification.Date par mois],'[Date \de \mo\dificatio\n\.Date par \moi\s]\.[yyyy]\.[mmmm]')}} ON ROWS
--  FROM [Projets]

  
  
-- SELECT
-- CrossJoin({[Measures].[Avancement]}, {CurrentDateMember([Date de modification.Date par mois],'[Date \de \mo\dificatio\n\.Date par \moi\s]\.[yyyy]\.[mmmm]').Lag(2)  
--	: CurrentDateMember([Date de modification.Date par mois],'[Date \de \mo\dificatio\n\.Date par \moi\s]\.[yyyy]\.[mmmm]')}) ON COLUMNS,
-- NON EMPTY {Hierarchize({{[Désignation.Projet par famille].[Famille].Members}, {[Désignation.Projet par famille].[Nom projet].Members}})} ON ROWS
-- FROM [Projets] WHERE {Hierarchize({[État].[En cours]})}


					
					
					
					
					
					
					
					
					