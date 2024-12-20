package fr.becpg.repo.survey;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;

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
	 * @return a {@link org.json.JSONObject} object
	 * @throws org.json.JSONException if any.
	 */
	JSONObject getSurveyData(NodeRef entityNodeRef, String dataListName) throws JSONException;
	
	/**
	 * <p>saveSurveyData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param dataListName a {@link java.lang.String} object
	 * @param data a {@link org.json.JSONObject} object
	 * @throws org.json.JSONException if any.
	 */
	void saveSurveyData(NodeRef entityNodeRef, String dataListName, JSONObject data) throws JSONException;
	
	
}
