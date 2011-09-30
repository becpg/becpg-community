--  View  Product (bcpg:product)
--		
--		
--	#bcpg:codeAspect
--		bcpg:code
--		
--	#bcpg:productAspect
--		bcpg:legalName
--		bcpg:productComments
--		bcpg:productHierarchy1
--		bcpg:productHierarchy2
--		bcpg:productState
--		bcpg:productUnit
--
--	#bcpg:entityListsAspect
--		bcpg:entityLists
-- 


create or replace view Product (id, code,name, legalName, productHierarchy1, productHierarchy2, productState) as
 select id, code.long_value, name.string_value  , legalName.string_value, productHierarchy1.string_value,
 		productHierarchy2.string_value, productState.string_value
 from alf_node
 left outer join alf_node_properties code on (id = code.node_id and code.qname_id in (select id from alf_qname where  local_name = 'code'))
 left outer join alf_node_properties name on (id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 left outer join alf_node_properties legalName on (id = legalName.node_id and legalName.qname_id in (select id from alf_qname where  local_name = 'legalName'))
 left outer join alf_node_properties productHierarchy1 on (id = productHierarchy1.node_id and productHierarchy1.qname_id in (select id from alf_qname where  local_name = 'productHierarchy1'))
 left outer join alf_node_properties productHierarchy2 on (id = productHierarchy2.node_id and productHierarchy2.qname_id in (select id from alf_qname where  local_name = 'productHierarchy2'))
 left outer join alf_node_properties productState on (id = productState.node_id and productState.qname_id in (select id from alf_qname where  local_name = 'productState'))
 where 
 (
 type_qname_id in (select id from alf_qname where  local_name = 'finishedProduct')
 )
 and store_id in (select id from alf_store where  identifier = 'SpacesStore' and protocol='workspace'); 
 
 -- Matière première
 
 create or replace view MP (id, code,name, legalName, productHierarchy1, productHierarchy2, productState) as
 select id, code.long_value, name.string_value  , legalName.string_value, productHierarchy1.string_value,
 		productHierarchy2.string_value, productState.string_value
 from alf_node
 left outer join alf_node_properties code on (id = code.node_id and code.qname_id in (select id from alf_qname where  local_name = 'code'))
 left outer join alf_node_properties name on (id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 left outer join alf_node_properties legalName on (id = legalName.node_id and legalName.qname_id in (select id from alf_qname where  local_name = 'legalName'))
 left outer join alf_node_properties productHierarchy1 on (id = productHierarchy1.node_id and productHierarchy1.qname_id in (select id from alf_qname where  local_name = 'productHierarchy1'))
 left outer join alf_node_properties productHierarchy2 on (id = productHierarchy2.node_id and productHierarchy2.qname_id in (select id from alf_qname where  local_name = 'productHierarchy2'))
 left outer join alf_node_properties productState on (id = productState.node_id and productState.qname_id in (select id from alf_qname where  local_name = 'productState'))
 where 
 (
 type_qname_id in (select id from alf_qname where  local_name = 'rawMaterial')
 )
 and store_id in (select id from alf_store where  identifier = 'SpacesStore' and protocol='workspace'); 
 
 
 --  Accociation Client Product bcpg:clientsAspect
 --  bcpg:clients
 
 
 create or replace view Product_assoc_Client (id, Product, Client) as
 select assoc__.id, assoc__.source_node_id, assoc__.target_node_id
 from alf_node_assoc assoc__
 left outer join alf_node node on node.id= assoc__.source_node_id
 where assoc__.type_qname_id in (select id from alf_qname where  local_name = 'clients')
 and node.type_qname_id in (select id from alf_qname where  local_name = 'finishedProduct');
 
 
 
 
 -- View Client (bcpg:client)
--   clientTel
--   clientEmail
 -- #bcpg:codeAspect
 --
 -- #bcpg:location
 create or replace view Client (id, code,name,title) as
 select id, code.long_value, name.string_value , title.string_value 
 from alf_node
 left outer join alf_node_properties code on (id = code.node_id and code.qname_id in (select id from alf_qname where  local_name = 'code'))
 left outer join alf_node_properties name on (id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 left outer join alf_node_properties title on (id = title.node_id and title.qname_id in (select id from alf_qname where  local_name = 'title'))
  where 
 (
 type_qname_id in (select id from alf_qname where  local_name = 'client')
 )
 and store_id in (select id from alf_store where  identifier = 'SpacesStore' and protocol='workspace'); 
 
 
 
-- View Caract Cost (bcpg:costList)
--    costListValue
--    costListUnit
-- #bcpg:costListCost
--
 create or replace view Cost (id, name, costCurrency,costListValue,costListUnit, entityListId) as
 select node.id, name.string_value, costCurrency.string_value,costListValue.float_value, costListUnit.string_value , entityListAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 left outer join alf_node_properties costCurrency on (assoc__.target_node_id = costCurrency.node_id and costCurrency.qname_id in (select id from alf_qname where  local_name = 'costCurrency'))
 left outer join alf_node_properties costListValue on (node.id = costListValue.node_id and costListValue.qname_id in (select id from alf_qname where  local_name = 'costListValue'))
 left outer join alf_node_properties costListUnit on (node.id = costListUnit.node_id and costListUnit.qname_id in (select id from alf_qname where  local_name = 'costListUnit'))
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 where 
 (
 node.type_qname_id in (select id from alf_qname where  local_name = 'costList')
 )
 and  node.store_id in (select id from alf_store where  identifier = 'SpacesStore' and protocol='workspace'); 
 
 
 
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
  
 -- bcpg:nutList
 -- bcpg:nutType
 -- bcpg:nutGroup
 	
 create or replace view NutList (id, name,nutType, nutGroup, entityListId) as
 select node.id, name.string_value, nutType.string_value, nutGroup.string_value , entityListAssoc.parent_node_id
 from alf_node node
 left outer join alf_node_assoc assoc__ on assoc__.source_node_id = node.id
 left outer join alf_node_properties name on (assoc__.target_node_id = name.node_id and name.qname_id in (select id from alf_qname where  local_name = 'name'))
 left outer join alf_node_properties nutType on (assoc__.target_node_id = nutType.node_id and nutType.qname_id in (select id from alf_qname where  local_name = 'nutType'))
 left outer join alf_node_properties nutGroup on (node.id = nutGroup.node_id and nutGroup.qname_id in (select id from alf_qname where  local_name = 'nutGroup'))
 inner join alf_child_assoc entityListAssoc on (node.id = entityListAssoc.child_node_id)
 where 
 (
 node.type_qname_id in (select id from alf_qname where  local_name = 'nutList')
 )
 and  node.store_id in (select id from alf_store where  identifier = 'SpacesStore' and protocol='workspace'); 

 	
 
 
 -- Select all cost from product 
 
 select * from EntityList where entityType = 'bcpg:costList';
 select * from Cost cost where cost.id in ( select entityListId from EntityList where entityType = 'bcpg:costList');
 

 