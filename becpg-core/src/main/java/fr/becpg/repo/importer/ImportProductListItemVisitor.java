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
import fr.becpg.repo.product.ProductDAO;

public class ImportProductListItemVisitor extends AbstractImportVisitor implements ImportVisitor{

	/** The logger. */
	private static Log logger = LogFactory.getLog(ImportProductListItemVisitor.class);
	
	private ProductDAO productDAO;
	
	private FileFolderService fileFolderService;	
	
	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * Check if the node exists, according to :
	 * - keys or productCode
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
		 * 	Look for product		
		 */				
		
		Map<QName, Serializable> propValues = getNodePropertiesToImport(importContext, values);		
		Map<QName, Serializable> productProperties = new HashMap<QName, Serializable>();
		Map<QName, Serializable> productListItemProperties = new HashMap<QName, Serializable>();
		
		// calculate product properties		
		for(QName qName : classMapping.getNodeColumnKeys()){
			productProperties.put(qName, propValues.get(qName));
		}
		
		// calculate product list properties (there are not product properties)
		for(QName qName : propValues.keySet()){
			
			if(!productProperties.containsKey(qName)){
				productListItemProperties.put(qName, propValues.get(qName));
			}			
		}
		
		QName productType = BeCPGModel.TYPE_PRODUCT;
		QName productCode = BeCPGModel.PROP_PRODUCT_CODE;
		if(!classMapping.getNodeColumnKeys().isEmpty()){
			
			productCode = classMapping.getNodeColumnKeys().get(0);
		}
				
		NodeRef productNodeRef = findNodeByKeyOrCode(importContext, productType, productCode, productProperties);		
		
		if(productNodeRef == null){
			throw new ImporterException("Failed to find product, impossible to import the product data list. Values:" + values);
		}
		
		/*
		 * 	Look for productList
		 */
		
		NodeRef listContainerNodeRef = productDAO.getListContainer(productNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = productDAO.createListContainer(productNodeRef);
		}
		
		NodeRef listNodeRef = productDAO.getList(listContainerNodeRef, importContext.getType());
		if(listNodeRef == null){
			listNodeRef = productDAO.createList(listContainerNodeRef, importContext.getType());
		}			
		
		/*
		 * 	Look for productListItem		
		 */
		
		NodeRef productListItemNodeRef = null;
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

			productListItemNodeRef = findProductListItem(listNodeRef, dataListColumnsProps, dataListColumnsAssocs);
		}		
		
		/*
		 * Create or update product list item
		 */
		
		if(productListItemNodeRef == null){
			 logger.debug("create product list item. Properties: " + productListItemProperties);			 
			 
			 productListItemNodeRef = nodeService.createNode(listNodeRef, 
					 ContentModel.ASSOC_CONTAINS, 
					 ContentModel.ASSOC_CHILDREN, 
					 importContext.getType(), 
					 productListItemProperties).getChildRef();	
			 
		 }
		 else if(importContext.isDoUpdate()){
			 
			 logger.debug("update product list item. Properties: " + productListItemProperties);			 
			 nodeService.setType(productListItemNodeRef, importContext.getType());
			 
			 for(Map.Entry<QName, Serializable> entry : productListItemProperties.entrySet()){
				 nodeService.setProperty(productListItemNodeRef, entry.getKey(), entry.getValue()); 
			 }			 
		 }
		 else{
			 logger.debug("Update mode is not enabled so no update is done.");
		 }
		
		// import associations	
	 	importAssociations(importContext, values, productListItemNodeRef);
	 	
	 	return productListItemNodeRef;
	}
	
	/**
	 * Look for the product list item (check props and assocs match)
	 * @param listNodeRef
	 * @param dataListColumnsProps
	 * @param dataListColumnsAssocs
	 * @return
	 */
	private NodeRef findProductListItem(NodeRef listNodeRef, Map<QName, String> dataListColumnsProps, Map<QName, List<NodeRef>> dataListColumnsAssocs){
		
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
