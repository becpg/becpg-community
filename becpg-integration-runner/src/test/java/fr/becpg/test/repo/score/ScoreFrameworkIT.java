package fr.becpg.test.repo.score;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScoreEngine;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoreScale;
import fr.becpg.repo.score.data.EntityScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Checks that a score is published in the score list of an entity only when a matching
 * definition exists and applies to the markets of the entity.
 *
 * @author matthieu
 */
public class ScoreFrameworkIT extends PLMBaseTestCase {

	private static final String TEST_CODE = "TESTSCORE";

	private static final String TEST_VERSION = "1.0";

	private static final String TEST_CLASS = "B";

	private static final Double TEST_VALUE = 42d;

	@Autowired
	private ScoreDefinitionService scoreDefinitionService;

	@Autowired
	private ScoreResultWriter scoreResultWriter;

	@Test
	public void testScoreIsPublishedWhenADefinitionExists() {

		final NodeRef definitionNodeRef = inWriteTx(this::createTestDefinition);

		final NodeRef productNodeRef = inWriteTx(() -> {
			FinishedProductData product = new FinishedProductData();
			product.setName("Score framework product " + System.nanoTime());
			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
		});

		inWriteTx(() -> {
			FinishedProductData product = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);

			scoreResultWriter.write(product, buildContext());

			alfrescoRepository.save(product);
			return null;
		});

		inReadTx(() -> {
			FinishedProductData product = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);

			List<EntityScoreListDataItem> scores = product.getEntityScoreList();
			Assert.assertNotNull("The score list should have been created", scores);
			Assert.assertEquals(1, scores.size());

			EntityScoreListDataItem score = scores.get(0);
			Assert.assertEquals(definitionNodeRef, score.getScoreDef());
			Assert.assertEquals(TEST_VALUE, score.getValue());
			Assert.assertEquals(TEST_CLASS, score.getScoreClass());
			Assert.assertEquals(TEST_VERSION, score.getVersion());
			Assert.assertNotNull("The computation date tracks the freshness of the score", score.getComputedDate());

			ScoreContext details = ScoreContext.parse(score.getDetails());
			Assert.assertEquals(TEST_CODE, details.getCode());
			Assert.assertEquals(1, details.getParts().size());

			return null;
		});
	}

	@Test
	public void testScoreIsSkippedWithoutDefinition() {

		final NodeRef productNodeRef = inWriteTx(() -> {
			FinishedProductData product = new FinishedProductData();
			product.setName("Dormant framework product " + System.nanoTime());
			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
		});

		inWriteTx(() -> {
			FinishedProductData product = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);

			ScoreContext context = buildContext();
			context.setCode("UNKNOWN_SCORE");

			scoreResultWriter.write(product, context);

			alfrescoRepository.save(product);
			return null;
		});

		inReadTx(() -> {
			FinishedProductData product = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);

			List<EntityScoreListDataItem> scores = product.getEntityScoreList();
			Assert.assertTrue("The framework stays dormant without a definition", (scores == null) || scores.isEmpty());

			return null;
		});
	}

	@Test
	public void testPreviousValueIsKeptOnRecompute() {

		inWriteTx(this::createTestDefinition);

		final NodeRef productNodeRef = inWriteTx(() -> {
			FinishedProductData product = new FinishedProductData();
			product.setName("Recomputed framework product " + System.nanoTime());
			return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
		});

		inWriteTx(() -> {
			FinishedProductData product = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
			scoreResultWriter.write(product, buildContext());
			alfrescoRepository.save(product);
			return null;
		});

		inWriteTx(() -> {
			FinishedProductData product = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);

			ScoreContext context = buildContext();
			context.setValue(50d);
			context.setScoreClass("A");

			scoreResultWriter.write(product, context);
			alfrescoRepository.save(product);
			return null;
		});

		inReadTx(() -> {
			FinishedProductData product = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);

			EntityScoreListDataItem score = product.getEntityScoreList().get(0);
			Assert.assertEquals((Double) 50d, score.getValue());
			Assert.assertEquals("A", score.getScoreClass());
			Assert.assertEquals(TEST_VALUE, score.getPreviousValue());
			Assert.assertEquals(TEST_CLASS, score.getPreviousClass());

			return null;
		});
	}

	/**
	 * <p>createTestDefinition.</p>
	 *
	 * @return the node reference of the definition, existing or created
	 */
	private NodeRef createTestDefinition() {
		Optional<ScoreDefinitionItem> existing = scoreDefinitionService.findByCode(TEST_CODE, TEST_VERSION);
		if (existing.isPresent()) {
			return existing.get().getNodeRef();
		}

		NodeRef listNodeRef = entitySystemService.getSystemEntityDataList(systemFolderNodeRef, RepoConsts.PATH_CHARACTS,
				PlmRepoConsts.PATH_SCORE_DEFINITIONS);

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, TEST_CODE + " " + TEST_VERSION);
		properties.put(BeCPGModel.PROP_CHARACT_NAME, TEST_CODE + " " + TEST_VERSION);
		properties.put(PLMModel.PROP_SCORE_DEF_CODE, TEST_CODE);
		properties.put(PLMModel.PROP_SCORE_DEF_VERSION, TEST_VERSION);
		properties.put(PLMModel.PROP_SCORE_DEF_ENGINE, ScoreEngine.Plugin.name());
		properties.put(PLMModel.PROP_SCORE_DEF_SCALE, ScoreScale.Letter.name());

		NodeRef definitionNodeRef = nodeService
				.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_SCORE_DEFINITION, properties)
				.getChildRef();

		scoreDefinitionService.clearCache();

		return definitionNodeRef;
	}

	/**
	 * <p>buildContext.</p>
	 *
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext buildContext() {
		ScoreContext context = new ScoreContext();
		context.setCode(TEST_CODE);
		context.setVersion(TEST_VERSION);
		context.setValue(TEST_VALUE);
		context.setScoreClass(TEST_CLASS);
		context.setScale(ScoreScale.Letter.name());
		context.getParts().add(new ScorePart("part1").withContribution(TEST_VALUE));
		return context;
	}

}
