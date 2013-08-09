package fr.becpg.repo.product.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingLevel;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.report.entity.impl.AbstractEntityReportExtractor;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.variant.model.VariantData;

//TODO use annotation on product data instead
@Deprecated
public class DefaultProductReportExtractor extends AbstractEntityReportExtractor {

	/** The Constant KEY_PRODUCT_IMAGE. */
	protected static final String KEY_PRODUCT_IMAGE = "productImage";

	private static Log logger = LogFactory.getLog(DefaultProductReportExtractor.class);

	/** The Constant TAG_ALLERGENLIST. */
	protected static final String TAG_ALLERGENLIST = "allergenList";

	/** The Constant TAG_COMPOLIST. */
	protected static final String TAG_COMPOLIST = "compoList";

	protected static final String TAG_DYNAMICCHARACTLIST = "dynamicCharactList";

	protected static final String TAG_DYNAMICCHARACT = "dynamicCharact";

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

	private static final String TAG_PLANTS = "plants";
	private static final String TAG_PLANT = "plant";

	/** The Constant TAG_PHYSICOCHEM. */
	protected static final String TAG_PHYSICOCHEM = "physicoChem";

	protected static final String ATTR_LANGUAGE = "language";	
	
	private static final String ATTR_PKG_TARE_LEVEL_1 = "tarePkgLevel1";
	private static final String ATTR_PKG_TARE_LEVEL_2 = "tarePkgLevel2";
	private static final String ATTR_PKG_TARE_LEVEL_3 = "tarePkgLevel3";
	private static final String ATTR_PKG_NET_WEIGHT_LEVEL_1 = "netWeightPkgLevel1";
	private static final String ATTR_PKG_NET_WEIGHT_LEVEL_2 = "netWeightPkgLevel2";
	private static final String ATTR_PKG_NET_WEIGHT_LEVEL_3 = "netWeightPkgLevel3";
	private static final String ATTR_PKG_GROSS_WEIGHT_LEVEL_1 = "grossWeightPkgLevel1";
	private static final String ATTR_PKG_GROSS_WEIGHT_LEVEL_2 = "grossWeightPkgLevel2";
	private static final String ATTR_PKG_GROSS_WEIGHT_LEVEL_3 = "grossWeightPkgLevel3";
	private static final String ATTR_NB_PRODUCTS_LEVEL_3= "nbProductsPkgLevel3";
	private static final String ATTR_NB_PRODUCTS_LEVEL_2= "nbProductsPkgLevel2";
	
	private static final String PROP_DYNAMIC_CHARACT_COLUMN = "bcpg:dynamicCharactColumn";
	

	protected AlfrescoRepository<ProductData> alfrescoRepository;

	protected ProductDictionaryService productDictionaryService;

	private NodeService mlNodeService;

	

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
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
	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, Map<String, byte[]> images) {

		// TODO make it more generic!!!!
		ProductData productData = (ProductData) alfrescoRepository.findOne(entityNodeRef);
		
		NodeRef defaultVariantNodeRef = loadVariants(productData, dataListsElt.getParent());

		// allergen
		if (productData.getAllergenList() != null) {
			Element allergenListElt = dataListsElt.addElement(TAG_ALLERGENLIST);
			String volAllergens = "";
			String inVolAllergens = "";

			for (AllergenListDataItem dataItem : productData.getAllergenList()) {

				String allergen = (String) nodeService.getProperty(dataItem.getAllergen(), ContentModel.PROP_NAME);
				String allergenType = (String) nodeService.getProperty(dataItem.getAllergen(), BeCPGModel.PROP_ALLERGEN_TYPE);

				Element AllergenElt = allergenListElt.addElement(TAG_ALLERGEN);
				AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_ALLERGEN.getLocalName(), allergen);
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGEN_TYPE.getLocalName(), allergenType);
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY.getLocalName(), Boolean.toString(dataItem.getVoluntary()));
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY.getLocalName(), Boolean.toString(dataItem.getInVoluntary()));
				AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES.getLocalName(), extractNames(dataItem.getVoluntarySources()));
				AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES.getLocalName(), extractNames(dataItem.getInVoluntarySources()));
				extractVariants(dataItem.getVariants(), AllergenElt, defaultVariantNodeRef);
				
				// concat allergens
				if (dataItem.getVoluntary()) {
					if (volAllergens.isEmpty()) {
						volAllergens = allergen;
					} else {
						volAllergens += RepoConsts.LABEL_SEPARATOR + allergen;
					}
				}
				else if(dataItem.getInVoluntary()){
					if (inVolAllergens.isEmpty()) {
						inVolAllergens = allergen;
					} else {
						inVolAllergens += RepoConsts.LABEL_SEPARATOR + allergen;
					}
				}				
			}

			allergenListElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY.getLocalName(), volAllergens);
			allergenListElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY.getLocalName(), inVolAllergens);
		}

		// compoList
		if (productData.hasCompoListEl(EffectiveFilters.EFFECTIVE)) {
			Element compoListElt = dataListsElt.addElement(TAG_COMPOLIST);

			for (CompoListDataItem dataItem : productData.getCompoList(EffectiveFilters.EFFECTIVE)) {

				if (dataItem.getProduct() !=null && nodeService.exists(dataItem.getProduct())) {
					String partName = (String) nodeService.getProperty(dataItem.getProduct(), ContentModel.PROP_NAME);
					String partCode = (String) nodeService.getProperty(dataItem.getProduct(), BeCPGModel.PROP_CODE);
					String partERPCode = (String) nodeService.getProperty(dataItem.getProduct(), BeCPGModel.PROP_ERP_CODE);
					String legalName = (String) nodeService.getProperty(dataItem.getProduct(), BeCPGModel.PROP_LEGAL_NAME);					

					Element partElt = compoListElt.addElement(TAG_ENTITY);
					partElt.addAttribute(BeCPGModel.ASSOC_COMPOLIST_PRODUCT.getLocalName(), partName);
					partElt.addAttribute(BeCPGModel.PROP_CODE.getLocalName(), partCode);
					partElt.addAttribute(BeCPGModel.PROP_ERP_CODE.getLocalName(), partERPCode);
					partElt.addAttribute(BeCPGModel.PROP_LEGAL_NAME.getLocalName(), legalName);
					partElt.addAttribute(BeCPGModel.ASSOC_SUPPLIERS.getLocalName(), extractNames(associationService.getTargetAssocs(dataItem.getProduct(), BeCPGModel.ASSOC_SUPPLIERS)));
					partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), dataItem.getDepthLevel() == null ? VALUE_NULL : Integer.toString(dataItem.getDepthLevel()));
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY.getLocalName(), toString(dataItem.getQty()));
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA.getLocalName(), toString(dataItem.getQtySubFormula()));
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_UNIT.getLocalName(), dataItem.getCompoListUnit() == null ? VALUE_NULL : dataItem.getCompoListUnit().toString());
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_YIELD_PERC.getLocalName(), toString(dataItem.getYieldPerc()));
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_LOSS_PERC.getLocalName(), toString(dataItem.getLossPerc()));
					PropertyDefinition propertyDef = dictionaryService.getProperty(BeCPGModel.PROP_COMPOLIST_DECL_TYPE);
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_DECL_TYPE.getLocalName(),
							attributeExtractorService.getStringValue(propertyDef, dataItem.getDeclType().toString(), new PropertyFormats(true)));
					partElt.addAttribute(ATTR_ITEM_TYPE, nodeService.getType(dataItem.getProduct()).toPrefixString(namespaceService));
					partElt.addAttribute(ATTR_ASPECTS, extractAspects(dataItem.getProduct()));
					extractVariants(dataItem.getVariants(), partElt, defaultVariantNodeRef);
					
					for(int i=1 ; i<=5 ; i++){
						QName qName = QName.createQName(PROP_DYNAMIC_CHARACT_COLUMN + i, namespaceService);
						Serializable s = nodeService.getProperty(dataItem.getNodeRef(), qName);
						if(s != null){
							partElt.addAttribute(qName.getLocalName(), s.toString());
						}
					}
				}
			}

			Element dynCharactListElt = dataListsElt.addElement(TAG_DYNAMICCHARACTLIST);
			for (DynamicCharactListItem dc : productData.getCompoListView().getDynamicCharactList()) {
				Element dynamicCharact = dynCharactListElt.addElement(TAG_DYNAMICCHARACT);
				dynamicCharact.addAttribute(BeCPGModel.PROP_DYNAMICCHARACT_TITLE.getLocalName(), dc.getName());
				dynamicCharact.addAttribute(BeCPGModel.PROP_DYNAMICCHARACT_VALUE.getLocalName(), dc.getValue() == null ? VALUE_NULL : dc.getValue().toString());
			}

		}

		// packList
		loadPackaging(productData, dataListsElt, defaultVariantNodeRef, images);

		// CostList
		if (productData.getCostList() != null) {
			Element costListElt = dataListsElt.addElement(TAG_COSTLIST);

			for (CostListDataItem dataItem : productData.getCostList()) {
				
				String cost = (String) nodeService.getProperty(dataItem.getCost(), ContentModel.PROP_NAME);

				Element costElt = costListElt.addElement(TAG_COST);
				costElt.addAttribute(BeCPGModel.ASSOC_COSTLIST_COST.getLocalName(), cost);
				costElt.addAttribute(BeCPGModel.PROP_COSTLIST_VALUE.getLocalName(), toString(dataItem.getValue()));
				costElt.addAttribute(BeCPGModel.PROP_COSTLIST_UNIT.getLocalName(), dataItem.getUnit());
			}
		}

		// IngList
		if (productData.getIngList() != null) {
			Element ingListElt = dataListsElt.addElement(TAG_INGLIST);

			for (IngListDataItem dataItem : productData.getIngList()) {

				String ing = (String) nodeService.getProperty(dataItem.getIng(), ContentModel.PROP_NAME);
				String ingCEECode = (String) nodeService.getProperty(dataItem.getIng(), BeCPGModel.PROP_ING_CEECODE);

				Element ingElt = ingListElt.addElement(TAG_ING);
				ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_ING.getLocalName(), ing);
				ingElt.addAttribute(BeCPGModel.PROP_INGLIST_QTY_PERC.getLocalName(), toString(dataItem.getQtyPerc()));
				ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_GEO_ORIGIN.getLocalName(), extractNames(dataItem.getGeoOrigin()));
				ingElt.addAttribute(BeCPGModel.ASSOC_INGLIST_BIO_ORIGIN.getLocalName(), extractNames(dataItem.getBioOrigin()));
				ingElt.addAttribute(BeCPGModel.PROP_INGLIST_IS_GMO.getLocalName(), Boolean.toString(dataItem.getIsGMO()));
				ingElt.addAttribute(BeCPGModel.PROP_INGLIST_IS_IONIZED.getLocalName(), Boolean.toString(dataItem.getIsIonized()));
				ingElt.addAttribute(BeCPGModel.PROP_ING_CEECODE.getLocalName(), ingCEECode);
			}
		}

		// IngLabelingList
		if (productData.getIngLabelingList() != null) {
			Element ingListElt = dataListsElt.addElement(TAG_INGLABELINGLIST);

			for (IngLabelingListDataItem dataItem : productData.getIngLabelingList()) {

				List<String> locales = new ArrayList<String>();
				for (Locale locale : dataItem.getValue().getLocales()) {

					logger.debug("ill, locale: " + locale);
					MLText grpMLText = dataItem.getGrp() != null ? (MLText) mlNodeService.getProperty(dataItem.getGrp(), BeCPGModel.PROP_LEGAL_NAME) : null;

					if (!locales.contains(locale.getLanguage())) {

						locales.add(locale.getLanguage());

						Element ingLabelingElt = ingListElt.addElement(TAG_INGLABELING);
						ingLabelingElt.addAttribute(ATTR_LANGUAGE, locale.getDisplayLanguage());
						ingLabelingElt.addAttribute(BeCPGModel.ASSOC_ILL_GRP.getLocalName(), grpMLText != null ? grpMLText.getValue(locale) : VALUE_NULL);
						ingLabelingElt.addAttribute(BeCPGModel.PROP_ILL_VALUE.getLocalName(), dataItem.getValue() != null ? dataItem.getValue().getValue(locale) : VALUE_NULL);
						ingLabelingElt.addAttribute(BeCPGModel.PROP_ILL_MANUAL_VALUE.getLocalName(), dataItem.getManualValue() != null  ? dataItem.getManualValue().getValue(locale) : VALUE_NULL);

						if (logger.isDebugEnabled()) {
							logger.debug("ingLabelingElt: " + ingLabelingElt.asXML());
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
				String value = dataItem.getValue() == null ? VALUE_NULL : String.valueOf(dataItem.getValue());

				Element nutElt = nutListElt.addElement(TAG_NUT);
				nutElt.addAttribute(BeCPGModel.ASSOC_NUTLIST_NUT.getLocalName(), nut);
				nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_VALUE.getLocalName(), value);
				nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_UNIT.getLocalName(), dataItem.getUnit());
				nutElt.addAttribute(BeCPGModel.PROP_NUTLIST_GROUP.getLocalName(), dataItem.getGroup());

				// add nut as attributes
				nutListElt.addAttribute(generateKeyAttribute(nut), value);
			}
		}

		// OrganoList
		if (productData.getOrganoList() != null) {

			Element organoListElt = dataListsElt.addElement(TAG_ORGANOLIST);

			for (OrganoListDataItem dataItem : productData.getOrganoList()) {

				String organo = nodeService.getProperty(dataItem.getOrgano(), ContentModel.PROP_NAME).toString();

				Element organoElt = organoListElt.addElement(TAG_ORGANO);
				organoElt.addAttribute(BeCPGModel.ASSOC_ORGANOLIST_ORGANO.getLocalName(), organo);
				//organoElt.addAttribute(BeCPGModel.PROP_ORGANOLIST_VALUE.getLocalName(), dataItem.getValue());
				Element cDATAElt = organoElt.addElement(BeCPGModel.PROP_ORGANOLIST_VALUE.getLocalName());
				cDATAElt.addCDATA(dataItem.getValue());				
			}
		}

		// MicrobioList
		Map<NodeRef, MicrobioListDataItem> microbioMap = new HashMap<NodeRef, MicrobioListDataItem>();
		if (productData.getMicrobioList() != null) {
			for (MicrobioListDataItem dataItem : productData.getMicrobioList()) {
				microbioMap.put(dataItem.getMicrobio(), dataItem);
			}
		}
		
		List<NodeRef> productMicrobioCriteriaNodeRefs = associationService.getTargetAssocs(entityNodeRef, BeCPGModel.ASSOC_PRODUCT_MICROBIO_CRITERIA);
		if (!productMicrobioCriteriaNodeRefs.isEmpty()) {
			NodeRef productMicrobioCriteriaNodeRef = productMicrobioCriteriaNodeRefs.get(0);

			if (productMicrobioCriteriaNodeRef != null) {
				ProductData pmcData = alfrescoRepository.findOne(productMicrobioCriteriaNodeRef);
				
				if (pmcData.getMicrobioList() != null) {
					for (MicrobioListDataItem dataItem : pmcData.getMicrobioList()) {
						if(!microbioMap.containsKey(dataItem.getMicrobio())){
							microbioMap.put(dataItem.getMicrobio(), dataItem);
						}
					}
				}
			}
		}		
		
		if(!microbioMap.isEmpty()){
			Element organoListElt = dataListsElt.addElement(TAG_MICROBIOLIST);
			for (MicrobioListDataItem dataItem : microbioMap.values()) {

				String microbio = nodeService.getProperty(dataItem.getMicrobio(), ContentModel.PROP_NAME).toString();

				Element microbioElt = organoListElt.addElement(TAG_MICROBIO);
				microbioElt.addAttribute(BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO.getLocalName(), microbio);
				microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_VALUE.getLocalName(), toString(dataItem.getValue()));
				microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_UNIT.getLocalName(), dataItem.getUnit());
				microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_MAXI.getLocalName(), toString(dataItem.getMaxi()));
				microbioElt.addAttribute(BeCPGModel.PROP_MICROBIOLIST_TEXT_CRITERIA.getLocalName(), dataItem.getTextCriteria());
			}
		}

		// PhysicoChemList
		if (productData.getPhysicoChemList() != null) {

			Element physicoChemListElt = dataListsElt.addElement(TAG_PHYSICOCHEMLIST);

			for (PhysicoChemListDataItem dataItem : productData.getPhysicoChemList()) {

				String physicoChem = nodeService.getProperty(dataItem.getPhysicoChem(), ContentModel.PROP_NAME).toString();

				Element physicoChemElt = physicoChemListElt.addElement(TAG_PHYSICOCHEM);
				physicoChemElt.addAttribute(BeCPGModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM.getLocalName(), physicoChem);
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_VALUE.getLocalName(), toString(dataItem.getValue()));
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_UNIT.getLocalName(), dataItem.getUnit());
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_MINI.getLocalName(), toString(dataItem.getMini()));
				physicoChemElt.addAttribute(BeCPGModel.PROP_PHYSICOCHEMLIST_MAXI.getLocalName(), toString(dataItem.getMaxi()));
			}
		}
		
		if(productData.getLabelClaimList() != null){
			Element labelClaimListElt = dataListsElt.addElement(BeCPGModel.TYPE_LABELCLAIMLIST.getLocalName());
			
			for(LabelClaimListDataItem l : productData.getLabelClaimList()){
				
				String labelClaim = (String)nodeService.getProperty(l.getLabelClaim(), ContentModel.PROP_NAME);
				
				Element labelClaimElt = labelClaimListElt.addElement(BeCPGModel.TYPE_LABEL_CLAIM.getLocalName());				
				labelClaimElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), labelClaim);
				labelClaimElt.addAttribute(BeCPGModel.PROP_LCL_IS_CLAIMED.getLocalName(), Boolean.toString(l.getIsClaimed()));
				labelClaimElt.addAttribute(BeCPGModel.PROP_LCL_TYPE.getLocalName(), l.getType());
			}
		}
	}

	@Override
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt) {

		if(assocDef != null && assocDef.getName() != null){
			if(assocDef.getName().equals(BeCPGModel.ASSOC_PLANTS) || assocDef.getName().equals(BeCPGModel.ASSOC_STORAGE_CONDITIONS) || assocDef.getName().equals(BeCPGModel.ASSOC_PRECAUTION_OF_USE)){

				extractTargetAssoc(entityNodeRef, assocDef, entityElt);				
				return true;
			}
		}
		
		return false;			
	}
	
	private void loadPackaging(ProductData productData, Element dataListsElt, NodeRef defaultVariantNodeRef, Map<String, byte[]> images){			
		
		if (productData.hasPackagingListEl(EffectiveFilters.EFFECTIVE)) {
			
			PackagingData packagingData = new PackagingData();
			Element packagingListElt = dataListsElt.addElement(BeCPGModel.TYPE_PACKAGINGLIST.getLocalName());

			for (PackagingListDataItem dataItem : productData.getPackagingList(EffectiveFilters.EFFECTIVE)) {
				loadPackaging(dataItem, packagingListElt, packagingData, defaultVariantNodeRef, images);				
			}
			
			// display tare, net weight and gross weight
			Double tarePrimary = packagingData.getTarePrimary();
			Double netWeightPrimary = FormulationHelper.getNetWeight(productData.getNodeRef(), nodeService);
			packagingListElt.addAttribute(ATTR_PKG_TARE_LEVEL_1, toString(tarePrimary));
			packagingListElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_1, toString(netWeightPrimary));
			packagingListElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_1, toString(tarePrimary + netWeightPrimary));
			
			if(packagingData.getProductPerBoxes() != null){							
				Double tareSecondary = tarePrimary * packagingData.getProductPerBoxes() + packagingData.getTareSecondary();
				Double netWeightSecondary = netWeightPrimary * packagingData.getProductPerBoxes();
				packagingListElt.addAttribute(ATTR_PKG_TARE_LEVEL_2, toString(tareSecondary));
				packagingListElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_2, toString(netWeightSecondary));
				packagingListElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_2, toString(tareSecondary + netWeightSecondary));
				
				if(packagingData.getBoxesPerPallet() != null){
					
					Double tareTertiary = tareSecondary * packagingData.getBoxesPerPallet() + packagingData.getTareTertiary();
					Double netWeightTertiary = netWeightSecondary * packagingData.getBoxesPerPallet();
					packagingListElt.addAttribute(ATTR_PKG_TARE_LEVEL_3, toString(tareTertiary));
					packagingListElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_3, toString(netWeightTertiary));
					packagingListElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_3, toString(tareTertiary + netWeightTertiary));
					packagingListElt.addAttribute(ATTR_NB_PRODUCTS_LEVEL_2, toString(packagingData.getProductPerBoxes()));
					packagingListElt.addAttribute(ATTR_NB_PRODUCTS_LEVEL_3, toString(packagingData.getProductPerBoxes() * packagingData.getBoxesPerPallet()));
				}
			}
					
		}		
	}

	private void loadPackaging(PackagingListDataItem dataItem, Element packagingListElt, 
			PackagingData packagingData, NodeRef defaultVariantNodeRef, Map<String, byte[]> images) {

		if (nodeService.getType(dataItem.getProduct()).equals(BeCPGModel.TYPE_PACKAGINGKIT)) {					
			loadPackagingKit(dataItem, packagingListElt, packagingData, defaultVariantNodeRef);
			Element imgsElt = (Element)packagingListElt.getDocument().selectSingleNode(TAG_ENTITY + "/" + TAG_IMAGES);
			if(imgsElt != null){
				extractEntityImages(dataItem.getProduct(), imgsElt, images);
			}
		} else {
			loadPackaging(dataItem, packagingListElt, packagingData, defaultVariantNodeRef);
		}		
	}

	private Element loadPackaging(PackagingListDataItem dataItem, Element packagingListElt, PackagingData packagingData, NodeRef defaultVariantNodeRef) {
		String partName = (String) nodeService.getProperty(dataItem.getProduct(), ContentModel.PROP_NAME);
		String partCode = (String) nodeService.getProperty(dataItem.getProduct(), BeCPGModel.PROP_CODE);
		String partERPCode = (String) nodeService.getProperty(dataItem.getProduct(), BeCPGModel.PROP_ERP_CODE);
		String legalName = (String) nodeService.getProperty(dataItem.getProduct(), BeCPGModel.PROP_LEGAL_NAME);	
		QName nodeType = nodeService.getType(dataItem.getProduct());

		Element partElt = packagingListElt.addElement(TAG_ENTITY);
		partElt.addAttribute(BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT.getLocalName(), partName);
		partElt.addAttribute(BeCPGModel.PROP_CODE.getLocalName(), partCode);
		partElt.addAttribute(BeCPGModel.PROP_ERP_CODE.getLocalName(), partERPCode);
		partElt.addAttribute(BeCPGModel.PROP_LEGAL_NAME.getLocalName(), legalName);
		partElt.addAttribute(BeCPGModel.PROP_PRODUCT_COMMENTS.getLocalName(), (String) nodeService.getProperty(dataItem.getProduct(), BeCPGModel.PROP_PRODUCT_COMMENTS));
		partElt.addAttribute(BeCPGModel.ASSOC_SUPPLIERS.getLocalName(), extractNames(associationService.getTargetAssocs(dataItem.getProduct(), BeCPGModel.ASSOC_SUPPLIERS)));
		partElt.addAttribute(BeCPGModel.PROP_PACKAGINGLIST_QTY.getLocalName(), toString(dataItem.getQty()));
		partElt.addAttribute(BeCPGModel.PROP_PACKAGINGLIST_UNIT.getLocalName(), dataItem.getPackagingListUnit() == null ? VALUE_NULL : dataItem.getPackagingListUnit().toString());
		partElt.addAttribute(BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL.getLocalName(), dataItem.getPkgLevel() == null ? VALUE_NULL : dataItem.getPkgLevel().toString());
		partElt.addAttribute(BeCPGModel.PROP_PACKAGINGLIST_ISMASTER.getLocalName(), dataItem.getIsMaster() == null ? VALUE_NULL : dataItem.getIsMaster().toString());
		partElt.addAttribute(ATTR_ITEM_TYPE, nodeType.toPrefixString(namespaceService));
		partElt.addAttribute(ATTR_ASPECTS, extractAspects(dataItem.getProduct()));
		extractVariants(dataItem.getVariants(), partElt, defaultVariantNodeRef);

		if (nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_TARE)) {
			
			// Sum tare (don't take in account packagingKit)
			if(dataItem.getPkgLevel() != null && 
					(defaultVariantNodeRef == null || dataItem.getVariants() == null || dataItem.getVariants().contains(defaultVariantNodeRef)) &&
					!BeCPGModel.TYPE_PACKAGINGKIT.equals(nodeType)){
				
				Double tare = null;				
				if(FormulationHelper.isPackagingListUnitKg(dataItem.getPackagingListUnit())){
					tare = FormulationHelper.getQty(dataItem);
				}else{
					tare = FormulationHelper.getTareInKg(dataItem.getProduct(), nodeService);
				}
				
				if(tare != null){
					tare = FormulationHelper.getQty(dataItem) * tare;
					logger.debug("Tare " + partName + " " + tare);
					
					if(dataItem.getPkgLevel().equals(PackagingLevel.Primary)){
						packagingData.setTarePrimary(packagingData.getTarePrimary() + tare);
					}
					else if(dataItem.getPkgLevel().equals(PackagingLevel.Secondary)){					
						packagingData.setTareSecondary(packagingData.getTareSecondary() + tare);
					}
					else if(dataItem.getPkgLevel().equals(PackagingLevel.Tertiary)){
						packagingData.setTareTertiary(packagingData.getTareTertiary() + tare);
					}
				}				
			}
			
			partElt.addAttribute(PackModel.PROP_TARE.getLocalName(), toString((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_TARE)));
			partElt.addAttribute(PackModel.PROP_TARE_UNIT.getLocalName(), (String) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_TARE_UNIT));

		}
		if (nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_SIZE)) {
			partElt.addAttribute(PackModel.PROP_LENGTH.getLocalName(), toString((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_LENGTH)));
			partElt.addAttribute(PackModel.PROP_WIDTH.getLocalName(), toString((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_WIDTH)));
			partElt.addAttribute(PackModel.PROP_HEIGHT.getLocalName(), toString((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_HEIGHT)));
		}

		if (nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_PALLET)) {
			partElt.addAttribute(PackModel.PROP_PALLET_BOXES_PER_LAYER.getLocalName(), toString((Integer) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_BOXES_PER_LAYER)));
			partElt.addAttribute(PackModel.PROP_PALLET_LAYERS.getLocalName(), toString((Integer) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_LAYERS)));
			partElt.addAttribute(PackModel.PROP_PALLET_BOXES_PER_PALLET.getLocalName(), toString((Integer) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_BOXES_PER_PALLET)));			
			partElt.addAttribute(PackModel.PROP_PALLET_HEIGHT.getLocalName(), toString((Integer) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_HEIGHT)));
			
			// product per box and boxes per pallet
			if(defaultVariantNodeRef == null || dataItem.getVariants() == null || dataItem.getVariants().contains(defaultVariantNodeRef)){
				packagingData.setProductPerBoxes(dataItem.getQty().intValue());
				packagingData.setBoxesPerPallet((Integer)nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_BOXES_PER_PALLET));
			}			
		}

		if (nodeService.hasAspect(dataItem.getNodeRef(), PackModel.ASPECT_LABELING)) {	
			partElt.addAttribute(PackModel.PROP_LABELING_POSITION.getLocalName(), (String) nodeService.getProperty(dataItem.getNodeRef(), PackModel.PROP_LABELING_POSITION));
			extractTargetAssoc(dataItem.getNodeRef(), dictionaryService.getAssociation(PackModel.ASSOC_LABELING_TEMPLATE), partElt);
		}
		
		return partElt;
	}
	
	// manage 2 level depth
	private void loadPackagingKit(PackagingListDataItem dataItem, Element packagingListElt, PackagingData packagingData, NodeRef defaultVariantNodeRef) {

		Element packagingKitEl = loadPackaging(dataItem, packagingListElt, packagingData, defaultVariantNodeRef);
		Element dataListsElt = packagingKitEl.addElement(TAG_DATALISTS);
		Element packagingKitListEl = dataListsElt.addElement(BeCPGModel.TYPE_PACKAGINGLIST.getLocalName());	
		ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());
	
		for (PackagingListDataItem p : packagingKitData.getPackagingList(EffectiveFilters.EFFECTIVE)) {			
			loadPackaging(p, packagingKitListEl, packagingData, defaultVariantNodeRef);
		}
	}

	private String toString(Integer value) {
		return value == null ? VALUE_NULL : Integer.toString(value);
	}

	private String toString(Double value) {

		return value == null ? VALUE_NULL : Double.toString(value);
	}
	
	private class PackagingData{
		private Double tarePrimary = 0d;
		private Double tareSecondary = 0d;
		private Double tareTertiary = 0d;
		private Integer productPerBoxes;
		private Integer boxesPerPallet;
		
		public Double getTarePrimary() {
			return tarePrimary;
		}
		public void setTarePrimary(Double tarePrimary) {
			this.tarePrimary = tarePrimary;
		}
		public Double getTareSecondary() {
			return tareSecondary;
		}
		public void setTareSecondary(Double tareSecondary) {
			this.tareSecondary = tareSecondary;
		}
		public Double getTareTertiary() {
			return tareTertiary;
		}
		public void setTareTertiary(Double tareTertiary) {
			this.tareTertiary = tareTertiary;
		}
		public Integer getProductPerBoxes() {
			return productPerBoxes;
		}
		public void setProductPerBoxes(Integer productPerBoxes) {
			this.productPerBoxes = productPerBoxes;
		}
		public Integer getBoxesPerPallet() {
			return boxesPerPallet;
		}
		public void setBoxesPerPallet(Integer boxesPerPallet) {
			this.boxesPerPallet = boxesPerPallet;
		}	
	}	
	
	protected NodeRef loadVariants(ProductData productData, Element entityElt){
		NodeRef defaultVariantNodeRef = null;
		if(productData.getVariants() != null){
			Element variantsElt = entityElt.addElement(BeCPGModel.ASSOC_VARIANTS.getLocalName());
			for (VariantData variant : productData.getVariants()) {
				if (variant.getIsDefaultVariant()) {
					defaultVariantNodeRef = variant.getNodeRef();
				}
				
				Element variantElt = variantsElt.addElement(BeCPGModel.TYPE_VARIANT.getLocalName());
				variantElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), variant.getName());
				variantElt.addAttribute(BeCPGModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), Boolean.toString(variant.getIsDefaultVariant()));
				
			}
		}		
		return defaultVariantNodeRef;
	}
	
	protected void extractVariants(List<NodeRef> variantNodeRefs, Element dataItemElt, NodeRef defaultVariantNodeRef){
		
		if(variantNodeRefs != null && !variantNodeRefs.isEmpty()){
			dataItemElt.addAttribute(BeCPGModel.ASSOC_VARIANTS.getLocalName(), extractNames(variantNodeRefs));
			dataItemElt.addAttribute(BeCPGModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), Boolean.toString(variantNodeRefs.contains(defaultVariantNodeRef)));
		}			
	}
}
