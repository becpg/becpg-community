/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
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
import org.json.JSONObject;
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

	private CommonDataListCallBack commonDataListCallBack = new CommonDataListCallBack();

	private class CommonDataListCallBack implements DataListCallBack {

		@Override
		public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field, FormatMode mode) {
			List<Map<String, Object>> ret = new ArrayList<>();

			if (field.getFieldDef() instanceof AssociationDefinition) {
				List<NodeRef> assocRefs;
				if (((AssociationDefinition) field.getFieldDef()).isChild()) {
					assocRefs = associationService.getChildAssocs(nodeRef, field.getFieldDef().getName());
				} else {
					assocRefs = associationService.getTargetAssocs(nodeRef, field.getFieldDef().getName());
				}
				for (NodeRef itemNodeRef : assocRefs) {
					addExtracted(itemNodeRef, field, ret, mode);
				}

			} else if (field.getFieldDef() instanceof PropertyDefinition
					&& DataTypeDefinition.NODE_REF.equals(((PropertyDefinition) field.getFieldDef()).getDataType().getName())) {

				Object value = nodeService.getProperty(nodeRef, field.getFieldDef().getName());
				if (value != null) {
					if (!((PropertyDefinition) field.getFieldDef()).isMultiValued()) {

						addExtracted((NodeRef) value, field, ret, mode);
					} else {
						@SuppressWarnings("unchecked")
						List<NodeRef> values = (List<NodeRef>) value;
						for (NodeRef tempValue : values) {
							addExtracted(tempValue, field, ret, mode);
						}

					}
				}

			}
			return ret;
		}

		private void addExtracted(NodeRef itemNodeRef, AttributeExtractorStructure field, List<Map<String, Object>> ret, FormatMode mode) {
			if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
				QName itemType = nodeService.getType(itemNodeRef);
				Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
				ret.add(extractNodeData(itemNodeRef, itemType, properties, field.getChildrens(), mode, null));
			}
		}

	}

	private AttributeExtractorPlugin getAttributeExtractorPlugin(QName type) {

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

		final AttributeExtractorField field;
		boolean isEntityField = false;
		boolean isDataListField = false;
		ClassAttributeDefinition fieldDef;
		List<AttributeExtractorStructure> childrens;
		AttributeExtractorFilter filter;
		Locale locale;
		QName fieldQname;
		QName itemType;
		String formula = null;

		public AttributeExtractorStructure(AttributeExtractorField field, ClassAttributeDefinition fieldDef, QName itemType) {
			this.fieldDef = fieldDef;
			this.fieldQname = fieldDef.getName();
			this.field = field;
			this.itemType = itemType;
		}

		public AttributeExtractorStructure(AttributeExtractorField field, ClassAttributeDefinition fieldDef, Locale locale, QName itemType) {
			this(field, fieldDef, itemType);
			this.locale = locale;
		}

		public AttributeExtractorStructure(AttributeExtractorField field, QName fieldQname, ClassAttributeDefinition fieldDef,
				List<AttributeExtractorField> dLFields, QName itemType) {
			this(field, fieldDef, itemType);
			this.childrens = readExtractStructure(fieldQname, dLFields);
		}

		protected AttributeExtractorStructure(AttributeExtractorField field, QName fieldQname, ClassDefinition fieldDef, boolean isEntityField,
				AttributeExtractorFilter filter, List<AttributeExtractorField> dLFields, QName itemType) {
			this(field, fieldQname, new ClassAttributeDefinition() {

				@Override
				public boolean isProtected() {
					return false;
				}

				@Override
				public String getTitle(MessageLookup messageLookup) {
					return fieldDef.getTitle(messageLookup);
				}

				@Override
				public QName getName() {
					return fieldDef.getName();
				}

				@Override
				public ModelDefinition getModel() {
					return fieldDef.getModel();
				}

				@Override
				public String getDescription(MessageLookup messageLookup) {
					return fieldDef.getDescription(messageLookup);
				}
			}, dLFields, itemType);
			this.filter = filter;
			this.isEntityField = isEntityField;
			this.isDataListField = !isEntityField;
		}

		public AttributeExtractorStructure(AttributeExtractorField field, String formula) {
			this.field = field;
			this.formula = formula;
		}

		public String getFieldName() {
			return field.getFieldName();
		}

		public String getFieldLabel() {
			return field.getFieldLabel();
		}

		public boolean isEntityField() {
			return isEntityField;
		}

		public boolean isFormulaField() {
			return (formula != null) && !formula.isEmpty();
		}

		public boolean isDataListItems() {
			return isDataListField;
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
			result = (prime * result) + ((field == null) ? 0 : field.hashCode());
			result = (prime * result) + ((fieldQname == null) ? 0 : fieldQname.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if ((obj == null) || (getClass() != obj.getClass())) {
				return false;
			}
			AttributeExtractorStructure other = (AttributeExtractorStructure) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (field == null) {
				if (other.field != null) {
					return false;
				}
			} else if (!field.equals(other.field)) {
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
			return "AttributeExtractorStructure [field=" + field + ", isEntityField=" + isEntityField + ", fieldDef=" + fieldDef + ", childrens="
					+ childrens + ", filter=" + filter + ", locale=" + locale + ", fieldQname=" + fieldQname + ", itemType=" + itemType + ", formula="
					+ formula + "]";
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

		StringBuilder value = null;

		if ((v == null) || (propertyDef == null)) {
			return null;
		}

		String dataType = propertyDef.getDataType().toString();

		if (dataType.equals(DataTypeDefinition.ASSOC_REF.toString())) {
			return extractPropName((NodeRef) v);
		} else if (dataType.equals(DataTypeDefinition.CATEGORY.toString())) {

			List<NodeRef> categories = (ArrayList<NodeRef>) v;

			for (NodeRef categoryNodeRef : categories) {
				if (value == null) {
					value = new StringBuilder(extractPropName(categoryNodeRef));
				} else {
					value.append(RepoConsts.LABEL_SEPARATOR);
					value.append(extractPropName(categoryNodeRef));
				}
			}

		} else if (dataType.equals(DataTypeDefinition.BOOLEAN.toString())
				|| (dataType.equals(DataTypeDefinition.ANY.toString()) && (v instanceof Boolean))) {

			return TranslateHelper.getTranslatedBoolean((Boolean) v, propertyFormats.isUseDefaultLocale());

		} else if (dataType.equals(DataTypeDefinition.TEXT.toString())) {

			String constraintName = null;
			DynListConstraint dynListConstraint = null;

			if (!propertyDef.getConstraints().isEmpty()) {

				for (ConstraintDefinition constraint : propertyDef.getConstraints()) {
					boolean found = false;
					if (constraint.getConstraint() instanceof DynListConstraint) {
						dynListConstraint = (DynListConstraint) constraint.getConstraint();
						found = true;

					} else if ("LIST".equals(constraint.getConstraint().getType())) {
						constraintName = entityDictionaryService.toPrefixString(constraint.getRef()).replace(":", "_");
						found = true;
					}
					if (found) {
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
							value.append(RepoConsts.LABEL_SEPARATOR);
						} else {
							value = new StringBuilder();
						}

						if (dynListConstraint != null) {
							if (!formatData) {
								value.append(tempValue);
							} else {
								value.append(dynListConstraint.getDisplayLabel(tempValue));
							}
						} else {
							if (!formatData) {
								value.append(tempValue);
							} else {

								value.append(constraintName != null
										? TranslateHelper.getConstraint(constraintName, tempValue, propertyFormats.isUseDefaultLocale())
										: tempValue);
							}
						}
					}

				}
			} else {

				if (SecurityModel.PROP_ACL_PROPNAME.equals(propertyDef.getName())) {
					QName aclPropName = QName.createQName(v.toString(), namespaceService);
					ClassAttributeDefinition aclDef = entityDictionaryService.getPropDef(aclPropName);
					if (aclDef != null) {
						return aclDef.getTitle(dictionaryService);
					} else {
						return v.toString();
					}

				} else {
					if (dynListConstraint != null) {
						return dynListConstraint.getDisplayLabel(v.toString());
					} else {
						return constraintName != null
								? TranslateHelper.getConstraint(constraintName, v.toString(), propertyFormats.isUseDefaultLocale())
								: v.toString();
					}
				}
			}

		} else if (dataType.equals(DataTypeDefinition.DATE.toString())) {
			return propertyFormats.formatDate(v);
		} else if (dataType.equals(DataTypeDefinition.DATETIME.toString())) {
			return propertyFormats.formatDateTime(v);
		} else if (dataType.equals(DataTypeDefinition.NODE_REF.toString())) {
			if (!propertyDef.isMultiValued() || v instanceof NodeRef) {
				return extractPropName((NodeRef) v);
			} else {
				List<NodeRef> values = (List<NodeRef>) v;
				if (values != null) {
					for (NodeRef tempValue : values) {
						if (tempValue != null) {
							if (value != null) {
								value.append(RepoConsts.LABEL_SEPARATOR);
							} else {
								value = new StringBuilder();
							}

							value.append(extractPropName(tempValue));
						}
					}
				}
			}

		} else if (dataType.equals(DataTypeDefinition.MLTEXT.toString())) {
			return v.toString();
		} else if (dataType.equals(DataTypeDefinition.DOUBLE.toString()) || dataType.equals(DataTypeDefinition.FLOAT.toString())
				|| (dataType.equals(DataTypeDefinition.ANY.toString()) && ((v instanceof Double) || (v instanceof Float)))) {

			return propertyFormats.formatDecimal(v);

		} else if (dataType.equals(DataTypeDefinition.QNAME.toString())) {

			if (v != null) {
				String ret = I18NUtil
						.getMessage("bcpg_bcpgmodel.type." + entityDictionaryService.toPrefixString((QName) v).replace(":", "_") + ".title");
				if (ret == null) {
					ret = v.toString();
				}
				return ret;

			}
		}

		else {

			TypeConverter converter = new TypeConverter();
			return converter.convert(propertyDef.getDataType(), v).toString();
		}
		return value != null ? value.toString() : null;
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
					if (value == null) {
						return "";
					}
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
	 * formula| excel| image| entity|
	 *
	 * EXCEL -> bcpg:compoListProduct_bcpg:erpCode, attribute -> bcpg_compoListProduct|bcpg_erpCode
	 *
	 * bcpg:compoList[bcpg:compoListProduct_bcpg:erpCode == "PF1"]|bcpg:compoListQty bcpg:allergenList[bcpg:allergenListAllergen_bcpg:allergenCode == "FX1"]|bcpg:allergenListInVoluntary
	 * bcpg:allergenList[bcpg:allergenListAllergen#bcpg:allergenCode == "FX1"]_bcpg:allergenListInVoluntary bcpg:allergenList[bcpg:allergenListAllergen#bcpg:allergenCode ==
	 * "FX1"]|bcpg:allergenListInVoluntary pack:packMaterialList[pack:pmlMaterial.startsWith("Autres matériaux")] _pack:pmlWeight
	 *
	 * -> bcpg_compoList[bcpg_compoListProduct|bcpg_erpCode=="PF1"]| bcpg_compoListQty
	 *
	 * Field bcpg_nutListMethod -> prop_bcpg_nutListMethod Assoc -> assoc_ dyn_ DataListField bcpg_activityList|bcpg_alType|bcpg_alData|bcpg_alUserId|cm_created -> dt_bcpg_activityList Assocs Field
	 * bcpg_nutListNut|bcpg_nutGDA|bcpg_nutUL|bcpg_nutUnit -> dt_bcpg_nutListNut
	 */
	/** {@inheritDoc} */
	@Override
	public List<AttributeExtractorStructure> readExtractStructure(QName itemType, List<AttributeExtractorField> metadataFields) {

		List<AttributeExtractorStructure> ret = new LinkedList<>();

		int formulaCount = 0;

		for (AttributeExtractorField field : metadataFields) {
			Locale locale = null;

			if (field.isNested()) {
				AttributeExtractorField dlField = field.nextToken();
				if ("entity".equals(dlField.getFieldName()) || "product".equals(dlField.getFieldName())) {
					field = field.nextToken();
					QName fieldQname = QName.createQName(field.getFieldName(), namespaceService);
					if (hasReadAccess(itemType, field.getFieldName())) {
						ClassAttributeDefinition prodDef = entityDictionaryService.getPropDef(fieldQname);
						if (prodDef != null) {
							if("product".equals(dlField.getFieldName())) {
								ret.add(new AttributeExtractorStructure(field.prefixed("product_"), prodDef, itemType));
							} else {
								ret.add(new AttributeExtractorStructure(field.prefixed("entity_"), prodDef, itemType));
							}
						}
					}

				} else if ("formula".equals(dlField.getFieldName())) {
					field = field.nextToken();
					ret.add(new AttributeExtractorStructure(new AttributeExtractorField("formula_" + (formulaCount++), field.getFieldLabel()),
							field.getFieldName()));
				} else if ("excel".equals(dlField.getFieldName())) {
					field = field.nextToken();
					ret.add(new AttributeExtractorStructure(new AttributeExtractorField("excel_" + (formulaCount++), field.getFieldLabel()),
							field.getFieldName()));
				} else if ("image".equals(dlField.getFieldName())) {
					field = field.nextToken();
					ret.add(new AttributeExtractorStructure(new AttributeExtractorField("image_" + (formulaCount++), field.getFieldLabel()),
									field.getFieldName()));
				 } else {
					List<AttributeExtractorField> dLFields = new ArrayList<>();
					AttributeExtractorFilter dataListFilter = null;
					QName fieldQname = null;
					if (dlField.getFieldName().contains("[")) {
						Matcher maEqual = Pattern.compile("(.*)(\\[(.*)\\s*==\\s*\"(.*)\"\\])").matcher(dlField.getFieldName());
						if (maEqual.matches()) {
							fieldQname = QName.createQName(maEqual.group(1), namespaceService);
							dataListFilter = new AttributeExtractorFilter(maEqual.group(3).replace("#", "|").trim(), maEqual.group(4).trim());
						} else {
							Matcher maStartsWith = Pattern.compile("(.*)(\\[(.*)\\.startsWith\\(\"(.*)\"\\)\\])").matcher(dlField.getFieldName());
							if (maStartsWith.matches()) {
								fieldQname = QName.createQName(maStartsWith.group(1), namespaceService);
								dataListFilter = new AttributeExtractorFilter(maStartsWith.group(3).replace("#", "|").trim(),
										"^" + maStartsWith.group(4).trim());
							} else {
								logger.error("Cannot extract datalist filter: " + dlField);
							}
						}

					} else if (field.hasMoreTokens()) {
						AttributeExtractorField nextToken = field.nextToken();
						if ((MLTextHelper.getSupportedLocalesList() != null)
								&& MLTextHelper.getSupportedLocalesList().contains(nextToken.getFieldName())) {
							locale = MLTextHelper.parseLocale(nextToken.getFieldName());
						} else {
							dLFields.add(nextToken);
						}
						fieldQname = QName.createQName(dlField.getFieldName(), namespaceService);
					} else {
						fieldQname = QName.createQName(dlField.getFieldName(), namespaceService);
					}
					while (field.hasMoreTokens()) {
						AttributeExtractorField nextToken = field.nextToken();
						if ((MLTextHelper.getSupportedLocalesList() != null)
								&& MLTextHelper.getSupportedLocalesList().contains(nextToken.getFieldName())) {
							dLFields.set(dLFields.size() - 1, new AttributeExtractorField(
									dLFields.get(dLFields.size() - 1).getFieldName() + "|" + nextToken.getFieldName(), nextToken.getFieldLabel()));
						} else {
							dLFields.add(nextToken);
						}
					}
					
					// Reset positions for next level
					field.resetPositions();

					if (entityDictionaryService.isSubClass(fieldQname, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

						ClassDefinition propDef = entityDictionaryService.getClass(fieldQname);

						ret.add(new AttributeExtractorStructure(dlField.prefixed(DT_SUFFIX), fieldQname, propDef, false, dataListFilter, dLFields,
								itemType));

					} else if (entityDictionaryService.isSubClass(fieldQname, BeCPGModel.TYPE_ENTITY_V2)) {

						ClassDefinition propDef = entityDictionaryService.getClass(fieldQname);

						ret.add(new AttributeExtractorStructure(dlField.prefixed(DT_SUFFIX), fieldQname, propDef, true, null, dLFields, itemType));
					} else {

						ClassAttributeDefinition propDef = entityDictionaryService.getPropDef(fieldQname);
						// nested assoc
						if (hasReadAccess(itemType, dlField.getFieldName())) {
							if (isAssoc(propDef)) {
								ret.add(new AttributeExtractorStructure(dlField.prefixed(DT_SUFFIX),
										((AssociationDefinition) propDef).getTargetClass().getName(), propDef, dLFields, itemType));
							} else if ((propDef != null) ) {
								
								if(locale != null) {
								String prefix = AttributeExtractorService.PROP_SUFFIX;
								ret.add(new AttributeExtractorStructure(
										new AttributeExtractorField(prefix + dlField.getFieldName().replaceFirst(":", "_") + "_" + locale.toString(),
												dlField.getFieldLabel()),
										propDef, locale, itemType));
								} else if(DataTypeDefinition.NODE_REF.equals(((PropertyDefinition)propDef).getDataType().getName())) {
									ret.add(new AttributeExtractorStructure(dlField.prefixed(DT_SUFFIX),
											((PropertyDefinition) propDef).getName(), propDef, dLFields, itemType));
								}
										
							}
						}

					}
				}
			} else {

				QName fieldQname = QName.createQName(field.getFieldName(), namespaceService);
				if (hasReadAccess(itemType, field.getFieldName())) {

					ClassAttributeDefinition prodDef = entityDictionaryService.getPropDef(fieldQname);

					if (field.getFieldName().startsWith("dyn_")) {
						ret.add(new AttributeExtractorStructure(field, field.getFieldName()));
					}

					if (prodDef != null) {
						String prefix = AttributeExtractorService.PROP_SUFFIX;
						if (isAssoc(prodDef)) {
							prefix = AttributeExtractorService.ASSOC_SUFFIX;
						}
						ret.add(new AttributeExtractorStructure(field.prefixed(prefix), prodDef, itemType));
					}
				}
			}
		}
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, List<AttributeExtractorField> metadataFields, FormatMode mode) {
		return extractNodeData(nodeRef, itemType, nodeService.getProperties(nodeRef), readExtractStructure(itemType, metadataFields), mode,
				commonDataListCallBack);

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

		int order = 0;

		for (AttributeExtractorStructure field : metadataFields) {
			if (field.isNested()) {
				List<Map<String, Object>> extracted = callback.extractNestedField(nodeRef, field, mode);

				if ((FormatMode.CSV.equals(mode) || FormatMode.XLSX.equals(mode)) && !extracted.isEmpty()) {

					if (extracted.size() > 1) {
						for (Map<String, Object> extractedElt : extracted) {
							for (Map.Entry<String, Object> entry : extractedElt.entrySet()) {
								String key = field.getFieldName() + "_" + entry.getKey();
								if ((entry.getValue() instanceof Number) && ret.containsKey(key) && (ret.get(key) instanceof Number)) {
									Double value = (Double) ret.get(key) + (Double) entry.getValue();
									ret.put(key, value);
								}
								if ((entry.getValue() instanceof String) && ret.containsKey(key) && (ret.get(key) instanceof String)) {
									String value = (String) ret.get(key) + "," + (String) entry.getValue();
									ret.put(key, value);
								} else {
									ret.put(key, entry.getValue());
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

	@SuppressWarnings("unchecked")
	private Object extractNodeData(NodeRef nodeRef, Map<QName, Serializable> properties, Locale locale, ClassAttributeDefinition attribute,
			FormatMode mode, int order) {

		Serializable value;
		String displayName = "";
		QName type;

		// property
		if (attribute instanceof PropertyDefinition && !isPropertyToExtractAsAssoc(attribute)) {

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

						tmp.put("metadata", metadata);
					} else {
						tmp.put("metadata", extractMetadata(type, nodeRef));
					}
				}

				if ((properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL) != null)
						&& !((String) properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL)).isBlank()) {
					tmp.put("version", properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL));
				} else if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
					tmp.put("version", properties.get(BeCPGModel.PROP_VERSION_LABEL));
				} else if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
					tmp.put("version", properties.get(ContentModel.PROP_VERSION_LABEL));
				}
				tmp.put("displayValue", displayName);
				tmp.put("value", JsonHelper.formatValue(value));

				return tmp;
			}

		}

		if (attribute instanceof AssociationDefinition || isPropertyToExtractAsAssoc(attribute)) {// associations

			List<NodeRef> assocRefs = null;
			if (attribute instanceof PropertyDefinition){
				if (((PropertyDefinition) attribute).isMultiValued()) {
				  assocRefs  = (List<NodeRef>) properties.get(attribute.getName());
				}
			} else {
			
				if (((AssociationDefinition) attribute).isChild()) {
					assocRefs = associationService.getChildAssocs(nodeRef, attribute.getName());
				} else {
					assocRefs = associationService.getTargetAssocs(nodeRef, attribute.getName());
				}
			}

			if(assocRefs!=null) {
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
					StringBuilder ret = new StringBuilder();
					for (NodeRef assocNodeRef : assocRefs) {
						type = nodeService.getType(assocNodeRef);
						if (ret.length() > 0) {
							ret.append(RepoConsts.LABEL_SEPARATOR);
						}
						ret.append(extractPropName(type, assocNodeRef));
					}
					return ret.toString();
	
				} else {
					List<Map<String, Object>> ret = new ArrayList<>(assocRefs.size());
					for (NodeRef assocNodeRef : assocRefs) {
						ret.add(extractCommonNodeData(assocNodeRef));
					}
					return ret;
				}
			}
		}
		return null;
	}

	private boolean isPropertyToExtractAsAssoc(ClassAttributeDefinition attribute) {
		if(attribute instanceof PropertyDefinition ) {
			return ((PropertyDefinition)attribute).isMultiValued() && DataTypeDefinition.NODE_REF.equals(((PropertyDefinition)attribute).getDataType().getName().getPrefixedQName(namespaceService));
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> extractCommonNodeData(NodeRef nodeRef) {
		Map<String, Object> tmp = new HashMap<>(5);

		QName type = nodeService.getType(nodeRef);

		tmp.put("metadata", extractMetadata(type, nodeRef));

		Serializable manualVersionLabel = nodeService.getProperty(nodeRef, BeCPGModel.PROP_MANUAL_VERSION_LABEL);

		if ((manualVersionLabel instanceof String) && !((String) manualVersionLabel).isBlank()) {
			tmp.put("version", manualVersionLabel);
		} else if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
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
	
	@Override
	public String extractPropName(QName type, JSONObject v) {
		AttributeExtractorPlugin plugin = getAttributeExtractorPlugin(type);
		if (plugin != null) {
			return plugin.extractPropName(v);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasAttributeExtractorPlugin(NodeRef nodeRef) {
		QName type = nodeService.getType(nodeRef);
		return getAttributeExtractorPlugin(type) != null;
	}

	/** {@inheritDoc} */
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		String value;

		if (permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED) {
			AttributeExtractorPlugin plugin = getAttributeExtractorPlugin(type);
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
	public String extractExpr(String format, NodeRef nodeRef) {
		String value;
		if (permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED) {
			value = ((CharactAttributeExtractorPlugin) getAttributeExtractorPlugin(BeCPGModel.TYPE_CHARACT)).extractExpr(nodeRef, format);
		} else {
			value = I18NUtil.getMessage("message.becpg.access.denied");
		}
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {

		String metadata;

		AttributeExtractorPlugin plugin = getAttributeExtractorPlugin(type);
		if (plugin != null) {
			metadata = plugin.extractMetadata(type, nodeRef);
		} else if (type.equals(ContentModel.TYPE_FOLDER)) {
			metadata = "container";
		} else {
			metadata = entityDictionaryService.toPrefixString(type).split(":")[1];
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
		return securityService.computeAccessMode(null, nodeType, propName) != SecurityService.NONE_ACCESS;
	}

	/** {@inheritDoc} */
	@Override
	public String getPersonDisplayName(String userId) {
		return personAttributeExtractorPlugin.getPersonDisplayName(userId);
	}

	@Override
	public boolean matchCriteria(NodeRef nodeRef, Map<String, String> criteriaMap) {
		
		if (internalMatchCriteria(nodeRef, criteriaMap)) {
			return true;
		}
		
		if (attributeExtractorPlugins != null) {
			for (AttributeExtractorPlugin attributeExtractorPlugin : attributeExtractorPlugins) {
				if (attributeExtractorPlugin.matchCriteria(nodeRef, criteriaMap)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private boolean internalMatchCriteria(NodeRef nodeRef, Map<String, String> criteriaMap) {

		Locale currentLocal = I18NUtil.getLocale();
		Locale currentContentLocal = I18NUtil.getContentLocale();
		try {

			I18NUtil.setLocale((Locale.getDefault()));
			I18NUtil.setContentLocale(null);

			LinkedList<AttributeExtractorField> fields = new LinkedList<>();
			for (String metadataField : criteriaMap.keySet()) {
				AttributeExtractorField field = new AttributeExtractorField(metadataField, null);
				fields.add(field);
			}

			Map<String, Object> comp = extractNodeData(nodeRef, nodeService.getType(nodeRef), fields, FormatMode.JSON);

			/** Criteria:{bcpg:allergenListAllergen|bcpg:allergenCode=FX1}
			 * Extracted:{dt_bcpg_allergenListAllergen=[{prop_bcpg_allergenCode={displayValue=F257,
			 * metadata=text, value=F257}}]}
			
			 * Criteria:{pack:pmlMaterial=Autres matériaux}
			 * Extracted:{assoc_pack_pmlMaterial=[{displayValue=Autres matériaux -
			 * Bois, siteId=null, metadata=lvValue,
			 * value=workspace://SpacesStore/405f98a1-ebfa-41f0-a3e7-8ac7c7c150ca}]}
			 *
			 */

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

					String compValue = criteriaMap.get(critKey).toLowerCase();
					String displayValue = data.get("displayValue").toString().toLowerCase();
					if (compValue.startsWith("\"") && compValue.endsWith("\"")) {
						compValue = compValue.replace("\"", "");
					}
					
					if ((compValue != null) && compValue.contains("\\ ")) {
						compValue = compValue.replace("\\ ", " ");
					}

					if (logger.isTraceEnabled()) {
						logger.trace("Test Match on: " + critKey);
						logger.trace("Test Match : " + value + "/" + displayValue + " - " + compValue);
					}
					if ((compValue != null) && compValue.contains("*")) {

						compValue = compValue.replace("*", "");

						if (!value.contains(compValue) && !displayValue.contains(compValue)) {
							return false;
						}
					} else if ((compValue != null) && compValue.startsWith("^")) {

						compValue = compValue.replace("^", "");

						if (!value.startsWith(compValue) && !displayValue.startsWith(compValue)) {
							return false;
						}
					} else if ((compValue != null) && compValue.contains("..")) {
						String[] bounds = compValue.split("\\.\\.");

						if (bounds.length > 1) {
							String lowerBound = bounds[0];
							String upperBound = bounds[1];

							if ((value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0)
									&& (displayValue.compareTo(lowerBound) < 0 || displayValue.compareTo(lowerBound) > 0)) {
								return false;
							}

						}
					} else if (compValue != null && data.containsKey("metadata")
							&& ("datetime".equals(data.get("metadata")) || "date".equals(data.get("metadata")))) {
						if (!dateMatches(value, compValue)) {
							return false;
						}
					} else if ((compValue != null) && (!value.equals(compValue) && !displayValue.equals(compValue))) {
						return false;

					}
				}
			}
			return true;
		} finally {
			I18NUtil.setLocale(currentLocal);
			I18NUtil.setContentLocale(currentContentLocal);
		}

	}

	private boolean dateMatches(String value, String compValue) {
		String dateRegex = "^\\d{4}-\\d{2}-\\d{2}$";
		Pattern pattern = Pattern.compile(dateRegex);
		Matcher matcher = pattern.matcher(compValue);
		if (matcher.matches()) {
			return value.startsWith(compValue);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public PropertyFormats getPropertyFormats(FormatMode mode, boolean useServerLocale) {
		return propertyFormatService.getPropertyFormats(mode, useServerLocale);
	}

}
