/*
 *
 */
package fr.becpg.repo.product.data.productList;

import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.ControlableListDataItem;

/**
 * <p>OrganoListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:organoList")
public class OrganoListDataItem extends BeCPGDataObject implements ControlableListDataItem {

	private static final long serialVersionUID = 6048458461427271748L;

	private MLText textCriteria;

	private NodeRef organo;

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:organoListValue")
	public MLText getTextCriteria() {
		return textCriteria;
	}

	/**
	 * <p>Setter for the field <code>textCriteria</code>.</p>
	 *
	 * @param textCriteria a {@link java.lang.String} object.
	 */
	public void setTextCriteria(MLText textCriteria) {
		this.textCriteria = textCriteria;
	}

	/**
	 * <p>Getter for the field <code>organo</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:organoListOrgano")
	@InternalField
	@DataListIdentifierAttr
	public NodeRef getOrgano() {
		return organo;
	}

	/**
	 * <p>Setter for the field <code>organo</code>.</p>
	 *
	 * @param organo a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setOrgano(NodeRef organo) {
		this.organo = organo;
	}

	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef organo) {
		setOrgano(organo);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getCharactNodeRef() {
		return getOrgano();
	}

	/** {@inheritDoc} */
	@Override
	public Double getValue() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void setValue(Double value) {
		//Not implemented
	}

	/**
	 * <p>Constructor for OrganoListDataItem.</p>
	 */
	public OrganoListDataItem() {
		super();
	}

	/**
	 * <p>Constructor for OrganoListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param textCriteria a {@link java.lang.String} object.
	 * @param organo a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public OrganoListDataItem(NodeRef nodeRef, MLText textCriteria, NodeRef organo) {
		super();
		setNodeRef(nodeRef);
		setTextCriteria(textCriteria);
		setOrgano(organo);
	}

	/**
	 * <p>Constructor for OrganoListDataItem.</p>
	 *
	 * @param o a {@link fr.becpg.repo.product.data.productList.OrganoListDataItem} object.
	 */
	public OrganoListDataItem(OrganoListDataItem o) {
		super(o);
		this.textCriteria = o.textCriteria;
		this.organo = o.organo;
	}
	
	/** {@inheritDoc} */
	@Override
	public OrganoListDataItem copy() {
		return new OrganoListDataItem(this);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(organo, textCriteria);
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
		OrganoListDataItem other = (OrganoListDataItem) obj;
		return Objects.equals(organo, other.organo) && Objects.equals(textCriteria, other.textCriteria);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "OrganoListDataItem [textCriteria=" + textCriteria + ", organo=" + organo + ", nodeRef=" + nodeRef + ", parentNodeRef=" + parentNodeRef
				+ ", name=" + name + ", aspects=" + aspects + ", aspectsToRemove=" + aspectsToRemove + ", extraProperties=" + extraProperties
				+ ", isTransient=" + isTransient + "]";
	}

}
