package fr.becpg.repo.survey;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.survey.data.SurveyListDataItem;

/**
 * <p>SurveyService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SurveyService {

	/**
	 * <p>getSurveyData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param dataListName a {@link java.lang.String} object
	 * @param writeAccessTester a {@java.util.function.Predicate} object
	 * @return a {@link org.json.JSONObject} object
	 * @throws org.json.JSONException if any.
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

	/**
	 * <p>getVisibles.</p>
	 *
	 * @param surveyListDataItems a {@link java.util.List} object
	 * @return a {@link java.util.List} object
	 */
	List<SurveyListDataItem> getVisibles(List<SurveyListDataItem> surveyListDataItems);	
}
