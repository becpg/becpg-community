/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Computed score of an entity, one line per score definition.
 *
 * @author matthieu
 */
@AlfType
@MultiLevelDataList
@AlfQname(qname = "bcpg:regulatoryScoreList")
public class RegulatoryScoreListDataItem extends BeCPGDataObject {

	private static final long serialVersionUID = 1L;

	private NodeRef scoreDef;

	private Double value;

	private String scoreClass;

	private String details;

	private String version;

	private Date computedDate;

	private Double previousValue;

	private String previousClass;

	private Boolean isManual = false;

	private String errorLog;

	private String category;

	private List<NodeRef> countries = new ArrayList<>();

	private List<NodeRef> usages = new ArrayList<>();

	private Integer depthLevel = 0;

	private NodeRef parentLevel;

	/**
	 * <p>Getter for the field <code>scoreDef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr
	@AlfQname(qname = "bcpg:rslScoreDef")
	public NodeRef getScoreDef() {
		return scoreDef;
	}

	/**
	 * <p>Setter for the field <code>scoreDef</code>.</p>
	 *
	 * @param scoreDef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setScoreDef(NodeRef scoreDef) {
		this.scoreDef = scoreDef;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:rslValue")
	public Double getValue() {
		return value;
	}

	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link java.lang.Double} object
	 */
	public void setValue(Double value) {
		this.value = value;
	}

	/**
	 * <p>Getter for the field <code>scoreClass</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:rslClass")
	public String getScoreClass() {
		return scoreClass;
	}

	/**
	 * <p>Setter for the field <code>scoreClass</code>.</p>
	 *
	 * @param scoreClass a {@link java.lang.String} object
	 */
	public void setScoreClass(String scoreClass) {
		this.scoreClass = scoreClass;
	}

	/**
	 * <p>Getter for the field <code>details</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:rslDetails")
	@InternalField
	public String getDetails() {
		return details;
	}

	/**
	 * <p>Setter for the field <code>details</code>.</p>
	 *
	 * @param details a {@link java.lang.String} object
	 */
	public void setDetails(String details) {
		this.details = details;
	}

	/**
	 * <p>Getter for the field <code>version</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:rslVersion")
	public String getVersion() {
		return version;
	}

	/**
	 * <p>Setter for the field <code>version</code>.</p>
	 *
	 * @param version a {@link java.lang.String} object
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * <p>Getter for the field <code>computedDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:rslComputedDate")
	public Date getComputedDate() {
		return computedDate;
	}

	/**
	 * <p>Setter for the field <code>computedDate</code>.</p>
	 *
	 * @param computedDate a {@link java.util.Date} object
	 */
	public void setComputedDate(Date computedDate) {
		this.computedDate = computedDate;
	}

	/**
	 * <p>Getter for the field <code>previousValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:rslPreviousValue")
	public Double getPreviousValue() {
		return previousValue;
	}

	/**
	 * <p>Setter for the field <code>previousValue</code>.</p>
	 *
	 * @param previousValue a {@link java.lang.Double} object
	 */
	public void setPreviousValue(Double previousValue) {
		this.previousValue = previousValue;
	}

	/**
	 * <p>Getter for the field <code>previousClass</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:rslPreviousClass")
	public String getPreviousClass() {
		return previousClass;
	}

	/**
	 * <p>Setter for the field <code>previousClass</code>.</p>
	 *
	 * @param previousClass a {@link java.lang.String} object
	 */
	public void setPreviousClass(String previousClass) {
		this.previousClass = previousClass;
	}

	/**
	 * <p>Getter for the field <code>isManual</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:rslIsManual")
	public Boolean getIsManual() {
		return isManual;
	}

	/**
	 * <p>Setter for the field <code>isManual</code>.</p>
	 *
	 * @param isManual a {@link java.lang.Boolean} object
	 */
	public void setIsManual(Boolean isManual) {
		this.isManual = isManual;
	}

	/**
	 * <p>Getter for the field <code>errorLog</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:rslErrorLog")
	@InternalField
	public String getErrorLog() {
		return errorLog;
	}

	/**
	 * <p>Setter for the field <code>errorLog</code>.</p>
	 *
	 * @param errorLog a {@link java.lang.String} object
	 */
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}

	/**
	 * Keeps the last computed value as the previous one, so a score change stays visible
	 * after a reformulation.
	 */
	public void keepPreviousValue() {
		this.previousValue = value;
		this.previousClass = scoreClass;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), scoreDef, value, scoreClass);
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
		RegulatoryScoreListDataItem other = (RegulatoryScoreListDataItem) obj;
		return Objects.equals(scoreDef, other.scoreDef) && Objects.equals(value, other.value)
				&& Objects.equals(scoreClass, other.scoreClass);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "RegulatoryScoreListDataItem [scoreDef=" + scoreDef + ", value=" + value + ", scoreClass=" + scoreClass + "]";
	}

	/**
	 * Markets the published score applies to, copied from its definition so the score can be
	 * read by market without walking back to the administration.
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	public List<NodeRef> getCountries() {
		return countries;
	}

	/**
	 * <p>Setter for the field <code>countries</code>.</p>
	 *
	 * @param countries a {@link java.util.List} object
	 */
	public void setCountries(List<NodeRef> countries) {
		this.countries = countries;
	}

	/**
	 * Usages the published score applies to.
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	public List<NodeRef> getUsages() {
		return usages;
	}

	/**
	 * <p>Setter for the field <code>usages</code>.</p>
	 *
	 * @param usages a {@link java.util.List} object
	 */
	public void setUsages(List<NodeRef> usages) {
		this.usages = usages;
	}


	/**
	 * Depth of the score in the tree of scores, zero for a score standing on its own.
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:depthLevel")
	@InternalField
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * <p>Setter for the field <code>depthLevel</code>.</p>
	 *
	 * @param depthLevel a {@link java.lang.Integer} object
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/**
	 * Score this one is a sub score of.
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	@InternalField
	public NodeRef getParentLevel() {
		return parentLevel;
	}

	/**
	 * <p>Setter for the field <code>parentLevel</code>.</p>
	 *
	 * @param parentLevel a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setParentLevel(NodeRef parentLevel) {
		this.parentLevel = parentLevel;
	}


	/**
	 * Category the score was computed for, read on the product through the property its
	 * definition names.
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:rslCategory")
	public String getCategory() {
		return category;
	}

	/**
	 * <p>Setter for the field <code>category</code>.</p>
	 *
	 * @param category a {@link java.lang.String} object
	 */
	public void setCategory(String category) {
		this.category = category;
	}

}
