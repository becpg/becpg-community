package fr.becpg.repo.product.report;

import java.util.ArrayList;
import java.util.HashMap;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AbstractManualVariantListDataItem;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.PackagingLevel;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.report.entity.impl.AbstractEntityReportExtractor;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.variant.model.VariantData;

//TODO use annotation on product data instead
@Deprecated
public class DefaultProductReportExtractor extends AbstractEntityReportExtractor {

	/** The Constant KEY_PRODUCT_IMAGE. */
	protected static final String KEY_PRODUCT_IMAGE = "productImage";

	private static Log logger = LogFactory.getLog(DefaultProductReportExtractor.class);
	
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
	
	protected ProductDictionaryService productDictionaryService;

	private NodeService mlNodeService;	
	
	protected AlfrescoRepository<ProductData> alfrescoRepository;
	
	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;

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
	
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setRepositoryEntityDefReader(RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader) {
		this.repositoryEntityDefReader = repositoryEntityDefReader;
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

		RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
		Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);

		// TODO make it more generic!!!!
		ProductData productData = (ProductData) alfrescoRepository.findOne(entityNodeRef);
		NodeRef defaultVariantNodeRef = loadVariants(productData, dataListsElt.getParent());
				
		if (datalists != null && !datalists.isEmpty()) {
			
			for (QName dataListQName : datalists.keySet()) {
				
				if(alfrescoRepository.hasDataList(entityNodeRef, dataListQName)){
					Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName()+"s");
					
					if(!dataListQName.isMatch(BeCPGModel.TYPE_COMPOLIST) &&
							!dataListQName.isMatch(BeCPGModel.TYPE_PACKAGINGLIST) &&
							!dataListQName.isMatch(MPMModel.TYPE_PROCESSLIST) &&
							!dataListQName.isMatch(BeCPGModel.TYPE_MICROBIOLIST) &&
							!dataListQName.isMatch(BeCPGModel.TYPE_INGLABELINGLIST)){
					
						List<BeCPGDataObject> dataListItems = (List)datalists.get(dataListQName);
						
						for(BeCPGDataObject dataListItem : dataListItems){
							
							Element nodeElt = dataListElt.addElement(dataListQName.getLocalName());
							loadDataListItemAttributes(dataListItem.getNodeRef(), nodeElt, false);
							
							if(dataListItem instanceof AbstractManualVariantListDataItem){
								extractVariants(((AbstractManualVariantListDataItem)dataListItem).getVariants(), nodeElt, defaultVariantNodeRef);
							}			
							
							if(dataListItem instanceof CompositionDataItem){
								CompositionDataItem compositionDataItem = (CompositionDataItem)dataListItem;
								nodeElt.addAttribute(ATTR_ITEM_TYPE, nodeService.getType(compositionDataItem.getProduct()).toPrefixString(namespaceService));
								nodeElt.addAttribute(ATTR_ASPECTS, extractAspects(compositionDataItem.getProduct()));
							}
						}
					}										
				}				
			}
		}
		
		// allergen
		Element allergenListElt = (Element)dataListsElt.selectSingleNode(BeCPGModel.TYPE_ALLERGENLIST.getLocalName()+"s");				
		if (allergenListElt != null) {
			
			List<AllergenListDataItem> allergenList = (List<AllergenListDataItem>)datalists.get(BeCPGModel.TYPE_ALLERGENLIST);
			String volAllergens = "";
			String inVolAllergens = "";

			for (AllergenListDataItem dataItem : allergenList) {
				
				String allergen = (String) nodeService.getProperty(dataItem.getAllergen(), ContentModel.PROP_NAME);
								
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
			Element compoListElt = dataListsElt.addElement(BeCPGModel.TYPE_COMPOLIST.getLocalName()+"s");

			for (CompoListDataItem dataItem : productData.getCompoList(EffectiveFilters.EFFECTIVE)) {

				if (dataItem.getProduct() !=null && nodeService.exists(dataItem.getProduct())) {
					
					Element partElt = compoListElt.addElement(BeCPGModel.TYPE_COMPOLIST.getLocalName());
					loadDataListItemAttributes(dataItem.getNodeRef(), partElt, false);
					loadProductData(dataItem.getProduct(), partElt);
																			
					partElt.addAttribute(ATTR_ITEM_TYPE, nodeService.getType(dataItem.getProduct()).toPrefixString(namespaceService));
					partElt.addAttribute(ATTR_ASPECTS, extractAspects(dataItem.getProduct()));
					extractVariants(dataItem.getVariants(), partElt, defaultVariantNodeRef);
				}
			}

			loadDynamicCharactList(productData.getCompoListView().getDynamicCharactList(), compoListElt);
		}

		// packList
		loadPackagingList(productData, dataListsElt, defaultVariantNodeRef, images);

		
		// processList
		if (productData.hasProcessListEl(EffectiveFilters.EFFECTIVE)) {
			Element processListElt = dataListsElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName()+"s");

			for (ProcessListDataItem dataItem : productData.getProcessList(EffectiveFilters.EFFECTIVE)) {

				Element partElt = processListElt.addElement(MPMModel.TYPE_PROCESSLIST.getLocalName());
				loadDataListItemAttributes(dataItem.getNodeRef(), partElt, false);
				loadProductData(dataItem.getProduct(), partElt);
				
				if (dataItem.getProduct() !=null && nodeService.exists(dataItem.getProduct())) {					
					partElt.addAttribute(ATTR_ITEM_TYPE, nodeService.getType(dataItem.getProduct()).toPrefixString(namespaceService));
					partElt.addAttribute(ATTR_ASPECTS, extractAspects(dataItem.getProduct()));														
				}
				
				extractVariants(dataItem.getVariants(), partElt, defaultVariantNodeRef);
			}

			loadDynamicCharactList(productData.getProcessListView().getDynamicCharactList(), processListElt);
		}
		

		// IngLabelingList
		if (productData.getLabelingListView().getIngLabelingList() != null) {
			Element ingListElt = dataListsElt.addElement(BeCPGModel.TYPE_INGLABELINGLIST.getLocalName()+"s");

			for (IngLabelingListDataItem dataItem : productData.getLabelingListView().getIngLabelingList()) {

				List<String> locales = new ArrayList<String>();
				for (Locale locale : dataItem.getValue().getLocales()) {

					logger.debug("ill, locale: " + locale);					

					if (!locales.contains(locale.getLanguage())) {

						locales.add(locale.getLanguage());
						
						String grpName = "";
						if(dataItem.getGrp() != null){
							MLText grpMLText = (MLText)mlNodeService.getProperty(dataItem.getGrp(), BeCPGModel.PROP_LABELING_RULE_LABEL);
							if(grpMLText != null && grpMLText.getValue(locale) != null && !grpMLText.getValue(locale).isEmpty()){
								grpName = grpMLText.getValue(locale);
							}
							else{
								grpName = (String)nodeService.getProperty(dataItem.getGrp(), ContentModel.PROP_NAME);
							}
						}
												
						
						Element ingLabelingElt = ingListElt.addElement(BeCPGModel.TYPE_INGLABELINGLIST.getLocalName());
						addCDATA(ingLabelingElt, ATTR_LANGUAGE, locale.getDisplayLanguage());
						addCDATA(ingLabelingElt, BeCPGModel.ASSOC_ILL_GRP.getLocalName(), grpName);
						addCDATA(ingLabelingElt, BeCPGModel.PROP_ILL_VALUE.getLocalName(), dataItem.getValue() != null ? dataItem.getValue().getValue(locale) : VALUE_NULL);
						addCDATA(ingLabelingElt, BeCPGModel.PROP_ILL_MANUAL_VALUE.getLocalName(), dataItem.getManualValue() != null  ? dataItem.getManualValue().getValue(locale) : VALUE_NULL);

						if (logger.isDebugEnabled()) {
							logger.debug("ingLabelingElt: " + ingLabelingElt.asXML());
						}
					}
				}
			}
		}

		// NutList
		Element nutListsElt = (Element)dataListsElt.selectSingleNode(BeCPGModel.TYPE_NUTLIST.getLocalName()+"s");
		if(nutListsElt != null){
			List<Element> nutListElts = (List<Element>)nutListsElt.selectNodes(BeCPGModel.TYPE_NUTLIST.getLocalName());
			for(Element nutListElt : nutListElts){
				String nut = nutListElt.valueOf("@"+BeCPGModel.ASSOC_NUTLIST_NUT.getLocalName());
				if(nut != null){
					String value = nutListElt.valueOf("@"+BeCPGModel.PROP_NUTLIST_VALUE.getLocalName());				
					nutListsElt.addAttribute(generateKeyAttribute(nut), value != null ? value : "");
				}			
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
			Element organoListElt = dataListsElt.addElement(BeCPGModel.TYPE_MICROBIOLIST.getLocalName() + "s");
			for (MicrobioListDataItem dataItem : microbioMap.values()) {

				Element nodeElt = organoListElt.addElement(BeCPGModel.TYPE_MICROBIOLIST.getLocalName());
				loadDataListItemAttributes(dataItem.getNodeRef(), nodeElt, false);				
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
	
	private void loadPackagingList(ProductData productData, Element dataListsElt, NodeRef defaultVariantNodeRef, Map<String, byte[]> images){			
		
		if (productData.hasPackagingListEl(EffectiveFilters.EFFECTIVE)) {
			
			PackagingData packagingData = new PackagingData();
			Element packagingListElt = dataListsElt.addElement(BeCPGModel.TYPE_PACKAGINGLIST.getLocalName()+"s");

			for (PackagingListDataItem dataItem : productData.getPackagingList(EffectiveFilters.EFFECTIVE)) {
				loadPackagingItem(dataItem, packagingListElt, packagingData, defaultVariantNodeRef, images);				
			}
			
			loadDynamicCharactList(productData.getPackagingListView().getDynamicCharactList(), packagingListElt);
			
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
				packagingListElt.addAttribute(ATTR_NB_PRODUCTS_LEVEL_2, toString(packagingData.getProductPerBoxes()));
				
				if(packagingData.getBoxesPerPallet() != null){
					
					Double tareTertiary = tareSecondary * packagingData.getBoxesPerPallet() + packagingData.getTareTertiary();
					Double netWeightTertiary = netWeightSecondary * packagingData.getBoxesPerPallet();
					packagingListElt.addAttribute(ATTR_PKG_TARE_LEVEL_3, toString(tareTertiary));
					packagingListElt.addAttribute(ATTR_PKG_NET_WEIGHT_LEVEL_3, toString(netWeightTertiary));
					packagingListElt.addAttribute(ATTR_PKG_GROSS_WEIGHT_LEVEL_3, toString(tareTertiary + netWeightTertiary));					
					packagingListElt.addAttribute(ATTR_NB_PRODUCTS_LEVEL_3, toString(packagingData.getProductPerBoxes() * packagingData.getBoxesPerPallet()));
				}
			}
					
		}		
	}

	private void loadPackagingItem(PackagingListDataItem dataItem, Element packagingListElt, 
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
		QName nodeType = nodeService.getType(dataItem.getProduct());

		Element partElt = packagingListElt.addElement(BeCPGModel.TYPE_PACKAGINGLIST.getLocalName());
		loadDataListItemAttributes(dataItem.getNodeRef(), partElt, false);
		loadProductData(dataItem.getProduct(), partElt);
		
		//we want to have true instead of Vrai
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
			logger.debug("load pallet aspect ");
			partElt.addAttribute(PackModel.PROP_PALLET_BOXES_PER_LAYER.getLocalName(), toString((Integer) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_BOXES_PER_LAYER)));
			partElt.addAttribute(PackModel.PROP_PALLET_LAYERS.getLocalName(), toString((Integer) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_LAYERS)));
			partElt.addAttribute(PackModel.PROP_PALLET_BOXES_PER_PALLET.getLocalName(), toString((Integer) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_BOXES_PER_PALLET)));			
			partElt.addAttribute(PackModel.PROP_PALLET_HEIGHT.getLocalName(), toString((Integer) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_HEIGHT)));
						
			// product per box and boxes per pallet
			if(defaultVariantNodeRef == null || dataItem.getVariants() == null || dataItem.getVariants().contains(defaultVariantNodeRef)){
				logger.debug("setProductPerBoxes " + dataItem.getQty().intValue());
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
	
	protected void loadDynamicCharactList(List<DynamicCharactListItem> dynamicCharactList, Element dataListElt) {

		Element dynCharactListElt = dataListElt.addElement(BeCPGModel.TYPE_DYNAMICCHARACTLIST.getLocalName()+"s");
		for (DynamicCharactListItem dc : dynamicCharactList) {
			Element dynamicCharact = dynCharactListElt.addElement(BeCPGModel.TYPE_DYNAMICCHARACTLIST.getLocalName());
			dynamicCharact.addAttribute(BeCPGModel.PROP_DYNAMICCHARACT_TITLE.getLocalName(), dc.getTitle());
			dynamicCharact.addAttribute(BeCPGModel.PROP_DYNAMICCHARACT_VALUE.getLocalName(),
					dc.getValue() == null ? VALUE_NULL : dc.getValue().toString());
		}
	}
	
	protected void loadProductData(NodeRef nodeRef, Element dataListItemElt){
		
		if(nodeRef != null){
			QName [] props = { BeCPGModel.PROP_CODE, BeCPGModel.PROP_ERP_CODE, BeCPGModel.PROP_LEGAL_NAME, BeCPGModel.PROP_LEGAL_NAME};
			QName [] assocs = { BeCPGModel.ASSOC_SUPPLIERS};
			
			for(QName prop : props){
				dataListItemElt.addAttribute(prop.getLocalName(), (String)nodeService.getProperty(nodeRef, prop));
			}
					
			for(QName assoc : assocs){
				dataListItemElt.addAttribute(assoc.getLocalName(), extractNames(associationService.getTargetAssocs(nodeRef, assoc)));
			}
		}			
	}
}
