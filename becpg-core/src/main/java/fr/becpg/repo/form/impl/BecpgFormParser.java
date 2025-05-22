package fr.becpg.repo.form.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.form.FormParser;
import fr.becpg.repo.helper.MessageHelper;

/**
 * <p>BecpgFormParser class.</p>
 *
 * @author matthieu
 */
public class BecpgFormParser implements FormParser {

	/**
	 * {@inheritDoc}
	 *
	 *
	 * {
	 *  forms : [ {
	 *    id: "",
	 *    itemId : "",
	 *    itemKind : "",
	 *    fields : {
	 *     id: "",
	 *     label: "",
	 *     help: "",
	 *     readOnly : "",
	 *     mandatory: "",
	 *     force: ""
	 *
	 *    }
	 *
	 *  }]
	 *
	 * }
	 */

	@Override
	public void visitConfig(Map<String, Map<String, BecpgFormDefinition>> definitions, InputStream in) throws JSONException {
		String inputString = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
		visitConfig(definitions, inputString);

	}

	/** {@inheritDoc} */
	@Override
	public void visitConfig(Map<String, Map<String, BecpgFormDefinition>> definitions, String inputString) throws JSONException {
		JSONObject defs = new JSONObject(inputString);
		if (defs.has("forms")) {
			JSONArray formDefs = defs.getJSONArray("forms");

			for (int i = 0; i < formDefs.length(); i++) {
				String formId = formDefs.getJSONObject(i).getString(BecpgFormDefinition.PROP_ID);
				String itemId = formDefs.getJSONObject(i).getString("itemId");
				String itemKind = formDefs.getJSONObject(i).getString("itemKind");

				Map<String, BecpgFormDefinition> forms = definitions.computeIfAbsent(itemId, f -> new HashMap<>());

				visitFormElement(formDefs.getJSONObject(i), forms, itemKind, formId);
			}

		}

	}

	private void visitFormElement(JSONObject form, Map<String, BecpgFormDefinition> forms, String itemKind, String formId) throws JSONException {

		if ((formId == null) || ("-".equals(formId)) || formId.isEmpty()) {
			if ("model".equals(itemKind)) {
				formId = "create";
			} else {
				formId = "default";
			}
		}

		BecpgFormDefinition formDef = new BecpgFormDefinition();
		if (forms.containsKey(formId)) {
			formDef = forms.get(formId);
		} else {
			forms.put(formId, formDef);
		}
		if (form.has("fields")) {
			JSONArray formFields = form.getJSONArray("fields");

			for (int i = 0; i < formFields.length(); i++) {

				JSONObject jsonField = new JSONObject();

				JSONObject cur = formFields.getJSONObject(i);

				String fieldName = cur.getString(BecpgFormDefinition.PROP_ID);
				jsonField.put(BecpgFormDefinition.PROP_ID, fieldName);

				if (cur.has(BecpgFormDefinition.PROP_LABEL)) {
					String labelId = cur.getString(BecpgFormDefinition.PROP_LABEL);
					if ((labelId != null) && !labelId.isEmpty()) {
						String label = MessageHelper.getMessage(labelId, I18NUtil.getContentLocale());
						if ((label == null) || !label.isEmpty()) {
							label = labelId;
						}

						if ((label != null) && !label.isEmpty()) {
							jsonField.put(BecpgFormDefinition.PROP_LABEL, label);
						}
					}

				}

				if (cur.has(BecpgFormDefinition.PROP_HELP)) {
					String labelId = cur.getString(BecpgFormDefinition.PROP_HELP);
					if ((labelId != null) && !labelId.isEmpty()) {
						String label = MessageHelper.getMessage(labelId, I18NUtil.getContentLocale());
						if ((label == null) || !label.isEmpty()) {
							label = labelId;
						}

						if ((label != null) && !label.isEmpty()) {
							jsonField.put(BecpgFormDefinition.PROP_HELP, label);
						}
					}
				}

				boolean force = false;
				if (cur.has(PROP_FORCE)) {
					force = cur.getBoolean(PROP_FORCE);
					jsonField.put(PROP_FORCE, force);
				}
				if (cur.has(PROP_READONLY)) {
					jsonField.put(PROP_READONLY, cur.getBoolean(PROP_READONLY));
				}
				if (cur.has(BecpgFormDefinition.PROP_MANDATORY)) {
					jsonField.put(BecpgFormDefinition.PROP_MANDATORY, cur.getBoolean(BecpgFormDefinition.PROP_MANDATORY));
				}

				formDef.addField(null, jsonField, force);

			}
		}
	}

}
