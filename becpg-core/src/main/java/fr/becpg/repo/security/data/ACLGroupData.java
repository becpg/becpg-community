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
package fr.becpg.repo.security.data;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;

/**
 * <p>ACLGroupData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "sec:aclGroup")
public class ACLGroupData extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1271936196192020455L;

	private String nodeType;
	
	private Boolean isLocalPermission;
	
	private Boolean isDefaultReadOnly;
	
	List<ACLEntryDataItem> acls = new ArrayList<>();

	/**
	 * <p>Getter for the field <code>acls</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "sec:aclEntry")
	public List<ACLEntryDataItem> getAcls() {
		return acls;
	}

	/**
	 * <p>Setter for the field <code>acls</code>.</p>
	 *
	 * @param acls a {@link java.util.List} object.
	 */
	public void setAcls(List<ACLEntryDataItem> acls) {
		this.acls = acls;
	}

	/**
	 * <p>Getter for the field <code>nodeType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "sec:nodeType")
	public String getNodeType() {
		return nodeType;
	}

	/**
	 * <p>Setter for the field <code>nodeType</code>.</p>
	 *
	 * @param nodeType a {@link java.lang.String} object.
	 */
	public void setNodeType(String nodeType) {
		this.nodeType =  nodeType;
	}
	
	/**
	 * <p>Getter for the field <code>isLocalPermission</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "sec:isLocalPermission")
	public Boolean getIsLocalPermission() {
		return isLocalPermission;
	}

	/**
	 * <p>Setter for the field <code>isLocalPermission</code>.</p>
	 *
	 * @param isLocalPermission a {@link java.lang.Boolean} object
	 */
	public void setIsLocalPermission(Boolean isLocalPermission) {
		this.isLocalPermission = isLocalPermission;
	}
	
	/**
	 * <p>Getter for the field <code>isDefaultReadOnly</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "sec:isDefaultReadOnly")
	public Boolean getIsDefaultReadOnly() {
		return isDefaultReadOnly;
	}

	/**
	 * <p>Setter for the field <code>isDefaultReadOnly</code>.</p>
	 *
	 * @param isDefaultReadOnly a {@link java.lang.Boolean} object
	 */
	public void setIsDefaultReadOnly(Boolean isDefaultReadOnly) {
		this.isDefaultReadOnly = isDefaultReadOnly;
	}

	
	
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((acls == null) ? 0 : acls.hashCode());
		result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
		result = prime * result + ((isLocalPermission == null) ? 0 : isLocalPermission.hashCode());
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
		ACLGroupData other = (ACLGroupData) obj;
		if (acls == null) {
			if (other.acls != null)
				return false;
		} else if (!acls.equals(other.acls))
			return false;
		if (nodeType == null) {
			if (other.nodeType != null)
				return false;
		} else if (!nodeType.equals(other.nodeType))
			return false;
		if (isLocalPermission == null) {
			if (other.isLocalPermission != null)
				return false;
		} else if (!isLocalPermission.equals(other.isLocalPermission))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ACLGroupData [nodeType=" + nodeType + ", acls=" + acls + ", isLocalPermission=" + isLocalPermission + "]";
	}
	
	
}
