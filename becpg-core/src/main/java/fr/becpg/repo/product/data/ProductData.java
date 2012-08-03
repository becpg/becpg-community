/*
 * 
 */
package fr.becpg.repo.product.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostDetailsListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.FormulateException;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductData.
 *
 * @author querephi
 */
public class ProductData extends BaseObject implements ProductElement {
	
	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The version label. */
	private String versionLabel;
	
	/** The hierarchy1. */
	private NodeRef hierarchy1;
	
	/** The hierarchy2. */
	private NodeRef hierarchy2;
	
	/** The name. */
	private String name;
	
	/** The legal name. */
	private MLText legalName;
	
	/** The title. */
	private String title;	
	
	/** The state. */
	private SystemState state = SystemState.ToValidate;
	
	/** The unit. */
	private ProductUnit unit = ProductUnit.kg;	
	
	/*
	 * Transformable properties
	 */
	private Double qty;
	private Double density;
	private Double yield;
	
	/*
	 * Profitability properties
	 */
	private Double unitTotalCost;	
	private Double unitPrice;	
	private Double profitability;	
	private Long breakEven;
	
	/** The lists container. */
	private NodeRef listsContainer;
	
	/** The allergen list. */
	private List<AllergenListDataItem> allergenList;
	
	/** The compo list. */
	private List<CompoListDataItem> compoList;

	/** The dynamicCharactList*/
	private List<DynamicCharactListItem> dynamicCharactList;
	
	/** The cost list. */
	private List<CostListDataItem> costList;
	
	private List<CostDetailsListDataItem> costDetailsList;
	
	private List<PriceListDataItem> priceList;
	
	/** The ing list. */
	private List<IngListDataItem> ingList;
	
	/** The nut list. */
	private List<NutListDataItem> nutList;
	
	/** The organo list. */
	private List<OrganoListDataItem> organoList;	
	
	/** The ing labeling list. */
	private List<IngLabelingListDataItem> ingLabelingList;
	
	/** The microbio list. */
	private List<MicrobioListDataItem> microbioList;
	
	/** The physico chem list. */
	private List<PhysicoChemListDataItem> physicoChemList;
	
	private List<PackagingListDataItem> packagingList;
	
	private List<ForbiddenIngListDataItem> forbiddenIngList;
	
	private List<ReqCtrlListDataItem> reqCtrlList;
	
	private List<ProcessListDataItem> processList;

	/**
	 * Gets the node ref.
	 *
	 * @return the node ref
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	
	/**
	 * Sets the node ref.
	 *
	 * @param nodeRef the new node ref
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	
	/**
	 * Gets the hierarchy1.
	 *
	 * @return the hierarchy1
	 */
	public NodeRef getHierarchy1() {
		return hierarchy1;
	}
	
	/**
	 * Sets the hierarchy1.
	 *
	 * @param hierarchy1 the new hierarchy1
	 */
	public void setHierarchy1(NodeRef hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}
	
	/**
	 * Gets the hierarchy2.
	 *
	 * @return the hierarchy2
	 */
	public NodeRef getHierarchy2() {
		return hierarchy2;
	}
	
	/**
	 * Sets the hierarchy2.
	 *
	 * @param hierarchy2 the new hierarchy2
	 */
	public void setHierarchy2(NodeRef hierarchy2) {
		this.hierarchy2 = hierarchy2;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the version label.
	 *
	 * @return the version label
	 */
	public String getVersionLabel() {
		return versionLabel;
	}
	
	/**
	 * Gets the legal name.
	 *
	 * @return the legal name
	 */
	public MLText getLegalName() {
		return legalName;
	}
	
	/**
	 * Sets the legal name.
	 *
	 * @param legalName the new legal name
	 */
	public void setLegalName(MLText legalName) {
		this.legalName = legalName;
	}
	
	public void setLegalName(String legalName) {
		this.legalName = new MLText(legalName);
	}
	
	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public SystemState getState() {
		return state;
	}
	
	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(SystemState state) {
		this.state = state;
	}
	
	/**
	 * Gets the unit.
	 *
	 * @return the unit
	 */
	public ProductUnit getUnit() {
		return unit;
	}
	
	/**
	 * Sets the unit.
	 *
	 * @param unit the new unit
	 */
	public void setUnit(ProductUnit unit) {
		this.unit = unit;
	}	
	
	/**
	 * Gets the qty.
	 *
	 * @return the qty
	 */
	public Double getQty() {
		return qty;
	}
	
	/**
	 * Sets the qty.
	 *
	 * @param qty the new qty
	 */
	public void setQty(Double qty) {
		this.qty = qty;
	}
	
	/**
	 * Gets the density.
	 *
	 * @return the density
	 */
	public Double getDensity() {
		return density;
	}
	
	/**
	 * Sets the density.
	 *
	 * @param density the new density
	 */
	public void setDensity(Double density) {
		this.density = density;
	}
	
	public Double getYield() {
		return yield;
	}

	public void setYield(Double yield) {
		this.yield = yield;
	}

	public Double getUnitTotalCost() {
		return unitTotalCost;
	}

	public void setUnitTotalCost(Double unitTotalCost) {
		this.unitTotalCost = unitTotalCost;
	}

	public Double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public Double getProfitability() {
		return profitability;
	}

	public void setProfitability(Double profitability) {
		this.profitability = profitability;
	}

	public Long getBreakEven() {
		return breakEven;
	}

	public void setBreakEven(Long breakEven) {
		this.breakEven = breakEven;
	}
	
	/**
	 * Gets the lists container.
	 *
	 * @return the lists container
	 */
	public NodeRef getListsContainer() {
		return listsContainer;
	}
	
	/**
	 * Sets the lists container.
	 *
	 * @param ListsContainer the new lists container
	 */
	public void setListsContainer(NodeRef ListsContainer) {
		this.listsContainer = ListsContainer;
	}	
	
	/**
	 * Gets the allergen list.
	 *
	 * @return the allergen list
	 */
	public List<AllergenListDataItem> getAllergenList() {
		return allergenList;
	}
	
	/**
	 * Sets the allergen list.
	 *
	 * @param allergenList the new allergen list
	 */
	public void setAllergenList(List<AllergenListDataItem> allergenList) {
		this.allergenList = allergenList;
	}
	
	/**
	 * Gets the compo list.
	 *
	 * @return the compo list
	 */
	public List<CompoListDataItem> getCompoList() {
		return compoList;
	}
	
	/**
	 * Sets the compo list.
	 *
	 * @param compoList the new compo list
	 */
	public void setCompoList(List<CompoListDataItem> compoList) {
		this.compoList = compoList;
	}
	
	/**
	 * Gets the cost list.
	 *
	 * @return the cost list
	 */
	public List<CostListDataItem> getCostList() {
		return costList;
	}
	
	/**
	 * Sets the cost list.
	 *
	 * @param costList the new cost list
	 */
	public void setCostList(List<CostListDataItem> costList) {
		this.costList = costList;
	}
	
	public List<CostDetailsListDataItem> getCostDetailsList() {
		return costDetailsList;
	}

	public void setCostDetailsList(List<CostDetailsListDataItem> costDetailsList) {
		this.costDetailsList = costDetailsList;
	}

	public List<PriceListDataItem> getPriceList() {
		return priceList;
	}

	public void setPriceList(List<PriceListDataItem> priceList) {
		this.priceList = priceList;
	}

	/**
	 * Gets the ing list.
	 *
	 * @return the ing list
	 */
	public List<IngListDataItem> getIngList() {
		return ingList;
	}
	
	/**
	 * Sets the ing list.
	 *
	 * @param ingList the new ing list
	 */
	public void setIngList(List<IngListDataItem> ingList) {
		this.ingList = ingList;
	}
	
	/**
	 * Gets the nut list.
	 *
	 * @return the nut list
	 */
	public List<NutListDataItem> getNutList() {
		return nutList;
	}
	
	/**
	 * Sets the nut list.
	 *
	 * @param nutList the new nut list
	 */
	public void setNutList(List<NutListDataItem> nutList) {
		this.nutList = nutList;
	}
	
	/**
	 * Gets the organo list.
	 *
	 * @return the organo list
	 */
	public List<OrganoListDataItem> getOrganoList() {
		return organoList;
	}
	
	/**
	 * Sets the organo list.
	 *
	 * @param organoList the new organo list
	 */
	public void setOrganoList(List<OrganoListDataItem> organoList) {
		this.organoList = organoList;
	}
	
	/**
	 * Gets the ing labeling list.
	 *
	 * @return the ing labeling list
	 */
	public List<IngLabelingListDataItem> getIngLabelingList() {
		return ingLabelingList;
	}
	
	/**
	 * Sets the ing labeling list.
	 *
	 * @param ingLabelingList the new ing labeling list
	 */
	public void setIngLabelingList(List<IngLabelingListDataItem> ingLabelingList) {
		this.ingLabelingList = ingLabelingList;
	}
	
	/**
	 * Gets the microbio list.
	 *
	 * @return the microbio list
	 */
	public List<MicrobioListDataItem> getMicrobioList() {
		return microbioList;
	}
	
	/**
	 * Sets the microbio list.
	 *
	 * @param microbioList the new microbio list
	 */
	public void setMicrobioList(List<MicrobioListDataItem> microbioList) {
		this.microbioList = microbioList;
	}
	
	/**
	 * Gets the physico chem list.
	 *
	 * @return the physico chem list
	 */
	public List<PhysicoChemListDataItem> getPhysicoChemList() {
		return physicoChemList;
	}
	
	/**
	 * Sets the physico chem list.
	 *
	 * @param physicoChemList the new physico chem list
	 */
	public void setPhysicoChemList(List<PhysicoChemListDataItem> physicoChemList) {
		this.physicoChemList = physicoChemList;
	}
	
	public List<PackagingListDataItem> getPackagingList() {
		return packagingList;
	}

	public void setPackagingList(List<PackagingListDataItem> packagingList) {
		this.packagingList = packagingList;
	}
	
	public List<ForbiddenIngListDataItem> getForbiddenIngList() {
		return forbiddenIngList;
	}

	public void setForbiddenIngList(List<ForbiddenIngListDataItem> forbiddenIngList) {
		this.forbiddenIngList = forbiddenIngList;
	}

	public List<ReqCtrlListDataItem> getReqCtrlList() {
		return reqCtrlList;
	}

	public void setReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList) {
		this.reqCtrlList = reqCtrlList;
	}

	public List<ProcessListDataItem> getProcessList() {
		return processList;
	}

	public void setProcessList(List<ProcessListDataItem> processList) {
		this.processList = processList;
	}


	public List<DynamicCharactListItem> getDynamicCharactList() {
		return dynamicCharactList;
	}

	public void setDynamicCharactList(List<DynamicCharactListItem> dynamicCharactList) {
		this.dynamicCharactList = dynamicCharactList;
	}

	/**
	 * Instantiates a new product data.
	 */
	public ProductData()
	{
	}
	
	/**
	 * Instantiates a new product data.
	 *
	 * @param productData the product data
	 */
	public ProductData(ProductData productData){
		
		nodeRef = productData.getNodeRef();
		setProperties(productData.getProperties());
		
		copyDataLists(productData);		
	}
	
	/**
	 * Gets the system state.
	 *
	 * @param systemState the system state
	 * @return the system state
	 */
	public static SystemState getSystemState(String systemState) {
		
		return (systemState != null && systemState != "") ? SystemState.valueOf(systemState) : SystemState.ToValidate;		
	}
		
	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Map<QName, Serializable> getProperties(){
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, this.getName());
		//properties.put(ContentModel.PROP_VERSION_LABEL, this.getVersionLabel()); alfresco system property, don't manage it for edition
		properties.put(ContentModel.PROP_TITLE, this.getTitle());	
		properties.put(BeCPGModel.PROP_PRODUCT_LEGALNAME, this.getLegalName());
		properties.put(BeCPGModel.PROP_PRODUCT_HIERARCHY1, this.getHierarchy1());
		properties.put(BeCPGModel.PROP_PRODUCT_HIERARCHY2, this.getHierarchy2());
		properties.put(BeCPGModel.PROP_PRODUCT_STATE, this.getState().toString());		
		properties.put(BeCPGModel.PROP_PRODUCT_UNIT, this.getUnit().toString());
		properties.put(BeCPGModel.PROP_PRODUCT_QTY, this.getQty());
		properties.put(BeCPGModel.PROP_PRODUCT_DENSITY, this.getDensity());		
		properties.put(BeCPGModel.PROP_UNIT_TOTAL_COST, this.getUnitTotalCost());
		properties.put(BeCPGModel.PROP_UNIT_PRICE, this.getUnitPrice());
		properties.put(BeCPGModel.PROP_PROFITABILITY, this.getProfitability());
		properties.put(BeCPGModel.PROP_BREAK_EVEN, this.getBreakEven());
		
		return properties;
	}
	
	/**
	 * Sets the properties.
	 *
	 * @param properties the properties
	 */
	public void setProperties(Map<QName, Serializable> properties){
		
		this.setHierarchy1((NodeRef)properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1));
		this.setHierarchy2((NodeRef)properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY2));
		this.setName((String)properties.get(ContentModel.PROP_NAME));
		this.versionLabel = (String)properties.get(ContentModel.PROP_VERSION_LABEL);
		// need to call mlNodeService if we want MLText
		this.setLegalName((String)properties.get(BeCPGModel.PROP_PRODUCT_LEGALNAME));
    	this.setTitle((String)properties.get(ContentModel.PROP_TITLE));
    	SystemState systemState = getSystemState((String)properties.get(BeCPGModel.PROP_PRODUCT_STATE));
    	this.setState(systemState);    	
    	String strProductUnit = (String)properties.get(BeCPGModel.PROP_PRODUCT_UNIT);  
    	this.setUnit(ProductUnit.getUnit(strProductUnit));    	    	
    	this.setQty((Double)properties.get(BeCPGModel.PROP_PRODUCT_QTY));
    	this.setDensity((Double)properties.get(BeCPGModel.PROP_PRODUCT_DENSITY));    	
    	this.setUnitTotalCost((Double)properties.get(BeCPGModel.PROP_UNIT_TOTAL_COST));
    	this.setUnitPrice((Double)properties.get(BeCPGModel.PROP_UNIT_PRICE));
    	this.setProfitability((Double)properties.get(BeCPGModel.PROP_PROFITABILITY));
    	this.setBreakEven((Long)properties.get(BeCPGModel.PROP_BREAK_EVEN));	
	}
	
	/**
	 * Copy data lists.
	 *
	 * @param productData the product data
	 */
	public void copyDataLists(ProductData productData){
		
		if(productData.getAllergenList() != null){
			allergenList = new ArrayList<AllergenListDataItem>(productData.getAllergenList());
			for(AllergenListDataItem a : productData.getAllergenList()){
				allergenList.add(new AllergenListDataItem(a));
			}
		}
		
		if(productData.getCompoList() != null){
			compoList = new ArrayList<CompoListDataItem>(productData.getCompoList());
			for(CompoListDataItem c : productData.getCompoList()){
				compoList.add(new CompoListDataItem(c));
			}
		}
		
		if(productData.getDynamicCharactList() != null){
			dynamicCharactList = new ArrayList<DynamicCharactListItem>(productData.getDynamicCharactList());
			for(DynamicCharactListItem c : productData.getDynamicCharactList()){
				dynamicCharactList.add(new DynamicCharactListItem(c));
			}
		}
		
		
		if(productData.getCostList() != null){
			costList = new ArrayList<CostListDataItem>(productData.getCostList());
			for(CostListDataItem c : productData.getCostList()){
				costList.add(new CostListDataItem(c));
			}
		}
		
		if(productData.getCostDetailsList() != null){
			costDetailsList = new ArrayList<CostDetailsListDataItem>(productData.getCostDetailsList());
			for(CostDetailsListDataItem c : productData.getCostDetailsList()){
				costDetailsList.add(new CostDetailsListDataItem(c));
			}
		}
		
		if(productData.getIngList() != null){
			ingList = new ArrayList<IngListDataItem>(productData.getIngList());
			for(IngListDataItem i : productData.getIngList()){
				ingList.add(new IngListDataItem(i));
			}
		}
		
		if(productData.getNutList() != null){
			nutList = new ArrayList<NutListDataItem>(productData.getNutList());
			for(NutListDataItem n : productData.getNutList()){
				nutList.add(new NutListDataItem(n));
			}	
		}
			
		if(productData.getOrganoList() != null){
			organoList = new ArrayList<OrganoListDataItem>(productData.getOrganoList());
			for(OrganoListDataItem o : productData.getOrganoList()){
				organoList.add(new OrganoListDataItem(o));
			}
		}		
		
		if(productData.getIngLabelingList() != null){
			setIngLabelingList(productData.getIngLabelingList());
			ingLabelingList = new ArrayList<IngLabelingListDataItem>(productData.getIngLabelingList());
			for(IngLabelingListDataItem i  : productData.getIngLabelingList()){
				ingLabelingList.add(new IngLabelingListDataItem(i));
			}
		}
		
		if(productData.getMicrobioList() != null){
			setMicrobioList(productData.getMicrobioList());
			microbioList = new ArrayList<MicrobioListDataItem>(productData.getMicrobioList());
			for(MicrobioListDataItem m : productData.getMicrobioList()){
				microbioList.add(new MicrobioListDataItem(m));
			}
		}
		
		if(productData.getPhysicoChemList() != null){
			setPhysicoChemList(productData.getPhysicoChemList());
			physicoChemList = new ArrayList<PhysicoChemListDataItem>(productData.getPhysicoChemList());
			for(PhysicoChemListDataItem p : productData.getPhysicoChemList()){
				physicoChemList.add(new PhysicoChemListDataItem(p));
			}
		}
		
		if(productData.getProcessList() != null){
			setProcessList(productData.getProcessList());
			processList = new ArrayList<ProcessListDataItem>(productData.getProcessList());
			for(ProcessListDataItem p : productData.getProcessList()){
				processList.add(new ProcessListDataItem(p));
			}
		}
	}	
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ProductElement#accept(fr.becpg.repo.product.ProductVisitor)
	 */
	@Override
	public void accept(ProductVisitor productVisitor) throws FormulateException {
		this.accept(productVisitor);
	}	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allergenList == null) ? 0 : allergenList.hashCode());
		result = prime * result + ((breakEven == null) ? 0 : breakEven.hashCode());
		result = prime * result + ((compoList == null) ? 0 : compoList.hashCode());
		result = prime * result + ((costDetailsList == null) ? 0 : costDetailsList.hashCode());
		result = prime * result + ((costList == null) ? 0 : costList.hashCode());
		result = prime * result + ((density == null) ? 0 : density.hashCode());
		result = prime * result + ((dynamicCharactList == null) ? 0 : dynamicCharactList.hashCode());
		result = prime * result + ((forbiddenIngList == null) ? 0 : forbiddenIngList.hashCode());
		result = prime * result + ((hierarchy1 == null) ? 0 : hierarchy1.hashCode());
		result = prime * result + ((hierarchy2 == null) ? 0 : hierarchy2.hashCode());
		result = prime * result + ((ingLabelingList == null) ? 0 : ingLabelingList.hashCode());
		result = prime * result + ((ingList == null) ? 0 : ingList.hashCode());
		result = prime * result + ((legalName == null) ? 0 : legalName.hashCode());
		result = prime * result + ((listsContainer == null) ? 0 : listsContainer.hashCode());
		result = prime * result + ((microbioList == null) ? 0 : microbioList.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((nutList == null) ? 0 : nutList.hashCode());
		result = prime * result + ((organoList == null) ? 0 : organoList.hashCode());
		result = prime * result + ((packagingList == null) ? 0 : packagingList.hashCode());
		result = prime * result + ((physicoChemList == null) ? 0 : physicoChemList.hashCode());
		result = prime * result + ((priceList == null) ? 0 : priceList.hashCode());
		result = prime * result + ((processList == null) ? 0 : processList.hashCode());
		result = prime * result + ((profitability == null) ? 0 : profitability.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
		result = prime * result + ((reqCtrlList == null) ? 0 : reqCtrlList.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((unitPrice == null) ? 0 : unitPrice.hashCode());
		result = prime * result + ((unitTotalCost == null) ? 0 : unitTotalCost.hashCode());
		result = prime * result + ((versionLabel == null) ? 0 : versionLabel.hashCode());
		result = prime * result + ((yield == null) ? 0 : yield.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductData other = (ProductData) obj;
		if (allergenList == null) {
			if (other.allergenList != null)
				return false;
		} else if (!allergenList.equals(other.allergenList))
			return false;
		if (breakEven == null) {
			if (other.breakEven != null)
				return false;
		} else if (!breakEven.equals(other.breakEven))
			return false;
		if (compoList == null) {
			if (other.compoList != null)
				return false;
		} else if (!compoList.equals(other.compoList))
			return false;
		if (costDetailsList == null) {
			if (other.costDetailsList != null)
				return false;
		} else if (!costDetailsList.equals(other.costDetailsList))
			return false;
		if (costList == null) {
			if (other.costList != null)
				return false;
		} else if (!costList.equals(other.costList))
			return false;
		if (density == null) {
			if (other.density != null)
				return false;
		} else if (!density.equals(other.density))
			return false;
		if (dynamicCharactList == null) {
			if (other.dynamicCharactList != null)
				return false;
		} else if (!dynamicCharactList.equals(other.dynamicCharactList))
			return false;
		if (forbiddenIngList == null) {
			if (other.forbiddenIngList != null)
				return false;
		} else if (!forbiddenIngList.equals(other.forbiddenIngList))
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
		if (ingLabelingList == null) {
			if (other.ingLabelingList != null)
				return false;
		} else if (!ingLabelingList.equals(other.ingLabelingList))
			return false;
		if (ingList == null) {
			if (other.ingList != null)
				return false;
		} else if (!ingList.equals(other.ingList))
			return false;
		if (legalName == null) {
			if (other.legalName != null)
				return false;
		} else if (!legalName.equals(other.legalName))
			return false;
		if (listsContainer == null) {
			if (other.listsContainer != null)
				return false;
		} else if (!listsContainer.equals(other.listsContainer))
			return false;
		if (microbioList == null) {
			if (other.microbioList != null)
				return false;
		} else if (!microbioList.equals(other.microbioList))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (nutList == null) {
			if (other.nutList != null)
				return false;
		} else if (!nutList.equals(other.nutList))
			return false;
		if (organoList == null) {
			if (other.organoList != null)
				return false;
		} else if (!organoList.equals(other.organoList))
			return false;
		if (packagingList == null) {
			if (other.packagingList != null)
				return false;
		} else if (!packagingList.equals(other.packagingList))
			return false;
		if (physicoChemList == null) {
			if (other.physicoChemList != null)
				return false;
		} else if (!physicoChemList.equals(other.physicoChemList))
			return false;
		if (priceList == null) {
			if (other.priceList != null)
				return false;
		} else if (!priceList.equals(other.priceList))
			return false;
		if (processList == null) {
			if (other.processList != null)
				return false;
		} else if (!processList.equals(other.processList))
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
		if (reqCtrlList == null) {
			if (other.reqCtrlList != null)
				return false;
		} else if (!reqCtrlList.equals(other.reqCtrlList))
			return false;
		if (state != other.state)
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
		if (versionLabel == null) {
			if (other.versionLabel != null)
				return false;
		} else if (!versionLabel.equals(other.versionLabel))
			return false;
		if (yield == null) {
			if (other.yield != null)
				return false;
		} else if (!yield.equals(other.yield))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProductData [nodeRef=" + nodeRef + ", versionLabel=" + versionLabel + ", hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", name=" + name
				+ ", legalName=" + legalName + ", title=" + title + ", state=" + state + ", unit=" + unit + ", qty=" + qty + ", density=" + density + ", yield=" + yield
				+ ", unitTotalCost=" + unitTotalCost + ", unitPrice=" + unitPrice + ", profitability=" + profitability + ", breakEven=" + breakEven + ", listsContainer="
				+ listsContainer + ", allergenList=" + allergenList + ", compoList=" + compoList + ", dynamicCharactList=" + dynamicCharactList + ", costList=" + costList
				+ ", costDetailsList=" + costDetailsList + ", priceList=" + priceList + ", ingList=" + ingList + ", nutList=" + nutList + ", organoList=" + organoList
				+ ", ingLabelingList=" + ingLabelingList + ", microbioList=" + microbioList + ", physicoChemList=" + physicoChemList + ", packagingList=" + packagingList
				+ ", forbiddenIngList=" + forbiddenIngList + ", reqCtrlList=" + reqCtrlList + ", processList=" + processList + "]";
	}			
}
