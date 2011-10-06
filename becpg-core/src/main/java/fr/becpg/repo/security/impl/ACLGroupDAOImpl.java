package fr.becpg.repo.security.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.SecurityModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;

/**
 *  ACL Group DAO implementation
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class ACLGroupDAOImpl implements BeCPGDao<ACLGroupData> {

	private static Log logger = LogFactory.getLog(ACLGroupDAOImpl.class);

	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private EntityListDAO entityListDAO;
	private AssociationService associationService;
	private AuthorityDAO authorityDAO;
	
	

	public void setAuthorityDAO(AuthorityDAO authorityDAO) {
		this.authorityDAO = authorityDAO;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public NodeRef create(NodeRef parentNodeRef, ACLGroupData cpData) {
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, cpData.getName());
		properties.put(SecurityModel.PROP_ACL_GROUP_NODE_TYPE,
				cpData.getNodeType().toString());

		NodeRef cpNodeRef = nodeService.createNode(
				parentNodeRef,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
						cpData.getName()), SecurityModel.TYPE_ACL_GROUP,
				properties).getChildRef();

		// acl entry list
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(cpNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(cpNodeRef);
		}
		createAclList(listContainerNodeRef, cpData.getAcls());

		return cpNodeRef;
	}

	@Override
	public void update(NodeRef cpNodeRef, ACLGroupData cpData) {

		nodeService.setProperty(cpData.getNodeRef(), ContentModel.PROP_NAME,
				cpData.getName());
		nodeService.setProperty(cpData.getNodeRef(),
				SecurityModel.PROP_ACL_GROUP_NODE_TYPE, cpData.getNodeType().toString());

		// control def list
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(cpNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(cpNodeRef);
		}
		createAclList(listContainerNodeRef, cpData.getAcls());

	}

	@Override
	public ACLGroupData find(NodeRef cpNodeRef) {
		ACLGroupData cpData = new ACLGroupData();
		cpData.setNodeRef(cpNodeRef);
		cpData.setName((String) nodeService.getProperty(cpNodeRef,
				ContentModel.PROP_NAME));
		cpData.setNodeType((String) nodeService.getProperty(cpNodeRef,
				SecurityModel.PROP_ACL_GROUP_NODE_TYPE));
		cpData.setNodeAspects((List) nodeService.getProperty(cpNodeRef,
				SecurityModel.PROP_ACL_GROUP_NODE_ASPECTS));

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(cpNodeRef);
		if (listContainerNodeRef != null) {
			cpData.setAcls(loadAclList(listContainerNodeRef));
		}

		return cpData;
	}

	@Override
	public void delete(NodeRef cpNodeRef) {
		nodeService.deleteNode(cpNodeRef);

	}

	private List<ACLEntryDataItem> loadAclList(NodeRef listContainerNodeRef) {

		List<ACLEntryDataItem> aclsList = null;

		logger.debug("loadAclsList");

		if (listContainerNodeRef != null) {
			logger.debug("loadAclsList, list container exists");
			NodeRef aclsListNodeRef = entityListDAO.getList(listContainerNodeRef,
					SecurityModel.TYPE_ACL_ENTRY);

			if (aclsListNodeRef != null) {
				aclsList = new ArrayList<ACLEntryDataItem>();
				List<FileInfo> nodes = fileFolderService
						.listFiles(aclsListNodeRef);

				logger.debug("loadAclsList, list exists, size: " + nodes.size());

				for (int z_idx = 0; z_idx < nodes.size(); z_idx++) {
					FileInfo node = nodes.get(z_idx);
					NodeRef nodeRef = node.getNodeRef();

					List<AssociationRef> groupsAssocRefs = nodeService
							.getTargetAssocs(nodeRef,
									SecurityModel.ASSOC_GROUPS_ASSIGNEE);
					List<String> groups = new ArrayList<String>(
							groupsAssocRefs.size());
					for (AssociationRef assocRef : groupsAssocRefs) {
						groups.add((String) nodeService.getProperty(
								assocRef.getTargetRef(), ContentModel.PROP_AUTHORITY_NAME));
					}

					ACLEntryDataItem aclEntry = new ACLEntryDataItem(nodeRef,
							(String) nodeService.getProperty(nodeRef,
									SecurityModel.PROP_ACL_PROPNAME),
							(String) nodeService.getProperty(nodeRef,
									SecurityModel.PROP_ACL_PERMISSION), groups);

					aclsList.add(aclEntry);
				}
			}
		}

		return aclsList;
	}

	private void createAclList(NodeRef listContainerNodeRef,
			List<ACLEntryDataItem> aclsList) {

		if (listContainerNodeRef != null) {
			NodeRef aclsListNodeRef = entityListDAO.getList(listContainerNodeRef,
					SecurityModel.TYPE_ACL_ENTRY);

			if (aclsList == null) {
				// delete existing list
				if (aclsListNodeRef != null)
					nodeService.deleteNode(aclsListNodeRef);
			} else {
				// acls list, create if needed
				if (aclsListNodeRef == null) {
					aclsListNodeRef = entityListDAO.createList(
							listContainerNodeRef, SecurityModel.TYPE_ACL_ENTRY);
				}

				List<FileInfo> files = fileFolderService
						.listFiles(aclsListNodeRef);

				// create temp list
				List<NodeRef> aclsListToTreat = new ArrayList<NodeRef>();
				for (ACLEntryDataItem aclsListDataItem : aclsList) {
					aclsListToTreat.add(aclsListDataItem.getNodeRef());
				}

				// remove deleted nodes
				Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
				for (FileInfo file : files) {

					if (!aclsListToTreat.contains(file.getNodeRef())) {
						// delete
						nodeService.deleteNode(file.getNodeRef());
					} else {
						filesToUpdate.put(file.getNodeRef(), file.getNodeRef());
					}
				}

				// update or create nodes
				for (ACLEntryDataItem aclsListDataItem : aclsList) {
					NodeRef aclsNodeRef = aclsListDataItem.getNodeRef();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(SecurityModel.PROP_ACL_PROPNAME,
							aclsListDataItem.getPropName());
					properties.put(SecurityModel.PROP_ACL_PERMISSION,
							aclsListDataItem.getPermissionModel()
									.getPermission());

					if (filesToUpdate.containsKey(aclsNodeRef)) {
						// update
						nodeService.setProperties(
								filesToUpdate.get(aclsNodeRef), properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService
								.createNode(
										aclsListNodeRef,
										ContentModel.ASSOC_CONTAINS,
										QName.createQName(
												NamespaceService.CONTENT_MODEL_1_0_URI,
												GUID.generate()),
										SecurityModel.TYPE_ACL_ENTRY,
										properties);
						aclsNodeRef = childAssocRef.getChildRef();
					}

					List<NodeRef> groups = new ArrayList<NodeRef>();
					for(String group : aclsListDataItem.getPermissionModel().getGroups()){
						NodeRef grNode = authorityDAO.getAuthorityNodeRefOrNull(group);
						if(grNode!=null){
							groups.add(grNode);
						} else {
							logger.warn("Group not found : "+group);
						}
					}
					
					
					 // Groups
					 associationService.update(aclsNodeRef,
					 SecurityModel.ASSOC_GROUPS_ASSIGNEE,groups
					);
				
				}
			}
		}

	}

}
