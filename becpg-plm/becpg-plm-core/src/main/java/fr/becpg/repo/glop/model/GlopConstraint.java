package fr.becpg.repo.glop.model;

import java.io.Serializable;

/**
 * Specifies a constraint for a problem solved by the Glop service
 *
 * @author pierrecolin
 * @see fr.becpg.repo.glop.GlopService
 * @version $Id: $Id
 */
public class GlopConstraint implements Serializable {

	/**
	 * Automatically-generated for {@link java.io.Serializable}
	 */
	private static final long serialVersionUID = 7861470148317601534L;

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "GlopConstraint [data=" + data + ", minValue=" + minValue + ", maxValue=" + maxValue + "]";
	}

	private Object data;

	private Double minValue;

	private Double maxValue;
	
	private Double minTolerance;

	private Double maxTolerance;

	private Double tolerance;

	/**
	 * <p>Getter for the field <code>minTolerance</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMinTolerance() {
		return minTolerance;
	}

	/**
	 * <p>Setter for the field <code>minTolerance</code>.</p>
	 *
	 * @param minTolerance a {@link java.lang.Double} object
	 */
	public void setMinTolerance(Double minTolerance) {
		this.minTolerance = minTolerance;
	}

	/**
	 * <p>Getter for the field <code>maxTolerance</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMaxTolerance() {
		return maxTolerance;
	}

	/**
	 * <p>Setter for the field <code>maxTolerance</code>.</p>
	 *
	 * @param maxTolerance a {@link java.lang.Double} object
	 */
	public void setMaxTolerance(Double maxTolerance) {
		this.maxTolerance = maxTolerance;
	}

	/**
	 * <p>Getter for the field <code>minValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMinValue() {
		return minValue;
	}

	/**
	 * <p>Setter for the field <code>minValue</code>.</p>
	 *
	 * @param minValue a {@link java.lang.Double} object
	 */
	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}

	/**
	 * <p>Getter for the field <code>maxValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMaxValue() {
		return maxValue;
	}

	/**
	 * <p>Setter for the field <code>maxValue</code>.</p>
	 *
	 * @param maxValue a {@link java.lang.Double} object
	 */
	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * Constructs a data object constraint specification
	 *
	 * @param data
	 *            the data item from which to generate a constraint
	 * @param minValue
	 *            the lowest accepted value for the item
	 * @param maxValue
	 *            the highest accepted value for the item
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code minValue > maxValue}
	 */
	public GlopConstraint(Object data, Double minValue, Double maxValue) throws IllegalArgumentException {
		if (maxValue < minValue) {
			throw new IllegalArgumentException("Min value (" + minValue + ") bigger than max value (" + maxValue + ")");
		}

		this.data = data;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/**
	 * Returns the data item used in the specification
	 *
	 * @return the data item
	 */
	public Object getData() {
		return data;
	}
	
	/**
	 * <p>Getter for the field <code>tolerance</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getTolerance() {
		return tolerance;
	}

	/**
	 * <p>Setter for the field <code>tolerance</code>.</p>
	 *
	 * @param tolerance a {@link java.lang.Double} object
	 */
	public void setTolerance(Double tolerance) {
		this.tolerance = tolerance;
	}

}
