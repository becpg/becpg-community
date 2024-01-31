/*
 *
 */
package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.permissions.AccessControlListDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 * The Class RepoServiceImpl.
 *
 * @author querephi
 * @version $Id: $Id
 */
@Service("repoService")
public class RepoServiceImpl implements RepoService {

	private static final Log logger = LogFactory.getLog(RepoServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private Repository repository;

	@Autowired
	private NamespaceService namespaceService;
	
	@Autowired
	private NodeDAO nodeDAO;
	
	@Autowired
	private AccessControlListDAO accessControlListDAO;

	@Autowired
	protected Repository repositoryHelper;

	@Autowired
	protected RuleService ruleService;
	
	/** {@inheritDoc} */
	@Override
	public NodeRef getOrCreateFolderByPaths(NodeRef parentNodeRef, List<String> paths) {

		for (String folderName : paths) {
			if ((folderName != null) && !folderName.isEmpty()) {
				parentNodeRef = getOrCreateFolderByPath(parentNodeRef, folderName, folderName);
			}
		}
		return parentNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getOrCreateFolderByPath(NodeRef parentNodeRef, String path, String name) {

		NodeRef folderNodeRef = getFolderByPath(parentNodeRef, path);
        
		if (folderNodeRef == null) {
			logger.debug("Create folder : " + name + "  ");
			folderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, PropertiesHelper.cleanFolderName(name));
			if (folderNodeRef == null) {
				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, PropertiesHelper.cleanFolderName(name));
				folderNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.resolveToQName(namespaceService, path),
						ContentModel.TYPE_FOLDER, properties).getChildRef();
			}
		}

		return folderNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getFolderByPath(NodeRef parentNodeRef, String path) {

		NodeRef ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(parentNodeRef, AbstractBeCPGQueryBuilder.encodePath(path));
		if ((ret == null) && !path.contains(RepoConsts.MODEL_PREFIX_SEPARATOR)) {
			// try by name...
			return nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, path);
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getFolderByPath(String path) {
		return getFolderByPath(repository.getCompanyHome(), path);
	}

	/** {@inheritDoc} */
	@Override
	public boolean moveNode(NodeRef nodeRefToMove, NodeRef destinationNodeRef) {

		logger.debug("start moveNode");

		// check the nodeRefToMove is not already moved !
		NodeRef parentOfNodeRefToMove = nodeService.getPrimaryParent(nodeRefToMove).getParentRef();
		if (destinationNodeRef.equals(parentOfNodeRefToMove)) {
			// nothing to do...
			logger.debug("nodeRefToMove is already moved, nothing to do...");
			return false;
		}
		FileExistsException ex = null;
		try {

			for (int i = 0; i < 4; i++) {
				// Check there is not a node with the same name, then rename
				// node
				String name = getAvailableName(destinationNodeRef, (String) nodeService.getProperty(nodeRefToMove, ContentModel.PROP_NAME), false);

				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Move node '%s' in folder '%s'", name, destinationNodeRef));
				}

				try {
					fileFolderService.move(nodeRefToMove, destinationNodeRef, name);

				} catch (FileExistsException e) {
					ex = e;
					// Concurrency error retry
					continue;
				} catch(ConcurrencyFailureException e) {
					String message = e.getMessage();
					if(message!=null && message.startsWith("setFixedAcls: unexpected shared acl: ")) {
						  final Long nodeId = nodeDAO.getNodePair(nodeRefToMove).getFirst();
	                        // retrieve acl properties from node
	                        Long inheritFrom = (Long) nodeDAO.getNodeProperty(nodeId, ContentModel.PROP_INHERIT_FROM_ACL);
	                        Long sharedAclToReplace = (Long) nodeDAO.getNodeProperty(nodeId, ContentModel.PROP_SHARED_ACL_TO_REPLACE);

	                        // set inheritance using retrieved prop
	                        accessControlListDAO.setInheritanceForChildren(nodeRefToMove, inheritFrom, sharedAclToReplace, true,
	                                true);
	                        continue;
					} 
					throw e;
				}
				ex = null;
				break;
			}
		} catch (FileNotFoundException e) {
			logger.error("Failed to move node", e);
		}

		if (ex != null) {
			throw ex;
		}
		
		return true;

	}

	/** {@inheritDoc} */
	@Override
	public String getAvailableName(NodeRef folderNodeRef, String name, boolean forceRename, boolean keepExtension) {

		int count = 0;
		String nameWithCounter = name;
        while (this.nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, nameWithCounter) != null || forceRename)
        {
        	count++;
			forceRename = false;
			
			int dotIndex = name.lastIndexOf(".");
				
			if (keepExtension && dotIndex > 0) {
				String extension = name.substring(dotIndex);
				String nameWithoutExtension = name.substring(0, dotIndex);
				nameWithCounter =  String.format("%s (%d)", nameWithoutExtension, count) + extension;
			} else {
				nameWithCounter =  String.format("%s (%d)", name, count);
			}
			
        }
       
		return nameWithCounter;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getAvailableName(NodeRef folderNodeRef, String name, boolean forceRename) {
		return getAvailableName(folderNodeRef, name, forceRename, false);
	}
	
	@Override
	public boolean moveEntity(NodeRef entityNodeRef, NodeRef destinationFolder) {
		Rule classifyRule = null;
		
		List<Rule> rules = ruleService.getRules(repositoryHelper.getCompanyHome(), false);
		for (Rule rule : rules) {
			if (!rule.getRuleDisabled() && "classifyEntityRule".equals(rule.getTitle())) {
				classifyRule = rule;
				break;
			}
		}

		if (classifyRule != null) {
			ruleService.disableRule(classifyRule);
		}
		
		nodeService.moveNode(entityNodeRef, destinationFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
		
		if (classifyRule != null) {
			ruleService.enableRule(classifyRule);
		}
		
		return true;
	}

}
