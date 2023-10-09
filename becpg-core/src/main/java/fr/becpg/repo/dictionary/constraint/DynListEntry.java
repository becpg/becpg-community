package fr.becpg.repo.dictionary.constraint;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;

public class DynListEntry {
	
	private String code;
	private MLText values;
	private List<String> groups;
	private Boolean isDeleted;
	
	
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public MLText getValues() {
		return values;
	}

	public void setValues(MLText values) {
		this.values = values;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, groups, isDeleted, values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DynListEntry other = (DynListEntry) obj;
		return Objects.equals(code, other.code) && Objects.equals(groups, other.groups) && Objects.equals(isDeleted, other.isDeleted)
				&& Objects.equals(values, other.values);
	}
	
	
	
		
}