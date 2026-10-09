package fr.becpg.test.repo.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
 * Checks how a computed score is published next to a score entered by hand, and that the
 * score list follows the entity template.
 *
 * @author matthieu
 */
public class ScoreResultWriterTest {

	private static final double PRECISION = 0.01d;

	private static final String CODE = "PPWR";

	private static final String RANGE = "A: [95;100] B: [80;95) C: [70;80) NR: [0;70)";

	private static final NodeRef DEFINITION_REF = new NodeRef("workspace://SpacesStore/ppwr-definition");

	private static final NodeRef OUT_OF_MARKET_REF = new NodeRef("workspace://SpacesStore/mtl-definition");

	private static final NodeRef RETIRED_REF = new NodeRef("workspace://SpacesStore/retired-definition");

	private ScoreResultWriter writer;

	private FinishedProductData product;

	private ScoreDefinitionService definitions;

	@Before
	public void setUp() {
		ScoreDefinitionItem definition = new ScoreDefinitionItem();
		definition.setNodeRef(DEFINITION_REF);
		definition.setCode(CODE);
		definition.setRange(RANGE);

		ScoreDefinitionItem outOfMarket = new ScoreDefinitionItem();
		outOfMarket.setNodeRef(OUT_OF_MARKET_REF);
		outOfMarket.setCode("MTL");

		definitions = mock(ScoreDefinitionService.class);
		when(definitions.findByCode(eq(CODE), any())).thenReturn(Optional.of(definition));
		when(definitions.findByNodeRef(DEFINITION_REF)).thenReturn(Optional.of(definition));
		when(definitions.findByNodeRef(OUT_OF_MARKET_REF)).thenReturn(Optional.of(outOfMarket));
		when(definitions.findByNodeRef(RETIRED_REF)).thenReturn(Optional.empty());
		when(definitions.isApplicable(any(), any())).thenReturn(true);
		when(definitions.isApplicable(eq(outOfMarket), any())).thenReturn(false);

		writer = new ScoreResultWriter(definitions, null, null);
		product = new FinishedProductData();
		product.setName("Product");
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
		product.getRegulatoryScoreList().add(line(DEFINITION_REF));

		writer.write(product, computed(56d, "NR"));

		ScoreContext details = ScoreContext.parse(product.getRegulatoryScoreList().get(0).getDetails());
		assertFalse(details.isManual());
		assertEquals("NR", details.getScoreClass());
	}

	@Test
	public void testAScoreListedNeitherOnTheProductNorOnItsTemplateIsNotPublished() {
		writer.write(product, computed(56d, "NR"));

		assertTrue(product.getRegulatoryScoreList().isEmpty());
	}

	@Test
	public void testAScoreListedOnTheTemplateIsPublished() {
		withTemplate(line(DEFINITION_REF));

		writer.write(product, computed(56d, "NR"));

		assertEquals(1, product.getRegulatoryScoreList().size());
		RegulatoryScoreListDataItem item = product.getRegulatoryScoreList().get(0);
		assertEquals(DEFINITION_REF, item.getScoreDef());
		assertEquals(56d, item.getValue(), PRECISION);
		assertNull("The line is a copy, the template keeps its own", item.getNodeRef());
	}

	@Test
	public void testTheTemplateIsSynchronizedOnce() {
		withTemplate(line(DEFINITION_REF));

		writer.synchronizeTemplate(product);
		writer.synchronizeTemplate(product);
		writer.write(product, computed(56d, "NR"));

		assertEquals(1, product.getRegulatoryScoreList().size());
	}

	@Test
	public void testATemplateLineOutOfTheMarketsOfTheProductIsNotCopied() {
		withTemplate(line(OUT_OF_MARKET_REF), line(RETIRED_REF));

		writer.synchronizeTemplate(product);

		assertTrue("Neither a score of another market nor a retired score is copied", product.getRegulatoryScoreList().isEmpty());
	}

	@Test
	public void testATemplateLineEnteredByHandKeepsItsEntry() {
		RegulatoryScoreListDataItem templateLine = line(DEFINITION_REF);
		templateLine.setIsManual(true);
		templateLine.setValue(96d);
		templateLine.setScoreClass("A");
		withTemplate(templateLine);

		writer.write(product, computed(56d, "NR"));

		RegulatoryScoreListDataItem item = product.getRegulatoryScoreList().get(0);
		assertTrue(item.getIsManual());
		assertEquals(96d, item.getValue(), PRECISION);
		assertEquals("A", item.getScoreClass());
	}

	@Test
	public void testTheVersionsListedAreThoseOfTheCode() {
		ScoreDefinitionItem version2023 = new ScoreDefinitionItem();
		NodeRef version2023Ref = new NodeRef("workspace://SpacesStore/ppwr-2023");
		version2023.setNodeRef(version2023Ref);
		version2023.setCode(CODE);
		version2023.setVersion("2023");
		when(definitions.findByNodeRef(version2023Ref)).thenReturn(Optional.of(version2023));

		product.getRegulatoryScoreList().add(line(version2023Ref));
		product.getRegulatoryScoreList().add(line(OUT_OF_MARKET_REF));

		assertEquals(List.of("2023"), writer.listedVersions(product, CODE));
	}

	/**
	 * <p>Gives the product a template listing the given score lines.</p>
	 *
	 * @param lines the score lines of the template
	 */
	private void withTemplate(RegulatoryScoreListDataItem... lines) {
		FinishedProductData template = new FinishedProductData();
		template.setName("Template");
		template.setRegulatoryScoreList(new ArrayList<>(List.of(lines)));
		product.setEntityTpl(template);
	}

	/**
	 * <p>A score line pointing to a definition.</p>
	 *
	 * @param scoreDef the node reference of the definition
	 * @return a {@link fr.becpg.repo.score.data.RegulatoryScoreListDataItem} object
	 */
	private static RegulatoryScoreListDataItem line(NodeRef scoreDef) {
		RegulatoryScoreListDataItem item = new RegulatoryScoreListDataItem();
		item.setScoreDef(scoreDef);
		return item;
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
