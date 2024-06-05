package fr.becpg.repo.security.data;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>PermissionModel class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PermissionModel {

	/** Constant <code>READ_ONLY="read"</code> */
	public static final String READ_ONLY = "read";
	/** Constant <code>READ_WRITE="write"</code> */
	public static final String READ_WRITE = "write";
	/** Constant <code>READ_READANDWRITE="readandwrite"</code> */
	public static final String READ_READANDWRITE = "readandwrite";

	/**
	 * <p>Constructor for PermissionModel.</p>
	 *
	 * @param permission a {@link java.lang.String} object
	 * @param groups a {@link java.util.List} object
	 * @param isEnforceACL a {@link java.lang.Boolean} object
	 */
	public PermissionModel(String permission, List<NodeRef> groups, Boolean isEnforceACL) {
		super();
		this.permission = permission;
		this.groups = groups;
		this.isEnforceACL = isEnforceACL;
	}

	private Boolean isEnforceACL;
	
	private String permission;

	private List<NodeRef> groups;

	/**
	 * <p>Getter for the field <code>isEnforceACL</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean getIsEnforceACL() {
		return isEnforceACL;
	}
	
	/**
	 * <p>Setter for the field <code>isEnforceACL</code>.</p>
	 *
	 * @param isEnforceACL a {@link java.lang.Boolean} object
	 */
	public void setIsEnforceACL(Boolean isEnforceACL) {
		this.isEnforceACL = isEnforceACL;
	}
	
	/**
	 * <p>Getter for the field <code>permission</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * <p>Setter for the field <code>permission</code>.</p>
	 *
	 * @param permission a {@link java.lang.String} object
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}

	/**
	 * <p>Getter for the field <code>groups</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<NodeRef> getGroups() {
		return groups;
	}

	/**
	 * <p>Setter for the field <code>groups</code>.</p>
	 *
	 * @param groups a {@link java.util.List} object
	 */
	public void setGroups(List<NodeRef> groups) {
		this.groups = groups;
	}

	/**
	 * <p>isExclusiveRead.</p>
	 *
	 * @return a boolean
	 */
	public boolean isExclusiveRead() {
		return READ_ONLY.equals(permission)  || READ_READANDWRITE.equals(permission);
	}

	/**
	 * <p>isWrite.</p>
	 *
	 * @return a boolean
	 */
	public boolean isWrite() {
		return READ_WRITE.equals(permission) || READ_READANDWRITE.equals(permission);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "PermissionModel [permission=" + permission + ", groups=" + groups + "]";
	}

}
