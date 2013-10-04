package fr.becpg.repo.product.data.spel;

import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import fr.becpg.repo.product.data.ing.AbstractLabelingComponent;
import fr.becpg.repo.product.data.ing.CompositeLabeling;
import fr.becpg.repo.product.data.ing.DeclarationFilter;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.LabelingRuleType;
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

	private String defaultFormat = "{0}";

	// Exemple <b>{1}</b> : {2}
	public boolean formatText(List<NodeRef> components, String textFormat) {
		if (components != null && !components.isEmpty()) {
			for (NodeRef component : components) {
				textFormaters.put(component, textFormat);
			}
		} else {
			defaultFormat = textFormat;
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
				return new MessageFormat("<b>{0} ({1,number,0.#%}):</b> {2}");
			}
			return new MessageFormat("{0} {1,number,0.#%} ({2})");
		} else if (lblComponent instanceof IngTypeItem) {
			return new MessageFormat("{0}: {1}");
		} else if (lblComponent instanceof IngItem && ((IngItem) lblComponent).getSubIngs().size() > 0) {
			return new MessageFormat("{0} ({2})");
		}

		return new MessageFormat(defaultFormat);
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

		MLText label;
		NodeRef replacement;
		Double qty = 100d;
		LabelingRuleType labelingRuleType;
		List<NodeRef> components;

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
			return replacement != null ? replacement : new NodeRef(RepoConsts.SPACES_STORE, "aggr-" + label.getDefaultValue().hashCode());
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

	}

	private Map<NodeRef, AggregateRule> aggregateRules = new HashMap<>();

	public Map<NodeRef, AggregateRule> getAggregateRules() {
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

	public boolean addRule(List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula, LabelingRuleType labeLabelingRuleType) {

		if (LabelingRuleType.Type.equals(labeLabelingRuleType)
				|| ((components != null && components.size() > 1) || (replacement != null && !replacement.isEmpty()))
				&& (LabelingRuleType.Detail.equals(labeLabelingRuleType) || LabelingRuleType.Group.equals(labeLabelingRuleType) || LabelingRuleType.DoNotDetails
						.equals(labeLabelingRuleType))) {
			aggregate(components, replacement, label, formula, labeLabelingRuleType);
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

	private void aggregate(List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula, LabelingRuleType labelingRuleType) {
		String[] qtys = formula != null && !formula.isEmpty() ? formula.split(",") : null;

		// components peut être ING, SF ou MP
		int i = 0;
		for (NodeRef component : components) {
			AggregateRule aggregateRule = new AggregateRule();

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
			aggregateRules.put(component, aggregateRule);
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
						subComponent.setQty(qtyPerc);
						merged.add(subComponent);
					} else {
						if (qtyPerc != null) {
							toMerged.setQty(toMerged.getQty() + qtyPerc);
						}
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

		for (AbstractLabelingComponent component : lblCompositeContext.getIngList().values()) {

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
		for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> kv : getSortedIngListByType(compositeLabeling).entrySet()) {
			if (ret.length() > 0) {
				ret.append(RepoConsts.LABEL_SEPARATOR);
			}

			if (kv.getKey() != null && getIngName(kv.getKey()) != null) {
				ret.append(getIngTextFormat(kv.getKey()).format(new Object[] { getIngName(kv.getKey()), renderLabelingComponent(compositeLabeling, kv.getValue()) }));
			} else {
				ret.append(renderLabelingComponent(compositeLabeling, kv.getValue()));
			}
		}
		return cleanLabel(ret);
	}

	private String cleanLabel(StringBuffer buffer) {
		return buffer.toString().replaceAll("null|\\(null\\)|\\(\\)", "").trim();
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

				ret.append(getIngTextFormat(component).format(new Object[] { ingName, qtyPerc, subIngBuff.toString() }));

			} else if (component instanceof CompositeLabeling) {
				ret.append(getIngTextFormat(component).format(new Object[] { ingName, qtyPerc, renderCompositeIng((CompositeLabeling) component) }));

			} else {
				logger.error("Unsupported ing type. Name: " + component.getName());
			}

		}

		return ret;

	}

	Map<IngTypeItem, List<AbstractLabelingComponent>> getSortedIngListByType(CompositeLabeling compositeLabeling) {

		Map<IngTypeItem, List<AbstractLabelingComponent>> tmp = new LinkedHashMap<IngTypeItem, List<AbstractLabelingComponent>>();

		for (AbstractLabelingComponent lblComponent : compositeLabeling.getIngList().values()) {
			IngTypeItem ingType = null;
			if (lblComponent instanceof IngItem) {
				ingType = ((IngItem) lblComponent).getIngType();

				if (aggregateRules.containsKey(lblComponent.getNodeRef())) {
					AggregateRule aggregateRule = aggregateRules.get(lblComponent.getNodeRef());
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

				if (ingType != null) {

					// Type replacement
					if (aggregateRules.containsKey(ingType.getNodeRef())) {
						AggregateRule aggregateRule = aggregateRules.get(ingType.getNodeRef());
						if (LabelingRuleType.Type.equals(aggregateRule.getLabelingRuleType())) {
							if (aggregateRule.getReplacement() != null) {
								ingType = (IngTypeItem) alfrescoRepository.findOne(aggregateRule.getReplacement());
							} else {
								ingType = new IngTypeItem();
								ingType.setLegalName(aggregateRule.getLabel());
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

		/*
		 * Sort by qty, default is always first
		 */
		List<Map.Entry<IngTypeItem, List<AbstractLabelingComponent>>> entries = new ArrayList<>(tmp.entrySet());
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
		Map<IngTypeItem, List<AbstractLabelingComponent>> sortedIngListByType = new LinkedHashMap<IngTypeItem, List<AbstractLabelingComponent>>();
		for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> entry : entries) {

			Collections.sort(entry.getValue());
			sortedIngListByType.put(entry.getKey(), entry.getValue());
		}

		return sortedIngListByType;

	}

	public boolean matchFormule(String formula, DeclarationFilterContext declarationFilterContext) {
		if (formula != null && !formula.isEmpty()) {
		    if(logger.isDebugEnabled()){
		    	logger.debug("Test Match formula :"+formula);
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
