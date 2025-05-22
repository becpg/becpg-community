package fr.becpg.web.scripts.forms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.web.config.forms.FormConfigElement;
import org.alfresco.web.config.forms.FormsConfigElement;
import org.alfresco.web.config.forms.Mode;
import org.alfresco.web.scripts.forms.FormUIGet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.ConfigModel;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * <p>FilterFormUIGet class.</p>
 *
 * @author Matthieu
 * @version $Id: $Id
 */
public class FilterFormUIGet extends FormUIGet {

	private static final String ENTITY_PREFIX = "entity_";

	private static final String PARAM_SITEID = "siteId";

	private static final String PARAM_ENTITY_TYPE = "entityType";

	private static final String PARAM_LIST = "list";

	private static final Log logger = LogFactory.getLog(FilterFormUIGet.class);

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Object> generateModel(String itemKind, String itemId, WebScriptRequest request, Status status, Cache cache) {
		Map<String, Object> model = null;

		// get mode formId
		String formId = getParameter(request, PARAM_FORM_ID);
		String siteIdParam = getParameter(request, PARAM_SITEID);
		String list = getParameter(request, PARAM_LIST);
		String entityType = getParameter(request, PARAM_ENTITY_TYPE);

		if (entityType != null && entityType.contains(":")) {
			entityType = entityType.split(":")[1];
		}

		Mode mode = Mode.CREATE;
		if (list != null && list.indexOf("WUsed") > -1) {
			mode = Mode.VIEW;
			if (formId != null && testFormConfig(itemId, formId + "-wused") != null) {
				formId = formId + "-wused";
			}
		}

		if (formId != null && !formId.isBlank()) {
			if (entityType != null && !entityType.isBlank() && siteIdParam != null && !siteIdParam.isBlank()
					&& testFormConfig(itemId, formId + "-" + entityType + "-" + siteIdParam) != null) {
				formId = formId + "-" + entityType + "-" + siteIdParam;

			} else if (siteIdParam != null && !siteIdParam.isBlank() && testFormConfig(itemId, formId + "-" + siteIdParam) != null) {
				formId = formId + "-" + siteIdParam;
			} else if (entityType != null && !entityType.isBlank() && testFormConfig(itemId, formId + "-" + entityType) != null) {
				formId = formId + "-" + entityType;
			}

		} else {
			if (entityType != null && !entityType.isBlank() && siteIdParam != null && !siteIdParam.isBlank()
					&& testFormConfig(itemId, entityType + "-" + siteIdParam) != null) {
				formId = entityType + "-" + siteIdParam;

			} else if (siteIdParam != null && !siteIdParam.isBlank() && testFormConfig(itemId, siteIdParam) != null) {
				formId = siteIdParam;
			} else if (entityType != null && !entityType.isBlank() && testFormConfig(itemId, entityType) != null) {
				formId = entityType;
			}
		}

		// get the form configuration and list of fields that are visible (if any)
		FormConfigElement formConfig = getFormConfig(itemId, formId);

		List<String> visibleFields = getVisibleFields(mode, formConfig);

		// get the form definition from the form service
		Response formSvcResponse = retrieveFormDefinition(itemKind, itemId, visibleFields, formConfig);
		if (formSvcResponse.getStatus().getCode() == Status.STATUS_OK) {
			model = generateFormModel(request, mode, formSvcResponse, formConfig);
		} else if (formSvcResponse.getStatus().getCode() == Status.STATUS_UNAUTHORIZED) {
			// set status to 401 and return null model
			status.setCode(Status.STATUS_UNAUTHORIZED);
			status.setRedirect(true);
		} else {
			String errorKey = getParameter(request, PARAM_ERROR_KEY);
			model = generateErrorModel(formSvcResponse, errorKey);
		}

		visibleFields = getVisibleFields(mode, formConfig);

		String prevFieldId = null;
		for (String fieldId : visibleFields) {
			if (fieldId.indexOf(ENTITY_PREFIX) == 0) {

				String fieldSet = formConfig.getFields().get(fieldId).getSet();

				String[] splitted = fieldId.replace(ENTITY_PREFIX, "").split("_");
				String name = splitted[0];

				String subItemId = splitted[1];

				String subFormId = "sub-datagrid-filter";

				if (subItemId.contains("@")) {
					String[] newSplitted = subItemId.split("@");
					subItemId = newSplitted[0];
					subFormId = newSplitted[1];
				}

				FormConfigElement subFormConfig = getFormConfig(subItemId, subFormId);

				List<String> subVisibleFields = new ArrayList<>(getVisibleFields(Mode.CREATE, subFormConfig));

				subVisibleFields.removeAll(visibleFields);
				subVisibleFields = Collections.unmodifiableList(subVisibleFields);

				formSvcResponse = retrieveFormDefinition(itemKind, subItemId, subVisibleFields, subFormConfig);
				if (formSvcResponse.getStatus().getCode() == Status.STATUS_OK && model != null) {
					merge(model, name, generateFormModel(request, Mode.CREATE, formSvcResponse, subFormConfig), fieldSet, prevFieldId);
				}

			} else {
				prevFieldId = fieldId;
			}
		}

		if (model != null && model.containsKey(MODEL_FORM)) {
			Map<String, Object> form = (Map<String, Object>) model.get(MODEL_FORM);
			form.put("mode", "create");
		}

		return model;
	}

	public class FieldPointer extends Element {
		FieldPointer(String id) {
			this.kind = FIELD;
			this.id = id;
		}
	}

	@SuppressWarnings("unchecked")
	private void merge(Map<String, Object> model, String name, Map<String, Object> subModel, String fieldSet, String prevFieldId) {

		Map<String, Object> form = (Map<String, Object>) model.get(MODEL_FORM);
		Map<String, Object> toMergeForm = (Map<String, Object>) subModel.get(MODEL_FORM);

		if (toMergeForm != null) {

			Map<String, Field> fields = (Map<String, Field>) form.get(MODEL_FIELDS);
			List<Constraint> constraints = (List<Constraint>) form.get(MODEL_CONSTRAINTS);
			Set mainSet = findSet(((List<Element>) form.get(MODEL_STRUCTURE)), fieldSet);
			if (mainSet != null) {

				Set toMergedSet = findSet((List<Element>) toMergeForm.get(MODEL_STRUCTURE), null);

				if (fields != null && toMergeForm.containsKey(MODEL_FIELDS) && toMergedSet != null) {
					for (Element el : toMergedSet.getChildren()) {
						if (FIELD.equals(el.getKind())) {

							Field field = ((Map<String, Field>) toMergeForm.get(MODEL_FIELDS)).get(el.getId());
							if (field != null) {
								String id = "nested_" + name.replace(":", "_") + "_" + field.getId();
								field.setName(id);
								fields.put(id, field);
								insertAfter(prevFieldId, mainSet, new FieldPointer(id));
								prevFieldId = id;
							}
						}
					}
				}

				if (constraints != null && toMergeForm.containsKey(MODEL_CONSTRAINTS)) {
					for (Constraint constraint : (List<Constraint>) toMergeForm.get(MODEL_CONSTRAINTS)) {

						constraints.add(createProxy(constraint, name));
					}
				}
			} else {
				logger.error("Cannot find set with id : " + fieldSet);
			}
		}

	}

	private Constraint createProxy(final Constraint constraint, final String name) {

		try {
			java.lang.reflect.Field f = constraint.getClass().getDeclaredField("fieldId");
			f.setAccessible(true);
			f.set(constraint, "nested_" + name.replace(":", "_") + "_" + f.get(constraint));
		} catch (Exception e) {
			logger.error(e, e);
		}

		return constraint;

	}

	private void insertAfter(String prevFieldId, Set mainSet, FieldPointer fieldPointer) {
		int idx = 0;
		if (prevFieldId != null) {
			prevFieldId = prevFieldId.replace(":", "_");
			for (Iterator<Element> iterator = mainSet.getChildren().iterator(); iterator.hasNext();) {
				Element el = iterator.next();

				if (FIELD.equals(el.getKind()) && el.getId().contains(prevFieldId)) {
					mainSet.getChildren().add(idx + 1, fieldPointer);
					return;
				}
				idx++;
			}
		}

		mainSet.addChild(fieldPointer);
	}

	private Set findSet(List<Element> elements, String fieldSet) {

		for (Element el : elements) {
			if (SET.equals(el.getKind())) {
				if (fieldSet == null || el.getId().equals(fieldSet)) {
					return (Set) el;
				} else {
					Set ret = findSet(((Set) el).getChildren(), fieldSet);
					if (ret != null) {
						return ret;
					}
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Generates the POST body to send to the FormService.
	 */
	protected ByteArrayInputStream generateFormDefPostBody(String itemKind, String itemId, List<String> visibleFields, FormConfigElement formConfig)
			throws IOException {
		StringBuilderWriter buf = new StringBuilderWriter(512);
		JSONWriter writer = new JSONWriter(buf);

		writer.startObject();
		writer.writeValue(PARAM_ITEM_KIND, itemKind);
		writer.writeValue(PARAM_ITEM_ID, itemId.replace(":/", ""));

		List<String> forcedFields = null;
		if (visibleFields != null && !visibleFields.isEmpty()) {
			// list the requested fields
			writer.startValue(MODEL_FIELDS);
			writer.startArray();

			forcedFields = new ArrayList<>(visibleFields.size());
			for (String fieldId : visibleFields) {
				if (fieldId.indexOf("dataList_") < 0 && fieldId.indexOf(ENTITY_PREFIX) < 0) {

					// write out the fieldId
					writer.writeValue(fieldId);

					// determine which fields need to be forced
					if (formConfig.isFieldForced(fieldId)) {
						forcedFields.add(fieldId);
					}
				}
			}

			// close the fields array
			writer.endArray();
		}

		// list the forced fields, if present
		if (forcedFields != null && !forcedFields.isEmpty()) {
			writer.startValue(MODEL_FORCE);
			writer.startArray();

			for (String fieldId : forcedFields) {
				writer.writeValue(fieldId);
			}

			writer.endArray();
		}

		// end the JSON object
		writer.endObject();

		if (logger.isDebugEnabled())
			logger.debug("Generated JSON POST body: " + buf.toString());

		// return the JSON body as a stream
		return new ByteArrayInputStream(buf.toString().getBytes());
	}

	/**
	 * <p>testFormConfig.</p>
	 *
	 * @param itemId a {@link java.lang.String} object.
	 * @param formId a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.web.config.forms.FormConfigElement} object.
	 */
	protected FormConfigElement testFormConfig(String itemId, String formId) {
		FormConfigElement formConfig = null;
		FormsConfigElement formsConfig = null;
		RequestContext requestContext = ThreadLocalRequestContext.getRequestContext();
		ConfigModel extendedTemplateConfigModel = requestContext.getExtendedTemplateConfigModel(null);

		if (extendedTemplateConfigModel != null) {
			@SuppressWarnings("unchecked")
			Map<String, ConfigElement> configs = (Map<String, ConfigElement>) extendedTemplateConfigModel.getScoped().get(itemId);
			formsConfig = (FormsConfigElement) configs.get(CONFIG_FORMS);
		}

		if (formsConfig == null) {
			Config configResult = this.configService.getConfig(itemId);
			formsConfig = (FormsConfigElement) configResult.getConfigElement(CONFIG_FORMS);
		}

		if (formsConfig != null) {
			// Extract the form we are looking for
			// try and retrieve the specified form 
			if (formId != null && formId.length() > 0) {
				formConfig = formsConfig.getForm(formId);
			}

		} else if (logger.isWarnEnabled()) {
			logger.warn("Could not lookup form configuration as configService has not been set");
		}
		return formConfig;
	}

}
