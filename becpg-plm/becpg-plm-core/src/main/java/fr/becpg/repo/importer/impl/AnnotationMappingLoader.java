package fr.becpg.repo.importer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.config.mapping.FileMapping;
import fr.becpg.config.mapping.FormulaMapping;
import fr.becpg.config.mapping.HierarchyMapping;
import fr.becpg.config.mapping.MappingException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.importer.ClassMapping;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportType;
import fr.becpg.repo.importer.MappingLoader;
import fr.becpg.repo.importer.MappingLoaderFactory;
import fr.becpg.repo.importer.MappingType;
import fr.becpg.repo.importer.annotation.Annotation;
import fr.becpg.repo.importer.annotation.Assoc;
import fr.becpg.repo.importer.annotation.Attribute;
import fr.becpg.repo.importer.annotation.Charact;
import fr.becpg.repo.importer.annotation.DataListKey;
import fr.becpg.repo.importer.annotation.File;
import fr.becpg.repo.importer.annotation.Formula;
import fr.becpg.repo.importer.annotation.Hierarchy;
import fr.becpg.repo.importer.annotation.Key;
import fr.becpg.repo.importer.annotation.MLText;

/**
 * Service to load the annotation mapping.
 *
 * @author rabah
 * @version $Id: $Id
 */

@Service("annotationMappingLoader")
public class AnnotationMappingLoader implements MappingLoader {

	private static final Log logger = LogFactory.getLog(AnnotationMappingLoader.class);
	private static final String ANNOTATION_PACKAGE = Annotation.class.getPackage().getName() + ".";
	private static final String REGX_ANNOTATION = "\\s*@(\\w+)(\\s*\\(\\s*(.*)\\s*\\))*";
	private static final String REGX_ANNOTATION_PROPERTIES = "\\s*(\\w+)\\s*=\\s*\"(.+)\"\\s*";

	@Autowired
	private NodeService nodeService;
	@Autowired
	private DictionaryService dictionaryService;
	@Autowired
	private NamespaceService namespaceService;
	@Autowired
	private MappingLoaderFactory mappingLoaderFactory;

	@PostConstruct
	private void init() {
		mappingLoaderFactory.register(this);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public ImportContext loadClassMapping(Object mapping, ImportContext importContext) throws MappingException {
		Map<String, Object> map = (Map<String, Object>) mapping;
		List<String> columns = (List<String>) map.get(ImportHelper.PFX_COLUMS);
		List<List<String>> columnsParams = (List<List<String>>) map.get(ImportHelper.PFX_COLUMNS_PARAMS);

		if ((columns != null) && (columnsParams != null)) {
			for (List<String> annotationMapping : columnsParams) {
				for (int i = 1; (i < annotationMapping.size()) && (i < columns.size()); i++) {
					Annotation annotation = (Annotation) parseAnnotation(annotationMapping.get(i));
					if (annotation != null) {
						boolean isMissedAssocType = StringUtils.isEmpty(annotation.getType());

						if (StringUtils.isEmpty(annotation.getId())) {
							annotation.setId(columns.get(i));
						}
						if (StringUtils.isEmpty(annotation.getKey())) {
							annotation.setTargetKey(columns.get(i));
						}
						if ((importContext.getType() != null) && StringUtils.isEmpty(annotation.getType())) {
							annotation.setTargetClass(importContext.getType().toPrefixString());
						}

						QName typeQName = QName.createQName(annotation.getType(), namespaceService);

						if ((annotation instanceof Assoc) && isMissedAssocType) {
							AssociationDefinition assocDef = dictionaryService
									.getAssociation(QName.createQName(annotation.getId(), namespaceService));
							ClassDefinition typeDef = assocDef.getTargetClass();
							typeQName = typeDef.getName();
							annotation.setTargetClass(typeQName.getPrefixString());
						}

						logger.debug("Parsed Annotation, " + annotation);

						ClassMapping classMapping = importContext.getClassMappings().get(typeQName) != null
								? importContext.getClassMappings().get(typeQName)
								: new ClassMapping();
						classMapping.setType(typeQName);
						importContext.getClassMappings().put(typeQName, classMapping);

						QName attribute;
						ClassAttributeDefinition attributeDef;

						// Charact
						if (annotation instanceof Charact) {
							Charact charactAnnot = (Charact) annotation;
							attribute = QName.createQName(charactAnnot.getDataListAttribute(), namespaceService);
							QName dataListQName = QName.createQName(charactAnnot.getDataListQName(), namespaceService);
							NodeRef charactNodeRef;
							String charactNodeRefString = charactAnnot.getCharactNodeRef();
							String charactName = charactAnnot.getCharactKeyValue();
							QName charactKeyQName = QName.createQName(charactAnnot.getCharactKeyQName(), namespaceService);
							QName charactQName = QName.createQName(charactAnnot.getCharactQName(), namespaceService);

							// get characteristic nodeRef
							if ((charactNodeRefString != null) && !charactNodeRefString.isEmpty() && NodeRef.isNodeRef(charactNodeRefString)) {
								charactNodeRef = new NodeRef(charactNodeRefString);
							} else if (!charactName.isEmpty()) {
								AssociationDefinition assocDef = dictionaryService.getAssociation(charactQName);
								charactNodeRef = ImportHelper.findCharact(assocDef.getTargetClass().getName(),
										charactKeyQName != null ? charactKeyQName : BeCPGModel.PROP_CHARACT_NAME, charactName, nodeService);

								if (charactNodeRef == null) {
									String error = I18NUtil.getMessage(ImportHelper.MSG_ERROR_GET_NODEREF_CHARACT,
											assocDef.getTargetClass().getName(), charactName);
									logger.error(error);
									throw new MappingException(error);
								}
							} else {
								throw new MappingException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_UNDEFINED_CHARACT, annotation));
							}

							attributeDef = dictionaryService.getProperty(attribute);
							if (attributeDef == null) {
								attributeDef = dictionaryService.getAssociation(attribute);
							}

							CharacteristicMapping attributeMapping = new CharacteristicMapping(charactAnnot.getId(), attributeDef, dataListQName,
									charactQName, charactNodeRef);
							classMapping.getColumns().add(attributeMapping);
						}
						// MLText
						else if (annotation instanceof MLText) {
							String strAttribute = annotation.getAttribute() != null ? annotation.getAttribute() : annotation.getId().split("_")[0];
							attribute = QName.createQName(strAttribute, namespaceService);
							attributeDef = dictionaryService.getProperty(attribute);
							if (attributeDef == null) {
								attributeDef = dictionaryService.getAssociation(attribute);
								if (attributeDef == null) {
									throw new MappingException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
								}
							}
							AttributeMapping attributeMapping = new AttributeMapping(annotation.getId(), attributeDef);
							attributeMapping.setMLText(true);
							classMapping.getColumns().add(attributeMapping);

						} else {
							String strAttribute = annotation.getKey();
							if (isColumnHasAnnotation(i, "@MLText", columnsParams)) {
								strAttribute = annotation.getAttribute() != null ? annotation.getAttribute() : annotation.getId().split("_")[0];
							}
							attribute = QName.createQName(strAttribute, namespaceService);
							attributeDef = dictionaryService.getProperty(attribute);
							if (attributeDef == null) {
								attributeDef = dictionaryService.getAssociation(attribute);
								if (attributeDef == null) {
									throw new MappingException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
								}

							}
						}

						// Node key
						if (annotation instanceof Key) {
							classMapping.getNodeColumnKeys().add(attribute);

							if (ImportType.EntityListItem.equals(importContext.getImportType()) && (importContext.getEntityType() != null)) {
								ClassMapping entityClassMapping = importContext.getClassMappings().get(importContext.getEntityType()) != null
										? importContext.getClassMappings().get(importContext.getEntityType())
										: new ClassMapping();
								entityClassMapping.setType(importContext.getEntityType());
								entityClassMapping.getNodeColumnKeys().add(attribute);
								importContext.getClassMappings().put(importContext.getEntityType(), entityClassMapping);
							}
						}
						// Assoc
						if (annotation instanceof Assoc) {
							classMapping.getNodeColumnKeys().add(attribute);
							if (StringUtils.isNotEmpty(((Assoc) annotation).getPath())) {
								classMapping.getPaths().put(QName.createQName(annotation.getId(), namespaceService), ((Assoc) annotation).getPath());
							}
						}
						// NodeListKey
						if (annotation instanceof DataListKey) {
							classMapping.getDataListColumnKeys().add(attribute);
						}
						// Attribute
						if (annotation instanceof Attribute) {
							AttributeMapping attributeMapping = new AttributeMapping(annotation.getId(), attributeDef);
							if ((annotation.getType() != null) && !annotation.getType().isEmpty()) {
								attributeMapping.setTargetClass(QName.createQName(annotation.getType(), namespaceService));
							}
							classMapping.getColumns().add(attributeMapping);
						}
						// Formula
						if (annotation instanceof Formula) {
							AbstractAttributeMapping attributeMapping = new FormulaMapping(annotation.getId(), attributeDef);
							classMapping.getColumns().add(attributeMapping);
						}
						// File
						if (annotation instanceof File) {
							String path = ((File) annotation).getPath();
							List<String> paths = new ArrayList<>();
							String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
							Collections.addAll(paths, arrPath);

							PropertyDefinition propertyDefinition = dictionaryService.getProperty(attribute);
							FileMapping attributeMapping = new FileMapping(((File) annotation).getId(), propertyDefinition, paths);
							classMapping.getColumns().add(attributeMapping);
						}
						// Hierarchy
						if (annotation instanceof Hierarchy) {
							Hierarchy hierarchyAnnot = (Hierarchy) annotation;
							ClassAttributeDefinition parentLevelAttributeDef = null;
							if ((hierarchyAnnot.getParentLevelAttribute() != null) && !hierarchyAnnot.getParentLevelAttribute().isEmpty()) {

								attribute = QName.createQName(hierarchyAnnot.getParentLevelAttribute(), namespaceService);
								parentLevelAttributeDef = dictionaryService.getProperty(attribute);
								if (parentLevelAttributeDef == null) {
									parentLevelAttributeDef = dictionaryService.getAssociation(attribute);
									if (parentLevelAttributeDef == null) {
										throw new MappingException(
												I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
									}
								}
							}
							AbstractAttributeMapping attributeMapping = new HierarchyMapping(hierarchyAnnot.getId(), attributeDef,
									hierarchyAnnot.getParentLevelColumn(), hierarchyAnnot.getPath(), parentLevelAttributeDef);
							classMapping.getColumns().add(attributeMapping);
						}

					}

				}
			}
		}

		return importContext;
	}

	private boolean isColumnHasAnnotation(int columnId, String annotation, List<List<String>> columnsParams) {
		for (List<String> params : columnsParams) {
			if ((columnId < params.size()) && annotation.equals(params.get(columnId))) {
				return true;
			}
		}
		return false;
	}

	private Object parseAnnotation(String value) throws MappingException {
		Pattern pattern = Pattern.compile(REGX_ANNOTATION);
		Matcher matcher = pattern.matcher(value);
		Object annotation = null;
		if (matcher.find()) {
			String strAnnotation = matcher.group(1).trim();
			String strProperties = matcher.group(3) != null ? matcher.group(3) : "";

			Map<String, Object> properties = getAnnotationProperties(strProperties.split(","));

			try {
				Class<?> annotationClass = Class.forName(ANNOTATION_PACKAGE + strAnnotation);
				annotation = annotationClass.getDeclaredConstructor().newInstance();
				BeanUtils.populate(annotation, properties);
			} catch (Exception e) {
				String error = I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, strAnnotation);
				logger.error("Annotation not found: " + strAnnotation, e);
				throw new MappingException(error);
			}

		}
		return annotation;
	}

	private Map<String, Object> getAnnotationProperties(String[] properties) {
		Pattern pattern = Pattern.compile(REGX_ANNOTATION_PROPERTIES);
		Matcher matcher;
		Map<String, Object> ret = new HashMap<>();
		for (String prop : properties) {
			matcher = pattern.matcher(prop);
			if (matcher.find()) {
				ret.put(matcher.group(1).trim(), matcher.group(2).trim());
			}
		}
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(MappingType mappingType) {
		return MappingType.ANNOTATION.equals(mappingType);
	}

}
