<#assign instanceId = args.instance />
<?xml version="1.0" encoding="UTF-8"?>
<Schema name="beCPG OLAP Schema"> 
	

	<Dimension type="TimeDimension" name="timeDimension" caption="${msg("jsolap.timeDimension.title")}">	
		<Hierarchy name="date" hasAll="true" allMemberName="${msg("jsolap.allPeriods.title")}" allMemberCaption="${msg("jsolap.date.caption")}"  primaryKey="id" caption="${msg("jsolap.date.title")}">
			<Table name="becpg_dimdate" alias="olapDate" />
			<Level name="Year" caption="${msg("jsolap.year.title")}" column="Year" type="Numeric"  levelType="TimeYears"  />
			<Level name="Quarter" caption="${msg("jsolap.quarter.title")}" column="Quarter" nameColumn="NQuarter" type="String"  levelType="TimeQuarters"  />
			<Level name="Month" caption="${msg("jsolap.month.title")}" column="Month" nameColumn="NMonth4L" ordinalColumn="Month" type="Numeric"  levelType="TimeMonths"  />
			<Level name="Week" caption="${msg("jsolap.week.title")}" column="Week" nameColumn="NWeek" type="String"  levelType="TimeWeeks"  />
			<Level name="Day" caption="${msg("jsolap.day.title")}" column="Day" nameColumn="NDay" ordinalColumn="Day" type="Numeric"  levelType="TimeDays"  />
		</Hierarchy>		
	</Dimension>


	<Cube name="requirements" caption="${msg("jsolap.requirements.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.requirementsNumber.title")}">
		<View alias="nc">
			<SQL dialect="generic">
			<![CDATA[
				select
					datalist.id as id,
					datalist.datalist_id as noderef,
					datalist.entity_fact_id as entity_fact_id,
					datalist.is_last_version as isLastVersion,
					MAX(IF(prop.prop_name = "bcpg:rclReqType",prop.string_value,NULL)) as rclReqType,
					MAX(IF(prop.prop_name = "bcpg:rclReqMessage",prop.string_value,NULL)) as rclReqMessage,
					MAX(IF(prop.prop_name = "bcpg:rclSources",prop.prop_id,NULL)) as rclSources,
					datalist.instance_id as instance_id
				from
					becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
				where
					datalist.item_type = "bcpg:reqCtrlList" and datalist.is_last_version = true and instance_id = ${instanceId}
				group by 
					id
				]]>
			</SQL>
		</View>
		
		<Dimension name="rclReqMessage" caption="${msg("jsolap.message.title")}" >
			<Hierarchy name="rclReqMessage" caption="${msg("jsolap.message.title")}" hasAll="true" allMemberCaption="${msg("jsolap.message.caption")}">
				<Level name="rclReqMessage" caption="${msg("jsolap.message.title")}" column="rclReqMessage"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="rclReqType" caption="${msg("jsolap.requirementsLevels.title")}">
			<Hierarchy name="rclReqType" caption="${msg("jsolap.requirementsLevels.title")}" hasAll="true" allMemberCaption="${msg("jsolap.requirementsLevels.caption")}">
				<Level name="rclReqType" caption="${msg("jsolap.requirementsLevels.title")}" column="rclReqType"  type="String"    >
					 <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN rclReqType='Forbidden' THEN '${msg("listconstraint.bcpg_rclReqType.Forbidden")}'
	                            WHEN rclReqType='Tolerated' THEN '${msg("listconstraint.bcpg_rclReqType.Tolerated")}'
	                            WHEN rclReqType='Info' THEN '${msg("listconstraint.bcpg_rclReqType.Info")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
              		 </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension type="StandardDimension" foreignKey="entity_fact_id"  name="targetProducts" caption="${msg("jsolap.targetProducts.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.targetProducts.caption")}" primaryKey="id">
				<View alias="pr_target">
						<SQL dialect="generic">
							<![CDATA[
							select
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								entity.entity_type as entity_type,
								type_name.entity_label as entity_label,
								entity.is_last_version as isLastVersion,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy1",prop.string_value,NULL)) as productHierarchy1,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy2",prop.string_value,NULL)) as productHierarchy2,
								MAX(IF(prop.prop_name = "bcpg:productState",prop.string_value,NULL)) as productState,
								entity.id as id
							from
								 becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
								 						LEFT JOIN becpg_entity_type AS type_name ON type_name.entity_type = entity.entity_type
							where
								entity.is_last_version = true and entity.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>
				<Level approxRowCount="5" name="productState" caption="${msg("jsolap.productState.title")}"  column="productState"  type="String"  >
				  <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN productState='Simulation' THEN '${msg("listconstraint.bcpg_systemState.Simulation")}'
	                            WHEN productState='ToValidate' THEN '${msg("listconstraint.bcpg_systemState.ToValidate")}'
	                            WHEN productState='Valid' THEN '${msg("listconstraint.bcpg_systemState.Valid")}'
	                            WHEN productState='Refused' THEN '${msg("listconstraint.bcpg_systemState.Refused")}'
	                            WHEN productState='Archived' THEN '${msg("listconstraint.bcpg_systemState.Archived")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
             		 </NameExpression>
				</Level>	
				<Level approxRowCount="10" name="entity_type" caption="${msg("jsolap.type.title")}" column="entity_type" nameColumn="entity_label" type="String"   >
				</Level>		
				<Level name="productHierarchy1" caption="${msg("jsolap.family.title")}" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="productHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="productHierarchy2" type="String"   >
				</Level>
				
				<Level name="entity_noderef" caption="${msg("jsolap.product.title")}" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension type="StandardDimension" foreignKey="rclSources"  name="sourceProducts" caption="${msg("jsolap.sourceProducts.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.sourceProducts.caption")}" primaryKey="entity_noderef">
				<View alias="pr_sources">
						<SQL dialect="generic">
							<![CDATA[
							select
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								entity.entity_type as entity_type,
								type_name.entity_label as entity_label,
								entity.is_last_version as isLastVersion,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy1",prop.string_value,NULL)) as productHierarchy1,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy2",prop.string_value,NULL)) as productHierarchy2,
								MAX(IF(prop.prop_name = "bcpg:productState",prop.string_value,NULL)) as productState,
								entity.id as id
							from
								 becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
								 LEFT JOIN becpg_entity_type AS type_name ON type_name.entity_type = entity.entity_type
							where
								 entity.is_last_version = true and entity.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>		
				<Level approxRowCount="5" name="productState" caption="${msg("jsolap.state.title")}"  column="productState"  type="String"   >
				  <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN productState='Simulation' THEN '${msg("listconstraint.bcpg_systemState.Simulation")}'
	                            WHEN productState='ToValidate' THEN '${msg("listconstraint.bcpg_systemState.ToValidate")}'
	                            WHEN productState='Valid' THEN '${msg("listconstraint.bcpg_systemState.Valid")}'
	                            WHEN productState='Refused' THEN '${msg("listconstraint.bcpg_systemState.Refused")}'
	                            WHEN productState='Archived' THEN '${msg("listconstraint.bcpg_systemState.Archived")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
             		 </NameExpression>
				</Level>
				<Level approxRowCount="10" name="entity_type" caption="${msg("jsolap.type.title")}" column="entity_type" nameColumn="entity_label" type="String"   >
				</Level>	
				<Level name="productHierarchy1" caption="${msg("jsolap.family.title")}" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="productHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="productHierarchy2" type="String"   >
				</Level>
				
				<Level name="entity_noderef" caption="${msg("jsolap.product.title")}" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
	
		
		<Measure name="requirementsNumber" caption="${msg("jsolap.requirementsNumber.title")}" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
    </Cube>


	<Cube name="incidents" caption="${msg("jsolap.incidents.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.incidentsNumber.title")}">
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
					MAX(IF(prop.prop_name = "metadata:siteId",prop.string_value,NULL)) as siteId,
					MAX(IF(prop.prop_name = "metadata:siteName",prop.string_value,NULL)) as siteName,
					MAX(IF(prop.prop_name = "qa:claimOriginHierarchy1",prop.string_value,NULL)) as claimOriginHierarchy1, 
					MAX(IF(prop.prop_name = "qa:claimOriginHierarchy2",prop.string_value,NULL)) as claimOriginHierarchy2, 
					MAX(IF(prop.prop_name = "cm:created",prop.date_value,NULL)) as dateCreated,
					MAX(IF(prop.prop_name = "qa:claimResponseDate",prop.date_value,NULL)) as claimResponseDate, 
					MAX(IF(prop.prop_name = "qa:claimTreatementDate",prop.date_value,NULL)) as claimTreatmentDate,
					MAX(IF(prop.prop_name = "qa:claimClosingDate",prop.date_value,NULL)) as claimClosingDate,
					entity.instance_id as instance_id
				from
					becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
				where
					entity.entity_type = "qa:nc" and instance_id = ${instanceId} and is_last_version = true
				group by 
					id
				]]>
			</SQL>
		</View>
		
		<Dimension name="site" caption="${msg("jsolap.site.title")}">
			<Hierarchy name="site" caption="${msg("jsolap.site.title")}" hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}">
				<Level name="site" caption="${msg("jsolap.site.title")}" column="siteName"  type="String" />
			</Hierarchy>
		</Dimension>
		
		
		
		<Dimension  name="designation" caption="${msg("jsolap.designation.title")}" >
			<Hierarchy name="incident" caption="${msg("jsolap.incident.title")}" hasAll="true" allMemberCaption="${msg("jsolap.incident.caption")}">
				<Level name="name" caption="${msg("jsolap.name.title")}" column="name"  type="String"    />
				<Level name="code" caption="${msg("jsolap.ncCode.title")}" column="code"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="batch" caption="${msg("jsolap.batch.title")}" >
			<Hierarchy name="batch" caption="${msg("jsolap.batch.title")}" hasAll="true" allMemberCaption="${msg("jsolap.batch.caption")}">
				<Level name="batchId" caption="${msg("jsolap.batchNumber.title")}" column="batchId"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="incidentOrigin" caption="${msg("jsolap.incidentOrigin.title")}" >
			<Hierarchy name="origin" caption="${msg("jsolap.origin.title")}" hasAll="true" allMemberCaption="${msg("jsolap.origin.caption")}">
				<Level name="claimOriginHierarchy1" caption="${msg("jsolap.family.title")}" column="claimOriginHierarchy1"  type="String"    />
				<Level name="claimOriginHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="claimOriginHierarchy2"  type="String" approxRowCount="20"   />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="incidentType" caption="${msg("jsolap.incidentType.title")}" >
			<Hierarchy name="incidentType" caption="${msg("jsolap.incidentType.title")}" hasAll="true" allMemberCaption="${msg("jsolap.incidentType.caption")}">
				<Level name="claimType" caption="${msg("jsolap.cause.title")}" column="claimType"  type="String" approxRowCount="20"   />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="priority" caption="${msg("jsolap.priority.title")}">
			<Hierarchy name="priority" caption="${msg("jsolap.priority.title")}" hasAll="true" allMemberCaption="${msg("jsolap.priority.caption")}">
				<Level name="ncPriority" caption="${msg("jsolap.priority.title")}" column="ncPriority"  type="String"   >
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
		
		<Dimension  name="type" caption="${msg("jsolap.type.title")}" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.type.caption")}" >
				<Level approxRowCount="2" name="ncType" caption="${msg("jsolap.family.title")}"  column="ncType"  type="String"    >
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
		
		<Dimension  name="state" caption="${msg("jsolap.state.title")}" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.state.caption")}" >
				<Level approxRowCount="7" name="ncState" caption="${msg("jsolap.state.title")}"  column="ncState"  type="String"    >
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


		
		<Dimension type="StandardDimension" foreignKey="id"  name="products" caption="${msg("jsolap.products.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.products.caption")}" primaryKey="entity_id">
				<View alias="qaProducts">
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
								and (prop.prop_name = "bcpg:productHierarchy1" or prop.prop_name = "bcpg:productHierarchy2") 
								and entity.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>		
				<Level name="productHierarchy1" caption="${msg("jsolap.family.title")}" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="productHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="entity_noderef" caption="${msg("jsolap.product.title")}" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension type="StandardDimension" foreignKey="id"  name="client" caption="${msg("jsolap.client.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.client.caption")}" primaryKey="entity_id">
				<View alias="${msg("jsolap.client.title")}">
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
				<Level name="name" caption="${msg("jsolap.clientName.title")}" nameColumn="name" column="nodeRef"  type="String" >
				</Level>
			</Hierarchy>
		</Dimension>

	    <DimensionUsage name="dateCreated" caption="${msg("jsolap.entryDate.title")}" source="timeDimension" foreignKey="dateCreated" />
	    <DimensionUsage name="claimTreatmentDate" caption="${msg("jsolap.treatmentDate.title")}" source="timeDimension" foreignKey="claimTreatmentDate" />
		<DimensionUsage name="claimResponseDate" caption="${msg("jsolap.answerDate.title")}" source="timeDimension" foreignKey="claimResponseDate" />
		<DimensionUsage name="claimClosingDate" caption="${msg("jsolap.closingDate.title")}" source="timeDimension" foreignKey="claimClosingDate" />
		
		
		
		<Measure name="noderef" caption="${msg("jsolap.incidentsNumber.title")}" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
		<Measure name="ncQuantityNc" caption="${msg("jsolap.nonConformQuantity.title")}" column="ncQuantityNc" datatype="Numeric" aggregator="sum" visible="true"  />
		<Measure name="ncCost" caption="${msg("jsolap.nonConformityCost.title")}" column="ncCost" datatype="Numeric" aggregator="sum" visible="true"  />
	</Cube>
	
	<Cube name="projectsSteps" caption="${msg("jsolap.projectsSteps.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.projectsSteps.title")}">
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
					MAX(IF(prop.prop_name = "pjt:tlWork",prop.double_value,NULL)) as tlWork,
					MAX(IF(prop.prop_name = "pjt:tlLoggedTime",prop.string_value,NULL)) as tlLoggedTime,
					MAX(IF(prop.prop_name = "bcpg:sort",prop.long_value,NULL)) as sortOrder,
					MAX(IF(prop.prop_name = "cm:modified",prop.date_value,NULL)) as projectDateModified,
					DATEDIFF(MAX(IF(prop.prop_name = "pjt:tlEnd",prop.date_value,NULL)),MAX(IF(prop.prop_name = "pjt:tlStart",prop.date_value,NULL))) as duration,					
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
		
		<Dimension type="StandardDimension" foreignKey="entity_fact_id"   name="site" caption="${msg("jsolap.site.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}" primaryKey="id">
				<View alias="site">
						<SQL dialect="generic">
							<![CDATA[
							select
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								entity.is_last_version as isLastVersion,
								MAX(IF(prop.prop_name = "metadata:siteId",prop.string_value,NULL)) as siteId,
								MAX(IF(prop.prop_name = "metadata:siteName",prop.string_value,NULL)) as siteName,
								entity.id as id
							from
								 becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
								 entity.entity_type = "pjt:project" and entity.is_last_version = true and entity.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>		
				<Level name="site" caption="${msg("jsolap.site.title")}" column="siteName"  type="String" />
			</Hierarchy>
		</Dimension>			
		
		<Dimension  name="designation" caption="${msg("jsolap.designation.title")}" >
			<Hierarchy name="taskPerName" caption="${msg("jsolap.taskPerName.title")}" hasAll="true" allMemberCaption="${msg("jsolap.task.caption")}">
				<Level name="tlTaskName" caption="${msg("jsolap.task.title")}" column="tlTaskName" type="String" ordinalColumn="sortOrder" />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="state" caption="${msg("jsolap.state.title")}" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.state.caption")}" >
				<Level approxRowCount="5" name="tlState" caption="${msg("jsolap.state.title")}"  column="tlState"  type="String"    >
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
		
		<Dimension type="StandardDimension" foreignKey="entity_fact_id"  name="project" caption="${msg("jsolap.project.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.project.caption")}" primaryKey="id">
				<View alias="pjt">
						<SQL dialect="generic">
							<![CDATA[
							select
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								entity.is_last_version as isLastVersion,
								MAX(IF(prop.prop_name = "pjt:projectState",prop.string_value,NULL)) as projectState,
								MAX(IF(prop.prop_name = "pjt:projectHierarchy1",prop.string_value,NULL)) as projectHierarchy1,
								MAX(IF(prop.prop_name = "pjt:projectHierarchy2",prop.string_value,NULL)) as projectHierarchy2,
								MAX(IF(prop.prop_name = "pjt:projectManager",prop.string_value,NULL)) as projectManager,
								entity.id as id
							from
								 becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
								 entity.entity_type = "pjt:project" and entity.is_last_version = true and entity.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>	
			     <Level approxRowCount="5" name="projectState" caption="${msg("jsolap.state.title")}" column="projectState" type="String"   >
				     <NameExpression>
						  <SQL dialect="generic" >
						  <![CDATA[CASE WHEN projectState='Planned' THEN 'Plannifié'
		                            WHEN projectState='InProgress' THEN 'En cours'
		                            WHEN projectState='OnHold' THEN 'En attente'
		                            WHEN projectState='Cancelled' THEN 'Annulé'
		                            WHEN projectState='Completed' THEN 'Terminé'
		                            ELSE 'Vide'
		                           END]]></SQL>
	                </NameExpression>
				</Level>	
				<Level name="projectHierarchy1" caption="${msg("jsolap.family.title")}" column="projectHierarchy1" type="String"   >
				</Level>
				<Level name="projectHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="projectHierarchy2" type="String"   >
				</Level>
				<Level name="projectManager" caption="${msg("jsolap.projectManager.title")}" column="projectManager"  type="String"    >
				</Level>
				<Level name="entity_noderef" caption="${msg("jsolap.project.title")}" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension name="resource" caption="${msg("jsolap.resource.title")}" >
			<Hierarchy name="resource" caption="${msg("jsolap.resource.title")}" hasAll="true" allMemberCaption="${msg("jsolap.resource.caption")}">
				<Level name="tlResources" caption="${msg("jsolap.resource.title")}" column="tlResources"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<DimensionUsage name="tlStart" caption="Date début" source="timeDimension" foreignKey="tlStart" />
		<DimensionUsage name="tlEnd" caption="Date de fin" source="timeDimension" foreignKey="tlEnd" />
		<DimensionUsage name="projectDateModified" caption="${msg("jsolap.modificationDate.title")}" source="timeDimension"  foreignKey="projectDateModified" />	
		<Measure name="stepsNumber" caption="${msg("jsolap.stepsNumber.title")}" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
		<Measure name="averageForecastDurations" caption="${msg("jsolap.averageForecastDurations.title")}" column="tlDuration" datatype="Numeric" aggregator="avg" visible="true"  />
		<Measure name="averageActualDurations" caption="${msg("jsolap.averageActualDurations.title")}" column="duration" datatype="Numeric" aggregator="avg" visible="true"  />
		<Measure name="workload" caption="${msg("jsolap.workload.title")}" column="tlWork" datatype="Integer" aggregator="sum" visible="true"></Measure>
		<Measure name="loggedTime" caption="${msg("jsolap.loggedTime.title")}" column="tlLoggedTime" datatype="Integer" aggregator="sum" visible="true"></Measure>
		<Measure name="avgLoggedTime" caption="${msg("jsolap.avgLoggedTime.title")}" column="tlLoggedTime" datatype="Integer" aggregator="avg" visible="true"></Measure>		
		
		<CalculatedMember name="averageDurations" caption="${msg("jsolap.averageDurations.title")}" dimension="Measures" visible="true">
			<Formula>([Measures].[averageDurations],[designation.taskPerName].PrevMember) + ([Measures].[averageDurations])</Formula>
		</CalculatedMember>  
		
		
	</Cube>
	
	<Cube name="projectsEvaluation" caption="${msg("jsolap.projectsEvaluation.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.note.title")}">
		<View alias="projectScore">
			<SQL dialect="generic">
			<![CDATA[
				select
					datalist.id as id,
					datalist.datalist_id as noderef,
					datalist.entity_fact_id as entity_fact_id,
					datalist.is_last_version as isLastVersion,
					MAX(IF(prop.prop_name = "pjt:slCriterion",prop.string_value,NULL)) as slCriterion,
					MAX(IF(prop.prop_name = "pjt:slWeight",prop.long_value,NULL)) as slWeight,
					MAX(IF(prop.prop_name = "pjt:slScore",prop.long_value,NULL)) as slScore,
					datalist.instance_id as instance_id
				from
					becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
				where
					datalist.datalist_name = "scoreList" and datalist.item_type = "pjt:scoreList"
				group by 
					id
				]]>
			</SQL>
		</View>
		
		<Dimension type="StandardDimension" foreignKey="entity_fact_id"  name="site" caption="${msg("jsolap.site.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}" primaryKey="id">
				<View alias="site">
						<SQL dialect="generic">
							<![CDATA[
							select
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								entity.is_last_version as isLastVersion,
								MAX(IF(prop.prop_name = "metadata:siteId",prop.string_value,NULL)) as siteId,
								MAX(IF(prop.prop_name = "metadata:siteName",prop.string_value,NULL)) as siteName,
								entity.id as id
							from
								 becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
								 entity.entity_type = "pjt:project" and entity.is_last_version = true and entity.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>		
					<Level name="site" caption="${msg("jsolap.site.title")}" column="siteName"  type="String" />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="designation" caption="${msg("jsolap.designation.title")}" >
			<Hierarchy name="criterionByName" caption="${msg("jsolap.criterionByName.title")}" hasAll="true" allMemberCaption="${msg("jsolap.criterion.caption")}">
				<Level name="slCriterion" caption="${msg("jsolap.criterion.title")}" column="slCriterion"  type="String"    />
			</Hierarchy>
		</Dimension>		
		
		<Dimension type="StandardDimension" foreignKey="entity_fact_id"  name="project" caption="${msg("jsolap.project.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.project.caption")}" primaryKey="id">
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
								 entity.entity_type = "pjt:project" and entity.is_last_version = true and entity.instance_id = ${instanceId}
							group by id
							]]>
						</SQL>
					</View>		
				<Level name="projectHierarchy1" caption="${msg("jsolap.family.title")}" column="projectHierarchy1" type="String"   >
				</Level>
				<Level name="projectHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="projectHierarchy2" type="String"   >
				</Level>
				<Level name="projectManager" caption="${msg("jsolap.projectManager.title")}" column="projectManager"  type="String"    >
				</Level>
				<Level name="entity_noderef" caption="${msg("jsolap.project.title")}" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Measure name="slWeight" caption="${msg("jsolap.weighting.title")}" column="slWeight" datatype="Numeric" aggregator="sum" visible="true" />
		<Measure name="slScore" caption="${msg("jsolap.note.title")}" column="slScore" datatype="Numeric" aggregator="avg" visible="true"  />
		
		<CalculatedMember name="weightingNote" caption="${msg("jsolap.weightingNote.title")}" dimension="Measures" visible="true">
			<Formula>[Measures].[slWeight] * [Measures].[slScore] / 100</Formula>
		</CalculatedMember> 
	</Cube>

	<Cube name="projects" caption="${msg("jsolap.projects.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.projectsNumberDistinct.title")}">
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
					MAX(IF(prop.prop_name = "cm:creator",prop.date_value,NULL)) as projectCreator,
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
					entity.entity_type = "pjt:project" and entity.instance_id = ${instanceId} 
				group by 
					id
				]]>
			</SQL>
		</View>
		

		<Dimension type="StandardDimension" foreignKey="id" name="site" caption="${msg("jsolap.site.title")}">
			<Hierarchy hasAll="true" primaryKey="entity_id">
				<View alias="site">
						<SQL dialect="generic">
							<![CDATA[
							select
								entity.id as id,
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								MAX(IF(prop.prop_name = "metadata:siteId",prop.string_value,NULL)) as siteId,
								MAX(IF(prop.prop_name = "metadata:siteName",prop.string_value,NULL)) as siteName,
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
				<Level name="site" caption="${msg("jsolap.site.title")}" column="siteName"  type="String" />	
			</Hierarchy>
		</Dimension>

		
		<Dimension  name="designation" caption="${msg("jsolap.designation.title")}" >
			<Hierarchy name="projectPerFamily" caption="${msg("jsolap.projectPerFamily.title")}" hasAll="true" allMemberCaption="${msg("jsolap.project.caption")}">
				<Level name="projectHierarchy1" caption="${msg("jsolap.family.title")}" column="projectHierarchy1"  type="String"    />
				<Level name="projectHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="projectHierarchy2"  type="String"    />
				<Level name="name" caption="${msg("jsolap.projectName.title")}" column="name"  type="String"    />
				<Level name="code" caption="${msg("jsolap.projectCode.title")}" column="code"  type="String"    />
			</Hierarchy>
			<Hierarchy name="projectPerName" caption="${msg("jsolap.projectPerName.title")}" hasAll="true" allMemberCaption="${msg("jsolap.project.caption")}">
				<Level name="name" caption="${msg("jsolap.projectName.title")}" column="name"  type="String"    />
				<Level name="code" caption="${msg("jsolap.projectCode.title")}" column="code"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		
		<Dimension  name="priority" caption="${msg("jsolap.priority.title")}">
			<Hierarchy name="priority" caption="${msg("jsolap.priority.title")}" hasAll="true" allMemberCaption="${msg("jsolap.priority.caption")}">
				<Level name="projectPriority" caption="${msg("jsolap.priority.title")}" column="projectPriority"  type="String"    >
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
		<Dimension  name="state" caption="${msg("jsolap.state.title")}" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.state.caption")}" >
				<Level approxRowCount="5" name="projectState" caption="${msg("jsolap.state.title")}" column="projectState" type="String">
				  <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN projectState='Planned' THEN 'Plannifié'
	                            WHEN projectState='InProgress' THEN 'En cours'
	                            WHEN projectState='OnHold' THEN 'En attente'
	                            WHEN projectState='Cancelled' THEN 'Annulé'
	                            WHEN projectState='Completed' THEN 'Terminé'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension type="StandardDimension" foreignKey="id" name="entities" caption="${msg("jsolap.entities.title")}">
			<Hierarchy hasAll="true" primaryKey="entity_id">
				<View alias="projectEntity">
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
				<Level name="productHierarchy1" caption="${msg("jsolap.family.title")}" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="productHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="entity_noderef" caption="${msg("jsolap.entity.caption")}" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		
		<Dimension  name="history" caption="${msg("jsolap.history.title")}" >
			<Hierarchy name="currentVersion" caption="${msg("jsolap.currentVersion.title")}" hasAll="true"   defaultMember="[history.currentVersion].[true]">
				<Level name="isLastVersion" caption="${msg("jsolap.currentVersion.title")}" column="isLastVersion"  type="Boolean"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="projectManager" caption="${msg("jsolap.projectManager.title")}" >
			<Hierarchy name="projectManager" caption="${msg("jsolap.projectManager.title")}" hasAll="true" allMemberCaption="${msg("jsolap.projectManager.caption")}">
				<Level name="projectManager" caption="${msg("jsolap.projectManager.title")}" column="projectManager"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="projectCreator" caption="${msg("jsolap.projectCreator.title")}" >
			<Hierarchy name="projectCreator" caption="${msg("jsolap.projectCreator.title")}" hasAll="true" allMemberCaption="${msg("jsolap.projectCreator.caption")}">
				<Level name="projectCreator" caption="${msg("jsolap.projectCreator.title")}" column="projectCreator"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="ideaOrigin" caption="${msg("jsolap.ideaOrigin.title")}" >
			<Hierarchy name="ideaOrigin" caption="${msg("jsolap.ideaOrigin.title")}" hasAll="true" allMemberCaption="${msg("jsolap.ideaOrigin.caption")}">
				<Level name="projectOrigin" caption="${msg("jsolap.ideaOrigin.title")}" column="projectOrigin"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="sponsor" caption="${msg("jsolap.sponsor.title")}" >
			<Hierarchy name="sponsor" caption="${msg("jsolap.sponsor.title")}" hasAll="true" allMemberCaption="${msg("jsolap.sponsor.caption")}">
				<Level name="projectSponsor" caption="${msg("jsolap.sponsor.title")}" column="projectSponsor"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="projectModel" caption="${msg("jsolap.projectModel.title")}" >
			<Hierarchy name="projectModel" caption="${msg("jsolap.projectModel.title")}" hasAll="true" allMemberCaption="${msg("jsolap.projectModel.title")}">
				<Level name="entityTplRef" caption="${msg("jsolap.projectModel.title")}" column="entityTplRef"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		
		<DimensionUsage name="projectDateModified" caption="${msg("jsolap.modificationDate.title")}" source="timeDimension"  foreignKey="projectDateModified" />
	   	<DimensionUsage name="projectDateCreated" caption="${msg("jsolap.creationDate.title")}" source="timeDimension" foreignKey="projectDateCreated" />
		<DimensionUsage name="projectStartDate" caption="${msg("jsolap.startDate.title")}" source="timeDimension" foreignKey="projectStartDate" />
		<DimensionUsage name="projectDueDate" caption="${msg("jsolap.dueDate.title")}" source="timeDimension" foreignKey="projectDueDate" />
		<DimensionUsage name="completionDate" caption="${msg("jsolap.completionDate.title")}" source="timeDimension" foreignKey="completionDate" />
	

		<Measure name="projectsNumber" caption="${msg("jsolap.projectsNumber.title")}" column="id" datatype="Numeric" aggregator="count" visible="true" />
		<Measure name="projectsNumberDistinct" caption="${msg("jsolap.projectsNumberDistinct.title")}" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
		<Measure name="averageDuration" caption="${msg("jsolap.averageDuration.title")}" column="duration" datatype="Numeric" aggregator="avg" visible="true" />
		<Measure name="averageProgress" caption="${msg("jsolap.averageProgress.title")}" column="completionPercent" datatype="Numeric" aggregator="avg" visible="true"  />
		<Measure name="averageNote" caption="${msg("jsolap.averageNote.title")}" column="projectScore" datatype="Numeric" aggregator="avg" visible="true"  />
		<Measure name="delay" caption="${msg("jsolap.delay.title")}" column="projectOverdue" datatype="Numeric" aggregator="sum" visible="true"  />
		
		<CalculatedMember name="lastProgress" caption="${msg("jsolap.lastProgress.title")}" dimension="Measures" visible="true">
			<Formula>[Measures].[averageProgress],LastNonEmpty(YTD(),[Measures].[averageProgress])</Formula>
		</CalculatedMember> 
		
		<CalculatedMember name="cumulatedProjectNumber" caption="${msg("jsolap.cumulatedProjectNumber.title")}" dimension="Measures" visible="true">
			<Formula>SUM(YTD(),[Measures].[projectsNumberDistinct])</Formula>
		</CalculatedMember> 
	
	</Cube>
	
	<Cube name="nutrients" caption="${msg("jsolap.nutrients.title")}" cache="true" enabled="true" >
		<View alias="${msg("jsolap.nutrient.title")}">
			<SQL dialect="generic">
				select
								datalist.id as id,
								MAX(IF(prop.prop_name = "bcpg:nutListNut",prop.string_value,NULL)) as nutName,
								MAX(IF(prop.prop_name = "bcpg:nutListNut",prop.prop_id,NULL)) as nutNodeRef,
								MAX(IF(prop.prop_name = "bcpg:nutListGroup",prop.string_value,NULL)) as nutGroup,
								MAX(IF(prop.prop_name = "bcpg:nutListValue",prop.double_value,NULL)) as nutValue,
								MAX(IF(prop.prop_name = "bcpg:nutListFormulatedValue",prop.double_value,NULL)) as nutFormulatedValue,
								entity.is_last_version as isLastVersion,
								datalist.entity_fact_id as entity_fact_id
							from
									becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
									                LEFT JOIN becpg_entity AS entity ON datalist.entity_fact_id = entity.id
							where datalist.datalist_name = "nutList" and datalist.item_type = "bcpg:nutList" and datalist.instance_id = ${instanceId} 
							      and  entity.is_last_version = true
							group by datalist.id

			</SQL>
		</View>

		<Dimension  name="site" caption="${msg("jsolap.site.title")}" type="StandardDimension" foreignKey="entity_fact_id" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}" primaryKey="id">
			<View alias="site">
					<SQL dialect="generic">
							select
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								entity.is_last_version as isLastVersion,								
								MAX(IF(prop.prop_name = "metadata:siteId",prop.string_value,NULL)) as siteId,
								MAX(IF(prop.prop_name = "metadata:siteName",prop.string_value,NULL)) as siteName,
								entity.id as id
							from
								 becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
								 entity.is_last_version = true and entity.instance_id = ${instanceId}
							group by id
					</SQL>
				</View>
				<Level name="site" caption="${msg("jsolap.site.title")}" column="siteName"  type="String" />
			</Hierarchy>
		</Dimension>

		<Dimension  name="designation" caption="${msg("jsolap.designation.title")}" type="StandardDimension" foreignKey="entity_fact_id" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.products.caption")}" primaryKey="id">
			<View alias="${msg("jsolap.designation.title")}">
					<SQL dialect="generic">
							select
								entity.entity_id as entity_noderef,
								entity.entity_name as name,
								entity.is_last_version as isLastVersion,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy1",prop.string_value,NULL)) as productHierarchy1,
								MAX(IF(prop.prop_name = "bcpg:productHierarchy2",prop.string_value,NULL)) as productHierarchy2,
								MAX(IF(prop.prop_name = "bcpg:code",prop.string_value,NULL)) as code,
								MAX(IF(prop.prop_name = "bcpg:erpCode",prop.string_value,NULL)) as erpCode,
								MAX(IF(prop.prop_name = "bcpg:legalName",prop.string_value,NULL)) as legalName,
								entity.id as id
							from
								 becpg_entity AS entity LEFT JOIN becpg_property AS prop ON prop.entity_id = entity.id
							where
								 entity.is_last_version = true and entity.instance_id = ${instanceId}
							group by id
					</SQL>
				</View>
				<Level name="productHierarchy1" caption="${msg("jsolap.family.title")}" column="productHierarchy1"  type="String"    />
				<Level name="productHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="productHierarchy2"  type="String"    />
				<Level name="name" caption="${msg("jsolap.productName.title")}" column="name"  type="String"    />
				<Level name="code" caption="${msg("jsolap.productCode.title")}" column="code"  type="String"   />
				<Level name="erpCode" caption="${msg("jsolap.erpCode.title")}" column="erpCode"  type="String"   />
				<Level name="legalName" caption="${msg("jsolap.legalName.title")}" column="legalName" type="String"    />
			</Hierarchy>
		</Dimension>

		<Dimension type="StandardDimension" foreignKey="id"  name="nutrient" caption="${msg("jsolap.nutrient.title")}">
			<Hierarchy name="nutrientPerGroup" caption="${msg("jsolap.nutrientPerGroup.title")}" hasAll="true" allMemberCaption="${msg("jsolap.nutrient.caption")}" primaryKey="entity_fact_id">
				<Level approxRowCount="3" name="nutGroup" caption="${msg("jsolap.nutrientGroup.title")}" column="nutGroup" type="String"   >
				</Level>
				<Level  name="nutNodeRef" caption="${msg("jsolap.nutrient.title")}" column="nutNodeRef"  nameColumn="nutName" type="String"   ></Level>
			</Hierarchy>	
		</Dimension>	
		<Measure name="nutValue" caption="${msg("jsolap.nutritionalValues.title")}" column="nutValue" datatype="Numeric" aggregator="avg" visible="true"></Measure>	
		<Measure name="nutFormulatedValue" caption="${msg("jsolap.nutritionalFormulatedValues.title")}" column="nutFormulatedValue" datatype="Numeric" aggregator="avg" visible="true"></Measure>	
		</Cube>				
	

	<Cube name="products" caption="${msg("jsolap.products.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.productsNumber.title")}">
		<View alias="${msg("jsolap.product.title")}">
			<SQL dialect="generic">
				select
					entity.id as id,
					entity.entity_id as noderef,
					entity.entity_name as name,
					entity.entity_type as productType,
					entity.is_last_version as isLastVersion,
					MAX(IF(prop.prop_name = "bcpg:productHierarchy1",prop.string_value,NULL)) as productHierarchy1,
					MAX(IF(prop.prop_name = "bcpg:productHierarchy2",prop.string_value,NULL)) as productHierarchy2,
					MAX(IF(prop.prop_name = "metadata:siteId",prop.string_value,NULL)) as siteId,
					MAX(IF(prop.prop_name = "metadata:siteName",prop.string_value,NULL)) as siteName,
					MAX(IF(prop.prop_name = "bcpg:code",prop.string_value,NULL)) as code,
					MAX(IF(prop.prop_name = "bcpg:erpCode",prop.string_value,NULL)) as erpCode,
					MAX(IF(prop.prop_name = "bcpg:legalName",prop.string_value,NULL)) as legalName,
					MAX(IF(prop.prop_name = "bcpg:nutrientProfilingClass",prop.string_value,NULL)) as nutrientProfilingClass,
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
		
		<Dimension name="site" caption="${msg("jsolap.site.title")}">
			<Hierarchy name="site" caption="${msg("jsolap.site.title")}" hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}">
				<Level name="site" caption="${msg("jsolap.site.title")}" column="siteName"  type="String" />
			</Hierarchy>
		</Dimension>

		<Dimension  name="designation" caption="${msg("jsolap.designation.title")}" >
			<Hierarchy name="productPerFamily" caption="${msg("jsolap.productPerFamily.title")}" hasAll="true" allMemberCaption="${msg("jsolap.product.caption")}">
				<Level name="productHierarchy1" caption="${msg("jsolap.family.title")}" column="productHierarchy1"  type="String"    />
				<Level name="productHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="productHierarchy2"  type="String"    />
				<Level name="code" caption="${msg("jsolap.productCode.title")}" column="code"  type="String"  uniqueKeyLevelname="" highCardinality="true"  />
				<Level name="name" caption="${msg("jsolap.productName.title")}" column="name"  type="String" />
			    <Level name="erpCode" caption="${msg("jsolap.erpCode.title")}" column="erpCode"  type="String" />
				<Level name="legalName" caption="${msg("jsolap.legalName.title")}" column="legalName"  type="String" />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="state" caption="${msg("jsolap.state.title")}" foreignKey="productState">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.state.caption")}" primaryKey="product_state">
				<Table name="becpg_product_state" />
				<Level approxRowCount="5" name="product_state" caption="${msg("jsolap.state.title")}" table="becpg_product_state" column="product_state" nameColumn="product_label" type="String"    />
			</Hierarchy>
		</Dimension>
		
		 
		<Dimension foreignKey="id"  name="geoOrigin" caption="${msg("jsolap.geoOrigin.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.geoOrigin.caption")}" primaryKey="entity_fact_id">
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
				<Level name="name" caption="${msg("jsolap.country.title")}" column="nodeRef" nameColumn="name" type="String"  >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension foreignKey="productType"  name="productType" caption="${msg("jsolap.productType.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.productType.caption")}" primaryKey="entity_type" >
			<Table name="becpg_entity_type"></Table>
			<Level approxRowCount="10" name="entity_type" caption="${msg("jsolap.type.title")}" column="entity_type" nameColumn="entity_label" type="String"   ></Level>
			</Hierarchy>
		</Dimension>
		<Dimension type="StandardDimension" foreignKey="id" name="client" caption="${msg("jsolap.client.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.client.caption")}" primaryKey="entity_id">
				<View alias="client">
					<SQL dialect="generic">
						select
							prop.prop_id as nodeRef,
							prop.string_value as name,
							prop.entity_id
						from
							 becpg_property AS prop
						where prop.prop_name = "bcpg:clients"
					</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.clientName.title")}" nameColumn="name" column="nodeRef"  type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		<Dimension type="StandardDimension" foreignKey="id"  name="supplier" caption="${msg("jsolap.supplier.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.supplier.caption")}" primaryKey="entity_id">
				<View alias="supplier">
					<SQL dialect="generic">
						select
							prop.prop_id as nodeRef,
							prop.string_value as name,
							prop.entity_id
						from
							 becpg_property AS prop
						where prop.prop_name = "bcpg:suppliers"
					</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.supplierName.title")}" nameColumn="name" column="nodeRef" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		<Dimension type="StandardDimension" foreignKey="id"  name="nutrient" caption="${msg("jsolap.nutrient.title")}">
			<Hierarchy name="nutrientPerGroup" caption="${msg("jsolap.nutrientPerGroup.title")}" hasAll="true" allMemberCaption="${msg("jsolap.nutrient.caption")}" primaryKey="entity_fact_id">
				<View alias="nutList">
					<SQL dialect="generic">
							select
								datalist.id as id,
								MAX(IF(prop.prop_name = "bcpg:nutListNut",prop.string_value,NULL)) as nutName,
								MAX(IF(prop.prop_name = "bcpg:nutListNut",prop.prop_id,NULL)) as nutNodeRef,
								MAX(IF(prop.prop_name = "bcpg:nutListGroup",prop.string_value,NULL)) as nutGroup,
								MAX(IF(prop.prop_name = "bcpg:nutListValue",prop.double_value,NULL)) as nutValue,								
								datalist.entity_fact_id as entity_fact_id
							from
									becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
							where datalist.datalist_name = "nutList" and datalist.item_type = "bcpg:nutList" and datalist.instance_id = ${instanceId}
								group by datalist.id
					</SQL>
				</View>
				<Level approxRowCount="3" name="nutGroup" caption="${msg("jsolap.nutrientGroup.title")}" column="nutGroup" type="String"   >
				</Level>
				<Level approxRowCount="20" name="nutName" caption="${msg("jsolap.nutrient.title")}" column="nutNodeRef"  nameColumn="nutName" type="String"   >
				</Level>
			</Hierarchy>	
		</Dimension>
	    <Dimension name="nutritionScale" caption="${msg("jsolap.nutritionScale.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.nutritionScale.caption")}" >
				<Level name="nutrientProfilingClass" caption="${msg("jsolap.nutritionClass.title")}" column="nutrientProfilingClass"  type="String"    />
			</Hierarchy>
		</Dimension>

		<Dimension type="StandardDimension" foreignKey="id" name="allergenVoluntary" caption="${msg("jsolap.allergenVoluntary.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.allergen.caption")}" primaryKey="entity_fact_id" >
				<View alias="allergenList">
					<SQL dialect="generic">
   							select  
								prop.prop_id  as nodeRef,
								prop.string_value name,
								datalist.entity_fact_id as entity_fact_id
							from
								becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
  		                        LEFT JOIN becpg_property AS prop2
        	                    ON prop2.datalist_id = datalist.id
              		            AND prop2.prop_name = "bcpg:allergenListVoluntary"
							where datalist.datalist_name = "allergenList"
								and datalist.item_type = "bcpg:allergenList"
								and prop.prop_name = "bcpg:allergenListAllergen"
								and prop2.boolean_value = true
								and datalist.instance_id = ${instanceId}
							group by datalist.id
					</SQL>
				</View>
				<Level approxRowCount="100" name="name" caption="${msg("jsolap.allergenVoluntary.title")}" column="nodeRef"  nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>

		<Dimension type="StandardDimension" foreignKey="id"  name="allergenInVoluntary" caption="${msg("jsolap.allergenInVoluntary.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.allergen.caption")}" primaryKey="entity_fact_id" >
				<View alias="allergenInList">
					<SQL dialect="generic">
							select  
								prop.prop_id  as nodeRef,
								prop.string_value   as name,							
								datalist.entity_fact_id as entity_fact_id
							from
								becpg_datalist AS datalist LEFT JOIN becpg_property AS prop ON prop.datalist_id = datalist.id
                         		LEFT JOIN becpg_property AS prop2
                          		ON prop2.datalist_id = datalist.id
                          		AND prop2.prop_name = "bcpg:allergenListInVoluntary"
							where datalist.datalist_name = "allergenList"
								and datalist.item_type = "bcpg:allergenList"
								and prop.prop_name = "bcpg:allergenListAllergen"
								and prop2.boolean_value = true
								and datalist.instance_id = ${instanceId}
							group by datalist.id
					</SQL>
				</View>
				<Level approxRowCount="100" name="name" caption="${msg("jsolap.allergenInVoluntary.title")}" column="nodeRef"  nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>							
		
		
		<Dimension type="StandardDimension" foreignKey="id"  name="ingredient" caption="${msg("jsolap.ingredient.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.ingredient.caption")}" primaryKey="entity_fact_id">
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
				<Level name="name" caption="${msg("jsolap.ingredient.title")}" column="nodeRef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		
		<Dimension type="StandardDimension" foreignKey="id"  name="composition" caption="${msg("jsolap.composition.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.composition.caption")}" primaryKey="entity_fact_id">
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
										and entity.is_last_version = true
							group by datalist.id
							]]>
						</SQL>
					</View>		
				<Level name="productHierarchy1" caption="${msg("jsolap.family.title")}" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="productHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="entity_noderef" caption="${msg("jsolap.component.title")}" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
	
		<Dimension type="StandardDimension" foreignKey="id"  name="packaging" caption="${msg("jsolap.packaging.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.packaging.caption")}" primaryKey="entity_fact_id">
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
										and entity.is_last_version = true
							group by datalist.id
							]]>
						</SQL>
					</View>	
				<Level name="productHierarchy1" caption="${msg("jsolap.family.title")}" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="productHierarchy2" caption="${msg("jsolap.subFamily.title")}" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="entity_noderef" caption="${msg("jsolap.packaging.title")}" column="entity_noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		

	    <Dimension  name="history" caption="${msg("jsolap.history.title")}"  >
			<Hierarchy name="currentVersion" caption="${msg("jsolap.currentVersion.title")}" hasAll="true" defaultMember="[history.currentVersion].[true]" >
				<Level name="isLastVersion" caption="${msg("jsolap.currentVersion.title")}" column="isLastVersion"  type="Boolean"    />
			</Hierarchy>
		</Dimension>
		

		<DimensionUsage name="productDateCreated" caption="${msg("jsolap.creationDate.title")}" source="timeDimension" foreignKey="productDateCreated" />
		<DimensionUsage name="productDateModified" caption="${msg("jsolap.modificationDate.title")}" source="timeDimension" foreignKey="productDateModified" />
		<DimensionUsage name="startEffectivity" caption="${msg("jsolap.effectivityStart.title")}" source="timeDimension" foreignKey="startEffectivity" />
		<DimensionUsage name="endEffectivity" caption="${msg("jsolap.effectivityEnd.title")}" source="timeDimension" foreignKey="endEffectivity" />
		
		<Measure name="productNumber" caption="${msg("jsolap.productNumber.title")}" column="noderef" datatype="Integer" aggregator="distinct-count" visible="true" />
		<Measure name="projectedQty" caption="${msg("jsolap.plannedQuantity.title")}" column="projectedQty" datatype="Integer" aggregator="sum" visible="true">
		</Measure>
		<Measure name="unitTotalCost" caption="${msg("jsolap.costs.title")}" column="unitTotalCost" datatype="Numeric" aggregator="sum" visible="true" >
		</Measure>
		<Measure name="profitability" caption="${msg("jsolap.unitProfitability.title")}" column="profitability" datatype="Numeric" aggregator="sum" visible="true">
		</Measure>
		<Measure name="unitPrice" caption="${msg("jsolap.unitPrice.title")}" column="unitPrice" datatype="Numeric" aggregator="sum" visible="true">
		</Measure>
		<CalculatedMember name="profit" caption="${msg("jsolap.profit.title")}" dimension="Measures" visible="true">
			<Formula>([Measures].[unitPrice] - [Measures].[unitTotalCost])*[Measures].[projectedQty]
			</Formula>
		</CalculatedMember>
		<CalculatedMember name="profitability" caption="${msg("jsolap.profitability.title")}" dimension="Measures" visible="true">
			<Formula>([Measures].[unitPrice] - [Measures].[unitTotalCost])/[Measures].[unitTotalCost]</Formula>
		</CalculatedMember>
	</Cube>

	
</Schema>