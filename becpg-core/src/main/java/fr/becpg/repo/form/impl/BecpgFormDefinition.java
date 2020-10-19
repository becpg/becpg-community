package fr.becpg.repo.form.impl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.form.column.decorator.DataGridFormFieldTitleProvider;

/**
 * <p>
 * BecpgFormDefinition class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BecpgFormDefinition {

	private List<String> forcedFields = new LinkedList<>();
	private List<String> fields = new LinkedList<>();

	private Map<String, JSONObject> tabs = new LinkedHashMap<>();
	private Map<String, JSONObject> sets = new LinkedHashMap<>();
	private Map<String, List<JSONObject>> tree = new LinkedHashMap<>();

	private static String ROOT = "root";

	private static boolean isContainerRepresentation(final JSONObject object) {
		try {
			return object.getString("fieldType").equals("ContainerRepresentation");
		} catch (JSONException e) {
			return false;
		}
	}

	private static JSONArray filterArray(JSONArray array, Set<String> tabIds) throws JSONException {
		final int size = array.length();
		JSONArray result = new JSONArray();
		for (int i = 0; i < size; ++i) {
			JSONObject element = filter(array.getJSONObject(i), tabIds);
			if (element != null) {
				result.put(element);
			}
		}
		return result;
	}

	private static JSONArray filterTabs(JSONArray tabs, Set<String> tabIds) throws JSONException {
		final int size = tabs.length();
		JSONArray result = new JSONArray();
		for (int i = 0; i < size; i++) {
			JSONObject element = tabs.getJSONObject(i);
			if (tabIds.contains(element.getString("id"))) {
				result.put(element);
			}
		}
		return result;
	}

	private static JSONObject filter(JSONObject object, Set<String> tabIds) {
		try {
			if (isContainerRepresentation(object)) {
				JSONObject fields;
				try {
					fields = object.getJSONObject("fields");
				} catch (JSONException e) {
					return null;
				}
				JSONObject filteredFields = new JSONObject();
				final int size = fields.length();
				int counter = 1;
				for (int i = 1; i <= size; ++i) {
					JSONArray array = fields.getJSONArray(Integer.toString(i));
					JSONArray filteredArray = filterArray(array, tabIds);
					if (filteredArray.length() > 0) {
						filteredFields.put(Integer.toString(counter), filteredArray);
						++counter;
					}
				}
				if (filteredFields.length() > 0) {
					object.put("fields", filteredFields);
				} else {
					return null;
				}
				tabIds.add(object.getString("tab"));
			} else {
				JSONArray array = object.getJSONArray("fields");
				JSONArray filteredArray = filterArray(array, tabIds);
				object.put("fields", filteredArray);
				try {
					JSONArray tabs = object.getJSONArray("tabs");
					JSONArray filteredTabs = filterTabs(tabs, tabIds);
					object.put("tabs", filteredTabs);
				} catch (JSONException e) {
					// Ignore exception; this will simply skip tab filtering
				}
			}
		} catch (JSONException e) {
			// Ignore exception; this will simply skip deeper filtering
		}

		return object;
	}

	/**
	 * <p>
	 * merge.
	 * </p>
	 *
	 * @param form
	 *            a {@link org.alfresco.repo.forms.Form} object.
	 * @param resolver
	 *            a
	 *            {@link fr.becpg.repo.form.column.decorator.DataGridFormFieldTitleProvider}
	 *            object.
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException
	 *             if any.
	 */
	public JSONObject merge(Form form, DataGridFormFieldTitleProvider resolver) throws JSONException {
		JSONObject ret = new JSONObject();

		for (JSONObject tab : tabs.values()) {

			ret.append("tabs", tab);
		}
		Map<String, JSONObject> cloned = new LinkedHashMap<>();

		for (String entry : tree.keySet()) {

			int i = 0;
			int column = 1;

			for (JSONObject toClone : tree.get(entry)) {

				String id = toClone.getString("id");

				boolean found = true;

				if (!sets.containsKey(id)) {
					found = loadDef(toClone, form, resolver);
				}

				if (!cloned.containsKey(id)) {
					JSONObject tmp = new JSONObject(toClone.toString());
					cloned.put(id, tmp);
				}
				JSONObject field = cloned.get(id);

				if (!sets.containsKey(id)) {
					loadData(field, form);
					if (field.has("dataKey")) {
						field.put("id", field.getString("dataKey"));
						field.remove("dataKey");
						field.remove("loaded");
					}
				}

				if (found) {
					if (ROOT.equals(entry)) {

						ret.append("fields", field);

					} else {

						JSONObject set = sets.get(entry);

						if (set != null) {
							if (!cloned.containsKey(entry)) {
								cloned.put(entry, new JSONObject(set.toString()));
							}
							set = cloned.get(entry);
						}

						if ((set != null) && set.has("numberOfColumns")) {
							column = set.getInt("numberOfColumns");
						}

						String colName = "" + ((i % column) + 1);
						JSONArray columnJson = new JSONArray();
						if ((set != null) && set.has("fields") && set.getJSONObject("fields").has(colName)) {
							columnJson = set.getJSONObject("fields").getJSONArray(colName);
						} else {
							JSONObject tmp = new JSONObject();
							if (set != null) {
								if (set.has("fields")) {
									tmp = set.getJSONObject("fields");
								} else {
									set.put("fields", tmp);
								}
							}

							tmp.put(colName, columnJson);
						}
						columnJson.put(field);

					}
					i++;
				}
			}
		}

		return filter(ret, new HashSet<String>());
	}

	private void loadData(JSONObject field, Form form) throws JSONException {
		if (field.has("dataKey")) {
			String key = field.getString("dataKey");
			if ((key != null) && (form.getFormData() != null)) {
				FieldData data = form.getFormData().getFieldData(key);

				if (data != null) {
					field.put("value", data.getValue());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private boolean loadDef(JSONObject field, Form form, DataGridFormFieldTitleProvider resolver) throws JSONException {
		String id = field.getString("id");

		boolean loaded = false;
		if (field.has("loaded")) {
			loaded = field.getBoolean("loaded");
		}

		boolean found = false;

		if (!loaded) {
			for (FieldDefinition fieldDefinition : form.getFieldDefinitions()) {
				if (fieldDefinition.getName().equals(id)) {

					found = true;

					if (fieldDefinition.getDefaultValue() != null) {
						field.put("value", fieldDefinition.getDefaultValue());
					}

					QName fieldQName = QName.createQName(id);
					if (!field.has("name") || ((resolver != null) && resolver.isAllowed(fieldQName))) {
						if ((resolver != null) && resolver.isAllowed(fieldQName)) {
							field.put("name", resolver.getTitle(fieldQName));
						} else {
							field.put("name", fieldDefinition.getLabel());
						}
					}

					field.put("dataKey", fieldDefinition.getDataKeyName());

					String formWidget = "text";

					if (fieldDefinition instanceof PropertyFieldDefinition) {

						boolean isList = false;
						if (((PropertyFieldDefinition) fieldDefinition).getConstraints() != null) {
							for (FieldConstraint constraint : ((PropertyFieldDefinition) fieldDefinition).getConstraints()) {

								if ( "LIST".equals(constraint.getType())) {
									isList = true;
									if (constraint.getParameters().containsKey("allowedValues")) {

										for (String option : ((List<String>) constraint.getParameters().get("allowedValues"))) {
											JSONObject optionJson = new JSONObject();
											if (option.indexOf('|') > 0) {
												optionJson.put("id", option.split("\\|")[0]);
												optionJson.put("name", option.split("\\|")[1]);
											} else {
												optionJson.put("id", option);
												optionJson.put("name", option);
											}
											field.append("options", optionJson);
										}

									}
								} else if ("REGEXP".equals(constraint.getType()) ) {
									if (constraint.getParameters().containsKey("expression")) {
										field.put("regexPattern", constraint.getParameters().get("expression"));
									}
									if (constraint.getParameters().containsKey("requiresMatch")) {
										// TODO
									}
								} else if ("MIN-MAX".equals(constraint.getType() ) ) {
									if (constraint.getParameters().containsKey("minValue")) {
										field.put("minValue", constraint.getParameters().get("minValue"));
									}
									if (constraint.getParameters().containsKey("maxValue")) {
										field.put("maxValue", constraint.getParameters().get("maxValue"));
									}
								} else if ("LENGTH".equals(constraint.getType())) {
									if (constraint.getParameters().containsKey("minLength")) {
										field.put("minLength", constraint.getParameters().get("minLength"));

									}
									if (constraint.getParameters().containsKey("maxLength")) {
										field.put("maxLength", constraint.getParameters().get("maxLength"));

									}
								}

							}
						}

						switch (((PropertyFieldDefinition) fieldDefinition).getDataType()) {
						case "text":
							if (isList) {
								if (((PropertyFieldDefinition) fieldDefinition).isRepeating()) {
									formWidget = "dropdown";
								} else {
									formWidget = "dropdown";
								}
							} else {
								formWidget = "text";
							}
							break;
						case "boolean":
							formWidget = "boolean";
							break;
						case "mltext":
							formWidget = "mtlangue";
							break;
						case "integer":
							formWidget = "integer";
							break;
						case "double":
							formWidget = "integer";
							break;
						case "long":
							formWidget = "integer";
							break;
						case "float":
							formWidget = "integer";
							break;
						case "noderef":
							formWidget = "text";
							break;
						case "date":
							formWidget = "date";
							break;
						case "datetime":
							formWidget = "datetime";
							break;

						}

						if (((PropertyFieldDefinition) fieldDefinition).isMandatory()) {
							field.put("required", true);
						}

					}
					if (fieldDefinition instanceof AssociationFieldDefinition) {
						JSONObject jsonParams = new JSONObject();
						if (field.has("params")) {
							jsonParams = field.getJSONObject("params");
						} else {
							field.put("params", jsonParams);
						}

						jsonParams.put("endpointType", ((AssociationFieldDefinition) fieldDefinition).getEndpointType());
						jsonParams.put("endpointMany", ((AssociationFieldDefinition) fieldDefinition).isEndpointMany());

						if (((AssociationFieldDefinition) fieldDefinition).isEndpointMandatory()) {
							field.put("required", true);
						}

						formWidget = "autocomplete";
					}

					if (!field.has("type")) {
						field.put("type", formWidget);
					}

					field.put("loaded", true);

					break;
				}
			}
		} else {
			for (FieldDefinition fieldDefinition : form.getFieldDefinitions()) {
				if (fieldDefinition.getName().equals(id)) {
					found = true;
					break;
				}
			}
		}
		return found;
	}

	/**
	 * <p>
	 * Getter for the field <code>forcedFields</code>.
	 * </p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getForcedFields() {
		return forcedFields;
	}

	/**
	 * <p>
	 * Getter for the field <code>fields</code>.
	 * </p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getFields() {
		return fields;
	}

	/**
	 * <p>
	 * addTab.
	 * </p>
	 *
	 * @param tabId
	 *            a {@link java.lang.String} object.
	 * @param tab
	 *            a {@link org.json.JSONObject} object.
	 */
	public void addTab(String tabId, JSONObject tab) {
		tabs.put(tabId, tab);

	}

	/**
	 * <p>
	 * addSet.
	 * </p>
	 *
	 * @param parentId
	 *            a {@link java.lang.String} object.
	 * @param set
	 *            a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException
	 *             if any.
	 */
	public void addSet(String parentId, JSONObject set) throws JSONException {
		if (tabs.containsKey(parentId)) {
			set.put("tab", parentId);
			parentId = ROOT;

		}

		if ((parentId == null) || parentId.isEmpty()) {
			parentId = ROOT;
		}

		List<JSONObject> tmp = new LinkedList<>();
		if (tree.containsKey(parentId)) {
			tmp = tree.get(parentId);
		} else {
			tree.put(parentId, tmp);
		}

		tmp.add(set);

		sets.put(set.getString("id"), set);

	}

	/**
	 * <p>
	 * addField.
	 * </p>
	 *
	 * @param parentId
	 *            a {@link java.lang.String} object.
	 * @param field
	 *            a {@link org.json.JSONObject} object.
	 * @param force
	 *            a boolean.
	 * @throws org.json.JSONException
	 *             if any.
	 */
	public void addField(String parentId, JSONObject field, boolean force) throws JSONException {
		if (tabs.containsKey(parentId)) {
			field.put("tab", parentId);
			parentId = ROOT;

		}

		if ((parentId == null) || parentId.isEmpty()) {
			parentId = ROOT;
		}

		List<JSONObject> tmp = new LinkedList<>();
		if (tree.containsKey(parentId)) {
			tmp = tree.get(parentId);
		} else {
			tree.put(parentId, tmp);
		}

		tmp.add(field);

		String fieldId = field.getString("id");

		if (force) {
			forcedFields.add(fieldId);
		}

		fields.add(fieldId);

	}

}
