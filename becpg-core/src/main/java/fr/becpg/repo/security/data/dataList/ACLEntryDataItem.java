package fr.becpg.repo.security.data.dataList;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * 
 * @author "Matthieu Laborie <laborima@gmail.com>"
 *
 */
public class ACLEntryDataItem extends BeCPGDataObject {

	private String propName;
	
	private PermissionModel permissionModel;
	
	public class PermissionModel {
		
		public static final String READ_ONLY = "read";
	    public static final String READ_WRITE = "write";
		
		
		
		public PermissionModel(String permission, List<String> groups) {
			super();
			this.permission = permission;
			this.groups = groups;
		}

		private String permission;
		
		private List<String> groups;

		public String getPermission() {
			return permission;
		}

		public void setPermission(String permission) {
			this.permission = permission;
		}

		public List<String> getGroups() {
			return groups;
		}

		public void setGroups(List<String> groups) {
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
			return "PermissionModel [permission=" + permission + ", groups="
					+ groups + "]";
		}

		
		

	}

	public String getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public PermissionModel getPermissionModel() {
		return permissionModel;
	}

	public void setPermissionModel(PermissionModel permissionModel) {
		this.permissionModel = permissionModel;
	}

	public ACLEntryDataItem(NodeRef nodeRef, String propName, String permission, List<String> groups) {
		setNodeRef(nodeRef);
		this.propName = propName;
		this.permissionModel = new PermissionModel(permission,groups);
		
	}

	@Override
	public String toString() {
		return "ACLEntryDataItem [propName=" + propName + ", permissionModel="
				+ permissionModel + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((permissionModel == null) ? 0 : permissionModel.hashCode());
		result = prime * result + ((propName == null) ? 0 : propName.hashCode());
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
		ACLEntryDataItem other = (ACLEntryDataItem) obj;
		if (permissionModel == null) {
			if (other.permissionModel != null)
				return false;
		} else if (!permissionModel.equals(other.permissionModel))
			return false;
		if (propName == null) {
			if (other.propName != null)
				return false;
		} else if (!propName.equals(other.propName))
			return false;
		return true;
	}



	
	
}

