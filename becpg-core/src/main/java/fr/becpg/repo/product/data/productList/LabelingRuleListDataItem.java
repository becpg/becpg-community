package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfEnforced;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;

@AlfType
@AlfQname(qname = "bcpg:labelingRuleList")
public class LabelingRuleListDataItem extends AbstractManualDataItem {
	
	private String formula;
	private MLText label;
	private LabelingRuleType labelingRuleType;
	private List<NodeRef> components = new ArrayList<NodeRef>();
	private List<NodeRef> replacements = new ArrayList<NodeRef>();
	private Boolean isActive = true;
	
	
	
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:lrLabel")
	public MLText getLabel() {
		return label;
	}

	public void setLabel(MLText label) {
		this.label = label;
	}
	
	
	@AlfProp
	@AlfEnforced
	@AlfQname(qname="bcpg:lrIsActive")
	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	@AlfProp
	@AlfQname(qname="bcpg:lrFormula")
	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:lrComponents")
	public List<NodeRef> getComponents() {
		return components;
	}

	public void setComponents(List<NodeRef> components) {
		this.components = components;
	}

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:lrReplacements")
	public List<NodeRef> getReplacements() {
		return replacements;
	}

	public void setReplacements(List<NodeRef> replacements) {
		this.replacements = replacements;
	}

	@AlfProp
	@AlfQname(qname="bcpg:lrType")
	public LabelingRuleType getLabelingRuleType() {
		return labelingRuleType;
	}

	public void setLabelingRuleType(LabelingRuleType labelingRuleType) {
		this.labelingRuleType = labelingRuleType;
	}

	

	public LabelingRuleListDataItem() {
		super();
	}

	public LabelingRuleListDataItem(String name, String formula, LabelingRuleType labelingRuleType) {
		super();
		this.name = name;
		this.formula = formula;
		this.labelingRuleType = labelingRuleType;
	}

	
	
	
	
	public LabelingRuleListDataItem(String name, String formula, LabelingRuleType labelingRuleType, List<NodeRef> components, List<NodeRef> replacements) {
		super();
		this.name = name;
		this.formula = formula;
		this.labelingRuleType = labelingRuleType;
		this.components = components;
		this.replacements = replacements;
	}

	public LabelingRuleListDataItem(String name, MLText label ,String formula, LabelingRuleType labelingRuleType, List<NodeRef> components, List<NodeRef> replacements) {
		super();
		this.name = name;
		this.label = label;
		this.formula = formula;
		this.labelingRuleType = labelingRuleType;
		this.components = components;
		this.replacements = replacements;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((components == null) ? 0 : components.hashCode());
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((labelingRuleType == null) ? 0 : labelingRuleType.hashCode());
		result = prime * result + ((replacements == null) ? 0 : replacements.hashCode());
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
		LabelingRuleListDataItem other = (LabelingRuleListDataItem) obj;
		if (components == null) {
			if (other.components != null)
				return false;
		} else if (!components.equals(other.components))
			return false;
		if (formula == null) {
			if (other.formula != null)
				return false;
		} else if (!formula.equals(other.formula))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (labelingRuleType != other.labelingRuleType)
			return false;
		if (replacements == null) {
			if (other.replacements != null)
				return false;
		} else if (!replacements.equals(other.replacements))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LabelingRuleListDataItem [formula=" + formula + ", label=" + label + ", labelingRuleType=" + labelingRuleType + ", components=" + components + ", replacements="
				+ replacements + "]";
	}

	
	

}
