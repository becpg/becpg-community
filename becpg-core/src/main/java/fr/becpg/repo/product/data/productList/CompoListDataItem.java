/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.Leaf;
import fr.becpg.repo.product.data.BaseObject;

/**
 * The Class CompoListDataItem.
 *
 * @author querephi
 */
public class CompoListDataItem extends BaseObject {

	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The depth level. */
	private Integer depthLevel;
	
	/** The qty. */
	private Double qty = 0d;
		
	private Double qtySubFormula = null;
	
	private Double qtyAfterProcess = null;
	
	/** The compo list unit. */
	private CompoListUnit compoListUnit = CompoListUnit.Unknown;
	
	private Double lossPerc = 0d;
	
	private Double yieldPerc = null;
	
	/** The decl type. */
	private DeclarationType declType = DeclarationType.Declare;			
	
	/** The product. */
	private NodeRef product;
	
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
	 * Gets the depth level.
	 *
	 * @return the depth level
	 */
	public Integer getDepthLevel() {
		return depthLevel;
	}
	
	/**
	 * Sets the depth level.
	 *
	 * @param depthLevel the new depth level
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
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
		
	public Double getQtySubFormula() {
		return qtySubFormula;
	}

	public void setQtySubFormula(Double qtySubFormula) {
		this.qtySubFormula = qtySubFormula;
	}
		
	public Double getQtyAfterProcess() {
		return qtyAfterProcess;
	}

	public void setQtyAfterProcess(Double qtyAfterProcess) {
		this.qtyAfterProcess = qtyAfterProcess;
	}

	/**
	 * Gets the compo list unit.
	 *
	 * @return the compo list unit
	 */
	public CompoListUnit getCompoListUnit() {
		return compoListUnit;
	}
	
	/**
	 * Sets the compo list unit.
	 *
	 * @param compoListUnit the new compo list unit
	 */
	public void setCompoListUnit(CompoListUnit compoListUnit) {
		this.compoListUnit = compoListUnit;
	}
		
	public Double getLossPerc() {
		return lossPerc;
	}

	public void setLossPerc(Double lossPerc) {
		this.lossPerc = lossPerc;
	}

	public Double getYieldPerc() {
		return yieldPerc;
	}

	public void setYieldPerc(Double yieldPerc) {
		this.yieldPerc = yieldPerc;
	}

	/**
	 * Gets the decl type.
	 *
	 * @return the decl type
	 */
	public DeclarationType getDeclType() {
		return declType;
	}
	
	/**
	 * Sets the decl type.
	 *
	 * @param declType the new decl type
	 */
	public void setDeclType(DeclarationType declType) {
		this.declType = declType;
	}
	
	/**
	 * Gets the product.
	 *
	 * @return the product
	 */
	public NodeRef getProduct() {
		return product;
	}
	
	/**
	 * Sets the product.
	 *
	 * @param product the new product
	 */
	public void setProduct(NodeRef product) {
		this.product = product;
	}	

	/**
	 * Instantiates a new compo list data item.
	 */
	public CompoListDataItem() {
		
	}
	
	/**
	 * Instantiates a new compo list data item.
	 * @param nodeRef
	 * @param depthLevel
	 * @param qty
	 * @param qtySubFormula
	 * @param qtyAfterProcess
	 * @param compoListUnit
	 * @param lossPerc
	 * @param yieldPerc
	 * @param declType
	 * @param product
	 */
	public CompoListDataItem(NodeRef nodeRef, Integer depthLevel, Double qty, Double qtySubFormula, Double qtyAfterProcess, CompoListUnit compoListUnit, Double lossPerc, Double yieldPerc, DeclarationType declType, NodeRef product){
		
		setNodeRef(nodeRef);
		setDepthLevel(depthLevel);
		setQty(qty);
		setQtySubFormula(qtySubFormula);
		setQtyAfterProcess(qtyAfterProcess);
		setCompoListUnit(compoListUnit);
		setLossPerc(lossPerc);
		setYieldPerc(yieldPerc);
		setDeclType(declType);
		setProduct(product);
	}
	
	/**
	 * Instantiates a new compo list data item.
	 * @param nodeRef
	 * @param depthLevel
	 * @param qty
	 * @param qtySubFormula
	 * @param compoListUnit
	 * @param qtyLossPerc
	 * @param declType
	 * @param product
	 */
	public CompoListDataItem(NodeRef nodeRef, Integer depthLevel, Double qty, Double qtySubFormula, Double qtyAfterProcess, CompoListUnit compoListUnit, Double lossPerc, DeclarationType declType, NodeRef product){
		
		setNodeRef(nodeRef);
		setDepthLevel(depthLevel);
		setQty(qty);
		setQtySubFormula(qtySubFormula);
		setQtyAfterProcess(qtyAfterProcess);
		setCompoListUnit(compoListUnit);
		setLossPerc(lossPerc);
		setDeclType(declType);
		setProduct(product);
	}
	
	/**
	 * Copy constructor
	 * @param c
	 */
	public CompoListDataItem(CompoListDataItem c){
		setNodeRef(new NodeRef(c.getNodeRef().toString()));
		setDepthLevel(c.getDepthLevel());
		setQty(c.getQty());
		setQtySubFormula(c.getQtySubFormula());
		setQtyAfterProcess(c.getQtyAfterProcess());
		setCompoListUnit(c.getCompoListUnit());
		setLossPerc(c.getLossPerc());
		setDeclType(c.getDeclType());
		setProduct(new NodeRef(c.getProduct().toString()));
	}
	
	public static Composite<CompoListDataItem> getHierarchicalCompoList(List<CompoListDataItem> items){
		
		Composite<CompoListDataItem> composite = new Composite<CompoListDataItem>();
		loadChildren(composite, 1, 0, items);
		return composite;
	}
	
	private static int loadChildren(Composite<CompoListDataItem> composite, int level, int startPos, List<CompoListDataItem> items){
		
		int z_idx = startPos; 
		
		for( ; z_idx<items.size() ; z_idx++){
			
			CompoListDataItem compoListDataItem = items.get(z_idx);
			
			if(compoListDataItem.getDepthLevel() == level){				
				
				// is composite ?
				boolean isComposite = false;
				if((z_idx+1) < items.size()){
				
					CompoListDataItem nextComponent = items.get(z_idx+1);
					if(nextComponent.getDepthLevel() > compoListDataItem.getDepthLevel()){
						isComposite = true;
					}
				}
				
				if(isComposite){
					Composite<CompoListDataItem> c = new Composite<CompoListDataItem>(compoListDataItem);
					composite.addChild(c);
					z_idx = loadChildren(c, level+1, z_idx+1, items);
				}
				else{
					Leaf<CompoListDataItem> leaf = new Leaf<CompoListDataItem>(compoListDataItem);
					composite.addChild(leaf);
				}				
			}
			else if(compoListDataItem.getDepthLevel() < level){
				z_idx--;
				break;				
			}
		}
		
		return z_idx;
	}

	public static DeclarationType parseDeclarationType(String declType) {
		
		return (declType != null && declType != "") ? DeclarationType.valueOf(declType) : DeclarationType.Declare;		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((compoListUnit == null) ? 0 : compoListUnit.hashCode());
		result = prime * result + ((declType == null) ? 0 : declType.hashCode());
		result = prime * result + ((depthLevel == null) ? 0 : depthLevel.hashCode());
		result = prime * result + ((lossPerc == null) ? 0 : lossPerc.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
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
		if (obj == null)
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
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
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
		return "CompoListDataItem [nodeRef=" + nodeRef + ", depthLevel=" + depthLevel + ", qty=" + qty + ", qtySubFormula=" + qtySubFormula + ", qtyAfterProcess="
				+ qtyAfterProcess + ", compoListUnit=" + compoListUnit + ", lossPerc=" + lossPerc + ", yieldPerc=" + yieldPerc + ", declType=" + declType + ", product=" + product
				+ "]";
	}
	
	
}
