package fr.becpg.test.repo.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Checks how a computed score is published next to a score entered by hand.
 *
 * @author matthieu
 */
public class ScoreResultWriterTest {

	private static final double PRECISION = 0.01d;

	private static final String CODE = "PPWR";

	private static final String RANGE = "A: [95;100] B: [80;95) C: [70;80) NR: [0;70)";

	private static final NodeRef DEFINITION_REF = new NodeRef("workspace://SpacesStore/ppwr-definition");

	private ScoreResultWriter writer;

	private FinishedProductData product;

	@Before
	public void setUp() {
		ScoreDefinitionItem definition = new ScoreDefinitionItem();
		definition.setNodeRef(DEFINITION_REF);
		definition.setCode(CODE);
		definition.setRange(RANGE);

		ScoreDefinitionService definitions = mock(ScoreDefinitionService.class);
		when(definitions.findByCode(eq(CODE), any())).thenReturn(Optional.of(definition));
		when(definitions.isApplicable(any(), any())).thenReturn(true);

		writer = new ScoreResultWriter(definitions, null, null);
		product = new FinishedProductData();
		product.setRegulatoryScoreList(new ArrayList<>());
	}

	@Test
	public void testTheBreakdownOfAManualScoreCarriesTheGradeEntered() {
		RegulatoryScoreListDataItem item = manualItem(96d, "A");

		writer.write(product, computed(56d, "NR"));

		// the marking is drawn from the breakdown, it must show the grade entered by hand
		ScoreContext details = ScoreContext.parse(item.getDetails());
		assertTrue(details.isManual());
		assertEquals("A", details.getScoreClass());
		assertEquals("NR", details.getComputedClass());
		assertEquals(56d, details.getComputedValue(), PRECISION);
	}

	@Test
	public void testTheFormulationDoesNotOverwriteAManualScore() {
		RegulatoryScoreListDataItem item = manualItem(96d, "A");

		writer.write(product, computed(56d, "NR"));

		assertEquals(96d, item.getValue(), PRECISION);
		assertEquals("A", item.getScoreClass());
	}

	@Test
	public void testAManualScoreWithoutClassIsGradedFromItsValue() {
		RegulatoryScoreListDataItem item = manualItem(85d, null);

		writer.write(product, computed(56d, "NR"));

		assertEquals("B", ScoreContext.parse(item.getDetails()).getScoreClass());
	}

	@Test
	public void testAComputedScoreIsNotFlaggedManual() {
		writer.write(product, computed(56d, "NR"));

		ScoreContext details = ScoreContext.parse(product.getRegulatoryScoreList().get(0).getDetails());
		assertFalse(details.isManual());
		assertEquals("NR", details.getScoreClass());
	}

	/**
	 * <p>Adds to the product a PPWR score entered by hand.</p>
	 *
	 * @param value the value entered
	 * @param scoreClass the class entered, may be null
	 * @return a {@link fr.becpg.repo.score.data.RegulatoryScoreListDataItem} object
	 */
	private RegulatoryScoreListDataItem manualItem(Double value, String scoreClass) {
		RegulatoryScoreListDataItem item = new RegulatoryScoreListDataItem();
		item.setScoreDef(DEFINITION_REF);
		item.setIsManual(true);
		item.setValue(value);
		item.setScoreClass(scoreClass);
		product.getRegulatoryScoreList().add(item);
		return item;
	}

	/**
	 * <p>Breakdown of a PPWR score as the formulation computes it.</p>
	 *
	 * @param value the computed value
	 * @param scoreClass the computed class
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext computed(Double value, String scoreClass) {
		ScoreContext context = new ScoreContext();
		context.setCode(CODE);
		context.setValue(value);
		context.setScoreClass(scoreClass);
		return context;
	}

}
