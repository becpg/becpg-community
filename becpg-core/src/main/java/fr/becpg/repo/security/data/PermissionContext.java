package fr.becpg.repo.security.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PermissionContext {

	private Boolean isDefaultReadOnly = false;
	
	private List<PermissionModel> permissions = new ArrayList<>();

	public Boolean isDefaultReadOnly() {
		return isDefaultReadOnly;
	}

	public void setIsDefaultReadOnly(Boolean isDefaultReadOnly) {
		this.isDefaultReadOnly = isDefaultReadOnly;
	}

	public List<PermissionModel> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<PermissionModel> permissions) {
		this.permissions = permissions;
	}

	@Override
	public int hashCode() {
		return Objects.hash(isDefaultReadOnly, permissions);
	}

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

	@Override
	public String toString() {
		return "PermissionContext [isDefaultReadOnly=" + isDefaultReadOnly + ", permissions=" + permissions + "]";
	}

}
