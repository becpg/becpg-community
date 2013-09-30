package fr.becpg.repo.product.data.spel;

import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
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

	public LabelingFormulaContext(NodeService mlNodeService) {
		super();
		this.mlNodeService = mlNodeService;
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

	// Exemple <b>{1}</b> : {2}
	public boolean formatText(List<NodeRef> components, String textFormat) {
		for (NodeRef component : components) {
			textFormaters.put(component, textFormat);
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

		return new MessageFormat("{0}");
	}

	/*
	 * RENAME
	 */

	private Map<NodeRef, MLText> renameRules = new HashMap<>();

	public boolean rename(List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula) {
		for (NodeRef component : components) {
			MLText mlText = null;
			if (replacement != null && !replacement.isEmpty()) {
				// TODO several replacement
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
	// {20,30}
	// Combiner --> nouveau groupe
	// Aggréger -- aggrege (quel niveau ?) (remplacement au même niveau )
	// fusionne

	public class AggregateRule {

		MLText label;
		NodeRef replacement;
		Double qty = 100d;

		public NodeRef getReplacement() {
			return replacement;
		}

		public void setReplacement(NodeRef replacement) {
			this.replacement = replacement;
		}

		public MLText getLabel() {
			return label;
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

	}

	private Map<NodeRef, AggregateRule> aggregateRules = new HashMap<>();

	public Map<NodeRef, AggregateRule> getAggregateRules() {
		return aggregateRules;
	}

	public void aggregate(List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula) {
		String[] qtys = formula != null && !formula.isEmpty() ? formula.split(",") : null;

		// components peut être ING, SF ou MP
		int i = 0;
		for (NodeRef component : components) {
			AggregateRule aggregateRule = new AggregateRule();

			if (replacement != null && !replacement.isEmpty()) {
				aggregateRule.setReplacement(replacement.get(0));
			}

			if (label != null) {
				aggregateRule.setLabel(label);
			}

			if (qtys != null && qtys.length > i) {
				try {
					aggregateRule.setQty(Double.valueOf(qtys[i]));
				} catch (NumberFormatException e) {
					logger.error(e, e);
				}
			}

			i++;
			aggregateRules.put(component, aggregateRule);
		}
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

	/*
	 * Selector : (ing.type == « gélifiant », ingListItem.value < 5 ,
	 * ingListDataItem.isProcessingAid == true) Type : Declare, Detail, Group,
	 * Omit, DoNotDeclare Scope: PF , MP
	 */
	public boolean declare(List<NodeRef> components, String selector, DeclarationType declarationType) {
		if (components != null) {
			for (NodeRef component : components) {
				nodeDeclarationFilters.put(component, new DeclarationFilter(selector, declarationType));
			}
		} else {
			declarationFilters.add(new DeclarationFilter(selector, declarationType));
		}

		return true;
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
					Double qtyPerc = (subComponent.getQty() / compositeLabeling.getQtyRMUsed()) * compositeLabeling.getQty();
					if (toMerged == null) {
						subComponent.setQty(qtyPerc);
						merged.add(subComponent);
					} else {
						toMerged.setQty(toMerged.getQty() + qtyPerc);
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

	public String renderGroupList() {
		StringBuffer ret = new StringBuffer();

		if (logger.isDebugEnabled()) {
			logger.debug(" Render Group list ");
		}

		for (AbstractLabelingComponent component : lblCompositeContext.getIngList().values()) {

			String ingName = getIngName(component);
			
			Double qtyPerc = component.getQty();
			if (lblCompositeContext.getQtyRMUsed() > 0) {
				qtyPerc = qtyPerc / lblCompositeContext.getQtyRMUsed();
			}
			if (isGroup(component)) {
				if (ret.length() > 0) {
					ret.append(RepoConsts.LABEL_SEPARATOR);
				}
				ret.append(new MessageFormat("<b>{0} {1,number,0.#%}</b>").format(new Object[] {ingName, qtyPerc }));
			}
		}

		return ret.toString();
	}

	private boolean isGroup(AbstractLabelingComponent component) {
		return component instanceof CompositeLabeling && ((CompositeLabeling) component).isGroup();
	}

	//
	// public String renderGroup(NodeRef groupNodeRef) {
	// CompositeLabeling compositeLabeling = findGroup(groupNodeRef);
	// if (compositeLabeling != null) {
	// return renderCompositeIng(compositeLabeling);
	// }
	// return "";
	// }
	//
	// private CompositeLabeling findGroup(NodeRef groupNodeRef) {
	// for (CompositeLabeling compositeLabeling : compositeLabelings) {
	// if (groupNodeRef.equals(compositeLabeling.getNodeRef())) {
	// return compositeLabeling;
	// }
	// }
	// return null;
	// }

	private String renderCompositeIng(CompositeLabeling compositeLabeling) {
		StringBuffer ret = new StringBuffer();
		for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> kv : getSortedIngListByType(compositeLabeling).entrySet()) {
			if (ret.length() > 0) {
				ret.append(RepoConsts.LABEL_SEPARATOR);
			}

			// TODO apply combine

			if (kv.getKey() != null && !IngTypeItem.DEFAULT.equals(kv.getKey())) {
				ret.append(getIngTextFormat(kv.getKey()).format(new Object[] { getIngName(kv.getKey()), renderLabelingComponent(compositeLabeling, kv.getValue()) }));
			} else {
				ret.append(renderLabelingComponent(compositeLabeling, kv.getValue()));
			}
		}
		return ret.toString();
	}

	private StringBuffer renderLabelingComponent(CompositeLabeling parent, List<AbstractLabelingComponent> subComponents) {

		StringBuffer ret = new StringBuffer();

		boolean appendEOF = false;
		for (AbstractLabelingComponent component : subComponents) {

			Double qtyPerc = component.getQty();
			if (parent.getQtyRMUsed() > 0) {
				qtyPerc = qtyPerc / parent.getQtyRMUsed();
			}

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
			
			if(isGroup(component)){
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

		Map<IngTypeItem, List<AbstractLabelingComponent>> tmp = new HashMap<IngTypeItem, List<AbstractLabelingComponent>>();

		for (AbstractLabelingComponent lblComponent : compositeLabeling.getIngList().values()) {
			IngTypeItem ingType = null;
			if (lblComponent instanceof IngItem) {
				ingType = ((IngItem) lblComponent).getIngType();
				if (ingType != null) {
					// First aggregate
					if (aggregateRules.containsKey(ingType.getNodeRef())) {
						AggregateRule aggregateRule = aggregateRules.get(ingType.getNodeRef());
						if (aggregateRule.getReplacement() != null) {
							ingType = (IngTypeItem) alfrescoRepository.findOne(aggregateRule.getReplacement());
						} else {
							ingType = new IngTypeItem();
							ingType.setLegalName(aggregateRule.getLabel());
						}
					}
					// If Omit
					if (nodeDeclarationFilters.containsKey(ingType.getNodeRef())) {
						DeclarationFilter declarationFilter = nodeDeclarationFilters.get(ingType.getNodeRef());
						if (DeclarationType.Omit.equals(declarationFilter.getDeclarationType()) && matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext())) {
							ingType = null;
						}
					}
				}
			}

			if (ingType == null) {
				ingType = IngTypeItem.DEFAULT;
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

				if (IngTypeItem.DEFAULT.equals(a.getKey())) {
					return -1;
				}

				if (IngTypeItem.DEFAULT.equals(b.getKey())) {
					return 1;
				}

				return getQty(a.getValue()).compareTo(getQty(b.getValue()));
			}

			private Double getQty(List<AbstractLabelingComponent> lblComponents) {
				Double ret = 0d;
				for (AbstractLabelingComponent lblComponent : lblComponents) {
					ret += lblComponent.getQty();
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
