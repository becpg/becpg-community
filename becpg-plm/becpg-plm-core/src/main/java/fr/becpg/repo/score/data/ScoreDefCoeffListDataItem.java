/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.data;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Normalization and weighting factors of a score definition, one line per LCA indicator.
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:scoreDefCoeffList")
public class ScoreDefCoeffListDataItem extends BeCPGDataObject {

	private static final long serialVersionUID = 1L;

	private NodeRef lca;

	private Double normalization;

	private Double ponderation;

	/**
	 * <p>Getter for the field <code>lca</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr
	@AlfQname(qname = "bcpg:sdclLca")
	public NodeRef getLca() {
		return lca;
	}

	/**
	 * <p>Setter for the field <code>lca</code>.</p>
	 *
	 * @param lca a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setLca(NodeRef lca) {
		this.lca = lca;
	}

	/**
	 * <p>Getter for the field <code>normalization</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:sdclNormalization")
	public Double getNormalization() {
		return normalization;
	}

	/**
	 * <p>Setter for the field <code>normalization</code>.</p>
	 *
	 * @param normalization a {@link java.lang.Double} object
	 */
	public void setNormalization(Double normalization) {
		this.normalization = normalization;
	}

	/**
	 * <p>Getter for the field <code>ponderation</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:sdclPonderation")
	public Double getPonderation() {
		return ponderation;
	}

	/**
	 * <p>Setter for the field <code>ponderation</code>.</p>
	 *
	 * @param ponderation a {@link java.lang.Double} object
	 */
	public void setPonderation(Double ponderation) {
		this.ponderation = ponderation;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), lca, normalization, ponderation);
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
		ScoreDefCoeffListDataItem other = (ScoreDefCoeffListDataItem) obj;
		return Objects.equals(lca, other.lca) && Objects.equals(normalization, other.normalization)
				&& Objects.equals(ponderation, other.ponderation);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ScoreDefCoeffListDataItem [lca=" + lca + ", normalization=" + normalization + ", ponderation=" + ponderation + "]";
	}
}
