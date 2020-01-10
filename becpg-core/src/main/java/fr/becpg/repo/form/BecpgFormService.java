package fr.becpg.repo.form;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.common.BeCPGException;

public interface BecpgFormService {

	JSONObject getForm(String itemKind, String itemId, String formId, String siteId) throws BeCPGException, JSONException;

	void reloadConfig() throws IOException;

}
