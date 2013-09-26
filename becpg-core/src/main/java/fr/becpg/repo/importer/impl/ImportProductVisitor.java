/*
 * 
 */
package fr.becpg.repo.importer.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.config.mapping.HierarchyMapping;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.importer.ClassMapping;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;

// TODO: Auto-generated Javadoc
/**
 * Class used to import a product with its attributes, characteristics and
 * files.
 * 
 * @author querephi
 */
public class ImportProductVisitor extends ImportEntityListAspectVisitor implements ImportVisitor {

	protected static final String MSG_ERROR_PRODUCTHIERARCHY1_EMPTY = "import_service.error.err_producthierarchy1_empty";
	protected static final String MSG_ERROR_PRODUCTHIERARCHY2_EMPTY = "import_service.error.err_producthierarchy2_empty";
	protected static final String MSG_ERROR_UNKNOWN_PRODUCTTYPE = "import_service.error.err_unknown_producttype";
	protected static final String MSG_ERROR_OVERRIDE_EXISTING_ONE = "import_service.error.err_override_existing_one";

	/** The repository helper. */
	private Repository repositoryHelper;

	private HierarchyService hierarchyService;

	private static Log logger = LogFactory.getLog(ImportProductVisitor.class);

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}

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

		NodeRef nodeRef = findNodeByKeyOrCode(importContext, type, BeCPGModel.PROP_CODE, properties);

		// look by name
		if (nodeRef == null) {

			String name = (String) properties.get(ContentModel.PROP_NAME);
			if (name != null && name != "") {

				// look in the product hierarchy of the repository if we don't
				// import in a site
				if (nodeRef == null && !importContext.isSiteDocLib()) {

					// SystemProductType
					SystemProductType systemProductType = SystemProductType.valueOf(type);
					if (!systemProductType.equals(SystemProductType.Unknown)) {

						// TODO change by unique hierachy field
						// hierarchy
						NodeRef hierarchy = (NodeRef) properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY2);
						if (hierarchy != null) {

							// look for path where product should be
							// stored
							String path = hierarchyService.getHierarchyPath(hierarchy, systemProductType);

							List<NodeRef> nodes = beCPGSearchService.searchByPath(repositoryHelper.getCompanyHome(), path);

							if (!nodes.isEmpty()) {
								nodeRef = nodeService.getChildByName(nodes.get(0), ContentModel.ASSOC_CONTAINS, name);
							}

						} else {
							throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY2_EMPTY, properties));
						}

					} else {
						throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNKNOWN_PRODUCTTYPE, nodeService.getType(nodeRef)));
					}
				}

				// Check if product exists in Import folder
				if (nodeRef == null) {
					logger.debug("Product to update found in import folder");
					nodeRef = nodeService.getChildByName(importContext.getParentNodeRef(), ContentModel.ASSOC_CONTAINS, name);
				}
			} else {

				throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_GET_OR_CREATE_NODEREF));
			}
		}

		// check key columns, we don't want to update the wrong product
		if (nodeRef != null) {

			ClassMapping classMapping = importContext.getClassMappings().get(importContext.getType());
			List<QName> nodeColumnKeys = null;
			if (classMapping != null) {

				nodeColumnKeys = classMapping.getNodeColumnKeys();
			} else {
				nodeColumnKeys = new ArrayList<QName>();
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
			NodeRef hierarchyNodeRef = null;
			if (((HierarchyMapping) attributeMapping).getParentLevelColumn() != null && !((HierarchyMapping) attributeMapping).getParentLevelColumn().isEmpty()) {
				NodeRef parentHierachyNodeRef = (NodeRef) properties.get(QName.createQName(((HierarchyMapping) attributeMapping).getParentLevelColumn(), namespaceService));
				if (parentHierachyNodeRef != null) {
					hierarchyNodeRef = hierarchyService.getHierarchy(importContext.getType(), parentHierachyNodeRef, value);
				} else {
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY1_EMPTY, properties));
				}
			} else {
				hierarchyNodeRef = hierarchyService.getRootHierarchy(importContext.getType(), value);
			}
			if (hierarchyNodeRef == null) {
				throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY2_EMPTY, properties));
			}
			return hierarchyNodeRef;
		}

		return super.findPropertyTargetNodeByValue(importContext, propDef, attributeMapping, value, properties);

	}
}
