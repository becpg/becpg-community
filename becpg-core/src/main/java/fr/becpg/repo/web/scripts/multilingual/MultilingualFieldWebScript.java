package fr.becpg.repo.web.scripts.multilingual;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

/**
 * Return or save MLText field
 * 
 * @author matthieu
 * 
 */
public class MultilingualFieldWebScript extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(MultilingualFieldWebScript.class);

	private static final String PARAM_NODEREF = "nodeRef";

	private static final String PARAM_FIELD = "field";

	private ServiceRegistry serviceRegistry;

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String nodeRef = req.getParameter(PARAM_NODEREF);

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		NodeRef formNodeRef = null;
		QName fieldQname = null;

		if (nodeRef != null && !nodeRef.isEmpty()) {
			formNodeRef = new NodeRef(nodeRef);
		}

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		if (templateArgs != null) {
			String fieldName = templateArgs.get(PARAM_FIELD);
			if (fieldName != null) {
				fieldName = fieldName.replace("_", ":");
				fieldQname = QName.createQName(fieldName, serviceRegistry.getNamespaceService());
			}
		}

		if (formNodeRef == null || fieldQname == null) {
			throw new WebScriptException("Invalid params");
		}

		try {

			PropertyDefinition propertyDef = serviceRegistry.getDictionaryService().getProperty(fieldQname);
			QName dataType = propertyDef.getDataType().getName();

			// MLText
			if (dataType.isMatch(DataTypeDefinition.MLTEXT)) {
				// Save
				MLText mlText = null;

				JSONObject json = (JSONObject) req.parseContent();
				if (json != null) {
					mlText = new MLText();
					for (Iterator<String> iterator = json.keys(); iterator.hasNext();) {
						String key = iterator.next();
						if (!"-".equals(key)) {
							Locale loc = new Locale(new Locale(key).getLanguage());
							if (json.getString(key) != null && json.getString(key).length() > 0) {
								mlText.addValue(loc, json.getString(key));
							}
						}
					}
					serviceRegistry.getNodeService().setProperty(formNodeRef, fieldQname, mlText);

				}

				boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
				try {
					if (mlText == null) {
						Serializable value = serviceRegistry.getNodeService().getProperty(formNodeRef, fieldQname);
						if (value instanceof MLText) {
							mlText = (MLText) value;
						} else if (value != null && value instanceof String) {
							mlText = new MLText();
							mlText.addValue(I18NUtil.getContentLocaleLang(), (String) value);
						} else {
							mlText = new MLText();
							mlText.addValue(I18NUtil.getContentLocaleLang(), "");
						}
					}
				} finally {
					MLPropertyInterceptor.setMLAware(wasMLAware);
				}

				JSONObject ret = new JSONObject();

				if (mlText != null) {
					JSONArray items = new JSONArray();
					for (Map.Entry<Locale, String> mlEntry : mlText.entrySet()) {
						JSONObject item = new JSONObject();
						item.put("label", propertyDef.getTitle(serviceRegistry.getDictionaryService()));
						item.put("description", propertyDef.getDescription(serviceRegistry.getDictionaryService()));
						item.put("name", fieldQname.toPrefixString(serviceRegistry.getNamespaceService()));
						item.put("value", mlEntry.getValue());
						item.put("locale", mlEntry.getKey().getLanguage());
						items.put(item);
					}

					ret.put("items", items);
				}

				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				res.getWriter().write(ret.toString(3));

			} else {
				throw new WebScriptException("Field is not an MLText");
			}

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("MultilingualFieldWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

}
