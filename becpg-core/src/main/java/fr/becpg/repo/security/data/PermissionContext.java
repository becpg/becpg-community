package fr.becpg.repo.security.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>PermissionContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PermissionContext {

	private Boolean isDefaultReadOnly = false;
	
	private List<PermissionModel> permissions = new ArrayList<>();

	/**
	 * <p>isDefaultReadOnly.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean isDefaultReadOnly() {
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

	/**
	 * <p>Getter for the field <code>permissions</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<PermissionModel> getPermissions() {
		return permissions;
	}

	/**
	 * <p>Setter for the field <code>permissions</code>.</p>
	 *
	 * @param permissions a {@link java.util.List} object
	 */
	public void setPermissions(List<PermissionModel> permissions) {
		this.permissions = permissions;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(isDefaultReadOnly, permissions);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PermissionContext other = (PermissionContext) obj;
		return Objects.equals(isDefaultReadOnly, other.isDefaultReadOnly) && Objects.equals(permissions, other.permissions);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "PermissionContext [isDefaultReadOnly=" + isDefaultReadOnly + ", permissions=" + permissions + "]";
	}

}
