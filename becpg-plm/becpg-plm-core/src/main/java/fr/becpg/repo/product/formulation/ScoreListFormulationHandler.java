package fr.becpg.repo.product.formulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.survey.data.Survey;
import fr.becpg.repo.survey.data.SurveyQuestion;

/**
 * 
 */
public class ScoreListFormulationHandler extends FormulationBaseHandler<ProductData> {

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
	public boolean process(ProductData formulatedProduct) {

		List<ScoreListDataItem> scoreList = formulatedProduct.getScoreList();

		// If surveyList is empty, we do nothing
		if ((formulatedProduct.getSurveyList() != null) && !formulatedProduct.getSurveyList().isEmpty()) {

			Map<String, Integer> scoresPerCriterion = new HashMap<>();
			Map<String, Integer> nbOfQuestionsPerCriterion = new HashMap<>();

			for (Survey s : formulatedProduct.getSurveyList()) {

				SurveyQuestion question = (SurveyQuestion) alfrescoRepository.findOne(s.getQuestion());

				if (!scoresPerCriterion.containsKey(question.getSurveyCriterion())) {
					scoresPerCriterion.computeIfAbsent(question.getSurveyCriterion(), value -> question.getQuestionScore());
					nbOfQuestionsPerCriterion.computeIfAbsent(question.getSurveyCriterion(), value -> 1);
				} else {
					scoresPerCriterion.computeIfPresent(question.getSurveyCriterion(), (key, value) -> value + question.getQuestionScore());
					nbOfQuestionsPerCriterion.computeIfPresent(question.getSurveyCriterion(), (key, value) -> value + 1);
				}
			}

			// For each criterion present in the surveyList, we calculate the score for each criterion in the scoreList. For the criterion that are not
			// in the surveyList, we do nothing
			for (Entry<String, Integer> criterionScore : scoresPerCriterion.entrySet()) {

				Integer score = (int) (criterionScore.getValue() / nbOfQuestionsPerCriterion.get(criterionScore.getKey()));

				Optional<ScoreListDataItem> scoreForCriterion = scoreList.stream().filter(sl -> sl.getCriterion().equals(criterionScore.getKey()))
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
