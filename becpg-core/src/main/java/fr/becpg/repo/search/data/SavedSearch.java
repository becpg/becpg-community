package fr.becpg.repo.search.data;

import java.util.Objects;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * author Matthieu
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:savedSearch")
public class SavedSearch extends BeCPGDataObject {

	private static final long serialVersionUID = 4295437589439380453L;

	private String searchType;

	private String siteId;

	private Boolean isGlobal = false;

	/**
	 * <p>Getter for the field <code>searchType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:savedSearchType")
	public String getSearchType() {
		return searchType;
	}

	/**
	 * <p>Setter for the field <code>searchType</code>.</p>
	 *
	 * @param searchType a {@link java.lang.String} object
	 */
	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	/**
	 * <p>Getter for the field <code>siteId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:savedSearchSiteId")
	public String getSiteId() {
		return siteId;
	}

	/**
	 * <p>Setter for the field <code>siteId</code>.</p>
	 *
	 * @param siteId a {@link java.lang.String} object
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/**
	 * <p>Getter for the field <code>isGlobal</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:isGlobalSavedSearch")
	public Boolean getIsGlobal() {
		return isGlobal;
	}

	/**
	 * <p>Setter for the field <code>isGlobal</code>.</p>
	 *
	 * @param isGlobal a {@link java.lang.Boolean} object
	 */
	public void setIsGlobal(Boolean isGlobal) {
		this.isGlobal = isGlobal;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SavedSearch [searchType=" + searchType + ", siteId=" + siteId + ", isGlobal=" + isGlobal + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + Objects.hash(isGlobal, searchType, siteId);
		return result;
	}

	/** {@inheritDoc} */
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

	/**
	 * <p>setGlobal.</p>
	 *
	 * @param isGlobal a {@link java.lang.Boolean} object
	 */
	public void setGlobal(Boolean isGlobal) {
		this.isGlobal = isGlobal;
	}

}
