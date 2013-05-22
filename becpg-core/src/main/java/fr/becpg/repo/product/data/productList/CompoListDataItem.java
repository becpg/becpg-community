/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "bcpg:compoList")
public class CompoListDataItem extends AbstractEffectiveVariantListDataItem  implements CompositeDataItem<CompoListDataItem>, CompositionDataItem {

	
	private Integer depthLevel;
	
	private Double qty = 0d;
		
	private Double qtySubFormula = null;
	
	private Double qtyAfterProcess = null;
	
	private CompoListUnit compoListUnit = CompoListUnit.Unknown;
	
	private Double lossPerc = 0d;
	
	private Double yieldPerc = null;
	
	private DeclarationType declType = DeclarationType.Declare;			
	
	private NodeRef product;
	
	private CompoListDataItem parent;
	
	
	@AlfProp
	@AlfQname(qname="bcpg:parentLevel")
	public CompoListDataItem getParent() {
		return parent;
	}

	public void setParent(CompoListDataItem parent) {
		this.parent = parent;
	}

	@AlfProp
	@AlfQname(qname="bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}
	
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	@AlfProp
	@AlfQname(qname="bcpg:compoListQty")
	public Double getQty() {
		return qty;
	}
	
	public void setQty(Double qty) {
		this.qty = qty;
	}
	@AlfProp
	@AlfQname(qname="bcpg:compoListQtySubFormula")	
	public Double getQtySubFormula() {
		return qtySubFormula;
	}

	public void setQtySubFormula(Double qtySubFormula) {
		this.qtySubFormula = qtySubFormula;
	}
	@AlfProp
	@AlfQname(qname="bcpg:compoListQtyAfterProcess")	
	public Double getQtyAfterProcess() {
		return qtyAfterProcess;
	}

	public void setQtyAfterProcess(Double qtyAfterProcess) {
		this.qtyAfterProcess = qtyAfterProcess;
	}

	@AlfProp
	@AlfQname(qname="bcpg:compoListUnit")
	public CompoListUnit getCompoListUnit() {
		return compoListUnit;
	}
	
	
	public void setCompoListUnit(CompoListUnit compoListUnit) {
		if(compoListUnit==null){
			compoListUnit  = CompoListUnit.Unknown;
		}
		this.compoListUnit = compoListUnit;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:compoListLossPerc")
	public Double getLossPerc() {
		return lossPerc;
	}

	public void setLossPerc(Double lossPerc) {
		this.lossPerc = lossPerc;
	}

	@AlfProp
	@AlfQname(qname="bcpg:compoListYieldPerc")
	public Double getYieldPerc() {
		return yieldPerc;
	}

	public void setYieldPerc(Double yieldPerc) {
		this.yieldPerc = yieldPerc;
	}

	@AlfProp
	@AlfQname(qname="bcpg:compoListDeclType")
	public DeclarationType getDeclType() {
		return declType;
	}
	

	public void setDeclType(DeclarationType declType) {
		if(declType==null){
			declType=DeclarationType.Declare;
		}
		
		this.declType = declType;
	}
	
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:compoListProduct")
	public NodeRef getProduct() {
		return product;
	}
	
	public void setProduct(NodeRef product) {
		this.product = product;
	}	

	/**
	 * Instantiates a new compo list data item.
	 */
	public CompoListDataItem() {
		super();
	}
	
	public CompoListDataItem(NodeRef nodeRef, CompoListDataItem parent, Double qty, Double qtySubFormula, Double qtyAfterProcess, CompoListUnit compoListUnit, Double lossPerc, DeclarationType declType, NodeRef product){
		super();
		this.nodeRef=nodeRef;
		this.parent=parent;
		this.qty=qty;
		this.qtySubFormula=qtySubFormula;
		this.qtyAfterProcess=qtyAfterProcess;
		this.compoListUnit=compoListUnit;
		this.lossPerc=lossPerc;
		this.declType=declType;
		this.product=product;
		if(parent == null){
			depthLevel = 1;
		} else {
			depthLevel = parent.getDepthLevel() + 1;
		}
	}

	
	/**
	 * Copy constructor
	 * @param c
	 */
	public CompoListDataItem(CompoListDataItem c){
		super();
		this.nodeRef=c.nodeRef;
		this.depthLevel=c.depthLevel;
		this.qty=c.qty;
		this.qtySubFormula=c.qtySubFormula;
		this.qtyAfterProcess=c.qtyAfterProcess;
		this.compoListUnit=c.compoListUnit;
		this.lossPerc=c.lossPerc;
		this.declType=c.declType;
		this.product=c.product;
		this.parent = c.parent;
	}
	

	public static DeclarationType parseDeclarationType(String declType) {
		
		return (declType != null && declType != "") ? DeclarationType.valueOf(declType) : DeclarationType.Declare;		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((compoListUnit == null) ? 0 : compoListUnit.hashCode());
		result = prime * result + ((declType == null) ? 0 : declType.hashCode());
		result = prime * result + ((depthLevel == null) ? 0 : depthLevel.hashCode());
		result = prime * result + ((lossPerc == null) ? 0 : lossPerc.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
		result = prime * result + ((qtyAfterProcess == null) ? 0 : qtyAfterProcess.hashCode());
		result = prime * result + ((qtySubFormula == null) ? 0 : qtySubFormula.hashCode());
		result = prime * result + ((yieldPerc == null) ? 0 : yieldPerc.hashCode());
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
		CompoListDataItem other = (CompoListDataItem) obj;
		if (compoListUnit != other.compoListUnit)
			return false;
		if (declType != other.declType)
			return false;
		if (depthLevel == null) {
			if (other.depthLevel != null)
				return false;
		} else if (!depthLevel.equals(other.depthLevel))
			return false;
		if (lossPerc == null) {
			if (other.lossPerc != null)
				return false;
		} else if (!lossPerc.equals(other.lossPerc))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (product == null) {
			if (other.product != null)
				return false;
		} else if (!product.equals(other.product))
			return false;
		if (qty == null) {
			if (other.qty != null)
				return false;
		} else if (!qty.equals(other.qty))
			return false;
		if (qtyAfterProcess == null) {
			if (other.qtyAfterProcess != null)
				return false;
		} else if (!qtyAfterProcess.equals(other.qtyAfterProcess))
			return false;
		if (qtySubFormula == null) {
			if (other.qtySubFormula != null)
				return false;
		} else if (!qtySubFormula.equals(other.qtySubFormula))
			return false;
		if (yieldPerc == null) {
			if (other.yieldPerc != null)
				return false;
		} else if (!yieldPerc.equals(other.yieldPerc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CompoListDataItem [depthLevel=" + depthLevel + ", qty=" + qty + ", qtySubFormula=" + qtySubFormula + ", qtyAfterProcess=" + qtyAfterProcess + ", compoListUnit="
				+ compoListUnit + ", lossPerc=" + lossPerc + ", yieldPerc=" + yieldPerc + ", declType=" + declType + ", product=" + product + ", parent=" + parent + "]";
	}

	@Override
	public CompositionDataItem createCopy() {
		return new CompoListDataItem(this);
	}


	
	
	
}
