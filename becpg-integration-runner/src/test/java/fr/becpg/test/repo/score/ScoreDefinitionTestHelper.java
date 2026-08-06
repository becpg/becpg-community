package fr.becpg.test.repo.score;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScoreEngine;
import fr.becpg.repo.score.ScoreScale;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.data.EntityScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Creates the score definitions the integration tests need, and reads back the scores they
 * produce.
 *
 * <p>The definitions ship as reference data, which the tests do not import, so a test that
 * wants to check the score list has to create the definition it expects.</p>
 *
 * @author matthieu
 */
public final class ScoreDefinitionTestHelper {

	private ScoreDefinitionTestHelper() {
		// helper
	}

	/**
	 * <p>Creates a definition of a score computed by a plugin, unless it already exists.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param entitySystemService a {@link fr.becpg.repo.entity.EntitySystemService} object
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param systemFolderNodeRef the system folder of the repository
	 * @param code the score code, shared with the plugin computing it
	 * @param version the version of the method
	 * @return the node reference of the definition
	 */
	public static NodeRef createPluginDefinition(NodeService nodeService, EntitySystemService entitySystemService,
			ScoreDefinitionService scoreDefinitionService, NodeRef systemFolderNodeRef, String code, String version) {

		Optional<ScoreDefinitionItem> existing = scoreDefinitionService.findByCode(code, version);
		if (existing.isPresent()) {
			return existing.get().getNodeRef();
		}

		NodeRef listNodeRef = entitySystemService.getSystemEntityDataList(systemFolderNodeRef, RepoConsts.PATH_CHARACTS,
				PlmRepoConsts.PATH_SCORE_DEFINITIONS);

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, code + " " + version);
		properties.put(BeCPGModel.PROP_CHARACT_NAME, code + " " + version);
		properties.put(PLMModel.PROP_SCORE_DEF_CODE, code);
		properties.put(PLMModel.PROP_SCORE_DEF_VERSION, version);
		properties.put(PLMModel.PROP_SCORE_DEF_ENGINE, ScoreEngine.Plugin.name());
		properties.put(PLMModel.PROP_SCORE_DEF_SCALE, ScoreScale.Letter.name());

		NodeRef definitionNodeRef = nodeService
				.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_SCORE_DEFINITION, properties)
				.getChildRef();

		scoreDefinitionService.clearCache();

		return definitionNodeRef;
	}

	/**
	 * <p>Finds the score an entity carries for a given definition.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param definitionNodeRef the definition of the wanted score
	 * @return a {@link java.util.Optional} object
	 */
	public static Optional<EntityScoreListDataItem> findScore(ScoredEntity entity, NodeRef definitionNodeRef) {
		List<EntityScoreListDataItem> scores = entity.getEntityScoreList();

		if (scores == null) {
			return Optional.empty();
		}

		for (EntityScoreListDataItem score : scores) {
			if (Objects.equals(score.getScoreDef(), definitionNodeRef)) {
				return Optional.of(score);
			}
		}

		return Optional.empty();
	}

}
