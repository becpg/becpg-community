package fr.becpg.repo.product.formulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.survey.data.SurveyList;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.data.SurveyableEntity;

/**
 * 
 */
public class ScoreListFormulationHandler extends FormulationBaseHandler<SurveyableEntity> {

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/** {@inheritDoc} */
	protected Class<ScoreListDataItem> getInstanceClass() {
		return ScoreListDataItem.class;
	}

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
	public boolean process(SurveyableEntity formulatedEntity) {

		List<ScoreListDataItem> scoreList = formulatedEntity.getScoreList();

		// If surveyList is empty, we do nothing
		if ((formulatedEntity.getSurveyList() != null) && !formulatedEntity.getSurveyList().isEmpty()) {

			Map<String, Integer> scoresPerCriterion = new HashMap<>();
			Map<String, Integer> nbOfQuestionsPerCriterion = new HashMap<>();

			for (SurveyList s : formulatedEntity.getSurveyList()) {

				SurveyQuestion question = (SurveyQuestion) alfrescoRepository.findOne(s.getQuestion());

				String criterion = question.getSurveyCriterion() == null ? "" : question.getSurveyCriterion();

				if (!scoresPerCriterion.containsKey(criterion)) {
					scoresPerCriterion.computeIfAbsent(criterion, value -> question.getQuestionScore() == null ? 0 : question.getQuestionScore());
					nbOfQuestionsPerCriterion.computeIfAbsent(criterion, value -> 1);
				} else {
					scoresPerCriterion.computeIfPresent(criterion, (key, value) -> value + question.getQuestionScore());
					nbOfQuestionsPerCriterion.computeIfPresent(criterion, (key, value) -> value + 1);
				}
			}

			// For each criterion present in the surveyList, we calculate the score for each criterion in the scoreList. For the criterion that are not
			// in the surveyList, we do nothing
			for (Entry<String, Integer> criterionScore : scoresPerCriterion.entrySet()) {

				Integer score = (int) (criterionScore.getValue() / nbOfQuestionsPerCriterion.get(criterionScore.getKey()));

				Optional<ScoreListDataItem> scoreForCriterion = scoreList.stream().filter(sl -> criterionScore.getKey().equals(sl.getCriterion()))
						.findFirst();
				if (scoreForCriterion.isPresent()) {
					scoreForCriterion.get().setScore(score);

				} else {
					ScoreListDataItem scoreItem = new ScoreListDataItem();
					scoreItem.setCriterion(criterionScore.getKey());
					scoreItem.setScore(score);

					scoreList.add(scoreItem);
				}
			}
		}
		return true;
	}

}
