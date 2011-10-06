-- #############################
--  View used by OLAP Cube
-- #############################



create or replace view becpg_alf_prop (node_id,string_value,long_value,float_value,local_name) as
  select prop.node_id, prop.string_value, prop.long_value, prop.float_value, qname.local_name
	from alf_node_properties prop
	inner join alf_qname qname on prop.qname_id = qname.id;



--
-- bcpg:product
--
--
-- create or replace view Product (id, code,name, legalName, productHierarchy1, productHierarchy2, productState) as
-- select node.id, code.long_value, name.string_value  , legalName.string_value, productHierarchy1.string_value,
-- 		productHierarchy2.string_value, productState.string_value
-- from alf_node node
-- left outer join becpg_alf_prop code on (node.id = code.node_id   and code.local_name = 'code')
-- left outer join becpg_alf_prop name on (node.id = name.node_id   and name.local_name = 'name')
-- left outer join becpg_alf_prop legalName on (node.id = legalName.node_id and legalName.local_name = 'legalName')
-- left outer join becpg_alf_prop productHierarchy1 on (node.id = productHierarchy1.node_id  and productHierarchy1.local_name = 'productHierarchy1')
-- left outer join becpg_alf_prop productHierarchy2 on (node.id = productHierarchy2.node_id  and productHierarchy2.local_name = 'productHierarchy2')
-- left outer join becpg_alf_prop productState on (node.id = productState.node_id  and productState.local_name = 'productState')
-- inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
-- inner join alf_store node_store on (node.store_id = node_store.id)
-- where node_qname.local_name = 'finishedProduct' and node_store.identifier = 'SpacesStore' and node_store.protocol='workspace';
 
 --
 -- becpg:rawMaterial
 --
-- create or replace view MP (id, code,name, legalName, productHierarchy1, productHierarchy2, productState) as
-- select node.id, code.long_value, name.string_value  , legalName.string_value, productHierarchy1.string_value,
-- 		productHierarchy2.string_value, productState.string_value
-- from alf_node node
-- left outer join becpg_alf_prop code on (node.id = code.node_id and code.local_name = 'code')
-- left outer join becpg_alf_prop name on (node.id = name.node_id and name.local_name = 'name')
-- left outer join becpg_alf_prop legalName on (node.id = legalName.node_id and legalName.local_name = 'legalName')
-- left outer join becpg_alf_prop productHierarchy1 on (node.id = productHierarchy1.node_id and productHierarchy1.local_name = 'productHierarchy1')
-- left outer join becpg_alf_prop productHierarchy2 on (node.id = productHierarchy2.node_id and productHierarchy2.local_name = 'productHierarchy2')
-- left outer join becpg_alf_prop productState on (node.id = productState.node_id and productState.local_name = 'productState')
-- inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
-- inner join alf_store node_store on (node.store_id = node_store.id)
-- where node_qname.local_name = 'rawMaterial' and node_store.identifier = 'SpacesStore' and node_store.protocol='workspace';
-- 
--
-- bcpg:client
--
 create or replace view becpg_client (id, code,name,assocId) as
 select node.id, code.long_value, name.string_value , client_assoc.id
 from alf_node node
 inner join becpg_alf_prop code on (node.id = code.node_id and code.local_name = 'code')
 inner join becpg_alf_prop name on (node.id = name.node_id and name.local_name = 'name')
 left outer join alf_node_assoc client_assoc on (client_assoc.target_node_id = node.id and client_assoc.type_qname_id 
 in (select client_assoc_qname.id from alf_qname client_assoc_qname where client_assoc_qname.local_name = 'clients'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
 where node_qname.local_name = 'client' and node_store.identifier = 'SpacesStore' and node_store.protocol='workspace';
 
 --
 -- bcpg:supplier
 --
 create or replace view becpg_supplier (id, code,name,assocId) as
 select node.id, code.long_value, name.string_value , supplier_assoc.id
 from alf_node node
 inner join becpg_alf_prop code on (node.id = code.node_id and code.local_name = 'code')
 inner join becpg_alf_prop name on (node.id = name.node_id and name.local_name = 'name')
 left outer join alf_node_assoc supplier_assoc on (supplier_assoc.target_node_id = node.id and supplier_assoc.type_qname_id 
 in (select supplier_assoc_qname.id from alf_qname supplier_assoc_qname where supplier_assoc_qname.local_name = 'suppliers'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'supplier' and node_store.identifier = 'SpacesStore' and node_store.protocol='workspace';
  
  
  
 --
 -- View Entity list 
 -- Entity List associated with product
 -- bcpg:entityLists
 --
 --
 --
  create or replace view becpg_entity_list (entityId, entityListId, entityType) as
  select parentAssoc.parent_node_id, entityAssoc.child_node_id, entityType.string_value
  from alf_child_assoc entityAssoc
  inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id)
  inner join alf_qname qnameParentAssoc on (parentAssoc.type_qname_id = qnameParentAssoc.id)
  inner join becpg_alf_prop entityType on (entityAssoc.child_node_id = entityType.node_id 
  and entityType.local_name = 'dataListItemType');
  

 
--
-- bcpg:costList
--
-- create or replace view becp_cost_list (id, name, costListValue, entityListId, entityId) as
-- select node.id, name.string_value, costListValue.float_value, entityListAssoc.parent_node_id, parentAssoc.parent_node_id
-- from alf_node node
-- inner join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
-- inner join becpg_alf_prop name on (assoc__.target_node_id = name.node_id and name.local_name = 'name')
-- inner join becpg_alf_prop costListValue on (node.id = costListValue.node_id and costListValue.local_name = 'costListValue')
-- inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
-- inner join alf_store node_store on (node.store_id = node_store.id)
-- inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
-- inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
-- inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id) 
-- inner join alf_qname qnameParentAssoc on (parentAssoc.type_qname_id = qnameParentAssoc.id)
--  where node_qname.local_name = 'costList' and node_store.identifier = 'SpacesStore' and node_store.protocol='workspace'
--  	and qnameParentAssoc.local_name = 'entityLists';
-- 
 
-- 
-- bcpg:nutList
--
 	
 create or replace view becpg_nut_list (id, name, nutType, nutGroup, entityListId) as
 select node.id, name.string_value, nutType.string_value, nutGroup.string_value , entityListAssoc.parent_node_id
 from alf_node node
 inner join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join becpg_alf_prop name on (assoc__.target_node_id = name.node_id and name.local_name = 'name')
 left outer join becpg_alf_prop nutType on (assoc__.target_node_id = nutType.node_id and nutType.local_name = 'nutType')
 left outer join becpg_alf_prop nutGroup on (node.id = nutGroup.node_id and nutGroup.local_name = 'nutGroup')
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'nutList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace';

 --
 -- bcpg:allergenList
 --  
 
 create or replace view becpg_allergen_list (id, name, entityListId) as
select node.id, name.string_value , entityListAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join becpg_alf_prop name on (assoc__.target_node_id = name.node_id and name.local_name = 'name')
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'allergenList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'allergenListAllergen';
  	
  
 --
 -- bcpg:ingList
 --  
 
 create or replace view becpg_ing_list (id, name, entityListId) as
select node.id, name.string_value , entityListAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join becpg_alf_prop name on (assoc__.target_node_id = name.node_id and name.local_name = 'name')
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'ingList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'ingListIng';
  		
 
 --
 -- bcpg:microbioList
 --  
 
 create or replace view becpg_microbio_list (id, name, entityListId) as
select node.id, name.string_value , entityListAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join becpg_alf_prop name on (assoc__.target_node_id = name.node_id and name.local_name = 'name')
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'microbioList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'mblMicrobio';

 --
 -- bcpg:packagingList
 --
  		
create or replace view becpg_packaging_list (id, name, entityListId) as
select node.id, name.string_value , entityListAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join becpg_alf_prop name on (assoc__.target_node_id = name.node_id and name.local_name = 'name')
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'packagingList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'packagingListProduct';
  		
  		
 --
 -- bcpg:compoList
 --
  		 		
create or replace view becpg_compo_list (id, name, entityListId) as
select node.id, name.string_value , entityListAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join becpg_alf_prop name on (assoc__.target_node_id = name.node_id and name.local_name = 'name')
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'compoList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'compoListProduct';
  		
 
  		
