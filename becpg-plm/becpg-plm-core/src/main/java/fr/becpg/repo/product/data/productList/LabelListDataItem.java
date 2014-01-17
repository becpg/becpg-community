package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.annotation.AlfIdentAttr;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "pack:labelingList")
public class LabelListDataItem extends AbstractManualDataItem {

	private NodeRef label;
	private String type;
	private String position;
	
	@AlfSingleAssoc
	@AlfQname(qname="pack:llLabel")
	@AlfIdentAttr
	public NodeRef getLabel() {
		return label;
	}
	public void setLabel(NodeRef label) {
		this.label = label;
	}
	
	@AlfProp
	@AlfQname(qname="pack:llType")
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@AlfProp
	@AlfQname(qname="pack:llPosition")
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	
	public LabelListDataItem(){
		super();
	}
	
	public LabelListDataItem(NodeRef label, String type, String  position) {
		super();
		this.label = label;
		this.type = type;
		this.position = position;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
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
		LabelListDataItem other = (LabelListDataItem) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "LabelListDataItem [label=" + label + ", type=" + type + ", position=" + position + "]";
	}
	
}
