package fr.becpg.repo.product.data;

/**
 * <p>CharactDetailAdditionalValue class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CharactDetailAdditionalValue {

	private String columnName;
	private Double value;
	
	private String unit;

	/**
	 * <p>Constructor for CharactDetailAdditionalValue.</p>
	 *
	 * @param columnName a {@link java.lang.String} object
	 * @param value a {@link java.lang.Double} object
	 * @param unit a {@link java.lang.String} object
	 */
	public CharactDetailAdditionalValue(String columnName, Double value, String unit) {
		super();
		this.columnName = columnName;
		this.value = value;
		this.unit = unit;
	}
	
	/**
	 * <p>Constructor for CharactDetailAdditionalValue.</p>
	 *
	 * @param charactDetailAdditionalValue a {@link fr.becpg.repo.product.data.CharactDetailAdditionalValue} object
	 */
	public CharactDetailAdditionalValue(CharactDetailAdditionalValue charactDetailAdditionalValue) {
		super();
		this.columnName = charactDetailAdditionalValue.columnName;
		this.value = charactDetailAdditionalValue.value;
		this.unit = charactDetailAdditionalValue.unit;
	}
	
	/**
	 * <p>Getter for the field <code>columnName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getValue() {
		return value!=null && value != 0d && !value.isInfinite() && !value.isNaN() ? value : null;
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "CharactDetailAdditionalValue [columnName=" + columnName + ", value=" + value + ", unit=" + unit + "]";
	}

}
