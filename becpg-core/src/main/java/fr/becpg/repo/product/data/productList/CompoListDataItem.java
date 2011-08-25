/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.Leaf;

/**
 * The Class CompoListDataItem.
 *
 * @author querephi
 */
public class CompoListDataItem{

	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The depth level. */
	private Integer depthLevel;
	
	/** The qty. */
	private Float qty = 0f;
		
	private Float qtySubFormula = null;
	
	private Float qtyAfterProcess = null;
	
	/** The compo list unit. */
	private CompoListUnit compoListUnit = CompoListUnit.Unknown;
	
	private Float lossPerc = 0f;
	
	/** The decl grp. */
	private String declGrp;
	
	/** The decl type. */
	private String declType;			
	
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
		
	public Float getQtySubFormula() {
		return qtySubFormula;
	}

	public void setQtySubFormula(Float qtySubFormula) {
		this.qtySubFormula = qtySubFormula;
	}
		
	public Float getQtyAfterProcess() {
		return qtyAfterProcess;
	}

	public void setQtyAfterProcess(Float qtyAfterProcess) {
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
		
	public Float getLossPerc() {
		return lossPerc;
	}

	public void setLossPerc(Float lossPerc) {
		this.lossPerc = lossPerc;
	}

	/**
	 * Gets the decl grp.
	 *
	 * @return the decl grp
	 */
	public String getDeclGrp() {
		return declGrp;
	}
	
	/**
	 * Sets the decl grp.
	 *
	 * @param declGrp the new decl grp
	 */
	public void setDeclGrp(String declGrp) {
		this.declGrp = declGrp;
	}
	
	/**
	 * Gets the decl type.
	 *
	 * @return the decl type
	 */
	public String getDeclType() {
		return declType;
	}
	
	/**
	 * Sets the decl type.
	 *
	 * @param declType the new decl type
	 */
	public void setDeclType(String declType) {
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
	 * @param compoListUnit
	 * @param qtyLossPerc
	 * @param declGrp
	 * @param declType
	 * @param product
	 */
	public CompoListDataItem(NodeRef nodeRef, Integer depthLevel, Float qty, Float qtySubFormula, Float qtyAfterProcess, CompoListUnit compoListUnit, Float lossPerc, String declGrp, String declType, NodeRef product){
		
		setNodeRef(nodeRef);
		setDepthLevel(depthLevel);
		setQty(qty);
		setQtySubFormula(qtySubFormula);
		setQtyAfterProcess(qtyAfterProcess);
		setCompoListUnit(compoListUnit);
		setLossPerc(lossPerc);
		setDeclGrp(declGrp);
		setDeclType(declType);
		setProduct(product);
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
}
