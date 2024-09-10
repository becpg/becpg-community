package fr.becpg.repo.survey.data;

import java.util.List;

import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;

/**
 * <p>SurveyableEntity interface.</p>
 *
 * @author frederic
 * @version $Id: $Id
 */
public interface SurveyableEntity extends FormulatedEntity {


	List<SurveyListDataItem> getSurveyList();

	/**
	 * <p>setSurveyList.</p>
	 *
	 * @param surveyList a {@link java.util.List} object
	 */
	void setSurveyList(List<SurveyListDataItem> surveyList);


	/**
	 * <p>getScoreList.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	List<ScoreListDataItem> getScoreList();

	/**
	 * <p>setScoreList.</p>
	 *
	 * @param scoreList a {@link java.util.List} object
	 */
	void setScoreList(List<ScoreListDataItem> scoreList);

	/**
	 * <p>getScore.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	Integer getScore();

	/**
	 * <p>setScore.</p>
	 *
	 * @param score a {@link java.lang.Integer} object
	 */
	void setScore(Integer score);

}
