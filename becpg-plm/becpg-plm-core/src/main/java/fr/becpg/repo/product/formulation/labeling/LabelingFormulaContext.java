/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StringUtils;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.formulation.spel.BeCPGSpelExpressionParser;
import fr.becpg.repo.formulation.spel.SpelFormulaContext;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.PlaceOfActivityTypeCode;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.ing.CompositeLabeling;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.ing.LabelingComponent;
import fr.becpg.repo.product.data.meat.MeatType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.spel.LabelingFormulaFilterContext;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>
 * LabelingFormulaContext class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LabelingFormulaContext extends RuleParser implements SpelFormulaContext<ProductData> {

	private static final Log logger = LogFactory.getLog(LabelingFormulaContext.class);

	/** Constant <code>DEFAULT_RATIO</code> */
	public static final BigDecimal DEFAULT_RATIO = BigDecimal.valueOf(1d);

	private static final int PRECISION_RATIO = 16;
	/** Constant <code>PRECISION</code> */
	public static final MathContext PRECISION = new MathContext(PRECISION_RATIO, RoundingMode.HALF_UP);

	/** Constant <code>ALLERGEN_DETECTION_PATTERN</code> */
	public static final Pattern ALLERGEN_DETECTION_PATTERN = Pattern.compile(
			"(<\\s*up[^>]*>.*?<\\s*/\\s*up>|<\\s*b[^>]*>.*?<\\s*/\\s*b>|<\\s*u[^>]*>.*?<\\s*/\\s*u>|<\\s*i[^>]*>.*?<\\s*/\\s*i>|[A-Z]{4,}|\\p{Lu}{4,})");

	private static final String UNSUPPORTED_ING_TYPE = "Unsupported ing type. Name: %s";
	private static final String REMOVING_NULL_QTY = "Removing ing with qty of 0: %s";

	private CompositeLabeling lblCompositeContext;

	private CompositeLabeling mergedLblCompositeContext;

	private final AssociationService associationService;

	private final AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private final SpelFormulaService formulaService;

	private List<ReqCtrlListDataItem> errors = new ArrayList<>();

	private Map<Locale, Set<String>> detectedAllergensByLocale = new HashMap<>();

	private Map<NodeRef, Double> allergens = new HashMap<>();
	private Map<NodeRef, Double> inVolAllergens = new HashMap<>();
	private Map<NodeRef, Double> inVolAllergensProcess = new HashMap<>();
	private Map<NodeRef, Double> inVolAllergensRawMaterial = new HashMap<>();

	private Set<FootNoteRule> footNotes = new HashSet<>();

	private Set<NodeRef> toApplyThresholdItems = new HashSet<>();

	// Spel variable
	private Locale locale;

	// Spel variable
	private ProductData entity;

	/**
	 * <p>
	 * Getter for the field <code>locale</code>.
	 * </p>
	 *
	 * @return a {@link java.util.Locale} object.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * <p>
	 * Setter for the field <code>locale</code>.
	 * </p>
	 *
	 * @param locale
	 *            a {@link java.util.Locale} object.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Getter for the field <code>entity</code>.
	 * </p>
	 */
	@Override
	public ProductData getEntity() {
		return entity;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Setter for the field <code>entity</code>.
	 * </p>
	 */
	@Override
	public void setEntity(ProductData entity) {
		this.entity = entity;
	}

	/**
	 * <p>
	 * Getter for the field <code>errors</code>.
	 * </p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<ReqCtrlListDataItem> getErrors() {
		return errors;
	}

	/**
	 * <p>
	 * Setter for the field <code>errors</code>.
	 * </p>
	 *
	 * @param errors
	 *            a {@link java.util.List} object.
	 */
	public void setErrors(List<ReqCtrlListDataItem> errors) {
		this.errors = errors;
	}

	/**
	 * <p>
	 * Getter for the field <code>allergens</code>.
	 * </p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Map<NodeRef, Double> getAllergens() {
		return allergens;
	}

	/**
	 * <p>addAllergens.</p>
	 *
	 * @param toAdd a {@link java.util.List} object
	 */
	public void addAllergens(List<NodeRef> toAdd) {
		if (toAdd != null) {
			toAdd.forEach(a -> allergens.putIfAbsent(a, null));
		}
	}

	/**
	 * <p>
	 * Getter for the field <code>inVolAllergens</code>.
	 * </p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Map<NodeRef, Double> getInVolAllergens() {
		return inVolAllergens;
	}

	/**
	 * <p>addInVolAllergens.</p>
	 *
	 * @param toAdd a {@link java.util.List} object
	 */
	public void addInVolAllergens(List<NodeRef> toAdd) {
		if (toAdd != null) {
			toAdd.forEach(a -> inVolAllergens.putIfAbsent(a, null));
		}
	}

	/**
	 * <p>
	 * Getter for the field <code>inVolAllergensProcess</code>.
	 * </p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Map<NodeRef, Double> getInVolAllergensProcess() {
		return inVolAllergensProcess;
	}

	/**
	 * <p>addInVolAllergensProcess.</p>
	 *
	 * @param toAdd a {@link java.util.List} object
	 */
	public void addInVolAllergensProcess(List<NodeRef> toAdd) {
		if (toAdd != null) {
			toAdd.forEach(a -> inVolAllergensProcess.putIfAbsent(a, null));
		}
	}

	/**
	 * <p>
	 * Getter for the field <code>inVolAllergensRawMaterial</code>.
	 * </p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Map<NodeRef, Double> getInVolAllergensRawMaterial() {
		return inVolAllergensRawMaterial;
	}

	/**
	 * <p>addInVolAllergensRawMaterial.</p>
	 *
	 * @param toAdd a {@link java.util.List} object
	 */
	public void addInVolAllergensRawMaterial(List<NodeRef> toAdd) {
		if (toAdd != null) {
			toAdd.forEach(a -> inVolAllergensRawMaterial.putIfAbsent(a, null));
		}
	}

	/**
	 * <p>
	 * getCompositeLabeling.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ing.CompositeLabeling}
	 *         object.
	 */
	public CompositeLabeling getCompositeLabeling() {
		return lblCompositeContext;
	}

	/**
	 * <p>Getter for the field <code>footNotes</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<FootNoteRule> getFootNotes() {
		return footNotes;
	}

	/**
	 * <p>
	 * Getter for the field <code>toApplyThresholdItems</code>.
	 * </p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<NodeRef> getToApplyThresholdItems() {
		return toApplyThresholdItems;
	}

	/**
	 * <p>
	 * setCompositeLabeling.
	 * </p>
	 *
	 * @param compositeLabeling
	 *            a {@link fr.becpg.repo.product.data.ing.CompositeLabeling}
	 *            object.
	 */
	public void setCompositeLabeling(CompositeLabeling compositeLabeling) {
		this.lblCompositeContext = compositeLabeling;
	}

	/**
	 * <p>
	 * Getter for the field <code>mergedLblCompositeContext</code>.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ing.CompositeLabeling}
	 *         object.
	 */
	public CompositeLabeling getMergedLblCompositeContext() {
		return mergedLblCompositeContext;
	}

	/**
	 * <p>
	 * Setter for the field <code>mergedLblCompositeContext</code>.
	 * </p>
	 *
	 * @param mergedLblCompositeContext
	 *            a {@link fr.becpg.repo.product.data.ing.CompositeLabeling}
	 *            object.
	 */
	public void setMergedLblCompositeContext(CompositeLabeling mergedLblCompositeContext) {
		this.mergedLblCompositeContext = mergedLblCompositeContext;
	}

	/**
	 * <p>
	 * Constructor for LabelingFormulaContext.
	 * </p>
	 *
	 * @param mlNodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 * @param associationService
	 *            a {@link fr.becpg.repo.helper.AssociationService} object.
	 * @param alfrescoRepository
	 *            a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object
	 */
	public LabelingFormulaContext(NodeService mlNodeService, AssociationService associationService,
			AlfrescoRepository<RepositoryEntity> alfrescoRepository, SpelFormulaService formulaService) {
		super(mlNodeService);
		this.alfrescoRepository = alfrescoRepository;
		this.associationService = associationService;
		this.formulaService = formulaService;
	}

	/**
	 *
	 * Supported tag <b>bold</b> <u>underline</u> <i>italic</i>
	 * <up>uppercase</up> <lo>lowercase</lo> <ca>capitalize</ca>
	 *
	 * i,u, b are html tag and will be print as it into label up, lo, ca will
	 * replace containing text with transformed text value and remove tag ca
	 * will be the first transformation appended then lo then up.
	 *
	 * For exemple if you define detailsDefaultFormat = "<ca>{0}</ca>
	 * (<lo>{2}</lo>) and allergenReplacementPattern = "<up>$1</up>" for
	 * following labelling <ca>sugar</ca>,<ca>garniture</ca> (
	 * <lo><up>Milk</up>, <ca>Lactose</ca> (<up>Milk</up>), <ca>Sugar</ca></lo>)
	 * you will get Sugar, Garniture ( MILK, lactose (MILK), sugar)
	 *
	 *
	 */
	private static final String HTML_BREAK_LINE = "<br/>";

	private String ingDefaultFormat = "{0} [{3}]";
	private String groupDefaultFormat = "<b>{0}:</b> {2}";
	private String groupListDefaultFormat = "<b>{0}</b>";
	private String detailsDefaultFormat = "{0} ({2}) [{3}]";
	private String ingTypeDefaultFormat = "{0}: {2} [{3}]";
	private String ingTypeSingleValueFormat = null;
	private String ingTypeDecThresholdFormat = "{0} [{3}]";
	private String subIngsDefaultFormat = "{0} ({2}) [{3}]";
	private String geoPlaceOfActivityFormat = "{0}: {1}";

	private String allergenDetailsFormat = "{0} ({2})";
	private String allergenReplacementPattern = "<b>$1</b>";
	private String htmlTableRowFormat = "<tr><td style=\"border: solid 1px; padding: 5px;\" >{0}</td>"
			+ "<td style=\"border: solid 1px;padding: 5px;\" >{2}</td>" + "<td style=\"border: solid 1px;padding: 5px;\" >{3}</td>"
			+ "<td style=\"border: solid 1px;padding: 5px;text-align:center;\">{1,number,0.#%}</td>"
			+ "<td style=\"border: solid 1px;padding: 5px;text-align:center;\">{4,number,0.#%}</td></tr>";
	private String htmlTableHeaderFormat = "<thead><tr><th style=\"border: solid 1px; padding: 5px;\" >{0}</th>"
			+ "<th style=\"border: solid 1px;padding: 5px;\" >{2}</th>" + "<th style=\"border: solid 1px;padding: 5px;\" >{3}</th>"
			+ "<th style=\"border: solid 1px;padding: 5px;text-align:center;\">{1}</th>"
			+ "<th style=\"border: solid 1px;padding: 5px;text-align:center;\">{4}</th></tr></thead>";
	private String htmlTableFooterFormat = "<tfoot><tr><th style=\"border: solid 1px; padding: 5px;\" ><b>{0}</b></th>"
			+ "<td style=\"border: solid 1px;padding: 5px;\"></td>" + "<td style=\"border: solid 1px; padding: 5px;\"></td>"
			+ "<td style=\"border: solid 1px;padding: 5px;text-align:center;\"><b>{1,number,0.#%}</b></td>"
			+ "<td style=\"border: solid 1px;padding: 5px;\"></td></tr></tfoot>";

	private String defaultSeparator = RepoConsts.LABEL_SEPARATOR;
	private String atEndSeparator = RepoConsts.LABEL_SEPARATOR;
	private String groupDefaultSeparator = RepoConsts.LABEL_SEPARATOR;
	private String ingTypeDefaultSeparator = RepoConsts.LABEL_SEPARATOR;
	private String allergensSeparator = RepoConsts.LABEL_SEPARATOR;
	private String geoOriginsSeparator = RepoConsts.LABEL_SEPARATOR;
	private String geoPlaceOfActiviySeparator = RepoConsts.LABEL_SEPARATOR;
	private String bioOriginsSeparator = RepoConsts.LABEL_SEPARATOR;
	private String subIngsSeparator = RepoConsts.LABEL_SEPARATOR;
	private String footNotesLabelSeparator = HTML_BREAK_LINE;
	private String sortWithSpecificLocale = null;

	private boolean showIngCEECode = false;
	private boolean useVolume = false;
	private boolean ingsLabelingWithYield = false;
	private boolean doNotPropagateYield = false;
	
	private boolean uncapitalizeLegalName = false;
	private boolean shouldBreakIngType = false;
	private boolean labelingByLanguage = false;
	private boolean force100Perc = false;
	private boolean useSecondaryYield = false;
	private boolean forceKeepOrder = false;

	private Double yield = null;

	/**
	 * Use to disable allergen detection in legalName - Comma separated list of
	 * locale codes - Empty for all allergens - Wildcard (*) can be used for
	 * disable all Language
	 */
	private String disableAllergensForLocales = "";

	private Integer maxPrecision = 4;

	private Double qtyPrecisionThreshold = 1d / Math.pow(10, (double) maxPrecision + (double) 2);

	/**
	 * <p>Getter for the field <code>qtyPrecisionThreshold</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getQtyPrecisionThreshold() {
		return qtyPrecisionThreshold;
	}

	/**
	 * <p>
	 * isShouldBreakIngType.
	 * </p>
	 *
	 * @return a boolean.
	 */
	public boolean isShouldBreakIngType() {
		return shouldBreakIngType;
	}

	/**
	 * <p>
	 * Setter for the field <code>shouldBreakIngType</code>.
	 * </p>
	 *
	 * @param shouldBreakIngType
	 *            a boolean.
	 */
	public void setShouldBreakIngType(boolean shouldBreakIngType) {
		this.shouldBreakIngType = shouldBreakIngType;
	}

	/**
	 * <p>
	 * Setter for the field <code>disableAllergensForLocales</code>.
	 * </p>
	 *
	 * @param disableAllergensForLocales
	 *            a {@link java.lang.String} object.
	 */
	public void setDisableAllergensForLocales(String disableAllergensForLocales) {
		this.disableAllergensForLocales = disableAllergensForLocales;
	}

	/**
	 * <p>
	 * Setter for the field <code>useVolume</code>.
	 * </p>
	 *
	 * @param useVolume
	 *            a boolean.
	 */
	public void setUseVolume(boolean useVolume) {
		this.useVolume = useVolume;
	}

	/**
	 * <p>
	 * Setter for the field <code>showIngCEECode</code>.
	 * </p>
	 *
	 * @param showIngCEECode
	 *            a boolean.
	 */
	public void setShowIngCEECode(boolean showIngCEECode) {
		this.showIngCEECode = showIngCEECode;
	}

	/**
	 * <p>
	 * isIngsLabelingWithYield.
	 * </p>
	 *
	 * @return a boolean.
	 */
	public boolean isIngsLabelingWithYield() {
		return ingsLabelingWithYield;
	}

	/**
	 * <p>
	 * Setter for the field <code>ingsLabelingWithYield</code>.
	 * </p>
	 *
	 * @param ingsLabelingWithYield
	 *            a boolean.
	 */
	public void setIngsLabelingWithYield(boolean ingsLabelingWithYield) {
		this.ingsLabelingWithYield = ingsLabelingWithYield;
	}
	
	

	/**
	 * <p>isDoNotPropagateYield.</p>
	 *
	 * @return a boolean
	 */
	public boolean isDoNotPropagateYield() {
		return doNotPropagateYield;
	}

	/**
	 * <p>Setter for the field <code>doNotPropagateYield</code>.</p>
	 *
	 * @param doNotPropagateYield a boolean
	 */
	public void setDoNotPropagateYield(boolean doNotPropagateYield) {
		this.doNotPropagateYield = doNotPropagateYield;
	}

	/**
	 * <p>
	 * Getter for the field <code>yield</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getYield() {
		return yield;
	}

	/**
	 * <p>
	 * Setter for the field <code>yield</code>.
	 * </p>
	 *
	 * @param yield
	 *            a {@link java.lang.Double} object.
	 */
	public void setYield(Double yield) {
		this.yield = yield;
		this.ingsLabelingWithYield = true;
	}

	/**
	 * <p>isUseSecondaryYield.</p>
	 *
	 * @return a boolean
	 */
	public boolean isUseSecondaryYield() {
		return useSecondaryYield;
	}

	/**
	 * <p>Setter for the field <code>useSecondaryYield</code>.</p>
	 *
	 * @param useSecondaryYield a boolean
	 */
	public void setUseSecondaryYield(boolean useSecondaryYield) {
		this.useSecondaryYield = useSecondaryYield;
	}

	/**
	 * <p>Setter for the field <code>geoPlaceOfActivityFormat</code>.</p>
	 *
	 * @param geoPlaceOfActivityFormat a {@link java.lang.String} object
	 */
	public void setGeoPlaceOfActivityFormat(String geoPlaceOfActivityFormat) {
		this.geoPlaceOfActivityFormat = geoPlaceOfActivityFormat;
	}

	/**
	 * <p>Setter for the field <code>geoPlaceOfActiviySeparator</code>.</p>
	 *
	 * @param geoPlaceOfActiviySeparator a {@link java.lang.String} object
	 */
	public void setGeoPlaceOfActiviySeparator(String geoPlaceOfActiviySeparator) {
		this.geoPlaceOfActiviySeparator = geoPlaceOfActiviySeparator;
	}

	/**
	 * <p>
	 * Setter for the field <code>ingDefaultFormat</code>.
	 * </p>
	 *
	 * @param ingDefaultFormat
	 *            a {@link java.lang.String} object.
	 */
	public void setIngDefaultFormat(String ingDefaultFormat) {
		this.ingDefaultFormat = ingDefaultFormat;
	}

	/**
	 * <p>
	 * Setter for the field <code>groupDefaultFormat</code>.
	 * </p>
	 *
	 * @param groupDefaultFormat
	 *            a {@link java.lang.String} object.
	 */
	public void setGroupDefaultFormat(String groupDefaultFormat) {
		this.groupDefaultFormat = groupDefaultFormat;
	}

	/**
	 * <p>
	 * Setter for the field <code>allergenDetailsFormat</code>.
	 * </p>
	 *
	 * @param allergenDetailsFormat
	 *            a {@link java.lang.String} object.
	 */
	public void setAllergenDetailsFormat(String allergenDetailsFormat) {
		this.allergenDetailsFormat = allergenDetailsFormat;
	}

	/**
	 * <p>
	 * Setter for the field <code>groupListDefaultFormat</code>.
	 * </p>
	 *
	 * @param groupListDefaultFormat
	 *            a {@link java.lang.String} object.
	 */
	public void setGroupListDefaultFormat(String groupListDefaultFormat) {
		this.groupListDefaultFormat = groupListDefaultFormat;
	}

	/**
	 * <p>
	 * Setter for the field <code>uncapitalizeLegalName</code>.
	 * </p>
	 *
	 * @param uncapitalizeLegalName
	 *            a boolean.
	 */
	public void setUncapitalizeLegalName(boolean uncapitalizeLegalName) {
		this.uncapitalizeLegalName = uncapitalizeLegalName;
	}

	/**
	 * <p>
	 * Setter for the field <code>detailsDefaultFormat</code>.
	 * </p>
	 *
	 * @param detailsDefaultFormat
	 *            a {@link java.lang.String} object.
	 */
	public void setDetailsDefaultFormat(String detailsDefaultFormat) {
		this.detailsDefaultFormat = detailsDefaultFormat;
	}

	/**
	 * <p>
	 * Setter for the field <code>ingTypeDefaultFormat</code>.
	 * </p>
	 *
	 * @param ingTypeDefaultFormat
	 *            a {@link java.lang.String} object.
	 */
	public void setIngTypeDefaultFormat(String ingTypeDefaultFormat) {
		this.ingTypeDefaultFormat = ingTypeDefaultFormat;
	}

	/**
	 * <p>Setter for the field <code>ingTypeSingleValueFormat</code>.</p>
	 *
	 * @param ingTypeSingleValueFormat a {@link java.lang.String} object
	 */
	public void setIngTypeSingleValueFormat(String ingTypeSingleValueFormat) {
		this.ingTypeSingleValueFormat = ingTypeSingleValueFormat;
	}

	/**
	 * <p>
	 * Setter for the field <code>subIngsDefaultFormat</code>.
	 * </p>
	 *
	 * @param subIngsDefaultFormat
	 *            a {@link java.lang.String} object.
	 */
	public void setSubIngsDefaultFormat(String subIngsDefaultFormat) {
		this.subIngsDefaultFormat = subIngsDefaultFormat;
	}

	/**
	 * <p>
	 * Setter for the field <code>htmlTableRowFormat</code>.
	 * </p>
	 *
	 * @param htmlTableRowFormat
	 *            a {@link java.lang.String} object.
	 */
	public void setHtmlTableRowFormat(String htmlTableRowFormat) {
		this.htmlTableRowFormat = htmlTableRowFormat;
	}

	/**
	 * <p>Setter for the field <code>htmlTableHeaderFormat</code>.</p>
	 *
	 * @param htmlTableHeaderFormat a {@link java.lang.String} object
	 */
	public void setHtmlTableHeaderFormat(String htmlTableHeaderFormat) {
		this.htmlTableHeaderFormat = htmlTableHeaderFormat;
	}

	/**
	 * <p>Setter for the field <code>htmlTableFooterFormat</code>.</p>
	 *
	 * @param htmlTableFooterFormat a {@link java.lang.String} object
	 */
	public void setHtmlTableFooterFormat(String htmlTableFooterFormat) {
		this.htmlTableFooterFormat = htmlTableFooterFormat;
	}

	/**
	 * <p>
	 * Setter for the field <code>ingTypeDecThresholdFormat</code>.
	 * </p>
	 *
	 * @param ingTypeDecThresholdFormat
	 *            a {@link java.lang.String} object.
	 */
	public void setIngTypeDecThresholdFormat(String ingTypeDecThresholdFormat) {
		this.ingTypeDecThresholdFormat = ingTypeDecThresholdFormat;
	}

	/**
	 * <p>
	 * Setter for the field <code>defaultSeparator</code>.
	 * </p>
	 *
	 * @param defaultSeparator
	 *            a {@link java.lang.String} object.
	 */
	public void setDefaultSeparator(String defaultSeparator) {
		this.defaultSeparator = defaultSeparator;
	}

	/**
	 * <p>
	 * Setter for the field <code>atEndSeparator</code>.
	 * </p>
	 *
	 * @param atEndSeparator
	 *            a {@link java.lang.String} object.
	 */
	public void setAtEndSeparator(String atEndSeparator) {
		this.atEndSeparator = atEndSeparator;
	}

	/**
	 * <p>
	 * Setter for the field <code>groupDefaultSeparator</code>.
	 * </p>
	 *
	 * @param groupDefaultSeparator
	 *            a {@link java.lang.String} object.
	 */
	public void setGroupDefaultSeparator(String groupDefaultSeparator) {
		this.groupDefaultSeparator = groupDefaultSeparator;
	}

	/**
	 * <p>
	 * Setter for the field <code>ingTypeDefaultSeparator</code>.
	 * </p>
	 *
	 * @param ingTypeDefaultSeparator
	 *            a {@link java.lang.String} object.
	 */
	public void setIngTypeDefaultSeparator(String ingTypeDefaultSeparator) {
		this.ingTypeDefaultSeparator = ingTypeDefaultSeparator;
	}

	/**
	 * <p>
	 * Setter for the field <code>allergensSeparator</code>.
	 * </p>
	 *
	 * @param allergensSeparator
	 *            a {@link java.lang.String} object.
	 */
	public void setAllergensSeparator(String allergensSeparator) {
		this.allergensSeparator = allergensSeparator;
	}

	/**
	 * <p>
	 * Setter for the field <code>subIngsSeparator</code>.
	 * </p>
	 *
	 * @param subIngsSeparator
	 *            a {@link java.lang.String} object.
	 */
	public void setSubIngsSeparator(String subIngsSeparator) {
		this.subIngsSeparator = subIngsSeparator;
	}

	/**
	 * <p>
	 * Setter for the field <code>geoOriginsSeparator</code>.
	 * </p>
	 *
	 * @param geoOriginsSeparator
	 *            a {@link java.lang.String} object.
	 */
	public void setGeoOriginsSeparator(String geoOriginsSeparator) {
		this.geoOriginsSeparator = geoOriginsSeparator;
	}

	/**
	 * <p>
	 * Setter for the field <code>geoOriginsSeparator</code>.
	 * </p>
	 *
	 * @param bioOriginsSeparator
	 *            a {@link java.lang.String} object.
	 */
	public void setBioOriginsSeparator(String bioOriginsSeparator) {
		this.bioOriginsSeparator = bioOriginsSeparator;
	}

	/**
	 * <p>Setter for the field <code>footNotesLabelSeparator</code>.</p>
	 *
	 * @param footNotesLabelSeparator a {@link java.lang.String} object
	 */
	public void setFootNotesLabelSeparator(String footNotesLabelSeparator) {
		this.footNotesLabelSeparator = footNotesLabelSeparator;
	}

	/**
	 * <p>
	 * Setter for the field <code>allergenReplacementPattern</code>.
	 * </p>
	 *
	 * @param allergenReplacementPattern
	 *            a {@link java.lang.String} object.
	 */
	public void setAllergenReplacementPattern(String allergenReplacementPattern) {
		this.allergenReplacementPattern = allergenReplacementPattern;
	}

	/**
	 * <p>
	 * Setter for the field <code>qtyPrecisionThreshold</code>.
	 * </p>
	 *
	 * @param qtyPrecisionThreshold
	 *            a {@link java.lang.Double} object.
	 */
	public void setQtyPrecisionThreshold(Double qtyPrecisionThreshold) {
		this.qtyPrecisionThreshold = qtyPrecisionThreshold;
	}

	/**
	 * <p>
	 * Setter for the field <code>maxPrecision</code>.
	 * </p>
	 *
	 * @param maxPrecision
	 *            a {@link java.lang.Integer} object.
	 */
	public void setMaxPrecision(Integer maxPrecision) {
		this.maxPrecision = maxPrecision;
	}

	/**
	 * <p>
	 * isLabelingByLanguage.
	 * </p>
	 *
	 * @return a boolean.
	 */
	public boolean isLabelingByLanguage() {
		return labelingByLanguage;
	}

	/**
	 * <p>
	 * Setter for the field <code>labelingByLanguage</code>.
	 * </p>
	 *
	 * @param labelingByLanguage
	 *            a boolean.
	 */
	public void setLabelingByLanguage(boolean labelingByLanguage) {
		this.labelingByLanguage = labelingByLanguage;
	}

	/**
	 * <p>
	 * Setter for the field <code>force100Perc</code>.
	 * </p>
	 *
	 * @param force100Perc
	 *            a boolean.
	 */
	public void setForce100Perc(boolean force100Perc) {
		this.force100Perc = force100Perc;
	}

	/**
	 * <p>Setter for the field <code>forceKeepOrder</code>.</p>
	 *
	 * @param forceKeepOrder a boolean
	 */
	public void setForceKeepOrder(boolean forceKeepOrder) {
		this.forceKeepOrder = forceKeepOrder;
	}

	@Override
	void updateDefaultFormat(TextFormatRule textFormatRule) {

		formatsByName.put("ingDefaultFormat", textFormatRule);
		formatsByName.put("detailsDefaultFormat", textFormatRule);

	}

	/**
	 * <p>Getter for the field <code>sortWithSpecificLocale</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getSortWithSpecificLocale() {
		return sortWithSpecificLocale;
	}

	/**
	 * <p>Setter for the field <code>sortWithSpecificLocale</code>.</p>
	 *
	 * @param sortWithSpecificLocale a {@link java.lang.String} object
	 */
	public void setSortWithSpecificLocale(String sortWithSpecificLocale) {
		this.sortWithSpecificLocale = sortWithSpecificLocale;
	}

	/* formaters */
	private MessageFormat getIngTextFormat(LabelingComponent lblComponent, Double qty, boolean multiple) {

		if (textFormaters.containsKey(lblComponent.getNodeRef())) {
			TextFormatRule textFormatRule = textFormaters.get(lblComponent.getNodeRef());
			if (textFormatRule.matchLocale(I18NUtil.getLocale())) {
				return applyRoundingMode(new MessageFormat(textFormatRule.getTextFormat(), getContentLocale()), qty);
			}
		}

		if (lblComponent instanceof CompositeLabeling) {
			if (((CompositeLabeling) lblComponent).isGroup()) {
				return applyRoundingMode(new MessageFormat(getTextFormatByName("groupDefaultFormat", groupDefaultFormat), getContentLocale()), qty);
			}
			if (DeclarationType.Detail.equals(((CompositeLabeling) lblComponent).getDeclarationType())) {
				if ((lblComponent instanceof IngItem) && !((CompositeLabeling) lblComponent).getIngList().isEmpty()) {
					return applyRoundingMode(new MessageFormat(getTextFormatByName("subIngsDefaultFormat", subIngsDefaultFormat), getContentLocale()),
							qty);
				}
				return applyRoundingMode(new MessageFormat(getTextFormatByName("detailsDefaultFormat", detailsDefaultFormat), getContentLocale()),
						qty);
			}

			return applyRoundingMode(new MessageFormat(getTextFormatByName("ingDefaultFormat", ingDefaultFormat), getContentLocale()), qty);
		} else if (lblComponent instanceof IngTypeItem) {
			if (isDoNotDetails((IngTypeItem) lblComponent)) {
				return applyRoundingMode(
						new MessageFormat(getTextFormatByName("ingTypeDecThresholdFormat", ingTypeDecThresholdFormat), getContentLocale()), qty);
			} else if (ingTypeSingleValueFormat != null && !multiple) {
				return applyRoundingMode(
						new MessageFormat(getTextFormatByName("ingTypeSingleValueFormat", ingTypeSingleValueFormat), getContentLocale()), qty);
			}
			return applyRoundingMode(new MessageFormat(getTextFormatByName("ingTypeDefaultFormat", ingTypeDefaultFormat), getContentLocale()), qty);
		}

		return applyRoundingMode(new MessageFormat(getTextFormatByName("ingDefaultFormat", ingDefaultFormat), getContentLocale()), qty);
	}

	private String getTextFormatByName(String formatName, String defaultFormat) {
		if (formatsByName.containsKey(formatName)) {
			TextFormatRule textFormatRule = formatsByName.get(formatName);
			if (textFormatRule.matchLocale(I18NUtil.getLocale())) {
				return textFormatRule.getTextFormat();
			}
		}
		return defaultFormat;
	}

	private MessageFormat applyRoundingMode(MessageFormat messageFormat, Double qty) {
		return applyRoundingMode(messageFormat, qty, false);
	}

	private MessageFormat applyTotalRoundingMode(MessageFormat messageFormat) {
		return applyRoundingMode(messageFormat, qtyPrecisionThreshold, true);
	}

	private MessageFormat applyRoundingMode(MessageFormat messageFormat, Double qty, boolean useTotalPrecision) {
		if (messageFormat.getFormats() != null) {
			for (Format format : messageFormat.getFormats()) {
				if (format instanceof DecimalFormat decimalFormat) {
					applyAutomaticPrecicion(decimalFormat, qty, defaultRoundingMode, useTotalPrecision);
					break;
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
			while (((Math.pow(10, maxNum + 2d) * qty) < 1)) {
				if (maxNum >= maxPrecision) {
					decimalFormat.setRoundingMode(maxRoundingMode);
					decimalFormat.setMaximumFractionDigits(maxNum);
					decimalFormat.setMinimumFractionDigits(maxNum);
					break;
				}
				maxNum++;
			}
			decimalFormat.setMaximumFractionDigits(maxNum);
			decimalFormat.setMinimumIntegerDigits(1);
		}

	}

	private String getName(LabelingComponent lblComponent) {
		if (lblComponent instanceof IngItem ingItem) {
			return ingItem.getCharactName();
		}
		return lblComponent.getName();
	}

	/**
	 * <p>
	 * getLegalIngName.
	 * </p>
	 *
	 * @param lblComponent
	 *            a {@link fr.becpg.repo.product.data.ing.LabelingComponent}
	 *            object.
	 * @return a {@link java.lang.String} object.
	 */
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

		if ((lblComponent instanceof IngTypeItem ingItem) && ingItem.doNotDeclare()) {
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

			if (showIngCEECode && (lblComponent instanceof IngItem ingItem)
					&& ((ingItem.getIngCEECode() != null) && !ingItem.getIngCEECode().isEmpty())) {
				ingLegalName = ingItem.getIngCEECode();

			}
		}

		if (uncapitalizeLegalName) {
			ingLegalName = uncapitalize(ingLegalName);
		}

		if (!lblComponent.getAllergens().isEmpty()) {
			ingLegalName = createAllergenAwareLabel(ingLegalName, lblComponent.getAllergens(),
					!((lblComponent instanceof CompositeLabeling) && ((CompositeLabeling) lblComponent).getIngList().isEmpty()));
		}

		if (!lblComponent.getFootNotes().isEmpty()) {
			ingLegalName = createFootNoteMarkersLabel(ingLegalName, lblComponent.getFootNotes());
		}

		if (qty != null) {
			ingLegalName = createPercAwareLabel(lblComponent, ingLegalName, qty, useTotalPrecision);
		}

		return ingLegalName;
	}

	private String createFootNoteMarkersLabel(String ingLegalName, Set<FootNoteRule> footNotes) {

		String footNoteLabel = footNotes.stream().filter(f -> f.matchLocale(I18NUtil.getLocale())).sorted().map(FootNoteRule::getFootNoteMarker)
				.collect(Collectors.joining(" "));
		if ((footNoteLabel != null) && !footNoteLabel.isBlank()) {
			return ingLegalName + footNoteLabel;
		}

		return ingLegalName;
	}

	private String createPercAwareLabel(LabelingComponent lblComponent, String ingLegalName, Double qty, boolean useTotalPrecision) {
		if (qty != null) {
			QtyFormater qtyFormater = getQtyFormater(lblComponent, qty, useTotalPrecision);
			if (qtyFormater != null) {
				ingLegalName = ingLegalName + " " + qtyFormater.format(qty);
			}
		}

		return ingLegalName;
	}

	private boolean showPerc(LabelingComponent lblComponent) {
		return showPercRules.isEmpty() || (getSelectedRule(lblComponent, null) != null);
	}

	private QtyFormater getDefaultQtyFormater(DecimalFormat decimalFormat, Double qty) {
		if (decimalFormat == null) {
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(getContentLocale());
			decimalFormat = new DecimalFormat(defaultPercFormat, symbols);
		}
		applyAutomaticPrecicion(decimalFormat, qty, defaultRoundingMode, false);

		return new QtyFormater(decimalFormat, defaultRoundingMode, null);
	}

	private QtyFormater getQtyFormater(LabelingComponent lblComponent, Double qty, boolean useTotalPrecision) {
		DecimalFormat decimalFormat = null;
		RoundingMode roundingMode = defaultRoundingMode;
		String qtyFormula = null;

		DecimalFormatSymbols symbols = new DecimalFormatSymbols(getContentLocale());
		if ((lblComponent != null)) {

			boolean applyAllPerc = true;

			if (lblComponent instanceof IngTypeItem ingItem && !isDoNotDetails(ingItem)) {
				applyAllPerc = false;
			}

			ShowRule selectedRule = getSelectedRule(lblComponent, qty);

			if (selectedRule == null && applyAllPerc) {
				for (ShowRule showRule : showAllPerc) {
					if (isShowRuleMatch(selectedRule, showRule, qty)) {
						selectedRule = showRule;
					}
				}
			}

			if (selectedRule != null) {
				decimalFormat = new DecimalFormat(
						(selectedRule.format != null) && !selectedRule.format.isEmpty() ? selectedRule.format : defaultPercFormat, symbols);
				if (selectedRule.roundingMode != null) {
					roundingMode = selectedRule.roundingMode;
				}

				qtyFormula = selectedRule.qtyFormula;

			}

			if (decimalFormat != null) {

				applyAutomaticPrecicion(decimalFormat, useTotalPrecision ? qtyPrecisionThreshold : qty, roundingMode, useTotalPrecision);

				return new QtyFormater(decimalFormat, roundingMode, qtyFormula);
			}

		}
		return null;
	}

	private ShowRule getSelectedRule(LabelingComponent lblComponent, Double qty) {
		NodeRef nodeRef = lblComponent.getNodeRef();

		ShowRule selectedRule = null;

		if (showPercRules.containsKey(nodeRef)) {
			for (ShowRule showRule : showPercRules.get(nodeRef)) {
				if (isShowRuleMatch(selectedRule, showRule, qty)) {
					selectedRule = showRule;
				}
			}
		} else {

			if (renameRules.containsKey(nodeRef)) {
				RenameRule renameRule = renameRules.get(nodeRef);
				if (renameRule.matchLocale(I18NUtil.getLocale()) && (renameRule.getReplacement() != null)
						&& showPercRules.containsKey(renameRule.getReplacement())) {
					for (ShowRule showRule : showPercRules.get(renameRule.getReplacement())) {
						if (isShowRuleMatch(selectedRule, showRule, qty)) {
							selectedRule = showRule;
						}
					}
				}
			}

			for (Map.Entry<NodeRef, RenameRule> entry : renameRules.entrySet()) {
				NodeRef entryNodeRef = entry.getKey();
				RenameRule renameRule = entry.getValue();

				if (Objects.equals(renameRule.getReplacement(), nodeRef) && renameRule.matchLocale(I18NUtil.getLocale())
						&& showPercRules.get(entryNodeRef) != null) {

					for (ShowRule showRule : showPercRules.get(entryNodeRef)) {
						if (isShowRuleMatch(selectedRule, showRule, qty)) {
							selectedRule = showRule;
						}
					}
				}
			}
		}

		return selectedRule;
	}

	private boolean isShowRuleMatch(ShowRule selectedRule, ShowRule showRule, Double qty) {
		if (showRule.matchLocale(I18NUtil.getLocale()) && showRule.matchQty(qty)) {
			if ((selectedRule == null) || ((selectedRule.getThreshold() == null) && (showRule.getThreshold() != null))
					|| ((selectedRule.getThreshold() != null) && (showRule.getThreshold() != null)
							&& (selectedRule.getThreshold() > showRule.getThreshold()))) {
				return true;
			}

		}
		return false;
	}

	private String uncapitalize(String legalName) {
		if ((legalName == null) || legalName.isEmpty() || Pattern.compile("^([A-Z]{2}|[A-Z][1-9]|[A-Z]\\-|[A-Z]_).*$").matcher(legalName).find()) {
			return legalName;
		}
		return StringUtils.uncapitalize(legalName);
	}

	private static final String[] ESCAPED_ALLERGEN_TAGS = new String[] { "<b>", "</b>", "<u>", "</u>", "<i>", "</i>", "<up>", "</up>" };

	private static final String ING_TYPE_PREFIX = "ingType-";

	private String createAllergenAwareLabel(String ingLegalName, Set<NodeRef> allergens, boolean boldOnly) {
		if (isAllergensDisableForLocale()) {
			return ingLegalName;
		}

		Set<String> detectedAllergens = getDetectedAllergens();

		Matcher ma = ALLERGEN_DETECTION_PATTERN.matcher(ingLegalName);
		if (ma.find() && (ma.group(1) != null)) {
			String allergenName = ma.group(1);
			for (String toEscape : ESCAPED_ALLERGEN_TAGS) {
				allergenName = allergenName.replace(toEscape, "");
			}

			detectedAllergens.add(allergenName);
			return ma.replaceFirst(allergenReplacementPattern.replace("$1", allergenName));
		}

		StringBuilder ret = new StringBuilder();
		for (NodeRef allergen : allergens) {
			if (!isAllergenDisableForLocale(allergen)) {
				boolean shouldAppend = !boldOnly;
				if (getAllergens().keySet().contains(allergen)) {
					String allergenName = getCharactName(allergen);
					if ((allergenName != null) && !allergenName.isEmpty()) {
						if (ret.length() > 0) {
							ret.append(getLocaleSeparator(allergensSeparator));
						} else {
							ma = Pattern.compile("\\b(" + Pattern.quote(allergenName) + "(s?))\\b", Pattern.CASE_INSENSITIVE).matcher(ingLegalName);
							if (ma.find() && (ma.group(1) != null)) {

								detectedAllergens.add(ma.group(1));
								ingLegalName = ma.replaceAll(allergenReplacementPattern);
								shouldAppend = false;
							} else {
								for (NodeRef subAllergen : associationService.getTargetAssocs(allergen, PLMModel.ASSOC_ALLERGENSUBSETS)) {
									String subAllergenName = uncapitalize(getCharactName(subAllergen));
									if ((subAllergenName != null) && !subAllergenName.isEmpty()) {
										ma = Pattern.compile("\\b(" + Pattern.quote(subAllergenName) + "(s?))\\b", Pattern.CASE_INSENSITIVE)
												.matcher(ingLegalName);
										if (ma.find() && (ma.group(1) != null)) {
											detectedAllergens.add(ma.group(1));
											ingLegalName = ma.replaceAll(allergenReplacementPattern);
											shouldAppend = false;
										}
									}
								}
							}
						}
						if (shouldAppend) {
							detectedAllergens.add(allergenName);
							ret.append(allergenName.replaceFirst("(.*)", allergenReplacementPattern));
						}
					}
				}
			}
		}
		return applyRoundingMode(new MessageFormat(allergenDetailsFormat), null).format(new Object[] { ingLegalName, null, ret.toString(), null });
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
		Map<NodeRef, Double> tmp = new HashMap<>();
		for (LabelingComponent ing : ingList) {
			for (NodeRef allergen : ing.getAllergens()) {
				Double qty = ing.getQty(ingsLabelingWithYield);

				if (tmp.containsKey(allergen) && (qty != null) && (tmp.get(allergen) != null)) {
					qty += tmp.get(allergen);
				}

				tmp.put(allergen, qty);
			}
		}

		return createAllergenAwareLabel(ingLegalName, sorted(tmp), false);
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

	/**
	 * <p>
	 * render.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String render() {
		return render(true);
	}

	/**
	 * <p>
	 * render.
	 * </p>
	 *
	 * @param showGroup
	 *            a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public String render(boolean showGroup) {

		if (logger.isTraceEnabled()) {
			logger.trace(" Render label (showGroup:" + showGroup + "): ");
		}

		BigDecimal total = null;
		if (showGroup) {
			if (force100Perc) {
				total = getTotal(lblCompositeContext, ingsLabelingWithYield);
			}

			return decorate(renderCompositeIng(lblCompositeContext, DEFAULT_RATIO, total, false, false));
		} else {
			if (force100Perc) {
				total = getTotal(mergedLblCompositeContext, ingsLabelingWithYield);
			}

			return decorate(renderCompositeIng(mergedLblCompositeContext, DEFAULT_RATIO, total, false, false));
		}

	}

	/**
	 * <p>
	 * renderGroupList.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String renderGroupList() {
		StringBuilder ret = new StringBuilder();

		if (logger.isTraceEnabled()) {
			logger.trace(" Render Group list ");
		}

		List<LabelingComponent> components = new LinkedList<>(lblCompositeContext.getIngList().values());

		sort(components);

		for (LabelingComponent component : components) {

			Double qtyPerc = roundedDouble(computeQtyPerc(lblCompositeContext, component, DEFAULT_RATIO));

			String ingName = getLegalIngName(component, qtyPerc, false, false);

			if (isGroup(component)) {
				if (ret.length() > 0) {
					ret.append(getLocaleSeparator(groupDefaultSeparator));
				}
				ret.append(applyRoundingMode(new MessageFormat(groupListDefaultFormat), qtyPerc).format(new Object[] { ingName, qtyPerc }));
			}
		}

		return decorate(ret.toString());
	}

	/**
	 * <p>roundedDouble.</p>
	 *
	 * @param qtyPerc a {@link java.math.BigDecimal} object
	 * @return a {@link java.lang.Double} object
	 */
	public static Double roundedDouble(BigDecimal qtyPerc) {
		if (qtyPerc != null) {
			return qtyPerc.round(new MathContext(PRECISION_RATIO - 6, RoundingMode.HALF_UP)).stripTrailingZeros().doubleValue();
		}
		return null;
	}

	/**
	 * <p>
	 * renderAllergens.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String renderAllergens() {
		return renderAllergens(sorted(this.allergens));
	}

	/**
	 * <p>
	 * renderDetectedAllergens.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String renderDetectedAllergens() {
		StringBuilder ret = new StringBuilder();

		if (logger.isTraceEnabled()) {
			logger.trace(" Render Allergens list ");
		}

		Set<String> detectedAllergens = getDetectedAllergens();

		for (String allergen : detectedAllergens) {
			if (ret.length() > 0) {
				ret.append(getLocaleSeparator(allergensSeparator));
			}
			ret.append(allergen);
		}

		return decorate(ret.toString());

	}

	private Set<String> getDetectedAllergens() {
		return detectedAllergensByLocale.computeIfAbsent(I18NUtil.getLocale(), r -> new LinkedHashSet<>());
	}

	private class CompositeParent {
		CompositeLabeling parent;
		LabelingComponent component;
		BigDecimal ratio;

		public CompositeParent(CompositeLabeling parent, LabelingComponent component, BigDecimal ratio) {
			super();
			this.parent = parent;
			this.component = component;
			this.ratio = ratio;
		}
	}

	/**
	 * <p>renderFootNotes.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String renderFootNotes() {

		return footNotes.stream().filter(f -> f.matchLocale(I18NUtil.getLocale())).sorted().map(f -> {
			Double qtyPerc = 0d;
			Queue<CompositeParent> queue = new LinkedList<>();

			// Add all LabelingComponents with a ratio of 1 to the queue
			for (LabelingComponent component : lblCompositeContext.getIngList().values()) {
				queue.add(new CompositeParent(lblCompositeContext, component, DEFAULT_RATIO));
			}

			while (!queue.isEmpty()) {
				CompositeParent compositeParent = queue.poll();

				BigDecimal subQty = computeQtyPerc(compositeParent.parent, compositeParent.component, compositeParent.ratio);

				if (subQty != null && compositeParent.component.getFootNotes() != null && compositeParent.component.getFootNotes().contains(f)) {
					qtyPerc += roundedDouble(subQty);

				}

				if (compositeParent.component instanceof CompositeLabeling) {
					CompositeLabeling composite = (CompositeLabeling) compositeParent.component;

					// If the CompositeLabeling component is a Kit, set subRatio to 1
					if (DeclarationType.Kit.equals(composite.getDeclarationType())) {
						subQty = DEFAULT_RATIO;
					}

					// Add all child LabelingComponents to the queue with the updated ratio
					for (LabelingComponent child : composite.getIngList().values()) {
						queue.add(new CompositeParent(composite, child, subQty));
					}
				}
			}
			MessageFormat messageFormat = new MessageFormat(f.getFootNoteLabel(I18NUtil.getLocale()), getContentLocale());

			return messageFormat.format(new Object[] { qtyPerc });

		}).collect(Collectors.joining(footNotesLabelSeparator));
	}

	/**
	 * <p>
	 * renderInvoluntaryAllergens.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String renderInvoluntaryAllergens() {
		return renderAllergens(sorted(this.inVolAllergens));

	}

	/**
	 * <p>
	 * renderInvoluntaryAllergenInProcess.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String renderInvoluntaryAllergenInProcess() {
		return renderAllergens(sorted(this.inVolAllergensProcess));

	}

	/**
	 * <p>
	 * renderInvoluntaryInRawMaterial.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String renderInvoluntaryInRawMaterial() {
		return renderAllergens(sorted(this.inVolAllergensRawMaterial));
	}

	private Set<NodeRef> sorted(Map<NodeRef, Double> toSortHashMap) {
		return toSortHashMap.entrySet().stream()
				.sorted(Comparator.comparing(Map.Entry<NodeRef, Double>::getValue, Comparator.nullsLast(Comparator.naturalOrder())))
				.map(Map.Entry<NodeRef, Double>::getKey).collect(Collectors.toSet());
	}

	private void sort(List<LabelingComponent> toSort) {
		Locale currentLocal = I18NUtil.getLocale();
		try {
			if (sortWithSpecificLocale != null) {
				I18NUtil.setLocale(MLTextHelper.parseLocale(sortWithSpecificLocale));
			}

			Collections.sort(toSort, (a, b) -> {
				int result = compareLabelingComponents(a, b);
				if (result == 0) {
					result = compareIngredientNames(a, b);
				}
				return result;
			});

		} finally {
			I18NUtil.setLocale(currentLocal);
		}
	}

	private int compareLabelingComponents(LabelingComponent a, LabelingComponent b) {

		if ((b instanceof CompositeLabeling) && ((CompositeLabeling) b).isGroup()
				&& !((a instanceof CompositeLabeling) && ((CompositeLabeling) a).isGroup())) {
			return 1;
		}

		if (!((b instanceof CompositeLabeling) && ((CompositeLabeling) b).isGroup())
				&& ((a instanceof CompositeLabeling) && ((CompositeLabeling) a).isGroup())) {
			return -1;
		}

		if (a instanceof IngTypeItem && IngTypeItem.DEFAULT_GROUP.equals(a) || b instanceof IngTypeItem && ((IngTypeItem) b).lastGroup()) {
			return -1;
		}

		if (b instanceof IngTypeItem && IngTypeItem.DEFAULT_GROUP.equals(b) || a instanceof IngTypeItem && ((IngTypeItem) a).lastGroup()) {
			return 1;
		}

		if (useVolume) {
			Double volumeA = a.getVolume(ingsLabelingWithYield);
			Double volumeB = b.getVolume(ingsLabelingWithYield);
			if (volumeA == null && volumeB == null) {
				return 0;
			} else if (volumeA == null) {
				return 1; // b is considered greater if a is null
			} else if (volumeB == null) {
				return -1; // a is considered greater if b is null
			}
			if (Math.abs(volumeB - volumeA) < qtyPrecisionThreshold) {
				return 0; // Consider them equal if within the threshold
			} else {
				return Double.compare(volumeB, volumeA);
			}

		}

		Double qtyA = a.getQty(ingsLabelingWithYield);
		Double qtyB = b.getQty(ingsLabelingWithYield);

		if (qtyA == null && qtyB == null) {
			return 0;
		} else if (qtyA == null) {
			return 1; // b is considered greater if a is null
		} else if (qtyB == null) {
			return -1; // a is considered greater if b is null
		}
		if (Math.abs(qtyB - qtyA) < qtyPrecisionThreshold) {
			return 0; // Consider them equal if within the threshold
		} else {
			return Double.compare(qtyB, qtyA);
		}

	}

	private int compareIngredientNames(LabelingComponent a, LabelingComponent b) {
		String nameA = getLegalIngName(a);
		String nameB = getLegalIngName(b);
		return (nameA == null ? "" : nameA).compareTo(nameB == null ? "" : nameB);
	}

	/**
	 * <p>
	 * renderAllergens.
	 * </p>
	 *
	 * @param allergensList
	 *            a {@link java.util.Set} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String renderAllergens(Set<NodeRef> allergensList) {
		StringBuilder ret = new StringBuilder();

		if (logger.isTraceEnabled()) {
			logger.trace(" Render Allergens list ");
		}

		for (NodeRef allergen : allergensList) {
			if (!isAllergenDisableForLocale(allergen)) {
				if (ret.length() > 0) {
					ret.append(getLocaleSeparator(allergensSeparator));
				}
				ret.append(getCharactName(allergen));
			}
		}

		return decorate(ret.toString());

	}

	/**
	 * <p>
	 * renderAsHtmlTable.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String renderAsHtmlTable() {
		return renderAsHtmlTable(null, false);
	}

	/**
	 * <p>
	 * renderAsHtmlTable.
	 * </p>
	 *
	 * @param styleCss
	 *            a {@link java.lang.String} object.
	 * @param showTotal
	 *            a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public String renderAsHtmlTable(String styleCss, boolean showTotal) {
		return renderAsHtmlTable(styleCss, showTotal, false);
	}

	/**
	 * <p>
	 * renderAsHtmlTable.
	 * </p>
	 *
	 * @param styleCss
	 *            a {@link java.lang.String} object.
	 * @param showTotal
	 *            a boolean.
	 * @return a {@link java.lang.String} object.
	 * @param force100PercForTable a boolean
	 */
	public String renderAsHtmlTable(String styleCss, boolean showTotal, boolean force100PercForTable) {
		StringBuilder ret = new StringBuilder();
		StringBuilder tableContent = new StringBuilder();

		BigDecimal total = getTotal(lblCompositeContext, false);
		BigDecimal totalWithYield = getTotal(lblCompositeContext, true);

		ret.append("<table class=\"labelingTable\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\""
				+ ((styleCss == null) || (styleCss).isBlank() ? "border: solid 1px; border-collapse:collapse" : styleCss) + "\" rules=\"none\">");

		if ((htmlTableHeaderFormat != null) && !htmlTableHeaderFormat.isBlank()) {
			ret.append(new MessageFormat(htmlTableHeaderFormat, getContentLocale())
					.format(new Object[] { I18NUtil.getMessage("bcpg_bcpgmodel.association.bcpg_ingListIng.title"),
							I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListQtyPerc.title"),
							I18NUtil.getMessage("bcpg_bcpgmodel.association.bcpg_ingListGeoOrigin.title"),
							I18NUtil.getMessage("bcpg_bcpgmodel.association.bcpg_ingListBioOrigin.title"),
							I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListQtyPercWithYield.title") }));
		}

		ret.append("<tbody>");

		boolean first = true;
		String firstLabel = "";
		Double firstQtyPerc = 0d;
		Double firstQtyPercWithYield = 0d;
		LabelingComponent firstLabelingComponent = null;
		String firstGeo = "";
		String firstOtherGeo = "";
		String firstBio = "";
		for (Map.Entry<IngTypeItem, List<LabelingComponent>> kv : getSortedIngListByType(lblCompositeContext).entrySet()) {

			if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), null, false, false) != null)) {

				Double qtyPerc = roundedDouble(computeQtyPerc(lblCompositeContext, kv.getKey(), DEFAULT_RATIO, false));
				Double volumePerc = roundedDouble(computeVolumePerc(lblCompositeContext, kv.getKey(), DEFAULT_RATIO, false));
				Double qtyPercWithYield = roundedDouble(computeQtyPerc(lblCompositeContext, kv.getKey(), DEFAULT_RATIO, true));
				Double volumePercWithYield = roundedDouble(computeVolumePerc(lblCompositeContext, kv.getKey(), DEFAULT_RATIO, true));

				qtyPerc = (useVolume ? volumePerc : qtyPerc);
				qtyPercWithYield = (useVolume ? volumePercWithYield : qtyPercWithYield);

				String ingTypeLegalName = getLegalIngName(kv.getKey(), null,
						((kv.getValue().size() > 1) || (!kv.getValue().isEmpty() && kv.getValue().get(0).isPlural())), false);

				if (isDoNotDetails(kv.getKey())) {
					ingTypeLegalName = createAllergenAwareLabel(ingTypeLegalName, kv.getValue());
				}

				String geoOriginsLabel = createGeoOriginsLabel(null, kv.getValue(), PlaceOfActivityTypeCode.LAST_PROCESSING);
				String otherGeoOriginsLabel = createGeoOriginsLabel(null, kv.getValue(), PlaceOfActivityTypeCode.EMPTY);
				String bioOriginsLabel = createBioOriginsLabel(null, kv.getValue());
				NodeRef origNodeRef = kv.getKey().getOrigNodeRef() != null ? kv.getKey().getOrigNodeRef() : kv.getKey().getNodeRef();

				String subLabel = getIngTextFormat(kv.getKey(), qtyPerc, kv.getValue().size() > 1).format(new Object[] { ingTypeLegalName, null,
						isDoNotDetails(origNodeRef) ? null
								: renderLabelingComponent(lblCompositeContext, kv.getValue(), true, DEFAULT_RATIO,
										ingsLabelingWithYield && force100Perc ? totalWithYield : null, true, true),
						null, null });

				if ((subLabel != null) && !subLabel.isEmpty()) {

					if (force100Perc && totalWithYield != null && qtyPercWithYield != null) {
						qtyPercWithYield = roundedDouble(
								BigDecimal.valueOf(qtyPercWithYield).divide(totalWithYield, PRECISION).multiply(BigDecimal.valueOf(1d), PRECISION));
					}

					if (first) {
						first = false;
						firstLabelingComponent = kv.getKey();
						firstLabel = subLabel;
						firstQtyPerc = qtyPerc;
						firstQtyPercWithYield = qtyPercWithYield;
						firstGeo = geoOriginsLabel != null ? geoOriginsLabel : "";
						firstBio = bioOriginsLabel != null ? bioOriginsLabel : "";
						firstOtherGeo = otherGeoOriginsLabel != null ? otherGeoOriginsLabel : "";
					} else {

						boolean showPerc = showPerc(kv.getKey());

						tableContent.append(getHtmlTableRowFormat(kv.getKey(), qtyPerc, qtyPercWithYield, false).format(new Object[] {
								decorate(subLabel), showPerc ? formatQty(kv.getKey(), qtyPerc, false) : null,
								geoOriginsLabel != null ? decorate(geoOriginsLabel) : "", bioOriginsLabel != null ? decorate(bioOriginsLabel) : "",
								showPerc ? formatQty(kv.getKey(), qtyPercWithYield, false) : null, otherGeoOriginsLabel }));
					}

				}

			} else {

				for (LabelingComponent component : kv.getValue()) {

					Double qtyPerc = roundedDouble(computeQtyPerc(lblCompositeContext, component, DEFAULT_RATIO, false));
					Double volumePerc = roundedDouble(computeVolumePerc(lblCompositeContext, component, DEFAULT_RATIO, false));

					Double qtyPercWithYield = roundedDouble(computeQtyPerc(lblCompositeContext, component, DEFAULT_RATIO, true));
					Double volumePercWithYield = roundedDouble(computeVolumePerc(lblCompositeContext, component, DEFAULT_RATIO, true));

					String ingName = getLegalIngName(component, null, false, false);
					String geoOriginsLabel = createGeoOriginsLabel(null, component.getGeoOriginsByPlaceOfActivity(),
							PlaceOfActivityTypeCode.LAST_PROCESSING);
					String otherGeoOriginsLabel = createGeoOriginsLabel(null, component.getGeoOriginsByPlaceOfActivity(),
							PlaceOfActivityTypeCode.EMPTY);
					String bioOriginsLabel = createBioOriginsLabel(null, component.getBioOrigins());

					qtyPerc = (useVolume ? volumePerc : qtyPerc);
					qtyPercWithYield = (useVolume ? volumePercWithYield : qtyPercWithYield);

					boolean shouldSkip = shouldSkip(component.getNodeRef(), qtyPerc);
					boolean shouldSkipWithYield = shouldSkip(component.getNodeRef(), qtyPercWithYield);

					if (!(shouldSkip && shouldSkipWithYield)) {

						String subLabel = "";
						if (component instanceof CompositeLabeling) {
							BigDecimal subRatio = computeQtyPerc(lblCompositeContext, component, DEFAULT_RATIO, false);

							if (DeclarationType.Kit.equals(((CompositeLabeling) component).getDeclarationType()) || computePercByParent) {
								subRatio = DEFAULT_RATIO;
							}

							subLabel = getIngTextFormat(component, qtyPerc, ((CompositeLabeling) component).getIngList().size() > 1)
									.format(new Object[] { ingName, qtyPerc, renderCompositeIng((CompositeLabeling) component, subRatio,
											ingsLabelingWithYield && force100Perc ? totalWithYield : null, true, true), null, null });

						} else {
							logger.error(String.format(UNSUPPORTED_ING_TYPE, component.getName()));
						}

						if ((subLabel != null) && !subLabel.isEmpty()) {

							if (force100Perc && totalWithYield != null && qtyPercWithYield != null) {
								qtyPercWithYield = roundedDouble(BigDecimal.valueOf(qtyPercWithYield).divide(totalWithYield, PRECISION)
										.multiply(BigDecimal.valueOf(1d), PRECISION));
							}

							if (first) {
								firstLabelingComponent = component;
								first = false;
								firstLabel = subLabel;
								firstQtyPerc = qtyPerc;
								firstQtyPercWithYield = qtyPercWithYield;
								firstGeo = geoOriginsLabel != null ? geoOriginsLabel : "";
								firstBio = bioOriginsLabel != null ? bioOriginsLabel : "";
								firstOtherGeo = otherGeoOriginsLabel != null ? otherGeoOriginsLabel : "";
							} else {
								boolean showPerc = showPerc(component);

								tableContent.append(getHtmlTableRowFormat(component, qtyPerc, qtyPercWithYield, false).format(
										new Object[] { decorate(subLabel), showPerc && !shouldSkip ? formatQty(component, qtyPerc, false) : null,
												geoOriginsLabel != null ? decorate(geoOriginsLabel) : "",
												bioOriginsLabel != null ? decorate(bioOriginsLabel) : "",
												showPerc && !shouldSkipWithYield ? formatQty(component, qtyPercWithYield, false) : null,
												otherGeoOriginsLabel }));
							}

						}

					} else {
						logger.debug(String.format(REMOVING_NULL_QTY, ingName));
					}

				}
			}

		}

		if (force100PercForTable) {

			BigDecimal diffValue = BigDecimal.valueOf(1d).subtract(total);
			total = total.add(diffValue);
			firstQtyPerc = roundeedValue(firstLabelingComponent, firstQtyPerc, new MessageFormat(htmlTableRowFormat, getContentLocale()))
					+ roundedDouble(diffValue);

		}

		ret.append(getHtmlTableRowFormat(firstLabelingComponent, firstQtyPerc, firstQtyPercWithYield, force100PercForTable).format(new Object[] {
				decorate(firstLabel), showPerc(firstLabelingComponent) ? formatQty(firstLabelingComponent, firstQtyPerc, force100PercForTable) : null,
				decorate(firstGeo), decorate(firstBio),
				showPerc(firstLabelingComponent) ? formatQty(firstLabelingComponent, firstQtyPercWithYield, false) : null, firstOtherGeo }));

		ret.append(tableContent);

		if (showTotal && (total.doubleValue() > 0)) {
			ret.append(applyTotalRoundingMode(new MessageFormat(htmlTableFooterFormat, getContentLocale())).format(new Object[] {
					I18NUtil.getMessage("entity.datalist.item.details.total"), roundedDouble(total), "", "", roundedDouble(totalWithYield) }));
		}

		ret.append("</tbody></table>");
		return ret.toString().replaceAll(" null| \\(null\\)| \\(\\)| \\[null\\]", "").replace(">null<", "><");

	}

	private Double roundeedValue(LabelingComponent component, Double qty, MessageFormat messageFormat) {

		for (Format format : messageFormat.getFormats()) {
			if (format instanceof DecimalFormat decimalFormat) {

				QtyFormater qtyFormater = getQtyFormater(component, qty, false);
				if (qtyFormater != null) {
					//	((DecimalFormat) format).applyPattern(qtyFormater.decimalFormat.toPattern());
					return qtyFormater.round(qty);
				} else {
					return getDefaultQtyFormater(decimalFormat, qty).round(qty);
				}
			}
		}
		return qty;

	}

	private MessageFormat getHtmlTableRowFormat(LabelingComponent component, Double qtyPerc, Double qtyPercWithYield, boolean isForce100Perc) {

		MessageFormat messageFormat = new MessageFormat(htmlTableRowFormat, getContentLocale());

		if (messageFormat.getFormats() != null) {
			boolean isFirst = true;
			for (Format format : messageFormat.getFormats()) {
				if (format instanceof DecimalFormat decimalFormat) {
					Double qty = qtyPerc;
					if (!isFirst) {
						qty = qtyPercWithYield;
					}

					QtyFormater qtyFormater = getQtyFormater(component, qty, false);

					if (qtyFormater != null && !(isFirst && isForce100Perc)) {
						((DecimalFormat) format).applyPattern(qtyFormater.getDecimalFormat().toPattern());
						applyAutomaticPrecicion(decimalFormat, qtyPerc, qtyFormater.getRoundingMode(), false);
					} else {
						if ((isFirst && isForce100Perc)) {
							applyAutomaticPrecicion(decimalFormat, qtyPrecisionThreshold, defaultRoundingMode, true);
						} else {
							applyAutomaticPrecicion(decimalFormat, qty, defaultRoundingMode, false);
						}
					}

					isFirst = false;
				}
			}
		}

		return messageFormat;
	}

	private Double formatQty(LabelingComponent component, Double qtyPerc, boolean isForce100Perc) {
		QtyFormater qtyFormater = getQtyFormater(component, qtyPerc, false);

		if (qtyFormater != null && !(isForce100Perc)) {
			return qtyFormater.apply(qtyPerc);
		}
		return qtyPerc;
	}

	/**
	 * <p>
	 * renderAsFlatHtmlTable.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String renderAsFlatHtmlTable() {
		return renderAsFlatHtmlTable("border-collapse:collapse", false);
	}

	/**
	 * <p>
	 * renderAsFlatHtmlTable.
	 * </p>
	 *
	 * @param styleCss
	 *            a {@link java.lang.String} object.
	 * @param showTotal
	 *            a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public String renderAsFlatHtmlTable(String styleCss, boolean showTotal) {
		return renderAsFlatHtmlTable(styleCss, showTotal, false);
	}

	private class HtmlTableStruct {
		String label;
		Double qtyPerc;
		String geoOriginsLabel;
		String bioOriginsLabel;
		Integer level;

		public HtmlTableStruct(String label, Double qtyPerc, String geoOriginsLabel, String bioOriginsLabel, Integer level) {
			super();
			this.label = label;
			this.geoOriginsLabel = geoOriginsLabel;
			this.bioOriginsLabel = bioOriginsLabel;
			this.qtyPerc = qtyPerc;
			this.level = level;
		}

	}

	/**
	 * <p>
	 * renderAsFlatHtmlTable.
	 * </p>
	 *
	 * @param styleCss
	 *            a {@link java.lang.String} object.
	 * @param showTotal
	 *            a boolean.
	 * @param force100Perc
	 *            a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public String renderAsFlatHtmlTable(String styleCss, boolean showTotal, boolean force100Perc) {

		BigDecimal total = BigDecimal.valueOf(0d);

		StringBuilder tableContent = new StringBuilder();
		StringBuilder ret = new StringBuilder();

		try {
			shouldBreakIngType = true;

			tableContent.append("<table class=\"labelingTable\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\""
					+ ((styleCss == null) || (styleCss).isBlank() ? "border: solid 1px; border-collapse:collapse" : styleCss) + "\" rules=\"none\">");

			List<HtmlTableStruct> flatList = flatCompositeLabeling(lblCompositeContext, DEFAULT_RATIO, 0);
			if (!flatList.isEmpty()) {

				boolean first = true;
				for (HtmlTableStruct tmp : flatList) {

					if (first && force100Perc) {
						first = false;
					} else {

						if (tmp.level == 0) {
							total = total.add(BigDecimal.valueOf(tmp.qtyPerc));
						}

						ret.append(applyRoundingMode(new MessageFormat(htmlTableRowFormat, getContentLocale()), tmp.qtyPerc)
								.format(new Object[] { indent(decorate(tmp.label), tmp.level), tmp.qtyPerc, decorate(tmp.geoOriginsLabel),
										decorate(tmp.bioOriginsLabel), null, null }));

					}
				}

				if (force100Perc) {
					BigDecimal diffValue = BigDecimal.valueOf(1d).subtract(total);

					total = BigDecimal.valueOf(1d);

					Double qtyPerc = roundeedValue(null, flatList.get(0).qtyPerc, new MessageFormat(htmlTableRowFormat, getContentLocale()))
							+ roundedDouble(diffValue);

					tableContent.append(applyTotalRoundingMode(new MessageFormat(htmlTableRowFormat, getContentLocale()))
							.format(new Object[] { decorate(flatList.get(0).label), qtyPerc, decorate(flatList.get(0).geoOriginsLabel),
									decorate(flatList.get(0).bioOriginsLabel), null, null }));

				}
				tableContent.append(ret);
			}

			if (showTotal && (total.doubleValue() > 0)) {
				tableContent.append(applyTotalRoundingMode(new MessageFormat(htmlTableRowFormat, getContentLocale())).format(
						new Object[] { "<b>" + I18NUtil.getMessage("entity.datalist.item.details.total") + "</b>", roundedDouble(total), "" }));
			}

			tableContent.append("</table>");

			return tableContent.toString().replaceAll(" null| \\(null\\)| \\(\\)| \\[null\\]", "").replace(">null<", "><");
		} finally {
			shouldBreakIngType = false;
		}

	}

	private String indent(String label, Integer level) {
		if ((level != null) && (level > 0)) {
			StringBuilder indent = new StringBuilder();
			for (int i = 0; i < level; i++) {
				indent.append("&nbsp;&nbsp;");
			}
			return indent.append(label).toString();

		}

		return label;
	}

	private List<HtmlTableStruct> flatCompositeLabeling(CompositeLabeling parent, BigDecimal ratio, Integer level) {
		List<HtmlTableStruct> ret = new LinkedList<>();

		for (Map.Entry<IngTypeItem, List<LabelingComponent>> kv : getSortedIngListByType(parent).entrySet()) {

			for (LabelingComponent component : kv.getValue()) {

				Double qtyPerc = roundedDouble(computeQtyPerc(parent, component, ratio));
				Double volumePerc = roundedDouble(computeVolumePerc(parent, component, ratio));

				qtyPerc = (useVolume ? volumePerc : qtyPerc);

				String ingName = getLegalIngName(component, qtyPerc, false, false);

				if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), null, false, false) != null)) {

					String ingTypeLegalName = getLegalIngName(kv.getKey(), null,
							((kv.getValue().size() > 1) || (!kv.getValue().isEmpty() && kv.getValue().get(0).isPlural())), false);

					if (isDoNotDetails(kv.getKey())) {
						ingTypeLegalName = createAllergenAwareLabel(ingTypeLegalName, kv.getValue());
					}

					NodeRef origNodeRef = kv.getKey().getOrigNodeRef() != null ? kv.getKey().getOrigNodeRef() : kv.getKey().getNodeRef();

					ingName = getIngTextFormat(kv.getKey(), qtyPerc, false)
							.format(new Object[] { ingTypeLegalName, null, isDoNotDetails(origNodeRef) ? null : ingName, null });

				}

				String geoOriginsLabel = createGeoOriginsLabel(component.getNodeRef(), component.getGeoOriginsByPlaceOfActivity(),
						PlaceOfActivityTypeCode.LAST_PROCESSING);
				String bioOriginsLabel = createBioOriginsLabel(component.getNodeRef(), component.getBioOrigins());

				if (!shouldSkip(component.getNodeRef(), qtyPerc)) {
					if (component instanceof CompositeLabeling) {
						BigDecimal subRatio = computeQtyPerc(parent, component, ratio, false);
						if (DeclarationType.Kit.equals(((CompositeLabeling) component).getDeclarationType()) || computePercByParent) {
							subRatio = DEFAULT_RATIO;
						}

						ret.add(new HtmlTableStruct(ingName, qtyPerc, geoOriginsLabel != null ? geoOriginsLabel : "",
								bioOriginsLabel != null ? bioOriginsLabel : "", level));

						ret.addAll(flatCompositeLabeling((CompositeLabeling) component, subRatio, level + 1));

					} else {
						logger.error(String.format(UNSUPPORTED_ING_TYPE, component.getName()));
					}

				} else {
					logger.debug(String.format(REMOVING_NULL_QTY, ingName));
				}

			}

		}
		return ret;
	}

	private boolean isDoNotDetails(IngTypeItem ingTypeItem) {
		return isDoNotDetails(ingTypeItem.getOrigNodeRef() != null ? ingTypeItem.getOrigNodeRef() : ingTypeItem.getNodeRef())
				|| (ingTypeItem.getDecThreshold() != null && ingTypeItem.getQty(ingsLabelingWithYield) <= (ingTypeItem.getDecThreshold() / 100));
	}

	private boolean isDoNotDetails(NodeRef nodeRef) {

		Locale currentLocale = I18NUtil.getLocale();
		if (nodeDeclarationFilters.containsKey(nodeRef)) {
			for (DeclarationFilterRule declarationFilter : nodeDeclarationFilters.get(nodeRef)) {
				if (DeclarationType.DoNotDetails.equals(declarationFilter.getDeclarationType())
						&& matchFormule(declarationFilter, new LabelingFormulaFilterContext(formulaService, null))
						&& declarationFilter.matchLocale(currentLocale)) {
					return true;
				}
			}

		}
		return false;
	}

	private Double roundeedValue(Double qty, LabelingComponent lblComponent) {
		QtyFormater qtyFormater = getQtyFormater(lblComponent, qty, false);
		if (qtyFormater != null) {
			return qtyFormater.round(qty);
		}
		return getDefaultQtyFormater(null, qty).round(qty);

	}

	private BigDecimal getTotal(CompositeLabeling compositeLabeling, boolean withYield) {
		BigDecimal total = BigDecimal.valueOf(0d);

		for (Map.Entry<IngTypeItem, List<LabelingComponent>> kv : getSortedIngListByType(compositeLabeling).entrySet()) {

			if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), null, false, false) != null)) {

				Double qtyPerc = roundedDouble(computeQtyPerc(compositeLabeling, kv.getKey(), DEFAULT_RATIO, withYield));
				Double volumePerc = roundedDouble(computeVolumePerc(compositeLabeling, kv.getKey(), DEFAULT_RATIO, withYield));
				qtyPerc = (useVolume ? volumePerc : qtyPerc);

				if (qtyPerc != null && qtyPerc > 0d) {
					total = total.add(BigDecimal.valueOf(roundeedValue(qtyPerc, kv.getKey())));
				}

			} else {

				for (LabelingComponent component : kv.getValue()) {

					Double qtyPerc = roundedDouble(computeQtyPerc(compositeLabeling, component, DEFAULT_RATIO, withYield));
					Double volumePerc = roundedDouble(computeVolumePerc(compositeLabeling, component, DEFAULT_RATIO, withYield));

					qtyPerc = (useVolume ? volumePerc : qtyPerc);

					if (!shouldSkip(component.getNodeRef(), qtyPerc) && (qtyPerc != null) && qtyPerc > 0) {
						total = total.add(BigDecimal.valueOf(roundeedValue(qtyPerc, component)));

					}
				}
			}
		}
		return total;

	}

	private String renderCompositeIng(CompositeLabeling compositeLabeling, BigDecimal ratio, BigDecimal total, boolean hideGeo, boolean hideBio) {
		StringBuilder ret = new StringBuilder();
		boolean appendEOF = false;
		boolean first = true;

		boolean applySeparatorRule = true;

		for (Map.Entry<IngTypeItem, List<LabelingComponent>> kv : getSortedIngListByType(compositeLabeling).entrySet()) {

			StringBuilder toAppend = new StringBuilder();

			Double qtyPerc = null;

			BigDecimal subTotal = first || ingsLabelingWithYield ? total : null;
			if ((kv.getKey() != null) && (getLegalIngName(kv.getKey(), null, false, false) != null)) {

				qtyPerc = roundedDouble(computeQtyPerc(compositeLabeling, kv.getKey(), ratio));
				Double volumePerc = roundedDouble(computeVolumePerc(compositeLabeling, kv.getKey(), ratio));
				qtyPerc = (useVolume ? volumePerc : qtyPerc);
				if (ingsLabelingWithYield) {
					kv.getKey().setQtyWithYield(qtyPerc);
					kv.getKey().setVolumeWithYield(volumePerc);
				} else {
					kv.getKey().setQty(qtyPerc);
					kv.getKey().setVolume(volumePerc);
				}

				String ingTypeLegalName = getLegalIngName(kv.getKey(), qtyPerc,
						((kv.getValue().size() > 1) || (!kv.getValue().isEmpty() && kv.getValue().get(0).isPlural())), false);

				if (isDoNotDetails(kv.getKey())) {
					ingTypeLegalName = createAllergenAwareLabel(ingTypeLegalName, kv.getValue());
				}

				String geoOriginsLabel = createGeoOriginsLabel(kv.getKey().getNodeRef(), kv.getValue(), PlaceOfActivityTypeCode.LAST_PROCESSING);
				String bioOriginsLabel = createBioOriginsLabel(kv.getKey().getNodeRef(), kv.getValue());

				NodeRef origNodeRef = kv.getKey().getOrigNodeRef() != null ? kv.getKey().getOrigNodeRef() : kv.getKey().getNodeRef();

				toAppend.append(getIngTextFormat(kv.getKey(), qtyPerc, kv.getValue().size() > 1).format(new Object[] { ingTypeLegalName, qtyPerc,
						isDoNotDetails(origNodeRef) ? null
								: renderLabelingComponent(compositeLabeling, kv.getValue(), true, ratio, subTotal, hideGeo, hideBio),
						hideGeo ? null : geoOriginsLabel, hideBio ? null : bioOriginsLabel }));

			} else {
				if (!kv.getValue().isEmpty()) {
					qtyPerc = roundedDouble(computeQtyPerc(compositeLabeling, kv.getValue().get(0), ratio));
					Double volumePerc = roundedDouble(computeVolumePerc(compositeLabeling, kv.getValue().get(0), ratio));
					qtyPerc = (useVolume ? volumePerc : qtyPerc);
				}

				toAppend.append(renderLabelingComponent(compositeLabeling, kv.getValue(), false, ratio, subTotal, hideGeo, hideBio));
			}

			first = false;
			if ((toAppend != null) && !toAppend.toString().isEmpty()) {
				if (ret.length() > 0) {
					if (appendEOF) {
						ret.append(HTML_BREAK_LINE);
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

								if (compositeLabeling instanceof IngItem) {
									ret.append(getLocaleSeparator(subIngsSeparator));
								} else {
									ret.append(getLocaleSeparator(defaultSeparator));
								}
							}
						} else {
							if (compositeLabeling instanceof IngItem) {
								ret.append(getLocaleSeparator(subIngsSeparator));
							} else {
								ret.append(getLocaleSeparator(defaultSeparator));
							}

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

		if (!compositeLabeling.getIngListAtEnd().isEmpty()) {
			if (ret.length() > 0) {
				ret.append(getLocaleSeparator(atEndSeparator));
			}
			ret.append(renderLabelingComponent(compositeLabeling, compositeLabeling.getIngListAtEnd().values().stream().collect(Collectors.toList()),
					false, null, null, true, true));
		}

		return ret.toString().trim();
	}

	private String getLocaleSeparator(String separator) {
		if ("ar".equalsIgnoreCase((I18NUtil.getContentLocale().getLanguage()))) {
			return separator.replace(",", "،");
		}
		return separator;
	}

	private Locale getContentLocale() {
		if ("ar".equalsIgnoreCase((I18NUtil.getContentLocale().getLanguage()))) {
			return Locale.US;
		}
		return I18NUtil.getContentLocale();
	}

	private StringBuilder renderLabelingComponent(CompositeLabeling parent, List<LabelingComponent> subComponents, boolean isIngType,
			BigDecimal ratio, BigDecimal total, boolean hideGeo, boolean hideBio) {

		StringBuilder ret = new StringBuilder();

		boolean appendEOF = false;
		boolean first = true;

		for (LabelingComponent component : subComponents) {

			Double qtyPerc = roundedDouble(computeQtyPerc(parent, component, ratio));
			Double volumePerc = roundedDouble(computeVolumePerc(parent, component, ratio));
			BigDecimal subTotal = first || ingsLabelingWithYield ? total : null;
			Boolean useTotalPrecision = first && total != null;

			qtyPerc = (useVolume ? volumePerc : qtyPerc);
			if (ingsLabelingWithYield && total != null && qtyPerc != null) {
				qtyPerc = roundeedValue(
						roundedDouble(BigDecimal.valueOf(qtyPerc).divide(total, PRECISION).multiply(BigDecimal.valueOf(1d), PRECISION)), component);
			} else {
				if (first && (total != null)) {
					BigDecimal diffValue = BigDecimal.valueOf(1d).subtract(total);
					qtyPerc = roundeedValue(qtyPerc, component) + roundedDouble(diffValue);
				}
			}

			String ingName = getLegalIngName(component, qtyPerc, false, useTotalPrecision);

			String geoOriginsLabel = createGeoOriginsLabel(component.getNodeRef(), component.getGeoOriginsByPlaceOfActivity(),
					PlaceOfActivityTypeCode.LAST_PROCESSING);
			String bioOriginsLabel = createBioOriginsLabel(component.getNodeRef(), component.getBioOrigins());

			if (logger.isDebugEnabled()) {

				logger.debug(" --" + ingName + "(" + component.getNodeRef() + ") qtyRMUsed: " + parent.getQtyTotal() + " qtyPerc " + qtyPerc
						+ " apply precision (" + (toApplyThresholdItems.contains(component.getNodeRef()) && ((qtyPerc - qtyPrecisionThreshold) > 0))
						+ "), ratio: " + ratio);
			}

			if (!shouldSkip(component.getNodeRef(), qtyPerc)) {

				String toAppend = "";

				if (component instanceof CompositeLabeling) {

					MessageFormat formater = getIngTextFormat(component, qtyPerc, ((CompositeLabeling) component).getIngList().size() > 1);
					BigDecimal subRatio = computeQtyPerc(parent, component, ratio, ingsLabelingWithYield && (component instanceof IngItem));

					if (DeclarationType.Kit.equals(((CompositeLabeling) component).getDeclarationType()) || computePercByParent) {
						subRatio = DEFAULT_RATIO;
					} else if (Boolean.TRUE.equals(useTotalPrecision)) {
						applyTotalRoundingMode(formater);
					}

					toAppend = formater.format(
							new Object[] { ingName, qtyPerc, renderCompositeIng((CompositeLabeling) component, subRatio, subTotal, hideGeo, hideBio),
									hideGeo ? null : geoOriginsLabel, hideBio ? null : bioOriginsLabel });

					first = false;

				} else {
					logger.error(String.format(UNSUPPORTED_ING_TYPE, component.getName()));
				}

				if ((toAppend != null) && !toAppend.isEmpty()) {
					if (ret.length() > 0) {
						if (appendEOF) {
							ret.append(HTML_BREAK_LINE);
						} else {
							if (isIngType) {
								ret.append(getLocaleSeparator(ingTypeDefaultSeparator));
							} else if (parent instanceof IngItem) {
								ret.append(getLocaleSeparator(subIngsSeparator));
							} else {
								ret.append(getLocaleSeparator(defaultSeparator));
							}

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
				logger.debug(String.format(REMOVING_NULL_QTY, ingName));
			}

		}

		return ret;
	}

	private String createGeoOriginsLabel(NodeRef nodeRef, List<LabelingComponent> components, PlaceOfActivityTypeCode placeOfActivity) {

		if (((showAllGeo != null) && showAllGeo.matchLocale(I18NUtil.getLocale()))
				|| ((nodeRef != null) && showGeoRules.containsKey(nodeRef) && showGeoRules.get(nodeRef).matchLocale(I18NUtil.getLocale()))) {

			if ((components != null) && !components.isEmpty()) {

				EnumMap<PlaceOfActivityTypeCode, Set<NodeRef>> geoOrigins = new EnumMap<>(PlaceOfActivityTypeCode.class);

				for (LabelingComponent component : components) {

					for (Map.Entry<PlaceOfActivityTypeCode, Set<NodeRef>> entry : component.getGeoOriginsByPlaceOfActivity().entrySet()) {
						if (geoOrigins.containsKey(entry.getKey())) {
							geoOrigins.get(entry.getKey()).addAll(entry.getValue());
						} else {
							geoOrigins.put(entry.getKey(), new HashSet<>(entry.getValue()));
						}

					}
				}

				return createGeoOriginsLabel(null, geoOrigins, placeOfActivity);
			}

		}

		return null;
	}

	private String createGeoOriginsLabel(NodeRef nodeRef, Map<PlaceOfActivityTypeCode, Set<NodeRef>> geoOriginsByPlaceOfActivity,
			PlaceOfActivityTypeCode placeOfActivity) {

		ShowRule showRule = showAllGeo;
		if (showGeoRules.containsKey(nodeRef) && showGeoRules.get(nodeRef).matchLocale(I18NUtil.getLocale())) {
			showRule = showGeoRules.get(nodeRef);
		}

		if ((showRule != null) && showRule.matchLocale(I18NUtil.getLocale())) {

			List<PlaceOfActivityTypeCode> filters = new LinkedList<>();

			if ((showRule.format != null) && !showRule.format.isEmpty()) {
				for (String splitted : showRule.format.split(",")) {
					try {
						filters.add(PlaceOfActivityTypeCode.valueOf(splitted));
					} catch (IllegalArgumentException e) {
						//Do nothing
					}
				}
			}

			if (filters.isEmpty()) {
				filters.add(placeOfActivity);
			}

			StringBuilder ret = new StringBuilder();

			for (PlaceOfActivityTypeCode filter : filters) {

				if (!ret.toString().isBlank()) {
					ret.append(geoPlaceOfActiviySeparator);
				}

				Set<NodeRef> geoOrigins = geoOriginsByPlaceOfActivity.get(filter);

				if ((geoOrigins != null) && !geoOrigins.isEmpty()) {

					Set<String> geoOriginsBuffer = new HashSet<>();
					for (NodeRef geoOrigin : geoOrigins) {
						geoOriginsBuffer.add(getCharactName(geoOrigin));
					}

					if ((filters.size() > 1) && !PlaceOfActivityTypeCode.EMPTY.equals(filter)) {
						ret.append((MessageFormat.format(geoPlaceOfActivityFormat, getPlaceOfActivityName(filter),
								String.join(getLocaleSeparator(geoOriginsSeparator), geoOriginsBuffer))));
					} else {
						ret.append(String.join(getLocaleSeparator(geoOriginsSeparator), geoOriginsBuffer));
					}
				}

			}
			return ret.toString().isBlank() ? null : ret.toString();
		}
		return null;
	}

	private String getPlaceOfActivityName(PlaceOfActivityTypeCode filter) {
		return I18NUtil.getMessage("listconstraint.gs1_productActivityTypeCodes." + filter.toString());
	}

	private String createBioOriginsLabel(NodeRef nodeRef, List<LabelingComponent> components) {

		if (((showAllBio != null) && showAllBio.matchLocale(I18NUtil.getLocale()))
				|| ((nodeRef != null) && showBioRules.containsKey(nodeRef) && showBioRules.get(nodeRef).matchLocale(I18NUtil.getLocale()))) {

			if ((components != null) && !components.isEmpty()) {

				Set<NodeRef> bioOrigins = new HashSet<>();

				for (LabelingComponent component : components) {
					if (component.getBioOrigins() != null) {
						bioOrigins.addAll(component.getBioOrigins());
					}
				}

				return createBioOriginsLabel(null, bioOrigins);
			}

		}

		return null;
	}

	private String createBioOriginsLabel(NodeRef nodeRef, Set<NodeRef> bioOrigins) {

		if (((showAllBio != null) && showAllBio.matchLocale(I18NUtil.getLocale()))
				|| ((nodeRef != null) && showBioRules.containsKey(nodeRef) && showBioRules.get(nodeRef).matchLocale(I18NUtil.getLocale()))) {

			if ((bioOrigins != null) && !bioOrigins.isEmpty()) {

				Set<String> bioOriginsBuffer = new HashSet<>();
				for (NodeRef bioOrigin : bioOrigins) {
					bioOriginsBuffer.add(getCharactName(bioOrigin));
				}
				return String.join(getLocaleSeparator(bioOriginsSeparator), bioOriginsBuffer);
			}
		}
		return null;
	}

	private boolean shouldSkip(NodeRef nodeRef, Double qtyPerc) {

		boolean shouldSkip = !((qtyPerc == null) || (toApplyThresholdItems.contains(nodeRef) && (qtyPerc > qtyPrecisionThreshold))
				|| (!toApplyThresholdItems.contains(nodeRef) && (qtyPerc > 0)));

		if (!shouldSkip && qtyPerc != null) {

			Locale currentLocale = I18NUtil.getLocale();

			if (nodeDeclarationFilters.containsKey(nodeRef)) {
				for (DeclarationFilterRule declarationFilter : nodeDeclarationFilters.get(nodeRef)) {
					if (declarationFilter.isThreshold() && (qtyPerc < (declarationFilter.getThreshold() / 100d))
							&& declarationFilter.matchLocale(currentLocale)) {
						return true;
					}
				}
			}

			for (DeclarationFilterRule declarationFilter : declarationFilters) {
				if (declarationFilter.isThreshold() && (qtyPerc < (declarationFilter.getThreshold() / 100d))
						&& declarationFilter.matchLocale(currentLocale)) {
					return true;
				}

			}

		}

		return shouldSkip;
	}

	/**
	 * <p>
	 * createJsonLog.
	 * </p>
	 *
	 * @param mergedLabeling
	 *            a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public String createJsonLog(boolean mergedLabeling) {
		if (!mergedLabeling) {
			return createJsonLog(lblCompositeContext, null, DEFAULT_RATIO, new HashSet<>()).toString();
		}
		return createJsonLog(mergedLblCompositeContext, null, DEFAULT_RATIO, new HashSet<>()).toString();
	}

	@SuppressWarnings("unchecked")
	private JSONObject createJsonLog(LabelingComponent component, CompositeLabeling parent, BigDecimal ratio, Set<LabelingComponent> visited) {

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
			tree.put("legal", decorate(getLegalIngName(component, null, component.isPlural(), false)));

			Double qtyPerc = roundedDouble(computeQtyPerc(parent, component, ratio));
			Double volumePerc = roundedDouble(computeVolumePerc(parent, component, ratio));

			if (volumePerc != null) {
				tree.put("vol", volumePerc * 100);
			}
			if (qtyPerc != null) {
				tree.put("qte", qtyPerc * 100);

				if (component instanceof CompositeLabeling) {

					ratio = BigDecimal.valueOf(qtyPerc);
					if (DeclarationType.Kit.equals(((CompositeLabeling) component).getDeclarationType()) || computePercByParent) {
						ratio = DEFAULT_RATIO;
					}

				}

			}
			if (!component.getAllergens().isEmpty()
					&& (!(component instanceof CompositeLabeling) || ((CompositeLabeling) component).getIngList().isEmpty())) {
				JSONArray tmp = new JSONArray();
				for (NodeRef allergen : component.getAllergens()) {
					tmp.add(getCharactName(allergen));
				}
				tree.put("allergens", tmp);
			}

			Set<NodeRef> geos = component.getGeoOriginsByPlaceOfActivity().computeIfAbsent(PlaceOfActivityTypeCode.LAST_PROCESSING,
					a -> new HashSet<>());

			if (!geos.isEmpty() && (!(component instanceof CompositeLabeling) || ((CompositeLabeling) component).getIngList().isEmpty())) {
				JSONArray geoOrigins = new JSONArray();

				for (NodeRef geoOrigin : geos) {
					geoOrigins.add(getCharactName(geoOrigin));
				}
				tree.put("geoOrigins", geoOrigins);
			}

			if (!component.getBioOrigins().isEmpty()
					&& (!(component instanceof CompositeLabeling) || ((CompositeLabeling) component).getIngList().isEmpty())) {
				JSONArray bioOrigins = new JSONArray();
				for (NodeRef bioOrigin : component.getBioOrigins()) {
					bioOrigins.add(getCharactName(bioOrigin));
				}
				tree.put("bioOrigins", bioOrigins);
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
						ingTypeJson.put("legal", decorate(getLegalIngName(kv.getKey(), null,
								(kv.getValue().size() > 1) || (!kv.getValue().isEmpty() && kv.getValue().get(0).isPlural()), false)));

						qtyPerc = roundedDouble(computeQtyPerc((CompositeLabeling) component, kv.getKey(), ratio));
						volumePerc = roundedDouble(computeVolumePerc((CompositeLabeling) component, kv.getKey(), ratio));
						if (ingsLabelingWithYield) {
							kv.getKey().setQtyWithYield(qtyPerc);
							kv.getKey().setVolumeWithYield(volumePerc);
						} else {
							kv.getKey().setQty(qtyPerc);
							kv.getKey().setVolume(volumePerc);
						}

						if (volumePerc != null) {
							ingTypeJson.put("vol", volumePerc * 100);
						}
						if (qtyPerc != null) {
							ingTypeJson.put("qte", qtyPerc * 100);
						}

						JSONArray ingTypeJsonChildren = new JSONArray();
						for (LabelingComponent childComponent : kv.getValue()) {
							ingTypeJsonChildren.add(createJsonLog(childComponent, composite, ratio, visited));
						}
						ingTypeJson.put("children", ingTypeJsonChildren);
						children.add(ingTypeJson);
					} else {

						for (LabelingComponent childComponent : kv.getValue()) {
							children.add(createJsonLog(childComponent, composite, ratio, visited));
						}
					}

				}

				tree.put("children", children);

			}

		}

		return tree;
	}

	private String decorate(String input) {
		if (input != null) {

			for (LabelingDecorator labelingDecorator : labelingDecorators) {
				if (labelingDecorator.matchLocale(I18NUtil.getLocale())) {
					input = labelingDecorator.decorate(input);
				}
			}

			return input.replaceAll(" null| \\(null\\)| \\(\\)| \\[null\\]", "").replace(":,", ",").replaceAll(":$", "").replace(">null<", "><")
					.replace("  ", "").trim();
		}
		return "";
	}

	/**
	 * <p>
	 * isGroup.
	 * </p>
	 *
	 * @param component
	 *            a {@link fr.becpg.repo.product.data.ing.LabelingComponent}
	 *            object.
	 * @return a boolean.
	 */
	public boolean isGroup(LabelingComponent component) {
		return (component instanceof CompositeLabeling) && ((CompositeLabeling) component).isGroup();
	}

	/**
	 * <p>
	 * computeQtyPerc.
	 * </p>
	 *
	 * @param parent
	 *            a {@link fr.becpg.repo.product.data.ing.CompositeLabeling}
	 *            object.
	 * @param component
	 *            a {@link fr.becpg.repo.product.data.ing.LabelingComponent}
	 *            object.
	 * @param ratio
	 *            a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public BigDecimal computeQtyPerc(CompositeLabeling parent, LabelingComponent component, BigDecimal ratio) {
		return computeQtyPerc(parent, component, ratio, ingsLabelingWithYield);
	}

	private BigDecimal computeQtyPerc(CompositeLabeling parent, LabelingComponent component, BigDecimal ratio, boolean withYield) {

		if ((ratio == null) || (parent == null)) {
			return null;
		}
		Double qty = component.getQty(withYield);

		if ((parent.getQtyTotal() != null) && (parent.getQtyTotal() != 0d) && (qty != null)) {

			return BigDecimal.valueOf(qty).multiply(ratio, PRECISION).divide(BigDecimal.valueOf(parent.getQtyTotal()), PRECISION);
		}
		return qty != null ? BigDecimal.valueOf(qty) : null;
	}

	/**
	 * <p>
	 * computeVolumePerc.
	 * </p>
	 *
	 * @param parent
	 *            a {@link fr.becpg.repo.product.data.ing.CompositeLabeling}
	 *            object.
	 * @param component
	 *            a {@link fr.becpg.repo.product.data.ing.LabelingComponent}
	 *            object.
	 * @param ratio
	 *            a {@link java.lang.Double} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public BigDecimal computeVolumePerc(CompositeLabeling parent, LabelingComponent component, BigDecimal ratio) {
		return computeVolumePerc(parent, component, ratio, ingsLabelingWithYield);
	}

	private BigDecimal computeVolumePerc(CompositeLabeling parent, LabelingComponent component, BigDecimal ratio, boolean withYield) {
		if ((ratio == null) || (parent == null)) {
			return null;
		}

		Double volume = component.getVolume(withYield);
		if ((parent.getVolumeTotal() != null) && (parent.getVolumeTotal() != 0d) && (volume != null)) {
			return BigDecimal.valueOf(volume).multiply(ratio, PRECISION).divide(BigDecimal.valueOf(parent.getVolumeTotal()), PRECISION);
		}
		return volume != null ? BigDecimal.valueOf(volume) : null;
	}

	Map<IngTypeItem, List<LabelingComponent>> getSortedIngListByType(CompositeLabeling compositeLabeling) {

		Locale currentLocale = I18NUtil.getLocale();

		Map<IngTypeItem, List<LabelingComponent>> tmp = new LinkedHashMap<>();

		boolean keepOrder = false;
		for (CompositeLabeling lblComponent : compositeLabeling.getIngList().values()) {
			IngTypeItem ingType = lblComponent.getIngType() != null ? lblComponent.getIngType().createCopy() : null;

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
				if ((ingType != null) && nodeDeclarationFilters.containsKey(ingType.getNodeRef())) {
					boolean shouldBreak = false;
					for (DeclarationFilterRule declarationFilter : nodeDeclarationFilters.get(ingType.getNodeRef())) {
						if (DeclarationType.Omit.equals(declarationFilter.getDeclarationType()) && declarationFilter.matchLocale(currentLocale)
								&& matchFormule(declarationFilter, new LabelingFormulaFilterContext(formulaService, ingType))) {
							shouldBreak = true;
							break;
						} else if ((DeclarationType.DoNotDeclare.equals(declarationFilter.getDeclarationType()) && !declarationFilter.isThreshold()
								&& matchFormule(declarationFilter, new LabelingFormulaFilterContext(formulaService, ingType))
								&& declarationFilter.matchLocale(currentLocale))) {
							ingType = ingType.createCopy();
							ingType.setIsDoNotDeclare(true);
						} else if ((DeclarationType.DoNotDetailsAtEnd.equals(declarationFilter.getDeclarationType())
								&& !declarationFilter.isThreshold() && declarationFilter.matchLocale(currentLocale))
								&& matchFormule(declarationFilter, new LabelingFormulaFilterContext(formulaService, ingType))) {
							ingType = ingType.createCopy();
							ingType.setIsLastGroup(true);
						}
					}
					if (shouldBreak) {
						continue;
					}

				}

				if ((ingType != null) && ingType.doNotDeclare() && !ingType.lastGroup()) {
					ingType = null;
				}

			}

			if ((lblComponent.getQty(ingsLabelingWithYield) == null) || (useVolume && (lblComponent.getVolume(ingsLabelingWithYield) == null))) {
				keepOrder = true;
			}

			if ((lblComponent instanceof CompositeLabeling) && lblComponent.isGroup()) {
				ingType = IngTypeItem.DEFAULT_GROUP;
			} else if (shouldBreakIngType && (ingType != null)) {

				ingType = ingType.createCopy();

				if ((ingType.getNodeRef() != null) && (ingType.getOrigNodeRef() == null)) {
					ingType.setOrigNodeRef(ingType.getNodeRef());
				}
				ingType.setNodeRef(new NodeRef(RepoConsts.SPACES_STORE, ING_TYPE_PREFIX + lblComponent.getNodeRef().hashCode()));

			}

			if (ingType == null) {
				ingType = new IngTypeItem();
				ingType.setNodeRef(new NodeRef(RepoConsts.SPACES_STORE, ING_TYPE_PREFIX + lblComponent.getNodeRef().hashCode()));
			}

			// Reset qty for equality
			ingType.setQties(0d);

			List<LabelingComponent> subSortedList = tmp.get(ingType);

			if (subSortedList == null) {
				subSortedList = new LinkedList<>();
				tmp.put(ingType, subSortedList);
			}
			subSortedList.add(lblComponent);

		}

		keepOrder = forceKeepOrder || (DeclarationType.Detail.equals(compositeLabeling.getDeclarationType()) && keepOrder);

		List<Map.Entry<IngTypeItem, List<LabelingComponent>>> entries = new ArrayList<>(tmp.entrySet());
		/*
		 * Compute IngType Qty
		 */

		for (Map.Entry<IngTypeItem, List<LabelingComponent>> entry : entries) {
			Double qty = 0d;
			Double vol = 0d;
			Double qtyWithYield = 0d;
			Double volWithYield = 0d;

			for (LabelingComponent lblComponent : entry.getValue()) {
				if (lblComponent.getQty() != null) {
					qty += lblComponent.getQty();
				}
				if (lblComponent.getVolume() != null) {
					vol += lblComponent.getVolume();
				}
				if (lblComponent.getQtyWithYield() != null) {
					qtyWithYield += lblComponent.getQtyWithYield();
				}
				if (lblComponent.getVolumeWithYield() != null) {
					volWithYield += lblComponent.getVolumeWithYield();
				}
			}
			entry.getKey().setQty(qty);
			entry.getKey().setVolume(vol);
			entry.getKey().setQtyWithYield(qtyWithYield);
			entry.getKey().setVolumeWithYield(volWithYield);

		}

		/*
		 * Sort by qty, default is always first
		 */

		if (!keepOrder) {

			for (Map.Entry<IngTypeItem, List<LabelingComponent>> entry : entries) {
				sort(entry.getValue());
			}

			Locale currentLocal = I18NUtil.getLocale();
			try {
				if (sortWithSpecificLocale != null) {
					I18NUtil.setLocale(MLTextHelper.parseLocale(sortWithSpecificLocale));
				}

				Collections.sort(entries, (a, b) -> {
					int result = compareLabelingComponents(a.getKey(), b.getKey());
					if (result == 0) {
						String nameA = getLegalIngName(a.getKey());
						if (nameA == null && !a.getValue().isEmpty()) {
							nameA = getLegalIngName(a.getValue().get(0));
						}

						String nameB = getLegalIngName(b.getKey());
						if (nameB == null && !b.getValue().isEmpty()) {
							nameB = getLegalIngName(b.getValue().get(0));
						}

						result = Comparator.nullsLast(String::compareTo).compare(nameA, nameB);
					}
					return result;
				});

			} finally {
				I18NUtil.setLocale(currentLocal);
			}
		}

		Map<IngTypeItem, List<LabelingComponent>> sortedIngListByType = new LinkedHashMap<>();
		for (Map.Entry<IngTypeItem, List<LabelingComponent>> entry : entries) {
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

						if ((prec.getKey().getQtyWithYield() != null) && (entry.getKey().getQtyWithYield() != null)) {
							prec.getKey().setQtyWithYield(prec.getKey().getQtyWithYield() + entry.getKey().getQtyWithYield());
						}

						if ((prec.getKey().getVolumeWithYield() != null) && (entry.getKey().getVolumeWithYield() != null)) {
							prec.getKey().setVolumeWithYield(prec.getKey().getVolumeWithYield() + entry.getKey().getVolumeWithYield());
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
		} else if (aggregateRule.getLabel() != null && !MLTextHelper.isEmpty(aggregateRule.getLabel())) {

			ingType = new IngTypeItem();
			ingType.setLegalName(aggregateRule.getLabel());
			ingType.setNodeRef(new NodeRef(RepoConsts.SPACES_STORE, ING_TYPE_PREFIX + aggregateRule.getLabel().getDefaultValue()));
		}

		return ingType;
	}

	/**
	 * <p>
	 * matchFormule.
	 * </p>
	 *
	 * @return a boolean.
	 * @param formulaFilter a {@link fr.becpg.repo.product.formulation.labeling.AbstractFormulaFilterRule} object
	 * @param formulaFilterContext a {@link fr.becpg.repo.product.data.spel.LabelingFormulaFilterContext} object
	 */
	public boolean matchFormule(AbstractFormulaFilterRule formulaFilter, LabelingFormulaFilterContext formulaFilterContext) {
		if ((formulaFilter.getFormula() != null) && !formulaFilter.getFormula().isEmpty()) {
			if (!MeatType.isMeatType(formulaFilter.getFormula())) {
				try {
					ExpressionParser parser = new BeCPGSpelExpressionParser();

					StandardEvaluationContext dataContext = formulaService.createCustomSpelContext(entity, formulaFilterContext, false);

					Expression exp = parser.parseExpression(SpelHelper.formatFormula(formulaFilter.getFormula()));

					Boolean ret = exp.getValue(dataContext, Boolean.class);

					if (Boolean.TRUE.equals(ret) && logger.isDebugEnabled()) {
						logger.debug("Matching formula :" + formulaFilter.getFormula());
					}

					return Boolean.TRUE.equals(ret);
				} catch (Exception e) {

					getEntity().getReqCtrlList().add(ReqCtrlListDataItem.forbidden().withMessage(
							MLTextHelper.getI18NMessage("message.formulate.labelRule.error", formulaFilter.getRuleName(), e.getLocalizedMessage()))
							.ofDataType(RequirementDataType.Labelling)

					);
					if (logger.isDebugEnabled()) {
						logger.debug("Cannot evaluate formula :" + formulaFilter.getFormula() + " on " + formulaFilterContext.toString(), e);
					}
				}
			}
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LabelingFormulaContext [compositeLabeling=" + lblCompositeContext + ", textFormaters=" + textFormaters + ", renameRules="
				+ renameRules + ", nodeDeclarationFilters=" + nodeDeclarationFilters + ", declarationFilters=" + declarationFilters + "]";
	}

}
