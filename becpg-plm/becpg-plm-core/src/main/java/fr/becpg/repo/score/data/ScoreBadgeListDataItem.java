/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.data;

import java.util.Objects;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Badge of one class of a score, the image being the content of the node.
 *
 * <p>Holding the badge as content is what makes it importable like any other reference
 * data, and lets a customer brand a score without redeploying Share.</p>
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:scoreBadgeList")
public class ScoreBadgeListDataItem extends BeCPGDataObject {

	private static final long serialVersionUID = 1L;

	private String scoreClass;

	/**
	 * Class the badge illustrates, such as the letter of a nutrition label.
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@DataListIdentifierAttr
	@AlfQname(qname = "bcpg:sblClass")
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

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), scoreClass);
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
		return Objects.equals(scoreClass, ((ScoreBadgeListDataItem) obj).scoreClass);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ScoreBadgeListDataItem [scoreClass=" + scoreClass + "]";
	}
}
