/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
package fr.becpg.repo.product.formulation.labeling;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StringUtils;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.ing.AbstractLabelingComponent;
import fr.becpg.repo.product.data.ing.CompositeLabeling;
import fr.becpg.repo.product.data.ing.DeclarationFilter;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.spel.DeclarationFilterContext;
import fr.becpg.repo.product.data.spel.SpelHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 *
 * @author matthieu
 *
 */
public class LabelingFormulaContext {

	private static final Log logger = LogFactory.getLog(LabelingFormulaContext.class);

	public static int PRECISION_FACTOR = 100;

	private CompositeLabeling lblCompositeContext;

	private CompositeLabeling mergedLblCompositeContext;

	private final Set<Locale> availableLocales;

	private final NodeService mlNodeService;

	private final AssociationService associationService;

	private final AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private List<ReqCtrlListDataItem> errors = new ArrayList<>();

	private List<ReconstituableDataItem> reconstituableDataItems = new ArrayList<>();

	private Set<NodeRef> allergens = new HashSet<>();

	private Set<NodeRef> toApplyThresholdItems = new HashSet<>();

	public List<ReqCtrlListDataItem> getErrors() {
		return errors;
	}

	public void setErrors(List<ReqCtrlListDataItem> errors) {
		this.errors = errors;
	}

	public Set<NodeRef> getAllergens() {
		return allergens;
	}

	public List<ReconstituableDataItem> getReconstituableDataItems() {
		return reconstituableDataItems;
	}

	public void setReconstituableDataItems(List<ReconstituableDataItem> reconstituableDataItems) {
		this.reconstituableDataItems = reconstituableDataItems;
	}

	public CompositeLabeling getCompositeLabeling() {
		return lblCompositeContext;
	}

	public Set<NodeRef> getToApplyThresholdItems() {
		return toApplyThresholdItems;
	}

	public void setCompositeLabeling(CompositeLabeling compositeLabeling) {
		this.lblCompositeContext = compositeLabeling;
	}

	public CompositeLabeling getMergedLblCompositeContext() {
		return mergedLblCompositeContext;
	}

	public void setMergedLblCompositeContext(CompositeLabeling mergedLblCompositeContext) {
		this.mergedLblCompositeContext = mergedLblCompositeContext;
	}

	public LabelingFormulaContext(NodeService mlNodeService, AssociationService associationService,
			AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		super();
		this.mlNodeService = mlNodeService;
		this.alfrescoRepository = alfrescoRepository;
		this.associationService = associationService;
		availableLocales = new HashSet<>();
	}

	/*
	 * LOCALE
	 */

	public void addLocale(String value, List<String> locales) {
		if (((locales == null) || locales.isEmpty()) && (value != null)) {
			locales = Arrays.asList(value.split(","));
		}

		if (locales != null) {
			for (String tmp : locales) {
				availableLocales.add(MLTextHelper.parseLocale(tmp));
			}
		}
	}

	public Set<Locale> getLocales() {
		return availableLocales;
	}

	/*
	 * FORMAT
	 */

	private class TextFormatRule {
		String textFormat;
		Set<Locale> locales = new HashSet<>();

		public TextFormatRule(String textFormat, List<String> locales) {
			this.textFormat = textFormat;

			if (locales != null) {
				for (String tmp : locales) {
					this.locales.add(MLTextHelper.parseLocale(tmp));
				}
			}
		}

		public boolean matchLocale(Locale locale) {
			return locales.isEmpty() || locales.contains(locale);
		}

		public String getTextFormat() {
			return textFormat;
		}
	}

	private final Map<NodeRef, TextFormatRule> textFormaters = new HashMap<>();

	private String ingDefaultFormat = "{0}";
	private String groupDefaultFormat = "<b>{0} ({1,number,0.#%}):</b> {2}";
	private String groupListDefaultFormat = "<b>{0} {1,number,0.#%}</b>";
	private String detailsDefaultFormat = "{0} ({2})";
	private String ingTypeDefaultFormat = "{0}: {2}";
	private String ingTypeDecThresholdFormat = "{0}";
	private String subIngsDefaultFormat = "{0} ({2})";
	private String allergenReplacementPattern = "<b>$1</b>";

	private String defaultSeparator = RepoConsts.LABEL_SEPARATOR;
	private String groupDefaultSeparator = RepoConsts.LABEL_SEPARATOR;
	private String ingTypeDefaultSeparator = RepoConsts.LABEL_SEPARATOR;
	private String subIngsSeparator = RepoConsts.LABEL_SEPARATOR;

	private boolean showIngCEECode = false;
	private boolean useVolume = false;
	private boolean ingsLabelingWithYield = false;
	private boolean uncapitalizeLegalName = false;

	private Double qtyPrecisionThreshold = (1d / (PRECISION_FACTOR * PRECISION_FACTOR));

	public void setUseVolume(boolean useVolume) {
		this.useVolume = useVolume;
	}

	public void setShowIngCEECode(boolean showIngCEECode) {
		this.showIngCEECode = showIngCEECode;
	}

	public boolean isIngsLabelingWithYield() {
		return ingsLabelingWithYield;
	}

	public void setIngsLabelingWithYield(boolean ingsLabelingWithYield) {
		this.ingsLabelingWithYield = ingsLabelingWithYield;
	}

	public void setIngDefaultFormat(String ingDefaultFormat) {
		this.ingDefaultFormat = ingDefaultFormat;
	}

	public void setGroupDefaultFormat(String groupDefaultFormat) {
		this.groupDefaultFormat = groupDefaultFormat;
	}

	public void setGroupListDefaultFormat(String groupListDefaultFormat) {
		this.groupListDefaultFormat = groupListDefaultFormat;
	}

	public void setUncapitalizeLegalName(boolean uncapitalizeLegalName) {
		this.uncapitalizeLegalName = uncapitalizeLegalName;
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

	public void setIngTypeDecThresholdFormat(String ingTypeDecThresholdFormat) {
		this.ingTypeDecThresholdFormat = ingTypeDecThresholdFormat;
	}

	public void setDefaultSeparator(String defaultSeparator) {
		this.defaultSeparator = defaultSeparator;
	}

	public void setGroupDefaultSeparator(String groupDefaultSeparator) {
		this.groupDefaultSeparator = groupDefaultSeparator;
	}

	public void setIngTypeDefaultSeparator(String ingTypeDefaultSeparator) {
		this.ingTypeDefaultSeparator = ingTypeDefaultSeparator;
	}

	public void setSubIngsSeparator(String subIngsSeparator) {
		this.subIngsSeparator = subIngsSeparator;
	}

	public void setAllergenReplacementPattern(String allergenReplacementPattern) {
		this.allergenReplacementPattern = allergenReplacementPattern;
	}

	public void setQtyPrecisionThreshold(Double qtyPrecisionThreshold) {
		this.qtyPrecisionThreshold = qtyPrecisionThreshold;
	}

	// Exemple <b>{1}</b> : {2}
	public boolean formatText(List<NodeRef> components, String textFormat, List<String> locales) {
		if ((components != null) && !components.isEmpty()) {
			for (NodeRef component : components) {
				textFormaters.put(component, new TextFormatRule(textFormat, locales));
			}
		} else {
			ingDefaultFormat = textFormat;
		}
		return true;
	}

	/* formaters */

	private Format getIngTextFormat(AbstractLabelingComponent lblComponent) {

		if (textFormaters.containsKey(lblComponent.getNodeRef())) {
			TextFormatRule textFormatRule = textFormaters.get(lblComponent.getNodeRef());
			if (textFormatRule.matchLocale(I18NUtil.getLocale())) {
				return applyRoundingMode(new MessageFormat(textFormatRule.getTextFormat()));
			}
		}

		if (lblComponent instanceof CompositeLabeling) {
			if (((CompositeLabeling) lblComponent).isGroup()) {
				return applyRoundingMode(new MessageFormat(groupDefaultFormat));
			}
			if (DeclarationType.Detail.equals(((CompositeLabeling) lblComponent).getDeclarationType())) {
				return applyRoundingMode(new MessageFormat(detailsDefaultFormat));
			}
			return applyRoundingMode(new MessageFormat(ingDefaultFormat));
		} else if (lblComponent instanceof IngTypeItem) {
			if (((((IngTypeItem) lblComponent)).getDecThreshold() != null)
					&& ((((IngTypeItem) lblComponent)).getQty() <= ((((IngTypeItem) lblComponent)).getDecThreshold() / 100))) {
				return applyRoundingMode(new MessageFormat(ingTypeDecThresholdFormat));
			}
			return applyRoundingMode(new MessageFormat(ingTypeDefaultFormat));
		} else if ((lblComponent instanceof IngItem) && (((IngItem) lblComponent).getSubIngs().size() > 0)) {
			return applyRoundingMode(new MessageFormat(subIngsDefaultFormat));
		}
		return applyRoundingMode(new MessageFormat(ingDefaultFormat));
	}

	private Format applyRoundingMode(MessageFormat messageFormat) {
		if (messageFormat.getFormats() != null) {
			for (Format format : messageFormat.getFormats()) {
				if (format instanceof DecimalFormat) {
					((DecimalFormat) format).setRoundingMode(RoundingMode.HALF_DOWN);
				}
			}
		}
		return messageFormat;
	}

	/*
	 * RENAME
	 */

	private class RenameRule {
		MLText mlText;
		MLText pluralMlText;
		
		Set<Locale> locales = new HashSet<>();

		public RenameRule(MLText mlText, MLText pluralMlText, List<String> locales) {
			this.mlText = mlText;
			this.pluralMlText = pluralMlText;
			
			if (locales != null) {
				for (String tmp : locales) {
					this.locales.add(MLTextHelper.parseLocale(tmp));
				}
			}
		}

		public boolean matchLocale(Locale locale) {
			return locales.isEmpty() || locales.contains(locale);
		}

		public String getClosestValue(Locale locale, boolean plural) {
			String ret = null;
			
			if(plural && pluralMlText!=null && ! pluralMlText.isEmpty()){
				ret = MLTextHelper.getClosestValue(pluralMlText, locale);
			}

			if(ret==null || ret.isEmpty()){
				 ret =  MLTextHelper.getClosestValue(mlText, locale) ;
			}
			
			return ret;
		}
			
	}

	private final Map<NodeRef, RenameRule> renameRules = new HashMap<>();

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

				Set<Locale> availableLocales = getLocales();

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
				renameRules.put(component, new RenameRule(mlText,pluralMlText, locales));
			}
		}
		return true;
	}

	private String getName(AbstractLabelingComponent lblComponent) {
		if (lblComponent instanceof IngItem) {
			return ((IngItem) lblComponent).getCharactName();
		}
		return lblComponent.getName();
	}

	public String getLegalIngName(AbstractLabelingComponent lblComponent) {
		String ingLegalName = lblComponent.getLegalName(I18NUtil.getLocale());

		if (renameRules.containsKey(lblComponent.getNodeRef())) {
			RenameRule renameRule = renameRules.get(lblComponent.getNodeRef());
			if (renameRule.matchLocale(I18NUtil.getLocale())) {
				ingLegalName = renameRule.getClosestValue(I18NUtil.getLocale(),false);
			}
		}

		return ingLegalName;
	}

	private String getLegalIngName(AbstractLabelingComponent lblComponent, boolean plural) {

		String ingLegalName = lblComponent.isPlural() ? lblComponent.getPluralLegalName(I18NUtil.getLocale())
				: lblComponent.getLegalName(I18NUtil.getLocale());

		if (renameRules.containsKey(lblComponent.getNodeRef())) {
			RenameRule renameRule = renameRules.get(lblComponent.getNodeRef());
			if (renameRule.matchLocale(I18NUtil.getLocale())) {
				ingLegalName = renameRule.getClosestValue(I18NUtil.getLocale(),lblComponent.isPlural());
			}
		} else {

			if (plural && (lblComponent instanceof IngTypeItem)) {
				if(uncapitalizeLegalName){
					return uncapitalize(((IngTypeItem) lblComponent).getPluralLegalName(I18NUtil.getLocale()));
				} else {
					return ((IngTypeItem) lblComponent).getPluralLegalName(I18NUtil.getLocale());
			   }
			}

			if (showIngCEECode && (lblComponent instanceof IngItem)) {
				if ((((IngItem) lblComponent).getIngCEECode() != null) && !((IngItem) lblComponent).getIngCEECode().isEmpty()) {
					return ((IngItem) lblComponent).getIngCEECode();
				}
			}
		}

		if (uncapitalizeLegalName) {
			ingLegalName = uncapitalize(ingLegalName);
		}

		if (!lblComponent.getAllergens().isEmpty()) {
			if (((lblComponent instanceof CompositeLabeling) && ((CompositeLabeling) lblComponent).getIngList().isEmpty())
					|| (lblComponent instanceof IngItem)) {
				return createAllergenAwareLabel(ingLegalName, lblComponent.getAllergens());
			}

		}

		return ingLegalName;
	}

	private String uncapitalize(String legalName) {
		if ((legalName == null) || legalName.isEmpty() || Pattern.compile("^([A-Z]{2}|[A-Z][1-9]|[A-Z]\\-|[A-Z]_).*$").matcher(legalName).find()) {
			return legalName;
		}
		return StringUtils.uncapitalize(legalName);
	}

	private String createAllergenAwareLabel(String ingLegalName, Set<NodeRef> allergens) {

		if (Pattern.compile("<b>|<u>|<i>|[A-Z]{3}").matcher(ingLegalName).find()) {
			return ingLegalName;
		}

		StringBuilder ret = new StringBuilder();
		for (NodeRef allergen : allergens) {
			if (getAllergens().contains(allergen)) {
				String allergenName = uncapitalize(getAllergenName(allergen));
				if ((allergenName != null) && !allergenName.isEmpty()) {
					if (ret.length() > 0) {
						ret.append(defaultSeparator);
					} else {
						Matcher ma = Pattern.compile("\\b(" + Pattern.quote(allergenName) + "(s?))\\b").matcher(uncapitalize(ingLegalName));
						if (ma.find() && (ma.group(1) != null)) {
							return ma.replaceAll(allergenReplacementPattern);
						} else {
							for (NodeRef subAllergen : associationService.getTargetAssocs(allergen, PLMModel.ASSOC_ALLERGENSUBSETS)) {
								String subAllergenName = uncapitalize(getAllergenName(subAllergen));
								if ((subAllergenName != null) && !subAllergenName.isEmpty()) {
									ma = Pattern.compile("\\b(" + Pattern.quote(subAllergenName) + "(s?))\\b").matcher(uncapitalize(ingLegalName));
									if (ma.find() && (ma.group(1) != null)) {
										return ma.replaceAll(allergenReplacementPattern);
									}
								}
							}
						}
					}
					ret.append(allergenName.replaceFirst("(.*)", allergenReplacementPattern));
				}
			}
		}
		return applyRoundingMode(new MessageFormat(detailsDefaultFormat)).format(new Object[] { ingLegalName, null, ret.toString() });
	}

	private String createAllergenAwareLabel(String ingLegalName, List<AbstractLabelingComponent> ingList) {
		Set<NodeRef> allergens = new HashSet<>();
		for (AbstractLabelingComponent ing : ingList) {
			for (NodeRef allergen : ing.getAllergens()) {
				allergens.add(allergen);
			}
		}

		return createAllergenAwareLabel(ingLegalName, allergens);
	}

	private String getAllergenName(NodeRef allergen) {

		MLText legalName = (MLText) mlNodeService.getProperty(allergen, BeCPGModel.PROP_LEGAL_NAME);

		String ret = MLTextHelper.getClosestValue(legalName, I18NUtil.getLocale());

		if ((ret == null) || ret.isEmpty()) {
			legalName = (MLText) mlNodeService.getProperty(allergen, BeCPGModel.PROP_CHARACT_NAME);

			ret = MLTextHelper.getClosestValue(legalName, I18NUtil.getLocale());
		}

		return ret;
	}

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

		public AggregateRule(NodeRef ruleNodeRef, String name, List<String> locales) {
			super();
			this.name = name;

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

	private final Map<NodeRef, List<AggregateRule>> aggregateRules = new HashMap<>();

	public Map<NodeRef, List<AggregateRule>> getAggregateRules() {
		return aggregateRules;
	}

	/*
	 * DECLARE
	 */

	private final Map<NodeRef, DeclarationFilter> nodeDeclarationFilters = new HashMap<>();
	private final List<DeclarationFilter> declarationFilters = new ArrayList<>();

	public Map<NodeRef, DeclarationFilter> getNodeDeclarationFilters() {
		return nodeDeclarationFilters;
	}

	public List<DeclarationFilter> getDeclarationFilters() {
		return declarationFilters;
	}

	public boolean addRule(NodeRef ruleNodeRef, String name, List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula,
			LabelingRuleType labeLabelingRuleType, List<String> locales) {

		if (labeLabelingRuleType != null) {
			if (LabelingRuleType.Type.equals(labeLabelingRuleType)
					|| ((((components != null) && (components.size() > 1)) || ((replacement != null) && !replacement.isEmpty()))
							&& (LabelingRuleType.Detail.equals(labeLabelingRuleType) || LabelingRuleType.Group.equals(labeLabelingRuleType)
									|| LabelingRuleType.DoNotDetails.equals(labeLabelingRuleType)))) {
				aggregate(ruleNodeRef, name, components, replacement, label, formula, labeLabelingRuleType, locales);
			} else {

				DeclarationType type = null;

				if (LabelingRuleType.DetailComponents.equals(labeLabelingRuleType)) {
					type = DeclarationType.Detail;
				} else if (LabelingRuleType.DoNotDetailsComponents.equals(labeLabelingRuleType)) {
					type = DeclarationType.DoNotDetails;
				} else {
					type = DeclarationType.valueOf(labeLabelingRuleType.toString());
				}

				if ((components != null) && !components.isEmpty()) {
					for (NodeRef component : components) {
						nodeDeclarationFilters.put(component, new DeclarationFilter(formula, type, locales));
					}
				} else {
					declarationFilters.add(new DeclarationFilter(formula, type, locales));
				}

			}
		}

		return true;
	}

	private void aggregate(NodeRef ruleNodeRef, String name, List<NodeRef> components, List<NodeRef> replacement, MLText label, String formula,
			LabelingRuleType labelingRuleType, List<String> locales) {
		String[] qtys = (formula != null) && !formula.isEmpty() ? formula.split(",") : null;

		// components peut être ING, SF ou MP
		int i = 0;
		for (NodeRef component : components) {
			AggregateRule aggregateRule = new AggregateRule(ruleNodeRef, name, locales);

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

	public String render() {
		return render(true);
	}

	public String render(boolean showGroup) {

		if (logger.isTraceEnabled()) {
			logger.trace(" Render label (showGroup:" + showGroup + "): ");
		}

		if (showGroup) {
			return renderCompositeIng(lblCompositeContext, 1d);
		} else {
			return renderCompositeIng(mergedLblCompositeContext, 1d);
		}

	}

	public String renderGroupList() {
		StringBuffer ret = new StringBuffer();

		if (logger.isTraceEnabled()) {
			logger.trace(" Render Group list ");
		}

		List<AbstractLabelingComponent> components = new LinkedList<>(lblCompositeContext.getIngList().values());
		Collections.sort(components);

		for (AbstractLabelingComponent component : components) {

			String ingName = getLegalIngName(component, false);

			Double qtyPerc = computeQtyPerc(lblCompositeContext, component, 1d);

			if (isGroup(component)) {
				if (ret.length() > 0) {
					ret.append(groupDefaultSeparator);
				}
				ret.append(applyRoundingMode(new MessageFormat(groupListDefaultFormat)).format(new Object[] { ingName, qtyPerc }));
			}
		}

		return cleanLabel(ret);
	}

	public String renderAllergens() {
		StringBuffer ret = new StringBuffer();

		if (logger.isTraceEnabled()) {
			logger.trace(" Render Allergens list ");
		}

		for (NodeRef allergen : allergens) {
			if (ret.length() > 0) {
				ret.append(defaultSeparator);
			}
			ret.append(getAllergenName(allergen));
		}

		return ret.toString();
	}

	private String renderCompositeIng(CompositeLabeling compositeLabeling, Double ratio) {
		StringBuffer ret = new StringBuffer();
		boolean appendEOF = false;

		for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> kv : getSortedIngListByType(compositeLabeling).entrySet()) {

			StringBuilder toAppend = new StringBuilder();

			if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), false) != null)) {

				Double qtyPerc = computeQtyPerc(compositeLabeling, kv.getKey(), ratio);
				kv.getKey().setQty(qtyPerc);

				String ingTypeLegalName = getLegalIngName(kv.getKey(),
						((kv.getValue().size() > 1) || (!kv.getValue().isEmpty() && kv.getValue().get(0).isPlural())));
				String allergenAwareLegalName = createAllergenAwareLabel(ingTypeLegalName, kv.getValue());

				toAppend.append(getIngTextFormat(kv.getKey())
						.format(new Object[] { ingTypeLegalName, (useVolume ? kv.getKey().getVolume() : kv.getKey().getQty()),
								renderLabelingComponent(compositeLabeling, kv.getValue(), ingTypeDefaultSeparator, ratio), allergenAwareLegalName }));

			} else {
				toAppend.append(renderLabelingComponent(compositeLabeling, kv.getValue(), defaultSeparator, ratio));
			}

			if ((toAppend != null) && !toAppend.toString().isEmpty()) {
				if (ret.length() > 0) {
					if (appendEOF) {
						ret.append("<br/>");
					} else {
						ret.append(defaultSeparator);
					}
				}
				if (IngTypeItem.DEFAULT_GROUP.equals(kv.getKey())) {
					appendEOF = true;
				} else {
					appendEOF = false;
				}

				ret.append(toAppend);

			}

		}
		return cleanLabel(ret);
	}

	private StringBuilder renderLabelingComponent(CompositeLabeling parent, List<AbstractLabelingComponent> subComponents, String separator,
			Double ratio) {

		StringBuilder ret = new StringBuilder();

		boolean appendEOF = false;
		for (AbstractLabelingComponent component : subComponents) {

			Double qtyPerc = computeQtyPerc(parent, component, ratio);
			Double volumePerc = computeVolumePerc(parent, component, ratio);

			String ingName = getLegalIngName(component, false);

			if (logger.isDebugEnabled()) {

				logger.debug(" --" + ingName + "(" + component.getNodeRef() + ") qtyRMUsed: " + parent.getQtyTotal() + " qtyPerc " + qtyPerc
						+ " apply precision (" + (toApplyThresholdItems.contains(component.getNodeRef()) && ((qtyPerc - qtyPrecisionThreshold) > 0))
						+ ") ");
			}

			qtyPerc = (useVolume ? volumePerc : qtyPerc);

			if (!shouldSkip(component.getNodeRef(), qtyPerc)) {

				String toAppend = new String();

				if (component instanceof IngItem) {
					IngItem ingItem = (IngItem) component;

					StringBuilder subIngBuff = new StringBuilder();
					for (IngItem subIngItem : ingItem.getSubIngs()) {
						if (subIngBuff.length() > 0) {
							subIngBuff.append(subIngsSeparator);
						}
						Double subIngQtyPerc = (useVolume ? subIngItem.getVolume() : subIngItem.getQty());

						if (!shouldSkip(subIngItem.getNodeRef(), subIngQtyPerc)) {

							subIngBuff.append(
									getIngTextFormat(subIngItem).format(new Object[] { getLegalIngName(subIngItem, false), subIngQtyPerc, null }));
						} else {
							logger.debug("Removing subIng with qty of 0: " + subIngItem);
						}
					}

					toAppend = getIngTextFormat(component).format(new Object[] { ingName, qtyPerc, subIngBuff.toString() });

				} else if (component instanceof CompositeLabeling) {

					Double subRatio = qtyPerc;
					if (DeclarationType.Kit.equals(((CompositeLabeling) component).getDeclarationType())) {
						subRatio = 1d;
					}

					toAppend = getIngTextFormat(component)
							.format(new Object[] { ingName, qtyPerc, renderCompositeIng((CompositeLabeling) component, subRatio) });

				} else {
					logger.error("Unsupported ing type. Name: " + component.getName());
				}

				if ((toAppend != null) && !toAppend.isEmpty()) {
					if (ret.length() > 0) {
						if (appendEOF) {
							ret.append("<br/>");
						} else {
							ret.append(separator);
						}
					}

					if (isGroup(component)) {
						appendEOF = true;
					} else {
						appendEOF = false;
					}

					ret.append(toAppend);
				}

			} else {
				logger.debug("Removing ing with qty of 0: " + ingName);
			}

		}

		return ret;
	}

	private boolean shouldSkip(NodeRef nodeRef, Double qtyPerc) {
		return !((qtyPerc == null) || (toApplyThresholdItems.contains(nodeRef) && (qtyPerc > qtyPrecisionThreshold))
				|| (!toApplyThresholdItems.contains(nodeRef) && (qtyPerc > 0)));
	}

	public String createJsonLog(boolean mergedLabeling) {
		if (!mergedLabeling) {
			return createJsonLog(lblCompositeContext, null, null, new HashSet<>()).toString();
		}
		return createJsonLog(mergedLblCompositeContext, null, null, new HashSet<>()).toString();
	}

	@SuppressWarnings("unchecked")
	private JSONObject createJsonLog(AbstractLabelingComponent component, Double totalQty, Double totalVol, Set<AbstractLabelingComponent> visited) {

		JSONObject tree = new JSONObject();

		if (visited.contains(component)) {
			return tree;
		}

		if (!(component instanceof IngItem)) {
			visited.add(component);
		}

		if (component != null) {

			if ((component.getNodeRef() != null) && mlNodeService.exists(component.getNodeRef())) {
				tree.put("nodeRef", component.getNodeRef().toString());
				tree.put("cssClass", mlNodeService.getType(component.getNodeRef()).getLocalName());
			}

			tree.put("name", getName(component));
			tree.put("legal", getLegalIngName(component,false));
			if ((component.getVolume() != null) && (totalVol != null) && (totalVol > 0)) {
				tree.put("vol", (component.getVolume() / totalVol) * 100);
			}
			if ((component.getQty() != null) && (totalQty != null) && (totalQty > 0)) {
				tree.put("qte", (component.getQty() / totalQty) * 100);
			}
			if (!component.getAllergens().isEmpty()
					&& (!(component instanceof CompositeLabeling) || ((CompositeLabeling) component).getIngList().isEmpty())) {
				JSONArray allergens = new JSONArray();
				for (NodeRef allergen : component.getAllergens()) {
					allergens.add(getAllergenName(allergen));
				}
				tree.put("allergens", allergens);
			}

			if (component instanceof CompositeLabeling) {
				CompositeLabeling composite = (CompositeLabeling) component;

				if (composite.getDeclarationType() != null) {
					tree.put("decl", I18NUtil.getMessage("listconstraint.bcpg_declarationTypes." + composite.getDeclarationType().toString()));
				}

				JSONArray children = new JSONArray();
				for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> kv : getSortedIngListByType(composite).entrySet()) {

					if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), false) != null)) {

						JSONObject ingTypeJson = new JSONObject();
						ingTypeJson.put("nodeRef", kv.getKey().getNodeRef().toString());
						ingTypeJson.put("cssClass", "ingType");
						ingTypeJson.put("name", getName(kv.getKey()));
						ingTypeJson.put("legal", getLegalIngName(kv.getKey(),
								(kv.getValue().size() > 1) || (!kv.getValue().isEmpty() && kv.getValue().get(0).isPlural())));

						if ((kv.getKey().getQty() != null) && (totalQty != null) && (totalQty > 0)) {
							ingTypeJson.put("qte", (kv.getKey().getQty() / totalQty) * 100);
						}
						if ((kv.getKey().getVolume() != null) && (totalVol != null) && (totalVol > 0)) {
							ingTypeJson.put("vol", (kv.getKey().getVolume() / totalVol) * 100);

						}
						JSONArray ingTypeJsonChildren = new JSONArray();
						for (AbstractLabelingComponent childComponent : kv.getValue()) {
							ingTypeJsonChildren.add(createJsonLog(childComponent, composite.getQtyTotal(), composite.getVolumeTotal(), visited));
						}
						ingTypeJson.put("children", ingTypeJsonChildren);
						children.add(ingTypeJson);
					} else {
						for (AbstractLabelingComponent childComponent : kv.getValue()) {
							children.add(createJsonLog(childComponent, composite.getQtyTotal(), composite.getVolumeTotal(), visited));
						}
					}

				}

				tree.put("children", children);

			} else if ((component instanceof IngItem) && !((IngItem) component).getSubIngs().isEmpty()) {
				JSONArray children = new JSONArray();
				for (IngItem childComponent : ((IngItem) component).getSubIngs()) {
					children.add(createJsonLog(childComponent, ((IngItem) component).getQty(), ((IngItem) component).getVolume(), visited));
				}
				tree.put("children", children);
			}

		}

		return tree;
	}

	private String cleanLabel(StringBuffer buffer) {
		return buffer.toString().replaceAll(" null| \\(null\\)| \\(\\)", "").trim();
	}

	public boolean isGroup(AbstractLabelingComponent component) {
		return (component instanceof CompositeLabeling) && ((CompositeLabeling) component).isGroup();
	}

	public Double computeQtyPerc(CompositeLabeling parent, AbstractLabelingComponent component, Double ratio) {
		if (ratio == null) {
			return null;
		}

		Double qty = component.getQty();
		if ((parent.getQtyTotal() != null) && (parent.getQtyTotal() > 0) && (qty != null)) {
			qty = (qty / parent.getQtyTotal()) * ratio;
		}
		return qty;
	}

	public Double computeVolumePerc(CompositeLabeling parent, AbstractLabelingComponent component, Double ratio) {
		if (ratio == null) {
			return null;
		}

		Double volume = component.getVolume();
		if ((parent.getVolumeTotal() != null) && (parent.getVolumeTotal() > 0) && (volume != null)) {
			volume = (volume / parent.getVolumeTotal()) * ratio;
		}
		return volume;
	}

	Map<IngTypeItem, List<AbstractLabelingComponent>> getSortedIngListByType(CompositeLabeling compositeLabeling) {

		Locale currentLocale = I18NUtil.getLocale();

		Map<IngTypeItem, List<AbstractLabelingComponent>> tmp = new LinkedHashMap<>();

		boolean keepOrder = false;
		for (AbstractLabelingComponent lblComponent : compositeLabeling.getIngList().values()) {
			IngTypeItem ingType = null;
			if (lblComponent instanceof IngItem) {
				ingType = ((IngItem) lblComponent).getIngType();
			}

			if (lblComponent instanceof CompositeLabeling) {
				ingType = ((CompositeLabeling) lblComponent).getIngType();
			}

			if (aggregateRules.containsKey(lblComponent.getNodeRef())) {
				for (AggregateRule aggregateRule : aggregateRules.get(lblComponent.getNodeRef())) {
					if (LabelingRuleType.Type.equals(aggregateRule.getLabelingRuleType()) && aggregateRule.matchLocale(currentLocale)) {
						ingType = getReplacementIngType(aggregateRule);
					}
				}
			}

			if (ingType != null) {

				// Type replacement
				if (aggregateRules.containsKey(ingType.getNodeRef())) {
					for (AggregateRule aggregateRule : aggregateRules.get(ingType.getNodeRef())) {
						if (LabelingRuleType.Type.equals(aggregateRule.getLabelingRuleType()) && aggregateRule.matchLocale(currentLocale)) {
							ingType = getReplacementIngType(aggregateRule);
						}
					}
					// Ing IngType replacement
				}

				// If Omit
				if (nodeDeclarationFilters.containsKey(ingType.getNodeRef())) {
					DeclarationFilter declarationFilter = nodeDeclarationFilters.get(ingType.getNodeRef());
					if (DeclarationType.Omit.equals(declarationFilter.getDeclarationType())
							&& matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext())
							&& declarationFilter.matchLocale(currentLocale)) {
						break;
					} else if (DeclarationType.DoNotDeclare.equals(declarationFilter.getDeclarationType())
							&& matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext())
							&& declarationFilter.matchLocale(currentLocale)) {
						ingType = null;
					}

				}

			}

			if ((lblComponent.getQty() == null) || (useVolume && (lblComponent.getVolume() == null))) {
				keepOrder = true;
			}

			if ((lblComponent instanceof CompositeLabeling) && ((CompositeLabeling) lblComponent).isGroup()) {
				ingType = IngTypeItem.DEFAULT_GROUP;
			}

			if (ingType == null) {
				ingType = new IngTypeItem();
				ingType.setNodeRef(new NodeRef(RepoConsts.SPACES_STORE, "ingType-" + lblComponent.getNodeRef().hashCode()));
			}

			// Reset qty for equality
			ingType.setQty(0d);
			ingType.setVolume(0d);

			List<AbstractLabelingComponent> subSortedList = tmp.get(ingType);

			if (subSortedList == null) {
				subSortedList = new LinkedList<>();
				tmp.put(ingType, subSortedList);
			}
			subSortedList.add(lblComponent);

		}

		keepOrder = DeclarationType.Detail.equals(compositeLabeling.getDeclarationType()) && keepOrder;

		List<Map.Entry<IngTypeItem, List<AbstractLabelingComponent>>> entries = new ArrayList<>(tmp.entrySet());
		/*
		 * Compute IngType Qty
		 */

		for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> entry : entries) {
			Double qty = 0d;
			Double vol = 0d;

			for (AbstractLabelingComponent lblComponent : entry.getValue()) {
				if (lblComponent.getQty() != null) {
					qty += lblComponent.getQty();
				}
				if (lblComponent.getVolume() != null) {
					vol += lblComponent.getVolume();
				}
			}
			entry.getKey().setQty(qty);
			entry.getKey().setVolume(vol);

		}

		/*
		 * Sort by qty, default is always first
		 */

		if (!keepOrder) {
			Collections.sort(entries, (a, b) -> {

				if (IngTypeItem.DEFAULT_GROUP.equals(a.getKey())) {
					return -1;
				}

				if (IngTypeItem.DEFAULT_GROUP.equals(b.getKey())) {
					return 1;
				}

				if (useVolume) {
					return b.getKey().getVolume().compareTo(a.getKey().getVolume());
				}

				return b.getKey().getQty().compareTo(a.getKey().getQty());
			});
		}
		Map<IngTypeItem, List<AbstractLabelingComponent>> sortedIngListByType = new LinkedHashMap<>();
		for (Map.Entry<IngTypeItem, List<AbstractLabelingComponent>> entry : entries) {

			if (!keepOrder) {
				// Sort by value
				Collections.sort(entry.getValue());
			}
			sortedIngListByType.put(entry.getKey(), entry.getValue());
		}

		return sortedIngListByType;

	}

	private IngTypeItem getReplacementIngType(AggregateRule aggregateRule) {
		IngTypeItem ingType = null;

		if (aggregateRule.getReplacement() != null) {
			RepositoryEntity repositoryEntity = alfrescoRepository.findOne(aggregateRule.getReplacement());
			if (repositoryEntity instanceof IngTypeItem) {
				ingType = (IngTypeItem) repositoryEntity;
			}
		} else {
			ingType = new IngTypeItem();
			ingType.setLegalName(aggregateRule.getLabel());
		}

		return ingType;
	}

	public boolean matchFormule(String formula, DeclarationFilterContext declarationFilterContext) {
		if ((formula != null) && !formula.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Test Match formula :" + formula);
			}
			try {
				ExpressionParser parser = new SpelExpressionParser();
				StandardEvaluationContext dataContext = new StandardEvaluationContext(declarationFilterContext);

				Expression exp = parser.parseExpression(SpelHelper.formatFormula(formula));

				return exp.getValue(dataContext, Boolean.class);
			} catch (SpelParseException | SpelEvaluationException e) {
				logger.error("Cannot evaluate formula :" + formula, e);
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "LabelingFormulaContext [compositeLabeling=" + lblCompositeContext + ", textFormaters=" + textFormaters + ", renameRules="
				+ renameRules + ", nodeDeclarationFilters=" + nodeDeclarationFilters + ", declarationFilters=" + declarationFilters + "]";
	}

}
