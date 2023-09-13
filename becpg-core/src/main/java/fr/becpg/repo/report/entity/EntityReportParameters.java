package fr.becpg.repo.report.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 *
 *
 * rep:reportTextParameters
 *
 *
 * Exemple 1:
 {
  iterationKey : "bcpg:compoList",
  params : [{
    id: "param1",
    prop : "bcpg:compoListProduct|cm:name",
    // Values
    nodeRef : dataListNodeRef
    value : productName
  }],
  prefs : {
   extractInMultiLevel : "true",
   componentDatalistsToExtract : "",
   extractPriceBreaks : "true",
   mlTextFields: "cm:title",
   assocsToExtract : "bcpg:plants,bcpg:suppliers,bcpg:storageConditionsRef,bcpg:precautionOfUseRef,bcpg:nutListNut",
   assocsToExtractWithDataList : "",
   assocsToExtractWithImage : "bcpg:clients",
   multilineProperties:"bcpg:organoListValue"
  }
  nameFormat : "{entity_cm:name} - {report_cm:name} - {locale} - {param1}",
  titleFormat : "{report_cm:name} - {locale} - {param1}"

 }

 Exemple 2:
 {
  iterationKey : "bcpg:variants",
  params : [{
  	id: "param1",
    prop : "cm:name",
    //Values
    nodeRef : variantNoderef,
    value : variantName,
  }]

  Exemple 3:
 {
  iterationKey : "bcpg:ingListIng",
  params : [{
  	id: "param1",
    prop : "bcpg:ingListIng|cm:name"
  },
  {
  	id: "param2",
    prop : "bcpg:ingListIng|bcpg:legalName"
  }

  ]

}

XML :

<reportParameters>
	<param1 prop="" nodeRef="" value="" />
	<param2 prop="" nodeRef="" value="" />
</reportParameters>

*/

/**
 * <p>EntityReportParameters class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityReportParameters {

	public static final String PARAM_COMPONENT_DATALISTS_TO_EXTRACT = "componentDatalistsToExtract";
	public static final String PARAM_ENTITY_DATALISTS_TO_EXTRACT = "entityDatalistsToExtract";
	public static final String PARAM_EXTRACT_IN_MULTILEVEL = "extractInMultiLevel";
	public static final String PARAM_EXTRACT_NON_EFFECTIVE_COMPONENT = "extractNonEffectiveComponent";
	public static final String PARAM_MAX_COMPOLIST_LEVEL_TO_EXTRACT = "maxCompoListLevelToExtract";

	private static Log logger = LogFactory.getLog(EntityReportParameters.class);

	public static class EntityReportParameter {
		private String id;
		private String prop;
		private NodeRef nodeRef;
		private String value;

		@Override
		public EntityReportParameter clone() {
			EntityReportParameter ret = new EntityReportParameter();

			ret.id = this.id;
			ret.prop = this.prop;
			ret.nodeRef = this.nodeRef;
			ret.value = this.value;

			return ret;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getProp() {
			return prop;
		}

		public void setProp(String prop) {
			this.prop = prop;
		}

		public NodeRef getNodeRef() {
			return nodeRef;
		}

		public void setNodeRef(NodeRef nodeRef) {
			this.nodeRef = nodeRef;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((id == null) ? 0 : id.hashCode());
			result = (prime * result) + ((nodeRef == null) ? 0 : nodeRef.hashCode());
			result = (prime * result) + ((prop == null) ? 0 : prop.hashCode());
			result = (prime * result) + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			EntityReportParameter other = (EntityReportParameter) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
			if (nodeRef == null) {
				if (other.nodeRef != null) {
					return false;
				}
			} else if (!nodeRef.equals(other.nodeRef)) {
				return false;
			}
			if (prop == null) {
				if (other.prop != null) {
					return false;
				}
			} else if (!prop.equals(other.prop)) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

	}

	private String iterationKey;

	private Map<String, String> preferences = new HashMap<>();

	/**
	 * <p>Constructor for EntityReportParameters.</p>
	 *
	 * @param config a {@link fr.becpg.repo.report.entity.EntityReportParameters} object.
	 */
	public EntityReportParameters(EntityReportParameters config) {
		this.preferences = config.preferences;
		this.reportNameFormat = config.reportNameFormat;
		this.reportTitleFormat = config.reportTitleFormat;
	}

	/**
	 * <p>Constructor for EntityReportParameters.</p>
	 */
	public EntityReportParameters() {
		super();
	}

	/**
	 * <p>Getter for the field <code>preferences</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> getPreferences() {
		return preferences;
	}

	private String reportNameFormat = null;

	private String reportTitleFormat = null;

	/**
	 * <p>Setter for the field <code>reportNameFormat</code>.</p>
	 *
	 * @param reportNameFormat a {@link java.lang.String} object.
	 */
	public void setReportNameFormat(String reportNameFormat) {
		this.reportNameFormat = reportNameFormat;
	}

	/**
	 * <p>Setter for the field <code>reportTitleFormat</code>.</p>
	 *
	 * @param reportTitleFormat a {@link java.lang.String} object.
	 */
	public void setReportTitleFormat(String reportTitleFormat) {
		this.reportTitleFormat = reportTitleFormat;
	}

	/**
	 * <p>Getter for the field <code>reportNameFormat</code>.</p>
	 *
	 * @param defaultNameFormat a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getReportNameFormat(String defaultNameFormat) {

		if (reportNameFormat != null && !reportNameFormat.isEmpty()) {
			return reportNameFormat;
		}

		return defaultNameFormat;
	}

	/**
	 * <p>Getter for the field <code>reportTitleFormat</code>.</p>
	 *
	 * @param defaultTitleFormat a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getReportTitleFormat(String defaultTitleFormat) {
		if (reportTitleFormat != null && !reportTitleFormat.isEmpty()) {
			return reportTitleFormat;
		}
		return defaultTitleFormat;
	}

	private List<EntityReportParameter> parameters = new ArrayList<>();

	/**
	 * <p>Getter for the field <code>iterationKey</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getIterationKey() {
		return iterationKey;
	}

	/**
	 * <p>Setter for the field <code>iterationKey</code>.</p>
	 *
	 * @param iterationKey a {@link java.lang.String} object.
	 */
	public void setIterationKey(String iterationKey) {
		this.iterationKey = iterationKey;
	}

	/**
	 * <p>Getter for the field <code>parameters</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<EntityReportParameter> getParameters() {
		return parameters;
	}

	/**
	 * <p>Setter for the field <code>parameters</code>.</p>
	 *
	 * @param parameters a {@link java.util.List} object.
	 */
	public void setParameters(List<EntityReportParameter> parameters) {
		this.parameters = parameters;
	}

	/**
	 * <p>isParametersEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isParametersEmpty() {
		return parameters.isEmpty();
	}

	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEmpty() {
		return parameters.isEmpty() && preferences.isEmpty() && (reportNameFormat == null || reportNameFormat.isEmpty())
				&& (reportTitleFormat == null || reportTitleFormat.isEmpty());
	}

	/**
	 * <p>updateDataSource.</p>
	 *
	 * @param xmlDataSource a {@link org.dom4j.Element} object.
	 */
	public void updateDataSource(Element xmlDataSource) {

		Element entityEl = (Element) xmlDataSource.getDocument().selectSingleNode("entity");
		if (entityEl != null) {

			Element reportParamsEl = (Element) entityEl.selectSingleNode("reportParams");

			if (reportParamsEl != null) {
				reportParamsEl.detach();
			}
			reportParamsEl = entityEl.addElement("reportParams");

			for (EntityReportParameter param : getParameters()) {
				if (param.getId() != null) {
					Element reportParam = reportParamsEl.addElement(param.getId());
					if (param.getNodeRef() != null) {
						reportParam.addAttribute("nodeRef", param.getNodeRef().toString());
					}
					reportParam.addAttribute("prop", param.getProp());
					reportParam.addAttribute("value", param.getValue());
				} else {
					logger.warn("No param id for parameter : " + param);
				}

			}
		} else {
			logger.warn("Cannot find entity in XML");
		}

	}

	/**
	 * <p>createFromJSON.</p>
	 *
	 * @param jsonString a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.report.entity.EntityReportParameters} object.
	 */
	public static EntityReportParameters createFromJSON(String jsonString) {
		EntityReportParameters ret = new EntityReportParameters();

		if ((jsonString != null) && !jsonString.isEmpty()) {
			JSONObject json = new JSONObject(jsonString);

			if (json.has("params")) {
				JSONArray params = json.getJSONArray("params");

				for (int i = 0; i < params.length(); i++) {
					JSONObject param = params.getJSONObject(i);
					if (param.has("id")) {
						EntityReportParameter tmp = new EntityReportParameter();
						tmp.setId(param.getString("id"));
						if (param.has("prop")) {
							tmp.setProp(param.getString("prop"));
						}
						if (param.has("nodeRef")) {
							tmp.setNodeRef(new NodeRef(param.getString("nodeRef")));
						}
						if (param.has("value")) {
							tmp.setValue(param.getString("value"));
						}

						ret.getParameters().add(tmp);
					}

				}

			}

			if (json.has("iterationKey")) {
				ret.setIterationKey(json.getString("iterationKey"));
			}

			if (json.has("prefs")) {
				JSONObject prefs = json.getJSONObject("prefs");
				JSONArray keys = prefs.names();
				for (int i = 0; i < keys.length(); i++) {
					String key = keys.getString(i);
					Object value = prefs.get(key); 
					if (value instanceof Boolean) {
						ret.getPreferences().put(key, Boolean.toString((Boolean) value));
					} else {
						ret.getPreferences().put(key, value.toString());
					}
				}

			}

			if (json.has("nameFormat")) {
				ret.setReportNameFormat(json.getString("nameFormat"));
			}

			if (json.has("titleFormat")) {
				ret.setReportTitleFormat(json.getString("titleFormat"));
			}

		}

		return ret;
	}

	/**
	 * <p>toJSONString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toJSONString() {
		JSONObject ret = new JSONObject();
		try {
			if ((iterationKey != null) && !iterationKey.isEmpty()) {
				ret.put("iterationKey", iterationKey);
			}

			if (!parameters.isEmpty()) {
				JSONArray params = new JSONArray();
				for (EntityReportParameter param : parameters) {
					JSONObject tmp = new JSONObject();
					tmp.put("id", param.getId());
					tmp.put("prop", param.getProp());
					if (param.getValue() != null) {
						tmp.put("value", param.getValue());
					}
					if (param.getNodeRef() != null) {
						tmp.put("nodeRef", param.getNodeRef().toString());
					}
					params.put(tmp);
				}
				ret.put("params", params);
			}

			if (!preferences.isEmpty()) {
				JSONObject prefs = new JSONObject();

				for (Map.Entry<String, String> pref : preferences.entrySet()) {
					prefs.put(pref.getKey(), pref.getValue());
				}
				ret.put("prefs", prefs);

			}

			if (reportNameFormat != null && !reportNameFormat.isEmpty()) {
				ret.put("nameFormat", reportNameFormat);
			}

			if (reportTitleFormat != null && !reportTitleFormat.isEmpty()) {
				ret.put("titleFormat", reportTitleFormat);
			}

		} catch (JSONException e) {
			logger.warn("Failed to write JSON report params", e);
		}
		return ret.toString();
	}

	/**
	 * <p>match.</p>
	 *
	 * @param source a {@link fr.becpg.repo.report.entity.EntityReportParameters} object.
	 * @return a boolean.
	 */
	public boolean match(EntityReportParameters source) {

		for (EntityReportParameter param : source.getParameters()) {
			boolean found = false;
			for (EntityReportParameter param2 : getParameters()) {
				if (param2.equals(param)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "EntityReportParameters [iterationKey=" + iterationKey + ", parameters=" + parameters + ", preferences=" + preferences + "]";
	}

}
