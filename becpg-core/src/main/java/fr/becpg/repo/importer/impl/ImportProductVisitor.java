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
import org.alfresco.util.ISO9075;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.importer.ClassMapping;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.product.hierarchy.HierarchyHelper;
import fr.becpg.repo.product.hierarchy.HierarchyService;

// TODO: Auto-generated Javadoc
/**
 * Class used to import a product with its attributes, characteristics and
 * files.
 * 
 * @author querephi
 */
public class ImportProductVisitor extends ImportEntityListAspectVisitor implements ImportVisitor {

	/** The PAT h_ produc t_ folder. */
	private static String PATH_PRODUCT_FOLDER = "./cm:Products/cm:%s/cm:%s/cm:%s";

	protected static final String MSG_ERROR_PRODUCTHIERARCHY1_EMPTY = "import_service.error.err_producthierarchy1_empty";
	protected static final String MSG_ERROR_PRODUCTHIERARCHY2_EMPTY = "import_service.error.err_producthierarchy2_empty";
	protected static final String MSG_ERROR_UNKNOWN_PRODUCTTYPE = "import_service.error.err_unknown_producttype";
	protected static final String MSG_ERROR_OVERRIDE_EXISTING_ONE = "import_service.error.err_override_existing_one";

	/** The repository helper. */
	private Repository repositoryHelper;

	private HierarchyService hierarchyService;

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

						// hierarchy 1
						NodeRef hierarchy1 = (NodeRef) properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1);
						if (hierarchy1 != null ) {

							// hierarchy 2
							NodeRef hierarchy2 = (NodeRef) properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY2);
							if (hierarchy2 != null ) {

								// look for path where product should be
								// stored
								String path = String.format(PATH_PRODUCT_FOLDER, systemProductType, ISO9075.encode(HierarchyHelper.getHierachyName(hierarchy1,nodeService)), ISO9075.encode(HierarchyHelper.getHierachyName(hierarchy2,nodeService)));

								List<NodeRef> nodes = beCPGSearchService.searchByPath(repositoryHelper.getCompanyHome(), path);

								if (!nodes.isEmpty()) {
									nodeRef = nodeService.getChildByName(nodes.get(0), ContentModel.ASSOC_CONTAINS, name);
								}

							} else {
								throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY2_EMPTY, properties));
							}

						} else {
							throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY1_EMPTY, properties));
						}
					} else {
						throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNKNOWN_PRODUCTTYPE, nodeService.getType(nodeRef)));
					}					
				}

				// Check if product exists in Import folder
				if (nodeRef == null) {
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
				// otherwise we update an existing product that we shouldn't (value.isEmpty && dbvalue is not)
				if (value != null && dbvalue != null && !value.equals(dbvalue)) {

					logger.error(I18NUtil.getMessage(MSG_ERROR_OVERRIDE_EXISTING_ONE, value, dbvalue));
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_OVERRIDE_EXISTING_ONE, value, dbvalue));
				}
			}
		}

		return nodeRef;
	}

	@Override
	protected NodeRef findPropertyTargetNodeByValue(ImportContext importContext, PropertyDefinition propDef, String value, Map<QName, Serializable> properties) throws ImporterException {
		QName propName = propDef.getName();

		if (propName.equals(BeCPGModel.PROP_PRODUCT_HIERARCHY1) || propName.equals(BeCPGModel.PROP_PRODUCT_HIERARCHY2)) {

			NodeRef hierarchyNodeRef = null;
			if (propName.equals(BeCPGModel.PROP_PRODUCT_HIERARCHY2)) {
				// nodeRef found before (hierarchy1 must be before hierarchy2 in import file)
				NodeRef hierachy1NodeRef = (NodeRef) properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1);
				if (hierachy1NodeRef != null) {					 
					hierarchyNodeRef = hierarchyService.getHierarchy2(importContext.getType(), hierachy1NodeRef, value);
		
				} else {
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY1_EMPTY, properties));
				}
			} else {
				hierarchyNodeRef = hierarchyService.getHierarchy1(importContext.getType(), value);
			}			

			if(hierarchyNodeRef == null){
				throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY1_EMPTY, properties));
			}			
			return hierarchyNodeRef;
		}
		return super.findPropertyTargetNodeByValue(importContext, propDef, value, properties);
	}
}
