package fr.becpg.repo.product.formulation.labeling;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.ing.AbstractLabelingComponent;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

/*
 * AGGREGATE
 */
public class AggregateRule {

	final String name;
	MLText label;
	NodeRef replacement;
	NodeRef ruleNodeRef;
	Double qty = 100d;
	LabelingRuleType labelingRuleType;
	List<NodeRef> components;
	Set<Locale> locales = new HashSet<>();
	NodeService mlNodeService;

	public AggregateRule(NodeService mlNodeService, NodeRef ruleNodeRef, String name, List<String> locales) {
		super();
		this.name = name;
		this.mlNodeService = mlNodeService; 
		this.ruleNodeRef = ruleNodeRef;
		if (locales != null) {
			for (String tmp : locales) {
				this.locales.add(MLTextHelper.parseLocale(tmp));
			}
		}

	}

	public String getName() {
		return name;
	}

	public NodeRef getReplacement() {
		return replacement;
	}

	public void setReplacement(NodeRef replacement) {
		this.replacement = replacement;
	}

	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	public MLText getLabel() {
		MLText mlText = null;
		if (replacement != null) {
			mlText = (MLText) mlNodeService.getProperty(replacement, BeCPGModel.PROP_LEGAL_NAME);
		} else if (label != null) {
			mlText = label;
		}
		return mlText;
	}

	public void setLabel(MLText label) {
		this.label = label;
	}

	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

	public LabelingRuleType getLabelingRuleType() {
		return labelingRuleType;
	}

	public void setLabelingRuleType(LabelingRuleType labelingRuleType) {
		this.labelingRuleType = labelingRuleType;
	}

	public void setComponents(List<NodeRef> components) {
		this.components = components;
	}

	public NodeRef getKey() {
		return replacement != null ? replacement
				: ruleNodeRef != null ? ruleNodeRef : new NodeRef(RepoConsts.SPACES_STORE, "aggr-" + name.hashCode());
	}

	public boolean matchAll(Collection<AbstractLabelingComponent> values, boolean recur) {
		// #2352
		if (!recur) {
			for (AbstractLabelingComponent abstractLabelingComponent : values) {
				if (getKey().equals(abstractLabelingComponent.getNodeRef())) {
					return true;
				}
			}
		}

		int matchCount = components.size();
		for (AbstractLabelingComponent abstractLabelingComponent : values) {
			if (components.contains(abstractLabelingComponent.getNodeRef())) {
				matchCount--;
			}
		}

		return matchCount == 0;
	}

	public boolean matchAll(List<Composite<CompoListDataItem>> values) {
		int matchCount = components.size();
		for (Composite<CompoListDataItem> component : values) {
			if (components.contains(component.getData().getProduct())) {
				matchCount--;
			}
		}

		return matchCount == 0;
	}

	@Override
	public String toString() {
		return "AggregateRule [name=" + name + ", labelingRuleType=" + labelingRuleType + "]";
	}

}
