package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.ServiceRegistry;
import org.json.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;

/**
 * <p>
 * VersionsGraphWebScript class.
 * </p>
 *
 * @author Alexandre Masanes
 * @version $Id: $Id
 */
public class SystemStatesWebScript extends AbstractWebScript {

	private final ServiceRegistry serviceRegistry;
	
	/**
	 * <p>
	 * Constructor.
	 * </p>
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public SystemStatesWebScript(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		final JSONArray jsonArray = new JSONArray(new ArrayList<>(
				(List<?>) serviceRegistry.getDictionaryService().getConstraint(BeCPGModel.CONSTRAINT_BECPG_SYSTEM_STATE)
						.getConstraint().getParameters().get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM)));

		res.getWriter().write(jsonArray.toString());
	}

}
