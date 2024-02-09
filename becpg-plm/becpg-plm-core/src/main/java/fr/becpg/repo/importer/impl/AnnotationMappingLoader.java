package fr.becpg.repo.importer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import fr.becpg.model.PLMModel;
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
import jakarta.annotation.PostConstruct;

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

		ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType()) != null
				? importContext.getClassMappings().get(importContext.getType())
				: new ClassMapping();
		classMapping.setType(importContext.getType());
		importContext.getClassMappings().put(importContext.getType(), classMapping);

		if ((columns != null) && (columnsParams != null)) {
			for (List<String> annotationMapping : columnsParams) {
				for (int i = 1; (i < annotationMapping.size()) && (i < columns.size()); i++) {
					Annotation annotation = (Annotation) parseAnnotation(annotationMapping.get(i));
					if (annotation != null) {

						if (StringUtils.isEmpty(annotation.getId())) {
							annotation.setId(columns.get(i));
						}

						QName targetClassQName = null;
						QName targetKey = null;
						QName columnQname = null;
						ClassAttributeDefinition attributeDef = null;
						ClassMapping targetClassMapping = null;

						if (!StringUtils.isEmpty(annotation.getType())) {
							targetClassQName = QName.createQName(annotation.getType(), namespaceService);
						} else if ((annotation instanceof Assoc)) {
							AssociationDefinition assocDef = dictionaryService
									.getAssociation(QName.createQName(annotation.getId(), namespaceService));
							ClassDefinition typeDef = assocDef.getTargetClass();
							targetClassQName = typeDef.getName();
							annotation.setTargetClass(targetClassQName.getPrefixString());
						}

						if (!StringUtils.isEmpty(annotation.getKey())) {
							targetKey = QName.createQName(annotation.getKey(), namespaceService);
						}

						logger.debug("Parsed Annotation, " + annotation);

						if (targetClassQName != null) {
							targetClassMapping = importContext.getClassMappings().get(targetClassQName) != null
									? importContext.getClassMappings().get(targetClassQName)
									: new ClassMapping();
							targetClassMapping.setType(targetClassQName);
							importContext.getClassMappings().put(targetClassQName, targetClassMapping);
						}

						// Charact
						if (annotation instanceof Charact) {
							Charact charactAnnot = (Charact) annotation;
							columnQname = QName.createQName(charactAnnot.getDataListAttribute(), namespaceService);
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

							attributeDef = dictionaryService.getProperty(columnQname);
							if (attributeDef == null) {
								attributeDef = dictionaryService.getAssociation(columnQname);
							}

							CharacteristicMapping attributeMapping = new CharacteristicMapping(charactAnnot.getId(), attributeDef, dataListQName,
									charactQName, charactNodeRef);

							if (targetClassMapping != null) {
								targetClassMapping.getColumns().add(attributeMapping);
							} else {
								classMapping.getColumns().add(attributeMapping);
							}
						}
						// MLText
						else if (annotation instanceof MLText) {
							String strAttribute = annotation.getAttribute() != null ? annotation.getAttribute() : annotation.getId().split("_")[0];
							columnQname = QName.createQName(strAttribute, namespaceService);
							attributeDef = dictionaryService.getProperty(columnQname);
							if (attributeDef == null) {
								attributeDef = dictionaryService.getAssociation(columnQname);
								if (attributeDef == null) {
									throw new MappingException(
											I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, targetClassQName, columnQname));
								}
							}
							AttributeMapping attributeMapping = new AttributeMapping(annotation.getId(), attributeDef);
							attributeMapping.setMLText(true);
							classMapping.getColumns().add(attributeMapping);

						} else {
							String strAttribute = annotation.getId();
							if (isColumnHasAnnotation(i, "@MLText", columnsParams)) {
								strAttribute = annotation.getAttribute() != null ? annotation.getAttribute() : annotation.getId().split("_")[0];
							}
							columnQname = QName.createQName(strAttribute, namespaceService);
							attributeDef = dictionaryService.getProperty(columnQname);
							if (attributeDef == null) {
								if (annotation instanceof Hierarchy) {
									attributeDef = dictionaryService.getProperty(BeCPGModel.PROP_PARENT_LEVEL);
								} else {
									attributeDef = dictionaryService.getAssociation(columnQname);
									if (attributeDef == null) {
										throw new MappingException(
												I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, targetClassQName, columnQname));
									}
								}
							}
						}

						// Node key
						if (annotation instanceof Key) {
							classMapping.getNodeColumnKeys().add(columnQname);

							if (ImportType.EntityListItem.equals(importContext.getImportType())) {

								QName entityType = importContext.getEntityType() != null ? importContext.getEntityType() : PLMModel.TYPE_PRODUCT;

								ClassMapping entityClassMapping = importContext.getClassMappings().get(entityType) != null
										? importContext.getClassMappings().get(entityType)
										: new ClassMapping();
								entityClassMapping.setType(entityType);
								entityClassMapping.getNodeColumnKeys().add(targetKey != null ? targetKey : columnQname);
								importContext.getClassMappings().put(entityType, entityClassMapping);

							}
						}

						// NodeListKey
						if (annotation instanceof DataListKey) {
							classMapping.getDataListColumnKeys().add(columnQname);
						}

						// Assoc
						if ((annotation instanceof Assoc) || (annotation instanceof Attribute)) {
							AttributeMapping attributeMapping = new AttributeMapping(annotation.getId(), attributeDef);
							if (targetClassQName != null) {
								attributeMapping.setTargetClass(targetClassQName);
							}
							classMapping.getColumns().add(attributeMapping);

							if (targetClassMapping != null) {
								targetClassMapping.getNodeColumnKeys().add(targetKey != null ? targetKey : columnQname);
								if ((annotation instanceof Assoc) && StringUtils.isNotEmpty(((Assoc) annotation).getPath())) {
									targetClassMapping.getPaths().put(QName.createQName(annotation.getId(), namespaceService),
											((Assoc) annotation).getPath());
								}
							}
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

							PropertyDefinition propertyDefinition = dictionaryService.getProperty(columnQname);
							FileMapping attributeMapping = new FileMapping(((File) annotation).getId(), propertyDefinition, paths);
							classMapping.getColumns().add(attributeMapping);
						}
						// Hierarchy
						if (annotation instanceof Hierarchy) {
							Hierarchy hierarchyAnnot = (Hierarchy) annotation;

							AbstractAttributeMapping attributeMapping = new HierarchyMapping(hierarchyAnnot.getId(), attributeDef,
									hierarchyAnnot.getParentLevelColumn(), hierarchyAnnot.getPath(), hierarchyAnnot.getKey());
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
