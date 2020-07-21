/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class AssociationServiceImpl extends AbstractBeCPGPolicy implements AssociationService, NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnCreateChildAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy,
		NodeServicePolicies.OnDeleteChildAssociationPolicy, NodeServicePolicies.OnDeleteNodePolicy, CheckOutCheckInServicePolicies.OnCheckIn {

	private static final Log logger = LogFactory.getLog(AssociationServiceImpl.class);

	private BeCPGCacheService beCPGCacheService;

	private EntityDictionaryService entityDictionaryService;

	private DataSource dataSource;

	private TenantService tenantService;

	private NamespaceService namespaceService;

	private static Set<QName> ignoredAssocs = new HashSet<>();

	private Set<String> cacheNames = ConcurrentHashMap.newKeySet();

	private QNameDAO qnameDAO;

	static {
		ignoredAssocs.add(ContentModel.ASSOC_ORIGINAL);
	}

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
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
		if (logger.isDebugEnabled()) {
			logger.debug("Remove assoc from  " + cacheName + " " + createCacheKey(nodeRef, qName));
		}
		String key = createCacheKey(nodeRef, qName);

		beCPGCacheService.removeFromCache(cacheName, key);

		for (String tmp : cacheNames) {
			if (tmp.startsWith(key)) {
				beCPGCacheService.removeFromCache(cacheName, tmp);
			}
		}

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

	@Override
	public NodeRef getChildAssoc(NodeRef nodeRef, QName qName) {
		List<NodeRef> assocRefs = getChildAssocsImpl(nodeRef, qName, null, null);
		return (assocRefs != null) && !assocRefs.isEmpty() ? assocRefs.get(0) : null;
	}

	@Override
	public List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName) {
		return getChildAssocs(nodeRef, qName, null);
	}

	@Override
	public List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName, QName childTypeQName) {
		return getChildAssocsImpl(nodeRef, qName, childTypeQName, null);
	}

	@Override
	public List<NodeRef> getChildAssocs(NodeRef listNodeRef, QName qName, QName childTypeQName, Map<String, Boolean> sortMap) {
		return getChildAssocsImpl(listNodeRef, qName, childTypeQName, sortMap);
	}

	private List<NodeRef> getChildAssocsImpl(final NodeRef nodeRef, final QName qName, final QName childType, final Map<String, Boolean> sortProps) {

		if ((childType != null) && (sortProps != null) && !sortProps.isEmpty() && !isDefaultSort(sortProps)) {
			// No cache if specific sort
			return dbChildAssocSearch(nodeRef, qName, childType, sortProps);
		}

		final String cacheKey = createCacheKey(nodeRef, qName, childType);
		final String cacheName = childAssocCacheName();

		List<NodeRef> ret = beCPGCacheService.getFromCache(cacheName, cacheKey, () -> {

			if ((childType != null) && (sortProps != null) && !sortProps.isEmpty()) {
				return dbChildAssocSearch(nodeRef, qName, childType, sortProps);
			}

			return nodeService.getChildAssocs(nodeRef, qName, RegexQNamePattern.MATCH_ALL, true).stream()
					.filter(n -> (childType == null) || nodeService.getType(n.getChildRef()).equals(childType))
					.map(assocRef -> assocRef.getChildRef()).collect(Collectors.toCollection(LinkedList::new));

		}, true);

		ret.sort(new CommonDataListSort(nodeService));

		return ret;
	}

	private boolean isDefaultSort(Map<String, Boolean> sortProps) {
		return (sortProps != null) && (sortProps.get("@bcpg:sort") != null) && sortProps.get("@bcpg:sort") && (sortProps.get("@cm:created") != null)
				&& sortProps.get("@cm:created");
	}

	private List<NodeRef> dbChildAssocSearch(final NodeRef nodeRef, final QName qName, final QName childType, Map<String, Boolean> sortProps) {

		List<NodeRef> ret = new LinkedList<>();
		QName sortFieldQName = null;
		String sortDirection = null;
		String createSortDirection = "ASC";

		StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
		if (AuthenticationUtil.isMtEnabled()) {
			storeRef = tenantService.getName(storeRef);
		}

		int count = 0;
		for (Map.Entry<String, Boolean> entry : sortProps.entrySet()) {
			if ("cm:created".equals(entry.getKey().replace("@", ""))
					|| ("{http://www.alfresco.org/model/content/1.0}created").equals(entry.getKey().replace("@", ""))) {
				createSortDirection = entry.getValue() ? "ASC" : "DESC";
			} else {
				if (count > 0) {
					logger.warn("Only one sort dir is allowed in getChildAssocsImplV2");
					break;
				}

				if (entry.getKey().indexOf(QName.NAMESPACE_BEGIN) != -1) {
					sortFieldQName = QName.createQName(entry.getKey().replace("@", ""));
				} else {
					sortFieldQName = QName.createQName(entry.getKey().replace("@", ""), namespaceService);
				}
				sortDirection = entry.getValue() ? "ASC" : "DESC";
				count++;
			}
		}

		String sql = "select alf_node.uuid, alf_node.audit_created from alf_node ";

		String sortOrderSql = " order by alf_node.audit_created " + createSortDirection;

		if ((sortFieldQName != null) && (sortDirection != null)) {
			DataTypeDefinition dateType = entityDictionaryService.getProperty(sortFieldQName).getDataType();
			String fieldType = "string_value";

			if (DataTypeDefinition.INT.equals(dateType.getName()) || DataTypeDefinition.LONG.equals(dateType.getName())) {
				fieldType = "long_value";
			} else if (DataTypeDefinition.DOUBLE.equals(dateType.getName())) {
				fieldType = "double_value";
			} else if (DataTypeDefinition.FLOAT.equals(dateType.getName())) {
				fieldType = "float_value";
			} else if (DataTypeDefinition.BOOLEAN.equals(dateType.getName())) {
				fieldType = "boolean_value";
			}

			sql = "select alf_node.uuid, alf_node_properties." + fieldType + ", alf_node.audit_created " + "from alf_node "
					+ "left join alf_node_properties " + "on (alf_node_properties.node_id = alf_node.id "
					+ "and alf_node_properties.qname_id=(select id from alf_qname " + "where ns_id=(select id from alf_namespace where uri='"
					+ sortFieldQName.getNamespaceURI() + "') " + "and local_name='" + sortFieldQName.getLocalName() + "') " + ") ";

			sortOrderSql = " order by alf_node_properties." + fieldType + " " + sortDirection + ", alf_node.audit_created " + createSortDirection;

		}

		sql += "where alf_node.store_id=(select id from alf_store where protocol='" + storeRef.getProtocol() + "' and identifier='"
				+ storeRef.getIdentifier() + "') " + "and alf_node.type_qname_id=(select id from alf_qname "
				+ "where ns_id=(select id from alf_namespace where uri='" + childType.getNamespaceURI() + "') " + "and local_name='"
				+ childType.getLocalName() + "') " + "and id in (select child_node_id from alf_child_assoc where "
				+ "parent_node_id = (select id from alf_node where uuid='" + nodeRef.getId() + "'"
				+ " and store_id=(select id from alf_store where protocol='" + storeRef.getProtocol() + "'" + " and identifier='"
				+ storeRef.getIdentifier() + "') )) ";

		sql += sortOrderSql;

		if (logger.isTraceEnabled()) {
			logger.trace("Searching with: " + sql);
		}
		try (Connection con = dataSource.getConnection()) {

			try (PreparedStatement statement = con.prepareStatement(sql)) {
				try (java.sql.ResultSet res = statement.executeQuery()) {
					while (res.next()) {
						NodeRef tmp = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, res.getString("uuid"));
						if (nodeService.exists(nodeRef)) {
							ret.add(tmp);
						}
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error running : " + sql, e);
		}

		return ret;

	}

	private String createCacheKey(NodeRef nodeRef, QName qName) {
		return nodeRef.toString() + "-" + qName.toString();
	}

	private String createCacheKey(NodeRef nodeRef, QName qName, QNamePattern qNamepattern) {
		String cacheKey = createCacheKey(nodeRef, qName);

		if ((qNamepattern == null) || RegexQNamePattern.MATCH_ALL.equals(qNamepattern)) {
			return cacheKey;
		}

		String ret = cacheKey + "-" + qNamepattern.toString();
		cacheNames.add(ret);

		return ret;
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
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QNamePattern qNamePattern) {
		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, qNamePattern);
		List<NodeRef> listItems = new LinkedList<>();
		for (AssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getSourceRef());
		}
		return listItems;
	}

	public class EntitySourceAssoc {
		private NodeRef entityNodeRef;
		private NodeRef dataListItemNodeRef;
		private NodeRef sourceNodeRef;

		public EntitySourceAssoc(NodeRef entityNodeRef, NodeRef dataListItemNodeRef, NodeRef sourceNodeRef) {
			super();
			this.entityNodeRef = entityNodeRef;
			this.dataListItemNodeRef = dataListItemNodeRef;
			this.sourceNodeRef = sourceNodeRef;
		}

		public NodeRef getEntityNodeRef() {
			return entityNodeRef;
		}

		public NodeRef getDataListItemNodeRef() {
			return dataListItemNodeRef;
		}

		public NodeRef getSourceNodeRef() {
			return sourceNodeRef;
		}

	}

	private static final String SQL_SELECT_SOURCE_ASSOC_ENTITY = "select entity.uuid as entity, dataListItem.uuid as dataListItem, dataListItem.type_qname_id as dataListItemType, targetNode.uuid as targetNode"
			+ " from alf_node entity " + " join alf_child_assoc dataListContainerAssoc on (entity.id = dataListContainerAssoc.parent_node_id)"
			+ " join alf_child_assoc dataListAssoc on (dataListAssoc.parent_node_id = dataListContainerAssoc.child_node_id)"
			+ " join alf_child_assoc dataListItemAssoc on (dataListItemAssoc.parent_node_id = dataListAssoc.child_node_id)"
			+ " join alf_node dataListItem on (dataListItem.id = dataListItemAssoc.child_node_id ) "
			+ " join alf_node_assoc assoc on ( dataListItem.id = assoc.source_node_id ) "
			+ " join alf_node targetNode on (targetNode.id = assoc.target_node_id) "
			+ " join alf_store targetNodeStore on (  targetNodeStore.id = targetNode.store_id and targetNodeStore.protocol= ? and targetNodeStore.identifier=?) "
			+ " left join alf_node_aspects q1 on (q1.qname_id = ? and q1.node_id=entity.id)"
			+ " left join alf_node_aspects q2 on (q2.qname_id = ? and q2.node_id=dataListItem.id)"
			+ " where  assoc.type_qname_id=?  "
			+ " and q1.qname_id IS NULL and q2.qname_id IS NULL ";


	@Override
	public List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocTypeQName, boolean isOrOperator) {
		List<EntitySourceAssoc> ret = null;

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		if ((nodeRefs != null) && !nodeRefs.isEmpty()) {

			boolean isAnd = !isOrOperator && (nodeRefs.size() > 1);

			if (isAnd) {
				for (NodeRef nodeRef : nodeRefs) {
					if (ret == null) {
						ret = internalEntitySourceAssocs(Arrays.asList(nodeRef), assocTypeQName);
					} else {
						// TODO make it in DB
						List<EntitySourceAssoc> tmp = internalEntitySourceAssocs(Arrays.asList(nodeRef), assocTypeQName);

						for (Iterator<EntitySourceAssoc> iterator = ret.iterator(); iterator.hasNext();) {
							EntitySourceAssoc entitySourceAssoc = iterator.next();
							boolean remove = true;
							for (EntitySourceAssoc toAdd : tmp) {
								if (toAdd.getEntityNodeRef().equals(entitySourceAssoc.getEntityNodeRef())) {
									remove = false;
								}
							}
							if (remove) {
								iterator.remove();
							}
						}

					}
				}

			} else {
				ret = internalEntitySourceAssocs(nodeRefs, assocTypeQName);
			}

		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getEntitySourceAssocs  takes " + watch.getTotalTimeSeconds() + " seconds");
		}

		return ret;
	}

	private List<EntitySourceAssoc> internalEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocTypeQName) {
		List<EntitySourceAssoc> ret = new ArrayList<>();

		if ((nodeRefs != null) && !nodeRefs.isEmpty()) {

			StringBuilder query = new StringBuilder();

			query.append(SQL_SELECT_SOURCE_ASSOC_ENTITY);

			query.append(" and ( ");

			boolean isFirst = true;
			for (NodeRef nodeRef : nodeRefs) {
				if (!isFirst) {
					query.append(" or ");
				}
				query.append("targetNode.uuid='");
				query.append(nodeRef.getId());
				query.append("'");
				isFirst = false;

			}
			query.append(")");
			query.append("  group by dataListItem.uuid ");

			Long typeQNameId = null;
			Long aspectQnameId = qnameDAO.getQName(BeCPGModel.ASPECT_COMPOSITE_VERSION).getFirst();
			if (assocTypeQName != null) {
				Pair<Long, QName> typeQNamePair = qnameDAO.getQName(assocTypeQName);
				if (typeQNamePair == null) {
					// No such QName
					return Collections.emptyList();
				}
				typeQNameId = typeQNamePair.getFirst();
			}

			StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
			if (AuthenticationUtil.isMtEnabled()) {
				storeRef = tenantService.getName(storeRef);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Run query:" + query.toString() + " " + typeQNameId + " " + aspectQnameId + " " + storeRef.getProtocol() + " "
						+ storeRef.getIdentifier());
			}

			try (Connection con = dataSource.getConnection()) {

				try (PreparedStatement statement = con.prepareStatement(query.toString())) {

					statement.setString(1, storeRef.getProtocol());
					statement.setString(2, storeRef.getIdentifier());
					statement.setLong(3, aspectQnameId);
					statement.setLong(4, aspectQnameId);
					statement.setLong(5, typeQNameId);
					
					try (java.sql.ResultSet res = statement.executeQuery()) {
						while (res.next()) {
							NodeRef entityNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, res.getString("entity"));
							NodeRef sourceNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, res.getString("targetNode"));
							NodeRef dataListItemNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, res.getString("dataListItem"));
							Pair<Long, QName> entityType = qnameDAO.getQName(res.getLong("dataListItemType"));
							if (!entityDictionaryService.isSubClass(entityType.getSecond(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
								entityNodeRef = dataListItemNodeRef;
							}

							ret.add(new EntitySourceAssoc(entityNodeRef, dataListItemNodeRef, sourceNodeRef));
						}

					}
				}
			} catch (SQLException e) {
				logger.error(e, e);
			}

		}
		return ret;
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
		logger.debug("onDeleteChildAssociation: " + associationRef.getTypeQName());

		removeCachedAssoc(childAssocCacheName(), associationRef.getParentRef(), associationRef.getTypeQName());

	}

	@Override
	public void onCreateChildAssociation(ChildAssociationRef associationRef, boolean arg1) {
		logger.debug("onCreateChildAssociation: " + associationRef.getTypeQName());

		removeCachedAssoc(childAssocCacheName(), associationRef.getParentRef(), associationRef.getTypeQName());

	}

	@Override
	public void onDeleteNode(ChildAssociationRef associationRef, boolean arg1) {
		logger.debug("onDeleteNode: " + associationRef.getTypeQName());

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
