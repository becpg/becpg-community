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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.cache.RefreshableCacheListener;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQuery;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.alfresco.util.cache.AsynchronouslyRefreshedCacheRegistry;
import org.alfresco.util.cache.RefreshableCacheEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;
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
	
	private SqlSessionTemplate sqlSessionTemplate;
	
	private PermissionService permissionService;
	
	private NodeDAO nodeDAO;
	
	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}
	
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

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
	
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
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
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef) {
		return getSourcesAssocs(nodeRef, null);
	}
	
	@Override
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qName) {
		return getSourcesAssocs(nodeRef, qName, false);
	}
	
	@Override
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qName, Boolean includeVersions) {
		return getSourcesAssocs(nodeRef, qName, includeVersions, null, null);
	}
	
	@Override
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qName, Boolean includeVersions, Integer maxResults, Integer offset) {
		return getSourcesAssocs(nodeRef, qName, includeVersions, maxResults, offset, false);
	}
	
	@Override
	public List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QName qName, Boolean includeVersions, Integer maxResults, Integer offset, boolean checkPermissions) {
		Map<String, Object> params = new HashMap<>();
		params.put("qName", qName != null ? qName.getLocalName() : null);
		params.put("includeVersions", includeVersions != null && includeVersions.booleanValue());
		Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(nodeRef);
		params.put("targetId", nodePair.getFirst());
		
		Set<String> authorisations = AuthenticationUtil.runAs(() -> permissionService.getAuthorisations(),
				AuthenticationUtil.getFullyAuthenticatedUser());

		Predicate<Node> permissionChecker = item -> canCurrentUserRead(item.getAclId(), authorisations);
		return queryItems("alfresco.node.select_SourcesAssocs", params, maxResults, offset, checkPermissions, permissionChecker).stream().map(Node::getNodeRef).toList();
	}
	
	private <T> List<T> queryItems(String template, Map<String, Object> params, Integer maxResults, Integer offset
			, boolean checkPermissions, Predicate<T> permissionChecker) {
		List<T> foundNodes = new ArrayList<>();
		if (checkPermissions) {
			Set<String> authorisations = AuthenticationUtil.runAs(() -> permissionService.getAuthorisations(),
					AuthenticationUtil.getFullyAuthenticatedUser());
			boolean isSystemReading = AuthenticationUtil.runAs(AuthenticationUtil::isRunAsUserTheSystemUser,
					AuthenticationUtil.getFullyAuthenticatedUser());
			boolean isAdminReading = AuthenticationUtil.runAs(() -> authorisations.contains(AuthenticationUtil.getAdminRoleName()),
					AuthenticationUtil.getFullyAuthenticatedUser());
			if (maxResults == null || maxResults == -1 || maxResults == Integer.MAX_VALUE) {
				maxResults = Integer.MAX_VALUE;
			}
			int batchStart = 0;
			int batchSize = 1000;
			params.put("offset", batchStart);
			params.put("maxResults", batchSize);
			int offsetCount = 0;
			while (foundNodes.size() < maxResults) {
				params.put("offset", batchStart);
				List<T> nextResults = sqlSessionTemplate.selectList(template, params);
				for (T node : nextResults) {
					if (foundNodes.size() >= maxResults) {
						break;
					}
					if (isSystemReading || isAdminReading || permissionChecker.test(node)) {
						if (offset != null && offsetCount < offset) {
							offsetCount++;
						} else {
							foundNodes.add(node);
						}
					}
				}
				if (foundNodes.size() >= maxResults || nextResults.size() < batchSize) {
					break;
				} else {
					batchStart += batchSize;
				}
			}
			return foundNodes;
		}
		params.put("offset", offset);
		params.put("maxResults", maxResults);
		foundNodes = sqlSessionTemplate.selectList(template, params);
		return foundNodes;
	}
	
	protected boolean canCurrentUserRead(Long aclId, Set<String> authorities) {
		Set<String> aclReadersDenied = permissionService.getReadersDenied(aclId);
		for (String auth : aclReadersDenied) {
			if (authorities.contains(auth)) {
				return false;
			}
		}
		Set<String> aclReaders = permissionService.getReaders(aclId);
		for (String auth : aclReaders) {
			if (authorities.contains(auth)) {
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocQName, QName listTypeQname,
			boolean isOrOperator, List<AssociationCriteriaFilter> criteriaFilters) {
		return getEntitySourceAssocs(nodeRefs, assocQName, listTypeQname, isOrOperator, criteriaFilters, null);
	}
	
	@Override
	public List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocQName, QName listTypeQname, boolean isOrOperator,
			List<AssociationCriteriaFilter> criteriaFilters, PagingRequest pagingRequest) {
		return getEntitySourceAssocs(nodeRefs, assocQName, listTypeQname, isOrOperator, criteriaFilters, pagingRequest, false);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<EntitySourceAssoc> getEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocTypeQName, QName listTypeQname, boolean isOrOperator,
			List<AssociationCriteriaFilter> criteriaFilters, PagingRequest pagingRequest, boolean checkPermissions) {
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
						ret = internalEntitySourceAssocs(Arrays.asList(nodeRef), assocTypeQName, listTypeQname, criteriaFilters, pagingRequest, checkPermissions);
					} else {
						List<EntitySourceAssoc> tmp = internalEntitySourceAssocs(Arrays.asList(nodeRef), assocTypeQName, listTypeQname,
								criteriaFilters, pagingRequest, checkPermissions);

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
				ret = internalEntitySourceAssocs(nodeRefs, assocTypeQName, listTypeQname, criteriaFilters, pagingRequest, checkPermissions);
			}

		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getEntitySourceAssocs  takes " + watch.getTotalTimeSeconds() + " seconds - size: "+(ret!=null ? ret.size():0));
		}

		return ret;
	}

	private List<EntitySourceAssoc> internalEntitySourceAssocs(List<NodeRef> nodeRefs, QName assocTypeQName, QName listTypeQname,
			List<AssociationCriteriaFilter> criteriaFilters, PagingRequest pagingRequest, boolean checkPermissions) {
		List<EntitySourceAssoc> ret = new ArrayList<>();

		if ((nodeRefs != null) && !nodeRefs.isEmpty()) {
			Map<String, Object> params = buildQueryParameters(nodeRefs, assocTypeQName, listTypeQname);
		    
			if (params.isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Params are empty");
				}
				return ret;
			}
			
		    if (criteriaFilters != null && !criteriaFilters.isEmpty()) {
		        Map<String, Object> filterMap = buildCriteriaFilterMap(criteriaFilters);
		        params.putAll(filterMap);
		    }
		    
			Predicate<Map<String, Object>> permissionChecker = item -> {
				NodeRef entityNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, (String) item.get("entity"));
				return AuthenticationUtil.runAs(() -> permissionService.hasReadPermission(entityNodeRef) == AccessStatus.ALLOWED,
						AuthenticationUtil.getFullyAuthenticatedUser());
			};
					
			Integer maxResults = pagingRequest == null ? null : pagingRequest.getMaxItems();
			Integer offset = pagingRequest == null ? null : pagingRequest.getSkipCount();
			
			List<Map<String, Object>> results = queryItems("alfresco.node.select_EntitySourceAssocs", params, maxResults, offset, checkPermissions, permissionChecker);
			
			for (Map<String, Object> res : results) {
				NodeRef entityNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, (String) res.get("entity"));
				NodeRef sourceNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, (String) res.get("targetNode"));
				NodeRef dataListItemNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, (String) res.get("dataListItem"));
				
				if ((boolean) params.get("isEntity")) {
					entityNodeRef = dataListItemNodeRef;
				} else if (listTypeQname == null) {
					Pair<Long, QName> entityType = qnameDAO.getQName((Long) res.get("dataListItemType"));
					if (!entityDictionaryService.isSubClass(entityType.getSecond(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
						entityNodeRef = dataListItemNodeRef;
					}
				}
				ret.add(new EntitySourceAssoc(entityNodeRef, dataListItemNodeRef, sourceNodeRef));
			}
		}
		return ret;
	}
	
	private Map<String, Object> buildQueryParameters(List<NodeRef> nodeRefs, QName assocTypeQName, QName listTypeQname) {
	    Map<String, Object> queryParams = new HashMap<>();
	    List<Long> nodeIds = new ArrayList<>();
	    
	    for (NodeRef nodeRef : nodeRefs) {
	        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(nodeRef);
	        if (nodePair != null) {
	            nodeIds.add(nodePair.getFirst());
	        }
	    }
	    queryParams.put("nodeIds", nodeIds);
	    
	    Long typeQNameId = null;
	    if (assocTypeQName != null) {
	        Pair<Long, QName> typeQNamePair = qnameDAO.getQName(assocTypeQName);
	        if (typeQNamePair != null) {
	            typeQNameId = typeQNamePair.getFirst();
	        } else {
	            return Collections.emptyMap(); // No QName found, return empty map
	        }
	    }
	    queryParams.put("typeQNameId", typeQNameId);
	
	    Pair<Long, QName> aspectCompositeVersion = qnameDAO.getQName(BeCPGModel.ASPECT_COMPOSITE_VERSION);
	    Long aspectQNameId = (aspectCompositeVersion != null) ? aspectCompositeVersion.getFirst() : -1;
	    queryParams.put("aspectQNameId", aspectQNameId);
	
	    if (listTypeQname == null) {
	        AssociationDefinition assocDef = entityDictionaryService.getAssociation(assocTypeQName);
	        if (assocDef != null && assocDef.getSourceClass() != null && !assocDef.getSourceClass().isAspect()) {
	            listTypeQname = assocDef.getSourceClass().getName();
	        }
	    }
	    queryParams.put("listTypeQName", listTypeQname);
	
	    boolean isEntity = (listTypeQname != null) && !entityDictionaryService.isSubClass(listTypeQname, BeCPGModel.TYPE_ENTITYLIST_ITEM);
	    queryParams.put("isEntity", isEntity);
	
	    List<Long> typeQNameIds = new ArrayList<>();
	    if (listTypeQname != null) {
	        Pair<Long, QName> typeQNamePair = qnameDAO.getQName(listTypeQname);
	        if (typeQNamePair != null) {
	            typeQNameIds.add(typeQNamePair.getFirst());
	        } else {
	            return Collections.emptyMap();
	        }
	
	        if (isEntity) {
	            for (QName listSubTypeQname : entityDictionaryService.getSubTypes(listTypeQname)) {
	                if (listTypeQname.equals(listSubTypeQname)) {
	                    typeQNamePair = qnameDAO.getQName(listSubTypeQname);
	                    if (typeQNamePair != null) {
	                        typeQNameIds.add(typeQNamePair.getFirst());
	                    }
	                }
	            }
	        }
	    }
	    queryParams.put("typeQNameIds", typeQNameIds);
	
	    StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
	    if (AuthenticationUtil.isMtEnabled()) {
	        storeRef = tenantService.getName(storeRef);
	    }
	    queryParams.put("storeRef", storeRef);
	
	    return queryParams;
	}

	private Map<String, Object> buildCriteriaFilterMap(List<AssociationCriteriaFilter> criteriaFilters) {
	    Map<String, Object> filterMap = new HashMap<>();
	    List<Map<String, Object>> processedFilters = new ArrayList<>();
	    StringBuilder exclude = new StringBuilder("");
	    
	    int index = 0;
	    for (AssociationCriteriaFilter criteriaFilter : criteriaFilters) {
	        if (criteriaFilter.hasValue()) {
	            Map<String, Object> filterEntry = new HashMap<>();
	            filterEntry.put("index", index);
	            filterEntry.put("entityFilter", criteriaFilter.isEntityFilter());
	            filterEntry.put("mode", criteriaFilter.getMode().toString());
	            filterEntry.put("value", criteriaFilter.getValue());
	            
	            QName criteriaAttribute = criteriaFilter.getAttributeQname();
	            String fieldName = DBQuery.getFieldName(entityDictionaryService, criteriaAttribute, true);
	            if (criteriaFilter.getFromRange() != null) {
	            	filterEntry.put("fromRange", wrap(fieldName, criteriaFilter.getFromRange()));
	            }
	            if (criteriaFilter.getToRange() != null) {
	            	filterEntry.put("toRange", wrap(fieldName, criteriaFilter.getToRange()));
	            }
	            Pair<Long, QName> qNameIdPair = qnameDAO.getQName(criteriaAttribute);
				if (qNameIdPair != null) {
					Long qNameId = qNameIdPair.getFirst();
					if (qNameId != null && fieldName != null) {
						filterEntry.put("qNameId", qNameId);
						filterEntry.put("fieldName", fieldName);
						if (criteriaFilter.getValue() != null) {
							String[] values = criteriaFilter.getValue().split(",");
							String joinedValues = Arrays.stream(values)
									.map(value -> wrap(fieldName, value))
									.collect(Collectors.joining(","));
							filterEntry.put("joinedValues", joinedValues);
							if (AssociationCriteriaFilterMode.NOT_EQUALS.equals(criteriaFilter.getMode())) {
								exclude.append(" and (p" + index + "." + fieldName + " IS NULL or p" + index + "." + fieldName + " not in (" + joinedValues + "))");
							}
						}
						processedFilters.add(filterEntry);
						index++;
					}
				}
	        }
	    }

	    filterMap.put("criteriaFilters", processedFilters);
	    filterMap.put("exclude", exclude.toString());
	    return filterMap;
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
	 * @return a T object
	 * @param <T> a T class
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
