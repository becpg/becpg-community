/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.ControlableListDataItem;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.repository.model.UnitAwareDataItem;

@AlfType
@AlfQname(qname = "bcpg:nutList")
public class NutListDataItem extends AbstractManualDataItem  implements SimpleListDataItem, MinMaxValueDataItem, FormulatedCharactDataItem, UnitAwareDataItem, ControlableListDataItem, CompositeDataItem<NutListDataItem> {

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
	
	private Double gdaPercByProfile;
	
	private Double gdaPercByLocale;
	
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:parentLevel")
	public NutListDataItem getParent() {
		return this.parent;
	}

	@Override
	public void setParent(NutListDataItem parent) {
		this.parent = parent;
	}
	
		
	public Double getValue() {
		return manualValue!=null ? manualValue : formulatedValue;
	}
	
	
	public void setValue(Double value) {
		this.formulatedValue = value;
	}

	
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:nutListValue")
	public Double getManualValue() {
		return manualValue;
	}


	public void setManualValue(Double manualValue) {
		this.manualValue = manualValue;
	}


	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:nutListFormulatedValue")
	public Double getFormulatedValue() {
		return formulatedValue;
	}


	public void setFormulatedValue(Double formulatedValue) {
		this.formulatedValue = formulatedValue;
	}


	@AlfProp
	@AlfQname(qname="bcpg:nutListUnit")
	public String getUnit() {
		return unit;
	}
	
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	

	
	@AlfProp
	@AlfQname(qname="bcpg:nutListMini")
	public Double getMini() {
		return mini;
	}

	public void setMini(Double mini) {
		this.mini = mini;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutListMaxi")
	public Double getMaxi() {
		return maxi;
	}

	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutListValuePerServing")
	public Double getValuePerServing() {
		return valuePerServing;
	}


	public void setValuePerServing(Double valuePerServing) {
		this.valuePerServing = valuePerServing;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutListGDAPerc")
	public Double getGdaPerc() {
		return gdaPerc;
	}

	public void setGdaPerc(Double gdaPerc) {
		this.gdaPerc = gdaPerc;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:gdaPercByProfile")
	public Double getGdaPercByProfile(String profileId) {
		return gdaPercByProfile;
	}

	public void setGdaPercByProfile(Double gdaPercByProfile) {
		this.gdaPercByProfile = gdaPercByProfile;
	}
	
	public Double getGdaPercByLocale(Locale locale) {
		return gdaPercByLocale;
	}

	public void setGdaPercByLocale(Double gdaPercByLocale) {
		this.gdaPercByLocale = gdaPercByLocale;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutListLossPerc")
	public Double getLossPerc() {
		return lossPerc;
	}

	public void setLossPerc(Double lossPerc) {
		this.lossPerc = lossPerc;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutListGroup")
	public String getGroup() {
		return group;
	}
	
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:nutListMethod")
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@AlfSingleAssoc
	@InternalField
	@AlfQname(qname="bcpg:nutListNut")
	@DataListIdentifierAttr
	public NodeRef getNut() {
		return nut;
	}

	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return getNut();
	}
	
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setNut(nodeRef);		
	}
	
	/**
	 * Sets the nut.
	 *
	 * @param nut the new nut
	 */
	public void setNut(NodeRef nut) {
		this.nut = nut;
	}	

	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:nutListIsFormulated")
	public Boolean getIsFormulated() {
		return isFormulated;
	}


	public void setIsFormulated(Boolean isFormulated) {
		this.isFormulated = isFormulated;
	}

	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:nutListFormulaErrorLog")
	@Override
	public String getErrorLog() {
		return errorLog;
	}

	@Override
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}
	

	@Override
	public String getTextCriteria() {
		return null;
	}	
	
	
	@AlfProp
	@AlfQname(qname = "bcpg:nutListRoundedValue")
	public String getRoundedValue() {
		return roundedValue;
	}

	public void setRoundedValue(String roundedValue) {
		this.roundedValue = roundedValue;
	}
	

	
	/**
	 * Instantiates a new nut list data item.
	 */
	public NutListDataItem()
	{
		super();
	}
	
	/**
	 * Instantiates a new nut list data item.
	 *
	 * @param nodeRef the node ref
	 * @param value the value
	 * @param unit the unit
	 * @param group the group
	 * @param nut the nut
	 */
	public NutListDataItem(NodeRef nodeRef,	Double value, String unit, Double mini, Double maxi, String group, NodeRef nut, Boolean isManual)
	{
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
	 * @param n
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
		this.roundedValue=n.roundedValue;
	}


	@Override
	public NutListDataItem clone() {
		return new NutListDataItem(this);
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((depthLevel == null) ? 0 : depthLevel.hashCode());
		result = prime * result
				+ ((errorLog == null) ? 0 : errorLog.hashCode());
		result = prime * result
				+ ((formulatedValue == null) ? 0 : formulatedValue.hashCode());
		result = prime * result + ((gdaPerc == null) ? 0 : gdaPerc.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result
				+ ((isFormulated == null) ? 0 : isFormulated.hashCode());
		result = prime * result
				+ ((lossPerc == null) ? 0 : lossPerc.hashCode());
		result = prime * result
				+ ((manualValue == null) ? 0 : manualValue.hashCode());
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((mini == null) ? 0 : mini.hashCode());
		result = prime * result + ((nut == null) ? 0 : nut.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result
				+ ((roundedValue == null) ? 0 : roundedValue.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result
				+ ((valuePerServing == null) ? 0 : valuePerServing.hashCode());
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
		if (depthLevel == null) {
			if (other.depthLevel != null)
				return false;
		} else if (!depthLevel.equals(other.depthLevel))
			return false;
		if (errorLog == null) {
			if (other.errorLog != null)
				return false;
		} else if (!errorLog.equals(other.errorLog))
			return false;
		if (formulatedValue == null) {
			if (other.formulatedValue != null)
				return false;
		} else if (!formulatedValue.equals(other.formulatedValue))
			return false;
		if (gdaPerc == null) {
			if (other.gdaPerc != null)
				return false;
		} else if (!gdaPerc.equals(other.gdaPerc))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (isFormulated == null) {
			if (other.isFormulated != null)
				return false;
		} else if (!isFormulated.equals(other.isFormulated))
			return false;
		if (lossPerc == null) {
			if (other.lossPerc != null)
				return false;
		} else if (!lossPerc.equals(other.lossPerc))
			return false;
		if (manualValue == null) {
			if (other.manualValue != null)
				return false;
		} else if (!manualValue.equals(other.manualValue))
			return false;
		if (maxi == null) {
			if (other.maxi != null)
				return false;
		} else if (!maxi.equals(other.maxi))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (mini == null) {
			if (other.mini != null)
				return false;
		} else if (!mini.equals(other.mini))
			return false;
		if (nut == null) {
			if (other.nut != null)
				return false;
		} else if (!nut.equals(other.nut))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (roundedValue == null) {
			if (other.roundedValue != null)
				return false;
		} else if (!roundedValue.equals(other.roundedValue))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		if (valuePerServing == null) {
			if (other.valuePerServing != null)
				return false;
		} else if (!valuePerServing.equals(other.valuePerServing))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NutListDataItem [manualValue=" + manualValue
				+ ", formulatedValue=" + formulatedValue + ", unit=" + unit
				+ ", mini=" + mini + ", maxi=" + maxi + ", valuePerServing="
				+ valuePerServing + ", gdaPerc=" + gdaPerc + ", lossPerc="
				+ lossPerc + ", group=" + group + ", method=" + method
				+ ", nut=" + nut + ", isFormulated=" + isFormulated
				+ ", errorLog=" + errorLog + ", depthLevel=" + depthLevel
				+ ", parent=" + parent + ", roundedValue=" + roundedValue + "]";
	}
	

	
	

	

	

	


}
