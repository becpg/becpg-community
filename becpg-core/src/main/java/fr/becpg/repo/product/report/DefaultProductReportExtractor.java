package fr.becpg.repo.product.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
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
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.report.entity.impl.AbstractEntityReportExtractor;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.filters.EffectiveFilters;

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
	
	protected static final String ATTR_ITEM_TYPE = "itemType";

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
	private static final String TAG_IMAGES = "images";
	private static final String TAG_IMAGE = "image";
	private static final String PRODUCT_IMG_ID = "Img%d";

	/** The Constant TAG_PHYSICOCHEM. */
	protected static final String TAG_PHYSICOCHEM = "physicoChem";

	protected static final String ATTR_LANGUAGE = "language";
	protected static final String ATTR_NB_FP_THIRD_LEVEL = "nbFinishedProductThirdLevel";
	private static final String ATTR_ALLERGENS = "allergens";
	private static final String ATTR_IMAGE_ID = "id";

	protected AlfrescoRepository<ProductData> alfrescoRepository;

	protected ProductDictionaryService productDictionaryService;
	
	private NodeService mlNodeService;
	
	private FileFolderService fileFolderService;


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

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	@Override
	protected Map<String, byte[]> extractImages(NodeRef entityNodeRef, Element entityElt) {
		Map<String, byte[]> images = new HashMap<String, byte[]>();

		// create a dataset for images and load images
		Element imgsElt = entityElt.addElement(TAG_IMAGES);
		int cnt = 1;
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
		if (imagesFolderNodeRef != null) {
			for (FileInfo fileInfo : fileFolderService.listFiles(imagesFolderNodeRef)) {

				String imgName = fileInfo.getName().toLowerCase();
				if (imgName.endsWith(".jpg") || imgName.endsWith(".png") || imgName.endsWith(".gif")) {

					NodeRef imgNodeRef = fileInfo.getNodeRef();
					String imgId = String.format(PRODUCT_IMG_ID, cnt);
					byte[] imageBytes = entityService.getImage(imgNodeRef);
					if (imageBytes != null) {
						Element imgElt = imgsElt.addElement(TAG_IMAGE);
						imgElt.addAttribute(ATTR_IMAGE_ID, imgId);
						imgElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), imgName);
						imgElt.addAttribute(ContentModel.PROP_TITLE.getLocalName(),
								(String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_TITLE));

						images.put(imgId, imageBytes);
					}
					cnt++;
				}
			}
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
	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt) {

		// TODO make it more generic!!!!
		ProductData productData = (ProductData) alfrescoRepository.findOne(entityNodeRef);

		// allergen
		if (productData.getAllergenList() != null) {
			Element allergenListElt = dataListsElt.addElement(TAG_ALLERGENLIST);
			String allergens = "";

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
				AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_ALLERGEN.getLocalName(), allergen);
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGEN_TYPE.getLocalName(), allergenType);
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY.getLocalName(), Boolean.toString(dataItem.getVoluntary()));
				AllergenElt.addAttribute(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY.getLocalName(), Boolean.toString(dataItem.getInVoluntary()));
				AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES.getLocalName(), voluntarySources);
				AllergenElt.addAttribute(BeCPGModel.ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES.getLocalName(), inVoluntarySources);

				// concat allergens
				if (dataItem.getVoluntary() || dataItem.getInVoluntary()) {
					if (allergens.isEmpty()) {
						allergens = allergen;
					} else {
						allergens += RepoConsts.LABEL_SEPARATOR + allergen;
					}
				}
			}

			allergenListElt.addAttribute(ATTR_ALLERGENS, allergens);
		}

		// compoList
		if (productData.hasCompoListEl(EffectiveFilters.EFFECTIVE)) {
			Element compoListElt = dataListsElt.addElement(TAG_COMPOLIST);

			for (CompoListDataItem dataItem : productData.getCompoList(EffectiveFilters.EFFECTIVE)) {

				if (nodeService.exists(dataItem.getProduct())) {
					String partName = (String) nodeService.getProperty(dataItem.getProduct(), ContentModel.PROP_NAME);
					String legalName = (String) nodeService.getProperty(dataItem.getProduct(), BeCPGModel.PROP_LEGAL_NAME);
					List<AssociationRef> supplierAssocRefs = nodeService.getTargetAssocs(dataItem.getProduct(), BeCPGModel.ASSOC_SUPPLIERS);
					String suppliers = "";
					for (AssociationRef associationRef : supplierAssocRefs) {
						if (!suppliers.isEmpty()) {
							suppliers += RepoConsts.LABEL_SEPARATOR;
						}
						suppliers += (String) nodeService.getProperty(associationRef.getTargetRef(), ContentModel.PROP_NAME);
					}

					Element partElt = compoListElt.addElement(TAG_ENTITY);
					partElt.addAttribute(BeCPGModel.ASSOC_COMPOLIST_PRODUCT.getLocalName(), partName);
					partElt.addAttribute(BeCPGModel.PROP_LEGAL_NAME.getLocalName(), legalName);
					partElt.addAttribute(BeCPGModel.ASSOC_SUPPLIERS.getLocalName(), suppliers);
					partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(dataItem.getDepthLevel()));
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY.getLocalName(), dataItem.getQty() == null ? VALUE_NULL : Double.toString(dataItem.getQty()));
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA.getLocalName(),
							dataItem.getQtySubFormula() == null ? VALUE_NULL : Double.toString(dataItem.getQtySubFormula()));
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_QTY_AFTER_PROCESS.getLocalName(),
							dataItem.getQtyAfterProcess() == null ? VALUE_NULL : Double.toString(dataItem.getQtyAfterProcess()));
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_UNIT.getLocalName(), dataItem.getCompoListUnit() == null ? VALUE_NULL : dataItem.getCompoListUnit().toString());
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_YIELD_PERC.getLocalName(),
							dataItem.getYieldPerc() == null ? VALUE_NULL : Double.toString(dataItem.getYieldPerc()));
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_LOSS_PERC.getLocalName(), dataItem.getLossPerc() == null ? VALUE_NULL : Double.toString(dataItem.getLossPerc()));
					PropertyDefinition propertyDef = dictionaryService.getProperty(BeCPGModel.PROP_COMPOLIST_DECL_TYPE);
					partElt.addAttribute(BeCPGModel.PROP_COMPOLIST_DECL_TYPE.getLocalName(),
							attributeExtractorService.getStringValue(propertyDef, dataItem.getDeclType().toString(), new PropertyFormats(true)));
					partElt.addAttribute(ATTR_ITEM_TYPE, nodeService.getType(dataItem.getProduct()).toPrefixString(namespaceService));
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
		if (productData.hasPackagingListEl(EffectiveFilters.EFFECTIVE)) {
			Element packagingListElt = dataListsElt.addElement(BeCPGModel.TYPE_PACKAGINGLIST.getLocalName());

			for (PackagingListDataItem dataItem : productData.getPackagingList(EffectiveFilters.EFFECTIVE)) {
				loadPackaging(dataItem, packagingListElt);
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
						String groupName = grpMLText != null ? grpMLText.getValue(locale) : VALUE_NULL;

						Element ingLabelingElt = ingListElt.addElement(TAG_INGLABELING);
						ingLabelingElt.addAttribute(ATTR_LANGUAGE, locale.getDisplayLanguage());
						ingLabelingElt.addAttribute(BeCPGModel.ASSOC_ILL_GRP.getLocalName(), groupName);
						ingLabelingElt.addAttribute(BeCPGModel.PROP_ILL_VALUE.getLocalName(), dataItem.getValue().getValue(locale));

						if (logger.isDebugEnabled()) {
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
				organoElt.addAttribute(BeCPGModel.PROP_ORGANOLIST_VALUE.getLocalName(), dataItem.getValue());
			}
		}

		// MicrobioList
		List<AssociationRef> microbioAssocRefs = nodeService.getTargetAssocs(entityNodeRef, BeCPGModel.ASSOC_PRODUCT_MICROBIO_CRITERIA);
		if (!microbioAssocRefs.isEmpty()) {
			NodeRef productMicrobioCriteriaNodeRef = microbioAssocRefs.get(0).getTargetRef();

			if (productMicrobioCriteriaNodeRef != null) {
	
				ProductData pmcData = alfrescoRepository.findOne(productMicrobioCriteriaNodeRef);

				if (pmcData.getMicrobioList() != null) {

					Element organoListElt = dataListsElt.addElement(TAG_MICROBIOLIST);

					for (MicrobioListDataItem dataItem : pmcData.getMicrobioList()) {

						String microbio = nodeService.getProperty(dataItem.getMicrobio(), ContentModel.PROP_NAME).toString();

						Element microbioElt = organoListElt.addElement(TAG_MICROBIO);
						microbioElt.addAttribute(BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO.getLocalName(), microbio);
						microbioElt
								.addAttribute(BeCPGModel.PROP_MICROBIOLIST_VALUE.getLocalName(), dataItem.getValue() == null ? VALUE_NULL : Double.toString(dataItem.getValue()));
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
	}

	@Override
	protected void loadTargetAssocs(NodeRef entityNodeRef, Element entityElt) {

		// load plants
		Element plantsElt = entityElt.addElement(TAG_PLANTS);
		List<AssociationRef> plantAssocRefs = nodeService.getTargetAssocs(entityNodeRef, BeCPGModel.ASSOC_PLANTS);

		for (AssociationRef assocRef : plantAssocRefs) {

			Element plantElt = plantsElt.addElement(TAG_PLANT);
			NodeRef plantNodeRef = assocRef.getTargetRef();
			Map<ClassAttributeDefinition, String> plantAttributes = loadNodeAttributes(plantNodeRef);

			for (Map.Entry<ClassAttributeDefinition, String> attrKV : plantAttributes.entrySet()) {

				plantElt.addAttribute(attrKV.getKey().getName().getLocalName(), attrKV.getValue());
			}
		}
	}

	@Override
	protected void loadMultiLinesAttributes(Map.Entry<ClassAttributeDefinition, String> attrKV, Element entityElt) {

		if (attrKV.getKey().getName().equals(ContentModel.PROP_DESCRIPTION) || attrKV.getKey().getName().equals(BeCPGModel.PROP_PRODUCT_COMMENTS)) {
			extractMultiLines(entityElt, attrKV, attrKV.getKey().getName());
		}
	}

	private void loadPackaging(PackagingListDataItem dataItem, Element packagingListElt) {

		if (nodeService.getType(dataItem.getProduct()).equals(BeCPGModel.TYPE_PACKAGINGKIT)) {
			loadPackagingKit(dataItem, packagingListElt);
		} else {
			loadPackagingMaterial(dataItem, packagingListElt);
		}
	}

	private Element loadPackagingMaterial(PackagingListDataItem dataItem, Element packagingListElt) {
		String partName = (String) nodeService.getProperty(dataItem.getProduct(), ContentModel.PROP_NAME);
		String legalName = (String) nodeService.getProperty(dataItem.getProduct(), BeCPGModel.PROP_LEGAL_NAME);
		List<AssociationRef> supplierAssocRefs = nodeService.getTargetAssocs(dataItem.getProduct(), BeCPGModel.ASSOC_SUPPLIERS);
		String suppliers = "";
		for (AssociationRef associationRef : supplierAssocRefs) {
			if (!suppliers.isEmpty()) {
				suppliers += RepoConsts.LABEL_SEPARATOR;
			}
			suppliers += (String) nodeService.getProperty(associationRef.getTargetRef(), ContentModel.PROP_NAME);
		}

		Element partElt = packagingListElt.addElement(TAG_ENTITY);
		partElt.addAttribute(BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT.getLocalName(), partName);
		partElt.addAttribute(BeCPGModel.PROP_LEGAL_NAME.getLocalName(), legalName);
		partElt.addAttribute(BeCPGModel.ASSOC_SUPPLIERS.getLocalName(), suppliers);
		partElt.addAttribute(BeCPGModel.PROP_PACKAGINGLIST_QTY.getLocalName(), dataItem.getQty() == null ? VALUE_NULL : Double.toString(dataItem.getQty()));
		partElt.addAttribute(BeCPGModel.PROP_PACKAGINGLIST_UNIT.getLocalName(), dataItem.getPackagingListUnit() == null ? VALUE_NULL : dataItem.getPackagingListUnit().toString());
		partElt.addAttribute(BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL.getLocalName(), dataItem.getPkgLevel());
		partElt.addAttribute(BeCPGModel.PROP_PACKAGINGLIST_ISMASTER.getLocalName(), dataItem.getIsMaster() == null ? VALUE_NULL : dataItem.getIsMaster().toString());
		partElt.addAttribute(ATTR_ITEM_TYPE, nodeService.getType(dataItem.getProduct()).toPrefixString(namespaceService));

		return partElt;
	}

	// manage 2 level depth
	private void loadPackagingKit(PackagingListDataItem dataItem, Element packagingListElt) {

		ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());
		boolean isPallet = nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_PALLET);
		Element palletElt = null;

		for (PackagingListDataItem p : packagingKitData.getPackagingList(EffectiveFilters.EFFECTIVE)) {
			Element packagingMaterial = loadPackagingMaterial(p, packagingListElt);

			// use dataItem qty, instead of qty in pk
			packagingMaterial.addAttribute(BeCPGModel.PROP_PACKAGINGLIST_QTY.getLocalName(), dataItem.getQty() == null ? VALUE_NULL : Double.toString(dataItem.getQty()));

			// pallet has qty != 1
			if (isPallet && p.getQty() != 1d) {
				palletElt = packagingMaterial;
				Map<ClassAttributeDefinition, String> palletAttributes = loadNodeAttributes(dataItem.getProduct());

				for (Map.Entry<ClassAttributeDefinition, String> kv : palletAttributes.entrySet()) {

					QName propertyQName = kv.getKey().getName();
					if (palletElt.selectSingleNode("@" + propertyQName.getLocalName()) == null) {
						palletElt.addAttribute(propertyQName.getLocalName(), kv.getValue());
					}
				}

				// calculation (Nombre UVC/palette)
				if (dataItem.getQty() != null && p.getQty() != null) {
					Double qty = dataItem.getQty() * p.getQty();
					palletElt.addAttribute(ATTR_NB_FP_THIRD_LEVEL, qty.toString());
				}
			}
		}

		// is pallet
		if (isPallet && palletElt == null) {
			logger.error("PackagingKit with aspect Pallet doesn't have a third level packaging in is children");
		}
	}
}
