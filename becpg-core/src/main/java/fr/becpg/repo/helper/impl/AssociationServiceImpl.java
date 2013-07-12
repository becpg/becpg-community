package fr.becpg.repo.helper.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.stereotype.Service;

import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.AssociationService;

@Service
public class AssociationServiceImpl implements AssociationService {

	private NodeService nodeService;

	private BeCPGCacheService beCPGCacheService;

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs) {

		List<AssociationRef> dbAssocNodeRefs = getTargetAssocsImpl(nodeRef, qName);
		List<NodeRef> dbTargetNodeRefs = new ArrayList<NodeRef>();

		boolean invalidCache = false;

		if (dbAssocNodeRefs != null) {
			// remove from db
			for (AssociationRef assocRef : dbAssocNodeRefs) {
				if (assocNodeRefs == null) {
					nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), qName);
					invalidCache = true;
				} else if (!assocNodeRefs.contains(assocRef.getTargetRef())) {
					nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), qName);
					invalidCache = true;
				} else {
					dbTargetNodeRefs.add(assocRef.getTargetRef());// already in
																	// db
				}
			}
		}

		// add nodes that are not in db
		if (assocNodeRefs != null) {
			for (NodeRef n : assocNodeRefs) {
				if (!dbTargetNodeRefs.contains(n) && nodeService.exists(n)) {
					nodeService.createAssociation(nodeRef, n, qName);
					invalidCache = true;
				}
			}
		}

		if (invalidCache) {
			beCPGCacheService.removeFromCache(AssociationService.class.getName(), createCacheKey(nodeRef, qName));
		}
	}

	@Override
	public void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef) {

		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, qName);

		boolean createAssoc = true;
		boolean invalidCache = false;
		if (!assocRefs.isEmpty() && assocRefs.get(0).getTargetRef() != null) {
			if (assocRefs.get(0).getTargetRef().equals(assocNodeRef)) {
				createAssoc = false;
			} else {
				nodeService.removeAssociation(nodeRef, assocRefs.get(0).getTargetRef(), qName);
				invalidCache = true;
			}
		}

		if (createAssoc && assocNodeRef != null) {
			nodeService.createAssociation(nodeRef, assocNodeRef, qName);
			invalidCache = true;
		}

		if (invalidCache) {
			beCPGCacheService.removeFromCache(AssociationService.class.getName(), createCacheKey(nodeRef, qName));
		}
	}

	@Override
	public NodeRef getTargetAssoc(NodeRef nodeRef, QName qName) {
		List<AssociationRef> assocRefs = getTargetAssocsImpl(nodeRef, qName);
		return assocRefs != null && !assocRefs.isEmpty() ? assocRefs.get(0).getTargetRef() : null;
	}

	/**
	 * Cache targetAssocs as alfresco doesn't
	 */
	private List<AssociationRef> getTargetAssocsImpl(final NodeRef nodeRef, final QName qName) {

		final Date dateModified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);

		return beCPGCacheService.getFromCache(AssociationService.class.getName(), createCacheKey(nodeRef, qName), new BeCPGCacheDataProviderCallBack<List<AssociationRef>>() {
			public List<AssociationRef> getData() {
				return nodeService.getTargetAssocs(nodeRef, qName);

			}
		}, dateModified.getTime());

	}

	private List<ChildAssociationRef> getChildAssocsImpl(final NodeRef nodeRef, final QName qName, final QNamePattern qNamePattern) {
		final Date dateModified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);

		return beCPGCacheService.getFromCache(AssociationService.class.getName(), createCacheKey(nodeRef, qName), new BeCPGCacheDataProviderCallBack<List<ChildAssociationRef>>() {
			public List<ChildAssociationRef> getData() {
				return nodeService.getChildAssocs(nodeRef, qName, qNamePattern);

			}
		}, dateModified.getTime());

	}

	private String createCacheKey(NodeRef nodeRef, QName qName) {
		return nodeRef.toString() + "-" + qName.toString();
	}

	@Override
	public List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName) {
		List<AssociationRef> assocRefs = getTargetAssocsImpl(nodeRef, qName);
		List<NodeRef> listItems = new ArrayList<NodeRef>(assocRefs.size());
		for (AssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getTargetRef());
		}

		return listItems;
	}

	@Override
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qName) {
		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, qName);
		List<NodeRef> listItems = new ArrayList<NodeRef>(assocRefs.size());
		for (AssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getSourceRef());
		}

		return listItems;
	}

	@Override
	public NodeRef getChildAssoc(NodeRef nodeRef, QName qName) {
		List<ChildAssociationRef> assocRefs = getChildAssocsImpl(nodeRef, qName, RegexQNamePattern.MATCH_ALL);
		return assocRefs != null && !assocRefs.isEmpty() ? assocRefs.get(0).getChildRef() : null;
	}
	

	@Override
	public List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName) {
		List<ChildAssociationRef> assocRefs = getChildAssocsImpl(nodeRef, qName, RegexQNamePattern.MATCH_ALL);
		List<NodeRef> listItems = new ArrayList<NodeRef>(assocRefs.size());
		for (ChildAssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getChildRef());
		}

		return listItems;
	}

}
