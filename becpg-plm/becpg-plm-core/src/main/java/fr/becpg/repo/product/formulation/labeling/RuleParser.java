package fr.becpg.repo.product.formulation.labeling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.ing.DeclarationFilter;

public abstract class RuleParser {

	private static Log logger = LogFactory.getLog(RuleParser.class);

	protected final NodeService mlNodeService;
	protected final Set<Locale> availableLocales;
	protected final Map<NodeRef, TextFormatRule> textFormaters = new HashMap<>();
	protected final Map<NodeRef, DeclarationFilter> nodeDeclarationFilters = new HashMap<>();
	protected final List<DeclarationFilter> declarationFilters = new ArrayList<>();
	protected final Map<NodeRef, List<AggregateRule>> aggregateRules = new HashMap<>();
	protected final Map<NodeRef, RenameRule> renameRules = new HashMap<>();
	protected final Map<NodeRef, ShowRule> showPercRules = new HashMap<>();
	protected final Map<NodeRef, ShowRule> showGeoRules = new HashMap<>();

	protected String defaultPercFormat = "0.#%";

	protected boolean showAllPerc = false;
	protected boolean showAllGeo = false;
	

	public void setDefaultPercFormat(String defaultPercFormat) {
		this.defaultPercFormat = defaultPercFormat;
	}

	public void setShowAllPerc(boolean showAllPerc) {
		this.showAllPerc = showAllPerc;
	}

	public void setShowAllGeo(boolean showAllGeo) {
		this.showAllGeo = showAllGeo;
	}

	public Map<NodeRef, List<AggregateRule>> getAggregateRules() {
		return aggregateRules;
	}

	public Map<NodeRef, DeclarationFilter> getNodeDeclarationFilters() {
		return nodeDeclarationFilters;
	}

	public List<DeclarationFilter> getDeclarationFilters() {
		return declarationFilters;
	}

	public Set<Locale> getLocales() {
		return availableLocales;
	}

	public RuleParser(NodeService mlNodeService) {
		super();
		this.mlNodeService = mlNodeService;
		availableLocales = new LinkedHashSet<>();
	}

	public boolean addRule(NodeRef ruleNodeRef, String name, List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula,
			LabelingRuleType labeLabelingRuleType, List<String> locales) {

		if (labeLabelingRuleType != null) {

			if (LabelingRuleType.Format.equals(labeLabelingRuleType)) {
				formatText(components, formula, locales);
			} else if (LabelingRuleType.Rename.equals(labeLabelingRuleType)) {
				rename(components, replacement, label, formula, locales);
			} else if (LabelingRuleType.Locale.equals(labeLabelingRuleType)) {
				addLocale(formula, locales);
			} else if (LabelingRuleType.ShowPerc.equals(labeLabelingRuleType)) {
				if (components.isEmpty()) {
					showAllPerc = true;
					if ((formula != null) && !formula.isEmpty()) {
						defaultPercFormat = formula;
					}

				} else {
					for (NodeRef component : components) {
						showPercRules.put(component, new ShowRule((formula != null) && !formula.isEmpty() ? formula : defaultPercFormat, locales));
					}
				}

			} else if (LabelingRuleType.ShowGeo.equals(labeLabelingRuleType)) {
				if (components.isEmpty()) {
					showAllGeo = true;
				} else {
					for (NodeRef component : components) {
						showGeoRules.put(component, new ShowRule(formula, locales));
					}
				}
			} else if (LabelingRuleType.Type.equals(labeLabelingRuleType)
					|| ((((components != null) && (components.size() > 1)) || ((replacement != null) && !replacement.isEmpty()))
							&& (LabelingRuleType.Detail.equals(labeLabelingRuleType) || LabelingRuleType.Group.equals(labeLabelingRuleType)
									|| LabelingRuleType.DoNotDetails.equals(labeLabelingRuleType)))) {
				aggregate(ruleNodeRef, name, components, replacement, label, formula, labeLabelingRuleType, locales);
			} else {

				DeclarationType type = null;
				boolean isThreshold = false;

				if (LabelingRuleType.DetailComponents.equals(labeLabelingRuleType)) {
					type = DeclarationType.Detail;
				} else if (LabelingRuleType.DoNotDetailsComponents.equals(labeLabelingRuleType)) {
					type = DeclarationType.DoNotDetails;
				} else if (LabelingRuleType.DeclareThreshold.equals(labeLabelingRuleType)) {
					type = DeclarationType.DoNotDeclare;
					isThreshold = true;
				} else {
					type = DeclarationType.valueOf(labeLabelingRuleType.toString());
				}

				DeclarationFilter declarationFilter = new DeclarationFilter(formula, type, locales);
				if (isThreshold) {
					declarationFilter.setThreshold(Double.parseDouble(formula));
				}

				if ((components != null) && !components.isEmpty()) {
					for (NodeRef component : components) {
						nodeDeclarationFilters.put(component, declarationFilter);
					}
				} else {
					declarationFilters.add(declarationFilter);
				}

			}
		}
		return true;
	}

	// Exemple <b>{1}</b> : {2}
	private boolean formatText(List<NodeRef> components, String textFormat, List<String> locales) {
		if ((components != null) && !components.isEmpty()) {
			for (NodeRef component : components) {
				textFormaters.put(component, new TextFormatRule(textFormat, locales));
			}
		} else {
			updateDefaultFormat(textFormat);
		}
		return true;
	}

	abstract void updateDefaultFormat(String textFormat);

	private void addLocale(String value, List<String> locales) {
		if (((locales == null) || locales.isEmpty()) && (value != null)) {
			locales = Arrays.asList(value.split(","));
		}

		if (locales != null) {
			for (String tmp : locales) {
				availableLocales.add(MLTextHelper.parseLocale(tmp));
			}
		}
	}

	private void aggregate(NodeRef ruleNodeRef, String name, List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula,
			LabelingRuleType labelingRuleType, List<String> locales) {
		String[] qtys = (formula != null) && !formula.isEmpty() ? formula.split(",") : null;

		// components peut être ING, SF ou MP
		int i = 0;
		for (NodeRef component : components) {
			AggregateRule aggregateRule = new AggregateRule(mlNodeService, ruleNodeRef, name, locales);

			if ((replacement != null) && !replacement.isEmpty()) {
				aggregateRule.setReplacement(replacement.get(0));
			}

			if ((label != null) && !label.isEmpty()) {
				aggregateRule.setLabel(label);
			}

			if ((qtys != null) && (qtys.length > i)) {
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
				aggregateRules.put(component, new LinkedList<>(Collections.singletonList(aggregateRule)));
			}

		}
	}

	public boolean rename(List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula, List<String> locales) {
		for (NodeRef component : components) {
			MLText mlText = null;
			MLText pluralMlText = null;
			if ((replacement != null) && !replacement.isEmpty()) {
				mlText = (MLText) mlNodeService.getProperty(replacement.get(0), BeCPGModel.PROP_LEGAL_NAME);
				pluralMlText = (MLText) mlNodeService.getProperty(replacement.get(0), BeCPGModel.PROP_PLURAL_LEGAL_NAME);
			} else if (label != null) {
				mlText = label;
			} else if (formula != null) {
				mlText = new MLText();

				Set<Locale> availableLocales = new LinkedHashSet<>(getLocales());

				if (availableLocales.isEmpty()) {
					availableLocales.add(new Locale(Locale.getDefault().getLanguage()));
				}

				for (Locale locale : availableLocales) {
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
				renameRules.put(component, new RenameRule(mlText, pluralMlText, locales));
			}
		}
		return true;
	}

}
