package fr.becpg.repo.security.data.dataList;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.BeCPGDataItem;

/**
 * 
 * @author "Matthieu Laborie <laborima@gmail.com>"
 *
 */
public class ACLEntryDataItem extends BeCPGDataItem {

	private String propName;
	
	private PermissionModel permissionModel;
	
	public class PermissionModel {
		
		public static final String READ_ONLY = "Lecture seule";
	    public static final String READ_WRITE = "Lecture et écriture";
		
		
		
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
		super(nodeRef);
		this.propName = propName;
		this.permissionModel = new PermissionModel(permission,groups);
		
	}

	@Override
	public String toString() {
		return "ACLEntryDataItem [propName=" + propName + ", permissionModel="
				+ permissionModel + "]";
	}


	
	
}

