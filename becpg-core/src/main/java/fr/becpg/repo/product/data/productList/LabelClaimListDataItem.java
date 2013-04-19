package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:labelClaimList")
public class LabelClaimListDataItem extends BeCPGDataObject {

	private NodeRef labelClaim;
	private String type;
	private Boolean isClaimed;
	
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:lclLabelClaim")
	public NodeRef getLabelClaim() {
		return labelClaim;
	}
	public void setLabelClaim(NodeRef labelClaim) {
		this.labelClaim = labelClaim;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:lclType")
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:lclIsClaimed")
	public Boolean getIsClaimed() {
		return isClaimed;
	}
	public void setIsClaimed(Boolean isClaimed) {
		this.isClaimed = isClaimed;
	}
	
	public LabelClaimListDataItem(){
		
	}
	
	public LabelClaimListDataItem(NodeRef nodeRef, NodeRef labelClaim, String type, Boolean isClaimed){
		setNodeRef(nodeRef);
		setLabelClaim(labelClaim);
		setType(type);
		setIsClaimed(isClaimed);
	}
	
	public LabelClaimListDataItem(LabelClaimListDataItem l){
		setNodeRef(l.getNodeRef());
		setLabelClaim(l.getLabelClaim());
		setType(l.getType());
		setIsClaimed(l.getIsClaimed());
	}
		
	@Override
	public String toString() {
		return "LabelClaimListDataItem [labelClaim=" + labelClaim + ", type=" + type + ", isClaimed=" + isClaimed
				+ ", nodeRef=" + nodeRef + ", parentNodeRef=" + parentNodeRef + ", name=" + name + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((isClaimed == null) ? 0 : isClaimed.hashCode());
		result = prime * result + ((labelClaim == null) ? 0 : labelClaim.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		LabelClaimListDataItem other = (LabelClaimListDataItem) obj;
		if (isClaimed == null) {
			if (other.isClaimed != null)
				return false;
		} else if (!isClaimed.equals(other.isClaimed))
			return false;
		if (labelClaim == null) {
			if (other.labelClaim != null)
				return false;
		} else if (!labelClaim.equals(other.labelClaim))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
