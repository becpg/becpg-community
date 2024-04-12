/*
 *
 */
package fr.becpg.repo.product.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import fr.becpg.api.BeCPGPublicApi;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.decernis.DecernisMode;
import fr.becpg.repo.glop.model.GlopData;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RegulatoryResult;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.meat.MeatContentData;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamListItem;
import fr.becpg.repo.product.data.productList.SvhcListDataItem;
import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.annotation.DataListView;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.filters.DataListFilter;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.EffectiveDataItem;
import fr.becpg.repo.repository.model.StateableEntity;
import fr.becpg.repo.variant.model.VariantData;
import fr.becpg.repo.variant.model.VariantEntity;

/**
 * <p>ProductData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@BeCPGPublicApi
public class ProductData extends AbstractScorableEntity
		implements EffectiveDataItem, HierarchicalEntity, StateableEntity, AspectAwareDataItem, VariantEntity, RegulatoryEntity {

	private static final long serialVersionUID = 764534088277737617L;
	private static final Log logger = LogFactory.getLog(ProductData.class);

	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private MLText legalName;
	private MLText pluralLegalName;
	private MLText title;
	private String erpCode;
	private SystemState state = SystemState.Simulation;
	private ProductUnit unit = ProductUnit.kg;
	private ProductData entityTpl;
	private List<NodeRef> plants = new ArrayList<>();

	protected Date startEffectivity;
	protected Date endEffectivity;

	/*
	 * Glop
	 * 
	 */
	private GlopData glopData;

	/*
	 * Transformable properties
	 */
	private Double qty;
	private Double density;
	private Double yield;
	private Double manualYield;
	private Double secondaryYield;
	private Double yieldVolume;
	private Double netWeight;
	private Double weightPrimary;
	private Double netWeightSecondary;
	private Double weightSecondary;
	private Double netWeightTertiary;
	private Double weightTertiary;
	private Boolean dropPackagingOfComponents;

	private Double netVolume;
	private Double servingSize;
	private MLText servingSizeByCountry;
	private ProductUnit servingSizeUnit;
	private Double recipeQtyUsed;
	private Double recipeVolumeUsed;
	private Double productLossPerc;
	private Double componentLossPerc;
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
	 * Parent entity
	 */
	private ProductData parentEntity;

	/*
	 * Formulation
	 */
	private Date formulatedDate;
	private Date modifiedDate;
	private Integer reformulateCount;
	private Integer currentReformulateCount;
	private String formulationChainId;
	private Boolean isUpToDate = false;
	private Boolean updateFormulatedDate = true;

	/*
	 * Nutlist formulation
	 */
	private List<String> preparationStates;

	/*
	 * Labeling formulation
	 */

	private IngTypeItem ingType;
	private Boolean isIngListManual;

	/*
	 * Nutrient Score
	 */

	private Double nutrientScore;
	private String nutrientClass;
	private String nutrientDetails;
	private NodeRef nutrientProfile;
	private String nutrientProfileCategory;
	private String nutrientProfileVersion;

	/*
	 * Eco Score
	 */

	private Double ecoScore;
	private String ecoScoreClass;
	private String ecoScoreCategory;
	private String ecoScoreDetails;

	private String lcaScoreMethod;
	private Double lcaScore;

	/*
	 * Meat aspect
	 */

	private Map<String, MeatContentData> meatContentData = new HashMap<>();
	private String meatType;

	/*
	 * Compliance
	 */
	private List<NodeRef> regulatoryCountriesRef = new ArrayList<>();
	private List<NodeRef> regulatoryUsagesRef = new ArrayList<>();
	private List<String> regulatoryCountries = new ArrayList<>();
	private List<String> regulatoryUsages = new ArrayList<>();
	private Date regulatoryFormulatedDate;
	private DecernisMode regulatoryMode = DecernisMode.BECPG_ONLY;
	private String regulatoryRecipeId;
	private RegulatoryResult regulatoryResult;
	private String regulatoryUrl;

	/** 
	 * JSON Data { 
	 *   decernis: "checksum" 
	 * }
	 * 
	 */
	private String requirementChecksum;

	/**
	 * Specify that a product isGeneric
	 */
	private Boolean isGeneric;

	/*
	 * DataList
	 */
	private List<AllergenListDataItem> allergenList;
	private List<CostListDataItem> costList;
	private List<LCAListDataItem> lcaList;
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
	private List<PackMaterialListDataItem> packMaterialList;
	private List<StockListDataItem> stockList;
	private List<RegulatoryListDataItem> regulatoryList;
	private List<IngRegulatoryListDataItem> ingRegulatoryList;
	private List<SvhcListDataItem> svhcList;

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

	private List<VariantData> localVariants;
	private VariantPackagingData defaultVariantPackagingData;
	private VariantData defaultVariantData;

	/*
	 * Product specifications
	 */

	private List<ProductSpecificationData> productSpecifications;
	private List<ClientData> clients;

	private List<NodeRef> suppliers = new ArrayList<>();
	private List<NodeRef> supplierPlants = new ArrayList<>();

	/*
	 * Origin geo
	 */

	private List<NodeRef> geoOrigins = new ArrayList<>();

	/*
	 * Completion scores
	 */
	private String entityScore;
	private List<String> reportLocales;
	private List<ProductData> compareWithEntities;

	/** {@inheritDoc} */
	public GlopData getGlopData() {
		return glopData;
	}

	/** {@inheritDoc} */
	public void setGlopData(GlopData glopData) {
		this.glopData = glopData;
	}

	public void setLcaScore(Double lcaScore) {
		this.lcaScore = lcaScore;
	}

	public void setLcaScoreMethod(String lcaScoreMethod) {
		this.lcaScoreMethod = lcaScoreMethod;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:lcaScore")
	public Double getLcaScore() {
		return lcaScore;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:lcaScoreMethod")
	public String getLcaScoreMethod() {
		return lcaScoreMethod;
	}

	/** {@inheritDoc} */
	@AlfProp
	@AlfQname(qname = "bcpg:startEffectivity")
	@InternalField
	@Override
	public Date getStartEffectivity() {
		return startEffectivity;
	}

	/** {@inheritDoc} */
	@Override
	public void setStartEffectivity(Date startEffectivity) {
		this.startEffectivity = startEffectivity;
	}

	/** {@inheritDoc} */
	@AlfProp
	@AlfQname(qname = "bcpg:endEffectivity")
	@InternalField
	@Override
	public Date getEndEffectivity() {
		return endEffectivity;
	}

	/** {@inheritDoc} */
	@Override
	public void setEndEffectivity(Date endEffectivity) {
		this.endEffectivity = endEffectivity;
	}

	/**
	 * <p>Getter for the field <code>productSpecifications</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc(isEntity = true)
	@AlfQname(qname = "bcpg:productSpecifications")
	@AlfReadOnly
	public List<ProductSpecificationData> getProductSpecifications() {
		return productSpecifications;
	}

	/**
	 * <p>Setter for the field <code>productSpecifications</code>.</p>
	 *
	 * @param productSpecifications a {@link java.util.List} object.
	 */
	public void setProductSpecifications(List<ProductSpecificationData> productSpecifications) {
		this.productSpecifications = productSpecifications;
	}

	/**
	 * <p>Getter for the field <code>clients</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc(isEntity = true)
	@AlfQname(qname = "bcpg:clients")
	@AlfReadOnly
	public List<ClientData> getClients() {
		return clients;
	}

	/**
	 * <p>Getter for the field <code>suppliers</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:suppliers")
	public List<NodeRef> getSuppliers() {
		return suppliers;
	}

	/**
	 * <p>Setter for the field <code>suppliers</code>.</p>
	 *
	 * @param suppliers a {@link java.util.List} object.
	 */
	public void setSuppliers(List<NodeRef> suppliers) {
		this.suppliers = suppliers;
	}

	/**
	 * <p>Getter for the field <code>supplierPlants</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:supplierPlants")
	public List<NodeRef> getSupplierPlants() {
		return supplierPlants;
	}

	/**
	 * <p>Setter for the field <code>supplierPlants</code>.</p>
	 *
	 * @param supplierPlants a {@link java.util.List} object.
	 */
	public void setSupplierPlants(List<NodeRef> supplierPlants) {
		this.supplierPlants = supplierPlants;
	}

	/**
	 * <p>Setter for the field <code>clients</code>.</p>
	 *
	 * @param clients a {@link java.util.List} object.
	 */
	public void setClients(List<ClientData> clients) {
		this.clients = clients;
	}

	/**
	 * <p>Getter for the field <code>compareWithEntities</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc(isEntity = true)
	@AlfQname(qname = "bcpg:compareWithEntities")
	@AlfReadOnly
	public List<ProductData> getCompareWithEntities() {
		return compareWithEntities;
	}

	/**
	 * <p>Setter for the field <code>compareWithEntities</code>.</p>
	 *
	 * @param compareWithEntities a {@link java.util.List} object.
	 */
	public void setCompareWithEntities(List<ProductData> compareWithEntities) {
		this.compareWithEntities = compareWithEntities;
	}

	/**
	 * <p>Getter for the field <code>variants</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc(isChildAssoc = true, isEntity = true)
	@AlfQname(qname = "bcpg:variants")
	@AlfReadOnly
	public List<VariantData> getLocalVariants() {
		return localVariants;
	}

	/**
	 * <p>Setter for the field <code>variants</code>.</p>
	 *
	 * @param variants a {@link java.util.List} object.
	 */
	public void setLocalVariants(List<VariantData> localVariants) {
		this.localVariants = localVariants;
	}

	/**
	 * <p>Getter for the all variants.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<VariantData> getVariants() {
		List<VariantData> variants = localVariants != null ? new ArrayList<>(localVariants) : new ArrayList<>();
		if (entityTpl != null) {
			List<VariantData> entityTplVariants = entityTpl.getLocalVariants();
			if (entityTplVariants != null && !entityTplVariants.isEmpty()) {
				variants.addAll(entityTplVariants);
			}
		}
		return variants;
	}

	/**
	 * <p>Getter for the field <code>defaultVariantData</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.variant.model.VariantData} object.
	 */
	public VariantData getDefaultVariantData() {
		return defaultVariantData;
	}

	/**
	 * <p>Setter for the field <code>defaultVariantData</code>.</p>
	 *
	 * @param defaultVariantData a {@link fr.becpg.repo.variant.model.VariantData} object.
	 */
	public void setDefaultVariantData(VariantData defaultVariantData) {
		this.defaultVariantData = defaultVariantData;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:productHierarchy1")
	public NodeRef getHierarchy1() {
		return hierarchy1;
	}

	/**
	 * <p>Setter for the field <code>hierarchy1</code>.</p>
	 *
	 * @param hierarchy1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setHierarchy1(NodeRef hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:productHierarchy2")
	public NodeRef getHierarchy2() {
		return hierarchy2;
	}

	/**
	 * <p>Setter for the field <code>hierarchy2</code>.</p>
	 *
	 * @param hierarchy2 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setHierarchy2(NodeRef hierarchy2) {
		this.hierarchy2 = hierarchy2;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:formulatedDate")
	public Date getFormulatedDate() {
		return formulatedDate;
	}

	/** {@inheritDoc} */
	@Override
	public void setFormulatedDate(Date formulatedDate) {
		this.formulatedDate = formulatedDate;
	}

	/** {@inheritDoc} */
	@Override
	public boolean shouldUpdateFormulatedDate() {
		return updateFormulatedDate;
	}

	/** {@inheritDoc} */
	@Override
	public void setUpdateFormulatedDate(boolean updateFormulatedDate) {
		this.updateFormulatedDate = updateFormulatedDate;
	}

	/**
	 * <p>Getter for the field <code>isUpToDate</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getIsUpToDate() {
		return isUpToDate;
	}

	/**
	 * <p>Setter for the field <code>isUpToDate</code>.</p>
	 *
	 * @param isUpToDate a {@link java.lang.Boolean} object.
	 */
	public void setIsUpToDate(Boolean isUpToDate) {
		this.isUpToDate = isUpToDate;
	}

	/**
	 * <p>Getter for the field <code>modifiedDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfReadOnly
	@AlfQname(qname = "cm:modified")
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * <p>Setter for the field <code>modifiedDate</code>.</p>
	 *
	 * @param modifiedDate a {@link java.util.Date} object.
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	/**
	 * <p>Getter for the field <code>legalName</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfMlText
	@AlfProp
	@AlfQname(qname = "bcpg:legalName")
	public MLText getLegalName() {
		return legalName;
	}

	/**
	 * <p>Setter for the field <code>legalName</code>.</p>
	 *
	 * @param legalName a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setLegalName(MLText legalName) {
		this.legalName = legalName;
	}

	/**
	 * <p>Setter for the field <code>legalName</code>.</p>
	 *
	 * @param legalName a {@link java.lang.String} object.
	 */
	public void setLegalName(String legalName) {
		this.legalName = new MLText(legalName);
	}

	/**
	 * <p>Getter for the field <code>pluralLegalName</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfMlText
	@AlfProp
	@AlfQname(qname = "bcpg:pluralLegalName")
	public MLText getPluralLegalName() {
		return pluralLegalName;
	}

	/**
	 * <p>Setter for the field <code>pluralLegalName</code>.</p>
	 *
	 * @param pluralLegalName a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setPluralLegalName(MLText pluralLegalName) {
		this.pluralLegalName = pluralLegalName;
	}

	/**
	 * <p>Getter for the field <code>title</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "cm:title")
	public MLText getTitle() {
		return title;
	}

	/**
	 * <p>Setter for the field <code>title</code>.</p>
	 *
	 * @param title a {@link java.lang.String} object.
	 */
	public void setTitle(MLText title) {
		this.title = title;
	}

	/**
	 * <p>Getter for the field <code>erpCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:erpCode")
	public String getErpCode() {
		return erpCode;
	}

	/**
	 * <p>Setter for the field <code>erpCode</code>.</p>
	 *
	 * @param erpCode a {@link java.lang.String} object.
	 */
	public void setErpCode(String erpCode) {
		this.erpCode = erpCode;
	}

	/**
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link fr.becpg.model.SystemState} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:productState")
	public SystemState getState() {
		return state;
	}

	/**
	 * <p>Setter for the field <code>state</code>.</p>
	 *
	 * @param state a {@link fr.becpg.model.SystemState} object.
	 */
	public void setState(SystemState state) {
		this.state = state;
	}

	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:productUnit")
	public ProductUnit getUnit() {
		return unit;
	}

	/**
	 * <p>Setter for the field <code>unit</code>.</p>
	 *
	 * @param unit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 */
	public void setUnit(ProductUnit unit) {
		this.unit = unit;
	}

	/**
	 * <p>Getter for the field <code>entityTpl</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ProductData} object.
	 */
	@AlfSingleAssoc(isEntity = true, isCacheable = true)
	@AlfQname(qname = "bcpg:entityTplRef")
	public ProductData getEntityTpl() {
		return entityTpl;
	}

	/**
	 * <p>getFormulatedEntityTpl.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getFormulatedEntityTpl() {
		return entityTpl != null ? entityTpl.getNodeRef() : null;
	}

	/**
	 * <p>Setter for the field <code>entityTpl</code>.</p>
	 *
	 * @param entityTpl a {@link fr.becpg.repo.product.data.ProductData} object.
	 */
	public void setEntityTpl(ProductData entityTpl) {
		this.entityTpl = entityTpl;
	}

	@AlfSingleAssoc(isEntity = true)
	@AlfQname(qname = "bcpg:parentEntityRef")
	public ProductData getParentEntity() {
		return parentEntity;
	}

	public void setParentEntity(ProductData parentEntity) {
		this.parentEntity = parentEntity;
	}

	/**
	 * <p>Getter for the field <code>plants</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:plants")
	public List<NodeRef> getPlants() {
		return plants;
	}

	/**
	 * <p>Setter for the field <code>plants</code>.</p>
	 *
	 * @param plants a {@link java.util.List} object.
	 */
	public void setPlants(List<NodeRef> plants) {
		this.plants = plants;
	}

	public List<NodeRef> getAllPlants() {
		List<NodeRef> ret = new ArrayList<>();
		if (plants != null) {
			ret.addAll(plants);
		}
		if (supplierPlants != null) {
			ret.addAll(supplierPlants);
		}
		return ret;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:isIngListManual")
	public Boolean getIsIngListManual() {
		return isIngListManual;
	}

	public void setIsIngListManual(Boolean isIngListManual) {
		this.isIngListManual = isIngListManual;
	}

	/**
	 * <p>Getter for the field <code>ingType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ing.IngTypeItem} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingTypeV2")
	public IngTypeItem getIngType() {
		return ingType;
	}

	/**
	 * <p>Setter for the field <code>ingType</code>.</p>
	 *
	 * @param ingType a {@link fr.becpg.repo.product.data.ing.IngTypeItem} object.
	 */
	public void setIngType(IngTypeItem ingType) {
		this.ingType = ingType;
	}

	/**
	 * <p>Getter for the field <code>defaultVariantPackagingData</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.packaging.VariantPackagingData} object.
	 */
	public VariantPackagingData getDefaultVariantPackagingData() {
		return defaultVariantPackagingData;
	}

	/**
	 * <p>Setter for the field <code>defaultVariantPackagingData</code>.</p>
	 *
	 * @param defaultVariantPackagingData a {@link fr.becpg.repo.product.data.packaging.VariantPackagingData} object.
	 */
	public void setDefaultVariantPackagingData(VariantPackagingData defaultVariantPackagingData) {
		this.defaultVariantPackagingData = defaultVariantPackagingData;
	}

	/**
	 * <p>Getter for the field <code>qty</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:productQty")
	public Double getQty() {
		return qty;
	}

	/**
	 * <p>Setter for the field <code>qty</code>.</p>
	 *
	 * @param qty a {@link java.lang.Double} object.
	 */
	public void setQty(Double qty) {
		this.qty = qty;
	}

	/**
	 * <p>Getter for the field <code>density</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:productDensity")
	public Double getDensity() {
		return density;
	}

	/**
	 * <p>Setter for the field <code>density</code>.</p>
	 *
	 * @param density a {@link java.lang.Double} object.
	 */
	public void setDensity(Double density) {
		this.density = density;
	}

	/**
	 * <p>Getter for the field <code>yield</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:productYield")
	public Double getYield() {
		return yield;
	}

	/**
	 * <p>Setter for the field <code>yield</code>.</p>
	 *
	 * @param yield a {@link java.lang.Double} object.
	 */
	public void setYield(Double yield) {
		this.yield = yield;
	}

	/**
	 * <p>Getter for the field <code>manualYield</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getManualYield() {
		return manualYield;
	}

	/**
	 * <p>Setter for the field <code>manualYield</code>.</p>
	 *
	 * @param manualYield a {@link java.lang.Double} object.
	 */
	public void setManualYield(Double manualYield) {
		this.manualYield = manualYield;
	}

	/**
	 * <p>Getter for the field <code>secondaryYield</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getSecondaryYield() {
		return secondaryYield;
	}

	/**
	 * <p>Setter for the field <code>secondaryYield</code>.</p>
	 *
	 * @param secondaryYield a {@link java.lang.Double} object.
	 */
	public void setSecondaryYield(Double secondaryYield) {
		this.secondaryYield = secondaryYield;
	}

	/**
	 * <p>Getter for the field <code>yieldVolume</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getYieldVolume() {
		return yieldVolume;
	}

	/**
	 * <p>Setter for the field <code>yieldVolume</code>.</p>
	 *
	 * @param yieldVolume a {@link java.lang.Double} object.
	 */
	public void setYieldVolume(Double yieldVolume) {
		this.yieldVolume = yieldVolume;
	}

	/**
	 * <p>Getter for the field <code>productLossPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:productLossPerc")
	public Double getProductLossPerc() {
		return productLossPerc;
	}

	/**
	 * <p>Setter for the field <code>productLossPerc</code>.</p>
	 *
	 * @param lossPerc a {@link java.lang.Double} object.
	 */
	public void setProductLossPerc(Double lossPerc) {
		this.productLossPerc = lossPerc;
	}

	/**
	 * <p>Getter for the field <code>componentLossPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:componentLossPerc")
	public Double getComponentLossPerc() {
		return componentLossPerc;
	}

	/**
	 * <p>Setter for the field <code>componentLossPerc</code>.</p>
	 *
	 * @param componentLossPerc a {@link java.lang.Double} object.
	 */
	public void setComponentLossPerc(Double componentLossPerc) {
		this.componentLossPerc = componentLossPerc;
	}

	/**
	 * <p>Getter for the field <code>recipeQtyUsedWithLossPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getRecipeQtyUsedWithLossPerc() {
		return recipeQtyUsedWithLossPerc;
	}

	/**
	 * <p>Setter for the field <code>recipeQtyUsedWithLossPerc</code>.</p>
	 *
	 * @param recipeQtyUsedWithLossPerc a {@link java.lang.Double} object.
	 */
	public void setRecipeQtyUsedWithLossPerc(Double recipeQtyUsedWithLossPerc) {
		this.recipeQtyUsedWithLossPerc = recipeQtyUsedWithLossPerc;
	}

	/**
	 * <p>Getter for the field <code>recipeQtyUsed</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:productCompoQtyUsed")
	public Double getRecipeQtyUsed() {
		return recipeQtyUsed;
	}

	/**
	 * <p>Setter for the field <code>recipeQtyUsed</code>.</p>
	 *
	 * @param recipeQtyUsed a {@link java.lang.Double} object.
	 */
	public void setRecipeQtyUsed(Double recipeQtyUsed) {
		this.recipeQtyUsed = recipeQtyUsed;
	}

	/**
	 * <p>Getter for the field <code>recipeVolumeUsed</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:productCompoVolumeUsed")
	public Double getRecipeVolumeUsed() {
		return recipeVolumeUsed;
	}

	/**
	 * <p>Setter for the field <code>recipeVolumeUsed</code>.</p>
	 *
	 * @param recipeVolumeUsed a {@link java.lang.Double} object.
	 */
	public void setRecipeVolumeUsed(Double recipeVolumeUsed) {
		this.recipeVolumeUsed = recipeVolumeUsed;
	}

	/**
	 * <p>Getter for the field <code>netWeight</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:netWeight")
	public Double getNetWeight() {
		return netWeight;
	}

	/**
	 * <p>Setter for the field <code>netWeight</code>.</p>
	 *
	 * @param netWeight a {@link java.lang.Double} object.
	 */
	public void setNetWeight(Double netWeight) {
		this.netWeight = netWeight;
	}

	/**
	 * <p>Getter for the field <code>weightPrimary</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getWeightPrimary() {
		return weightPrimary;
	}

	/**
	 * <p>Setter for the field <code>weightPrimary</code>.</p>
	 *
	 * @param weightPrimary a {@link java.lang.Double} object.
	 */
	public void setWeightPrimary(Double weightPrimary) {
		this.weightPrimary = weightPrimary;
	}

	/**
	 * <p>Getter for the field <code>netWeightSecondary</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getNetWeightSecondary() {
		return netWeightSecondary;
	}

	/**
	 * <p>Setter for the field <code>netWeightSecondary</code>.</p>
	 *
	 * @param netWeightSecondary a {@link java.lang.Double} object.
	 */
	public void setNetWeightSecondary(Double netWeightSecondary) {
		this.netWeightSecondary = netWeightSecondary;
	}

	/**
	 * <p>Getter for the field <code>weightSecondary</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getWeightSecondary() {
		return weightSecondary;
	}

	/**
	 * <p>Setter for the field <code>weightSecondary</code>.</p>
	 *
	 * @param weightSecondary a {@link java.lang.Double} object.
	 */
	public void setWeightSecondary(Double weightSecondary) {
		this.weightSecondary = weightSecondary;
	}

	/**
	 * <p>Getter for the field <code>netWeightTertiary</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getNetWeightTertiary() {
		return netWeightTertiary;
	}

	/**
	 * <p>Setter for the field <code>netWeightTertiary</code>.</p>
	 *
	 * @param netWeightTertiary a {@link java.lang.Double} object.
	 */
	public void setNetWeightTertiary(Double netWeightTertiary) {
		this.netWeightTertiary = netWeightTertiary;
	}

	/**
	 * <p>Getter for the field <code>weightTertiary</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getWeightTertiary() {
		return weightTertiary;
	}

	/**
	 * <p>Setter for the field <code>weightTertiary</code>.</p>
	 *
	 * @param weightTertiary a {@link java.lang.Double} object.
	 */
	public void setWeightTertiary(Double weightTertiary) {
		this.weightTertiary = weightTertiary;
	}

	/**
	 * <p>Getter for the field <code>dropPackagingOfComponents</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:dropPackagingOfComponents")
	public Boolean getDropPackagingOfComponents() {
		return dropPackagingOfComponents;
	}

	/**
	 * <p>Setter for the field <code>dropPackagingOfComponents</code>.</p>
	 *
	 * @param dropPackagingOfComponents a {@link java.lang.Boolean} object.
	 */
	public void setDropPackagingOfComponents(Boolean dropPackagingOfComponents) {
		this.dropPackagingOfComponents = dropPackagingOfComponents;
	}

	/**
	 * <p>Getter for the field <code>netVolume</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:netVolume")
	public Double getNetVolume() {
		return netVolume;
	}

	/**
	 * <p>Setter for the field <code>netVolume</code>.</p>
	 *
	 * @param netVolume a {@link java.lang.Double} object.
	 */
	public void setNetVolume(Double netVolume) {
		this.netVolume = netVolume;
	}

	/**
	 * <p>Getter for the field <code>servingSize</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:servingSize")
	public Double getServingSize() {
		return servingSize;
	}

	/**
	 * <p>Setter for the field <code>servingSize</code>.</p>
	 *
	 * @param servingSize a {@link java.lang.Double} object.
	 */
	public void setServingSize(Double servingSize) {
		this.servingSize = servingSize;
	}

	/**
	 * <p>Getter for the field <code>servingSizeByCountry</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfMlText
	@AlfProp
	@AlfQname(qname = "bcpg:servingSizeByCountry")
	public MLText getServingSizeByCountry() {
		return servingSizeByCountry;
	}

	/**
	 * <p>Setter for the field <code>servingSizeByCountry</code>.</p>
	 *
	 * @param servingSizeByCountry a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setServingSizeByCountry(MLText servingSizeByCountry) {
		this.servingSizeByCountry = servingSizeByCountry;
	}

	/**
	 * <p>Getter for the field <code>servingSizeUnit</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:servingSizeUnit")
	public ProductUnit getServingSizeUnit() {
		return servingSizeUnit;
	}

	/**
	 * <p>Setter for the field <code>servingSizeUnit</code>.</p>
	 *
	 * @param servingSizeUnit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 */
	public void setServingSizeUnit(ProductUnit servingSizeUnit) {
		this.servingSizeUnit = servingSizeUnit;
	}

	/**
	 * <p>Getter for the field <code>tare</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pack:tare")
	public Double getTare() {
		return tare;
	}

	/**
	 * <p>Setter for the field <code>tare</code>.</p>
	 *
	 * @param tare a {@link java.lang.Double} object.
	 */
	public void setTare(Double tare) {
		this.tare = tare;
	}

	/**
	 * <p>Getter for the field <code>tareUnit</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.TareUnit} object.
	 */
	@AlfProp
	@AlfQname(qname = "pack:tareUnit")
	public TareUnit getTareUnit() {
		return tareUnit;
	}

	/**
	 * <p>Setter for the field <code>tareUnit</code>.</p>
	 *
	 * @param tareUnit a {@link fr.becpg.repo.product.data.constraints.TareUnit} object.
	 */
	public void setTareUnit(TareUnit tareUnit) {
		this.tareUnit = tareUnit;
	}

	/**
	 * <p>Getter for the field <code>unitTotalCost</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:unitTotalCost")
	public Double getUnitTotalCost() {
		return unitTotalCost;
	}

	/**
	 * <p>Setter for the field <code>unitTotalCost</code>.</p>
	 *
	 * @param unitTotalCost a {@link java.lang.Double} object.
	 */
	public void setUnitTotalCost(Double unitTotalCost) {
		this.unitTotalCost = unitTotalCost;
	}

	/**
	 * <p>Getter for the field <code>previousUnitTotalCost</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:previousUnitTotalCost")
	public Double getPreviousUnitTotalCost() {
		return previousUnitTotalCost;
	}

	/**
	 * <p>Setter for the field <code>previousUnitTotalCost</code>.</p>
	 *
	 * @param previousUnitTotalCost a {@link java.lang.Double} object.
	 */
	public void setPreviousUnitTotalCost(Double previousUnitTotalCost) {
		this.previousUnitTotalCost = previousUnitTotalCost;
	}

	/**
	 * <p>Getter for the field <code>geoOrigins</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:productGeoOrigin")
	public List<NodeRef> getGeoOrigins() {
		return geoOrigins;
	}

	/**
	 * <p>Setter for the field <code>geoOrigins</code>.</p>
	 *
	 * @param geoOrigins a {@link java.util.List} object.
	 */
	public void setGeoOrigins(List<NodeRef> geoOrigins) {
		this.geoOrigins = geoOrigins;
	}

	/**
	 * <p>Getter for the field <code>futureUnitTotalCost</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:futureUnitTotalCost")
	public Double getFutureUnitTotalCost() {
		return futureUnitTotalCost;
	}

	/**
	 * <p>Setter for the field <code>futureUnitTotalCost</code>.</p>
	 *
	 * @param futureUnitTotalCost a {@link java.lang.Double} object.
	 */
	public void setFutureUnitTotalCost(Double futureUnitTotalCost) {
		this.futureUnitTotalCost = futureUnitTotalCost;
	}

	/**
	 * <p>Getter for the field <code>unitPrice</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:unitPrice")
	public Double getUnitPrice() {
		return unitPrice;
	}

	/**
	 * <p>Setter for the field <code>unitPrice</code>.</p>
	 *
	 * @param unitPrice a {@link java.lang.Double} object.
	 */
	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}

	/**
	 * <p>Getter for the field <code>profitability</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:profitability")
	public Double getProfitability() {
		return profitability;
	}

	/**
	 * <p>Setter for the field <code>profitability</code>.</p>
	 *
	 * @param profitability a {@link java.lang.Double} object.
	 */
	public void setProfitability(Double profitability) {
		this.profitability = profitability;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:nutrientPreparationState")
	public List<String> getPreparationStates() {
		return preparationStates;
	}

	public void setPreparationStates(List<String> preparationStates) {
		this.preparationStates = preparationStates;
	}

	public boolean isPrepared() {
		return preparationStates != null && preparationStates.contains("Prepared");
	}

	@AlfProp
	@AlfQname(qname = "bcpg:ecoScore")
	public Double getEcoScore() {
		return ecoScore;
	}

	public void setEcoScore(Double ecoScore) {
		this.ecoScore = ecoScore;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:ecoScoreClass")
	public String getEcoScoreClass() {
		return ecoScoreClass;
	}

	public void setEcoScoreClass(String ecoScoreClass) {
		this.ecoScoreClass = ecoScoreClass;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:ecoScoreCategory")
	public String getEcoScoreCategory() {
		return ecoScoreCategory;
	}

	public void setEcoScoreCategory(String ecoScoreCategory) {
		this.ecoScoreCategory = ecoScoreCategory;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:ecoScoreDetails")
	public String getEcoScoreDetails() {
		return ecoScoreDetails;
	}

	public void setEcoScoreDetails(String ecoScoreDetails) {
		this.ecoScoreDetails = ecoScoreDetails;
	}

	/**
	 * <p>Getter for the field <code>nutrientScore</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:nutrientProfilingScore")
	public Double getNutrientScore() {
		return nutrientScore;
	}

	/**
	 * <p>Setter for the field <code>nutrientScore</code>.</p>
	 *
	 * @param nutrientScore a {@link java.lang.Double} object.
	 */
	public void setNutrientScore(Double nutrientScore) {
		this.nutrientScore = nutrientScore;
	}

	/**
	 * <p>Getter for the field <code>nutrientClass</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:nutrientProfilingClass")
	public String getNutrientClass() {
		return nutrientClass;
	}

	/**
	 * <p>Setter for the field <code>nutrientClass</code>.</p>
	 *
	 * @param nutrientClass a {@link java.lang.String} object.
	 */
	public void setNutrientClass(String nutrientClass) {
		this.nutrientClass = nutrientClass;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:nutrientProfilingDetails")
	public String getNutrientDetails() {
		return nutrientDetails;
	}

	public void setNutrientDetails(String nutrientDetails) {
		this.nutrientDetails = nutrientDetails;
	}

	/**
	 * <p>Getter for the field <code>nutrientProfile</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:nutrientProfileRef")
	public NodeRef getNutrientProfile() {
		return nutrientProfile;
	}

	/**
	 * <p>Setter for the field <code>nutrientProfile</code>.</p>
	 *
	 * @param nutrientProfile a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setNutrientProfile(NodeRef nutrientProfile) {
		this.nutrientProfile = nutrientProfile;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:nutrientProfileCategory")
	public String getNutrientProfileCategory() {
		return nutrientProfileCategory;
	}

	public void setNutrientProfileCategory(String nutrientProfileCategory) {
		this.nutrientProfileCategory = nutrientProfileCategory;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:nutrientProfileVersion")
	public String getNutrientProfileVersion() {
		return nutrientProfileVersion;
	}

	public void setNutrientProfileVersion(String nutrientProfileVersion) {
		this.nutrientProfileVersion = nutrientProfileVersion;
	}

	/**
	 * <p>Getter for the field <code>meatContentData</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:meatContentData")
	@InternalField
	public String getMeatContentData() {
		if (meatContentData == null || meatContentData.isEmpty()) {
			return null;
		} else {
			try {
				return MeatContentData.toJsonString(meatContentData);
			} catch (JSONException e) {
				return null;
			}
		}
	}

	/**
	 * <p>Setter for the field <code>meatContentData</code>.</p>
	 *
	 * @param meatContentdata a {@link java.lang.String} object.
	 */
	public void setMeatContentData(String meatContentdata) {
		try {
			meatContentData = MeatContentData.parseJsonString(meatContentdata);
		} catch (JSONException e) {
			logger.warn("Cannot parse meatContent JSON", e);
		}
	}

	/**
	 * <p>Getter for the field <code>meatType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:meatType")
	public String getMeatType() {
		return meatType;
	}

	/**
	 * <p>Setter for the field <code>meatType</code>.</p>
	 *
	 * @param meatType a {@link java.lang.String} object.
	 */
	public void setMeatType(String meatType) {
		this.meatType = meatType;
	}

	// Spel Helper
	/**
	 * <p>meatContentByType.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.product.data.meat.MeatContentData} object.
	 */
	public MeatContentData meatContentByType(String type) {
		return meatContentData.get(type);
	}

	/**
	 * <p>meatContentApplied.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String meatContentApplied() {
		StringBuilder ret = new StringBuilder();
		for (Map.Entry<String, MeatContentData> val : meatContentData.entrySet()) {
			ret.append(val.getKey() + ": " + val.getValue().getMeatContent() + "%\n");
		}
		return ret.toString();

	}

	/**
	 * <p>getMeatContents.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, MeatContentData> getMeatContents() {
		return meatContentData;
	}

	/**
	 * <p>Getter for the field <code>breakEven</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:breakEven")
	public Long getBreakEven() {
		return breakEven;
	}

	/**
	 * <p>Setter for the field <code>breakEven</code>.</p>
	 *
	 * @param breakEven a {@link java.lang.Long} object.
	 */
	public void setBreakEven(Long breakEven) {
		this.breakEven = breakEven;
	}

	/**
	 * <p>Getter for the field <code>projectedQty</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:projectedQty")
	public Long getProjectedQty() {
		return projectedQty;
	}

	/**
	 * <p>Setter for the field <code>projectedQty</code>.</p>
	 *
	 * @param projectedQty a {@link java.lang.Long} object.
	 */
	public void setProjectedQty(Long projectedQty) {
		this.projectedQty = projectedQty;
	}

	/**
	 * <p>Getter for the field <code>allergenList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:allergenList")
	public List<AllergenListDataItem> getAllergenList() {
		return allergenList;
	}

	/**
	 * <p>Setter for the field <code>allergenList</code>.</p>
	 *
	 * @param allergenList a {@link java.util.List} object.
	 */
	public void setAllergenList(List<AllergenListDataItem> allergenList) {
		this.allergenList = allergenList;
	}

	/**
	 * <p>Getter for the field <code>costList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:costList")
	public List<CostListDataItem> getCostList() {
		return costList;
	}

	/**
	 * <p>Setter for the field <code>costList</code>.</p>
	 *
	 * @param costList a {@link java.util.List} object.
	 */
	public void setCostList(List<CostListDataItem> costList) {
		this.costList = costList;
	}

	@DataList
	@AlfQname(qname = "bcpg:lcaList")
	public List<LCAListDataItem> getLcaList() {
		return lcaList;
	}

	public void setLcaList(List<LCAListDataItem> lcaList) {
		this.lcaList = lcaList;
	}

	/**
	 * <p>Getter for the field <code>priceList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:priceList")
	public List<PriceListDataItem> getPriceList() {
		return priceList;
	}

	/**
	 * <p>Setter for the field <code>priceList</code>.</p>
	 *
	 * @param priceList a {@link java.util.List} object.
	 */
	public void setPriceList(List<PriceListDataItem> priceList) {
		this.priceList = priceList;
	}

	/**
	 * <p>Getter for the field <code>ingList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:ingList")
	public List<IngListDataItem> getIngList() {
		return ingList;
	}

	/**
	 * <p>Setter for the field <code>ingList</code>.</p>
	 *
	 * @param ingList a {@link java.util.List} object.
	 */
	public void setIngList(List<IngListDataItem> ingList) {
		this.ingList = ingList;
	}

	/**
	 * <p>Getter for the field <code>nutList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:nutList")
	public List<NutListDataItem> getNutList() {
		return nutList;
	}

	/**
	 * <p>Setter for the field <code>nutList</code>.</p>
	 *
	 * @param nutList a {@link java.util.List} object.
	 */
	public void setNutList(List<NutListDataItem> nutList) {
		this.nutList = nutList;
	}

	/**
	 * <p>Getter for the field <code>organoList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:organoList")
	public List<OrganoListDataItem> getOrganoList() {
		return organoList;
	}

	/**
	 * <p>Setter for the field <code>organoList</code>.</p>
	 *
	 * @param organoList a {@link java.util.List} object.
	 */
	public void setOrganoList(List<OrganoListDataItem> organoList) {
		this.organoList = organoList;
	}

	/**
	 * <p>Getter for the field <code>microbioList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:microbioList")
	public List<MicrobioListDataItem> getMicrobioList() {
		return microbioList;
	}

	/**
	 * <p>Setter for the field <code>microbioList</code>.</p>
	 *
	 * @param microbioList a {@link java.util.List} object.
	 */
	public void setMicrobioList(List<MicrobioListDataItem> microbioList) {
		this.microbioList = microbioList;
	}

	/**
	 * <p>Getter for the field <code>physicoChemList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:physicoChemList")
	public List<PhysicoChemListDataItem> getPhysicoChemList() {
		return physicoChemList;
	}

	/**
	 * <p>Setter for the field <code>physicoChemList</code>.</p>
	 *
	 * @param physicoChemList a {@link java.util.List} object.
	 */
	public void setPhysicoChemList(List<PhysicoChemListDataItem> physicoChemList) {
		this.physicoChemList = physicoChemList;
	}

	/**
	 * <p>Getter for the field <code>labelClaimList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:labelClaimList")
	public List<LabelClaimListDataItem> getLabelClaimList() {
		return labelClaimList;
	}

	/**
	 * <p>Setter for the field <code>labelClaimList</code>.</p>
	 *
	 * @param labelClaimList a {@link java.util.List} object.
	 */
	public void setLabelClaimList(List<LabelClaimListDataItem> labelClaimList) {
		this.labelClaimList = labelClaimList;
	}

	/**
	 * <p>Getter for the field <code>controlDefList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "qa:controlDefList")
	public List<ControlDefListDataItem> getControlDefList() {
		return controlDefList;
	}

	/**
	 * <p>Setter for the field <code>controlDefList</code>.</p>
	 *
	 * @param controlDefList a {@link java.util.List} object.
	 */
	public void setControlDefList(List<ControlDefListDataItem> controlDefList) {
		this.controlDefList = controlDefList;
	}

	/**
	 * <p>Getter for the field <code>resourceParamList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "mpm:resourceParamList")
	public List<ResourceParamListItem> getResourceParamList() {
		return resourceParamList;
	}

	/**
	 * <p>Setter for the field <code>resourceParamList</code>.</p>
	 *
	 * @param resourceParamList a {@link java.util.List} object.
	 */
	public void setResourceParamList(List<ResourceParamListItem> resourceParamList) {
		this.resourceParamList = resourceParamList;
	}

	/**
	 * <p>Getter for the field <code>labelingList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "pack:labelingList")
	public List<LabelingListDataItem> getLabelingList() {
		return labelingList;
	}

	/**
	 * <p>Setter for the field <code>labelingList</code>.</p>
	 *
	 * @param labelingList a {@link java.util.List} object.
	 */
	public void setLabelingList(List<LabelingListDataItem> labelingList) {
		this.labelingList = labelingList;
	}

	/**
	 * <p>Getter for the field <code>labelingListView</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.LabelingListView} object.
	 */
	@DataListView
	@AlfQname(qname = "bcpg:ingLabelingList")
	public LabelingListView getLabelingListView() {
		return labelingListView;
	}

	/**
	 * <p>Setter for the field <code>labelingListView</code>.</p>
	 *
	 * @param labelingListView a {@link fr.becpg.repo.product.data.LabelingListView} object.
	 */
	public void setLabelingListView(LabelingListView labelingListView) {
		this.labelingListView = labelingListView;
	}

	/**
	 * <p>Getter for the field <code>packMaterialList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "pack:packMaterialList")
	public List<PackMaterialListDataItem> getPackMaterialList() {
		return packMaterialList;
	}

	/**
	 * <p>Setter for the field <code>packMaterialList</code>.</p>
	 *
	 * @param packMaterialList a {@link java.util.List} object.
	 */
	public void setPackMaterialList(List<PackMaterialListDataItem> packMaterialList) {
		this.packMaterialList = packMaterialList;
	}

	@DataList
	@AlfQname(qname = "qa:stockList")
	public List<StockListDataItem> getStockList() {
		return stockList;
	}

	public void setStockList(List<StockListDataItem> stockList) {
		this.stockList = stockList;
	}

	@DataList
	@AlfQname(qname = "bcpg:regulatoryList")
	public List<RegulatoryListDataItem> getRegulatoryList() {
		return regulatoryList;
	}

	public void setRegulatoryList(List<RegulatoryListDataItem> regulatoryList) {
		this.regulatoryList = regulatoryList;
	}

	@DataList
	@AlfQname(qname = "bcpg:ingRegulatoryList")
	public List<IngRegulatoryListDataItem> getIngRegulatoryList() {
		return ingRegulatoryList;
	}

	public void setIngRegulatoryList(List<IngRegulatoryListDataItem> ingRegulatoryList) {
		this.ingRegulatoryList = ingRegulatoryList;
	}

	/**
	 * <p>Getter for the field <code>svhcList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:svhcList")
	public List<SvhcListDataItem> getSvhcList() {
		return svhcList;
	}

	/**
	 * <p>Setter for the field <code>svhcList</code>.</p>
	 *
	 * @param svhcList a {@link java.util.List} object.
	 */
	public void setSvhcList(List<SvhcListDataItem> svhcList) {
		this.svhcList = svhcList;
	}

	/**
	 * <p>Getter for the field <code>compoListView</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.CompoListView} object.
	 */
	@DataListView
	@AlfQname(qname = "bcpg:compoList")
	public CompoListView getCompoListView() {
		return compoListView;
	}

	/**
	 * <p>Getter for the field <code>entityScore</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:entityScore")
	public String getEntityScore() {
		return entityScore;
	}

	/**
	 * <p>Setter for the field <code>entityScore</code>.</p>
	 *
	 * @param string a {@link java.lang.String} object.
	 */
	public void setEntityScore(String string) {
		this.entityScore = string;
	}

	/**
	 * <p>Getter for the field <code>reportLocales</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfProp
	@AlfQname(qname = "rep:reportLocales")
	public List<String> getReportLocales() {
		return reportLocales;
	}

	/**
	 * <p>Setter for the field <code>reportLocales</code>.</p>
	 *
	 * @param reportLocales a {@link java.util.List} object.
	 */
	public void setReportLocales(List<String> reportLocales) {
		this.reportLocales = reportLocales;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryMode")
	public DecernisMode getRegulatoryMode() {
		return regulatoryMode;
	}

	public void setRegulatoryMode(DecernisMode regulatoryMode) {
		this.regulatoryMode = regulatoryMode;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryUrl")
	public String getRegulatoryUrl() {
		return regulatoryUrl;
	}

	public void setRegulatoryUrl(String regulatoryUrl) {
		this.regulatoryUrl = regulatoryUrl;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryResult")
	public RegulatoryResult getRegulatoryResult() {
		return regulatoryResult;
	}

	public void setRegulatoryResult(RegulatoryResult regulatoryResult) {
		this.regulatoryResult = regulatoryResult;
	}

	/**
	 * <p>Getter for the field <code>regulatoryRecipeId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryRecipeId")
	public String getRegulatoryRecipeId() {
		return this.regulatoryRecipeId;
	}

	/**
	 * <p>Setter for the field <code>regulatoryRecipeId</code>.</p>
	 *
	 * @param regulatoryRecipeId a {@link java.lang.String} object.
	 */
	public void setRegulatoryRecipeId(String regulatoryRecipeId) {
		this.regulatoryRecipeId = regulatoryRecipeId;
	}

	/**
	 * <p>Getter for the field <code>regulatoryCountries</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	@InternalField
	public List<NodeRef> getRegulatoryCountriesRef() {
		return regulatoryCountriesRef;
	}

	/**
	 * <p>Setter for the field <code>regulatoryCountries</code>.</p>
	 *
	 * @param regulatoryCountries a {@link java.util.List} object.
	 */
	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries) {
		this.regulatoryCountriesRef = regulatoryCountries;
	}
	
	public List<String> getRegulatoryCountries() {
		return regulatoryCountries;
	}
	
	public void setRegulatoryCountries(List<String> regulatoryCountries) {
		this.regulatoryCountries = regulatoryCountries;
	}
	
	public List<String> getRegulatoryUsages() {
		return regulatoryUsages;
	}
	
	public void setRegulatoryUsages(List<String> regulatoryUsages) {
		this.regulatoryUsages = regulatoryUsages;
	}

	/**
	 * <p>Getter for the field <code>regulatoryUsages</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	@InternalField
	public List<NodeRef> getRegulatoryUsagesRef() {
		return regulatoryUsagesRef;
	}

	/**
	 * <p>Setter for the field <code>regulatoryUsages</code>.</p>
	 *
	 * @param regulatoryUsages a {@link java.util.List} object.
	 */
	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages) {
		this.regulatoryUsagesRef = regulatoryUsages;
	}

	/**
	 * <p>Getter for the field <code>regulatoryFormulatedDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryFormulatedDate")
	public Date getRegulatoryFormulatedDate() {
		return regulatoryFormulatedDate;
	}

	/**
	 * <p>Setter for the field <code>regulatoryFormulatedDate</code>.</p>
	 *
	 * @param regulatoryFormulatedDate a {@link java.util.Date} object.
	 */
	public void setRegulatoryFormulatedDate(Date regulatoryFormulatedDate) {
		this.regulatoryFormulatedDate = regulatoryFormulatedDate;
	}

	/**
	 * <p>Getter for the field <code>requirementChecksum</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:requirementChecksum")
	public String getRequirementChecksum() {
		return requirementChecksum;
	}

	/**
	 * <p>Setter for the field <code>requirementChecksum</code>.</p>
	 *
	 * @param requirementChecksum a {@link java.lang.String} object.
	 */
	public void setRequirementChecksum(String requirementChecksum) {
		this.requirementChecksum = requirementChecksum;
	}

	private <T> List<T> filterList(List<T> list, List<DataListFilter<ProductData, T>> filters) {
		if ((filters != null) && !filters.isEmpty()) {
			Stream<T> stream = list.stream();
			for (DataListFilter<ProductData, T> filter : filters) {
				stream = stream.filter(filter.createPredicate(this));
			}
			return stream.collect(Collectors.toList());
		}
		return list;
	}

	/**
	 * <p>getCompoList.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<CompoListDataItem> getCompoList() {
		return getCompoList(Collections.emptyList());
	}

	/**
	 * <p>getCompoList.</p>
	 *
	 * @param filter a {@link fr.becpg.repo.repository.filters.DataListFilter} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<CompoListDataItem> getCompoList(DataListFilter<ProductData, CompoListDataItem> filter) {
		return getCompoList(Collections.singletonList(filter));
	}

	/**
	 * <p>getCompoList.</p>
	 *
	 * @param filters a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<CompoListDataItem> getCompoList(List<DataListFilter<ProductData, CompoListDataItem>> filters) {
		if ((compoListView != null) && (compoListView.getCompoList() != null)) {
			return filterList(compoListView.getCompoList(), filters);
		}
		return null;
	}

	/**
	 * <p>hasCompoListEl.</p>
	 *
	 * @return a boolean.
	 */
	public boolean hasCompoListEl() {
		return hasCompoListEl(Collections.emptyList());
	}

	/**
	 * <p>hasCompoListEl.</p>
	 *
	 * @param filter a {@link fr.becpg.repo.repository.filters.DataListFilter} object.
	 * @return a boolean.
	 */
	public boolean hasCompoListEl(DataListFilter<ProductData, CompoListDataItem> filter) {
		return hasCompoListEl(Collections.singletonList(filter));
	}

	/**
	 * <p>hasCompoListEl.</p>
	 *
	 * @param filters a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public boolean hasCompoListEl(List<DataListFilter<ProductData, CompoListDataItem>> filters) {
		return (compoListView != null) && (compoListView.getCompoList() != null) && !getCompoList(filters).isEmpty();
	}

	/**
	 * <p>Setter for the field <code>compoListView</code>.</p>
	 *
	 * @param compoListView a {@link fr.becpg.repo.product.data.CompoListView} object.
	 */
	public void setCompoListView(CompoListView compoListView) {
		this.compoListView = compoListView;
	}

	/**
	 * <p>Getter for the field <code>processListView</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ProcessListView} object.
	 */
	@DataListView
	@AlfQname(qname = "mpm:processList")
	public ProcessListView getProcessListView() {
		return processListView;
	}

	/**
	 * <p>getProcessList.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<ProcessListDataItem> getProcessList() {
		return getProcessList(Collections.emptyList());
	}

	/**
	 * <p>getProcessList.</p>
	 *
	 * @param filter a {@link fr.becpg.repo.repository.filters.DataListFilter} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<ProcessListDataItem> getProcessList(DataListFilter<ProductData, ProcessListDataItem> filter) {
		return getProcessList(Collections.singletonList(filter));
	}

	/**
	 * <p>getProcessList.</p>
	 *
	 * @param filters a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<ProcessListDataItem> getProcessList(List<DataListFilter<ProductData, ProcessListDataItem>> filters) {
		if ((processListView != null) && (processListView.getProcessList() != null)) {
			return filterList(processListView.getProcessList(), filters);
		}
		return null;
	}

	/**
	 * <p>hasProcessListEl.</p>
	 *
	 * @return a boolean.
	 */
	public boolean hasProcessListEl() {
		return hasProcessListEl(Collections.emptyList());
	}

	/**
	 * <p>hasProcessListEl.</p>
	 *
	 * @param filter a {@link fr.becpg.repo.repository.filters.DataListFilter} object.
	 * @return a boolean.
	 */
	public boolean hasProcessListEl(DataListFilter<ProductData, ProcessListDataItem> filter) {
		return hasProcessListEl(Collections.singletonList(filter));
	}

	/**
	 * <p>hasProcessListEl.</p>
	 *
	 * @param filters a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public boolean hasProcessListEl(List<DataListFilter<ProductData, ProcessListDataItem>> filters) {
		return (processListView != null) && (processListView.getProcessList() != null) && !getProcessList(filters).isEmpty();
	}

	/**
	 * <p>Setter for the field <code>processListView</code>.</p>
	 *
	 * @param processListView a {@link fr.becpg.repo.product.data.ProcessListView} object.
	 */
	public void setProcessListView(ProcessListView processListView) {
		this.processListView = processListView;
	}

	/**
	 * <p>Getter for the field <code>packagingListView</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.PackagingListView} object.
	 */
	@DataListView
	@AlfQname(qname = "bcpg:packagingList")
	public PackagingListView getPackagingListView() {
		return packagingListView;
	}

	/**
	 * <p>getPackagingList.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<PackagingListDataItem> getPackagingList() {
		return getPackagingList(Collections.emptyList());
	}

	/**
	 * <p>getPackagingList.</p>
	 *
	 * @param filter a {@link fr.becpg.repo.repository.filters.DataListFilter} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<PackagingListDataItem> getPackagingList(DataListFilter<ProductData, PackagingListDataItem> filter) {
		return getPackagingList(Collections.singletonList(filter));
	}

	/**
	 * <p>getPackagingList.</p>
	 *
	 * @param filters a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<PackagingListDataItem> getPackagingList(List<DataListFilter<ProductData, PackagingListDataItem>> filters) {
		if ((packagingListView != null) && (packagingListView.getPackagingList() != null)) {
			return filterList(packagingListView.getPackagingList(), filters);
		}
		return null;
	}

	/**
	 * <p>hasPackagingListEl.</p>
	 *
	 * @return a boolean.
	 */
	public boolean hasPackagingListEl() {
		return hasPackagingListEl(Collections.emptyList());
	}

	/**
	 * <p>hasPackagingListEl.</p>
	 *
	 * @param filter a {@link fr.becpg.repo.repository.filters.DataListFilter} object.
	 * @return a boolean.
	 */
	public boolean hasPackagingListEl(DataListFilter<ProductData, PackagingListDataItem> filter) {
		return hasPackagingListEl(Collections.singletonList(filter));
	}

	/**
	 * <p>hasPackagingListEl.</p>
	 *
	 * @param filters a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public boolean hasPackagingListEl(List<DataListFilter<ProductData, PackagingListDataItem>> filters) {
		return (packagingListView != null) && (packagingListView.getPackagingList() != null) && !getPackagingList(filters).isEmpty();
	}

	/**
	 * <p>Setter for the field <code>packagingListView</code>.</p>
	 *
	 * @param packagingListView a {@link fr.becpg.repo.product.data.PackagingListView} object.
	 */
	public void setPackagingListView(PackagingListView packagingListView) {
		this.packagingListView = packagingListView;
	}

	/**
	 * <p>getViews.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<AbstractProductDataView> getViews() {
		return Arrays.asList(compoListView, packagingListView, processListView);
	}

	/** {@inheritDoc} */
	@Override
	public String getEntityState() {
		return state != null ? state.toString() : null;
	}

	/**
	 * <p>isLiquid.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isLiquid() {
		if (servingSizeUnit != null && !servingSizeUnit.equals(ProductUnit.kg) && servingSizeUnit.isVolume()) {
			return true;
		} else {
			return (unit != null && unit.isVolume());
		}
	}

	/**
	 * <p>isRawMaterial.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isRawMaterial() {
		return this instanceof RawMaterialData;
	}

	public boolean isGeneric() {
		return isRawMaterial() || Boolean.TRUE.equals(isGeneric);
	}

	public void setIsGeneric(Boolean isGeneric) {
		this.isGeneric = isGeneric;
	}

	/**
	 * <p>isPackaging.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPackaging() {
		return this instanceof PackagingMaterialData;
	}

	/**
	 * <p>isPackagingKit.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPackagingKit() {
		return this instanceof PackagingKitData;
	}

	/**
	 * <p>isSemiFinished.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isSemiFinished() {
		return this instanceof SemiFinishedProductData;
	}

	/**
	 * <p>isLocalSemiFinished.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isLocalSemiFinished() {
		return this instanceof LocalSemiFinishedProductData;
	}

	/**
	 * <p>isFinishedProduct.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isFinishedProduct() {
		return this instanceof FinishedProductData;
	}

	public boolean isEntityTemplate() {
		return getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL);
	}

	/** {@inheritDoc} */
	@Override
	public Integer getReformulateCount() {
		return reformulateCount;
	}

	/** {@inheritDoc} */
	@Override
	public void setReformulateCount(Integer reformulateCount) {
		this.reformulateCount = reformulateCount;
	}

	/** {@inheritDoc} */
	@Override
	public Integer getCurrentReformulateCount() {
		return currentReformulateCount;
	}

	/** {@inheritDoc} */
	@Override
	public void setCurrentReformulateCount(Integer currentReformulateCount) {
		this.currentReformulateCount = currentReformulateCount;
	}

	/**
	 * <p>Getter for the field <code>formulationChainId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFormulationChainId() {
		return formulationChainId;
	}

	/** {@inheritDoc} */
	public void setFormulationChainId(String formulationChainId) {
		this.formulationChainId = formulationChainId;
	}

	/**
	 * Instantiates a new product data.
	 */
	public ProductData() {
		super();
	}
	


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ProductData [hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", legalName=" + legalName + ", title=" + title + ", erpCode="
				+ erpCode + ", state=" + state + ", unit=" + unit + ", qty=" + qty + ", netWeight=" + netWeight + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(clients, suppliers, supplierPlants, density, erpCode, formulatedDate, futureUnitTotalCost, hierarchy1,
				hierarchy2, ingType, legalName, netVolume, netWeight, netWeightSecondary, netWeightTertiary, nutrientClass, nutrientProfile,
				nutrientScore, plants, profitability, projectedQty, qty, recipeQtyUsed, recipeQtyUsedWithLossPerc, recipeVolumeUsed, reformulateCount,
				regulatoryCountriesRef, regulatoryUsagesRef, regulatoryMode, regulatoryRecipeId, reportLocales, servingSize, servingSizeByCountry,
				servingSizeUnit, state, tare, tareUnit, title, unit, unitPrice, unitTotalCost, updateFormulatedDate, weightPrimary, weightSecondary,
				weightTertiary, yield, yieldVolume, suppliers);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductData other = (ProductData) obj;
		return Objects.equals(clients, other.clients) && Objects.equals(suppliers, other.suppliers)
				&& Objects.equals(supplierPlants, other.supplierPlants) && Objects.equals(density, other.density)
				&& Objects.equals(erpCode, other.erpCode) && Objects.equals(formulatedDate, other.formulatedDate)
				&& Objects.equals(futureUnitTotalCost, other.futureUnitTotalCost) && Objects.equals(hierarchy1, other.hierarchy1)
				&& Objects.equals(hierarchy2, other.hierarchy2) && Objects.equals(ingType, other.ingType)
				&& Objects.equals(legalName, other.legalName) && Objects.equals(netVolume, other.netVolume)
				&& Objects.equals(netWeight, other.netWeight) && Objects.equals(netWeightSecondary, other.netWeightSecondary)
				&& Objects.equals(netWeightTertiary, other.netWeightTertiary) && Objects.equals(nutrientClass, other.nutrientClass)
				&& Objects.equals(nutrientProfile, other.nutrientProfile) && Objects.equals(nutrientScore, other.nutrientScore)
				&& Objects.equals(plants, other.plants) && Objects.equals(profitability, other.profitability)
				&& Objects.equals(projectedQty, other.projectedQty) && Objects.equals(qty, other.qty)
				&& Objects.equals(recipeQtyUsed, other.recipeQtyUsed) && Objects.equals(recipeQtyUsedWithLossPerc, other.recipeQtyUsedWithLossPerc)
				&& Objects.equals(recipeVolumeUsed, other.recipeVolumeUsed) && Objects.equals(reformulateCount, other.reformulateCount)
				&& Objects.equals(regulatoryCountriesRef, other.regulatoryCountriesRef) && Objects.equals(regulatoryUsagesRef, other.regulatoryUsagesRef)
				&& Objects.equals(regulatoryMode, other.regulatoryMode) && Objects.equals(regulatoryRecipeId, other.regulatoryRecipeId)
				&& Objects.equals(reportLocales, other.reportLocales) && Objects.equals(servingSize, other.servingSize)
				&& Objects.equals(servingSizeByCountry, other.servingSizeByCountry) && servingSizeUnit == other.servingSizeUnit
				&& state == other.state && Objects.equals(tare, other.tare) && tareUnit == other.tareUnit && Objects.equals(title, other.title)
				&& unit == other.unit && Objects.equals(unitPrice, other.unitPrice) && Objects.equals(unitTotalCost, other.unitTotalCost)
				&& Objects.equals(updateFormulatedDate, other.updateFormulatedDate) && Objects.equals(weightPrimary, other.weightPrimary)
				&& Objects.equals(weightSecondary, other.weightSecondary) && Objects.equals(weightTertiary, other.weightTertiary)
				&& Objects.equals(yield, other.yield) && Objects.equals(yieldVolume, other.yieldVolume);
	}

}
