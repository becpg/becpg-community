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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.nutrient.RegulatedNutrient;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;
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

		RegulatedNutrients regulated = collectNutrients(product, locale, options);

		return new NutritionFactsData(format, options.regulationKey(), buildServing(product, locale),
				buildCalories(regulated, locale, options), buildLines(regulated, locale, options, false),
				buildLines(regulated, locale, options, true), NutritionFactsLabelResolver.footNote(options.regulationKey(), locale),
				NutritionFactsLabelResolver.notSignificantSource(options.regulationKey(), locale),
				NutritionFactsLabelResolver.panelLabels(options.regulationKey(), locale));
	}

	private RegulatedNutrients collectNutrients(ProductData product, Locale locale, NutritionFactsOptions options) {

		List<RegulatedNutrient> nutrients = new ArrayList<>();
		Map<String, String> charactNames = new HashMap<>();

		if (product.getNutList() != null) {
			for (NutListDataItem nutListItem : product.getNutList()) {
				addNutrient(nutListItem, nutrients, charactNames, locale, options);
			}
		}

		nutrients.sort(Comparator.comparing(nutrient -> nutrient.displayRule().sort(), Comparator.nullsLast(Comparator.naturalOrder())));
		return new RegulatedNutrients(nutrients, charactNames, shareDailyValues(nutrients, options));
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

	private void addNutrient(NutListDataItem nutListItem, List<RegulatedNutrient> nutrients, Map<String, String> charactNames, Locale locale,
			NutritionFactsOptions options) {

		if (nutListItem.getNut() == null) {
			return;
		}

		NutDataItem nut = (NutDataItem) alfrescoRepository.findOne(nutListItem.getNut());
		RegulatedNutrient regulated = RegulationFormulationHelper.extractRegulatedNutrient(nutListItem, nut.getNutCode(), locale,
				options.regulationKey());

		if (isDeclared(regulated, options)) {
			nutrients.add(regulated);
			charactNames.put(regulated.nutCode(), nut.getName());
		}
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
		return regulated.displayRule().optional() && (options.showOptional() || hasValue(regulated));
	}

	private boolean hasValue(RegulatedNutrient regulated) {
		return (regulated.value() != null) && (Math.abs(regulated.value()) > ZERO_THRESHOLD);
	}

	private NutritionFactsLine buildCalories(RegulatedNutrients regulated, Locale locale, NutritionFactsOptions options) {
		for (RegulatedNutrient nutrient : regulated.nutrients()) {
			if (isSort(nutrient, NutritionFactsOptions.CALORIES_SORT)) {
				return toLine(nutrient, regulated, locale, options);
			}
		}
		return null;
	}

	private List<NutritionFactsLine> buildLines(RegulatedNutrients regulated, Locale locale, NutritionFactsOptions options,
			boolean micronutrients) {

		List<NutritionFactsLine> lines = new ArrayList<>();
		for (RegulatedNutrient nutrient : regulated.nutrients()) {
			if (!isSort(nutrient, NutritionFactsOptions.CALORIES_SORT) && (isMicronutrient(nutrient, options) == micronutrients)) {
				lines.add(toLine(nutrient, regulated, locale, options));
			}
		}
		return lines;
	}

	private boolean isMicronutrient(RegulatedNutrient nutrient, NutritionFactsOptions options) {
		Integer sort = nutrient.displayRule().sort();
		return (sort != null) && (sort >= options.micronutrientStartSort());
	}

	private boolean isSort(RegulatedNutrient nutrient, int sort) {
		return (nutrient.displayRule().sort() != null) && (nutrient.displayRule().sort() == sort);
	}

	private NutritionFactsLine toLine(RegulatedNutrient regulated, RegulatedNutrients regulatedNutrients, Locale locale,
			NutritionFactsOptions options) {

		String unit = regulated.displayRule().unit();
		String unitValue = withUnit(regulated.displayValuePerServing(), unit);
		String value = unitValue;

		String label = NutritionFactsLabelResolver.nutrientLabel(options.regulationKey(), regulated.nutCode(),
				regulatedNutrients.charactNames().get(regulated.nutCode()), locale);

		if (NutritionFactsLabelResolver.embedsValue(label)) {
			label = NutritionFactsLabelResolver.formatWithValue(label, value, locale);
			value = null;
		}

		SharedDailyValues shared = regulatedNutrients.sharedDailyValues();

		String abbreviation = NutritionFactsLabelResolver.nutrientAbbreviation(options.regulationKey(), regulated.nutCode(), label, locale);
		if (NutritionFactsLabelResolver.embedsValue(abbreviation)) {
			abbreviation = NutritionFactsLabelResolver.formatWithValue(abbreviation, unitValue, locale);
		}

		return new NutritionFactsLine(regulated.nutCode(), label, abbreviation, value, withUnit(regulated.displayValuePerContainer(), unit),
				toPercent(shared.percentOf(regulated)), toPercent(regulated.gdaPercPerContainer()), regulated.displayRule().indentLevel(),
				regulated.displayRule().bold(), regulated.showsDailyValue() && !shared.isFoldedIn(regulated.nutCode()));
	}

	/**
	 * The regulation formats the figure, never its unit, which the report design used to append
	 * itself. A panel needs the two as a single token, "8g" and not "8" next to "g".
	 */
	private String withUnit(String displayValue, String unit) {
		if ((displayValue == null) || (unit == null) || unit.isBlank()) {
			return displayValue;
		}
		return displayValue + unit;
	}

	private String toPercent(Double gdaPerc) {
		return gdaPerc != null ? Math.round(gdaPerc) + "%" : null;
	}

	private NutritionFactsServing buildServing(ProductData product, Locale locale) {
		return new NutritionFactsServing(closestValue(product, PLMModel.PROP_PRODUCT_NUMBER_OF_SERVINGS, locale), servingSize(product, locale));
	}

	private String servingSize(ProductData product, Locale locale) {
		String servingSizeText = closestValue(product, PLMModel.PROP_PRODUCT_SERVING_SIZE_TEXT, locale);
		if ((servingSizeText != null) && !servingSizeText.isBlank()) {
			return servingSizeText;
		}
		return MLTextHelper.getClosestValue(product.getServingSizeByCountry(), locale);
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
	}

	/** Regulated nutrients of a product, with the characteristic names used when a regulation names nothing. */
	private record RegulatedNutrients(List<RegulatedNutrient> nutrients, Map<String, String> charactNames, SharedDailyValues sharedDailyValues) {
	}

}
