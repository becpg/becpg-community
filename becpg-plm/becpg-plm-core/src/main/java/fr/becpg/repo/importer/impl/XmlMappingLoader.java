package fr.becpg.repo.importer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Node;
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
import fr.becpg.repo.importer.MappingLoader;
import fr.becpg.repo.importer.MappingLoaderFactory;
import fr.becpg.repo.importer.MappingType;
import jakarta.annotation.PostConstruct;


/**
 * <p>XmlMappingLoader class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("xmlMappingLoader")
public class XmlMappingLoader implements MappingLoader {
	
	private static final Log logger = LogFactory.getLog(XmlMappingLoader.class);
	
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
	@Override
	public ImportContext loadClassMapping(Object mapping, ImportContext importContext) throws MappingException {
		Element mappingsElt = (Element) mapping;

		List<Node> mappingNodes = mappingsElt.selectNodes(ImportHelper.QUERY_XPATH_MAPPING);

		Node dateFormat = mappingsElt.selectSingleNode(ImportHelper.QUERY_XPATH_DATE_FORMAT);
		if (dateFormat != null) {
			importContext.setPropertyFormats(importContext.getPropertyFormats().withDateFormat(dateFormat.getStringValue()));
		}

		Node datetimeFormat = mappingsElt.selectSingleNode(ImportHelper.QUERY_XPATH_DATETIME_FORMAT);
		if (datetimeFormat != null) {
			importContext.setPropertyFormats(importContext.getPropertyFormats().withDateTimeFormat(datetimeFormat.getStringValue()));
		}

		Node decimalFormatPattern = mappingsElt.selectSingleNode(ImportHelper.QUERY_XPATH_DECIMAL_PATTERN);
		if (decimalFormatPattern != null) {
			importContext.setPropertyFormats(importContext.getPropertyFormats().withDecimalFormat(decimalFormatPattern.getStringValue()));
		}

		for (Node mappingNode : mappingNodes) {

			QName typeQName = QName.createQName(mappingNode.valueOf(ImportHelper.QUERY_ATTR_GET_NAME), namespaceService);
			ClassMapping classMapping = new ClassMapping();
			classMapping.setType(typeQName);

			logger.debug("Register mapping for : " + typeQName);

			importContext.getClassMappings().put(typeQName, classMapping);

			// node keys
			List<Node> nodeColumnKeyNodes = mappingNode.selectNodes(ImportHelper.QUERY_XPATH_NODE_COLUMN_KEY);
			for (Node columnNode : nodeColumnKeyNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}

				classMapping.getNodeColumnKeys().add(attribute);
			}

			// productlist keys
			List<Node> dataListColumnKeyNodes = mappingNode.selectNodes(ImportHelper.QUERY_XPATH_DATALIST_COLUMN_KEY);
			for (Node columnNode : dataListColumnKeyNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}

				classMapping.getDataListColumnKeys().add(attribute);
			}

			// attributes
			List<Node> columnNodes = mappingNode.selectNodes(ImportHelper.QUERY_XPATH_COLUMNS_ATTRIBUTE);
			for (Node columnNode : columnNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}

				AttributeMapping attributeMapping = new AttributeMapping(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ID), attributeDef);
				String targetClass = columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_TARGET_CLASS);
				logger.debug("targetClass: " + targetClass);
				if ((targetClass != null) && !targetClass.isEmpty()) {
					attributeMapping.setTargetClass(QName.createQName(targetClass, namespaceService));
				}
				classMapping.getColumns().add(attributeMapping);
			}

			// MLText
			columnNodes = mappingNode.selectNodes(ImportHelper.QUERY_XPATH_COLUMNS_MLTEXT);
			for (Node columnNode : columnNodes) {
				String strAttribute = columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ATTRIBUTE);
				QName attribute = StringUtils.isBlank(strAttribute) ? null
					: QName.createQName(strAttribute, namespaceService);
				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {
					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}
				AttributeMapping attributeMapping = new AttributeMapping(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ID), attributeDef);
				attributeMapping.setMLText(true);
				classMapping.getColumns().add(attributeMapping);
			}
			
			// Formula
			columnNodes = mappingNode.selectNodes(ImportHelper.QUERY_XPATH_COLUMNS_FORMULA);
			for (Node columnNode : columnNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}

				AbstractAttributeMapping attributeMapping = new FormulaMapping(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ID), attributeDef);
				classMapping.getColumns().add(attributeMapping);
			}

			// characteristics
			columnNodes = mappingNode.selectNodes(ImportHelper.QUERY_XPATH_COLUMNS_DATALIST);
			for (Node columnNode : columnNodes) {
				QName qName = QName.createQName(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ATTRIBUTE), namespaceService);
				QName dataListQName = QName.createQName(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_DATALIST_QNAME), namespaceService);
				NodeRef charactNodeRef;
				String charactNodeRefString = columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_CHARACT_NODE_REF);
				String charactName = columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_CHARACT_NAME);
				String strCharactKeyQName =  columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_CHARACT_KEY_QNAME);
				QName charactKeyQName = null;
				if(strCharactKeyQName !=null && !strCharactKeyQName.isEmpty()) {
					charactKeyQName =  QName.createQName(strCharactKeyQName, namespaceService);
				}
				QName charactQName = QName.createQName(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_CHARACT_QNAME), namespaceService);

				// get characteristic nodeRef
				if ((charactNodeRefString != null) && !charactNodeRefString.isEmpty() && NodeRef.isNodeRef(charactNodeRefString)) {
					charactNodeRef = new NodeRef(charactNodeRefString);
				} else if (!charactName.isEmpty()) {
					AssociationDefinition assocDef = dictionaryService.getAssociation(charactQName);
					charactNodeRef = ImportHelper.findCharact(assocDef.getTargetClass().getName(), charactKeyQName != null ? charactKeyQName : BeCPGModel.PROP_CHARACT_NAME, charactName, nodeService);

					if (charactNodeRef == null) {
						String error = I18NUtil.getMessage(ImportHelper.MSG_ERROR_GET_NODEREF_CHARACT, assocDef.getTargetClass().getName(), charactName);
						logger.error(error);
						throw new MappingException(error);
					}
				} else {
					throw new MappingException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_UNDEFINED_CHARACT, columnNode.asXML()));
				}

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(qName);
				if (attributeDef == null) {
					attributeDef = dictionaryService.getAssociation(qName);
				}

				CharacteristicMapping attributeMapping = new CharacteristicMapping(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ID), attributeDef, dataListQName,
						charactQName, charactNodeRef);
				classMapping.getColumns().add(attributeMapping);
			}

			// file import
			columnNodes = mappingNode.selectNodes(ImportHelper.QUERY_XPATH_COLUMNS_FILE);
			for (Node columnNode : columnNodes) {
				QName qName = QName.createQName(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				String path = columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_PATH);
				List<String> paths = new ArrayList<>();
				String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
				Collections.addAll(paths, arrPath);

				PropertyDefinition propertyDefinition = dictionaryService.getProperty(qName);
				FileMapping attributeMapping = new FileMapping(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ID), propertyDefinition, paths);
				classMapping.getColumns().add(attributeMapping);
			}

			// hierachies
			columnNodes = mappingNode.selectNodes(ImportHelper.QUERY_XPATH_COLUMNS_HIERARCHY);
			for (Node columnNode : columnNodes) {
				QName attribute = QName.createQName(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ATTRIBUTE), namespaceService);

				ClassAttributeDefinition attributeDef = dictionaryService.getProperty(attribute);
				if (attributeDef == null) {

					attributeDef = dictionaryService.getAssociation(attribute);
					if (attributeDef == null) {
						throw new MappingException(I18NUtil.getMessage(ImportHelper.MSG_ERROR_MAPPING_ATTR_FAILED, typeQName, attribute));
					}
				}

			

				AbstractAttributeMapping attributeMapping = new HierarchyMapping(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_ID), attributeDef,
						(columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_PARENT_LEVEL) != null) && !columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_PARENT_LEVEL).isEmpty()
								? columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_PARENT_LEVEL) : null,
						columnNode.valueOf(ImportHelper.QUERY_ATTR_GET_PATH), null);
				classMapping.getColumns().add(attributeMapping);
			}

		}

		return importContext;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public boolean applyTo(MappingType mappingType) {
		return MappingType.XML.equals(mappingType);
	}
	

}
