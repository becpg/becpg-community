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
 				product_client_assoc_id,
 				product_supplier_assoc_id,
 				product_nut_assoc_id,
 				product_allergen_assoc_id,
 				product_ing_assoc_id,
 				product_microbio_assoc_id,
 				product_packaging_assoc_id,
 				product_compo_assoc_id
 				)
select node.id, code.long_value, name.string_value  , legalName.string_value, productHierarchy1.string_value,
 		productHierarchy2.string_value, productState.string_value, node_qname.local_name,
 		year (startEffectivity.string_value) * 10000 + month (startEffectivity.string_value) * 100 + day (startEffectivity.string_value), 
 		year (endEffectivity.string_value) * 10000 + month (endEffectivity.string_value) * 100 + day (endEffectivity.string_value), 
 		client_assoc.id,
 		supplier_assoc.id,
 		nut_assoc.entityListId,
 		allergen_assoc.entityListId,
 		ing_assoc.entityListId,
 		microbio_assoc.entityListId,
 		packaging_assoc.entityListId,
 		compo_assoc.entityListId
 from alf_node node
 inner join becpg_alf_prop code on (node.id = code.node_id   and code.local_name = 'code')
 inner join becpg_alf_prop name on (node.id = name.node_id   and name.local_name = 'name')
 inner join  becpg_alf_prop productHierarchy1 on (node.id = productHierarchy1.node_id  and productHierarchy1.local_name = 'productHierarchy1')
 inner join  becpg_alf_prop productHierarchy2 on (node.id = productHierarchy2.node_id  and productHierarchy2.local_name = 'productHierarchy2')
 left outer join becpg_alf_prop legalName on (node.id = legalName.node_id and legalName.local_name = 'legalName')
 left outer join becpg_alf_prop startEffectivity on (node.id = startEffectivity.node_id  and startEffectivity.local_name = 'startEffectivity')
 left outer join becpg_alf_prop endEffectivity on (node.id = endEffectivity.node_id  and endEffectivity.local_name = 'endEffectivity')
 left outer join becpg_alf_prop productState on (node.id = productState.node_id  and productState.local_name = 'productState')
 left outer join becpg_entity_list nut_assoc on (nut_assoc.entityId = node.id and nut_assoc.entityType = 'bcpg:nutList')
 left outer join becpg_entity_list allergen_assoc on (allergen_assoc.entityId = node.id and allergen_assoc.entityType = 'bcpg:allergenList')
 left outer join becpg_entity_list ing_assoc on (ing_assoc.entityId = node.id and ing_assoc.entityType = 'bcpg:ingList')
 left outer join becpg_entity_list microbio_assoc on (microbio_assoc.entityId = node.id and microbio_assoc.entityType = 'bcpg:microbioList')
 left outer join becpg_entity_list packaging_assoc on (packaging_assoc.entityId = node.id and packaging_assoc.entityType = 'bcpg:packagingList')
 left outer join becpg_entity_list compo_assoc on (compo_assoc.entityId = node.id and compo_assoc.entityType = 'bcpg:compoList')
 left outer join alf_node_assoc client_assoc on (client_assoc.source_node_id = node.id and client_assoc.type_qname_id 
 in (select client_assoc_qname.id from alf_qname client_assoc_qname where client_assoc_qname.local_name = 'clients'))
 left outer join alf_node_assoc supplier_assoc on (supplier_assoc.source_node_id = node.id and supplier_assoc.type_qname_id 
 in (select supplier_assoc.id from alf_qname supplier_assoc_qname where supplier_assoc_qname.local_name = 'suppliers'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
 where node_store.identifier = 'SpacesStore' and node_store.protocol='workspace'
 order by code.long_value, name.string_value;
 
update  becpg_product set product_startEffectivity_id=20010101 where  product_startEffectivity_id is null or product_startEffectivity_id=0;
update  becpg_product set product_endEffectivity_id=20300101 where  product_endEffectivity_id is null;
