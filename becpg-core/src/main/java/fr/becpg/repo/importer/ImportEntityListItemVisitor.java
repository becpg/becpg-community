package fr.becpg.repo.importer;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;

public class ImportEntityListItemVisitor extends AbstractImportVisitor implements ImportVisitor{

	/** The logger. */
	private static Log logger = LogFactory.getLog(ImportEntityListItemVisitor.class);
	
	private EntityListDAO entityListDAO;
	
	private FileFolderService fileFolderService;	
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * Check if the node exists, according to :
	 * - keys or entityCode
	 *
	 * @param importContext the import context
	 * @param type the type
	 * @param properties the properties
	 * @return the node ref
	 * @throws InvalidTargetNodeException 
	 */
	@Override
	public NodeRef importNode(ImportContext importContext, List<String> values) throws ParseException, ImporterException{
						
		ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType());
		
		/*
		 * 	Look for entity		
		 */				
		
		Map<QName, Serializable> propValues = getNodePropertiesToImport(importContext, values);		
		Map<QName, Serializable> entityProperties = new HashMap<QName, Serializable>();
		Map<QName, Serializable> entityListItemProperties = new HashMap<QName, Serializable>();
		
		// calculate entity properties		
		for(QName qName : classMapping.getNodeColumnKeys()){
			entityProperties.put(qName, propValues.get(qName));
		}
		
		// calculate entity list properties (there are not entity properties)
		for(QName qName : propValues.keySet()){
			
			if(!entityProperties.containsKey(qName)){
				entityListItemProperties.put(qName, propValues.get(qName));
			}			
		}
		
		//TODO gérer les entités qui ne sont pas des produits (arg dans le xml de définition ou fichier d'import ou evaluator)		
		QName entityType = BeCPGModel.TYPE_PRODUCT;
		QName entityCode = BeCPGModel.PROP_CODE;
		if(!classMapping.getNodeColumnKeys().isEmpty()){
			
			entityCode = classMapping.getNodeColumnKeys().get(0);
		}
				
		NodeRef entityNodeRef = findNodeByKeyOrCode(importContext, entityType, entityCode, entityProperties);		
		
		if(entityNodeRef == null){
			throw new ImporterException("Failed to find entity, impossible to import the entity data list. Values:" + values);
		}
		
		/*
		 * 	Look for entityList
		 */
		
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = entityListDAO.createListContainer(entityNodeRef);
		}
		
		NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, importContext.getType());
		if(listNodeRef == null){
			listNodeRef = entityListDAO.createList(listContainerNodeRef, importContext.getType());
		}			
		
		/*
		 * 	Look for entityListItem		
		 */
		
		NodeRef entityListItemNodeRef = null;
		Map<QName, String> dataListColumnsProps = new HashMap<QName, String>();
		Map<QName, List<NodeRef>> dataListColumnsAssocs = new HashMap<QName, List<NodeRef>>();
		
		if(!classMapping.getDataListColumnKeys().isEmpty()){			
			
			for(QName qName : classMapping.getDataListColumnKeys()){
				
				int z_idx = -1, i = 0;				
				for(AbstractAttributeMapping a : classMapping.getColumns()){
					
					if(qName.equals(a.getAttribute().getName())){
						z_idx = i;
					}
					i++;
				}
				
				String value = values.get(z_idx);
				PropertyDefinition propertyDef = dictionaryService.getProperty(qName);				
				
				if(propertyDef instanceof PropertyDefinition){
					
					dataListColumnsProps.put(qName, value);
				}
				else{							
					
					AssociationDefinition associationDef = dictionaryService.getAssociation(qName);					
					List<NodeRef> targetRefs = findTargetNodesByValue(importContext, associationDef, value);
					dataListColumnsAssocs.put(qName, targetRefs);				
				}			
			}

			entityListItemNodeRef = findEntityListItem(listNodeRef, dataListColumnsProps, dataListColumnsAssocs);
		}		
		
		/*
		 * Create or update entity list item
		 */
		
		if(entityListItemNodeRef == null){
			 logger.debug("create entity list item. Properties: " + entityListItemProperties);			 
			 
			 entityListItemNodeRef = nodeService.createNode(listNodeRef, 
					 ContentModel.ASSOC_CONTAINS, 
					 ContentModel.ASSOC_CHILDREN, 
					 importContext.getType(), 
					 entityListItemProperties).getChildRef();	
			 
		 }
		 else if(importContext.isDoUpdate()){
			 
			 logger.debug("update entity list item. Properties: " + entityListItemProperties);			 
			 nodeService.setType(entityListItemNodeRef, importContext.getType());
			 
			 for(Map.Entry<QName, Serializable> entry : entityListItemProperties.entrySet()){
				 nodeService.setProperty(entityListItemNodeRef, entry.getKey(), entry.getValue()); 
			 }			 
		 }
		 else{
			 logger.debug("Update mode is not enabled so no update is done.");
		 }
		
		// import associations	
	 	importAssociations(importContext, values, entityListItemNodeRef);
	 	
	 	return entityListItemNodeRef;
	}
	
	/**
	 * Look for the entity list item (check props and assocs match)
	 * @param listNodeRef
	 * @param dataListColumnsProps
	 * @param dataListColumnsAssocs
	 * @return
	 */
	private NodeRef findEntityListItem(NodeRef listNodeRef, Map<QName, String> dataListColumnsProps, Map<QName, List<NodeRef>> dataListColumnsAssocs){
		
		List<FileInfo> nodes = fileFolderService.list(listNodeRef);
		NodeRef nodeRef = null;		
		boolean isFound = true;
		
		for(FileInfo node : nodes){
			
			isFound = true;
			nodeRef = node.getNodeRef();			
			
			// check properties match
			for(Map.Entry<QName, String> dataListColumnProps : dataListColumnsProps.entrySet()){
				
				Serializable s = nodeService.getProperty(nodeRef, dataListColumnProps.getKey());
				
				if(dataListColumnProps.getValue() == null){
					if(s != null){
						isFound = false;
						break;
					}
				}
				else if(!dataListColumnProps.getValue().equals(s)){
					isFound = false;
					break;
				}
			}
			
			// check associations match
			for(Map.Entry<QName, List<NodeRef>> dataListColumnAssocs : dataListColumnsAssocs.entrySet()){
				
				List<NodeRef> targetRefs1 = dataListColumnAssocs.getValue();							
				List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, dataListColumnAssocs.getKey());
				List<NodeRef> targetRefs2 = new ArrayList<NodeRef>();
				for(AssociationRef assocRef : assocRefs)
					targetRefs2.add(assocRef.getTargetRef());
				
				if(targetRefs1 == null){
					if(targetRefs2 != null){
						isFound = false;
						break;
					}
				}
				else if(!targetRefs1.equals(targetRefs2)){
					isFound = false;
					break;
				}
			}
			
			// OK, we found it
			if(isFound){
				break;
			}
		}
		
		return isFound ? nodeRef : null;
	}
}
