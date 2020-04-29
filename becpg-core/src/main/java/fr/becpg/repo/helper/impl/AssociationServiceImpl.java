/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class AssociationServiceImpl extends AbstractBeCPGPolicy implements AssociationService, NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnCreateChildAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy,
		NodeServicePolicies.OnDeleteChildAssociationPolicy, NodeServicePolicies.OnDeleteNodePolicy, CheckOutCheckInServicePolicies.OnCheckIn {

	private static final Log logger = LogFactory.getLog(AssociationServiceImpl.class);

	private BeCPGCacheService beCPGCacheService;

	private static Set<QName> ignoredAssocs = new HashSet<>();

	static {
		ignoredAssocs.add(ContentModel.ASSOC_ORIGINAL);
	}

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	@Override
	public void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs, boolean resetCache) {

		List<AssociationRef> dbAssocNodeRefs = getTargetAssocsImpl(nodeRef, qName, false);
		List<NodeRef> dbTargetNodeRefs = new ArrayList<>();

		boolean hasChanged = resetCache;

		if (dbAssocNodeRefs != null) {
			// remove from db

			for (AssociationRef assocRef : dbAssocNodeRefs) {
				if (assocNodeRefs == null) {
					nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), qName);
					hasChanged = true;
				} else if (!assocNodeRefs.contains(assocRef.getTargetRef())) {
					nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), qName);
					hasChanged = true;
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
					dbTargetNodeRefs.add(n);
					hasChanged = true;
					nodeService.createAssociation(nodeRef, n, qName);
				}
			}
		}
		if (hasChanged) {
			removeCachedAssoc(assocCacheName(), nodeRef, qName);
		}
		
	}
	
	@Override
	public void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs) {
		update(nodeRef, qName, assocNodeRefs, false);
	}

	@Override
	public void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef) {

		List<AssociationRef> assocRefs = getTargetAssocsImpl(nodeRef, qName, false);
		boolean hasChanged = false;

		boolean createAssoc = true;
		if (!assocRefs.isEmpty() && (assocRefs.get(0).getTargetRef() != null)) {
			if (assocRefs.get(0).getTargetRef().equals(assocNodeRef)) {
				createAssoc = false;
			} else {
				hasChanged = true;
				nodeService.removeAssociation(nodeRef, assocRefs.get(0).getTargetRef(), qName);
			}
		}

		if (createAssoc && (assocNodeRef != null)) {
			hasChanged = true;
			nodeService.createAssociation(nodeRef, assocNodeRef, qName);
		}

		if (hasChanged) {
			removeCachedAssoc(assocCacheName(), nodeRef, qName);
		}

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
		return (assocRefs != null) && !assocRefs.isEmpty() ? assocRefs.get(0).getTargetRef() : null;
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

		return beCPGCacheService.getFromCache(cacheName, cacheKey, () -> nodeService.getTargetAssocs(nodeRef, qName), true);

	}

	private List<ChildAssociationRef> getChildAssocsImpl(final NodeRef nodeRef, final QName qName) {
		final String cacheKey = createCacheKey(nodeRef, qName);
		final String cacheName = childAssocCacheName();

		return beCPGCacheService.getFromCache(cacheName, cacheKey, () -> nodeService.getChildAssocs(nodeRef, qName, RegexQNamePattern.MATCH_ALL),
				true);

	}

	@Override
	public String createCacheKey(NodeRef nodeRef, QName qName) {
		return nodeRef.toString() + "-" + qName.toString();
	}

	@Override
	public List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName, boolean fromCache) {
		List<AssociationRef> assocRefs = getTargetAssocsImpl(nodeRef, qName, fromCache);
		List<NodeRef> listItems = new LinkedList<>();
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
		List<NodeRef> listItems = new LinkedList<>();
		for (AssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getSourceRef());
		}

		return listItems;
	}
	
//
//	@Autowired
//	@Qualifier("dataSource")
//	private DataSource dataSource;
//	
//
//	@Autowired
//	private TenantService tenantService;

	
	/**
	 * ChildAssociationRef.getChildRef() --> dataListItem
	 * ChildAssociationRef.getParentRef() --> dataListItem
	 * @param assocs
	 * @param assocName
	 * @param orOperator
	 * @return
	 */
	@Override
	public List<AssociationRef> getEntitySourceAssocs(List<NodeRef> nodeRefs, QNamePattern assocQName, boolean isOrOperator){
		List<AssociationRef> ret = new ArrayList<>();
//		
		for (NodeRef nodeRef : nodeRefs) {

			if (nodeService.exists(nodeRef)) {

				List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, assocQName);

				// remove nodes that don't respect the
				// assoc_ criteria

				ret.addAll(assocRefs);

			}

		}
//		if (!isOROperand) {
//			nodes.retainAll(nodesToKeep);
//		} else {
//			nodesToKeepOr.addAll(nodesToKeep);
//		}
		
		
//
//		StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
//		if (AuthenticationUtil.isMtEnabled()) {
//			storeRef = tenantService.getName(storeRef);
//		}
//		
//		
//		from 
//		  alf_child_assoc dataListItemAssoc 
//		  join alf_child_assoc dataListAssoc on dataListAssoc.child_node_id = dataListItemAssoc.parent_node_id
//		  join alf_node entity  on  dataListAssoc.parent_node_id = entity.id
//		  
//        where
//           dataListItemAssoc.id = (nodeRef.getId)
//        and entity.store_id=(select id from alf_store where protocol='" + storeRef.getProtocol() + "' and identifier='"
//				+ storeRef.getIdentifier() + "') " 
//        
//		
//		
//		select
//	        assoc.id                    as id,
//	        parentNode.id               as parentNodeId,
//	        parentNode.version          as parentNodeVersion,
//	        parentStore.protocol        as parentNodeProtocol,
//	        parentStore.identifier      as parentNodeIdentifier,
//	        parentNode.uuid             as parentNodeUuid,
//	        childNode.id                as childNodeId,
//	        childNode.version           as childNodeVersion,
//	        childStore.protocol         as childNodeProtocol,
//	        childStore.identifier       as childNodeIdentifier,
//	        childNode.uuid              as childNodeUuid,
//	        assoc.type_qname_id         as type_qname_id,
//	        assoc.child_node_name_crc   as child_node_name_crc,
//	        assoc.child_node_name       as child_node_name,
//	        assoc.qname_ns_id           as qname_ns_id,
//	        assoc.qname_localname       as qname_localname,
//	        assoc.is_primary            as is_primary,
//	        assoc.assoc_index           as assoc_index
//	    from
//            alf_child_assoc assoc
//            join alf_node parentNode on (parentNode.id = assoc.parent_node_id)
//            join alf_store parentStore on (parentStore.id = parentNode.store_id)
//            join alf_node childNode on (childNode.id = assoc.child_node_id)
//            left join alf_store childStore on (childStore.id = childNode.store_id)
//         where
//            childNode.id = #{childNode.id}
//		
//            alf_node.store_id=(select id from alf_store where protocol='" + storeRef.getProtocol() + "' and identifier='"
//				+ storeRef.getIdentifier() + "') " 
//		
//		
//		
//		
//            and assoc.parent_node_id = #{parentNode.id}
//            <if test="qnameNamespaceId != null">and assoc.qname_ns_id = #{qnameNamespaceId}</if>
//            <if test="qnameLocalName != null">and assoc.qname_localname = #{qnameLocalName}</if>
//            and assoc.is_primary = true
		
		return ret;
	}
	

	

	@Override
	public NodeRef getChildAssoc(NodeRef nodeRef, QName qName) {
		List<ChildAssociationRef> assocRefs = getChildAssocsImpl(nodeRef, qName);
		return (assocRefs != null) && !assocRefs.isEmpty() ? assocRefs.get(0).getChildRef() : null;
	}

	@Override
	public List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName) {
		List<ChildAssociationRef> assocRefs = getChildAssocsImpl(nodeRef, qName);
		List<NodeRef> listItems = new LinkedList<>();
		for (ChildAssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getChildRef());
		}

		return listItems;
	}

	@Override
	public void doInit() {
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ContentModel.TYPE_AUTHORITY,
				new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ContentModel.TYPE_AUTHORITY,
				new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, ContentModel.TYPE_AUTHORITY,
				new JavaBehaviour(this, "onCreateChildAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteChildAssociationPolicy.QNAME, ContentModel.TYPE_AUTHORITY,
				new JavaBehaviour(this, "onDeleteChildAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onCreateChildAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteChildAssociationPolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onDeleteChildAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, ContentModel.TYPE_AUTHORITY,
				new JavaBehaviour(this, "onDeleteNode"));

		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, ContentModel.TYPE_AUTHORITY,
				new JavaBehaviour(this, "onCheckIn"));
		
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
		if (!ignoredAssocs.contains(associationRef.getTypeQName())) {
			removeCachedAssoc(assocCacheName(), associationRef.getSourceRef(), associationRef.getTypeQName());
		}
	}

	@Override
	public void onDeleteChildAssociation(ChildAssociationRef associationRef) {
		logger.debug("onDeleteChildAssociation: "+associationRef.getTypeQName());
		removeCachedAssoc(childAssocCacheName(), associationRef.getParentRef(), associationRef.getTypeQName());
		

	}

	@Override
	public void onCreateChildAssociation(ChildAssociationRef associationRef, boolean arg1) {
		logger.debug("onCreateChildAssociation: "+associationRef.getTypeQName());
		removeCachedAssoc(childAssocCacheName(), associationRef.getParentRef(), associationRef.getTypeQName());

	}

	@Override
	public void onDeleteNode(ChildAssociationRef associationRef, boolean arg1) {
		logger.debug("onDeleteNode: "+associationRef.getTypeQName());
       
		removeCachedAssoc(childAssocCacheName(), associationRef.getParentRef(), associationRef.getTypeQName());
	}

	@Override
	public void onCheckIn(NodeRef nodeRef) {
		// Bad but not so often
		for (String cacheName : Arrays.asList(assocCacheName(), childAssocCacheName())) {
			for (String cacheKey : beCPGCacheService.getCacheKeys(cacheName)) {
				if (cacheKey.startsWith(nodeRef.toString())) {
					if (logger.isDebugEnabled()) {
						logger.debug("In checkin delete:" + cacheKey);
					}

					beCPGCacheService.removeFromCache(cacheName, cacheKey);
				}
			}
		}

	}

}
