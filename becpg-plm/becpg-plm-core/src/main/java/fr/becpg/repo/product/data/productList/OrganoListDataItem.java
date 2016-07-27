/*
 *
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.ControlableListDataItem;

@AlfType
@AlfQname(qname = "bcpg:organoList")
public class OrganoListDataItem extends BeCPGDataObject implements ControlableListDataItem {

	/**
	 *
	 */
	private static final long serialVersionUID = 6048458461427271748L;

	private String textCriteria;

	private NodeRef organo;

	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:organoListValue")
	public String getTextCriteria() {
		return textCriteria;
	}

	public void setTextCriteria(String textCriteria) {
		this.textCriteria = textCriteria;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:organoListOrgano")
	@InternalField
	@DataListIdentifierAttr
	public NodeRef getOrgano() {
		return organo;
	}

	public void setOrgano(NodeRef organo) {
		this.organo = organo;
	}

	@Override
	public void setCharactNodeRef(NodeRef organo) {
		this.organo = organo;
	}

	@Override
	public NodeRef getCharactNodeRef() {
		return organo;
	}

	@Override
	public Double getValue() {
		return null;
	}

	@Override
	public void setValue(Double value) {

	}

	/**
	 * Instantiates a new organo list data item.
	 */
	public OrganoListDataItem() {

	}

	/**
	 * Instantiates a new organo list data item.
	 *
	 * @param nodeRef
	 *            the node ref
	 * @param value
	 *            the value
	 * @param organo
	 *            the organo
	 */
	public OrganoListDataItem(NodeRef nodeRef, String textCriteria, NodeRef organo) {
		setNodeRef(nodeRef);
		setTextCriteria(textCriteria);
		setOrgano(organo);
	}

	/**
	 * copy constructor
	 * 
	 * @param o
	 */
	public OrganoListDataItem(OrganoListDataItem o) {
		setNodeRef(nodeRef);
		setTextCriteria(textCriteria);
		setOrgano(organo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = (prime * result) + ((organo == null) ? 0 : organo.hashCode());
		result = (prime * result) + ((textCriteria == null) ? 0 : textCriteria.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OrganoListDataItem other = (OrganoListDataItem) obj;
		if (nodeRef == null) {
			if (other.nodeRef != null) {
				return false;
			}
		} else if (!nodeRef.equals(other.nodeRef)) {
			return false;
		}
		if (organo == null) {
			if (other.organo != null) {
				return false;
			}
		} else if (!organo.equals(other.organo)) {
			return false;
		}
		if (textCriteria == null) {
			if (other.textCriteria != null) {
				return false;
			}
		} else if (!textCriteria.equals(other.textCriteria)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "OrganoListDataItem [nodeRef=" + nodeRef + ", textCriteria=" + textCriteria + ", organo=" + organo + "]";
	}

}
