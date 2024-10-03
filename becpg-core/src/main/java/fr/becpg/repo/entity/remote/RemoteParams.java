package fr.becpg.repo.entity.remote;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>RemoteParams class.</p>
 *
 * @author matthieu
 */
public class RemoteParams {

	//Export

	/** Constant <code>PARAM_APPEND_CODE="appendCode"</code> */
	public static final String PARAM_APPEND_CODE = "appendCode";

	/** Constant <code>PARAM_APPEND_ERP_CODE="appendErpCode"</code> */
	public static final String PARAM_APPEND_ERP_CODE = "appendErpCode";

	/** Constant <code>PARAM_APPEND_MLTEXT_CONSTRAINT="appendMlTextConstraint"</code> */
	public static final String PARAM_APPEND_MLTEXT_CONSTRAINT = "appendMlTextConstraint";

	/** Constant <code>PARAM_APPEND_NODEREF="appendNodeRef"</code> */
	public static final String PARAM_APPEND_NODEREF = "appendNodeRef";

	/** Constant <code>PARAM_UPDATE_ENTITY_NODEREFS="updateEntityNodeRefs"</code> */
	public static final String PARAM_UPDATE_ENTITY_NODEREFS = "updateEntityNodeRefs";

	/** Constant <code>PARAM_REPLACE_HISTORY_NODEREFS="replaceHistoryNodeRefs"</code> */
	public static final String PARAM_REPLACE_HISTORY_NODEREFS = "replaceHistoryNodeRefs";
	
	/** Constant <code>PARAM_APPEND_CONTENT="appendContent"</code> */
	public static final String PARAM_APPEND_CONTENT = "appendContent";

	/** Constant <code>PARAM_IS_INITIAL_VERSION="isInitialVersion"</code> */
	public static final String PARAM_IS_INITIAL_VERSION = "isInitialVersion";

	//Import

	/** Constant <code>PARAM_REPLACE_EXISTING_LISTS="replaceExistingLists"</code> */
	public static final String PARAM_REPLACE_EXISTING_LISTS = "replaceExistingLists";

	/** Constant <code>PARAM_DATALISTS_TO_REPLACE="dataListsToReplace"</code> */
	public static final String PARAM_DATALISTS_TO_REPLACE = "dataListsToReplace";

	/** Constant <code>PARAM_FAIL_ON_ASSOC_NOT_FOUND="failOnAssociationNotFound"</code> */
	public static final String PARAM_FAIL_ON_ASSOC_NOT_FOUND = "failOnAssociationNotFound";

	/** Constant <code>PARAM_IGNORE_PATH_FOR_SEARCH="ignorePathOnSearch"</code> */
	public static final String PARAM_IGNORE_PATH_FOR_SEARCH = "ignorePathOnSearch";

	/** Constant <code>PARAM_APPEND_MLTEXT="appendMlText"</code> */
	public static final String PARAM_APPEND_MLTEXT = "appendMlText";
	
	/** Constant <code>PARAM_APPEND_NESTED_DATALIST_TYPE="appendNestedDataListType"</code> */
	public static final String PARAM_APPEND_NESTED_DATALIST_TYPE = "appendNestedDataListType";

	/** Constant <code>PARAM_APPEND_REPORT_PROPS="appendReportProps"</code> */
	public static final String PARAM_APPEND_REPORT_PROPS = "appendReportProps";

	private RemoteEntityFormat format;

	private JSONObject jsonParams;

	private Set<QName> filteredProperties = new HashSet<>();
	private Set<QName> ignoredFields = new HashSet<>();
	private Set<String> filteredLists = new HashSet<>();
	private Map<QName, Set<QName>> filteredAssocProperties = new HashMap<>();
	
	/**
	 * <p>Constructor for RemoteParams.</p>
	 *
	 * @param format a {@link fr.becpg.repo.entity.remote.RemoteEntityFormat} object
	 */
	public RemoteParams(RemoteEntityFormat format) {
		this.format = format;
	}

	/**
	 * <p>Getter for the field <code>format</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.entity.remote.RemoteEntityFormat} object
	 */
	public RemoteEntityFormat getFormat() {
		return format;
	}

	/**
	 * <p>Setter for the field <code>jsonParams</code>.</p>
	 *
	 * @param jsonParams a {@link org.json.JSONObject} object
	 */
	public void setJsonParams(JSONObject jsonParams) {
		this.jsonParams = jsonParams;
	}


	/**
	 * <p>Getter for the field <code>filteredAssocProperties</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<QName, Set<QName>> getFilteredAssocProperties() {
		return filteredAssocProperties;
	}

	/**
	 * <p>Getter for the field <code>jsonParams</code>.</p>
	 *
	 * @return a {@link org.json.JSONObject} object
	 */
	public JSONObject getJsonParams() {
		return jsonParams;
	}

	/**
	 * <p>Getter for the field <code>filteredProperties</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<QName> getFilteredProperties() {
		return filteredProperties;
	}

	/**
	 * <p>Getter for the field <code>ignoredFields</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<QName> getIgnoredFields() {
		return ignoredFields;
	}

	/**
	 * <p>Getter for the field <code>filteredLists</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<String> getFilteredLists() {
		return filteredLists;
	}
	
	/**
	 * <p>Setter for the field <code>filteredProperties</code>.</p>
	 *
	 * @param filteredProperties a {@link java.util.Set} object
	 */
	public void setFilteredProperties(Set<QName> filteredProperties) {
		this.filteredProperties = filteredProperties;
	}

	/**
	 * <p>Setter for the field <code>filteredLists</code>.</p>
	 *
	 * @param filteredLists a {@link java.util.Set} object
	 */
	public void setFilteredLists(Set<String> filteredLists) {
		this.filteredLists = filteredLists;
	}

	/**
	 * <p>Setter for the field <code>filteredAssocProperties</code>.</p>
	 *
	 * @param filteredAssocProperties a {@link java.util.Map} object
	 */
	public void setFilteredAssocProperties(Map<QName, Set<QName>> filteredAssocProperties) {
		this.filteredAssocProperties = filteredAssocProperties;
	}

	/**
	 * <p>setFilteredFields.</p>
	 *
	 * @param fields a {@link java.util.List} object.
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setFilteredFields(Set<String> fields, NamespaceService namespaceService) {

		if ((fields != null) && !fields.isEmpty()) {
			for (String el : fields) {
				try {
					boolean isRejected = false;

					if (el.startsWith("!")) {
						el = el.replace("!", "");
						isRejected = true;
					}

					String[] assoc = el.split("\\|");
					if (isValidQNameString(assoc[0])) {
						QName propQname = QName.createQName(assoc[0], namespaceService);
						if ((assoc.length > 1)) {
							if (isValidQNameString(assoc[1])) {
								QName assocPropQName = QName.createQName(assoc[1], namespaceService);
								if (filteredAssocProperties.containsKey(propQname)) {
									filteredAssocProperties.get(propQname).add(assocPropQName);
								} else {
									Set<QName> tmp = new HashSet<>();
									tmp.add(assocPropQName);
									filteredAssocProperties.put(propQname, tmp);
								}
							}
						} else {
							if (isRejected) {
								ignoredFields.add(propQname);
							} else {
								filteredProperties.add(propQname);
							}
						}
					}
				} catch (NamespaceException e) {
					//Do Nothing
				}
			}
		}

	}

	/**
	 * <p>isValidQNameString.</p>
	 *
	 * @param qName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	protected boolean isValidQNameString(String qName) {
		if (qName.startsWith("/")) {
			return false;
		}
		String[] qnameArray = qName.split(":");
		return qnameArray.length > 1;
	}

	/**
	 * <p>extractParams.</p>
	 *
	 * @param paramKey a {@link java.lang.String} object
	 * @param defaultValue a T object
	 * @return a T object
	 * @throws org.json.JSONException if any.
	 */
	@SuppressWarnings("unchecked")
	public <T> T extractParams(String paramKey, T defaultValue) throws JSONException {
		if ((jsonParams != null) && jsonParams.has(paramKey)) {
			return (T) jsonParams.get(paramKey);
		}

		return defaultValue;
	}

	/**
	 * <p>shouldExtractList.</p>
	 *
	 * @param listName a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean shouldExtractList(String listName) {
		if ((filteredLists != null) && !filteredLists.isEmpty()) {

			boolean rejected = false;
			for (String list : filteredLists) {
				if (list.startsWith("!")) {
					rejected = true;
				}
				if (list.equals("!" + listName)) {
					return false;
				}

				if (list.equals(listName)) {
					return true;
				}
			}

			return rejected;

		}

		return true;
	}

	/**
	 * <p>shouldExtractField.</p>
	 *
	 * @param field a {@link org.alfresco.service.namespace.QName} object
	 * @return a boolean
	 */
	public boolean shouldExtractField(QName field) {
		return !ignoredFields.contains(field);
	}

}
