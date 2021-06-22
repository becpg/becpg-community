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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.CostType;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
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
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamListItem;
import fr.becpg.repo.product.formulation.CostsCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.PackagingHelper;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;
import fr.becpg.repo.report.entity.EntityReportParameters;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>ProductReportExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
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

	@Value("${beCPG.product.report.multiLevel}")
	private Boolean extractInMultiLevel = false;

	@Value("${beCPG.product.report.componentDatalistsToExtract}")
	private String componentDatalistsToExtract = "";

	@Value("${beCPG.product.report.priceBreaks}")
	private Boolean extractPriceBreaks = false;

	@Value("${beCPG.product.report.extractRawMaterial}")
	private Boolean extractRawMaterial = false;

	@Value("${beCPG.product.report.showDeprecatedXml}")
	private Boolean showDeprecated = false;
	
	@Value("${beCPG.product.report.nutList.localesToExtract}")
	private String nutLocalesToExtract;

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
	}

	/**
	 * {@inheritDoc}
	 *
	 * load the datalists of the product data.
	 */
	@Override
	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, DefaultExtractorContext context) {
		loadDataLists(entityNodeRef, dataListsElt, context, true);
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
	private void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, DefaultExtractorContext context, boolean isExtractedProduct) {

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

					if (datalists != null && datalists.containsKey(dataListQName)) {
						// use entityRepository for performances
						@SuppressWarnings({ "rawtypes" })
						List<BeCPGDataObject> dataListItems = (List) datalists.get(dataListQName);

						if ((dataListItems != null) && !dataListItems.isEmpty()) {
							if (shouldExtractList(isExtractedProduct, context, type, dataListQName  )) {

								Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName() + "s");
								addDataListState(dataListElt, listNodeRef);

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
						}
					} else if (!BeCPGModel.TYPE_ACTIVITY_LIST.equals(dataListQName)) {
						// extract specific datalists
						loadDataList(dataListsElt, listNodeRef, dataListQName, context);
					}
				}
			}
		}

		if (productData != null) {
			// lists extracted on entity and raw materials
			if (shouldExtractList(isExtractedProduct, context,type, PLMModel.TYPE_ORGANOLIST)) {
				loadOrganoLists(productData, dataListsElt, context);
			}

			if (shouldExtractList(isExtractedProduct, context,type, PLMModel.TYPE_LABELCLAIMLIST)) {
				loadLabelCLaimLists(productData, dataListsElt, context);
			}

			if (shouldExtractList(isExtractedProduct,context,type, PLMModel.TYPE_INGLIST)) {
				loadIngLists(productData, dataListsElt, context);
			}

			if (shouldExtractList(isExtractedProduct, context,type, PLMModel.TYPE_NUTLIST)) {
				loadNutLists(productData, dataListsElt, context);
			}

			if (!isExtractedProduct && shouldExtractList(isExtractedProduct, context,type, PLMModel.TYPE_DYNAMICCHARACTLIST) ) {
				loadDynamicCharactList(productData.getCompoListView().getDynamicCharactList(), dataListsElt);
			}

			if (shouldExtractList(isExtractedProduct,context,type, PLMModel.TYPE_MICROBIOLIST)) {
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
					addDataListState(microbioListsElt, microbioList.get(0).getParentNodeRef());
					if (productMicrobioCriteriaNodeRef != null) {
						loadNodeAttributes(productMicrobioCriteriaNodeRef, microbioListsElt, false, context);
					}
					for (MicrobioListDataItem dataItem : microbioList) {
						Element nodeElt = microbioListsElt.addElement(PLMModel.TYPE_MICROBIOLIST.getLocalName());
						loadDataListItemAttributes(dataItem, nodeElt, context);
					}
				}
			}

			if (shouldExtractList(isExtractedProduct,context,type, PLMModel.TYPE_ALLERGENLIST)) {

				loadAllergenLists(productData, dataListsElt, context);
			}

			if (context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_IN_MULTILEVEL, extractInMultiLevel) || shouldExtractList(isExtractedProduct,context,type, PLMModel.TYPE_COMPOLIST)) {
				loadCompoList(productData, dataListsElt, context);
			}

			if (shouldExtractList(isExtractedProduct,context,type, PLMModel.TYPE_PACKAGINGLIST)) {

				// packList
				loadPackagingList(productData, dataListsElt, defaultVariantNodeRef, context, isExtractedProduct);

			}

			if (shouldExtractList(isExtractedProduct,context,type, MPMModel.TYPE_PROCESSLIST)) {

				// processList
				loadProcessList(productData, dataListsElt, context, isExtractedProduct);

			}

			if (isExtractedProduct && context.isPrefOn("extractPriceBreaks", extractPriceBreaks)) {

				extractPriceBreaks(productData, dataListsElt);
			}

			// extract RawMaterials
			if (isExtractedProduct && context.isPrefOn("extractRawMaterial", extractRawMaterial)) {

				extractRawMaterials(productData, dataListsElt, context);
			}

			if ( shouldExtractList(isExtractedProduct, context,type, PLMModel.TYPE_INGLABELINGLIST)) {
				// IngLabelingList
				if (productData.getLabelingListView().getIngLabelingList() != null
						&& !productData.getLabelingListView().getIngLabelingList().isEmpty()) {
					Element ingListElt = dataListsElt.addElement(PLMModel.TYPE_INGLABELINGLIST.getLocalName() + "s");
					addDataListState(ingListElt, productData.getLabelingListView().getIngLabelingList().get(0).getParentNodeRef());
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

							if(logger.isTraceEnabled()) {
								logger.trace("ill, locale: " + locale);
							}
							
							String grpName = "";
							if (dataItem.getGrp() != null) {
								MLText grpMLText = (MLText) mlNodeService.getProperty(dataItem.getGrp(), PLMModel.PROP_LABELINGRULELIST_LABEL);
								if ((grpMLText != null) && (grpMLText.getValue(locale) != null) && !grpMLText.getValue(locale).isEmpty()) {
									grpName = grpMLText.getValue(locale);
								} else {
									grpName = (String) nodeService.getProperty(dataItem.getGrp(), ContentModel.PROP_NAME);
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
							addCDATA(ingLabelingElt, PLMModel.ASSOC_ILL_GRP, grpName, null);
							// #4510
							Element cDATAElt = ingLabelingElt.addElement(PLMModel.PROP_ILL_VALUE.getLocalName());
							cDATAElt.addCDATA(dataItem.getValue() != null ? dataItem.getValue().getValue(locale) : VALUE_NULL);
							cDATAElt = ingLabelingElt.addElement(PLMModel.PROP_ILL_MANUAL_VALUE.getLocalName());
							cDATAElt.addCDATA(dataItem.getManualValue() != null ? dataItem.getManualValue().getValue(locale) : VALUE_NULL);

							if(logger.isTraceEnabled()) {
								logger.trace("ingLabelingElt: " + ingLabelingElt.asXML());
							}
						}
					}
				}

			}

		}

	}

	private boolean shouldExtractList(boolean isExtractedProduct, DefaultExtractorContext context, QName type, QName dataListQName) {
		if(isExtractedProduct) {
			if(context.isNotEmptyPrefs(EntityReportParameters.PARAM_ENTITY_DATALISTS_TO_EXTRACT , null) && 
					!context.multiPrefsEquals(EntityReportParameters.PARAM_ENTITY_DATALISTS_TO_EXTRACT, null, entityDictionaryService.toPrefixString(dataListQName))) {
				return false;
			}
			return true;
		}
		
		return context.multiPrefsEquals(EntityReportParameters.PARAM_COMPONENT_DATALISTS_TO_EXTRACT, componentDatalistsToExtract,
				entityDictionaryService.toPrefixString(dataListQName)) || context.multiPrefsEquals(EntityReportParameters.PARAM_COMPONENT_DATALISTS_TO_EXTRACT, componentDatalistsToExtract,
						entityDictionaryService.toPrefixString(dataListQName)+"|"+entityDictionaryService.toPrefixString(type));
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
	
	private void loadCompoList( ProductData productData, Element dataListsElt, DefaultExtractorContext context) {
		// compoList
		if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			Element compoListElt = dataListsElt.addElement(PLMModel.TYPE_COMPOLIST.getLocalName() + "s");
			addDataListState(compoListElt, productData.getCompoList().get(0).getParentNodeRef());

			for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				if (dataItem.getProduct() != null) {

					ProductData subProductData = (ProductData) alfrescoRepository.findOne(dataItem.getProduct());

					Double parentLossRatio = FormulationHelper.getComponentLossPerc(subProductData, dataItem);
					Double qty = dataItem.getQty() != null ? dataItem.getQty() : 0d;
					Double qtyForCost = FormulationHelper.getQtyForCost(dataItem, 0d, subProductData,
							CostsCalculatingFormulationHandler.keepProductUnit);

					loadCompoListItem(productData.getNodeRef(), null, dataItem, subProductData, compoListElt, 1, qty, qtyForCost, parentLossRatio, context);
				}
			}

			loadDynamicCharactList(productData.getCompoListView().getDynamicCharactList(), compoListElt);
			loadReqCtrlList(context, productData.getReqCtrlList(), compoListElt);
		}

	}

	private void loadProcessList(ProductData productData, Element dataListsElt, DefaultExtractorContext context, boolean isExtractedProduct) {
		if (productData.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			Element processListElt = dataListsElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName() + "s");
			addDataListState(processListElt, productData.getProcessList().get(0).getParentNodeRef());

			for (ProcessListDataItem dataItem : productData.getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				Double qty = dataItem.getQty() != null ? dataItem.getQty() : 0d;

				if ((qty == null) || (qty == 0d)) {
					qty = 1d;
				}

				if ((dataItem.getRateProduct() != null) && (dataItem.getRateProduct() != 0)) {
					qty /= dataItem.getRateProduct();
				}

				if (dataItem.getQtyResource() != null) {
					qty *= dataItem.getQtyResource();
				}

				Double qtyForCost = FormulationHelper.getQty(productData, dataItem);

				ProductData subProductData = null;

				if ((dataItem.getResource() != null) && nodeService.exists(dataItem.getResource())) {
					subProductData = (ProductData) alfrescoRepository.findOne(dataItem.getResource());
				}

				loadProcessListItem(productData.getNodeRef(), dataItem, subProductData, processListElt, 1, qty, qtyForCost, context);
			}

			if (context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_IN_MULTILEVEL, extractInMultiLevel) && isExtractedProduct) {

				if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

					for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
						if ((dataItem.getProduct() != null) && nodeService.exists(dataItem.getProduct())) {

							if ((nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
									|| nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))) {
								ProductData subProductData = (ProductData) alfrescoRepository.findOne(dataItem.getProduct());

								Double parentLossRatio = FormulationHelper.getComponentLossPerc(subProductData, dataItem);
								Double qty = dataItem.getQty() != null ? dataItem.getQty() : 0d;
								Double qtyForCost = FormulationHelper.getQtyForCost(dataItem, 0d, subProductData,
										CostsCalculatingFormulationHandler.keepProductUnit);

								loadProcessListItemForCompo(productData.getNodeRef(), dataItem, subProductData, processListElt, 1, qty, qtyForCost, parentLossRatio, context);
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

		if (productData.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

			Element packagingListElt = dataListsElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName() + "s");
			addDataListState(packagingListElt, productData.getPackagingList().get(0).getParentNodeRef());

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
			if (productData.hasPackagingListEl()) {
				for (PackagingListDataItem dataItem : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					loadPackagingItem(productData.getNodeRef(), 1d, 0d, dataItem, packagingListElt, defaultVariantNodeRef, 
							defaultVariantPackagingData, context, 1, false, false);
				}
			}

			if (context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_IN_MULTILEVEL, extractInMultiLevel) && isExtractedProduct) {

				if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

					for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
						if ((dataItem.getProduct() != null) && nodeService.exists(dataItem.getProduct())) {

							if ((nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
									|| nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))) {

								ProductData subProductData = (ProductData) alfrescoRepository.findOne(dataItem.getProduct());

								Double parentLossRatio = FormulationHelper.getComponentLossPerc(subProductData, dataItem);
								Double qty = dataItem.getQty() != null ? dataItem.getQty() : 0d;
								Double qtyForCost = FormulationHelper.getQtyForCost(dataItem, 0d, subProductData,
										CostsCalculatingFormulationHandler.keepProductUnit);
								
								loadPackagingListItemForCompo(productData.getNodeRef(), dataItem, subProductData, packagingListElt, 1, qty, qtyForCost, parentLossRatio,
										context, defaultVariantNodeRef, defaultVariantPackagingData, 
										productData.getDropPackagingOfComponents() != null && productData.getDropPackagingOfComponents());

							}
						}
					}
				}
			}

			loadDynamicCharactList(productData.getPackagingListView().getDynamicCharactList(), packagingListElt);
		}
	}

	private void loadPackagingListItemForCompo(NodeRef entityNodeRef, CompoListDataItem compoListItem, ProductData productData, Element packagingListElt, int level,
			double qty, double qtyForCost, double parentLossRatio, DefaultExtractorContext context, NodeRef defaultVariantNodeRef,
			VariantPackagingData defaultVariantPackagingData, boolean dropPackagingOfComponents) {
		
		if(level > 20) {
			//Avoid infinite loop
			return;
		}

		if (productData.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

			Element partElt = packagingListElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName());

			ProductData subProductData = (ProductData) alfrescoRepository.findOne(compoListItem.getComponent());

			loadProductData(entityNodeRef, compoListItem.getComponent(), partElt, context, CostType.Packaging);
			loadDataListItemAttributes(compoListItem, partElt, context);
			partElt.addAttribute(PLMModel.ASSOC_PACKAGINGLIST_PRODUCT.getLocalName(),
					(String) nodeService.getProperty(compoListItem.getComponent(), ContentModel.PROP_NAME));
			Double lossPerc = FormulationHelper.getComponentLossPerc(subProductData, compoListItem);

			partElt.addAttribute(PLMModel.PROP_PACKAGINGLIST_LOSS_PERC.getLocalName(), lossPerc != null ? lossPerc.toString() : "");
			partElt.addAttribute(PLMModel.PROP_PACKAGINGLIST_QTY.getLocalName(),
					compoListItem.getQtySubFormula() != null ? compoListItem.getQtySubFormula().toString() : "");
			partElt.addAttribute(PLMModel.PROP_PACKAGINGLIST_UNIT.getLocalName(),
					compoListItem.getCompoListUnit() != null ? compoListItem.getCompoListUnit().toString() : "");

			partElt.addAttribute(ATTR_PACKAGING_QTY_FOR_PRODUCT, Double.toString(qty));
			partElt.addAttribute(ATTR_QTY_FOR_COST, Double.toString(qtyForCost));

			extractVariants(((AbstractEffectiveVariantListDataItem) compoListItem).getVariants(), partElt);

			partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(level));

			for (PackagingListDataItem packagingListDataItem : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				loadPackagingItem(entityNodeRef, qtyForCost, parentLossRatio, packagingListDataItem, packagingListElt, defaultVariantNodeRef,
						defaultVariantPackagingData, context, level + 1, dropPackagingOfComponents, true);

			}
		}

		if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			for (CompoListDataItem subDataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				if ((subDataItem.getProduct() != null) && nodeService.exists(subDataItem.getProduct())) {

					if ((nodeService.getType(subDataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
							|| nodeService.getType(subDataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))) {
						ProductData subProductData = (ProductData) alfrescoRepository.findOne(subDataItem.getProduct());

						Double subQty = (FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT) != 0)
								&& (subDataItem.getQty() != null)
										? (qty * subDataItem.getQty())
												/ FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT)
										: 0d;

						Double subQtyForCost = (FormulationHelper.getQtyForCost(subDataItem, parentLossRatio, subProductData,
								CostsCalculatingFormulationHandler.keepProductUnit) / FormulationHelper.getNetQtyForCost(productData)) * qtyForCost;

						Double newLossPerc = FormulationHelper.getComponentLossPerc(subProductData, subDataItem);

						loadPackagingListItemForCompo(entityNodeRef, subDataItem, subProductData, packagingListElt, level + 1, subQty, subQtyForCost, newLossPerc,
								context, defaultVariantNodeRef, defaultVariantPackagingData, 
								dropPackagingOfComponents || (productData.getDropPackagingOfComponents() != null && productData.getDropPackagingOfComponents()));

					}

				}
			}
		}

	}

	private void loadProcessListItemForCompo(NodeRef entityNodeRef, CompoListDataItem dataItem, ProductData productData, Element processListElt, int level, double qty,
			double qtyForCost, double parentLossRatio, DefaultExtractorContext context) {
		
		if(level > 20) {
			//Avoid infinite loop
			return;
		}

		if (productData.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			loadProcessListItem(entityNodeRef, dataItem, productData, processListElt, level, qty, qtyForCost, context);
		}

		if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			for (CompoListDataItem subDataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				if ((subDataItem.getProduct() != null) && nodeService.exists(subDataItem.getProduct())) {

					if ((nodeService.getType(subDataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
							|| nodeService.getType(subDataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))) {

						ProductData subProductData = (ProductData) alfrescoRepository.findOne(subDataItem.getProduct());

						Double subQty = (FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT) != 0)
								&& (subDataItem.getQty() != null)
										? (qty * subDataItem.getQty())
												/ FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT)
										: 0d;

						Double subQtyForCost = (FormulationHelper.getQtyForCost(subDataItem, parentLossRatio, subProductData,
								CostsCalculatingFormulationHandler.keepProductUnit) / FormulationHelper.getNetQtyForCost(productData)) * qtyForCost;

						Double newLossPerc = FormulationHelper.getComponentLossPerc(subProductData, subDataItem);

						loadProcessListItemForCompo(entityNodeRef,subDataItem, subProductData, processListElt, level + 1, subQty, subQtyForCost, newLossPerc,
								context);
					}

				}
			}

		}

	}

	private void loadProcessListItem(NodeRef entityNodeRef, CompositionDataItem dataItem, ProductData productData, Element processListElt, int level, Double qty,
			Double qtyForCost, DefaultExtractorContext context) {

		if(level > 20) {
			//Avoid infinite loop
			return;
		}
		
		Element partElt = processListElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName());
		loadProductData(entityNodeRef, dataItem.getComponent(), partElt, context, CostType.Process);
		loadDataListItemAttributes((BeCPGDataObject) dataItem, partElt, context);
		if ((dataItem instanceof CompoListDataItem) && (dataItem.getComponent() != null)) {
			partElt.addAttribute(MPMModel.ASSOC_PL_RESOURCE.getLocalName(),
					(String) nodeService.getProperty(dataItem.getComponent(), ContentModel.PROP_NAME));
			CompoListDataItem compoListItem = (CompoListDataItem) dataItem;
			partElt.addAttribute(MPMModel.PROP_PL_QTY_RESOURCE.getLocalName(),
					compoListItem.getQtySubFormula() != null ? compoListItem.getQtySubFormula().toString() : "");
		}

		partElt.addAttribute(ATTR_PROCESS_QTY_FOR_PRODUCT, Double.toString(qty));
		partElt.addAttribute(ATTR_QTY_FOR_COST, Double.toString(qtyForCost));

		extractVariants(((AbstractEffectiveVariantListDataItem) dataItem).getVariants(), partElt);

		partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), Integer.toString(level));

		if (productData != null) {

			if (productData instanceof ResourceProductData) {
				loadResourceParams((ResourceProductData) productData, partElt, context);
			}

			if (productData.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				for (ProcessListDataItem subDataItem : productData.getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

					Double subQty = dataItem.getQty() != null ? dataItem.getQty() : 0d;

					if ((subQty == null) || (subQty == 0d)) {
						subQty = 1d;
					}

					if ((subDataItem.getRateProduct() != null) && (subDataItem.getRateProduct() != 0)) {
						subQty /= subDataItem.getRateProduct();
					}

					if (subDataItem.getQtyResource() != null) {

						subQty *= subDataItem.getQtyResource();
					}

					Double subQtyForCost = (FormulationHelper.getQty(productData, subDataItem) / FormulationHelper.getNetQtyForCost(productData))
							* qtyForCost;

					ProductData subProductData = null;

					if ((subDataItem.getResource() != null) && nodeService.exists(subDataItem.getResource())) {

						subProductData = (ProductData) alfrescoRepository.findOne(subDataItem.getResource());

					}

					loadProcessListItem(entityNodeRef,subDataItem, subProductData, processListElt, level + 1, subQty * qty, subQtyForCost, context);
				}
			}
		}

	}

	private void loadCompoListItem(NodeRef entityNodeRef, CompoListDataItem parentDataItem, CompoListDataItem dataItem, ProductData componentProductData, Element compoListElt,
			int level, double qty, double qtyForCost, double parentLossRatio, DefaultExtractorContext context) {
		
		if(level > 20) {
			//Avoid infinite loop
			return;
		}
		
		if ((dataItem.getProduct() != null) && nodeService.exists(dataItem.getProduct())) {

			Element partElt = compoListElt.addElement(PLMModel.TYPE_COMPOLIST.getLocalName());
			loadProductData(entityNodeRef ,dataItem.getComponent(), partElt, context, CostType.Composition);
			loadDataListItemAttributes(dataItem, partElt, context);
			partElt.addAttribute(ATTR_COMPOLIST_QTY_FOR_PRODUCT, Double.toString(qty));

			partElt.addAttribute(ATTR_QTY_FOR_COST, Double.toString(qtyForCost));

			extractVariants(dataItem.getVariants(), partElt);

			Integer depthLevel = dataItem.getDepthLevel();
			if (depthLevel != null) {
				level = depthLevel - 1 + level;
				partElt.addAttribute(BeCPGModel.PROP_DEPTH_LEVEL.getLocalName(), "" + level);
				partElt.addAttribute(ATTR_NODEREF, dataItem.getNodeRef().toString());
				if (parentDataItem != null) {
					partElt.addAttribute(ATTR_PARENT_NODEREF, parentDataItem.getNodeRef().toString());
				}
			}

			Element dataListsElt = null;
			if (context.isNotEmptyPrefs(EntityReportParameters.PARAM_COMPONENT_DATALISTS_TO_EXTRACT, componentDatalistsToExtract)) {
				dataListsElt = partElt.addElement(TAG_DATALISTS);
				loadDataLists(dataItem.getProduct(), dataListsElt, context, false);
			}

			if (context.isPrefOn(EntityReportParameters.PARAM_EXTRACT_IN_MULTILEVEL, extractInMultiLevel)) {

				if ((nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
						|| nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_FINISHEDPRODUCT))) {

					if (componentProductData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
						if (dataListsElt != null) {
							loadDynamicCharactList(componentProductData.getCompoListView().getDynamicCharactList(), dataListsElt);
						}

						for (CompoListDataItem subDataItem : componentProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

							ProductData subProductData = (ProductData) alfrescoRepository.findOne(subDataItem.getProduct());

							Double subQty = (FormulationHelper.getNetWeight(componentProductData, FormulationHelper.DEFAULT_NET_WEIGHT) != 0)
									&& (subDataItem.getQty() != null)
											? (qty * subDataItem.getQty())
													/ FormulationHelper.getNetWeight(componentProductData, FormulationHelper.DEFAULT_NET_WEIGHT)
											: 0d;

							Double subQtyForCost = (FormulationHelper.getQtyForCost(subDataItem, 0d, subProductData,
									CostsCalculatingFormulationHandler.keepProductUnit) / FormulationHelper.getNetQtyForCost(componentProductData))
									* qtyForCost;

							Double newLossPerc = FormulationHelper.getComponentLossPerc(subProductData, subDataItem);

							loadCompoListItem(entityNodeRef, dataItem, subDataItem, subProductData, compoListElt, level + 1, subQty, subQtyForCost, newLossPerc,
									context);

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
			addDataListState(resourceListsElt, productData.getResourceParamList().get(0).getParentNodeRef());

			for (ResourceParamListItem resourceParamListItem : productData.getResourceParamList()) {

				Element ressourceListElt = resourceListsElt.addElement(MPMModel.TYPE_RESOURCEPARAMLIST.getLocalName());

				loadDataListItemAttributes(resourceParamListItem, ressourceListElt, context);

			}
		}
	}

	private void loadNutLists(ProductData productData, Element dataListsElt, DefaultExtractorContext context) {

		if ((productData.getNutList() != null) && !productData.getNutList().isEmpty()) {

			Element nutListsElt = dataListsElt.addElement(PLMModel.TYPE_NUTLIST.getLocalName() + "s");
			addDataListState(nutListsElt, productData.getNutList().get(0).getParentNodeRef());

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

					if (dataListItem.getErrorLog() != null && !dataListItem.getErrorLog().isEmpty()) {
						nutListElt.addAttribute(PLMModel.PROP_NUTLIST_FORMULA_ERROR.getLocalName(), "Error");
					}
					nutListElt.addAttribute(RegulationFormulationHelper.ATTR_NUT_CODE, nut.getNutCode());
					nutListElt.addAttribute(BeCPGModel.PROP_COLOR.getLocalName(), nut.getNutColor());
					boolean isDisplayed = isCharactDisplayedForLocale(dataListItem.getNut());
					
					
					RegulationFormulationHelper.extractXMLAttribute(nutListElt, dataListItem.getRoundedValue(), I18NUtil.getLocale(), isDisplayed, context.getPrefValue("nutLocalesToExtract", nutLocalesToExtract));

					if (showDeprecated) {

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
			addDataListState(organoListsElt, productData.getOrganoList().get(0).getParentNodeRef());

			for (OrganoListDataItem dataListItem : productData.getOrganoList()) {
				Element organoListElt = organoListsElt.addElement(PLMModel.TYPE_ORGANOLIST.getLocalName());
				loadDataListItemAttributes(dataListItem, organoListElt, context);
			}
		}
	}

	private void loadLabelCLaimLists(ProductData productData, Element dataListsElt, DefaultExtractorContext context) {
		if ((productData.getLabelClaimList() != null) && !productData.getLabelClaimList().isEmpty()) {
			Element lcListsElt = dataListsElt.addElement(PLMModel.TYPE_LABELCLAIMLIST.getLocalName() + "s");
			addDataListState(lcListsElt, productData.getLabelClaimList().get(0).getParentNodeRef());

			for (LabelClaimListDataItem dataListItem : productData.getLabelClaimList()) {
				Element lcListElt = lcListsElt.addElement(PLMModel.TYPE_LABELCLAIMLIST.getLocalName());
				loadDataListItemAttributes(dataListItem, lcListElt, context);

				boolean isDisplay = isCharactDisplayedForLocale(dataListItem.getLabelClaim());
				String displayMode = (isDisplay? "O" : "");
				lcListElt.addAttribute("regulDisplayMode", displayMode);
			}
		}
	}

	private void loadAllergenLists(ProductData productData, Element dataListsElt, DefaultExtractorContext context) {
		if ((productData.getAllergenList() != null) && !productData.getAllergenList().isEmpty()) {
			Element allergenListsElt = dataListsElt.addElement(PLMModel.TYPE_ALLERGENLIST.getLocalName() + "s");
			addDataListState(allergenListsElt, productData.getAllergenList().get(0).getParentNodeRef());

			String volAllergens = "";
			String inVolAllergens = "";
			String inVolAllergensProcess = "";
			String inVolAllergensRawMaterial = "";

			for (AllergenListDataItem dataListItem : productData.getAllergenList()) {
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
							if (dataListItem.getVoluntary()) {
								if (volAllergens.isEmpty()) {
									volAllergens = allergen;
								} else {
									volAllergens += RepoConsts.LABEL_SEPARATOR + allergen;
								}
							} else if (dataListItem.getInVoluntary()) {
								if (inVolAllergens.isEmpty()) {
									inVolAllergens = allergen;
								} else {
									inVolAllergens += RepoConsts.LABEL_SEPARATOR + allergen;
								}
								boolean presentInRawMaterial = false;
								boolean presentInProcess = false;
								for (NodeRef inVoluntarySource : dataListItem.getInVoluntarySources()) {
									QName inVoluntarySourceType = nodeService.getType(inVoluntarySource);

									if (!presentInRawMaterial && PLMModel.TYPE_RAWMATERIAL.equals(inVoluntarySourceType)) {
										if (inVolAllergensRawMaterial.isEmpty()) {
											inVolAllergensRawMaterial = allergen;
										} else {
											inVolAllergensRawMaterial += RepoConsts.LABEL_SEPARATOR + allergen;
										}
										presentInRawMaterial = true;
									} else if (!presentInProcess && PLMModel.TYPE_RESOURCEPRODUCT.equals(inVoluntarySourceType)) {
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
				}
				allergenListElt.addAttribute("regulDisplayMode", displayMode);
			}

			allergenListsElt.addAttribute(PLMModel.PROP_ALLERGENLIST_VOLUNTARY.getLocalName(), volAllergens);
			allergenListsElt.addAttribute(PLMModel.PROP_ALLERGENLIST_INVOLUNTARY.getLocalName(), inVolAllergens);
			allergenListsElt.addAttribute(ATTR_ALLERGENLIST_INVOLUNTARY_FROM_PROCESS, inVolAllergensProcess);
			allergenListsElt.addAttribute(ATTR_ALLERGENLIST_INVOLUNTARY_FROM_RAW_MATERIAL, inVolAllergensRawMaterial);
		}
	}

	private void loadIngLists(ProductData productData, Element dataListsElt, DefaultExtractorContext context) {
		if ((productData.getIngList() != null) && !productData.getIngList().isEmpty()) {
			Element ingListsElt = dataListsElt.addElement(PLMModel.TYPE_INGLIST.getLocalName() + "s");
			addDataListState(ingListsElt, productData.getIngList().get(0).getParentNodeRef());

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
		rawMaterials = getRawMaterials(productData, rawMaterials, productNetWeight);
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
			addCDATA(rawMaterialElt, PLMModel.PROP_COMPOLIST_QTY, toString((100 * entry.getValue()) / (totalQty!=0d ? totalQty:1d)), null);
			if (productNetWeight != 0d) {
				Element cDATAElt = rawMaterialElt.addElement(ATTR_COMPOLIST_QTY_FOR_PRODUCT);
				cDATAElt.addCDATA(
						toString((100 * entry.getValue()) / FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT)));
			}
			Element rawMaterialDataListsElt = rawMaterialElt.addElement(TAG_DATALISTS);
			loadDataLists(entry.getKey(), rawMaterialDataListsElt, context, false);
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
		for (Long projectedQty : ret.keySet()) {
			List<PriceBreakReportData> tmp = ret.get(projectedQty);

			for (Long projectedQty2 : ret.keySet()) {
				if (projectedQty2 < projectedQty) {
					for (PriceBreakReportData pbrd : ret.get(projectedQty2)) {
						boolean add = true;
						for (PriceBreakReportData pbrd2 : tmp) {
							if (pbrd2.getProduct().equals(pbrd.getProduct())) {
								add = false;
								break;
							}
						}
						if (add) {
							tmp.add(pbrd);
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
			Double totalSimulatedValue = 0d;

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
			priceBreakElt.addAttribute("simulatedValue", totalSimulatedValue.toString());
			priceBreakElt.addAttribute("unitTotalCost", unitTotalCost.toString());

		}

	}

	private void extractPriceBreaks(ProductData productData, Double parentLossRatio, Double parentQty, List<PriceBreakReportData> priceBreaks) {

		Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

		for (CompoListDataItem compoList : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			NodeRef productNodeRef = compoList.getProduct();
			if ((productNodeRef != null) && !DeclarationType.Omit.equals(compoList.getDeclType())) {
				QName type = nodeService.getType(productNodeRef);
				Double qty = FormulationHelper.getQtyInKg(compoList);

				ProductData subProductData = (ProductData) alfrescoRepository.findOne(compoList.getProduct());

				Double qtyForCost = FormulationHelper.getQtyForCost(compoList, parentLossRatio, subProductData, false);

				if (logger.isDebugEnabled()) {
					logger.debug("Get rawMaterial " + nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME) + "qty: " + qty + " netWeight "
							+ netWeight + " parentQty " + parentQty);
				}
				if ((qty != null) && (netWeight != 0d)) {
					qty = (parentQty * qty * FormulationHelper.getYield(compoList)) / (100 * netWeight);

					qtyForCost = (parentQty * qtyForCost * FormulationHelper.getYield(compoList)) / (100 * netWeight);

					ProductData componentProduct = (ProductData) alfrescoRepository.findOne(productNodeRef);

					if (type.isMatch(PLMModel.TYPE_RAWMATERIAL)) {

						createPriceBreakReportData(productData, componentProduct, qty, qtyForCost, priceBreaks);

					} else if (type.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)) {
						continue;
					} else {
						Double lossPerc = FormulationHelper.getComponentLossPerc(componentProduct, compoList);
						Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);

						extractPriceBreaks(componentProduct, newLossPerc, qty, priceBreaks);
					}
				}
			}
		}

		extractPriceBreaksForPackaging(productData, priceBreaks, parentQty / (netWeight!=0d ?netWeight : 1d));
	}

	private void extractPriceBreaksForPackaging(ProductData productData, List<PriceBreakReportData> priceBreaks, Double parentQty) {

		for (PackagingListDataItem packagingListDataItem : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			ProductData componentProduct = (ProductData) alfrescoRepository.findOne(packagingListDataItem.getComponent());

			Double qtyForCost = FormulationHelper.getQtyForCostByPackagingLevel(productData, packagingListDataItem, componentProduct);

			if (componentProduct instanceof PackagingKitData) {
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

	private Map<NodeRef, Double> getRawMaterials(ProductData productData, Map<NodeRef, Double> rawMaterials, Double parentQty) {

		for (CompoListDataItem compoList : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			NodeRef productNodeRef = compoList.getProduct();
			if ((productNodeRef != null) && !DeclarationType.Omit.equals(compoList.getDeclType())) {
				QName type = nodeService.getType(productNodeRef);
				Double qty = FormulationHelper.getQtyInKg(compoList);
				Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
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
						getRawMaterials((ProductData) alfrescoRepository.findOne(productNodeRef), rawMaterials, qty);
					}
				}
			}
		}

		return rawMaterials;
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

	private void loadPackagingItem(NodeRef entityNodeRef,double sfQty, double parentLossRatio, PackagingListDataItem dataItem, Element packagingListElt,
			NodeRef defaultVariantNodeRef, VariantPackagingData defaultVariantPackagingData, DefaultExtractorContext context, int level, 
			boolean dropPackagingOfComponents, boolean isPackagingOfComponent) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			loadPackagingKit(entityNodeRef, sfQty, parentLossRatio, dataItem, packagingListElt, defaultVariantNodeRef, 
					defaultVariantPackagingData, context, level, dropPackagingOfComponents, isPackagingOfComponent);
			Element imgsElt = (Element) packagingListElt.getDocument().selectSingleNode(TAG_ENTITY + "/" + TAG_IMAGES);
			if (imgsElt != null) {
				extractEntityImages(dataItem.getProduct(), imgsElt, context);
			}
		} else {
			loadPackaging(entityNodeRef,sfQty, parentLossRatio, dataItem, packagingListElt, defaultVariantNodeRef, 
					defaultVariantPackagingData, context, level, dropPackagingOfComponents, isPackagingOfComponent);
		}
	}

	private Element loadPackaging(NodeRef entityNodeRef,double sfQtyForCost, double parentLossRatio, PackagingListDataItem dataItem, Element packagingListElt,
			NodeRef defaultVariantNodeRef, VariantPackagingData defaultVariantPackagingData, DefaultExtractorContext context, int level, 
			boolean dropPackagingOfComponents, boolean isPackagingOfComponent) {

		Element partElt = packagingListElt.addElement(PLMModel.TYPE_PACKAGINGLIST.getLocalName());
		loadProductData( entityNodeRef, dataItem.getComponent(), partElt, context, CostType.Packaging);
		if(dataItem.getIsRecycle() != null && dataItem.getIsRecycle()){
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
		
		if(packLevel == null) {
			packLevel = PackagingLevel.Primary;
		}

		if ((dataItem.getQty() != null) && (dataItem.getPackagingListUnit() != null)) {
			double sfQty = sfQtyForCost / (1 + (parentLossRatio / 100));
			// we display the quantity used in the SF
			// partElt.addAttribute(PLMModel.PROP_PACKAGINGLIST_QTY.getLocalName(),
			// Double.toString(dataItem.getQty() * sfQty));
			double qty = ProductUnit.PP.equals(dataItem.getPackagingListUnit()) ? 1 : dataItem.getQty();
			if ((dataItem.getPackagingListUnit() != null) && dataItem.getPackagingListUnit().isWeight()) {
				qty = qty / dataItem.getPackagingListUnit().getUnitFactor();
			}
			Double qtyForProduct = 0d;
			if (packLevel.equals(PackagingLevel.Primary)) {
				qtyForProduct = qty * sfQty;
			} else if (packLevel.equals(PackagingLevel.Secondary) && (defaultVariantPackagingData.getProductPerBoxes() != null)
					&& (defaultVariantPackagingData.getProductPerBoxes() != 0)) {
				qtyForProduct = (qty * sfQty) / defaultVariantPackagingData.getProductPerBoxes();
			} else if (packLevel.equals(PackagingLevel.Tertiary) && (defaultVariantPackagingData.getProductPerPallet() != null)
					&& (defaultVariantPackagingData.getProductPerPallet() != 0)) {
				qtyForProduct = (qty * sfQty) / defaultVariantPackagingData.getProductPerPallet();
			}
			partElt.addAttribute(ATTR_PACKAGING_QTY_FOR_PRODUCT, Double.toString(qtyForProduct));
			partElt.addAttribute(ATTR_QTY_FOR_COST, Double.toString(
					qtyForProduct * (1 + (parentLossRatio / 100)) * (1 + ((dataItem.getLossPerc() != null ? dataItem.getLossPerc() : 0d) / 100))));
			
			partElt.addAttribute(PLMModel.PROP_PRODUCT_DROP_PACKAGING_OF_COMPONENTS.getLocalName(), Boolean.toString((!packLevel.equals(PackagingLevel.Primary) && isPackagingOfComponent) || dropPackagingOfComponents));
		} 
		return partElt;
	}

	private void loadPackagingKit(NodeRef entityNodeRef, double sfQty, double parentLossRatio, PackagingListDataItem dataItem, Element packagingListElt,
			NodeRef defaultVariantNodeRef, VariantPackagingData defaultVariantPackagingData, DefaultExtractorContext context, int level, 
			boolean dropPackagingOfComponents, boolean isPackagingOfComponent) {
		loadPackaging(entityNodeRef, sfQty, parentLossRatio, dataItem, packagingListElt, defaultVariantNodeRef, 
				defaultVariantPackagingData, context, level, dropPackagingOfComponents, isPackagingOfComponent);
		ProductData packagingKitData = (ProductData) alfrescoRepository.findOne(dataItem.getProduct());
		if (packagingKitData.hasPackagingListEl()) {
			for (PackagingListDataItem p : packagingKitData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				if ((dataItem.getVariants() != null) && !dataItem.getVariants().isEmpty()) {
					p.setVariants(dataItem.getVariants());
				}
				loadPackagingItem( entityNodeRef, sfQty, parentLossRatio, p, packagingListElt, defaultVariantNodeRef, defaultVariantPackagingData, context,
						level + 1, dropPackagingOfComponents, isPackagingOfComponent);
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
				if (variant.getIsDefaultVariant()) {
					defaultVariantNodeRef = variant.getNodeRef();
				}

				Element variantElt = variantsElt.addElement(BeCPGModel.TYPE_VARIANT.getLocalName());
				variantElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), variant.getName());
				variantElt.addAttribute(BeCPGModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), Boolean.toString(variant.getIsDefaultVariant()));
			}
		} else {
			Element variantElt = variantsElt.addElement(BeCPGModel.TYPE_VARIANT.getLocalName());
			variantElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), "");
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
			String variantNames = "";
			for (NodeRef variantNodeRef : variantNodeRefs) {
				if (isDefault != null) {
					variantNames += ",";
				}

				variantNames += ((String) nodeService.getProperty(variantNodeRef, ContentModel.PROP_NAME));

				if ((isDefault == null) || !isDefault) {
					isDefault = (Boolean) nodeService.getProperty(variantNodeRef, BeCPGModel.PROP_IS_DEFAULT_VARIANT);
				}

				if (isDefault == null) {
					isDefault = false;
				}

			}
			dataItemElt.addAttribute(BeCPGModel.PROP_IS_DEFAULT_VARIANT.getLocalName(), isDefault!=null ? isDefault.toString(): Boolean.FALSE.toString());
			dataItemElt.addAttribute(BeCPGModel.PROP_VARIANTIDS.getLocalName(), variantNames);
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
			if(dc.getValue() !=null ) {
				ret = JsonFormulaHelper.cleanCompareJSON(dc.getValue().toString());
			}
			dynamicCharact.addAttribute(PLMModel.PROP_DYNAMICCHARACT_VALUE.getLocalName(),
					ret == null ? VALUE_NULL :ret.toString());
		}
	}

	/**
	 * <p>loadReqCtrlList.</p>
	 *
	 * @param context a DefaultExtractorContext object.
	 * @param reqCtrlList a {@link java.util.List} object.
	 * @param dataListElt a {@link org.dom4j.Element} object.
	 */
	protected void loadReqCtrlList(DefaultExtractorContext context, List<ReqCtrlListDataItem> reqCtrlList, Element dataListElt) {
		
		Element reqCtrlListsElt = dataListElt.addElement(PLMModel.TYPE_REQCTRLLIST.getLocalName() + "s");
		for (ReqCtrlListDataItem r : reqCtrlList) {
					
			Element reqCtrlListElt = reqCtrlListsElt.addElement(PLMModel.TYPE_REQCTRLLIST.getLocalName());
			
			List<QName> hiddenFields = new ArrayList<>();
			hiddenFields.add(ContentModel.PROP_NAME);
			
			if(!RequirementDataType.Specification.equals(r.getReqDataType())){
				hiddenFields.add(PLMModel.ASSOC_RCL_SOURCES);
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
	protected void loadProductData(NodeRef entityNodeRef, NodeRef partProductNodeRef, Element dataListItemElt, DefaultExtractorContext context, CostType costType) {
		if (partProductNodeRef != null) {

			context.doInDataListContext(() -> {
				loadNodeAttributes(partProductNodeRef, dataListItemElt, false, context);
			});
			extractCost(entityNodeRef, partProductNodeRef, dataListItemElt, costType);

			dataListItemElt.addAttribute(ATTR_ITEM_TYPE, entityDictionaryService.toPrefixString(nodeService.getType(partProductNodeRef)));
			dataListItemElt.addAttribute(ATTR_ASPECTS, extractAspects(partProductNodeRef));
		}
	}

	// TODO cache
	private boolean shouldExtractCost() {
		return !BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_COST).andPropEquals(PLMModel.PROP_COSTTYPE, CostType.Composition.toString())
				.inDB().list().isEmpty();
	}

	private void extractCost(NodeRef entityNodeRef, NodeRef partProductNodeRef, Element dataListItemElt, CostType type) {

		ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(partProductNodeRef);

		Double currentCost = 0d;
		Double previousCost = 0d;
		Double futureCost = 0d;
		Double totalCurrentCost = 0d;
		Double totalPreviousCost = 0d;
		Double totalFutureCost = 0d;

		for (CostListDataItem c : formulatedProduct.getCostList()) {

			Boolean isFixed = (Boolean) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED);
			if ( isFixed == null ||  Boolean.FALSE.equals(isFixed)) {

				String costType = (String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTTYPE);
				String costCurrency = (String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTCURRENCY);
				String productCurrency = (String) nodeService.getProperty(entityNodeRef, PLMModel.PROP_PRICE_CURRENCY);

				if ((productCurrency == null) || (costCurrency == null) || productCurrency.equals(costCurrency)) {

					if (c.getValue() != null) {

						if (type.toString().equals(costType)) {

							currentCost += c.getValue();

							if (c.getFutureValue() != null) {
								futureCost += c.getFutureValue();
							}

							if (c.getPreviousValue() != null) {
								previousCost += c.getPreviousValue();
							}

						} else if ((c.getDepthLevel() == null) || (c.getDepthLevel() == 1)) {

							totalCurrentCost += c.getValue();

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
		}

		if (shouldExtractCost()) {

			dataListItemElt.addAttribute("currentCost", Double.toString(currentCost));
			dataListItemElt.addAttribute("previousCost", Double.toString(previousCost));
			dataListItemElt.addAttribute("futureCost", Double.toString(futureCost));
		} else {
			dataListItemElt.addAttribute("currentCost", Double.toString(totalCurrentCost));
			dataListItemElt.addAttribute("previousCost", Double.toString(totalPreviousCost));
			dataListItemElt.addAttribute("futureCost", Double.toString(totalFutureCost));
		}

	}

	/** {@inheritDoc} */
	@Override
	protected boolean isMultiLinesAttribute(QName attribute, DefaultExtractorContext context) {
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

			if ((multilineProperties != null)
					&& context.prefsContains("multilineProperties", multilineProperties, attribute.toPrefixString(namespaceService))) {
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
