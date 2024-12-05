/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.cache.RefreshableCacheListener;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQuery;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.alfresco.util.cache.AsynchronouslyRefreshedCacheRegistry;
import org.alfresco.util.cache.RefreshableCacheEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.impl.BeCPGCacheServiceImpl;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.impl.AssociationCriteriaFilter.AssociationCriteriaFilterMode;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>AssociationServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AssociationServiceImplV2 extends AbstractBeCPGPolicy implements AssociationService, NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnCreateChildAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy,
		NodeServicePolicies.OnDeleteChildAssociationPolicy, NodeServicePolicies.OnDeleteNodePolicy, CheckOutCheckInServicePolicies.OnCheckIn,
		RefreshableCacheListener, NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnRestoreNodePolicy, InitializingBean {

	private static final Log logger = LogFactory.getLog(AssociationServiceImplV2.class);

	private EntityDictionaryService entityDictionaryService;

	private DataSource dataSource;

	private TenantService tenantService;

	private NamespaceService namespaceService;

	private CommonDataListSort commonDataListSort;

	//Immutable cluster cache
	private SimpleCache<AssociationCacheRegion, ChildAssocCacheEntry> childsAssocsCache;
	private SimpleCache<AssociationCacheRegion, Set<NodeRef>> assocsCache;

	private AsynchronouslyRefreshedCacheRegistry registry;

	private static Set<QName> ignoredAssocs = new HashSet<>();

	private static Set<StoreRef> ignoredStoreRefs = new HashSet<>();

	private QNameDAO qnameDAO;

	static {
		ignoredAssocs.add(ContentModel.ASSOC_ORIGINAL);
		ignoredStoreRefs.add(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID));
	}

	/**
	 * <p>Setter for the field <code>registry</code>.</p>
	 *
	 * @param registry a {@link org.alfresco.util.cache.AsynchronouslyRefreshedCacheRegistry} object
	 */
	public void setRegistry(AsynchronouslyRefreshedCacheRegistry registry) {
		this.registry = registry;
	}

	/** {@inheritDoc} */
	@Override
	public void setNodeService(NodeService nodeService) {
		super.setNodeService(nodeService);
		commonDataListSort = new CommonDataListSort(nodeService);
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

	/**
	 * <p>Setter for the field <code>qnameDAO</code>.</p>
	 *
	 * @param qnameDAO a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	/**
	 * <p>Setter for the field <code>childsAssocsCache</code>.</p>
	 *
	 * @param childsAssocsCache a {@link org.alfresco.repo.cache.SimpleCache} object
	 */
	public void setChildsAssocsCache(SimpleCache<AssociationCacheRegion, ChildAssocCacheEntry> childsAssocsCache) {
		this.childsAssocsCache = childsAssocsCache;
	}

	/**
	 * <p>Setter for the field <code>assocsCache</code>.</p>
	 *
	 * @param assocsCache a {@link org.alfresco.repo.cache.SimpleCache} object
	 */
	public void setAssocsCache(SimpleCache<AssociationCacheRegion, Set<NodeRef>> assocsCache) {
		this.assocsCache = assocsCache;
	}

	private static final String UPDATE_ASSOC_COUNT = "AssociationServiceImplV2.updateAssocCount";

	/** {@inheritDoc} */
	@Override
	public void update(NodeRef nodeRef, QName qName, List<NodeRef> toUpdateNodeRefs) {

		List<NodeRef> dbAssocNodeRefs = getTargetAssocs(nodeRef, qName);
		Set<NodeRef> assocNodeRefs = new HashSet<>();
		if (toUpdateNodeRefs != null) {
			assocNodeRefs.addAll(toUpdateNodeRefs);
		}
		boolean hasChanged = false;

		try {
			TransactionalResourceHelper.incrementCount(UPDATE_ASSOC_COUNT);

			if (dbAssocNodeRefs != null) {
				// remove from db

				for (NodeRef assocRef : dbAssocNodeRefs) {
					if ((assocNodeRefs == null) || !assocNodeRefs.contains(assocRef)) {
						try {
							hasChanged = true;
							if (!nodeService.hasAspect(assocRef, ContentModel.ASPECT_PENDING_DELETE)) {
								nodeService.removeAssociation(nodeRef, assocRef, qName);
							}
						} catch (InvalidNodeRefException e) {
							logger.error("Node already deleted:" + nodeRef + " " + qName);
						}
					}
				}
			}

			Set<NodeRef> toRemoveNodeRefs = new HashSet<>();

			// add nodes that are not in db
			if (assocNodeRefs != null) {
				for (NodeRef n : assocNodeRefs) {
					if (!dbAssocNodeRefs.contains(n) && nodeService.exists(n)) {
						if (!nodeService.hasAspect(n, ContentModel.ASPECT_PENDING_DELETE)) {
							hasChanged = true;
							nodeService.createAssociation(nodeRef, n, qName);
							toRemoveNodeRefs.remove(n);
						}
					}

				}
			}

			if (hasChanged) {
				assocNodeRefs.removeAll(toRemoveNodeRefs);
				assocsCache.put(new AssociationCacheRegion(nodeRef, qName), assocNodeRefs);
			}
		} finally {
			TransactionalResourceHelper.decrementCount(UPDATE_ASSOC_COUNT, false);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef) {
		update(nodeRef, qName, assocNodeRef != null ? Arrays.asList(assocNodeRef) : null);

	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName) {
		//always return a new List ensuring cache immutability
		//TO should be better using unmodifiable set
		return new LinkedList<>(getFromCache(assocsCache, new AssociationCacheRegion(nodeRef, qName), () -> {
			List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, qName);
			Set<NodeRef> listItems = new HashSet<>();
			for (AssociationRef assocRef : assocRefs) {
				listItems.add(assocRef.getTargetRef());
			}
			return listItems;
		}));

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getTargetAssoc(NodeRef nodeRef, QName qName) {
		List<NodeRef> assocRefs = getTargetAssocs(nodeRef, qName);
		return (assocRefs != null) && !assocRefs.isEmpty() ? assocRefs.get(0) : null;
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

	private @Nonnull List<NodeRef> getChildAssocsImpl(final NodeRef nodeRef, final QName qName, final QName childType,
			final Map<String, Boolean> sortProps) {

		//Faster that in alfresco Search ?
		if ((childType != null) && (sortProps != null) && !sortProps.isEmpty() && !isDefaultSort(sortProps)) {
			// No cache if specific sort
			return dbChildAssocSearch(nodeRef, childType, sortProps);
		}

		//Common sort returning from search
		ChildAssocCacheEntry cachedAssocs = getChildAssocsByType(nodeRef, qName);

		return cachedAssocs.get(childType);
	}

	/** {@inheritDoc} */
	@Override
	public ChildAssocCacheEntry getChildAssocsByType(final NodeRef nodeRef, final QName qName) {
		return getFromCache(childsAssocsCache, new AssociationCacheRegion(nodeRef, qName), () -> {
			ChildAssocCacheEntry childAssocCacheEntry = new ChildAssocCacheEntry();

			for (ChildAssociationRef assocRef : nodeService.getChildAssocs(nodeRef, qName, RegexQNamePattern.MATCH_ALL, true)) {
				if (nodeService.exists(assocRef.getChildRef())) {
					QName type = nodeService.getType(assocRef.getChildRef());
					childAssocCacheEntry.add(assocRef.getChildRef(), type);
				}
			}

			childAssocCacheEntry.sort(commonDataListSort);

			return childAssocCacheEntry;

		});
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
			sortOrderSql = " group by alf_node.uuid";
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

				sql += "and alf_node_properties.locale_id in (select id from alf_locale where locale_str like '"
						+ MLTextHelper.localeKey(I18NUtil.getContentLocale()) + "%' ) ";
			}

			sql += ") ";

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
	
	@Override
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QNamePattern qNamePattern, boolean includeVersions) {
		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, qNamePattern);
		List<NodeRef> listItems = new LinkedList<>();
		for (AssociationRef assocRef : assocRefs) {
			if (includeVersions || !isVersion(assocRef.getSourceRef()) && !nodeService.hasAspect(assocRef.getSourceRef(), BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
				listItems.add(assocRef.getSourceRef());
			}
		}
		return listItems;
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QNamePattern qNamePattern) {
		return getSourcesAssocs(nodeRef, qNamePattern, false);
	}
	
	private boolean isVersion(NodeRef nodeRef) {
		return nodeRef.getStoreRef().getProtocol().contains(VersionBaseModel.STORE_PROTOCOL)
				|| nodeRef.getStoreRef().getIdentifier().contains(Version2Model.STORE_ID);
	}

	private static final String SQL_SELECT_SOURCE_ASSOC_ENTITY_FIRST_PART = "select entity.uuid as entity, dataListItem.uuid as dataListItem, dataListItem.type_qname_id as dataListItemType, targetNode.uuid as targetNode"
			+ " from alf_node entity " + " join alf_child_assoc dataListContainerAssoc on (entity.id = dataListContainerAssoc.parent_node_id)"
			+ " join alf_child_assoc dataListAssoc on (dataListAssoc.parent_node_id = dataListContainerAssoc.child_node_id)"
			+ " join alf_child_assoc dataListItemAssoc on (dataListItemAssoc.parent_node_id = dataListAssoc.child_node_id)"
			+ " join alf_node dataListItem on (dataListItem.id = dataListItemAssoc.child_node_id ) "
			+ " join alf_node_assoc assoc on ( dataListItem.id = assoc.source_node_id ) "
			+ " join alf_node targetNode on (targetNode.id = assoc.target_node_id) "
			+ " join alf_store targetNodeStore on (  targetNodeStore.id = targetNode.store_id and targetNodeStore.protocol= ? and targetNodeStore.identifier=?) "
			+ " left join alf_node_aspects q1 on (q1.qname_id = ? and q1.node_id=entity.id)"
			+ " left join alf_node_aspects q2 on (q2.qname_id = ? and q2.node_id=dataListItem.id)";

	private static final String SQL_SELECT_SOURCE_ASSOC_ENTITY_FINAL_PART = " where  assoc.type_qname_id=? and q1.qname_id IS NULL and q2.qname_id IS NULL ";

	/** {@inheritDoc} */
	@Override
	public List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocQName, QName listTypeQname,
			boolean isOrOperator, List<AssociationCriteriaFilter> criteriaFilters) {
		return getEntitySourceAssocs(nodeRefs, assocQName, listTypeQname, isOrOperator, criteriaFilters, null);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocTypeQName, QName listTypeQname, boolean isOrOperator,
			List<AssociationCriteriaFilter> criteriaFilters, PagingRequest pagingRequest) {
		List<EntitySourceAssoc> ret = null;

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		if ((nodeRefs != null) && !nodeRefs.isEmpty()) {

			boolean isAnd = !isOrOperator && (nodeRefs.size() > 1);

			if(logger.isDebugEnabled()) {
				logger.debug("getEntitySourceAssocs");
				logger.debug(" - assocTypeQName : "+ assocTypeQName);
				logger.debug(" - listTypeQname : "+ listTypeQname);
				logger.debug(" - isOrOperator : "+ isOrOperator);
				logger.debug(" - criteriaFilters : "+ criteriaFilters);
			}
			
			if (isAnd) {
				for (NodeRef nodeRef : nodeRefs) {
					if (ret == null) {
						ret = internalEntitySourceAssocs(Arrays.asList(nodeRef), assocTypeQName, listTypeQname, criteriaFilters, pagingRequest);
					} else {
						List<EntitySourceAssoc> tmp = internalEntitySourceAssocs(Arrays.asList(nodeRef), assocTypeQName, listTypeQname,
								criteriaFilters, pagingRequest);

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
				ret = internalEntitySourceAssocs(nodeRefs, assocTypeQName, listTypeQname, criteriaFilters, pagingRequest);
			}

		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getEntitySourceAssocs  takes " + watch.getTotalTimeSeconds() + " seconds - size: "+(ret!=null ? ret.size():0));
		}

		return ret;
	}

	private List<EntitySourceAssoc> internalEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocTypeQName, QName listTypeQname,
			List<AssociationCriteriaFilter> criteriaFilters, PagingRequest pagingRequest) {
		List<EntitySourceAssoc> ret = new ArrayList<>();

		if ((nodeRefs != null) && !nodeRefs.isEmpty()) {

			StringBuilder query = new StringBuilder();
			StringBuilder exclude = new StringBuilder();

			query.append(SQL_SELECT_SOURCE_ASSOC_ENTITY_FIRST_PART);

			if (criteriaFilters != null) {

				int index = 0;

				for (AssociationCriteriaFilter criteriaFilter : criteriaFilters) {
					QName criteriaAttribute = criteriaFilter.getAttributeQname();
					if (criteriaFilter.hasValue()) {

						Pair<Long, QName> qNameIdPair = qnameDAO.getQName(criteriaAttribute);
						if (qNameIdPair != null) {
							Long qNameId = qNameIdPair.getFirst();

							String propertyName = "p" + index;
							
							if(AssociationCriteriaFilterMode.NOT_EQUALS.equals(criteriaFilter.getMode())) {
								query.append(" left");
							}

							query.append(" join alf_node_properties p" + index + " on (" + propertyName + ".node_id = dataListItem.id " + "and "
									+ propertyName + ".qname_id= " + qNameId );
							
							if(!AssociationCriteriaFilterMode.NOT_EQUALS.equals(criteriaFilter.getMode())) {
								query.append(" and ");
							}
							
							String fieldName = DBQuery.getFieldName(entityDictionaryService, criteriaAttribute, true);
						

							if (criteriaFilter.getValue() != null) {
								
									if(AssociationCriteriaFilterMode.NOT_EQUALS.equals(criteriaFilter.getMode())) {
										exclude.append(" and ("+propertyName + "."+fieldName+" IS NULL or "+propertyName + "."+fieldName+" != "+wrap(fieldName, criteriaFilter.getValue())+")");
									}else {
										query.append(propertyName + "."+fieldName+" = "+wrap(fieldName, criteriaFilter.getValue())+"");
									}
									
							} else {
								boolean isFirst = true;
								if (criteriaFilter.getFromRange() != null && !criteriaFilter.isMinMax(criteriaFilter.getFromRange())) {
									query.append(propertyName + "."+fieldName+" >= " + wrap(fieldName, criteriaFilter.getFromRange()));
									isFirst = false;
								}
								if (criteriaFilter.getToRange() != null && !criteriaFilter.isMinMax(criteriaFilter.getToRange())) {
									if (!isFirst) {
										query.append(" and ");
									}
									query.append(propertyName + "."+fieldName+" <= " + wrap(fieldName, criteriaFilter.getToRange()));
								}
							}
							query.append(")");

							index++;
						} else {
							logger.warn("No qnameId found for :" + criteriaAttribute);
						}
					}
				}
			}

			query.append(SQL_SELECT_SOURCE_ASSOC_ENTITY_FINAL_PART);

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
			
			query.append(exclude);

			Pair<Long, QName> aspectCompositeVersion = qnameDAO.getQName(BeCPGModel.ASPECT_COMPOSITE_VERSION);

			Long typeQNameId = null;

			Long aspectQnameId = aspectCompositeVersion != null ? aspectCompositeVersion.getFirst() : -1;
			if (assocTypeQName != null) {
				Pair<Long, QName> typeQNamePair = qnameDAO.getQName(assocTypeQName);
				if (typeQNamePair == null) {
					// No such QName
					return Collections.emptyList();
				}
				typeQNameId = typeQNamePair.getFirst();
			}
			
			if (listTypeQname == null) {
			
				AssociationDefinition assocDef = entityDictionaryService.getAssociation(assocTypeQName);
				if(assocDef!=null && assocDef.getSourceClass()!=null  && !assocDef.getSourceClass().isAspect()) {
					listTypeQname = assocDef.getSourceClass().getName();
				}
				
			}
			

			boolean isEntity = (listTypeQname != null) && !entityDictionaryService.isSubClass(listTypeQname, BeCPGModel.TYPE_ENTITYLIST_ITEM);

			if (listTypeQname != null) {
				query.append(" and ( ");

				Pair<Long, QName> typeQNamePair = qnameDAO.getQName(listTypeQname);
				if (typeQNamePair == null) {
					// No such QName
					return Collections.emptyList();
				}

				query.append("dataListItem.type_qname_id='");
				query.append(typeQNamePair.getFirst());
				query.append("'");

				if (isEntity) {
					for (QName listSubTypeQname : entityDictionaryService.getSubTypes(listTypeQname)) {
                         if(listTypeQname.equals(listSubTypeQname)) {
							typeQNamePair = qnameDAO.getQName(listSubTypeQname);
							if (typeQNamePair != null) {
	
								query.append(" or ");
								query.append("dataListItem.type_qname_id='");
								query.append(typeQNamePair.getFirst());
								query.append("'");
	
							}
                         }

					}
				}
				query.append(")");

			}

			query.append("  group by dataListItem.uuid ");
			
			if(pagingRequest != null ) {
			if (pagingRequest.getMaxItems() > -1) {
				query.append(" LIMIT " +  pagingRequest.getMaxItems());
			}
			
			if (pagingRequest.getSkipCount()>0) {
				query.append(" OFFSET " +  pagingRequest.getSkipCount());
			}
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

							if (isEntity) {
								entityNodeRef = dataListItemNodeRef;
							} else if (listTypeQname == null) {
								Pair<Long, QName> entityType = qnameDAO.getQName(res.getLong("dataListItemType"));
								if (!entityDictionaryService.isSubClass(entityType.getSecond(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
									entityNodeRef = dataListItemNodeRef;
								}
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

	private String wrap(String fieldName, String value) {
		if("string_value".equals(fieldName)) {
			return "'"+value+"'";
		}
		
		return value;
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

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnRestoreNodePolicy.QNAME, ContentModel.TYPE_CMOBJECT, new JavaBehaviour(this, "onRestoreNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnRestoreNodePolicy.QNAME, ContentModel.TYPE_AUTHORITY, new JavaBehaviour(this, "onRestoreNode"));

		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, ContentModel.TYPE_AUTHORITY,
				new JavaBehaviour(this, "onCheckIn"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST,
				new JavaBehaviour(this, "onUpdateProperties"));
		
		super.disableOnCopyBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

	}

	/** {@inheritDoc} */
	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
		
		NodeRef listNodeRef = nodeService.getPrimaryParent(destinationRef).getParentRef();
		
		removeChildCachedAssoc(listNodeRef, ContentModel.ASSOC_CONTAINS);
		
		NodeRef listContainerNodeRef = nodeService.getPrimaryParent(listNodeRef).getParentRef();
		
		removeChildCachedAssoc(listContainerNodeRef, ContentModel.ASSOC_CONTAINS);

		super.onCopyComplete(classRef, sourceNodeRef, destinationRef, copyToNewNode, copyMap);
	}
	
	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef associationRef) {
		if (TransactionalResourceHelper.getCount(UPDATE_ASSOC_COUNT) == 0) {
			if (!ignoredAssocs.contains(associationRef.getTypeQName())
					&& !ignoredStoreRefs.contains(tenantService.getBaseName(associationRef.getSourceRef().getStoreRef()))) {
				removeCachedAssoc(associationRef.getSourceRef(), associationRef.getTypeQName());
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef associationRef) {
		if (TransactionalResourceHelper.getCount(UPDATE_ASSOC_COUNT) == 0) {
			if (!ignoredAssocs.contains(associationRef.getTypeQName())
					&& !ignoredStoreRefs.contains(tenantService.getBaseName(associationRef.getSourceRef().getStoreRef()))) {
				removeCachedAssoc(associationRef.getSourceRef(), associationRef.getTypeQName());
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onRestoreNode(ChildAssociationRef associationRef) {
		removeChildCachedAssoc(associationRef.getParentRef(), associationRef.getTypeQName());

	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteChildAssociation(ChildAssociationRef associationRef) {
		removeChildCachedAssoc(associationRef.getParentRef(), associationRef.getTypeQName());

	}

	/** {@inheritDoc} */
	@Override
	public void onCreateChildAssociation(ChildAssociationRef associationRef, boolean arg1) {
		removeChildCachedAssoc(associationRef.getParentRef(), associationRef.getTypeQName());

	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteNode(ChildAssociationRef associationRef, boolean arg1) {
		removeChildCachedAssoc(associationRef.getParentRef(), associationRef.getTypeQName());

	}

	/** {@inheritDoc} */
	@Override
	public void onCheckIn(NodeRef nodeRef) {
		removeAllCacheAssocs(nodeRef);

	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		Serializable beforeSort = before.get(BeCPGModel.PROP_SORT);
		Serializable afterSort = after.get(BeCPGModel.PROP_SORT);

		if (((beforeSort != null) && !beforeSort.equals(afterSort)) || ((beforeSort == null) && (afterSort != null))) {
			removeChildCachedAssoc(nodeService.getPrimaryParent(nodeRef).getParentRef(), ContentModel.ASSOC_CONTAINS);
		}
	}

	//// Cache managment

	private void removeCachedAssoc(NodeRef nodeRef, QName qName) {

		AssociationCacheRegion cacheKey = new AssociationCacheRegion(nodeRef, qName);
		assocsCache.remove(cacheKey);
	}

	/** {@inheritDoc} */
	@Override
	public void removeChildCachedAssoc(NodeRef nodeRef, QName qName) {
		childsAssocsCache.remove(new AssociationCacheRegion(nodeRef, qName));
	}

	/** {@inheritDoc} */
	@Override
	public void removeAllCacheAssocs(NodeRef nodeRef) {

		Map<QName, Set<NodeRef>> assocs = new HashMap<>();

		// get the list of target nodes for each association type
		List<AssociationRef> refs = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
		for (AssociationRef assocRef : refs) {
			Set<NodeRef> listItems = assocs.get(assocRef.getTypeQName());
			if (listItems == null) {
				listItems = new HashSet<>();
				assocs.put(assocRef.getTypeQName(), listItems);
				assocsCache.put(new AssociationCacheRegion(nodeRef, assocRef.getTypeQName()), listItems);
			}

			listItems.add(assocRef.getTargetRef());

		}

		childsAssocsCache.remove(new AssociationCacheRegion(nodeRef, ContentModel.ASSOC_CONTAINS));

	}

	/**
	 * <p>getFromCache.</p>
	 *
	 * @param cache a {@link org.alfresco.repo.cache.SimpleCache} object
	 * @param cacheKey a {@link fr.becpg.repo.helper.impl.AssociationCacheRegion} object
	 * @param callback a {@link java.util.function.Supplier} object
	 * @param <T> a T class
	 * @return a T object
	 */
	public <T> T getFromCache(SimpleCache<AssociationCacheRegion, T> cache, AssociationCacheRegion cacheKey, Supplier<T> callback) {
		if (ignoredAssocs.contains(cacheKey.getAssocQName()) || ignoredStoreRefs.contains(tenantService.getBaseName(cacheKey.getNodeRef().getStoreRef()))) {
			return callback.get();
		}

		T ret = cache.get(cacheKey);

		if (ret == null) {

			ret = callback.get();
			if ((ret != null)) {
				cache.put(cacheKey, ret);
			}
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public void onRefreshableCacheEvent(RefreshableCacheEvent refreshableCacheEvent) {
		if (BeCPGCacheServiceImpl.class.getName().equals(refreshableCacheEvent.getCacheId()) && "all".equals(refreshableCacheEvent.getKey())) {
			if (logger.isInfoEnabled()) {
				logger.info("Clear associations caches: " + refreshableCacheEvent.getCacheId());
			}
			assocsCache.clear();
			childsAssocsCache.clear();
		}

	}

	/** {@inheritDoc} */
	@Override
	public String getCacheId() {
		return AssociationService.class.getName();
	}

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		registry.register(this);

	}
}
