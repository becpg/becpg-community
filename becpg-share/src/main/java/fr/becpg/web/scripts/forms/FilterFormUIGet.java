package fr.becpg.web.scripts.forms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.web.config.forms.FormConfigElement;
import org.alfresco.web.config.forms.Mode;
import org.alfresco.web.scripts.forms.FormUIGet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * 
 * @author Matthieu
 */
public class FilterFormUIGet extends FormUIGet {

	private static final String PARAM_LIST = "list";

	private static Log logger = LogFactory.getLog(FilterFormUIGet.class);

	protected Map<String, Object> generateModel(String itemKind, String itemId, WebScriptRequest request, Status status, Cache cache) {
		Map<String, Object> model = null;

		// get mode formId
		String formId = getParameter(request, PARAM_FORM_ID);

		String list = getParameter(request, PARAM_LIST);

		Mode mode = Mode.CREATE;
		if (list != null && list.indexOf("WUsed") > -1) {
			mode = Mode.VIEW;
		}

		// get the form configuration and list of fields that are visible (if
		// any)
		FormConfigElement formConfig = getFormConfig(itemId, formId);
		List<String> visibleFields = getVisibleFields(mode, formConfig);

		// get the form definition from the form service
		Response formSvcResponse = retrieveFormDefinition(itemKind, itemId, visibleFields, formConfig);
		if (formSvcResponse.getStatus().getCode() == Status.STATUS_OK) {
			model = generateFormModel(request, Mode.CREATE, formSvcResponse, formConfig);
		} else if (formSvcResponse.getStatus().getCode() == Status.STATUS_UNAUTHORIZED) {
			// set status to 401 and return null model
			status.setCode(Status.STATUS_UNAUTHORIZED);
			status.setRedirect(true);
		} else {
			String errorKey = getParameter(request, PARAM_ERROR_KEY);
			model = generateErrorModel(formSvcResponse, errorKey);
		}

		visibleFields = getVisibleFields(mode, formConfig);

		for (String fieldId : visibleFields) {
			if (fieldId.indexOf("entity_") == 0) {

				String[] splitted = fieldId.replace("entity_", "").split("_");
				String name = splitted[0];
				FormConfigElement subFormConfig = getFormConfig(splitted[1], "sub-datagrid-filter");
				List<String> subVisibleFields = getVisibleFields(Mode.CREATE, subFormConfig);

				formSvcResponse = retrieveFormDefinition(itemKind, splitted[1], subVisibleFields, subFormConfig);
				if (formSvcResponse.getStatus().getCode() == Status.STATUS_OK) {
					merge(model, name, generateFormModel(request, Mode.CREATE, formSvcResponse, subFormConfig));
				}

			}
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
	private void merge(Map<String, Object> model, String name, Map<String, Object> subModel) {

		Map<String, Object> form = (Map<String, Object>) model.get(MODEL_FORM);
		Map<String, Object> toMergeForm = (Map<String, Object>) subModel.get(MODEL_FORM);

		if (toMergeForm != null) {

			Map<String, Field> fields = (Map<String, Field>) form.get(MODEL_FIELDS);
			List<Constraint> constraints = (List<Constraint>) form.get(MODEL_CONSTRAINTS);
			Set mainSet = (Set) ((List<Element>) form.get(MODEL_STRUCTURE)).get(0);
			Set toMergedSet = (Set) ((List<Element>) toMergeForm.get(MODEL_STRUCTURE)).get(0);

			if (fields != null && toMergeForm.containsKey(MODEL_FIELDS)) {
				for (Element el : toMergedSet.getChildren()) {
					if(FIELD == el.getKind()){
					
						Field field = ((Map<String, Field>) toMergeForm.get(MODEL_FIELDS)).get(el.getId());
						if (field != null) {
							field.setName("nested_" + name.replace(":", "_") + "_" + field.getId());
							fields.put(field.getId(), field);
							mainSet.addChild(new FieldPointer(field.getId()));
						}
					}
				}
			}

			if (constraints != null && toMergeForm.containsKey(MODEL_CONSTRAINTS)) {
				for (Constraint constraint : (List<Constraint>) toMergeForm.get(MODEL_CONSTRAINTS)) {
					constraints.add(constraint);
				}
			}

		}

	}

	/**
	 * Generates the POST body to send to the FormService.
	 * 
	 * @param itemKind
	 *            The form item kind
	 * @param itemId
	 *            The form item id
	 * @param visibleFields
	 *            The list of field names to return or null to return all fields
	 * @param formConfig
	 *            The form configuration
	 * @return ByteArrayInputStream representing the POST body
	 * @throws IOException
	 */
	protected ByteArrayInputStream generateFormDefPostBody(String itemKind, String itemId, List<String> visibleFields, FormConfigElement formConfig)
			throws IOException {
		StringBuilderWriter buf = new StringBuilderWriter(512);
		JSONWriter writer = new JSONWriter(buf);

		writer.startObject();
		writer.writeValue(PARAM_ITEM_KIND, itemKind);
		writer.writeValue(PARAM_ITEM_ID, itemId.replace(":/", ""));

		List<String> forcedFields = null;
		if (visibleFields != null && visibleFields.size() > 0) {
			// list the requested fields
			writer.startValue(MODEL_FIELDS);
			writer.startArray();

			forcedFields = new ArrayList<String>(visibleFields.size());
			for (String fieldId : visibleFields) {
				if (fieldId.indexOf("dataList_") < 0 && fieldId.indexOf("entity_") < 0) {

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
		if (forcedFields != null && forcedFields.size() > 0) {
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

}
