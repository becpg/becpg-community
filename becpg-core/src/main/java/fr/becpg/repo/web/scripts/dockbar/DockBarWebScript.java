package fr.becpg.repo.web.scripts.dockbar;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.extractors.ContentDataExtractor;
import fr.becpg.repo.web.scripts.WebscriptHelper;

/**
 * return product history
 * 
 * @author matthieu
 * 
 */
public class DockBarWebScript extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(DockBarWebScript.class);

	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	private static final String PREF_DOCKBAR_HISTORY = "fr.becpg.DockBarHistory";

	protected static final String PARAM_FIELDS = "metadataFields";

	private AttributeExtractorService attributeExtractorService;

	private PreferenceService preferenceService;

	private ServiceRegistry serviceRegistry;

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	public void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String entityNodeRef = req.getParameter(PARAM_ENTITY_NODEREF);

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		NodeRef productNodeRef = null;
		if (entityNodeRef != null && !entityNodeRef.isEmpty()) {
			productNodeRef = new NodeRef(entityNodeRef);
		}
		List<String> metadataFields = WebscriptHelper.extractMetadataFields(req);

		try {

			if (req.getParameter("clear") != null) {
				preferenceService.clearPreferences(AuthenticationUtil.getFullyAuthenticatedUser());
			}

			String username = AuthenticationUtil.getFullyAuthenticatedUser();

			Map<String, Serializable> histories = preferenceService.getPreferences(username);

			String nodeRefs = (String) histories.get(PREF_DOCKBAR_HISTORY);
			if (logger.isDebugEnabled()) {
				logger.debug("Getting :" + nodeRefs + " from history for " + username);
			}

			LinkedList<NodeRef> elements = new LinkedList<NodeRef>();

			boolean addNodeRef = productNodeRef != null;

			if (nodeRefs != null && nodeRefs.length() > 0) {
				String[] splitted = nodeRefs.split(",");
				for (String field : splitted) {
					NodeRef nodeRef = new NodeRef(field);
					elements.add(nodeRef);
					if (nodeRef.equals(productNodeRef)) {
						addNodeRef = false;
					}
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Entity NodeRef empty : " + productNodeRef != null);
				logger.debug("Already added : " + addNodeRef);
				if (addNodeRef) {
					logger.debug("Subclass of product :"
							+ serviceRegistry.getDictionaryService().isSubClass(serviceRegistry.getNodeService().getType(productNodeRef), BeCPGModel.TYPE_PRODUCT));
					logger.debug("Element.size(): " + elements.size());
				}
			}

			if (productNodeRef != null) {

				QName type = serviceRegistry.getNodeService().getType(productNodeRef);

				if (addNodeRef
						&& (serviceRegistry.getDictionaryService().isSubClass(type, BeCPGModel.TYPE_PRODUCT) || (serviceRegistry.getDictionaryService().isSubClass(type,
								BeCPGModel.TYPE_ENTITY_V2) && !serviceRegistry.getDictionaryService().isSubClass(type, BeCPGModel.TYPE_SYSTEM_ENTITY)))) {
					if (elements.size() > 4) {
						elements.remove(4);
					}
					elements.add(0, productNodeRef);

					nodeRefs = "";
					for (NodeRef nodeRef : elements) {
						if (nodeRefs.length() > 0) {
							nodeRefs += ",";
						}
						nodeRefs += nodeRef.toString();
					}

					if (logger.isDebugEnabled()) {
						logger.debug("Setting :" + nodeRefs + " to history");
					}

					histories.put(PREF_DOCKBAR_HISTORY, nodeRefs);
					preferenceService.setPreferences(username, histories);
				}
			}

			JSONObject ret = processResults(elements, metadataFields);
			ret.put("page", 1);
			ret.put("pageSize", 5);
			ret.put("fullListSize", elements.size());

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(ret.toString(3));

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("DockBarWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

	private JSONObject processResults(List<NodeRef> results, List<String> metadataFields) throws InvalidNodeRefException, JSONException {

		JSONArray items = new JSONArray();

		for (Iterator<NodeRef> iterator = results.iterator(); iterator.hasNext();) {
			NodeRef nodeRef = (NodeRef) iterator.next();
			if (serviceRegistry.getNodeService().exists(nodeRef) && serviceRegistry.getPermissionService().hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {
				items.put(new ContentDataExtractor(metadataFields, serviceRegistry, attributeExtractorService).extract(nodeRef));
			}
		}

		JSONObject obj = new JSONObject();
		obj.put("items", items);
		return obj;

	}

}
