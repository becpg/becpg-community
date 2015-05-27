/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "sec:aclEntry")
public class ACLEntryDataItem extends BeCPGDataObject {

	private String propName;

	private String aclPermission;

	private List<NodeRef> groupsAssignee;

	public class PermissionModel {

		public static final String READ_ONLY = "read";
		public static final String READ_WRITE = "write";

		public PermissionModel(String permission, List<NodeRef> groups) {
			super();
			this.permission = permission;
			this.groups = groups;
		}

		private String permission;

		private List<NodeRef> groups;

		public String getPermission() {
			return permission;
		}

		public void setPermission(String permission) {
			this.permission = permission;
		}

		public List<NodeRef> getGroups() {
			return groups;
		}

		public void setGroups(List<NodeRef> groups) {
			this.groups = groups;
		}

		public boolean isReadOnly() {
			return READ_ONLY.equals(permission);
		}

		public boolean isWrite() {
			return READ_WRITE.equals(permission);
		}

		@Override
		public String toString() {
			return "PermissionModel [permission=" + permission + ", groups=" + groups + "]";
		}

	}

	@AlfProp
	@AlfQname(qname = "sec:propName")
	public String getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	@AlfProp
	@AlfQname(qname = "sec:aclPermission")
	public String getAclPermission() {
		return aclPermission;
	}

	public void setAclPermission(String aclPermission) {
		this.aclPermission = aclPermission;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "sec:groupsAssignee")
	public List<NodeRef> getGroupsAssignee() {
		return groupsAssignee;
	}

	public void setGroupsAssignee(List<NodeRef> groupsAssignee) {
		this.groupsAssignee = groupsAssignee;
	}

	public ACLEntryDataItem() {
		super();
	}

	public ACLEntryDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	public ACLEntryDataItem(String propName, String aclPermission, List<NodeRef> groupsAssignee) {
		super();
		this.propName = propName;
		this.aclPermission = aclPermission;
		this.groupsAssignee = groupsAssignee;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((aclPermission == null) ? 0 : aclPermission.hashCode());
		result = prime * result + ((groupsAssignee == null) ? 0 : groupsAssignee.hashCode());
		result = prime * result + ((propName == null) ? 0 : propName.hashCode());
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

	@Override
	public String toString() {
		return "ACLEntryDataItem [propName=" + propName + ", aclPermission=" + aclPermission + ", groupsAssignee=" + groupsAssignee + "]";
	}

	public PermissionModel getPermissionModel() {
		return new PermissionModel(getAclPermission(), getGroupsAssignee());
	}

}
