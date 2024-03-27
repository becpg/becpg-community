package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.survey.data.Survey;

/**
 * 
 */
public interface SurveyableEntity {

	public List<Survey> getSurveyList();

	public void setSurveyList(List<Survey> surveyList);

	public List<ScoreListDataItem> getScoreList();

	public void setScoreList(List<ScoreListDataItem> scorList);

}
