package fr.becpg.repo.product.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.packaging.PackagingData;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.AbstractManualVariantListDataItem;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
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

	protected static final String KEY_PRODUCT_IMAGE = "productImage";

	protected static final List<QName> DATALIST_SPECIFIC_EXTRACTOR = Arrays.asList(PLMModel.TYPE_COMPOLIST, PLMModel.TYPE_PACKAGINGLIST,
			MPMModel.TYPE_PROCESSLIST, PLMModel.TYPE_MICROBIOLIST, PLMModel.TYPE_INGLABELINGLIST, PLMModel.TYPE_NUTLIST, PLMModel.TYPE_ORGANOLIST, 
			PLMModel.TYPE_INGLIST, PLMModel.TYPE_FORBIDDENINGLIST, PLMModel.TYPE_LABELING_RULE_LIST);

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
	private static final String ATTR_PACKAGING_QTY_FOR_PRODUCT = "packagingListQtyForProduct";
	private static final String ATTR_PROCESS_QTY_FOR_PRODUCT = "processListQtyForProduct";
	private static final String TAG_PACKAGING_LEVEL_MEASURES = "packagingLevelMeasures";
	private static final String ATTR_NODEREF = "nodeRef";
	private static final String ATTR_PARENT_NODEREF = "parentNodeRef";

	private static final String ATTR_ALLERGENLIST_INVOLUNTARY_FROM_PROCESS = "allergenListInVoluntaryFromProcess";
	private static final String ATTR_ALLERGENLIST_INVOLUNTARY_FROM_RAW_MATERIAL = "allergenListInVoluntaryFromRawMaterial";
	
	

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

		if ((datalists != null) && !datalists.isEmpty()) {

			for (QName dataListQName : datalists.keySet()) {

				if (alfrescoRepository.hasDataList(entityNodeRef, dataListQName)) {
					if ((isExtractedProduct && !DATALIST_SPECIFIC_EXTRACTOR.contains(dataListQName))) {
						Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName() + "s");

						@SuppressWarnings({ "rawtypes" })
						List<BeCPGDataObject> dataListItems = (List) datalists.get(dataListQName);

						for (BeCPGDataObject dataListItem : dataListItems) {

							Element nodeElt = dataListElt.addElement(dataListQName.getLocalName());

							if (dataListItem instanceof CompositionDataItem) {
								CompositionDataItem compositionDataItem = (CompositionDataItem) dataListItem;
								loadProductData(compositionDataItem.getComponent(), nodeElt, images);
							}
							
							if (dataListItem instanceof AllergenListDataItem) {
								String allergenType = (String) nodeService.getProperty(((AllergenListDataItem) dataListItem).getAllergen(), PLMModel.PROP_ALLERGEN_TYPE);
								if (allergenType != null) {
									nodeElt.addAttribute("allergenType", allergenType);
								}
							}

							loadDataListItemAttributes(dataListItem, nodeElt, images);

							if (dataListItem instanceof AbstractManualVariantListDataItem) {
								extractVariants(((AbstractManualVariantListDataItem) dataListItem).getVariants(), nodeElt, defaultVariantNodeRef);
							}
						}
					}
				}
			}
		}
		// lists extracted on entity and raw materials
		loadOrganoLists(productData, dataListsElt, images);		
		loadIngLists(productData, dataListsElt, images);

		if (isExtractedProduct) {

			// allergen
			Element allergenListElt = (Element) dataListsElt.selectSingleNode(PLMModel.TYPE_ALLERGENLIST.getLocalName() + "s");
			if (allergenListElt != null) {

				List<AllergenListDataItem> allergenList = (List<AllergenListDataItem>) datalists.get(PLMModel.TYPE_ALLERGENLIST);
				String volAllergens = "";
				String inVolAllergens = "";
				String inVolAllergensProcess = "";
				String inVolAllergensRawMaterial = "";

				for (AllergenListDataItem dataItem : allergenList) {

					// #1815: takes in account major
					String allergenType = (String) nodeService.getProperty(dataItem.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE);
					if ((allergenType != null) && allergenType.equals("Major")) {
						String allergen = (String) nodeService.getProperty(dataItem.getAllergen(), BeCPGModel.PROP_LEGAL_NAME);

						if ((allergen == null) || allergen.isEmpty()) {
							allergen = (String) nodeService.getProperty(dataItem.getAllergen(), BeCPGModel.PROP_CHARACT_NAME);
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
							boolean presentInRawMaterial = false;
							boolean presentInProcess = false;
							for(NodeRef inVoluntarySource : dataItem.getInVoluntarySources()){
								QName inVoluntarySourceType = nodeService.getType(inVoluntarySource);
								
								if(!presentInRawMaterial && PLMModel.TYPE_RAWMATERIAL.equals(inVoluntarySourceType)){
									if (inVolAllergensRawMaterial.isEmpty()) {
										inVolAllergensRawMaterial = allergen;
									} else {
										inVolAllergensRawMaterial += RepoConsts.LABEL_SEPARATOR + allergen;
									}
									presentInRawMaterial = true;
								} else if(!presentInProcess && PLMModel.TYPE_RESOURCEPRODUCT.equals(inVoluntarySourceType)){
									if (inVolAllergensProcess.isEmpty()) {
										inVolAllergensProcess = allergen;
									} else {
										inVolAllergensProcess += RepoConsts.LABEL_SEPARATOR + allergen;
									}
									presentInProcess = true;
								}
							}

						}
					}
				}

				allergenListElt.addAttribute(PLMModel.PROP_ALLERGENLIST_VOLUNTARY.getLocalName(), volAllergens);
				allergenListElt.addAttribute(PLMModel.PROP_ALLERGENLIST_INVOLUNTARY.getLocalName(), inVolAllergens);
				allergenListElt.addAttribute(ATTR_ALLERGENLIST_INVOLUNTARY_FROM_PROCESS, inVolAllergensProcess);
				allergenListElt.addAttribute(ATTR_ALLERGENLIST_INVOLUNTARY_FROM_RAW_MATERIAL, inVolAllergensRawMaterial);

			}

			loadCompoList(productData, dataListsElt, defaultVariantNodeRef, images);

			// extract RawMaterials
			extractRawMaterials(productData, dataListsElt, images);

			// packList
			loadPackagingList(productData, dataListsElt, defaultVariantNodeRef, images);

			// processList

			loadProcessList(productData, dataListsElt, defaultVariantNodeRef, images);

			// IngLabelingList
			if (productData.getLabelingListView().getIngLabelingList() != null) {
				Element ingListElt = dataListsElt.addElement(PLMModel.TYPE_INGLABELINGLIST.getLocalName() + "s");

				for (IngLabelingListDataItem dataItem : productData.getLabelingListView().getIngLabelingList()) {

					MLText labelingText = dataItem.getValue();
			
					if (labelingText != null) {
						List<String> locales = new ArrayList<>();
						for (Locale locale : labelingText.getLocales()) {

							logger.debug("ill, locale: " + locale);

							if (!locales.contains(locale.getLanguage())) {

								locales.add(locale.getLanguage());

								String grpName = "";
								if (dataItem.getGrp() != null) {
									MLText grpMLText = (MLText) mlNodeService.getProperty(dataItem.getGrp(), PLMModel.PROP_LABELING_RULE_LABEL);
									if ((grpMLText != null) && (grpMLText.getValue(locale) != null) && !grpMLText.getValue(locale).isEmpty()) {
										grpName = grpMLText.getValue(locale);
									} else {
										grpName = (String) nodeService.getProperty(dataItem.getGrp(), ContentModel.PROP_NAME);
									}
								}

								Element ingLabelingElt = ingListElt.addElement(PLMModel.TYPE_INGLABELINGLIST.getLocalName());
								ingLabelingElt.addAttribute(ATTR_LANGUAGE, locale.getDisplayLanguage());
								addCDATA(ingLabelingElt, PLMModel.ASSOC_ILL_GRP, grpName,null);
								addCDATA(ingLabelingElt, PLMModel.PROP_ILL_VALUE,
										dataItem.getValue() != null ? dataItem.getValue().getValue(locale) : VALUE_NULL,null);
								addCDATA(ingLabelingElt, PLMModel.PROP_ILL_MANUAL_VALUE,
										dataItem.getManualValue() != null ? dataItem.getManualValue().getValue(locale) : VALUE_NULL,null);

								if (logger.isDebugEnabled()) {
									logger.debug("ingLabelingElt: " + ingLabelingElt.asXML());
								}
							}
						}
					}
				}
			}

			// NutList
			loadNutLists(productData, dataListsElt, images);			

			// MicrobioList
			List<MicrobioListDataItem> microbioList = null;
			NodeRef productMicrobioCriteriaNodeRef = null;

			if (!productData.getMicrobioList().isEmpty()) {
				microbioList = productData.getMicrobioList();
			} else {
				List<NodeRef> productMicrobioCriteriaNodeRefs = associationService.getTargetAssocs(entityNodeRef,
						PLMModel.ASSOC_PRODUCT_MICROBIO_CRITERIA);
				if (!productMicrobioCriteriaNodeRefs.isEmpty()) {
					productMicrobioCriteriaNodeRef = productMicrobioCriteriaNodeRefs.get(0);
					if (productMicrobioCriteriaNodeRef != null) {
						ProductData pmcData = alfrescoRepository.findOne(productMicrobioCriteriaNodeRef);						
						microbioList = pmcData.getMicrobioList();
					}
				}
			}

			if ((microbioList != null) && !microbioList.isEmpty()) {
				Element microbioListElt = dataListsElt.addElement(PLMModel.TYPE_MICROBIOLIST.getLocalName() + "s");
				if(productMicrobioCriteriaNodeRef != null){
					loadNodeAttributes(productMicrobioCriteriaNodeRef, microbioListElt, false, images);
				}
				for (MicrobioListDataItem dataItem : microbioList) {
					Element nodeElt = microbioListElt.addElement(PLMModel.TYPE_MICROBIOLIST.getLocalName());
					loadDataListItemAttributes(dataItem, nodeElt, images);
				}
			}
		}
	}

	private void loadProcessList(ProductData productData, Element dataListsElt, NodeRef defaultVariantNodeRef, Map<String, byte[]> images) {
		if (productData.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			Element processListElt = dataListsElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName() + "s");

			for (ProcessListDataItem dataItem : productData.getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				loadProcessListItem(dataItem, processListElt, defaultVariantNodeRef, 1, null, images);
			}

			loadDynamicCharactList(productData.getProcessListView().getDynamicCharactList(), processListElt);
		}

	}

	private void loadProcessListItem(ProcessListDataItem dataItem, Element processListElt, NodeRef defaultVariantNodeRef, int level,
			Double parentRateProduct, Map<String, byte[]> images) {

		Element partElt = processListElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName());
		loadProductData(dataItem.getComponent(), partElt, images);
		loadDataListItemAttributes(dataItem, partElt, images);
		if (dataItem.getQtyResource() != null) {
			if ((dataItem.getRateProduct() != null) && (dataItem.getRateProduct() != 0)) {
				partElt.addAttribute(ATTR_PROCESS_QTY_FOR_PRODUCT, Double.toString(dataItem.getQtyResource() / dataItem.getRateProduct()));
			} else if ((parentRateProduct != null) && (parentRateProduct != 0)) {
				partElt.addAttribute(ATTR_PROCESS_QTY_FOR_PRODUCT, Double.toString(dataItem.getQtyResource() / parentRateProduct));
			}
		}
		if ((dataItem.getResource() != null) && nodeService.exists(dataItem.getResource())) {
			loadResourceParams(dataItem.getResource(), partElt, images);
			ProductData productData = alfrescoRepository.findOne(dataItem.getResource());
			if (productData.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				for (ProcessListDataItem subDataItem : productData.getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					loadProcessListItem(subDataItem, processListElt, defaultVariantNodeRef, level + 1, dataItem.getRateProduct(), images);
				}
			}
		}
		extractVariants(dataItem.getVariants(), partElt, defaultVariantNodeRef);
		partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), "" + level);

	}

	private void loadResourceParams(NodeRef entityNodeRef, Element partElt, Map<String, byte[]> images) {
		ResourceProductData productData = (ResourceProductData) alfrescoRepository.findOne(entityNodeRef);

		if ((productData.getResourceParamList() != null) && !productData.getResourceParamList().isEmpty()) {

			Element dataListsElt = partElt.addElement(TAG_DATALISTS);
			Element resourceListsElt = dataListsElt.addElement(MPMModel.TYPE_RESOURCEPARAMLIST.getLocalName() + "s");

			for (ResourceParamListItem resourceParamListItem : productData.getResourceParamList()) {

				Element ressourceListElt = resourceListsElt.addElement(MPMModel.TYPE_RESOURCEPARAMLIST.getLocalName());

				loadDataListItemAttributes(resourceParamListItem, ressourceListElt, images);

			}
		}
	}

	private void loadNutLists(ProductData productData, Element dataListsElt, Map<String, byte[]> images) {

		if ((productData.getNutList() != null) && !productData.getNutList().isEmpty()) {

			Element nutListsElt = dataListsElt.addElement(PLMModel.TYPE_NUTLIST.getLocalName() + "s");

			for (NutListDataItem dataListItem : productData.getNutList()) {

				Element nutListElt = nutListsElt.addElement(PLMModel.TYPE_NUTLIST.getLocalName());

				loadDataListItemAttributes(dataListItem, nutListElt, images);

				String nut = nutListElt.valueOf("@" + PLMModel.ASSOC_NUTLIST_NUT.getLocalName());
				if (nut != null) {
					String value = nutListElt.valueOf("@" + PLMModel.PROP_NUTLIST_VALUE.getLocalName());
					if ((value == null) || value.isEmpty()) {
						value = nutListElt.valueOf("@" + PLMModel.PROP_NUTLIST_FORMULATED_VALUE.getLocalName());
						nutListElt.addAttribute(PLMModel.PROP_NUTLIST_VALUE.getLocalName(), value);

					}

					nutListsElt.addAttribute(generateKeyAttribute(nut), value != null ? value : "");
					NodeRef nutNodeRef = dataListItem.getNut();
					addCDATA(nutListElt, ContentModel.PROP_DESCRIPTION, (String) nodeService.getProperty(nutNodeRef, ContentModel.PROP_DESCRIPTION),null);
					addCDATA(nutListElt, PLMModel.PROP_NUTGDA, nodeService.getProperty(nutNodeRef, PLMModel.PROP_NUTGDA)!=null ?
							((Double) nodeService.getProperty(nutNodeRef, PLMModel.PROP_NUTGDA)).toString(): "",null);
				}
			}
		}
	}

	private void loadOrganoLists(ProductData productData, Element dataListsElt, Map<String, byte[]> images) {
		if ((productData.getOrganoList() != null) && !productData.getOrganoList().isEmpty()) {
			Element organoListsElt = dataListsElt.addElement(PLMModel.TYPE_ORGANOLIST.getLocalName() + "s");
			for (OrganoListDataItem dataListItem : productData.getOrganoList()) {
				Element organoListElt = organoListsElt.addElement(PLMModel.TYPE_ORGANOLIST.getLocalName());
				loadDataListItemAttributes(dataListItem, organoListElt, images);
			}
		}
	}
	
	private void loadIngLists(ProductData productData, Element dataListsElt, Map<String, byte[]> images) {
		if ((productData.getIngList() != null) && !productData.getIngList().isEmpty()) {
			Element ingListsElt = dataListsElt.addElement(PLMModel.TYPE_INGLIST.getLocalName() + "s");
			for (IngListDataItem dataListItem : productData.getIngList()) {
				Element ingListElt = ingListsElt.addElement(PLMModel.TYPE_INGLIST.getLocalName());
				String ingCEECode = (String) nodeService.getProperty(dataListItem.getIng(), PLMModel.PROP_ING_CEECODE);
				if (ingCEECode != null) {
					ingListElt.addAttribute(PLMModel.PROP_ING_CEECODE.getLocalName(), ingCEECode);
				}
				loadDataListItemAttributes(dataListItem, ingListElt, images);
			}
		}
	}

	private void extractRawMaterials(ProductData productData, Element dataListsElt, Map<String, byte[]> images) {

		Map<NodeRef, Double> rawMaterials = new HashMap<>();
		rawMaterials = getRawMaterials(productData, rawMaterials,
				FormulationHelper.getNetWeight(productData.getNodeRef(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT));
		Double totalQty = 0d;
		for (Double qty : rawMaterials.values()) {
			totalQty += qty;
		}

		// sort
		List<Map.Entry<NodeRef, Double>> sortedRawMaterials = new LinkedList<>(rawMaterials.entrySet());
		Collections.sort(sortedRawMaterials, (r1, r2) -> r2.getValue().compareTo(r1.getValue()));

		// render
		Element rawMaterialsElt = dataListsElt.addElement(PLMModel.TYPE_RAWMATERIAL.getLocalName() + "s");
		for (Map.Entry<NodeRef, Double> entry : sortedRawMaterials) {
			Element rawMaterialElt = rawMaterialsElt.addElement(PLMModel.TYPE_RAWMATERIAL.getLocalName());
			loadAttributes(entry.getKey(), rawMaterialElt, true, null, images);
			addCDATA(rawMaterialElt, PLMModel.PROP_COMPOLIST_QTY, toString((100 * entry.getValue()) / totalQty),null);
			if (FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT) != 0d) {
				Element cDATAElt = rawMaterialElt.addElement(ATTR_COMPOLIST_QTY_FOR_PRODUCT);
				cDATAElt.addCDATA(
						toString((100 * entry.getValue()) / FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT)));
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
			if ((qty != null) && (netWeight != 0d)) {
				qty = (parentQty * qty * FormulationHelper.getYield(compoList)) / (100 * netWeight);

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
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt, Map<String, byte[]> images) {

		if ((assocDef != null) && (assocDef.getName() != null)) {
			if (assocDef.getName().equals(PLMModel.ASSOC_PLANTS) || assocDef.getName().equals(PLMModel.ASSOC_STORAGE_CONDITIONS)
					|| assocDef.getName().equals(PLMModel.ASSOC_PRECAUTION_OF_USE) || assocDef.getName().equals(PLMModel.ASSOC_SUPPLIERS)) {

				extractTargetAssoc(entityNodeRef, assocDef, entityElt, images);
				return true;
			} else if (assocDef.getName().equals(PLMModel.ASSOC_PRODUCT_SPECIFICATIONS)) {
				extractProductSpecifications(entityNodeRef, entityElt, images);
				return true;
			} else if (assocDef.getName().equals(PLMModel.ASSOC_CLIENTS)) {

				List<NodeRef> nodeRefs = associationService.getTargetAssocs(entityNodeRef, assocDef.getName());

				for (NodeRef nodeRef : nodeRefs) {
					Element imgsElt = (Element) entityElt.getDocument().selectSingleNode(TAG_ENTITY + "/" + TAG_IMAGES);
					extractEntityImages(nodeRef, imgsElt, images);
				}

			}
		}

		return false;
	}

	private void loadPackagingList(ProductData productData, Element dataListsElt, NodeRef defaultVariantNodeRef, Map<String, byte[]> images) {

		if (productData.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

			Element packagingListElt = dataListsElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName() + "s");

			// display tare, net weight and gross weight
			BigDecimal tarePrimary = FormulationHelper.getTareInKg(productData.getTare(), productData.getTareUnit());
			if (tarePrimary == null) {
				tarePrimary = new BigDecimal(0d);
			}

			BigDecimal netWeightPrimary = new BigDecimal(
					FormulationHelper.getNetWeight(productData.getNodeRef(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT).toString());

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

					BigDecimal tareSecondary = tarePrimary.multiply(new BigDecimal(variantPackagingData.getProductPerBoxes()))
							.add(variantPackagingData.getTareSecondary());

					BigDecimal netWeightSecondary = netWeightPrimary.multiply(new BigDecimal(variantPackagingData.getProductPerBoxes()));
					BigDecimal grossWeightSecondary = tareSecondary.add(netWeightSecondary);

					packgLevelMesuresElt.addAttribute(ATTR_PKG_TARE_LEVEL_2, toString(tareSecondary));
					packgLevelMesuresElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_2, toString(netWeightSecondary));
					packgLevelMesuresElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_2, toString(grossWeightSecondary));
					packgLevelMesuresElt.addAttribute(ATTR_NB_PRODUCTS_LEVEL_2, toString(variantPackagingData.getProductPerBoxes()));

					if (variantPackagingData.getBoxesPerPallet() != null) {

						BigDecimal tareTertiary = tareSecondary.multiply(new BigDecimal(variantPackagingData.getBoxesPerPallet()))
								.add(variantPackagingData.getTareTertiary());
						BigDecimal netWeightTertiary = netWeightSecondary.multiply(new BigDecimal(variantPackagingData.getBoxesPerPallet()));
						packgLevelMesuresElt.addAttribute(ATTR_PKG_TARE_LEVEL_3, toString(tareTertiary));
						packgLevelMesuresElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_3, toString(netWeightTertiary));
						packgLevelMesuresElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_3, toString(tareTertiary.add(netWeightTertiary)));
						packgLevelMesuresElt.addAttribute(ATTR_NB_PRODUCTS_LEVEL_3,
								toString(variantPackagingData.getProductPerBoxes() * variantPackagingData.getBoxesPerPallet()));
					}
				}
			}

			VariantPackagingData defaultVariantPackagingData = packagingData.getVariants().get(defaultVariantNodeRef);
			for (PackagingListDataItem dataItem : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				loadPackagingItem(1d, dataItem, packagingListElt, defaultVariantNodeRef, defaultVariantPackagingData, images, 1);
			}

			if (extractInMultiLevel) {
				for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					if ((nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
							|| nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT) ) && extractInMultiLevel) {
						ProductData sfProductData = alfrescoRepository.findOne(dataItem.getProduct());
						if (sfProductData.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
							for (PackagingListDataItem subDataItem : sfProductData
									.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
								Double sfQty = 1d;
								// multiply by qty of compoList
								if(dataItem.getCompoListUnit() != null && dataItem.getCompoListUnit().equals(CompoListUnit.P) &&
										dataItem.getQtySubFormula() != null){
									sfQty = dataItem.getQtySubFormula();									
								}
								loadPackagingItem(sfQty, subDataItem, packagingListElt, defaultVariantNodeRef, defaultVariantPackagingData, images, 1);
							}
						}
					}
				}
			}

			loadDynamicCharactList(productData.getPackagingListView().getDynamicCharactList(), packagingListElt);
		}
	}

	private void loadCompoList(ProductData productData, Element dataListsElt, NodeRef defaultVariantNodeRef, Map<String, byte[]> images) {
		// compoList
		if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			Element compoListElt = dataListsElt.addElement(PLMModel.TYPE_COMPOLIST.getLocalName() + "s");

			for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				loadCompoListItem(null, dataItem, compoListElt, defaultVariantNodeRef, 0, dataItem.getQty() != null ? dataItem.getQty() : 0d, images);
			}

			loadDynamicCharactList(productData.getCompoListView().getDynamicCharactList(), compoListElt);
			loadReqCtrlList(productData.getCompoListView().getReqCtrlList(), compoListElt);
		}

	}

	private void loadCompoListItem(CompoListDataItem parentDataItem, CompoListDataItem dataItem, Element compoListElt, NodeRef defaultVariantNodeRef, int level, double compoListQty,
			Map<String, byte[]> images) {
		if ((dataItem.getProduct() != null) && nodeService.exists(dataItem.getProduct())) {
			Element dataListsElt = null;

			Element partElt = compoListElt.addElement(PLMModel.TYPE_COMPOLIST.getLocalName());
			loadProductData(dataItem.getComponent(), partElt, images);
			loadDataListItemAttributes(dataItem, partElt, images);
			partElt.addAttribute(ATTR_COMPOLIST_QTY_FOR_PRODUCT, Double.toString(compoListQty));
			dataListsElt = partElt.addElement(TAG_DATALISTS);
			ProductData productData = alfrescoRepository.findOne(dataItem.getProduct());
			loadNutLists(productData, dataListsElt, images);
			loadOrganoLists(productData, dataListsElt, images);
			loadDynamicCharactList(productData.getCompoListView().getDynamicCharactList(), dataListsElt);
			extractVariants(dataItem.getVariants(), partElt, defaultVariantNodeRef);
			
			Integer depthLevel = dataItem.getDepthLevel();
			if (depthLevel != null) {
				partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), "" + (depthLevel + level));
				partElt.addAttribute(ATTR_NODEREF, dataItem.getNodeRef().toString());
				if(parentDataItem != null){
					partElt.addAttribute(ATTR_PARENT_NODEREF, parentDataItem.getNodeRef().toString());
				}
			}

			if ((nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT) 
					|| nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))  && extractInMultiLevel) {
				if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					for (CompoListDataItem subDataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
						loadCompoListItem(dataItem, subDataItem, compoListElt, defaultVariantNodeRef, level + 1,
								(productData.getRecipeQtyUsed() != null) && (productData.getRecipeQtyUsed() != 0d) && (subDataItem.getQty() != null)
										? (compoListQty * subDataItem.getQty()) / productData.getRecipeQtyUsed() : 0d,
								images);
					}
				}
			}
		}
	}

	private void loadPackagingItem(double sfQty, PackagingListDataItem dataItem, Element packagingListElt, NodeRef defaultVariantNodeRef,
			VariantPackagingData defaultVariantPackagingData, Map<String, byte[]> images, int level) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			loadPackagingKit(sfQty, dataItem, packagingListElt, defaultVariantNodeRef, defaultVariantPackagingData, images, level);
			Element imgsElt = (Element) packagingListElt.getDocument().selectSingleNode(TAG_ENTITY + "/" + TAG_IMAGES);
			if (imgsElt != null) {
				extractEntityImages(dataItem.getProduct(), imgsElt, images);
			}
		} else {
			loadPackaging(sfQty, dataItem, packagingListElt, defaultVariantNodeRef, defaultVariantPackagingData, images, level);
		}
	}

	private Element loadPackaging(double sfQty, PackagingListDataItem dataItem, Element packagingListElt, NodeRef defaultVariantNodeRef,
			VariantPackagingData defaultVariantPackagingData, Map<String, byte[]> images, int level) {

		Element partElt = packagingListElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName());
		loadProductData(dataItem.getComponent(), partElt, images);
		loadDataListItemAttributes(dataItem, partElt, images);

		extractVariants(dataItem.getVariants(), partElt, defaultVariantNodeRef);

		// we want labeling template <labelingTemplate>...</labelingTemplate>
		if (nodeService.hasAspect(dataItem.getNodeRef(), PackModel.ASPECT_LABELING)) {
			extractTargetAssoc(dataItem.getNodeRef(), dictionaryService.getAssociation(PackModel.ASSOC_LABELING_TEMPLATE), partElt, images);
		}

		partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(level));
		if ((dataItem.getPkgLevel() != null) && (dataItem.getQty() != null) && (dataItem.getPackagingListUnit() != null)) {
			partElt.addAttribute(PLMModel.PROP_PACKAGINGLIST_QTY.getLocalName(), Double.toString(dataItem.getQty() * sfQty));
			double qty = dataItem.getPackagingListUnit().equals(PackagingListUnit.PP) ? 1 : dataItem.getQty();
			if (dataItem.getPkgLevel().equals(PackagingLevel.Primary)) {				
				partElt.addAttribute(ATTR_PACKAGING_QTY_FOR_PRODUCT, Double.toString(qty * sfQty));				
			} else if (dataItem.getPkgLevel().equals(PackagingLevel.Secondary)
					&& (defaultVariantPackagingData.getProductPerBoxes() != null) && (defaultVariantPackagingData.getProductPerBoxes() != 0)) {
				partElt.addAttribute(ATTR_PACKAGING_QTY_FOR_PRODUCT, Double.toString(qty / defaultVariantPackagingData.getProductPerBoxes()));
			} else if (dataItem.getPkgLevel().equals(PackagingLevel.Tertiary)
					&& (defaultVariantPackagingData.getProductPerPallet() != null) && (defaultVariantPackagingData.getProductPerPallet() != 0)) {
				partElt.addAttribute(ATTR_PACKAGING_QTY_FOR_PRODUCT, Double.toString(qty / defaultVariantPackagingData.getProductPerPallet()));
			}
		}
		return partElt;
	}

	private void loadPackagingKit(double sfQty, PackagingListDataItem dataItem, Element packagingListElt, NodeRef defaultVariantNodeRef,
			VariantPackagingData defaultVariantPackagingData, Map<String, byte[]> images, int level) {
		loadPackaging(sfQty, dataItem, packagingListElt, defaultVariantNodeRef, defaultVariantPackagingData, images, level);
		ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());

		for (PackagingListDataItem p : packagingKitData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			loadPackagingItem(sfQty, p, packagingListElt, defaultVariantNodeRef, defaultVariantPackagingData, images, level + 1);
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

		if ((variantNodeRefs != null) && !variantNodeRefs.isEmpty()) {
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
			dynamicCharact.addAttribute(PLMModel.PROP_DYNAMICCHARACT_VALUE.getLocalName(),
					dc.getValue() == null ? VALUE_NULL : JsonFormulaHelper.cleanCompareJSON(dc.getValue().toString()).toString());
		}
	}
	
	protected void loadReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList, Element dataListElt) {

		Element reqCtrlListsElt = dataListElt.addElement(PLMModel.TYPE_REQCTRLLIST.getLocalName() + "s");
		for (ReqCtrlListDataItem r : reqCtrlList) {
			Element reqCtrlListElt = reqCtrlListsElt.addElement(PLMModel.TYPE_REQCTRLLIST.getLocalName());
			reqCtrlListElt.addAttribute(PLMModel.PROP_RCL_REQ_MESSAGE.getLocalName(), r.getReqMessage());
			if(r.getReqType()!=null){
				reqCtrlListElt.addAttribute(PLMModel.PROP_RCL_REQ_TYPE.getLocalName(), r.getReqType().toString());
			}			
		}
	}

	protected void loadProductData(NodeRef nodeRef, Element dataListItemElt, Map<String, byte[]> images) {
		if (nodeRef != null) {
			loadNodeAttributes(nodeRef, dataListItemElt, false, images);

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
		if ((type != null) && type.equals(PLMModel.TYPE_CERTIFICATION)) {
			return ContentModel.PROP_TITLE;
		}
		else if(dictionaryService.isSubClass(type, PLMModel.TYPE_PRODUCT)){
			return ContentModel.PROP_NAME;
		}
		return null;

	}

	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return dictionaryService.isSubClass(type, PLMModel.TYPE_PRODUCT) ? EntityReportExtractorPriority.NORMAL : EntityReportExtractorPriority.NONE;
	}

	private void extractProductSpecifications(NodeRef productNodeRef, Element productElt, Map<String, byte[]> images) {
		Element productSpecificationsElt = productElt.addElement(PLMModel.ASSOC_PRODUCT_SPECIFICATIONS.getLocalName());
		List<NodeRef> nodeRefs = associationService.getTargetAssocs(productNodeRef, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
		for (NodeRef nodeRef : nodeRefs) {
			Element productSpecificationElt = productSpecificationsElt.addElement(PLMModel.TYPE_PRODUCT_SPECIFICATION.getLocalName());
			loadNodeAttributes(nodeRef, productSpecificationElt, true, images);
			Element dataListsElt = productSpecificationElt.addElement(TAG_DATALISTS);
			loadDataLists(nodeRef, dataListsElt, new HashMap<String, byte[]>());
		}
	}
}