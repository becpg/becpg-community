
analyze table alf_node;
analyze table alf_node_properties;
analyze table alf_child_assoc;

--
-- Populate temp table;
--

delete from becpg_entity_list_temp;

insert into becpg_entity_list_temp(entityId, entityListId, entityType)
	select entityId, entityListId, entityType from becpg_entity_list;


--
-- Populate product fact table;
--

delete from becpg_product;

insert into becpg_product(product_fact_id, 
 				product_code,
 				product_name,
 				product_legalName,
 				product_productHierarchy1,
 				product_productHierarchy2,
 				product_productState,
 				product_type,
 				product_startEffectivity_id,
 				product_endEffectivity_id,
 				product_createdDate_id,
 				product_modifiedDate_id,
 				product_nut_assoc_id,
 				product_allergen_assoc_id,
 				product_ing_assoc_id,
 				product_microbio_assoc_id,
 				product_packaging_assoc_id,
 				product_compo_assoc_id
 				)
select node.id, code.string_value, name.string_value  , legalName.string_value, productHierarchy1.string_value,
 		productHierarchy2.string_value, productState.string_value, node_qname.local_name,
 		year (startEffectivity.string_value) * 10000 + month (startEffectivity.string_value) * 100 + day (startEffectivity.string_value), 
 		year (endEffectivity.string_value) * 10000 + month (endEffectivity.string_value) * 100 + day (endEffectivity.string_value), 
 		year (node.audit_created) * 10000 + month (node.audit_created) * 100 + day (node.audit_created), 
 		year (node.audit_modified) * 10000 + month (node.audit_modified) * 100 + day (node.audit_modified), 
 		nut_assoc.entityListId,
 		allergen_assoc.entityListId,
 		ing_assoc.entityListId,
 		microbio_assoc.entityListId,
 		packaging_assoc.entityListId,
 		compo_assoc.entityListId
 from alf_node node
 inner join  alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join  becpg_alf_prop code on (node.id = code.node_id   and code.local_name = 'code')
 inner join  becpg_alf_prop name on (node.id = name.node_id   and name.local_name = 'name')
 inner join  becpg_alf_prop productHierarchy1 on (node.id = productHierarchy1.node_id  and productHierarchy1.local_name = 'productHierarchy1')
 inner join  becpg_alf_prop productHierarchy2 on (node.id = productHierarchy2.node_id  and productHierarchy2.local_name = 'productHierarchy2')
 left outer join becpg_alf_prop legalName on (node.id = legalName.node_id and legalName.local_name = 'legalName')
 left outer join becpg_alf_prop startEffectivity on (node.id = startEffectivity.node_id  and startEffectivity.local_name = 'startEffectivity')
 left outer join becpg_alf_prop endEffectivity on (node.id = endEffectivity.node_id  and endEffectivity.local_name = 'endEffectivity')
 left outer join becpg_alf_prop productState on (node.id = productState.node_id  and productState.local_name = 'productState')
 left outer join becpg_entity_list_temp nut_assoc on (nut_assoc.entityId = node.id and nut_assoc.entityType = 'bcpg:nutList')
 left outer join becpg_entity_list_temp allergen_assoc on (allergen_assoc.entityId = node.id and allergen_assoc.entityType = 'bcpg:allergenList')
 left outer join becpg_entity_list_temp ing_assoc on (ing_assoc.entityId = node.id and ing_assoc.entityType = 'bcpg:ingList')
 left outer join becpg_entity_list_temp microbio_assoc on (microbio_assoc.entityId = node.id and microbio_assoc.entityType = 'bcpg:microbioList')
 left outer join becpg_entity_list_temp packaging_assoc on (packaging_assoc.entityId = node.id and packaging_assoc.entityType = 'bcpg:packagingList')
 left outer join becpg_entity_list_temp compo_assoc on (compo_assoc.entityId = node.id and compo_assoc.entityType = 'bcpg:compoList')
 where node.store_id in (select id from alf_store where identifier = 'SpacesStore' and protocol = 'workspace')
 order by code.long_value, name.string_value;

 
delete from becpg_product where product_fact_id in 
 	( select target_node_id from
 			alf_node_assoc  where type_qname_id 
	 in (select id from alf_qname where local_name = 'simulationSourceItem'));

	 
	 
update becpg_product node, becpg_alf_prop projectedQty set node.product_projectedQty = projectedQty.long_value where node.product_fact_id = projectedQty.node_id   and projectedQty.local_name = 'projectedQty';
update becpg_product node, becpg_alf_prop unitTotalCost set node.product_unitTotalCost = unitTotalCost.float_value where node.product_fact_id = unitTotalCost.node_id   and unitTotalCost.local_name = 'unitTotalCost';
update becpg_product node, becpg_alf_prop unitPrice set node.product_unitPrice = unitPrice.float_value where node.product_fact_id = node.product_fact_id = unitPrice.node_id   and unitPrice.local_name = 'unitPrice';
	 
 
update  becpg_product set product_startEffectivity_id=20010101 where  product_startEffectivity_id is null or product_startEffectivity_id=0;
update  becpg_product set product_endEffectivity_id=20300101 where  product_endEffectivity_id is null;
 
--
-- Populate cost fact table;
--
 
delete from becpg_cost;


insert into becpg_cost(cost_fact_id, 
 				cost_name,
 				cost_value,
 				cost_startEffectivity_id,
 				cost_endEffectivity_id,
 				cost_assoc_id,
 				cost_source_id
 				)
 select node.id, name.string_value, costListValue.float_value,entity.product_startEffectivity_id, entity.product_endEffectivity_id ,  parentAssoc.parent_node_id, assoc_source.target_node_id
 from alf_node node
 inner join alf_node_assoc assoc__ on assoc__.source_node_id = node.id  and assoc__.type_qname_id in (select id from alf_qname  where local_name = 'costDetailsListCost') 
 inner join becpg_alf_prop name on (assoc__.target_node_id = name.node_id and name.local_name = 'name')
 inner join alf_node_assoc assoc_source on assoc_source.source_node_id = node.id and assoc_source.type_qname_id in (select id from alf_qname  where local_name = 'costDetailsListSource') 
 inner join becpg_alf_prop costListValue on (node.id = costListValue.node_id and costListValue.local_name = 'costDetailsListValue')
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
 inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id) 
 inner join alf_qname qnameParentAssoc on (parentAssoc.type_qname_id = qnameParentAssoc.id)
 inner join becpg_product entity on (entity.product_fact_id = parentAssoc.parent_node_id)
  where node_qname.local_name = 'costDetailsList' and node_store.identifier = 'SpacesStore' and node_store.protocol='workspace'
  	and qnameParentAssoc.local_name = 'entityLists';

 
 



