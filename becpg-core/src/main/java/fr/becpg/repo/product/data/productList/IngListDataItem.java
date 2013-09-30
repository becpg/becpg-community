
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfEnforced;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

@AlfType
@AlfQname(qname = "bcpg:ingList")
public class IngListDataItem extends AbstractManualDataItem  implements SimpleCharactDataItem, AspectAwareDataItem {

	
	private Double qtyPerc = 0d;
	
	private List<NodeRef> geoOrigin = new ArrayList<NodeRef>();
	
	private List<NodeRef> bioOrigin = new ArrayList<NodeRef>();
	
	private List<NodeRef> ingListSubIng = new ArrayList<NodeRef>();
	
	private Boolean isGMO = false;
	
	private Boolean isIonized = false;	
	
	private NodeRef ing;
	
	private Boolean isManual;

	private Boolean isProcessingAid = false;
	
	
	@AlfProp
	@AlfQname(qname="bcpg:ingListQtyPerc")
	public Double getQtyPerc() {
		return qtyPerc;
	}
	
	
	public void setQtyPerc(Double qtyPerc) {
		this.qtyPerc = qtyPerc;
	}
	
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:ingListGeoOrigin")
	public List<NodeRef> getGeoOrigin() {
		return geoOrigin;
	}
	

	public void setGeoOrigin(List<NodeRef> geoOrigin) {
		this.geoOrigin = geoOrigin;
	}
	
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:ingListBioOrigin")
	public List<NodeRef> getBioOrigin() {
		return bioOrigin;
	}
	

	public void setBioOrigin(List<NodeRef> bioOrigin) {
		this.bioOrigin = bioOrigin;
	}
	
	
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:ingListSubIng")
	public List<NodeRef> getIngListSubIng() {
		return ingListSubIng;
	}


	public void setIngListSubIng(List<NodeRef> ingListSubIng) {
		this.ingListSubIng = ingListSubIng;
	}


	@AlfProp
	@AlfEnforced
	@AlfQname(qname="bcpg:ingListIsGMO")
	public Boolean getIsGMO() {
		return isGMO;
	}
	

	public void setIsGMO(Boolean isGMO) {
		this.isGMO = isGMO;
	}
	
	
	
	@AlfProp
	@AlfEnforced
	@AlfQname(qname="bcpg:ingListIsProcessingAid")
	public Boolean getIsProcessingAid() {
		return isProcessingAid;
	}


	public void setIsProcessingAid(Boolean isProcessingAid) {
		this.isProcessingAid = isProcessingAid;
	}


	@AlfProp
	@AlfEnforced
	@AlfQname(qname="bcpg:ingListIsIonized")
	public Boolean getIsIonized() {
		return isIonized;
	}
	

	public void setIsIonized(Boolean isIonized) {
		this.isIonized = isIonized;
	}
	
	
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:ingListIng")
	public NodeRef getIng() {
		return ing;
	}
	

	public void setIng(NodeRef ing) {
		this.ing = ing;
	}
	
	//////////////////////////////
	
	@Override
	public NodeRef getCharactNodeRef() {
		return ing;
	}

	@Override
	public Double getValue() {
		return qtyPerc;
	}

	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setIng(nodeRef);
		
	}


	@Override
	public void setValue(Double value) {
		setQtyPerc(value);
		
	}
	
	
	/**
	 * Instantiates a new ing list data item.
	 */
	public IngListDataItem()
	{
		super();
	}
	

	public IngListDataItem(NodeRef nodeRef,	Double qtyPerc, List<NodeRef> geoOrigin, List<NodeRef> bioOrigin, Boolean isGMO, Boolean isIonized, Boolean processingAid, NodeRef ing, Boolean isManual)
	{
		setNodeRef(nodeRef);
		setQtyPerc(qtyPerc);
		setGeoOrigin(geoOrigin);
		setBioOrigin(bioOrigin);
		setIsGMO(isGMO);
		setIsIonized(isIonized);
		setIng(ing);
		setIsManual(isManual);
		setIsProcessingAid(processingAid);
	}
	
	/**
	 * Copy contructor
	 * @param i
	 */
	public IngListDataItem(IngListDataItem i){
		
		setNodeRef(i.getNodeRef());
		setQtyPerc(i.getQtyPerc());
		setGeoOrigin(i.getGeoOrigin());
		setBioOrigin(i.getBioOrigin());
		setIsGMO(i.getIsGMO());
		setIsIonized(i.getIsIonized());
		setIng(i.getIng());
		setIsManual(i.getIsManual());
		setIsProcessingAid(i.getIsProcessingAid());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((bioOrigin == null) ? 0 : bioOrigin.hashCode());
		result = prime * result + ((geoOrigin == null) ? 0 : geoOrigin.hashCode());
		result = prime * result + ((ing == null) ? 0 : ing.hashCode());
		result = prime * result + ((ingListSubIng == null) ? 0 : ingListSubIng.hashCode());
		result = prime * result + ((isGMO == null) ? 0 : isGMO.hashCode());
		result = prime * result + ((isIonized == null) ? 0 : isIonized.hashCode());
		result = prime * result + ((isManual == null) ? 0 : isManual.hashCode());
		result = prime * result + ((isProcessingAid == null) ? 0 : isProcessingAid.hashCode());
		result = prime * result + ((qtyPerc == null) ? 0 : qtyPerc.hashCode());
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
		IngListDataItem other = (IngListDataItem) obj;
		if (bioOrigin == null) {
			if (other.bioOrigin != null)
				return false;
		} else if (!bioOrigin.equals(other.bioOrigin))
			return false;
		if (geoOrigin == null) {
			if (other.geoOrigin != null)
				return false;
		} else if (!geoOrigin.equals(other.geoOrigin))
			return false;
		if (ing == null) {
			if (other.ing != null)
				return false;
		} else if (!ing.equals(other.ing))
			return false;
		if (ingListSubIng == null) {
			if (other.ingListSubIng != null)
				return false;
		} else if (!ingListSubIng.equals(other.ingListSubIng))
			return false;
		if (isGMO == null) {
			if (other.isGMO != null)
				return false;
		} else if (!isGMO.equals(other.isGMO))
			return false;
		if (isIonized == null) {
			if (other.isIonized != null)
				return false;
		} else if (!isIonized.equals(other.isIonized))
			return false;
		if (isManual == null) {
			if (other.isManual != null)
				return false;
		} else if (!isManual.equals(other.isManual))
			return false;
		if (isProcessingAid == null) {
			if (other.isProcessingAid != null)
				return false;
		} else if (!isProcessingAid.equals(other.isProcessingAid))
			return false;
		if (qtyPerc == null) {
			if (other.qtyPerc != null)
				return false;
		} else if (!qtyPerc.equals(other.qtyPerc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IngListDataItem [qtyPerc=" + qtyPerc + ", geoOrigin=" + geoOrigin + ", bioOrigin=" + bioOrigin + ", ingListSubIng=" + ingListSubIng + ", isGMO=" + isGMO
				+ ", isIonized=" + isIonized + ", ing=" + ing + ", isManual=" + isManual + ", isProcessingAid=" + isProcessingAid + "]";
	}


}
