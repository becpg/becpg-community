<#assign instanceId = args.instance />
<#assign isAdmin = people.isAdmin(people.getPerson(person.properties["cm:userName"])) && person.properties["cm:userName"] == "admin@demo.becpg.fr" && (instanceId == "2" || instanceId == "3")/>
<?xml version="1.0" encoding="UTF-8"?>
<Schema name="beCPG OLAP Schema"> 
	

	<Dimension type="TimeDimension" name="timeDimension" caption="${msg("jsolap.timeDimension.title")}">	
		<Hierarchy name="date" hasAll="true" allMemberName="${msg("jsolap.allPeriods.title")}" allMemberCaption="${msg("jsolap.date.caption")}"  primaryKey="id" caption="${msg("jsolap.date.title")}">
			<View name="olapDate" alias="olapDate">
				<SQL dialect="generic">
					select
						id,
						Year,
						NWeek,
						concat ( 'W', Week ,'/', Year) enNWeek,
						NQuarter,
						concat ( 'Q', Quarter,'/', Year) enNQuarter,
						Month,
						Day,
						NDay,
						NDayUsFormat
					from
						becpg_dimdate
				</SQL>
			</View>
			<Level name="Year" caption="${msg("jsolap.year.title")}" column="Year" type="Numeric"  levelType="TimeYears"  />
			<Level approxRowCount="12" name="Month" caption="${msg("jsolap.month.title")}" column="Month" type="Numeric" levelType="TimeMonths" >
				<NameExpression>
					<SQL dialect="generic" >
					  <![CDATA[CASE WHEN Month='1' THEN '${msg("jsolap.month.january.title")[0..2]}'
	                            WHEN Month='2' THEN '${msg("jsolap.month.february.title")[0..2]}'
	                            WHEN Month='3' THEN '${msg("jsolap.month.march.title")[0..2]}'
	                            WHEN Month='4' THEN '${msg("jsolap.month.april.title")[0..2]}'
	                            WHEN Month='5' THEN '${msg("jsolap.month.may.title")[0..2]}'
	                            WHEN Month='6' THEN '${msg("jsolap.month.june.title")[0..2]}'
	                            WHEN Month='7' THEN '${msg("jsolap.month.july.title")[0..2]}'
	                            WHEN Month='8' THEN '${msg("jsolap.month.august.title")[0..2]}'
	                            WHEN Month='9' THEN '${msg("jsolap.month.septembre.title")[0..2]}'
	                            WHEN Month='10' THEN '${msg("jsolap.month.october.title")[0..2]}'
	                            WHEN Month='11' THEN '${msg("jsolap.month.november.title")[0..2]}'
	                            WHEN Month='12' THEN '${msg("jsolap.month.december.title")[0..2]}'
	                            ELSE 'Vide'
	                           END]]></SQL>
				</NameExpression>
			</Level>
			<#if .locale == "fr" >
				<Level name="Week" caption="${msg("jsolap.week.title")}" column="nWeek" type="String"  levelType="TimeWeeks"  />
				<Level name="Quarter" caption="${msg("jsolap.quarter.title")}" column="nQuarter" type="String"  levelType="TimeQuarters"  />
			<#else>
				<Level name="Week" caption="${msg("jsolap.week.title")}" column="enNWeek" type="String"  levelType="TimeWeeks"  />
				<Level name="Quarter" caption="${msg("jsolap.quarter.title")}" column="enNQuarter" type="String"  levelType="TimeQuarters"  />
			</#if>
			<#if .locale == "en_US" >
				<Level name="Day" caption="${msg("jsolap.day.title")}" column="Day" nameColumn="NDayUsFormat" ordinalColumn="Day" type="Numeric"  levelType="TimeDays"  />
			<#else>
				<Level name="Day" caption="${msg("jsolap.day.title")}" column="Day" nameColumn="NDay" ordinalColumn="Day" type="Numeric"  levelType="TimeDays"  />
			</#if>
		</Hierarchy>		
	</Dimension>
	
	<#if isAdmin>
	<Dimension name="instancesDimension" caption="${msg("jsolap.instance.title")}">
		<Hierarchy name="instancesInfo" hasAll="true" allMemberCaption="${msg("jsolap.instance.caption")}"  defaultMember="[instancesDimension.instancesInfo].[${instanceId}]" caption="${msg("jsolap.instance.title")}" primaryKey="id">
			<Table name="becpg_instance" />
			<Level name="instance_id" column="id" visible="false" />
			<Level name="instance_name" column="instance_name" caption="${msg("jsolap.instance.title")}" />
			<Level name="tenant_name" column="tenant_name" caption="${msg("jsolap.tenant.title")}" />
		</Hierarchy>
	</Dimension>
	</#if>
	
	
	<Dimension  name="tagsDimension" caption="${msg("jsolap.tags.title")}" >
		<Hierarchy name="tags" hasAll="true" allMemberCaption="${msg("jsolap.tags.caption")}" primaryKey="entityNodeRef">		
				<View name="tags" alias="tags">
					<SQL dialect="generic">
						select  
							entityNodeRef,
							doc->>"$.name" as name,
							nodeRef
						from
							assoc_cm_taggable
						<#if !isAdmin>	
						  where instanceId = ${instanceId}
						</#if>
					</SQL>
				</View>
				<Level name="tag" caption="${msg("jsolap.tag.title")}" table="tags" column="nodeRef" nameColumn="name" type="String" ></Level>
			</Hierarchy>
		</Dimension>
		
		
	
		<Dimension name="clientsDimension" caption="${msg("jsolap.client.title")}">
			<Hierarchy name="clients" hasAll="true" allMemberCaption="${msg("jsolap.client.caption")}" primaryKey="entityNodeRef">
				<View name="clients" alias="clients">
								<SQL dialect="generic">
									select  
										a.entityNodeRef as entityNodeRef,
										a.doc->>"$.name" as name,
										a.nodeRef as nodeRef,
										b.doc->>"$.bcpg_clientState" as clientState,
										b.doc->>"$.bcpg_clientHierarchy1[0]" as clientHierarchy1,
										b.doc->>"$.bcpg_clientHierarchy2[0]" as clientHierarchy2
									from
										assoc_bcpg_clients a left join bcpg_client b on a.nodeRef = b.nodeRef	
									<#if !isAdmin>	
									  where b.instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.clientName.title")}" table="clients" nameColumn="name" column="nodeRef"  type="String"   >
				</Level>
				<Level name="family" caption="${msg("jsolap.clientFamily.title")}" column="clientHierarchy1" type="String">
				</Level>
				<Level name="subfamily" caption="${msg("jsolap.clientSubFamily.title")}" column="clientHierarchy2" type="String">
				</Level>
				<Level name="state" caption="${msg("jsolap.clientState.title")}" column="clientState" type="String">
				</Level>
			</Hierarchy>
		</Dimension>	
		
		
		<Dimension  name="productsDimension" caption="${msg("jsolap.products.title")}">
			<Hierarchy name="products" hasAll="true" allMemberCaption="${msg("jsolap.products.caption")}" primaryKey="nodeRef">		
				<View name="products_dim" alias="products_dim">
					<SQL dialect="generic">
						select
							nodeRef,
							doc->>"$.cm_name" as name,
							doc->>"$.bcpg_productHierarchy1[0]" as productHierarchy1,
							doc->>"$.bcpg_productHierarchy2[0]" as productHierarchy2,
							doc->>"$.bcpg_code" as code,
							doc->>"$.bcpg_erpCode" as erpCode,
							doc->>"$.bcpg_legalName" as legalName,
							doc->>"$.bcpg_productState" as productState,
							doc->>"$.bcpg_productType" as productType,
							doc->>"$.cm_versionLabel" as versionLabel
						from
							bcpg_product
						<#if !isAdmin>	
						 where 
						    instanceId = ${instanceId}
						</#if>
					</SQL>
				</View>
	
				<Level approxRowCount="5" name="productState" caption="${msg("jsolap.productState.title")}" table="products_dim" column="productState"  type="String"   >
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
				<Level approxRowCount="10" name="entity_type" caption="${msg("jsolap.productType.title")}" table="products_dim" column="productType" nameColumn="productType" type="String"   >
					<NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN productType='bcpg:rawMaterial' THEN "${msg('bcpg_bcpgmodel.type.bcpg_rawMaterial.title')}"
	                            WHEN productType='bcpg:finishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_finishedProduct.title')}"
	                            WHEN productType='bcpg:semiFinishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_semiFinishedProduct.title')}"
	                            WHEN productType='bcpg:packagingMaterial' THEN "${msg('bcpg_bcpgmodel.type.bcpg_packagingMaterial.title')}"
	                            WHEN productType='bcpg:packagingKit' THEN "${msg('bcpg_bcpgmodel.type.bcpg_packagingKit.title')}"
	                            WHEN productType='bcpg:localSemiFinishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_localSemiFinishedProduct.title')}"
	                            WHEN productType='bcpg:resourceProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_resourceProduct.title')}"
	                            ELSE 'Vide'
	                           END]]></SQL>
             		 </NameExpression>
				</Level>		
				<Level name="productHierarchy1" caption="${msg("jsolap.productFamily.title")}" table="products_dim" column="productHierarchy1"  type="String"    />
				<Level name="productHierarchy2" caption="${msg("jsolap.productSubFamily.title")}" table="products_dim" column="productHierarchy2"  type="String"    />
				<Level name="name" caption="${msg("jsolap.productName.title")}" table="products_dim" column="name"  type="String"    />
				<Level name="code" caption="${msg("jsolap.productCode.title")}" table="products_dim" column="code"  type="String"   />
				<Level name="erpCode" caption="${msg("jsolap.productErpCode.title")}" table="products_dim" column="erpCode"  type="String"   />
				<Level name="legalName" caption="${msg("jsolap.productLegalName.title")}" table="products_dim" column="legalName" type="String"    />
				<Level name="versionLabel" caption="${msg("jsolap.productVersionLabel.title")}" table="products_dim" column="versionLabel" type="String" />
			</Hierarchy>
			
		</Dimension>
		
		
		
		<Dimension name="projectsDimension" caption="${msg("jsolap.project.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.project.caption")}" primaryKey="nodeRef">
				<View name="projects_dim" alias="projects_dim">
					<SQL dialect="generic">
						select
							nodeRef,
							doc->>"$.cm_name" as name,
							doc->>"$.pjt_projectHierarchy1[0]" as	projectHierarchy1,
							doc->>"$.pjt_projectHierarchy2[0]" as	projectHierarchy2	
						from
							pjt_project
						<#if !isAdmin>	
						 where 
						    instanceId = ${instanceId}
						</#if>
					</SQL>
				 </View>
				<Level name="projectHierarchy1" caption="${msg("jsolap.projectFamily.title")}" table="projects_dim" column="projectHierarchy1" type="String"   >
				</Level>
				<Level name="projectHierarchy2" caption="${msg("jsolap.projectSubFamily.title")}" table="projects_dim" column="projectHierarchy2" type="String"   >
				</Level>
				<Level name="entity_noderef" caption="${msg("jsolap.project.title")}" table="projects_dim" column="noderef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="projectStateDimension" caption="${msg("jsolap.projectState.title")}" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.projectState.caption")}" primaryKey="nodeRef" >
				<View name="project_state" alias="project_state">
					<SQL dialect="generic">
						select
							nodeRef,
							doc->>"$.pjt_projectState" as projectState
						from
							pjt_project
						<#if !isAdmin>	
						 where 
						    instanceId = ${instanceId}
						</#if>
					</SQL>
				 </View>				 
				<Level approxRowCount="5" name="projectState" caption="${msg("jsolap.projectState.title")}" column="projectState" type="String">
				  <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN projectState='Planned' THEN '${msg("listconstraint.pjt_projectStates.Planned")}'
	                            WHEN projectState='InProgress' THEN '${msg("listconstraint.pjt_projectStates.InProgress")}'
	                            WHEN projectState='OnHold' THEN '${msg("listconstraint.pjt_projectStates.OnHold")}'
	                            WHEN projectState='Cancelled' THEN '${msg("listconstraint.pjt_projectStates.Cancelled")}'
	                            WHEN projectState='Completed' THEN '${msg("listconstraint.pjt_projectStates.Completed")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		
			<Dimension name="projectManagerDimension" caption="${msg("jsolap.projectManager.title")}">
			<Hierarchy hasAll="true" caption="${msg("jsolap.projectManager.title")}" allMemberCaption="${msg("jsolap.projectManager.caption")}" primaryKey="nodeRef">
				<View name="project_manager_dim" alias="project_manager_dim">
					<SQL dialect="generic">
						select
							nodeRef,
							doc->>"$.pjt_projectManager[0]" as projectManager
						from
							pjt_project
						<#if !isAdmin>	
						 where 
						    instanceId = ${instanceId}
						</#if>
					</SQL>
				 </View>
				<Level name="projectManager" caption="${msg("jsolap.projectManager.title")}" column="projectManager"  type="String" />
			</Hierarchy>
		</Dimension>
		
	
	<Cube name="software_usage" caption="${msg("jsolap.statistics.title")}" cache="false" enabled="true">
		<#if isAdmin>
			<Table name="becpg_public_statistics" />
		<#else>
			<Table name="becpg_public_statistics" alias="becpg_public_statistics" >
				<SQL dialect="generic">
					becpg_public_statistics.instance_id = ${instanceId}
				</SQL>
			</Table>
		</#if>
		
		<Dimension type="TimeDimension" name="frequency" caption="${msg("jsolap.date.title")}" foreignKey="olap_date">	
			<Hierarchy name="date" hasAll="true" allMemberName="${msg("jsolap.allPeriods.title")}" allMemberCaption="${msg("jsolap.date.caption")}"  primaryKey="Date" caption="${msg("jsolap.date.title")}">
				<View name="olapDate" alias="olapDate">
					<SQL dialect="generic">
						select
							id,
							Date,
							Year,
							NWeek,
							concat ( 'W', Week ,'/', Year) enNWeek,
							NQuarter,
							concat ( 'Q', Quarter,'/', Year) enNQuarter,
							Month,
							Day,
							NDay,
							NDayUsFormat
						from
							becpg_dimdate
					</SQL>
				</View>
				<Level name="Year" caption="${msg("jsolap.year.title")}" column="Year" type="Numeric"  levelType="TimeYears"  />
				<Level approxRowCount="12" name="Month" caption="${msg("jsolap.month.title")}" column="Month" type="Numeric" levelType="TimeMonths" >
					<NameExpression>
						<SQL dialect="generic" >
						  <![CDATA[CASE WHEN Month='1' THEN '${msg("jsolap.month.january.title")[0..2]}'
		                            WHEN Month='2' THEN '${msg("jsolap.month.february.title")[0..2]}'
		                            WHEN Month='3' THEN '${msg("jsolap.month.march.title")[0..2]}'
		                            WHEN Month='4' THEN '${msg("jsolap.month.april.title")[0..2]}'
		                            WHEN Month='5' THEN '${msg("jsolap.month.may.title")[0..2]}'
		                            WHEN Month='6' THEN '${msg("jsolap.month.june.title")[0..2]}'
		                            WHEN Month='7' THEN '${msg("jsolap.month.july.title")[0..2]}'
		                            WHEN Month='8' THEN '${msg("jsolap.month.august.title")[0..2]}'
		                            WHEN Month='9' THEN '${msg("jsolap.month.septembre.title")[0..2]}'
		                            WHEN Month='10' THEN '${msg("jsolap.month.october.title")[0..2]}'
		                            WHEN Month='11' THEN '${msg("jsolap.month.november.title")[0..2]}'
		                            WHEN Month='12' THEN '${msg("jsolap.month.december.title")[0..2]}'
		                            ELSE 'Vide'
		                           END]]></SQL>
					</NameExpression>
				</Level>
				<#if .locale == "fr" >
					<Level name="Week" caption="${msg("jsolap.week.title")}" column="nWeek" type="String"  levelType="TimeWeeks"  />
					<Level name="Quarter" caption="${msg("jsolap.quarter.title")}" column="nQuarter" type="String"  levelType="TimeQuarters"  />
				<#else>
					<Level name="Week" caption="${msg("jsolap.week.title")}" column="enNWeek" type="String"  levelType="TimeWeeks"  />
					<Level name="Quarter" caption="${msg("jsolap.quarter.title")}" column="enNQuarter" type="String"  levelType="TimeQuarters"  />
				</#if>
				<#if .locale == "en_US" >
					<Level name="Day" caption="${msg("jsolap.day.title")}" column="Day" nameColumn="NDayUsFormat" ordinalColumn="Day" type="Numeric"  levelType="TimeDays"  />
				<#else>
					<Level name="Day" caption="${msg("jsolap.day.title")}" column="Day" nameColumn="NDay" ordinalColumn="Day" type="Numeric"  levelType="TimeDays"  />
				</#if>
			</Hierarchy>		
		</Dimension>
		
		<#if isAdmin>
			<DimensionUsage name="instance" caption="${msg("jsolap.instance.title")}" source="instancesDimension" foreignKey="instance_id" />
		</#if>		
	
		<Measure name="avgUsers" caption="${msg("jsolap.usersAvg.title")}" column="connected_users" datatype="Numeric" aggregator="avg" visible="true"></Measure>
		<Measure name="maxUsers" caption="${msg("jsolap.usersMax.title")}" column="connected_users" datatype="Numeric" aggregator="max" visible="true"></Measure>
		<Measure name="maxLastHttpRespTime" caption="${msg("jsolap.lastHttpRespTime.title")}" column="latency" aggregator="max" datatype="Numeric" visible="true"></Measure>
		<Measure name="avgMemory" caption="${msg("jsolap.memoryAvg.title")}" datatype="Numeric" aggregator="avg" visible="true">
			<MeasureExpression>
				<SQL dialect="generic">
					total_memory - free_memory
				</SQL>
			</MeasureExpression>
		</Measure>
		<Measure name="maxMemory" caption="${msg("jsolap.memoryMax.title")}" datatype="Numeric" aggregator="max" visible="true">
			<MeasureExpression>
				<SQL dialect="generic">
					total_memory - free_memory
				</SQL>
			</MeasureExpression>
		</Measure>
		<Measure name="avgMemoryPct" caption="${msg("jsolap.mempctAvg.title")}" datatype="Numeric" aggregator="avg" visible="true">
			<MeasureExpression>
				<SQL dialect="generic">
					(1 - free_memory/total_memory)*100
				</SQL>
			</MeasureExpression>
		</Measure>
		<Measure name="maxMemoryPct" caption="${msg("jsolap.mempctMax.title")}" datatype="Numeric" aggregator="max" visible="true">
			<MeasureExpression>
				<SQL dialect="generic">
					(1 - free_memory/total_memory)*100
				</SQL>
			</MeasureExpression>
		</Measure>
		
		<Measure name="avgOsMemory" caption="${msg("jsolap.osMemoryAvg.title")}" column="system_free_memory" datatype="Numeric" aggregator="avg" visible="true"></Measure>
		<Measure name="maxOsMemory" caption="${msg("jsolap.osMemoryMax.title")}" column="system_free_memory" datatype="Numeric" aggregator="max" visible="true"></Measure>
		
		<Measure name="avgConcurrentRead" caption="${msg("jsolap.concurrentReadAvg.title")}" column="concurrentReadUsers" datatype="Numeric" aggregator="avg" visible="true"></Measure>
		<Measure name="maxConcurrentRead" caption="${msg("jsolap.concurrentReadMax.title")}" column="concurrentReadUsers" datatype="Numeric" aggregator="max" visible="true"></Measure>
		
		<Measure name="avgConcurrentSupplier" caption="${msg("jsolap.concurrentSupplierAvg.title")}" column="concurrentSupplierUsers" datatype="Numeric" aggregator="avg" visible="true"></Measure>
		<Measure name="maxConcurrentSupplier" caption="${msg("jsolap.concurrentSupplierMax.title")}" column="concurrentSupplierUsers" datatype="Numeric" aggregator="max" visible="true"></Measure>
		
		<Measure name="avgConcurrentWrite" caption="${msg("jsolap.concurrentWriteAvg.title")}" column="concurrentWriteUsers" datatype="Numeric" aggregator="avg" visible="true"></Measure>
		<Measure name="maxConcurrentWrite" caption="${msg("jsolap.concurrentWriteMax.title")}" column="concurrentWriteUsers" datatype="Numeric" aggregator="max" visible="true"></Measure>
		
		<Measure name="avgNamedRead" caption="${msg("jsolap.namedReadAvg.title")}" column="namedReadUsers" datatype="Numeric" aggregator="avg" visible="true"></Measure>
		<Measure name="maxNamedRead" caption="${msg("jsolap.namedReadMax.title")}" column="namedReadUsers" datatype="Numeric" aggregator="max" visible="true"></Measure>
		
		<Measure name="avgNamedWrite" caption="${msg("jsolap.namedWriteAvg.title")}" column="namedWriteUsers" datatype="Numeric" aggregator="avg" visible="true"></Measure>
		<Measure name="maxNamedWrite" caption="${msg("jsolap.namedWriteMax.title")}" column="namedWriteUsers" datatype="Numeric" aggregator="max" visible="true"></Measure>
		
	</Cube>
	
	
	<Cube name="activities" caption="${msg("jsolap.activities.title")}" cache="false" enabled="true">
		<Table name="becpg_activities" alias="becpg_activities" >
			<#if !isAdmin>
				<SQL dialect="generic">
					becpg_activities.instance_id = ${instanceId}
				</SQL>
			</#if>	
		</Table>
		
		<#if isAdmin>
			<DimensionUsage name="instance" caption="${msg("jsolap.instance.title")}" source="instancesDimension" foreignKey="instance_id" />
		</#if>	
		
		<DimensionUsage name="date" caption="${msg("jsolap.activityDate.title")}" source="timeDimension"  foreignKey="activity_date" />
		
		<Dimension name="site" caption="${msg("jsolap.site.title")}" foreignKey="site_id" >
			<Hierarchy name="site" caption="${msg("jsolap.site.title")}"  primaryKey="site_id" hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}">
				<Table name="becpg_activities_names" alias="becpg_activities_names">
					<SQL dialect="generic">
						<#if !isAdmin>becpg_activities_names.instance_id = ${instanceId} AND </#if> becpg_activities_names.user_id IS NULL AND becpg_activities_names.entity_id IS NULL
					</SQL>
				</Table>
				<Level name="site" caption="${msg("jsolap.site.title")}" column="site_id" nameColumn="name"  type="String" />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="userName" caption="${msg("jsolap.userName.title")}" >
			<Hierarchy name="users" caption="${msg("jsolap.userName.caption")}" hasAll="true" allMemberCaption="${msg("jsolap.userName.caption")}" >
				<Level name="userName" caption="${msg("jsolap.userName.title")}" column="user_id" type="String" />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="user" caption="${msg("jsolap.user.title")}" foreignKey="user_id" >
			<Hierarchy name="users" caption="${msg("jsolap.user.caption")}" primaryKey="user_id" hasAll="true" allMemberCaption="${msg("jsolap.user.caption")}" >
			   <Table name="becpg_activities_names" alias="becpg_activities_names">
					<SQL dialect="generic">
						<#if !isAdmin>becpg_activities_names.instance_id = ${instanceId} AND </#if> becpg_activities_names.site_id IS NULL AND becpg_activities_names.entity_id IS NULL
					</SQL>
				</Table>
			
				<Level name="user" caption="${msg("jsolap.user.title")}" column="user_id" nameColumn="name"  type="String" />
			</Hierarchy>
		</Dimension>
		
		<Dimension name="entity" caption="${msg("jsolap.activityEntity.title")}"  foreignKey="entity_id">
			<Hierarchy name="entity" caption="${msg("jsolap.activityEntity.title")}" primaryKey="entity_id"  hasAll="true" allMemberCaption="${msg("jsolap.entity.caption")}">
				<Table name="becpg_activities_names" alias="becpg_activities_names">
					<SQL dialect="generic">
						<#if !isAdmin>becpg_activities_names.instance_id = ${instanceId} AND </#if> becpg_activities_names.user_id IS NULL AND becpg_activities_names.site_id IS NULL
					</SQL>
				</Table>
				<Level name="entityType" caption="${msg("jsolap.activityEntity.type")}" column="entity_type"  nameColumn="entity_type" type="String" />
				<Level name="entityMime" caption="${msg("jsolap.activityEntity.mime")}" column="entity_mime_type"  nameColumn="entity_mime_type" type="String" />
				<Level name="entityName" caption="${msg("jsolap.activityEntity.name")}" column="entity_id"  nameColumn="name" type="String" />
			</Hierarchy>
		</Dimension>
		
		<Dimension name="type" caption="${msg("jsolap.activityType.title")}">
			<Hierarchy name="type" caption="${msg("jsolap.activityType.title")}" hasAll="true" allMemberCaption="${msg("jsolap.activityType.title")}">
				<Level name="type" caption="${msg("jsolap.activityType.title")}" column="activity_type" nameColumn="activity_type" type="String" >
					<NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN activity_type='comment-created' THEN '${msg("jsolap.activityType.comment-created")}'
	                           WHEN activity_type='entity-created' THEN '${msg("jsolap.activityType.entity-created")}'
	                           WHEN activity_type='file-added' THEN '${msg("jsolap.activityType.file-added")}'
	                           WHEN activity_type='file-deleted' THEN '${msg("jsolap.activityType.file-deleted")}'
	                           WHEN activity_type='file-downloaded' THEN '${msg("jsolap.activityType.file-downloaded")}'
	                           WHEN activity_type='file-previewed' THEN '${msg("jsolap.activityType.file-previewed")}'
	                           WHEN activity_type='state-changed' THEN '${msg("jsolap.activityType.state-changed")}'
	                           WHEN activity_type='comment-deleted' THEN '${msg("jsolap.activityType.comment-deleted")}'
	                           ELSE 'Vide'
	                           END]]></SQL>
              		 </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
	
		<Measure name="productNumber" caption="${msg("jsolap.activityNumber.title")}" column="id" datatype="Integer" aggregator="distinct-count" visible="true" />
		
	</Cube>

	<Cube name="requirements" caption="${msg("jsolap.requirements.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.requirementsNumber.title")}">
		
			<View name="requirements" alias="requirements">
				<SQL dialect="generic">
					select
						nodeRef,
						entityNodeRef,
						doc->>"$.cm_name" as name,
						doc->>"$.bcpg_rclReqType" as rclReqType,
						doc->>"$.bcpg_rclReqMessage" as rclReqMessage,
						instanceId
					from
						reqCtrlList
					<#if !isAdmin>	
					 where 
					    instanceId = ${instanceId}
					</#if>
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
					  <![CDATA[CASE WHEN rclReqType='Forbidden' THEN '${msg("listconstraint.bcpg_reqTypes.Forbidden")}'
	                            WHEN rclReqType='Tolerated' THEN '${msg("listconstraint.bcpg_reqTypes.Tolerated")}'
	                            WHEN rclReqType='Info' THEN '${msg("listconstraint.bcpg_reqTypes.Info")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
              		 </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		
		
		<DimensionUsage name="targetProducts" caption="${msg("jsolap.products.title")}" source="productsDimension" foreignKey="entityNodeRef" />
		
		
		<Dimension type="StandardDimension" foreignKey="nodeRef"  name="sourceProducts" caption="${msg("jsolap.sourceProducts.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.sourceProducts.caption")}" primaryKey="dataListNodeRef">
				
			<View name="rclSources" alias="rclSources">
						<SQL dialect="generic">
							select  a.dataListNodeRef,
								b.doc->>"$.cm_name" as name,
								b.nodeRef,
								b.doc->>"$.bcpg_productHierarchy1[0]" as productHierarchy1,
								b.doc->>"$.bcpg_productHierarchy2[0]" as productHierarchy2,
								b.doc->>"$.bcpg_productState" as productState,
								b.doc->>"$.type" as productType,
								b.doc->>"$.cm_versionLabel" as versionLabel
							from
								assoc_bcpg_rclSources a left join  bcpg_product b on a.nodeRef = b.nodeRef
							<#if !isAdmin>	
							  where a.instanceId = ${instanceId} and b.instanceId
							</#if>
						</SQL>
				</View>
				<Level approxRowCount="5" name="productState" caption="${msg("jsolap.srcProductState.title")}"  column="productState"  type="String"  >
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
				<Level approxRowCount="10" name="entity_type" caption="${msg("jsolap.srcProductType.title")}" column="productType" nameColumn="productType" type="String"   >
					<NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN productType='bcpg:rawMaterial' THEN "${msg('bcpg_bcpgmodel.type.bcpg_rawMaterial.title')}"
	                            WHEN productType='bcpg:finishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_finishedProduct.title')}"
	                            WHEN productType='bcpg:semiFinishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_semiFinishedProduct.title')}"
	                            WHEN productType='bcpg:packagingMaterial' THEN "${msg('bcpg_bcpgmodel.type.bcpg_packagingMaterial.title')}"
	                            WHEN productType='bcpg:packagingKit' THEN "${msg('bcpg_bcpgmodel.type.bcpg_packagingKit.title')}"
	                            WHEN productType='bcpg:localSemiFinishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_localSemiFinishedProduct.title')}"
	                            WHEN productType='bcpg:resourceProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_resourceProduct.title')}"
	                            ELSE 'Vide'
	                           END]]></SQL>
             	</NameExpression>
				</Level>
				<Level name="productHierarchy1" caption="${msg("jsolap.srcProductFamily.title")}" table="rclSources" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="productHierarchy2" caption="${msg("jsolap.srcProductSubFamily.title")}" table="rclSources" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="entity_noderef" caption="${msg("jsolap.srcProductComponent.title")}" table="rclSources" column="nodeRef" nameColumn="name" type="String"   >
				</Level>
				<Level name="versionLabel" caption="${msg("jsolap.srcProductVersionLabel.title")}" table="rclSources" column="versionLabel" type="String" />
			</Hierarchy>
		</Dimension>
		
		
		
		<DimensionUsage name="tags" caption="${msg("jsolap.tags.title")}" source="tagsDimension" foreignKey="entityNodeRef" />
		
		
		<#if isAdmin>
			<DimensionUsage name="instance" caption="${msg("jsolap.instance.title")}" source="instancesDimension" foreignKey="instanceId" />
		</#if>
		<Measure name="requirementsNumber" caption="${msg("jsolap.requirementsNumber.title")}" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
    </Cube>


	<Cube name="incidents" caption="${msg("jsolap.incidents.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.incidentsNumber.title")}">
		
		<View name="incidents" alias="incidents">
				<SQL dialect="generic">
					select
						nodeRef,
						doc->>"$.cm_name" as name,
						doc->>"$.metadata_siteId" as siteId,
						doc->>"$.metadata_siteName" as siteName,
						doc->>"$.bcpg_code" as code,
						doc->>"$.qa_ncType" as ncType,
						doc->>"$.qa_ncPriority" as ncPriority,
						doc->>"$.qa_ncState" as ncState,
						doc->>"$.qa_ncQuantityNc" as ncQuantityNc,
						doc->>"$.qa_ncCost" as ncCost,
						doc->>"$.qa_batchId" as batchId,
						doc->>"$.qa_claimType" as claimType,
						doc->>"$.qa_claimOriginHierarchy1[0]" as claimOriginHierarchy1,
						doc->>"$.qa_claimOriginHierarchy2[0]" as claimOriginHierarchy2,
						CAST(doc->>"$.cm_created" as DATE) as dateCreated,
						CAST(doc->>"$.qa_claimResponseDate" as DATE)  as claimResponseDate,
						CAST(doc->>"$.qa_claimTreatementDate" as DATE)  as claimTreatmentDate,
						doc->>"$.qa_claimClosingDate" as claimClosingDate,
						doc->>"$.qa_product_bcpg_nodeRef[0]" as productNodeRef,
						instanceId
					from
						qa_nc
					<#if !isAdmin>	
					 where 
					    instanceId = ${instanceId}
					</#if>
				</SQL>
			</View>
		
		<Dimension  name="designation" caption="${msg("jsolap.incident.title")}" >
			<Hierarchy name="incident" caption="${msg("jsolap.incident.title")}" hasAll="true" allMemberCaption="${msg("jsolap.incident.caption")}">
				<Level name="name" caption="${msg("jsolap.incidentName.title")}" column="name"  type="String"    />
				<Level name="code" caption="${msg("jsolap.ncCode.title")}" column="code"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="batch" caption="${msg("jsolap.batch.title")}" >
			<Hierarchy name="batch" caption="${msg("jsolap.batch.title")}" hasAll="true" allMemberCaption="${msg("jsolap.batch.caption")}">
				<Level name="batchId" caption="${msg("jsolap.batchNumber.title")}" column="batchId"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="incidentOrigin" caption="${msg("jsolap.claimOrigin.title")}" >
			<Hierarchy name="origin" caption="${msg("jsolap.origin.title")}" hasAll="true" allMemberCaption="${msg("jsolap.origin.caption")}">
				<Level name="claimOriginHierarchy1" caption="${msg("jsolap.claimOriginFamily.title")}" column="claimOriginHierarchy1"  type="String"    />
				<Level name="claimOriginHierarchy2" caption="${msg("jsolap.claimOriginSubFamily.title")}" column="claimOriginHierarchy2"  type="String" approxRowCount="20"   />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="incidentType" caption="${msg("jsolap.claimType.title")}" >
			<Hierarchy name="incidentType" caption="${msg("jsolap.claimType.title")}" hasAll="true" allMemberCaption="${msg("jsolap.claimType.caption")}">
				<Level name="claimType" caption="${msg("jsolap.claimType.title")}" column="claimType"  type="String" approxRowCount="20"   />
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
		
		<Dimension  name="type" caption="${msg("jsolap.ncType.title")}" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.ncType.caption")}" >
				<Level approxRowCount="2" name="ncType" caption="${msg("jsolap.ncType.title")}"  column="ncType"  type="String"    >
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
		
		<Dimension foreignKey="nodeRef"  name="plant" caption="${msg("jsolap.plant.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.plant.caption")}" primaryKey="entityNodeRef">
				<View name="plant" alias="plant">
								<SQL dialect="generic">
									select  
										entityNodeRef,
										doc->>"$.name" as name,
										nodeRef
									from
										assoc_bcpg_plants
									<#if !isAdmin>	
									  where instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.plant.title")}" column="nodeRef" nameColumn="name" type="String"  >
				</Level>
			</Hierarchy>
		</Dimension>


		<DimensionUsage name="products" caption="${msg("jsolap.products.title")}" source="productsDimension" foreignKey="productNodeRef" />

		<DimensionUsage name="clients" caption="${msg("jsolap.client.title")}" source="clientsDimension" foreignKey="nodeRef" />
		
		<DimensionUsage name="tags" caption="${msg("jsolap.tags.title")}" source="tagsDimension" foreignKey="nodeRef" />
		
		<#if isAdmin>
			<DimensionUsage name="instance" caption="${msg("jsolap.instance.title")}" source="instancesDimension" foreignKey="instanceId" />
		</#if>
	    <DimensionUsage name="dateCreated" caption="${msg("jsolap.entryDate.title")}" source="timeDimension" foreignKey="dateCreated" />
	    <DimensionUsage name="claimTreatmentDate" caption="${msg("jsolap.treatmentDate.title")}" source="timeDimension" foreignKey="claimTreatmentDate" />
		<DimensionUsage name="claimResponseDate" caption="${msg("jsolap.answerDate.title")}" source="timeDimension" foreignKey="claimResponseDate" />
		<DimensionUsage name="claimClosingDate" caption="${msg("jsolap.closingDate.title")}" source="timeDimension" foreignKey="claimClosingDate" />
		
		
		<Measure name="noderef" caption="${msg("jsolap.incidentsNumber.title")}" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
		<Measure name="ncQuantityNc" caption="${msg("jsolap.nonConformQuantity.title")}" column="ncQuantityNc" datatype="Numeric" aggregator="sum" visible="true"  />
		<Measure name="ncCost" caption="${msg("jsolap.nonConformityCost.title")}" column="ncCost" datatype="Numeric" aggregator="sum" visible="true"  />
	</Cube>
	
	<Cube name="projectsSteps" caption="${msg("jsolap.projectsTasks.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.projectsTasks.title")}">
			
				<View name="taskList" alias="taskList">
					<SQL dialect="generic">
						select  
							a.nodeRef,
							a.entityNodeRef,
							a.doc->>"$.pjt_tlTaskName" as tlTaskName,
							a.doc->>"$.pjt_tlDuration" as tlDuration,
							a.doc->>"$.pjt_tlRealDuration" as tlRealDuration,
							CAST(a.doc->>"$.pjt_tlStart" as DATE) as tlStart,
							CAST(a.doc->>"$.pjt_tlEnd" as DATE) as tlEnd,
							a.doc->>"$.pjt_tlState" as tlState,
							a.doc->>"$.pjt_tlResources[0]" as tlResources,
							a.doc->>"$.pjt_tlWork" as tlWork,
							a.doc->>"$.pjt_tlLoggedTime" as tlLoggedTime,
							a.doc->>"$.bcpg_sort" as sortOrder,
							CAST(a.doc->>"$.cm_modified" as DATE) as projectDateModified,
							a.instanceId,
							CASE 
								 WHEN a.doc->>"$.pjt_tlResources[0]" LIKE 'GROUP_PROJECT%'
								 	THEN
								 	  REGEXP_REPLACE(json_extract(b.doc,CONCAT('$.', SUBSTRING_INDEX (a.doc->>"$.pjt_tlResources[0]","_",-2))),'[\\[\\"\\]]*','')
								 ELSE
								 	a.doc->>"$.pjt_tlResources[0]"
								 END as taskResources
						from
							taskList a inner join pjt_project b on a.entityNodeRef = b.nodeRef 
						<#if !isAdmin>	
						  where a.instanceId = ${instanceId}
						</#if>						
					</SQL>
				</View>

		
		<Dimension type="StandardDimension" foreignKey="entityNodeRef"   name="site" caption="${msg("jsolap.site.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}" primaryKey="nodeRef">
				<View name="projects_site" alias="projects_site">
				<SQL dialect="generic">
					select
						nodeRef,
						doc->>"$.metadata_siteId" as siteId,
						doc->>"$.metadata_siteName" as siteName		
					from
						pjt_project
					<#if !isAdmin>	
					 where 
					    instanceId = ${instanceId}
					</#if>
				</SQL>
			 </View>
			<Level name="site" caption="${msg("jsolap.site.title")}" column="siteId"  nameColumn="siteName" type="String" />
			</Hierarchy>
		</Dimension>			
		
		<Dimension  name="designation" caption="${msg("jsolap.task.title")}" >
			<Hierarchy name="taskPerName" caption="${msg("jsolap.taskPerName.title")}" hasAll="true" allMemberCaption="${msg("jsolap.task.caption")}">
				<Level name="tlTaskName" caption="${msg("jsolap.taskName.title")}" column="tlTaskName" type="String" ordinalColumn="sortOrder" />
			</Hierarchy>
		</Dimension>
		
		
		<Dimension  name="taskState" caption="${msg("jsolap.taskState.title")}" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.taskState.caption")}" >
				<Level approxRowCount="5" name="tlState" caption="${msg("jsolap.taskState.title")}"  column="tlState"  type="String"    >
				  <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN tlState='Planned' THEN '${msg("listconstraint.pjt_taskStates.Planned")}'
	                            WHEN tlState='InProgress' THEN '${msg("listconstraint.pjt_taskStates.InProgress")}'
	                            WHEN tlState='OnHold' THEN '${msg("listconstraint.pjt_taskStates.OnHold")}'
	                            WHEN tlState='Cancelled' THEN '${msg("listconstraint.pjt_taskStates.Cancelled")}'
	                            WHEN tlState='Refused' THEN '${msg("listconstraint.pjt_taskStates.Refused")}'
	                            WHEN tlState='Completed' THEN '${msg("listconstraint.pjt_taskStates.Completed")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>

		<Dimension name="resource" caption="${msg("jsolap.resource.title")}" >
			<Hierarchy name="resource" caption="${msg("jsolap.resource.title")}" hasAll="true" allMemberCaption="${msg("jsolap.resource.caption")}">
					<Level name="taskResources" caption="${msg("jsolap.resource.title")}" column="taskResources"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<DimensionUsage name="project" caption="${msg("jsolap.project.title")}" source="projectsDimension" foreignKey="entityNodeRef" />
			
		<DimensionUsage name="tags" caption="${msg("jsolap.tags.title")}" source="tagsDimension" foreignKey="entityNodeRef" />
		
		<DimensionUsage name="projectState" caption="${msg("jsolap.projectState.title")}" source="projectStateDimension" foreignKey="entityNodeRef" />
		
		<DimensionUsage name="projectManager" caption="${msg("jsolap.projectManager.title")}" source="projectManagerDimension" foreignKey="entityNodeRef" />		
		
		<#if isAdmin>
			<DimensionUsage name="instance" caption="${msg("jsolap.instance.title")}" source="instancesDimension" foreignKey="instanceId" />
		</#if>
		<DimensionUsage name="tlStart" caption="${msg("jsolap.startDate.title")}" source="timeDimension" foreignKey="tlStart" />
		<DimensionUsage name="tlEnd" caption="${msg("jsolap.endDate.title")}" source="timeDimension" foreignKey="tlEnd" />
		<DimensionUsage name="projectDateModified" caption="${msg("jsolap.modificationDate.title")}" source="timeDimension"  foreignKey="projectDateModified" />	
		<Measure name="stepsNumber" caption="${msg("jsolap.tasksNumber.title")}" column="noderef" datatype="Numeric" aggregator="distinct-count" visible="true" />
		<Measure name="averageForecastDurations" caption="${msg("jsolap.averageForecastDurations.title")}" column="tlDuration" datatype="Numeric" aggregator="avg" visible="true"  />
		<Measure name="averageActualDurations" caption="${msg("jsolap.averageActualDurations.title")}" column="tlRealDuration" datatype="Numeric" aggregator="avg" visible="true"  />
		<Measure name="workload" caption="${msg("jsolap.workload.title")}" column="tlWork" datatype="Integer" aggregator="sum" visible="true"></Measure>
		<Measure name="loggedTime" caption="${msg("jsolap.loggedTime.title")}" column="tlLoggedTime" datatype="Integer" aggregator="sum" visible="true"></Measure>
		<Measure name="avgLoggedTime" caption="${msg("jsolap.avgLoggedTime.title")}" column="tlLoggedTime" datatype="Integer" aggregator="avg" visible="true"></Measure>
		
		<CalculatedMember name="averageDurations" caption="${msg("jsolap.averageDurations.title")}" dimension="Measures" visible="true">
			<Formula>([Measures].[averageDurations],[designation.taskPerName].PrevMember) + ([Measures].[averageActualDurations])</Formula>
		</CalculatedMember>  
		
		
	</Cube>
	
	
	<Cube name="projectsEvaluation" caption="${msg("jsolap.projectsEvaluation.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.note.title")}">

				<View name="scoreList" alias="scoreList">
					<SQL dialect="generic">
						select  
							entityNodeRef,
							doc->>"$.pjt_slCriterion" as slCriterion,
							doc->>"$.pjt_slWeight" as slWeight,
							doc->>"$.pjt_slScore" as slScore,
							instanceId
						from
							scoreList
						<#if !isAdmin>	
						  where instanceId = ${instanceId}
						</#if>
					</SQL>
				</View>
		
		
		
		<Dimension type="StandardDimension" foreignKey="entityNodeRef"   name="site" caption="${msg("jsolap.site.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}" primaryKey="nodeRef">
				<View name="projects_site" alias="projects_site">
				<SQL dialect="generic">
					select
						nodeRef,
						doc->>"$.metadata_siteId" as siteId,
						doc->>"$.metadata_siteName" as siteName		
					from
						pjt_project
					<#if !isAdmin>	
					 where 
					    instanceId = ${instanceId}
					</#if>
				</SQL>
			 </View>
			<Level name="site" caption="${msg("jsolap.site.title")}" column="siteId"  nameColumn="siteName" type="String" />
			</Hierarchy>
		</Dimension>			
		
		<Dimension  name="designation" caption="${msg("jsolap.criterion.title")}" >
			<Hierarchy name="criterionByName" caption="${msg("jsolap.criterionByName.title")}" hasAll="true" allMemberCaption="${msg("jsolap.criterion.caption")}">
				<Level name="slCriterion" caption="${msg("jsolap.criterion.title")}" column="slCriterion"  type="String"    />
			</Hierarchy>
		</Dimension>		
		
		<DimensionUsage name="project" caption="${msg("jsolap.project.title")}" source="projectsDimension" foreignKey="entityNodeRef" />

		<DimensionUsage name="projectState" caption="${msg("jsolap.projectState.title")}" source="projectStateDimension" foreignKey="entityNodeRef" />
		
		<DimensionUsage name="projectManager" caption="${msg("jsolap.projectManager.title")}" source="projectManagerDimension" foreignKey="entityNodeRef" />
		
		<DimensionUsage name="tags" caption="${msg("jsolap.tags.title")}" source="tagsDimension" foreignKey="entityNodeRef" />
		
		<#if isAdmin>
			<DimensionUsage name="instance" caption="${msg("jsolap.instance.title")}" source="instancesDimension" foreignKey="instanceId" />
		</#if>
		<Measure name="slWeight" caption="${msg("jsolap.weighting.title")}" column="slWeight" datatype="Numeric" aggregator="avg" visible="true" />
		<Measure name="slScore" caption="${msg("jsolap.note.title")}" column="slScore" datatype="Numeric" aggregator="avg" visible="true"  />
		
		<CalculatedMember name="weightingNote" caption="${msg("jsolap.weightingNote.title")}" dimension="Measures" visible="true">
			<Formula>[Measures].[slWeight] * [Measures].[slScore] / 100</Formula>
		</CalculatedMember> 
	</Cube>

	<Cube name="projects" caption="${msg("jsolap.projects.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.projectsNumberDistinct.title")}">
			<View name="projects" alias="projects">
				<SQL dialect="generic">
					select
						nodeRef,
						doc->>"$.cm_name" as name,
						doc->>"$.pjt_projectState" as projectState,
						doc->>"$.pjt_projectHierarchy1[0]" as	projectHierarchy1,
						doc->>"$.pjt_projectHierarchy2[0]" as	projectHierarchy2,
						doc->>"$.metadata_siteId" as siteId,
						doc->>"$.metadata_siteName" as siteName,
						doc->>"$.bcpg_code" as code,
						CAST(doc->>"$.cm_created" as DATE) as projectDateCreated,
						doc->>"$.cm_creator" as projectCreator,
						CAST(doc->>"$.cm_modified" as DATE)  as projectDateModified,
						doc->>"$.cm_modifier" as  projectModifier,
						CAST(doc->>"$.pjt_projectStartDate" as DATE)  as projectStartDate,
						CAST(doc->>"$.pjt_projectDueDate" as DATE)  as projectDueDate,
						CAST(doc->>"$.pjt_projectCompletionDate" as DATE)  as completionDate,
						doc->>"$.pjt_projectPriority" as projectPriority,
						doc->>"$.pjt_completionPercent" as completionPercent,
						doc->>"$.pjt_projectScore" as projectScore,
						doc->>"$.pjt_projectOverdue" as projectOverdue,
						doc->>"$.pjt_projectManager[0]" as projectManager,
						doc->>"$.pjt_projectOrigin" as projectOrigin,
						doc->>"$.pjt_projectSponsor" as projectSponsor,
						doc->>"$.bcpg_entityTplRef[0]" as entityTplRef,
						doc->>"$.pjt_projectEntity_bcpg_nodeRef[0]" as projectEntityNodeRef,
						DATEDIFF(CAST(doc->>"$.pjt_projectCompletionDate" as DATE),CAST(doc->>"$.pjt_projectStartDate" as DATE)) as duration,
						instanceId
					from
						pjt_project
					<#if !isAdmin>	
					 where 
					    instanceId = ${instanceId}
					</#if>
				</SQL>
			 </View>

		<Dimension name="site" caption="${msg("jsolap.site.title")}">
			<Hierarchy name="site" caption="${msg("jsolap.site.title")}" hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}">
				<Level name="site" caption="${msg("jsolap.site.title")}" column="siteName"  type="String" />
			</Hierarchy>
		</Dimension>

		
		<Dimension  name="designation" caption="${msg("jsolap.project.title")}" >
			<Hierarchy name="projectPerFamily" caption="${msg("jsolap.projectPerFamily.title")}" hasAll="true" allMemberCaption="${msg("jsolap.project.caption")}">
				<Level name="projectHierarchy1" caption="${msg("jsolap.projectFamily.title")}" column="projectHierarchy1"  type="String"    />
				<Level name="projectHierarchy2" caption="${msg("jsolap.projectSubFamily.title")}" column="projectHierarchy2"  type="String"    />
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
					  <![CDATA[CASE WHEN projectPriority=1 THEN '${msg("listconstraint.qa_ncPriority.1")}'
	                            WHEN projectPriority=2 THEN '${msg("listconstraint.qa_ncPriority.2")}'
	                            WHEN projectPriority=3 THEN '${msg("listconstraint.qa_ncPriority.3")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		<Dimension  name="state" caption="${msg("jsolap.projectState.title")}" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.state.caption")}" >
				<Level approxRowCount="5" name="projectState" caption="${msg("jsolap.projectState.title")}" column="projectState" type="String">
				  <NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN projectState='Planned' THEN '${msg("listconstraint.pjt_projectStates.Planned")}'
	                            WHEN projectState='InProgress' THEN '${msg("listconstraint.pjt_projectStates.InProgress")}'
	                            WHEN projectState='OnHold' THEN '${msg("listconstraint.pjt_projectStates.OnHold")}'
	                            WHEN projectState='Cancelled' THEN '${msg("listconstraint.pjt_projectStates.Cancelled")}'
	                            WHEN projectState='Completed' THEN '${msg("listconstraint.pjt_projectStates.Completed")}'
	                            ELSE 'Vide'
	                           END]]></SQL>
              </NameExpression>
				</Level>
			</Hierarchy>
		</Dimension>
		
		<DimensionUsage name="entities" caption="${msg("jsolap.entities.title")}" source="productsDimension" foreignKey="projectEntityNodeRef" />
		
		
		<Dimension  name="projectManager" caption="${msg("jsolap.projectManager.title")}" >
			<Hierarchy name="projectManager" caption="${msg("jsolap.projectManager.title")}" hasAll="true" allMemberCaption="${msg("jsolap.projectManager.caption")}">
				<Level name="projectManager" caption="${msg("jsolap.projectManager.title")}" column="projectManager"  type="String"    />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="projectCreator" caption="${msg("jsolap.creator.title")}" >
			<Hierarchy name="projectCreator" caption="${msg("jsolap.creator.caption")}" hasAll="true" allMemberCaption="${msg("jsolap.creator.caption")}">
				<Level name="projectCreator" caption="${msg("jsolap.creator.title")}" column="projectCreator"  type="String"    />
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
		
		<Dimension  name="modification" caption="${msg("jsolap.modifier.title")}" >
			<Hierarchy name="modifiers" caption="${msg("jsolap.modifier.caption")}" hasAll="true" allMemberCaption="${msg("jsolap.modifier.caption")}" >
				<Level name="modifier" caption="${msg("jsolap.modifier.title")}" column="projectModifier"  type="String" />
			</Hierarchy>
		</Dimension>
		
		<DimensionUsage name="tags" caption="${msg("jsolap.tags.title")}" source="tagsDimension" foreignKey="nodeRef" />
	
		
		<#if isAdmin>
			<DimensionUsage name="instance" caption="${msg("jsolap.instance.title")}" source="instancesDimension" foreignKey="instanceId" />
		</#if>
		<DimensionUsage name="projectDateModified" caption="${msg("jsolap.modificationDate.title")}" source="timeDimension"  foreignKey="projectDateModified" />
	   	<DimensionUsage name="projectDateCreated" caption="${msg("jsolap.creationDate.title")}" source="timeDimension" foreignKey="projectDateCreated" />
		<DimensionUsage name="projectStartDate" caption="${msg("jsolap.startDate.title")}" source="timeDimension" foreignKey="projectStartDate" />
		<DimensionUsage name="projectDueDate" caption="${msg("jsolap.dueDate.title")}" source="timeDimension" foreignKey="projectDueDate" />
		<DimensionUsage name="completionDate" caption="${msg("jsolap.completionDate.title")}" source="timeDimension" foreignKey="completionDate" />
	

		<Measure name="projectsNumber" caption="${msg("jsolap.projectsNumber.title")}" column="noderef" datatype="Numeric" aggregator="count" visible="true" />
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
	
		<View name="nutList" alias="nutList">
					<SQL dialect="generic">
						select  
							entityNodeRef,
							doc->>"$.bcpg_nutListNut[0]" as name,
							doc->>"$.bcpg_nutListNut_bcpg_nodeRef[0]" as nodeRef,
							doc->>"$.bcpg_nutListGroup" as nutGroup,
							doc->>"$.bcpg_nutListValue" as nutValue,
							doc->>"$.bcpg_nutListFormulatedValue" as nutFormulatedValue,
							doc->>"$.bcpg_nutListGDAPerc" as nutListGDAPerc,
							doc->>"$.bcpg_nutListValuePerServing" as nutListValuePerServing,
							instanceId
						from
							nutList
						<#if !isAdmin>	
						  where instanceId = ${instanceId}
						</#if>
					</SQL>
		</View>
	

		<Dimension  name="site" caption="${msg("jsolap.site.title")}" type="StandardDimension" foreignKey="entityNodeRef" >
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}" primaryKey="noderef">
			<View name="products" alias="products">
				<SQL dialect="generic">
					select
						nodeRef,
						doc->>"$.metadata_siteId" as siteId,
						doc->>"$.metadata_siteName" as siteName		
					from
						bcpg_product
					<#if !isAdmin>	
					 where 
					    instanceId = ${instanceId}
					</#if>
				</SQL>
			</View>
				<Level name="site" caption="${msg("jsolap.site.title")}" column="siteId"  nameColumn="siteName" type="String" />
			</Hierarchy>
		</Dimension>

		<DimensionUsage name="designation" caption="${msg("jsolap.designation.title")}" source="productsDimension" foreignKey="entityNodeRef" />

		
		<Dimension foreignKey="entityNodeRef"  name="productType" caption="${msg("jsolap.productType.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.productType.caption")}" primaryKey="nodeRef" >
			
			<View name="products_type" alias="products_type">
					<SQL dialect="generic">
						select
							nodeRef,
							doc->>"$.type" as productType
						from
							bcpg_product
						<#if !isAdmin>	
						 where 
						    instanceId = ${instanceId}
						</#if>
					</SQL>
				</View>
				
			<Level approxRowCount="10" name="entity_type" caption="${msg("jsolap.type.title")}" column="productType"  type="String"   >
				<NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN productType='bcpg:rawMaterial' THEN "${msg('bcpg_bcpgmodel.type.bcpg_rawMaterial.title')}"
	                            WHEN productType='bcpg:finishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_finishedProduct.title')}"
	                            WHEN productType='bcpg:semiFinishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_semiFinishedProduct.title')}"
	                            WHEN productType='bcpg:packagingMaterial' THEN "${msg('bcpg_bcpgmodel.type.bcpg_packagingMaterial.title')}"
	                            WHEN productType='bcpg:packagingKit' THEN "${msg('bcpg_bcpgmodel.type.bcpg_packagingKit.title')}"
	                            WHEN productType='bcpg:localSemiFinishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_localSemiFinishedProduct.title')}"
	                            WHEN productType='bcpg:resourceProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_resourceProduct.title')}"
	                            ELSE 'Vide'
	                           END]]></SQL>
             		 </NameExpression>
			
			</Level>
			</Hierarchy>
		</Dimension>

		<Dimension type="StandardDimension"  name="nutrient" caption="${msg("jsolap.nutrient.title")}">
			<Hierarchy name="nutrientPerGroup" caption="${msg("jsolap.nutrientPerGroup.title")}" hasAll="true" allMemberCaption="${msg("jsolap.nutrient.caption")}" >
				<Level approxRowCount="3" name="nutGroup" caption="${msg("jsolap.nutrientGroup.title")}" column="nutGroup" type="String"   >
				</Level>
				<Level  name="nutNodeRef" caption="${msg("jsolap.nutrient.title")}" column="nodeRef"  nameColumn="name" type="String"   ></Level>
			</Hierarchy>	
		</Dimension>
		
	
		
		<DimensionUsage name="tags" caption="${msg("jsolap.tags.title")}" source="tagsDimension" foreignKey="nodeRef" />
		
		<#if isAdmin>
			<DimensionUsage name="instance" caption="${msg("jsolap.instance.title")}" source="instancesDimension" foreignKey="instanceId" />
		</#if>
		<Measure name="nutValue" caption="${msg("jsolap.nutritionalValues.title")}" column="nutValue" datatype="Numeric" aggregator="avg" visible="true"></Measure>	
		<Measure name="nutFormulatedValue" caption="${msg("jsolap.nutritionalFormulatedValues.title")}" column="nutFormulatedValue" datatype="Numeric" aggregator="avg" visible="true"></Measure>	
		<Measure name="nutListValuePerServing" caption="${msg("jsolap.nutListValuePerServing.title")}" column="nutListValuePerServing" datatype="Numeric" aggregator="avg" visible="true"></Measure>
		<Measure name="nutListGDAPerc" caption="${msg("jsolap.nutListGDAPerc.title")}" column="nutListGDAPerc" datatype="Numeric" aggregator="avg" visible="true"></Measure>
	</Cube>				
	
	<Cube name="products" caption="${msg("jsolap.products.title")}" cache="true" enabled="true" defaultMeasure="${msg("jsolap.productsNumber.title")}">
		
		
			<View name="products" alias="products">
				<SQL dialect="generic">
					select
						nodeRef,
						doc->>"$.cm_name" as name,
						doc->>"$.type" as productType,
						doc->>"$.bcpg_productHierarchy1[0]" as productHierarchy1,
						doc->>"$.bcpg_productHierarchy2[0]" as productHierarchy2,
						doc->>"$.metadata_siteId" as siteId,
						doc->>"$.metadata_siteName" as siteName,
						doc->>"$.bcpg_code" as code,
						doc->>"$.bcpg_erpCode" as erpCode,
						doc->>"$.bcpg_legalName" as legalName,
						doc->>"$.bcpg_nutrientProfilingClass" as nutrientProfilingClass,
						CAST( doc->>"$.cm_created" as DATE) as productDateCreated,
						CAST( doc->>"$.cm_modified" as DATE) as productDateModified,
						CAST( doc->>"$.bcpg_startEffectivity" as DATE) as startEffectivity,
						CAST( doc->>"$.bcpg_endEffectivity" as DATE) as endEffectivity,
						doc->>"$.bcpg_productState" as productState,
						doc->>"$.bcpg_projectedQty" as projectedQty,
						doc->>"$.bcpg_unitTotalCost" as unitTotalCost,
						doc->>"$.bcpg_profitability" as profitability,
						doc->>"$.bcpg_unitPrice" as unitPrice,
						doc->>"$.cm_versionLabel" as versionLabel,
						doc->>"$.cm_creator" as creator,
						doc->>"$.cm_modifier" as modifier,
						instanceId
					from
						bcpg_product
					<#if !isAdmin>	
					 where 
					    instanceId = ${instanceId}
					</#if>
				</SQL>
			</View>
		
		
		<Dimension name="site" caption="${msg("jsolap.site.title")}">
			<Hierarchy name="site" caption="${msg("jsolap.site.title")}" hasAll="true" allMemberCaption="${msg("jsolap.site.caption")}">
				<Level name="site" caption="${msg("jsolap.site.title")}" column="siteName"  type="String" />
			</Hierarchy>
		</Dimension>

		<Dimension  name="designation" caption="${msg("jsolap.product.title")}" >
			<Hierarchy name="productPerFamily" caption="${msg("jsolap.productPerFamily.title")}" hasAll="true" allMemberCaption="${msg("jsolap.product.caption")}">
				<Level name="productHierarchy1" caption="${msg("jsolap.productFamily.title")}" column="productHierarchy1"  type="String"    />
				<Level name="productHierarchy2" caption="${msg("jsolap.productSubFamily.title")}" column="productHierarchy2"  type="String"    />
				<Level name="code" caption="${msg("jsolap.productCode.title")}" column="code"  type="String"  uniqueKeyLevelname="" highCardinality="true"  />
				<Level name="name" caption="${msg("jsolap.productName.title")}" column="name"  type="String" />
			    <Level name="erpCode" caption="${msg("jsolap.productErpCode.title")}" column="erpCode"  type="String" />
				<Level name="legalName" caption="${msg("jsolap.productLegalName.title")}" column="legalName"  type="String" />
				<Level name="versionLabel" caption="${msg("jsolap.productVersionLabel.title")}" column="versionLabel" type="String" />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="state" caption="${msg("jsolap.productState.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.productState.caption")}" >
				<Level approxRowCount="5" name="productState" caption="${msg("jsolap.productState.title")}"  column="productState"  type="String"   >
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
			</Hierarchy>
		</Dimension>
		
		 
		<Dimension foreignKey="nodeRef"  name="geoOrigin" caption="${msg("jsolap.geoOrigin.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.geoOrigin.caption")}" primaryKey="entityNodeRef">
				<View name="ingListgeoOrigin" alias="ingListgeoOrigin">
								<SQL dialect="generic">
									select  
										entityNodeRef,
										doc->>"$.name" as name,
										nodeRef
									from
										assoc_bcpg_ingListGeoOrigin
									<#if !isAdmin>	
									  where instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.ingredientCountry.title")}" column="nodeRef" nameColumn="name" type="String"  >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension foreignKey="nodeRef"  name="productGeoOrigin" caption="${msg("jsolap.productGeoOrigin.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.geoOrigin.caption")}" primaryKey="entityNodeRef">
				<View name="productgeoOrigin" alias="productgeoOrigin">
								<SQL dialect="generic">
									select  
										entityNodeRef,
										doc->>"$.name" as name,
										nodeRef
									from
										assoc_bcpg_productGeoOrigin
									<#if !isAdmin>	
									  where instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.productCountry.title")}" column="nodeRef" nameColumn="name" type="String"  >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension foreignKey="nodeRef"  name="plant" caption="${msg("jsolap.plant.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.plant.caption")}" primaryKey="entityNodeRef">
				<View name="plant" alias="plant">
								<SQL dialect="generic">
									select  
										entityNodeRef,
										doc->>"$.name" as name,
										nodeRef
									from
										assoc_bcpg_plants
									<#if !isAdmin>	
									  where instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.plant.title")}" column="nodeRef" nameColumn="name" type="String"  >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension foreignKey="nodeRef"  name="trademark" caption="${msg("jsolap.trademark.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.trademark.caption")}" primaryKey="entityNodeRef">
				<View name="trademark" alias="trademark">
								<SQL dialect="generic">
									select  
										entityNodeRef,
										doc->>"$.name" as name,
										nodeRef
									from
										assoc_bcpg_trademarkRef
									<#if !isAdmin>	
									  where instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.trademark.title")}" column="nodeRef" nameColumn="name" type="String"  >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension foreignKey="nodeRef"  name="subsidiary" caption="${msg("jsolap.subsidiary.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.subsidiary.caption")}" primaryKey="entityNodeRef">
				<View name="subsidiary" alias="subsidiary">
								<SQL dialect="generic">
									select  
										entityNodeRef,
										doc->>"$.name" as name,
										nodeRef
									from
										assoc_bcpg_subsidiaryRef
									<#if !isAdmin>	
									  where instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.subsidiary.title")}" column="nodeRef" nameColumn="name" type="String"  >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension name="productType" caption="${msg("jsolap.productType.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.productType.caption")}"  >
				
			<Level approxRowCount="10" name="entity_type" caption="${msg("jsolap.productType.title")}" column="productType" nameColumn="entity_label" type="String"   >
				<NameExpression>
					  <SQL dialect="generic" >
					  <![CDATA[CASE WHEN productType='bcpg:rawMaterial' THEN "${msg('bcpg_bcpgmodel.type.bcpg_rawMaterial.title')}"
	                            WHEN productType='bcpg:finishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_finishedProduct.title')}"
	                            WHEN productType='bcpg:semiFinishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_semiFinishedProduct.title')}"
	                            WHEN productType='bcpg:packagingMaterial' THEN "${msg('bcpg_bcpgmodel.type.bcpg_packagingMaterial.title')}"
	                            WHEN productType='bcpg:packagingKit' THEN "${msg('bcpg_bcpgmodel.type.bcpg_packagingKit.title')}"
	                            WHEN productType='bcpg:localSemiFinishedProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_localSemiFinishedProduct.title')}"
	                            WHEN productType='bcpg:resourceProduct' THEN "${msg('bcpg_bcpgmodel.type.bcpg_resourceProduct.title')}"
	                            ELSE 'Vide'
	                           END]]></SQL>
             		 </NameExpression>
			</Level>
			</Hierarchy>
		</Dimension>
		
		<DimensionUsage name="clients" caption="${msg("jsolap.client.title")}" source="clientsDimension" foreignKey="nodeRef" />
	
		<Dimension type="StandardDimension" foreignKey="nodeRef"  name="supplier" caption="${msg("jsolap.supplier.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.supplier.caption")}" primaryKey="entityNodeRef">
				<View name="suppliers" alias="suppliers">
								<SQL dialect="generic">
									select  
										a.entityNodeRef,
										a.doc->>"$.name" as name,
										a.nodeRef as nodeRef,
										b.doc->>"$.bcpg_supplierState" as supplierState,
										b.doc->>"$.bcpg_supplierHierarchy1[0]" as supplierHierarchy1,
										b.doc->>"$.bcpg_supplierHierarchy2[0]" as supplierHierarchy2
									from
										assoc_bcpg_suppliers a left join bcpg_supplier b on a.nodeRef = b.nodeRef						
									<#if !isAdmin>	
									  where b.instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.supplierName.title")}" nameColumn="name" column="nodeRef" type="String">
				</Level>
				<Level name="family" caption="${msg("jsolap.supplierFamily.title")}" column="supplierHierarchy1" type="String">
				</Level>
				<Level name="subfamily" caption="${msg("jsolap.supplierSubFamily.title")}" column="supplierHierarchy2" type="String">
				</Level>
				<Level name="state" caption="${msg("jsolap.supplierState.title")}" column="supplierState" type="String">
				</Level>
			</Hierarchy>
		</Dimension>
	
		
	    <Dimension name="nutritionScale" caption="${msg("jsolap.nutrientScore.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.nutrientScore.caption")}" >
				<Level name="nutrientProfilingClass" caption="${msg("jsolap.nutritionClass.title")}" column="nutrientProfilingClass"  type="String"    />
			</Hierarchy>
		</Dimension>

		<Dimension type="StandardDimension" foreignKey="nodeRef" name="allergenVoluntary" caption="${msg("jsolap.allergenVoluntary.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.allergen.caption")}" primaryKey="entityNodeRef" >
					<View name="allergenList" alias="allergenList">
								<SQL dialect="generic">
									select  
										entityNodeRef,
										doc->>"$.bcpg_allergenListAllergen[0]" as name,
										doc->>"$.bcpg_allergenListAllergen_bcpg_nodeRef[0]" as nodeRef
									from
										allergenList
									where 
									   doc->>"$.bcpg_allergenListVoluntary" = "true"
									<#if !isAdmin>	
									  and instanceId = ${instanceId}
									</#if>
								</SQL>
					</View>
				<Level approxRowCount="100" name="name" caption="${msg("jsolap.allergenVoluntary.title")}" column="nodeRef"  nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>

		<Dimension type="StandardDimension" foreignKey="nodeRef"  name="allergenInVoluntary" caption="${msg("jsolap.allergenInVoluntary.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.allergen.caption")}" primaryKey="entityNodeRef" >
				<View name="allergenList" alias="allergenList">
								<SQL dialect="generic">
									select  
										entityNodeRef,
										doc->>"$.bcpg_allergenListAllergen[0]" as name,
										doc->>"$.bcpg_allergenListAllergen_bcpg_nodeRef[0]" as nodeRef
									from
										allergenList
									where 
									   doc->>"$.bcpg_allergenListVoluntary" = "false" and doc->>"$.bcpg_allergenListInVoluntary" = "true"
									<#if !isAdmin>	
									  and instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level approxRowCount="100" name="name" caption="${msg("jsolap.allergenInVoluntary.title")}" column="nodeRef"  nameColumn="name" type="String"   >
				</Level>

			</Hierarchy>
		</Dimension>							
		
		
		<Dimension type="StandardDimension" foreignKey="nodeRef"  name="ingredient" caption="${msg("jsolap.ingredient.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.ingredient.caption")}" primaryKey="entityNodeRef">
				<View name="ingList" alias="ingList">
								<SQL dialect="generic">
									select  
										entityNodeRef,
										doc->>"$.bcpg_ingListIng[0]" as name,
										doc->>"$.bcpg_ingListIng_bcpg_nodeRef[0]" as nodeRef
									from
										ingList
									<#if !isAdmin>	
									  where instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level name="name" caption="${msg("jsolap.ingredient.title")}" table="ingList" column="nodeRef" nameColumn="name" type="String"   >
				</Level>
			</Hierarchy>
		</Dimension>
		
		<Dimension type="StandardDimension" foreignKey="nodeRef"  name="labelClaim" caption="${msg("jsolap.labelClaim.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.labelClaim.caption")}" primaryKey="entityNodeRef">
				<View name="labelClaimList" alias="labelClaimList">
								<SQL dialect="generic">
									select  
										entityNodeRef,
										doc->>"$.bcpg_lclLabelClaim[0]" as name,
										doc->>"$.bcpg_lclLabelClaim_bcpg_nodeRef[0]" as nodeRef
									from
										labelClaimList
									where doc->>"$.bcpg_lclClaimValue" = "true" 
									<#if !isAdmin>	
									   and instanceId = ${instanceId}
									</#if>
								</SQL>
				</View>
				<Level name="lclLabelClaimName" caption="${msg("jsolap.labelClaim.title")}" table="labelClaimList" column="nodeRef" nameColumn="name" type="String" ></Level>
			</Hierarchy>
		</Dimension>
		
		
		<Dimension type="StandardDimension" foreignKey="nodeRef"  name="composition" caption="${msg("jsolap.component.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.component.caption")}" primaryKeyTable="compoList" primaryKey="entityNodeRef">
				<View name="compoList" alias="compoList">
								<SQL dialect="generic">
									select  a.entityNodeRef,
										b.doc->>"$.cm_name" as name,
										b.nodeRef,
										b.doc->>"$.bcpg_productHierarchy1[0]" as productHierarchy1,
										b.doc->>"$.bcpg_productHierarchy2[0]" as productHierarchy2,
										b.doc->>"$.cm_versionLabel" as versionLabel
									from
										compoList a left join  bcpg_product b on a.doc->>"$.bcpg_compoListProduct_bcpg_nodeRef[0]" = b.nodeRef
									<#if !isAdmin>	
									  where a.instanceId = ${instanceId} and b.instanceId
									</#if>
								</SQL>
				</View>
		
				<Level name="productHierarchy1" caption="${msg("jsolap.componentFamily.title")}" table="compoList" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="productHierarchy2" caption="${msg("jsolap.componentSubFamily.title")}" table="compoList" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="entity_noderef" caption="${msg("jsolap.componentName.title")}" table="compoList" column="nodeRef" nameColumn="name" type="String"   >
				</Level>
				<Level name="versionLabel" caption="${msg("jsolap.componentVersionLabel.title")}" table="compoList" column="versionLabel" type="String" />
			</Hierarchy>
		</Dimension>
	
		<Dimension type="StandardDimension" foreignKey="noderef"  name="packaging" caption="${msg("jsolap.packaging.title")}">
			<Hierarchy hasAll="true" allMemberCaption="${msg("jsolap.packaging.caption")}" primaryKeyTable="packagingList" primaryKey="entityNodeRef">
				<View name="packagingList" alias="packagingList">
						<SQL dialect="generic">
							select  a.entityNodeRef,
								b.doc->>"$.cm_name" as name,
								b.nodeRef,
								b.doc->>"$.bcpg_productHierarchy1[0]" as productHierarchy1,
								b.doc->>"$.bcpg_productHierarchy2[0]" as productHierarchy2,
								b.doc->>"$.cm_versionLabel" as versionLabel
							from
								packagingList a left join  bcpg_product b on a.doc->>"$.bcpg_packagingListProduct_bcpg_nodeRef[0]" = b.nodeRef
							<#if !isAdmin>	
							  where a.instanceId = ${instanceId} and b.instanceId
							</#if>
						</SQL>
				</View>
				
				<Level name="productHierarchy1" caption="${msg("jsolap.packagingFamily.title")}" table="packagingList" column="productHierarchy1" type="String"   >
				</Level>
				<Level name="productHierarchy2" caption="${msg("jsolap.packagingSubFamily.title")}" table="packagingList" column="productHierarchy2" type="String"   >
				</Level>
				<Level name="entity_noderef" caption="${msg("jsolap.packagingName.title")}" table="packagingList" column="nodeRef" nameColumn="name" type="String"   >
				</Level>
				<Level name="versionLabel" caption="${msg("jsolap.packagingVersionLabel.title")}" table="packagingList" column="versionLabel" type="String" />
			</Hierarchy>
		</Dimension>
		

		<Dimension  name="creation" caption="${msg("jsolap.creator.title")}" >
			<Hierarchy name="creators" caption="${msg("jsolap.creator.caption")}" hasAll="true" allMemberCaption="${msg("jsolap.creator.caption")}">
				<Level name="creator"  caption="${msg("jsolap.creator.title")}"  column="creator"  type="String" />
			</Hierarchy>
		</Dimension>
		
		<Dimension  name="modification" caption="${msg("jsolap.modifier.title")}" >
			<Hierarchy name="modifiers" caption="${msg("jsolap.modifier.caption")}" hasAll="true" allMemberCaption="${msg("jsolap.modifier.caption")}" >
				<Level name="modifier" caption="${msg("jsolap.modifier.title")}" column="modifier"  type="String" />
			</Hierarchy>
		</Dimension>
		
		<DimensionUsage name="tags" caption="${msg("jsolap.tags.title")}" source="tagsDimension" foreignKey="nodeRef" />
		
		<#if isAdmin>
			<DimensionUsage name="instance" caption="${msg("jsolap.instance.title")}" source="instancesDimension" foreignKey="instanceId" />
		</#if>
		<DimensionUsage name="productDateCreated" caption="${msg("jsolap.creationDate.title")}" source="timeDimension" foreignKey="productDateCreated" />
		<DimensionUsage name="productDateModified" caption="${msg("jsolap.modificationDate.title")}" source="timeDimension" foreignKey="productDateModified" />
		<DimensionUsage name="startEffectivity" caption="${msg("jsolap.effectivityStart.title")}" source="timeDimension" foreignKey="startEffectivity" />
		<DimensionUsage name="endEffectivity" caption="${msg("jsolap.effectivityEnd.title")}" source="timeDimension" foreignKey="endEffectivity" />
		
		<Measure name="productNumber" caption="${msg("jsolap.productNumber.title")}" column="noderef" datatype="Integer" aggregator="distinct-count" visible="true" />
		<Measure name="projectedQty" caption="${msg("jsolap.projectedQuantity.title")}" column="projectedQty" datatype="Integer" aggregator="sum" visible="true">
		</Measure>
		<Measure name="unitTotalCost" caption="${msg("jsolap.saleUnitCosts.title")}" column="unitTotalCost" datatype="Numeric" aggregator="avg" visible="true" >
		</Measure>
		<Measure name="unitProfitability" caption="${msg("jsolap.unitProfitability.title")}" column="profitability" datatype="Numeric" aggregator="avg" visible="true">
		</Measure>
		<Measure name="unitPrice" caption="${msg("jsolap.unitPrice.title")}" column="unitPrice" datatype="Numeric" aggregator="avg" visible="true">
		</Measure>
		<CalculatedMember name="profit" caption="${msg("jsolap.profitProjectedQuantity.title")}" dimension="Measures" visible="true">
			<Formula>([Measures].[unitPrice] - [Measures].[unitTotalCost])*[Measures].[projectedQty]
			</Formula>
		</CalculatedMember>
		<CalculatedMember name="profitability" caption="${msg("jsolap.profitability.title")}" dimension="Measures" visible="true">
			<Formula>([Measures].[unitPrice] - [Measures].[unitTotalCost])/[Measures].[unitTotalCost]*100</Formula>
		</CalculatedMember>
	</Cube>

	
</Schema>
