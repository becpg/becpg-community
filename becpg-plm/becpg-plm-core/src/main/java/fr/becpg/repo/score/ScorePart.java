/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import org.json.JSONObject;

/**
 * One contributing line of a score, rendered as a row of the score detail panel.
 *
 * <p>A part is a pure data holder: it carries what the renderer needs to explain
 * where a score comes from, without knowing how the score was computed.</p>
 *
 * @author matthieu
 */
public class ScorePart {

	/** Constant <code>CODE="code"</code> */
	public static final String CODE = "code";
	/** Constant <code>LABEL="label"</code> */
	public static final String LABEL = "label";

	/** Constant <code>SCORE_CLASS="class"</code> */
	public static final String SCORE_CLASS = "class";
	/** Constant <code>VALUE="value"</code> */
	public static final String VALUE = "value";
	/** Constant <code>UNIT="unit"</code> */
	public static final String UNIT = "unit";
	/** Constant <code>NORMALIZATION="normalization"</code> */
	public static final String NORMALIZATION = "normalization";
	/** Constant <code>WEIGHT="weight"</code> */
	public static final String WEIGHT = "weight";
	/** Constant <code>CONTRIBUTION="contribution"</code> */
	public static final String CONTRIBUTION = "contribution";
	/** Constant <code>SHARE="share"</code> */
	public static final String SHARE = "share";

	private final String code;

	private String label;

	private Double value;

	private String unit;

	private Double normalization;

	private Double weight;

	private Double contribution;

	private Double share;

	private String scoreClass;

	/**
	 * <p>Constructor for ScorePart.</p>
	 *
	 * @param code the stable identifier of the part, used as the i18n key
	 */
	public ScorePart(String code) {
		this.code = code;
	}

	/**
	 * <p>Getter for the field <code>code</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getCode() {
		return code;
	}

	/**
	 * <p>Getter for the field <code>label</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * <p>Getter for the field <code>normalization</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getNormalization() {
		return normalization;
	}

	/**
	 * <p>Getter for the field <code>weight</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getWeight() {
		return weight;
	}

	/**
	 * <p>Getter for the field <code>contribution</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getContribution() {
		return contribution;
	}

	/**
	 * <p>Getter for the field <code>share</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getShare() {
		return share;
	}

	/**
	 * <p>Sets the share of this part in the total score.</p>
	 *
	 * @param share the share in percent
	 */
	public void setShare(Double share) {
		this.share = share;
	}

	/**
	 * <p>withLabel.</p>
	 *
	 * @param label the human readable name of the part
	 * @return this part
	 */
	public ScorePart withLabel(String label) {
		this.label = label;
		return this;
	}

	/**
	 * <p>withValue.</p>
	 *
	 * @param value the raw value of the part
	 * @param unit the unit of the raw value
	 * @return this part
	 */
	public ScorePart withValue(Double value, String unit) {
		this.value = value;
		this.unit = unit;
		return this;
	}

	/**
	 * <p>withCoefficients.</p>
	 *
	 * @param normalization the normalization factor applied to the raw value
	 * @param weight the weighting factor applied to the normalized value
	 * @return this part
	 */
	public ScorePart withCoefficients(Double normalization, Double weight) {
		this.normalization = normalization;
		this.weight = weight;
		return this;
	}

	/**
	 * <p>withContribution.</p>
	 *
	 * @param contribution the amount this part adds to the total score
	 * @return this part
	 */
	/**
	 * <p>Class of one part, for the marks whose axes are graded on their own scale.</p>
	 *
	 * <p>It is kept apart from the label so the breakdown can name the axis and grade it at
	 * the same time, rather than showing a column of bare letters.</p>
	 *
	 * @param scoreClass the class of the part
	 * @return a {@link fr.becpg.repo.score.ScorePart} object
	 */
	public ScorePart withScoreClass(String scoreClass) {
		this.scoreClass = scoreClass;
		return this;
	}

	/**
	 * <p>Getter for the field <code>scoreClass</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getScoreClass() {
		return scoreClass;
	}

	/**
	 * <p>Sets the share this part takes in the overall score.</p>
	 *
	 * @param contribution a {@link java.lang.Double} object
	 * @return this part
	 */
	public ScorePart withContribution(Double contribution) {
		this.contribution = contribution;
		return this;
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object
	 */
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put(CODE, code);
		json.putOpt(LABEL, label);
		json.putOpt(VALUE, value);
		json.putOpt(UNIT, unit);
		json.putOpt(NORMALIZATION, normalization);
		json.putOpt(WEIGHT, weight);
		json.putOpt(CONTRIBUTION, contribution);
		json.putOpt(SHARE, share);
		json.putOpt(SCORE_CLASS, scoreClass);
		return json;
	}

	/**
	 * <p>parse.</p>
	 *
	 * @param json a {@link org.json.JSONObject} object
	 * @return a {@link fr.becpg.repo.score.ScorePart} object
	 */
	public static ScorePart parse(JSONObject json) {
		ScorePart part = new ScorePart(json.getString(CODE));
		part.label = json.optString(LABEL, null);
		part.value = optDouble(json, VALUE);
		part.unit = json.optString(UNIT, null);
		part.normalization = optDouble(json, NORMALIZATION);
		part.weight = optDouble(json, WEIGHT);
		part.contribution = optDouble(json, CONTRIBUTION);
		part.share = optDouble(json, SHARE);
		part.scoreClass = json.optString(SCORE_CLASS, null);
		return part;
	}

	/**
	 * <p>optDouble.</p>
	 *
	 * @param json a {@link org.json.JSONObject} object
	 * @param key a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	private static Double optDouble(JSONObject json, String key) {
		return json.has(key) && !json.isNull(key) ? json.getDouble(key) : null;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ScorePart [code=" + code + ", value=" + value + ", contribution=" + contribution + "]";
	}
}
