/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.report;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.birt.report.model.parser.ReportState;

import fr.becpg.common.RepoConsts;
import fr.becpg.repo.NodeVisitor;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.report.entity.EntityExtractor;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;

// TODO: Auto-generated Javadoc
/**
 * Class that generates product reports.
 *
 * @author querephi
 */
public class ProductReportVisitor implements NodeVisitor {
	
	/** The Constant TAG_PRODUCT. */
	private static final String TAG_PRODUCT = "product";
	
	/** The Constant TAG_DATALISTS. */
	private static final String TAG_DATALISTS = "dataLists";
	private static final String TAG_ATTRIBUTES = "attributes";
	private static final String TAG_ATTRIBUTE = "attribute";
	private static final String ATTR_SET = "set";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_VALUE = "value";
	
	/** The Constant KEY_PRODUCT_IMAGE. */
	private static final String KEY_PRODUCT_IMAGE = "productImage";
	
	private static final String REPORT_FORM_CONFIG_PATH = "beCPG/birt/becpg-report-form-config.xml";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductReportVisitor.class);
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	/** The product dao. */
	private ProductDAO productDAO;	
	
	/** The product report service. */
	private ProductReportService productReportService;
	
	private ReportTplService reportTplService;	
	
	private EntityReportService entityReportService;
	
	private EntityService entityService;
			
	/**
	 * Sets the product dictionary service.
	 *
	 * @param productDictionaryService the new product dictionary service
	 */
	public void setProductDictionaryService(ProductDictionaryService productDictionaryService){
		this.productDictionaryService = productDictionaryService;
	}
	
	/**
	 * Sets the product dao.
	 *
	 * @param productDAO the new product dao
	 */
	public void setProductDAO(ProductDAO productDAO){
		this.productDAO = productDAO;
	}	
	
	
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/**
	 * Sets the product report service.
	 *
	 * @param productReportService the new product report service
	 */
	public void setProductReportService(ProductReportService productReportService) {
		this.productReportService = productReportService;
	}		
	
	public void setReportTplService(
			ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}
	
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.NodeVisitor#visitNode(org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public void visitNode(NodeRef productNodeRef) {	
	
		logger.debug("Start visitProduct");		
		
		List<NodeRef> tplsNodeRef = entityReportService.getReportTplsToGenerate(productNodeRef);					
		tplsNodeRef = reportTplService.cleanDefaultTpls(tplsNodeRef);		
		
		if(!tplsNodeRef.isEmpty()){
			
			logger.debug("templateNodeRef " + tplsNodeRef);						
			
			// extract data
			EntityExtractor productExtractor = new ProductExtractorImpl(productNodeRef);
			productExtractor.extract();
			logger.trace("product loaded, xml data: " + productExtractor.getXmlData().getDocument().asXML());
			
			entityReportService.generateReports(productNodeRef, tplsNodeRef, productExtractor.getXmlData(), productExtractor.getImages());						
    	}			
	}	
	
	/**
	 * The Class ProductExtractorImpl.
	 *
	 * @author querephi
	 */
	private class ProductExtractorImpl implements EntityExtractor {
		
		/** The product node ref. */
		private NodeRef productNodeRef;
		
		/** The product elt. */
		private Element productElt;
		
		/** The images. */
		private Map<String, byte[]> images;

		/**
		 * Instantiates a new product extractor impl.
		 *
		 * @param productNodeRef the product node ref
		 */
		private ProductExtractorImpl(NodeRef productNodeRef) {
			
			this.productNodeRef = productNodeRef;
			
			Document document = DocumentHelper.createDocument();
			productElt = document.addElement(TAG_PRODUCT);
			
			images = new HashMap<String, byte[]>();
		}

		/* (non-Javadoc)
		 * @see fr.becpg.repo.product.report.ProductExtractor#getXmlData()
		 */
		@Override
		public Element getXmlData() {
			return productElt;
		}

		/* (non-Javadoc)
		 * @see fr.becpg.repo.product.report.ProductExtractor#getImages()
		 */
		@Override
		public Map<String, byte[]> getImages() {
			return images;
		}

		/* (non-Javadoc)
		 * @see fr.becpg.repo.product.report.ProductExtractor#extract()
		 */
		@Override
		public void extract() {
			
			loadXmlData();
			loadImages();										
		}
		
		/**
		 * Load xml data.
		 */
		private void loadXmlData(){			
			
			Element attributesElt = productElt.addElement(TAG_ATTRIBUTES);	
			
			// add attributes at <product/> tag
			Map<ClassAttributeDefinition, String> attributes = entityReportService.loadNodeAttributes(productNodeRef);
			
			for (Map.Entry<ClassAttributeDefinition, String> attrKV : attributes.entrySet()){
				
				productElt.addAttribute(attrKV.getKey().getName().getLocalName(), attrKV.getValue());																	
			}
			
			// add attributes at <product><attributes/></product> and group them by set
			Map<String, List<String>> fieldsBySets = entityReportService.getFieldsBySets(productNodeRef, REPORT_FORM_CONFIG_PATH);
			
			// set
			for(Map.Entry<String, List<String>> kv : fieldsBySets.entrySet()){											
				
				// field
				for(String fieldId : kv.getValue()){
					
					// look for value
					Map.Entry<ClassAttributeDefinition, String> attrKV = null;
					for (Map.Entry<ClassAttributeDefinition, String> a : attributes.entrySet()){
						
						if(a.getKey().getName().getPrefixString().equals(fieldId)){
							attrKV = a;
							break;
						}
					}
					
					if(attrKV != null){
						
						Element attributeElt = attributesElt.addElement(TAG_ATTRIBUTE);
						attributeElt.addAttribute(ATTR_SET, kv.getKey());
						attributeElt.addAttribute(ATTR_NAME, attrKV.getKey().getTitle());
						attributeElt.addAttribute(ATTR_VALUE, attrKV.getValue());
					}					
				}
			}
			
			//render data lists
	    	Element dataListsElt = productElt.addElement(TAG_DATALISTS);
	    	ProductData productData = productDAO.find(productNodeRef, productDictionaryService.getDataLists());
	    	dataListsElt = productReportService.loadDataLists(productData, dataListsElt);
		}
		
		/**
		 * Load images.
		 */
		private void loadImages(){
			
			/*
			 *	get the product image 
			 */
			String productImageFileName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_IMAGE).toLowerCase();
			NodeRef imgNodeRef = entityService.getImage(productNodeRef, productImageFileName);
			byte[] imageBytes = null;
			
			if(imgNodeRef != null){
				imageBytes = entityReportService.getImage(imgNodeRef);
				images.put(KEY_PRODUCT_IMAGE, imageBytes);
			}								
		}				
	}

}
