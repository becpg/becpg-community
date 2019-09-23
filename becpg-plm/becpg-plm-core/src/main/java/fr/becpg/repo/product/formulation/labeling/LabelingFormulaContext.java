/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.alfresco.util.Pair;
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
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.ing.CompositeLabeling;
import fr.becpg.repo.product.data.ing.DeclarationFilter;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.ing.LabelingComponent;
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
public class LabelingFormulaContext extends RuleParser {

	private static final Log logger = LogFactory.getLog(LabelingFormulaContext.class);

	public static int PRECISION_FACTOR = 100;

	private CompositeLabeling lblCompositeContext;

	private CompositeLabeling mergedLblCompositeContext;

	private final AssociationService associationService;

	private final AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private List<ReqCtrlListDataItem> errors = new ArrayList<>();

	private List<ReconstituableDataItem> reconstituableDataItems = new ArrayList<>();

	private Set<NodeRef> allergens = new HashSet<>();
	private Set<NodeRef> inVolAllergens = new HashSet<>();
	private Set<NodeRef> inVolAllergensProcess = new HashSet<>();
	private Set<NodeRef> inVolAllergensRawMaterial = new HashSet<>();

	private Set<NodeRef> toApplyThresholdItems = new HashSet<>();

	// Spel variable
	private Locale locale;

	// Spel variable
	private ProductData entity;

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public ProductData getEntity() {
		return entity;
	}

	public void setEntity(ProductData entity) {
		this.entity = entity;
	}

	public List<ReqCtrlListDataItem> getErrors() {
		return errors;
	}

	public void setErrors(List<ReqCtrlListDataItem> errors) {
		this.errors = errors;
	}

	public Set<NodeRef> getAllergens() {
		return allergens;
	}

	public Set<NodeRef> getInVolAllergens() {
		return inVolAllergens;
	}

	public Set<NodeRef> getInVolAllergensProcess() {
		return inVolAllergensProcess;
	}

	public Set<NodeRef> getInVolAllergensRawMaterial() {
		return inVolAllergensRawMaterial;
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
		super(mlNodeService);
		this.alfrescoRepository = alfrescoRepository;
		this.associationService = associationService;
	}

	private String ingDefaultFormat = "{0} [{3}]";
	private String groupDefaultFormat = "<b>{0}:</b> {2}";
	private String groupListDefaultFormat = "<b>{0}</b>";
	private String detailsDefaultFormat = "{0} ({2}) [{3}]";
	private String ingTypeDefaultFormat = "{0}: {2} [{3}]";
	private String ingTypeDecThresholdFormat = "{0} [{3}]";
	private String subIngsDefaultFormat = "{0} ({2}) [{3}]";
	private String allergenReplacementPattern = "<b>$1</b>";
	private String htmlTableRowFormat = "<tr><td style=\"border: solid 1px !important;padding: 5px;\" >{0}</td>"
			+ "<td style=\"border: solid 1px !important;padding: 5px;\" >{2}</td>"
			+ "<td style=\"border: solid 1px !important;padding: 5px;text-align:center;\">{1,number,0.#%}</td></tr>";

	private String defaultSeparator = RepoConsts.LABEL_SEPARATOR;
	private String groupDefaultSeparator = RepoConsts.LABEL_SEPARATOR;
	private String ingTypeDefaultSeparator = RepoConsts.LABEL_SEPARATOR;
	private String allergensSeparator = RepoConsts.LABEL_SEPARATOR;
	private String geoOriginsSeparator = RepoConsts.LABEL_SEPARATOR;

	private boolean showIngCEECode = false;
	private boolean useVolume = false;
	private boolean ingsLabelingWithYield = false;
	private boolean uncapitalizeLegalName = false;
	private boolean shouldBreakIngType = false;
	private boolean labelingByLanguage = false;
	private boolean force100Perc = false;

	private Double yield = null;

	/**
	 * Use to disable allergen detection in legalName - Comma separated list of
	 * locale codes - Empty for all allergens - Wildcard (*) can be used for
	 * disable all Language
	 */
	private String disableAllergensForLocales = "";

	private Double qtyPrecisionThreshold = (1d / (PRECISION_FACTOR * PRECISION_FACTOR));

	private Integer maxPrecision = 4;

	public boolean isShouldBreakIngType() {
		return shouldBreakIngType;
	}

	public void setShouldBreakIngType(boolean shouldBreakIngType) {
		this.shouldBreakIngType = shouldBreakIngType;
	}

	public void setDisableAllergensForLocales(String disableAllergensForLocales) {
		this.disableAllergensForLocales = disableAllergensForLocales;
	}

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

	public Double getYield() {
		return yield;
	}

	public void setYield(Double yield) {
		this.yield = yield;
		this.ingsLabelingWithYield = true;
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

	public void setHtmlTableRowFormat(String htmlTableRowFormat) {
		this.htmlTableRowFormat = htmlTableRowFormat;
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

	public void setAllergensSeparator(String allergensSeparator) {
		this.allergensSeparator = allergensSeparator;
	}

	public void setGeoOriginsSeparator(String geoOriginsSeparator) {
		this.geoOriginsSeparator = geoOriginsSeparator;
	}

	public void setAllergenReplacementPattern(String allergenReplacementPattern) {
		this.allergenReplacementPattern = allergenReplacementPattern;
	}

	public void setQtyPrecisionThreshold(Double qtyPrecisionThreshold) {
		this.qtyPrecisionThreshold = qtyPrecisionThreshold;
	}

	public void setMaxPrecision(Integer maxPrecision) {
		this.maxPrecision = maxPrecision;
	}

	public boolean isLabelingByLanguage() {
		return labelingByLanguage;
	}

	public void setLabelingByLanguage(boolean labelingByLanguage) {
		this.labelingByLanguage = labelingByLanguage;
	}

	public void setForce100Perc(boolean force100Perc) {
		this.force100Perc = force100Perc;
	}

	@Override
	void updateDefaultFormat(String textFormat) {

		ingDefaultFormat = textFormat;
		detailsDefaultFormat = textFormat;

	}

	/* formaters */

	private MessageFormat getIngTextFormat(LabelingComponent lblComponent, Double qty) {

		if (textFormaters.containsKey(lblComponent.getNodeRef())) {
			TextFormatRule textFormatRule = textFormaters.get(lblComponent.getNodeRef());
			if (textFormatRule.matchLocale(I18NUtil.getLocale())) {
				return applyRoundingMode(new MessageFormat(textFormatRule.getTextFormat(), I18NUtil.getContentLocale()), qty);
			}
		}

		if (lblComponent instanceof CompositeLabeling) {
			if (((CompositeLabeling) lblComponent).isGroup()) {
				return applyRoundingMode(new MessageFormat(groupDefaultFormat, I18NUtil.getContentLocale()), qty);
			}
			if (DeclarationType.Detail.equals(((CompositeLabeling) lblComponent).getDeclarationType())) {
				if (lblComponent instanceof IngItem) {
					if (!((CompositeLabeling) lblComponent).getIngList().isEmpty()) {
						return applyRoundingMode(new MessageFormat(subIngsDefaultFormat, I18NUtil.getContentLocale()), qty);
					}
				}
				return applyRoundingMode(new MessageFormat(detailsDefaultFormat, I18NUtil.getContentLocale()), qty);
			}

			return applyRoundingMode(new MessageFormat(ingDefaultFormat, I18NUtil.getContentLocale()), qty);
		} else if (lblComponent instanceof IngTypeItem) {

			boolean doNotDetailsDeclType = isDoNotDetails(
					((IngTypeItem) lblComponent).getOrigNodeRef() != null ? ((IngTypeItem) lblComponent).getOrigNodeRef()
							: lblComponent.getNodeRef());

			if (doNotDetailsDeclType || (((((IngTypeItem) lblComponent)).getDecThreshold() != null)
					&& ((((IngTypeItem) lblComponent)).getQty() <= ((((IngTypeItem) lblComponent)).getDecThreshold() / 100)))) {
				return applyRoundingMode(new MessageFormat(ingTypeDecThresholdFormat, I18NUtil.getContentLocale()), qty);
			}
			return applyRoundingMode(new MessageFormat(ingTypeDefaultFormat, I18NUtil.getContentLocale()), qty);
		}

		return applyRoundingMode(new MessageFormat(ingDefaultFormat, I18NUtil.getContentLocale()), qty);
	}

	private MessageFormat applyRoundingMode(MessageFormat messageFormat, Double qty) {
		return applyRoundingMode(messageFormat, qty, false);
	}

	private MessageFormat applyTotalRoundingMode(MessageFormat messageFormat) {
		return applyRoundingMode(messageFormat, totalPrecision, true);
	}

	private MessageFormat applyRoundingMode(MessageFormat messageFormat, Double qty, boolean useTotalPrecision) {
		if (messageFormat.getFormats() != null) {
			for (Format format : messageFormat.getFormats()) {
				if (format instanceof DecimalFormat) {
					applyAutomaticPrecicion(((DecimalFormat) format), qty, defaultRoundingMode, useTotalPrecision);
				}
			}
		}
		return messageFormat;
	}

	private void applyAutomaticPrecicion(DecimalFormat decimalFormat, Double qty, RoundingMode roundingMode, boolean useTotalPrecision) {

		RoundingMode maxRoundingMode = RoundingMode.FLOOR;
		if (useTotalPrecision) {
			maxRoundingMode = RoundingMode.HALF_UP;
		}
		decimalFormat.setRoundingMode(roundingMode);
		if ((qty != null) && (qty > -1) && (qty != 0d)) {
			int maxNum = decimalFormat.getMaximumFractionDigits();
			while (((Math.pow(10, maxNum + 2) * qty) < 1)) {
				if (maxNum >= maxPrecision) {
					decimalFormat.setRoundingMode(maxRoundingMode);
					decimalFormat.setMaximumFractionDigits(maxNum);
					decimalFormat.setMinimumFractionDigits(maxNum);
					break;
				}
				maxNum++;
			}
			decimalFormat.setMaximumFractionDigits(maxNum);
		}

	}

	private String getName(LabelingComponent lblComponent) {
		if (lblComponent instanceof IngItem) {
			return ((IngItem) lblComponent).getCharactName();
		}
		return lblComponent.getName();
	}

	public String getLegalIngName(LabelingComponent lblComponent) {
		String ingLegalName = lblComponent.getLegalName(I18NUtil.getLocale());

		if (renameRules.containsKey(lblComponent.getNodeRef())) {
			RenameRule renameRule = renameRules.get(lblComponent.getNodeRef());
			if (renameRule.matchLocale(I18NUtil.getLocale())) {
				ingLegalName = renameRule.getClosestValue(I18NUtil.getLocale(), false);
			}
		}

		return ingLegalName;
	}

	private String getLegalIngName(LabelingComponent lblComponent, Double qty, boolean plural, boolean useTotalPrecision) {

		if ((lblComponent instanceof IngTypeItem) && ((IngTypeItem) lblComponent).doNotDeclare()) {
			return null;
		}

		boolean isPlural = (lblComponent.isPlural() || (plural && (lblComponent instanceof IngTypeItem)));

		String ingLegalName = isPlural ? lblComponent.getPluralLegalName(I18NUtil.getLocale()) : lblComponent.getLegalName(I18NUtil.getLocale());

		if (renameRules.containsKey(lblComponent.getNodeRef())) {
			RenameRule renameRule = renameRules.get(lblComponent.getNodeRef());
			if (renameRule.matchLocale(I18NUtil.getLocale())) {
				ingLegalName = renameRule.getClosestValue(I18NUtil.getLocale(), isPlural);
			}
		} else {

			if (showIngCEECode && (lblComponent instanceof IngItem)) {
				if ((((IngItem) lblComponent).getIngCEECode() != null) && !((IngItem) lblComponent).getIngCEECode().isEmpty()) {
					ingLegalName = ((IngItem) lblComponent).getIngCEECode();
				}
			}
		}

		if (uncapitalizeLegalName) {
			ingLegalName = uncapitalize(ingLegalName);
		}

		if (!lblComponent.getAllergens().isEmpty()) {
			if (((lblComponent instanceof CompositeLabeling) && ((CompositeLabeling) lblComponent).getIngList().isEmpty())
					|| (lblComponent instanceof IngItem)) {
				ingLegalName = createAllergenAwareLabel(ingLegalName, lblComponent.getAllergens());
			}
		}

		if (qty != null) {
			ingLegalName = createPercAwareLabel(lblComponent, ingLegalName, qty, useTotalPrecision);
		}

		return ingLegalName;
	}

	private String createPercAwareLabel(LabelingComponent lblComponent, String ingLegalName, Double qty, boolean useTotalPrecision) {
		if (qty != null) {
			Pair<DecimalFormat, RoundingMode> decimalFormat = getDecimalFormat(lblComponent);
			if (decimalFormat != null) {

				applyAutomaticPrecicion(decimalFormat.getFirst(), useTotalPrecision ? totalPrecision : qty, decimalFormat.getSecond(),
						useTotalPrecision);

				ingLegalName = ingLegalName + " " + decimalFormat.getFirst().format(qty);
			}
		}

		return ingLegalName;
	}

	private Pair<DecimalFormat, RoundingMode> getDecimalFormat(LabelingComponent lblComponent) {
		DecimalFormat decimalFormat = null;
		RoundingMode roundingMode = defaultRoundingMode;

		DecimalFormatSymbols symbols = new DecimalFormatSymbols(I18NUtil.getContentLocale());
		if ((lblComponent != null)) {
			if (showAllPerc) {
				if (lblComponent instanceof IngTypeItem) {
					NodeRef ingItemNodeRef = ((IngTypeItem) lblComponent).getOrigNodeRef() != null ? ((IngTypeItem) lblComponent).getOrigNodeRef()
							: lblComponent.getNodeRef();
					if (isDoNotDetails(ingItemNodeRef) || showPercRules.containsKey(ingItemNodeRef)) {
						decimalFormat = new DecimalFormat(defaultPercFormat, symbols);
					}
				} else {
					decimalFormat = new DecimalFormat(defaultPercFormat, symbols);
				}

			} else if (showPercRules.containsKey(lblComponent.getNodeRef())) {
				ShowRule showRule = showPercRules.get(lblComponent.getNodeRef());
				if (showRule.matchLocale(I18NUtil.getLocale())) {
					decimalFormat = new DecimalFormat((showRule.format != null) && !showRule.format.isEmpty() ? showRule.format : defaultPercFormat,
							symbols);
					
					if(showRule.roundingMode!=null) {
						roundingMode = showRule.roundingMode;
					}
				}

			}

		}
		if (decimalFormat != null) {
			return new Pair<>(decimalFormat, roundingMode);
		}

		return null;
	}

	private String uncapitalize(String legalName) {
		if ((legalName == null) || legalName.isEmpty() || Pattern.compile("^([A-Z]{2}|[A-Z][1-9]|[A-Z]\\-|[A-Z]_).*$").matcher(legalName).find()) {
			return legalName;
		}
		return StringUtils.uncapitalize(legalName);
	}

	public static Pattern ALLERGEN_DETECTION_PATTERN = Pattern.compile("<b>|<u>|<i>|[A-Z]{3}|\\p{Lu}{3}");

	private String createAllergenAwareLabel(String ingLegalName, Set<NodeRef> allergens) {

		if (ALLERGEN_DETECTION_PATTERN.matcher(ingLegalName).find() || isAllergensDisableForLocale()) {
			return ingLegalName;
		}

		StringBuilder ret = new StringBuilder();
		for (NodeRef allergen : allergens) {
			if (!isAllergenDisableForLocale(allergen)) {
				boolean shouldAppend = true;
				if (getAllergens().contains(allergen)) {
					String allergenName = getCharactName(allergen);
					if ((allergenName != null) && !allergenName.isEmpty()) {
						if (ret.length() > 0) {
							ret.append(allergensSeparator);
						} else {
							Matcher ma = Pattern.compile("\\b(" + Pattern.quote(allergenName) + "(s?))\\b", Pattern.CASE_INSENSITIVE)
									.matcher(ingLegalName);
							if (ma.find() && (ma.group(1) != null)) {
								ingLegalName = ma.replaceAll(allergenReplacementPattern);
								shouldAppend = false;
							} else {
								for (NodeRef subAllergen : associationService.getTargetAssocs(allergen, PLMModel.ASSOC_ALLERGENSUBSETS)) {
									String subAllergenName = uncapitalize(getCharactName(subAllergen));
									if ((subAllergenName != null) && !subAllergenName.isEmpty()) {
										ma = Pattern.compile("\\b(" + Pattern.quote(subAllergenName) + "(s?))\\b", Pattern.CASE_INSENSITIVE)
												.matcher(ingLegalName);
										if (ma.find() && (ma.group(1) != null)) {
											ingLegalName = ma.replaceAll(allergenReplacementPattern);
											shouldAppend = false;
										}
									}
								}
							}
						}
						if (shouldAppend) {
							ret.append(allergenName.replaceFirst("(.*)", allergenReplacementPattern));
						}
					}
				}
			}
		}
		return applyRoundingMode(new MessageFormat(detailsDefaultFormat), null).format(new Object[] { ingLegalName, null, ret.toString(), null });
	}

	Set<Locale> disableAllergensForLocalesCache = null;

	private boolean isAllergensDisableForLocale() {

		if (disableAllergensForLocales.isEmpty()) {
			return false;
		}

		if ("*".equals(disableAllergensForLocales)) {
			return true;
		}

		if (disableAllergensForLocalesCache == null) {
			disableAllergensForLocalesCache = MLTextHelper.extractLocales(Arrays.asList(disableAllergensForLocales.split(",")));
		}

		return disableAllergensForLocalesCache.contains(I18NUtil.getLocale());
	}

	@SuppressWarnings("unchecked")
	private boolean isAllergenDisableForLocale(NodeRef allergen) {
		if (mlNodeService.hasAspect(allergen, ReportModel.ASPECT_REPORT_LOCALES)) {
			List<String> langs = (List<String>) mlNodeService.getProperty(allergen, ReportModel.PROP_REPORT_LOCALES);
			if ((langs != null) && !langs.isEmpty()) {
				return !MLTextHelper.extractLocales(langs).contains(I18NUtil.getLocale());
			}
		}
		return false;
	}

	private String createAllergenAwareLabel(String ingLegalName, List<LabelingComponent> ingList) {
		Set<NodeRef> allergens = new HashSet<>();
		for (LabelingComponent ing : ingList) {
			for (NodeRef allergen : ing.getAllergens()) {
				allergens.add(allergen);
			}
		}

		return createAllergenAwareLabel(ingLegalName, allergens);
	}

	private String getCharactName(NodeRef charact) {

		MLText legalName = (MLText) mlNodeService.getProperty(charact, BeCPGModel.PROP_LEGAL_NAME);

		String ret = MLTextHelper.getClosestValue(legalName, I18NUtil.getLocale());

		if ((ret == null) || ret.isEmpty()) {
			legalName = (MLText) mlNodeService.getProperty(charact, BeCPGModel.PROP_CHARACT_NAME);

			ret = MLTextHelper.getClosestValue(legalName, I18NUtil.getLocale());
		}

		return ret;

	}

	public String render() {
		return render(true);
	}

	public String render(boolean showGroup) {

		if (logger.isTraceEnabled()) {
			logger.trace(" Render label (showGroup:" + showGroup + "): ");
		}

		BigDecimal total = null;
		if (showGroup) {
			if (force100Perc) {
				total = getTotal(lblCompositeContext);
			}

			return renderCompositeIng(lblCompositeContext, 1d, total, false);
		} else {
			if (force100Perc) {
				total = getTotal(mergedLblCompositeContext);
			}

			return renderCompositeIng(mergedLblCompositeContext, 1d, total, false);
		}

	}

	public String renderGroupList() {
		StringBuffer ret = new StringBuffer();

		if (logger.isTraceEnabled()) {
			logger.trace(" Render Group list ");
		}

		List<LabelingComponent> components = new LinkedList<>(lblCompositeContext.getIngList().values());
		Collections.sort(components);

		for (LabelingComponent component : components) {

			Double qtyPerc = computeQtyPerc(lblCompositeContext, component, 1d);

			String ingName = getLegalIngName(component, qtyPerc, false, false);

			if (isGroup(component)) {
				if (ret.length() > 0) {
					ret.append(groupDefaultSeparator);
				}
				ret.append(applyRoundingMode(new MessageFormat(groupListDefaultFormat), qtyPerc).format(new Object[] { ingName, qtyPerc }));
			}
		}

		return cleanLabel(ret);
	}

	public String renderAllergens() {
		return renderAllergens(this.allergens);
	}

	public String renderInvoluntaryAllergens() {
		return renderAllergens(this.inVolAllergens);

	}

	public String renderInvoluntaryAllergenInProcess() {
		return renderAllergens(this.inVolAllergensProcess);

	}

	public String renderInvoluntaryInRawMaterial() {
		return renderAllergens(this.inVolAllergensRawMaterial);
	}

	public String renderAllergens(Set<NodeRef> allergensList) {
		StringBuffer ret = new StringBuffer();

		if (logger.isTraceEnabled()) {
			logger.trace(" Render Allergens list ");
		}

		for (NodeRef allergen : allergensList) {
			if (!isAllergenDisableForLocale(allergen)) {
				if (ret.length() > 0) {
					ret.append(allergensSeparator);
				}
				ret.append(getCharactName(allergen));
			}
		}

		return ret.toString();

	}

	public String renderAsHtmlTable() {
		return renderAsHtmlTable("border-collapse:collapse", false);
	}

	public String renderAsHtmlTable(String styleCss, boolean showTotal) {
		return renderAsHtmlTable(styleCss, showTotal, false);
	}

	public String renderAsHtmlTable(String styleCss, boolean showTotal, boolean force100Perc) {
		StringBuffer ret = new StringBuffer();
		StringBuffer tableContent = new StringBuffer();

		BigDecimal total = new BigDecimal(0d);

		ret.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"" + styleCss + "\" rules=\"none\">");

		boolean first = true;
		String firstLabel = "";
		Double firstQtyPerc = 0d;
		String firstGeo = "";

		for (Map.Entry<IngTypeItem, List<LabelingComponent>> kv : getSortedIngListByType(lblCompositeContext).entrySet()) {

			if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), null, false, false) != null)) {

				Double qtyPerc = computeQtyPerc(lblCompositeContext, kv.getKey(), 1d);
				Double volumePerc = computeVolumePerc(lblCompositeContext, kv.getKey(), 1d);
				qtyPerc = (useVolume ? volumePerc : qtyPerc);

				String ingTypeLegalName = getLegalIngName(kv.getKey(), null,
						((kv.getValue().size() > 1) || (!kv.getValue().isEmpty() && kv.getValue().get(0).isPlural())), false);

				boolean doNotDetailsDeclType = isDoNotDetails(
						kv.getKey().getOrigNodeRef() != null ? kv.getKey().getOrigNodeRef() : kv.getKey().getNodeRef());

				if (doNotDetailsDeclType) {
					ingTypeLegalName = createAllergenAwareLabel(ingTypeLegalName, kv.getValue());
				}

				String geoOriginsLabel = createGeoOriginsLabel(null, kv.getValue());

				String subLabel = getIngTextFormat(kv.getKey(), qtyPerc).format(new Object[] { ingTypeLegalName, null,
						doNotDetailsDeclType ? null : renderLabelingComponent(lblCompositeContext, kv.getValue(), true, 1d, null, true), null });

				if ((subLabel != null) && !subLabel.isEmpty()) {

					if (first) {

						first = false;
						firstLabel = subLabel;
						firstQtyPerc = qtyPerc;
						firstGeo = geoOriginsLabel != null ? geoOriginsLabel : "";
					} else {
						tableContent.append(applyRoundingMode(new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale()), qtyPerc)
								.format(new Object[] { subLabel, qtyPerc, geoOriginsLabel != null ? geoOriginsLabel : "" }));
					}
					if (qtyPerc != null) {
						total = total.add(roundeedValue(qtyPerc, new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale())));
					}

				}

			} else {

				for (LabelingComponent component : kv.getValue()) {

					Double qtyPerc = computeQtyPerc(lblCompositeContext, component, 1d);
					Double volumePerc = computeVolumePerc(lblCompositeContext, component, 1d);

					String ingName = getLegalIngName(component, null, false, false);
					String geoOriginsLabel = createGeoOriginsLabel(null, component.getGeoOrigins());

					qtyPerc = (useVolume ? volumePerc : qtyPerc);

					if (!shouldSkip(component.getNodeRef(), qtyPerc)) {

						String subLabel = new String();
 if (component instanceof CompositeLabeling) {

							Double subRatio = qtyPerc;
							if (DeclarationType.Kit.equals(((CompositeLabeling) component).getDeclarationType())) {
								subRatio = 1d;
							}

							subLabel = getIngTextFormat(component, qtyPerc).format(
									new Object[] { ingName, qtyPerc, renderCompositeIng((CompositeLabeling) component, subRatio, null, true), null });

						} else {
							logger.error("Unsupported ing type. Name: " + component.getName());
						}

						if ((subLabel != null) && !subLabel.toString().isEmpty()) {
							if (first) {
								first = false;
								firstLabel = subLabel;
								firstQtyPerc = qtyPerc;
								firstGeo = geoOriginsLabel != null ? geoOriginsLabel : "";
							} else {
								tableContent.append(applyRoundingMode(new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale()), qtyPerc)
										.format(new Object[] { subLabel, qtyPerc, geoOriginsLabel != null ? geoOriginsLabel : "" }));
							}

							if (qtyPerc != null) {
								total = total.add(roundeedValue(qtyPerc, new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale())));
							}
						}

					} else {
						logger.debug("Removing ing with qty of 0: " + ingName);
					}

				}
			}

		}

		if (force100Perc) {

			BigDecimal diffValue = (new BigDecimal(1d)).subtract(total);

			total = new BigDecimal(1);

			firstQtyPerc = roundeedValue(firstQtyPerc, new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale())).add(diffValue)
					.doubleValue();

			ret.append(applyTotalRoundingMode(new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale()))
					.format(new Object[] { firstLabel, firstQtyPerc, firstGeo }));
		} else {
			ret.append(applyRoundingMode(new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale()), firstQtyPerc)
					.format(new Object[] { firstLabel, firstQtyPerc, firstGeo }));
		}

		ret.append(tableContent);

		if (showTotal && (total.doubleValue() > 0)) {
			ret.append(applyTotalRoundingMode(new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale()))
					.format(new Object[] { "<b>" + I18NUtil.getMessage("entity.datalist.item.details.total") + "</b>", total.doubleValue(), "" }));
		}

		ret.append("</table>");
		return cleanLabel(ret);

	}

	public String renderAsFlatHtmlTable() {
		return renderAsFlatHtmlTable("border-collapse:collapse", false);
	}

	public String renderAsFlatHtmlTable(String styleCss, boolean showTotal) {
		return renderAsFlatHtmlTable(styleCss, showTotal, false);
	}

	private class HtmlTableStruct {
		String label;
		Double qtyPerc;
		String geoOriginsLabel;
		Integer level;

		public HtmlTableStruct(String label, Double qtyPerc, String geoOriginsLabel, Integer level) {
			super();
			this.label = label;
			this.geoOriginsLabel = geoOriginsLabel;
			this.qtyPerc = qtyPerc;
			this.level = level;
		}

	}

	public String renderAsFlatHtmlTable(String styleCss, boolean showTotal, boolean force100Perc) {

		BigDecimal total = new BigDecimal(0d);

		StringBuffer tableContent = new StringBuffer();
		StringBuffer ret = new StringBuffer();

		try {
			shouldBreakIngType = true;

			tableContent.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"" + styleCss + "\" rules=\"none\">");

			List<HtmlTableStruct> flatList = flatCompositeLabeling(lblCompositeContext, 1d, 0);
			if (flatList.size() > 0) {

				boolean first = true;
				for (HtmlTableStruct tmp : flatList) {

					if (first && force100Perc) {
						first = false;
					} else {

						if (tmp.level == 0) {
							total = total.add(new BigDecimal(tmp.qtyPerc));
						}

						ret.append(applyRoundingMode(new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale()), tmp.qtyPerc)
								.format(new Object[] { indent(tmp.label, tmp.level), tmp.qtyPerc, tmp.geoOriginsLabel }));
					}
				}

				if (force100Perc) {
					BigDecimal diffValue = (new BigDecimal(1d)).subtract(total);

					total = new BigDecimal(1);

					Double qtyPerc = roundeedValue(flatList.get(0).qtyPerc, new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale()))
							.add(diffValue).doubleValue();

					tableContent.append(applyTotalRoundingMode(new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale()))
							.format(new Object[] { flatList.get(0).label, qtyPerc, flatList.get(0).geoOriginsLabel }));

				}
				tableContent.append(ret);
			}

			if (showTotal && (total.doubleValue() > 0)) {
				tableContent.append(applyTotalRoundingMode(new MessageFormat(htmlTableRowFormat, I18NUtil.getContentLocale())).format(
						new Object[] { "<b>" + I18NUtil.getMessage("entity.datalist.item.details.total") + "</b>", total.doubleValue(), "" }));
			}

			tableContent.append("</table>");

			return cleanLabel(tableContent);
		} finally {
			shouldBreakIngType = false;
		}

	}

	private String indent(String label, Integer level) {
		if ((level != null) && (level > 0)) {
			String indent = "";
			for (int i = 0; i < level; i++) {
				indent += "&nbsp;&nbsp;";
			}
			return indent + label;

		}

		return label;
	}

	private List<HtmlTableStruct> flatCompositeLabeling(CompositeLabeling parent, Double ratio, Integer level) {
		List<HtmlTableStruct> ret = new LinkedList<>();

		for (Map.Entry<IngTypeItem, List<LabelingComponent>> kv : getSortedIngListByType(parent).entrySet()) {

			for (LabelingComponent component : kv.getValue()) {

				Double qtyPerc = computeQtyPerc(parent, component, ratio);
				Double volumePerc = computeVolumePerc(parent, component, ratio);

				qtyPerc = (useVolume ? volumePerc : qtyPerc);

				String ingName = getLegalIngName(component, qtyPerc, false, false);

				if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), null, false, false) != null)) {
					boolean doNotDetailsDeclType = isDoNotDetails(
							kv.getKey().getOrigNodeRef() != null ? kv.getKey().getOrigNodeRef() : kv.getKey().getNodeRef());

					String ingTypeLegalName = getLegalIngName(kv.getKey(), null,
							((kv.getValue().size() > 1) || (!kv.getValue().isEmpty() && kv.getValue().get(0).isPlural())), false);

					if (doNotDetailsDeclType) {
						ingTypeLegalName = createAllergenAwareLabel(ingTypeLegalName, kv.getValue());
					}

					ingName = getIngTextFormat(kv.getKey(), qtyPerc)
							.format(new Object[] { ingTypeLegalName, null, doNotDetailsDeclType ? null : ingName, null });

				}

				String geoOriginsLabel = createGeoOriginsLabel(component.getNodeRef(), component.getGeoOrigins());

				if (!shouldSkip(component.getNodeRef(), qtyPerc)) {
if (component instanceof CompositeLabeling) {

						Double subRatio = qtyPerc;
						if (DeclarationType.Kit.equals(((CompositeLabeling) component).getDeclarationType())) {
							subRatio = 1d;
						}

						ret.add(new HtmlTableStruct(ingName, qtyPerc, geoOriginsLabel != null ? geoOriginsLabel : "", level));

						ret.addAll(flatCompositeLabeling((CompositeLabeling) component, subRatio, level + 1));

					} else {
						logger.error("Unsupported ing type. Name: " + component.getName());
					}

				} else {
					logger.debug("Removing ing with qty of 0: " + ingName);
				}

			}

		}
		return ret;
	}

	private boolean isDoNotDetails(NodeRef nodeRef) {

		Locale currentLocale = I18NUtil.getLocale();
		if (nodeDeclarationFilters.containsKey(nodeRef)) {
			for (DeclarationFilter declarationFilter : nodeDeclarationFilters.get(nodeRef)) {
				if (DeclarationType.DoNotDetails.equals(declarationFilter.getDeclarationType())
						&& matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext())
						&& declarationFilter.matchLocale(currentLocale)) {
					return true;
				}
			}

		}
		return false;
	}

	private BigDecimal roundeedValue(Double qty, LabelingComponent lblComponent) {
		Pair<DecimalFormat, RoundingMode> ret = getDecimalFormat(lblComponent);
		if (ret != null) {
			return roundeedValue(qty, ret.getFirst(), ret.getSecond());
		}
		return roundeedValue(qty, null, defaultRoundingMode);
	}

	private BigDecimal roundeedValue(Double qty, DecimalFormat decimalFormat, RoundingMode roundingMode) {
		if (decimalFormat == null) {
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(I18NUtil.getContentLocale());
			decimalFormat = new DecimalFormat(defaultPercFormat, symbols);
		}
		decimalFormat.setRoundingMode(roundingMode);
		if ((qty != null) && (qty > -1) && (qty != 0d)) {
			int maxNum = decimalFormat.getMaximumFractionDigits();
			while (((Math.pow(10, maxNum + 2) * qty) < 1)) {
				if (maxNum >= maxPrecision) {
					decimalFormat.setRoundingMode(RoundingMode.FLOOR);
					decimalFormat.setMaximumFractionDigits(maxNum);
					break;
				}
				maxNum++;
			}
			decimalFormat.setMaximumFractionDigits(maxNum);

			String roundedQty = decimalFormat.format(qty);
			try {

				qty = decimalFormat.parse(roundedQty).doubleValue();
				return new BigDecimal(qty);

			} catch (ParseException e) {
				logger.error(e, e);
			}
		}
		return new BigDecimal(qty != null ? qty : 0d);

	}

	private BigDecimal roundeedValue(Double qty, MessageFormat messageFormat) {

		for (Format format : messageFormat.getFormats()) {
			if (format instanceof DecimalFormat) {
				return roundeedValue(qty, (DecimalFormat) format, defaultRoundingMode);
			}
		}
		return new BigDecimal(qty);

	}

	private BigDecimal getTotal(CompositeLabeling compositeLabeling) {
		BigDecimal total = new BigDecimal(0d);

		for (Map.Entry<IngTypeItem, List<LabelingComponent>> kv : getSortedIngListByType(compositeLabeling).entrySet()) {

			if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), null, false, false) != null)) {

				Double qtyPerc = computeQtyPerc(compositeLabeling, kv.getKey(), 1d);
				Double volumePerc = computeVolumePerc(compositeLabeling, kv.getKey(), 1d);
				qtyPerc = (useVolume ? volumePerc : qtyPerc);

				if (qtyPerc != null) {
					total = total.add(roundeedValue(qtyPerc, kv.getKey()));
				}

			} else {

				for (LabelingComponent component : kv.getValue()) {

					Double qtyPerc = computeQtyPerc(compositeLabeling, component, 1d);
					Double volumePerc = computeVolumePerc(compositeLabeling, component, 1d);

					qtyPerc = (useVolume ? volumePerc : qtyPerc);

					if (!shouldSkip(component.getNodeRef(), qtyPerc)) {

						if (qtyPerc != null) {
							total = total.add(roundeedValue(qtyPerc, component));
						}
					}
				}
			}
		}
		return total;

	}

	private String renderCompositeIng(CompositeLabeling compositeLabeling, Double ratio, BigDecimal total, boolean hideGeo) {
		StringBuffer ret = new StringBuffer();
		boolean appendEOF = false;
		boolean first = true;

		boolean applySeparatorRule = true;

		for (Map.Entry<IngTypeItem, List<LabelingComponent>> kv : getSortedIngListByType(compositeLabeling).entrySet()) {

			StringBuilder toAppend = new StringBuilder();

			Double qtyPerc = null;
			if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), null, false, false) != null)) {

				qtyPerc = computeQtyPerc(compositeLabeling, kv.getKey(), ratio);
				Double volumePerc = computeVolumePerc(compositeLabeling, kv.getKey(), ratio);
				qtyPerc = (useVolume ? volumePerc : qtyPerc);
				kv.getKey().setQty(qtyPerc);
				kv.getKey().setVolume(volumePerc);

				String ingTypeLegalName = getLegalIngName(kv.getKey(), qtyPerc,
						((kv.getValue().size() > 1) || (!kv.getValue().isEmpty() && kv.getValue().get(0).isPlural())), false);

				boolean doNotDetailsDeclType = isDoNotDetails(
						kv.getKey().getOrigNodeRef() != null ? kv.getKey().getOrigNodeRef() : kv.getKey().getNodeRef());

				if (doNotDetailsDeclType) {
					ingTypeLegalName = createAllergenAwareLabel(ingTypeLegalName, kv.getValue());
				}

				String geoOriginsLabel = createGeoOriginsLabel(kv.getKey().getNodeRef(), kv.getValue());

				toAppend.append(getIngTextFormat(kv.getKey(), qtyPerc).format(new Object[] { ingTypeLegalName, qtyPerc,
						doNotDetailsDeclType ? null
								: renderLabelingComponent(compositeLabeling, kv.getValue(), true, ratio, first ? total : null, hideGeo),
						hideGeo ? null : geoOriginsLabel }));

			} else {
				if (!kv.getValue().isEmpty()) {
					qtyPerc = computeQtyPerc(compositeLabeling, kv.getValue().get(0), ratio);
					Double volumePerc = computeVolumePerc(compositeLabeling, kv.getValue().get(0), ratio);
					qtyPerc = (useVolume ? volumePerc : qtyPerc);
				}

				toAppend.append(renderLabelingComponent(compositeLabeling, kv.getValue(), false, ratio, first ? total : null, hideGeo));
			}

			first = false;
			if ((toAppend != null) && !toAppend.toString().isEmpty()) {
				if (ret.length() > 0) {
					if (appendEOF) {
						ret.append("<br/>");
					} else {
						if ((separatorRules != null) && !separatorRules.isEmpty() && applySeparatorRule) {

							for (SeparatorRule separatorRule : separatorRules) {
								if (separatorRule.matchLocale(I18NUtil.getLocale()) && (qtyPerc != null)
										&& (qtyPerc <= separatorRule.getThreshold())) {
									ret.append(separatorRule.getClosestValue(I18NUtil.getLocale()));
									applySeparatorRule = false;
									break;
								}
							}
							if (applySeparatorRule) {
								ret.append(defaultSeparator);
							}
						} else {

							ret.append(defaultSeparator);
						}
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

	Double totalPrecision = 1 / Math.pow(10, maxPrecision + 2);

	private StringBuilder renderLabelingComponent(CompositeLabeling parent, List<LabelingComponent> subComponents, boolean isIngType, Double ratio,
			BigDecimal total, boolean hideGeo) {

		StringBuilder ret = new StringBuilder();

		boolean appendEOF = false;
		boolean first = true;

		for (LabelingComponent component : subComponents) {

			Double qtyPerc = computeQtyPerc(parent, component, ratio);
			Double volumePerc = computeVolumePerc(parent, component, ratio);

			qtyPerc = (useVolume ? volumePerc : qtyPerc);

			if (first && (total != null)) {
				BigDecimal diffValue = (new BigDecimal(1d)).subtract(total);
				qtyPerc = roundeedValue(qtyPerc, component).add(diffValue).doubleValue();
			}

			String ingName = getLegalIngName(component, qtyPerc, false, first && (total != null));

			String geoOriginsLabel = createGeoOriginsLabel(component.getNodeRef(), component.getGeoOrigins());

			if (logger.isDebugEnabled()) {

				logger.debug(" --" + ingName + "(" + component.getNodeRef() + ") qtyRMUsed: " + parent.getQtyTotal() + " qtyPerc " + qtyPerc
						+ " apply precision (" + (toApplyThresholdItems.contains(component.getNodeRef()) && ((qtyPerc - qtyPrecisionThreshold) > 0))
						+ ") ");
			}

			if (!shouldSkip(component.getNodeRef(), qtyPerc)) {

				String toAppend = new String();

				 if (component instanceof CompositeLabeling) {

					MessageFormat formater = getIngTextFormat(component, qtyPerc);
					Double subRatio = qtyPerc;
					if (DeclarationType.Kit.equals(((CompositeLabeling) component).getDeclarationType())) {
						subRatio = 1d;
					} else if (first && (total != null)) {
						applyTotalRoundingMode(formater);
						if (!DeclarationType.Group.equals(((CompositeLabeling) component).getDeclarationType())) {
							first = false;
						}
					}

					toAppend = formater.format(new Object[] { ingName, qtyPerc,
							renderCompositeIng((CompositeLabeling) component, subRatio, first ? total : null, hideGeo),
							hideGeo ? null : geoOriginsLabel });

					first = false;

				} else {
					logger.error("Unsupported ing type. Name: " + component.getName());
				}

				if ((toAppend != null) && !toAppend.isEmpty()) {
					if (ret.length() > 0) {
						if (appendEOF) {
							ret.append("<br/>");
						} else {
							ret.append(isIngType ? ingTypeDefaultSeparator : defaultSeparator);
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

	private String createGeoOriginsLabel(NodeRef nodeRef, List<LabelingComponent> components) {

		if (showAllGeo || ((nodeRef != null) && showGeoRules.containsKey(nodeRef) && showGeoRules.get(nodeRef).matchLocale(I18NUtil.getLocale()))) {

			if ((components != null) && !components.isEmpty()) {

				Set<NodeRef> geoOrigins = new HashSet<>();

				for (LabelingComponent component : components) {
					if (component.getGeoOrigins() != null) {
						geoOrigins.addAll(component.getGeoOrigins());
					}
				}

				return createGeoOriginsLabel(null, geoOrigins);
			}

		}

		return null;
	}

	private String createGeoOriginsLabel(NodeRef nodeRef, Set<NodeRef> geoOrigins) {

		if (showAllGeo || ((nodeRef != null) && showGeoRules.containsKey(nodeRef) && showGeoRules.get(nodeRef).matchLocale(I18NUtil.getLocale()))) {

			if ((geoOrigins != null) && !geoOrigins.isEmpty()) {
				StringBuilder geoOriginsBuffer = new StringBuilder();
				for (NodeRef geoOrigin : geoOrigins) {
					if (geoOriginsBuffer.length() > 0) {
						geoOriginsBuffer.append(geoOriginsSeparator);
					}
					geoOriginsBuffer.append(getCharactName(geoOrigin));
				}
				return geoOriginsBuffer.toString();
			}
		}
		return null;
	}

	private boolean shouldSkip(NodeRef nodeRef, Double qtyPerc) {

		boolean shouldSkip = !((qtyPerc == null) || (toApplyThresholdItems.contains(nodeRef) && (qtyPerc > qtyPrecisionThreshold))
				|| (!toApplyThresholdItems.contains(nodeRef) && (qtyPerc > 0)));

		if (!shouldSkip) {

			Locale currentLocale = I18NUtil.getLocale();

			if (nodeDeclarationFilters.containsKey(nodeRef)) {
				for (DeclarationFilter declarationFilter : nodeDeclarationFilters.get(nodeRef)) {
					if (declarationFilter.isThreshold() && (qtyPerc < (declarationFilter.getThreshold() / 100d))
							&& declarationFilter.matchLocale(currentLocale)) {
						return true;
					}
				}
			}

			for (DeclarationFilter declarationFilter : declarationFilters) {
				if (declarationFilter.isThreshold() && (qtyPerc < (declarationFilter.getThreshold() / 100d))
						&& declarationFilter.matchLocale(currentLocale)) {
					return true;
				}

			}

		}

		return shouldSkip;
	}

	public String createJsonLog(boolean mergedLabeling) {
		if (!mergedLabeling) {
			return createJsonLog(lblCompositeContext, null, null, new HashSet<>()).toString();
		}
		return createJsonLog(mergedLblCompositeContext, null, null, new HashSet<>()).toString();
	}

	@SuppressWarnings("unchecked")
	private JSONObject createJsonLog(LabelingComponent component, Double totalQty, Double totalVol, Set<LabelingComponent> visited) {

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
			tree.put("legal", cleanLabel(getLegalIngName(component, null, false, false)));
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
					allergens.add(getCharactName(allergen));
				}
				tree.put("allergens", allergens);
			}

			if (!component.getGeoOrigins().isEmpty()
					&& (!(component instanceof CompositeLabeling) || ((CompositeLabeling) component).getIngList().isEmpty())) {
				JSONArray geoOrigins = new JSONArray();
				for (NodeRef geoOrigin : component.getGeoOrigins()) {
					geoOrigins.add(getCharactName(geoOrigin));
				}
				tree.put("geoOrigins", geoOrigins);
			}

			if (component instanceof CompositeLabeling) {
				CompositeLabeling composite = (CompositeLabeling) component;

				if (composite.getDeclarationType() != null) {
					tree.put("decl", I18NUtil.getMessage("listconstraint.bcpg_declarationTypes." + composite.getDeclarationType().toString()));
				}

				JSONArray children = new JSONArray();
				for (Map.Entry<IngTypeItem, List<LabelingComponent>> kv : getSortedIngListByType(composite).entrySet()) {

					if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), null, false, false) != null)) {

						JSONObject ingTypeJson = new JSONObject();
						ingTypeJson.put("nodeRef", kv.getKey().getNodeRef().toString());
						ingTypeJson.put("cssClass", "ingType");
						ingTypeJson.put("name", getName(kv.getKey()));
						ingTypeJson.put("legal", cleanLabel(getLegalIngName(kv.getKey(), null,
								(kv.getValue().size() > 1) || (!kv.getValue().isEmpty() && kv.getValue().get(0).isPlural()), false)));

						if ((kv.getKey().getQty() != null) && (totalQty != null) && (totalQty > 0)) {
							ingTypeJson.put("qte", (kv.getKey().getQty() / totalQty) * 100);
						}
						if ((kv.getKey().getVolume() != null) && (totalVol != null) && (totalVol > 0)) {
							ingTypeJson.put("vol", (kv.getKey().getVolume() / totalVol) * 100);

						}
						JSONArray ingTypeJsonChildren = new JSONArray();
						for (LabelingComponent childComponent : kv.getValue()) {
							ingTypeJsonChildren.add(createJsonLog(childComponent, composite.getQtyTotal(), composite.getVolumeTotal(), visited));
						}
						ingTypeJson.put("children", ingTypeJsonChildren);
						children.add(ingTypeJson);
					} else {
						for (LabelingComponent childComponent : kv.getValue()) {
							children.add(createJsonLog(childComponent, composite.getQtyTotal(), composite.getVolumeTotal(), visited));
						}
					}

				}

				tree.put("children", children);

			} 

		}

		return tree;
	}

	private String cleanLabel(String buffer) {
		if (buffer != null) {
			return buffer.replaceAll(" null| \\(null\\)| \\(\\)| \\[null\\]", "").replaceAll(":,", ",").replaceAll(":$", "")
					.replaceAll(">null<", "><").trim();
		}
		return "";
	}

	private String cleanLabel(StringBuffer buffer) {
		return cleanLabel(buffer.toString());
	}

	public boolean isGroup(LabelingComponent component) {
		return (component instanceof CompositeLabeling) && ((CompositeLabeling) component).isGroup();
	}

	public Double computeQtyPerc(CompositeLabeling parent, LabelingComponent component, Double ratio) {
		if (ratio == null) {
			return null;
		}

		Double qty = component.getQty();
		if ((parent.getQtyTotal() != null) && (parent.getQtyTotal() > 0) && (qty != null)) {
			qty = (qty / parent.getQtyTotal()) * ratio;
		}
		return qty;
	}

	public Double computeVolumePerc(CompositeLabeling parent, LabelingComponent component, Double ratio) {
		if (ratio == null) {
			return null;
		}

		Double volume = component.getVolume();
		if ((parent.getVolumeTotal() != null) && (parent.getVolumeTotal() > 0) && (volume != null)) {
			volume = (volume / parent.getVolumeTotal()) * ratio;
		}
		return volume;
	}

	Map<IngTypeItem, List<LabelingComponent>> getSortedIngListByType(CompositeLabeling compositeLabeling) {

		Locale currentLocale = I18NUtil.getLocale();

		Map<IngTypeItem, List<LabelingComponent>> tmp = new LinkedHashMap<>();

		boolean keepOrder = false;
		for (LabelingComponent lblComponent : compositeLabeling.getIngList().values()) {
			IngTypeItem ingType = null;

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
					boolean shouldBreak = false;
					for (DeclarationFilter declarationFilter : nodeDeclarationFilters.get(ingType.getNodeRef())) {
						if (DeclarationType.Omit.equals(declarationFilter.getDeclarationType())
								&& matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext())
								&& declarationFilter.matchLocale(currentLocale)) {
							shouldBreak = true;
							break;
						} else if ((DeclarationType.DoNotDeclare.equals(declarationFilter.getDeclarationType()) && !declarationFilter.isThreshold()
								&& matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext())
								&& declarationFilter.matchLocale(currentLocale))) {
							ingType = null;
							break;
						}
					}
					if (shouldBreak) {
						break;
					}

				} else if (ingType.doNotDeclare() && !ingType.lastGroup()) {
					ingType = null;
				}

			}

			if ((lblComponent.getQty() == null) || (useVolume && (lblComponent.getVolume() == null))) {
				keepOrder = true;
			}

			if ((lblComponent instanceof CompositeLabeling) && ((CompositeLabeling) lblComponent).isGroup()) {
				ingType = IngTypeItem.DEFAULT_GROUP;
			} else if (shouldBreakIngType && (ingType != null)) {

				ingType = ingType.clone();

				if ((ingType.getNodeRef() != null) && (ingType.getOrigNodeRef() == null)) {
					ingType.setOrigNodeRef(ingType.getNodeRef());
				}
				ingType.setNodeRef(new NodeRef(RepoConsts.SPACES_STORE, "ingType-" + lblComponent.getNodeRef().hashCode()));

			}

			if (ingType == null) {
				ingType = new IngTypeItem();
				ingType.setNodeRef(new NodeRef(RepoConsts.SPACES_STORE, "ingType-" + lblComponent.getNodeRef().hashCode()));
			}

			// Reset qty for equality
			ingType.setQty(0d);
			ingType.setVolume(0d);

			List<LabelingComponent> subSortedList = tmp.get(ingType);

			if (subSortedList == null) {
				subSortedList = new LinkedList<>();
				tmp.put(ingType, subSortedList);
			}
			subSortedList.add(lblComponent);

		}

		keepOrder = DeclarationType.Detail.equals(compositeLabeling.getDeclarationType()) && keepOrder;

		List<Map.Entry<IngTypeItem, List<LabelingComponent>>> entries = new ArrayList<>(tmp.entrySet());
		/*
		 * Compute IngType Qty
		 */

		for (Map.Entry<IngTypeItem, List<LabelingComponent>> entry : entries) {
			Double qty = 0d;
			Double vol = 0d;

			for (LabelingComponent lblComponent : entry.getValue()) {
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

				if (a.getKey().lastGroup()) {
					return 1;
				}

				if (b.getKey().lastGroup()) {
					return -1;
				}

				if (useVolume) {
					return b.getKey().getVolume().compareTo(a.getKey().getVolume());
				}

				return b.getKey().getQty().compareTo(a.getKey().getQty());
			});
		}
		Map<IngTypeItem, List<LabelingComponent>> sortedIngListByType = new LinkedHashMap<>();
		for (Map.Entry<IngTypeItem, List<LabelingComponent>> entry : entries) {

			if (!keepOrder) {
				// Sort by value
				Collections.sort(entry.getValue());
			}
			sortedIngListByType.put(entry.getKey(), entry.getValue());
		}

		if (shouldBreakIngType) {

			Map.Entry<IngTypeItem, List<LabelingComponent>> prec = null;
			Set<IngTypeItem> toRemove = new HashSet<>();

			for (Map.Entry<IngTypeItem, List<LabelingComponent>> entry : sortedIngListByType.entrySet()) {

				if (prec != null) {
					if ((prec.getKey().getOrigNodeRef() != null) && prec.getKey().getOrigNodeRef().equals(entry.getKey().getOrigNodeRef())) {

						if ((prec.getKey().getQty() != null) && (entry.getKey().getQty() != null)) {
							prec.getKey().setQty(prec.getKey().getQty() + entry.getKey().getQty());
						}

						if ((prec.getKey().getVolume() != null) && (entry.getKey().getVolume() != null)) {
							prec.getKey().setVolume(prec.getKey().getVolume() + entry.getKey().getVolume());
						}

						prec.getValue().addAll(entry.getValue());
						toRemove.add(entry.getKey());

					} else {
						prec = entry;
					}

				} else {
					prec = entry;
				}

			}

			for (IngTypeItem entry : toRemove) {
				sortedIngListByType.remove(entry);
			}

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

			try {
				ExpressionParser parser = new SpelExpressionParser();
				StandardEvaluationContext dataContext = new StandardEvaluationContext(declarationFilterContext);

				Expression exp = parser.parseExpression(SpelHelper.formatFormula(formula));

				boolean ret = exp.getValue(dataContext, Boolean.class);

				if (ret && logger.isDebugEnabled()) {
					logger.debug("Matching formula :" + formula);
				}

				return ret;
			} catch (SpelParseException | SpelEvaluationException e) {
				logger.error("Cannot evaluate formula :" + formula + " for " + declarationFilterContext.getCompoListDataItem());
				if (logger.isDebugEnabled()) {
					logger.error("Cannot evaluate formula :" + formula + " on " + declarationFilterContext.toString(), e);
				}
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
