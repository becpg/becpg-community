package fr.becpg.repo.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link SearchWord}: the common form of a word and the prefix comparison.
 */
public class SearchWordTest {

	@Test
	public void regularPluralLosesItsFinalLetter() {
		assertEquals("pate", SearchWord.of("pates").stem());
		assertEquals("noi", SearchWord.of("noix").stem());
		assertEquals("gateau", SearchWord.of("gateaux").stem());
	}

	@Test
	public void frenchAuxPluralBecomesAl() {
		assertEquals("vegetal", SearchWord.of("vegetaux").stem());
		assertEquals("animal", SearchWord.of("animaux").stem());
	}

	@Test
	public void englishIesPluralBecomesY() {
		assertEquals("berry", SearchWord.of("berries").stem());
		assertEquals("cherry", SearchWord.of("cherries").stem());
	}

	@Test
	public void feminineParticipleLosesItsSecondE() {
		assertEquals("sucre", SearchWord.of("sucree").stem());
		assertEquals("sucre", SearchWord.of("sucrees").stem());
		assertEquals("epice", SearchWord.of("epicees").stem());
	}

	@Test
	public void shortWordsAreLeftUntouched() {
		assertEquals("les", SearchWord.of("les").stem());
		assertEquals("aux", SearchWord.of("aux").stem());
		assertEquals("tau", SearchWord.of("taux").stem());
		assertEquals("pie", SearchWord.of("pies").stem());
	}

	@Test
	public void typedWordMatchesEitherForm() {
		assertTrue(SearchWord.of("berries").isStartedBy(SearchWord.of("berri")));
		assertTrue(SearchWord.of("berries").isStartedBy(SearchWord.of("berry")));
		assertTrue(SearchWord.of("sucre").isStartedBy(SearchWord.of("sucree")));
		assertTrue(SearchWord.of("chaux").isStartedBy(SearchWord.of("chau")));
		assertFalse(SearchWord.of("organe").isStartedBy(SearchWord.of("organisation")));
	}

	@Test
	public void typedPluralFindsItsSingularButIsNoShorterPrefix() {
		assertTrue(SearchWord.of("lacs").isStartedBy(SearchWord.of("lacs")));
		assertTrue(SearchWord.of("lac").isStartedBy(SearchWord.of("lacs")));
		assertFalse(SearchWord.of("lactose").isStartedBy(SearchWord.of("lacs")));
		assertFalse(SearchWord.of("lacets").isStartedBy(SearchWord.of("lacs")));
	}

	@Test
	public void sameWordIsComparedInEitherFormButNotAsPrefix() {
		assertTrue(SearchWord.of("r").isSameWordAs(SearchWord.of("r")));
		assertTrue(SearchWord.of("pate").isSameWordAs(SearchWord.of("pates")));
		assertFalse(SearchWord.of("recette").isSameWordAs(SearchWord.of("r")));
	}
}
