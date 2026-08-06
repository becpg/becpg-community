/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.ecobalyse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.helper.BeCPGQueryHelper;

/**
 * Reads the Ecobalyse ingredient reference shipped with beCPG.
 *
 * <p>The reference is the open data published by the Ecobalyse project. It carries the
 * ecosystemic services, which are the non-LCA complements of the environmental cost, and
 * the land occupation of every ingredient. The impacts themselves are not in this file:
 * they come from the LCA database the customer imports.</p>
 *
 * @author matthieu
 */
@Service("ecobalyseIngredientService")
public class EcobalyseIngredientService {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(EcobalyseIngredientService.class);

	/** Constant <code>INGREDIENTS_PATH="beCPG/databases/ecobalyse/ecobalyse_ing"{trunked}</code> */
	private static final String INGREDIENTS_PATH = "beCPG/databases/ecobalyse/ecobalyse_ingredients_3_1.csv";

	/** Constant <code>CSV_DELIMITER=';'</code> */
	private static final char CSV_DELIMITER = ';';

	/** Constant <code>CSV_QUOTE='&quot;'</code> */
	private static final char CSV_QUOTE = '"';

	/** Constant <code>SKIP_HEADER=1</code> */
	private static final int SKIP_HEADER = 1;

	/** Constant <code>SUGGESTION_LIMIT=100</code> */
	private static final int SUGGESTION_LIMIT = 100;

	private Map<String, EcobalyseIngredient> ingredients;

	/**
	 * <p>Finds an ingredient by its Ecobalyse alias or identifier.</p>
	 *
	 * @param code the alias or the identifier held by {@code bcpg:ecobalyseCode}
	 * @return a {@link java.util.Optional} object
	 */
	public Optional<EcobalyseIngredient> findByCode(String code) {
		if ((code == null) || code.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(getIngredients().get(code));
	}

	/**
	 * <p>Returns every ingredient of the reference, indexed by alias and by identifier.</p>
	 *
	 * @return a {@link java.util.Map} object, never null
	 */
	public Map<String, EcobalyseIngredient> getIngredients() {
		if (ingredients == null) {
			loadIngredients();
		}
		return Collections.unmodifiableMap(ingredients);
	}

	/**
	 * <p>Suggests the ingredients matching a query, for the autocomplete of the code field.</p>
	 *
	 * @param query the typed query
	 * @param pageNum a {@link java.lang.Integer} object
	 * @param pageSize a {@link java.lang.Integer} object
	 * @return a {@link fr.becpg.repo.autocomplete.AutoCompletePage} object
	 */
	public AutoCompletePage suggest(String query, Integer pageNum, Integer pageSize) {
		List<EcobalyseIngredient> matches = new ArrayList<>();

		for (EcobalyseIngredient ingredient : distinctIngredients()) {
			if (BeCPGQueryHelper.isQueryMatch(query, ingredient.getName()) && (matches.size() < SUGGESTION_LIMIT)) {
				matches.add(ingredient);
			}
		}

		matches.sort((left, right) -> left.getName().compareTo(right.getName()));

		return new AutoCompletePage(matches, pageNum, pageSize, values -> {
			List<AutoCompleteEntry> suggestions = new ArrayList<>();
			if (values != null) {
				for (EcobalyseIngredient value : values) {
					suggestions.add(new AutoCompleteEntry(value.getAlias(), value.toString(), "category"));
				}
			}
			return suggestions;
		});
	}

	/**
	 * The reference is indexed twice, by alias and by identifier, so the suggestions have to
	 * be de-duplicated.
	 *
	 * @return a {@link java.util.List} object
	 */
	private List<EcobalyseIngredient> distinctIngredients() {
		List<EcobalyseIngredient> distinct = new ArrayList<>();
		for (Map.Entry<String, EcobalyseIngredient> entry : getIngredients().entrySet()) {
			if (entry.getKey().equals(entry.getValue().getAlias())) {
				distinct.add(entry.getValue());
			}
		}
		return distinct;
	}

	/**
	 * <p>loadIngredients.</p>
	 */
	private void loadIngredients() {
		ingredients = new LinkedHashMap<>();

		try (InputStream in = new ClassPathResource(INGREDIENTS_PATH).getInputStream();
				InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
				CSVReader csvReader = new CSVReader(reader, CSV_DELIMITER, CSV_QUOTE, SKIP_HEADER)) {

			String[] line = null;
			while ((line = csvReader.readNext()) != null) {
				indexIngredient(parseLine(line));
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + ingredients.size() + " Ecobalyse ingredient keys");
			}
		} catch (IOException e) {
			logger.error("Failed to load the Ecobalyse ingredients from " + INGREDIENTS_PATH, e);
			ingredients = new LinkedHashMap<>();
		}
	}

	/**
	 * <p>indexIngredient.</p>
	 *
	 * @param ingredient a {@link fr.becpg.repo.product.formulation.ecobalyse.EcobalyseIngredient} object
	 */
	private void indexIngredient(EcobalyseIngredient ingredient) {
		if ((ingredient.getAlias() != null) && !ingredient.getAlias().isBlank()) {
			ingredients.put(ingredient.getAlias(), ingredient);
		}
		if ((ingredient.getId() != null) && !ingredient.getId().isBlank()) {
			ingredients.put(ingredient.getId(), ingredient);
		}
	}

	/**
	 * <p>parseLine.</p>
	 *
	 * @param line the CSV line
	 * @return a {@link fr.becpg.repo.product.formulation.ecobalyse.EcobalyseIngredient} object
	 */
	private EcobalyseIngredient parseLine(String[] line) {
		return EcobalyseIngredient.builder().withIdentity(column(line, 0), column(line, 1), column(line, 2))
				.withOrigin(column(line, 3), column(line, 4), column(line, 5)).withLandOccupation(parseDouble(column(line, 6)))
				.withEcosystemicServices(parseDouble(column(line, 7)), parseDouble(column(line, 8)), parseDouble(column(line, 9)),
						parseDouble(column(line, 10)))
				.build();
	}

	/**
	 * <p>column.</p>
	 *
	 * @param line the CSV line
	 * @param index the column index
	 * @return a {@link java.lang.String} object
	 */
	private String column(String[] line, int index) {
		return index < line.length ? line[index] : "";
	}

	/**
	 * <p>parseDouble.</p>
	 *
	 * @param value a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double parseDouble(String value) {
		if ((value == null) || value.trim().isEmpty()) {
			return null;
		}
		return Double.valueOf(value.trim().replace(",", "."));
	}

}
