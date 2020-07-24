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
import fr.becpg.repo.product.data.ing.CompositeLabeling;
import fr.becpg.repo.product.data.ing.LabelingComponent;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

/*
 * AGGREGATE
 */
/**
 * <p>AggregateRule class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
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

	/**
	 * <p>Constructor for AggregateRule.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param ruleNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 * @param locales a {@link java.util.List} object.
	 */
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

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Getter for the field <code>replacement</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getReplacement() {
		return replacement;
	}

	/**
	 * <p>Setter for the field <code>replacement</code>.</p>
	 *
	 * @param replacement a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setReplacement(NodeRef replacement) {
		this.replacement = replacement;
	}

	/**
	 * <p>matchLocale.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a boolean.
	 */
	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	/**
	 * <p>Getter for the field <code>label</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public MLText getLabel() {
		MLText mlText = null;
		if (replacement != null) {
			mlText = (MLText) mlNodeService.getProperty(replacement, BeCPGModel.PROP_LEGAL_NAME);
		} else if (label != null) {
			mlText = label;
		}
		return mlText;
	}

	/**
	 * <p>Setter for the field <code>label</code>.</p>
	 *
	 * @param label a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setLabel(MLText label) {
		this.label = label;
	}

	/**
	 * <p>Getter for the field <code>qty</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getQty() {
		return qty;
	}

	/**
	 * <p>Setter for the field <code>qty</code>.</p>
	 *
	 * @param qty a {@link java.lang.Double} object.
	 */
	public void setQty(Double qty) {
		this.qty = qty;
	}

	/**
	 * <p>Getter for the field <code>labelingRuleType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.LabelingRuleType} object.
	 */
	public LabelingRuleType getLabelingRuleType() {
		return labelingRuleType;
	}

	/**
	 * <p>Setter for the field <code>labelingRuleType</code>.</p>
	 *
	 * @param labelingRuleType a {@link fr.becpg.repo.product.data.constraints.LabelingRuleType} object.
	 */
	public void setLabelingRuleType(LabelingRuleType labelingRuleType) {
		this.labelingRuleType = labelingRuleType;
	}

	/**
	 * <p>Setter for the field <code>components</code>.</p>
	 *
	 * @param components a {@link java.util.List} object.
	 */
	public void setComponents(List<NodeRef> components) {
		this.components = components;
	}

	/**
	 * <p>getKey.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getKey() {
		return replacement != null ? replacement
				: ruleNodeRef != null ? ruleNodeRef : new NodeRef(RepoConsts.SPACES_STORE, "aggr-" + name.hashCode());
	}

	/**
	 * <p>matchAll.</p>
	 *
	 * @param values a {@link java.util.Collection} object.
	 * @param recur a boolean.
	 * @return a boolean.
	 */
	public boolean matchAll(Collection<CompositeLabeling> values, boolean recur) {
		// #2352
		if (!recur) {
			for (CompositeLabeling abstractLabelingComponent : values) {
				if (getKey().equals(abstractLabelingComponent.getNodeRef())) {
					return true;
				}
			}
		}

		int matchCount = components.size();
		for (LabelingComponent abstractLabelingComponent : values) {
			if (components.contains(abstractLabelingComponent.getNodeRef())) {
				matchCount--;
			}
		}

		return matchCount == 0;
	}

	/**
	 * <p>matchAll.</p>
	 *
	 * @param values a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public boolean matchAll(List<Composite<CompoListDataItem>> values) {
		int matchCount = components.size();
		for (Composite<CompoListDataItem> component : values) {
			if (components.contains(component.getData().getProduct())) {
				matchCount--;
			}
		}

		return matchCount == 0;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "AggregateRule [name=" + name + ", labelingRuleType=" + labelingRuleType + "]";
	}

}
