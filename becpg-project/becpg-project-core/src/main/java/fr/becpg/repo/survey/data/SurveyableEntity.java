package fr.becpg.repo.survey.data;

import java.util.List;

import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;

/**
 * 
 * @author frederic
 */
public interface SurveyableEntity extends FormulatedEntity {

	List<SurveyList> getSurveyList();

	void setSurveyList(List<SurveyList> surveyList);

	List<ScoreListDataItem> getScoreList();

	void setScoreList(List<ScoreListDataItem> scoreList);

	Integer getScore();

	void setScore(Integer score);

}