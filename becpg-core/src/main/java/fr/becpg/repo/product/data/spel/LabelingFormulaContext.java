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
				return new MessageFormat("<b>{0} ({1,number,0.#%}): </b>");
			}
			return new MessageFormat("{0} {1,number,0.#%} ({2})");
		} else if (lblComponent instanceof IngTypeItem) {
			return new MessageFormat("{0}: {1}");
		}

		return new MessageFormat("{0}");
	}

	/*
	 * RENAME
	 */

	private Map<NodeRef, MLText> renameRules = new HashMap<>();

	public boolean rename(List<NodeRef> components, List<NodeRef> replacement, String label) {
		for (NodeRef component : components) {
			MLText mlText = null;
			if (replacement != null && !replacement.isEmpty()) {
				// TODO several replacement
				mlText = (MLText) mlNodeService.getProperty(replacement.get(0), BeCPGModel.PROP_LEGAL_NAME);
			} else if (label != null) {
				mlText = new MLText();
				for (Locale locale : getLocales()) {
					String val = I18NUtil.getMessage(label, locale);
					if(val == null){
						if(logger.isDebugEnabled()){
							logger.debug("I18 not found for key "+label+" locale "+locale.toString());
						}
						val = label;
					}
					
					mlText.addValue(locale,val);
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

		// if (ingName == null || ingName.length() < 0) {
		// // TODO exigence non respecté ?
		// logger.warn("Ing '" + ing.getIng() +
		// "' doesn't have a value for this locale '" + I18NUtil.getLocale() +
		// "'.");
		// }

		return lblComponent.getLegalName(I18NUtil.getLocale());
	}

	/*
	 * COMBINE
	 */

	public void combine(List<NodeRef> components, List<NodeRef> replacement, String label) {
		// components peut être ING, SF ou MP

		
	}

	/*
	 * AGGREGATE
	 */
	
	
	// Combiner --> nouveau groupe
	// Aggréger -- aggrege (quel niveau ?) (remplacement au même niveau ) fusionne

	private Map<NodeRef, NodeRef> replacements = new HashMap<>();
	private Map<NodeRef, String> aggregateGroups = new HashMap<>();


	public NodeRef getPossibleReplacement(NodeRef nodeRef) {
		if(replacements.containsKey(nodeRef)){
			return replacements.get(nodeRef);
		}
		return nodeRef;
	}
	
	public void aggregate(List<NodeRef> components, List<NodeRef> replacement, String groupId) {
		// components peut être ING, SF ou MP
		for (NodeRef component : components) {
			if (replacement != null && !replacement.isEmpty()) {
				// TODO several replacement
				replacements.put(component, replacement.get(0));
			} else {
				aggregateGroups.put(component, groupId);
			}
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
		StringBuffer ret = new StringBuffer();

		if (logger.isDebugEnabled()) {
			logger.debug(" Render label (showGroup:" + showGroup + "): ");
		}

		if (showGroup) {
			
				ret.append(renderCompositeIng(lblCompositeContext));

		} else {
			//TODO
//			CompositeLabeling merged = new CompositeLabeling();
//			for (CompositeLabeling compositeLabeling : compositeLabelings) {
//				merged.getIngList().putAll(compositeLabeling.getIngList());
//				merged.setQtyRMUsed(100d);
//			}
//			ret.append(renderCompositeIng(merged));
		}

		return ret.toString();
	}

	public String renderGroupList() {
		StringBuffer ret = new StringBuffer();

		if (logger.isDebugEnabled()) {
			logger.debug(" Render Group list ");
		}

		for (AbstractLabelingComponent component : lblCompositeContext.getIngList().values()) {
			
			if(component instanceof CompositeLabeling && ((CompositeLabeling) component).isGroup()){
					if (ret.length() > 0) {
						ret.append(RepoConsts.LABEL_SEPARATOR);
					}
					ret.append(getIngTextFormat(component).format(new Object[] { getIngName(component), component.getQty() }));
			}
		}

		return ret.toString().replaceAll(":", "");
	}
//
//	public String renderGroup(NodeRef groupNodeRef) {
//		CompositeLabeling compositeLabeling = findGroup(groupNodeRef);
//		if (compositeLabeling != null) {
//			return renderCompositeIng(compositeLabeling);
//		}
//		return "";
//	}
//
//	private CompositeLabeling findGroup(NodeRef groupNodeRef) {
//		for (CompositeLabeling compositeLabeling : compositeLabelings) {
//			if (groupNodeRef.equals(compositeLabeling.getNodeRef())) {
//				return compositeLabeling;
//			}
//		}
//		return null;
//	}

	private String renderCompositeIng(CompositeLabeling compositeLabeling) {
		StringBuffer ret = new StringBuffer();

		if (compositeLabeling.isGroup() && compositeLabeling.getNodeRef() != null) {
			if (ret.length() > 0) {
				ret.append("<br/>");
			}
			ret.append(getIngTextFormat(compositeLabeling).format(new Object[] { getIngName(compositeLabeling), compositeLabeling.getQty() }));
		}

		boolean first = true;
		for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> kv : getSortedIngListByType(compositeLabeling).entrySet()) {

			StringBuffer subList = new StringBuffer();

			for (AbstractLabelingComponent ing : kv.getValue()) {

				Double qtyPerc = ing.getQty();
				if (compositeLabeling.getQtyRMUsed() > 0) {
					qtyPerc = qtyPerc / compositeLabeling.getQtyRMUsed();
				}

				String ingName = getIngName(ing);

				if (logger.isDebugEnabled()) {
					logger.debug(" --" + ingName + " qtyRMUsed: " + compositeLabeling.getQtyRMUsed() + " qtyPerc " + qtyPerc);
				}

				if (subList.length() > 0) {
					subList.append(RepoConsts.LABEL_SEPARATOR);
				}

				if (ing instanceof IngItem) {
					subList.append(getIngTextFormat(ing).format(new Object[] { ingName, qtyPerc }));
				} else if (ing instanceof CompositeLabeling) {
					subList.append(getIngTextFormat(ing).format(new Object[] { ingName, qtyPerc, renderCompositeIng((CompositeLabeling) ing) }));
				} else {
					logger.error("Unsupported ing type. Name: " + ing.getName());
				}

			}

			if (!first) {
				ret.append(RepoConsts.LABEL_SEPARATOR);
			}

			if (kv.getKey() != null && !IngTypeItem.DEFAULT.equals(kv.getKey())) {

				if (logger.isDebugEnabled()) {
					logger.debug("      - append sub label [" + subList.toString() + "] for type : " + kv.getKey());
				}

				ret.append(getIngTextFormat(kv.getKey()).format(new Object[] { getIngName(kv.getKey()), subList.toString() }));

			} else {
				ret.append(subList);
			}
			first = false;
		}

		return ret.toString();
	}

	Map<IngTypeItem, List<AbstractLabelingComponent>> getSortedIngListByType(CompositeLabeling compositeLabeling) {

		Map<IngTypeItem, List<AbstractLabelingComponent>> tmp = new HashMap<IngTypeItem, List<AbstractLabelingComponent>>();

		for (AbstractLabelingComponent lblComponent : compositeLabeling.getIngList().values()) {
			IngTypeItem ingType = null;
			if (lblComponent instanceof IngItem) {
				ingType = ((IngItem) lblComponent).getIngType();
				if (ingType != null) {
					// First aggregate
					if (replacements.containsKey(ingType.getNodeRef())) {
						ingType = (IngTypeItem) alfrescoRepository.findOne(replacements.get(ingType.getNodeRef()));
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

				if(IngTypeItem.DEFAULT.equals(a.getKey())){
					return 1;
				}
				
				if(IngTypeItem.DEFAULT.equals(b.getKey())){
					return 2;
				}
				
				
				return getQty(a.getValue()).compareTo(getQty(b.getValue()));
			}

			private Double getQty(List<AbstractLabelingComponent> lblComponents) {
				Double ret = 0d;
				 for(AbstractLabelingComponent lblComponent : lblComponents) {
					 ret+=lblComponent.getQty();
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
		return "LabelingFormulaContext [compositeLabeling=" + lblCompositeContext + ", textFormaters=" + textFormaters + ", renameRules=" + renameRules + ", nodeDeclarationFilters="
				+ nodeDeclarationFilters + ", declarationFilters=" + declarationFilters + "]";
	}


}
