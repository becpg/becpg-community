package fr.becpg.repo.entity.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.BeCPGModel.EntityFormat;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.version.VersionExporter;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("entityFormatService")
public class EntityFormatServiceImpl implements EntityFormatService {

	@Autowired
	@Qualifier("mtAwareNodeService")
	private NodeService dbNodeService;

	@Autowired
	protected EntityListDAO entityListDAO;

	@Autowired
	private RemoteEntityService remoteEntityService;

	@Autowired
	protected ContentService contentService;

	@Autowired
	protected NamespaceService namespaceService;
	
	@Autowired
	private EntityReportService entityReportService;
	
	@Autowired
	private TransactionService transactionService;

	@Autowired
	private VersionService versionService;
	
	@Autowired
	@Qualifier("exporterComponent")
	private ExporterService exporterService;
	
	@Autowired
	private AssociationService associationService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private IntegrityChecker integrityChecker;
	
	@Autowired
	private EntityDictionaryService entityDictionaryService;
	
	@Autowired
	private TenantAdminService tenantAdminService;
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	protected Repository repositoryHelper;

	@Autowired
	protected RuleService ruleService;
	
	private static final Log logger = LogFactory.getLog(EntityFormatServiceImpl.class);

	@Override
	public void setDatalistFormat(NodeRef entityNodeRef, QName dataListType, EntityFormat format) {

		NodeRef entityListContainer = entityListDAO.getListContainer(entityNodeRef);

		if (entityListContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(entityListContainer, dataListType);
			setDatalistFormat(listNodeRef, format);
		}
	}

	@Override
	public void setDatalistFormat(NodeRef listNodeRef, EntityFormat format) {
		dbNodeService.setProperty(listNodeRef, BeCPGModel.PROP_ENTITY_FORMAT, format);
	}

	@Override
	public String getDatalistFormat(NodeRef entityNodeRef, QName dataListType) {

		NodeRef entityListContainer = entityListDAO.getListContainer(entityNodeRef);

		if (entityListContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(entityListContainer, dataListType);
			if (listNodeRef != null) {
				return getDatalistFormat(listNodeRef);
			}
		}

		return null;
	}

	@Override
	public String getDatalistFormat(NodeRef listNodeRef) {
		return dbNodeService.exists(listNodeRef) ? (String) dbNodeService.getProperty(listNodeRef, BeCPGModel.PROP_ENTITY_FORMAT) : null;
	}

	@Override
	public void setDataListData(NodeRef entityNodeRef, QName dataListType, String data) {

		NodeRef entityListContainer = entityListDAO.getListContainer(entityNodeRef);

		if (entityListContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(entityListContainer, dataListType);
			if (listNodeRef != null) {
				setDataListData(listNodeRef, data);
			}
		}
	}

	@Override
	public void setDataListData(NodeRef listNodeRef, String data) {
		ContentWriter contentWriter = this.contentService.getWriter(listNodeRef, BeCPGModel.PROP_ENTITY_DATA, true);
		contentWriter.setEncoding("UTF-8");
		contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
		contentWriter.putContent(data);
	}

	@Override
	public String getDataListData(NodeRef listNodeRef) {

		if (this.dbNodeService.hasAspect(listNodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)) {
			ContentReader reader = this.contentService.getReader(listNodeRef, BeCPGModel.PROP_ENTITY_DATA);
			if (reader != null) {
				return reader.getContentString();
			}
		}
		return null;

	}

	@Override
	public String getDataListData(NodeRef entityNodeRef, QName dataListType) {

		NodeRef entityListContainer = entityListDAO.getListContainer(entityNodeRef);

		if (entityListContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(entityListContainer, dataListType);
			return getDataListData(listNodeRef);
		}

		return null;
	}

	private String extractDatalistData(NodeRef entityNodeRef, QName dataListType, RemoteEntityFormat format) {

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			RemoteParams params = new RemoteParams(format);
			params.setFilteredLists(Arrays.asList(dataListType.getLocalName()));

			remoteEntityService.getEntity(entityNodeRef, out, params);

			return out.toString();
		} catch (IOException e) {
			logger.error("Failed to extract datalist", e);
		}

		return null;
	}

	@Override
	public void convertDataListFormat(NodeRef entityNodeRef, QName dataListType, EntityFormat toFormat) {

		NodeRef entityListContainer = entityListDAO.getListContainer(entityNodeRef);

		if (entityListContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(entityListContainer, dataListType);

			if (listNodeRef != null) {
				convertDataListFormat(listNodeRef, toFormat);
			}
		}
	}

	@Override
	public void convertDataListFormat(NodeRef listNodeRef, EntityFormat toFormat) {

		String fromFormat = getDatalistFormat(listNodeRef);

		if (!EntityFormat.JSON.toString().equals(fromFormat) && EntityFormat.JSON.equals(toFormat)) {

			String name = (String) dbNodeService.getProperty(listNodeRef, ContentModel.PROP_NAME);

			if (name.startsWith(RepoConsts.WUSED_PREFIX) || name.startsWith(RepoConsts.CUSTOM_VIEW_PREFIX)
					|| name.startsWith(RepoConsts.SMART_CONTENT_PREFIX) || name.contains("@")) {
				setDatalistFormat(listNodeRef, EntityFormat.NODE);
				return;
			}

			NodeRef entityNodeRef = dbNodeService.getPrimaryParent(dbNodeService.getPrimaryParent(listNodeRef).getParentRef()).getParentRef();

			QName dataListType = QName.createQName((String) dbNodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE),
					namespaceService);

			String json = extractDatalistData(entityNodeRef, dataListType, RemoteEntityFormat.json);

			setDataListData(listNodeRef, json);

			List<NodeRef> listItems = entityListDAO.getListItems(listNodeRef, dataListType);

			for (NodeRef listItem : listItems) {
				dbNodeService.removeChild(listNodeRef, listItem);
			}

			setDatalistFormat(listNodeRef, toFormat);

		} else if (EntityFormat.JSON.toString().equals(fromFormat) && EntityFormat.NODE.equals(toFormat)) {
			// TODO
		}

	}
	
	@Override
	public String extractEntityData(NodeRef entityNodeRef, EntityFormat toFormat) {
		
		if (EntityFormat.JSON.equals(toFormat)) {
			
			boolean isMLAware = MLPropertyInterceptor.isMLAware();
			
			MLPropertyInterceptor.setMLAware(false);
			
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				
				RemoteParams remoteParams = new RemoteParams(RemoteEntityFormat.json_all);
				
				JSONObject jsonParams = new JSONObject();
				jsonParams.put(RemoteParams.PARAM_APPEND_MLTEXT_CONSTRAINT, false);
				jsonParams.put(RemoteParams.PARAM_UPDATE_ENTITY_NODEREFS, true);
				jsonParams.put(RemoteParams.PARAM_REPLACE_HISTORY_NODEREFS, true);
				
				remoteParams.setJsonParams(jsonParams);
				
				remoteEntityService.getEntity(entityNodeRef, out, remoteParams);
				return out.toString();
			} catch (IOException | JSONException e) {
				logger.error("Failed to convert entity to JSON format", e);
			} finally {
				MLPropertyInterceptor.setMLAware(isMLAware);
			}
		}
		
		return null;
	}
	
	@Override
	public void convert(NodeRef entityNodeRef, EntityFormat toFormat) {

		String fromFormat = getEntityFormat(entityNodeRef);

		if (!EntityFormat.JSON.toString().equals(fromFormat) && EntityFormat.JSON.equals(toFormat)) {
			
			setEntityData(entityNodeRef, extractEntityData(entityNodeRef, EntityFormat.JSON));
			setEntityFormat(entityNodeRef, EntityFormat.JSON);

			NodeRef listContainer = entityListDAO.getListContainer(entityNodeRef);

			if (listContainer != null) {
				List<ChildAssociationRef> childAssocs = dbNodeService.getChildAssocs(listContainer);
				for (ChildAssociationRef childAssoc : childAssocs) {
					if (dbNodeService.getType(childAssoc.getChildRef()).equals(DataListModel.TYPE_DATALIST)) {
						List<ChildAssociationRef> listItems = dbNodeService.getChildAssocs(childAssoc.getChildRef());
						for (ChildAssociationRef listItem : listItems) {
							dbNodeService.removeChild(childAssoc.getChildRef(), listItem.getChildRef());
						}
					}
				}
			}
			
		} else if (EntityFormat.JSON.toString().equals(fromFormat) && EntityFormat.NODE.equals(toFormat)) {

			String entityJson = getEntityData(entityNodeRef);

			if (entityJson != null) {
				ByteArrayInputStream in = new ByteArrayInputStream(entityJson.getBytes());
				remoteEntityService.createOrUpdateEntity(entityNodeRef, in,  new RemoteParams(RemoteEntityFormat.json), null);
			}
			
			setEntityFormat(entityNodeRef, EntityFormat.NODE);
		}
		
	}

	@Override
	public void convert(final NodeRef from, final NodeRef to, EntityFormat toFormat) {

		integrityChecker.setEnabled(false);
		
		try {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				
				ExporterCrawlerParameters crawlerParameters = new ExporterCrawlerParameters();
				
				Location exportFrom = new Location(from);
				crawlerParameters.setExportFrom(exportFrom);
				
				crawlerParameters.setCrawlSelf(true);
				crawlerParameters.setExcludeChildAssocs(new QName[] { RenditionModel.ASSOC_RENDITION, ForumModel.ASSOC_DISCUSSION,
						BeCPGModel.ASSOC_ENTITYLISTS, ContentModel.ASSOC_RATINGS });
				
				crawlerParameters.setExcludeNamespaceURIs(Arrays.asList(ReportModel.TYPE_REPORT.getNamespaceURI()).toArray(new String[0]));
				
				exporterService.exportView(new VersionExporter(from, to, dbNodeService, entityDictionaryService), crawlerParameters, null);
				
				return null;
			}, false, true);
			
			
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				
				String fromName = (String) dbNodeService.getProperty(from, ContentModel.PROP_NAME);
				dbNodeService.setProperty(to, ContentModel.PROP_NAME, fromName);
				
				String versionLabel = (String) dbNodeService.getProperty(to, BeCPGModel.PROP_VERSION_LABEL);
				dbNodeService.setProperty(to, ContentModel.PROP_VERSION_LABEL, versionLabel);
				
				setEntityData(to, extractEntityData(from, toFormat));
				
				setEntityFormat(to, toFormat);
				
				return null;
				
			}, false, true);
			
			AuthenticationUtil.runAs(() -> {
				
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					
					entityReportService.generateReports(from, to);
					
					return null;
					
				}, false, true);
				return null;
			}, AuthenticationUtil.getAdminUserName());
			
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				
				dbNodeService.addAspect(from, ContentModel.ASPECT_TEMPORARY, null);
				
				dbNodeService.deleteNode(from);
				return null;
				
			}, false, true);
		} finally {
			integrityChecker.setEnabled(true);
		}

	}

	@Override
	public void createOrUpdateEntityFromJson(NodeRef entityNodeRef, String entityJson) {
		
		try {
			JSONObject root = new JSONObject(entityJson);
			
			if (root.has(RemoteEntityService.ELEM_ENTITY)) {
				JSONObject entity = root.getJSONObject(RemoteEntityService.ELEM_ENTITY);
				
				if (!entity.has(RemoteEntityService.ELEM_PARAMS)) {
					JSONObject params = new JSONObject();
					entity.put(RemoteEntityService.ELEM_PARAMS, params);
				}
				
				JSONObject params = entity.getJSONObject(RemoteEntityService.ELEM_PARAMS);
				params.put(RemoteParams.PARAM_FAIL_ON_ASSOC_NOT_FOUND, false);
			}

			ByteArrayInputStream in = new ByteArrayInputStream(root.toString().getBytes());
			remoteEntityService.createOrUpdateEntity(entityNodeRef, in, new RemoteParams(RemoteEntityFormat.json), null);
		} catch (JSONException e) {
			logger.error("Failed to parse JSON", e);
		}
		
	}
	
	@Override
	public void setEntityData(NodeRef entityNodeRef, String data) {
		ContentWriter contentWriter = this.contentService.getWriter(entityNodeRef, BeCPGModel.PROP_ENTITY_DATA, true);
		contentWriter.setEncoding("UTF-8");
		contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
		contentWriter.putContent(data);
	}

	@Override
	public String getEntityData(NodeRef entityNodeRef) {
		if (dbNodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)) {
			ContentReader reader = this.contentService.getReader(entityNodeRef, BeCPGModel.PROP_ENTITY_DATA);
			if (reader != null) {
				return reader.getContentString();
			}
		}

		return null;
	}

	@Override
	public void setEntityFormat(NodeRef entityNodeRef, EntityFormat format) {
		dbNodeService.setProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT, format);
	}

	@Override
	public String getEntityFormat(NodeRef entityNodeRef) {
		if (entityNodeRef != null && dbNodeService.exists(entityNodeRef) && dbNodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)) {
			Serializable prop = dbNodeService.getProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT);
			return prop == null ? null : prop.toString();
		}
		return null;
	}

	@Override
	public NodeRef convertVersionHistoryNodeRef(NodeRef node) {
		
		long start = System.currentTimeMillis();
		
		Set<NodeRef> relatives = findConvertibleRelatives(node, new HashSet<>(), null, -1, new AtomicInteger(0), null);
		
		if (relatives.size() > 1) {
			String name = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);

			StringBuilder sb = new StringBuilder();
			
			sb.append("Couldn't convert entity '" + name + "' because it is used by not converted entities :");
			
			for (NodeRef relative : relatives) {
				if (!relative.equals(node)) {
					String relativeName = (String) nodeService.getProperty(relative, ContentModel.PROP_NAME);
					sb.append(" '" + relativeName + "' ");
				}
			}
			logger.debug(sb.toString());
			
			return null;
		}
		
		for (NodeRef toMove : getContainedEntities(node)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				
				moveToImportToDoFolder(toMove);
				
				return null;
			}, false, true);
		}
		
		String name = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
		
		String versionLabel = (String) dbNodeService.getProperty(node, BeCPGModel.PROP_VERSION_LABEL);
		
		if (versionLabel == null) {
			versionLabel = (String) dbNodeService.getProperty(node, ContentModel.PROP_VERSION_LABEL);
		}
		
		if (versionLabel == null) {
			String[] splitted = name.split(RepoConsts.VERSION_NAME_DELIMITER);
			versionLabel = splitted[splitted.length - 1];
		}
		
		NodeRef parentNode = dbNodeService.getPrimaryParent(node).getParentRef();
		
		String parentName = (String) dbNodeService.getProperty(parentNode, ContentModel.PROP_NAME);
		
		NodeRef originalNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentName);
		
		if (!nodeService.exists(originalNode)) {
			originalNode = nodeService.getTargetAssocs(node, ContentModel.ASSOC_ORIGINAL).get(0).getTargetRef();
		}
		
		VersionHistory versionHistory = dbNodeService.exists(originalNode) ? versionService.getVersionHistory(originalNode) : null;

		if (versionHistory != null) {
			NodeRef versionNode = VersionUtil.convertNodeRef(versionHistory.getVersion(versionLabel).getFrozenStateNodeRef());
			
			convert(node, versionNode, EntityFormat.JSON);
			
			String tenantName = tenantAdminService.getCurrentUserDomain();
			
			if (TenantService.DEFAULT_DOMAIN.equals(tenantName)) {
				tenantName = "default";
			}
			
			long stop = System.currentTimeMillis();

			logger.debug("Converted entity '" + name + "', from " + node + " to " + versionNode + ", tenant : " + tenantName + ", time elapsed : " + (stop - start) + " ms");

			return versionNode;
		}
		
		return null;

	}

	@Override
	public void moveToImportToDoFolder(NodeRef toMove) {
		Rule classifyRule = null;
		
		List<Rule> rules = ruleService.getRules(repositoryHelper.getCompanyHome(), false);
		for (Rule rule : rules) {
			if (!rule.getRuleDisabled()) {
				if ("classifyEntityRule".equals(rule.getTitle())) {
					classifyRule = rule;
					break;
				}
			}
		}

		if (classifyRule != null) {
			ruleService.disableRule(classifyRule);
		}
		NodeRef originalParent = nodeService.getPrimaryParent(toMove).getParentRef();
		String parentName = (String) nodeService.getProperty(originalParent, ContentModel.PROP_NAME);
		
		NodeRef rootNode = nodeService.getRootNode(RepoConsts.SPACES_STORE);
		NodeRef importToDoNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(rootNode,RemoteEntityService.FULL_PATH_IMPORT_TO_DO);
		
		NodeRef newParent = nodeService.getChildByName(importToDoNodeRef, ContentModel.ASSOC_CONTAINS, parentName);
		
		if (newParent == null) {
			newParent = nodeService.createNode(importToDoNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER).getChildRef();
			nodeService.setProperty(newParent, ContentModel.PROP_NAME, parentName);
		}
		
		nodeService.moveNode(toMove, newParent, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
		
		if (classifyRule != null) {
			ruleService.enableRule(classifyRule);
		}
	}

	private Set<NodeRef> getContainedEntities(NodeRef node) {
		
		Set<NodeRef> result = new HashSet<>();
		
		for (NodeRef assocNodeRef : associationService.getChildAssocs(node, ContentModel.ASSOC_CONTAINS)) {
			if (entityDictionaryService.isSubClass(nodeService.getType(assocNodeRef), BeCPGModel.TYPE_ENTITY_V2)) {
				result.add(assocNodeRef);
			}
			result.addAll(getContainedEntities(assocNodeRef));
		}
		
		return result;
	}
	
	@Override
	public Set<NodeRef> findConvertibleRelatives(NodeRef originalEntity, Set<NodeRef> visited, final List<NodeRef> ignoredItems, final int maxProcessedNodes, AtomicInteger currentCount, String path) {
		Set<NodeRef> ret = new LinkedHashSet<>();
		
		if ((maxProcessedNodes >= 0 && currentCount.get() >= maxProcessedNodes) || visited.contains(originalEntity) || !nodeService.exists(originalEntity)) {
			return ret;
		}
		
		visited.add(originalEntity);
		
		Set<NodeRef> refs = new HashSet<>();
		
		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(originalEntity, RegexQNamePattern.MATCH_ALL);

		List<ChildAssociationRef> parentRefs = nodeService.getParentAssocs(originalEntity);
		
		for (AssociationRef assocRef : assocRefs) {
			refs.add(assocRef.getSourceRef());
		}
		
		for (ChildAssociationRef parentRef : parentRefs) {
			refs.add(parentRef.getParentRef());
		}
		
		for (NodeRef ref : refs) {
			
			NodeRef entitySource = entityService.getEntityNodeRef(ref, BeCPGModel.TYPE_ENTITY_V2);
			
			if (entitySource != null) {
				
				logger.trace("findConvertibleRelatives from " + originalEntity + "to " + entitySource);
				
				Set<NodeRef> convertibleRelatives = findConvertibleRelatives(entitySource, visited, ignoredItems, maxProcessedNodes, currentCount, path);
				
				if (ignoredItems != null) {
					convertibleRelatives.removeAll(ignoredItems);
				}
				
				ret.addAll(convertibleRelatives);
			}
		}
		
		
		if (ignoredItems == null || !ignoredItems.contains(originalEntity)) {
			
			boolean isConvertible = entityDictionaryService.isSubClass(nodeService.getType(originalEntity), BeCPGModel.TYPE_ENTITY_V2)
					&& !originalEntity.getStoreRef().getProtocol().contains(VersionBaseModel.STORE_PROTOCOL)
					&& !originalEntity.getStoreRef().getIdentifier().contains(Version2Model.STORE_ID);
			
			if (isConvertible) {
				isConvertible = nodeService.hasAspect(originalEntity, BeCPGModel.ASPECT_COMPOSITE_VERSION);
				
				if (!isConvertible && path != null) {
					isConvertible = nodeService.getPath(originalEntity).toPrefixString(namespaceService).contains(path);
				}
			}
			
			if (isConvertible) {
				ret.add(originalEntity);
				currentCount.addAndGet(1);
				logger.trace("found " + currentCount.get() + " items out of " + maxProcessedNodes);
			}
		}
		
		return ret;
	}
	
}
