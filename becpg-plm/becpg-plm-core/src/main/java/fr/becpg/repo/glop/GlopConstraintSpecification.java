package fr.becpg.repo.glop;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * Specifies a constraint for a problem solved by the Glop service
 *
 * @author pierrecolin
 * @see fr.becpg.repo.glop.GlopService
 */
public class GlopConstraintSpecification implements Serializable {

	/**
	 * Automatically-generated for {@link java.io.Serializable}
	 */
	private static final long serialVersionUID = 7861470148317601534L;

	@Override
	public String toString() {
		if (isSpecial()) {
			return "GlopConstraintSpecification [special=" + special + ", minValue=" + minValue + ", maxValue=" + maxValue + "]";
		} else {
			return "GlopConstraintSpecification [data=" + data + ", minValue=" + minValue + ", maxValue=" + maxValue + "]";
		}
	}

	private SimpleCharactDataItem data;

	private String special;

	private Double minValue;

	private Double maxValue;

	/**
	 * Constructs a data object constraint specification
	 *
	 * @param data
	 *            the data item from which to generate a constraint
	 * @param minValue
	 *            the lowest accepted value for the item
	 * @param maxValue
	 *            the highest accepted value for the item
	 * @throws IllegalArgumentException
	 *             if {@code minValue > maxValue}
	 */
	public GlopConstraintSpecification(SimpleCharactDataItem data, Double minValue, Double maxValue) throws IllegalArgumentException {
		if (maxValue < minValue) {
			throw new IllegalArgumentException("Min value (" + minValue + ") bigger than max value (" + maxValue + ")");
		}

		this.data = data;
		this.special = null;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/**
	 * Constructs a special constraint specification
	 *
	 * @param special
	 *            the special constraint identifier
	 * @param minValue
	 *            the lowest accepted value
	 * @param maxValue
	 *            the highest accepted value
	 * @throws IllegalArgumentException
	 *             if {@code minValue > maxValue}
	 */
	public GlopConstraintSpecification(String special, Double minValue, Double maxValue) throws IllegalArgumentException {
		if (maxValue < minValue) {
			throw new IllegalArgumentException("Min value (" + minValue + ") bigger than max value (" + maxValue + ")");
		}

		this.data = null;
		this.special = special;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/**
	 * Returns the data item used in the specification
	 *
	 * @return the data item
	 */
	public SimpleCharactDataItem getData() {
		return data;
	}

	/**
	 * Returns the lowest accepted value for the constraint
	 *
	 * @return the lowest accepted value
	 */
	public double getMin() {
		return minValue;
	}

	/**
	 * Returns the highest accepted value for the constraint
	 *
	 * @return the highest accepted value
	 */
	public double getMax() {
		return maxValue;
	}

	private boolean isSpecial() {
		return data == null;
	}

	/**
	 * Adds a constraint to the problem specification if it's a special
	 * constraint. Intended to be used by
	 * {@link fr.becpg.repo.glop.impl.GlopServiceImpl}.
	 *
	 * @param specialContributions
	 *            the map of special constraints
	 * @param compoListDataItem
	 *            the component under scrutiny
	 * @param variables
	 *            the set of variables
	 */
	public void fillIfSpecial(Map<String, Map<CompoListDataItem, Double>> specialContributions, CompoListDataItem compoListDataItem,
			Set<CompoListDataItem> variables) {
		if (isSpecial()) {
			Double coef = null;
			if (special.equals("recipeQtyUsed")) {
				coef = 1d;
			}
			specialContributions.get(special).put(compoListDataItem, coef);
			variables.add(compoListDataItem);
		}
	}

	private static void putNumerical(JSONObject obj, String field, double value) throws JSONException {
		if (Double.isFinite(value)) {
			obj.put(field, value);
		} else if (value > 0) {
			obj.put(field, "inf");
		} else {
			obj.put(field, "-inf");
		}
	}

	/**
	 * Generates a {@link org.json.JSONObject} representation of the constraint.
	 * Intended to be used by {@link fr.becpg.repo.glop.impl.GlopServiceImpl}.
	 *
	 * @param constraintContributions
	 *            the map of data constraints
	 * @param specialContributions
	 *            the map of special constraints
	 * @return the JSON representation of the constraint
	 * @throws JSONException
	 *             if the creation failed
	 */
	public JSONObject toJson(Map<SimpleCharactDataItem, Map<CompoListDataItem, Double>> constraintContributions,
			Map<String, Map<CompoListDataItem, Double>> specialContributions) throws JSONException {
		JSONObject ret = new JSONObject();
		JSONObject coefficients = new JSONObject();
		if (isSpecial()) {
			for (Map.Entry<CompoListDataItem, Double> entry : specialContributions.get(special).entrySet()) {
				coefficients.put(entry.getKey().getProduct().toString(), entry.getValue());
			}
		} else {
			for (Map.Entry<CompoListDataItem, Double> entry : constraintContributions.get(data).entrySet()) {
				coefficients.put(entry.getKey().getProduct().toString(), entry.getValue());
			}
		}
		ret.put("coefficients", coefficients);
		putNumerical(ret, "lower", minValue);
		putNumerical(ret, "upper", maxValue);
		return ret;
	}

	/**
	 * Initializes the constraint in the correct map. Intended to be used by
	 * {@link fr.becpg.repo.glop.impl.GlopServiceImpl}.
	 *
	 * @param constraintContributions
	 *            the map of data constraints
	 * @param specialContributions
	 *            the map of special constraints
	 */
	public void initContributions(Map<SimpleCharactDataItem, Map<CompoListDataItem, Double>> constraintContributions,
			Map<String, Map<CompoListDataItem, Double>> specialContributions) {
		if (isSpecial()) {
			specialContributions.put(special, new HashMap<>());
		} else {
			constraintContributions.put(data, new HashMap<>());
		}
	}

}
