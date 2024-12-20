/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.project.formulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.data.SurveyableEntity;
import fr.becpg.repo.survey.helper.SurveyableEntityHelper;
import fr.becpg.repo.survey.impl.SurveyServiceImpl.ResponseType;

/**
 * Project visitor to calculate project score
 *
 * @author frederic
 * @version $Id: $Id
 */
public class ScoreListFormulationHandler extends FormulationBaseHandler<SurveyableEntity> {

	private static final Log logger = LogFactory.getLog(ScoreListFormulationHandler.class);

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(SurveyableEntity surveyableEntity) {

		if (accept(surveyableEntity)) {

			List<ScoreListDataItem> scoreList = surveyableEntity.getScoreList();
			
			final List<SurveyListDataItem> surveyList = SurveyableEntityHelper
					.getNamesSurveyLists(alfrescoRepository, surveyableEntity).values().stream()
					.filter(Objects::nonNull)
					.flatMap(List::stream)
					.toList();

			if (alfrescoRepository.hasDataList(surveyableEntity, ProjectModel.TYPE_SCORE_LIST)) {

				// If surveyList is empty, we do nothing
				if (CollectionUtils.isNotEmpty(surveyList)) {

					Map<String, Integer> scoresPerCriterion = new HashMap<>();
					Map<String, Integer> maxScoresPerCriterion = new HashMap<>();

					fillScores(surveyList, scoresPerCriterion, maxScoresPerCriterion);

					// For each criterion present in the surveyList, we calculate the score for each criterion in the scoreList. For the criterion that are not
					// in the surveyList, we do nothing
					calculateAndFillScoreList(scoreList, scoresPerCriterion, maxScoresPerCriterion);
				}
			}

			// Score can be set manually
			if (CollectionUtils.isNotEmpty(surveyableEntity.getScoreList())) {

				int totalScore = 0;
				int totalWeight = 0;
				surveyableEntity.setScore(null);
				for (ScoreListDataItem sl : surveyableEntity.getScoreList()) {

					if ((sl.getWeight() != null) && (sl.getScore() != null)) {
						totalScore += sl.getWeight() * sl.getScore();
						totalWeight += sl.getWeight();
					}
					logger.debug("totalScore: " + totalScore + " totalWeight: " + totalWeight);
				}

				if (totalWeight == 0) {
					logger.debug("Total weight of project " + surveyableEntity.getNodeRef() + " is equal to 0.");
				} else {
					surveyableEntity.setScore(totalScore / totalWeight);
				}

			}
		}

		return true;
	}

	private boolean accept(SurveyableEntity surveyableEntity) {

		if (surveyableEntity instanceof BeCPGDataObject dataObj && dataObj.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			return false;
		}

		return true;
	}

	/**
	 * @param surveyList
	 * @param scoresPerCriterion
	 * @param maxScoresPerCriterion
	 */
	private void fillScores(List<SurveyListDataItem> surveyList, Map<String, Integer> scoresPerCriterion,
			Map<String, Integer> maxScoresPerCriterion) {
		for (SurveyListDataItem s : surveyList) {

			SurveyQuestion question = (SurveyQuestion) alfrescoRepository.findOne(s.getQuestion());

			String criterion = question.getSurveyCriterion() == null ? "" : question.getSurveyCriterion();
			
			final int maxScore;
			
			if (question.getQuestionScore() != null) {
				maxScore = question.getQuestionScore();
			} else {
				// Find all children (answers) of the current question with a positive score
				final List<NodeRef> answers = BeCPGQueryBuilder.createQuery().ofType(SurveyModel.TYPE_SURVEY_QUESTION)
						.andPropEquals(BeCPGModel.PROP_PARENT_LEVEL, question.getNodeRef().toString())
						.andBetween(SurveyModel.PROP_SURVEY_QUESTION_SCORE, "1", "MAX").inDB().list();

				final IntStream scoreStream = answers.stream().map(alfrescoRepository::findOne)
						.map(SurveyQuestion.class::cast).map(SurveyQuestion::getQuestionScore).filter(Objects::nonNull)
						.mapToInt(Integer::intValue);

				maxScore = ResponseType.list.name().equals(question.getResponseType()) ? scoreStream.max().orElse(0)
						: scoreStream.sum();
			}
			
			if (maxScore != 0) {
				final int questionScore = CollectionUtils.emptyIfNull(s.getChoices()).stream()
						.map(alfrescoRepository::findOne).map(SurveyQuestion.class::cast)
						.map(SurveyQuestion::getQuestionScore).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
				
				if (!scoresPerCriterion.containsKey(criterion)) {
					scoresPerCriterion.computeIfAbsent(criterion, value -> questionScore);
					maxScoresPerCriterion.computeIfAbsent(criterion, __ -> maxScore);
				} else {
					scoresPerCriterion.computeIfPresent(criterion,
							(key, value) -> value + questionScore);
					maxScoresPerCriterion.computeIfPresent(criterion, (__, value) -> value + maxScore);
				}
			}
		}
	}

	/**
	 * @param scoreList
	 * @param scoresPerCriterion
	 * @param maxScoresPerCriterion
	 */
	private void calculateAndFillScoreList(List<ScoreListDataItem> scoreList, Map<String, Integer> scoresPerCriterion,
			Map<String, Integer> maxScoresPerCriterion) {
		for (Entry<String, Integer> criterionScore : scoresPerCriterion.entrySet()) {

			Integer score = (int) (100 * ((float) criterionScore.getValue() / maxScoresPerCriterion.get(criterionScore.getKey())));

			Optional<ScoreListDataItem> scoreForCriterion = scoreList.stream().filter(sl -> criterionScore.getKey().equals(sl.getCriterion()))
					.findFirst();
			if (scoreForCriterion.isPresent()) {
				scoreForCriterion.get().setScore(score);
			}
		}
	}
}
