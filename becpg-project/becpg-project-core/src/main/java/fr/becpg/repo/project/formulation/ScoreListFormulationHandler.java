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
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;

import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.survey.SurveyModel;

import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.data.SurveyableEntity;

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

			if (alfrescoRepository.hasDataList(surveyableEntity, ProjectModel.TYPE_SCORE_LIST)
					&& alfrescoRepository.hasDataList(surveyableEntity, SurveyModel.TYPE_SURVEY_LIST)) {

				// If surveyList is empty, we do nothing
				if ((surveyableEntity.getSurveyList() != null) && !surveyableEntity.getSurveyList().isEmpty()) {

					Map<String, Integer> scoresPerCriterion = new HashMap<>();
					Map<String, Integer> nbOfQuestionsPerCriterion = new HashMap<>();

					fillScoresAndNbQuestions(surveyableEntity, scoresPerCriterion, nbOfQuestionsPerCriterion);

					// For each criterion present in the surveyList, we calculate the score for each criterion in the scoreList. For the criterion that are not
					// in the surveyList, we do nothing
					calculateAndFillScoreList(scoreList, scoresPerCriterion, nbOfQuestionsPerCriterion);
				}
			}

			// Score can be set manually
			if ((surveyableEntity.getScoreList() != null) && !surveyableEntity.getScoreList().isEmpty()) {

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
	 * @param surveyableEntity
	 * @param scoresPerCriterion
	 * @param nbOfQuestionsPerCriterion
	 */
	private void fillScoresAndNbQuestions(SurveyableEntity surveyableEntity, Map<String, Integer> scoresPerCriterion,
			Map<String, Integer> nbOfQuestionsPerCriterion) {
		for (SurveyListDataItem s : surveyableEntity.getSurveyList()) {

			SurveyQuestion question = (SurveyQuestion) alfrescoRepository.findOne(s.getQuestion());

			String criterion = question.getSurveyCriterion() == null ? "" : question.getSurveyCriterion();

			if (!scoresPerCriterion.containsKey(criterion)) {
				scoresPerCriterion.computeIfAbsent(criterion, value -> question.getQuestionScore() == null ? 0 : question.getQuestionScore());
				nbOfQuestionsPerCriterion.computeIfAbsent(criterion, value -> 1);
			} else {
				scoresPerCriterion.computeIfPresent(criterion,
						(key, value) -> value + (question.getQuestionScore() == null ? 0 : question.getQuestionScore()));
				nbOfQuestionsPerCriterion.computeIfPresent(criterion, (key, value) -> value + 1);
			}
		}
	}

	/**
	 * @param scoreList
	 * @param scoresPerCriterion
	 * @param nbOfQuestionsPerCriterion
	 */
	private void calculateAndFillScoreList(List<ScoreListDataItem> scoreList, Map<String, Integer> scoresPerCriterion,
			Map<String, Integer> nbOfQuestionsPerCriterion) {
		for (Entry<String, Integer> criterionScore : scoresPerCriterion.entrySet()) {

			Integer score = (int) (criterionScore.getValue() / nbOfQuestionsPerCriterion.get(criterionScore.getKey()));

			Optional<ScoreListDataItem> scoreForCriterion = scoreList.stream().filter(sl -> criterionScore.getKey().equals(sl.getCriterion()))
					.findFirst();
			if (scoreForCriterion.isPresent()) {
				scoreForCriterion.get().setScore(score);

			}
		}
	}
}
