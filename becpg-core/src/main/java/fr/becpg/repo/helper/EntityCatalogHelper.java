package fr.becpg.repo.helper;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EntityCatalogHelper {

	public static final String PROP_CATALOGS = "catalogs";
	public static final String PROP_MISSING_FIELDS = "missingFields";
	public static final String PROP_UNIQUE_FIELDS = "uniqueFields";
	public static final String PROP_NON_UNIQUE_FIELDS = "nonUniqueFields";
	public static final String PROP_DISPLAY_NAME = "displayName";
	public static final String PROP_FIELDS = "fields";
	public static final String PROP_LABEL = "label";
	public static final String PROP_ID = "id";
	public static final String PROP_LOCALES = "locales";
	public static final String PROP_SCORE = "score";
	public static final String PROP_LOCALE = "locale";
	public static final String PROP_ENTITY_TYPE = "entityType";
	public static final String PROP_COLOR = "color";
	public static final String PROP_ENTITY_FILTER = "entityFilter";
	public static final String PROP_OPERATOR = "operator";
	public static final String PROP_FORMULA = "formula";
	public static final String PROP_CATALOG_MODIFIED_DATE_FIELD = "modifiedDateField";
	public static final String PROP_CATALOG_PUBLISHED_DATE_FIELD = "publishedDateField";
	public static final String PROP_AUDITED_FIELDS = "auditedFields";
	public static final String CATALOGS_PATH = "/app:company_home/cm:System/cm:PropertyCatalogs";
	public static final String CATALOG_DEFS = "CATALOG_DEFS";


	public static boolean isMatcheEntityType(JSONObject catalog, QName productType, NamespaceService namespaceService) throws JSONException {
		JSONArray catalogEntityTypes = (catalog.has(EntityCatalogHelper.PROP_ENTITY_TYPE)) ? catalog.getJSONArray(EntityCatalogHelper.PROP_ENTITY_TYPE)
				: new JSONArray();

		for (int i = 0; i < catalogEntityTypes.length(); ++i) {
			QName qname = QName.createQName(catalogEntityTypes.getString(i), namespaceService);
			if(qname.isMatch(productType)) {
				return true;
			}
		}

		return false;
	}

	
	public static Set<String> getLocales(List<String> reportLocales, JSONObject catalog) throws JSONException {
		Set<String> langs = new HashSet<>();

		if (catalog.has(EntityCatalogHelper.PROP_LOCALES)) {
			JSONArray catalogLocales = catalog.getJSONArray(EntityCatalogHelper.PROP_LOCALES);
			for (int j = 0; j < catalogLocales.length(); j++) {
				langs.add(catalogLocales.getString(j));
			}
		} else if (reportLocales != null) {
			langs.addAll(reportLocales);
		}

		if (langs.isEmpty()) {
			// put system locale
			langs.add(Locale.getDefault().getLanguage());
		}

		return langs;
	}
	

	public static Set<QName> getAuditedFields(JSONObject catalog, NamespaceService namespaceService) throws JSONException {
		Set<QName> ret = new HashSet<>();
		
		if (catalog.has(EntityCatalogHelper.PROP_AUDITED_FIELDS)) {
			JSONArray monitoredFields = catalog.getJSONArray(EntityCatalogHelper.PROP_AUDITED_FIELDS);
			for (int i = 0; i < monitoredFields.length(); i++) {
				String field = monitoredFields.getString(i);
				QName propQName = QName.createQName(field, namespaceService);
				ret.add(propQName);
			}
		}
		
		return ret;
	}
	
}
