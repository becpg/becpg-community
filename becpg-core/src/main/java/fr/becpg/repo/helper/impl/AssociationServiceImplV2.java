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

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
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
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>AssociationServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AssociationServiceImplV2 extends AbstractBeCPGPolicy implements AssociationService, NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnCreateChildAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy,
		NodeServicePolicies.OnDeleteChildAssociationPolicy, NodeServicePolicies.OnDeleteNodePolicy, CheckOutCheckInServicePolicies.OnCheckIn {

	private static final Log logger = LogFactory.getLog(AssociationServiceImplV2.class);


	private EntityDictionaryService entityDictionaryService;

	private DataSource dataSource;

	private TenantService tenantService;

	private NamespaceService namespaceService;
	
	private BeCPGCacheService beCPGCacheService;
	
	private SimpleCache<AssociationCacheRegion, List<NodeRef>> childsAssocsCache;
	private SimpleCache<AssociationCacheRegion, List<AssociationRef>> assocsCache;

	private static Set<QName> ignoredAssocs = new HashSet<>();

	private Set<QName> childAssocCacheRegions = ConcurrentHashMap.newKeySet();

	private QNameDAO qnameDAO;

	static {
		ignoredAssocs.add(ContentModel.ASSOC_ORIGINAL);
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>dataSource</code>.</p>
	 *
	 * @param dataSource a DataSource object.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * <p>Setter for the field <code>tenantService</code>.</p>
	 *
	 * @param tenantService a {@link org.alfresco.repo.tenant.TenantService} object.
	 */
	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	/**
	 * <p>Setter for the field <code>qnameDAO</code>.</p>
	 *
	 * @param qnameDAO a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}
	

	public void setChildsAssocsCache(SimpleCache<AssociationCacheRegion, List<NodeRef>> childsAssocsCache) {
		this.childsAssocsCache = childsAssocsCache;
	}

	public void setAssocsCache(SimpleCache<AssociationCacheRegion, List<AssociationRef>> assocsCache) {
		this.assocsCache = assocsCache;
	}

	/** {@inheritDoc} */
	@Override
	public void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs, boolean resetCache) {
 
		List<AssociationRef> dbAssocNodeRefs = getTargetAssocsImpl(nodeRef, qName, false);
		List<NodeRef> dbTargetNodeRefs = new ArrayList<>();
		List<AssociationRef> cachedAssocNodeRefs = new ArrayList<>(dbAssocNodeRefs);
		boolean hasChanged = resetCache;

		if (dbAssocNodeRefs != null) {
			// remove from db

			for (AssociationRef assocRef : dbAssocNodeRefs) {
				if (assocNodeRefs == null || !assocNodeRefs.contains(assocRef.getTargetRef())) {
					nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), qName);
					cachedAssocNodeRefs.remove(assocRef);
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
						cachedAssocNodeRefs.add(nodeService.createAssociation(nodeRef, n, qName));
						dbTargetNodeRefs.add(n);
						hasChanged = true;
				}
			}
		}
		if (hasChanged) {
			beCPGCacheService.getFromCache(assocsCache, new AssociationCacheRegion(nodeRef, qName), () -> cachedAssocNodeRefs);
		}
 

	}

	/** {@inheritDoc} */
	@Override
	public void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs) {
		update(nodeRef, qName, assocNodeRefs, false);
	}

	/** {@inheritDoc} */
	@Override
	public void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef) {
		update(nodeRef, qName,assocNodeRef!=null ? Arrays.asList(assocNodeRef): null, false);
	
	}

	

	/** {@inheritDoc} */
	@Override
	public NodeRef getTargetAssoc(NodeRef nodeRef, QName qName) {
		return getTargetAssoc(nodeRef, qName, true);
	}

	/** {@inheritDoc} */
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

		return beCPGCacheService.getFromCache(assocsCache , new AssociationCacheRegion(nodeRef, qName), () -> nodeService.getTargetAssocs(nodeRef, qName));

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getChildAssoc(NodeRef nodeRef, QName qName) {
		List<NodeRef> assocRefs = getChildAssocsImpl(nodeRef, qName, null, null);
		return !assocRefs.isEmpty() ? assocRefs.get(0) : null;
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName) {
		return getChildAssocs(nodeRef, qName, null);
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName, QName childTypeQName) {
		return getChildAssocsImpl(nodeRef, qName, childTypeQName, null);
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getChildAssocs(NodeRef listNodeRef, QName qName, QName childTypeQName, Map<String, Boolean> sortMap) {
		return getChildAssocsImpl(listNodeRef, qName, childTypeQName, sortMap);
	}

	private @Nonnull List<NodeRef> getChildAssocsImpl(final NodeRef nodeRef, final QName qName, final QName childType, final Map<String, Boolean> sortProps) {

		if ((childType != null) && (sortProps != null) && !sortProps.isEmpty() && !isDefaultSort(sortProps)) {
			// No cache if specific sort
			return dbChildAssocSearch(nodeRef, childType, sortProps);
		}
		
		
		List<NodeRef> ret = beCPGCacheService.getFromCache(childsAssocsCache, new AssociationCacheRegion(nodeRef, qName, childType), () -> {
			
			if(childType!=null) {
				childAssocCacheRegions.add(childType);
			}
			
			if ((childType != null) && (sortProps != null) && !sortProps.isEmpty()) {
				return dbChildAssocSearch(nodeRef, childType, sortProps);
			}

			return nodeService.getChildAssocs(nodeRef, qName, RegexQNamePattern.MATCH_ALL, true).stream()
					.filter(n -> (childType == null) || nodeService.getType(n.getChildRef()).equals(childType))
					.map(ChildAssociationRef::getChildRef).collect(Collectors.toCollection(LinkedList::new));

		});

		ret.sort(new CommonDataListSort(nodeService));

		return ret;
	}

	private boolean isDefaultSort(Map<String, Boolean> sortProps) {
		return (sortProps != null) && (sortProps.get("@bcpg:sort") != null) && sortProps.get("@bcpg:sort") && (sortProps.get("@cm:created") != null)
				&& sortProps.get("@cm:created");
	}

	private List<NodeRef> dbChildAssocSearch(final NodeRef nodeRef, final QName childType, Map<String, Boolean> sortProps) {

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
				createSortDirection = Boolean.TRUE.equals(entry.getValue()) ? "ASC" : "DESC";
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
				sortDirection = Boolean.TRUE.equals(entry.getValue()) ? "ASC" : "DESC";
				count++;
			}
		}

		String sql = "select alf_node.uuid, alf_node.audit_created from alf_node ";

		String sortOrderSql = " order by alf_node.audit_created " + createSortDirection;

		if ((sortFieldQName != null) && (sortDirection != null)) {
			DataTypeDefinition dateType = entityDictionaryService.getProperty(sortFieldQName).getDataType();
			String fieldType = "string_value";
			sortOrderSql = "";
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
					+ sortFieldQName.getNamespaceURI() + "') " + "and local_name='" + sortFieldQName.getLocalName() + "') ";

			if (DataTypeDefinition.MLTEXT.equals(dateType.getName())) {
				
				sql += "and alf_node_properties.locale_id in (select id from alf_locale where locale_str like '"+MLTextHelper.localeKey(I18NUtil.getContentLocale())+"%' ) ";
				sortOrderSql = " group by alf_node.uuid";
			}
			
			sql +=") ";
						
			sortOrderSql += " order by alf_node_properties." + fieldType + " " + sortDirection + ", alf_node.audit_created " + createSortDirection;

			
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


	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName, boolean fromCache) {
		List<AssociationRef> assocRefs = getTargetAssocsImpl(nodeRef, qName, fromCache);
		List<NodeRef> listItems = new LinkedList<>();
		for (AssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getTargetRef());
		}

		return listItems;
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName) {
		return getTargetAssocs(nodeRef, qName, true);
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QNamePattern qNamePattern) {
		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, qNamePattern);
		List<NodeRef> listItems = new LinkedList<>();
		for (AssociationRef assocRef : assocRefs) {
			listItems.add(assocRef.getSourceRef());
		}
		return listItems;
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


	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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


	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef associationRef) {
		logger.debug("onDeleteAssociation");
		removeCachedAssoc(associationRef.getSourceRef(), associationRef.getTypeQName());
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef associationRef) {
		logger.debug("onCreateAssociation");
		if (!ignoredAssocs.contains(associationRef.getTypeQName())) {
			removeCachedAssoc(associationRef.getSourceRef(), associationRef.getTypeQName());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteChildAssociation(ChildAssociationRef associationRef) {
		logger.debug("onDeleteChildAssociation: " + associationRef.getTypeQName());

		removeCachedAssoc(associationRef.getParentRef(), associationRef.getTypeQName(),true);

	}

	/** {@inheritDoc} */
	@Override
	public void onCreateChildAssociation(ChildAssociationRef associationRef, boolean arg1) {
		logger.debug("onCreateChildAssociation: " + associationRef.getTypeQName());

		removeCachedAssoc(associationRef.getParentRef(), associationRef.getTypeQName(),true);

	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteNode(ChildAssociationRef associationRef, boolean arg1) {
		logger.debug("onDeleteNode: " + associationRef.getTypeQName());

		removeCachedAssoc( associationRef.getParentRef(), associationRef.getTypeQName(),true);
	}

	/** {@inheritDoc} */
	@Override
	public void onCheckIn(NodeRef nodeRef) {
		// Bad but not so often
		
		for (SimpleCache<AssociationCacheRegion,?> cache : Arrays.asList(assocsCache, childsAssocsCache)) {
			for (AssociationCacheRegion cacheKey : cache.getKeys()) {
				if (cacheKey.getNodeRef().equals(nodeRef)) {
					if (logger.isDebugEnabled()) {
						logger.debug("In checkin delete:" + cacheKey);
					}
					cache.remove(cacheKey);
				}
			}
		}

	}
	//// Cache managment
	
	private  void removeCachedAssoc( NodeRef nodeRef, QName qName) {
	     removeCachedAssoc(nodeRef, qName, false);
	}
	
	private  void removeCachedAssoc( NodeRef nodeRef, QName qName, boolean isChildAssocCache) {
		
		AssociationCacheRegion key = new AssociationCacheRegion(nodeRef, qName);
		if (logger.isDebugEnabled()) {
			logger.debug("Remove assoc from  " + (isChildAssocCache ? "childAssocCache" : "assocCache") + " " + key);
		}
		if(!isChildAssocCache) {
			assocsCache.remove(key);
		} else {
			childsAssocsCache.remove(key);
			
			for (QName cacheRegion : childAssocCacheRegions) {
				childsAssocsCache.remove(new AssociationCacheRegion(nodeRef,qName, cacheRegion));
			}
		}

		

		

	}


}
