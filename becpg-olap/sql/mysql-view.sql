-- #############################
--  View used by OLAP Cube
-- #############################



--
-- bcpg:product
--

create or replace view Product (id, code,name, legalName, productHierarchy1, productHierarchy2, productState) as
 select node.id, code.long_value, name.string_value  , legalName.string_value, productHierarchy1.string_value,
 		productHierarchy2.string_value, productState.string_value
 from alf_node node
 left outer join alf_node_properties code on (node.id = code.node_id and code.qname_id in (select id from alf_qname where  local_name = 'code'))
 left outer join alf_node_properties name on (node.id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 left outer join alf_node_properties legalName on (node.id = legalName.node_id and legalName.qname_id in (select id from alf_qname where  local_name = 'legalName'))
 left outer join alf_node_properties productHierarchy1 on (node.id = productHierarchy1.node_id and productHierarchy1.qname_id in (select id from alf_qname where  local_name = 'productHierarchy1'))
 left outer join alf_node_properties productHierarchy2 on (node.id = productHierarchy2.node_id and productHierarchy2.qname_id in (select id from alf_qname where  local_name = 'productHierarchy2'))
 left outer join alf_node_properties productState on (node.id = productState.node_id and productState.qname_id in (select id from alf_qname where  local_name = 'productState'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
 where node_qname.local_name = 'finishedProduct' and node_store.identifier = 'SpacesStore' and node_store.protocol='workspace';
 
 --
 -- becpg:rawMaterial
 --
 create or replace view MP (id, code,name, legalName, productHierarchy1, productHierarchy2, productState) as
 select node.id, code.long_value, name.string_value  , legalName.string_value, productHierarchy1.string_value,
 		productHierarchy2.string_value, productState.string_value
 from alf_node node
 left outer join alf_node_properties code on (node.id = code.node_id and code.qname_id in (select id from alf_qname where  local_name = 'code'))
 left outer join alf_node_properties name on (node.id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 left outer join alf_node_properties legalName on (node.id = legalName.node_id and legalName.qname_id in (select id from alf_qname where  local_name = 'legalName'))
 left outer join alf_node_properties productHierarchy1 on (node.id = productHierarchy1.node_id and productHierarchy1.qname_id in (select id from alf_qname where  local_name = 'productHierarchy1'))
 left outer join alf_node_properties productHierarchy2 on (node.id = productHierarchy2.node_id and productHierarchy2.qname_id in (select id from alf_qname where  local_name = 'productHierarchy2'))
 left outer join alf_node_properties productState on (node.id = productState.node_id and productState.qname_id in (select id from alf_qname where  local_name = 'productState'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
 where node_qname.local_name = 'rawMaterial' and node_store.identifier = 'SpacesStore' and node_store.protocol='workspace';
 
--
-- bcpg:client
--
 create or replace view Client (id, code,name) as
 select node.id, code.long_value, name.string_value 
 from alf_node node
 left outer join alf_node_properties code on (node.id = code.node_id and code.qname_id in (select id from alf_qname where  local_name = 'code'))
 left outer join alf_node_properties name on (node.id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
 where node_qname.local_name = 'client' and node_store.identifier = 'SpacesStore' and node_store.protocol='workspace';
 
 --
 -- bcpg:supplier
 --
 create or replace view Supplier (id, code,name) as
 select node.id, code.long_value, name.string_value 
 from alf_node node
 left outer join alf_node_properties code on (node.id = code.node_id and code.qname_id in (select id from alf_qname where  local_name = 'code'))
 left outer join alf_node_properties name on (node.id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
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
  create or replace view EntityList (entityId, entityListId, entityType) as
  select parentAssoc.parent_node_id, entityAssoc.child_node_id, entityType.string_value
 from alf_child_assoc entityAssoc
 inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id
  and  parentAssoc.type_qname_id in  (select id from alf_qname where  local_name = 'entityLists'))
  inner join alf_node_properties entityType on (entityAssoc.child_node_id = entityType.node_id 
  and entityType.qname_id in (select id from alf_qname where  local_name = 'dataListItemType'));
  

 
--
-- bcpg:costList
--
 create or replace view Cost (id, name, costListValue, entityListId, entityId) as
 select node.id, name.string_value, costListValue.float_value, entityListAssoc.parent_node_id, parentAssoc.parent_node_id
 from alf_node node
 inner join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 inner join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 inner join alf_node_properties costListValue on (node.id = costListValue.node_id and costListValue.qname_id in (select id from alf_qname where  local_name = 'costListValue'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
  inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id
  and  parentAssoc.type_qname_id in  (select id from alf_qname where  local_name = 'entityLists'))
  where node_qname.local_name = 'costList' and node_store.identifier = 'SpacesStore' and node_store.protocol='workspace';
 
 
-- 
-- bcpg:nutList
--
 	
 create or replace view NutList (id, name, nutType, nutGroup, entityListId,entityId) as
 select node.id, name.string_value, nutType.string_value, nutGroup.string_value , entityListAssoc.parent_node_id, parentAssoc.parent_node_id
 from alf_node node
 inner join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 left outer join alf_node_properties nutType on (assoc__.target_node_id = nutType.node_id and nutType.qname_id in (select id from alf_qname where  local_name = 'nutType'))
 left outer join alf_node_properties nutGroup on (node.id = nutGroup.node_id and nutGroup.qname_id in (select id from alf_qname where  local_name = 'nutGroup'))
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
 inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id
  and  parentAssoc.type_qname_id in  (select id from alf_qname where  local_name = 'entityLists'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'nutList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace';

 --
 -- bcpg:allergenList
 --  
 
 create or replace view AllergenList (id, name, entityListId,entityId) as
select node.id, name.string_value , entityListAssoc.parent_node_id, parentAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
  inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id
  and  parentAssoc.type_qname_id in  (select id from alf_qname where  local_name = 'entityLists'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'allergenList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'allergenListAllergen';
  	
  
 --
 -- bcpg:ingList
 --  
 
 create or replace view IngList (id, name, entityListId,entityId) as
select node.id, name.string_value , entityListAssoc.parent_node_id, parentAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
  inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id
  and  parentAssoc.type_qname_id in  (select id from alf_qname where  local_name = 'entityLists'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'ingList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'ingListIng';
  		
 
 --
 -- bcpg:microbioList
 --  
 
 create or replace view MicrobioList (id, name, entityListId,entityId) as
select node.id, name.string_value , entityListAssoc.parent_node_id, parentAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
  inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id
  and  parentAssoc.type_qname_id in  (select id from alf_qname where  local_name = 'entityLists'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'microbioList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'mblMicrobio';
  		
--
-- bcpg:organoList
--

  
 create or replace view OrganoList (id, name, entityListId,entityId) as
select node.id, name.string_value , entityListAssoc.parent_node_id, parentAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
  inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id
  and  parentAssoc.type_qname_id in  (select id from alf_qname where  local_name = 'entityLists'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'organoList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'organoListOrgano'; 		
  		
  		
--
-- bcpg:physicoChemList
--

create or replace view PhysicoChemList (id, name, entityListId, entityId) as
select node.id, name.string_value , entityListAssoc.parent_node_id, parentAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
  inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id
  and  parentAssoc.type_qname_id in  (select id from alf_qname where  local_name = 'entityLists'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'physicoChemList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'pclPhysicoChem';
  		
  		
 --
 -- bcpg:packagingList
 --
  		
create or replace view PackagingList (id, name, entityListId,entityId) as
select node.id, name.string_value , entityListAssoc.parent_node_id, parentAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
  inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id
  and  parentAssoc.type_qname_id in  (select id from alf_qname where  local_name = 'entityLists'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'packagingList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'packagingListProduct';
  		
  		
 --
 -- bcpg:compoList
 --
  		 		
create or replace view CompoList (id, name, entityListId,entityId) as
select node.id, name.string_value , entityListAssoc.parent_node_id, parentAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 inner join alf_qname assoc_qname on (assoc_qname.id =  assoc__.type_qname_id)
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 inner join alf_child_assoc entityAssoc  on (entityListAssoc.parent_node_id = entityAssoc.child_node_id)
  inner join alf_child_assoc parentAssoc on (parentAssoc.child_node_id = entityAssoc.parent_node_id
  and  parentAssoc.type_qname_id in  (select id from alf_qname where  local_name = 'entityLists'))
 inner join alf_qname node_qname on (node.type_qname_id= node_qname.id)
 inner join alf_store node_store on (node.store_id = node_store.id)
  where node_qname.local_name = 'compoList' 
  		and node_store.identifier = 'SpacesStore' 
  		and node_store.protocol='workspace'
  		and assoc_qname.local_name = 'compoListProduct';
  		
 