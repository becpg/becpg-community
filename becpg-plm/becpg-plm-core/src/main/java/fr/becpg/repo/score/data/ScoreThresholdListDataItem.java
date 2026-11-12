/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.data;

import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
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

	private NodeRef scoreDef;

	private String nutCode;

	private String category;

	private Double lowerBound;

	private Double upperBound;

	private String result;

	private Double points;

	private Double referenceIntake;

	private String ratioNutCode;

	private Double ratioFactor;

	private Boolean lowerBoundIncluded;

	private String conditionNutCode;

	private Double conditionLowerBound;

	private Double conditionUpperBound;

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
	 * Lower bound of the range, excluded unless {@link #getLowerBoundIncluded()} says
	 * otherwise. Null means no lower bound.
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
	 * Characteristic the value is divided by before being compared, such as the energy of
	 * the product. Empty means the value is compared as it stands.
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlRatioNutCode")
	public String getRatioNutCode() {
		return ratioNutCode;
	}

	/**
	 * <p>Setter for the field <code>ratioNutCode</code>.</p>
	 *
	 * @param ratioNutCode a {@link java.lang.String} object
	 */
	public void setRatioNutCode(String ratioNutCode) {
		this.ratioNutCode = ratioNutCode;
	}

	/**
	 * Multiplier applied to the value before the division, typically the energy density of
	 * the nutrient when a regulation reasons in share of the total energy.
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlRatioFactor")
	public Double getRatioFactor() {
		return ratioFactor;
	}

	/**
	 * <p>Setter for the field <code>ratioFactor</code>.</p>
	 *
	 * @param ratioFactor a {@link java.lang.Double} object
	 */
	public void setRatioFactor(Double ratioFactor) {
		this.ratioFactor = ratioFactor;
	}

	/**
	 * Whether a value equal to the lower bound falls inside the range, for the regulations
	 * written with "greater than or equal to". Null or false keeps the bound excluded.
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlLowerBoundIncluded")
	public Boolean getLowerBoundIncluded() {
		return lowerBoundIncluded;
	}

	/**
	 * <p>Setter for the field <code>lowerBoundIncluded</code>.</p>
	 *
	 * @param lowerBoundIncluded a {@link java.lang.Boolean} object
	 */
	public void setLowerBoundIncluded(Boolean lowerBoundIncluded) {
		this.lowerBoundIncluded = lowerBoundIncluded;
	}

	/**
	 * Characteristic the threshold depends on, such as the energy of a beverage that only
	 * counts as a no calorie beverage below a few kilocalories. Empty means the threshold
	 * always applies.
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlConditionNutCode")
	public String getConditionNutCode() {
		return conditionNutCode;
	}

	/**
	 * <p>Setter for the field <code>conditionNutCode</code>.</p>
	 *
	 * @param conditionNutCode a {@link java.lang.String} object
	 */
	public void setConditionNutCode(String conditionNutCode) {
		this.conditionNutCode = conditionNutCode;
	}

	/**
	 * Lower bound of the condition, excluded. Null means no lower bound.
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlConditionLowerBound")
	public Double getConditionLowerBound() {
		return conditionLowerBound;
	}

	/**
	 * <p>Setter for the field <code>conditionLowerBound</code>.</p>
	 *
	 * @param conditionLowerBound a {@link java.lang.Double} object
	 */
	public void setConditionLowerBound(Double conditionLowerBound) {
		this.conditionLowerBound = conditionLowerBound;
	}

	/**
	 * Upper bound of the condition, included. Null means no upper bound.
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:stlConditionUpperBound")
	public Double getConditionUpperBound() {
		return conditionUpperBound;
	}

	/**
	 * <p>Setter for the field <code>conditionUpperBound</code>.</p>
	 *
	 * @param conditionUpperBound a {@link java.lang.Double} object
	 */
	public void setConditionUpperBound(Double conditionUpperBound) {
		this.conditionUpperBound = conditionUpperBound;
	}

	/**
	 * Value actually compared to the bounds: the raw value, or its share of another
	 * characteristic when the regulation reasons in ratio.
	 *
	 * @param value the value of the nutrient
	 * @param values every documented characteristic of the product
	 * @return a {@link java.lang.Double} object
	 */
	public Double comparedValue(Double value, Map<String, Double> values) {
		if ((value == null) || (ratioNutCode == null) || ratioNutCode.isBlank()) {
			return value;
		}

		Double divisor = values.get(ratioNutCode);
		if ((divisor == null) || (divisor == 0d)) {
			return null;
		}

		return (value * (ratioFactor != null ? ratioFactor : 1d)) / divisor;
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
		if ((lowerBound != null) && isBelowLowerBound(value)) {
			return false;
		}
		return (upperBound == null) || (value <= upperBound);
	}

	/**
	 * <p>isBelowLowerBound.</p>
	 *
	 * @param value the value of the nutrient
	 * @return a boolean
	 */
	private boolean isBelowLowerBound(double value) {
		return Boolean.TRUE.equals(lowerBoundIncluded) ? value < lowerBound : value <= lowerBound;
	}

	/**
	 * <p>Checks whether the condition of this threshold holds for a product.</p>
	 *
	 * <p>An undocumented condition characteristic does not hold: a beverage whose energy is
	 * unknown cannot be judged a no calorie beverage.</p>
	 *
	 * @param values every documented characteristic of the product
	 * @return a boolean, true when the threshold has no condition
	 */
	public boolean conditionHolds(Map<String, Double> values) {
		if ((conditionNutCode == null) || conditionNutCode.isBlank()) {
			return true;
		}

		Double value = values.get(conditionNutCode);
		if (value == null) {
			return false;
		}

		return ((conditionLowerBound == null) || (value > conditionLowerBound))
				&& ((conditionUpperBound == null) || (value <= conditionUpperBound));
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
		return Objects.hash(super.hashCode(), nutCode, category, lowerBound, upperBound, lowerBoundIncluded, conditionNutCode);
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
				&& Objects.equals(lowerBound, other.lowerBound) && Objects.equals(upperBound, other.upperBound)
				&& Objects.equals(lowerBoundIncluded, other.lowerBoundIncluded) && Objects.equals(conditionNutCode, other.conditionNutCode);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ScoreThresholdListDataItem [nutCode=" + nutCode + ", category=" + category + ", lowerBound=" + lowerBound + ", upperBound="
				+ upperBound + ", result=" + result + "]";
	}

	/**
	 * Definition the line belongs to.
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:stlScoreDef")
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

}
