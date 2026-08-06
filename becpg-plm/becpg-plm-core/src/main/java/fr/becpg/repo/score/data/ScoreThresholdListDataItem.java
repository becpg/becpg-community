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
 * One threshold of a score definition: the verdict a nutrient earns inside a range of
 * values.
 *
 * <p>Keeping the thresholds as data is what lets a nutritionist adjust a regulation
 * without a release, which matters for the front of pack schemes that keep being revised.</p>
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:scoreThresholdList")
public class ScoreThresholdListDataItem extends BeCPGDataObject {

	private static final long serialVersionUID = 1L;

	private String nutCode;

	private String category;

	private Double lowerBound;

	private Double upperBound;

	private String result;

	private Double points;

	private Double referenceIntake;

	/**
	 * <p>Getter for the field <code>nutCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@DataListIdentifierAttr
	@AlfQname(qname = "bcpg:stlNutCode")
	public String getNutCode() {
		return nutCode;
	}

	/**
	 * <p>Setter for the field <code>nutCode</code>.</p>
	 *
	 * @param nutCode a {@link java.lang.String} object
	 */
	public void setNutCode(String nutCode) {
		this.nutCode = nutCode;
	}

	/**
	 * Category the threshold applies to, empty meaning every category.
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlCategory")
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

	/**
	 * Lower bound of the range, excluded. Null means no lower bound.
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlLowerBound")
	public Double getLowerBound() {
		return lowerBound;
	}

	/**
	 * <p>Setter for the field <code>lowerBound</code>.</p>
	 *
	 * @param lowerBound a {@link java.lang.Double} object
	 */
	public void setLowerBound(Double lowerBound) {
		this.lowerBound = lowerBound;
	}

	/**
	 * Upper bound of the range, included. Null means no upper bound.
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlUpperBound")
	public Double getUpperBound() {
		return upperBound;
	}

	/**
	 * <p>Setter for the field <code>upperBound</code>.</p>
	 *
	 * @param upperBound a {@link java.lang.Double} object
	 */
	public void setUpperBound(Double upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * Verdict earned inside the range, such as a traffic light level or a grade letter.
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlResult")
	public String getResult() {
		return result;
	}

	/**
	 * <p>Setter for the field <code>result</code>.</p>
	 *
	 * @param result a {@link java.lang.String} object
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * Points earned inside the range, for the schemes summing points.
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlPoints")
	public Double getPoints() {
		return points;
	}

	/**
	 * <p>Setter for the field <code>points</code>.</p>
	 *
	 * @param points a {@link java.lang.Double} object
	 */
	public void setPoints(Double points) {
		this.points = points;
	}

	/**
	 * Daily reference intake of the nutrient, for the schemes displaying a percentage.
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlReferenceIntake")
	public Double getReferenceIntake() {
		return referenceIntake;
	}

	/**
	 * <p>Setter for the field <code>referenceIntake</code>.</p>
	 *
	 * @param referenceIntake a {@link java.lang.Double} object
	 */
	public void setReferenceIntake(Double referenceIntake) {
		this.referenceIntake = referenceIntake;
	}

	/**
	 * <p>Checks whether a value falls inside the range of this threshold.</p>
	 *
	 * @param value the value of the nutrient
	 * @return a boolean
	 */
	public boolean matches(Double value) {
		if (value == null) {
			return false;
		}
		if ((lowerBound != null) && (value <= lowerBound)) {
			return false;
		}
		return (upperBound == null) || (value <= upperBound);
	}

	/**
	 * <p>Checks whether this threshold applies to a category.</p>
	 *
	 * @param productCategory the category of the product, may be null
	 * @return a boolean
	 */
	public boolean appliesTo(String productCategory) {
		return (category == null) || category.isBlank() || category.equals(productCategory);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), nutCode, category, lowerBound, upperBound);
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
		ScoreThresholdListDataItem other = (ScoreThresholdListDataItem) obj;
		return Objects.equals(nutCode, other.nutCode) && Objects.equals(category, other.category)
				&& Objects.equals(lowerBound, other.lowerBound) && Objects.equals(upperBound, other.upperBound);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ScoreThresholdListDataItem [nutCode=" + nutCode + ", category=" + category + ", lowerBound=" + lowerBound + ", upperBound="
				+ upperBound + ", result=" + result + "]";
	}
}
