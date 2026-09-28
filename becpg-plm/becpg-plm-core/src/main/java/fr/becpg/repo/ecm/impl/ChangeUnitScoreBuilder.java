/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.ecm.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;

/**
 * Builds the scores and requirements stored on a change unit, so that the change order shows the
 * completion and alerts of the <b>simulated</b> product, and what the change adds or resolves compared
 * with the product before the change order.
 *
 * <p>The snapshot of the product is taken before formulation: the formulation rebuilds the requirement
 * list of the product and may reuse its items, so the previous requirements are copied at once.</p>
 *
 * <p>Requirements are sorted by severity, specification and regulatory non-conformities first, so the
 * blocking alerts come before the incomplete fields.</p>
 */
public final class ChangeUnitScoreBuilder {

	private static final Log logger = LogFactory.getLog(ChangeUnitScoreBuilder.class);

	static final int MAX_REQUIREMENTS = 50;

	/**
	 * Size budgets of the two lists: the whole JSON is stored in one d:text property, whose database
	 * column holds 64 KB, and every message comes in all the supported locales.
	 */
	static final int MAX_REQUIREMENTS_BYTES = 30000;
	static final int MAX_RESOLVED_BYTES = 15000;

	static final String DEFAULT_LOCALE_KEY = "";

	static final String PROP_PREVIOUS = "previous";
	static final String PROP_REQUIREMENTS = "requirements";
	static final String PROP_REQUIREMENTS_COUNT = "requirementsCount";
	static final String PROP_RESOLVED = "resolved";
	static final String PROP_RESOLVED_COUNT = "resolvedCount";
	static final String PROP_REQ_TYPE = "reqType";
	static final String PROP_REQ_DATA_TYPE = "reqDataType";
	static final String PROP_MESSAGE = "message";
	static final String PROP_IS_NEW = "isNew";

	private static final String[] SCORE_PROPS = { "global", "details", "ctrlCount", "totalForbidden", "regulatoryCodeLabels" };
	private static final String[] PREVIOUS_SCORE_PROPS = { "global", "details", "totalForbidden" };

	private static final Comparator<Requirement> BY_SEVERITY = bySeverity(Requirement::reqType, Requirement::reqDataType);

	/**
	 * Immutable copy of a requirement of the product.
	 *
	 * @param key the identity of the requirement, as computed by {@link RequirementListDataItem#getKey()}
	 * @param reqType the level of the requirement
	 * @param reqDataType the kind of the requirement
	 * @param message the localized message of the requirement
	 */
	record Requirement(String key, RequirementType reqType, RequirementDataType reqDataType, MLText message) {

		static Requirement of(RequirementListDataItem item) {
			MLText message = new MLText();
			if (item.getReqMlMessage() != null) {
				message.putAll(item.getReqMlMessage());
			}
			return new Requirement(item.getKey(), item.getReqType(), item.getReqDataType(), message);
		}
	}

	private final String previousScore;

	private final List<Requirement> previousRequirements;

	private ChangeUnitScoreBuilder(String previousScore, List<Requirement> previousRequirements) {
		this.previousScore = previousScore;
		this.previousRequirements = previousRequirements;
	}

	/**
	 * Takes the snapshot of a product before the change order is applied to it.
	 *
	 * @param product the product loaded from the repository, not formulated yet
	 * @return a builder holding the previous scores and requirements of the product
	 */
	public static ChangeUnitScoreBuilder before(ProductData product) {
		return new ChangeUnitScoreBuilder(product.getEntityScore(), toRequirements(product.getReqCtrlList()));
	}

	/**
	 * Sorts requirements by severity, specification and regulatory ones first within a level.
	 *
	 * @param requirements the requirements of the formulated product
	 * @return a new sorted list, the given list is left untouched
	 */
	public static List<RequirementListDataItem> sortBySeverity(List<RequirementListDataItem> requirements) {
		List<RequirementListDataItem> sorted = new ArrayList<>(requirements);
		sorted.sort(bySeverity(RequirementListDataItem::getReqType, RequirementListDataItem::getReqDataType));
		return sorted;
	}

	private static <T> Comparator<T> bySeverity(Function<T, RequirementType> reqType, Function<T, RequirementDataType> reqDataType) {
		return Comparator.comparing(reqType, Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(item -> !RequirementDataType.Specification.equals(reqDataType.apply(item)));
	}

	/**
	 * Builds the JSON of the simulated product: its scores, the scores before the change order, its
	 * requirements flagged as new or not, and the requirements the change order resolves.
	 *
	 * @param simulatedProduct the product formulated with the change order applied
	 * @return the JSON to store in {@code ecm:culEntityScore}
	 */
	public String buildFor(ProductData simulatedProduct) {
		List<Requirement> requirements = toRequirements(simulatedProduct.getReqCtrlList());
		JSONObject result = copyScores(simulatedProduct.getEntityScore(), SCORE_PROPS);

		JSONObject previous = copyScores(previousScore, PREVIOUS_SCORE_PROPS);
		if (!previous.isEmpty()) {
			result.put(PROP_PREVIOUS, previous);
		}

		putRequirements(result, requirements);
		putResolvedRequirements(result, requirements);
		return result.toString();
	}

	private void putRequirements(JSONObject result, List<Requirement> requirements) {
		Set<String> previousKeys = keysOf(previousRequirements);
		List<Requirement> sorted = new ArrayList<>(requirements);
		sorted.sort(BY_SEVERITY.thenComparing(requirement -> previousKeys.contains(requirement.key())));

		result.put(PROP_REQUIREMENTS, toJsonArray(sorted, MAX_REQUIREMENTS_BYTES,
				requirement -> toJson(requirement).put(PROP_IS_NEW, !previousKeys.contains(requirement.key()))));
		result.put(PROP_REQUIREMENTS_COUNT, sorted.size());
	}

	private void putResolvedRequirements(JSONObject result, List<Requirement> requirements) {
		Set<String> currentKeys = keysOf(requirements);
		List<Requirement> resolved = new ArrayList<>();
		for (Requirement previousRequirement : previousRequirements) {
			if (!currentKeys.contains(previousRequirement.key())) {
				resolved.add(previousRequirement);
			}
		}
		resolved.sort(BY_SEVERITY);

		result.put(PROP_RESOLVED, toJsonArray(resolved, MAX_RESOLVED_BYTES, ChangeUnitScoreBuilder::toJson));
		result.put(PROP_RESOLVED_COUNT, resolved.size());
	}

	private static JSONObject toJson(Requirement requirement) {
		JSONObject json = new JSONObject();
		if (requirement.reqType() != null) {
			json.put(PROP_REQ_TYPE, requirement.reqType().toString());
		}
		if (requirement.reqDataType() != null) {
			json.put(PROP_REQ_DATA_TYPE, requirement.reqDataType().toString());
		}
		json.put(PROP_MESSAGE, toCompactMessage(requirement.message()));
		return json;
	}

	/**
	 * Keeps the message once under the default key, then only the locales whose text differs:
	 * most locales share the same fallback text, and the change unit must fit in one property.
	 */
	private static JSONObject toCompactMessage(MLText mlText) {
		JSONObject message = new JSONObject();
		String defaultMessage = MLTextHelper.getClosestValue(mlText, Locale.getDefault());
		if (defaultMessage != null) {
			message.put(DEFAULT_LOCALE_KEY, defaultMessage);
		}
		for (Map.Entry<Locale, String> entry : mlText.entrySet()) {
			if ((entry.getValue() != null) && !entry.getValue().equals(defaultMessage)) {
				message.put(entry.getKey().toString(), entry.getValue());
			}
		}
		return message;
	}

	/**
	 * Converts the requirements, most severe first, until the item cap or the size budget is reached.
	 */
	private static JSONArray toJsonArray(List<Requirement> requirements, int maxBytes, Function<Requirement, JSONObject> converter) {
		JSONArray array = new JSONArray();
		int usedBytes = 0;
		for (Requirement requirement : requirements) {
			JSONObject json = converter.apply(requirement);
			usedBytes += json.toString().getBytes(StandardCharsets.UTF_8).length;
			if ((array.length() >= MAX_REQUIREMENTS) || (usedBytes > maxBytes)) {
				break;
			}
			array.put(json);
		}
		return array;
	}

	private static JSONObject copyScores(String entityScore, String[] props) {
		JSONObject copy = new JSONObject();
		if ((entityScore == null) || entityScore.isBlank()) {
			return copy;
		}
		try {
			JSONObject scores = new JSONObject(entityScore);
			for (String prop : props) {
				if (scores.has(prop)) {
					copy.put(prop, scores.get(prop));
				}
			}
		} catch (JSONException e) {
			logger.warn("Cannot read entity score of change unit: " + e.getMessage());
		}
		return copy;
	}

	private static List<Requirement> toRequirements(List<RequirementListDataItem> items) {
		List<Requirement> requirements = new ArrayList<>();
		if (items != null) {
			for (RequirementListDataItem item : items) {
				requirements.add(Requirement.of(item));
			}
		}
		return requirements;
	}

	private static Set<String> keysOf(List<Requirement> requirements) {
		Set<String> keys = new HashSet<>();
		for (Requirement requirement : requirements) {
			keys.add(requirement.key());
		}
		return keys;
	}
}
