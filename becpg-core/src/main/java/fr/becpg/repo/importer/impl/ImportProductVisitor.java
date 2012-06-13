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
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.HierarchyHelper;
import fr.becpg.repo.helper.LuceneHelper;
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

	/** The PAT h_ produc t_ folder. */
	private static String PATH_PRODUCT_FOLDER = "./cm:Products/cm:%s/cm:%s/cm:%s/cm:%s";

	protected static final String MSG_ERROR_PRODUCTHIERARCHY1_EMPTY = "import_service.error.err_producthierarchy1_empty";
	protected static final String MSG_ERROR_PRODUCTHIERARCHY2_EMPTY = "import_service.error.err_producthierarchy2_empty";
	protected static final String MSG_ERROR_UNKNOWN_PRODUCTTYPE = "import_service.error.err_unknown_producttype";
	protected static final String MSG_ERROR_PRODUCTSTATE_EMPTY = "import_service.error.err_productstate_empty";
	protected static final String MSG_ERROR_OVERRIDE_EXISTING_ONE = "import_service.error.err_override_existing_one";

	/** The repository helper. */
	private Repository repositoryHelper;

	private SearchService searchService;

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
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

					// state
					String state = (String) properties.get(BeCPGModel.PROP_PRODUCT_STATE);
					if (state != null) {
						if (!state.isEmpty()) {

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
										String path = String.format(PATH_PRODUCT_FOLDER, state, systemProductType, ISO9075.encode(HierarchyHelper.getHierachyName(hierarchy1,nodeService)), ISO9075.encode(HierarchyHelper.getHierachyName(hierarchy2,nodeService)));

										List<NodeRef> nodes = searchService.selectNodes(repositoryHelper.getCompanyHome(), path, null, namespaceService, false);

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
						} else {
							throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTSTATE_EMPTY, properties));
						}
					}
				}

				// Check if product exists in Import folder
				if (nodeRef == null) {
					nodeRef = nodeService.getChildByName(importContext.getParentNodeRef(), ContentModel.ASSOC_CONTAINS, name);
				}

				// productFolder => look for product
				if (nodeRef != null && nodeService.getType(nodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)) {
					nodeRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, name);
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
				if (value != null && !((String) value).isEmpty() && dbvalue != null && !value.equals(dbvalue)) {

					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_OVERRIDE_EXISTING_ONE, value, dbvalue));
				}
			}
		}

		return nodeRef;
	}

	@Override
	protected NodeRef findTargetNodeByValue(ImportContext importContext, PropertyDefinition propDef, String value, Map<QName, Serializable> properties) throws ImporterException {
		QName propName = propDef.getName();

	

		if (propName.equals(BeCPGModel.PROP_PRODUCT_HIERARCHY1) || propName.equals(BeCPGModel.PROP_PRODUCT_HIERARCHY2)) {

			String queryPath; 
			if (propName.equals(BeCPGModel.PROP_PRODUCT_HIERARCHY2)) {
				NodeRef hierachy1NodeRef = (NodeRef) properties.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1);
				if (hierachy1NodeRef != null) {
					 queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE, LuceneHelper.encodePath(HierarchyHelper.getHierarchyPath(importContext.getType(),namespaceService)), hierachy1NodeRef.toString(),
							value);
		
				} else {
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY1_EMPTY, properties));
				}
			} else {
				queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE_ROOT, LuceneHelper.encodePath(HierarchyHelper.getHierarchyPath(importContext.getType(),namespaceService)),
						value);
			}

			List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, RepoConsts.MAX_RESULTS_SINGLE_VALUE);

			logger.debug("resultSet.length() : " + ret.size()+" for "+queryPath);
			if (ret.size() != 0) {
				return ret.get(0);
			}
			logger.error(" Hierachy : "+queryPath+" not found ");
			throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_PRODUCTHIERARCHY1_EMPTY, properties));

		}
		return super.findTargetNodeByValue(importContext, propDef, value, properties);
	}
}
