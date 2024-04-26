package fr.becpg.repo.security.data;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public class PermissionModel {

	public static final String READ_ONLY = "read";
	public static final String READ_WRITE = "write";
	public static final String READ_READANDWRITE = "readandwrite";

	public PermissionModel(String permission, List<NodeRef> groups, Boolean isEnforceACL) {
		super();
		this.permission = permission;
		this.groups = groups;
		this.isEnforceACL = isEnforceACL;
	}

	private Boolean isEnforceACL;
	
	private String permission;

	private List<NodeRef> groups;

	public Boolean getIsEnforceACL() {
		return isEnforceACL;
	}
	
	public void setIsEnforceACL(Boolean isEnforceACL) {
		this.isEnforceACL = isEnforceACL;
	}
	
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

	public boolean isExclusiveRead() {
		return READ_ONLY.equals(permission)  || READ_READANDWRITE.equals(permission);
	}

	public boolean isWrite() {
		return READ_WRITE.equals(permission) || READ_READANDWRITE.equals(permission);
	}

	@Override
	public String toString() {
		return "PermissionModel [permission=" + permission + ", groups=" + groups + "]";
	}

}
