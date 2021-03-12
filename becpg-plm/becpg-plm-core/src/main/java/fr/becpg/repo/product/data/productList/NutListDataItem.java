/*
 *
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.ControlableListDataItem;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.repository.model.UnitAwareDataItem;
import fr.becpg.repo.repository.model.VariantAwareDataItem;

/**
 * <p>NutListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:nutList")
public class NutListDataItem extends VariantAwareDataItem implements SimpleListDataItem, MinMaxValueDataItem, FormulatedCharactDataItem,
		UnitAwareDataItem, ControlableListDataItem, CompositeDataItem<NutListDataItem> {

	/**
	 *
	 */
	private static final long serialVersionUID = -4580421935974923617L;

	private Double manualValue;

	private Double formulatedValue;

	private String unit;

	private Double mini;

	private Double maxi;

	private Double valuePerServing;

	private Double gdaPerc;

	private Double lossPerc;

	private String group;

	private String method;

	private NodeRef nut;

	private Boolean isFormulated;

	private String errorLog;

	private Integer depthLevel;

	private NutListDataItem parent;

	private String roundedValue;

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * <p>Setter for the field <code>depthLevel</code>.</p>
	 *
	 * @param depthLevel a {@link java.lang.Integer} object.
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:parentLevel")
	public NutListDataItem getParent() {
		return this.parent;
	}

	/** {@inheritDoc} */
	@Override
	public void setParent(NutListDataItem parent) {
		this.parent = parent;
	}

	/** {@inheritDoc} */
	@Override
	public Double getValue() {
		return manualValue != null ? manualValue : formulatedValue;
	}

	/** {@inheritDoc} */
	@Override
	public void setValue(Double value) {
		this.formulatedValue = value;
	}

	/**
	 * <p>value.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public Double value(String key) {
		return RegulationFormulationHelper.extractValue(getRoundedValue(), key);
	}
	
	/**
	 * <p>variantValue.</p>
	 *
	 * @param variantColumn a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public Double variantValue(String variantColumn, String key) {
		return RegulationFormulationHelper.extractVariantValue(getRoundedValue(), variantColumn, key);
	}
	

	/**
	 * <p>Getter for the field <code>manualValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:nutListValue")
	public Double getManualValue() {
		return manualValue;
	}

	/**
	 * <p>Setter for the field <code>manualValue</code>.</p>
	 *
	 * @param manualValue a {@link java.lang.Double} object.
	 */
	public void setManualValue(Double manualValue) {
		this.manualValue = manualValue;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:nutListFormulatedValue")
	public Double getFormulatedValue() {
		return formulatedValue;
	}

	/** {@inheritDoc} */
	@Override
	public void setFormulatedValue(Double formulatedValue) {
		this.formulatedValue = formulatedValue;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:nutListUnit")
	public String getUnit() {
		return unit;
	}

	/** {@inheritDoc} */
	@Override
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:nutListMini")
	public Double getMini() {
		return mini;
	}

	/** {@inheritDoc} */
	@Override
	public void setMini(Double mini) {
		this.mini = mini;
	}

	/**
	 * <p>mini.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public Double mini(String key) {
		return RegulationFormulationHelper.extractMini(getRoundedValue(), key);
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:nutListMaxi")
	public Double getMaxi() {
		return maxi;
	}

	/** {@inheritDoc} */
	@Override
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}

	/**
	 * <p>maxi.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public Double maxi(String key) {
		return  RegulationFormulationHelper.extractMaxi(getRoundedValue(), key);
	}

	/**
	 * <p>Getter for the field <code>valuePerServing</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:nutListValuePerServing")
	public Double getValuePerServing() {
		return valuePerServing;
	}

	/**
	 * <p>Setter for the field <code>valuePerServing</code>.</p>
	 *
	 * @param valuePerServing a {@link java.lang.Double} object.
	 */
	public void setValuePerServing(Double valuePerServing) {
		this.valuePerServing = valuePerServing;
	}

	/**
	 * <p>valuePerServing.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public Double valuePerServing(String key) {
		return RegulationFormulationHelper.extractValuePerServing(getRoundedValue(), key);
	}

	/**
	 * <p>Getter for the field <code>gdaPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:nutListGDAPerc")
	public Double getGdaPerc() {
		return gdaPerc;
	}

	/**
	 * <p>Setter for the field <code>gdaPerc</code>.</p>
	 *
	 * @param gdaPerc a {@link java.lang.Double} object.
	 */
	public void setGdaPerc(Double gdaPerc) {
		this.gdaPerc = gdaPerc;
	}

	
	/**
	 * <p>gdaPerc.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public Double gdaPerc(String key) {
		return RegulationFormulationHelper.extractGDAPerc(getRoundedValue(), key);
	}

	
	
	/**
	 * <p>Getter for the field <code>lossPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:nutListLossPerc")
	public Double getLossPerc() {
		return lossPerc;
	}

	/**
	 * <p>Setter for the field <code>lossPerc</code>.</p>
	 *
	 * @param lossPerc a {@link java.lang.Double} object.
	 */
	public void setLossPerc(Double lossPerc) {
		this.lossPerc = lossPerc;
	}

	/**
	 * <p>Getter for the field <code>group</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:nutListGroup")
	public String getGroup() {
		return group;
	}

	/**
	 * <p>Setter for the field <code>group</code>.</p>
	 *
	 * @param group a {@link java.lang.String} object.
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * <p>Getter for the field <code>method</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:nutListMethod")
	public String getMethod() {
		return method;
	}

	/**
	 * <p>Setter for the field <code>method</code>.</p>
	 *
	 * @param method a {@link java.lang.String} object.
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * <p>Getter for the field <code>nut</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@InternalField
	@AlfQname(qname = "bcpg:nutListNut")
	@DataListIdentifierAttr
	public NodeRef getNut() {
		return nut;
	}

	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return getNut();
	}

	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setNut(nodeRef);
	}

	/**
	 * Sets the nut.
	 *
	 * @param nut
	 *            the new nut
	 */
	public void setNut(NodeRef nut) {
		this.nut = nut;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:nutListIsFormulated")
	public Boolean getIsFormulated() {
		return isFormulated;
	}

	/** {@inheritDoc} */
	@Override
	public void setIsFormulated(Boolean isFormulated) {
		this.isFormulated = isFormulated;
	}

	/** {@inheritDoc} */
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:nutListFormulaErrorLog")
	@Override
	public String getErrorLog() {
		return errorLog;
	}

	/** {@inheritDoc} */
	@Override
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}

	/** {@inheritDoc} */
	@Override
	public String getTextCriteria() {
		return null;
	}

	/**
	 * <p>Getter for the field <code>roundedValue</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:nutListRoundedValue")
	public String getRoundedValue() {
		return roundedValue;
	}

	/**
	 * <p>Setter for the field <code>roundedValue</code>.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 */
	public void setRoundedValue(String roundedValue) {
		this.roundedValue = roundedValue;
	}
	

	/**
	 * Instantiates a new nut list data item.
	 */
	public NutListDataItem() {
		super();
	}

	/**
	 * Instantiates a new nut list data item.
	 *
	 * @param nodeRef
	 *            the node ref
	 * @param value
	 *            the value
	 * @param unit
	 *            the unit
	 * @param group
	 *            the group
	 * @param nut
	 *            the nut
	 * @param mini a {@link java.lang.Double} object.
	 * @param maxi a {@link java.lang.Double} object.
	 * @param isManual a {@link java.lang.Boolean} object.
	 */
	public NutListDataItem(NodeRef nodeRef, Double value, String unit, Double mini, Double maxi, String group, NodeRef nut, Boolean isManual) {
		setNodeRef(nodeRef);
		setValue(value);
		setUnit(unit);
		setMini(mini);
		setMaxi(maxi);
		setGroup(group);
		setNut(nut);
		setIsManual(isManual);
	}

	/**
	 * Copy constructor
	 *
	 * @param n a {@link fr.becpg.repo.product.data.productList.NutListDataItem} object.
	 */
	public NutListDataItem(NutListDataItem n) {
		super(n);
		this.manualValue = n.manualValue;
		this.formulatedValue = n.formulatedValue;
		this.unit = n.unit;
		this.mini = n.mini;
		this.maxi = n.maxi;
		this.valuePerServing = n.valuePerServing;
		this.gdaPerc = n.gdaPerc;
		this.lossPerc = n.lossPerc;
		this.group = n.group;
		this.method = n.method;
		this.nut = n.nut;
		this.isFormulated = n.isFormulated;
		this.errorLog = n.errorLog;
		this.roundedValue = n.roundedValue;
	}

	/** {@inheritDoc} */
	@Override
	public NutListDataItem clone() {
		return new NutListDataItem(this);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((depthLevel == null) ? 0 : depthLevel.hashCode());
		result = (prime * result) + ((errorLog == null) ? 0 : errorLog.hashCode());
		result = (prime * result) + ((formulatedValue == null) ? 0 : formulatedValue.hashCode());
		result = (prime * result) + ((gdaPerc == null) ? 0 : gdaPerc.hashCode());
		result = (prime * result) + ((group == null) ? 0 : group.hashCode());
		result = (prime * result) + ((isFormulated == null) ? 0 : isFormulated.hashCode());
		result = (prime * result) + ((lossPerc == null) ? 0 : lossPerc.hashCode());
		result = (prime * result) + ((manualValue == null) ? 0 : manualValue.hashCode());
		result = (prime * result) + ((maxi == null) ? 0 : maxi.hashCode());
		result = (prime * result) + ((method == null) ? 0 : method.hashCode());
		result = (prime * result) + ((mini == null) ? 0 : mini.hashCode());
		result = (prime * result) + ((nut == null) ? 0 : nut.hashCode());
		result = (prime * result) + ((parent == null) ? 0 : parent.hashCode());
		result = (prime * result) + ((roundedValue == null) ? 0 : roundedValue.hashCode());
		result = (prime * result) + ((unit == null) ? 0 : unit.hashCode());
		result = (prime * result) + ((valuePerServing == null) ? 0 : valuePerServing.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NutListDataItem other = (NutListDataItem) obj;
		if (depthLevel == null) {
			if (other.depthLevel != null) {
				return false;
			}
		} else if (!depthLevel.equals(other.depthLevel)) {
			return false;
		}
		if (errorLog == null) {
			if (other.errorLog != null) {
				return false;
			}
		} else if (!errorLog.equals(other.errorLog)) {
			return false;
		}
		if (formulatedValue == null) {
			if (other.formulatedValue != null) {
				return false;
			}
		} else if (!formulatedValue.equals(other.formulatedValue)) {
			return false;
		}
		if (gdaPerc == null) {
			if (other.gdaPerc != null) {
				return false;
			}
		} else if (!gdaPerc.equals(other.gdaPerc)) {
			return false;
		}
		if (group == null) {
			if (other.group != null) {
				return false;
			}
		} else if (!group.equals(other.group)) {
			return false;
		}
		if (isFormulated == null) {
			if (other.isFormulated != null) {
				return false;
			}
		} else if (!isFormulated.equals(other.isFormulated)) {
			return false;
		}
		if (lossPerc == null) {
			if (other.lossPerc != null) {
				return false;
			}
		} else if (!lossPerc.equals(other.lossPerc)) {
			return false;
		}
		if (manualValue == null) {
			if (other.manualValue != null) {
				return false;
			}
		} else if (!manualValue.equals(other.manualValue)) {
			return false;
		}
		if (maxi == null) {
			if (other.maxi != null) {
				return false;
			}
		} else if (!maxi.equals(other.maxi)) {
			return false;
		}
		if (method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!method.equals(other.method)) {
			return false;
		}
		if (mini == null) {
			if (other.mini != null) {
				return false;
			}
		} else if (!mini.equals(other.mini)) {
			return false;
		}
		if (nut == null) {
			if (other.nut != null) {
				return false;
			}
		} else if (!nut.equals(other.nut)) {
			return false;
		}
		if (parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!parent.equals(other.parent)) {
			return false;
		}
		if (roundedValue == null) {
			if (other.roundedValue != null) {
				return false;
			}
		} else if (!roundedValue.equals(other.roundedValue)) {
			return false;
		}
		if (unit == null) {
			if (other.unit != null) {
				return false;
			}
		} else if (!unit.equals(other.unit)) {
			return false;
		}
		if (valuePerServing == null) {
			if (other.valuePerServing != null) {
				return false;
			}
		} else if (!valuePerServing.equals(other.valuePerServing)) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "NutListDataItem [manualValue=" + manualValue + ", formulatedValue=" + formulatedValue + ", unit=" + unit + ", mini=" + mini
				+ ", maxi=" + maxi + ", valuePerServing=" + valuePerServing + ", gdaPerc=" + gdaPerc + ", lossPerc=" + lossPerc + ", group=" + group
				+ ", method=" + method + ", nut=" + nut + ", isFormulated=" + isFormulated + ", errorLog=" + errorLog + ", depthLevel=" + depthLevel
				+ ", parent=" + parent + ", roundedValue=" + roundedValue + "]";
	}

}
