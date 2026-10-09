package fr.becpg.test.repo.regulatory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import fr.becpg.repo.regulatory.IngredientRegulatoryCodes;

public class IngredientRegulatoryCodesTest {

	@Test
	public void parse_trimsDeduplicatesAndKeepsOrder() {
		IngredientRegulatoryCodes codes = IngredientRegulatoryCodes.parse(" BECPG_123 ,, DECERNIS_42 ,BECPG_123, ");

		assertEquals(List.of("BECPG_123", "DECERNIS_42"), codes.tokens());
		assertEquals("BECPG_123,DECERNIS_42", codes.format());
	}

	@Test
	public void parse_acceptsNullAndBlank() {
		assertEquals(List.of(), IngredientRegulatoryCodes.parse(null).tokens());
		assertEquals("", IngredientRegulatoryCodes.parse(" , ").format());
	}

	@Test
	public void decernisId_readsPrefixedThenLegacyNumericToken() {
		assertEquals(Optional.of("42"), IngredientRegulatoryCodes.parse("BECPG_123,DECERNIS_42").decernisId());
		assertEquals(Optional.of("42"), IngredientRegulatoryCodes.parse("42,BECPG_123").decernisId());
		assertEquals(Optional.of("42"), IngredientRegulatoryCodes.parse("7,DECERNIS_42").decernisId());
		assertEquals(Optional.empty(), IngredientRegulatoryCodes.parse("DECERNIS_,BECPG_123,unknown").decernisId());
	}

	@Test
	public void withUnknownDecernisId_isSticky() {
		IngredientRegulatoryCodes codes = IngredientRegulatoryCodes.parse("BECPG_123").withUnknownDecernisId();

		assertTrue(codes.isDecernisIdUnknown());
		assertEquals("BECPG_123,unknown", codes.withUnknownDecernisId().format());
	}

	@Test
	public void withDecernisId_replacesUnknownAndLegacyTokens() {
		IngredientRegulatoryCodes codes = IngredientRegulatoryCodes.parse("unknown,BECPG_123,7").withDecernisId(" 42 ");

		assertFalse(codes.isDecernisIdUnknown());
		assertEquals("BECPG_123,DECERNIS_42", codes.format());
	}

	@Test
	public void withBecpgCodes_replacesOnlyBecpgTokens() {
		IngredientRegulatoryCodes present = IngredientRegulatoryCodes.parse("BECPG_1,DECERNIS_42,unknown_vendor");
		IngredientRegulatoryCodes updated = present.withBecpgCodes(List.of("BECPG_2", "BECPG_3"));

		assertEquals("DECERNIS_42,unknown_vendor,BECPG_2,BECPG_3", updated.format());
		assertEquals(List.of("BECPG_2", "BECPG_3"), updated.becpgCodes());
	}

	@Test
	public void withBecpgCodes_isStableWhenNothingChanges() {
		IngredientRegulatoryCodes present = IngredientRegulatoryCodes.parse("DECERNIS_42,BECPG_1");

		assertEquals(present, present.withBecpgCodes(List.of("BECPG_1")));
	}
}
