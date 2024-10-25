package fr.becpg.repo.search.data;

import java.util.Objects;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * author Matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:savedSearch")
public class SavedSearch extends BeCPGDataObject {

	private static final long serialVersionUID = 4295437589439380453L;

	private String searchType;

	private String siteId;

	private Boolean isGlobal = false;

	@AlfProp
	@AlfQname(qname = "bcpg:savedSearchType")
	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:savedSearchSiteId")
	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:isGlobalSavedSearch")
	public Boolean getIsGlobal() {
		return isGlobal;
	}

	public void setIsGlobal(Boolean isGlobal) {
		this.isGlobal = isGlobal;
	}

	@Override
	public String toString() {
		return "SavedSearch [searchType=" + searchType + ", siteId=" + siteId + ", isGlobal=" + isGlobal + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + Objects.hash(isGlobal, searchType, siteId);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || (getClass() != obj.getClass())) {
			return false;
		}
		SavedSearch other = (SavedSearch) obj;
		return Objects.equals(isGlobal, other.isGlobal) && Objects.equals(searchType, other.searchType) && Objects.equals(siteId, other.siteId);
	}

	public void setGlobal(Boolean isGlobal) {
		this.isGlobal = isGlobal;
	}

}
