package fr.becpg.repo.product.formulation.labeling;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
import fr.becpg.repo.product.data.meat.MeatType;

/**
 * <p>Abstract RuleParser class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class RuleParser {

	private static Log logger = LogFactory.getLog(RuleParser.class);

	protected final NodeService mlNodeService;
	protected final Set<Locale> availableLocales;
	protected final Map<NodeRef, TextFormatRule> textFormaters = new HashMap<>();
	
	protected final  Map<String, TextFormatRule> formatsByName = new HashMap<>();
	
	protected final Map<NodeRef, List<DeclarationFilterRule>> nodeDeclarationFilters = new HashMap<>();
	protected final List<DeclarationFilterRule> declarationFilters = new ArrayList<>();
	protected final List<SeparatorRule> separatorRules = new ArrayList<>();
	protected final Map<NodeRef, List<AggregateRule>> aggregateRules = new HashMap<>();
	protected final Map<String, MeatContentRule> meatContentRules = new HashMap<>();
	protected final Map<NodeRef, RenameRule> renameRules = new HashMap<>();

	protected final Map<NodeRef, ShowRule> showGeoRules = new HashMap<>();
	protected final Map<NodeRef, ShowRule> showBioRules = new HashMap<>();
	
	protected final List<FootNoteRule> footNoteRules = new ArrayList<>();
	protected final Map<NodeRef, List<FootNoteRule>> nodeFootNoteRules = new HashMap<>();

	protected String defaultPercFormat = "0.#%";
	protected boolean computePercByParent = false;
	protected RoundingMode defaultRoundingMode = RoundingMode.HALF_DOWN;

	protected List<ShowRule> showAllPerc = new ArrayList<>();
	protected final Map<NodeRef, List<ShowRule>> showPercRules = new HashMap<>();
	
	protected ShowRule showAllGeo = null;
	protected ShowRule showAllBio = null;

	protected List<LabelingDecorator> labelingDecorators = new ArrayList<>();

	{
		labelingDecorators.add(new CapitalizeDecorator());
		labelingDecorators.add(new LowerCaseDecorator());
		labelingDecorators.add(new UppercaseDecorator());
		labelingDecorators.add(new FrenchTypoDecorator());
	}

	/**
	 * <p>Setter for the field <code>defaultRoundingMode</code>.</p>
	 *
	 * @param defaultRoundingMode a {@link java.math.RoundingMode} object.
	 */
	public void setDefaultRoundingMode(RoundingMode defaultRoundingMode) {
		this.defaultRoundingMode = defaultRoundingMode;
	}

	/**
	 * <p>Setter for the field <code>defaultPercFormat</code>.</p>
	 *
	 * @param defaultPercFormat a {@link java.lang.String} object.
	 */
	public void setDefaultPercFormat(String defaultPercFormat) {
		this.defaultPercFormat = defaultPercFormat;
	}

	/**
	 * <p>Setter for the field <code>computePercByParent</code>.</p>
	 *
	 * @param computePercByParent a boolean
	 */
	public void setComputePercByParent(boolean computePercByParent) {
		this.computePercByParent = computePercByParent;
	}

	/**
	 * <p>Setter for the field <code>showAllPerc</code>.</p>
	 *
	 * @param showAllPerc a boolean.
	 */
	public void setShowAllPerc(boolean showAllPerc) {
		if (showAllPerc) {
			this.showAllPerc.add(new ShowRule(defaultPercFormat, defaultRoundingMode, null));
		} else {
			this.showAllPerc = null;
		}
	}

	/**
	 * <p>Setter for the field <code>showAllGeo</code>.</p>
	 *
	 * @param showAllGeo a boolean.
	 */
	public void setShowAllGeo(boolean showAllGeo) {
		if (showAllGeo) {
			this.showAllGeo = new ShowRule("", null, null);
		} else {
			this.showAllGeo = null;
		}
	}
	
	/**
	 * <p>Setter for the field <code>showAllGeo</code>.</p>
	 *
	 * @param showAllBio a boolean
	 */
	public void setShowAllBio(boolean showAllBio) {
		if (showAllBio) {
			this.showAllBio = new ShowRule("", null, null);
		} else {
			this.showAllBio = null;
		}
	}

	/**
	 * <p>Getter for the field <code>aggregateRules</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<NodeRef, List<AggregateRule>> getAggregateRules() {
		return aggregateRules;
	}

	/**
	 * <p>Getter for the field <code>meatContentRules</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<MeatContentRule> getMeatContentRules() {
		return new ArrayList<>(meatContentRules.values());
	}

	/**
	 * <p>Getter for the field <code>nodeDeclarationFilters</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<NodeRef, List<DeclarationFilterRule>> getNodeDeclarationFilters() {
		return nodeDeclarationFilters;
	}

	
	
	
	/**
	 * <p>Getter for the field <code>nodeFootNoteRules</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<NodeRef, List<FootNoteRule>> getNodeFootNoteRules() {
		return nodeFootNoteRules;
	}

	
	
	/**
	 * <p>Getter for the field <code>footNoteRules</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<FootNoteRule> getFootNoteRules() {
		return footNoteRules;
	}

	/**
	 * <p>Getter for the field <code>declarationFilters</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<DeclarationFilterRule> getDeclarationFilters() {
		return declarationFilters;
	}

	/**
	 * <p>getLocales.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Locale> getLocales() {
		return availableLocales;
	}

	/**
	 * <p>Constructor for RuleParser.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	protected RuleParser(NodeService mlNodeService) {
		super();
		this.mlNodeService = mlNodeService;
		availableLocales = new LinkedHashSet<>();
	}

	/**
	 * <p>addRule.</p>
	 *
	 * @param ruleNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 * @param components a {@link java.util.List} object.
	 * @param replacement a {@link java.util.List} object.
	 * @param label a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @param formula a {@link java.lang.String} object.
	 * @param labeLabelingRuleType a {@link fr.becpg.repo.product.data.constraints.LabelingRuleType} object.
	 * @param locales a {@link java.util.List} object.
	 * @return a boolean.
	 * @param sort a int
	 */
	public boolean addRule(NodeRef ruleNodeRef, String name, List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula,
			LabelingRuleType labeLabelingRuleType, List<String> locales, int sort) {

		if (labeLabelingRuleType != null) {

			if (LabelingRuleType.Format.equals(labeLabelingRuleType)) {
				formatText(components, formula, locales);
			} else if (LabelingRuleType.Rename.equals(labeLabelingRuleType)) {
				rename(components, replacement, label, formula, locales);
			} else if (LabelingRuleType.Locale.equals(labeLabelingRuleType)) {
				addLocale(formula, locales);
			} else if (LabelingRuleType.ShowPerc.equals(labeLabelingRuleType)) {
				if (components == null || components.isEmpty()) {
					if ((formula != null) && !formula.isEmpty()) {
						String[] splitted = formula.split(",");
						for(String roundFormula : splitted) {
							if (roundFormula.contains("|")) {
								if (roundFormula.split("\\|").length == 2) {
									defaultPercFormat = roundFormula.split("\\|")[0];
									defaultRoundingMode = RoundingMode.valueOf(roundFormula.split("\\|")[1]);
								} 
								showAllPerc.add(new ShowRule(roundFormula, defaultRoundingMode , locales));
								
							} else {
								defaultPercFormat = formula;
								showAllPerc.add(new ShowRule(formula, defaultRoundingMode, locales));
							}
						}
					} else {
						showAllPerc.add(new ShowRule(defaultPercFormat, defaultRoundingMode, locales));
					}

				} else {
					for (NodeRef component : components) {
						List<ShowRule> rules = showPercRules.get(component);
						if(rules == null) {
							rules = new ArrayList<>();
							showPercRules.put(component, rules);
						}
						
						if ((formula != null) && !formula.isEmpty()) {
							String[] splitted = formula.split(",");
							for(String roundFormula : splitted) {
								rules.add(new ShowRule(roundFormula, defaultRoundingMode, locales));
							}
						} else {
							rules.add( new ShowRule(defaultPercFormat, defaultRoundingMode, locales));
						}
					}
				}

			} else if (LabelingRuleType.ShowGeo.equals(labeLabelingRuleType)) {
				if (components==null || components.isEmpty()) {
					showAllGeo = new ShowRule((formula != null) && !formula.isEmpty() ? formula : "", null, locales);
				} else {
					for (NodeRef component : components) {
						showGeoRules.put(component, new ShowRule((formula != null) && !formula.isEmpty() ? formula : "", null, locales));
					}
				}
			} else if (LabelingRuleType.ShowBio.equals(labeLabelingRuleType)) {
				if (components==null ||components.isEmpty()) {
					showAllBio = new ShowRule((formula != null) && !formula.isEmpty() ? formula : "", null, locales);
				} else {
					for (NodeRef component : components) {
						showBioRules.put(component, new ShowRule((formula != null) && !formula.isEmpty() ? formula : "", null, locales));
					}
				}
			} else if (LabelingRuleType.Type.equals(labeLabelingRuleType)
					|| ((((components != null) && (components.size() > 1)) || ((replacement != null) && !replacement.isEmpty()))
							&& (LabelingRuleType.Detail.equals(labeLabelingRuleType) || LabelingRuleType.Group.equals(labeLabelingRuleType)
									|| LabelingRuleType.DoNotDetails.equals(labeLabelingRuleType)))) {
				if (MeatType.isMeatType(formula)) {
					addMeatContentRule(components, replacement, formula, locales);
				} else {
					aggregate(ruleNodeRef, name, components, replacement, label, formula, labeLabelingRuleType, locales);
				}
			} else if(LabelingRuleType.FootNote.equals(labeLabelingRuleType)) {
				
				FootNoteRule footNoteRule = new FootNoteRule(name, label, formula, locales, sort);
				
				if (components==null || components.isEmpty()) {
					footNoteRules.add(footNoteRule);
				} else {
					for (NodeRef component : components) {
						
						List<FootNoteRule> tmp = new ArrayList<>();
						if (nodeFootNoteRules.containsKey(component)) {
							tmp = nodeFootNoteRules.get(component);
						}
						tmp.add(footNoteRule);

						nodeFootNoteRules.put(component, tmp);
					}
				}
			
		    }  else {

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

				DeclarationFilterRule declarationFilter = new DeclarationFilterRule(name, formula, type, locales);
				if (isThreshold) {
					declarationFilter.setThreshold(Double.parseDouble(formula));
				}

				if ((components != null) && !components.isEmpty()) {
					for (NodeRef component : components) {
						List<DeclarationFilterRule> tmp = new ArrayList<>();
						if (nodeDeclarationFilters.containsKey(component)) {
							tmp = nodeDeclarationFilters.get(component);
						}
						tmp.add(declarationFilter);

						nodeDeclarationFilters.put(component, tmp);
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
			if(textFormat.contains("|")) {
				formatsByName.put(textFormat.split("\\|")[0], new TextFormatRule(textFormat.split("\\|")[1],locales));
			} else {
				updateDefaultFormat(new TextFormatRule(textFormat, locales));
			}
		}
		return true;
	}

	abstract void updateDefaultFormat(TextFormatRule textFormatRule);

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
					logger.info("Cannot read double value" + qtys[i] + " for rule: " + ruleNodeRef);
				}
			}
			aggregateRule.setComponents(components);
			aggregateRule.setLabelingRuleType(labelingRuleType);

			i++;

			if (aggregateRules.containsKey(component)) {
				aggregateRules.get(component).add(aggregateRule);
			} else {
				aggregateRules.put(component, new ArrayList<>(Collections.singletonList(aggregateRule)));
			}

		}
	}

	private void addMeatContentRule(List<NodeRef> components, List<NodeRef> replacement, String formula, List<String> locales) {

		String meatType = formula.replace("-CT", "").replace("-FAT", "");

		for (NodeRef component : components) {
			MeatContentRule meatContentRule = meatContentRules.computeIfAbsent(meatType, a -> new MeatContentRule(meatType, locales));

			if ((replacement != null) && !replacement.isEmpty()) {
				if (formula.endsWith("-CT")) {
					meatContentRule.setCtReplacement(replacement.get(0));
				} else {
					meatContentRule.setFatReplacement(replacement.get(0));
				}
			}
			meatContentRule.setComponent(component);

		}
	}

	/**
	 * <p>rename.</p>
	 *
	 * @param components a {@link java.util.List} object.
	 * @param replacement a {@link java.util.List} object.
	 * @param label a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @param formula a {@link java.lang.String} object.
	 * @param locales a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public boolean rename(List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula, List<String> locales) {

		if (components.isEmpty() && replacement.isEmpty() && formula != null && formula.matches("-?\\d+(\\.\\d+)?")) {
			separatorRules.add(new SeparatorRule(label, Double.parseDouble(formula), locales));
		} else {
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

					Set<Locale> availableLocalesTmp = new LinkedHashSet<>(getLocales());

					if (availableLocalesTmp.isEmpty()) {
						availableLocalesTmp.add(new Locale(Locale.getDefault().getLanguage()));
					}

					for (Locale locale : availableLocalesTmp) {
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
					renameRules.put(component, new RenameRule(mlText, pluralMlText, locales, (replacement != null) && !replacement.isEmpty() ? replacement.get(0): null));
				}
			}
		}
		return true;
	}

}
