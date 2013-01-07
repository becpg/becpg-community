/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;

@AlfType
@AlfQname(qname = "bcpg:ingLabelingList")
public class IngLabelingListDataItem extends AbstractManualDataItem {

	
	private NodeRef grp;
	
	private MLText value;
	

	@AlfSingleAssoc
	@AlfQname(qname="bcpg:illGrp")
	public NodeRef getGrp() {
		return grp;
	}


	public void setGrp(NodeRef grp) {
		this.grp = grp;
	}

	@AlfMlText
	@AlfProp
	@AlfQname(qname="bcpg:illValue")
	public MLText getValue() {
		return value;
	}

	
	public void setValue(MLText value) {
		this.value = value;
	}
	
	
	
	/**
	 * Instantiates a new ing labeling list data item.
	 */
	public IngLabelingListDataItem(){
	
	}
	
	/**
	 * Instantiates a new ing labeling list data item.
	 *
	 * @param nodeRef the node ref
	 * @param grp the grp
	 * @param value the value
	 */
	public IngLabelingListDataItem(NodeRef nodeRef, NodeRef grp, MLText value, Boolean isManual){
		this.nodeRef = nodeRef;;
		this.grp=grp;
		this.value=value;		
		this.isManual=isManual;
	}
	
	/**
	 * Copy constructor
	 * @param i
	 */
	public IngLabelingListDataItem(IngLabelingListDataItem i){
		this.nodeRef = i.nodeRef;;
		this.grp = i.grp;
		this.value = i.value;		
		this.isManual = i.isManual;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((grp == null) ? 0 : grp.hashCode());
		result = prime * result + ((isManual == null) ? 0 : isManual.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		IngLabelingListDataItem other = (IngLabelingListDataItem) obj;
		if (grp == null) {
			if (other.grp != null)
				return false;
		} else if (!grp.equals(other.grp))
			return false;
		if (isManual == null) {
			if (other.isManual != null)
				return false;
		} else if (!isManual.equals(other.isManual))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IngLabelingListDataItem [nodeRef=" + nodeRef + ", grp=" + grp + ", value=" + value + ", isManual=" + isManual + "]";
	}
	
	
}
