/*
 *
 */
package fr.becpg.repo.product.data.productList;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.RegulatoryResult;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.CopiableDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

@AlfType
@AlfQname(qname = "bcpg:ingRegulatoryList")
public class IngRegulatoryListDataItem extends BeCPGDataObject {

	private static final long serialVersionUID = 8297326459126736070L;

	private NodeRef ing;

	private MLText citation;

	private MLText restrictionLevels;

	private MLText comment;

	private MLText usages;

	private List<NodeRef> sources;

	private List<NodeRef> regulatoryCountries;

	private List<NodeRef> regulatoryUsages;

	private RegulatoryResult regulatoryResult;

	@AlfProp
	@AlfQname(qname = "bcpg:irlIng")
	public NodeRef getIng() {
		return ing;
	}

	public void setIng(NodeRef ing) {
		this.ing = ing;
	}


	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:irlCitation")
	public MLText getCitation() {
		return citation;
	}

	public void setCitation(MLText citation) {
		this.citation = citation;
	}

	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:irlRestrictionLevels")
	public MLText getRestrictionLevels() {
		return restrictionLevels;
	}

	public void setRestrictionLevels(MLText restrictionLevels) {
		this.restrictionLevels = restrictionLevels;
	}

	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:regulatoryComment")
	public MLText getComment() {
		return comment;
	}

	public void setComment(MLText comment) {
		this.comment = comment;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:irlSources")
	public List<NodeRef> getSources() {
		return sources;
	}

	public void setSources(List<NodeRef> sources) {
		this.sources = sources;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	public List<NodeRef> getRegulatoryCountries() {
		return regulatoryCountries;
	}

	public void setRegulatoryCountries(List<NodeRef> regulatoryCountries) {
		this.regulatoryCountries = regulatoryCountries;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	public List<NodeRef> getRegulatoryUsages() {
		return regulatoryUsages;
	}

	public void setRegulatoryUsages(List<NodeRef> regulatoryUsages) {
		this.regulatoryUsages = regulatoryUsages;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryResult")
	public RegulatoryResult getRegulatoryResult() {
		return regulatoryResult;
	}

	public void setRegulatoryResult(RegulatoryResult regulatoryResult) {
		this.regulatoryResult = regulatoryResult;
	}

	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:irlUsages")
	public MLText getUsages() {
		return usages;
	}

	public void setUsages(MLText usages) {
		this.usages = usages;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(citation, comment, ing, regulatoryCountries, regulatoryResult, regulatoryUsages, restrictionLevels, sources, usages);
		return result;
	}

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
				&& Objects.equals(regulatoryCountries, other.regulatoryCountries) && regulatoryResult == other.regulatoryResult
				&& Objects.equals(regulatoryUsages, other.regulatoryUsages) && Objects.equals(restrictionLevels, other.restrictionLevels)
				&& Objects.equals(sources, other.sources) && Objects.equals(usages, other.usages);
	}

	@Override
	public String toString() {
		return "IngRegulatoryListDataItem [ing=" + ing + ", citation=" + citation + ", restrictionLevels=" + restrictionLevels + ", comment="
				+ comment + ", usages=" + usages + ", sources=" + sources + ", regulatoryCountries=" + regulatoryCountries + ", regulatoryUsages="
				+ regulatoryUsages + ", regulatoryResult=" + regulatoryResult + "]";
	}


}
