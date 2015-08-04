package fr.becpg.repo.product.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.packaging.PackagingData;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.AbstractManualVariantListDataItem;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamListItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.PackagingHelper;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.variant.model.VariantData;

//TODO use annotation on product data instead
@Deprecated
@Service
public class ProductReportExtractorPlugin extends DefaultEntityReportExtractor {

	/** The Constant KEY_PRODUCT_IMAGE. */
	protected static final String KEY_PRODUCT_IMAGE = "productImage";

	protected static final List<QName> DATALIST_SPECIFIC_EXTRACTOR = Arrays.asList(PLMModel.TYPE_COMPOLIST, PLMModel.TYPE_PACKAGINGLIST,
			MPMModel.TYPE_PROCESSLIST, PLMModel.TYPE_MICROBIOLIST, PLMModel.TYPE_INGLABELINGLIST, PLMModel.TYPE_NUTLIST, PLMModel.TYPE_ORGANOLIST);

	protected static final List<QName> RAWMATERIAL_DATALIST = Arrays.asList(PLMModel.TYPE_INGLIST, PLMModel.TYPE_ORGANOLIST);

	private static final Log logger = LogFactory.getLog(ProductReportExtractorPlugin.class);
	

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
	private static final String ATTR_NB_PRODUCTS_LEVEL_3 = "nbProductsPkgLevel3";
	private static final String ATTR_NB_PRODUCTS_LEVEL_2 = "nbProductsPkgLevel2";
	private static final String ATTR_VARIANT_ID = "variantId";
	private static final String ATTR_COMPOLIST_QTY_FOR_PRODUCT = "compoListQtyForProduct";	
	private static final String TAG_PACKAGING_LEVEL_MEASURES = "packagingLevelMeasures";

	@Value("${beCPG.product.report.multiLevel}")
	private Boolean extractInMultiLevel = false;

	@Autowired
	protected ProductDictionaryService productDictionaryService;

	@Autowired
	@Qualifier("mlAwareNodeService")
	private NodeService mlNodeService;

	@Autowired
	protected AlfrescoRepository<ProductData> alfrescoRepository;
	
	@Autowired
	protected PackagingHelper packagingHelper;

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
		loadDataLists(entityNodeRef, dataListsElt, images, true);
	}

	/**
	 * 
	 * @param entityNodeRef
	 * @param dataListsElt
	 * @param images
	 * @param isExtractedProduct
	 *            extracted product (more info)
	 */
	@SuppressWarnings("unchecked")
	private void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, Map<String, byte[]> images, boolean isExtractedProduct) {

		RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
		Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);

		// TODO make it more generic!!!!
		ProductData productData = alfrescoRepository.findOne(entityNodeRef);
		NodeRef defaultVariantNodeRef = loadVariants(productData, dataListsElt.getParent());

		if (datalists != null && !datalists.isEmpty()) {

			for (QName dataListQName : datalists.keySet()) {

				if (alfrescoRepository.hasDataList(entityNodeRef, dataListQName)) {
					if ((isExtractedProduct && !DATALIST_SPECIFIC_EXTRACTOR.contains(dataListQName)) || !isExtractedProduct
							&& RAWMATERIAL_DATALIST.contains(dataListQName)) {
						Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName() + "s");
						
						@SuppressWarnings({ "rawtypes" })
						List<BeCPGDataObject> dataListItems = (List) datalists.get(dataListQName);

						for (BeCPGDataObject dataListItem : dataListItems) {

							Element nodeElt = dataListElt.addElement(dataListQName.getLocalName());

							if (dataListItem instanceof CompositionDataItem) {
								CompositionDataItem compositionDataItem = (CompositionDataItem) dataListItem;
								loadProductData(compositionDataItem.getComponent(), nodeElt);
							}

							loadDataListItemAttributes(dataListItem, nodeElt);

							if (dataListItem instanceof AbstractManualVariantListDataItem) {
								extractVariants(((AbstractManualVariantListDataItem) dataListItem).getVariants(), nodeElt, defaultVariantNodeRef);
							}
						}
					}
				}
			}
		}

		if (isExtractedProduct) {

			// allergen
			Element allergenListElt = (Element) dataListsElt.selectSingleNode(PLMModel.TYPE_ALLERGENLIST.getLocalName() + "s");
			if (allergenListElt != null) {

				List<AllergenListDataItem> allergenList = (List<AllergenListDataItem>) datalists.get(PLMModel.TYPE_ALLERGENLIST);
				String volAllergens = "";
				String inVolAllergens = "";

				for (AllergenListDataItem dataItem : allergenList) {

					String allergen = (String) nodeService.getProperty(dataItem.getAllergen(), BeCPGModel.PROP_LEGAL_NAME);

					if (allergen == null || allergen.isEmpty()) {
						allergen = (String) nodeService.getProperty(dataItem.getAllergen(), ContentModel.PROP_NAME);
					}

					// concat allergens
					if (dataItem.getVoluntary()) {
						if (volAllergens.isEmpty()) {
							volAllergens = allergen;
						} else {
							volAllergens += RepoConsts.LABEL_SEPARATOR + allergen;
						}
					} else if (dataItem.getInVoluntary()) {
						if (inVolAllergens.isEmpty()) {
							inVolAllergens = allergen;
						} else {
							inVolAllergens += RepoConsts.LABEL_SEPARATOR + allergen;
						}
					}
				}

				allergenListElt.addAttribute(PLMModel.PROP_ALLERGENLIST_VOLUNTARY.getLocalName(), volAllergens);
				allergenListElt.addAttribute(PLMModel.PROP_ALLERGENLIST_INVOLUNTARY.getLocalName(), inVolAllergens);
			}

			loadCompoList(productData, dataListsElt, defaultVariantNodeRef);

			// extract RawMaterials
			extractRawMaterials(productData, dataListsElt, images);

			// packList
			loadPackagingList(productData, dataListsElt, defaultVariantNodeRef, images);

			// processList

			loadProcessList(productData, dataListsElt, defaultVariantNodeRef);

			// IngLabelingList
			if (productData.getLabelingListView().getIngLabelingList() != null) {
				Element ingListElt = dataListsElt.addElement(PLMModel.TYPE_INGLABELINGLIST.getLocalName() + "s");

				for (IngLabelingListDataItem dataItem : productData.getLabelingListView().getIngLabelingList()) {

					MLText labelingText = dataItem.getManualValue();
					if (labelingText == null || labelingText.isEmpty()) {
						labelingText = dataItem.getValue();
					}

					if (labelingText != null) {
						List<String> locales = new ArrayList<>();
						for (Locale locale : labelingText.getLocales()) {

							logger.debug("ill, locale: " + locale);

							if (!locales.contains(locale.getLanguage())) {

								locales.add(locale.getLanguage());

								String grpName = "";
								if (dataItem.getGrp() != null) {
									MLText grpMLText = (MLText) mlNodeService.getProperty(dataItem.getGrp(), PLMModel.PROP_LABELING_RULE_LABEL);
									if (grpMLText != null && grpMLText.getValue(locale) != null && !grpMLText.getValue(locale).isEmpty()) {
										grpName = grpMLText.getValue(locale);
									} else {
										grpName = (String) nodeService.getProperty(dataItem.getGrp(), ContentModel.PROP_NAME);
									}
								}

								Element ingLabelingElt = ingListElt.addElement(PLMModel.TYPE_INGLABELINGLIST.getLocalName());
								ingLabelingElt.addAttribute(ATTR_LANGUAGE, locale.getDisplayLanguage());
								addCDATA(ingLabelingElt, PLMModel.ASSOC_ILL_GRP, grpName);
								addCDATA(ingLabelingElt, PLMModel.PROP_ILL_VALUE, dataItem.getValue() != null ? dataItem.getValue().getValue(locale)
										: VALUE_NULL);
								addCDATA(ingLabelingElt, PLMModel.PROP_ILL_MANUAL_VALUE, dataItem.getManualValue() != null ? dataItem
										.getManualValue().getValue(locale) : VALUE_NULL);

								if (logger.isDebugEnabled()) {
									logger.debug("ingLabelingElt: " + ingLabelingElt.asXML());
								}
							}
						}
					}
				}
			}

			// NutList
			loadNutLists(productData, dataListsElt);
			
			loadOrganoLists(productData, dataListsElt);
						
			// MicrobioList
			List<MicrobioListDataItem> microbioList = null;

			if (!productData.getMicrobioList().isEmpty()) {
				microbioList = productData.getMicrobioList();
			} else {
				List<NodeRef> productMicrobioCriteriaNodeRefs = associationService.getTargetAssocs(entityNodeRef,
						PLMModel.ASSOC_PRODUCT_MICROBIO_CRITERIA);
				if (!productMicrobioCriteriaNodeRefs.isEmpty()) {
					NodeRef productMicrobioCriteriaNodeRef = productMicrobioCriteriaNodeRefs.get(0);
					if (productMicrobioCriteriaNodeRef != null) {
						ProductData pmcData = alfrescoRepository.findOne(productMicrobioCriteriaNodeRef);
						microbioList = pmcData.getMicrobioList();
					}
				}
			}

			if (microbioList != null && !microbioList.isEmpty()) {
				Element organoListElt = dataListsElt.addElement(PLMModel.TYPE_MICROBIOLIST.getLocalName() + "s");
				for (MicrobioListDataItem dataItem : microbioList) {
					Element nodeElt = organoListElt.addElement(PLMModel.TYPE_MICROBIOLIST.getLocalName());
					loadDataListItemAttributes(dataItem, nodeElt);
				}
			}
		}
	}

	private void loadProcessList(ProductData productData, Element dataListsElt, NodeRef defaultVariantNodeRef) {
		if (productData.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			Element processListElt = dataListsElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName() + "s");

			for (ProcessListDataItem dataItem : productData.getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				loadProcessListItem(dataItem, processListElt, defaultVariantNodeRef, 1);
			}

			loadDynamicCharactList(productData.getProcessListView().getDynamicCharactList(), processListElt);
		}

	}

	private void loadProcessListItem(ProcessListDataItem dataItem, Element processListElt, NodeRef defaultVariantNodeRef, int level) {

		Element dataListsElt;

		Element partElt = processListElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName());
		loadProductData(dataItem.getProduct(), partElt);
		loadDataListItemAttributes(dataItem, partElt);
		if (dataItem.getResource() != null && nodeService.exists(dataItem.getResource())) {
			dataListsElt = loadResourceParams(dataItem.getResource(), partElt);

			if(dataListsElt!=null){
				ProductData productData = alfrescoRepository.findOne(dataItem.getResource());
				if (productData.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					Element subProcessListElt = dataListsElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName() + "s");
					for (ProcessListDataItem subDataItem : productData.getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
						loadProcessListItem(subDataItem, subProcessListElt, defaultVariantNodeRef, level + 1);
					}
				}
			}
		}
		extractVariants(dataItem.getVariants(), partElt, defaultVariantNodeRef);
		partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), "" + level);

	}

	private Element loadResourceParams(NodeRef entityNodeRef, Element partElt) {
		Element dataListsElt = null;
		ResourceProductData productData = (ResourceProductData) alfrescoRepository.findOne(entityNodeRef);

		if (productData.getResourceParamList() != null && !productData.getResourceParamList().isEmpty()) {

			dataListsElt = partElt.addElement(TAG_DATALISTS);
			Element resourceListsElt = dataListsElt.addElement(MPMModel.TYPE_RESOURCEPARAMLIST.getLocalName() + "s");

			for (ResourceParamListItem resourceParamListItem : productData.getResourceParamList()) {

				Element ressourceListElt = resourceListsElt.addElement(MPMModel.TYPE_RESOURCEPARAMLIST.getLocalName());

				loadDataListItemAttributes(resourceParamListItem, ressourceListElt);

			}
		}

		return dataListsElt;
	}

	private void loadNutLists(ProductData productData, Element dataListsElt) {
		

		if (productData.getNutList() != null && !productData.getNutList().isEmpty()) {

			
			Element nutListsElt = dataListsElt.addElement(PLMModel.TYPE_NUTLIST.getLocalName() + "s");

			for (NutListDataItem dataListItem : productData.getNutList()) {

				Element nutListElt = nutListsElt.addElement(PLMModel.TYPE_NUTLIST.getLocalName());

				loadDataListItemAttributes(dataListItem, nutListElt);
	
				String nut = nutListElt.valueOf("@" + PLMModel.ASSOC_NUTLIST_NUT.getLocalName());
				if (nut != null) {
					String value = nutListElt.valueOf("@" + PLMModel.PROP_NUTLIST_VALUE.getLocalName());
					if(value==null || value.isEmpty()){
						value = nutListElt.valueOf("@" + PLMModel.PROP_NUTLIST_FORMULATED_VALUE.getLocalName());
						nutListElt.addAttribute(PLMModel.PROP_NUTLIST_VALUE.getLocalName(), value);
						
					}
					
					nutListsElt.addAttribute(generateKeyAttribute(nut), value != null ? value : "");
					NodeRef nutNodeRef = dataListItem.getNut();
					addCDATA(nutListElt, ContentModel.PROP_DESCRIPTION, (String)nodeService.getProperty(nutNodeRef, ContentModel.PROP_DESCRIPTION));				   				   
				}
			}
		}
	}
	
	private void loadOrganoLists(ProductData productData, Element dataListsElt) {
		if (productData.getOrganoList() != null && !productData.getOrganoList().isEmpty()) {
			Element organoListsElt = dataListsElt.addElement(PLMModel.TYPE_ORGANOLIST.getLocalName() + "s");
			for (OrganoListDataItem dataListItem : productData.getOrganoList()) {
				Element organoListElt = organoListsElt.addElement(PLMModel.TYPE_ORGANOLIST.getLocalName());
				loadDataListItemAttributes(dataListItem, organoListElt);
			}
		}
	}

	private void extractRawMaterials(ProductData productData, Element dataListsElt, Map<String, byte[]> images) {

		Map<NodeRef, Double> rawMaterials = new HashMap<>();
		rawMaterials = getRawMaterials(productData, rawMaterials, FormulationHelper.getNetWeight(productData.getNodeRef(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT));
		Double totalQty = 0d;
		for (Double qty : rawMaterials.values()) {
			totalQty += qty;
		}

		// sort
		List<Map.Entry<NodeRef, Double>> sortedRawMaterials = new LinkedList<>(rawMaterials.entrySet());
		Collections.sort(sortedRawMaterials, new Comparator<Map.Entry<NodeRef, Double>>() {
			@Override
			public int compare(Map.Entry<NodeRef, Double> r1, Map.Entry<NodeRef, Double> r2) {
				// increase
				return r2.getValue().compareTo(r1.getValue());
			}
		});

		// render
		Element rawMaterialsElt = dataListsElt.addElement(PLMModel.TYPE_RAWMATERIAL.getLocalName() + "s");
		for (Map.Entry<NodeRef, Double> entry : sortedRawMaterials) {
			Element rawMaterialElt = rawMaterialsElt.addElement(PLMModel.TYPE_RAWMATERIAL.getLocalName());
			loadAttributes(entry.getKey(), rawMaterialElt, true, null);
			addCDATA(rawMaterialElt, PLMModel.PROP_COMPOLIST_QTY, toString(100 * entry.getValue() / totalQty));
			if(FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT) != 0d){
				Element cDATAElt = rawMaterialElt.addElement(ATTR_COMPOLIST_QTY_FOR_PRODUCT);
				cDATAElt.addCDATA(toString(100 * entry.getValue() / FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT)));				
			}
			Element rawMaterialDataListsElt = rawMaterialElt.addElement(TAG_DATALISTS);
			loadDataLists(entry.getKey(), rawMaterialDataListsElt, images, false);
		}
	}

	private Map<NodeRef, Double> getRawMaterials(ProductData productData, Map<NodeRef, Double> rawMaterials, Double parentQty) {

		for (CompoListDataItem compoList : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			NodeRef productNodeRef = compoList.getProduct();
			QName type = nodeService.getType(productNodeRef);
			Double qty = FormulationHelper.getQtyInKg(compoList);
			Double netWeight = FormulationHelper.getNetWeight(productData.getNodeRef(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT);
			if (logger.isDebugEnabled()) {
				logger.debug("Get rawMaterial " + nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME) + "qty: " + qty + " netWeight "
						+ netWeight + " parentQty " + parentQty);
			}
			if (qty != null && netWeight != 0d) {
				qty = parentQty * qty * FormulationHelper.getYield(compoList) / (100 * netWeight);

				if (type.isMatch(PLMModel.TYPE_RAWMATERIAL)) {
					Double rmQty = rawMaterials.get(productNodeRef);
					if (rmQty == null) {
						rmQty = 0d;
					}
					rmQty += qty;
					rawMaterials.put(productNodeRef, rmQty);
				} else if (type.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)) {
					continue;
				} else {
					getRawMaterials(alfrescoRepository.findOne(productNodeRef), rawMaterials, qty);
				}
			}
		}

		return rawMaterials;
	}

	@Override
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt) {

		if (assocDef != null && assocDef.getName() != null) {
			if (assocDef.getName().equals(PLMModel.ASSOC_PLANTS) || assocDef.getName().equals(PLMModel.ASSOC_STORAGE_CONDITIONS)
					|| assocDef.getName().equals(PLMModel.ASSOC_PRECAUTION_OF_USE)) {

				extractTargetAssoc(entityNodeRef, assocDef, entityElt);
				return true;
			}
			else if(assocDef.getName().equals(PLMModel.ASSOC_PRODUCT_SPECIFICATIONS)){
				extractProductSpecifications(entityNodeRef, entityElt);
				return true;
			}
		}

		return false;
	}

	private void loadPackagingList(ProductData productData, Element dataListsElt, NodeRef defaultVariantNodeRef, Map<String, byte[]> images) {

		if (productData.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			
			Element packagingListElt = dataListsElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName() + "s");

			for (PackagingListDataItem dataItem : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				loadPackagingItem(dataItem, packagingListElt, defaultVariantNodeRef, images);
			}

			if (extractInMultiLevel) {
				for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT) && extractInMultiLevel) {
						ProductData sfProductData = alfrescoRepository.findOne(dataItem.getProduct());
						if (sfProductData.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
							for (PackagingListDataItem subDataItem : sfProductData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
								loadPackagingItem(subDataItem, packagingListElt, defaultVariantNodeRef, images);
							}
						}
					}
				}
			}

			loadDynamicCharactList(productData.getPackagingListView().getDynamicCharactList(), packagingListElt);

			// display tare, net weight and gross weight
			BigDecimal tarePrimary = FormulationHelper.getTareInKg(productData.getTare(), productData.getTareUnit());
			if (tarePrimary == null) {
				tarePrimary = new BigDecimal(0d);
			}
			
			BigDecimal netWeightPrimary = new BigDecimal(FormulationHelper.getNetWeight(productData.getNodeRef(), nodeService,
					FormulationHelper.DEFAULT_NET_WEIGHT).toString());
			
			BigDecimal grossWeightPrimary = tarePrimary.add(netWeightPrimary);
			
			PackagingData packagingData = packagingHelper.getPackagingData(productData);
			for (Map.Entry<NodeRef, VariantPackagingData> kv : packagingData.getVariants().entrySet()) {
				VariantPackagingData variantPackagingData = kv.getValue();

				Element packgLevelMesuresElt = packagingListElt.addElement(TAG_PACKAGING_LEVEL_MEASURES);
				if (kv.getKey() != null) {
					packgLevelMesuresElt.addAttribute(ATTR_VARIANT_ID, (String) nodeService.getProperty(kv.getKey(), ContentModel.PROP_NAME));
					packgLevelMesuresElt.addAttribute(PLMModel.PROP_IS_DEFAULT_VARIANT.getLocalName(),
							Boolean.toString((Boolean) nodeService.getProperty(kv.getKey(), PLMModel.PROP_IS_DEFAULT_VARIANT)));
				} else {
					packgLevelMesuresElt.addAttribute(ATTR_VARIANT_ID, "");
					packgLevelMesuresElt.addAttribute(PLMModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), "true");
				}

				packgLevelMesuresElt.addAttribute(ATTR_PKG_TARE_LEVEL_1, toString(tarePrimary));
				packgLevelMesuresElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_1, toString(netWeightPrimary));
				packgLevelMesuresElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_1, toString(grossWeightPrimary));

				if (variantPackagingData.getProductPerBoxes() != null) {
					
					BigDecimal tareSecondary = tarePrimary.multiply(new BigDecimal(variantPackagingData.getProductPerBoxes())).add(
							variantPackagingData.getTareSecondary());
					
					BigDecimal netWeightSecondary = netWeightPrimary.multiply(new BigDecimal(variantPackagingData.getProductPerBoxes()));
					BigDecimal grossWeightSecondary = tareSecondary.add(netWeightSecondary);
				
					packgLevelMesuresElt.addAttribute(ATTR_PKG_TARE_LEVEL_2, toString(tareSecondary));
					packgLevelMesuresElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_2, toString(netWeightSecondary));
					packgLevelMesuresElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_2, toString(grossWeightSecondary));
					packgLevelMesuresElt.addAttribute(ATTR_NB_PRODUCTS_LEVEL_2, toString(variantPackagingData.getProductPerBoxes()));

					
					if (variantPackagingData.getBoxesPerPallet() != null) {

						BigDecimal tareTertiary = tareSecondary.multiply(new BigDecimal(variantPackagingData.getBoxesPerPallet())).add(
								variantPackagingData.getTareTertiary());
						BigDecimal netWeightTertiary = netWeightSecondary.multiply(new BigDecimal(variantPackagingData.getBoxesPerPallet()));
						packgLevelMesuresElt.addAttribute(ATTR_PKG_TARE_LEVEL_3, toString(tareTertiary));
						packgLevelMesuresElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_3, toString(netWeightTertiary));
						packgLevelMesuresElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_3, toString(tareTertiary.add(netWeightTertiary)));
						packgLevelMesuresElt.addAttribute(ATTR_NB_PRODUCTS_LEVEL_3, toString(variantPackagingData.getProductPerBoxes()
								* variantPackagingData.getBoxesPerPallet()));
					}
				}
			}
		}
	}

	private void loadCompoList(ProductData productData, Element dataListsElt, NodeRef defaultVariantNodeRef) {
		// compoList
		if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			Element compoListElt = dataListsElt.addElement(PLMModel.TYPE_COMPOLIST.getLocalName() + "s");

			for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {				
				loadCompoListItem(dataItem, compoListElt, defaultVariantNodeRef, 1, dataItem.getQty() != null ? dataItem.getQty() : 0d);
			}

			loadDynamicCharactList(productData.getCompoListView().getDynamicCharactList(), compoListElt);
		}

	}

	private void loadCompoListItem(CompoListDataItem dataItem, Element compoListElt, NodeRef defaultVariantNodeRef, int level, double compoListQty) {
		if (dataItem.getProduct() != null && nodeService.exists(dataItem.getProduct())) {
			Element dataListsElt = null;

			Element partElt = compoListElt.addElement(PLMModel.TYPE_COMPOLIST.getLocalName());
			loadProductData(dataItem.getProduct(), partElt);
			loadDataListItemAttributes(dataItem, partElt);
			partElt.addAttribute(ATTR_COMPOLIST_QTY_FOR_PRODUCT, Double.toString(compoListQty));
			if (level == 1) {
				dataListsElt = partElt.addElement(TAG_DATALISTS);
				ProductData productData = alfrescoRepository.findOne(dataItem.getProduct());
				loadNutLists(productData, dataListsElt);
				loadOrganoLists(productData, dataListsElt);
				extractVariants(dataItem.getVariants(), partElt, defaultVariantNodeRef);
			}
			Integer depthLevel = dataItem.getDepthLevel();
			if (depthLevel != null) {
				partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), "" + (depthLevel * level));
			}

			if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT) && extractInMultiLevel) {
				ProductData productData = alfrescoRepository.findOne(dataItem.getProduct());
				if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					if (dataListsElt == null) {
						dataListsElt = partElt.addElement(TAG_DATALISTS);
					}
					Element subCompoListElt = dataListsElt.addElement(PLMModel.TYPE_COMPOLIST.getLocalName() + "s");

					for (CompoListDataItem subDataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
						loadCompoListItem(subDataItem, 
								subCompoListElt, 
								defaultVariantNodeRef, 
								level + 1, 
								productData.getRecipeQtyUsed() != null && productData.getRecipeQtyUsed() != 0d && subDataItem.getQty() != null ? compoListQty * subDataItem.getQty() / productData.getRecipeQtyUsed() : 0d);
					}
				}
			}
		}
	}

	private void loadPackagingItem(PackagingListDataItem dataItem, Element packagingListElt,
			NodeRef defaultVariantNodeRef, Map<String, byte[]> images) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			loadPackagingKit(dataItem, packagingListElt, defaultVariantNodeRef);
			Element imgsElt = (Element) packagingListElt.getDocument().selectSingleNode(TAG_ENTITY + "/" + TAG_IMAGES);
			if (imgsElt != null) {
				extractEntityImages(dataItem.getProduct(), imgsElt, images);
			}
		} else {
			loadPackaging(dataItem, packagingListElt, defaultVariantNodeRef, dataItem.getVariants());
		}
	}

	private Element loadPackaging(PackagingListDataItem dataItem, Element packagingListElt,
			NodeRef defaultVariantNodeRef, List<NodeRef> currentVariants) {

		Element partElt = packagingListElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName());
		loadProductData(dataItem.getProduct(), partElt);
		loadDataListItemAttributes(dataItem, partElt);

		extractVariants(dataItem.getVariants(), partElt, defaultVariantNodeRef);

		// we want labeling template <labelingTemplate>...</labelingTemplate>
		if (nodeService.hasAspect(dataItem.getNodeRef(), PackModel.ASPECT_LABELING)) {
			extractTargetAssoc(dataItem.getNodeRef(), dictionaryService.getAssociation(PackModel.ASSOC_LABELING_TEMPLATE), partElt);
		}

		return partElt;
	}

	// manage 2 level depth
	private void loadPackagingKit(PackagingListDataItem dataItem, Element packagingListElt, NodeRef defaultVariantNodeRef) {

		Element packagingKitEl = loadPackaging(dataItem, packagingListElt, defaultVariantNodeRef, dataItem.getVariants());
		Element dataListsElt = packagingKitEl.addElement(TAG_DATALISTS);
		Element packagingKitListEl = dataListsElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName() + "s");
		ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());

		for (PackagingListDataItem p : packagingKitData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			loadPackaging(p, packagingKitListEl, defaultVariantNodeRef, dataItem.getVariants());
		}
	}

	private String toString(Integer value) {
		return value == null ? VALUE_NULL : Integer.toString(value);
	}

	private String toString(Double value) {

		return value == null ? VALUE_NULL : Double.toString(value);
	}

	private String toString(BigDecimal value) {

		return value == null ? VALUE_NULL : toString(value.doubleValue());
	}

	protected NodeRef loadVariants(ProductData productData, Element entityElt) {
		NodeRef defaultVariantNodeRef = null;
		if (productData.getVariants() != null) {
			Element variantsElt = entityElt.addElement(PLMModel.ASSOC_VARIANTS.getLocalName());
			for (VariantData variant : productData.getVariants()) {
				if (variant.getIsDefaultVariant()) {
					defaultVariantNodeRef = variant.getNodeRef();
				}

				Element variantElt = variantsElt.addElement(PLMModel.TYPE_VARIANT.getLocalName());
				variantElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), variant.getName());
				variantElt.addAttribute(PLMModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), Boolean.toString(variant.getIsDefaultVariant()));
			}
		}
		return defaultVariantNodeRef;
	}

	protected void extractVariants(List<NodeRef> variantNodeRefs, Element dataItemElt, NodeRef defaultVariantNodeRef) {

		if (variantNodeRefs != null && !variantNodeRefs.isEmpty()) {
			dataItemElt.addAttribute(PLMModel.PROP_IS_DEFAULT_VARIANT.getLocalName(),
					Boolean.toString(variantNodeRefs.contains(defaultVariantNodeRef)));
		} else {
			dataItemElt.addAttribute(PLMModel.PROP_VARIANTIDS.getLocalName(), "");
			dataItemElt.addAttribute(PLMModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), Boolean.TRUE.toString());
		}
	}

	protected void loadDynamicCharactList(List<DynamicCharactListItem> dynamicCharactList, Element dataListElt) {

		Element dynCharactListElt = dataListElt.addElement(PLMModel.TYPE_DYNAMICCHARACTLIST.getLocalName() + "s");
		for (DynamicCharactListItem dc : dynamicCharactList) {
			Element dynamicCharact = dynCharactListElt.addElement(PLMModel.TYPE_DYNAMICCHARACTLIST.getLocalName());
			dynamicCharact.addAttribute(PLMModel.PROP_DYNAMICCHARACT_TITLE.getLocalName(), dc.getTitle());
			dynamicCharact.addAttribute(PLMModel.PROP_DYNAMICCHARACT_VALUE.getLocalName(), dc.getValue() == null ? VALUE_NULL : JsonFormulaHelper
					.cleanCompareJSON(dc.getValue().toString()).toString());
		}
	}

	protected void loadProductData(NodeRef nodeRef, Element dataListItemElt) {
		if (nodeRef != null) {
			loadNodeAttributes(nodeRef, dataListItemElt, false);

			dataListItemElt.addAttribute(ATTR_ITEM_TYPE, nodeService.getType(nodeRef).toPrefixString(namespaceService));
			dataListItemElt.addAttribute(ATTR_ASPECTS, extractAspects(nodeRef));
		}
	}

	@Override
	protected boolean isMultiLinesAttribute(QName attribute) {
		if (attribute != null) {
			if (attribute.equals(PLMModel.PROP_INSTRUCTION)) {
				return true;
			}
			if (attribute.equals(PLMModel.PROP_PRODUCT_COMMENTS)) {
				return true;
			}
			if (attribute.equals(ContentModel.PROP_DESCRIPTION)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected QName getPropNameOfType(QName type) {
		if (type != null && type.equals(PLMModel.TYPE_CERTIFICATION)) {
			return ContentModel.PROP_TITLE;
		} else {
			return ContentModel.PROP_NAME;
		}
	}

	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return dictionaryService.isSubClass(type, PLMModel.TYPE_PRODUCT) ? EntityReportExtractorPriority.NORMAL : EntityReportExtractorPriority.NONE;
	}

	private void extractProductSpecifications(NodeRef productNodeRef, Element productElt){		
		Element productSpecificationsElt = productElt.addElement(PLMModel.ASSOC_PRODUCT_SPECIFICATIONS.getLocalName());
		List<NodeRef> nodeRefs = associationService.getTargetAssocs(productNodeRef, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);		
		for(NodeRef nodeRef : nodeRefs){
			Element productSpecificationElt = productSpecificationsElt.addElement(PLMModel.TYPE_PRODUCT_SPECIFICATION.getLocalName());
			loadNodeAttributes(nodeRef, productSpecificationElt, true);
			Element dataListsElt = productSpecificationElt.addElement(TAG_DATALISTS);
			loadDataLists(nodeRef, dataListsElt, new HashMap<String, byte[]>());
		}
	}
}