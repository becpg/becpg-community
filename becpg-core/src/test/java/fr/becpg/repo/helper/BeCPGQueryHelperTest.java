package fr.becpg.repo.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link BeCPGQueryHelper#isQueryMatch(String, String)}, on the task descriptions of the acceptance
 * test of ticket #25578 ("project code - project name - task name").
 */
public class BeCPGQueryHelperTest {

	private static final String BRIEF_MARKETING = "PJ169 - A1 - brief:marketing";
	private static final String SQL_INJECTION = "PJ169 - A1 - ' OR 1=1 --";
	private static final String MIXED_CASE_NAME = "PJ169 - A1 - aXEl";
	private static final String BRIE = "PJ170 - A2 - brie";
	private static final String SCRIPT = "PJ170 - A2 - <script>alert(1)</script>";
	private static final String AMPERSAND_BETWEEN_WORDS = "PJ170 - A2 - azerty & ytreza";
	private static final String OPERATOR_WORDS = "PJ171 - A3 - brief AND / NOT brief";
	private static final String RIEF = "PJ171 - A3 - rief";
	private static final String R_AND_D = "PJ171 - A3 - R&D";
	private static final String LIGATURE = "PJ171 - A3 - Cœur de gamme";
	private static final String STOP_WORD = "PJ171 - A3 - lEs éclairs";
	private static final String ACCENT = "PJ171 - A3 - Validation éthique";

	private static final List<String> TASKS = List.of(BRIEF_MARKETING, SQL_INJECTION, MIXED_CASE_NAME, BRIE, SCRIPT,
			AMPERSAND_BETWEEN_WORDS, OPERATOR_WORDS, RIEF, R_AND_D, LIGATURE, STOP_WORD, ACCENT);

	@Test
	public void blankOrStarQueryMatchesEverything() {
		assertEquals(TASKS, search(""));
		assertEquals(TASKS, search("   "));
		assertEquals(TASKS, search("*"));
	}

	@Test
	public void nullQueryOrNameMatchesNothing() {
		assertFalse(BeCPGQueryHelper.isQueryMatch(null, BRIE));
		assertFalse(BeCPGQueryHelper.isQueryMatch("brie", null));
	}

	@Test
	public void wordsMatchOnTheirStartOnly() {
		assertEquals(List.of(BRIEF_MARKETING, BRIE, OPERATOR_WORDS), search("brie"));
		assertEquals(List.of(RIEF), search("rief"));
	}

	@Test
	public void allWordsMustMatchInAnyOrder() {
		assertEquals(List.of(AMPERSAND_BETWEEN_WORDS), search("ytreza azerty"));
		assertEquals(List.of(BRIEF_MARKETING), search("PJ169 brief"));
		assertEquals(List.of(), search("PJ170 brief"));
	}

	@Test
	public void caseAccentsAndLigaturesAreIgnored() {
		assertEquals(List.of(MIXED_CASE_NAME), search("AXEL"));
		assertEquals(List.of(STOP_WORD), search("ÉCLAIRS"));
		assertEquals(List.of(ACCENT), search("validation ethique"));
		assertEquals(List.of(LIGATURE), search("coeur"));
		assertEquals(List.of(LIGATURE), search("Cœur gamme"));
	}

	@Test
	public void stopWordsAreSearchedAsWords() {
		assertEquals(List.of(STOP_WORD), search("LES"));
		assertEquals(List.of(STOP_WORD), search("les eclairs"));
	}

	@Test
	public void punctuationSeparatesWords() {
		assertEquals(List.of(BRIEF_MARKETING), search("brief marketing"));
		assertEquals(List.of(BRIEF_MARKETING), search("brief   marketing"));
		assertEquals(List.of(BRIEF_MARKETING), search("brief:marketing"));
		assertEquals(List.of(R_AND_D), search("R&D"));
		assertEquals(List.of(R_AND_D), search("r&d"));
	}

	@Test
	public void repeatedWordMustBeFoundTwice() {
		assertEquals(List.of(SQL_INJECTION), search("1=1"));
		assertEquals(List.of(OPERATOR_WORDS), search("brief brief"));
	}

	@Test
	public void operatorsAreSearchedAsWords() {
		assertEquals(List.of(OPERATOR_WORDS), search("brief AND"));
		assertEquals(List.of(OPERATOR_WORDS), search("NOT brief"));
		assertEquals(List.of(), search("brief OR"));
		assertEquals(List.of(), search("PJ169 OR PJ170"));
	}

	@Test
	public void punctuationOnlyQueryIsSearchedAsSubstring() {
		assertEquals(List.of(AMPERSAND_BETWEEN_WORDS, R_AND_D), search("&"));
		assertEquals(List.of(SQL_INJECTION), search("--"));
		assertEquals(List.of(), search("%"));
	}

	@Test
	public void markupAndInjectionAreSearchedAsText() {
		assertEquals(List.of(SCRIPT), search("<script>alert(1)</script>"));
		assertEquals(List.of(SQL_INJECTION), search("' OR 1=1 --"));
		assertEquals(List.of(BRIEF_MARKETING, OPERATOR_WORDS), search("brief*"));
	}

	@Test
	public void pluralFindsSingularAndBack() {
		assertTrue(BeCPGQueryHelper.isQueryMatch("P\u00e2tes*", "P\u00e2te de riz"));
		assertTrue(BeCPGQueryHelper.isQueryMatch("P\u00e2te*", "Pates de riz"));
		assertTrue(BeCPGQueryHelper.isQueryMatch("eclair", "Les \u00e9clairs"));
		assertTrue(BeCPGQueryHelper.isQueryMatch("gateaux", "G\u00e2teau au chocolat"));
	}

	@Test
	public void irregularPluralAndFeminineFindTheirBaseWord() {
		assertTrue(BeCPGQueryHelper.isQueryMatch("végétaux", "Huile végétale"));
		assertTrue(BeCPGQueryHelper.isQueryMatch("sucrée", "Préparation sucré"));
		assertTrue(BeCPGQueryHelper.isQueryMatch("berry", "Mixed berries"));
		assertTrue(BeCPGQueryHelper.isQueryMatch("berri", "Mixed berries"));
		assertFalse(BeCPGQueryHelper.isQueryMatch("organisation", "Organe"));
		assertFalse(BeCPGQueryHelper.isQueryMatch("game", "Gamme"));
	}

	@Test
	public void autocompleteExamplesKeepMatching() {
		assertTrue(BeCPGQueryHelper.isQueryMatch("P\u00e2te*", "pate de riz"));
		assertTrue(BeCPGQueryHelper.isQueryMatch("Pat*", "Patisserie de riz"));
		assertTrue(BeCPGQueryHelper.isQueryMatch("Riz*", "P\u00e2te de riz"));
		assertTrue(BeCPGQueryHelper.isQueryMatch("Riz*", "Riz au lait"));
		assertFalse(BeCPGQueryHelper.isQueryMatch("P\u00e2to*", "Patisserie"));
		assertFalse(BeCPGQueryHelper.isQueryMatch("P\u00e2te*", "DesPates"));
	}

	@Test
	public void hanIdeographsAreWordsOnTheirOwn() {
		assertTrue(BeCPGQueryHelper.isQueryMatch("\u725b\u5976", "\u6709\u673a\u725b\u5976"));
		assertFalse(BeCPGQueryHelper.isQueryMatch("\u7f8a\u5976", "\u6709\u673a\u725b\u5976"));
	}

	@Test
	public void sortingQueryUsesTheSameWords() {
		assertEquals("pate de riz*", BeCPGQueryHelper.prepareQueryForSorting("P\u00e2tes de Riz"));
		assertEquals("brie*", BeCPGQueryHelper.prepareQueryForSorting("brie*"));
	}

	private static List<String> search(String query) {
		List<String> matches = new ArrayList<>();
		for (String task : TASKS) {
			if (BeCPGQueryHelper.isQueryMatch(query, task)) {
				matches.add(task);
			}
		}
		return matches;
	}
}
