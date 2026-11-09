package fr.becpg.repo.score;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Unit tests of {@link CriteriaScoreEngine}: what happens to a score once no criterion
 * targets it any more.
 *
 * @author matthieu
 */
public class CriteriaScoreEngineTest {

	private static final NodeRef FLORINDEX = new NodeRef("workspace://SpacesStore/florindex");

	private static final NodeRef OTHER_SCORE = new NodeRef("workspace://SpacesStore/other-score");

	private ScoreResultWriter writer;

	private CriteriaScoreEngine engine;

	private ScoreDefinitionItem definition;

	private FinishedProductData product;

	@Before
	public void setUp() {
		definition = new ScoreDefinitionItem();
		definition.setNodeRef(FLORINDEX);
		definition.setCode("FLORINDEX");
		definition.setEngine(ScoreEngine.Criteria.name());

		ScoreDefinitionService definitions = mock(ScoreDefinitionService.class);
		when(definitions.getEffectiveScoreDefinitions(any())).thenReturn(List.of(definition));
		writer = mock(ScoreResultWriter.class);
		engine = new CriteriaScoreEngine(mock(NodeService.class), definitions, writer);

		product = new FinishedProductData();
		product.setScoreList(new ArrayList<>());
	}

	@Test
	public void scoreWithoutCriterionIsEmptied() {
		engine.formulateScores(product);

		verify(writer).clear(product, definition);
	}

	@Test
	public void scoreWhoseCriteriaTargetAnotherScoreIsEmptied() {
		product.getScoreList().add(criterion(OTHER_SCORE, 20d, 80d));

		engine.formulateScores(product);

		verify(writer).clear(product, definition);
	}

	@Test
	public void scoreWithoutWeightedCriterionIsEmptied() {
		product.getScoreList().add(criterion(FLORINDEX, null, 80d));

		engine.formulateScores(product);

		verify(writer).clear(product, definition);
		verify(writer, never()).write(any(), any());
	}

	private static ScoreListDataItem criterion(NodeRef scoreDefinition, Double weight, Double score) {
		ScoreListDataItem item = new ScoreListDataItem();
		item.setScoreDefinition(scoreDefinition);
		item.setWeight(weight);
		item.setScore(score);
		return item;
	}
}
