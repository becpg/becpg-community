/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.data;

import java.util.Date;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Computed score of an entity, one line per score definition.
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:entityScoreList")
public class EntityScoreListDataItem extends BeCPGDataObject {

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

	/**
	 * <p>Getter for the field <code>scoreDef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr
	@AlfQname(qname = "bcpg:eslScoreDef")
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
	@AlfQname(qname = "bcpg:eslValue")
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
	@AlfQname(qname = "bcpg:eslClass")
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
	@AlfQname(qname = "bcpg:eslDetails")
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
	@AlfQname(qname = "bcpg:eslVersion")
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
	@AlfQname(qname = "bcpg:eslComputedDate")
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
	@AlfQname(qname = "bcpg:eslPreviousValue")
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
	@AlfQname(qname = "bcpg:eslPreviousClass")
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
	@AlfQname(qname = "bcpg:eslIsManual")
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
	@AlfQname(qname = "bcpg:eslErrorLog")
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
		EntityScoreListDataItem other = (EntityScoreListDataItem) obj;
		return Objects.equals(scoreDef, other.scoreDef) && Objects.equals(value, other.value)
				&& Objects.equals(scoreClass, other.scoreClass);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "EntityScoreListDataItem [scoreDef=" + scoreDef + ", value=" + value + ", scoreClass=" + scoreClass + "]";
	}
}
