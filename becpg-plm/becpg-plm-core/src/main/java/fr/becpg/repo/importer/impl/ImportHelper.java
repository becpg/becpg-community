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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * @version $Id: $Id
 */
public class ImportHelper {

	/** Constant <code>PFX_COMMENT="#"</code> */
	public static final String PFX_COMMENT = "#";

	/** Constant <code>PFX_MAPPING="MAPPING"</code> */
	public static final String PFX_MAPPING = "MAPPING";

	/** Constant <code>PFX_COLUMNS_PARAMS="COLUMNS_PARAMS"</code> */
	public static final String PFX_COLUMNS_PARAMS = "COLUMNS_PARAMS";

	/** Constant <code>PFX_PATH="PATH"</code> */
	public static final String PFX_PATH = "PATH";

	/** Constant <code>PFX_TYPE="TYPE"</code> */
	public static final String PFX_TYPE = "TYPE";

	/** Constant <code>PFX_LIST_TYPE="LIST_TYPE"</code> */
	public static final String PFX_LIST_TYPE = "LIST_TYPE";

	/** Constant <code>PFX_ENTITY_TYPE="ENTITY_TYPE"</code> */
	public static final String PFX_ENTITY_TYPE = "ENTITY_TYPE";

	/** Constant <code>PFX_STOP_ON_FIRST_ERROR="STOP_ON_FIRST_ERROR"</code> */
	public static final String PFX_STOP_ON_FIRST_ERROR = "STOP_ON_FIRST_ERROR";

	/** Constant <code>PFX_DELETE_DATALIST="DELETE_DATALIST"</code> */
	public static final String PFX_DELETE_DATALIST = "DELETE_DATALIST";

	/** Constant <code>PFX_COLUMS="COLUMNS"</code> */
	public static final String PFX_COLUMS = "COLUMNS";

	/** Constant <code>PFX_VALUES="VALUES"</code> */
	public static final String PFX_VALUES = "VALUES";

	/** Constant <code>PFX_IMPORT_TYPE="IMPORT_TYPE"</code> */
	public static final String PFX_IMPORT_TYPE = "IMPORT_TYPE";

	/** Constant <code>PFX_DOCS_BASE_PATH="DOCS_BASE_PATH"</code> */
	public static final String PFX_DOCS_BASE_PATH = "DOCS_BASE_PATH";

	/** Constant <code>PFX_DISABLED_POLICIES="DISABLED_POLICIES"</code> */
	public static final String PFX_DISABLED_POLICIES = "DISABLED_POLICIES";

	/** Constant <code>QUERY_XPATH_MAPPING="mapping"</code> */
	public static final String QUERY_XPATH_MAPPING = "mapping";

	/** Constant <code>QUERY_XPATH_DATE_FORMAT="settings/setting[@id='dateFormat']/@val"{trunked}</code> */
	public static final String QUERY_XPATH_DATE_FORMAT = "settings/setting[@id='dateFormat']/@value";

	/** Constant <code>QUERY_XPATH_DATETIME_FORMAT="settings/setting[@id='datetimeFormat']/"{trunked}</code> */
	public static final String QUERY_XPATH_DATETIME_FORMAT = "settings/setting[@id='datetimeFormat']/@value";

	/** Constant <code>QUERY_XPATH_DECIMAL_PATTERN="settings/setting[@id='decimalPattern']/"{trunked}</code> */
	public static final String QUERY_XPATH_DECIMAL_PATTERN = "settings/setting[@id='decimalPattern']/@value";

	/** Constant <code>QUERY_XPATH_NODE_COLUMN_KEY="nodeColumnKeys/nodeColumnKey"</code> */
	public static final String QUERY_XPATH_NODE_COLUMN_KEY = "nodeColumnKeys/nodeColumnKey";

	/** Constant <code>QUERY_XPATH_DATALIST_COLUMN_KEY="dataListColumnKeys/dataListColumnKey"</code> */
	public static final String QUERY_XPATH_DATALIST_COLUMN_KEY = "dataListColumnKeys/dataListColumnKey";

	/** Constant <code>QUERY_XPATH_COLUMNS_ATTRIBUTE="columns/column[@type='Attribute']"</code> */
	public static final String QUERY_XPATH_COLUMNS_ATTRIBUTE = "columns/column[@type='Attribute']";

	/** Constant <code>QUERY_XPATH_COLUMNS_FORMULA="columns/column[@type='Formula']"</code> */
	public static final String QUERY_XPATH_COLUMNS_FORMULA = "columns/column[@type='Formula']";

	/** Constant <code>QUERY_XPATH_COLUMNS_MLTEXT="columns/column[@type='MLText']"</code> */
	public static final String QUERY_XPATH_COLUMNS_MLTEXT = "columns/column[@type='MLText']";

	/** Constant <code>QUERY_XPATH_COLUMNS_DATALIST="columns/column[@type='Characteristic']"</code> */
	public static final String QUERY_XPATH_COLUMNS_DATALIST = "columns/column[@type='Characteristic']";

	/** Constant <code>QUERY_XPATH_COLUMNS_FILE="columns/column[@type='File']"</code> */
	public static final String QUERY_XPATH_COLUMNS_FILE = "columns/column[@type='File']";

	/** Constant <code>QUERY_ATTR_GET_ID="@id"</code> */
	public static final String QUERY_ATTR_GET_ID = "@id";

	/** Constant <code>QUERY_ATTR_GET_ATTRIBUTE="@attribute"</code> */
	public static final String QUERY_ATTR_GET_ATTRIBUTE = "@attribute";

	/** Constant <code>QUERY_ATTR_GET_TARGET_CLASS="@targetClass"</code> */
	public static final String QUERY_ATTR_GET_TARGET_CLASS = "@targetClass";

	/** Constant <code>QUERY_ATTR_GET_NAME="@name"</code> */
	public static final String QUERY_ATTR_GET_NAME = "@name";

	/** Constant <code>QUERY_ATTR_GET_DATALIST_QNAME="@dataListQName"</code> */
	public static final String QUERY_ATTR_GET_DATALIST_QNAME = "@dataListQName";

	/** Constant <code>QUERY_ATTR_GET_PATH="@path"</code> */
	public static final String QUERY_ATTR_GET_PATH = "@path";

	/** Constant <code>QUERY_ATTR_GET_CHARACT_QNAME="@charactQName"</code> */
	public static final String QUERY_ATTR_GET_CHARACT_QNAME = "@charactQName";

	/** Constant <code>QUERY_ATTR_GET_CHARACT_NODE_REF="@charactNodeRef"</code> */
	public static final String QUERY_ATTR_GET_CHARACT_NODE_REF = "@charactNodeRef";

	/** Constant <code>QUERY_ATTR_GET_CHARACT_NAME="@charactName"</code> */
	public static final String QUERY_ATTR_GET_CHARACT_NAME = "@charactName";

	/** Constant <code>QUERY_ATTR_GET_CHARACT_KEY_QNAME="@charactKeyName"</code> */
	public static final String QUERY_ATTR_GET_CHARACT_KEY_QNAME = "@charactKeyName";

	/** Constant <code>MSG_ERROR_LOAD_FILE="import_service.error.err_load_file"</code> */
	public static final String MSG_ERROR_LOAD_FILE = "import_service.error.err_load_file";

	/** Constant <code>MSG_ERROR_FILE_NOT_FOUND="import_service.error.err_file_not_found"</code> */
	public static final String MSG_ERROR_FILE_NOT_FOUND = "import_service.error.err_file_not_found";

	/** Constant <code>MSG_ERROR_FILE_BAD_PREFIX="import_service.error.err_file_bad_prefi"{trunked}</code> */
	public static final String MSG_ERROR_FILE_BAD_PREFIX = "import_service.error.err_file_bad_prefix";

	/** Constant <code>MSG_ERROR_MAPPING_ATTR_FAILED="import_service.error.err_mapping_attr_f"{trunked}</code> */
	public static final String MSG_ERROR_MAPPING_ATTR_FAILED = "import_service.error.err_mapping_attr_failed";
	
	/** Constant <code>MSG_ERROR_MAPPING_ANNOTATION_NOT_FOUND="import_service.error.err_mapping_annota"{trunked}</code> */
	public static final String MSG_ERROR_MAPPING_ANNOTATION_NOT_FOUND = "import_service.error.err_mapping_annotation_not_found";

	/** Constant <code>MSG_ERROR_GET_OR_CREATE_NODEREF="import_service.error.err_get_or_create_"{trunked}</code> */
	public static final String MSG_ERROR_GET_OR_CREATE_NODEREF = "import_service.error.err_get_or_create_noderef";

	/** Constant <code>MSG_ERROR_GET_NODEREF_CHARACT="import_service.error.err_get_noderef_ch"{trunked}</code> */
	public static final String MSG_ERROR_GET_NODEREF_CHARACT = "import_service.error.err_get_noderef_charact";

	/** Constant <code>MSG_ERROR_UNDEFINED_CHARACT="import_service.error.err_undefined_char"{trunked}</code> */
	public static final String MSG_ERROR_UNDEFINED_CHARACT = "import_service.error.err_undefined_charact";

	/** Constant <code>MSG_ERROR_COLUMNS_DO_NOT_RESPECT_MAPPING="import_service.error.err_columns_do_not"{trunked}</code> */
	public static final String MSG_ERROR_COLUMNS_DO_NOT_RESPECT_MAPPING = "import_service.error.err_columns_do_not_respect_mapping";

	/** Constant <code>MSG_ERROR_TARGET_ASSOC_NOT_FOUND="import_service.error.err_target_assoc_n"{trunked}</code> */
	public static final String MSG_ERROR_TARGET_ASSOC_NOT_FOUND = "import_service.error.err_target_assoc_not_found";

	/** Constant <code>MSG_ERROR_TARGET_ASSOC_SEVERAL="import_service.error.err_target_assoc_s"{trunked}</code> */
	public static final String MSG_ERROR_TARGET_ASSOC_SEVERAL = "import_service.error.err_target_assoc_several";

	/** Constant <code>MSG_ERROR_GET_ASSOC_TARGET="import_service.error.err_get_assoc_targ"{trunked}</code> */
	public static final String MSG_ERROR_GET_ASSOC_TARGET = "import_service.error.err_get_assoc_target";

	/** Constant <code>MSG_ERROR_NO_DOCS_BASE_PATH_SET="import_service.error.err_no_docs_base_p"{trunked}</code> */
	public static final String MSG_ERROR_NO_DOCS_BASE_PATH_SET = "import_service.error.err_no_docs_base_path_set";

	/** Constant <code>MSG_ERROR_NO_PARENT="import_service.error.err_no_parent"</code> */
	public static final String MSG_ERROR_NO_PARENT = "import_service.error.err_no_parent";
	
	/** Constant <code>MSG_ERROR_FIELD_TYPE="import_service.error.err_field_type"</code> */
	public static final String MSG_ERROR_FIELD_TYPE = "import_service.error.err_field_type";

	/** Constant <code>QUERY_XPATH_MAPPING_NODE="mappings/mapping[@name="</code> */
	public static final String QUERY_XPATH_MAPPING_NODE = "mappings/mapping[@name=";

	/** Constant <code>QUERY_XPATH_COLUMNS_HIERARCHY="columns/column[@type='Hierarchy']"</code> */
	public static final String QUERY_XPATH_COLUMNS_HIERARCHY = "columns/column[@type='Hierarchy']";

	/** Constant <code>QUERY_ATTR_GET_PARENT_LEVEL_ATTRIBUTE="@parentLevelAttribute"</code> */
	public static final String QUERY_ATTR_GET_PARENT_LEVEL_ATTRIBUTE = "@parentLevelAttribute";

	/** Constant <code>QUERY_ATTR_GET_PARENT_LEVEL="@parentLevel"</code> */
	public static final String QUERY_ATTR_GET_PARENT_LEVEL = "@parentLevel";
	
	
	/** The Constant MLTEXT_SEPARATOR. */
	public static final String MLTEXT_SEPARATOR = "_";

	/** Constant <code>NULL_VALUE="NULL"</code> */
	public static final String NULL_VALUE = "NULL";
	
	private ImportHelper() {
		//Do Nothing
	}

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
	 * @throws java.text.ParseException if any.
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
					boolean currentLocaleUnset = true;
					for (int z_idx = pos; z_idx < importContext.getColumns().size() && z_idx < values.size(); z_idx++) {

						// bcpg:legalName_en
						String transColumn = importContext.getColumns().get(z_idx).getId();
						if (!Objects.equals(transColumn, "")) {

							String transLocalName = transColumn.contains(RepoConsts.MODEL_PREFIX_SEPARATOR)
									? transColumn.split(RepoConsts.MODEL_PREFIX_SEPARATOR)[1]
									: null;
							
							if ((transLocalName != null) && transLocalName.startsWith(qName.getLocalName() + MLTEXT_SEPARATOR)) {

								String strLocale = transLocalName.replace(qName.getLocalName() + MLTEXT_SEPARATOR, "");
								Locale locale = MLTextHelper.parseLocale(strLocale);
								if(values.get(z_idx) == null || values.get(z_idx).isBlank()) {
									mlText.removeValue(locale);
								} else 	if (MLTextHelper.isSupportedLocale(locale)) {
									mlText.addValue(locale, values.get(z_idx));
								} else {
									throw new IllegalStateException("Unsupported locale : "+locale);
								}
							} else if (currentLocaleUnset) {
								mlText.addValue(I18NUtil.getContentLocaleLang(), values.get(z_idx));
								currentLocaleUnset = false;
							} else {
								// the translation is finished
								break;
							}
						}
					}
					
					if (currentLocaleUnset) {
						mlText.addValue(I18NUtil.getContentLocaleLang(), "");
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
				else if (dataType.isMatch(DataTypeDefinition.DATE)) {

					if (values.get(pos).isEmpty()) {
						value = null;
					} else {
						try {
							value = importContext.getPropertyFormats().parseDate(values.get(pos));
						} catch (ParseException e) {
							value = importContext.getPropertyFormats().parseDateTime(values.get(pos));
						}
					}
				}
				// Datetime
				else if (dataType.isMatch(DataTypeDefinition.DATETIME)) {
					
					if (values.get(pos).isEmpty()) {
						value = null;
					} else {
						try {
							value = importContext.getPropertyFormats().parseDateTime(values.get(pos));
						} catch (ParseException e) {
							value = importContext.getPropertyFormats().parseDate(values.get(pos));
						}
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
	
	private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?[0-9\\s.,]+$");

	/**
	 * <p>parseNumber.</p>
	 *
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object
	 * @param val a {@link java.lang.String} object
	 * @return a {@link java.lang.Number} object
	 * @throws java.text.ParseException if any.
	 */
	public static Number parseNumber(ImportContext importContext, String val) throws ParseException {
		
	    val = val.replaceAll("\\s", "").replace("\u202F", "");
		Matcher m = NUMBER_PATTERN.matcher(val);
	    if (!m.find()) {
	    	throw new ParseException("Not a number",0);
	    }
		
		if(importContext.getImportFileReader() instanceof ImportCSVFileReader) {
			if (importContext.getPropertyFormats().getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator() == ',') {
				val = val.replace("\\.", ",");
			} else {
				val = val.replace(",", ".");
			}
		}
		return importContext.getPropertyFormats().parseDecimal(val);

	}

	/**
	 * <p>mergeMLText.</p>
	 *
	 * @param value a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @param currentValue a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @return a {@link java.io.Serializable} object.
	 */
	public static Serializable mergeMLText(MLText value, MLText currentValue) {
		if (value != null) {
			if (currentValue != null) {
				for (Locale loc : value.getLocales()) {
					if (!(I18NUtil.getContentLocaleLang().equals(loc) && "".equals(value.get(loc)) && !currentValue.isEmpty())) {
						if (ImportHelper.NULL_VALUE.equals(value.get(loc))) {
							currentValue.remove(loc);
						} else {
							currentValue.put(loc, value.get(loc));
						}
					}
				}
				return currentValue;
			}
			return value;
		}
		return null;
	}

	/**
	 * <p>guestCharset.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @param readerCharset a {@link java.lang.String} object.
	 * @return a {@link java.nio.charset.Charset} object.
	 */
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

	/**
	 * <p>cleanProperties.</p>
	 *
	 * @param properties a {@link java.util.Map} object.
	 * @return a {@link java.util.Map} object.
	 */
	public static Map<QName, Serializable> cleanProperties(Map<QName, Serializable> properties) {
		for (Iterator<Map.Entry<QName, Serializable>> iterator = properties.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<QName, Serializable> entry = iterator.next();
			if ((entry.getValue() != null) && ImportHelper.NULL_VALUE.equals(entry.getValue())) {
				iterator.remove();
			} else if ( (entry.getValue() instanceof MLText)) {
				for (Locale loc : ((MLText) entry.getValue()).getLocales()) {
					if (ImportHelper.NULL_VALUE.equals(((MLText) entry.getValue()).get(loc))) {
						((MLText) entry.getValue()).remove(loc);
					}
				}

			}  else if (ContentModel.PROP_CONTENT.equals(entry.getKey())) {
				iterator.remove();
			}
		}

		return properties;
	}
	
	/**
	 * <p>findCharact.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param property a {@link org.alfresco.service.namespace.QName} object.
	 * @param name a {@link java.lang.String} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
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