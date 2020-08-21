package fr.becpg.repo.form;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.common.BeCPGException;

/**
 * <p>BecpgFormService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface BecpgFormService {

	/**
	 * <p>getForm.</p>
	 *
	 * @param itemKind a {@link java.lang.String} object.
	 * @param itemId a {@link java.lang.String} object.
	 * @param formId a {@link java.lang.String} object.
	 * @param siteId a {@link java.lang.String} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.json.JSONObject} object.
	 * @throws fr.becpg.common.BeCPGException if any.
	 * @throws org.json.JSONException if any.
	 */
	JSONObject getForm(String itemKind, String itemId, String formId, String siteId, NodeRef entityNodeRef) throws BeCPGException, JSONException;
	
	/**
	 * <p>reloadConfig.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	void reloadConfig() throws IOException;

}
