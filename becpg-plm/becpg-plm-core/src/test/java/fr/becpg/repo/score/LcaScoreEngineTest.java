package fr.becpg.repo.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.score.data.ScoreDefCoeffListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Unit tests of the aggregation of the life cycle indicators into a single score.
 */
public class LcaScoreEngineTest {

	private static final double PRECISION = 0.0001d;

	private static final double MICROPOINTS = 1000000d;

	private static final String CLIMATE_CHANGE = "CLIMATE_CHANGE";

	private static final String COMPLEMENTS = "BIODIVERSITY_COMPLEMENTS";

	private static final String WATER_USE = "WATER_USE";

	private final ScoreDefinitionService scoreDefinitionService = mock(ScoreDefinitionService.class);

	private final NodeService nodeService = mock(NodeService.class);

	private final LcaScoreEngine engine = new LcaScoreEngine(scoreDefinitionService, nodeService);

	private final List<ScoreDefCoeffListDataItem> coefficients = new ArrayList<>();

	private final List<LCAListDataItem> lcaList = new ArrayList<>();

	private final ScoreDefinitionItem definition = new ScoreDefinitionItem();

	@Before
	public void setUp() {
		definition.setCode("ECOBALYSE");
		when(scoreDefinitionService.getCoefficients(definition)).thenReturn(coefficients);
		when(scoreDefinitionService.findCoefficient(any(), any())).thenAnswer(invocation -> findCoefficient(invocation.getArgument(1)));
	}

	@Test
	public void contributionsAreExpressedInTheUnitOfTheScore() {
		definition.setScaleFactor(MICROPOINTS);
		givenIndicator(CLIMATE_CHANGE, 2.9d, 7553.08d, 0.2106d);

		ScoreContext context = compute();

		double expected = (2.9d / 7553.08d) * 0.2106d * MICROPOINTS;
		assertEquals(expected, context.getParts().get(0).getContribution(), PRECISION);
		assertEquals(expected, context.getValue(), PRECISION);
	}

	@Test
	public void scoreIsTheSumOfTheDisplayedContributions() {
		definition.setScaleFactor(MICROPOINTS);
		givenIndicator(CLIMATE_CHANGE, 2.9d, 7553.08d, 0.2106d);
		givenIndicator(WATER_USE, 1d, 11468.7d, 0.0674d);

		ScoreContext context = compute();

		double sum = 0d;
		for (ScorePart part : context.getParts()) {
			sum += part.getContribution();
		}
		assertEquals(sum, context.getValue(), PRECISION);
	}

	@Test
	public void complementsInMicropointsAreSubtractedAsSuch() {
		definition.setScaleFactor(MICROPOINTS);
		givenIndicator(COMPLEMENTS, -27.75d, MICROPOINTS, 1d);

		ScoreContext context = compute();

		assertEquals(-27.75d, context.getValue(), PRECISION);
	}

	@Test
	public void definitionWithoutScaleFactorKeepsTheWeightedSum() {
		givenIndicator(CLIMATE_CHANGE, 2.9d, 7553.08d, 0.2106d);

		ScoreContext context = compute();

		assertEquals((2.9d / 7553.08d) * 0.2106d, context.getValue(), PRECISION);
	}

	@Test
	public void indicatorWithoutCoefficientIsLeftOut() {
		givenIndicator(CLIMATE_CHANGE, 2.9d, 7553.08d, 0.2106d);
		lcaList.add(line(lcaNode(WATER_USE), 1d));

		ScoreContext context = compute();

		assertEquals(1, context.getParts().size());
		assertEquals(CLIMATE_CHANGE, context.getParts().get(0).getCode());
	}

	@Test
	public void productWithoutIndicatorsHasNoScore() {
		ScoreContext context = compute();

		assertTrue(context.getParts().isEmpty());
		assertNull(context.getValue());
	}

	private ScoreContext compute() {
		FinishedProductData product = new FinishedProductData();
		product.setLcaList(lcaList);
		return engine.compute(product, definition);
	}

	private void givenIndicator(String code, double value, double normalization, double weighting) {
		NodeRef lca = lcaNode(code);

		ScoreDefCoeffListDataItem coefficient = new ScoreDefCoeffListDataItem();
		coefficient.setLca(lca);
		coefficient.setNormalization(normalization);
		coefficient.setPonderation(weighting);
		coefficients.add(coefficient);

		lcaList.add(line(lca, value));
	}

	private NodeRef lcaNode(String code) {
		NodeRef lca = new NodeRef("workspace://SpacesStore/" + code);
		when(nodeService.getProperty(lca, PLMModel.PROP_LCA_CODE)).thenReturn(code);
		return lca;
	}

	private LCAListDataItem line(NodeRef lca, double value) {
		LCAListDataItem line = new LCAListDataItem();
		line.setLca(lca);
		line.setValue(value);
		return line;
	}

	private Optional<ScoreDefCoeffListDataItem> findCoefficient(NodeRef lca) {
		for (ScoreDefCoeffListDataItem coefficient : coefficients) {
			if (coefficient.getLca().equals(lca)) {
				return Optional.of(coefficient);
			}
		}
		return Optional.empty();
	}

}
