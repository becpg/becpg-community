package fr.becpg.repo.product.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>CharactDetailsValue class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CharactDetailsValue {

	private NodeRef parentNodeRef;
	private NodeRef keyNodeRef;
	private NodeRef compositeNodeRef;
	private List<String> forecastColumns = new ArrayList<>();
	private Map<String, Double> forecastValues = new HashMap<>();
	private Double value;
	private Double mini;
	private Double maxi;
	private Integer level;
	private String unit;
	private String name;
	private List<CharactDetailAdditionalValue> additionalValues = new ArrayList<>();

	/**
	 * <p>Constructor for CharactDetailsValue.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param keyNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param compositeNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param value a {@link java.lang.Double} object.
	 * @param level a {@link java.lang.Integer} object.
	 * @param unit a {@link java.lang.String} object.
	 */
	public CharactDetailsValue(NodeRef parentNodeRef, NodeRef keyNodeRef, NodeRef compositeNodeRef, Double value, Integer level, String unit) {
		super();
		this.parentNodeRef = parentNodeRef;
		this.keyNodeRef = keyNodeRef;
		this.compositeNodeRef = compositeNodeRef;
		this.value = value;
		this.level = level;
		this.unit = unit;
	}
	
	/**
	 * <p>Getter for the field <code>forecastColumns</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<String> getForecastColumns() {
		return forecastColumns;
	}
	
	/**
	 * <p>setForecastValue.</p>
	 *
	 * @param forecastColumn a {@link java.lang.String} object
	 * @param value a {@link java.lang.Double} object
	 */
	public void setForecastValue(String forecastColumn, Double value) {
		if (!forecastColumns.contains(forecastColumn)) {
			forecastColumns.add(forecastColumn);
		}
		this.forecastValues.put(forecastColumn, value);
	}
	
	/**
	 * <p>getForecastValue.</p>
	 *
	 * @param forecastColumn a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double getForecastValue(String forecastColumn) {
		return forecastValues.get(forecastColumn);
	}
	
	/**
	 * <p>Getter for the field <code>additionalValues</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<CharactDetailAdditionalValue> getAdditionalValues() {
		return additionalValues;
	}

	/**
	 * <p>Getter for the field <code>keyNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getKeyNodeRef() {
		return keyNodeRef;
	}
	

	/**
	 * <p>Getter for the field <code>compositeNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getCompositeNodeRef() {
		return compositeNodeRef;
	}

	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 */
	public void setValue(Double value) {
		this.value = value;
	}


	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getValue() {
		return value!=null && value != 0d && !value.isInfinite() && !value.isNaN() ? value : null;
	}
	
	
	/**
	 * <p>Getter for the field <code>level</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getLevel() {
		return level;
	}

	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * <p>Getter for the field <code>mini</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getMini() {
		return mini;
	}

	/**
	 * <p>Setter for the field <code>mini</code>.</p>
	 *
	 * @param mini a {@link java.lang.Double} object.
	 */
	public void setMini(Double mini) {
		this.mini = mini;
	}

	/**
	 * <p>Getter for the field <code>maxi</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getMaxi() {
		return maxi;
	}

	/**
	 * <p>Setter for the field <code>maxi</code>.</p>
	 *
	 * @param maxi a {@link java.lang.Double} object.
	 */
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Setter for the field <code>name</code>.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>add.</p>
	 *
	 * @param caractValue a {@link fr.becpg.repo.product.data.CharactDetailsValue} object.
	 */
	public void add(CharactDetailsValue caractValue) {
		if (caractValue.getValue() != null) {
			if(value==null) {
				value = 0d;
			}
			value += caractValue.getValue();
		}
		
		if (caractValue.getMini() != null) {
			if(mini==null) {
				mini = 0d;
			}
			mini += caractValue.getMini();
		}
		
		if (caractValue.getMaxi() != null) {
			if(maxi==null) {
				maxi = 0d;
			}
			maxi += caractValue.getMaxi();
		}
		
		for (Entry<String, Double> entry : caractValue.forecastValues.entrySet()) {
			String forecastColumn = entry.getKey();
			Double forecastValue = entry.getValue();
			if (forecastValue != null) {
				if (getForecastValue(forecastColumn) == null) {
					setForecastValue(forecastColumn, 0d);
				}
				setForecastValue(forecastColumn, getForecastValue(forecastColumn) + forecastValue);
			}
		}
		
		for (CharactDetailAdditionalValue additionalValue : caractValue.getAdditionalValues()) {
			CharactDetailAdditionalValue currentAdditionalValue = getAdditionalValue(additionalValue.getColumnName());
			if (currentAdditionalValue == null) {
				additionalValues.add(new CharactDetailAdditionalValue(additionalValue));
			} else {
				Double currentValue = currentAdditionalValue.getValue();
				currentValue = currentValue == null ? 0d : currentValue;
				Double addedValue = additionalValue.getValue();
				addedValue = addedValue == null ? 0d : addedValue;
				currentAdditionalValue.setValue(currentValue + addedValue);
			}
		}
	}
	
	/**
	 * <p>getAdditionalValue.</p>
	 *
	 * @param columnName a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.CharactDetailAdditionalValue} object
	 */
	public CharactDetailAdditionalValue getAdditionalValue(String columnName) {
		for (CharactDetailAdditionalValue additionalValue : additionalValues) {
			if (columnName.equals(additionalValue.getColumnName())) {
				return additionalValue;
			}
		}
		return null;
	}
	
	/**
	 * <p>keyEquals.</p>
	 *
	 * @param other a {@link fr.becpg.repo.product.data.CharactDetailsValue} object.
	 * @return a boolean.
	 */
	public boolean keyEquals(CharactDetailsValue other) {
		if (keyNodeRef == null) {
			if (other.keyNodeRef != null)
				return false;
		} else if (!keyNodeRef.equals(other.keyNodeRef))
			return false;
		if (level == null) {
			if (other.level != null)
				return false;
		} else if (!level.equals(other.level))
			return false;
		if (parentNodeRef == null) {
			if (other.parentNodeRef != null)
				return false;
		} else if (!parentNodeRef.equals(other.parentNodeRef))
			return false;
		if (compositeNodeRef == null) {
			if (other.compositeNodeRef != null)
				return false;
		} else if (!compositeNodeRef.equals(other.compositeNodeRef))
			return false;
		return true;
	}

	//TODO A refaire issue #4612
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyNodeRef == null) ? 0 : keyNodeRef.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((mini == null) ? 0 : mini.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parentNodeRef == null) ? 0 : parentNodeRef.hashCode());
		result = prime * result + ((compositeNodeRef == null) ? 0 : compositeNodeRef.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CharactDetailsValue other = (CharactDetailsValue) obj;
		if (keyNodeRef == null) {
			if (other.keyNodeRef != null)
				return false;
		} else if (!keyNodeRef.equals(other.keyNodeRef))
			return false;
		if (level == null) {
			if (other.level != null)
				return false;
		} else if (!level.equals(other.level))
			return false;
		if (maxi == null) {
			if (other.maxi != null)
				return false;
		} else if (!maxi.equals(other.maxi))
			return false;
		if (mini == null) {
			if (other.mini != null)
				return false;
		} else if (!mini.equals(other.mini))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parentNodeRef == null) {
			if (other.parentNodeRef != null)
				return false;
		} else if (!parentNodeRef.equals(other.parentNodeRef))
			return false;
		if (compositeNodeRef == null) {
			if (other.compositeNodeRef != null)
				return false;
		} else if (!compositeNodeRef.equals(other.compositeNodeRef))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "CharactDetailsValue [parentNodeRef=" + parentNodeRef + ", keyNodeRef=" + keyNodeRef + ", compositeNodeRef=" + compositeNodeRef
				+ ", value=" + value + ", mini=" + mini + ", maxi=" + maxi
				+ ", level=" + level + ", unit=" + unit + ", name=" + name + "]";
	}

}
