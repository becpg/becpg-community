/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.security.data.dataList;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>ACLEntryDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "sec:aclEntry")
public class ACLEntryDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9182353002403432757L;

	private String propName;

	private String aclPermission;

	private List<NodeRef> groupsAssignee;
	
	private Boolean isEnforceACL;
	
	@AlfProp
	@AlfQname(qname = "sec:isEnforceACL")
	public Boolean getIsEnforceACL() {
		return isEnforceACL;
	}
	
	public void setIsEnforceACL(Boolean isEnforceACL) {
		this.isEnforceACL = isEnforceACL;
	}

	/**
	 * <p>Getter for the field <code>propName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "sec:propName")
	public String getPropName() {
		return propName;
	}

	/**
	 * <p>Setter for the field <code>propName</code>.</p>
	 *
	 * @param propName a {@link java.lang.String} object.
	 */
	public void setPropName(String propName) {
		this.propName = propName;
	}

	/**
	 * <p>Getter for the field <code>aclPermission</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "sec:aclPermission")
	public String getAclPermission() {
		return aclPermission;
	}

	/**
	 * <p>Setter for the field <code>aclPermission</code>.</p>
	 *
	 * @param aclPermission a {@link java.lang.String} object.
	 */
	public void setAclPermission(String aclPermission) {
		this.aclPermission = aclPermission;
	}

	/**
	 * <p>Getter for the field <code>groupsAssignee</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "sec:groupsAssignee")
	public List<NodeRef> getGroupsAssignee() {
		return groupsAssignee;
	}

	/**
	 * <p>Setter for the field <code>groupsAssignee</code>.</p>
	 *
	 * @param groupsAssignee a {@link java.util.List} object.
	 */
	public void setGroupsAssignee(List<NodeRef> groupsAssignee) {
		this.groupsAssignee = groupsAssignee;
	}

	/**
	 * <p>Constructor for ACLEntryDataItem.</p>
	 */
	public ACLEntryDataItem() {
		super();
	}

	/**
	 * <p>Constructor for ACLEntryDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public ACLEntryDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	/**
	 * <p>Constructor for ACLEntryDataItem.</p>
	 *
	 * @param propName a {@link java.lang.String} object.
	 * @param aclPermission a {@link java.lang.String} object.
	 * @param groupsAssignee a {@link java.util.List} object.
	 */
	public ACLEntryDataItem(String propName, String aclPermission, List<NodeRef> groupsAssignee) {
		super();
		this.propName = propName;
		this.aclPermission = aclPermission;
		this.groupsAssignee = groupsAssignee;
	}
	
	public ACLEntryDataItem(String propName, String aclPermission, List<NodeRef> groupsAssignee, boolean isEnforceACL) {
		super();
		this.propName = propName;
		this.aclPermission = aclPermission;
		this.groupsAssignee = groupsAssignee;
		this.isEnforceACL = isEnforceACL;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((aclPermission == null) ? 0 : aclPermission.hashCode());
		result = prime * result + ((groupsAssignee == null) ? 0 : groupsAssignee.hashCode());
		result = prime * result + ((propName == null) ? 0 : propName.hashCode());
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
		ACLEntryDataItem other = (ACLEntryDataItem) obj;
		if (aclPermission == null) {
			if (other.aclPermission != null)
				return false;
		} else if (!aclPermission.equals(other.aclPermission))
			return false;
		if (groupsAssignee == null) {
			if (other.groupsAssignee != null)
				return false;
		} else if (!groupsAssignee.equals(other.groupsAssignee))
			return false;
		if (propName == null) {
			if (other.propName != null)
				return false;
		} else if (!propName.equals(other.propName))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ACLEntryDataItem [propName=" + propName + ", aclPermission=" + aclPermission + ", groupsAssignee=" + groupsAssignee + "]";
	}

}
