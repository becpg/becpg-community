package fr.becpg.repo.entity.catalog;

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>EntityCatalogService interface.</p>
 *
 * @author matthieu
 */
public interface EntityCatalogService {
	
	/** Constant <code>PROP_CATALOGS="catalogs"</code> */
	public static final String PROP_CATALOGS = "catalogs";
	/** Constant <code>PROP_MISSING_FIELDS="missingFields"</code> */
	public static final String PROP_MISSING_FIELDS = "missingFields";
	/** Constant <code>PROP_UNIQUE_FIELDS="uniqueFields"</code> */
	public static final String PROP_UNIQUE_FIELDS = "uniqueFields";
	/** Constant <code>PROP_NON_UNIQUE_FIELDS="nonUniqueFields"</code> */
	public static final String PROP_NON_UNIQUE_FIELDS = "nonUniqueFields";
	/** Constant <code>PROP_I18N_MESSAGES="i18nMessages"</code> */
	public static final String PROP_I18N_MESSAGES = "i18nMessages";
	/** Constant <code>PROP_DISPLAY_NAME="displayName"</code> */
	public static final String PROP_DISPLAY_NAME = "displayName";
	/** Constant <code>PROP_FIELDS="fields"</code> */
	public static final String PROP_FIELDS = "fields";
	/** Constant <code>PROP_LABEL="label"</code> */
	public static final String PROP_LABEL = "label";
	/** Constant <code>PROP_ID="id"</code> */
	public static final String PROP_ID = "id";
	/** Constant <code>PROP_LOCALES="locales"</code> */
	public static final String PROP_LOCALES = "locales";
	/** Constant <code>PROP_SCORE="score"</code> */
	public static final String PROP_SCORE = "score";
	/** Constant <code>PROP_LOCALE="locale"</code> */
	public static final String PROP_LOCALE = "locale";
	/** Constant <code>PROP_ENTITY_TYPE="entityType"</code> */
	public static final String PROP_ENTITY_TYPE = "entityType";
	/** Constant <code>PROP_COLOR="color"</code> */
	public static final String PROP_COLOR = "color";
	/** Constant <code>PROP_ENTITY_FILTER="entityFilter"</code> */
	public static final String PROP_ENTITY_FILTER = "entityFilter";
	/** Constant <code>PROP_CATALOG_MODIFIED_DATE="modifiedDate"</code> */
	public static final String PROP_CATALOG_MODIFIED_DATE = "modifiedDate";
	/** Constant <code>PROP_CATALOG_MODIFIED_FIELD="modifiedField"</code> */
	public static final String PROP_CATALOG_MODIFIED_FIELD = "modifiedField";
	/** Constant <code>PROP_AUDITED_FIELDS="auditedFields"</code> */
	public static final String PROP_AUDITED_FIELDS = "auditedFields";
	/** Constant <code>PROP_VALUE="value"</code> */
	public static final String PROP_VALUE = "value";
	/** Constant <code>PROP_ENTITIES="entities"</code> */
	public static final String PROP_ENTITIES = "entities";

	/** Constant <code>CATALOG_DEFS="CATALOG_DEFS"</code> */
	public static final String CATALOG_DEFS = "CATALOG_DEFS";

	/** Constant <code>MESSAGE_OR="message.formulate.or"</code> */
	public static final String MESSAGE_OR = "message.formulate.or";

	/**
	 * <p>formulateCatalogs.</p>
	 *
	 * @param formulatedEntity a {@link fr.becpg.repo.repository.RepositoryEntity} object
	 * @param locales a {@link java.util.List} object
	 * @return a {@link org.json.JSONArray} object
	 * @throws org.json.JSONException if any.
	 */
	JSONArray formulateCatalogs(RepositoryEntity formulatedEntity, List<String> locales) throws JSONException;

	/**
	 * <p>formulateCatalog.</p>
	 *
	 * @param catalogId a {@link java.lang.String} object
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param locales a {@link java.util.List} object
	 * @return a {@link org.json.JSONArray} object
	 * @throws org.json.JSONException if any.
	 */
	JSONArray formulateCatalog(String catalogId, NodeRef entityNodeRef, List<String> locales) throws JSONException;

	/**
	 * <p>updateAuditedField.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param diffQnames a {@link java.util.Set} object
	 * @param listNodeRefs a {@link java.util.Set} object
	 */
	void updateAuditedField(NodeRef entityNodeRef, Set<QName> diffQnames, Set<NodeRef> listNodeRefs);

}
