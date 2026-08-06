/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Normalized detail of a computed score.
 *
 * <p>Every score, whatever computes it, publishes its breakdown in this format so that a
 * single renderer, a single BIRT datasource and a single export extractor can serve them
 * all. Score specific contexts such as
 * {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} keep their own
 * computation state and expose a {@code toScoreContext()} converter.</p>
 *
 * @author matthieu
 */
public class ScoreContext {

	/** Constant <code>CODE="code"</code> */
	public static final String CODE = "code";
	/** Constant <code>VERSION="version"</code> */
	public static final String VERSION = "version";
	/** Constant <code>VALUE="value"</code> */
	public static final String VALUE = "value";
	/** Constant <code>UNIT="unit"</code> */
	public static final String UNIT = "unit";
	/** Constant <code>SCORE_CLASS="class"</code> */
	public static final String SCORE_CLASS = "class";
	/** Constant <code>RANGE="range"</code> */
	public static final String RANGE = "range";
	/** Constant <code>SCALE="scale"</code> */
	public static final String SCALE = "scale";
	/** Constant <code>PARTS="parts"</code> */
	public static final String PARTS = "parts";
	/** Constant <code>STEPS="steps"</code> */
	public static final String STEPS = "steps";

	private String code;

	private String version;

	private Double value;

	private String unit;

	private String scoreClass;

	private String range;

	private String scale;

	private final List<ScorePart> parts = new ArrayList<>();

	private final List<ScorePart> steps = new ArrayList<>();

	/**
	 * <p>Getter for the field <code>code</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getCode() {
		return code;
	}

	/**
	 * <p>Setter for the field <code>code</code>.</p>
	 *
	 * @param code a {@link java.lang.String} object
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * <p>Getter for the field <code>version</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
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
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
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
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * <p>Setter for the field <code>unit</code>.</p>
	 *
	 * @param unit a {@link java.lang.String} object
	 */
	public void setUnit(String unit) {
		this.unit = unit;
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
	 * <p>Setter for the field <code>scoreClass</code>.</p>
	 *
	 * @param scoreClass a {@link java.lang.String} object
	 */
	public void setScoreClass(String scoreClass) {
		this.scoreClass = scoreClass;
	}

	/**
	 * <p>Getter for the field <code>range</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getRange() {
		return range;
	}

	/**
	 * <p>Setter for the field <code>range</code>.</p>
	 *
	 * @param range a {@link java.lang.String} object
	 */
	public void setRange(String range) {
		this.range = range;
	}

	/**
	 * <p>Getter for the field <code>scale</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getScale() {
		return scale;
	}

	/**
	 * <p>Setter for the field <code>scale</code>.</p>
	 *
	 * @param scale a {@link java.lang.String} object
	 */
	public void setScale(String scale) {
		this.scale = scale;
	}

	/**
	 * Contributions of the score, one per indicator or per nutrient.
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<ScorePart> getParts() {
		return parts;
	}

	/**
	 * Stages of the computation, such as the bonuses and the maluses applied after the
	 * aggregation of the parts.
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<ScorePart> getSteps() {
		return steps;
	}

	/**
	 * <p>Computes the share of each part in the sum of the contributions.</p>
	 */
	public void computeShares() {
		double total = 0d;
		for (ScorePart part : parts) {
			if (part.getContribution() != null) {
				total += Math.abs(part.getContribution());
			}
		}

		if (total == 0d) {
			return;
		}

		for (ScorePart part : parts) {
			if (part.getContribution() != null) {
				part.setShare((Math.abs(part.getContribution()) * 100d) / total);
			}
		}
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object
	 */
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.putOpt(CODE, code);
		json.putOpt(VERSION, version);
		json.putOpt(VALUE, value);
		json.putOpt(UNIT, unit);
		json.putOpt(SCORE_CLASS, scoreClass);
		json.putOpt(RANGE, range);
		json.putOpt(SCALE, scale);
		json.put(PARTS, toJSONArray(parts));
		json.put(STEPS, toJSONArray(steps));
		return json;
	}

	/**
	 * <p>toJSONArray.</p>
	 *
	 * @param scoreParts a {@link java.util.List} object
	 * @return a {@link org.json.JSONArray} object
	 */
	private JSONArray toJSONArray(List<ScorePart> scoreParts) {
		JSONArray array = new JSONArray();
		for (ScorePart part : scoreParts) {
			array.put(part.toJSON());
		}
		return array;
	}

	/**
	 * <p>parse.</p>
	 *
	 * @param details the serialized context
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	public static ScoreContext parse(String details) {
		JSONObject json = new JSONObject(details);

		ScoreContext context = new ScoreContext();
		context.code = json.optString(CODE, null);
		context.version = json.optString(VERSION, null);
		context.value = json.has(VALUE) && !json.isNull(VALUE) ? json.getDouble(VALUE) : null;
		context.unit = json.optString(UNIT, null);
		context.scoreClass = json.optString(SCORE_CLASS, null);
		context.range = json.optString(RANGE, null);
		context.scale = json.optString(SCALE, null);

		fillParts(json.optJSONArray(PARTS), context.parts);
		fillParts(json.optJSONArray(STEPS), context.steps);

		return context;
	}

	/**
	 * <p>fillParts.</p>
	 *
	 * @param array a {@link org.json.JSONArray} object
	 * @param target a {@link java.util.List} object
	 */
	private static void fillParts(JSONArray array, List<ScorePart> target) {
		if (array == null) {
			return;
		}
		for (int i = 0; i < array.length(); i++) {
			target.add(ScorePart.parse(array.getJSONObject(i)));
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ScoreContext [code=" + code + ", version=" + version + ", value=" + value + ", class=" + scoreClass + ", parts=" + parts.size() + "]";
	}
}
