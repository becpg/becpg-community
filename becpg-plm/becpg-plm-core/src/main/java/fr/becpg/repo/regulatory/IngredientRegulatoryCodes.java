/*******************************************************************************
 *  Copyright (C) 2010-2026 beCPG.
 *
 *  This file is part of beCPG
 *
 *  beCPG is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  beCPG is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.regulatory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The tokens of an ingredient {@code bcpg:regulatoryCode}, the single place that knows how the value is
 * written and read.
 * <p>
 * The property is shared by the Decernis and the beCPG regulatory services, each owning one kind of
 * token: {@code DECERNIS_<id>} (or the {@code unknown} marker once Decernis has been asked and has no
 * id) for the former, {@code BECPG_<code>} for the latter. A bare numeric token is a Decernis id written
 * before the prefix existed. Any other token is kept as it is. Tokens are comma separated, trimmed,
 * deduplicated and keep their order, so that the stored value is stable between two runs.
 * <p>
 * Instances are immutable.
 *
 * @author matthieu
 */
public final class IngredientRegulatoryCodes {

	/** Marker stored once Decernis has been asked for an ingredient id and has none, so it is not asked again. */
	public static final String UNKNOWN_DECERNIS_ID = "unknown";

	private static final String SEPARATOR = ",";

	private final List<String> tokens;

	private IngredientRegulatoryCodes(List<String> tokens) {
		this.tokens = List.copyOf(tokens);
	}

	/**
	 * Parses a raw {@code bcpg:regulatoryCode} value.
	 *
	 * @param rawCode the stored value, possibly null or blank
	 * @return the parsed tokens, empty when there is nothing to parse
	 */
	public static IngredientRegulatoryCodes parse(String rawCode) {
		Set<String> parsed = new LinkedHashSet<>();
		if (rawCode != null) {
			for (String token : rawCode.split(SEPARATOR)) {
				String trimmed = token.trim();
				if (!trimmed.isEmpty()) {
					parsed.add(trimmed);
				}
			}
		}
		return new IngredientRegulatoryCodes(new ArrayList<>(parsed));
	}

	/**
	 * The Decernis ingredient id, taken from the first {@code DECERNIS_} token, or from the first bare
	 * numeric token written before the prefix existed.
	 *
	 * @return the id without its prefix, empty when the ingredient carries none
	 */
	public Optional<String> decernisId() {
		for (String token : tokens) {
			if (isPrefixedDecernisId(token)) {
				return Optional.of(token.substring(RegulatoryHelper.DECERNIS_PREFIX.length()));
			}
		}
		return tokens.stream().filter(IngredientRegulatoryCodes::isLegacyDecernisId).findFirst();
	}

	/**
	 * Tells whether Decernis has already been asked for an id and has none.
	 *
	 * @return true when the {@code unknown} marker is present
	 */
	public boolean isDecernisIdUnknown() {
		return tokens.contains(UNKNOWN_DECERNIS_ID);
	}

	/**
	 * The codes owned by the beCPG regulatory service.
	 *
	 * @return the {@code BECPG_} tokens, in their stored order
	 */
	public List<String> becpgCodes() {
		return tokens.stream().filter(IngredientRegulatoryCodes::isBecpgCode).toList();
	}

	/**
	 * All the tokens, in their stored order.
	 *
	 * @return an unmodifiable list
	 */
	public List<String> tokens() {
		return tokens;
	}

	/**
	 * Replaces the Decernis id, whatever its former form, by the given one.
	 *
	 * @param decernisId the id returned by Decernis, without prefix
	 * @return the new tokens
	 */
	public IngredientRegulatoryCodes withDecernisId(String decernisId) {
		return replace(IngredientRegulatoryCodes::isDecernisToken, List.of(RegulatoryHelper.DECERNIS_PREFIX + decernisId.trim()));
	}

	/**
	 * Records that Decernis has been asked for an id and has none.
	 *
	 * @return the new tokens
	 */
	public IngredientRegulatoryCodes withUnknownDecernisId() {
		return replace(IngredientRegulatoryCodes::isDecernisToken, List.of(UNKNOWN_DECERNIS_ID));
	}

	/**
	 * Replaces the codes owned by the beCPG regulatory service by the given ones, the other tokens are
	 * kept untouched.
	 *
	 * @param becpgCodes the codes returned by the beCPG regulatory service
	 * @return the new tokens
	 */
	public IngredientRegulatoryCodes withBecpgCodes(Collection<String> becpgCodes) {
		return replace(IngredientRegulatoryCodes::isBecpgCode, becpgCodes);
	}

	/**
	 * Formats the tokens back into a {@code bcpg:regulatoryCode} value.
	 *
	 * @return the comma separated tokens, an empty string when there is none
	 */
	public String format() {
		return String.join(SEPARATOR, tokens);
	}

	private IngredientRegulatoryCodes replace(Predicate<String> owned, Collection<String> replacement) {
		Set<String> updated = new LinkedHashSet<>();
		for (String token : tokens) {
			if (!owned.test(token)) {
				updated.add(token);
			}
		}
		for (String token : replacement) {
			String trimmed = token == null ? "" : token.trim();
			if (!trimmed.isEmpty()) {
				updated.add(trimmed);
			}
		}
		return new IngredientRegulatoryCodes(new ArrayList<>(updated));
	}

	private static boolean isDecernisToken(String token) {
		return token.startsWith(RegulatoryHelper.DECERNIS_PREFIX) || isLegacyDecernisId(token) || UNKNOWN_DECERNIS_ID.equals(token);
	}

	private static boolean isPrefixedDecernisId(String token) {
		return token.startsWith(RegulatoryHelper.DECERNIS_PREFIX) && isNumeric(token.substring(RegulatoryHelper.DECERNIS_PREFIX.length()));
	}

	private static boolean isLegacyDecernisId(String token) {
		return isNumeric(token);
	}

	private static boolean isBecpgCode(String token) {
		return token.startsWith(RegulatoryHelper.BECPG_PREFIX);
	}

	private static boolean isNumeric(String value) {
		return !value.isEmpty() && value.chars().allMatch(Character::isDigit);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object other) {
		return other instanceof IngredientRegulatoryCodes codes && tokens.equals(codes.tokens);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(tokens);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return format();
	}
}
