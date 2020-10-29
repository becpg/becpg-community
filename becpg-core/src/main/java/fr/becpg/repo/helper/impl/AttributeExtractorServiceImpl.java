/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormatService;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.security.SecurityService;

/**
 * <p>
 * AttributeExtractorServiceImpl class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("attributeExtractorService")
public class AttributeExtractorServiceImpl implements AttributeExtractorService {

	private static final Log logger = LogFactory.getLog(AttributeExtractorServiceImpl.class);

	@Autowired
	@Qualifier("mlAwareNodeService")
	protected NodeService mlNodeService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private TaggingService taggingService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private AttributeExtractorPlugin[] attributeExtractorPlugins;

	@Autowired
	private PersonAttributeExtractorPlugin personAttributeExtractorPlugin;

	@Autowired
	private BeCPGCacheService beCPGCacheService;

	@Autowired
	private PropertyFormatService propertyFormatService;

	private AttributeExtractorPlugin getAttributeExtractorPlugin(QName type, NodeRef nodeRef) {

		Map<QName, AttributeExtractorPlugin> pluginsCache = beCPGCacheService.getFromCache(AttributeExtractorService.class.getName(), "PLUGINS_CACHE",
				() -> {
					Map<QName, AttributeExtractorPlugin> ret = new HashMap<>();

					Arrays.sort(attributeExtractorPlugins, (a, b) -> Integer.compare(a.getPriority(), b.getPriority()));

					for (AttributeExtractorPlugin plugin : attributeExtractorPlugins) {
						for (QName plugType : plugin.getMatchingTypes()) {
							ret.put(plugType, plugin);
						}
					}
					return ret;
				});

		return pluginsCache.get(type);
	}

	public class AttributeExtractorFilter {

		Map<String, String> criteriaMap = new HashMap<>();

		public AttributeExtractorFilter(String field, String filter) {
			criteriaMap.put(field, filter);
		}

		public Map<String, String> getCriteriaMap() {
			return criteriaMap;
		}

	}

	public class AttributeExtractorStructure {

		final String fieldName;
		boolean isEntityField = false;
		ClassAttributeDefinition fieldDef;
		List<AttributeExtractorStructure> childrens;
		AttributeExtractorFilter filter;
		Locale locale;
		QName fieldQname;
		QName itemType;
		String formula = null;

		public AttributeExtractorStructure(String fieldName, QName fieldQname, AttributeExtractorFilter filter, List<String> dLFields,
				QName itemType) {
			this.fieldName = fieldName;
			this.filter = filter;
			this.fieldQname = fieldQname;
			this.itemType = itemType;
			this.childrens = readExtractStructure(fieldQname, dLFields);
		}

		public AttributeExtractorStructure(String fieldName, QName fieldQname, boolean isEntityField, List<String> dLFields, QName itemType) {
			this.fieldName = fieldName;
			this.isEntityField = isEntityField;
			this.fieldQname = fieldQname;
			this.itemType = itemType;
			this.childrens = readExtractStructure(fieldQname, dLFields);
		}

		public AttributeExtractorStructure(String fieldName, QName fieldQname, ClassAttributeDefinition fieldDef, List<String> dLFields,
				QName itemType) {
			this.fieldName = fieldName;
			this.fieldDef = fieldDef;
			this.fieldQname = fieldDef.getName();
			this.itemType = itemType;
			this.childrens = readExtractStructure(fieldQname, dLFields);
		}

		public AttributeExtractorStructure(String fieldName, ClassAttributeDefinition fieldDef, QName itemType) {
			this.fieldDef = fieldDef;
			this.fieldQname = fieldDef.getName();
			this.fieldName = fieldName;
			this.itemType = itemType;
		}

		public AttributeExtractorStructure(String fieldName, ClassAttributeDefinition fieldDef, Locale locale, QName itemType) {
			this.fieldDef = fieldDef;
			this.fieldQname = fieldDef.getName();
			this.fieldName = fieldName;
			this.locale = locale;
			this.itemType = itemType;
		}

		public AttributeExtractorStructure(String fieldName, String formula) {
			this.fieldName = fieldName;
			this.formula = formula;
		}

		public String getFieldName() {
			return fieldName;
		}

		public boolean isEntityField() {
			return isEntityField;
		}

		public boolean isFormulaField() {
			return (formula != null) && !formula.isEmpty();
		}

		public boolean isDataListItems() {
			return isNested() && !isEntityField && (fieldDef == null);
		}

		public boolean isNested() {
			return (childrens != null) && !childrens.isEmpty();
		}

		public ClassAttributeDefinition getFieldDef() {
			return fieldDef;
		}

		public List<AttributeExtractorStructure> getChildrens() {
			return childrens;
		}

		public QName getFieldQname() {
			return fieldQname;
		}

		public QName getItemType() {
			return itemType;
		}

		public String getFormula() {
			return formula;
		}

		public AttributeExtractorFilter getFilter() {
			return filter;
		}

		public Locale getLocale() {
			return locale;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + getOuterType().hashCode();
			result = (prime * result) + ((fieldName == null) ? 0 : fieldName.hashCode());
			result = (prime * result) + ((fieldQname == null) ? 0 : fieldQname.hashCode());
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
			AttributeExtractorStructure other = (AttributeExtractorStructure) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (fieldName == null) {
				if (other.fieldName != null) {
					return false;
				}
			} else if (!fieldName.equals(other.fieldName)) {
				return false;
			}
			if (fieldQname == null) {
				if (other.fieldQname != null) {
					return false;
				}
			} else if (!fieldQname.equals(other.fieldQname)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "AttributeExtractorStructure [fieldName=" + fieldName + ", isEntityField=" + isEntityField + ", fieldDef=" + fieldDef
					+ ", childrens=" + childrens + ", filter=" + filter + ", locale=" + locale + ", fieldQname=" + fieldQname + ", itemType="
					+ itemType + ", formula=" + formula + "]";
		}

		private AttributeExtractorServiceImpl getOuterType() {
			return AttributeExtractorServiceImpl.this;
		}

	}

	/** {@inheritDoc} */
	@Override
	public String getStringValue(PropertyDefinition propertyDef, Serializable v, PropertyFormats propertyFormats) {
		return getStringValue(propertyDef, v, propertyFormats, true);
	}

	@SuppressWarnings("unchecked")
	private String getStringValue(PropertyDefinition propertyDef, Serializable v, PropertyFormats propertyFormats, boolean formatData) {

		String value = null;

		if ((v == null) || (propertyDef == null)) {
			return value;
		}

		String dataType = propertyDef.getDataType().toString();

		if (dataType.equals(DataTypeDefinition.ASSOC_REF.toString())) {
			value = extractPropName((NodeRef) v);
		} else if (dataType.equals(DataTypeDefinition.CATEGORY.toString())) {

			List<NodeRef> categories = (ArrayList<NodeRef>) v;

			for (NodeRef categoryNodeRef : categories) {
				if (value == null) {
					value = extractPropName(categoryNodeRef);
				} else {
					value += RepoConsts.LABEL_SEPARATOR + extractPropName(categoryNodeRef);
				}
			}
		} else if (dataType.equals(DataTypeDefinition.BOOLEAN.toString())
				|| (dataType.equals(DataTypeDefinition.ANY.toString()) && (v instanceof Boolean))) {

			Boolean b = (Boolean) v;

			value = TranslateHelper.getTranslatedBoolean(b, propertyFormats.isUseDefaultLocale());

		} else if (dataType.equals(DataTypeDefinition.TEXT.toString())) {

			String constraintName = null;
			DynListConstraint dynListConstraint = null;

			if (!propertyDef.getConstraints().isEmpty()) {

				for (ConstraintDefinition constraint : propertyDef.getConstraints()) {
					if (constraint.getConstraint() instanceof DynListConstraint) {
						dynListConstraint = (DynListConstraint) constraint.getConstraint();
						break;

					} else if ("LIST".equals(constraint.getConstraint().getType())) {
						constraintName = constraint.getRef().toPrefixString(namespaceService).replace(":", "_");
						break;
					}
				}

			}

			if (propertyDef.isMultiValued()) {
				List<String> values;

				if (v instanceof String) {
					values = Collections.singletonList((String) v);
				} else {
					values = (List<String>) v;
				}

				for (String tempValue : values) {
					if (tempValue != null) {
						if (value != null) {
							value += RepoConsts.LABEL_SEPARATOR;
						} else {
							value = "";
						}

						if (dynListConstraint != null) {
							if (!formatData) {
								value += tempValue;
							} else {
								value += dynListConstraint.getDisplayLabel(tempValue);
							}
						} else {
							if (!formatData) {
								value += tempValue;
							} else {

								value += constraintName != null
										? TranslateHelper.getConstraint(constraintName, tempValue, propertyFormats.isUseDefaultLocale())
										: tempValue;
							}
						}
					}

				}
			} else {

				if (SecurityModel.PROP_ACL_PROPNAME.equals(propertyDef.getName())) {
					QName aclPropName = QName.createQName(v.toString(), namespaceService);
					ClassAttributeDefinition aclDef = entityDictionaryService.getPropDef(aclPropName);
					if (aclDef != null) {
						value = aclDef.getTitle(dictionaryService);
					} else {
						value = v.toString();
					}

				} else {
					if (dynListConstraint != null) {
						value = dynListConstraint.getDisplayLabel(v.toString());
					} else {
						value = constraintName != null
								? TranslateHelper.getConstraint(constraintName, v.toString(), propertyFormats.isUseDefaultLocale())
								: v.toString();
					}
				}
			}

		} else if (dataType.equals(DataTypeDefinition.DATE.toString())) {
			value = propertyFormats.formatDate(v);
		} else if (dataType.equals(DataTypeDefinition.DATETIME.toString())) {
			value = propertyFormats.formatDateTime(v);
		} else if (dataType.equals(DataTypeDefinition.NODE_REF.toString())) {
			if (!propertyDef.isMultiValued()) {
				value = extractPropName((NodeRef) v);
			} else {
				List<NodeRef> values = (List<NodeRef>) v;
				if (values != null) {
					for (NodeRef tempValue : values) {
						if (tempValue != null) {
							if (value != null) {
								value += RepoConsts.LABEL_SEPARATOR;
							} else {
								value = "";
							}

							value += extractPropName(tempValue);
						}
					}
				}
			}

		} else if (dataType.equals(DataTypeDefinition.MLTEXT.toString())) {
			value = v.toString();
		} else if (dataType.equals(DataTypeDefinition.DOUBLE.toString()) || dataType.equals(DataTypeDefinition.FLOAT.toString())
				|| (dataType.equals(DataTypeDefinition.ANY.toString()) && ((v instanceof Double) || (v instanceof Float)))) {

			value = propertyFormats.formatDecimal(v);

		} else if (dataType.equals(DataTypeDefinition.QNAME.toString())) {

			if (v != null) {
				value = I18NUtil.getMessage("bcpg_bcpgmodel.type." + ((QName) v).toPrefixString(namespaceService).replace(":", "_") + ".title");
				if (value == null) {
					value = v.toString();
				}

			}
		}

		else {

			TypeConverter converter = new TypeConverter();
			value = converter.convert(propertyDef.getDataType(), v).toString();
		}
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public String extractPropertyForReport(PropertyDefinition propertyDef, Serializable value, boolean formatData) {
		PropertyFormats propertyFormats = getPropertyFormats(FormatMode.REPORT, false);

		return extractPropertyForReport(propertyDef, value, propertyFormats, formatData);
	}

	/** {@inheritDoc} */
	@Override
	public String extractPropertyForReport(PropertyDefinition propertyDef, Serializable value, PropertyFormats propertyFormats, boolean formatData) {

		if (value != null) {

			if ((value instanceof NodeRef) || (value instanceof String) || (value instanceof List)) {
				if (DataTypeDefinition.ANY.toString().equals(propertyDef.getDataType().toString()) && (value instanceof String)) {
					value = (Serializable) JsonFormulaHelper.cleanCompareJSON((String) value);
				}
				if (propertyDef.getConstraints().isEmpty() || (DataTypeDefinition.TEXT.toString().equals(propertyDef.getDataType().toString()))) {
					if (formatData || (value instanceof NodeRef) || (value instanceof List)) {
						return getStringValue(propertyDef, value, propertyFormats, formatData);
					} else {
						return value.toString();
					}
				} else {
					return value.toString();
				}
			} else if (value instanceof Date) {
				if (formatData) {
					return getStringValue(propertyDef, value, propertyFormats);
				} else {
					return ISO8601DateFormat.format((Date) value);
				}

			} else {
				if (formatData) {
					return getStringValue(propertyDef, value, propertyFormats);
				} else {
					return value.toString();
				}
			}
		} else {
			return "";
		}
	}

	/*
	 * formula| excel| entity|
	 *
	 * EXCEL -> bcpg:compoListProduct_bcpg:erpCode, attribute ->
	 * bcpg_compoListProduct|bcpg_erpCode
	 *
	 * bcpg:compoList[bcpg:compoListProduct_bcpg:erpCode ==
	 * "PF1"]|bcpg:compoListQty
	 * bcpg:allergenList[bcpg:allergenListAllergen_bcpg:allergenCode ==
	 * "FX1"]|bcpg:allergenListInVoluntary
	 * bcpg:allergenList[bcpg:allergenListAllergen#bcpg:allergenCode ==
	 * "FX1"]_bcpg:allergenListInVoluntary
	 * bcpg:allergenList[bcpg:allergenListAllergen#bcpg:allergenCode ==
	 * "FX1"]|bcpg:allergenListInVoluntary
	 * pack:packMaterialList[pack:pmlMaterial.startsWith("Autres matériaux")]
	 * _pack:pmlWeight
	 *
	 * -> bcpg_compoList[bcpg_compoListProduct|bcpg_erpCode=="PF1"]|
	 * bcpg_compoListQty
	 *
	 * Field bcpg_nutListMethod -> prop_bcpg_nutListMethod Assoc -> assoc_ dyn_
	 * DataListField
	 * bcpg_activityList|bcpg_alType|bcpg_alData|bcpg_alUserId|cm_created ->
	 * dt_bcpg_activityList Assocs Field
	 * bcpg_nutListNut|bcpg_nutGDA|bcpg_nutUL|bcpg_nutUnit -> dt_bcpg_nutListNut
	 */
	/** {@inheritDoc} */
	@Override
	public List<AttributeExtractorStructure> readExtractStructure(QName itemType, List<String> metadataFields) {
		List<AttributeExtractorStructure> ret = new LinkedList<>();

		int formulaCount = 0;

		for (String field : metadataFields) {
			Locale locale = null;
			if (field.contains("|")) {
				StringTokenizer tokeniser = new StringTokenizer(field, "|");
				String dlField = tokeniser.nextToken();

				if ("entity".equals(dlField)) {
					field = tokeniser.nextToken();
					QName fieldQname = QName.createQName(field, namespaceService);

					if (hasReadAccess(itemType, field)) {

						ClassAttributeDefinition prodDef = entityDictionaryService.getPropDef(fieldQname);
						if (prodDef != null) {
							String prefix = "entity_";
							ret.add(new AttributeExtractorStructure(prefix + field.replaceFirst(":", "_"), prodDef, itemType));
						}
					}

				} else if ("formula".equals(dlField)) {
					field = tokeniser.nextToken();
					ret.add(new AttributeExtractorStructure("formula_" + (formulaCount++), field));
				} else if ("excel".equals(dlField)) {
					field = tokeniser.nextToken();
					ret.add(new AttributeExtractorStructure("excel_" + (formulaCount++), field));
				} else {
					List<String> dLFields = new ArrayList<>();
					AttributeExtractorFilter dataListFilter = null;
					QName fieldQname = null;
					if (dlField.contains("[")) {
						Matcher maEqual = Pattern.compile("(.*)(\\[(.*)\\s*==\\s*\"(.*)\"\\])").matcher(dlField);
						if (maEqual.matches()) {
							fieldQname = QName.createQName(maEqual.group(1), namespaceService);
							dataListFilter = new AttributeExtractorFilter(maEqual.group(3).replaceAll("#", "|").trim(), maEqual.group(4).trim());
						} else {
							Matcher maStartsWith = Pattern.compile("(.*)(\\[(.*)\\.startsWith\\(\"(.*)\"\\)\\])").matcher(dlField);
							if (maStartsWith.matches()) {
								fieldQname = QName.createQName(maStartsWith.group(1), namespaceService);
								dataListFilter = new AttributeExtractorFilter(maStartsWith.group(3).replaceAll("#", "|").trim(),
										"^" + maStartsWith.group(4).trim());
							} else {
								logger.error("Cannot extract datalist filter: " + dlField);
							}
						}

					} else if (tokeniser.hasMoreTokens()) {
						String nextToken = tokeniser.nextToken();
						if ((MLTextHelper.getSupportedLocalesList() != null) && MLTextHelper.getSupportedLocalesList().contains(nextToken)) {
							locale = MLTextHelper.parseLocale(nextToken);
						} else {
							dLFields.add(nextToken);
						}
						fieldQname = QName.createQName(dlField, namespaceService);
					} else {
						fieldQname = QName.createQName(dlField, namespaceService);
					}
					while (tokeniser.hasMoreTokens()) {
						String nextToken = tokeniser.nextToken();
						if ((MLTextHelper.getSupportedLocalesList() != null) && MLTextHelper.getSupportedLocalesList().contains(nextToken)) {
							dLFields.set(dLFields.size() - 1, dLFields.get(dLFields.size() - 1) + "|" + nextToken);
						} else {
							dLFields.add(nextToken);
						}
					}

					if (entityDictionaryService.isSubClass(fieldQname, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
						ret.add(new AttributeExtractorStructure(DT_SUFFIX + dlField.replaceFirst(":", "_"), fieldQname, dataListFilter, dLFields,
								itemType));

					} else if (entityDictionaryService.isSubClass(fieldQname, BeCPGModel.TYPE_ENTITY_V2)) {
						ret.add(new AttributeExtractorStructure(DT_SUFFIX + dlField.replaceFirst(":", "_"), fieldQname, true, dLFields, itemType));
					} else {
						// nested assoc
						ClassAttributeDefinition propDef = entityDictionaryService.getPropDef(fieldQname);
						if (hasReadAccess(itemType, dlField)) {
							if (isAssoc(propDef)) {
								ret.add(new AttributeExtractorStructure(DT_SUFFIX + dlField.replaceFirst(":", "_"),
										((AssociationDefinition) propDef).getTargetClass().getName(), propDef, dLFields, itemType));
							} else if ((propDef != null) && (locale != null)) {
								String prefix = AttributeExtractorService.PROP_SUFFIX;
								ret.add(new AttributeExtractorStructure(prefix + dlField.replaceFirst(":", "_") + "_" + locale.toString(), propDef,
										locale, itemType));
							}
						}

					}
				}
			} else {

				QName fieldQname = QName.createQName(field, namespaceService);
				if (hasReadAccess(itemType, field)) {

					ClassAttributeDefinition prodDef = entityDictionaryService.getPropDef(fieldQname);

					if (field.startsWith("dyn_")) {
						ret.add(new AttributeExtractorStructure(field, field));
					}

					if (prodDef != null) {
						String prefix = AttributeExtractorService.PROP_SUFFIX;
						if (isAssoc(prodDef)) {
							prefix = AttributeExtractorService.ASSOC_SUFFIX;
						}
						ret.add(new AttributeExtractorStructure(prefix + field.replaceFirst(":", "_"), prodDef, itemType));
					}
				}
			}
		}
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, List<String> metadataFields, FormatMode mode) {
		return extractNodeData(nodeRef, itemType, nodeService.getProperties(nodeRef), readExtractStructure(itemType, metadataFields), mode,
				new DataListCallBack() {

					@Override
					public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field) {
						List<Map<String, Object>> ret = new ArrayList<>();

						if (field.getFieldDef() instanceof AssociationDefinition) {
							List<NodeRef> assocRefs;
							if (((AssociationDefinition) field.getFieldDef()).isChild()) {
								assocRefs = associationService.getChildAssocs(nodeRef, field.getFieldDef().getName());
							} else {
								assocRefs = associationService.getTargetAssocs(nodeRef, field.getFieldDef().getName());
							}
							for (NodeRef itemNodeRef : assocRefs) {
								addExtracted(itemNodeRef, field, new HashMap<>(), ret);
							}

						}
						return ret;
					}

					private void addExtracted(NodeRef itemNodeRef, AttributeExtractorStructure field, Map<NodeRef, Map<String, Object>> cache,
							List<Map<String, Object>> ret) {
						if (cache.containsKey(itemNodeRef)) {
							ret.add(cache.get(itemNodeRef));
						} else {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								QName itemType = nodeService.getType(itemNodeRef);
								Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
								ret.add(extractNodeData(itemNodeRef, itemType, properties, field.getChildrens(), mode, null));
							}
						}
					}
				});
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, Map<QName, Serializable> properties,
			List<AttributeExtractorStructure> metadataFields, FormatMode mode, AttributeExtractorService.DataListCallBack callback) {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		Map<String, Object> ret = new HashMap<>(metadataFields.size());

		Integer order = 0;

		for (AttributeExtractorStructure field : metadataFields) {
			if (field.isNested()) {
				List<Map<String, Object>> extracted = callback.extractNestedField(nodeRef, field);

				if ((FormatMode.CSV.equals(mode) || FormatMode.XLSX.equals(mode)) && !extracted.isEmpty()) {

					if (extracted.size() > 1) {
						Double value;
						for (Map<String, Object> extractedElt : extracted) {
							for (Map.Entry<String, Object> entry : extractedElt.entrySet()) {
								if ((entry.getValue() instanceof Number) && ret.containsKey(field.getFieldName() + "_" + entry.getKey())
										&& (ret.get(field.getFieldName() + "_" + entry.getKey()) instanceof Number)) {
									value = (Double) ret.get(field.getFieldName() + "_" + entry.getKey()) + (Double) entry.getValue();
									ret.put(field.getFieldName() + "_" + entry.getKey(), value);
								} else {
									ret.put(field.getFieldName() + "_" + entry.getKey(), entry.getValue());
								}
							}
						}

					} else {
						for (Map.Entry<String, Object> entry : extracted.get(0).entrySet()) {
							// Prefix with field name for CSV
							ret.put(field.getFieldName() + "_" + entry.getKey(), entry.getValue());
						}
					}
				} else {
					if (field.isEntityField() && !extracted.isEmpty()) {
						ret.put(field.getFieldName(), extracted.get(0));
					} else {
						ret.put(field.getFieldName(), extracted);
					}
				}
			} else if (!field.isFormulaField()) {
				ret.put(field.getFieldName(), extractNodeData(nodeRef, properties, field.getLocale(), getFieldDef(itemType, field), mode, order++));
			}

		}
		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug(getClass().getSimpleName() + " extract node data in  " + watch.getTotalTimeSeconds());
		}
		return ret;
	}

	private ClassAttributeDefinition getFieldDef(QName itemType, AttributeExtractorStructure field) {

		if (!field.getItemType().equals(itemType)) {
			return entityDictionaryService.findMatchingPropDef(field.getItemType(), itemType, field.getFieldQname());
		}
		return field.getFieldDef();
	}

	private boolean isAssoc(ClassAttributeDefinition propDef) {
		return propDef instanceof AssociationDefinition;
	}

	private Object extractNodeData(NodeRef nodeRef, Map<QName, Serializable> properties, Locale locale, ClassAttributeDefinition attribute,
			FormatMode mode, int order) {

		Serializable value;
		String displayName = "";
		QName type;

		// property
		if (attribute instanceof PropertyDefinition) {

			value = properties.get(attribute.getName());
			if (locale != null) {
				MLText mltext = (MLText) mlNodeService.getProperty(nodeRef, attribute.getName());
				if ((mltext != null) && mltext.containsKey(locale)) {
					displayName = mltext.get(locale);
				} else {
					displayName = "";
				}

			} else {
				displayName = getStringValue((PropertyDefinition) attribute, value, getPropertyFormats(mode, false));
			}

			if (FormatMode.CSV.equals(mode)) {
				return displayName;
			} else if (FormatMode.XLSX.equals(mode)) {
				if (ExcelHelper.isExcelType(value)) {
					return value;
				} else {
					// if
					// (DataTypeDefinition.ANY.toString().equals((((PropertyDefinition)
					// attribute).getDataType()).toString())
					// && value instanceof String) {
					// return JsonFormulaHelper.cleanCompareJSON((String)
					// value);
					// }
					return displayName;
				}

			} else {
				HashMap<String, Object> tmp = new HashMap<>(6);

				type = ((PropertyDefinition) attribute).getDataType().getName().getPrefixedQName(namespaceService);

				if (FormatMode.SEARCH.equals(mode)) {
					tmp.put("order", order);
					tmp.put("type", type);
					tmp.put("label", attribute.getTitle(dictionaryService));
				} else if (type != null) {
					if ((value != null) && type.equals(DataTypeDefinition.NODE_REF)) {
						String metadata = null;
						if (!((PropertyDefinition) attribute).isMultiValued()) {
							metadata = extractMetadata(nodeService.getType((NodeRef) value), (NodeRef) value);
						} else {
							@SuppressWarnings("unchecked")
							List<NodeRef> values = (List<NodeRef>) value;
							if (values != null) {
								for (NodeRef tempValue : values) {
									if (tempValue != null) {
										if (metadata != null) {
											metadata += ",";
										} else {
											metadata = "";
										}
										metadata += extractMetadata(nodeService.getType(tempValue), tempValue);
									}
								}
							}
						}

						tmp.put("metadata", metadata);
					} else {
						tmp.put("metadata", extractMetadata(type, nodeRef));
					}
				}
				if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
					tmp.put("version", properties.get(BeCPGModel.PROP_VERSION_LABEL));
				} else if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
					tmp.put("version", properties.get(ContentModel.PROP_VERSION_LABEL));
				}
				tmp.put("displayValue", displayName);
				tmp.put("value", JsonHelper.formatValue(value));

				return tmp;
			}

		}

		if (attribute instanceof AssociationDefinition) {// associations

			List<NodeRef> assocRefs;
			if (((AssociationDefinition) attribute).isChild()) {
				assocRefs = associationService.getChildAssocs(nodeRef, attribute.getName());
			} else {
				assocRefs = associationService.getTargetAssocs(nodeRef, attribute.getName());
			}

			if (FormatMode.SEARCH.equals(mode)) {
				HashMap<String, Object> tmp = new HashMap<>(5);

				String nodeRefs = "";
				for (NodeRef assocNodeRef : assocRefs) {

					if (!displayName.isEmpty()) {
						displayName += RepoConsts.LABEL_SEPARATOR;
						nodeRefs += RepoConsts.LABEL_SEPARATOR;
					}

					type = nodeService.getType(assocNodeRef);
					displayName += extractPropName(type, assocNodeRef);
					nodeRefs += assocNodeRef.toString();
				}
				tmp.put("order", order);
				tmp.put("label", attribute.getTitle(dictionaryService));
				tmp.put("type", "subtype");
				tmp.put("displayValue", displayName);
				tmp.put("value", nodeRefs);
				return tmp;

			} else if (FormatMode.CSV.equals(mode) || FormatMode.XLSX.equals(mode)) {
				String ret = "";
				for (NodeRef assocNodeRef : assocRefs) {
					type = nodeService.getType(assocNodeRef);
					if (ret.length() > 0) {
						ret += RepoConsts.LABEL_SEPARATOR;
					}
					ret += extractPropName(type, assocNodeRef);
				}
				return ret;

			} else {
				List<Map<String, Object>> ret = new ArrayList<>(assocRefs.size());
				for (NodeRef assocNodeRef : assocRefs) {
					ret.add(extractCommonNodeData(assocNodeRef));
				}
				return ret;
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> extractCommonNodeData(NodeRef nodeRef) {
		Map<String, Object> tmp = new HashMap<>(5);

		QName type = nodeService.getType(nodeRef);

		tmp.put("metadata", extractMetadata(type, nodeRef));

		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
			tmp.put("version", nodeService.getProperty(nodeRef, BeCPGModel.PROP_VERSION_LABEL));
		} else if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			tmp.put("version", nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL));
		}

		tmp.put("displayValue", extractPropName(type, nodeRef));
		tmp.put("value", nodeRef.toString());
		tmp.put("siteId", extractSiteId(nodeRef));

		return tmp;
	}

	/** {@inheritDoc} */
	@Override
	public String extractSiteId(NodeRef entityNodeRef) {
		String path = nodeService.getPath(entityNodeRef).toPrefixString(namespaceService);
		return SiteHelper.extractSiteId(path);
	}

	/** {@inheritDoc} */
	@Override
	public String extractPropName(NodeRef v) {
		QName type = nodeService.getType(v);
		return extractPropName(type, v);
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttributeExtractorPlugin(NodeRef nodeRef) {
		QName type = nodeService.getType(nodeRef);
		return getAttributeExtractorPlugin(type, nodeRef) != null;
	}

	/** {@inheritDoc} */
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		String value;

		if (permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED) {
			AttributeExtractorPlugin plugin = getAttributeExtractorPlugin(type, nodeRef);
			if (plugin != null) {
				value = plugin.extractPropName(type, nodeRef);
			} else {
				value = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			}
		} else {
			value = I18NUtil.getMessage("message.becpg.access.denied");
		}

		return value;
	}

	/** {@inheritDoc} */
	@Override
	public String extractPropName(String format, NodeRef nodeRef) {
		String value;
		if (permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED) {
			value = ((CharactAttributeExtractorPlugin) getAttributeExtractorPlugin(BeCPGModel.TYPE_CHARACT, nodeRef)).extractExpr(nodeRef, format);
		} else {
			value = I18NUtil.getMessage("message.becpg.access.denied");
		}
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {

		String metadata;

		AttributeExtractorPlugin plugin = getAttributeExtractorPlugin(type, nodeRef);
		if (plugin != null) {
			metadata = plugin.extractMetadata(type, nodeRef);
		} else if (type.equals(ContentModel.TYPE_FOLDER)) {
			metadata = "container";
		} else {
			metadata = type.toPrefixString(namespaceService).split(":")[1];
		}

		return metadata;
	}

	/** {@inheritDoc} */
	@Override
	public String[] getTags(NodeRef nodeRef) {
		String[] result;
		List<String> tags = taggingService.getTags(nodeRef);
		if ((tags == null) || tags.isEmpty()) {
			result = new String[0];
		} else {
			result = tags.toArray(new String[tags.size()]);
		}
		return result;
	}

	private boolean hasReadAccess(QName nodeType, String propName) {

		return securityService.computeAccessMode(nodeType, propName) != SecurityService.NONE_ACCESS;

	}

	/** {@inheritDoc} */
	@Override
	public String getPersonDisplayName(String userId) {
		return personAttributeExtractorPlugin.getPersonDisplayName(userId);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public boolean matchCriteria(NodeRef nodeRef, Map<String, String> criteriaMap) {
		Map<String, Object> comp = extractNodeData(nodeRef, nodeService.getType(nodeRef), new ArrayList<>(criteriaMap.keySet()), FormatMode.JSON);

		// Criteria:{bcpg:allergenListAllergen|bcpg:allergenCode=FX1}
		// Extracted:{dt_bcpg_allergenListAllergen=[{prop_bcpg_allergenCode={displayValue=F257,
		// metadata=text, value=F257}}]}

		// Criteria:{pack:pmlMaterial=Autres matériaux}
		// Extracted:{assoc_pack_pmlMaterial=[{displayValue=Autres matériaux -
		// Bois, siteId=null, metadata=lvValue,
		// value=workspace://SpacesStore/405f98a1-ebfa-41f0-a3e7-8ac7c7c150ca}]}

		for (Map.Entry<String, Object> entry : comp.entrySet()) {
			String critKey = entry.getKey().replace(PROP_SUFFIX, "").replace(ASSOC_SUFFIX, "").replace(DT_SUFFIX, "").replace("_", ":");

			Object tmp = entry.getValue();
			if (tmp != null) {
				Map<String, Object> data = null;

				if (tmp instanceof ArrayList<?>) {
					if (!((ArrayList<?>) tmp).isEmpty()) {
						data = (Map<String, Object>) ((ArrayList<?>) tmp).get(0);
					}
				} else {
					data = (Map<String, Object>) tmp;
				}

				if ((data == null) || data.isEmpty()) {
					return false;
				}

				String value = null;

				if (data.containsKey("value") && (data.get("value") != null)) {
					value = data.get("value").toString().toLowerCase();
				} else {

					for (Map.Entry<String, Object> subEntry : data.entrySet()) {
						tmp = subEntry.getValue();

						critKey += "|"
								+ subEntry.getKey().replace(PROP_SUFFIX, "").replace(ASSOC_SUFFIX, "").replace(DT_SUFFIX, "").replace("_", ":");

						if (tmp instanceof ArrayList<?>) {
							if (!((ArrayList<?>) tmp).isEmpty()) {
								data = (Map<String, Object>) ((ArrayList<?>) tmp).get(0);
							}
						} else {
							data = (Map<String, Object>) tmp;
						}

						if ((data == null) || data.isEmpty()) {
							return false;
						}

						if (data.containsKey("value") && (data.get("value") != null)) {
							value = data.get("value").toString().toLowerCase();
						}

						break;
					}

				}

				if (value == null) {
					return false;
				}

				if (logger.isTraceEnabled()) {
					logger.trace("Test Match on: " + critKey);
					logger.trace("Test Match : " + value + " - " + criteriaMap.get(critKey).toLowerCase());
				}

				String compValue = criteriaMap.get(critKey).toLowerCase();
				String displayValue = data.get("displayValue").toString().toLowerCase();
				if (compValue.startsWith("\"") && compValue.endsWith("\"")) {
					compValue = compValue.replaceAll("\"", "");
				}

				if ((compValue != null) && compValue.startsWith("^")) {

					compValue = compValue.replaceAll("\\^", "");

					if (!value.startsWith(compValue.toLowerCase()) && !displayValue.startsWith(compValue)) {
						return false;
					}
				} else if ((compValue != null) && !value.equalsIgnoreCase(compValue) && !displayValue.equals(compValue)) {
					return false;

				}
			}
		}
		return true;

	}

	/** {@inheritDoc} */
	@Override
	public PropertyFormats getPropertyFormats(FormatMode mode, boolean useServerLocale) {
		return propertyFormatService.getPropertyFormats(mode, useServerLocale);
	}

}
