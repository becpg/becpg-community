package fr.becpg.repo.survey;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.data.SurveyQuestionCache;

/**
 * <p>SurveyService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SurveyService {
	
	public static final String CACHE_KEY = SurveyQuestion.class.getName();

	/**
	 * <p>getSurveyData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param dataListName a {@link java.lang.String} object
	 * @return a {@link org.json.JSONObject} object
	 * @throws org.json.JSONException if any.
	 * @param disabled a {@link java.lang.Boolean} object
	 */
	JSONObject getSurveyData(NodeRef entityNodeRef, String dataListName, Boolean disabled)
			throws JSONException;
	
	/**
	 * <p>saveSurveyData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param dataListName a {@link java.lang.String} object
	 * @param data a {@link org.json.JSONObject} object
	 * @throws org.json.JSONException if any.
	 */
	void saveSurveyData(NodeRef entityNodeRef, String dataListName, JSONObject data) throws JSONException;
	
	SurveyQuestionCache getSurveyQuestionCache();
}
