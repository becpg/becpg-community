package fr.becpg.repo.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

/**
 * Unit tests for {@link BeCPGQueryHelper#isQueryMatch(String, String)}, on the task descriptions of the acceptance
 * test of ticket #25578 ("project code - project name - task name"), and for
 * {@link BeCPGQueryHelper#sortByRelevance(String, List, Function)}, on the ingredient suggestions of ticket #37670.
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

	private static final String WATER = "Eau";
	private static final String TAP_WATER = "Eau du r\u00e9seau";
	private static final String FILTERED_WATER = "eau filtr\u00e9e";
	private static final String WATER_CHESTNUT = "Ch\u00e2taigne d'eau";
	private static final String EGG_AND_WATER = "\u0152uf + eau";
	private static final String CAKE = "G\u00e2teau";
	private static final String EGG = "\u0152uf";
	private static final String EGG_WHITE = "Blanc d'\u0153uf";

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

	@Test
	public void exactLabelComesFirstThenPrefixThenWordPrefix() {
		List<String> engineOrder = List.of(EGG_AND_WATER, FILTERED_WATER, WATER_CHESTNUT, TAP_WATER, WATER);

		assertEquals(List.of(WATER, FILTERED_WATER, TAP_WATER, EGG_AND_WATER, WATER_CHESTNUT), sortByRelevance("eau", engineOrder));
	}

	@Test
	public void sameRankKeepsTheSearchEngineOrder() {
		assertEquals(List.of(TAP_WATER, FILTERED_WATER), sortByRelevance("eau", List.of(TAP_WATER, FILTERED_WATER)));
		assertEquals(List.of(FILTERED_WATER, TAP_WATER), sortByRelevance("eau", List.of(FILTERED_WATER, TAP_WATER)));
	}

	@Test
	public void rankingIgnoresCaseAccentsAndLigatures() {
		assertEquals(List.of(EGG, EGG_WHITE), sortByRelevance("OEUF", List.of(EGG_WHITE, EGG)));
		assertEquals(List.of(CAKE, WATER), sortByRelevance("gateau", List.of(WATER, CAKE)));
	}

	@Test
	public void textInsideAWordIsNotAWordPrefix() {
		assertEquals(List.of(WATER_CHESTNUT, CAKE), sortByRelevance("eau", List.of(CAKE, WATER_CHESTNUT)));
	}

	@Test
	public void trailingWildcardAndQuotesAreIgnored() {
		assertEquals(List.of(WATER, TAP_WATER), sortByRelevance("eau*", List.of(TAP_WATER, WATER)));
		assertEquals(List.of(WATER, TAP_WATER), sortByRelevance("\"eau\" ", List.of(TAP_WATER, WATER)));
	}

	@Test
	public void missingLabelGoesLast() {
		List<String> engineOrder = new ArrayList<>();
		engineOrder.add(null);
		engineOrder.add(WATER);

		assertEquals(List.of(WATER), sortByRelevance("eau", engineOrder).subList(0, 1));
	}

	@Test
	public void nothingToRankReturnsTheSameList() {
		List<String> engineOrder = List.of(TAP_WATER, WATER);

		assertSame(engineOrder, sortByRelevance("*", engineOrder));
		assertSame(engineOrder, sortByRelevance("  ", engineOrder));
		assertSame(engineOrder, sortByRelevance(null, engineOrder));
		assertSame(engineOrder, sortByRelevance("**", engineOrder));
	}

	@Test
	public void inputListIsLeftUntouched() {
		List<String> engineOrder = new ArrayList<>(List.of(TAP_WATER, WATER));

		sortByRelevance("eau", engineOrder);

		assertEquals(List.of(TAP_WATER, WATER), engineOrder);
	}

	private static List<String> sortByRelevance(String query, List<String> labels) {
		return BeCPGQueryHelper.sortByRelevance(query, labels, Function.identity());
	}
}
