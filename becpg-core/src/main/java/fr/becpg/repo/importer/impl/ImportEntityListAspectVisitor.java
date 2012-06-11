/*
 * 
 */
package fr.becpg.repo.importer.impl;

import java.io.Serializable;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;

// TODO: Auto-generated Javadoc
/**
 * Class used to import a product that has the productAspect but it is not a Product (ie: product template, product microbio criteria, etc...)
 * with its attributes, characteristics and files.
 *
 * @author querephi
 */
public class ImportEntityListAspectVisitor extends AbstractImportVisitor implements ImportVisitor{				
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ImportEntityListAspectVisitor.class);
		
	/** The product dao. */
	private EntityListDAO entityListDAO;	
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.importer.AbstractImportVisitor#importNode(fr.becpg.repo.importer.ImportContext, java.util.List)
	 */
	@Override
	public NodeRef importNode(ImportContext importContext, List<String> values) throws ParseException, ImporterException{
		
		
		// create product node
		NodeRef productNodeRef = super.importNode(importContext, values);
			
		// create list container
		logger.debug("Create lists container");
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);		
		if(listContainerNodeRef == null){
			listContainerNodeRef = entityListDAO.createListContainer(productNodeRef);
		}
		
		// import characteristics
		logger.debug("import characteristics");
		for(int z_idx=0; z_idx<values.size() && z_idx < importContext.getColumns().size(); z_idx++){
			 
			 AbstractAttributeMapping attributeMapping = importContext.getColumns().get(z_idx);
			 
			 if(attributeMapping instanceof CharacteristicMapping){
				 
				 CharacteristicMapping charactMapping = (CharacteristicMapping)attributeMapping;
				 ClassAttributeDefinition column = charactMapping.getAttribute();
				 
				 if(column instanceof PropertyDefinition){
					 Serializable value = ImportHelper.loadPropertyValue(importContext, values, z_idx);					 
					 logger.debug("import characteristic: " + charactMapping.getId() + " - value: " + value);
					 
					 if(value != null){						 
						 
						 NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, charactMapping.getDataListQName());
						 NodeRef linkNodeRef = null;
						 
						 if(listNodeRef == null){
							 listNodeRef = entityListDAO.createList(listContainerNodeRef, charactMapping.getDataListQName());
						 }
						 else{
							 linkNodeRef = entityListDAO.getLink(listNodeRef, charactMapping.getCharactQName(), charactMapping.getCharactNodeRef());
						 }
						
						 if(linkNodeRef != null){				    			
							 nodeService.setProperty(linkNodeRef, column.getName(), value);				    			
						 }
						 else{
							 Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
							 properties.put(column.getName(), value);
							 ChildAssociationRef childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, charactMapping.getCharactNodeRef().getId()), charactMapping.getDataListQName(), properties);
							 nodeService.createAssociation(childAssocRef.getChildRef(), charactMapping.getCharactNodeRef(), charactMapping.getCharactQName());
						 }
					 }
				 } 
			 }			 			
		 }				
				
		return productNodeRef;
	}
	
	/**
	 * Check if the node exists, according to :
	 * - keys or productCode
	 *
	 * @param importContext the import context
	 * @param type the type
	 * @param properties the properties
	 * @return the node ref
	 */
	@Override
	protected NodeRef findNode(ImportContext importContext, QName type, Map<QName, Serializable> properties) throws ImporterException{
		
		NodeRef nodeRef = findNodeByKeyOrCode(importContext, type, BeCPGModel.PROP_CODE, properties);		
		
				
		return nodeRef;	
	}
	
}
