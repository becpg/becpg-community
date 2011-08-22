/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.report;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.common.RepoConsts;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.PropertyService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;

/**
 * Class used to manage product report instances (used by report visitors classes)
 *
 * @author querephi
 */
public class ProductReportServiceImpl implements ProductReportService {		
	
	/** The Constant TAG_PRODUCT. */
	private static final String TAG_PRODUCT = "product";	
	
	/** The Constant TAG_ALLERGENLIST. */
	private static final String TAG_ALLERGENLIST = "allergenList";
	
	/** The Constant TAG_COMPOLIST. */
	private static final String TAG_COMPOLIST = "compoList";
	
	/** The Constant TAG_COSTLIST. */
	private static final String TAG_COSTLIST = "costList";
	
	/** The Constant TAG_INGLIST. */
	private static final String TAG_INGLIST = "ingList";
	
	/** The Constant TAG_INGLABELINGLIST. */
	private static final String TAG_INGLABELINGLIST = "ingLabelingList";
	
	/** The Constant TAG_NUTLIST. */
	private static final String TAG_NUTLIST = "nutList";	
	
	/** The Constant TAG_ORGANOLIST. */
	private static final String TAG_ORGANOLIST = "organoList";
	
	/** The Constant TAG_MICROBIOLIST. */
	private static final String TAG_MICROBIOLIST = "microbioList";
	
	/** The Constant TAG_PHYSICOCHEMLIST. */
	private static final String TAG_PHYSICOCHEMLIST = "physicoChemList";
	
	/** The Constant TAG_ALLERGEN. */
	private static final String TAG_ALLERGEN = "allergen";
	
	/** The Constant TAG_COST. */
	private static final String TAG_COST = "cost";
	
	/** The Constant TAG_ING. */
	private static final String TAG_ING = "ing";
	
	/** The Constant TAG_INGLABELING. */
	private static final String TAG_INGLABELING = "ingLabeling";
	
	/** The Constant TAG_NUT. */
	private static final String TAG_NUT = "nut";
	
	/** The Constant TAG_ORGANO. */
	private static final String TAG_ORGANO = "organo";
	
	/** The Constant TAG_MICROBIO. */
	private static final String TAG_MICROBIO = "microbio";
	
	/** The Constant TAG_PHYSICOCHEM. */
	private static final String TAG_PHYSICOCHEM = "physicoChem";
	
	/** The Constant SUFFIX_LOCALE_FRENCH. */
	private static final String SUFFIX_LOCALE_FRENCH = "_fr";
	
	/** The Constant SUFFIX_LOCALE_ENGLISH. */
	private static final String SUFFIX_LOCALE_ENGLISH = "_en";
	
	/** The Constant VALUE_NULL. */
	private static final String VALUE_NULL = "";	
	
	/** The Constant QUERY_PRODUCTLIST_ITEMS_OUT_OF_DATE. */
	private static final String QUERY_PRODUCTLIST_ITEMS_OUT_OF_DATE = "(%s) AND +@cm\\:modified:[%s TO MAX]";
	
	/** The Constant QUERY_OPERATOR_OR. */
	private static final String QUERY_OPERATOR_OR = " OR ";
	
	/** The Constant QUERY_PARENT. */
	private static final String QUERY_PARENT = " PARENT:\"%s\"";
	
	private static final String KEY_XML_INPUTSTREAM = "org.eclipse.datatools.enablement.oda.xml.inputStream";
	
	private static final String PARAM_VALUE_HIDE_CHAPTER_SUFFIX = "HideChapter";
		
	private static final String QUERY_XPATH_FORM_SETS = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/appearance/set";
	private static final String QUERY_XPATH_FORM_FIELDS_BY_SET = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/appearance/field[@set=\"%s\"]";
	private static final String QUERY_XPATH_FORM_FIELDS = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/field-visibility/show";
	private static final String QUERY_ATTR_GET_ID = "@id";
	private static final String QUERY_ATTR_GET_LABEL = "@label";
	private static final String SET_DEFAULT = "";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductReportServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The content service. */
	private ContentService contentService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The product dao. */
	private ProductDAO productDAO;	
	
	/** The search service. */
	private SearchService searchService;
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;
	
	/** The report engine. */
	private IReportEngine reportEngine;
	
	/** The mimetype service. */
	private MimetypeService mimetypeService;
	
	private ServiceRegistry serviceRegistry;
	
	private PropertyService propertyService;
			
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the content service.
	 *
	 * @param contentService the new content service
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	/**
	 * Sets the file folder service.
	 *
	 * @param fileFolderService the new file folder service
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}
	
	/**
	 * Sets the product dao.
	 *
	 * @param productDAO the new product dao
	 */
	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}
	
	/**
	 * Sets the search service.
	 *
	 * @param searchService the new search service
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}	
	
	/**
	 * Sets the dictionary service.
	 *
	 * @param dictionaryService the new dictionary service
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	/**
	 * Sets the report engine.
	 *
	 * @param reportEngine the new report engine
	 */
	public void setReportEngine(IReportEngine reportEngine){
		this.reportEngine = reportEngine;
	}
	
	/**
	 * Sets the mimetype service.
	 *
	 * @param mimetypeService the new mimetype service
	 */
	public void setMimetypeService(MimetypeService mimetypeService){
		this.mimetypeService = mimetypeService;
	}		
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	public void setPropertyService(PropertyService propertyService) {
		this.propertyService = propertyService;
	}

	
	
	/**
	 * Load node attributes.
	 *
	 * @param nodeRef the node ref
	 * @param elt the elt
	 * @return the element
	 */
	@Override
	public Map<ClassAttributeDefinition, String> loadNodeAttributes(NodeRef nodeRef) {

		PropertyFormats propertyFormats = new PropertyFormats(false);
		Map<ClassAttributeDefinition, String> values = new HashMap<ClassAttributeDefinition, String>();		
		
		// properties
		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		for (Map.Entry<QName, Serializable> property : properties.entrySet()) {

			// do not display system properties
			if(!(property.getKey().equals(ContentModel.PROP_NODE_REF) || 
			property.getKey().equals(ContentModel.PROP_NODE_DBID) ||
			property.getKey().equals(ContentModel.PROP_NODE_UUID) ||
			property.getKey().equals(ContentModel.PROP_STORE_IDENTIFIER) ||
			property.getKey().equals(ContentModel.PROP_STORE_NAME) ||
			property.getKey().equals(ContentModel.PROP_STORE_PROTOCOL) ||
			property.getKey().equals(ContentModel.PROP_CONTENT) ||
			property.getKey().equals(ContentModel.PROP_VERSION_LABEL))){
			
				PropertyDefinition propertyDef =  dictionaryService.getProperty(property.getKey());
				if(propertyDef == null){
					logger.error("This property doesn't exist. Name: " + property.getKey());
					continue;
				}
				
				String value = VALUE_NULL;				
				if (property.getValue() != null) {
					
					value = propertyService.getStringValue(propertyDef, property.getValue(), propertyFormats);
				}			
				
				values.put(propertyDef, value);
			}			
		}		
		
		
		// associations
		Map<QName, String> tempValues = new HashMap<QName, String>();
		List<AssociationRef> associations = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);

		for (AssociationRef assocRef : associations) {

			QName qName = assocRef.getTypeQName();
			NodeRef targetNodeRef = assocRef.getTargetRef();
			String name = (String) nodeService.getProperty(targetNodeRef, ContentModel.PROP_NAME);

			if (tempValues.containsKey(qName)) {
				String names = tempValues.get(qName);
				names += RepoConsts.LABEL_SEPARATOR;
				names += name;
			} else {
				tempValues.put(qName, name);
			}
		}		
		
		for(Map.Entry<QName, String> tempValue : tempValues.entrySet()){
			AssociationDefinition associationDef =  dictionaryService.getAssociation(tempValue.getKey());
			values.put(associationDef, tempValue.getValue());
		}
		
		return values;
	}	

	/**
	 * Load the image associated to the node.
	 *
	 * @param nodeRef the node ref
	 * @return the image
	 */
	@Override
	public byte[] getImage(NodeRef nodeRef) {

		byte[] imageBytes = null;
		
		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		
		if(reader != null){
			InputStream is = reader.getContentInputStream();

			try {
				Image image = ImageIO.read(is);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write((RenderedImage) image, "jpg", out);
				imageBytes = out.toByteArray();
			} catch (IOException e) {
				logger.error("Failed to get the content", e);
			}
		}		
		
		return imageBytes;
	}
	
	/**
	 * Load an image in the folder Images.
	 *
	 * @param productNodeRef the product node ref
	 * @param imgName the img name
	 * @return the product image
	 */
	@Override
	public NodeRef getProductImage(NodeRef productNodeRef, String imgName){		
		
		NodeRef parentNodeRef = nodeService.getPrimaryParent(productNodeRef).getParentRef();
				
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));		
		if(imagesFolderNodeRef == null){
			logger.debug("Folder 'Images' doesn't exist.");
			return null;
		}
		
		NodeRef productImageNodeRef = null;		
		List<FileInfo> files = fileFolderService.listFiles(imagesFolderNodeRef);				
		for(FileInfo file : files){
			if(file.getName().toLowerCase().startsWith(imgName.toLowerCase())){
				productImageNodeRef = file.getNodeRef();
			}
		}
		
		if(productImageNodeRef == null){
			logger.debug("image not found. imgName: " + imgName);
			return null;
		}			
		
		return productImageNodeRef;
	}
	
	/**
	 * Check if product has changed, so the report is out of date.
	 *
	 * @param productNodeRef the product node ref
	 * @return true, if is report up to date
	 */	
	@Override
	public boolean isReportUpToDate(NodeRef productNodeRef) {
					
		Date reportModified = (Date)nodeService.getProperty(productNodeRef, BeCPGModel.PROP_PRODUCT_REPORT_MODIFIED);
		
		// report not generated
		if(reportModified == null){
			logger.debug("report not generated");
			return false;
		}
		
		// check product modified date (modified is always bigger than reportModified so a delta is defined)
		Date modified = (Date)nodeService.getProperty(productNodeRef, ContentModel.PROP_MODIFIED);		
		logger.debug("modified: " + ISO8601DateFormat.format(modified) + " - reportModified: " + ISO8601DateFormat.format(reportModified));
		if(modified.after(reportModified)){			
			logger.debug("product has been modified");
			return false;
		}
		
		// check product lists
		NodeRef listContainerNodeRef = productDAO.getListContainer(productNodeRef);
		
		if(listContainerNodeRef != null){
			
			String queryParentsSearch = "";
			
			for(FileInfo fileInfo : fileFolderService.listFolders(listContainerNodeRef)){
				
				NodeRef listNodeRef = fileInfo.getNodeRef();
				
				// check list folder modified date
				Date productListModified = (Date)nodeService.getProperty(listNodeRef, ContentModel.PROP_MODIFIED);
				logger.debug("list modified: " + ISO8601DateFormat.format(productListModified) + " - reportModified: " + ISO8601DateFormat.format(reportModified));
				if(productListModified.after(reportModified)){
					logger.debug("list folder has been modified");
					return false;
				}
				
				if(!queryParentsSearch.isEmpty()){
					queryParentsSearch += QUERY_OPERATOR_OR;
				}
				queryParentsSearch += String.format(QUERY_PARENT, listNodeRef);						
			}		
			
			// check list children modified date
			if(!queryParentsSearch.isEmpty()){
				
				String querySearch = String.format(QUERY_PRODUCTLIST_ITEMS_OUT_OF_DATE, queryParentsSearch, ISO8601DateFormat.format(reportModified));
				
				SearchParameters sp = new SearchParameters();
		        sp.addStore(RepoConsts.SPACES_STORE);
		        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		        sp.setQuery(querySearch);	        
		        sp.setLimitBy(LimitBy.FINAL_SIZE);
		        sp.setLimit(RepoConsts.MAX_RESULTS_SINGLE_VALUE);        
		        sp.setMaxItems(RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		        
		        ResultSet resultSet = null;
		        
		        try{
		        	
		        	logger.debug("queryPath: " + querySearch);	        		
			        resultSet = searchService.query(sp);		        
			        logger.debug("resultSet.length() : " + resultSet.length());
			        
			        if (resultSet.length() > 0){
			        			        	
			        	logger.debug("list children has been modified");	        	
			        	return false;
			        }		        		        
		        }
		        finally{
		        	if(resultSet != null)
		        		resultSet.close();
		        }			
			}
		}		
		
		return true;
	}	
	
	/**
	 * Get the node where the document will we stored.
	 *
	 * @param productNodeRef the product node ref
	 * @param tplNodeRef the tpl node ref
	 * @return the document content writer
	 */
	@Override
	public ContentWriter getDocumentContentWriter(NodeRef productNodeRef, NodeRef tplNodeRef){
		
		ContentWriter contentWriter = null;
		
		if((Boolean)nodeService.getProperty(tplNodeRef, BeCPGModel.PROP_PRODUCT_REPORTTEMPLATE_IS_DEFAULT)){
			contentWriter = contentService.getWriter(productNodeRef, ContentModel.PROP_CONTENT, true);
		}
		else{
			// the doc will be stored in the documents folder of the product			
			NodeRef parentNodeRef = nodeService.getPrimaryParent(productNodeRef).getParentRef();
			
			if(nodeService.getType(parentNodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)){
			
				String documentsFolderName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS);
				NodeRef documentsFolderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, documentsFolderName);		
				if(documentsFolderNodeRef == null){
					
					documentsFolderNodeRef = fileFolderService.create(parentNodeRef, documentsFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
				}
				
				String documentName = (String)nodeService.getProperty(tplNodeRef, ContentModel.PROP_NAME);
				NodeRef documentNodeRef = nodeService.getChildByName(documentsFolderNodeRef, ContentModel.ASSOC_CONTAINS, documentName);
				if(documentNodeRef == null){
					
					documentNodeRef = fileFolderService.create(documentsFolderNodeRef, documentName, ContentModel.TYPE_CONTENT).getNodeRef();
				}
				
				contentWriter = contentService.getWriter(documentNodeRef, ContentModel.PROP_CONTENT, true);
			}
			else{
				logger.debug("The product does not have a productFolder so we cannot store doc in the folder 'Documents'.");
			}			
		}
		
		return contentWriter;
	}
	
	/**
	 * load the datalists of the product data.
	 *
	 * @param productData the product data
	 * @param dataListsElt the data lists elt
	 * @return the element
	 */
	@Override
	public Element loadDataLists(ProductData productData, Element dataListsElt) {
		
		//allergen    	
    	if(productData.getAllergenList() != null){
    		Element allergenListElt = dataListsElt.addElement(TAG_ALLERGENLIST);	    	
	    	
	    	for(AllergenListDataItem dataItem :productData.getAllergenList()){
	    		
	    		String voluntarySources = VALUE_NULL;
	    		for(NodeRef nodeRef : dataItem.getVoluntarySources()){
	    			if(voluntarySources.isEmpty())
	    				voluntarySources = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	    			else
	    				voluntarySources += RepoConsts.LABEL_SEPARATOR + (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	    		}
	    		
	    		String inVoluntarySources = VALUE_NULL;
	    		for(NodeRef nodeRef : dataItem.getInVoluntarySources()){
	    			if(inVoluntarySources.isEmpty())
	    				inVoluntarySources = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	    			else
	    				inVoluntarySources += RepoConsts.LABEL_SEPARATOR + (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	    		}
	    		
	    		String allergen = (String)nodeService.getProperty(dataItem.getAllergen(), ContentModel.PROP_NAME);
	    		String allergenType = (String)nodeService.getProperty(dataItem.getAllergen(), BeCPGModel.PROP_ALLERGEN_TYPE);
	    		
	    		Element AllergenElt = allergenListElt.addElement(TAG_ALLERGEN);
	    		AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_ALLERGEN.getLocalName(), allergen);
	    		AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGEN_TYPE.getLocalName(), allergenType);
	    		AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY.getLocalName(), Boolean.toString(dataItem.getVoluntary()));
	    		AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY.getLocalName(), Boolean.toString(dataItem.getInVoluntary()));
	    		AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY_SOURCES.getLocalName(), voluntarySources);
	    		AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY_SOURCES.getLocalName(), inVoluntarySources);	    			    		
	    	}	    	
    	}
    	
    	//compoList
    	if(productData.getCompoList() != null){
    		Element compoListElt = dataListsElt.addElement(TAG_COMPOLIST);	    	
	    	
	    	for(CompoListDataItem dataItem :productData.getCompoList()){	    			    	
	    		
	    		String partName = (String)nodeService.getProperty(dataItem.getProduct(), ContentModel.PROP_NAME);    		
	    		
	    		Element partElt = compoListElt.addElement(TAG_PRODUCT);
	    		partElt.addAttribute(BeCPGModel.ASSOC_COMPOLIST_PRODUCT.getLocalName(), partName);
	    		partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(dataItem.getDepthLevel()));
	    		partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY.getLocalName(), dataItem.getQty() == null ? VALUE_NULL : Float.toString(dataItem.getQty()));
	    		partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA.getLocalName(), dataItem.getQtySubFormula() == null ? VALUE_NULL : Float.toString(dataItem.getQtySubFormula()));
	    		partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_LOSS_PERC.getLocalName(), dataItem.getLossPerc() == null ? VALUE_NULL : Float.toString(dataItem.getLossPerc()));
	    		partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_DECL_TYPE.getLocalName(), dataItem.getDeclType());	    		
	    	}	    	
    	}
    	
    	//CostList
    	if(productData.getCostList() != null){
    		Element costListElt = dataListsElt.addElement(TAG_COSTLIST);	    	
	    	
	    	for(CostListDataItem dataItem :productData.getCostList()){	    			    	
	    		
	    		String cost = (String)nodeService.getProperty(dataItem.getCost(), ContentModel.PROP_NAME);    		
	    		
	    		Element costElt = costListElt.addElement(TAG_COST);
	    		costElt.addAttribute(BeCPGModel.ASSOC_COSTLIST_COST.getLocalName(), cost);
	    		costElt.addAttribute(BeCPGModel.PROP_COSTLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : Float.toString(dataItem.getValue()));
	    		costElt.addAttribute(BeCPGModel.PROP_COSTLIST_UNIT.getLocalName(), dataItem.getUnit());	    		
	    	}	    	
    	}
    	
    	//IngList
    	if(productData.getIngList() != null){
    		Element ingListElt = dataListsElt.addElement(TAG_INGLIST);	    	
	    	
	    	for(IngListDataItem dataItem :productData.getIngList()){	    			    	
	    		
	    		String ing = (String)nodeService.getProperty(dataItem.getIng(), ContentModel.PROP_NAME);
	    		String ingCEECode = (String)nodeService.getProperty(dataItem.getIng(), BeCPGModel.PROP_ING_CEECODE);
	    		
	    		String geoOrigins = VALUE_NULL;
	    		for(NodeRef nodeRef : dataItem.getGeoOrigin()){
	    			if(geoOrigins.isEmpty())
	    				geoOrigins = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString();
	    			else
	    				geoOrigins += RepoConsts.LABEL_SEPARATOR + (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	    		}
	    		
	    		String bioOrigins = VALUE_NULL;
	    		for(NodeRef nodeRef : dataItem.getBioOrigin()){
	    			if(bioOrigins.isEmpty())
	    				bioOrigins = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	    			else
	    				bioOrigins += RepoConsts.LABEL_SEPARATOR + (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	    		}
	    	 
	    		Element ingElt = ingListElt.addElement(TAG_ING);
	    		ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_ING.getLocalName(), ing);
	    		ingElt.addAttribute(BeCPGModel.PROP_INGLIST_QTY_PERC.getLocalName(), dataItem.getQtyPerc() == null ? VALUE_NULL : Float.toString(dataItem.getQtyPerc()));
	    		ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_GEO_ORIGIN.getLocalName(), geoOrigins);
	    		ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_BIO_ORIGIN.getLocalName(), bioOrigins);
	    		ingElt.addAttribute(BeCPGModel.PROP_INGLIST_IS_GMO.getLocalName(), Boolean.toString(dataItem.isGMO()));
	    		ingElt.addAttribute(BeCPGModel.PROP_INGLIST_IS_IONIZED.getLocalName(), Boolean.toString(dataItem.isIonized()));
	    		ingElt.addAttribute(BeCPGModel.PROP_ING_CEECODE.getLocalName(), ingCEECode);
	    	}	    	
    	}
    	
    	//IngLabelingList
    	if(productData.getIngLabelingList() != null){
    		Element ingListElt = dataListsElt.addElement(TAG_INGLABELINGLIST);	    	
    		String frenchILLGrpName = BeCPGModel.PROP_ILL_GRP.getLocalName() + SUFFIX_LOCALE_FRENCH;
    		String englishILLGrpName = BeCPGModel.PROP_ILL_GRP.getLocalName() + SUFFIX_LOCALE_ENGLISH;
    		String frenchILLValueName = BeCPGModel.PROP_ILL_VALUE.getLocalName() + SUFFIX_LOCALE_FRENCH;
    		String englishILLValueName = BeCPGModel.PROP_ILL_VALUE.getLocalName() + SUFFIX_LOCALE_ENGLISH;
    		
	    	for(IngLabelingListDataItem dataItem :productData.getIngLabelingList()){	    			    		    			    				    		
	    		
	    		Element ingLabelingElt = ingListElt.addElement(TAG_INGLABELING);
	    		ingLabelingElt.addAttribute(frenchILLGrpName, dataItem.getGrp());
	    		ingLabelingElt.addAttribute(englishILLGrpName, dataItem.getGrp());
	    		
	    		ingLabelingElt.addAttribute(frenchILLValueName, dataItem.getValue().getValue(Locale.FRENCH));
	    		ingLabelingElt.addAttribute(englishILLValueName, dataItem.getValue().getValue(Locale.ENGLISH));
	    	}	    	
    	}
    	
    	//NutList
    	if(productData.getNutList() != null){
    		
    		Element nutListElt = dataListsElt.addElement(TAG_NUTLIST);
	    	
	    	for(NutListDataItem dataItem :productData.getNutList()){	    		
	    		
	    		String nut = nodeService.getProperty(dataItem.getNut(), ContentModel.PROP_NAME).toString();
	    		
	    		Element nutElt = nutListElt.addElement(TAG_NUT);
	    		nutElt.addAttribute(BeCPGModel.ASSOC_NUTLIST_NUT.getLocalName(), nut);
	    		nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL :String.valueOf(dataItem.getValue()));
	    		nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_UNIT.getLocalName(), dataItem.getUnit());
	    		nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_GROUP .getLocalName(), dataItem.getGroup());	    		
	    	}	    		    	
    	}
    	
    	//OrganoList
		if(productData.getOrganoList() != null){
    		
    		Element organoListElt = dataListsElt.addElement(TAG_ORGANOLIST);
	    	
	    	for(OrganoListDataItem dataItem :productData.getOrganoList()){	    		
	    		
	    		String organo = nodeService.getProperty(dataItem.getOrgano(), ContentModel.PROP_NAME).toString();
	    		
	    		Element organoElt = organoListElt.addElement(TAG_ORGANO);
	    		organoElt.addAttribute(BeCPGModel.ASSOC_ORGANOLIST_ORGANO.getLocalName(), organo);
	    		organoElt.addAttribute(BeCPGModel.PROP_ORGANOLIST_VALUE.getLocalName(), dataItem.getValue());	    		
	    	}	    		    	
    	}
		
		//MicrobioList
		if(productData.getMicrobioList() != null){
    		
    		Element organoListElt = dataListsElt.addElement(TAG_MICROBIOLIST);
	    	
	    	for(MicrobioListDataItem dataItem :productData.getMicrobioList()){	    		
	    		
	    		String microbio = nodeService.getProperty(dataItem.getMicrobio(), ContentModel.PROP_NAME).toString();
	    		
	    		Element microbioElt = organoListElt.addElement(TAG_MICROBIO);
	    		microbioElt.addAttribute(BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO.getLocalName(), microbio);
	    		microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : Float.toString(dataItem.getValue()));
	    		microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_UNIT.getLocalName(), dataItem.getUnit());
	    		microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_MAXI.getLocalName(), dataItem.getMaxi() == null ? VALUE_NULL : Float.toString(dataItem.getMaxi()));	    		
	    	}	    		    	
    	}
		
		//PhysicoChemList
		if(productData.getPhysicoChemList() != null){
    		
    		Element physicoChemListElt = dataListsElt.addElement(TAG_PHYSICOCHEMLIST);
	    	
	    	for(PhysicoChemListDataItem dataItem :productData.getPhysicoChemList()){	    		
	    		
	    		String physicoChem = nodeService.getProperty(dataItem.getPhysicoChem(), ContentModel.PROP_NAME).toString();
	    			    		
	    		Element physicoChemElt = physicoChemListElt.addElement(TAG_PHYSICOCHEM);
	    		physicoChemElt.addAttribute(BeCPGModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM.getLocalName(), physicoChem);
	    		physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : Float.toString(dataItem.getValue()));
	    		physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_UNIT.getLocalName(), dataItem.getUnit());	    		
	    		physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_MINI.getLocalName(), dataItem.getMini() == null ? VALUE_NULL : Float.toString(dataItem.getMini()));
	    		physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_MAXI.getLocalName(), dataItem.getMaxi() == null ? VALUE_NULL : Float.toString(dataItem.getMaxi()));	    		
	    	}	    		    	
    	}
		
		return dataListsElt;
	}
	
	/**
	 * Method that generates reports.
	 *
	 * @param productNodeRef the product node ref
	 * @param tplsNodeRef the tpls node ref
	 * @param productElt the product elt
	 * @param images the images
	 */
	@Override
	public void generateReports(NodeRef productNodeRef, List<NodeRef> tplsNodeRef, Element productElt, Map<String, byte[]> images) {
		
		if(productNodeRef == null){
			throw new IllegalArgumentException("productNodeRef is null");
		}
		
		if(tplsNodeRef.isEmpty()){
			throw new IllegalArgumentException("tplsNodeRef is empty");
		}
		
		if(productElt == null){
			throw new IllegalArgumentException("productElt is null");
		}		
		
		// calculate the visible datalists
		NodeRef listContainerNodeRef = productDAO.getListContainer(productNodeRef);
		Set<QName> existingLists = productDAO.getExistingListsQName(listContainerNodeRef);		
		
		// generate reports
		for(NodeRef tplNodeRef : tplsNodeRef){        			
			
			NodeRef reportTplNodeRef = nodeService.getChildByName(tplNodeRef, ContentModel.ASSOC_CONTAINS, "ProductReport.rptdesign");
			
			if(reportTplNodeRef == null){
			
				logger.error("Failed to find the '*.rptdesign' file. ProductReportFolder: " + tplNodeRef);
			}
			else{
							        					        						
				//prepare
				try{							
				    
					logger.debug("reportTplNodeRef " + reportTplNodeRef);
					ContentReader reader = contentService.getReader(reportTplNodeRef, ContentModel.PROP_CONTENT);
	    			InputStream inputStream = reader.getContentInputStream();
					IReportRunnable design = reportEngine.openReportDesign(inputStream);							
					
					//Run report		
					logger.debug("Run report");
					ContentWriter writer = getDocumentContentWriter(productNodeRef, tplNodeRef);
					
					if(writer != null){
					
						String mimetype = mimetypeService.guessMimetype(RepoConsts.REPORT_EXTENSION_PDF);
						logger.debug("add file, mimetype: " + mimetype);
						writer.setMimetype(mimetype);
						
						//Create task to run and render the report,
						logger.debug("Create task to run and render the report");
						IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design);
						
						IRenderOption options = new RenderOption();
						OutputStream outputStream = writer.getContentOutputStream();
						options.setOutputStream(outputStream);							
						options.setOutputFormat(IRenderOption.OUTPUT_FORMAT_PDF);
						task.setRenderOption(options);
						
						// xml data
						logger.trace("add Xml data: " + productElt.asXML());
						ByteArrayInputStream bais = new ByteArrayInputStream( productElt.asXML().getBytes());
						task.getAppContext().put(KEY_XML_INPUTSTREAM, bais);
						
						// images
						logger.debug("add images");
						if(images != null){
						
							for(Map.Entry<String, byte[]> entry : images.entrySet()){
								task.getAppContext().put(entry.getKey(), entry.getValue());
							}
						}					
						
						IGetParameterDefinitionTask paramTask = reportEngine.createGetParameterDefinitionTask(design);											
						
						// hide all datalists and display visible ones
						for(Object key : paramTask.getDefaultValues().keySet()){
							
							if(((String)key).endsWith(PARAM_VALUE_HIDE_CHAPTER_SUFFIX)){
								task.setParameterValue((String)key, Boolean.TRUE);
							}
						}							
						
						for(QName existingList : existingLists){
							task.setParameterValue(existingList.getLocalName() + PARAM_VALUE_HIDE_CHAPTER_SUFFIX, Boolean.FALSE);
						}
						
						logger.debug("run task");
						task.run();
						task.close();
						outputStream.close();
							
						logger.debug("Report generated.");
					}    							
				}
				catch(Exception e){
					logger.error("Failed to execute report: ",  e);
				}						
        	}	
		}        			
	}
	
	
	@Override
	public Map<String, List<String>> getFieldsBySets(NodeRef productNodeRef, String reportFormConfigPath){
				
		NamespaceService namespaceService = serviceRegistry.getNamespaceService();
		Map<String, List<String>> fieldsBySets = new LinkedHashMap<String, List<String>>();
		Document doc = null;
		try{
			ClassPathResource classPathResource = new ClassPathResource(reportFormConfigPath);
			
			SAXReader reader = new SAXReader();
			doc = reader.read(classPathResource.getInputStream());
		}
		catch(Exception e){
			logger.error("Failed to load file " + reportFormConfigPath, e);
			return fieldsBySets;
		}				
		
		// fields to show
		List<String> fields = new ArrayList<String>();
		QName productType = nodeService.getType(productNodeRef);		
		String productTypeWithPrefix = productType.toPrefixString(namespaceService);
		List<Element> fieldElts = doc.selectNodes(String.format(QUERY_XPATH_FORM_FIELDS, productTypeWithPrefix));		
		for(Element fieldElt : fieldElts){
			fields.add(fieldElt.valueOf(QUERY_ATTR_GET_ID));
		}				
		
		// sets to show
		List<Element> setElts = doc.selectNodes(String.format(QUERY_XPATH_FORM_SETS, productTypeWithPrefix));		
		for(Element setElt : setElts){
						
			String setId = setElt.valueOf(QUERY_ATTR_GET_ID);
			String setLabel = setElt.valueOf(QUERY_ATTR_GET_LABEL);
			
			List<String> fieldsForSet = new ArrayList<String>(); 
			List<Element> fieldsForSetElts = doc.selectNodes(String.format(QUERY_XPATH_FORM_FIELDS_BY_SET, productTypeWithPrefix, setId));			
			for(Element fieldElt : fieldsForSetElts){
				
				String fieldId = fieldElt.valueOf(QUERY_ATTR_GET_ID);						
				fieldsForSet.add(fieldId);
				fields.remove(fieldId);
			}

			fieldsBySets.put(setLabel, fieldsForSet);
		}
		
		// fields not associated to set
		fieldsBySets.put(SET_DEFAULT, fields);	
		
		return fieldsBySets;
	}		
}


