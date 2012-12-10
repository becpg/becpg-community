/*
 * 
 */
package fr.becpg.repo.product.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.collections.CollectionUtils;

import fr.becpg.model.SystemState;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
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
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.filters.DataListFilter;
import fr.becpg.repo.repository.model.AbstractEffectiveDataItem;


public class ProductData extends AbstractEffectiveDataItem  {
	

	private String versionLabel;
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private MLText legalName;
	private String title;	
	private SystemState state = SystemState.ToValidate;
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
	
	private List<AllergenListDataItem> allergenList;
	private List<CompoListDataItem> compoList;
	private List<DynamicCharactListItem> dynamicCharactList;
	private List<CostListDataItem> costList;	
	private List<PriceListDataItem> priceList;
	private List<IngListDataItem> ingList;
	private List<NutListDataItem> nutList;
	private List<OrganoListDataItem> organoList;	
	private List<IngLabelingListDataItem> ingLabelingList;
	private List<MicrobioListDataItem> microbioList;
	private List<PhysicoChemListDataItem> physicoChemList;
	private List<PackagingListDataItem> packagingList;
	private List<ForbiddenIngListDataItem> forbiddenIngList;
	private List<ReqCtrlListDataItem> reqCtrlList;
	private List<ProcessListDataItem> processList;

	
	
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

	@AlfProp
	@AlfQname(qname = "bcpg:unitTotalCost")
	public Double getUnitTotalCost() {
		return unitTotalCost;
	}

	public void setUnitTotalCost(Double unitTotalCost) {
		this.unitTotalCost = unitTotalCost;
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
	@AlfQname(qname = "bcpg:breakEven")
	public Long getBreakEven() {
		return breakEven;
	}

	public void setBreakEven(Long breakEven) {
		this.breakEven = breakEven;
	}

	@DataList
	@AlfQname(qname="bcpg:allergenList")
	public List<AllergenListDataItem> getAllergenList() {
		return allergenList;
	}
	
	
	public void setAllergenList(List<AllergenListDataItem> allergenList) {
		this.allergenList = allergenList;
	}
	
	@DataList
	@AlfQname(qname="bcpg:compoList")
	public List<CompoListDataItem> getCompoList() {
		return compoList;
	}

	@DataList
	@AlfQname(qname="bcpg:packagingList")
	public List<PackagingListDataItem> getPackagingList() {
		return packagingList;
	}
	
	@DataList
	@AlfQname(qname="mpm:processList")
	public List<ProcessListDataItem> getProcessList() {
		return processList;
	}
	
	
	public List<CompoListDataItem> getCompoList(DataListFilter<ProductData> filter) {
		if(compoList!=null){
			List<CompoListDataItem> ret = new ArrayList<CompoListDataItem>(compoList);
			CollectionUtils.filter(ret, filter.createPredicate(this));
			return ret;
		}
		return compoList;
	}
	

	public List<PackagingListDataItem> getPackagingList(DataListFilter<ProductData> filter) {
		if(packagingList!=null){
			List<PackagingListDataItem> ret = new ArrayList<PackagingListDataItem>(packagingList);
			CollectionUtils.filter(ret, filter.createPredicate(this));
			return ret;
		}
		return packagingList;
	}
	

	public List<ProcessListDataItem> getProcessList(DataListFilter<ProductData> filter) {
		if(processList!=null){
			List<ProcessListDataItem> ret = new ArrayList<ProcessListDataItem>(processList);
			CollectionUtils.filter(ret, filter.createPredicate(this));
			return ret;
		}
		return processList;
	}
	
	
	public boolean hasCompoListEl(DataListFilter<ProductData> filter) {
		return compoList!=null && !getCompoList(filter).isEmpty();
	}

	public boolean hasPackagingListEl(DataListFilter<ProductData> filter) {
		return packagingList!=null && !getPackagingList(filter).isEmpty();
	}

	public boolean hasProcessListEl(DataListFilter<ProductData> filter) {
		return processList!=null && !getProcessList(filter).isEmpty();
	}			
	
	
	public void setCompoList(List<CompoListDataItem> compoList) {
		this.compoList = compoList;
	}
	
	@DataList
	@AlfQname(qname="bcpg:costList")
	public List<CostListDataItem> getCostList() {
		return costList;
	}
	

	public void setCostList(List<CostListDataItem> costList) {
		this.costList = costList;
	}
	
	@DataList
	@AlfQname(qname="bcpg:priceList")
	public List<PriceListDataItem> getPriceList() {
		return priceList;
	}

	public void setPriceList(List<PriceListDataItem> priceList) {
		this.priceList = priceList;
	}

	@DataList
	@AlfQname(qname="bcpg:ingList")
	public List<IngListDataItem> getIngList() {
		return ingList;
	}
	
	
	public void setIngList(List<IngListDataItem> ingList) {
		this.ingList = ingList;
	}
	
	@DataList
	@AlfQname(qname="bcpg:nutList")
	public List<NutListDataItem> getNutList() {
		return nutList;
	}
	
	public void setNutList(List<NutListDataItem> nutList) {
		this.nutList = nutList;
	}
	
	@DataList
	@AlfQname(qname="bcpg:organoList")
	public List<OrganoListDataItem> getOrganoList() {
		return organoList;
	}
	
	public void setOrganoList(List<OrganoListDataItem> organoList) {
		this.organoList = organoList;
	}
	
	@DataList
	@AlfQname(qname="bcpg:ingLabelingList")
	public List<IngLabelingListDataItem> getIngLabelingList() {
		return ingLabelingList;
	}
	
	
	public void setIngLabelingList(List<IngLabelingListDataItem> ingLabelingList) {
		this.ingLabelingList = ingLabelingList;
	}
	
	@DataList
	@AlfQname(qname="bcpg:microbioList")
	public List<MicrobioListDataItem> getMicrobioList() {
		return microbioList;
	}
	
	
	public void setMicrobioList(List<MicrobioListDataItem> microbioList) {
		this.microbioList = microbioList;
	}
	
	@DataList
	@AlfQname(qname="bcpg:physicoChemList")
	public List<PhysicoChemListDataItem> getPhysicoChemList() {
		return physicoChemList;
	}
	

	public void setPhysicoChemList(List<PhysicoChemListDataItem> physicoChemList) {
		this.physicoChemList = physicoChemList;
	}
	

	public void setPackagingList(List<PackagingListDataItem> packagingList) {
		this.packagingList = packagingList;
	}
	
	@DataList
	@AlfQname(qname="bcpg:forbiddenIngList")
	public List<ForbiddenIngListDataItem> getForbiddenIngList() {
		return forbiddenIngList;
	}

	public void setForbiddenIngList(List<ForbiddenIngListDataItem> forbiddenIngList) {
		this.forbiddenIngList = forbiddenIngList;
	}

	@DataList
	@AlfQname(qname="bcpg:reqCtrlList")
	public List<ReqCtrlListDataItem> getReqCtrlList() {
		return reqCtrlList;
	}

	public void setReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList) {
		this.reqCtrlList = reqCtrlList;
	}


	public void setProcessList(List<ProcessListDataItem> processList) {
		this.processList = processList;
	}

	@DataList
	@AlfQname(qname="bcpg:dynamicCharactList")
	public List<DynamicCharactListItem> getDynamicCharactList() {
		return dynamicCharactList;
	}

	public void setDynamicCharactList(List<DynamicCharactListItem> dynamicCharactList) {
		this.dynamicCharactList = dynamicCharactList;
	}

	/**
	 * Instantiates a new product data.
	 */
	public ProductData(){
		super();
	}

	
	@Override
	public String toString() {
		return "ProductData [versionLabel=" + versionLabel + ", hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", legalName=" + legalName + ", title=" + title
				+ ", state=" + state + ", unit=" + unit + ", qty=" + qty + ", density=" + density + ", yield=" + yield + ", unitTotalCost=" + unitTotalCost + ", unitPrice="
				+ unitPrice + ", profitability=" + profitability + ", breakEven=" + breakEven + ", allergenList=" + allergenList + ", compoList=" + compoList
				+ ", dynamicCharactList=" + dynamicCharactList + ", costList=" + costList + ", priceList=" + priceList + ", ingList=" + ingList + ", nutList=" + nutList
				+ ", organoList=" + organoList + ", ingLabelingList=" + ingLabelingList + ", microbioList=" + microbioList + ", physicoChemList=" + physicoChemList
				+ ", packagingList=" + packagingList + ", forbiddenIngList=" + forbiddenIngList + ", reqCtrlList=" + reqCtrlList + ", processList=" + processList + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((allergenList == null) ? 0 : allergenList.hashCode());
		result = prime * result + ((breakEven == null) ? 0 : breakEven.hashCode());
		result = prime * result + ((compoList == null) ? 0 : compoList.hashCode());
		result = prime * result + ((costList == null) ? 0 : costList.hashCode());
		result = prime * result + ((density == null) ? 0 : density.hashCode());
		result = prime * result + ((dynamicCharactList == null) ? 0 : dynamicCharactList.hashCode());
		result = prime * result + ((forbiddenIngList == null) ? 0 : forbiddenIngList.hashCode());
		result = prime * result + ((hierarchy1 == null) ? 0 : hierarchy1.hashCode());
		result = prime * result + ((hierarchy2 == null) ? 0 : hierarchy2.hashCode());
		result = prime * result + ((ingLabelingList == null) ? 0 : ingLabelingList.hashCode());
		result = prime * result + ((ingList == null) ? 0 : ingList.hashCode());
		result = prime * result + ((legalName == null) ? 0 : legalName.hashCode());
		result = prime * result + ((microbioList == null) ? 0 : microbioList.hashCode());
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
		if (!super.equals(obj))
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
		if (microbioList == null) {
			if (other.microbioList != null)
				return false;
		} else if (!microbioList.equals(other.microbioList))
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
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
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
	
	



}
