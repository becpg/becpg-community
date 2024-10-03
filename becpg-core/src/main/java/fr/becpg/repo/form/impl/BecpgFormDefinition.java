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
	
	private JSONObject mergeDef = new JSONObject();
	
	private List<String> forcedFields = new LinkedList<>();
	private List<String> fields = new LinkedList<>();

	private Map<String, JSONObject> tabs = new LinkedHashMap<>();
	private Map<String, JSONObject> sets = new LinkedHashMap<>();
	private Map<String, List<JSONObject>> tree = new LinkedHashMap<>();

	private static final String PROP_ROOT = "root";

	private static final String PROP_FIELDS = "fields";
	private static final String PROP_TAB = "tab";
	private static final String PROP_TABS = "tabs";
	private static final String LOADED = "loaded";

	static final String PROP_ID = "id";
	static final String PROP_DATAKEY = "dataKey";
	static final String PROP_NAME = "name";
	static final String PROP_LABEL = "label";
	static final String PROP_HELP = "help";
	static final String PROP_TYPE = "type";
	static final String PROP_MANDATORY = "mandatory";

	/**
	 * <p>Constructor for BecpgFormDefinition.</p>
	 */
	public BecpgFormDefinition() {
	}

	/**
	 * <p>Constructor for BecpgFormDefinition.</p>
	 *
	 * @param fields a {@link java.util.List} object
	 * @param forcedFields a {@link java.util.List} object
	 * @throws org.json.JSONException if any.
	 */
	public BecpgFormDefinition(List<String> fields, List<String> forcedFields) throws JSONException {

		if (fields != null) {
			for (String field : fields) {
				JSONObject jsonField = new JSONObject();
				jsonField.put(PROP_ID, field);
				addField(null, jsonField, (forcedFields != null) && forcedFields.contains(field));
			}
		}

		if (forcedFields != null) {
			for (String field : forcedFields) {
				if ((fields == null) || !fields.contains(field)) {
					JSONObject jsonField = new JSONObject();
					jsonField.put(PROP_ID, field);
					addField(null, jsonField, true);
				}
			}
		}

	}

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
			if (tabIds.contains(element.getString(PROP_ID))) {
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
					fields = object.getJSONObject(PROP_FIELDS);
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
					object.put(PROP_FIELDS, filteredFields);
				} else {
					return null;
				}
				tabIds.add(object.getString(PROP_TAB));
			} else {
				JSONArray array = object.getJSONArray(PROP_FIELDS);
				JSONArray filteredArray = filterArray(array, tabIds);
				object.put(PROP_FIELDS, filteredArray);
				try {
					JSONArray tabs = object.getJSONArray(PROP_TABS);
					JSONArray filteredTabs = filterTabs(tabs, tabIds);
					object.put(PROP_TABS, filteredTabs);
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
	 * @return a {@link fr.becpg.repo.form.impl.BecpgFormDefinition} object.
	 * @throws org.json.JSONException
	 *             if any.
	 */
	public BecpgFormDefinition merge(Form form, DataGridFormFieldTitleProvider resolver) throws JSONException {
		for (JSONObject tab : tabs.values()) {

			this.mergeDef.append(PROP_TABS, tab);
		}
		Map<String, JSONObject> cloned = new LinkedHashMap<>();

		for (String entry : tree.keySet()) {

			int i = 0;
			int column = 1;

			for (JSONObject toClone : tree.get(entry)) {

				String id = toClone.getString(PROP_ID);

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
					if (field.has(PROP_DATAKEY)) {
						field.put(PROP_ID, field.getString(PROP_DATAKEY));
						field.remove(PROP_DATAKEY);
						field.remove(LOADED);
					}
				}

				if (found) {
					if (PROP_ROOT.equals(entry)) {

						this.mergeDef.append(PROP_FIELDS, field);

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
						if ((set != null) && set.has(PROP_FIELDS) && set.getJSONObject(PROP_FIELDS).has(colName)) {
							columnJson = set.getJSONObject(PROP_FIELDS).getJSONArray(colName);
						} else {
							JSONObject tmp = new JSONObject();
							if (set != null) {
								if (set.has(PROP_FIELDS)) {
									tmp = set.getJSONObject(PROP_FIELDS);
								} else {
									set.put(PROP_FIELDS, tmp);
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

		this.mergeDef = filter(this.mergeDef, new HashSet<>());
		
		return this;
	}

	private void loadData(JSONObject field, Form form) throws JSONException {
		if (field.has(PROP_DATAKEY)) {
			String key = field.getString(PROP_DATAKEY);
			if ((key != null) && (form.getFormData() != null)) {
				FieldData data = form.getFormData().getFieldData(key);

				if (data != null) {
					field.put("value", data.getValue());
				}
			}
		}
	}

	private boolean loadDef(JSONObject field, Form form, DataGridFormFieldTitleProvider resolver) throws JSONException {
		String id = field.getString(PROP_ID);

		boolean loaded = false;
		if (field.has(LOADED)) {
			loaded = field.getBoolean(LOADED);
		}

		boolean found = false;

		if (form.getFieldDefinitions() != null) {
			if (!loaded) {
				for (FieldDefinition fieldDefinition : form.getFieldDefinitions()) {
					if (fieldDefinition.getName().equals(id)) {
	
						found = true;
	
						if (fieldDefinition.getDefaultValue() != null) {
							field.put("value", fieldDefinition.getDefaultValue());
						}
	
						field.put(PROP_NAME, fieldDefinition.getName());
	
						QName fieldQName = QName.createQName(id);
						if (!field.has(PROP_LABEL) || ((resolver != null) && resolver.isAllowed(fieldQName))) {
							if ((resolver != null) && resolver.isAllowed(fieldQName)) {
								field.put(PROP_LABEL, resolver.getTitle(fieldQName));
							} else {
								field.put(PROP_LABEL, fieldDefinition.getLabel());
							}
						}
	
						field.put(PROP_DATAKEY, fieldDefinition.getDataKeyName());
	
						//					String formWidget = "text";
	
						if (fieldDefinition instanceof PropertyFieldDefinition) {
	
							field.put(PROP_TYPE, "property");
							if (((PropertyFieldDefinition) fieldDefinition).getDataType() != null) {
								field.put("dataType", ((PropertyFieldDefinition) fieldDefinition).getDataType());
							}
							if (((PropertyFieldDefinition) fieldDefinition).getDataTypeParameters() != null) {
								field.put("dataTypeParameters", ((PropertyFieldDefinition) fieldDefinition).getDataTypeParameters().getAsJSON());
							}
	
							//						boolean isList = false;
							if (((PropertyFieldDefinition) fieldDefinition).getConstraints() != null) {
	
								JSONArray constraints = new JSONArray();
								field.put("constraints", constraints);
	
								for (FieldConstraint constraint : ((PropertyFieldDefinition) fieldDefinition).getConstraints()) {
	
									JSONObject constraintJson = new JSONObject();
									constraints.put(constraintJson);
									constraintJson.put(PROP_TYPE, constraint.getType());
									if (constraint.getParametersAsJSON() != null) {
										constraintJson.put("parameters", constraint.getParametersAsJSON());
									}
									//
									//								if ("LIST".equals(constraint.getType())) {
									//									isList = true;
									//									if (constraint.getParameters().containsKey("allowedValues")) {
									//
									//										for (String option : ((List<String>) constraint.getParameters().get("allowedValues"))) {
									//											JSONObject optionJson = new JSONObject();
									//											if (option.indexOf('|') > 0) {
									//												optionJson.put(PROP_ID, option.split("\\|")[0]);
									//												optionJson.put(PROP_NAME, option.split("\\|")[1]);
									//											} else {
									//												optionJson.put(PROP_ID, option);
									//												optionJson.put(PROP_NAME, option);
									//											}
									//											field.append("options", optionJson);
									//										}
									//
									//									}
									//								} else if ("REGEXP".equals(constraint.getType())) {
									//									if (constraint.getParameters().containsKey("expression")) {
									//										field.put("regexPattern", constraint.getParameters().get("expression"));
									//									}
									//									if (constraint.getParameters().containsKey("requiresMatch")) {
	
									//									}
									//								} else if ("MIN-MAX".equals(constraint.getType())) {
									//									if (constraint.getParameters().containsKey("minValue")) {
									//										field.put("minValue", constraint.getParameters().get("minValue"));
									//									}
									//									if (constraint.getParameters().containsKey("maxValue")) {
									//										field.put("maxValue", constraint.getParameters().get("maxValue"));
									//									}
									//								} else if ("LENGTH".equals(constraint.getType())) {
									//									if (constraint.getParameters().containsKey("minLength")) {
									//										field.put("minLength", constraint.getParameters().get("minLength"));
									//
									//									}
									//									if (constraint.getParameters().containsKey("maxLength")) {
									//										field.put("maxLength", constraint.getParameters().get("maxLength"));
									//
									//									}
									//								}
	
								}
							}
							//
							//						switch (((PropertyFieldDefinition) fieldDefinition).getDataType()) {
							//						case "text":
							//							if (isList) {
							//								if (((PropertyFieldDefinition) fieldDefinition).isRepeating()) {
							//									formWidget = "dropdown";
							//								} else {
							//									formWidget = "dropdown";
							//								}
							//							} else {
							//								formWidget = "text";
							//							}
							//							break;
							//						case "boolean":
							//							formWidget = "boolean";
							//							break;
							//						case "mltext":
							//							formWidget = "mtlangue";
							//							break;
							//						case "integer":
							//							formWidget = "integer";
							//							break;
							//						case "double":
							//							formWidget = "integer";
							//							break;
							//						case "long":
							//							formWidget = "integer";
							//							break;
							//						case "float":
							//							formWidget = "integer";
							//							break;
							//						case "noderef":
							//							formWidget = "text";
							//							break;
							//						case "date":
							//							formWidget = "date";
							//							break;
							//						case "datetime":
							//							formWidget = "datetime";
							//							break;
							//
							//						}
	
							if (((PropertyFieldDefinition) fieldDefinition).isMandatory()) {
								field.put(PROP_MANDATORY, true);
							}
	
							if (((PropertyFieldDefinition) fieldDefinition).isRepeating()) {
								field.put("repeating", true);
							}
							if (((PropertyFieldDefinition) fieldDefinition).isProtectedField()) {
								field.put("protectedField", true);
							}
						} else if (fieldDefinition instanceof AssociationFieldDefinition) {
	
							field.put(PROP_TYPE, "association");
							field.put("endpointType", ((AssociationFieldDefinition) fieldDefinition).getEndpointType());
							field.put("endpointDirection", ((AssociationFieldDefinition) fieldDefinition).getEndpointDirection());
							field.put("endpointMany", ((AssociationFieldDefinition) fieldDefinition).isEndpointMany());
	
							if (((AssociationFieldDefinition) fieldDefinition).isEndpointMandatory()) {
								field.put("endpointMandatory", true);
								field.put(PROP_MANDATORY, true);
							}
	
							//						formWidget = "autocomplete";
						}
	
						//					if (!field.has("widget")) {
						//						field.put("widget", formWidget);
						//					}
	
						field.put(LOADED, true);
	
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
	 * Getter for the field <code>mergeDef</code>.
	 * </p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 */
	public JSONObject getMergeDef() {
		return mergeDef;
	}
	
	/**
	 * <p>
	 * Get the title defined in mergeDef.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object.
	 * @param attName a {@link java.lang.String} object
	 */
	public String getTitle(String attName) {
		
		if (mergeDef.has(PROP_FIELDS)) {
			for (var field : (JSONArray) mergeDef.get(PROP_FIELDS)) {
				JSONObject json = (JSONObject) field;
				
				if (json.has(PROP_LABEL)) {
					String name = (String) json.get(PROP_NAME);
					String label = (String) json.get(PROP_LABEL);
					
					if (!label.equals("") && attName.equals(name)) {
						return label;
					}
				}
			}
		}
		
		return null;
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
			set.put(PROP_TAB, parentId);
			parentId = PROP_ROOT;

		}

		if ((parentId == null) || parentId.isEmpty()) {
			parentId = PROP_ROOT;
		}

		List<JSONObject> tmp = new LinkedList<>();
		if (tree.containsKey(parentId)) {
			tmp = tree.get(parentId);
		} else {
			tree.put(parentId, tmp);
		}

		tmp.add(set);

		sets.put(set.getString(PROP_ID), set);

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
			field.put(PROP_TAB, parentId);
			parentId = PROP_ROOT;

		}

		if ((parentId == null) || parentId.isEmpty()) {
			parentId = PROP_ROOT;
		}

		List<JSONObject> tmp = new LinkedList<>();
		if (tree.containsKey(parentId)) {
			tmp = tree.get(parentId);
		} else {
			tree.put(parentId, tmp);
		}

		tmp.add(field);

		String fieldId = field.getString(PROP_ID);

		if (force) {
			forcedFields.add(fieldId);
		}

		fields.add(fieldId);

	}
	
	/**
	 * <p>
	 * setMergeDef.
	 * </p>
	 *
	 * @param mergeDef
	 *            a {@link org.json.JSONObject} object.
	 */
	public void setMergeDef(JSONObject mergeDef) {
		this.mergeDef = mergeDef;
	}

}
