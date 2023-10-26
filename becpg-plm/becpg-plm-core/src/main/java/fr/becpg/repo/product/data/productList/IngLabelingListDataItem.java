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
import fr.becpg.repo.repository.model.CopiableDataItem;

/**
 * <p>IngLabelingListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:ingLabelingList")
public class IngLabelingListDataItem extends AbstractManualDataItem implements AspectAwareDataItem, CopiableDataItem{

	
	private static final long serialVersionUID = 3043212457177647400L;

	private NodeRef grp;
	private MLText value;
	private MLText manualValue;
	private String logValue;
	private List<String> locales;


	/**
	 * <p>Getter for the field <code>grp</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:illGrp")
	public NodeRef getGrp() {
		return grp;
	}


	/**
	 * <p>Setter for the field <code>grp</code>.</p>
	 *
	 * @param grp a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setGrp(NodeRef grp) {
		this.grp = grp;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfMlText
	@AlfProp
	@AlfQname(qname="bcpg:illValue")
	public MLText getValue() {
		return value;
	}

	
	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setValue(MLText value) {
		this.value = value;
	}
	
	/**
	 * <p>Getter for the field <code>manualValue</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfMlText
	@AlfProp
	@AlfQname(qname="bcpg:illManualValue")
	public MLText getManualValue() {
		return manualValue;
	}

	
	/**
	 * <p>Setter for the field <code>manualValue</code>.</p>
	 *
	 * @param manualValue a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setManualValue(MLText manualValue) {
		this.manualValue = manualValue;
	}
	
	/**
	 * <p>Getter for the field <code>logValue</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:illLogValue")
	public String getLogValue() {
		return logValue;
	}


	/**
	 * <p>Setter for the field <code>logValue</code>.</p>
	 *
	 * @param logValue a {@link java.lang.String} object.
	 */
	public void setLogValue(String logValue) {
		this.logValue = logValue;
	}
	
	
	/**
	 * <p>Getter for the field <code>locales</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lrLocales")
	public List<String> getLocales() {
		return locales;
	}

	/**
	 * <p>Setter for the field <code>locales</code>.</p>
	 *
	 * @param locales a {@link java.util.List} object.
	 */
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
	 * @param isManual a {@link java.lang.Boolean} object.
	 */
	public IngLabelingListDataItem(NodeRef nodeRef, NodeRef grp, MLText value, Boolean isManual){
		this.nodeRef = nodeRef;
		this.grp=grp;
		this.value=value;		
		this.isManual=isManual;
	}
	
	/**
	 * Copy constructor
	 *
	 * @param i a {@link fr.becpg.repo.product.data.productList.IngLabelingListDataItem} object.
	 */
	public IngLabelingListDataItem(IngLabelingListDataItem i){
		super(i);
		this.nodeRef = i.nodeRef;
		this.grp = i.grp;
		this.value = i.value;		
		this.isManual = i.isManual;
		this.locales = i.locales;
		
	}
	
	/** {@inheritDoc} */
	@Override
	public IngLabelingListDataItem copy() {
		IngLabelingListDataItem ret = new IngLabelingListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "IngLabelingListDataItem [nodeRef=" + nodeRef + ", grp=" + grp + ", value=" + value + ", isManual=" + isManual + "]";
	}
	
	
}
