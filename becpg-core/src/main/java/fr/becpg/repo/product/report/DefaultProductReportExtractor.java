package fr.becpg.repo.product.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import fr.becpg.common.BeCPGException;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
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
import fr.becpg.repo.report.entity.impl.AbstractEntityReportExtractor;

public class DefaultProductReportExtractor extends AbstractEntityReportExtractor {

	/** The Constant KEY_PRODUCT_IMAGE. */
	protected static final String KEY_PRODUCT_IMAGE = "productImage";

	private static Log logger = LogFactory.getLog(DefaultProductReportExtractor.class);

	/** The Constant TAG_ALLERGENLIST. */
	protected static final String TAG_ALLERGENLIST = "allergenList";

	/** The Constant TAG_COMPOLIST. */
	protected static final String TAG_COMPOLIST = "compoList";

	/** The Constant TAG_COSTLIST. */
	protected static final String TAG_COSTLIST = "costList";

	/** The Constant TAG_INGLIST. */
	protected static final String TAG_INGLIST = "ingList";

	/** The Constant TAG_INGLABELINGLIST. */
	protected static final String TAG_INGLABELINGLIST = "ingLabelingList";

	/** The Constant TAG_NUTLIST. */
	protected static final String TAG_NUTLIST = "nutList";

	/** The Constant TAG_ORGANOLIST. */
	protected static final String TAG_ORGANOLIST = "organoList";

	/** The Constant TAG_MICROBIOLIST. */
	protected static final String TAG_MICROBIOLIST = "microbioList";

	/** The Constant TAG_PHYSICOCHEMLIST. */
	protected static final String TAG_PHYSICOCHEMLIST = "physicoChemList";

	/** The Constant TAG_ALLERGEN. */
	protected static final String TAG_ALLERGEN = "allergen";

	/** The Constant TAG_COST. */
	protected static final String TAG_COST = "cost";

	/** The Constant TAG_ING. */
	protected static final String TAG_ING = "ing";

	/** The Constant TAG_INGLABELING. */
	protected static final String TAG_INGLABELING = "ingLabeling";

	/** The Constant TAG_NUT. */
	protected static final String TAG_NUT = "nut";

	/** The Constant TAG_ORGANO. */
	protected static final String TAG_ORGANO = "organo";

	/** The Constant TAG_MICROBIO. */
	protected static final String TAG_MICROBIO = "microbio";

	/** The Constant TAG_PHYSICOCHEM. */
	protected static final String TAG_PHYSICOCHEM = "physicoChem";

	protected static final String ATTR_LANGUAGE = "language";

	protected ProductDAO productDAO;

	protected ProductDictionaryService productDictionaryService;
	
	private NodeService mlNodeService;

	/**
	 * @param productDAO
	 *            the productDAO to set
	 */
	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}

	/**
	 * @param productDictionaryService
	 *            the productDictionaryService to set
	 */
	public void setProductDictionaryService(ProductDictionaryService productDictionaryService) {
		this.productDictionaryService = productDictionaryService;
	}

	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	@Override
	protected Map<String, byte[]> extractImages(NodeRef entityNodeRef) {
		Map<String, byte[]> images = new HashMap<String, byte[]>();
		/*
		 * get the product image
		 */
		try {
			NodeRef imgNodeRef = entityService.getEntityDefaultImage(entityNodeRef);

			byte[] imageBytes = null;

			if (imgNodeRef != null) {
				imageBytes = entityService.getImage(imgNodeRef);
				images.put(KEY_PRODUCT_IMAGE, imageBytes);
			}
		} catch (BeCPGException e) {
			//DO Nothing here
		}
		return images;
	}

	/**
	 * load the datalists of the product data.
	 * 
	 * @param productData
	 *            the product data
	 * @param dataListsElt
	 *            the data lists elt
	 * @return the element
	 */
	@Override
	protected Element loadDataLists(NodeRef entityNodeRef, Element dataListsElt) {

		// TODO make it more generic!!!!
		ProductData productData = productDAO.find(entityNodeRef, productDictionaryService.getDataLists());

		// allergen
		if (productData.getAllergenList() != null) {
			Element allergenListElt = dataListsElt.addElement(TAG_ALLERGENLIST);

			for (AllergenListDataItem dataItem : productData.getAllergenList()) {

				String voluntarySources = VALUE_NULL;
				for (NodeRef nodeRef : dataItem.getVoluntarySources()) {
					if (voluntarySources.isEmpty())
						voluntarySources = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
					else
						voluntarySources += RepoConsts.LABEL_SEPARATOR + (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}

				String inVoluntarySources = VALUE_NULL;
				for (NodeRef nodeRef : dataItem.getInVoluntarySources()) {
					if (inVoluntarySources.isEmpty())
						inVoluntarySources = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
					else
						inVoluntarySources += RepoConsts.LABEL_SEPARATOR + (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}

				String allergen = (String) nodeService.getProperty(dataItem.getAllergen(), ContentModel.PROP_NAME);
				String allergenType = (String) nodeService.getProperty(dataItem.getAllergen(), BeCPGModel.PROP_ALLERGEN_TYPE);

				Element AllergenElt = allergenListElt.addElement(TAG_ALLERGEN);
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_ALLERGEN.getLocalName(), allergen);
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGEN_TYPE.getLocalName(), allergenType);
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY.getLocalName(), Boolean.toString(dataItem.getVoluntary()));
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY.getLocalName(), Boolean.toString(dataItem.getInVoluntary()));
				AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES.getLocalName(), voluntarySources);
				AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES.getLocalName(), inVoluntarySources);
			}
		}

		// compoList
		if (productData.getCompoList() != null) {
			Element compoListElt = dataListsElt.addElement(TAG_COMPOLIST);

			for (CompoListDataItem dataItem : productData.getCompoList()) {

				String partName = (String) nodeService.getProperty(dataItem.getProduct(), ContentModel.PROP_NAME);

				Element partElt = compoListElt.addElement(TAG_ENTITY);
				partElt.addAttribute(BeCPGModel.ASSOC_COMPOLIST_PRODUCT.getLocalName(), partName);
				partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(dataItem.getDepthLevel()));
				partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY.getLocalName(), dataItem.getQty() == null ? VALUE_NULL : Double.toString(dataItem.getQty()));
				partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA.getLocalName(),
						dataItem.getQtySubFormula() == null ? VALUE_NULL : Double.toString(dataItem.getQtySubFormula()));
				partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_LOSS_PERC.getLocalName(), dataItem.getLossPerc() == null ? VALUE_NULL : Double.toString(dataItem.getLossPerc()));
				PropertyDefinition propertyDef = dictionaryService.getProperty(BeCPGModel.PROP_COMPOLIST_DECL_TYPE);
				partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_DECL_TYPE.getLocalName(), attributeExtractorService.getStringValue(propertyDef, dataItem.getDeclType().toString(), new PropertyFormats(true)));
			}
		}

		// CostList
		if (productData.getCostList() != null) {
			Element costListElt = dataListsElt.addElement(TAG_COSTLIST);

			for (CostListDataItem dataItem : productData.getCostList()) {

				String cost = (String) nodeService.getProperty(dataItem.getCost(), ContentModel.PROP_NAME);

				Element costElt = costListElt.addElement(TAG_COST);
				costElt.addAttribute(BeCPGModel.ASSOC_COSTLIST_COST.getLocalName(), cost);
				costElt.addAttribute(BeCPGModel.PROP_COSTLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : Double.toString(dataItem.getValue()));
				costElt.addAttribute(BeCPGModel.PROP_COSTLIST_UNIT.getLocalName(), dataItem.getUnit());
			}
		}

		// IngList
		if (productData.getIngList() != null) {
			Element ingListElt = dataListsElt.addElement(TAG_INGLIST);

			for (IngListDataItem dataItem : productData.getIngList()) {

				String ing = (String) nodeService.getProperty(dataItem.getIng(), ContentModel.PROP_NAME);
				String ingCEECode = (String) nodeService.getProperty(dataItem.getIng(), BeCPGModel.PROP_ING_CEECODE);

				String geoOrigins = VALUE_NULL;
				for (NodeRef nodeRef : dataItem.getGeoOrigin()) {
					if (geoOrigins.isEmpty())
						geoOrigins = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString();
					else
						geoOrigins += RepoConsts.LABEL_SEPARATOR + (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}

				String bioOrigins = VALUE_NULL;
				for (NodeRef nodeRef : dataItem.getBioOrigin()) {
					if (bioOrigins.isEmpty())
						bioOrigins = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
					else
						bioOrigins += RepoConsts.LABEL_SEPARATOR + (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}

				Element ingElt = ingListElt.addElement(TAG_ING);
				ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_ING.getLocalName(), ing);
				ingElt.addAttribute(BeCPGModel.PROP_INGLIST_QTY_PERC.getLocalName(), dataItem.getQtyPerc() == null ? VALUE_NULL : Double.toString(dataItem.getQtyPerc()));
				ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_GEO_ORIGIN.getLocalName(), geoOrigins);
				ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_BIO_ORIGIN.getLocalName(), bioOrigins);
				ingElt.addAttribute(BeCPGModel.PROP_INGLIST_IS_GMO.getLocalName(), Boolean.toString(dataItem.isGMO()));
				ingElt.addAttribute(BeCPGModel.PROP_INGLIST_IS_IONIZED.getLocalName(), Boolean.toString(dataItem.isIonized()));
				ingElt.addAttribute(BeCPGModel.PROP_ING_CEECODE.getLocalName(), ingCEECode);
			}
		}

		// IngLabelingList
		if (productData.getIngLabelingList() != null) {
			Element ingListElt = dataListsElt.addElement(TAG_INGLABELINGLIST);

			for (IngLabelingListDataItem dataItem : productData.getIngLabelingList()) {								
				
				List<String> locales = new ArrayList<String>();
				for(Locale locale : dataItem.getValue().getLocales()){			
					
					logger.debug("ill, locale: " + locale);							
					MLText grpMLText =  dataItem.getGrp()!=null ? (MLText)mlNodeService.getProperty(dataItem.getGrp(), BeCPGModel.PROP_LEGAL_NAME) : null;	

					if(!locales.contains(locale.getLanguage())){
					
						locales.add(locale.getLanguage());						
						String groupName = grpMLText!=null ? grpMLText.getValue(locale) : VALUE_NULL;						
						
						Element ingLabelingElt = ingListElt.addElement(TAG_INGLABELING);
						ingLabelingElt.addAttribute(ATTR_LANGUAGE, locale.getDisplayLanguage());					
						ingLabelingElt.addAttribute(BeCPGModel.ASSOC_ILL_GRP.getLocalName(), groupName);
						ingLabelingElt.addAttribute(BeCPGModel.PROP_ILL_VALUE.getLocalName(), dataItem.getValue().getValue(locale));
						
						if(logger.isDebugEnabled()){
							logger.debug("ill, locale: " + locale + " - group: " + groupName + " - value: " + dataItem.getValue().getValue(locale));							
						}
					}					
				}				
			}
		}

		// NutList
		if (productData.getNutList() != null) {

			Element nutListElt = dataListsElt.addElement(TAG_NUTLIST);

			for (NutListDataItem dataItem : productData.getNutList()) {

				String nut = nodeService.getProperty(dataItem.getNut(), ContentModel.PROP_NAME).toString();

				Element nutElt = nutListElt.addElement(TAG_NUT);
				nutElt.addAttribute(BeCPGModel.ASSOC_NUTLIST_NUT.getLocalName(), nut);
				nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : String.valueOf(dataItem.getValue()));
				nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_UNIT.getLocalName(), dataItem.getUnit());
				nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_GROUP.getLocalName(), dataItem.getGroup());
			}
		}

		// OrganoList
		if (productData.getOrganoList() != null) {

			Element organoListElt = dataListsElt.addElement(TAG_ORGANOLIST);

			for (OrganoListDataItem dataItem : productData.getOrganoList()) {

				String organo = nodeService.getProperty(dataItem.getOrgano(), ContentModel.PROP_NAME).toString();

				Element organoElt = organoListElt.addElement(TAG_ORGANO);
				organoElt.addAttribute(BeCPGModel.ASSOC_ORGANOLIST_ORGANO.getLocalName(), organo);
				organoElt.addAttribute(BeCPGModel.PROP_ORGANOLIST_VALUE.getLocalName(), dataItem.getValue());
			}
		}

		// MicrobioList
		List<AssociationRef> microbioAssocRefs = nodeService.getTargetAssocs(entityNodeRef,
				BeCPGModel.ASSOC_PRODUCT_MICROBIO_CRITERIA);
		if(microbioAssocRefs.size()>0){
			NodeRef productMicrobioCriteriaNodeRef = microbioAssocRefs.get(0).getTargetRef();			
			
			if(productMicrobioCriteriaNodeRef != null){
				Set<QName> pmcdataLists = new HashSet<QName>();
				pmcdataLists.add(BeCPGModel.TYPE_MICROBIOLIST);
				ProductData pmcData = productDAO.find(productMicrobioCriteriaNodeRef, pmcdataLists);
				
				if (pmcData.getMicrobioList() != null) {
					
					Element organoListElt = dataListsElt.addElement(TAG_MICROBIOLIST);

					for (MicrobioListDataItem dataItem : pmcData.getMicrobioList()) {

						String microbio = nodeService.getProperty(dataItem.getMicrobio(), ContentModel.PROP_NAME).toString();

						Element microbioElt = organoListElt.addElement(TAG_MICROBIO);
						microbioElt.addAttribute(BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO.getLocalName(), microbio);
						microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : Double.toString(dataItem.getValue()));
						microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_UNIT.getLocalName(), dataItem.getUnit());
						microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_MAXI.getLocalName(), dataItem.getMaxi() == null ? VALUE_NULL : Double.toString(dataItem.getMaxi()));
						microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_TEXT_CRITERIA.getLocalName(), dataItem.getTextCriteria());
					}
				}
			}
		}		

		// PhysicoChemList
		if (productData.getPhysicoChemList() != null) {

			Element physicoChemListElt = dataListsElt.addElement(TAG_PHYSICOCHEMLIST);

			for (PhysicoChemListDataItem dataItem : productData.getPhysicoChemList()) {

				String physicoChem = nodeService.getProperty(dataItem.getPhysicoChem(), ContentModel.PROP_NAME).toString();

				Element physicoChemElt = physicoChemListElt.addElement(TAG_PHYSICOCHEM);
				physicoChemElt.addAttribute(BeCPGModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM.getLocalName(), physicoChem);
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : Double.toString(dataItem.getValue()));
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_UNIT.getLocalName(), dataItem.getUnit());
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_MINI.getLocalName(), dataItem.getMini() == null ? VALUE_NULL : Double.toString(dataItem.getMini()));
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_MAXI.getLocalName(), dataItem.getMaxi() == null ? VALUE_NULL : Double.toString(dataItem.getMaxi()));
			}
		}

		return dataListsElt;
	}

}
