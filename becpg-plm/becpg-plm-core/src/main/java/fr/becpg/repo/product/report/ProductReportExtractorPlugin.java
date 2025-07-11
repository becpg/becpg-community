package fr.becpg.repo.product.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.formulation.ReportableError;
import fr.becpg.repo.formulation.ReportableError.ReportableErrorType;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.CurrentLevelQuantities;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.CostType;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.packaging.PackagingData;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.AbstractEffectiveVariantListDataItem;
import fr.becpg.repo.product.data.productList.AbstractManualVariantListDataItem;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamListItem;
import fr.becpg.repo.product.formulation.CostCalculatingHelper;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.PackagingHelper;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;
import fr.becpg.repo.product.helper.AllocationHelper;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.report.entity.EntityReportParameters;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor;
import fr.becpg.repo.report.entity.impl.DefaultExtractorContext;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>ProductReportExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@SuppressWarnings("deprecation")
@Service("productReportExtractor")
public class ProductReportExtractorPlugin extends DefaultEntityReportExtractor {

	/** Constant <code>KEY_PRODUCT_IMAGE="productImage"</code> */
	protected static final String KEY_PRODUCT_IMAGE = "productImage";

	/** Constant <code>DATALIST_SPECIFIC_EXTRACTOR</code> */
	protected static final List<QName> DATALIST_SPECIFIC_EXTRACTOR = Arrays.asList(PLMModel.TYPE_COMPOLIST, PLMModel.TYPE_PACKAGINGLIST,
			MPMModel.TYPE_PROCESSLIST, PLMModel.TYPE_MICROBIOLIST, PLMModel.TYPE_INGLABELINGLIST, PLMModel.TYPE_NUTLIST, PLMModel.TYPE_ORGANOLIST,
			PLMModel.TYPE_INGLIST, PLMModel.TYPE_FORBIDDENINGLIST, PLMModel.TYPE_LABELINGRULELIST, PLMModel.TYPE_REQCTRLLIST,
			PLMModel.TYPE_LABELCLAIMLIST, PLMModel.TYPE_ALLERGENLIST);

	private static final Log logger = LogFactory.getLog(ProductReportExtractorPlugin.class);

	/** Constant <code>ATTR_LANGUAGE="language"</code> */
	protected static final String ATTR_LANGUAGE = "language";
	/** Constant <code>ATTR_LANGUAGE_CODE="languageCode"</code> */
	protected static final String ATTR_LANGUAGE_CODE = "languageCode";
	/** Constant <code>ATTR_GROUP="group"</code> */
	protected static final String ATTR_GROUP = "group";

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
	private static final String ATTR_QTY_FOR_COST = "qtyForCost";

	private static final String TAG_PACKAGING_LEVEL_MEASURES = "packagingLevelMeasures";
	private static final String ATTR_NODEREF = "nodeRef";
	private static final String ATTR_PARENT_NODEREF = "parentNodeRef";

	private static final String ATTR_ALLERGENLIST_INVOLUNTARY_FROM_PROCESS = "allergenListInVoluntaryFromProcess";
	private static final String ATTR_ALLERGENLIST_INVOLUNTARY_FROM_RAW_MATERIAL = "allergenListInVoluntaryFromRawMaterial";

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	private Boolean extractInMultiLevel() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.product.report.multiLevel"));
	}

	private Boolean extractNonEffectiveComponent() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.product.report.nonEffectiveComponent"));
	}

	private String componentDatalistsToExtract() {
		return systemConfigurationService.confValue("beCPG.product.report.componentDatalistsToExtract");
	}

	private Boolean extractPriceBreaks() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.product.report.priceBreaks"));
	}

	private Boolean extractRawMaterial() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.product.report.extractRawMaterial"));
	}

	private Boolean showDeprecated() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.product.report.showDeprecatedXml"));
	}

	private String nutLocalesToExtract() {
		return systemConfigurationService.confValue("beCPG.product.report.nutList.localesToExtract");
	}

	@Autowired
	protected PackagingHelper packagingHelper;

	static {
		hiddenNodeAttributes.add(PLMModel.PROP_NUT_FORMULA);
		hiddenNodeAttributes.add(PLMModel.PROP_LABEL_CLAIM_FORMULA);

		hiddenDataListItemAttributes.add(PLMModel.PROP_LCL_FORMULAERROR);
		hiddenDataListItemAttributes.add(PLMModel.ASSOC_LCL_MISSING_LABELCLAIMS);
		hiddenDataListItemAttributes.add(PLMModel.PROP_PHYSICOCHEMFORMULA_ERROR);
		hiddenDataListItemAttributes.add(PLMModel.PROP_NUTLIST_FORMULA_ERROR);
		hiddenDataListItemAttributes.add(PLMModel.PROP_NUTLIST_ROUNDED_VALUE);
		hiddenDataListItemAttributes.add(PLMModel.PROP_NUTLIST_PREPARED_ROUNDED_VALUE);
	}

	/**
	 * {@inheritDoc}
	 *
	 * load the datalists of the product data.
	 */
	@Override
	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, DefaultExtractorContext context) {
		loadDataLists(entityNodeRef, dataListsElt, context, true, 1);
	}

	/**
	 *
	 * @param entityNodeRef
	 * @param dataListsElt
	 * @param images
	 * @param isExtractedProduct
	 *            extracted product (more info)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, DefaultExtractorContext context, boolean isExtractedProduct, int level) {

		RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);

		QName type = nodeService.getType(entityNodeRef);

		Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);

		NodeRef defaultVariantNodeRef = null;
		ProductData productData = null;

		if (entity instanceof ProductData) {
			productData = (ProductData) entity;
			defaultVariantNodeRef = loadVariants(productData, dataListsElt.getParent());
		}

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef != null) {
			List<NodeRef> listNodeRefs = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);

			for (NodeRef listNodeRef : listNodeRefs) {
				QName dataListQName = QName.createQName((String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE),
						namespaceService);

				if (!DATALIST_SPECIFIC_EXTRACTOR.contains(dataListQName)) {
					
					String dataListName = (String) nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME);

					if ((datalists != null) && datalists.containsKey(dataListQName)
							&& shouldExtractList(isExtractedProduct, context, type, dataListQName)) {

					
						List<BeCPGDataObject> dataListItems = (List) datalists.get(dataListQName);
						if (dataListName.contains("@")) {
							dataListItems = alfrescoRepository.loadDataList(listNodeRef, dataListQName);
						}

						if ((dataListItems != null) && !dataListItems.isEmpty()) {

							Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName() + "s");
							addDataListStateAndName(dataListElt, listNodeRef);

							for (BeCPGDataObject dataListItem : dataListItems) {
								Element nodeElt = dataListElt.addElement(dataListQName.getLocalName());

								if (dataListItem instanceof CompositionDataItem) {
									CompositionDataItem compositionDataItem = (CompositionDataItem) dataListItem;
									loadProductData(entityNodeRef, compositionDataItem.getComponent(), nodeElt, context, null);
								}

								loadDataListItemAttributes(dataListItem, nodeElt, context);

								if (dataListItem instanceof AbstractManualVariantListDataItem) {
									extractVariants(((AbstractManualVariantListDataItem) dataListItem).getVariants(), nodeElt);
								}
							}
						}
					} else if (!BeCPGModel.TYPE_ACTIVITY_LIST.equals(dataListQName)
							&& shouldExtractList(isExtractedProduct, context, type, dataListQName)) {

						if ( dataListName.startsWith(RepoConsts.SMART_CONTENT_PREFIX) && isExtractedProduct) {
							loadSmartContent(dataListsElt,entityNodeRef, listNodeRef, dataListQName, context);
						} else {
							// extract specific datalists
							loadDataList(dataListsElt, listNodeRef, dataListQName, context);
						}
					}
				}
			}
		}

		if (productData != null) {
			// lists extracted on entity and raw materials
			if (shouldExtractList(isExtractedProduct, context, type, PLMModel.TYPE_ORGANOLIST)) {
				loadOrganoLists(productData, dataListsElt, context);
			}

			if (shouldExtractList(isExtractedProduct, context, type, PLMModel.TYPE_LABELCLAIMLIST)) {
				loadLabelCLaimLists(productData, dataListsElt, context);
			}

			if (shouldExtractList(isExtractedProduct, context, type, PLMModel.TYPE_INGLIST)) {
				loadIngLists(productData, dataListsElt, context);
			}

			if (shouldExtractList(isExtractedProduct, context, type, PLMModel.TYPE_NUTLIST)) {
				loadNutLists(productData, dataListsElt, context);
			}
			
			if (!isExtractedProduct && shouldExtractList(isExtractedProduct, context, type, PLMModel.TYPE_DYNAMICCHARACTLIST)) {
				loadDynamicCharactList(productData.getCompoListView().getDynamicCharactList(), dataListsElt);
			}

			if (shouldExtractList(isExtractedProduct, context, type, PLMModel.TYPE_MICROBIOLIST)) {
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
							ProductData pmcData = (ProductData) alfrescoRepository.findOne(productMicrobioCriteriaNodeRef);
							microbioList = pmcData.getMicrobioList();
						}
					}
				}

				if ((microbioList != null) && !microbioList.isEmpty()) {
					Element microbioListsElt = dataListsElt.addElement(PLMModel.TYPE_MICROBIOLIST.getLocalName() + "s");
					addDataListStateAndName(microbioListsElt, microbioList.get(0).getParentNodeRef());
					if (productMicrobioCriteriaNodeRef != null) {
						loadNodeAttributes(productMicrobioCriteriaNodeRef, microbioListsElt, false, context);
					}
					for (MicrobioListDataItem dataItem : microbioList) {
						Element nodeElt = microbioListsElt.addElement(PLMModel.TYPE_MICROBIOLIST.getLocalName());
						loadDataListItemAttributes(dataItem, nodeElt, context);
					}
				}
			}

			if (shouldExtractList(isExtractedProduct, context, type, PLMModel.TYPE_ALLERGENLIST)) {

				loadAllergenLists(productData, dataListsElt, context);
			}

			if (context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_IN_MULTILEVEL, extractInMultiLevel())
					|| shouldExtractList(isExtractedProduct, context, type, PLMModel.TYPE_COMPOLIST)) {
				loadCompoList(productData, dataListsElt, context, level);
			}

			if (shouldExtractList(isExtractedProduct, context, type, PLMModel.TYPE_PACKAGINGLIST)) {

				// packList
				loadPackagingList(productData, dataListsElt, defaultVariantNodeRef, context, isExtractedProduct);

			}

			if (shouldExtractList(isExtractedProduct, context, type, MPMModel.TYPE_PROCESSLIST)) {

				// processList
				loadProcessList(productData, dataListsElt, context, isExtractedProduct);

			}

			if (isExtractedProduct && context.isPrefOn("extractPriceBreaks", extractPriceBreaks())) {

				extractPriceBreaks(productData, dataListsElt);
			}

			// extract RawMaterials
			if (isExtractedProduct && context.isPrefOn("extractRawMaterial", extractRawMaterial())) {

				extractRawMaterials(productData, dataListsElt, context);
			}

			if (shouldExtractList(isExtractedProduct, context, type, PLMModel.TYPE_INGLABELINGLIST)) {
				// IngLabelingList
				if ((productData.getLabelingListView().getIngLabelingList() != null)
						&& !productData.getLabelingListView().getIngLabelingList().isEmpty()) {
					Element ingListElt = dataListsElt.addElement(PLMModel.TYPE_INGLABELINGLIST.getLocalName() + "s");
					addDataListStateAndName(ingListElt, productData.getLabelingListView().getIngLabelingList().get(0).getParentNodeRef());
					for (IngLabelingListDataItem dataItem : productData.getLabelingListView().getIngLabelingList()) {

						MLText labelingText = dataItem.getValue();
						MLText manualLabelingText = dataItem.getManualValue();

						Set<Locale> locales = new HashSet<>();
						if (labelingText != null) {
							locales.addAll(labelingText.getLocales());
						}

						if (manualLabelingText != null) {
							locales.addAll(manualLabelingText.getLocales());
						}

						for (Locale locale : locales) {

							if (logger.isTraceEnabled()) {
								logger.trace("ill, locale: " + locale);
							}

							String grpName = "";
							String grpKey = "";
							if (dataItem.getGrp() != null) {
								grpKey =  (String) nodeService.getProperty(dataItem.getGrp(), ContentModel.PROP_NAME);
								MLText grpMLText = (MLText) mlNodeService.getProperty(dataItem.getGrp(), PLMModel.PROP_LABELINGRULELIST_LABEL);
								if ((grpMLText != null) && (grpMLText.getValue(locale) != null) && !grpMLText.getValue(locale).isEmpty()) {
									grpName = grpMLText.getValue(locale);
								} else {
									grpName = grpKey;
								}
							}

							Element ingLabelingElt = ingListElt.addElement(PLMModel.TYPE_INGLABELINGLIST.getLocalName());

							List<QName> excludesProp = new ArrayList<>();
							excludesProp.add(PLMModel.ASSOC_ILL_GRP);
							excludesProp.add(PLMModel.PROP_ILL_VALUE);
							excludesProp.add(PLMModel.PROP_ILL_MANUAL_VALUE);
							excludesProp.add(PLMModel.PROP_ILL_LOG_VALUE);

							loadDataListItemAttributes(dataItem, ingLabelingElt, context, excludesProp);

							ingLabelingElt.addAttribute(ATTR_LANGUAGE, locale.getDisplayLanguage());
							ingLabelingElt.addAttribute(ATTR_LANGUAGE_CODE, locale.toString());
							ingLabelingElt.addAttribute(ATTR_GROUP, grpKey);
							addCDATA(ingLabelingElt, PLMModel.ASSOC_ILL_GRP, grpName, null);
							// #4510
							Element cDATAElt = ingLabelingElt.addElement(PLMModel.PROP_ILL_VALUE.getLocalName());
							cDATAElt.addCDATA(dataItem.getValue() != null ? dataItem.getValue().getValue(locale) : VALUE_NULL);
							cDATAElt = ingLabelingElt.addElement(PLMModel.PROP_ILL_MANUAL_VALUE.getLocalName());
							cDATAElt.addCDATA(dataItem.getManualValue() != null ? dataItem.getManualValue().getValue(locale) : VALUE_NULL);

							if (logger.isTraceEnabled()) {
								logger.trace("ingLabelingElt: " + ingLabelingElt.asXML());
							}
						}
					}
				}

			}

		}

	}



	private boolean shouldExtractList(boolean isExtractedProduct, DefaultExtractorContext context, QName type, QName dataListQName) {
		if (isExtractedProduct) {
			return !(context.isNotEmptyPrefs(EntityReportParameters.PARAM_ENTITY_DATALISTS_TO_EXTRACT, null)
					&& !context.multiPrefsEquals(EntityReportParameters.PARAM_ENTITY_DATALISTS_TO_EXTRACT, null,
							entityDictionaryService.toPrefixString(dataListQName)));
		}

		return context.multiPrefsEquals(EntityReportParameters.PARAM_COMPONENT_DATALISTS_TO_EXTRACT, componentDatalistsToExtract(),
				entityDictionaryService.toPrefixString(dataListQName))
				|| context.multiPrefsEquals(EntityReportParameters.PARAM_COMPONENT_DATALISTS_TO_EXTRACT, componentDatalistsToExtract(),
						entityDictionaryService.toPrefixString(dataListQName) + "|" + entityDictionaryService.toPrefixString(type));
	}

	/**
	 * <p>isCharactDisplayedForLocale.</p>
	 *
	 * @param charact a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	public boolean isCharactDisplayedForLocale(NodeRef charact) {
		if (mlNodeService.hasAspect(charact, ReportModel.ASPECT_REPORT_LOCALES)) {
			@SuppressWarnings("unchecked")
			List<String> langs = (List<String>) nodeService.getProperty(charact, ReportModel.PROP_REPORT_LOCALES);
			if ((langs != null) && !langs.isEmpty() && !"".equals(langs.get(0))) {
				return MLTextHelper.extractLocales(langs).contains(I18NUtil.getLocale());
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected void loadDataListItemAttributes(BeCPGDataObject dataListItem, Element nodeElt, DefaultExtractorContext context,
			List<QName> hiddentAttributes) {
		loadDataListItemAttributes(dataListItem, nodeElt, context, hiddentAttributes, false);
	}

	private void loadCompoList(ProductData productData, Element dataListsElt, DefaultExtractorContext context, int level) {
		// compoList
		String filter = "";
		if (!context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_NON_EFFECTIVE_COMPONENT, extractNonEffectiveComponent())) {
			filter = EffectiveFilters.EFFECTIVE;
		}

		if (productData.hasCompoListEl(new EffectiveFilters<>(filter))) {
			Element compoListElt = dataListsElt.addElement(PLMModel.TYPE_COMPOLIST.getLocalName() + "s");
			addDataListStateAndName(compoListElt, productData.getCompoList().get(0).getParentNodeRef());

			for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(filter))) {
				if ((dataItem.getProduct() != null) && nodeService.exists(dataItem.getProduct())) {
					loadCompoListItem(productData.getNodeRef(), null, compoListElt, level,
							new CurrentLevelQuantities(alfrescoRepository, packagingHelper, productData, dataItem), context);
				}
			}

			loadDynamicCharactList(productData.getCompoListView().getDynamicCharactList(), compoListElt);
			loadReqCtrlList(context, productData.getReqCtrlList(), compoListElt);
		}

	}

	private void loadProcessList(ProductData productData, Element dataListsElt, DefaultExtractorContext context, boolean isExtractedProduct) {
		String filter = "";
		if (!context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_NON_EFFECTIVE_COMPONENT, extractNonEffectiveComponent())) {
			filter = EffectiveFilters.EFFECTIVE;
		}

		if (productData.hasProcessListEl(new EffectiveFilters<>(filter))) {
			Element processListElt = dataListsElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName() + "s");
			addDataListStateAndName(processListElt, productData.getProcessList().get(0).getParentNodeRef());

			for (ProcessListDataItem dataItem : productData.getProcessList(new EffectiveFilters<>(filter))) {
				loadProcessListItem(productData.getNodeRef(), new CurrentLevelQuantities(nodeService, alfrescoRepository, productData, dataItem),
						dataItem, processListElt, 1, context);
			}

			if (context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_IN_MULTILEVEL, extractInMultiLevel()) && isExtractedProduct) {

				if (productData.hasCompoListEl(new EffectiveFilters<>(filter))) {

					for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(filter))) {
						if ((dataItem.getProduct() != null) && nodeService.exists(dataItem.getProduct())) {

							if ((nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
									|| nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))) {

								loadProcessListItemForCompo(productData.getNodeRef(), processListElt, 1,
										new CurrentLevelQuantities(alfrescoRepository, packagingHelper, productData, dataItem), context);
							}
						}
					}
				}

			}

			loadDynamicCharactList(productData.getProcessListView().getDynamicCharactList(), processListElt);
		}

	}

	private void loadPackagingList(ProductData productData, Element dataListsElt, NodeRef defaultVariantNodeRef, DefaultExtractorContext context,
			boolean isExtractedProduct) {
		String filter = "";
		if (!context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_NON_EFFECTIVE_COMPONENT, extractNonEffectiveComponent())) {
			filter = EffectiveFilters.EFFECTIVE;
		}

		if (productData.hasPackagingListEl(new EffectiveFilters<>(filter))) {

			Element packagingListElt = dataListsElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName() + "s");
			addDataListStateAndName(packagingListElt, productData.getPackagingList().get(0).getParentNodeRef());

			BigDecimal netWeightPrimary = BigDecimal.valueOf(FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT));

			// display tare, net weight and gross weight
			BigDecimal tarePrimary = FormulationHelper.getTareInKg(productData.getTare(), productData.getTareUnit());
			if (tarePrimary == null) {
				tarePrimary = BigDecimal.valueOf(0d);
			}
			BigDecimal grossWeightPrimary = tarePrimary.add(netWeightPrimary);

			PackagingData packagingData = packagingHelper.getPackagingData(productData);
			for (Map.Entry<NodeRef, VariantPackagingData> kv : packagingData.getVariants().entrySet()) {
				VariantPackagingData variantPackagingData = kv.getValue();

				Element packgLevelMesuresElt = packagingListElt.addElement(TAG_PACKAGING_LEVEL_MEASURES);
				if (kv.getKey() != null) {
					packgLevelMesuresElt.addAttribute(ATTR_VARIANT_ID, (String) nodeService.getProperty(kv.getKey(), ContentModel.PROP_NAME));
					packgLevelMesuresElt.addAttribute(BeCPGModel.PROP_IS_DEFAULT_VARIANT.getLocalName(),
							Boolean.toString((Boolean) nodeService.getProperty(kv.getKey(), BeCPGModel.PROP_IS_DEFAULT_VARIANT)));
				} else {
					packgLevelMesuresElt.addAttribute(ATTR_VARIANT_ID, "");
					packgLevelMesuresElt.addAttribute(BeCPGModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), "true");
				}

				packgLevelMesuresElt.addAttribute(ATTR_PKG_TARE_LEVEL_1, toString(tarePrimary));
				packgLevelMesuresElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_1, toString(netWeightPrimary));
				packgLevelMesuresElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_1, toString(grossWeightPrimary));

				if (variantPackagingData.getProductPerBoxes() != null) {

					BigDecimal tareSecondary = tarePrimary.multiply(BigDecimal.valueOf(variantPackagingData.getProductPerBoxes()))
							.add(variantPackagingData.getTareSecondary());

					BigDecimal netWeightSecondary = netWeightPrimary.multiply(BigDecimal.valueOf(variantPackagingData.getProductPerBoxes()));
					BigDecimal grossWeightSecondary = tareSecondary.add(netWeightSecondary);

					packgLevelMesuresElt.addAttribute(ATTR_PKG_TARE_LEVEL_2, toString(tareSecondary));
					packgLevelMesuresElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_2, toString(netWeightSecondary));
					packgLevelMesuresElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_2, toString(grossWeightSecondary));
					packgLevelMesuresElt.addAttribute(ATTR_NB_PRODUCTS_LEVEL_2, toString(variantPackagingData.getProductPerBoxes()));

					if (variantPackagingData.getBoxesPerPallet() != null) {

						BigDecimal tareTertiary = tareSecondary.multiply(BigDecimal.valueOf(variantPackagingData.getBoxesPerPallet()))
								.add(variantPackagingData.getTareTertiary());
						BigDecimal netWeightTertiary = netWeightSecondary.multiply(BigDecimal.valueOf(variantPackagingData.getBoxesPerPallet()));
						packgLevelMesuresElt.addAttribute(ATTR_PKG_TARE_LEVEL_3, toString(tareTertiary));
						packgLevelMesuresElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_3, toString(netWeightTertiary));
						packgLevelMesuresElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_3, toString(tareTertiary.add(netWeightTertiary)));
						packgLevelMesuresElt.addAttribute(ATTR_NB_PRODUCTS_LEVEL_3,
								toString(variantPackagingData.getProductPerBoxes() * variantPackagingData.getBoxesPerPallet()));
					}
				}
			}

			VariantPackagingData defaultVariantPackagingData = packagingData.getVariants().get(defaultVariantNodeRef);

			productData.setDefaultVariantPackagingData(defaultVariantPackagingData);

			for (PackagingListDataItem dataItem : productData.getPackagingList(new EffectiveFilters<>(filter))) {
				loadPackagingItem(productData.getNodeRef(), new CurrentLevelQuantities(alfrescoRepository, productData, dataItem), dataItem,
						packagingListElt, context, 1, false, false);
			}

			if (context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_IN_MULTILEVEL, extractInMultiLevel()) && isExtractedProduct) {

				if (productData.hasCompoListEl(new EffectiveFilters<>(filter))) {

					for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(filter))) {
						if ((dataItem.getProduct() != null) && nodeService.exists(dataItem.getProduct())) {

							if ((nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
									|| nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))) {

								loadPackagingListItemForCompo(productData.getNodeRef(), packagingListElt, 1,
										new CurrentLevelQuantities(alfrescoRepository, packagingHelper, productData, dataItem), context,
										defaultVariantNodeRef,
										(productData.getDropPackagingOfComponents() != null) && productData.getDropPackagingOfComponents());
							}
						}
					}
				}

			}

			loadDynamicCharactList(productData.getPackagingListView().getDynamicCharactList(), packagingListElt);
		}
	}

	private void loadPackagingListItemForCompo(NodeRef entityNodeRef, Element packagingListElt, int level,
			CurrentLevelQuantities currentLevelQuantities, DefaultExtractorContext context, NodeRef defaultVariantNodeRef,
			boolean dropPackagingOfComponents) {

		if (level > 20) {
			addInfiniteLoopError(currentLevelQuantities, context);
			return;
		}

		if (currentLevelQuantities.getComponentProductData().hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
				|| hasCost(currentLevelQuantities.getComponentProductData(), CostType.Packaging)) {

			Element partElt = packagingListElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName());

			loadProductData(entityNodeRef, currentLevelQuantities.getCompoListItem().getComponent(), partElt, context, CostType.Packaging);
			loadDataListItemAttributes(currentLevelQuantities.getCompoListItem(), partElt, context);
			partElt.addAttribute(PLMModel.ASSOC_PACKAGINGLIST_PRODUCT.getLocalName(), currentLevelQuantities.getComponentProductData().getName());

			partElt.addAttribute(PLMModel.PROP_PACKAGINGLIST_LOSS_PERC.getLocalName(), Double.toString(currentLevelQuantities.getLossRatio()));
			partElt.addAttribute(PLMModel.PROP_PACKAGINGLIST_QTY.getLocalName(),
					currentLevelQuantities.getCompoListItem().getQtySubFormula() != null
							? currentLevelQuantities.getCompoListItem().getQtySubFormula().toString()
							: "");
			partElt.addAttribute(PLMModel.PROP_PACKAGINGLIST_UNIT.getLocalName(),
					currentLevelQuantities.getCompoListItem().getCompoListUnit() != null
							? currentLevelQuantities.getCompoListItem().getCompoListUnit().toString()
							: "");

			partElt.addAttribute(ATTR_PACKAGING_QTY_FOR_PRODUCT, Double.toString(currentLevelQuantities.getQtyForProduct()));
			partElt.addAttribute(ATTR_QTY_FOR_COST, Double.toString(currentLevelQuantities.getQtyForCost()));

			extractVariants(((AbstractEffectiveVariantListDataItem) currentLevelQuantities.getCompoListItem()).getVariants(), partElt);

			partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(level));

			if (currentLevelQuantities.getComponentProductData().hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				for (PackagingListDataItem packagingListDataItem : currentLevelQuantities.getComponentProductData()
						.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

					loadPackagingItem(entityNodeRef, new CurrentLevelQuantities(alfrescoRepository, packagingListDataItem, currentLevelQuantities),
							packagingListDataItem, packagingListElt, context, level + 1, dropPackagingOfComponents, true);

				}
			}
		}

		if (currentLevelQuantities.getComponentProductData().hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			for (CompoListDataItem subDataItem : currentLevelQuantities.getComponentProductData()
					.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				if ((subDataItem.getProduct() != null) && nodeService.exists(subDataItem.getProduct())) {

					if ((nodeService.getType(subDataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
							|| nodeService.getType(subDataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))) {

						loadPackagingListItemForCompo(entityNodeRef, packagingListElt, level + 1,
								new CurrentLevelQuantities(alfrescoRepository, packagingHelper, subDataItem, currentLevelQuantities), context,
								defaultVariantNodeRef,
								dropPackagingOfComponents
										|| ((currentLevelQuantities.getComponentProductData().getDropPackagingOfComponents() != null)
												&& currentLevelQuantities.getComponentProductData().getDropPackagingOfComponents()));

					}

				}
			}

		}

	}

	private void addInfiniteLoopError(CurrentLevelQuantities currentLevelQuantities, DefaultExtractorContext context) {
		context.setInfiniteLoop(true);
		String message = I18NUtil.getMessage("message.datasource.infinite-loop");
		context.getReportData().getLogs().add(new ReportableError(ReportableErrorType.ERROR, message,
				MLTextHelper.getI18NMessage("message.datasource.infinite-loop"), List.of(currentLevelQuantities.getCompoListItem().getNodeRef())));
		logger.error(
				"Infinite loop during datasource generation due to the following item: " + currentLevelQuantities.getCompoListItem().getNodeRef());
	}

	private void loadProcessListItemForCompo(NodeRef entityNodeRef, Element processListElt, int level, CurrentLevelQuantities currentLevelQuantities,
			DefaultExtractorContext context) {

		if (level > 20) {
			addInfiniteLoopError(currentLevelQuantities, context);
			return;
		}

		if (currentLevelQuantities.getComponentProductData().hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
				|| hasCost(currentLevelQuantities.getComponentProductData(), CostType.Process)) {

			Element partElt = processListElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName());
			loadProductData(entityNodeRef, currentLevelQuantities.getCompoListItem().getComponent(), partElt, context, CostType.Process);
			loadDataListItemAttributes(currentLevelQuantities.getCompoListItem(), partElt, context);

			partElt.addAttribute(MPMModel.ASSOC_PL_RESOURCE.getLocalName(), currentLevelQuantities.getComponentProductData().getName());
			partElt.addAttribute(MPMModel.PROP_PL_QTY_RESOURCE.getLocalName(),
					currentLevelQuantities.getCompoListItem().getQtySubFormula() != null
							? currentLevelQuantities.getCompoListItem().getQtySubFormula().toString()
							: "");

			partElt.addAttribute(PLMModel.PROP_COMPOLIST_LOSS_PERC.getLocalName(), Double.toString(currentLevelQuantities.getLossRatio()));
			partElt.addAttribute(MPMModel.PROP_PL_QTY_RESOURCE.getLocalName(),
					currentLevelQuantities.getCompoListItem().getQtySubFormula() != null
							? currentLevelQuantities.getCompoListItem().getQtySubFormula().toString()
							: "");
			partElt.addAttribute(PLMModel.PROP_COMPOLIST_UNIT.getLocalName(),
					currentLevelQuantities.getCompoListItem().getCompoListUnit() != null
							? currentLevelQuantities.getCompoListItem().getCompoListUnit().toString()
							: "");

			partElt.addAttribute(ATTR_PROCESS_QTY_FOR_PRODUCT, Double.toString(currentLevelQuantities.getQtyForProduct()));
			partElt.addAttribute(ATTR_QTY_FOR_COST, Double.toString(currentLevelQuantities.getQtyForCost()));

			extractVariants(((AbstractEffectiveVariantListDataItem) currentLevelQuantities.getCompoListItem()).getVariants(), partElt);

			partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(level));

			if (currentLevelQuantities.getComponentProductData().hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				for (ProcessListDataItem processListDataItem : currentLevelQuantities.getComponentProductData()
						.getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

					loadProcessListItem(entityNodeRef,
							new CurrentLevelQuantities(nodeService, alfrescoRepository, processListDataItem, currentLevelQuantities),
							processListDataItem, processListElt, level + 1, context);

				}
			}
		}

		if (currentLevelQuantities.getComponentProductData().hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			for (CompoListDataItem subDataItem : currentLevelQuantities.getComponentProductData()
					.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				if ((subDataItem.getProduct() != null) && nodeService.exists(subDataItem.getProduct())) {

					if ((nodeService.getType(subDataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
							|| nodeService.getType(subDataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))) {

						loadProcessListItemForCompo(entityNodeRef, processListElt, level + 1,
								new CurrentLevelQuantities(alfrescoRepository, packagingHelper, subDataItem, currentLevelQuantities), context);
					}
				}
			}

		}

	}

	private boolean hasCost(ProductData componentProductData, CostType typeOfCost) {
		for (CostListDataItem c : componentProductData.getCostList()) {
			if (c.getCost() != null) {
				String costType = (String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTTYPE);
				if (typeOfCost.toString().equals(costType)) {
					return true;
				}

			}
		}
		return false;
	}

	/**
	 * <p>loadCompoListItem.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param parentDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 * @param compoListElt a {@link org.dom4j.Element} object
	 * @param level a int
	 * @param currentLevelQuantities a {@link fr.becpg.repo.product.data.CurrentLevelQuantities} object
	 * @param context a {@link fr.becpg.repo.report.entity.impl.DefaultExtractorContext} object
	 */
	protected void loadCompoListItem(NodeRef entityNodeRef, CompoListDataItem parentDataItem, Element compoListElt, int level,
			CurrentLevelQuantities currentLevelQuantities, DefaultExtractorContext context) {

		if (level > 20) {
			addInfiniteLoopError(currentLevelQuantities, context);
			return;
		}

		Element partElt = compoListElt.addElement(PLMModel.TYPE_COMPOLIST.getLocalName());
		loadProductData(entityNodeRef, currentLevelQuantities.getCompoListItem().getComponent(), partElt, context, CostType.Composition);
		loadDataListItemAttributes(currentLevelQuantities.getCompoListItem(), partElt, context);

		partElt.addAttribute(ATTR_COMPOLIST_QTY_FOR_PRODUCT, Double.toString(currentLevelQuantities.getQtyForProduct()));
		partElt.addAttribute(ATTR_QTY_FOR_COST, Double.toString(currentLevelQuantities.getQtyForCost()));

		extractVariants(currentLevelQuantities.getCompoListItem().getVariants(), partElt);

		Element dataListsElt = null;
		if (context.isNotEmptyPrefs(EntityReportParameters.PARAM_COMPONENT_DATALISTS_TO_EXTRACT, componentDatalistsToExtract())) {

			boolean extractNextDatalist = true;

			if (context.getPreferences().containsKey(EntityReportParameters.PARAM_MAX_COMPOLIST_LEVEL_TO_EXTRACT)) {

				List<String> maxLevelPrefs = Arrays
						.asList(context.getPreferences().get(EntityReportParameters.PARAM_MAX_COMPOLIST_LEVEL_TO_EXTRACT).split(","));

				List<Integer> maxLevels = new ArrayList<>();

				for (String pref : maxLevelPrefs) {
					maxLevels.add(Integer.parseInt(pref));
				}

				int maxLevel = Collections.min(maxLevels);

				if (maxLevel < (level + 1)) {
					extractNextDatalist = false;
				}
			}

			if (extractNextDatalist) {
				dataListsElt = partElt.addElement(TAG_DATALISTS);
				loadDataLists(currentLevelQuantities.getCompoListItem().getProduct(), dataListsElt, context, false, level + 1);
			}
		}

		Integer depthLevel = currentLevelQuantities.getCompoListItem().getDepthLevel();
		if (depthLevel != null) {
			level = (depthLevel - 1) + level;
			partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), "" + level);
			partElt.addAttribute(ATTR_NODEREF, currentLevelQuantities.getCompoListItem().getNodeRef().toString());
			if (parentDataItem != null) {
				partElt.addAttribute(ATTR_PARENT_NODEREF, parentDataItem.getNodeRef().toString());
			}
		}

		if (context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_IN_MULTILEVEL, extractInMultiLevel())) {

			if ((nodeService.getType(currentLevelQuantities.getCompoListItem().getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
					|| nodeService.getType(currentLevelQuantities.getCompoListItem().getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))) {

				if (currentLevelQuantities.getComponentProductData().hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					if (dataListsElt != null) {
						loadDynamicCharactList(currentLevelQuantities.getComponentProductData().getCompoListView().getDynamicCharactList(),
								dataListsElt);
					}

					for (CompoListDataItem subDataItem : currentLevelQuantities.getComponentProductData()
							.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
						if (subDataItem.getProduct() != null) {

							boolean extractNextLevel = true;

							if (context.getPreferences().containsKey(EntityReportParameters.PARAM_MAX_COMPOLIST_LEVEL_TO_EXTRACT)) {
								List<String> maxLevelPrefs = Arrays
										.asList(context.getPreferences().get(EntityReportParameters.PARAM_MAX_COMPOLIST_LEVEL_TO_EXTRACT).split(","));

								List<Integer> maxLevels = new ArrayList<>();

								for (String pref : maxLevelPrefs) {
									maxLevels.add(Integer.parseInt(pref));
								}

								int maxLevel = Collections.min(maxLevels);

								if (maxLevel < (level + 1)) {
									extractNextLevel = false;
								}
							}

							if (extractNextLevel && !context.isInfiniteLoop()) {
								loadCompoListItem(entityNodeRef, currentLevelQuantities.getCompoListItem(), compoListElt, level + 1,
										new CurrentLevelQuantities(alfrescoRepository, packagingHelper, subDataItem, currentLevelQuantities),
										context);
							}
						}

					}

				}
			}
		}

	}

	private void loadResourceParams(ResourceProductData productData, Element partElt, DefaultExtractorContext context) {
		if ((productData.getResourceParamList() != null) && !productData.getResourceParamList().isEmpty()) {

			Element dataListsElt = partElt.addElement(TAG_DATALISTS);
			Element resourceListsElt = dataListsElt.addElement(MPMModel.TYPE_RESOURCEPARAMLIST.getLocalName() + "s");
			addDataListStateAndName(resourceListsElt, productData.getResourceParamList().get(0).getParentNodeRef());

			for (ResourceParamListItem resourceParamListItem : productData.getResourceParamList()) {

				Element ressourceListElt = resourceListsElt.addElement(MPMModel.TYPE_RESOURCEPARAMLIST.getLocalName());

				loadDataListItemAttributes(resourceParamListItem, ressourceListElt, context);

			}
		}
	}

	private void loadNutLists(ProductData productData, Element dataListsElt, DefaultExtractorContext context) {

		if ((productData.getNutList() != null) && !productData.getNutList().isEmpty()) {

			Element nutListsElt = dataListsElt.addElement(PLMModel.TYPE_NUTLIST.getLocalName() + "s");
			addDataListStateAndName(nutListsElt, productData.getNutList().get(0).getParentNodeRef());

			for (NutListDataItem dataListItem : productData.getNutList()) {

				if (dataListItem.getNut() != null) {

					NutDataItem nut = (NutDataItem) alfrescoRepository.findOne(dataListItem.getNut());
					Element nutListElt = nutListsElt.addElement(PLMModel.TYPE_NUTLIST.getLocalName());

					loadDataListItemAttributes(dataListItem, nutListElt, context);

					String value = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_VALUE.getLocalName());
					if ((value == null) || value.isEmpty()) {
						value = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_FORMULATED_VALUE.getLocalName());
						nutListElt.addAttribute(PLMModel.PROP_NUTLIST_VALUE.getLocalName(), value);
					}

					value = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_VALUE_PER_SERVING.getLocalName());
					if ((value == null) || value.isEmpty()) {
						value = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_FORMULATED_VALUE_PER_SERVING.getLocalName());
						nutListElt.addAttribute(PLMModel.PROP_NUTLIST_VALUE_PER_SERVING.getLocalName(), value);
					}

					value = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_MINI.getLocalName());
					if ((value == null) || value.isEmpty()) {
						value = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_FORMULATED_MINI.getLocalName());
						nutListElt.addAttribute(PLMModel.PROP_NUTLIST_MINI.getLocalName(), value);
					}

					value = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_MAXI.getLocalName());
					if ((value == null) || value.isEmpty()) {
						value = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_FORMULATED_MAXI.getLocalName());
						nutListElt.addAttribute(PLMModel.PROP_NUTLIST_MAXI.getLocalName(), value);
					}

					value = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_VALUE_PREPARED.getLocalName());
					if ((value == null) || value.isEmpty()) {
						value = nutListElt.attributeValue(PLMModel.PROP_NUTLIST_FORMULATED_PREPARED.getLocalName());
						nutListElt.addAttribute(PLMModel.PROP_NUTLIST_VALUE_PREPARED.getLocalName(), value);
					}

					for (RequirementListDataItem reqCtrlList : productData.getReqCtrlList()) {
						if (reqCtrlList.getReqDataType().equals(RequirementDataType.Nutrient)
								&& dataListItem.getCharactNodeRef().equals(reqCtrlList.getCharact())) {
							nutListElt.addAttribute(PLMModel.PROP_NUTLIST_FORMULA_ERROR.getLocalName(), "Error");
						}
					}

					nutListElt.addAttribute(RegulationFormulationHelper.ATTR_NUT_CODE, nut.getNutCode());
					nutListElt.addAttribute(BeCPGModel.PROP_COLOR.getLocalName(), nut.getNutColor());
					boolean isDisplayed = isCharactDisplayedForLocale(dataListItem.getNut());

					RegulationFormulationHelper.extractXMLAttribute(nutListElt, dataListItem.getRoundedValue(), I18NUtil.getLocale(), isDisplayed,
							context.getPrefValue("nutLocalesToExtract", nutLocalesToExtract()));

					if (Boolean.TRUE.equals(showDeprecated())) {

						addCDATA(nutListElt, PLMModel.PROP_NUTGDA, nut.getNutGDA() != null ? nut.getNutGDA().toString() : "", null);

						String assocNut = nutListElt.attributeValue(PLMModel.ASSOC_NUTLIST_NUT.getLocalName());
						if ((assocNut != null) && !assocNut.isEmpty()) {

							nutListsElt.addAttribute(generateKeyAttribute(assocNut), value != null ? value : "");

						} else {
							logger.warn("Nut is null for " + dataListItem.getNut());
						}
					}
				}
			}
		}
	}

	private void loadOrganoLists(ProductData productData, Element dataListsElt, DefaultExtractorContext context) {
		if ((productData.getOrganoList() != null) && !productData.getOrganoList().isEmpty()) {
			Element organoListsElt = dataListsElt.addElement(PLMModel.TYPE_ORGANOLIST.getLocalName() + "s");
			addDataListStateAndName(organoListsElt, productData.getOrganoList().get(0).getParentNodeRef());

			for (OrganoListDataItem dataListItem : productData.getOrganoList()) {
				Element organoListElt = organoListsElt.addElement(PLMModel.TYPE_ORGANOLIST.getLocalName());
				loadDataListItemAttributes(dataListItem, organoListElt, context);
			}
		}
	}

	private void loadLabelCLaimLists(ProductData productData, Element dataListsElt, DefaultExtractorContext context) {
		if ((productData.getLabelClaimList() != null) && !productData.getLabelClaimList().isEmpty()) {
			Element lcListsElt = dataListsElt.addElement(PLMModel.TYPE_LABELCLAIMLIST.getLocalName() + "s");
			addDataListStateAndName(lcListsElt, productData.getLabelClaimList().get(0).getParentNodeRef());

			for (LabelClaimListDataItem dataListItem : productData.getLabelClaimList()) {
				if (dataListItem.getLabelClaim() != null) {
					Element lcListElt = lcListsElt.addElement(PLMModel.TYPE_LABELCLAIMLIST.getLocalName());
					loadDataListItemAttributes(dataListItem, lcListElt, context);

					boolean isDisplay = isCharactDisplayedForLocale(dataListItem.getLabelClaim());
					String displayMode = (isDisplay ? "O" : "");
					lcListElt.addAttribute("regulDisplayMode", displayMode);
				}
			}
		}
	}

	private void loadAllergenLists(ProductData productData, Element dataListsElt, DefaultExtractorContext context) {
		if ((productData.getAllergenList() != null) && !productData.getAllergenList().isEmpty()) {
			Element allergenListsElt = dataListsElt.addElement(PLMModel.TYPE_ALLERGENLIST.getLocalName() + "s");
			addDataListStateAndName(allergenListsElt, productData.getAllergenList().get(0).getParentNodeRef());

			StringBuilder volAllergens = null;
			StringBuilder inVolAllergens = null;
			StringBuilder inVolAllergensProcess = null;
			StringBuilder inVolAllergensRawMaterial = null;

			for (AllergenListDataItem dataListItem : productData.getAllergenList()) {
				if (dataListItem.getAllergen() != null) {
					Element allergenListElt = allergenListsElt.addElement(PLMModel.TYPE_ALLERGENLIST.getLocalName());
					loadDataListItemAttributes(dataListItem, allergenListElt, context);

					String allergenType = (String) nodeService.getProperty(dataListItem.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE);
					boolean isDisplayed = isCharactDisplayedForLocale(dataListItem.getAllergen());
					String displayMode = "";

					if (isDisplayed) {
						displayMode = "O";

						if (allergenType != null) {
							allergenListElt.addAttribute("allergenType", allergenType);
							if (allergenType.equals("Major")) {

								displayMode = "M";
								String allergen = (String) nodeService.getProperty(dataListItem.getAllergen(), BeCPGModel.PROP_LEGAL_NAME);

								if ((allergen == null) || allergen.isEmpty()) {
									allergen = (String) nodeService.getProperty(dataListItem.getAllergen(), BeCPGModel.PROP_CHARACT_NAME);
								}

								if (allergen == null) {
									allergen = "###";
								}

								// concat allergens
								if (Boolean.TRUE.equals(dataListItem.getVoluntary())) {
									if (volAllergens == null) {
										volAllergens = new StringBuilder();
									} else {
										volAllergens.append(RepoConsts.LABEL_SEPARATOR);
									}

									volAllergens.append(allergen);

								} else if (Boolean.TRUE.equals(dataListItem.getInVoluntary())) {
									if (inVolAllergens == null) {
										inVolAllergens = new StringBuilder();
									} else {
										inVolAllergens.append(RepoConsts.LABEL_SEPARATOR);
									}
									inVolAllergens.append(allergen);

									boolean presentInRawMaterial = false;
									boolean presentInProcess = false;
									for (NodeRef inVoluntarySource : dataListItem.getInVoluntarySources()) {
										QName inVoluntarySourceType = nodeService.getType(inVoluntarySource);

										if (!presentInRawMaterial && PLMModel.TYPE_RAWMATERIAL.equals(inVoluntarySourceType)) {
											if (inVolAllergensRawMaterial == null) {
												inVolAllergensRawMaterial = new StringBuilder();
											} else {
												inVolAllergensRawMaterial.append(RepoConsts.LABEL_SEPARATOR);
											}
											inVolAllergensRawMaterial.append(allergen);
											presentInRawMaterial = true;
										} else if (!presentInProcess && PLMModel.TYPE_RESOURCEPRODUCT.equals(inVoluntarySourceType)) {
											if (inVolAllergensProcess == null) {
												inVolAllergensProcess = new StringBuilder();
											} else {
												inVolAllergensProcess.append(RepoConsts.LABEL_SEPARATOR);
											}
											inVolAllergensProcess.append(allergen);
											presentInProcess = true;
										}
									}

								}
							}
						}
					}
					allergenListElt.addAttribute("regulDisplayMode", displayMode);
				}

				allergenListsElt.addAttribute(PLMModel.PROP_ALLERGENLIST_VOLUNTARY.getLocalName(),
						volAllergens != null ? volAllergens.toString() : "");
				allergenListsElt.addAttribute(PLMModel.PROP_ALLERGENLIST_INVOLUNTARY.getLocalName(),
						inVolAllergens != null ? inVolAllergens.toString() : "");
				allergenListsElt.addAttribute(ATTR_ALLERGENLIST_INVOLUNTARY_FROM_PROCESS,
						inVolAllergensProcess != null ? inVolAllergensProcess.toString() : "");
				allergenListsElt.addAttribute(ATTR_ALLERGENLIST_INVOLUNTARY_FROM_RAW_MATERIAL,
						inVolAllergensRawMaterial != null ? inVolAllergensRawMaterial.toString() : "");
			}
		}
	}

	private void loadIngLists(ProductData productData, Element dataListsElt, DefaultExtractorContext context) {
		if ((productData.getIngList() != null) && !productData.getIngList().isEmpty()) {
			Element ingListsElt = dataListsElt.addElement(PLMModel.TYPE_INGLIST.getLocalName() + "s");
			addDataListStateAndName(ingListsElt, productData.getIngList().get(0).getParentNodeRef());

			for (IngListDataItem dataListItem : productData.getIngList()) {
				if (dataListItem.getIng() != null) {
					Element ingListElt = ingListsElt.addElement(PLMModel.TYPE_INGLIST.getLocalName());
					String ingCEECode = (String) nodeService.getProperty(dataListItem.getIng(), PLMModel.PROP_ING_CEECODE);
					if (ingCEECode != null) {
						ingListElt.addAttribute(PLMModel.PROP_ING_CEECODE.getLocalName(), ingCEECode);
					}
					loadDataListItemAttributes(dataListItem, ingListElt, context);
				}
			}
		}
	}

	private void extractRawMaterials(ProductData productData, Element dataListsElt, DefaultExtractorContext context) {

		Double productNetWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

		Map<NodeRef, Double> rawMaterials = new HashMap<>();
		rawMaterials = AllocationHelper.extractAllocations(productData, rawMaterials, productNetWeight, alfrescoRepository);
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
			loadAttributes(entry.getKey(), rawMaterialElt, true, null, context);
			addCDATA(rawMaterialElt, PLMModel.PROP_COMPOLIST_QTY, toString((100 * entry.getValue()) / (totalQty != 0d ? totalQty : 1d)), null);
			if (productNetWeight != 0d) {
				Element cDATAElt = rawMaterialElt.addElement(ATTR_COMPOLIST_QTY_FOR_PRODUCT);
				cDATAElt.addCDATA(
						toString((100 * entry.getValue()) / FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT)));
			}
			Element rawMaterialDataListsElt = rawMaterialElt.addElement(TAG_DATALISTS);
			loadDataLists(entry.getKey(), rawMaterialDataListsElt, context, false, 1);
		}
	}

	private void extractPriceBreaks(ProductData productData, Element dataListsElt) {

		Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

		List<PriceBreakReportData> priceBreaks = new ArrayList<>();
		extractPriceBreaks(productData, 1d, 1d, priceBreaks);

		Collections.sort(priceBreaks, (r1, r2) -> r2.getProjectedQty().compareTo(r1.getProjectedQty()));

		Map<Long, List<PriceBreakReportData>> ret = new LinkedHashMap<>();

		// Group by projected Qty
		for (PriceBreakReportData priceBreakReportData : priceBreaks) {
			Long projectedQty = 0L;
			if (priceBreakReportData.getProjectedQty() != null) {
				projectedQty = Math.round(priceBreakReportData.getProjectedQty() / netWeight);
			}

			if (ret.containsKey(projectedQty)) {
				ret.get(projectedQty).add(priceBreakReportData);
			} else {
				List<PriceBreakReportData> tmp = new ArrayList<>();
				tmp.add(priceBreakReportData);
				ret.put(projectedQty, tmp);
			}

		}

		// Merge PriceBreakReportData
		for (Map.Entry<Long, List<PriceBreakReportData>> entry : ret.entrySet()) {
			for (Map.Entry<Long, List<PriceBreakReportData>> entry2 : ret.entrySet()) {
				if (entry2.getKey() < entry.getKey()) {
					for (PriceBreakReportData pbrd : entry2.getValue()) {
						boolean add = true;
						for (PriceBreakReportData pbrd2 : entry.getValue()) {
							if (pbrd2.getProduct().equals(pbrd.getProduct())) {
								add = false;
								break;
							}
						}
						if (add) {
							entry.getValue().add(pbrd);
						}
					}

				}
			}

		}

		// Add to XML
		Element priceBreaksElt = dataListsElt.addElement("priceBreaks");

		for (Map.Entry<Long, List<PriceBreakReportData>> entry : ret.entrySet()) {

			Element priceBreakElt = priceBreaksElt.addElement("priceBreak");
			List<PriceBreakReportData> tmp = entry.getValue();
			priceBreakElt.addAttribute("projectedQty", entry.getKey().toString());

			String products = "";
			double totalSimulatedValue = 0d;

			for (PriceBreakReportData priceBreakReportData : tmp) {

				Element priceBreakEltDetailElt = priceBreakElt.addElement("priceBreakDetail");
				priceBreakEltDetailElt.addAttribute("cost",
						(String) nodeService.getProperty(priceBreakReportData.getCost(), BeCPGModel.PROP_CHARACT_NAME));

				String product = (String) nodeService.getProperty(priceBreakReportData.getProduct(), ContentModel.PROP_NAME);
				priceBreakEltDetailElt.addAttribute("product", product);
				priceBreakEltDetailElt.addAttribute("projectedQtyByKg", Math.round(priceBreakReportData.getProjectedQty()) + "");
				priceBreakEltDetailElt.addAttribute("projectedQty", Math.round(priceBreakReportData.getProjectedQty() / netWeight) + "");

				priceBreakEltDetailElt.addAttribute(PLMModel.PROP_PRICELIST_VALUE.getLocalName(), "" + priceBreakReportData.getPriceListValue());
				priceBreakEltDetailElt.addAttribute(PLMModel.PROP_PRICELIST_UNIT.getLocalName(), priceBreakReportData.getPriceListUnit());
				priceBreakEltDetailElt.addAttribute(PLMModel.PROP_PRICELIST_PURCHASE_QTY.getLocalName(),
						"" + priceBreakReportData.getPriceListPurchaseValue());
				priceBreakEltDetailElt.addAttribute(PLMModel.PROP_PRICELIST_PREF_RANK.getLocalName(),
						"" + priceBreakReportData.getPriceListPrefRank());
				priceBreakEltDetailElt.addAttribute(PLMModel.PROP_PRICELIST_PURCHASE_UNIT.getLocalName(),
						priceBreakReportData.getPriceListPurchaseUnit());

				String suppliers = "";
				if (priceBreakReportData.getSuppliers() != null) {
					for (NodeRef supplier : priceBreakReportData.getSuppliers()) {
						if (!suppliers.isEmpty()) {
							suppliers += ",";
						}
						suppliers += (String) nodeService.getProperty(supplier, ContentModel.PROP_NAME);
					}

				}

				priceBreakEltDetailElt.addAttribute("suppliers", suppliers);

				String geoOrigins = "";
				if (priceBreakReportData.getGeoOrigins() != null) {
					for (NodeRef geoOrigin : priceBreakReportData.getGeoOrigins()) {
						if (!geoOrigins.isEmpty()) {
							geoOrigins += ",";
						}
						geoOrigins += (String) nodeService.getProperty(geoOrigin, BeCPGModel.PROP_CHARACT_NAME);
					}

				}

				priceBreakEltDetailElt.addAttribute("geoOrigins", geoOrigins);

				if (!suppliers.isEmpty()) {
					product += " [" + suppliers + "]";
				}

				if (Math.round(priceBreakReportData.getProjectedQty() / netWeight) == entry.getKey()) {
					if (!products.isEmpty()) {
						products += ",";
					}

					products += product;

				}
				Double simulatedValue = priceBreakReportData.getSimulatedValue() != null ? priceBreakReportData.getSimulatedValue() : 0d;
				priceBreakEltDetailElt.addAttribute("simulatedValue", simulatedValue.toString());
				totalSimulatedValue += simulatedValue;

			}

			Double unitTotalCost = productData.getUnitTotalCost() != null ? productData.getUnitTotalCost() : 0d;

			if ((productData.getUnit() != null) && productData.getUnit().isP()) {
				unitTotalCost += totalSimulatedValue;
			} else {
				unitTotalCost += (netWeight * totalSimulatedValue);
			}
			priceBreakElt.addAttribute("products", products);
			priceBreakElt.addAttribute("simulatedValue", Double.toString(totalSimulatedValue));
			priceBreakElt.addAttribute("unitTotalCost", unitTotalCost.toString());

		}

	}

	private void extractPriceBreaks(ProductData productData, Double parentLossRatio, Double parentQty, List<PriceBreakReportData> priceBreaks) {

		Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

		for (CompoListDataItem compoList : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			if ((compoList.getProduct() != null) && !DeclarationType.Omit.equals(compoList.getDeclType())) {
				Double qty = FormulationHelper.getQtyInKg(compoList);

				ProductData componentProduct = (ProductData) alfrescoRepository.findOne(compoList.getProduct());

				Double qtyForCost = FormulationHelper.getQtyForCost(compoList, parentLossRatio, componentProduct, false);

				if (logger.isDebugEnabled()) {
					logger.debug(
							"Get rawMaterial " + componentProduct.getName() + "qty: " + qty + " netWeight " + netWeight + " parentQty " + parentQty);
				}
				if ((qty != null) && (netWeight != 0d)) {
					qty = (parentQty * qty * FormulationHelper.getYield(compoList)) / (100 * netWeight);

					qtyForCost = (parentQty * qtyForCost * FormulationHelper.getYield(compoList)) / (100 * netWeight);

					if (componentProduct.isRawMaterial()) {

						createPriceBreakReportData(productData, componentProduct, qty, qtyForCost, priceBreaks);

					} else if (!componentProduct.isLocalSemiFinished()) {
						Double lossPerc = FormulationHelper.getComponentLossPerc(componentProduct, compoList);
						Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);

						extractPriceBreaks(componentProduct, newLossPerc, qty, priceBreaks);
					}
				}
			}
		}

		extractPriceBreaksForPackaging(productData, priceBreaks, parentQty / (netWeight != 0d ? netWeight : 1d));
	}

	private void extractPriceBreaksForPackaging(ProductData productData, List<PriceBreakReportData> priceBreaks, Double parentQty) {

		for (PackagingListDataItem packagingListDataItem : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			ProductData componentProduct = (ProductData) alfrescoRepository.findOne(packagingListDataItem.getComponent());

			Double qtyForCost = FormulationHelper.getQtyForCostByPackagingLevel(productData, packagingListDataItem, componentProduct);

			if (componentProduct.isPackagingKit()) {
				extractPriceBreaksForPackaging(componentProduct, priceBreaks, parentQty * qtyForCost);
			} else {
				createPriceBreakReportData(productData, componentProduct, qtyForCost * parentQty, qtyForCost * parentQty, priceBreaks);
			}

		}

	}

	private void createPriceBreakReportData(ProductData productData, ProductData componentProduct, Double qty, Double qtyForCost,
			List<PriceBreakReportData> priceBreaks) {
		if (componentProduct.getPriceList() != null) {
			for (PriceListDataItem item : componentProduct.getPriceList()) {
				PriceBreakReportData priceBreakReportData = new PriceBreakReportData();

				priceBreakReportData.setCost(item.getCost());
				priceBreakReportData.setProduct(componentProduct.getNodeRef());
				priceBreakReportData.setSuppliers(item.getSuppliers());
				priceBreakReportData.setGeoOrigins(item.getGeoOrigins());
				priceBreakReportData.setPriceListValue(item.getValue());
				priceBreakReportData.setPriceListUnit(item.getUnit());
				priceBreakReportData.setPriceListPrefRank(item.getPrefRank());
				priceBreakReportData.setPriceListPurchaseValue(item.getPurchaseValue());
				priceBreakReportData.setPriceListPurchaseUnit(item.getPurchaseUnit());

				Double purchaseValue = item.getPurchaseValue();

				if ((item.getPurchaseUnit() != null) && (!item.getPurchaseUnit().isEmpty())) {
					ProductUnit purchaseUnit = ProductUnit.valueOf(item.getPurchaseUnit());

					if (purchaseValue != null) {

						if (purchaseUnit.isWeight() || purchaseUnit.isVolume()) {
							purchaseValue = purchaseValue / purchaseUnit.getUnitFactor();
						} else if (purchaseUnit.isP()) {
							purchaseValue = purchaseValue * FormulationHelper.getNetWeight(productData, 1d);
						}

					}
				}

				if ((qty != 0) && (qty != null) && (purchaseValue != null)) {
					priceBreakReportData.setProjectedQty(item.getPurchaseValue() / qty);
				}

				if ((item.getValue() != null) && (qtyForCost != null)) {
					for (CostListDataItem cost : componentProduct.getCostList()) {
						if (((cost.getCost() != null) && (cost.getValue() != null)) && cost.getCost().equals(item.getCost())) {

							Double simulatedValue = (item.getValue() - cost.getValue()) * qtyForCost;
							priceBreakReportData.setSimulatedValue(simulatedValue);
							break;
						}

					}
				}

				priceBreaks.add(priceBreakReportData);
			}

		}
	}

	/** {@inheritDoc} */
	@Override
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt, DefaultExtractorContext context) {

		// compatibility with existing reports
		if (assocDef.getName().equals(PLMModel.ASSOC_STORAGE_CONDITIONS) || assocDef.getName().equals(PLMModel.ASSOC_PRECAUTION_OF_USE)) {
			extractTargetAssoc(entityNodeRef, assocDef, entityElt, context, false);
			return true;
		} else {
			return super.loadTargetAssoc(entityNodeRef, assocDef, entityElt, context);
		}
	}

	private void loadPackagingItem(NodeRef entityNodeRef, CurrentLevelQuantities currentLevelQuantities, PackagingListDataItem dataItem,
			Element packagingListElt, DefaultExtractorContext context, int level, boolean dropPackagingOfComponents, boolean isPackagingOfComponent) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			loadPackagingKit(entityNodeRef, currentLevelQuantities, dataItem, packagingListElt, context, level, dropPackagingOfComponents,
					isPackagingOfComponent);
			Element imgsElt = (Element) packagingListElt.getDocument().selectSingleNode(TAG_ENTITY + "/" + TAG_IMAGES);
			if (imgsElt != null) {
				extractPackagingImages(dataItem.getProduct(), imgsElt, context, dataItem, dropPackagingOfComponents, isPackagingOfComponent);
			}
		} else {
			loadPackaging(entityNodeRef, currentLevelQuantities, dataItem, packagingListElt, context, level, dropPackagingOfComponents,
					isPackagingOfComponent);
		}
	}

	private void extractPackagingImages(NodeRef product, Element imgsElt, DefaultExtractorContext context, PackagingListDataItem dataItem,
			boolean dropPackagingOfComponents, boolean isPackagingOfComponent) {

		Map<String, String> extraAttributes = new HashMap<>();

		PackagingLevel packLevel = dataItem.getPkgLevel();
		if (packLevel == null) {
			packLevel = PackagingLevel.Primary;
		}

		extraAttributes.put(PLMModel.PROP_PRODUCT_DROP_PACKAGING_OF_COMPONENTS.getLocalName(),
				Boolean.toString((!packLevel.equals(PackagingLevel.Primary) && isPackagingOfComponent) || dropPackagingOfComponents));

		extractEntityImages(product, imgsElt, context, extraAttributes);

	}

	private Element loadPackaging(NodeRef entityNodeRef, CurrentLevelQuantities currentLevelQuantities, PackagingListDataItem dataItem,
			Element packagingListElt, DefaultExtractorContext context, int level, boolean dropPackagingOfComponents, boolean isPackagingOfComponent) {

		Element partElt = packagingListElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName());
		loadProductData(entityNodeRef, dataItem.getComponent(), partElt, context, CostType.Packaging);
		if (Boolean.TRUE.equals(dataItem.getIsRecycle())) {
			partElt.addAttribute("currentCost", Double.toString(0d));
			partElt.addAttribute("previousCost", Double.toString(0d));
			partElt.addAttribute("futureCost", Double.toString(0d));
		}
		loadDataListItemAttributes(dataItem, partElt, context);

		extractVariants(dataItem.getVariants(), partElt);

		// we want labeling template <labelingTemplate>...</labelingTemplate>
		if (nodeService.hasAspect(dataItem.getNodeRef(), PackModel.ASPECT_LABELING)) {
			extractTargetAssoc(dataItem.getNodeRef(), dictionaryService.getAssociation(PackModel.ASSOC_LABELING_TEMPLATE), partElt, context, false);
		}

		partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(level));
		PackagingLevel packLevel = dataItem.getPkgLevel();

		if (packLevel == null) {
			packLevel = PackagingLevel.Primary;
		}

		if ((dataItem.getQty() != null) && (dataItem.getPackagingListUnit() != null)) {

			// we display the quantity used in the SF
			// partElt.addAttribute(PLMModel.PROP_PACKAGINGLIST_QTY.getLocalName(),
			// Double.toString(dataItem.getQty() * sfQty));
			//			double qty = ProductUnit.PP.equals(dataItem.getPackagingListUnit()) ? 1 : dataItem.getQty();
			//			if ((dataItem.getPackagingListUnit() != null) && dataItem.getPackagingListUnit().isWeight()) {
			//				qty = qty / dataItem.getPackagingListUnit().getUnitFactor();
			//			}
			Double qtyForProduct = currentLevelQuantities.getQtyForProduct();
			Double qtyForCost = currentLevelQuantities.getQtyForCost();
			//			if (packLevel.equals(PackagingLevel.Secondary) && (defaultVariantPackagingData.getProductPerBoxes() != null)
			//					&& (defaultVariantPackagingData.getProductPerBoxes() != 0)) {
			//				qtyForProduct = ( currentLevelQuantities.getQtyForProduct()) / defaultVariantPackagingData.getProductPerBoxes();
			//				qtyForCost = currentLevelQuantities.getQtyForCost() / defaultVariantPackagingData.getProductPerBoxes();
			//			} else if (packLevel.equals(PackagingLevel.Tertiary) && (defaultVariantPackagingData.getProductPerPallet() != null)
			//					&& (defaultVariantPackagingData.getProductPerPallet() != 0)) {
			//				qtyForProduct = ( currentLevelQuantities.getQtyForProduct()) / defaultVariantPackagingData.getProductPerPallet();
			//				qtyForCost =  currentLevelQuantities.getQtyForCost() / defaultVariantPackagingData.getProductPerPallet();
			//			}
			partElt.addAttribute(ATTR_PACKAGING_QTY_FOR_PRODUCT, Double.toString(qtyForProduct));

			partElt.addAttribute(ATTR_QTY_FOR_COST, Double.toString(qtyForCost));

			partElt.addAttribute(PLMModel.PROP_PRODUCT_DROP_PACKAGING_OF_COMPONENTS.getLocalName(),
					Boolean.toString((!packLevel.equals(PackagingLevel.Primary) && isPackagingOfComponent) || dropPackagingOfComponents));
		}
		return partElt;
	}

	private void loadProcessListItem(NodeRef entityNodeRef, CurrentLevelQuantities currentLevelQuantities, ProcessListDataItem dataItem,
			Element processListElt, int level, DefaultExtractorContext context) {

		if (level > 20) {
			addInfiniteLoopError(currentLevelQuantities, context);
			return;
		}

		Element partElt = processListElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName());
		loadProductData(entityNodeRef, dataItem.getComponent(), partElt, context, CostType.Process);
		loadDataListItemAttributes(dataItem, partElt, context);

		partElt.addAttribute(ATTR_PROCESS_QTY_FOR_PRODUCT, Double.toString(currentLevelQuantities.getQtyForProduct()));
		partElt.addAttribute(ATTR_QTY_FOR_COST, Double.toString(currentLevelQuantities.getQtyForCost()));
		extractVariants(((AbstractEffectiveVariantListDataItem) dataItem).getVariants(), partElt);
		partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(level));

		if (currentLevelQuantities.getComponentProductData() != null) {

			loadResourceParams((ResourceProductData) currentLevelQuantities.getComponentProductData(), partElt, context);

			if (currentLevelQuantities.getComponentProductData().hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				for (ProcessListDataItem subDataItem : currentLevelQuantities.getComponentProductData()
						.getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					loadProcessListItem(entityNodeRef,
							new CurrentLevelQuantities(nodeService, alfrescoRepository, subDataItem, currentLevelQuantities), subDataItem,
							processListElt, level + 1, context);
				}
			}
		}

	}

	private void loadPackagingKit(NodeRef entityNodeRef, CurrentLevelQuantities currentLevelQuantities, PackagingListDataItem dataItem,
			Element packagingListElt, DefaultExtractorContext context, int level, boolean dropPackagingOfComponents, boolean isPackagingOfComponent) {
		loadPackaging(entityNodeRef, currentLevelQuantities, dataItem, packagingListElt, context, level, dropPackagingOfComponents,
				isPackagingOfComponent);
		ProductData packagingKitData = (ProductData) alfrescoRepository.findOne(dataItem.getProduct());
		if (packagingKitData.hasPackagingListEl()) {
			for (PackagingListDataItem p : packagingKitData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				if ((dataItem.getVariants() != null) && !dataItem.getVariants().isEmpty()) {
					p.setVariants(dataItem.getVariants());
				}
				loadPackagingItem(entityNodeRef, new CurrentLevelQuantities(alfrescoRepository, p, currentLevelQuantities), p, packagingListElt,
						context, level + 1, dropPackagingOfComponents, isPackagingOfComponent);
			}
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

	/**
	 * <p>loadVariants.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param entityElt a {@link org.dom4j.Element} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	protected NodeRef loadVariants(ProductData productData, Element entityElt) {
		NodeRef defaultVariantNodeRef = null;

		Element variantsElt = entityElt.addElement(BeCPGModel.ASSOC_VARIANTS.getLocalName());
		if ((productData.getVariants() != null) && !productData.getVariants().isEmpty()) {
			for (VariantData variant : productData.getVariants()) {
				if (Boolean.TRUE.equals(variant.getIsDefaultVariant())) {
					defaultVariantNodeRef = variant.getNodeRef();
				}

				Element variantElt = variantsElt.addElement(BeCPGModel.TYPE_VARIANT.getLocalName());
				variantElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), variant.getName());
				variantElt.addAttribute(BeCPGModel.PROP_VARIANT_COLUMN.getLocalName(), variant.getVariantColumn());
				variantElt.addAttribute(BeCPGModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), Boolean.toString(variant.getIsDefaultVariant()));
			}
		} else {
			Element variantElt = variantsElt.addElement(BeCPGModel.TYPE_VARIANT.getLocalName());
			variantElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), "");
			variantElt.addAttribute(BeCPGModel.PROP_VARIANT_COLUMN.getLocalName(), "");
			variantElt.addAttribute(BeCPGModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), Boolean.TRUE.toString());
		}
		return defaultVariantNodeRef;
	}

	/**
	 * <p>extractVariants.</p>
	 *
	 * @param variantNodeRefs a {@link java.util.List} object.
	 * @param dataItemElt a {@link org.dom4j.Element} object.
	 */
	protected void extractVariants(List<NodeRef> variantNodeRefs, Element dataItemElt) {

		if ((variantNodeRefs != null) && !variantNodeRefs.isEmpty()) {
			Boolean isDefault = null;
			StringBuilder variantNames = new StringBuilder();
			for (NodeRef variantNodeRef : variantNodeRefs) {
				if (nodeService.exists(variantNodeRef)) {
					if (isDefault != null) {
						variantNames.append(",");
					}

					variantNames.append(((String) nodeService.getProperty(variantNodeRef, ContentModel.PROP_NAME)));

					if ((isDefault == null) || !Boolean.TRUE.equals(isDefault)) {
						isDefault = (Boolean) nodeService.getProperty(variantNodeRef, BeCPGModel.PROP_IS_DEFAULT_VARIANT);
					}

					if (isDefault == null) {
						isDefault = false;
					}
				}

			}
			dataItemElt.addAttribute(BeCPGModel.PROP_IS_DEFAULT_VARIANT.getLocalName(),
					isDefault != null ? isDefault.toString() : Boolean.FALSE.toString());
			dataItemElt.addAttribute(BeCPGModel.PROP_VARIANTIDS.getLocalName(), variantNames.toString());
		} else {
			dataItemElt.addAttribute(BeCPGModel.PROP_VARIANTIDS.getLocalName(), "");
			dataItemElt.addAttribute(BeCPGModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), Boolean.TRUE.toString());
		}
	}

	/**
	 * <p>loadDynamicCharactList.</p>
	 *
	 * @param dynamicCharactList a {@link java.util.List} object.
	 * @param dataListElt a {@link org.dom4j.Element} object.
	 */
	protected void loadDynamicCharactList(List<DynamicCharactListItem> dynamicCharactList, Element dataListElt) {

		Element dynCharactListElt = dataListElt.addElement(PLMModel.TYPE_DYNAMICCHARACTLIST.getLocalName() + "s");
		for (DynamicCharactListItem dc : dynamicCharactList) {
			Element dynamicCharact = dynCharactListElt.addElement(PLMModel.TYPE_DYNAMICCHARACTLIST.getLocalName());
			dynamicCharact.addAttribute(PLMModel.PROP_DYNAMICCHARACT_TITLE.getLocalName(), dc.getTitle());
			Object ret = null;
			if (dc.getValue() != null) {
				ret = JsonFormulaHelper.cleanCompareJSON(dc.getValue().toString());
			}
			dynamicCharact.addAttribute(PLMModel.PROP_DYNAMICCHARACT_VALUE.getLocalName(), ret == null ? VALUE_NULL : ret.toString());
		}
	}

	/**
	 * <p>loadReqCtrlList.</p>
	 *
	 * @param context a DefaultExtractorContext object.
	 * @param reqCtrlList a {@link java.util.List} object.
	 * @param dataListElt a {@link org.dom4j.Element} object.
	 */
	protected void loadReqCtrlList(DefaultExtractorContext context, List<RequirementListDataItem> reqCtrlList, Element dataListElt) {

		Element reqCtrlListsElt = dataListElt.addElement(PLMModel.TYPE_REQCTRLLIST.getLocalName() + "s");
		for (RequirementListDataItem r : reqCtrlList) {

			Element reqCtrlListElt = reqCtrlListsElt.addElement(PLMModel.TYPE_REQCTRLLIST.getLocalName());

			List<QName> hiddenFields = new ArrayList<>();
			hiddenFields.add(ContentModel.PROP_NAME);

			if (!RequirementDataType.Specification.equals(r.getReqDataType())) {
				hiddenFields.add(PLMModel.PROP_RCL_SOURCES_V2);
			}

			loadDataListItemAttributes(r, reqCtrlListElt, context, hiddenFields);

		}
	}

	/**
	 * <p>loadProductData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param partProductNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataListItemElt a {@link org.dom4j.Element} object.
	 * @param context a DefaultExtractorContext object.
	 * @param costType a {@link fr.becpg.repo.product.data.constraints.CostType} object.
	 */
	protected void loadProductData(NodeRef entityNodeRef, NodeRef partProductNodeRef, Element dataListItemElt, DefaultExtractorContext context,
			CostType costType) {
		if (partProductNodeRef != null) {

			context.doInDataListContext(() -> {
				loadNodeAttributes(partProductNodeRef, dataListItemElt, false, context);
			});
			extractCost(entityNodeRef, partProductNodeRef, dataListItemElt, costType, context);

			dataListItemElt.addAttribute(ATTR_ITEM_TYPE, entityDictionaryService.toPrefixString(nodeService.getType(partProductNodeRef)));
			dataListItemElt.addAttribute(ATTR_ASPECTS, extractAspects(partProductNodeRef));
		}
	}

	private static final String SHOULD_EXTRACT_COST_CACHE_KEY = "shouldExtractCost";

	private boolean shouldExtractCost(DefaultExtractorContext context) {
		if (context.getCache().containsKey(SHOULD_EXTRACT_COST_CACHE_KEY)) {
			return Boolean.TRUE.equals(context.getCache().get(SHOULD_EXTRACT_COST_CACHE_KEY));
		}
		Boolean shouldExtractCost = !BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_COST)
				.andPropEquals(PLMModel.PROP_COSTTYPE, CostType.Composition.toString()).inDB().list().isEmpty();
		context.getCache().put(SHOULD_EXTRACT_COST_CACHE_KEY, shouldExtractCost);

		return shouldExtractCost;
	}

	private void extractCost(NodeRef entityNodeRef, NodeRef partProductNodeRef, Element dataListItemElt, CostType type,
			DefaultExtractorContext context) {

		if (alfrescoRepository.findOne(entityNodeRef) instanceof ProductData) {
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(entityNodeRef);
			ProductData partProduct = (ProductData) alfrescoRepository.findOne(partProductNodeRef);

			Double currentCost = 0d;
			Double previousCost = 0d;
			Double futureCost = 0d;
			Double totalCurrentCost = 0d;
			Double totalPreviousCost = 0d;
			Double totalFutureCost = 0d;

			for (CostListDataItem c : partProduct.getCostList()) {

				Boolean isFixed = (Boolean) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED);
				if ((isFixed == null) || Boolean.FALSE.equals(isFixed)) {

					String costType = (String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTTYPE);
					String costCurrency = (String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTCURRENCY);
					String productCurrency = (String) nodeService.getProperty(entityNodeRef, PLMModel.PROP_PRICE_CURRENCY);

					if ((productCurrency == null) || (costCurrency == null) || productCurrency.equals(costCurrency)) {

						if (type.toString().equals(costType)) {

							if (c.getValue() != null) {
								currentCost += CostCalculatingHelper.extractValue(formulatedProduct, partProduct, c);
							}

							if (c.getFutureValue() != null) {
								futureCost += c.getFutureValue();
							}

							if (c.getPreviousValue() != null) {
								previousCost += c.getPreviousValue();
							}

						} else if ((c.getDepthLevel() == null) || (c.getDepthLevel() == 1)) {

							if (c.getValue() != null) {
								totalCurrentCost += CostCalculatingHelper.extractValue(formulatedProduct, partProduct, c);
							}

							if (c.getFutureValue() != null) {
								totalFutureCost += c.getFutureValue();
							}

							if (c.getPreviousValue() != null) {
								totalPreviousCost += c.getPreviousValue();
							}

						}
					}
				}
			}

			if (shouldExtractCost(context)) {

				dataListItemElt.addAttribute("currentCost", Double.toString(currentCost));
				dataListItemElt.addAttribute("previousCost", Double.toString(previousCost));
				dataListItemElt.addAttribute("futureCost", Double.toString(futureCost));
			} else {
				dataListItemElt.addAttribute("currentCost", Double.toString(totalCurrentCost));
				dataListItemElt.addAttribute("previousCost", Double.toString(totalPreviousCost));
				dataListItemElt.addAttribute("futureCost", Double.toString(totalFutureCost));
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	protected boolean isMultiLinesAttribute(QName attribute, DefaultExtractorContext context) {
		if (attribute != null) {
			if (attribute.equals(PLMModel.PROP_INSTRUCTION) || attribute.equals(PLMModel.PROP_PRODUCT_COMMENTS)
					|| attribute.equals(ContentModel.PROP_DESCRIPTION)) {
				return true;
			}

			if ((multilineProperties() != null)
					&& context.prefsContains("multilineProperties", multilineProperties(), attribute.toPrefixString(namespaceService))) {
				return true;
			}

		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected QName getPropNameOfType(QName type) {
		if ((type != null) && type.equals(PLMModel.TYPE_CERTIFICATION)) {
			return ContentModel.PROP_TITLE;
		} else if (entityDictionaryService.isSubClass(type, PLMModel.TYPE_PRODUCT)) {
			return ContentModel.PROP_NAME;
		}
		return null;

	}

	/** {@inheritDoc} */
	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return entityDictionaryService.isSubClass(type, PLMModel.TYPE_PRODUCT) ? EntityReportExtractorPriority.NORMAL
				: EntityReportExtractorPriority.NONE;
	}
}
