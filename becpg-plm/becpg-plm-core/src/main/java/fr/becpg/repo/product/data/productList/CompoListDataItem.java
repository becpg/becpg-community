/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.model.CompositionDataItem;

@AlfType
@AlfQname(qname = "bcpg:compoList")
@MultiLevelDataList
public class CompoListDataItem extends AbstractEffectiveVariantListDataItem  implements CompositeDataItem<CompoListDataItem>, CompositionDataItem {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6389166205836523748L;

	private Integer depthLevel;
	
	private Double qty = 0d;
		
	private Double qtySubFormula = null;	
	
	private ProductUnit compoListUnit;
	
	private Double lossPerc = 0d;
	
	private Double yieldPerc = null;
	
	private DeclarationType declType = DeclarationType.Declare;
	
	private Double overrunPerc = null;
	
	private Double volume;
	
	private NodeRef product;
	
	private CompoListDataItem parent;
	
	
	@AlfProp
	@AlfQname(qname="bcpg:parentLevel")
	@InternalField
	public CompoListDataItem getParent() {
		return parent;
	}

	public void setParent(CompoListDataItem parent) {
		this.parent = parent;
	}

	@AlfProp
	@AlfQname(qname="bcpg:depthLevel")
	@InternalField
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
	@AlfQname(qname="bcpg:compoListUnit")
	public ProductUnit getCompoListUnit() {
		return compoListUnit;
	}
	
	
	public void setCompoListUnit(ProductUnit compoListUnit) {
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
	
	@AlfProp
	@AlfQname(qname="bcpg:compoListOverrunPerc")
	public Double getOverrunPerc() {
		return overrunPerc;
	}

	public void setOverrunPerc(Double overrunPerc) {
		this.overrunPerc = overrunPerc;
	}

	@AlfProp
	@AlfQname(qname="bcpg:compoListVolume")
	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	@AlfSingleAssoc
	@DataListIdentifierAttr
	@AlfQname(qname="bcpg:compoListProduct")
	@InternalField
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
	
	public CompoListDataItem(NodeRef nodeRef, CompoListDataItem parent, Double qty, Double qtySubFormula, ProductUnit compoListUnit, Double lossPerc, DeclarationType declType, NodeRef product){
		super();
		this.nodeRef=nodeRef;
		this.parent=parent;
		this.qty=qty;
		this.qtySubFormula=qtySubFormula;
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
		this.nodeRef = c.nodeRef;
		this.depthLevel = c.depthLevel;
		this.qty = c.qty;
		this.qtySubFormula = c.qtySubFormula;
		this.compoListUnit = c.compoListUnit;
		this.lossPerc = c.lossPerc;
		this.yieldPerc = c.yieldPerc;
		this.declType = c.declType;
		this.overrunPerc = c.overrunPerc;
		this.volume = c.volume;
		this.product = c.product;
		this.parent = c.parent;
	}
	
	
	


	public static DeclarationType parseDeclarationType(String declType) {
		
		return (declType != null && !Objects.equals(declType, "")) ? DeclarationType.valueOf(declType) : DeclarationType.Declare;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(compoListUnit, declType, depthLevel, lossPerc, overrunPerc, parent, product, qty, qtySubFormula, volume, yieldPerc);
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
		return compoListUnit == other.compoListUnit && declType == other.declType && Objects.equals(depthLevel, other.depthLevel)
				&& Objects.equals(lossPerc, other.lossPerc) && Objects.equals(overrunPerc, other.overrunPerc) && Objects.equals(parent, other.parent)
				&& Objects.equals(product, other.product) && Objects.equals(qty, other.qty) && Objects.equals(qtySubFormula, other.qtySubFormula)
				&& Objects.equals(volume, other.volume) && Objects.equals(yieldPerc, other.yieldPerc);
	}

	@Override
	public String toString() {
		return "CompoListDataItem [depthLevel=" + depthLevel + ", qty=" + qty + ", qtySubFormula=" + qtySubFormula + ", ProductUnit="
				+ compoListUnit + ", lossPerc=" + lossPerc + ", yieldPerc=" + yieldPerc + ", declType=" + declType + ", overrunPerc=" + overrunPerc
				+ ", volume=" + volume + ", product=" + product + ", parent=" + parent + ", startEffectivity=" + startEffectivity
				+ ", endEffectivity=" + endEffectivity + ", nodeRef=" + nodeRef + ", aspects=" + aspects + ", extraProperties=" + extraProperties
				+ "]";
	}

	@Override
	public CompoListDataItem clone() {
		return new CompoListDataItem(this);
	}

	@Override
	@InternalField
	public NodeRef getComponent() {
		return getProduct();
	}

	@Override
	public void setComponent(NodeRef targetItem) {
		setProduct(targetItem);		
	}

	@Override
	public QName getComponentAssocName() {
		return PLMModel.ASSOC_COMPOLIST_PRODUCT;
	}
	
}
