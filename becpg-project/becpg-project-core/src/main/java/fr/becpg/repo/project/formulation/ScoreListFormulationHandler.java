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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.DoubleStream;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.ReportableEntity;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.MLTextHelper;
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

	private NodeService nodeService;

	private NodeService mlNodeService;

	private SpelFormulaService formulaService;

	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(SurveyableEntity surveyableEntity) {

		if (accept(surveyableEntity)) {

			if (surveyableEntity.getSurveyList() == null) {
				surveyableEntity.setScoreList(new ArrayList<>());
			}

			formulateSurveylist(surveyableEntity);

			calculateScore(surveyableEntity);

			calculateScoreType(surveyableEntity);

		}

		return true;
	}

	public void calculateScoreType(SurveyableEntity surveyableEntity) {

		surveyableEntity.getScoreList().forEach(n ->
		n.setCriterion((String) nodeService.getProperty(n.getCharactNodeRef(), ProjectModel.PROP_SCORE_CRITERION_TYPE))
		);

	}

	public void formulateSurveylist(SurveyableEntity surveyableEntity) {
		List<ScoreListDataItem> scoreList = surveyableEntity.getScoreList();

		final List<SurveyListDataItem> surveyList = SurveyableEntityHelper.getNamesSurveyLists(alfrescoRepository, surveyableEntity).values().stream()
				.filter(Objects::nonNull).flatMap(List::stream).toList();

		// If surveyList is empty, we do nothing
		if (CollectionUtils.isNotEmpty(surveyList)) {

			Map<NodeRef, Double> scoresPerCriterion = new HashMap<>();
			Map<NodeRef, Double> maxScoresPerCriterion = new HashMap<>();

			fillScores(surveyList, scoresPerCriterion, maxScoresPerCriterion);

			// For each criterion present in the surveyList, we calculate the score for each criterion in the scoreList. For the criterion that are not
			// in the surveyList, we do nothing
			calculateAndFillScoreList(scoreList, scoresPerCriterion, maxScoresPerCriterion);
		}

	}

	public void calculateScore(SurveyableEntity surveyableEntity) {
		if (CollectionUtils.isEmpty(surveyableEntity.getScoreList())) {
			return;
		}

		Composite<ScoreListDataItem> composite = CompositeHelper.getHierarchicalCompoList(surveyableEntity.getScoreList());

		// Preserve original order of operations
		computeFormulas(surveyableEntity, surveyableEntity.getScoreList());
		calculateParentScores(surveyableEntity, composite);
		computeRanges(surveyableEntity, surveyableEntity.getScoreList());

		// Final score calculation
		computeFinalEntityScore(surveyableEntity, composite);
	}

	private void computeFormulas(SurveyableEntity surveyableEntity, List<ScoreListDataItem> scoreList) {
		ExpressionParser parser = formulaService.getSpelParser();

		for (ScoreListDataItem scoreListItem : scoreList) {
			String error = null;
			StandardEvaluationContext context = formulaService.createDataListItemSpelContext(surveyableEntity, scoreListItem);

			// Value formula
			error = processFormulaByType(parser, context, scoreListItem, ProjectModel.PROP_SCORE_CRITERION_FORMULA,
					value -> scoreListItem.setValue((Double) value), Double.class, "message.formulate.formula.incorrect.type.double");

			// Detail formula
			if (error == null) {
				error = processFormulaByType(parser, context, scoreListItem, ProjectModel.PROP_SCORE_CRITERION_FORMULA_DETAIL,
						value -> scoreListItem.setDetail((String) value), String.class, "message.formulate.formula.incorrect.type.string");
			}

			// Error handling
			if ((error != null) && surveyableEntity instanceof ReportableEntity reportableEntity) {
				reportableEntity.addError(MLTextHelper.getI18NMessage("message.formulate.scoreList.error",
						mlNodeService.getProperty(scoreListItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME), error));
			}
		}
	}

	private String processFormulaByType(ExpressionParser parser, StandardEvaluationContext context, ScoreListDataItem scoreListItem,
			QName propertyKey, Consumer<Object> setter, Class<?> expectedType, String errorMessageKey) {
		String formulaText = (String) nodeService.getProperty(scoreListItem.getCharactNodeRef(), propertyKey);

		if ((formulaText == null) || formulaText.isBlank()) {
			return null;
		}

		try {
			String[] formulas = SpelHelper.formatMTFormulas(formulaText);
			for (String formula : formulas) {
				Matcher varFormulaMatcher = SpelHelper.formulaVarPattern.matcher(formula);

				if (varFormulaMatcher.matches()) {
					Expression exp = parser.parseExpression(varFormulaMatcher.group(2));
					context.setVariable(varFormulaMatcher.group(1), exp.getValue(context));
				} else {
					Expression exp = parser.parseExpression(formula);
					Object result = exp.getValue(context);

					if ((result == null) || expectedType.isInstance(result)) {
						setter.accept(result);
					} else {
						return I18NUtil.getMessage(errorMessageKey, Locale.getDefault());
					}
				}
			}
		} catch (Exception e) {
			logger.debug("Error in formula: " + SpelHelper.formatFormula(formulaText), e);
			return e.getLocalizedMessage();
		}

		return null;
	}

	private void calculateParentScores(SurveyableEntity surveyableEntity, Composite<ScoreListDataItem> composite) {
		if (composite.isLeaf()) {
			return;
		}

		Double totalScore = 0d;
		for (Composite<ScoreListDataItem> component : composite.getChildren()) {
			calculateParentScores(surveyableEntity, component);

			ScoreListDataItem scoreListDataItem = component.getData();
			Double weight = retrieveEffectiveWeight(scoreListDataItem);

			if (scoreListDataItem.getValue() != null) {
				totalScore += (scoreListDataItem.getValue() * weight) / 100d;
			}
		}

		if (!composite.isRoot()) {
			composite.getData().setValue(totalScore);
		}
	}

	private Double retrieveEffectiveWeight(ScoreListDataItem scoreListDataItem) {
		
		if (scoreListDataItem.getCharactNodeRef() != null) {
			Double criterionWeight = (Double) nodeService.getProperty(scoreListDataItem.getCharactNodeRef(),
					ProjectModel.PROP_SCORE_CRITERION_WEIGHT);
			if( criterionWeight != null ) {
				scoreListDataItem.setWeight(criterionWeight);
			}
		}
		
		if (scoreListDataItem.getWeight() != null) {
			return scoreListDataItem.getWeight();
		}

		return 100.0;
	}

	private void computeRanges(SurveyableEntity surveyableEntity, List<ScoreListDataItem> scoreList) {
		ExpressionParser parser = formulaService.getSpelParser();

		for (ScoreListDataItem scoreListItem : scoreList) {
			if (scoreListItem.getScore() == null) {
				continue;
			}

			// Range from property
			String rangeText = (String) nodeService.getProperty(scoreListItem.getCharactNodeRef(), ProjectModel.PROP_SCORE_CRITERION_RANGE);
			if ((rangeText != null) && !rangeText.isBlank()) {
				ScoreRangeConverter scoreRangeConverter = new ScoreRangeConverter(rangeText);
				scoreListItem.setRange(scoreRangeConverter.getScoreLetter(scoreListItem.getScore()));
			}

			// Range from formula
			String error = processFormulaByType(parser, formulaService.createDataListItemSpelContext(surveyableEntity, scoreListItem), scoreListItem,
					ProjectModel.PROP_SCORE_CRITERION_RANGE_FORMULA, value -> scoreListItem.setRange((String) value), String.class,
					"message.formulate.formula.incorrect.type.string");

			// Error handling
			if ((error != null) && surveyableEntity instanceof ReportableEntity reportableEntity) {
				reportableEntity.addError(MLTextHelper.getI18NMessage("message.formulate.scoreList.error",
						mlNodeService.getProperty(scoreListItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME), error));
			}
		}
	}

	private void computeFinalEntityScore(SurveyableEntity surveyableEntity, Composite<ScoreListDataItem> composite) {
		int totalScore = 0;
		int totalWeight = 0;

		for (Composite<ScoreListDataItem> component : composite.getChildren()) {
			ScoreListDataItem sl = component.getData();
			if ((sl.getWeight() != null) && (sl.getScore() != null)) {
				totalScore += sl.getWeight() * sl.getScore();
				totalWeight += sl.getWeight();

				logger.debug(String.format("Component Score: %s, Weight: %s", sl.getScore(), sl.getWeight()));
			}
		}

		surveyableEntity.setScore(totalWeight > 0 ? totalScore / totalWeight : null);

		if (totalWeight == 0) {
			logger.debug(String.format("Total weight of project %s is zero.", surveyableEntity.getNodeRef()));
		}
	}

	protected boolean accept(SurveyableEntity surveyableEntity) {
		
		boolean hasScoreList = surveyableEntity.getScoreList() != null
				&& (( surveyableEntity.getScoreList() instanceof ArrayList) || alfrescoRepository.hasDataList(surveyableEntity, ProjectModel.TYPE_SCORE_LIST));
		
		return !isTemplateEntity(surveyableEntity) && hasScoreList;
	}

	protected boolean isTemplateEntity(SurveyableEntity surveyableEntity) {
		return surveyableEntity instanceof BeCPGDataObject dataObj && dataObj.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL);
	}

	private void fillScores(List<SurveyListDataItem> surveyList, Map<NodeRef, Double> scoresPerCriterion,
			Map<NodeRef, Double> maxScoresPerCriterion) {
		for (SurveyListDataItem s : surveyList) {
			SurveyQuestion question = (SurveyQuestion) alfrescoRepository.findOne(s.getQuestion());
			NodeRef criterion = question.getScoreCriterion();

			Double maxScore = calculateMaxScore(question);

			if (maxScore == 0) {
				continue;
			}

			Double questionScore = calculateQuestionScore(s);

			updateCriterionScores(criterion, questionScore, maxScore, scoresPerCriterion, maxScoresPerCriterion);
		}
	}

	private Double calculateMaxScore(SurveyQuestion question) {
		if (question.getQuestionScore() != null) {
			return question.getQuestionScore();
		}

		List<NodeRef> answers = BeCPGQueryBuilder.createQuery().ofType(SurveyModel.TYPE_SURVEY_QUESTION)
				.andPropEquals(BeCPGModel.PROP_PARENT_LEVEL, question.getNodeRef().toString())
				.andBetween(SurveyModel.PROP_SURVEY_QUESTION_SCORE, "1", "MAX").inDB().list();

		DoubleStream scoreStream = answers.stream().map(alfrescoRepository::findOne).map(SurveyQuestion.class::cast)
				.map(SurveyQuestion::getQuestionScore).filter(Objects::nonNull).mapToDouble(Double::doubleValue);

		return ResponseType.list.name().equals(question.getResponseType()) ? scoreStream.max().orElse(0) : scoreStream.sum();
	}

	private double calculateQuestionScore(SurveyListDataItem s) {
		return CollectionUtils.emptyIfNull(s.getChoices()).stream().map(alfrescoRepository::findOne).map(SurveyQuestion.class::cast)
				.map(SurveyQuestion::getQuestionScore).filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();
	}

	private void updateCriterionScores(NodeRef criterion, double questionScore, double maxScore, Map<NodeRef, Double> scoresPerCriterion,
			Map<NodeRef, Double> maxScoresPerCriterion) {
		scoresPerCriterion.merge(criterion, questionScore, Double::sum);
		maxScoresPerCriterion.merge(criterion, maxScore, Double::sum);
	}

	private void calculateAndFillScoreList(List<ScoreListDataItem> scoreList, Map<NodeRef, Double> scoresPerCriterion,
			Map<NodeRef, Double> maxScoresPerCriterion) {
		scoresPerCriterion.forEach((criterion, score) -> {
			if(criterion!=null) {
				double normalizedScore = 100 * (score / maxScoresPerCriterion.get(criterion));
	
				scoreList.stream().filter(sl -> criterion.equals(sl.getScoreCriterion())).findFirst()
						.ifPresent(scoreItem -> scoreItem.setScore(normalizedScore));
			}
		});
	}

}
