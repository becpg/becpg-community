package fr.becpg.repo.product.data;

public class CharactDetailAdditionalValue {

	private String columnName;
	private Double value;
	
	private String unit;

	public CharactDetailAdditionalValue(String columnName, Double value, String unit) {
		super();
		this.columnName = columnName;
		this.value = value;
		this.unit = unit;
	}
	
	public CharactDetailAdditionalValue(CharactDetailAdditionalValue charactDetailAdditionalValue) {
		super();
		this.columnName = charactDetailAdditionalValue.columnName;
		this.value = charactDetailAdditionalValue.value;
		this.unit = charactDetailAdditionalValue.unit;
	}
	
	public String getColumnName() {
		return columnName;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	@Override
	public String toString() {
		return "CharactDetailAdditionalValue [columnName=" + columnName + ", value=" + value + ", unit=" + unit + "]";
	}

}
