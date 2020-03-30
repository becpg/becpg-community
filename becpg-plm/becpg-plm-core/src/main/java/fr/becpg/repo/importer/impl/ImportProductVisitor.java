package fr.becpg.repo.importer.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.config.mapping.HierarchyMapping;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.importer.ClassMapping;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;

/**
 * Class used to import a product with its attributes, characteristics and
 * files.
 * 
 * @author querephi
 */
public class ImportProductVisitor extends ImportEntityListAspectVisitor implements ImportVisitor {

	protected static final String MSG_ERROR_PRODUCTHIERARCHY_EMPTY = "import_service.error.err_producthierarchy_empty";
	protected static final String MSG_ERROR_UNKNOWN_PRODUCTTYPE = "import_service.error.err_unknown_producttype";
	protected static final String MSG_ERROR_OVERRIDE_EXISTING_ONE = "import_service.error.err_override_existing_one";

	private static final Log logger = LogFactory.getLog(ImportProductVisitor.class);

	/**
	 * Check if the node exists, according to : - keys or productCode - Path
	 * where product is classified and name - Path where product is imported and
	 * name.
	 * 
	 * @param importContext
	 *            the import context
	 * @param type
	 *            the type
	 * @param properties
	 *            the properties
	 * @return the node ref
	 * @throws ImporterException
	 */
	@Override
	protected NodeRef findNode(ImportContext importContext, QName type, Map<QName, Serializable> properties) throws ImporterException {

		NodeRef nodeRef = findNodeByKeyOrCode(importContext,null, type, properties, null);

		// check key columns, we don't want to update the wrong product
		if (nodeRef != null) {

			ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType());
			List<QName> nodeColumnKeys;
			if (classMapping != null) {

				nodeColumnKeys = classMapping.getNodeColumnKeys();
			} else {
				nodeColumnKeys = new ArrayList<>();
				nodeColumnKeys.add(BeCPGModel.PROP_CODE);
			}

			for (QName qName : nodeColumnKeys) {

				Serializable value = properties.get(qName);
				Serializable dbvalue = nodeService.getProperty(nodeRef, qName);
				// Philippe: remove test !((String) value).isEmpty()
				// otherwise we update an existing product that we shouldn't
				// (value.isEmpty && dbvalue is not)
				if (value != null && dbvalue != null && !value.equals(dbvalue)) {

					logger.error(I18NUtil.getMessage(MSG_ERROR_OVERRIDE_EXISTING_ONE, value, dbvalue));
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_OVERRIDE_EXISTING_ONE, value, dbvalue));
				}
			}
		}

		return nodeRef;
	}

	@Override
	protected NodeRef findPropertyTargetNodeByValue(ImportContext importContext, PropertyDefinition propDef, AbstractAttributeMapping attributeMapping, String value,
			Map<QName, Serializable> properties) throws ImporterException {

		if (attributeMapping instanceof HierarchyMapping) {
			NodeRef hierarchyNodeRef;
			String path = PlmRepoConsts.PATH_PRODUCT_HIERARCHY + "cm:" + HierarchyHelper.getHierarchyPathName(importContext.getType());
			if(((HierarchyMapping) attributeMapping).getPath()!=null && 
					!((HierarchyMapping) attributeMapping).getPath().isEmpty()){
				path = ((HierarchyMapping) attributeMapping).getPath();
			}
			
			if (((HierarchyMapping) attributeMapping).getParentLevelColumn() != null && !((HierarchyMapping) attributeMapping).getParentLevelColumn().isEmpty()) {
				NodeRef parentHierachyNodeRef = (NodeRef) properties.get(QName.createQName(((HierarchyMapping) attributeMapping).getParentLevelColumn(), namespaceService));
				if (parentHierachyNodeRef != null) {
					hierarchyNodeRef = hierarchyService.getHierarchyByPath(path, parentHierachyNodeRef, value);
				} else {
					if(logger.isDebugEnabled()){
						logger.debug("No parent for column "+attributeMapping.getAttribute().getName()+ " prop "+((HierarchyMapping) attributeMapping).getParentLevelColumn());
					}
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY_EMPTY, attributeMapping.getAttribute().getName(), value));
				}
			} else {
				if(logger.isDebugEnabled()){
					logger.debug("Look for hierarchy "+attributeMapping.getAttribute().getName()+": "+value+" at path "+path);
				}
				hierarchyNodeRef = hierarchyService.getHierarchyByPath(path, null, value);
			}
			if (hierarchyNodeRef == null) {
				throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY_EMPTY, attributeMapping.getAttribute().getName(), value));
			}
			return hierarchyNodeRef;
		}

		return super.findPropertyTargetNodeByValue(importContext, propDef, attributeMapping, value, properties);

	}
	
	
}
