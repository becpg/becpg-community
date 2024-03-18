package fr.becpg.repo.entity.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoteParams {

	//Export

	public static final String PARAM_APPEND_CODE = "appendCode";

	public static final String PARAM_APPEND_ERP_CODE = "appendErpCode";

	public static final String PARAM_APPEND_MLTEXT_CONSTRAINT = "appendMlTextConstraint";

	public static final String PARAM_APPEND_NODEREF = "appendNodeRef";

	public static final String PARAM_UPDATE_ENTITY_NODEREFS = "updateEntityNodeRefs";

	public static final String PARAM_REPLACE_HISTORY_NODEREFS = "replaceHistoryNodeRefs";
	
	public static final String PARAM_APPEND_CONTENT = "appendContent";

	public static final String PARAM_IS_INITIAL_VERSION = "isInitialVersion";

	//Import

	public static final String PARAM_REPLACE_EXISTING_LISTS = "replaceExistingLists";

	public static final String PARAM_DATALISTS_TO_REPLACE = "dataListsToReplace";

	public static final String PARAM_FAIL_ON_ASSOC_NOT_FOUND = "failOnAssociationNotFound";

	public static final String PARAM_IGNORE_PATH_FOR_SEARCH = "ignorePathOnSearch";

	public static final String PARAM_APPEND_MLTEXT = "appendMlText";
	
	public static final String PARAM_APPEND_REPORT_PROPS = "appendReportProps";

	private RemoteEntityFormat format;

	private JSONObject jsonParams;

	private List<QName> filteredProperties = new ArrayList<>();
	private List<QName> ignoredFields = new ArrayList<>();
	private List<String> filteredLists = new ArrayList<>();
	private Map<QName, List<QName>> filteredAssocProperties = new HashMap<>();

	public void setFilteredLists(List<String> filteredLists) {
		this.filteredLists = filteredLists;
	}

	public RemoteParams(RemoteEntityFormat format) {
		this.format = format;
	}

	public RemoteEntityFormat getFormat() {
		return format;
	}

	public void setJsonParams(JSONObject jsonParams) {
		this.jsonParams = jsonParams;
	}

	public List<QName> getFilteredProperties() {
		return filteredProperties;
	}

	public void setIgnoredFields(List<QName> ignoredFields) {
		this.ignoredFields = ignoredFields;
	}

	public Map<QName, List<QName>> getFilteredAssocProperties() {
		return filteredAssocProperties;
	}

	/**
	 * <p>setFilteredFields.</p>
	 *
	 * @param fields a {@link java.util.List} object.
	 */
	public void setFilteredFields(List<String> fields, NamespaceService namespaceService) {

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
									List<QName> tmp = new ArrayList<>();
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

	@SuppressWarnings("unchecked")
	public <T> T extractParams(String paramKey, T defaultValue) throws JSONException {
		if ((jsonParams != null) && jsonParams.has(paramKey)) {
			return (T) jsonParams.get(paramKey);
		}

		return defaultValue;
	}

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

	public boolean shouldExtractField(QName field) {
		return !ignoredFields.contains(field);
	}

}
