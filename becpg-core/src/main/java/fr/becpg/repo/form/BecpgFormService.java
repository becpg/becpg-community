package fr.becpg.repo.form;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.form.column.decorator.ColumnDecorator;

/**
 * <p>BecpgFormService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface BecpgFormService {
	

	JSONObject getForm(String itemKind, String itemId, String formId, String siteId, List<String> fields, List<String> forcedFields,
			NodeRef entityNodeRef) throws BeCPGException, JSONException;

	void reloadConfig() throws IOException;

	void registerDecorator(ColumnDecorator columnDecorator);


	

}
