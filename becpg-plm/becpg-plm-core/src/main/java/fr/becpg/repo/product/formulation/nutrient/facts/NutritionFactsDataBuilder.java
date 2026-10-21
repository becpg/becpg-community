/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
package fr.becpg.repo.product.formulation.nutrient.facts;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.nutrient.RegulatedNutrient;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;
import fr.becpg.repo.product.formulation.nutrient.SupplementalIngredientMarking;
import fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsOptions.SharedDailyValue;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>Turns a formulated product into the fully formatted model a nutrition facts template consumes.</p>
 *
 * <p>No value is computed here: the rounding and the daily values were produced at formulation time
 * and are read back through
 * {@link fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper#extractRegulatedNutrient},
 * the very same source the BIRT data source reads. What this builder does is select the nutrients a
 * regulation wants to see, order them, name them and split them into the two blocks a panel draws.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NutritionFactsDataBuilder {

	private static final double ZERO_THRESHOLD = 0.0001d;

	/** Serving size shown without trailing zeros: "55g" and not "55.0g". */
	private static final String SERVING_SIZE_PATTERN = "#.##";

	private static final String PERCENT_SIGN = "%";

	/** What joins the two languages of a bilingual line: "Fat / Lipides". */
	private static final String LANGUAGE_SEPARATOR = " / ";

	/** What separates the measure of a referential unit from its basis, "mg/100g". */
	private static final char UNIT_SEPARATOR = '/';

	/**
	 * Marker a Canadian panel puts in front of a nutrient folded into the line above, "+ Trans".
	 * It belongs to the layout and not to the name, so a bilingual line carries it once.
	 */
	private static final String CONTINUATION_MARKER = "+ ";

	private final NodeService mlNodeService;

	private final AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>Constructor for NutritionFactsDataBuilder.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public NutritionFactsDataBuilder(NodeService mlNodeService, AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.mlNodeService = mlNodeService;
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Builds the panel model, using the default options of the regulation of the locale.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param locale a {@link java.util.Locale} object
	 * @param format a {@link java.lang.String} object, the panel format code
	 * @return a {@link fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsData} object
	 */
	public NutritionFactsData build(ProductData product, Locale locale, String format) {
		return build(product, locale, format, NutritionFactsOptions.forRegulation(RegulationFormulationHelper.getLocalKey(locale)));
	}

	/**
	 * <p>Builds the panel model.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param locale a {@link java.util.Locale} object
	 * @param format a {@link java.lang.String} object, the panel format code
	 * @param options a {@link fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsOptions} object
	 * @return a {@link fr.becpg.repo.product.formulation.nutrient.facts.NutritionFactsData} object
	 */
	public NutritionFactsData build(ProductData product, Locale locale, String format, NutritionFactsOptions options) {

		List<Locale> languages = options.languages(locale);
		Locale panelLocale = languages.get(0);
		Map<String, String> labels = NutritionFactsLabelResolver.panelLabels(options.regulationKey(), panelLocale);
		RegulatedNutrients regulated = collectNutrients(product, panelLocale, options, permittedSupplementalIngredients(format, options));

		return new NutritionFactsData(format, options.regulationKey(), buildServing(product, languages, labels, options),
				buildCalories(regulated, languages, options), buildLines(regulated, languages, options, Block.NUTRIENTS),
				buildLines(regulated, languages, options, Block.MICRONUTRIENTS), buildLines(regulated, languages, options, Block.SUPPLEMENTAL),
				NutritionFactsLabelResolver.footNote(options.regulationKey(), panelLocale),
				NutritionFactsLabelResolver.notSignificantSource(options.regulationKey(), panelLocale), labels,
				buildTranslation(product, languages, options), buildLanguagePanels(product, languages, format, options));
	}

	/**
	 * The same panel written in each of its languages alone, for a bilingual panel whose format
	 * draws them apart only: the Canadian linear format states its two languages as two complete
	 * texts set side by side, and not nutrient by nutrient as the tables do, which need none.
	 */
	private List<NutritionFactsData> buildLanguagePanels(ProductData product, List<Locale> languages, String format, NutritionFactsOptions options) {
		if ((languages.size() < 2) || !NutritionFactsPanelFormats.statesEachLanguageApart(format)) {
			return List.of();
		}
		List<NutritionFactsData> panels = new ArrayList<>(languages.size());
		for (Locale language : languages) {
			panels.add(build(product, language, format, options.withSingleLanguage()));
		}
		return panels;
	}

	/**
	 * Wording of the panel in the second official language, which a bilingual table prints under
	 * the first one.
	 */
	private NutritionFactsTranslation buildTranslation(ProductData product, List<Locale> languages, NutritionFactsOptions options) {
		if (languages.size() < 2) {
			return NutritionFactsTranslation.none();
		}
		Locale secondary = languages.get(1);
		return new NutritionFactsTranslation(NutritionFactsLabelResolver.panelLabels(options.regulationKey(), secondary),
				secondaryServingSize(product, languages, options.figureSeparator()), NutritionFactsLabelResolver.footNote(options.regulationKey(), secondary));
	}

	/**
	 * Ingredients a line marked as supplemental is declared as. Only a supplemented food facts table
	 * declares any: every other format ignores the marking, a core nutrient keeping its line there.
	 */
	private PermittedSupplementalIngredients permittedSupplementalIngredients(String format, NutritionFactsOptions options) {
		return NutritionFactsPanelFormats.declaresSupplementalIngredients(format)
				? PermittedSupplementalIngredients.forRegulation(options.regulationKey())
				: PermittedSupplementalIngredients.none();
	}

	private RegulatedNutrients collectNutrients(ProductData product, Locale locale, NutritionFactsOptions options,
			PermittedSupplementalIngredients permitted) {

		CollectedNutrients collected = new CollectedNutrients(new ArrayList<>(), new Characteristics(new HashMap<>(), new HashMap<>()),
				new HashSet<>());

		if (product.getNutList() != null) {
			for (NutListDataItem nutListItem : product.getNutList()) {
				addNutrient(nutListItem, collected, locale, options, permitted);
			}
		}

		List<RegulatedNutrient> nutrients = collected.nutrients();
		nutrients.sort(Comparator.comparing(nutrient -> nutrient.displayRule().sort(), Comparator.nullsLast(Comparator.naturalOrder())));
		return new RegulatedNutrients(nutrients, collected.characteristics(), shareDailyValues(nutrients, options), permitted,
				collected.supplementalNutCodes());
	}

	/**
	 * Adds up the percentages of the nutrients a regulation groups on a single line. Percentages
	 * are only additive when both nutrients are measured against the same reference intake, so a
	 * pair that disagrees on it is left alone rather than silently mixed.
	 */
	private SharedDailyValues shareDailyValues(List<RegulatedNutrient> nutrients, NutritionFactsOptions options) {

		if (options.sharedDailyValues().isEmpty()) {
			return SharedDailyValues.none();
		}

		Map<String, Double> hostPercents = new HashMap<>();
		Set<String> foldedIn = new HashSet<>();

		for (SharedDailyValue shared : options.sharedDailyValues()) {
			RegulatedNutrient host = findByNutCode(nutrients, shared.hostNutCode());
			RegulatedNutrient guest = findByNutCode(nutrients, shared.guestNutCode());

			if (hasSameReferenceIntake(host, guest)) {
				hostPercents.put(host.nutCode(), sumPercents(host.gdaPerc(), guest.gdaPerc()));
				foldedIn.add(guest.nutCode());
			}
		}
		return new SharedDailyValues(hostPercents, foldedIn);
	}

	private boolean hasSameReferenceIntake(RegulatedNutrient host, RegulatedNutrient guest) {
		return (host != null) && (guest != null) && (host.displayRule().gda() != null)
				&& host.displayRule().gda().equals(guest.displayRule().gda());
	}

	private Double sumPercents(Double hostPercent, Double guestPercent) {
		if ((hostPercent == null) && (guestPercent == null)) {
			return null;
		}
		return (hostPercent != null ? hostPercent : 0d) + (guestPercent != null ? guestPercent : 0d);
	}

	private RegulatedNutrient findByNutCode(List<RegulatedNutrient> nutrients, String nutCode) {
		for (RegulatedNutrient nutrient : nutrients) {
			if (nutrient.nutCode().equals(nutCode)) {
				return nutrient;
			}
		}
		return null;
	}

	/**
	 * Locale the figures of a panel are formatted with. A regulated panel states its numbers the
	 * way the regulation does: an American label writes "0.5g" and never "0,5g", whatever the
	 * language the reader browses in. The wording keeps following the content locale.
	 */
	private Locale numberLocale(String regulationKey, Locale contentLocale) {
		if (NutritionFactsOptions.US_REGULATION_KEY.equals(regulationKey)) {
			return Locale.US;
		}
		if (NutritionFactsOptions.CA_REGULATION_KEY.equals(regulationKey)) {
			return Locale.CANADA;
		}
		return contentLocale;
	}

	private void addNutrient(NutListDataItem nutListItem, CollectedNutrients collected, Locale locale, NutritionFactsOptions options,
			PermittedSupplementalIngredients permitted) {

		if (nutListItem.getNut() == null) {
			return;
		}

		NutDataItem nut = (NutDataItem) alfrescoRepository.findOne(nutListItem.getNut());
		RegulatedNutrient regulated = RegulationFormulationHelper.extractRegulatedNutrient(nutListItem, nut.getNutCode(),
				numberLocale(options.regulationKey(), locale), options.regulationKey());
		boolean isSupplemental = permitted.isPermitted(regulated.nutCode()) && isSupplementalIngredient(nutListItem);

		if (isDeclared(regulated, options) || (isSupplemental && regulated.displayRule().isDefined())) {
			collected.nutrients().add(regulated);
			collected.characteristics().names().put(regulated.nutCode(), charactName(nut, locale));
			collected.characteristics().units().put(regulated.nutCode(), charactUnit(nut));
			if (isSupplemental) {
				collected.supplementalNutCodes().add(regulated.nutCode());
			}
		}
	}

	/**
	 * Tells whether the formulator marked that line as an ingredient added to the product, which a
	 * supplemented food declares in a block of its own. The marking is a report kind carried by the
	 * line, so that it is done in the nutrition list rather than in a screen of its own. It only
	 * counts for an ingredient the regulation permits, which the caller checks.
	 */
	private boolean isSupplementalIngredient(NutListDataItem nutListItem) {
		if ((nutListItem.getNodeRef() == null) || !mlNodeService.exists(nutListItem.getNodeRef())) {
			return false;
		}
		return SupplementalIngredientMarking.isMarked(mlNodeService.getProperty(nutListItem.getNodeRef(), ReportModel.PROP_REPORT_KINDS));
	}

	/**
	 * Name of the characteristic, used when a regulation does not name a nutrient itself. The
	 * multilingual name is what a user reads; the node name is a fallback and is often a raw
	 * identifier, which must never be printed on a label.
	 */
	private String charactName(NutDataItem nut, Locale locale) {
		String charactName = MLTextHelper.getClosestValue(nut.getCharactName(), locale);
		return ((charactName != null) && !charactName.isBlank()) ? charactName : nut.getName();
	}

	/**
	 * Unit of the characteristic, which a panel falls back on when the regulation states none. It is
	 * held in the referential as the unit of the nutrition list, "mg/100g", and only the measure is
	 * kept: the amount printed on a panel is that of a serving.
	 */
	private String charactUnit(NutDataItem nut) {
		String nutUnit = nut.getNutUnit();
		if ((nutUnit == null) || nutUnit.isBlank()) {
			return null;
		}
		int perIndex = nutUnit.indexOf(UNIT_SEPARATOR);
		return perIndex >= 0 ? nutUnit.substring(0, perIndex) : nutUnit;
	}

	/**
	 * A mandatory nutrient stays on the panel even at zero, the regulation requiring the line to be
	 * printed. An optional one only earns its line when it carries something to say.
	 */
	private boolean isDeclared(RegulatedNutrient regulated, NutritionFactsOptions options) {
		if (!regulated.displayRule().isDefined()) {
			return false;
		}
		if (regulated.isMandatory()) {
			return true;
		}
		// The panel only declares what the regulation requires: a merely authorised nutrient shows
		// up on explicit request, otherwise it clutters the label.
		return regulated.displayRule().optional() && options.showOptional();
	}

	private boolean hasValue(RegulatedNutrient regulated) {
		return (regulated.value() != null) && (Math.abs(regulated.value()) > ZERO_THRESHOLD);
	}

	private NutritionFactsLine buildCalories(RegulatedNutrients regulated, List<Locale> languages, NutritionFactsOptions options) {
		for (RegulatedNutrient nutrient : regulated.nutrients()) {
			if (isSort(nutrient, NutritionFactsOptions.CALORIES_SORT)) {
				return toLine(nutrient, regulated, languages, options);
			}
		}
		return null;
	}

	private List<NutritionFactsLine> buildLines(RegulatedNutrients regulated, List<Locale> languages, NutritionFactsOptions options, Block block) {

		List<NutritionFactsLine> lines = new ArrayList<>();
		for (RegulatedNutrient nutrient : nutrientsOf(regulated, options, block)) {
			lines.add(toLine(nutrient, regulated, languages, options));
		}
		return lines;
	}

	/**
	 * Nutrients drawn in a block, calories left out. The supplemental ingredients are declared in the
	 * order of the list that permits them, the other blocks in the order of the regulation.
	 */
	private List<RegulatedNutrient> nutrientsOf(RegulatedNutrients regulated, NutritionFactsOptions options, Block block) {
		List<RegulatedNutrient> nutrients = new ArrayList<>();
		for (RegulatedNutrient nutrient : regulated.nutrients()) {
			if (!isSort(nutrient, NutritionFactsOptions.CALORIES_SORT) && (blockOf(nutrient, regulated, options) == block)) {
				nutrients.add(nutrient);
			}
		}
		if (block == Block.SUPPLEMENTAL) {
			nutrients.sort(Comparator.comparing(RegulatedNutrient::nutCode, regulated.permitted().listOrder()));
		}
		return nutrients;
	}

	/**
	 * Block a nutrient is drawn in. A nutrient marked as added is declared as a supplemental
	 * ingredient only, even a core nutrient such as calcium or potassium: the guidelines on
	 * supplemented foods (4.3.1.1 b) forbid stating its amount or its daily value in the table too.
	 */
	private Block blockOf(RegulatedNutrient nutrient, RegulatedNutrients regulated, NutritionFactsOptions options) {
		if (regulated.supplementalNutCodes().contains(nutrient.nutCode())) {
			return Block.SUPPLEMENTAL;
		}
		return isMicronutrient(nutrient, options) ? Block.MICRONUTRIENTS : Block.NUTRIENTS;
	}

	private boolean isMicronutrient(RegulatedNutrient nutrient, NutritionFactsOptions options) {
		Integer sort = nutrient.displayRule().sort();
		return (sort != null) && (sort >= options.micronutrientStartSort());
	}

	private boolean isSort(RegulatedNutrient nutrient, int sort) {
		return (nutrient.displayRule().sort() != null) && (nutrient.displayRule().sort() == sort);
	}

	private NutritionFactsLine toLine(RegulatedNutrient regulated, RegulatedNutrients regulatedNutrients, List<Locale> languages,
			NutritionFactsOptions options) {

		String unit = carriesUnit(regulated) ? unitOf(regulated, regulatedNutrients) : null;
		String separator = options.figureSeparator();
		String value = withUnit(regulated.displayValuePerServing(), unit, separator);

		Locale panelLocale = languages.get(0);
		Wordings wordings = Wordings.of(nutrientLabel(regulated, regulatedNutrients, languages, options),
				nutrientAbbreviation(regulated, regulatedNutrients, languages, options), value, panelLocale);

		SharedDailyValues shared = regulatedNutrients.sharedDailyValues();

		return new NutritionFactsLine(regulated.nutCode(), wordings.label(), wordings.abbreviation(), wordings.plainLabel(),
				wordings.plainAbbreviation(), value, withUnit(regulated.displayValuePerContainer(), unit, separator),
				toPercent(shared.percentOf(regulated), separator), toPercent(regulated.gdaPercPerContainer(), separator),
				regulated.displayRule().indentLevel(), regulated.displayRule().bold(),
				regulated.showsDailyValue() && !shared.isFoldedIn(regulated.nutCode()), wordings.valueInLabel(),
				shared.isShared(regulated.nutCode()), regulatedNutrients.supplementalNutCodes().contains(regulated.nutCode()));
	}

	/**
	 * Regulated wording of a nutrient in every language the panel is written in, joined into the
	 * single wording its line carries, "Fat / Lipides".
	 */
	private String nutrientLabel(RegulatedNutrient regulated, RegulatedNutrients nutrients, List<Locale> languages,
			NutritionFactsOptions options) {

		List<String> wordings = new ArrayList<>();
		for (Locale language : languages) {
			wordings.add(regulatedWording(regulated, nutrients, language, options));
		}
		return joinLanguages(wordings);
	}

	/** Same, for the shortened wording a linear panel names its nutrients by. */
	private String nutrientAbbreviation(RegulatedNutrient regulated, RegulatedNutrients nutrients, List<Locale> languages,
			NutritionFactsOptions options) {

		List<String> wordings = new ArrayList<>();
		for (Locale language : languages) {
			wordings.add(NutritionFactsLabelResolver.nutrientAbbreviation(options.regulationKey(), regulated.nutCode(),
					regulatedWording(regulated, nutrients, language, options), language));
		}
		return joinLanguages(wordings);
	}

	private String regulatedWording(RegulatedNutrient regulated, RegulatedNutrients nutrients, Locale language,
			NutritionFactsOptions options) {
		return NutritionFactsLabelResolver.nutrientLabel(options.regulationKey(), regulated.nutCode(),
				nutrients.characteristics().names().get(regulated.nutCode()), language);
	}

	/**
	 * Wordings of the languages of the panel gathered into one. A wording that reads the same in
	 * both languages is printed once, the way the regulation prints "Sodium", and the marker of a
	 * nutrient folded into the line above is carried by the first language only.
	 */
	private String joinLanguages(List<String> wordings) {
		List<String> distinct = new ArrayList<>();
		for (String wording : wordings) {
			String candidate = distinct.isEmpty() ? wording : withoutContinuationMarker(wording, distinct.get(0));
			if ((candidate != null) && !candidate.isBlank() && !distinct.contains(candidate)) {
				distinct.add(candidate);
			}
		}
		return String.join(LANGUAGE_SEPARATOR, distinct);
	}

	private String withoutContinuationMarker(String wording, String firstWording) {
		if ((wording == null) || !wording.startsWith(CONTINUATION_MARKER) || !firstWording.startsWith(CONTINUATION_MARKER)) {
			return wording;
		}
		return wording.substring(CONTINUATION_MARKER.length());
	}

	/**
	 * The energy line is printed as a bare figure: a panel writes "Calories 230", never
	 * "230kcal", and the unit would collide with the label at that type size.
	 */
	private boolean carriesUnit(RegulatedNutrient regulated) {
		Integer sort = regulated.displayRule().sort();
		return (sort == null) || (sort != NutritionFactsOptions.CALORIES_SORT);
	}

	/**
	 * Unit the amount of a nutrient is stated in: the one the regulation sets for it, and the unit
	 * of the characteristic when the regulation merely authorises the nutrient without stating one,
	 * as it does for caffeine. An amount printed with no unit states nothing.
	 */
	private String unitOf(RegulatedNutrient regulated, RegulatedNutrients nutrients) {
		String regulatedUnit = regulated.displayRule().unit();
		if ((regulatedUnit != null) && !regulatedUnit.isBlank()) {
			return regulatedUnit;
		}
		return nutrients.characteristics().units().get(regulated.nutCode());
	}

	/**
	 * The regulation formats the figure, never its unit, which the report design used to append
	 * itself. A panel needs the two as a single token, "8g" for the FDA and "8 g" for Canada, the
	 * separator being a no-break space so that the token is never split.
	 */
	private String withUnit(String displayValue, String unit, String separator) {
		if ((displayValue == null) || (unit == null) || unit.isBlank()) {
			return displayValue;
		}
		return displayValue + separator + unit;
	}

	private String toPercent(Double gdaPerc, String separator) {
		return gdaPerc != null ? Math.round(gdaPerc) + separator + PERCENT_SIGN : null;
	}

	private NutritionFactsServing buildServing(ProductData product, List<Locale> languages, Map<String, String> labels,
			NutritionFactsOptions options) {
		Locale panelLocale = languages.get(0);
		return new NutritionFactsServing(servingsPerContainer(product, panelLocale, labels),
				servingSize(product, panelLocale, options.figureSeparator()));
	}

	/**
	 * The count of servings only earns its line where the regulation words it: a Canadian panel
	 * opens on the serving alone, and a bare figure with nothing to name it states nothing.
	 */
	private String servingsPerContainer(ProductData product, Locale locale, Map<String, String> labels) {
		if (labels.getOrDefault(NutritionFactsLabelResolver.LABEL_SERVINGS_PER_CONTAINER, "").isBlank()) {
			return null;
		}
		return closestValue(product, PLMModel.PROP_PRODUCT_NUMBER_OF_SERVINGS, locale);
	}

	/** Serving wording of the second language, left out when it reads the same as the first. */
	private String secondaryServingSize(ProductData product, List<Locale> languages, String separator) {
		String panelServingSize = servingSize(product, languages.get(0), separator);
		String secondary = servingSize(product, languages.get(1), separator);
		return Objects.equals(panelServingSize, secondary) ? null : secondary;
	}

	/**
	 * Wording of the serving, preferring what the user wrote ("2/3 cup"), then the country specific
	 * wording, and falling back on the serving size itself so that the line is never printed empty
	 * on a product that only carries a quantity.
	 */
	private String servingSize(ProductData product, Locale locale, String separator) {
		String servingSizeText = closestValue(product, PLMModel.PROP_PRODUCT_SERVING_SIZE_TEXT, locale);
		if ((servingSizeText != null) && !servingSizeText.isBlank()) {
			return withWeight(servingSizeText, product, separator);
		}

		String byCountry = MLTextHelper.getClosestValue(product.getServingSizeByCountry(), locale);
		if ((byCountry != null) && !byCountry.isBlank()) {
			return withWeight(byCountry, product, separator);
		}

		return formatServingSize(product, separator);
	}

	/**
	 * The regulation states the household measure and its metric weight together, "2/3 cup (55g)",
	 * so the weight is appended to a wording that does not carry it already.
	 */
	private String withWeight(String wording, ProductData product, String separator) {
		String weight = formatServingSize(product, separator);
		if ((weight == null) || wording.contains(weight) || wording.contains("(")) {
			return wording;
		}
		return wording + " (" + weight + ")";
	}

	private String formatServingSize(ProductData product, String separator) {
		if (product.getServingSize() == null) {
			return null;
		}
		String figure = new DecimalFormat(SERVING_SIZE_PATTERN, DecimalFormatSymbols.getInstance(Locale.US)).format(product.getServingSize());
		return product.getServingSizeUnit() != null ? figure + separator + product.getServingSizeUnit() : figure;
	}

	private String closestValue(ProductData product, QName property, Locale locale) {
		if (product.getNodeRef() == null) {
			return null;
		}
		MLText value = (MLText) mlNodeService.getProperty(product.getNodeRef(), property);
		return value != null ? MLTextHelper.getClosestValue(value, locale) : null;
	}

	/**
	 * Percentages of the daily value a regulation asks to be shown on one line for several
	 * nutrients, the Canadian saturated fat line carrying saturated and trans fat together.
	 */
	private record SharedDailyValues(Map<String, Double> hostPercents, Set<String> foldedIn) {

		static SharedDailyValues none() {
			return new SharedDailyValues(Map.of(), Set.of());
		}

		/** Percentage to print for a nutrient, combined when it hosts another one. */
		Double percentOf(RegulatedNutrient nutrient) {
			return hostPercents.getOrDefault(nutrient.nutCode(), nutrient.gdaPerc());
		}

		/** Tells whether the percentage of a nutrient is already carried by another line. */
		boolean isFoldedIn(String nutCode) {
			return foldedIn.contains(nutCode);
		}

		/** Tells whether the percentage of a nutrient also accounts for the one folded into it. */
		boolean isShared(String nutCode) {
			return hostPercents.containsKey(nutCode);
		}
	}

	/**
	 * The four wordings a line carries: the regulated sentence and its abbreviation, each in the
	 * form that embeds the value ("Includes 10g Added Sugars") and in the form that does not, which
	 * is what the formats printing the figures in their own columns need.
	 */
	private record Wordings(String label, String abbreviation, String plainLabel, String plainAbbreviation, boolean valueInLabel) {

		static Wordings of(String rawLabel, String rawAbbreviation, String value, Locale locale) {
			return new Wordings(NutritionFactsLabelResolver.withValue(rawLabel, value, locale),
					NutritionFactsLabelResolver.withValue(rawAbbreviation, value, locale),
					NutritionFactsLabelResolver.withoutValue(rawLabel, locale),
					NutritionFactsLabelResolver.withoutValue(rawAbbreviation, locale), NutritionFactsLabelResolver.embedsValue(rawLabel));
		}
	}

	/** Regulated nutrients of a product, with what the referential says of the characteristics themselves. */
	private record RegulatedNutrients(List<RegulatedNutrient> nutrients, Characteristics characteristics, SharedDailyValues sharedDailyValues,
			PermittedSupplementalIngredients permitted, Set<String> supplementalNutCodes) {
	}

	/** What the reading of the nutrition list gathers, line after line, before the nutrients are ordered. */
	private record CollectedNutrients(List<RegulatedNutrient> nutrients, Characteristics characteristics, Set<String> supplementalNutCodes) {
	}

	/**
	 * What the referential holds of a characteristic, by nutrient code: its name and its unit, both
	 * used only where the regulation states none of its own.
	 */
	private record Characteristics(Map<String, String> names, Map<String, String> units) {
	}

	/** The three blocks a panel draws its nutrients in. */
	private enum Block {
		NUTRIENTS, MICRONUTRIENTS, SUPPLEMENTAL
	}

}
