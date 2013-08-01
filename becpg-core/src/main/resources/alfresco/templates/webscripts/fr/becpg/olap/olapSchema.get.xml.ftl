<#assign instanceId = args.instance />
<?xml version="1.0" encoding="UTF-8"?>
<Schema name="beCPG OLAP Schema">

	<#--
	<Dimension  name="Instance dimension" >
		<Hierarchy name="Instances" hasAll="true" allMemberCaption="Toutes les instances" primaryKey="id">
		   <Table name="becpg_instance" ></Table>
		   <Level name="Instance" column="instance_name"  type="String"    />
			<Level name="Tenant" column="tenant_name"  type="String"    />
		</Hierarchy>
	</Dimension>
	-->

	

	<Dimension type="TimeDimension"  name="Time dimension">
		<Hierarchy name="Date" hasAll="true" allMemberName="All Periods" allMemberCaption="Toutes les périodes"  primaryKey="id" caption="Date">
			<Table name="becpg_dimdate" alias="olapDate" />
			<Level name="Année" column="Year" type="Numeric" uniqueMembers="true" levelType="TimeYears"  />
			<Level name="Trimestre" column="Quarter" nameColumn="NQuarter" type="String"  levelType="TimeQuarters"  />
			<Level name="Mois" column="Month" nameColumn="NMonth4L" ordinalColumn="Month" type="Numeric"  levelType="TimeMonths"  />
			<Level name="Semaine" column="Week" nameColumn="NWeek" type="String"  levelType="TimeWeeks"  />
			<Level name="Jour" column="Day" nameColumn="NDay" ordinalColumn="Day" type="Numeric"  levelType="TimeDays"  />
		</Hierarchy>
		<#--
		<Hierarchy name="Date par mois"  hasAll="true" allMemberName="All Periods" allMemberCaption="Toutes les périodes"  primaryKey="id" caption="Date par mois" visible="false">
			<Table name="becpg_dimdate" alias="olapDate" />
			<Level name="Année" column="Year" type="Numeric" uniqueMembers="true" levelType="TimeYears"  />
			<Level name="Mois" column="Month" nameColumn="NMonth4L" ordinalColumn="Month" type="Numeric"  levelType="TimeMonths"  />
		</Hierarchy>
		<Hierarchy name="Date par semaine"  hasAll="true" allMemberName="All Periods" allMemberCaption="Toutes les périodes"  primaryKey="id" caption="Date par semaine" visible="false">
			<Table name="becpg_dimdate" alias="olapDate" />
			<Level name="Année" column="Year" type="Numeric" uniqueMembers="true" levelType="TimeYears"  />
			<Level name="Semaine" column="Week" nameColumn="NWeek" type="String"  levelType="TimeWeeks"  />
		</Hierarchy> -->
	</Dimension>

	<#--
	Top 10 des produits (Nb NC pour 1 000 000 			UVC facturés) + graph (camembert)
	NC par mois avec filtre par usine, année, 			marque, famille, produit, nom/groupe (tjs ramené à 1000 000 UVC 			fab) + graph (histogramme + ligne)
	Affichage des défauts avec % + graph 			(camembert)
	Nombre de NC par équipe et par défaut 			avec filtres possible
	Nombre NC par Centrale – client (2 niv)
	NC internes regrouper par défauts
  -->



	<Cube name="Incidents" cache="true" enabled="true" defaultMeasure="Nombre d'incidents">
		<View alias="nc">
			<SQL dialect="generic">
			<![CDATA[
				select
					entity.id as id,
					entity.entity_id as noderef,
					entity.entity_name as name,
					entity.is_last_version as isLastVersion,
					MAX(IF(prop.prop_name = "qa:ncType",prop.string_value,NULL)) as ncType,
					MAX(IF(prop.prop_name = "bcpg:code",prop.string_value,NULL)) as code,
					MAX(IF(prop.prop_name = "qa:ncPriority",prop.long_value,NULL)) as ncPriority,
					MAX(IF(prop.prop_name = "qa:ncState",prop.string_value,NULL)) as ncState,
					MAX(IF(prop.prop_name = "qa:ncQuantityNc",prop.long_value,NULL)) as ncQuantityNc, 
					MAX(IF(prop.prop_name = "qa:ncCost",prop.long_value,NULL)) as ncCost, 
					MAX(IF(prop.prop_name = "qa:batchId",prop.string_value,NULL)) as batchId, 
					MAX(IF(prop.prop_name = "qa:claimType",prop.string_value,NULL)) as claimType, 
					MAX(IF(prop.prop_name = "qa:claimOriginHierarchy1",prop.string_value,NULL)) as claimOriginHierarchy1, 
					MAX(IF(prop.prop_name = "qa:claimOriginHierarchy2",prop.string_value,NULL)) as claimOriginHierarchy2, 
					MAX(IF(prop.prop_name = "cm:created",prop.date_value,NULL)) as dateCreated,
					MAX(IF(prop.prop_name = "qa:claimResponseDate",prop.long_value,NULL)) as claimResponseDate, 
					MAX(IF(prop.prop_name = "qa:claimTreatmentDate",prop.long_value,NULL)) as claimTreatmentDate,
					MAX(IF(prop.prop_name = "qa:claimClosingDate",prop.long_value,NULL)) as claimClosingDate,
					entity.instance_id as instance_id
				from
					becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
				where
					entity.entity_type IN ("qa:nc") and instance_id = ${instanceId} and is_last_version = true
				group by 
					id
				]]>
			</SQL>
		</View>
		
		
		<Dimension  name="Désignation" >
			<Hierarchy name="Incident" hasAll="true" allMemberCaption="Tous les incidents">
				<Level name="Nom" column="name"  type="String"    />
				<Level name="Code NC" column="code"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="Lot" >
			<Hierarchy name="Lot" hasAll="true" allMemberCaption="Tous les lots">
				<Level name="Numéro de lot" column="batchId"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="Origine de l'incident" >
			<Hierarchy name="Origine" hasAll="true" allMemberCaption="Toutes les origines">
				<Level name="Famille" column="claimOriginHierarchy1"  type="String"    />
				<Level name="Sous famille" column="claimOriginHierarchy2"  type="String" approxRowCount="20"   />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="Type d'incident" >
			<Hierarchy name="Type d'incident" hasAll="true" allMemberCaption="Tous les types d'incidents">
				<Level name="Cause" column="claimType"  type="String" approxRowCount="20"   />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="Priorité">
			<Hierarchy name="Priorité" hasAll="true" allMemberCaption="Toutes les priorités">
				<Level name="Priorité" column="ncPriority"  type="String" uniqueMembers="true"   >
					 <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN ncPriority=1 THEN '${msg("listconstraint.qa_ncPriority.1")}'
	                            WHEN ncPriority=2 THEN '${msg("listconstraint.qa_ncPriority.2")}'
	                            WHEN ncPriority=3 THEN '${msg("listconstraint.qa_ncPriority.3")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="Type" >
			<Hierarchy hasAll="true" allMemberCaption="Tous les types" >
				<Level approxRowCount="2" name="Type"  column="ncType"  type="String" uniqueMembers="true"   >
				<NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN ncType='Claim' THEN '${msg("listconstraint.qa_ncTypes.Claim")}'
	                            WHEN ncType='NonConformity' THEN '${msg("listconstraint.qa_ncTypes.NonConformity")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="État" >
			<Hierarchy hasAll="true" allMemberCaption="Tous les états" >
				<Level approxRowCount="7" name="État"  column="ncState"  type="String" uniqueMembers="true"   >
				<NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN ncState='analysis' THEN '${msg("listconstraint.qa_ncStates.analysis")}'
	                            WHEN ncState='treatment' THEN '${msg("listconstraint.qa_ncStates.treatment")}'
	                            WHEN ncState='response' THEN '${msg("listconstraint.qa_ncStates.response")}'
	                            WHEN ncState='classification' THEN '${msg("listconstraint.qa_ncStates.classification")}'
	                            WHEN ncState='closing' THEN '${msg("listconstraint.qa_ncStates.closing")}'
	                            WHEN ncState='closed' THEN '${msg("listconstraint.qa_ncStates.closed")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>


		
		<Dimension type="StandardDimension" foreignKey="id"  name="Produits">
			<Hierarchy hasAll="true" allMemberCaption="Tous les produits liés" primaryKey="entity_id">
				<View alias="products">
						<SQL dialect="generic">
							<![CDATA[
							select
								entity.id as id,
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy1",prop.string_value,NULL)) as productHierarchy1,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy2",prop.string_value,NULL)) as productHierarchy2,
								prop_entity.batch_id as batch_id,
								prop_entity.entity_id as entity_id
							from
								becpg_property AS prop_entity  LEFT JOIN becpg_entity AS entity  ON entity.entity_id = prop_entity.prop_id
																			  LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
								prop_entity.prop_name="qa:product"
								and (prop.prop_name = "bcpg:productHierarchy1" or prop.prop_name = "bcpg:productHierarchy2") and entity.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>		
				<Level name="Famille" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="Sous famille" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="Produit" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension type="StandardDimension" foreignKey="id"  name="Client">
			<Hierarchy hasAll="true" allMemberCaption="Tous les clients" primaryKey="entity_id">
				<View alias="client">
					<SQL dialect="generic">
						select
							prop.id,
							prop.prop_id as nodeRef,
							prop.string_value as name,
							prop.entity_id
						from
							 becpg_property AS prop
						where prop.prop_name = "bcpg:clients"
					</SQL>
				</View>
				<Level name="Nom client" nameColumn="name" column="nodeRef"  type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<#-- 
				<show id="bcpg:plants"/>
				<show id="qa:claimTreatmentActor" />
				<show id="qa:claimResponseActor" />
				<show id="cm:creator" /> 
					
		-->
		
		
	   <DimensionUsage name="Date de saisie" caption="Date de création" source="Time dimension" foreignKey="dateCreated" />
	   <DimensionUsage name="Date de traitement" caption="Date de traitement" source="Time dimension" foreignKey="claimTreatmentDate" />
		<DimensionUsage name="Date de réponse" caption="Date de réponse" source="Time dimension" foreignKey="claimResponseDate" />
		<DimensionUsage name="Date de clôture" caption="Date de clôture" source="Time dimension" foreignKey="claimClosingDate" />
		
		
		<Measure name="Nombre d'incidents" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
		<Measure name="Quantité non conforme" column="ncQuantityNc" datatype="Numeric" aggregator="sum" visible="true"  />
		<Measure name="Montant lié &#224; la non conformité (euro)" column="ncCost" datatype="Numeric" aggregator="sum" visible="true"  />
	</Cube>
	
	<Cube name="Etapes de projets" cache="true" enabled="true" defaultMeasure="Nombre d'étapes">
		<View alias="projectTask">
			<SQL dialect="generic">
			<![CDATA[
				select
					datalist.id as id,
					datalist.datalist_id as noderef,
					datalist.entity_fact_id as entity_fact_id,
					datalist.is_last_version as isLastVersion,
					MAX(IF(prop.prop_name = "pjt:tlTaskName",prop.string_value,NULL)) as tlTaskName,
					MAX(IF(prop.prop_name = "pjt:tlDuration",prop.long_value,NULL)) as tlDuration,
					MAX(IF(prop.prop_name = "pjt:tlStart",prop.date_value,NULL)) as tlStart,
					MAX(IF(prop.prop_name = "pjt:tlEnd",prop.date_value,NULL)) as tlEnd,
					MAX(IF(prop.prop_name = "pjt:tlState",prop.string_value,NULL)) as tlState,
					MAX(IF(prop.prop_name = "pjt:tlResources",prop.string_value,NULL)) as tlResources,
					MAX(IF(prop.prop_name = "bcpg:sort",prop.long_value,NULL)) as sortOrder,
					DATEDIFF(MAX(IF(prop.prop_name = "pjt:tlEnd",prop.date_value,NULL)),
								MAX(IF(prop.prop_name = "pjt:tlStart",prop.date_value,NULL))
						) as duration,					
					datalist.instance_id as instance_id
				from
					becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
				where
					datalist.datalist_name = "taskList" and datalist.item_type = "pjt:taskList" and datalist.is_last_version = true and instance_id = ${instanceId}
				group by 
					id
				]]>
			</SQL>
		</View>
		
		
		<Dimension  name="Désignation" >
			<Hierarchy name="Etape par nom" hasAll="true" allMemberCaption="Toutes les étapes">
				<Level name="Nom étape" column="tlTaskName"  type="String"   ordinalColumn="sortOrder" />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="État" >
			<Hierarchy hasAll="true" allMemberCaption="Tous les états" >
				<Level approxRowCount="5" name="État"  column="tlState"  type="String" uniqueMembers="true"   >
				  <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN tlState='Planned' THEN 'Plannifié'
	                            WHEN tlState='InProgress' THEN 'En cours'
	                            WHEN tlState='OnHold' THEN 'Arrêté'
	                            WHEN tlState='Cancelled' THEN 'Annulé'
	                            WHEN tlState='Completed' THEN 'Terminé'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension type="StandardDimension" foreignKey="entity_fact_id"  name="Projet">
			<Hierarchy hasAll="true" allMemberCaption="Tous les projets" primaryKey="id">
				<View alias="pjt">
						<SQL dialect="generic">
							<![CDATA[
							select
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								entity.is_last_version as isLastVersion,
								MAX(IF(prop.prop_name = "pjt:projectHierarchy1",prop.string_value,NULL)) as projectHierarchy1,
								MAX(IF(prop.prop_name = "pjt:projectHierarchy2",prop.string_value,NULL)) as projectHierarchy2,
								MAX(IF(prop.prop_name = "pjt:projectManager",prop.string_value,NULL)) as projectManager,
								entity.id as id
							from
								 becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
								 entity.entity_type IN ("pjt:project") and entity.is_last_version = true and entity.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>		
				<Level name="Famille" column="projectHierarchy1" type="String"   >
				</Level>
				<Level name="Sous famille" column="projectHierarchy2" type="String"   >
				</Level>
				<Level name="Chef de projet" column="projectManager"  type="String"    >
				</Level>
				<Level name="Projet" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="Ressource" >
			<Hierarchy name="Ressource" hasAll="true" allMemberCaption="Toutes les ressources">
				<Level name="Ressource" column="tlResources"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<DimensionUsage name="Date début" caption="Date début" source="Time dimension" foreignKey="tlStart" />
		<DimensionUsage name="Date de fin" caption="Date de fin" source="Time dimension" foreignKey="tlEnd" />
		<Measure name="Nombre d'étapes" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
		<Measure name="Moyenne des durées (prévi)" column="tlDuration" datatype="Numeric" aggregator="avg" visible="true"  />
		<Measure name="Moyenne des durées" column="duration" datatype="Numeric" aggregator="avg" visible="true"  />
		
		<CalculatedMember name="Moyenne des durées (Cumulé)" dimension="Measures" visible="true">
			<Formula>([Measures].[Moyenne des durées],[Désignation.Etape par nom].PrevMember) + ([Measures].[Moyenne des durées])</Formula>
		</CalculatedMember> 
		
	</Cube>

	<Cube name="Projets" cache="true" enabled="true" defaultMeasure="Nombre de projets (Distinct)">
		<View alias="projet">
			<SQL dialect="generic">
			<![CDATA[
				select
					entity.id as id,
					entity.entity_id as noderef,
					entity.entity_name as name,
					entity.is_last_version as isLastVersion,
					MAX(IF(prop.prop_name = "pjt:projectHierarchy1",prop.string_value,NULL)) as projectHierarchy1,
					MAX(IF(prop.prop_name = "pjt:projectHierarchy2",prop.string_value,NULL)) as projectHierarchy2,
					MAX(IF(prop.prop_name = "bcpg:code",prop.string_value,NULL)) as code,
					MAX(IF(prop.prop_name = "cm:created",prop.date_value,NULL)) as projectDateCreated,
					MAX(IF(prop.prop_name = "cm:modified",prop.date_value,NULL)) as projectDateModified,
					MAX(IF(prop.prop_name = "pjt:projectState",prop.string_value,NULL)) as projectState,
					MAX(IF(prop.prop_name = "pjt:projectStartDate",prop.date_value,NULL)) as projectStartDate,
					MAX(IF(prop.prop_name = "pjt:projectDueDate",prop.date_value,NULL)) as projectDueDate,
					MAX(IF(prop.prop_name = "pjt:projectCompletionDate",prop.date_value,NULL)) as completionDate,
					MAX(IF(prop.prop_name = "pjt:projectPriority",prop.long_value,NULL)) as projectPriority, 
					MAX(IF(prop.prop_name = "pjt:completionPercent",prop.long_value,NULL)) as completionPercent,
					MAX(IF(prop.prop_name = "pjt:projectScore",prop.long_value,NULL)) as projectScore,
					MAX(IF(prop.prop_name = "pjt:projectOverdue",prop.long_value,NULL)) as projectOverdue,
					MAX(IF(prop.prop_name = "pjt:projectManager",prop.string_value,NULL)) as projectManager,
					MAX(IF(prop.prop_name = "pjt:projectOrigin",prop.string_value,NULL)) as projectOrigin,
					MAX(IF(prop.prop_name = "pjt:projectSponsor",prop.string_value,NULL)) as projectSponsor,
					MAX(IF(prop.prop_name = "bcpg:entityTplRef",prop.string_value,NULL)) as entityTplRef, 
					DATEDIFF(MAX(IF(prop.prop_name = "pjt:projectCompletionDate",prop.date_value,NULL)),
								MAX(IF(prop.prop_name = "pjt:projectStartDate",prop.date_value,NULL))
						) as duration,
					entity.instance_id as instance_id
				from
					becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
				where
					entity.entity_type IN ("pjt:project") and entity.instance_id = ${instanceId}
				group by 
					id
				]]>
			</SQL>
		</View>
		
		 <#-- <Dimension  name="Noeuds" visible="false"  >
			<Hierarchy name="Noeuds" hasAll="true">
				<Level name="Noeuds" column="noderef"  type="String"     />
			</Hierarchy>
		</Dimension> -->
		
		<Dimension  name="Désignation" >
			<Hierarchy name="Projet par famille" hasAll="true" allMemberCaption="Tous les projets">
				<Level name="Famille" column="projectHierarchy1"  type="String"    />
				<Level name="Sous famille" column="projectHierarchy2"  type="String"    />
				<Level name="Nom projet" column="name"  type="String"    />
				<Level name="Code projet" column="code"  type="String"    />
			</Hierarchy>
			<Hierarchy name="Projet par nom" hasAll="true" allMemberCaption="Tous les projets">
				<Level name="Nom projet" column="name"  type="String"    />
				<Level name="Code projet" column="code"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		
		<Dimension  name="Priorité">
			<Hierarchy name="Priorité" hasAll="true" allMemberCaption="Toutes les priorités">
				<Level name="Priorité" column="projectPriority"  type="String" uniqueMembers="true"   >
				 <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN projectPriority=1 THEN 'Basse'
	                            WHEN projectPriority=2 THEN 'Moyenne'
	                            WHEN projectPriority=3 THEN 'Haute'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		<Dimension  name="État" >
			<Hierarchy hasAll="true" allMemberCaption="Tous les états" >
				<Level approxRowCount="5" name="État"  column="projectState"  type="String" uniqueMembers="true"   >
				  <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN projectState='Planned' THEN 'Plannifié'
	                            WHEN projectState='InProgress' THEN 'En cours'
	                            WHEN projectState='OnHold' THEN 'Arrêté'
	                            WHEN projectState='Cancelled' THEN 'Annulé'
	                            WHEN projectState='Completed' THEN 'Terminé'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension type="StandardDimension" foreignKey="id"  name="Entités">
			<Hierarchy hasAll="true" allMemberCaption="Tous les entités liées" primaryKey="entity_id">
				<View alias="compoList">
						<SQL dialect="generic">
							<![CDATA[
							select
								entity.id as id,
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy1",prop.string_value,NULL)) as productHierarchy1,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy2",prop.string_value,NULL)) as productHierarchy2,
								prop_entity.batch_id as batch_id,
								prop_entity.entity_id as entity_id
							from
								becpg_property AS prop_entity  LEFT JOIN becpg_entity AS entity  ON entity.entity_id = prop_entity.prop_id
																			  LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
								prop_entity.prop_name="pjt:projectEntity"
								and (prop.prop_name = "bcpg:productHierarchy1" or prop.prop_name = "bcpg:productHierarchy2") and entity.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
				</View>		
				<Level name="Famille" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="Sous famille" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="Entité" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		
		<Dimension  name="Historique" >
			<Hierarchy name="Version courrante" hasAll="false" defaultMember="[Historique.Version courrante].[true]">
				<Level name="Version courrante" column="isLastVersion"  type="Boolean"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="Chef de projet" >
			<Hierarchy name="Chef de projet" hasAll="true" allMemberCaption="Tous les chefs de projet">
				<Level name="Chef de projet" column="projectManager"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="Origine de l'idée" >
			<Hierarchy name="Origine de l'idée" hasAll="true" allMemberCaption="Toutes les origines">
				<Level name="Origine de l'idée" column="projectOrigin"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="Sponsor" >
			<Hierarchy name="Sponsor" hasAll="true" allMemberCaption="Tous les sponsors">
				<Level name="Sponsor" column="projectSponsor"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="Modèle de projet" >
			<Hierarchy name="Modèle de projet" hasAll="true" allMemberCaption="Tous les modèles de projet">
				<Level name="Modèle de projet" column="entityTplRef"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		
		<DimensionUsage name="Date de modification" caption="Date de modification" source="Time dimension"  foreignKey="projectDateModified" />
	   <DimensionUsage name="Date de création" caption="Date de création" source="Time dimension" foreignKey="projectDateCreated" />
		<DimensionUsage name="Date de début" caption="Date de début" source="Time dimension" foreignKey="projectStartDate" />
		<DimensionUsage name="Date d'échéance" caption="Date d'échéance" source="Time dimension" foreignKey="projectDueDate" />
		<DimensionUsage name="Date d'achèvement" caption="Date d'achèvement" source="Time dimension" foreignKey="completionDate" />
	

		<Measure name="Nombre de projets" column="id" datatype="Numeric" aggregator="count" visible="true" />
		<Measure name="Nombre de projets (Distinct)" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
		<Measure name="Durée moyenne" column="duration" datatype="Numeric" aggregator="avg" visible="true" />
		<Measure name="Avancement (Moyen)" column="completionPercent" datatype="Numeric" aggregator="avg" visible="true"  />
		<Measure name="Note (Moyenne)" column="projectScore" datatype="Numeric" aggregator="avg" visible="true"  />
		<Measure name="Retard " column="projectOverdue" datatype="Numeric" aggregator="sum" visible="true"  />
		
		<CalculatedMember name="Avancement (Dernier)" dimension="Measures" visible="true">
			<Formula>[Measures].[Avancement (Moyen)],LastNonEmpty(YTD(),[Measures].[Avancement (Moyen)])</Formula>
		</CalculatedMember> 
		
		<CalculatedMember name="Nombre de projets (Cumulé)" dimension="Measures" visible="true">
			<Formula>SUM(YTD(),[Measures].[Nombre de projets (Distinct)])</Formula>
		</CalculatedMember> 
		
		<#--
		
		CalculatedMember name="Avancement (Dernier)" dimension="Measures" visible="true">
			<Formula>([Measures].[Avancement (Moyen)], LastNonEmpty(Descendants([Date de modification.Date].currentMember,[Date de modification.Date].[Jour]),[Measures].[Avancement (Moyen)]))</Formula>
		</CalculatedMember> 
		
	
		<NamedSet name="Trois derniers mois">
			<Formula>{CurrentDateMember([Date de modification.Date],'[Date \de \mo\dificatio\n\.Date]\.[yyyy]\.[mmmm]').Lag(2): CurrentDateMember([Date de modification.Date],'[Date \de \mo\dificatio\n\.Date]\.[yyyy]\.[mmmm]')}</Formula>
		</NamedSet>
		
		
		<CalculatedMember name="Dernier avancement" dimension="Measures" visible="true">
			<Formula>Tail(NonEmptyCrossJoin({[Date de modification].[Date].firstChild:[Date de modification].[Date].currentMember},[Measures].[Avancement (Moyen)])).Item(0)</Formula>
		</CalculatedMember> 
		
		
		  <CalculatedMember name="Retard" dimension="Measures" visible="true">
		       <Formula>[Date de modification].[Date].currentMember - CurrentDateMember([Date de modification.Date par mois],'[Date \de \mo\dificatio\n\.Date par \moi\s]\.[yyyy]\.[mmmm]') * [Retard]  </Formula>
		  </CalculatedMember> 
		-->
		
	
	</Cube>

	<Cube name="Produits" cache="true" enabled="true" defaultMeasure="Nombre de produits">
		<View alias="produit">
			<SQL dialect="generic">
				select
					entity.id as id,
					entity.entity_id as noderef,
					entity.entity_name as name,
					entity.entity_type as productType,
					entity.is_last_version as isLastVersion,
					MAX(IF(prop.prop_name = "bcpg:productHierarchy1",prop.string_value,NULL)) as productHierarchy1,
					MAX(IF(prop.prop_name = "bcpg:productHierarchy2",prop.string_value,NULL)) as productHierarchy2,
					MAX(IF(prop.prop_name = "bcpg:code",prop.string_value,NULL)) as code,
					MAX(IF(prop.prop_name = "bcpg:legalName",prop.string_value,NULL)) as legalName,
					MAX(IF(prop.prop_name = "cm:created",prop.date_value,NULL)) as productDateCreated,
					MAX(IF(prop.prop_name = "cm:modified",prop.date_value,NULL)) as productDateModified,
					MAX(IF(prop.prop_name = "bcpg:startEffectivity",prop.date_value,NULL)) as startEffectivity,
					MAX(IF(prop.prop_name = "bcpg:endEffectivity",prop.date_value,NULL)) as endEffectivity,
					MAX(IF(prop.prop_name = "bcpg:productState",prop.string_value,NULL)) as productState,
					MAX(IF(prop.prop_name = "bcpg:projectedQty",prop.long_value,NULL)) as projectedQty,
					MAX(IF(prop.prop_name = "bcpg:unitTotalCost",prop.double_value,NULL)) as unitTotalCost,
					MAX(IF(prop.prop_name = "bcpg:profitability",prop.double_value,NULL)) as profitability,
					MAX(IF(prop.prop_name = "bcpg:unitPrice",prop.double_value,NULL)) as unitPrice,
					entity.instance_id as instance_id
				from
					becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
				where
					entity.entity_type IN ("bcpg:finishedProduct","bcpg:semiFinishedProduct","bcpg:localSemiFinishedProduct","bcpg:rawMaterial","bcpg:packagingKit","bcpg:packagingMaterial")
					and entity.instance_id = ${instanceId}
				group by 
					id
			</SQL>
		</View>
		<!-- 
     <Dimension  name="Noeuds" visible="false"  >
			<Hierarchy name="Noeuds" hasAll="true">
				<Level name="Noeuds" column="noderef"  type="String"     />
			</Hierarchy>
		</Dimension>
 		-->

		<Dimension  name="Désignation" >
			<Hierarchy name="Produit par famille" hasAll="true" allMemberCaption="Tous les produits">
				<Level name="Famille" column="productHierarchy1"  type="String"    />
				<Level name="Sous famille" column="productHierarchy2"  type="String"    />
				<Level name="Nom produit" column="name"  type="String"    />
				<Level name="Code produit" column="code"  type="String"   />
				<Level name="Nom legal" column="legalName" type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="État" foreignKey="productState">
			<Hierarchy hasAll="true" allMemberCaption="Tous les états" primaryKey="product_state">
				<Table name="becpg_product_state" />
				<Level approxRowCount="5" name="État" table="becpg_product_state" column="product_state" nameColumn="product_label" type="String" uniqueMembers="true"   />
			</Hierarchy>
		</Dimension>
		
		 
		<Dimension foreignKey="id"  name="Origine géographique">
			<Hierarchy hasAll="true" allMemberCaption="Tous les origines" primaryKey="entity_fact_id">
				<View alias="geoOrigin">
					<SQL dialect="generic">
						select
							prop.prop_id as nodeRef, 
							prop.string_value as name,
							datalist.entity_fact_id as entity_fact_id
						from
							becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
						where datalist.datalist_name = "ingList" and datalist.item_type = "bcpg:ingList" and prop.prop_name="bcpg:ingListgeoOrigin" and datalist.instance_id = ${instanceId}
					</SQL>
				</View>
				<Level name="Pays" column="nodeRef" nameColumn="name" type="String"  >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension foreignKey="productType"  name="Type de produit">
			<Hierarchy hasAll="true" allMemberCaption="Tous les types de produit" primaryKey="entity_type" defaultMember="[Type de produit].[Produit fini]">
				<Table name="becpg_entity_type"></Table>
				<Level approxRowCount="10" name="Type" column="entity_type" nameColumn="entity_label" type="String" uniqueMembers="true"  >
				</Level>
			</Hierarchy>
		</Dimension>
		<Dimension type="StandardDimension" foreignKey="id"  name="Client">
			<Hierarchy hasAll="true" allMemberCaption="Tous les clients" primaryKey="entity_id">
				<View alias="client">
					<SQL dialect="generic">
						select
							prop.id,
							prop.prop_id as nodeRef,
							prop.string_value as name,
							prop.entity_id
						from
							 becpg_property AS prop
						where prop.prop_name = "bcpg:clients"
					</SQL>
				</View>
				<Level name="Nom client" nameColumn="name" column="nodeRef"  type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		<Dimension type="StandardDimension" foreignKey="id"  name="Fournisseur">
			<Hierarchy hasAll="true" allMemberCaption="Tous les fournisseurs" primaryKey="entity_id">
				<View alias="supplier">
					<SQL dialect="generic">
						select
							prop.id,
							prop.prop_id as nodeRef,
							prop.string_value as name,
							prop.entity_id
						from
							 becpg_property AS prop
						where prop.prop_name = "bcpg:suppliers"
					</SQL>
				</View>
				<Level name="Nom fournisseur" nameColumn="name" column="nodeRef" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		<Dimension type="StandardDimension" foreignKey="id"  name="Nutriment">
			<Hierarchy name="Nutriment par groupe" hasAll="true" allMemberCaption="Tous les nutriments" primaryKey="entity_fact_id">
				<View alias="nutList">
					<SQL dialect="generic">
							select
								datalist.id as id,
								MAX(IF(prop.prop_name = "bcpg:nutListNut",prop.string_value,NULL)) as nutName,
								MAX(IF(prop.prop_name = "bcpg:nutListNut",prop.prop_id,NULL)) as nutNodeRef,
								MAX(IF(prop.prop_name = "bcpg:nutListGroup",prop.string_value,NULL)) as nutGroup,
								datalist.entity_fact_id as entity_fact_id
							from
									becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
							where datalist.datalist_name = "nutList" and datalist.item_type = "bcpg:nutList" and datalist.instance_id = ${instanceId}
								group by datalist.id
					</SQL>
				</View>
				<Level approxRowCount="3" name="Groupe de nutriment" column="nutGroup" type="String" uniqueMembers="true"  >
				</Level>
				<Level approxRowCount="20" name="Nutriment" column="nutNodeRef"  nameColumn="nutName" type="String"   >
				</Level>
			</Hierarchy>
			
		</Dimension>
	
		<Dimension type="StandardDimension" foreignKey="id"  name="Allergène">
			<Hierarchy hasAll="true" allMemberCaption="Tous les allergènes" primaryKey="entity_fact_id">
				<View alias="allergenList">
					<SQL dialect="generic">
						select
							prop.prop_id as nodeRef, 
							prop.string_value as name,
							datalist.entity_fact_id as entity_fact_id
							from
								becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
							where datalist.datalist_name = "allergenList" and datalist.item_type = "bcpg:allergenList" and prop.prop_name="bcpg:allergenListAllergen" 
								and datalist.instance_id = ${instanceId}
					</SQL>
				</View>
				<Level approxRowCount="100" name="Allergène" column="nodeRef"  nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		
		<Dimension type="StandardDimension" foreignKey="id"  name="Ingredient">
			<Hierarchy hasAll="true" allMemberCaption="Tous les ingredients" primaryKey="entity_fact_id">
				<View alias="ingList">
					<SQL dialect="generic">
						select
							prop.prop_id as nodeRef, 
							prop.string_value as name,
							datalist.entity_fact_id as entity_fact_id
							from
								becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
							where datalist.datalist_name = "ingList" and datalist.item_type = "bcpg:ingList" and prop.prop_name="bcpg:ingListIng"
								and datalist.instance_id = ${instanceId}
					</SQL>
				</View>
				<Level name="Ingredient" column="nodeRef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		
		<Dimension type="StandardDimension" foreignKey="id"  name="Composition">
			<Hierarchy hasAll="true" allMemberCaption="Tous les composants" primaryKey="entity_fact_id">
				<View alias="compoList">
						<SQL dialect="generic">
							<![CDATA[
							select
								entity.id as id,
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								prop_datalist.batch_id as batch_id,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy1",prop.string_value,NULL)) as productHierarchy1,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy2",prop.string_value,NULL)) as productHierarchy2,
								datalist.entity_fact_id
							from
									becpg_datalist AS datalist LEFT JOIN becpg_property AS prop_datalist  ON prop_datalist.datalist_id = datalist.id
																	   LEFT JOIN becpg_entity AS entity  ON entity.entity_id = prop_datalist.prop_id
																		LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
									datalist.datalist_name = "compoList" 
										and datalist.item_type = "bcpg:compoList" 
										and prop_datalist.prop_name="bcpg:compoListProduct"
										and (prop.prop_name = "bcpg:productHierarchy1" or prop.prop_name = "bcpg:productHierarchy2")
										and datalist.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>		
				<Level name="Famille" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="Sous famille" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="Composant" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
	
		<Dimension type="StandardDimension" foreignKey="id"  name="Emballage">
			<Hierarchy hasAll="true" allMemberCaption="Tous les emballages" primaryKey="entity_fact_id">
				<View alias="packagingList">
						<SQL dialect="generic">
						<![CDATA[
						   select
								entity.id as id,
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								prop_datalist.batch_id as batch_id,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy1",prop.string_value,NULL)) as productHierarchy1,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy2",prop.string_value,NULL)) as productHierarchy2,
								datalist.entity_fact_id
							from
									becpg_datalist AS datalist LEFT JOIN becpg_property AS prop_datalist  ON prop_datalist.datalist_id = datalist.id
																	   LEFT JOIN becpg_entity AS entity  ON entity.entity_id = prop_datalist.prop_id
																		LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
									datalist.datalist_name = "packagingList" 
										and datalist.item_type = "bcpg:packagingList" 
										and prop_datalist.prop_name="bcpg:packagingListProduct"
										and (prop.prop_name = "bcpg:productHierarchy1" or prop.prop_name = "bcpg:productHierarchy2")
										and datalist.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>	
				<Level name="Famille" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="Sous famille" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="Emballage" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
	  <Dimension  name="Historique" >
			<Hierarchy name="Version courrante" hasAll="false" defaultMember="[Historique.Version courrante].[true]">
				<Level name="Version courrante" column="isLastVersion"  type="Boolean"    />
			</Hierarchy>
		</Dimension>
		
		<DimensionUsage name="Date de création" caption="Date de création" source="Time dimension" foreignKey="productDateCreated" />
		<DimensionUsage name="Date de modification" caption="Date de modification" source="Time dimension" foreignKey="productDateModified" />
		<DimensionUsage name="Debut d'effectivité" caption="Debut d'effectivité" source="Time dimension" foreignKey="startEffectivity" />
		<DimensionUsage name="Fin d'effectivité" caption="Fin d'effectivité" source="Time dimension" foreignKey="endEffectivity" />
		
		<Measure name="Nombre de produits" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
		<Measure name="Quantité previsionnelle" column="projectedQty" datatype="Integer" aggregator="sum" visible="true">
		</Measure>
		<Measure name="Coûts" column="unitTotalCost" datatype="Numeric" aggregator="sum" visible="true" >
		</Measure>
		<Measure name="Rentabilite unitaire" column="profitability" datatype="Numeric" aggregator="sum" visible="true">
		</Measure>
		<Measure name="Prix unitaire" column="unitPrice" datatype="Numeric" aggregator="sum" visible="true">
		</Measure>
		<CalculatedMember name="Profit" dimension="Measures" visible="true">
			<Formula>([Measures].[Prix unitaire] - [Measures].[Coûts])*[Measures].[Quantité previsionnelle]
			</Formula>
		</CalculatedMember>
		<CalculatedMember name="Rentabilité" dimension="Measures" visible="true">
			<Formula>([Measures].[Prix unitaire] - [Measures].[Coûts])/[Measures].[Coûts]</Formula>
		</CalculatedMember>
	</Cube>
	
	
	
	<#-- Sample roles 
	<Role name="ROLE_DEMO">
		<SchemaGrant access="none">
			<CubeGrant cube="Projets" access="all">
				<HierarchyGrant hierarchy="[Instances]" access="custom" rollupPolicy="partial" topLevel="[Instances].[Tenant]">
					<MemberGrant member="[Instances].[demo].[default]" access="all" />
				</HierarchyGrant>
			</CubeGrant>
			<CubeGrant cube="Produits" access="all">
				<HierarchyGrant hierarchy="[Instances]" access="custom" rollupPolicy="partial" topLevel="[Instances].[Tenant]" >
					<MemberGrant member="[Instances].[demo].[default]" access="all" />
				</HierarchyGrant>
			</CubeGrant>
		</SchemaGrant>
	</Role>
	
	<Role name="ROLE_USER">
		<SchemaGrant access="none"/>
	</Role>
	
	<Role name="ROLE_ADMIN">
		<SchemaGrant access="all"/>
	</Role>
	
	-->
	
</Schema>
