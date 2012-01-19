package fr.becpg.repo.report.entity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.PropertyService;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
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
import fr.becpg.repo.report.entity.EntityReportExtractor;

public class EntityReportExtractorImpl implements EntityReportExtractor {
	
	
	private static Log logger = LogFactory.getLog(EntityReportExtractorImpl.class);
	
	/** The Constant TAG_PRODUCT. */
	private static final String TAG_PRODUCT = "product";
	
	/** The Constant TAG_DATALISTS. */
	private static final String TAG_DATALISTS = "dataLists";
	private static final String TAG_ATTRIBUTES = "attributes";
	private static final String TAG_ATTRIBUTE = "attribute";
	private static final String ATTR_SET = "set";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_VALUE = "value";
	
	
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
	private static final String VALUE_PERSON = "%s %s";
	
	private static final String QUERY_XPATH_FORM_SETS = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/appearance/set";
	private static final String QUERY_XPATH_FORM_FIELDS_BY_SET = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/appearance/field[@set=\"%s\"]";
	private static final String QUERY_XPATH_FORM_FIELDS = "/alfresco-config/config[@evaluator=\"node-type\" and @condition=\"%s\"]/forms/form/field-visibility/show";
	private static final String QUERY_ATTR_GET_ID = "@id";
	private static final String QUERY_ATTR_GET_LABEL = "@label";
	private static final String SET_DEFAULT = "";
	
	private static final String REPORT_FORM_CONFIG_PATH = "beCPG/birt/document/becpg-report-form-config.xml";
	
	private NodeService nodeService;

	
	private DictionaryService dictionaryService;
	
	private NamespaceService namespaceService;
	
	private PropertyService propertyService;

	private ProductDAO productDAO;
	
	private ProductDictionaryService productDictionaryService;
	
	
	

	/**
	 * @param productDAO the productDAO to set
	 */
	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}



	/**
	 * @param productDictionaryService the productDictionaryService to set
	 */
	public void setProductDictionaryService(ProductDictionaryService productDictionaryService) {
		this.productDictionaryService = productDictionaryService;
	}


	/**
	 * @param nodeService the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}



	/**
	 * @param dictionaryService the dictionaryService to set
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}





	/**
	 * @param namespaceService the namespaceService to set
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}





	/**
	 * @param propertyService the propertyService to set
	 */
	public void setPropertyService(PropertyService propertyService) {
		this.propertyService = propertyService;
	}









	@Override
	public Element extractXml(NodeRef entityNodeRef) {
		Document document = DocumentHelper.createDocument();
		Element entityElt = document.addElement(TAG_PRODUCT);

		
		Element attributesElt = entityElt.addElement(TAG_ATTRIBUTES);	
		
		// add attributes at <product/> tag
		Map<ClassAttributeDefinition, String> attributes = loadNodeAttributes(entityNodeRef);
		
		for (Map.Entry<ClassAttributeDefinition, String> attrKV : attributes.entrySet()){
			
			entityElt.addAttribute(attrKV.getKey().getName().getLocalName(), attrKV.getValue());																	
		}
		
		// add attributes at <product><attributes/></product> and group them by set
		Map<String, List<String>> fieldsBySets = getFieldsBySets(entityNodeRef, REPORT_FORM_CONFIG_PATH);
		
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
    	Element dataListsElt = entityElt.addElement(TAG_DATALISTS);
    	dataListsElt = loadDataLists(entityNodeRef, dataListsElt);
    	
    	return entityElt;
	}
	
	
	/**
	 * load the datalists of the product data.
	 *
	 * @param productData the product data
	 * @param dataListsElt the data lists elt
	 * @return the element
	 */
	public Element loadDataLists(NodeRef entityNodeRef, Element dataListsElt) {
		
		//TODO make it more generic!!!!
		ProductData productData = productDAO.find(entityNodeRef, productDictionaryService.getDataLists());
		
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
	    		AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES.getLocalName(), voluntarySources);
	    		AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES.getLocalName(), inVoluntarySources);	    			    		
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
	    		nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_GROUP.getLocalName(), dataItem.getGroup());	    		
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
	    		microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_TEXT_CRITERIA.getLocalName(), dataItem.getTextCriteria());
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
	 * Load node attributes.
	 *
	 * @param nodeRef the node ref
	 * @param elt the elt
	 * @return the element
	 */
	private Map<ClassAttributeDefinition, String> loadNodeAttributes(NodeRef nodeRef) {

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
			QName targetQName = nodeService.getType(targetNodeRef);
			String name = "";			
			
			if(targetQName.equals(ContentModel.TYPE_PERSON)){
				name = String.format(VALUE_PERSON, (String)nodeService.getProperty(targetNodeRef, ContentModel.PROP_FIRSTNAME),
								(String) nodeService.getProperty(targetNodeRef, ContentModel.PROP_LASTNAME));
			}
			else{
				name = (String) nodeService.getProperty(targetNodeRef, ContentModel.PROP_NAME);
			}						
			
			logger.debug("###targetQName: " + targetQName + ", name: " + name);

			if (tempValues.containsKey(qName)) {
				String names = tempValues.get(qName);
				names += RepoConsts.LABEL_SEPARATOR;
				names += name;
				tempValues.put(qName, names);
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

	
	


	@SuppressWarnings("unchecked")
	public Map<String, List<String>> getFieldsBySets(NodeRef nodeRef, String reportFormConfigPath){
				
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
		QName nodeType = nodeService.getType(nodeRef);		
		String nodeTypeWithPrefix = nodeType.toPrefixString(namespaceService);
		
		List<Element> fieldElts = doc.selectNodes(String.format(QUERY_XPATH_FORM_FIELDS, nodeTypeWithPrefix));		
		for(Element fieldElt : fieldElts){
			fields.add(fieldElt.valueOf(QUERY_ATTR_GET_ID));
		}				
		
		// sets to show
		List<Element> setElts = doc.selectNodes(String.format(QUERY_XPATH_FORM_SETS, nodeTypeWithPrefix));		
		for(Element setElt : setElts){
						
			String setId = setElt.valueOf(QUERY_ATTR_GET_ID);
			String setLabel = setElt.valueOf(QUERY_ATTR_GET_LABEL);
			
			List<String> fieldsForSet = new ArrayList<String>(); 
			List<Element> fieldsForSetElts = doc.selectNodes(String.format(QUERY_XPATH_FORM_FIELDS_BY_SET, nodeTypeWithPrefix, setId));			
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
