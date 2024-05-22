/*
 *
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.ControlableListDataItem;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;
import fr.becpg.repo.repository.model.ManualDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.repository.model.SourceableDataItem;
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
public class NutListDataItem extends VariantAwareDataItem implements SimpleListDataItem, MinMaxValueDataItem, FormulatedCharactDataItem, SourceableDataItem,
		UnitAwareDataItem, ControlableListDataItem, CompositeDataItem<NutListDataItem>, ManualDataItem {

	/** Constant <code>UNIT_PER100G="/100g"</code> */
	public static final String UNIT_PER100G = "/100g";

	/** Constant <code>UNIT_PER100ML="/100mL"</code> */
	public static final String UNIT_PER100ML = "/100mL";

	/**
	 *
	 */
	private static final long serialVersionUID = -4580421935974923617L;

	private Double manualValue;
	
	private Double formulatedValue;
	
	private Double manualPreparedValue;
	
	private Double formulatedPreparedValue;

	private String unit;

	private Double manualMini;

	private Double formulatedMini;

	private Double manualMaxi;

	private Double formulatedMaxi;
	
	private Double manualValuePerServing;

	private Double formulatedValuePerServing;
	
	private Double gdaPerc;

	private Double lossPerc;

	private String group;

	private String method;
	
	private String measurementPrecision;

	private NodeRef nut;

	private Boolean isFormulated;

	private Integer depthLevel;

	private NutListDataItem parent;

	private String roundedValue;
	
	private String roundedValuePrepared;
	
	private List<NodeRef> sources = new ArrayList<>();

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
	
	
	
	@AlfMultiAssoc
	@InternalField
	@AlfQname(qname="bcpg:nutListSources")
	@Override
	public List<NodeRef> getSources() {
		return sources;
	}

	public void setSources(List<NodeRef> sources) {
		this.sources = sources;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:nutListFormulatedValuePrepared")
	public Double getFormulatedPreparedValue() {
		return formulatedPreparedValue;
	}

	public void setFormulatedPreparedValue(Double formulatedPreparedValue) {
		this.formulatedPreparedValue = formulatedPreparedValue;
	}
	
	@AlfProp
	@AlfQname(qname = "bcpg:nutListValuePrepared")
	public Double getManualPreparedValue() {
		return manualPreparedValue;
	}

	public void setManualPreparedValue(Double manualPreparedValue) {
		this.manualPreparedValue = manualPreparedValue;
	}

	/**
	 * <p>Getter for the field <code>preparedValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getPreparedValue() {
		return manualPreparedValue != null ? manualPreparedValue : formulatedPreparedValue;
	}

	/**
	 * <p>Setter for the field <code>preparedValue</code>.</p>
	 *
	 * @param preparedValue a {@link java.lang.Double} object.
	 */
	public void setPreparedValue(Double preparedValue) {
		this.formulatedPreparedValue = preparedValue;
	}
	
	@AlfProp
	@AlfQname(qname = "bcpg:nutListMeasurementPrecision")
	public String getMeasurementPrecision() {
		return measurementPrecision;
	}

	public void setMeasurementPrecision(String measurementPrecision) {
		this.measurementPrecision = measurementPrecision;
	}

	/**
	 * <p>preparedValue.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public Double preparedValue(String key) {
		return RegulationFormulationHelper.extractPreparedValue(getRoundedValue(), key);
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
	public Double getMini() {
		return manualMini != null ? manualMini : formulatedMini;
	}

	/** {@inheritDoc} */
	@Override
	public void setMini(Double mini) {
		this.formulatedMini = mini;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:nutListMini")
	public Double getManualMini() {
		return manualMini;
	}

	public void setManualMini(Double manualMini) {
		this.manualMini = manualMini;
	}

	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:nutListFormulatedMini")
	public Double getFormulatedMini() {
		return formulatedMini;
	}

	public void setFormulatedMini(Double formulatedMini) {
		this.formulatedMini = formulatedMini;
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
	public Double getMaxi() {
		return manualMaxi != null ? manualMaxi : formulatedMaxi;
	}

	/** {@inheritDoc} */
	@Override
	public void setMaxi(Double mini) {
		this.formulatedMaxi = mini;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:nutListMaxi")
	public Double getManualMaxi() {
		return manualMaxi;
	}

	public void setManualMaxi(Double manualMaxi) {
		this.manualMaxi = manualMaxi;
	}

	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:nutListFormulatedMaxi")
	public Double getFormulatedMaxi() {
		return formulatedMaxi;
	}

	public void setFormulatedMaxi(Double formulatedMaxi) {
		this.formulatedMaxi = formulatedMaxi;
	}

	/**
	 * <p>maxi.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public Double maxi(String key) {
		return RegulationFormulationHelper.extractMaxi(getRoundedValue(), key);
	}
	
	
	
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:nutListFormulatedValuePerServing")
	public Double getFormulatedValuePerServing() {
		return formulatedValuePerServing;
	}

	public void setFormulatedValuePerServing(Double formulatedValuePerServing) {
		this.formulatedValuePerServing = formulatedValuePerServing;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:nutListValuePerServing")
	public Double getManualValuePerServing() {
		return manualValuePerServing;
	}

	public void setManualValuePerServing(Double manualValuePerServing) {
		this.manualValuePerServing = manualValuePerServing;
	}

	/**
	 * <p>Getter for the field <code>valuePerServing</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getValuePerServing() {
		return manualValuePerServing!=null ? manualValuePerServing : formulatedValuePerServing;
	}

	/**
	 * <p>Setter for the field <code>valuePerServing</code>.</p>
	 *
	 * @param valuePerServing a {@link java.lang.Double} object.
	 */
	public void setValuePerServing(Double valuePerServing) {
		this.formulatedValuePerServing = valuePerServing;
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
	@Override
	public MLText getTextCriteria() {
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
	 * <p>Getter for the field <code>roundedValuePrepared</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:nutListRoundedValuePrepared")
	public String getRoundedValuePrepared() {
		return roundedValuePrepared;
	}

	/**
	 * <p>Setter for the field <code>roundedValuePrepared</code>.</p>
	 *
	 * @param roundedValue a {@link java.lang.String} object.
	 */
	public void setRoundedValuePrepared(String roundedValuePrepared) {
		this.roundedValuePrepared = roundedValuePrepared;
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
		this.manualMini = n.manualMini;
		this.formulatedMini = n.formulatedMini;
		this.manualMaxi = n.manualMaxi;
		this.formulatedMaxi = n.formulatedMaxi;
		this.manualValuePerServing = n.manualValuePerServing;
		this.formulatedValuePerServing = n.formulatedValuePerServing;
		this.gdaPerc = n.gdaPerc;
		this.lossPerc = n.lossPerc;
		this.group = n.group;
		this.method = n.method;
		this.nut = n.nut;
		this.isFormulated = n.isFormulated;
		this.roundedValue = n.roundedValue;
		this.roundedValuePrepared = n.roundedValuePrepared;
		this.manualPreparedValue = n.manualPreparedValue;
		this.formulatedPreparedValue = n.formulatedPreparedValue;
		this.measurementPrecision = n.measurementPrecision;
		this.sources = n.sources;
	}

	/** {@inheritDoc} */
	@Override
	public NutListDataItem copy() {
		NutListDataItem ret = new NutListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public Boolean shouldDetailIfZero() {
		return true;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(depthLevel, formulatedMaxi, formulatedMini, formulatedPreparedValue, formulatedValue,
				formulatedValuePerServing, gdaPerc, group, isFormulated, lossPerc, manualMaxi, manualMini, manualPreparedValue, manualValue,
				manualValuePerServing, measurementPrecision, method, nut, parent, roundedValue,roundedValuePrepared, sources, unit);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NutListDataItem other = (NutListDataItem) obj;
		return Objects.equals(depthLevel, other.depthLevel)
				&& Objects.equals(formulatedMaxi, other.formulatedMaxi) && Objects.equals(formulatedMini, other.formulatedMini)
				&& Objects.equals(formulatedPreparedValue, other.formulatedPreparedValue) && Objects.equals(formulatedValue, other.formulatedValue)
				&& Objects.equals(formulatedValuePerServing, other.formulatedValuePerServing) && Objects.equals(gdaPerc, other.gdaPerc)
				&& Objects.equals(group, other.group) && Objects.equals(isFormulated, other.isFormulated) && Objects.equals(lossPerc, other.lossPerc)
				&& Objects.equals(manualMaxi, other.manualMaxi) && Objects.equals(manualMini, other.manualMini)
				&& Objects.equals(manualPreparedValue, other.manualPreparedValue) && Objects.equals(manualValue, other.manualValue)
				&& Objects.equals(manualValuePerServing, other.manualValuePerServing)
				&& Objects.equals(measurementPrecision, other.measurementPrecision) && Objects.equals(method, other.method)
				&& Objects.equals(nut, other.nut) && Objects.equals(parent, other.parent) && Objects.equals(roundedValue, other.roundedValue) && Objects.equals(roundedValuePrepared, other.roundedValuePrepared)
				&& Objects.equals(sources, other.sources) && Objects.equals(unit, other.unit);
	}

	@Override
	public String toString() {
		return "NutListDataItem [manualValue=" + manualValue + ", formulatedValue=" + formulatedValue + ", manualPreparedValue=" + manualPreparedValue
				+ ", formulatedPreparedValue=" + formulatedPreparedValue + ", unit=" + unit + ", manualMini=" + manualMini + ", formulatedMini="
				+ formulatedMini + ", manualMaxi=" + manualMaxi + ", formulatedMaxi=" + formulatedMaxi + ", manualValuePerServing="
				+ manualValuePerServing + ", formulatedValuePerServing=" + formulatedValuePerServing + ", gdaPerc=" + gdaPerc + ", lossPerc="
				+ lossPerc + ", group=" + group + ", method=" + method + ", measurementPrecision=" + measurementPrecision + ", nut=" + nut
				+ ", isFormulated=" + isFormulated + ", depthLevel=" + depthLevel + ", parent=" + parent
				+ ", roundedValue=" + roundedValue + ", roundedValuePrepared=" + roundedValuePrepared + ", sources=" + sources + "]";
	}

}
