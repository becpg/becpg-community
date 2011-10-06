/*
 * 
 */
package fr.becpg.repo.product.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.FormulateException;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductData.
 *
 * @author querephi
 */
public class ProductData implements ProductElement {
	
	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The version label. */
	private String versionLabel;
	
	/** The hierarchy1. */
	private String hierarchy1;
	
	/** The hierarchy2. */
	private String hierarchy2;
	
	/** The name. */
	private String name;
	
	/** The legal name. */
	private String legalName;
	
	/** The title. */
	private String title;	
	
	/** The state. */
	private SystemState state = SystemState.ToValidate;
	
	/** The qty. */
	private Float qty;
	
	/** The unit. */
	private ProductUnit unit = ProductUnit.kg;
	
	/** The density. */
	private Float density;
	
	
	/** The lists container. */
	private NodeRef listsContainer;
	
	/** The allergen list. */
	private List<AllergenListDataItem> allergenList;
	
	/** The compo list. */
	private List<CompoListDataItem> compoList;
	
	/** The cost list. */
	private List<CostListDataItem> costList;
	
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
	public String getHierarchy1() {
		return hierarchy1;
	}
	
	/**
	 * Sets the hierarchy1.
	 *
	 * @param hierarchy1 the new hierarchy1
	 */
	public void setHierarchy1(String hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}
	
	/**
	 * Gets the hierarchy2.
	 *
	 * @return the hierarchy2
	 */
	public String getHierarchy2() {
		return hierarchy2;
	}
	
	/**
	 * Sets the hierarchy2.
	 *
	 * @param hierarchy2 the new hierarchy2
	 */
	public void setHierarchy2(String hierarchy2) {
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
	 * Sets the version label.
	 *
	 * @param versionLabel the new version label
	 */
	public void setVersionLabel(String versionLabel) {
		this.versionLabel = versionLabel;
	}
	
	/**
	 * Gets the legal name.
	 *
	 * @return the legal name
	 */
	public String getLegalName() {
		return legalName;
	}
	
	/**
	 * Sets the legal name.
	 *
	 * @param legalName the new legal name
	 */
	public void setLegalName(String legalName) {
		this.legalName = legalName;
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
	 * Gets the qty.
	 *
	 * @return the qty
	 */
	public Float getQty() {
		return qty;
	}
	
	/**
	 * Sets the qty.
	 *
	 * @param qty the new qty
	 */
	public void setQty(Float qty) {
		this.qty = qty;
	}
	
	/**
	 * Gets the density.
	 *
	 * @return the density
	 */
	public Float getDensity() {
		return density;
	}
	
	/**
	 * Sets the density.
	 *
	 * @param density the new density
	 */
	public void setDensity(Float density) {
		this.density = density;
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
		properties.put(BeCPGModel.PROP_VERSION_LABEL, this.getVersionLabel());
		properties.put(ContentModel.PROP_TITLE, this.getTitle());	
		properties.put(BeCPGModel.PROP_PRODUCT_LEGALNAME, this.getLegalName());
		properties.put(BeCPGModel.PROP_PRODUCT_HIERARCHY1, this.getHierarchy1());
		properties.put(BeCPGModel.PROP_PRODUCT_HIERARCHY2, this.getHierarchy2());
		properties.put(BeCPGModel.PROP_PRODUCT_STATE, this.getState().toString());
		properties.put(BeCPGModel.PROP_PRODUCT_QTY, this.getQty());
		properties.put(BeCPGModel.PROP_PRODUCT_UNIT, this.getUnit().toString());
		properties.put(BeCPGModel.PROP_PRODUCT_DENSITY, this.getDensity());
		
		return properties;
	}
	
	/**
	 * Sets the properties.
	 *
	 * @param properties the properties
	 */
	public void setProperties(Map<QName, Serializable> properties){
		
		this.setHierarchy1((String)properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1));
		this.setHierarchy2((String)properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY2));
		this.setName((String)properties.get(ContentModel.PROP_NAME));
		this.setVersionLabel((String)properties.get(BeCPGModel.PROP_VERSION_LABEL));
		this.setLegalName((String)properties.get(BeCPGModel.PROP_PRODUCT_LEGALNAME));
    	this.setTitle((String)properties.get(ContentModel.PROP_TITLE));
    	SystemState systemState = getSystemState((String)properties.get(BeCPGModel.PROP_PRODUCT_STATE));
    	this.setState(systemState);
    	this.setQty((Float)properties.get(BeCPGModel.PROP_PRODUCT_QTY));
    	String strProductUnit = (String)properties.get(BeCPGModel.PROP_PRODUCT_UNIT);  
    	this.setUnit(ProductUnit.getUnit(strProductUnit));
    	this.setDensity((Float)properties.get(BeCPGModel.PROP_PRODUCT_DENSITY));
    	
	}
	
	/**
	 * Copy data lists.
	 *
	 * @param productData the product data
	 */
	public void copyDataLists(ProductData productData){
		
		setAllergenList(productData.getAllergenList());
		setCompoList(productData.getCompoList());
		setCostList(productData.getCostList());
		setIngList(productData.getIngList());
		setNutList(productData.getNutList());
		setOrganoList(productData.getOrganoList());
		setIngLabelingList(productData.getIngLabelingList());
		setMicrobioList(productData.getMicrobioList());
		setPhysicoChemList(productData.getPhysicoChemList());
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ProductElement#accept(fr.becpg.repo.product.ProductVisitor)
	 */
	@Override
	public void accept(ProductVisitor productVisitor) throws FormulateException {
		this.accept(productVisitor);
	}	
}
