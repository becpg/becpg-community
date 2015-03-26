/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.helper.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class AssociationServiceImpl extends AbstractBeCPGPolicy implements AssociationService, NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnCreateChildAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnDeleteChildAssociationPolicy,
		NodeServicePolicies.OnDeleteNodePolicy, CheckOutCheckInServicePolicies.OnCheckIn {

	private static Log logger = LogFactory.getLog(AssociationServiceImpl.class);

	private BeCPGCacheService beCPGCacheService;

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	@Override
	public void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs) {

		List<AssociationRef> dbAssocNodeRefs = getTargetAssocsImpl(nodeRef, qName, false);
		List<NodeRef> dbTargetNodeRefs = new ArrayList<NodeRef>();

		if (dbAssocNodeRefs != null) {
			// remove from db
			
			for (AssociationRef assocRef : dbAssocNodeRefs) {
				if (assocNodeRefs == null) {
					nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), qName);
				} else if (!assocNodeRefs.contains(assocRef.getTargetRef())) {
					nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), qName);
				} else {
					dbTargetNodeRefs.add(assocRef.getTargetRef());// already in
																	// // db
				}
			}
		}

		// add nodes that are not in db
		if (assocNodeRefs != null) {
			for (NodeRef n : assocNodeRefs) {
				if (!dbTargetNodeRefs.contains(n) && nodeService.exists(n)) {
					nodeService.createAssociation(nodeRef, n, qName);
				}
			}
		}

		removeCachedAssoc(assocCacheName(), nodeRef, qName);
	}

	@Override
	public void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef) {

		List<AssociationRef> assocRefs = getTargetAssocsImpl(nodeRef, qName, false);

		boolean createAssoc = true;
		if (!assocRefs.isEmpty() && assocRefs.get(0).getTargetRef() != null) {
			if (assocRefs.get(0).getTargetRef().equals(assocNodeRef)) {
				createAssoc = false;
			} else {
				nodeService.removeAssociation(nodeRef, assocRefs.get(0).getTargetRef(), qName);
			}
		}

		if (createAssoc && assocNodeRef != null) {
			nodeService.createAssociation(nodeRef, assocNodeRef, qName);
		}

		removeCachedAssoc(assocCacheName(), nodeRef, qName);

	}

	private void removeCachedAssoc(String cacheName, NodeRef nodeRef, QName qName) {
		logger.debug("Remove assoc from  " + cacheName + " " + createCacheKey(nodeRef, qName));

		beCPGCacheService.removeFromCache(cacheName, createCacheKey(nodeRef, qName));
	}

	@Override
	public NodeRef getTargetAssoc(NodeRef nodeRef, QName qName) {
		return getTargetAssoc(nodeRef, qName, true);
	}

	@Override
	public NodeRef getTargetAssoc(NodeRef nodeRef, QName qName, boolean fromCache) {
		List<AssociationRef> assocRefs = getTargetAssocsImpl(nodeRef, qName, fromCache);
		return assocRefs != null && !assocRefs.isEmpty() ? assocRefs.get(0).getTargetRef() : null;
	}

	/**
	 * Cache targetAssocs as alfresco doesn't
	 */
	private List<AssociationRef> getTargetAssocsImpl(final NodeRef nodeRef, final QName qName, boolean fromCache) {

		if (!fromCache) {
			return nodeService.getTargetAssocs(nodeRef, qName);
		}

		final String cacheKey = createCacheKey(nodeRef, qName);
		final String cacheName = assocCacheName();

		return beCPGCacheService.getFromCache(cacheName, cacheKey, new BeCPGCacheDataProviderCallBack<List<AssociationRef>>() {
			public List<AssociationRef> getData() {

				return nodeService.getTargetAssocs(nodeRef, qName);
			}
		}, true);

	}

	private List<ChildAssociationRef> getChildAssocsImpl(final NodeRef nodeRef, final QName qName) {
		final String cacheKey = createCacheKey(nodeRef, qName);
		final String cacheName = childAssocCacheName();

		return beCPGCacheService.getFromCache(cacheName, cacheKey, new BeCPGCacheDataProviderCallBack<List<ChildAssociationRef>>() {
			public List<ChildAssociationRef> getData() {

				return nodeService.getChildAssocs(nodeRef, qName, RegexQNamePattern.MATCH_ALL);
			}
		}, true);

	}

	@Override
	public String createCacheKey(NodeRef nodeRef, QName qName) {
		return nodeRef.toString() + "-" + qName.toString();
	}

	@Override
	public List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName, boolean fromCache) {
		List<AssociationRef> assocRefs = getTargetAssocsImpl(nodeRef, qName, fromCache);
		List<NodeRef> listItems = new LinkedList<NodeRef>();
		for (AssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getTargetRef());
		}

		return listItems;
	}

	@Override
	public List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName) {
		return getTargetAssocs(nodeRef, qName, true);
	}

	@Override
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QNamePattern qName) {
		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, qName);
		List<NodeRef> listItems = new LinkedList<NodeRef>();
		for (AssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getSourceRef());
		}

		return listItems;
	}

	@Override
	public NodeRef getChildAssoc(NodeRef nodeRef, QName qName) {
		List<ChildAssociationRef> assocRefs = getChildAssocsImpl(nodeRef, qName);
		return assocRefs != null && !assocRefs.isEmpty() ? assocRefs.get(0).getChildRef() : null;
	}

	@Override
	public List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName) {
		List<ChildAssociationRef> assocRefs = getChildAssocsImpl(nodeRef, qName);
		List<NodeRef> listItems = new LinkedList<NodeRef>();
		for (ChildAssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getChildRef());
		}

		return listItems;
	}

	@Override
	public void doInit() {
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this,
				"onCreateChildAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteChildAssociationPolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this,
				"onDeleteChildAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this,
				"onCreateChildAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteChildAssociationPolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this,
				"onDeleteChildAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "onDeleteNode"));

		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "onCheckIn"));

	}

	private String assocCacheName() {
		return AssociationService.class.getName() + ".assocs";
	}

	private String childAssocCacheName() {
		return AssociationService.class.getName() + ".childs";
	}

	@Override
	public void onDeleteAssociation(AssociationRef associationRef) {
		logger.debug("onDeleteAssociation");
		removeCachedAssoc(assocCacheName(), associationRef.getSourceRef(), associationRef.getTypeQName());
	}

	@Override
	public void onCreateAssociation(AssociationRef associationRef) {
		logger.debug("onCreateAssociation");
		removeCachedAssoc(assocCacheName(), associationRef.getSourceRef(), associationRef.getTypeQName());
	}

	@Override
	public void onDeleteChildAssociation(ChildAssociationRef associationRef) {
		logger.debug("onDeleteChildAssociation");
		removeCachedAssoc(childAssocCacheName(), associationRef.getParentRef(), associationRef.getTypeQName());

	}

	@Override
	public void onCreateChildAssociation(ChildAssociationRef associationRef, boolean arg1) {
		logger.debug("onCreateChildAssociation");
		removeCachedAssoc(childAssocCacheName(), associationRef.getParentRef(), associationRef.getTypeQName());

	}

	@Override
	public void onDeleteNode(ChildAssociationRef associationRef, boolean arg1) {
		logger.debug("onDeleteNode");
		// TODO test est appels onDeleteAssociation

		removeCachedAssoc(childAssocCacheName(), associationRef.getParentRef(), associationRef.getTypeQName());
	}

	@Override
	public void onCheckIn(NodeRef nodeRef) {
		// Bad but not so often
		for (String cacheName : Arrays.asList(assocCacheName(), childAssocCacheName())) {
			for (String cacheKey : beCPGCacheService.getCacheKeys(cacheName)) {
				if (cacheKey.startsWith(nodeRef.toString())) {
					if(logger.isDebugEnabled()){
						logger.debug("In checkin delete:"+cacheKey);
					}
					
					beCPGCacheService.removeFromCache(cacheName, cacheKey);
				}
			}
		}

	}

}
