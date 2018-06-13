/*
 * 
 */
package fr.becpg.repo.product.data;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamListItem;
import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.annotation.DataListView;
import fr.becpg.repo.repository.filters.DataListFilter;
import fr.becpg.repo.repository.model.AbstractEffectiveDataItem;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.StateableEntity;
import fr.becpg.repo.variant.model.VariantData;

public class ProductData extends AbstractEffectiveDataItem implements FormulatedEntity, HierarchicalEntity, StateableEntity, AspectAwareDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 764534088277737617L;
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private MLText legalName;
	private String title;
	private String erpCode;
	private SystemState state = SystemState.Simulation;
	private ProductUnit unit = ProductUnit.kg;
	private ProductData entityTpl;
	private List<NodeRef> plants = new ArrayList<>();
	

	/*
	 * Transformable properties
	 */
	private Double qty;
	private Double density;
	private Double yield;
	private Double yieldVolume;
	private Double netWeight;
	private Double weightPrimary;
	private Double netWeightSecondary;
	private Double weightSecondary;
	private Double netWeightTertiary;
	private Double weightTertiary;
	
	private Double netVolume;
	private Double servingSize;
	private Double recipeQtyUsed;
	private Double recipeVolumeUsed;
	private Double productLossPerc =  0d;
	private Double recipeQtyUsedWithLossPerc;

	private Double tare;
	private TareUnit tareUnit;

	/*
	 * Profitability properties
	 */
	private Double unitTotalCost;
	private Double unitPrice;
	private Double previousUnitTotalCost;
	private Double futureUnitTotalCost;
	private Double profitability;
	private Long breakEven;
	private Long projectedQty;

	/*
	 * Formulation
	 */
	private Date formulatedDate;
	private Date modifiedDate;
	private Integer reformulateCount;
	private Integer currentReformulateCount;
	private IngTypeItem ingType;
	private Boolean isUpToDate = false;
	
	
	/*
	 * Nutrient Score
	 */
	
	private Double nutrientScore;
	private String nutrientClass;
	private NodeRef nutrientProfile;

	/*
	 * DataList
	 */
	private List<AllergenListDataItem> allergenList;
	private List<CostListDataItem> costList;
	private List<PriceListDataItem> priceList;
	private List<IngListDataItem> ingList;
	private List<NutListDataItem> nutList;
	private List<OrganoListDataItem> organoList;
	private List<MicrobioListDataItem> microbioList;
	private List<PhysicoChemListDataItem> physicoChemList;
	private List<LabelClaimListDataItem> labelClaimList;
	private List<ControlDefListDataItem> controlDefList;
	private List<LabelingListDataItem> labelingList;
    private List<ResourceParamListItem> resourceParamList;
    private List<ReqCtrlListDataItem> reqCtrlList;

	/*
	 * View
	 */
	private CompoListView compoListView = new CompoListView();
	private ProcessListView processListView = new ProcessListView();
	private PackagingListView packagingListView = new PackagingListView();
	private LabelingListView labelingListView = new LabelingListView();

	/*
	 * Variants
	 */

	private List<VariantData> variants;
	private VariantPackagingData defaultVariantPackagingData;
	private VariantData defaultVariantData;

	/*
	 * Product specifications
	 */

	private List<ProductSpecificationData> productSpecifications;
	private List<ClientData> clients;
	
	/*
	 * Completion scores
	 */
	private String entityScore;
	private List<String> reportLocales;
	

	@AlfMultiAssoc(isEntity = true)
	@AlfQname(qname = "bcpg:productSpecifications")
	@AlfReadOnly
	public List<ProductSpecificationData> getProductSpecifications() {
		return productSpecifications;
	}

	public void setProductSpecifications(List<ProductSpecificationData> productSpecifications) {
		this.productSpecifications = productSpecifications;
	}

	@AlfMultiAssoc(isEntity = true)
	@AlfQname(qname = "bcpg:clients")
	@AlfReadOnly
	public List<ClientData> getClients() {
		return clients;
	}

	public void setClients(List<ClientData> clients) {
		this.clients = clients;
	}

	@AlfMultiAssoc(isChildAssoc = true, isEntity = true)
	@AlfQname(qname = "bcpg:variants")
	@AlfReadOnly
	public List<VariantData> getVariants() {
		return variants;
	}

	public void setVariants(List<VariantData> variants) {
		this.variants = variants;
	}
	
	public VariantData getDefaultVariantData() {
		return defaultVariantData;
	}

	public void setDefaultVariantData(VariantData defaultVariantData) {
		this.defaultVariantData = defaultVariantData;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:productHierarchy1")
	public NodeRef getHierarchy1() {
		return hierarchy1;
	}

	public void setHierarchy1(NodeRef hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:productHierarchy2")
	public NodeRef getHierarchy2() {
		return hierarchy2;
	}

	public void setHierarchy2(NodeRef hierarchy2) {
		this.hierarchy2 = hierarchy2;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:formulatedDate")
	public Date getFormulatedDate() {
		return formulatedDate;
	}

	public void setFormulatedDate(Date formulatedDate) {
		this.formulatedDate = formulatedDate;
	}
	
	public Boolean getIsUpToDate() {
		return isUpToDate;
	}

	public void setIsUpToDate(Boolean isUpToDate) {
		this.isUpToDate = isUpToDate;
	}

	@AlfProp
	@AlfReadOnly
	@AlfQname(qname = "cm:modified")
	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	@AlfMlText
	@AlfProp
	@AlfQname(qname = "bcpg:legalName")
	public MLText getLegalName() {
		return legalName;
	}

	public void setLegalName(MLText legalName) {
		this.legalName = legalName;
	}

	public void setLegalName(String legalName) {
		this.legalName = new MLText(legalName);
	}

	@AlfProp
	@AlfQname(qname = "cm:title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:erpCode")
	public String getErpCode() {
		return erpCode;
	}

	public void setErpCode(String erpCode) {
		this.erpCode = erpCode;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:productState")
	public SystemState getState() {
		return state;
	}

	public void setState(SystemState state) {
		this.state = state;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:productUnit")
	public ProductUnit getUnit() {
		return unit;
	}

	

	public void setUnit(ProductUnit unit) {
		this.unit = unit;
	}

	@AlfSingleAssoc(isEntity = true)
	@AlfQname(qname = "bcpg:entityTplRef")
	public ProductData getEntityTpl() {
		return entityTpl;
	}

	public void setEntityTpl(ProductData entityTpl) {
		this.entityTpl = entityTpl;
	}
	
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:plants")
	public List<NodeRef> getPlants() {
		return plants;
	}

	public void setPlants(List<NodeRef> plants) {
		this.plants = plants;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:ingTypeV2")
	public IngTypeItem getIngType() {
		return ingType;
	}

	public void setIngType(IngTypeItem ingType) {
		this.ingType = ingType;
	}

	public VariantPackagingData getDefaultVariantPackagingData() {
		return defaultVariantPackagingData;
	}

	public void setDefaultVariantPackagingData(VariantPackagingData defaultVariantPackagingData) {
		this.defaultVariantPackagingData = defaultVariantPackagingData;
	}
	
	@AlfProp
	@AlfQname(qname = "bcpg:productQty")
	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:productDensity")
	public Double getDensity() {
		return density;
	}
	

	public void setDensity(Double density) {
		this.density = density;
	}

	public Double getYield() {
		return yield;
	}

	public void setYield(Double yield) {
		this.yield = yield;
	}

	public Double getYieldVolume() {
		return yieldVolume;
	}

	public void setYieldVolume(Double yieldVolume) {
		this.yieldVolume = yieldVolume;
	}
	
	public Double getProductLossPerc() {
		return productLossPerc;
	}

	public void setProductLossPerc(Double lossPerc) {
		this.productLossPerc = lossPerc;
	}

	public Double getRecipeQtyUsedWithLossPerc() {
		return recipeQtyUsedWithLossPerc;
	}

	public void setRecipeQtyUsedWithLossPerc(Double recipeQtyUsedWithLossPerc) {
		this.recipeQtyUsedWithLossPerc = recipeQtyUsedWithLossPerc;
	}


	@AlfProp
	@AlfQname(qname = "bcpg:productCompoQtyUsed")
	public Double getRecipeQtyUsed() {
		return recipeQtyUsed;
	}

	public void setRecipeQtyUsed(Double recipeQtyUsed) {
		this.recipeQtyUsed = recipeQtyUsed;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:productCompoVolumeUsed")
	public Double getRecipeVolumeUsed() {
		return recipeVolumeUsed;
	}

	public void setRecipeVolumeUsed(Double recipeVolumeUsed) {
		this.recipeVolumeUsed = recipeVolumeUsed;
	}
	
	

	@AlfProp
	@AlfQname(qname = "bcpg:netWeight")
	public Double getNetWeight() {
		return netWeight;
	}

	public void setNetWeight(Double netWeight) {
		this.netWeight = netWeight;
	}
	
	
	public Double getWeightPrimary() {
		return weightPrimary;
	}

	public void setWeightPrimary(Double weightPrimary) {
		this.weightPrimary = weightPrimary;
	}

	public Double getNetWeightSecondary() {
		return netWeightSecondary;
	}

	public void setNetWeightSecondary(Double netWeightSecondary) {
		this.netWeightSecondary = netWeightSecondary;
	}

	public Double getWeightSecondary() {
		return weightSecondary;
	}

	public void setWeightSecondary(Double weightSecondary) {
		this.weightSecondary = weightSecondary;
	}

	public Double getNetWeightTertiary() {
		return netWeightTertiary;
	}

	public void setNetWeightTertiary(Double netWeightTertiary) {
		this.netWeightTertiary = netWeightTertiary;
	}

	public Double getWeightTertiary() {
		return weightTertiary;
	}

	public void setWeightTertiary(Double weightTertiary) {
		this.weightTertiary = weightTertiary;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:netVolume")
	public Double getNetVolume() {
		return netVolume;
	}

	public void setNetVolume(Double netVolume) {
		this.netVolume = netVolume;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:servingSize")
	public Double getServingSize() {
		return servingSize;
	}

	public void setServingSize(Double servingSize) {
		this.servingSize = servingSize;
	}

	@AlfProp
	@AlfQname(qname = "pack:tare")
	public Double getTare() {
		return tare;
	}

	public void setTare(Double tare) {
		this.tare = tare;
	}

	@AlfProp
	@AlfQname(qname = "pack:tareUnit")
	public TareUnit getTareUnit() {
		return tareUnit;
	}

	public void setTareUnit(TareUnit tareUnit) {
		this.tareUnit = tareUnit;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:unitTotalCost")
	public Double getUnitTotalCost() {
		return unitTotalCost;
	}

	public void setUnitTotalCost(Double unitTotalCost) {
		this.unitTotalCost = unitTotalCost;
	}

	
	
	@AlfProp
	@AlfQname(qname = "bcpg:previousUnitTotalCost")
	public Double getPreviousUnitTotalCost() {
		return previousUnitTotalCost;
	}

	public void setPreviousUnitTotalCost(Double previousUnitTotalCost) {
		this.previousUnitTotalCost = previousUnitTotalCost;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:futureUnitTotalCost")
	public Double getFutureUnitTotalCost() {
		return futureUnitTotalCost;
	}

	public void setFutureUnitTotalCost(Double futureUnitTotalCost) {
		this.futureUnitTotalCost = futureUnitTotalCost;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:unitPrice")
	public Double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}
	

	@AlfProp
	@AlfQname(qname = "bcpg:profitability")
	public Double getProfitability() {
		return profitability;
	}

	public void setProfitability(Double profitability) {
		this.profitability = profitability;
	}
	
	@AlfProp
	@AlfQname(qname = "bcpg:nutrientProfilingScore")
	public Double getNutrientScore() {
		return nutrientScore;
	}

	public void setNutrientScore(Double nutrientScore) {
		this.nutrientScore = nutrientScore;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:nutrientProfilingClass")
	public String getNutrientClass() {
		return nutrientClass;
	}

	public void setNutrientClass(String nutrientClass) {
		this.nutrientClass = nutrientClass;
	}
	
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:nutrientProfileRef")
	public NodeRef getNutrientProfile() {
		return nutrientProfile;
	}

	public void setNutrientProfile(NodeRef nutrientProfile) {
		this.nutrientProfile = nutrientProfile;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:breakEven")
	public Long getBreakEven() {
		return breakEven;
	}

	public void setBreakEven(Long breakEven) {
		this.breakEven = breakEven;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:projectedQty")
	public Long getProjectedQty() {
		return projectedQty;
	}

	public void setProjectedQty(Long projectedQty) {
		this.projectedQty = projectedQty;
	}

	@DataList
	@AlfQname(qname = "bcpg:allergenList")
	public List<AllergenListDataItem> getAllergenList() {
		return allergenList;
	}

	public void setAllergenList(List<AllergenListDataItem> allergenList) {
		this.allergenList = allergenList;
	}

	@DataList
	@AlfQname(qname = "bcpg:costList")
	public List<CostListDataItem> getCostList() {
		return costList;
	}

	public void setCostList(List<CostListDataItem> costList) {
		this.costList = costList;
	}

	@DataList
	@AlfQname(qname = "bcpg:priceList")
	public List<PriceListDataItem> getPriceList() {
		return priceList;
	}

	public void setPriceList(List<PriceListDataItem> priceList) {
		this.priceList = priceList;
	}

	@DataList
	@AlfQname(qname = "bcpg:ingList")
	public List<IngListDataItem> getIngList() {
		return ingList;
	}

	public void setIngList(List<IngListDataItem> ingList) {
		this.ingList = ingList;
	}

	@DataList
	@AlfQname(qname = "bcpg:nutList")
	public List<NutListDataItem> getNutList() {
		return nutList;
	}

	public void setNutList(List<NutListDataItem> nutList) {
		this.nutList = nutList;
	}

	@DataList
	@AlfQname(qname = "bcpg:organoList")
	public List<OrganoListDataItem> getOrganoList() {
		return organoList;
	}

	public void setOrganoList(List<OrganoListDataItem> organoList) {
		this.organoList = organoList;
	}

	@DataList
	@AlfQname(qname = "bcpg:microbioList")
	public List<MicrobioListDataItem> getMicrobioList() {
		return microbioList;
	}

	public void setMicrobioList(List<MicrobioListDataItem> microbioList) {
		this.microbioList = microbioList;
	}

	@DataList
	@AlfQname(qname = "bcpg:physicoChemList")
	public List<PhysicoChemListDataItem> getPhysicoChemList() {
		return physicoChemList;
	}

	public void setPhysicoChemList(List<PhysicoChemListDataItem> physicoChemList) {
		this.physicoChemList = physicoChemList;
	}

	@DataList
	@AlfQname(qname = "bcpg:labelClaimList")
	public List<LabelClaimListDataItem> getLabelClaimList() {
		return labelClaimList;
	}

	public void setLabelClaimList(List<LabelClaimListDataItem> labelClaimList) {
		this.labelClaimList = labelClaimList;
	}

	@DataList
	@AlfQname(qname = "qa:controlDefList")
	public List<ControlDefListDataItem> getControlDefList() {
		return controlDefList;
	}

	public void setControlDefList(List<ControlDefListDataItem> controlDefList) {
		this.controlDefList = controlDefList;
	}
	
    @DataList
	@AlfQname(qname="mpm:resourceParamList")
	public List<ResourceParamListItem> getResourceParamList() {
		return resourceParamList;
	}

	public void setResourceParamList(List<ResourceParamListItem> resourceParamList) {
		this.resourceParamList = resourceParamList;
	}

	@DataList
	@AlfQname(qname = "pack:labelingList")
	public List<LabelingListDataItem> getLabelingList() {
		return labelingList;
	}

	public void setLabelingList(List<LabelingListDataItem> labelingList) {
		this.labelingList = labelingList;
	}

	@DataListView
	@AlfQname(qname = "bcpg:ingLabelingList")
	public LabelingListView getLabelingListView() {
		return labelingListView;
	}

	public void setLabelingListView(LabelingListView labelingListView) {
		this.labelingListView = labelingListView;
	}
	
	
	@DataList
	@AlfQname(qname="bcpg:reqCtrlList")
	public List<ReqCtrlListDataItem> getReqCtrlList() {
		return reqCtrlList;
	}

	public void setReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList) {
		this.reqCtrlList = reqCtrlList;
	}


	@DataListView
	@AlfQname(qname = "bcpg:compoList")
	public CompoListView getCompoListView() {
		return compoListView;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:entityScore")
	public String getEntityScore(){
		return entityScore;
	}
	
	public void setEntityScore(String string){
		this.entityScore = string;
	}
	
	
	@AlfProp
	@AlfQname(qname = "rep:reportLocales")
	public List<String> getReportLocales() {
		return reportLocales;
	}

	public void setReportLocales(List<String> reportLocales) {
		this.reportLocales = reportLocales;
	}

	private <T> List<T>  filterList(List<T> list,List<DataListFilter<ProductData, T>> filters ) {
		if (filters != null && !filters.isEmpty()) {
			Stream<T> stream = list.stream();
			for (DataListFilter<ProductData, T> filter : filters) {
				stream = stream.filter(filter.createPredicate(this));
			}
			return stream.collect(Collectors.toList());
		} 
		return list;
	}
	
	public List<CompoListDataItem> getCompoList(){
		return getCompoList(Collections.emptyList());
	}
	
	public List<CompoListDataItem> getCompoList(DataListFilter<ProductData, CompoListDataItem> filter) {
		return getCompoList(Collections.singletonList(filter));
	}
	
	public List<CompoListDataItem> getCompoList(List<DataListFilter<ProductData, CompoListDataItem>> filters) {
		if (compoListView != null && compoListView.getCompoList() != null) {
			return filterList(compoListView.getCompoList(),filters);
		}
		return null;
	}

	public boolean hasCompoListEl(){
		return hasCompoListEl(Collections.emptyList());
	}
	
	public boolean hasCompoListEl( DataListFilter<ProductData, CompoListDataItem> filter) {
		return hasCompoListEl(Collections.singletonList(filter));
	}
	
	public boolean hasCompoListEl( List<DataListFilter<ProductData, CompoListDataItem>> filters) {
		return compoListView != null && compoListView.getCompoList() != null && !getCompoList(filters).isEmpty();
	}

	public void setCompoListView(CompoListView compoListView) {
		this.compoListView = compoListView;
	}

	@DataListView
	@AlfQname(qname = "mpm:processList")
	public ProcessListView getProcessListView() {
		return processListView;
	}

	
	public  List<ProcessListDataItem> getProcessList() {
		return getProcessList(Collections.emptyList());
	}
	
	public  List<ProcessListDataItem> getProcessList(DataListFilter<ProductData,ProcessListDataItem> filter) {
		return getProcessList(Collections.singletonList(filter));
	}
	
	
	public  List<ProcessListDataItem> getProcessList(List<DataListFilter<ProductData,ProcessListDataItem>> filters) {
		if (processListView != null && processListView.getProcessList() != null) {
			return filterList(processListView.getProcessList(),filters);
		}
		return null;
	}
	
	
	public boolean hasProcessListEl(){
		return hasCompoListEl(Collections.emptyList());
	}
	
	public boolean hasProcessListEl( DataListFilter<ProductData, ProcessListDataItem> filter) {
		return hasProcessListEl(Collections.singletonList(filter));
	}
	
	public  boolean hasProcessListEl(List<DataListFilter<ProductData,ProcessListDataItem>> filters) {
		return processListView != null && processListView.getProcessList() != null && !getProcessList(filters).isEmpty();
	}

	public void setProcessListView(ProcessListView processListView) {
		this.processListView = processListView;
	}

	@DataListView
	@AlfQname(qname = "bcpg:packagingList")
	public PackagingListView getPackagingListView() {
		return packagingListView;
	}

	public  List<PackagingListDataItem> getPackagingList() {
		return getPackagingList(Collections.emptyList());
	}
	
	public  List<PackagingListDataItem> getPackagingList(DataListFilter<ProductData,PackagingListDataItem> filter) {
		return getPackagingList(Collections.singletonList(filter));
	}
	
	
	public List<PackagingListDataItem> getPackagingList(List<DataListFilter<ProductData,PackagingListDataItem>> filters) {
		if (packagingListView != null && packagingListView.getPackagingList() != null) {
			return filterList(packagingListView.getPackagingList(),filters);
		}
		return null;
	}

	public boolean hasPackagingListEl(){
		return hasPackagingListEl(Collections.emptyList());
	}
	
	public boolean hasPackagingListEl( DataListFilter<ProductData, PackagingListDataItem> filter) {
		return hasPackagingListEl(Collections.singletonList(filter));
	}
	
	public boolean hasPackagingListEl(List<DataListFilter<ProductData,PackagingListDataItem>> filters) {
		return packagingListView != null && packagingListView.getPackagingList() != null && !getPackagingList(filters).isEmpty();
	}

	public void setPackagingListView(PackagingListView packagingListView) {
		this.packagingListView = packagingListView;
	}

	public List<AbstractProductDataView> getViews() {
		return Arrays.asList(compoListView, packagingListView, processListView);
	}

	@Override
	public String getEntityState() {
		return state != null ? state.toString() : null;
	}
	
	// Formula helpers

	public boolean isLiquid() {
		return unit != null && (unit == ProductUnit.L || unit == ProductUnit.mL || unit == ProductUnit.cL);
	}
	
	public boolean isRawMaterial() {
		return this instanceof RawMaterialData;
	}
	
	public boolean isPackaging() {
		return this instanceof PackagingMaterialData;
	}
	
	public boolean isPackagingKit() {
		return this instanceof PackagingKitData;
	}
	
	public boolean isSemiFinished() {
		return this instanceof SemiFinishedProductData;
	}
	
	public boolean isLocalSemiFinished() {
		return this instanceof LocalSemiFinishedProductData;
	}
	

	public Integer getReformulateCount() {
		return reformulateCount;
	}

	public void setReformulateCount(Integer reformulateCount) {
		this.reformulateCount = reformulateCount;
	}

	public Integer getCurrentReformulateCount() {
		return currentReformulateCount;
	}

	public void setCurrentReformulateCount(Integer currentReformulateCount) {
		this.currentReformulateCount = currentReformulateCount;
	}

	/**
	 * Instantiates a new product data.
	 */
	public ProductData() {
		super();
	}

	@Override
	public String toString() {
				return "ProductData [hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", legalName=" + legalName + ", title=" + title
								+ ", erpCode=" + erpCode + ", state=" + state +  ", unit=" + unit + ", qty=" + qty + ", density=" + density + ", yield=" + yield
								+ ", yieldVolume=" + yieldVolume + ", netWeight=" + netWeight + ", netVolume=" + netVolume + ", servingSize=" + servingSize + ", recipeQtyUsed=" + recipeQtyUsed
								+ ", tare=" + tare + ", tareUnit=" + tareUnit + ", unitTotalCost=" + unitTotalCost + ", unitPrice=" + unitPrice + ", profitability="
								+ profitability + ", breakEven=" + breakEven + ", allergenList=" + allergenList + ", productScores="+ entityScore +"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((breakEven == null) ? 0 : breakEven.hashCode());
		result = prime * result + ((currentReformulateCount == null) ? 0 : currentReformulateCount.hashCode());
		result = prime * result + ((density == null) ? 0 : density.hashCode());
		result = prime * result + ((erpCode == null) ? 0 : erpCode.hashCode());
		result = prime * result + ((hierarchy1 == null) ? 0 : hierarchy1.hashCode());
		result = prime * result + ((hierarchy2 == null) ? 0 : hierarchy2.hashCode());
		result = prime * result + ((legalName == null) ? 0 : legalName.hashCode());
		result = prime * result + ((netWeight == null) ? 0 : netWeight.hashCode());
		result = prime * result + ((profitability == null) ? 0 : profitability.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
		result = prime * result + ((recipeQtyUsed == null) ? 0 : recipeQtyUsed.hashCode());
		result = prime * result + ((servingSize == null) ? 0 : servingSize.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((tare == null) ? 0 : tare.hashCode());
		result = prime * result + ((tareUnit == null) ? 0 : tareUnit.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((unitPrice == null) ? 0 : unitPrice.hashCode());
		result = prime * result + ((unitTotalCost == null) ? 0 : unitTotalCost.hashCode());
		result = prime * result + ((variants == null) ? 0 : variants.hashCode());
		result = prime * result + ((yield == null) ? 0 : yield.hashCode());
		result = prime * result + ((yieldVolume == null) ? 0 : yieldVolume.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductData other = (ProductData) obj;
		if (breakEven == null) {
			if (other.breakEven != null)
				return false;
		} else if (!breakEven.equals(other.breakEven))
			return false;
		if (currentReformulateCount == null) {
			if (other.currentReformulateCount != null)
				return false;
		} else if (!currentReformulateCount.equals(other.currentReformulateCount))
			return false;
		if (density == null) {
			if (other.density != null)
				return false;
		} else if (!density.equals(other.density))
			return false;
		if (erpCode == null) {
			if (other.erpCode != null)
				return false;
		} else if (!erpCode.equals(other.erpCode))
			return false;
		if (hierarchy1 == null) {
			if (other.hierarchy1 != null)
				return false;
		} else if (!hierarchy1.equals(other.hierarchy1))
			return false;
		if (hierarchy2 == null) {
			if (other.hierarchy2 != null)
				return false;
		} else if (!hierarchy2.equals(other.hierarchy2))
			return false;
		if (legalName == null) {
			if (other.legalName != null)
				return false;
		} else if (!legalName.equals(other.legalName))
			return false;
		if (netWeight == null) {
			if (other.netWeight != null)
				return false;
		} else if (!netWeight.equals(other.netWeight))
			return false;
		if (profitability == null) {
			if (other.profitability != null)
				return false;
		} else if (!profitability.equals(other.profitability))
			return false;
		if (qty == null) {
			if (other.qty != null)
				return false;
		} else if (!qty.equals(other.qty))
			return false;
		if (recipeQtyUsed == null) {
			if (other.recipeQtyUsed != null)
				return false;
		} else if (!recipeQtyUsed.equals(other.recipeQtyUsed))
			return false;
		if (servingSize == null) {
			if (other.servingSize != null)
				return false;
		} else if (!servingSize.equals(other.servingSize))
			return false;
		if (state != other.state)
			return false;
		if (tare == null) {
			if (other.tare != null)
				return false;
		} else if (!tare.equals(other.tare))
			return false;
		if (tareUnit != other.tareUnit)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (unit != other.unit)
			return false;
		if (unitPrice == null) {
			if (other.unitPrice != null)
				return false;
		} else if (!unitPrice.equals(other.unitPrice))
			return false;
		if (unitTotalCost == null) {
			if (other.unitTotalCost != null)
				return false;
		} else if (!unitTotalCost.equals(other.unitTotalCost))
			return false;
		if (variants == null) {
			if (other.variants != null)
				return false;
		} else if (!variants.equals(other.variants))
			return false;
		if (yield == null) {
			if (other.yield != null)
				return false;
		} else if (!yield.equals(other.yield))
			return false;
		if (yieldVolume == null) {
			if (other.yieldVolume != null)
				return false;
		} else if (!yieldVolume.equals(other.yieldVolume))
			return false;
		return true;
	}

}
