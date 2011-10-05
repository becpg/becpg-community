/*
 * 
 */
package fr.becpg.repo.importer.impl;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.importer.ClassMapping;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;

// TODO: Auto-generated Javadoc
/**
 * Class used to import a product with its attributes, characteristics and files.
 *
 * @author querephi
 */
public class ImportProductVisitor extends ImportEntityListAspectVisitor implements ImportVisitor{					
	
	/** The PAT h_ produc t_ folder. */
	private static String PATH_PRODUCT_FOLDER = "./cm:Products/cm:%s/cm:%s/cm:%s/cm:%s";
	
	protected static final String MSG_ERROR_PRODUCTHIERARCHY1_EMPTY = "import_service.error.err_producthierarchy1_empty";
	protected static final String MSG_ERROR_PRODUCTHIERARCHY2_EMPTY = "import_service.error.err_producthierarchy2_empty";
	protected static final String MSG_ERROR_UNKNOWN_PRODUCTTYPE = "import_service.error.err_unknown_producttype";
	protected static final String MSG_ERROR_PRODUCTSTATE_EMPTY = "import_service.error.err_productstate_empty";
	protected static final String MSG_ERROR_OVERRIDE_EXISTING_ONE = "import_service.error.err_override_existing_one";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ImportProductVisitor.class);
	
	/** The product service. */
	private ProductService productService;
	
	/** The repository helper. */
	private Repository repositoryHelper;				
	
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.importer.AbstractImportVisitor#importNode(fr.becpg.repo.importer.ImportContext, java.util.List)
	 */
	@Override
	public NodeRef importNode(ImportContext importContext, List<String> values) throws ParseException, ImporterException{		
		
		// create product node
		NodeRef productNodeRef = super.importNode(importContext, values);					
		
		// classify if product is not imported in a site
		if(!importContext.isSiteDocLib()){
			logger.debug("classify product");
			productService.classifyProduct(repositoryHelper.getCompanyHome(), productNodeRef);
		}		
		
		logger.debug("product imported");
		return productNodeRef;
	}
	
	/**
	 * Check if the node exists, according to :
	 * - keys or productCode
	 * - Path where product is classified and name
	 * - Path where product is imported and name.
	 *
	 * @param importContext the import context
	 * @param type the type
	 * @param properties the properties
	 * @return the node ref
	 * @throws ImporterException 
	 */
	@Override
	protected NodeRef findNode(ImportContext importContext, QName type, Map<QName, Serializable> properties) throws ImporterException{
				
		NodeRef nodeRef = findNodeByKeyOrCode(importContext, type, BeCPGModel.PROP_CODE, properties);
		
		// look by name
		if(nodeRef == null){
			
			String name = (String)properties.get(ContentModel.PROP_NAME);
			if(name != null && name != ""){
			
				// look in the product hierarchy of the repository if we don't import in a site
				if(nodeRef == null && !importContext.isSiteDocLib()){
					
					// state
					String state = (String)properties.get(BeCPGModel.PROP_PRODUCT_STATE);		
					if(state != null){			
						if(!state.isEmpty()){
							
							// SystemProductType
							SystemProductType systemProductType = SystemProductType.valueOf(type);
							if(!systemProductType.equals(SystemProductType.Unknown)){
								
								// hierarchy 1
								String hierarchy1 = (String)properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1);					
								if(hierarchy1 != null){
								
									if(!hierarchy1.isEmpty()){
										
										// hierarchy 2
										String hierarchy2 = (String)properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY2);					
										if(hierarchy2 != null){
											if(!hierarchy2.isEmpty()){
											
												// look for path where product should be stored
												String path = String.format(PATH_PRODUCT_FOLDER, 
																			state,
																			systemProductType,
																			ISO9075.encode(hierarchy1),
																			ISO9075.encode(hierarchy2));
												
												List<NodeRef> nodes = searchService.selectNodes(repositoryHelper.getCompanyHome(), 
																								path, 
																								null, namespaceService, false);
																	
												if(!nodes.isEmpty()){
													nodeRef = nodeService.getChildByName(nodes.get(0), ContentModel.ASSOC_CONTAINS, name);																
												}
											}
											else{
												throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY2_EMPTY, properties));
											}
										}															
									}
									else{
										throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY1_EMPTY, properties));
									}
								}							
							}
							else{
								throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNKNOWN_PRODUCTTYPE, nodeService.getType(nodeRef)));
							}
						}
						else{
							throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTSTATE_EMPTY, properties));
						}	
					}					
				}
				
				// Check if product exists in Import folder		
				if(nodeRef == null){			
					nodeRef = nodeService.getChildByName(importContext.getParentNodeRef(), ContentModel.ASSOC_CONTAINS, name);
				}
				
				// productFolder => look for product
				if(nodeRef != null && nodeService.getType(nodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)){
					nodeRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, name);
				}
			}
			else{
				
				throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_GET_OR_CREATE_NODEREF));				
			}
		}
		
		
		
		
		// check key columns, we don't want to update the wrong product
		if(nodeRef != null){																
			
			ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType());
			List<QName> nodeColumnKeys = null;
			if(classMapping != null){
			
				nodeColumnKeys = classMapping.getNodeColumnKeys();
			}	
			else{
				nodeColumnKeys = new ArrayList<QName>();
				nodeColumnKeys.add(BeCPGModel.PROP_CODE);
			}
			
			for(QName qName : nodeColumnKeys){
				
				Serializable value = properties.get(qName);
				Serializable dbvalue = nodeService.getProperty(nodeRef, qName);
				if(value != null && dbvalue != null && !value.equals(dbvalue)){
					
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_OVERRIDE_EXISTING_ONE, value, dbvalue));
				}
			}
		}
		
		return nodeRef;	
	}
	
}
