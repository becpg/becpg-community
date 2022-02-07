package fr.becpg.repo.survey;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;

public interface SurveyService {

	JSONObject getSurveyData(NodeRef entityNodeRef, String dataListName) throws JSONException;
	
	void saveSurveyData(NodeRef entityNodeRef, String dataListName, JSONObject data) throws JSONException;
	
	
}
