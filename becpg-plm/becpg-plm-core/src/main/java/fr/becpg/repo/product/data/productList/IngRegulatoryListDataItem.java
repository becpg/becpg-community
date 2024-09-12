/*
 *
 */
package fr.becpg.repo.product.data.productList;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>IngRegulatoryListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:ingRegulatoryList")
public class IngRegulatoryListDataItem extends BeCPGDataObject {

	private static final long serialVersionUID = 8297326459126736070L;

	private NodeRef ing;

	private MLText citation;

	private MLText restrictionLevels;
	
	private MLText precautions;

	private MLText comment;

	private MLText usages;
	
	private MLText resultIndicator;

	private List<NodeRef> sources;

	private List<NodeRef> regulatoryCountries;

	private List<NodeRef> regulatoryUsages;


	/**
	 * <p>Getter for the field <code>ing</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:irlIng")
	public NodeRef getIng() {
		return ing;
	}

	/**
	 * <p>Setter for the field <code>ing</code>.</p>
	 *
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setIng(NodeRef ing) {
		this.ing = ing;
	}


	/**
	 * <p>Getter for the field <code>citation</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:irlCitation")
	public MLText getCitation() {
		return citation;
	}

	/**
	 * <p>Setter for the field <code>citation</code>.</p>
	 *
	 * @param citation a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public void setCitation(MLText citation) {
		this.citation = citation;
	}

	/**
	 * <p>Getter for the field <code>restrictionLevels</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:irlRestrictionLevels")
	public MLText getRestrictionLevels() {
		return restrictionLevels;
	}

	/**
	 * <p>Setter for the field <code>restrictionLevels</code>.</p>
	 *
	 * @param restrictionLevels a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public void setRestrictionLevels(MLText restrictionLevels) {
		this.restrictionLevels = restrictionLevels;
	}
	
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:irlPrecautions")
	public MLText getPrecautions() {
		return precautions;
	}
	
	public void setPrecautions(MLText precautions) {
		this.precautions = precautions;
	}
	
	
	/**
	 * <p>Getter for the field <code>resultIndicator</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:irlResultIndicator")
	public MLText getResultIndicator() {
		return resultIndicator;
	}

	/**
	 * <p>Setter for the field <code>resultIndicator</code>.</p>
	 *
	 * @param resultIndicator a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public void setResultIndicator(MLText resultIndicator) {
		this.resultIndicator = resultIndicator;
	}

	/**
	 * <p>Getter for the field <code>comment</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:regulatoryComment")
	public MLText getComment() {
		return comment;
	}

	/**
	 * <p>Setter for the field <code>comment</code>.</p>
	 *
	 * @param comment a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public void setComment(MLText comment) {
		this.comment = comment;
	}

	/**
	 * <p>Getter for the field <code>sources</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:irlSources")
	public List<NodeRef> getSources() {
		return sources;
	}

	/**
	 * <p>Setter for the field <code>sources</code>.</p>
	 *
	 * @param sources a {@link java.util.List} object
	 */
	public void setSources(List<NodeRef> sources) {
		this.sources = sources;
	}

	/**
	 * <p>Getter for the field <code>regulatoryCountries</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	public List<NodeRef> getRegulatoryCountries() {
		return regulatoryCountries;
	}

	/**
	 * <p>Setter for the field <code>regulatoryCountries</code>.</p>
	 *
	 * @param regulatoryCountries a {@link java.util.List} object
	 */
	public void setRegulatoryCountries(List<NodeRef> regulatoryCountries) {
		this.regulatoryCountries = regulatoryCountries;
	}

	/**
	 * <p>Getter for the field <code>regulatoryUsages</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	public List<NodeRef> getRegulatoryUsages() {
		return regulatoryUsages;
	}

	/**
	 * <p>Setter for the field <code>regulatoryUsages</code>.</p>
	 *
	 * @param regulatoryUsages a {@link java.util.List} object
	 */
	public void setRegulatoryUsages(List<NodeRef> regulatoryUsages) {
		this.regulatoryUsages = regulatoryUsages;
	}


	/**
	 * <p>Getter for the field <code>usages</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:irlUsages")
	public MLText getUsages() {
		return usages;
	}

	/**
	 * <p>Setter for the field <code>usages</code>.</p>
	 *
	 * @param usages a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public void setUsages(MLText usages) {
		this.usages = usages;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(citation, comment, ing, regulatoryCountries, regulatoryUsages, restrictionLevels, resultIndicator, sources, usages);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IngRegulatoryListDataItem other = (IngRegulatoryListDataItem) obj;
		return Objects.equals(citation, other.citation) && Objects.equals(comment, other.comment) && Objects.equals(ing, other.ing)
				&& Objects.equals(regulatoryCountries, other.regulatoryCountries) && Objects.equals(regulatoryUsages, other.regulatoryUsages)
				&& Objects.equals(restrictionLevels, other.restrictionLevels) && Objects.equals(resultIndicator, other.resultIndicator)
				&& Objects.equals(sources, other.sources) && Objects.equals(usages, other.usages);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "IngRegulatoryListDataItem [ing=" + ing + ", citation=" + citation + ", restrictionLevels=" + restrictionLevels + ", comment="
				+ comment + ", usages=" + usages + ", resultIndicator=" + resultIndicator + ", sources=" + sources + ", regulatoryCountries="
				+ regulatoryCountries + ", regulatoryUsages=" + regulatoryUsages + "]";
	}

    


}
