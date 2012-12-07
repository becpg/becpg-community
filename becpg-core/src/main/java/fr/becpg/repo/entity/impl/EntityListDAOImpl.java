package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.LuceneHelper.Operator;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * 
 * @author querephi
 * 
 */
@Repository
public class EntityListDAOImpl implements EntityListDAO {

	private static final String QUERY_LIST_ITEM = " +PARENT:\"%s\" +TYPE:\"%s\"";

	private static Log logger = LogFactory.getLog(EntityListDAOImpl.class);

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private FileFolderService fileFolderService;

	private NamespaceService namespaceService;

	private CopyService copyService;

	private BeCPGSearchService beCPGSearchService;
	
	private AssociationService associationService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public NodeRef getListContainer(NodeRef nodeRef) {

		return nodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
	}

	@Override
	public NodeRef getList(NodeRef listContainerNodeRef, String name) {

		NodeRef listNodeRef = null;
		if (listContainerNodeRef != null) {
			listNodeRef = nodeService.getChildByName(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, QName.createValidLocalName(name));
		}
		return listNodeRef;
	}

	@Override
	public NodeRef getList(NodeRef listContainerNodeRef, QName listQName) {
		if (listQName == null) {
			return null;
		}

		return getList(listContainerNodeRef, listQName.getLocalName());
	}

	@Override
	public NodeRef createListContainer(NodeRef nodeRef) {
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, RepoConsts.CONTAINER_DATALISTS);
		properties.put(ContentModel.PROP_TITLE, RepoConsts.CONTAINER_DATALISTS);
		NodeRef ret =  nodeService.createNode(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, BeCPGModel.ASSOC_ENTITYLISTS, ContentModel.TYPE_FOLDER, properties).getChildRef();
		nodeService.addAspect(ret, BeCPGModel.ASPECT_HIDDEN_FOLDER, new HashMap<QName, Serializable>());
		return ret;
	}

	@Override
	public NodeRef createList(NodeRef listContainerNodeRef, QName listQName) {

		ClassDefinition classDef = dictionaryService.getClass(listQName);

		if(classDef==null){
			logger.error("No classDef found for :"+listQName);
			throw new InvalidParameterException("No classDef found for :"+listQName);
		}
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, listQName.getLocalName());
		properties.put(ContentModel.PROP_TITLE, classDef.getTitle());
		properties.put(ContentModel.PROP_DESCRIPTION, classDef.getDescription());
		properties.put(DataListModel.PROP_DATALISTITEMTYPE, listQName.toPrefixString(namespaceService));

		
		return nodeService.createNode(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, listQName, DataListModel.TYPE_DATALIST, properties).getChildRef();
		
	}

	@Override
	public NodeRef createList(NodeRef listContainerNodeRef, String name, QName listQName) {

		String entityTitle = TranslateHelper.getTranslatedPath(name);
		if (entityTitle == null) {
			entityTitle = name;
		}

		QName assocQname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name));

		if (logger.isDebugEnabled()) {
			logger.debug("Create data list with name:" + name + " of type " + listQName.getLocalName() + " title " + entityTitle + " with assocQname : "
					+ assocQname.toPrefixString(namespaceService));
		}

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, name);
		properties.put(ContentModel.PROP_TITLE, entityTitle);
		properties.put(DataListModel.PROP_DATALISTITEMTYPE, listQName.toPrefixString(namespaceService));

		return nodeService.createNode(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, assocQname, DataListModel.TYPE_DATALIST, properties).getChildRef();

	}

	@Override
	public List<NodeRef> getExistingListsNodeRef(NodeRef listContainerNodeRef) {

		List<NodeRef> existingLists = new ArrayList<NodeRef>();

		if (listContainerNodeRef != null) {
			List<FileInfo> nodes = fileFolderService.listFolders(listContainerNodeRef);

			for (FileInfo node : nodes) {

				NodeRef listNodeRef = node.getNodeRef();
				String dataListType = (String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);

				if (dataListType != null && !dataListType.isEmpty()) {

					QName dataListTypeQName = QName.createQName(dataListType, namespaceService);

					if (dictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

						existingLists.add(listNodeRef);
					}
					else{
						logger.warn("Existing list doesn't inheritate from 'bcpg:entityListItem'.");
					}
				}

			}
		}
		return existingLists;
	}

	@Override
	public List<QName> getExistingListsQName(NodeRef listContainerNodeRef) {

		List<QName> existingLists = new ArrayList<QName>();

		if (listContainerNodeRef != null) {
			List<FileInfo> nodes = fileFolderService.listFolders(listContainerNodeRef);

			for (FileInfo node : nodes) {

				NodeRef listNodeRef = node.getNodeRef();
				String dataListType = (String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);

				if (dataListType != null && !dataListType.isEmpty()) {

					existingLists.add(QName.createQName(dataListType, namespaceService));
				}
			}
		}
		return existingLists;
	}

	@Override
	public NodeRef getListItem(NodeRef listContainerNodeRef, QName assocQName, NodeRef nodeRef) {

		if (listContainerNodeRef != null && assocQName != null && nodeRef != null) {

			List<FileInfo> fileInfos = fileFolderService.listFiles(listContainerNodeRef);

			for (FileInfo fileInfo : fileInfos) {

				List<AssociationRef> assocRefs = nodeService.getTargetAssocs(fileInfo.getNodeRef(), assocQName);
				if (assocRefs!=null &&  !assocRefs.isEmpty() && nodeRef.equals(assocRefs.get(0).getTargetRef())) {
					return fileInfo.getNodeRef();
				}

			}
		}
		return null;
	}

	@Override
	public void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean override) {

		copyDataLists(sourceNodeRef, targetNodeRef, null, override);
	}
	
	@Override
	public void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, Collection<QName> listQNames, boolean override) {
		
		// do not initialize entity version
		if (nodeService.hasAspect(targetNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
			return;
		}

		if (sourceNodeRef != null) {

			/*-- copy source datalists--*/
			logger.debug("/*-- copy source datalists--*/");
			NodeRef sourceListContainerNodeRef = getListContainer(sourceNodeRef);
			NodeRef targetListContainerNodeRef = getListContainer(targetNodeRef);

			if (sourceListContainerNodeRef != null) {

				List<NodeRef> sourceListsNodeRef = getExistingListsNodeRef(sourceListContainerNodeRef);
				for (NodeRef sourceListNodeRef : sourceListsNodeRef) {

					// create container if needed
					if (targetListContainerNodeRef == null) {

						targetListContainerNodeRef = createListContainer(targetNodeRef);
					}

					String dataListType = (String) nodeService.getProperty(sourceListNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
					QName listQName = QName.createQName(dataListType, namespaceService);
					
					if(listQNames == null || listQNames.contains(listQName)){
					
						NodeRef existingListNodeRef = getList(targetListContainerNodeRef, listQName);
						boolean copy = true;
						if (existingListNodeRef != null) {
							if (override) {
								nodeService.deleteNode(existingListNodeRef);
							} else {
								copy = false;
							}
						}

						if (copy) {
							NodeRef newDLNodeRef = copyService.copy(sourceListNodeRef, targetListContainerNodeRef, ContentModel.ASSOC_CONTAINS, DataListModel.TYPE_DATALIST, true);
							nodeService.setProperty(newDLNodeRef, ContentModel.PROP_NAME, listQName.getLocalName());
						}
					}
				}
			}
		}
		
	}

	/**
	 * Get the manual ListItems
	 * 
	 * @param listNodeRef
	 * @return
	 */
	@Override
	public List<NodeRef> getManualListItems(NodeRef listNodeRef, QName listQName) {

		String query = String.format(QUERY_LIST_ITEM, listNodeRef, listQName);
		query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_IS_MANUAL_LISTITEM, Boolean.TRUE.toString(), Operator.AND);
			
		return beCPGSearchService.luceneSearch(query);
	}

	@Override
	public NodeRef createListItem(NodeRef listNodeRef, QName listType, Map<QName, Serializable> properties,
			Map<QName, List<NodeRef>> associations) {
		
		// create
		NodeRef nodeRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS,
				ContentModel.ASSOC_CHILDREN, listType, properties).getChildRef();
		
		for(Map.Entry<QName, List<NodeRef>> kv : associations.entrySet()){
			associationService.update(nodeRef, kv.getKey(), kv.getValue());
		}
		
		return nodeRef;
	}

	@Override
	public List<NodeRef> getListItems(NodeRef listNodeRef, QName listQName) {
				
		return beCPGSearchService.luceneSearch(String.format(QUERY_LIST_ITEM, listNodeRef, listQName), LuceneHelper.getSort(BeCPGModel.PROP_SORT), RepoConsts.MAX_RESULTS_256);
	}

	@Override
	public void moveDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		NodeRef sourceListContainerNodeRef = getListContainer(sourceNodeRef);
		NodeRef targetListContainerNodeRef = getListContainer(targetNodeRef);

		if (sourceListContainerNodeRef != null) {
			if (targetListContainerNodeRef != null) {
				nodeService.deleteNode(targetListContainerNodeRef);
			}
			
			if(logger.isDebugEnabled()){
				logger.debug("Move :"+nodeService.getPath(sourceListContainerNodeRef)+" to "+nodeService.getPath(targetNodeRef));
			}
			
			nodeService.moveNode(sourceListContainerNodeRef, targetNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, BeCPGModel.ASSOC_ENTITYLISTS);
			
			
		}
		
	}
}
