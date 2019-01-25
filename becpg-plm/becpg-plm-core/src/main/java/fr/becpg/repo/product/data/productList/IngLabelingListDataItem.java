/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.AspectAwareDataItem;

@AlfType
@AlfQname(qname = "bcpg:ingLabelingList")
public class IngLabelingListDataItem extends AbstractManualDataItem implements AspectAwareDataItem {

	
	private static final long serialVersionUID = 3043212457177647400L;

	private NodeRef grp;
	private MLText value;
	private MLText manualValue;
	private String logValue;
	private List<String> locales;


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
	
	@AlfMlText
	@AlfProp
	@AlfQname(qname="bcpg:illManualValue")
	public MLText getManualValue() {
		return manualValue;
	}

	
	public void setManualValue(MLText manualValue) {
		this.manualValue = manualValue;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:illLogValue")
	public String getLogValue() {
		return logValue;
	}


	public void setLogValue(String logValue) {
		this.logValue = logValue;
	}
	
	
	@AlfProp
	@AlfQname(qname="bcpg:lrLocales")
	public List<String> getLocales() {
		return locales;
	}

	public void setLocales(List<String> locales) {
		this.locales = locales;
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
		this.nodeRef = nodeRef;
		this.grp=grp;
		this.value=value;		
		this.isManual=isManual;
	}
	
	/**
	 * Copy constructor
	 * @param i
	 */
	public IngLabelingListDataItem(IngLabelingListDataItem i){
		super(i);
		this.nodeRef = i.nodeRef;
		this.grp = i.grp;
		this.value = i.value;		
		this.isManual = i.isManual;
		this.locales = i.locales;
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((grp == null) ? 0 : grp.hashCode());
		result = prime * result + ((locales == null) ? 0 : locales.hashCode());
		result = prime * result + ((logValue == null) ? 0 : logValue.hashCode());
		result = prime * result + ((manualValue == null) ? 0 : manualValue.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		IngLabelingListDataItem other = (IngLabelingListDataItem) obj;
		if (grp == null) {
			if (other.grp != null)
				return false;
		} else if (!grp.equals(other.grp))
			return false;
		if (locales == null) {
			if (other.locales != null)
				return false;
		} else if (!locales.equals(other.locales))
			return false;
		if (logValue == null) {
			if (other.logValue != null)
				return false;
		} else if (!logValue.equals(other.logValue))
			return false;
		if (manualValue == null) {
			if (other.manualValue != null)
				return false;
		} else if (!manualValue.equals(other.manualValue))
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
