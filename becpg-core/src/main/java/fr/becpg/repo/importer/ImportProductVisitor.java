/*
 * 
 */
package fr.becpg.repo.importer;

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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;

// TODO: Auto-generated Javadoc
/**
 * Class used to import a product with its attributes, characteristics and files.
 *
 * @author querephi
 */
public class ImportProductVisitor extends ImportProductListAspectVisitor implements ImportVisitor{					
	
	// we don't know where is the node ? product may be in the Products folder or in the sites or somewhere else !
	//private static final String PATH_QUERY_PRODUCT_BY_KEYS = " +PATH:\"/app:company_home/cm:Products/*/cm:%s/*/*/*\" ";	
	
	/** The PAT h_ produc t_ folder. */
	private static String PATH_PRODUCT_FOLDER = "./cm:Products/cm:%s/cm:%s/cm:%s/cm:%s";
	
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
			productService.classifyProduct(repositoryHelper.getCompanyHome(), productNodeRef);
		}		
		
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
				
		NodeRef nodeRef = findNodeByKeyOrCode(importContext, type, BeCPGModel.PROP_PRODUCT_CODE, properties);		
		
		// look in the product hierarchy of the repository if we don't import in a site
		if(nodeRef == null && !importContext.isSiteDocLib()){
			
			// state
			String state = (String)properties.get(BeCPGModel.PROP_PRODUCT_STATE);		
			if(state != null && !state.isEmpty()){
				
				// SystemProductType
				SystemProductType systemProductType = SystemProductType.valueOf(type);
				if(!systemProductType.equals(SystemProductType.Unknown)){
					
					// hierarchy 1
					String hierarchy1 = (String)properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1);					
					if(hierarchy1 != null && !hierarchy1.isEmpty()){
						
						// hierarchy 2
						String hierarchy2 = (String)properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY2);					
						if(hierarchy2 != null && !hierarchy2.isEmpty()){
					
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
								nodeRef = nodeService.getChildByName(nodes.get(0), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));																
							}

						}
						else{
							throw new ImporterException("Cannot import a product with the property 'productHierarchy2' empty. Properties: " + properties);
						}
					}
					else{
						throw new ImporterException("Cannot import a product with the property 'productHierarchy1' empty. Properties: " + properties);
					}
				}
				else{
					throw new ImporterException("Cannot import a product, unknown type. Type: " + nodeService.getType(nodeRef));
				}
			}
			else{
				throw new ImporterException("Cannot import a product with the property 'productState' empty. Properties: " + properties);
			}			
		}
		
		// Check if product exists in Import folder
		if(nodeRef == null){			
			nodeRef = nodeService.getChildByName(importContext.getParentNodeRef(), ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));
		}
		
		// productFolder => look for product
		if(nodeRef != null && nodeService.getType(nodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)){
			nodeRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));
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
				nodeColumnKeys.add(BeCPGModel.PROP_PRODUCT_CODE);
			}
			
			for(QName qName : nodeColumnKeys){
				
				Serializable value = properties.get(qName);
				Serializable dbvalue = nodeService.getProperty(nodeRef, qName);
				if(value != null && dbvalue != null && !value.equals(dbvalue)){
					
					throw new ImporterException("Cannot import the product since it will override an existing one. " +
											"It has the same properties (state, hierarchy and name) but code(s) are differents." +
											"Code in import file: " + value + " - Code in DB: " + dbvalue);
				}
			}
		}
		
		return nodeRef;	
	}
	
}
