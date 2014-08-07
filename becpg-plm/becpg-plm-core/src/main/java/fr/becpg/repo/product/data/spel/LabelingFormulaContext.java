/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.data.spel;

import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.product.data.ing.AbstractLabelingComponent;
import fr.becpg.repo.product.data.ing.CompositeLabeling;
import fr.becpg.repo.product.data.ing.DeclarationFilter;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.LabelingRuleType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * 
 * @author matthieu
 * 
 */
public class LabelingFormulaContext {

	private static Log logger = LogFactory.getLog(LabelingFormulaContext.class);

	private CompositeLabeling lblCompositeContext;

	private Set<Locale> availableLocales;

	private NodeService mlNodeService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private List<ReqCtrlListDataItem> errors = new ArrayList<>();

	public List<ReqCtrlListDataItem> getErrors() {
		return errors;
	}

	public void setErrors(List<ReqCtrlListDataItem> errors) {
		this.errors = errors;
	}

	public CompositeLabeling getCompositeLabeling() {
		return lblCompositeContext;
	}

	public void setCompositeLabeling(CompositeLabeling compositeLabeling) {
		this.lblCompositeContext = compositeLabeling;
	}


	public LabelingFormulaContext(NodeService mlNodeService, AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		super();
		this.mlNodeService = mlNodeService;
		this.alfrescoRepository = alfrescoRepository;
		availableLocales = new HashSet<>();
		availableLocales.add(new Locale(Locale.getDefault().getLanguage()));
	}

	/*
	 * LOCALE
	 */

	public void addLocale(String value) {
		String[] locales = value.split(",");
		for (String tmp : locales) {
			availableLocales.add(new Locale(tmp));
		}
	}

	public Set<Locale> getLocales() {
		return availableLocales;
	}

	/*
	 * FORMAT
	 */

	private Map<NodeRef, String> textFormaters = new HashMap<>();

	private String ingDefaultFormat = "{0}";
	private String groupDefaultFormat = "<b>{0} ({1,number,0.#%}):</b> {2}";
	private String detailsDefaultFormat = "{0} ({2})";
	private String ingTypeDefaultFormat = "{0}: {2}";
	private String subIngsDefaultFormat = "{0} ({2})";
	private boolean useVolume = false;
	
	public void setUseVolume(boolean useVolume) {
		this.useVolume = useVolume;
	}

	public void setIngDefaultFormat(String ingDefaultFormat) {
		this.ingDefaultFormat = ingDefaultFormat;
	}

	public void setGroupDefaultFormat(String groupDefaultFormat) {
		this.groupDefaultFormat = groupDefaultFormat;
	}

	public void setDetailsDefaultFormat(String detailsDefaultFormat) {
		this.detailsDefaultFormat = detailsDefaultFormat;
	}

	public void setIngTypeDefaultFormat(String ingTypeDefaultFormat) {
		this.ingTypeDefaultFormat = ingTypeDefaultFormat;
	}

	public void setSubIngsDefaultFormat(String subIngsDefaultFormat) {
		this.subIngsDefaultFormat = subIngsDefaultFormat;
	}

	// Exemple <b>{1}</b> : {2}
	public boolean formatText(List<NodeRef> components, String textFormat) {
		if (components != null && !components.isEmpty()) {
			for (NodeRef component : components) {
				textFormaters.put(component, textFormat);
			}
		} else {
			ingDefaultFormat = textFormat;
		}
		return true;
	}

	/* formaters */

	private Format getIngTextFormat(AbstractLabelingComponent lblComponent) {
		if (textFormaters.containsKey(lblComponent.getNodeRef())) {
			return new MessageFormat(textFormaters.get(lblComponent.getNodeRef()));
		}

		if (lblComponent instanceof CompositeLabeling) {
			if (((CompositeLabeling) lblComponent).isGroup()) {
				return new MessageFormat(groupDefaultFormat);
			}
			return new MessageFormat(detailsDefaultFormat);
		} else if (lblComponent instanceof IngTypeItem) {
			return new MessageFormat(ingTypeDefaultFormat);
		} else if (lblComponent instanceof IngItem && ((IngItem) lblComponent).getSubIngs().size() > 0) {
			return new MessageFormat(subIngsDefaultFormat);
		}
		return new MessageFormat(ingDefaultFormat);
	}

	/*
	 * RENAME
	 */

	private Map<NodeRef, MLText> renameRules = new HashMap<>();

	public boolean rename(List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula) {
		for (NodeRef component : components) {
			MLText mlText = null;
			if (replacement != null && !replacement.isEmpty()) {
				mlText = (MLText) mlNodeService.getProperty(replacement.get(0), BeCPGModel.PROP_LEGAL_NAME);
			} else if (label != null) {
				mlText = label;
			} else if (formula != null) {
				mlText = new MLText();
				for (Locale locale : getLocales()) {
					String val = I18NUtil.getMessage(formula, locale);
					if (val == null) {
						if (logger.isDebugEnabled()) {
							logger.debug("I18 not found for key " + label + " locale " + locale.toString());
						}
						val = formula;
					}
					mlText.addValue(locale, val);
				}
			}
			if (mlText != null) {
				renameRules.put(component, mlText);
			}
		}
		return true;
	}

	private String getIngName(AbstractLabelingComponent lblComponent) {
		if (renameRules.containsKey(lblComponent.getNodeRef())) {
			return renameRules.get(lblComponent.getNodeRef()).getValue(I18NUtil.getLocale());
		}
		return lblComponent.getLegalName(I18NUtil.getLocale());
	}

	/*
	 * AGGREGATE
	 */

	public class AggregateRule {

		String name;
		MLText label;
		NodeRef replacement;
		NodeRef ruleNodeRef;
		Double qty = 100d;
		LabelingRuleType labelingRuleType;
		List<NodeRef> components;

		public AggregateRule(NodeRef ruleNodeRef, String name) {
			super();
			this.name = name;
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
			return replacement != null ? replacement : ruleNodeRef!= null ? ruleNodeRef : new NodeRef(RepoConsts.SPACES_STORE, "aggr-" + name.hashCode());
		}

		public boolean matchAll(Collection<AbstractLabelingComponent> values) {
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

	}

	private Map<NodeRef, List<AggregateRule>> aggregateRules = new HashMap<>();

	public Map<NodeRef, List<AggregateRule>> getAggregateRules() {
		return aggregateRules;
	}

	/*
	 * DECLARE
	 */

	private Map<NodeRef, DeclarationFilter> nodeDeclarationFilters = new HashMap<>();
	private List<DeclarationFilter> declarationFilters = new ArrayList<>();

	public Map<NodeRef, DeclarationFilter> getNodeDeclarationFilters() {
		return nodeDeclarationFilters;
	}

	public List<DeclarationFilter> getDeclarationFilters() {
		return declarationFilters;
	}

	public boolean addRule(NodeRef ruleNodeRef, String name, List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula, LabelingRuleType labeLabelingRuleType) {

		if (LabelingRuleType.Type.equals(labeLabelingRuleType)
				|| ((components != null && components.size() > 1) || (replacement != null && !replacement.isEmpty()))
				&& (LabelingRuleType.Detail.equals(labeLabelingRuleType) || LabelingRuleType.Group.equals(labeLabelingRuleType) || LabelingRuleType.DoNotDetails
						.equals(labeLabelingRuleType))) {
			aggregate(ruleNodeRef, name, components, replacement, label, formula, labeLabelingRuleType);
		} else {
			if (components != null && !components.isEmpty()) {
				for (NodeRef component : components) {
					nodeDeclarationFilters.put(component, new DeclarationFilter(formula, DeclarationType.valueOf(labeLabelingRuleType.toString())));
				}
			} else {
				declarationFilters.add(new DeclarationFilter(formula, DeclarationType.valueOf(labeLabelingRuleType.toString())));
			}

		}

		return true;
	}

	private void aggregate(NodeRef ruleNodeRef, String name, List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula, LabelingRuleType labelingRuleType) {
		String[] qtys = formula != null && !formula.isEmpty() ? formula.split(",") : null;

		// components peut être ING, SF ou MP
		int i = 0;
		for (NodeRef component : components) {
			AggregateRule aggregateRule = new AggregateRule(ruleNodeRef,name);

			if (replacement != null && !replacement.isEmpty()) {
				aggregateRule.setReplacement(replacement.get(0));
			}

			if (label != null && !label.isEmpty()) {
				aggregateRule.setLabel(label);
			}

			if (qtys != null && qtys.length > i) {
				try {
					aggregateRule.setQty(Double.valueOf(qtys[i]));
				} catch (NumberFormatException e) {
					logger.error(e, e);
				}
			}
			aggregateRule.setComponents(components);
			aggregateRule.setLabelingRuleType(labelingRuleType);

			i++;

			if (aggregateRules.containsKey(component)) {
				aggregateRules.get(component).add(aggregateRule);
			} else {
				aggregateRules.put(component, new LinkedList<>(Arrays.asList(aggregateRule)));
			}

		}
	}

	public String render() {
		return render(true);
	}

	public String render(boolean showGroup) {

		if (logger.isDebugEnabled()) {
			logger.debug(" Render label (showGroup:" + showGroup + "): ");
		}

		if (showGroup) {
			return renderCompositeIng(lblCompositeContext);
		}

		CompositeLabeling merged = new CompositeLabeling();
		merged.setQtyRMUsed(lblCompositeContext.getQtyRMUsed());
		for (AbstractLabelingComponent component : lblCompositeContext.getIngList().values()) {
			if (isGroup(component)) {
				CompositeLabeling compositeLabeling = (CompositeLabeling) component;
				for (AbstractLabelingComponent subComponent : compositeLabeling.getIngList().values()) {
					AbstractLabelingComponent toMerged = merged.get(subComponent.getNodeRef());
					Double qtyPerc = computeQty(compositeLabeling, subComponent);
					if (compositeLabeling.getQty() != null && qtyPerc != null) {
						qtyPerc = qtyPerc * compositeLabeling.getQty();
					}

					if (toMerged == null) {
						AbstractLabelingComponent clonedSubComponent = null;
						if (subComponent instanceof CompositeLabeling) {
							clonedSubComponent = new CompositeLabeling((CompositeLabeling) subComponent);
						} else {
							clonedSubComponent = new IngItem((IngItem) subComponent);
						}
						clonedSubComponent.setQty(qtyPerc);
						merged.add(clonedSubComponent);
					} else {
						if (qtyPerc != null && toMerged.getQty() != null) {
							toMerged.setQty(toMerged.getQty() + qtyPerc);
						}
						// TODO else add warning
					}
				}
			} else {
				merged.add(component);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Merge labeling :" + merged.toString());
		}

		return renderCompositeIng(merged);

	}

	private Double computeQty(CompositeLabeling parent, AbstractLabelingComponent component) {
		Double qtyPerc = component.getQty();
		if (parent.getQtyRMUsed() != null && parent.getQtyRMUsed() > 0 && qtyPerc != null) {
			qtyPerc = qtyPerc / parent.getQtyRMUsed();
		}
		return qtyPerc;
	}

	public String renderGroupList() {
		StringBuffer ret = new StringBuffer();

		if (logger.isDebugEnabled()) {
			logger.debug(" Render Group list ");
		}

		List<AbstractLabelingComponent> components = new LinkedList<>(lblCompositeContext.getIngList().values());
		Collections.sort(components);

		for (AbstractLabelingComponent component : components) {

			String ingName = getIngName(component);

			Double qtyPerc = computeQty(lblCompositeContext, component);

			if (isGroup(component)) {
				if (ret.length() > 0) {
					ret.append(RepoConsts.LABEL_SEPARATOR);
				}
				ret.append(new MessageFormat("<b>{0} {1,number,0.#%}</b>").format(new Object[] { ingName, qtyPerc }));
			}
		}

		return cleanLabel(ret);
	}

	private boolean isGroup(AbstractLabelingComponent component) {
		return component instanceof CompositeLabeling && ((CompositeLabeling) component).isGroup();
	}

	private String renderCompositeIng(CompositeLabeling compositeLabeling) {
		StringBuffer ret = new StringBuffer();
		boolean appendEOF = false;
		for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> kv : getSortedIngListByType(compositeLabeling).entrySet()) {
			if (ret.length() > 0) {
				if (appendEOF) {
					ret.append("<br/>");
				} else {
					ret.append(RepoConsts.LABEL_SEPARATOR);
				}
			}

			if (IngTypeItem.DEFAULT_GROUP.equals(kv.getKey())) {
				appendEOF = true;
			} else {
				appendEOF = false;
			}

			if (kv.getKey() != null && getIngName(kv.getKey()) != null) {
				ret.append(getIngTextFormat(kv.getKey()).format(new Object[] { getIngName(kv.getKey()),null, renderLabelingComponent(compositeLabeling, kv.getValue()) }));
			} else {
				ret.append(renderLabelingComponent(compositeLabeling, kv.getValue()));
			}
		}
		return cleanLabel(ret);
	}

	private String cleanLabel(StringBuffer buffer) {
		return buffer.toString().replaceAll(" null| \\(null\\)| \\(\\)", "").trim();
	}

	private StringBuffer renderLabelingComponent(CompositeLabeling parent, List<AbstractLabelingComponent> subComponents) {

		StringBuffer ret = new StringBuffer();

		boolean appendEOF = false;
		for (AbstractLabelingComponent component : subComponents) {

			Double qtyPerc = computeQty(parent, component);

			String ingName = getIngName(component);

			if (logger.isDebugEnabled()) {
				logger.debug(" --" + ingName + " qtyRMUsed: " + parent.getQtyRMUsed() + " qtyPerc " + qtyPerc);
			}

			if (ret.length() > 0) {
				if (appendEOF) {
					ret.append("<br/>");
				} else {
					ret.append(RepoConsts.LABEL_SEPARATOR);
				}
			}

			if (isGroup(component)) {
				appendEOF = true;
			} else {
				appendEOF = false;
			}

			if (component instanceof IngItem) {
				IngItem ingItem = (IngItem) component;
				StringBuffer subIngBuff = new StringBuffer();
				for (IngItem subIngItem : ingItem.getSubIngs()) {
					if (subIngBuff.length() > 0) {
						subIngBuff.append(RepoConsts.LABEL_SEPARATOR);
					}
					subIngBuff.append(getIngName(subIngItem));
				}

				ret.append(getIngTextFormat(component).format(new Object[] { ingName, useVolume? ingItem.getVolumeQtyPerc() :  qtyPerc, subIngBuff.toString() }));

			} else if (component instanceof CompositeLabeling) {
				ret.append(getIngTextFormat(component).format(new Object[] { ingName, useVolume? component.getVolumeQtyPerc() :  qtyPerc, renderCompositeIng((CompositeLabeling) component)  }));

			} else {
				logger.error("Unsupported ing type. Name: " + component.getName());
			}

		}

		return ret;
	}


	Map<IngTypeItem, List<AbstractLabelingComponent>> getSortedIngListByType(CompositeLabeling compositeLabeling) {

		Map<IngTypeItem, List<AbstractLabelingComponent>> tmp = new LinkedHashMap<IngTypeItem, List<AbstractLabelingComponent>>();

		boolean keepOrder = false;
		for (AbstractLabelingComponent lblComponent : compositeLabeling.getIngList().values()) {
			IngTypeItem ingType = null;
			if (lblComponent instanceof IngItem) {
				ingType = ((IngItem) lblComponent).getIngType();

				if (aggregateRules.containsKey(lblComponent.getNodeRef())) {
					for (AggregateRule aggregateRule : aggregateRules.get(lblComponent.getNodeRef())) {
						if (LabelingRuleType.Type.equals(aggregateRule.getLabelingRuleType())) {
							if (aggregateRule.getReplacement() != null) {
								RepositoryEntity repositoryEntity = alfrescoRepository.findOne(aggregateRule.getReplacement());
								if (repositoryEntity instanceof IngTypeItem) {
									ingType = (IngTypeItem) repositoryEntity;
								}
							} else {
								ingType = new IngTypeItem();
								ingType.setLegalName(aggregateRule.getLabel());
							}
						}
					}

				}

				if (ingType != null) {

					// Type replacement
					if (aggregateRules.containsKey(ingType.getNodeRef())) {
						for (AggregateRule aggregateRule : aggregateRules.get(ingType.getNodeRef())) {
							if (LabelingRuleType.Type.equals(aggregateRule.getLabelingRuleType())) {
								if (aggregateRule.getReplacement() != null) {
									ingType = (IngTypeItem) alfrescoRepository.findOne(aggregateRule.getReplacement());
								} else {
									ingType = new IngTypeItem();
									ingType.setLegalName(aggregateRule.getLabel());
								}
							}
						}
						// Ing IngType replacement
					}

					// If Omit
					if (nodeDeclarationFilters.containsKey(ingType.getNodeRef())) {
						DeclarationFilter declarationFilter = nodeDeclarationFilters.get(ingType.getNodeRef());
						if (DeclarationType.Omit.equals(declarationFilter.getDeclarationType()) && matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext())) {
							break;
						} else if (DeclarationType.DoNotDeclare.equals(declarationFilter.getDeclarationType())
								&& matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext())) {
							ingType = null;
						}

					}
				}
			}

			if (lblComponent.getQty() == null) {
				keepOrder = true;
			}

			if (lblComponent instanceof CompositeLabeling && ((CompositeLabeling) lblComponent).isGroup()) {
				ingType = IngTypeItem.DEFAULT_GROUP;
			}

			if (ingType == null) {

				ingType = new IngTypeItem();
				ingType.setNodeRef(new NodeRef(RepoConsts.SPACES_STORE, "ingType-" + lblComponent.getNodeRef().hashCode()));
			}

			List<AbstractLabelingComponent> subSortedList = tmp.get(ingType);

			if (subSortedList == null) {
				subSortedList = new LinkedList<AbstractLabelingComponent>();
				tmp.put(ingType, subSortedList);
			}
			subSortedList.add(lblComponent);

		}

		keepOrder = DeclarationType.Detail.equals(compositeLabeling.getDeclarationType()) && keepOrder;

		/*
		 * Sort by qty, default is always first
		 */

		List<Map.Entry<IngTypeItem, List<AbstractLabelingComponent>>> entries = new ArrayList<>(tmp.entrySet());
		if (!keepOrder) {
			Collections.sort(entries, new Comparator<Map.Entry<IngTypeItem, List<AbstractLabelingComponent>>>() {
				public int compare(Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> a, Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> b) {

					if (IngTypeItem.DEFAULT_GROUP.equals(a.getKey())) {
						return -1;
					}

					if (IngTypeItem.DEFAULT_GROUP.equals(b.getKey())) {
						return 1;
					}

					return getQty(b.getValue()).compareTo(getQty(a.getValue()));
				}

				private Double getQty(List<AbstractLabelingComponent> lblComponents) {
					Double ret = 0d;
					for (AbstractLabelingComponent lblComponent : lblComponents) {
						if (lblComponent.getQty() != null) {
							ret += lblComponent.getQty();
						}
					}

					return ret;
				}
			});
		}
		Map<IngTypeItem, List<AbstractLabelingComponent>> sortedIngListByType = new LinkedHashMap<IngTypeItem, List<AbstractLabelingComponent>>();
		for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> entry : entries) {

			if (!keepOrder) {
				// Sort by value
				Collections.sort(entry.getValue());
			}
			sortedIngListByType.put(entry.getKey(), entry.getValue());
		}

		return sortedIngListByType;

	}

	public boolean matchFormule(String formula, DeclarationFilterContext declarationFilterContext) {
		if (formula != null && !formula.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Test Match formula :" + formula);
			}

			ExpressionParser parser = new SpelExpressionParser();
			StandardEvaluationContext dataContext = new StandardEvaluationContext(declarationFilterContext);

			Expression exp = parser.parseExpression(SpelHelper.formatFormula(formula));

			return exp.getValue(dataContext, Boolean.class);
		}
		return true;
	}

	@Override
	public String toString() {
		return "LabelingFormulaContext [compositeLabeling=" + lblCompositeContext + ", textFormaters=" + textFormaters + ", renameRules=" + renameRules
				+ ", nodeDeclarationFilters=" + nodeDeclarationFilters + ", declarationFilters=" + declarationFilters + "]";
	}

}
