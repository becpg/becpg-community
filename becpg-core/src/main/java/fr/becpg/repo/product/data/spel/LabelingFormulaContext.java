package fr.becpg.repo.product.data.spel;

import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.data.ing.AbstractLabelingComponent;
import fr.becpg.repo.product.data.ing.CompositeLabeling;
import fr.becpg.repo.product.data.ing.DeclarationFilter;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.DeclarationType;

/**
 * 
 * @author matthieu
 * 
 */
public class LabelingFormulaContext {

	private static Log logger = LogFactory.getLog(LabelingFormulaContext.class);

	private List<CompositeLabeling> compositeLabelings;

	private NodeService mlNodeService;

	public void setCompositeIngs(List<CompositeLabeling> compositeLabelings) {
		this.compositeLabelings = compositeLabelings;
	}

	public LabelingFormulaContext(NodeService mlNodeService) {
		super();
		this.mlNodeService = mlNodeService;
	}

	/*
	 * FORMAT
	 */

	// private Map<NodeRef, String> valueFormaters = new HashMap<>();
	//
	// // Exemple '#',##
	// public boolean formatValue(List<NodeRef> components, String
	// decimalFormat) {
	// for (NodeRef component : components) {
	// valueFormaters.put(component, decimalFormat);
	// }
	// return true;
	// }

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
			if (((CompositeLabeling)lblComponent).isGroup()) {
				return new MessageFormat("<b>{0} ({1,number,percent}):</b>");
			}
			return new MessageFormat("{0} {1,number,percent} ({2})");
		} else if(lblComponent instanceof IngTypeItem){
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
				// TODO MP legalName
				// TODO rename de type
				mlText = (MLText) mlNodeService.getProperty(replacement.get(0), BeCPGModel.PROP_LEGAL_NAME);
			} else if (label != null) {
				mlText = new MLText();
				for (Locale locale : getLocales()) {
					mlText.addValue(locale, I18NUtil.getMessage(label, locale));
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
		
//		if (ingName == null || ingName.length() < 0) {
//			// TODO exigence non respecté ?
//			logger.warn("Ing '" + ing.getIng() + "' doesn't have a value for this locale '" + I18NUtil.getLocale() + "'.");
//		}
		
		return lblComponent.getLegalName(I18NUtil.getLocale());
	}

	/*
	 * COMBINE
	 */

	public void combine(List<NodeRef> components, List<Double> values, String label) {
		// components peut être ING, SF ou MP
	}

	/*
	 * AGGREGATE
	 */

	public void aggregate(List<NodeRef> components, String label) {
		// components peut être ING, SF ou MP

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
	 * ingListDataItem.isProcessingAid == true) Type : Declare, Detail, Group, Omit,
	 * DoNotDeclare Scope: PF , MP
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
		
		
		if(showGroup){
			for (CompositeLabeling compositeLabeling : compositeLabelings) {
				ret.append(renderCompositeIng(compositeLabeling));
				ret.append("<br/>");
			}
		} else {
			CompositeLabeling merged  = new CompositeLabeling();
			for (CompositeLabeling compositeLabeling : compositeLabelings) {
				merged.getIngList().putAll(compositeLabeling.getIngList());
				merged.setQtyRMUsed(100d);
			}
			ret.append(renderCompositeIng(merged));
		}

		return ret.toString();
	}

	public String renderGroupList() {
		StringBuffer ret = new StringBuffer();

		if (logger.isDebugEnabled()) {
			logger.debug(" Render Group list ");
		}

		for (CompositeLabeling compositeLabeling : compositeLabelings) {
			if(compositeLabeling.getNodeRef()!=null){
				if(ret.length()>0){
					ret.append(RepoConsts.LABEL_SEPARATOR);
				}
				ret.append(getIngTextFormat(compositeLabeling).format(new Object[] { getIngName(compositeLabeling), compositeLabeling.getQty()  }));
			}
		}

		return ret.toString().replaceAll(":", "");
	}

	public String renderGroup(NodeRef groupNodeRef) {
		CompositeLabeling compositeLabeling = findGroup(groupNodeRef);
		if (compositeLabeling != null) {
			return renderCompositeIng(compositeLabeling);
		}
		return "";
	}

	private CompositeLabeling findGroup(NodeRef groupNodeRef) {
		for (CompositeLabeling compositeLabeling : compositeLabelings) {
			if (groupNodeRef.equals(compositeLabeling.getNodeRef())) {
				return compositeLabeling;
			}
		}
		return null;
	}

	private String renderCompositeIng(CompositeLabeling compositeLabeling) {
		StringBuffer ret = new StringBuffer();

		if (compositeLabeling.isGroup() && compositeLabeling.getNodeRef() != null) {
			ret.append(getIngTextFormat(compositeLabeling).format(new Object[] { getIngName(compositeLabeling), compositeLabeling.getQty()  }));
		}
		
		boolean first = true;
		for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> kv : getSortedIngListByType(compositeLabeling).entrySet()) {

			StringBuffer subList = new StringBuffer();

			for (AbstractLabelingComponent ing : kv.getValue()) {

				Double qtyPerc = ing.getQty();
				if( compositeLabeling.getQtyRMUsed()>0){
					qtyPerc =qtyPerc / compositeLabeling.getQtyRMUsed()*100;
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
			

			if (kv.getKey() != null && ! IngTypeItem.DEFAULT.equals(kv.getKey())) {

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

		Map<IngTypeItem, List<AbstractLabelingComponent>> sortedIngListByType = new LinkedHashMap<IngTypeItem, List<AbstractLabelingComponent>>();

		// TODO opère type change and rename
		for (AbstractLabelingComponent lblComponent : compositeLabeling.getIngList().values()) {
			IngTypeItem ingType = IngTypeItem.DEFAULT;
			if(lblComponent instanceof IngItem){
				ingType = ((IngItem)lblComponent).getIngType();
			}
			
			List<AbstractLabelingComponent> subSortedList = sortedIngListByType.get(ingType);

			if (subSortedList == null) {
				subSortedList = new LinkedList<AbstractLabelingComponent>();
				Collections.sort(subSortedList);
				sortedIngListByType.put(ingType, subSortedList);
			}
			subSortedList.add(lblComponent);
		}

		return sortedIngListByType;

	}

	public Set<Locale> getLocales() {
		// TODO
		return new HashSet<>(Arrays.asList(Locale.FRENCH, Locale.ENGLISH));
	}

	@Override
	public String toString() {
		return "LabelingFormulaContext [compositeIngs=" + compositeLabelings + ", textFormaters=" + textFormaters + ", renameRules=" + renameRules + ", nodeDeclarationFilters="
				+ nodeDeclarationFilters + ", declarationFilters=" + declarationFilters + "]";
	}

}
