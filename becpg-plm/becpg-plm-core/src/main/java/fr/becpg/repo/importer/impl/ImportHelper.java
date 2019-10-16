/*
 *
 */
package fr.becpg.repo.importer.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.alfresco.encoding.CharactersetFinder;
import org.alfresco.encoding.GuessEncodingCharsetFinder;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Helper used by the import classes.
 *
 * @author querephi
 */
public class ImportHelper {

	public static final String PFX_COMMENT = "#";

	public static final String PFX_MAPPING = "MAPPING";

	public static final String PFX_COLUMNS_PARAMS = "COLUMNS_PARAMS";

	public static final String PFX_PATH = "PATH";

	public static final String PFX_TYPE = "TYPE";

	public static final String PFX_LIST_TYPE = "LIST_TYPE";

	public static final String PFX_ENTITY_TYPE = "ENTITY_TYPE";

	public static final String PFX_STOP_ON_FIRST_ERROR = "STOP_ON_FIRST_ERROR";

	public static final String PFX_DELETE_DATALIST = "DELETE_DATALIST";

	public static final String PFX_COLUMS = "COLUMNS";

	public static final String PFX_VALUES = "VALUES";

	public static final String PFX_IMPORT_TYPE = "IMPORT_TYPE";

	public static final String PFX_DOCS_BASE_PATH = "DOCS_BASE_PATH";

	public static final String PFX_DISABLED_POLICIES = "DISABLED_POLICIES";

	public static final String QUERY_XPATH_MAPPING = "mapping";

	public static final String QUERY_XPATH_DATE_FORMAT = "settings/setting[@id='dateFormat']/@value";

	public static final String QUERY_XPATH_DATETIME_FORMAT = "settings/setting[@id='datetimeFormat']/@value";

	public static final String QUERY_XPATH_DECIMAL_PATTERN = "settings/setting[@id='decimalPattern']/@value";

	public static final String QUERY_XPATH_NODE_COLUMN_KEY = "nodeColumnKeys/nodeColumnKey";

	public static final String QUERY_XPATH_DATALIST_COLUMN_KEY = "dataListColumnKeys/dataListColumnKey";

	public static final String QUERY_XPATH_COLUMNS_ATTRIBUTE = "columns/column[@type='Attribute']";

	public static final String QUERY_XPATH_COLUMNS_FORMULA = "columns/column[@type='Formula']";

	public static final String QUERY_XPATH_COLUMNS_MLTEXT = "columns/column[@type='MLText']";

	public static final String QUERY_XPATH_COLUMNS_DATALIST = "columns/column[@type='Characteristic']";

	public static final String QUERY_XPATH_COLUMNS_FILE = "columns/column[@type='File']";

	public static final String QUERY_ATTR_GET_ID = "@id";

	public static final String QUERY_ATTR_GET_ATTRIBUTE = "@attribute";

	public static final String QUERY_ATTR_GET_TARGET_CLASS = "@targetClass";

	public static final String QUERY_ATTR_GET_NAME = "@name";

	public static final String QUERY_ATTR_GET_DATALIST_QNAME = "@dataListQName";

	public static final String QUERY_ATTR_GET_PATH = "@path";

	public static final String QUERY_ATTR_GET_CHARACT_QNAME = "@charactQName";

	public static final String QUERY_ATTR_GET_CHARACT_NODE_REF = "@charactNodeRef";

	public static final String QUERY_ATTR_GET_CHARACT_NAME = "@charactName";

	public static final String QUERY_ATTR_GET_CHARACT_KEY_QNAME = "@charactKeyName";

	public static final String MSG_ERROR_LOAD_FILE = "import_service.error.err_load_file";

	public static final String MSG_ERROR_FILE_NOT_FOUND = "import_service.error.err_file_not_found";

	public static final String MSG_ERROR_FILE_BAD_PREFIX = "import_service.error.err_file_bad_prefix";

	public static final String MSG_ERROR_MAPPING_ATTR_FAILED = "import_service.error.err_mapping_attr_failed";
	
	public static final String MSG_ERROR_MAPPING_ANNOTATION_NOT_FOUND = "import_service.error.err_mapping_annotation_not_found";

	public static final String MSG_ERROR_GET_OR_CREATE_NODEREF = "import_service.error.err_get_or_create_noderef";

	public static final String MSG_ERROR_GET_NODEREF_CHARACT = "import_service.error.err_get_noderef_charact";

	public static final String MSG_ERROR_UNDEFINED_CHARACT = "import_service.error.err_undefined_charact";

	public static final String MSG_ERROR_COLUMNS_DO_NOT_RESPECT_MAPPING = "import_service.error.err_columns_do_not_respect_mapping";

	public static final String MSG_ERROR_TARGET_ASSOC_NOT_FOUND = "import_service.error.err_target_assoc_not_found";

	public static final String MSG_ERROR_TARGET_ASSOC_SEVERAL = "import_service.error.err_target_assoc_several";

	public static final String MSG_ERROR_GET_ASSOC_TARGET = "import_service.error.err_get_assoc_target";

	public static final String MSG_ERROR_NO_DOCS_BASE_PATH_SET = "import_service.error.err_no_docs_base_path_set";

	public static final String MSG_ERROR_NO_PARENT = "import_service.error.err_no_parent";

	public static final String QUERY_XPATH_MAPPING_NODE = "mappings/mapping[@name=";

	public static final String QUERY_XPATH_COLUMNS_HIERARCHY = "columns/column[@type='Hierarchy']";

	public static final String QUERY_ATTR_GET_PARENT_LEVEL_ATTRIBUTE = "@parentLevelAttribute";

	public static final String QUERY_ATTR_GET_PARENT_LEVEL = "@parentLevel";
	
	
	/** The Constant MLTEXT_SEPARATOR. */
	public static final String MLTEXT_SEPARATOR = "_";

	public static final String NULL_VALUE = "NULL";

	/**
	 * Load the property according to the property type.
	 *
	 * @param importContext
	 *            the import context
	 * @param values
	 *            the values
	 * @param pos
	 *            the pos
	 * @return the serializable
	 * @throws ParseException
	 *             the parse exception
	 */
	public static Serializable loadPropertyValue(ImportContext importContext, List<String> values, int pos) throws ParseException {

		Serializable value = null;
		ClassAttributeDefinition attribute = importContext.getColumns().get(pos).getAttribute();

		if (attribute != null) {

			if (attribute instanceof PropertyDefinition) {

				PropertyDefinition propertyDef = (PropertyDefinition) attribute;
				QName qName = propertyDef.getName();
				QName dataType = propertyDef.getDataType().getName();

				if (NULL_VALUE.equalsIgnoreCase(values.get(pos))) {
					return NULL_VALUE;
				}

				// MLText
				if (dataType.isMatch(DataTypeDefinition.MLTEXT)) {

					MLText mlText = new MLText();

					// load translations
					boolean first = true;
					for (int z_idx = pos; z_idx < importContext.getColumns().size(); z_idx++) {

						// bcpg:legalName_en
						String transColumn = importContext.getColumns().get(z_idx).getId();
						if (!Objects.equals(transColumn, "")) {

							String transLocalName = transColumn.contains(RepoConsts.MODEL_PREFIX_SEPARATOR)
									? transColumn.split(RepoConsts.MODEL_PREFIX_SEPARATOR)[1]
									: null;
							// default locale
							if (first) {
								mlText.addValue(I18NUtil.getContentLocaleLang(), values.get(z_idx));
								first = false;
							}
							// other locales
							else if ((transLocalName != null) && transLocalName.startsWith(qName.getLocalName() + MLTEXT_SEPARATOR)) {

								String strLocale = transLocalName.replace(qName.getLocalName() + MLTEXT_SEPARATOR, "");
								Locale locale = MLTextHelper.parseLocale(strLocale);
								mlText.addValue(locale, values.get(z_idx));
							} else {
								// the translation is finished
								break;
							}
						}
					}

					value = mlText;
				}
				// Text
				else if (dataType.isMatch(DataTypeDefinition.TEXT)) {

					if (propertyDef.isMultiValued()) {
						// Multi-valued property
						value = new ArrayList<Serializable>(Arrays.asList((values.get(pos)).split(RepoConsts.MULTI_VALUES_SEPARATOR)));
					} else {
						// Single value property
						value = values.get(pos);
					}

					// clean name
					if (qName.getLocalName().equals(ContentModel.PROP_NAME.getLocalName())) {
						value = PropertiesHelper.cleanName((String) value);
					}
				}
				// Date
				else if (dataType.isMatch(DataTypeDefinition.DATE) || dataType.isMatch(DataTypeDefinition.DATETIME)) {

					if (values.get(pos).isEmpty()) {
						value = null;
					} else {
						value = importContext.getPropertyFormats().parseDate(values.get(pos));
					}
				}
				// int, long
				else if (dataType.isMatch(DataTypeDefinition.INT) || dataType.isMatch(DataTypeDefinition.LONG)) {

					if (values.get(pos).isEmpty()) {
						value = null;
					} else {
						value = importContext.getPropertyFormats().parseDecimal(values.get(pos)).longValue();
					}
				}
				// double
				else if (dataType.isMatch(DataTypeDefinition.DOUBLE)) {

					if (values.get(pos).trim().isEmpty()) {
						value = null;
					} else {
						value = parseNumber(importContext, values.get(pos)).doubleValue();
					}
				}
				// float
				else if (dataType.isMatch(DataTypeDefinition.FLOAT)) {

					if (values.get(pos).trim().isEmpty()) {
						value = null;
					} else {

						value = parseNumber(importContext, values.get(pos)).floatValue();
					}
				} else if (dataType.isMatch(DataTypeDefinition.ANY)) {

					if (values.get(pos).isEmpty()) {
						value = null;
					} else {
						// Try double
						try {
							value = parseNumber(importContext, values.get(pos)).doubleValue();
						} catch (ParseException e1) {
							// Try date
							try {
								value = importContext.getPropertyFormats().parseDate(values.get(pos));
							} catch (ParseException e2) {
								value = values.get(pos);
							}
						}
					}

				} else {
					value = values.get(pos);
				}
			}
		}

		return value;
	}

	private static Number parseNumber(ImportContext importContext, String val) throws ParseException {
		if (importContext.getPropertyFormats().getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator() == ',') {
			val = val.replaceAll("\\.", ",");
		} else {
			val = val.replaceAll(",", ".");
		}
		return importContext.getPropertyFormats().parseDecimal(val);

	}

	public static Serializable mergeMLText(MLText value, MLText currentValue) {
		if (value != null) {
			if (currentValue != null) {
				for (Locale loc : value.getLocales()) {
					if (ImportHelper.NULL_VALUE.equals(value.get(loc))) {
						currentValue.remove(loc);
					} else {

						currentValue.put(loc, value.get(loc));
					}
				}
				return currentValue;
			}
			return value;
		}
		return null;
	}

	public static Charset guestCharset(InputStream is, String readerCharset) {
		Charset defaultCharset = Charset.forName(RepoConsts.ISO_CHARSET);
		if (RepoConsts.ISO_CHARSET.equals(readerCharset)) {
			return defaultCharset;
		}
		CharactersetFinder finder = new GuessEncodingCharsetFinder();
		Charset charset = finder.detectCharset(is);
		if (charset == null) {
			return defaultCharset;
		}
		return charset;
	}

	public static Map<QName, Serializable> cleanProperties(Map<QName, Serializable> properties) {
		for (Iterator<Map.Entry<QName, Serializable>> iterator = properties.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<QName, Serializable> entry = iterator.next();
			if ((entry.getValue() != null) && ImportHelper.NULL_VALUE.equals(entry.getValue())) {
				iterator.remove();
			} else if ((entry.getValue() != null) && (entry.getValue() instanceof MLText)) {
				for (Locale loc : ((MLText) entry.getValue()).getLocales()) {
					if (ImportHelper.NULL_VALUE.equals(((MLText) entry.getValue()).get(loc))) {
						((MLText) entry.getValue()).remove(loc);
					}
				}

			}
		}

		return properties;
	}
	
	public static NodeRef findCharact(QName type, QName property, String name, NodeService nodeService) {

		for (NodeRef tmpNodeRef : BeCPGQueryBuilder.createQuery().ofType(type).andPropEquals(property, name).inDB().ftsLanguage()
				.list()) {
			if (!nodeService.hasAspect(tmpNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
					&& !nodeService.hasAspect(tmpNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
				return tmpNodeRef;
			}
		}
		return null;
	}


	

}
