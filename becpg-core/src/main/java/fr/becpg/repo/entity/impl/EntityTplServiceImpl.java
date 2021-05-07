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
package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.EntityTplPlugin;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.Synchronisable;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>
 * EntityTplServiceImpl class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("entityTplService")
public class EntityTplServiceImpl implements EntityTplService {

	private static final String ASYNC_ACTION_URL_PREFIX = "page/entity-data-lists?list=View-properties&nodeRef=";

	private static final String ENTITY_DATALIST_KEY_PREFIX = "entity-datalist-";

	private static final Log logger = LogFactory.getLog(EntityTplServiceImpl.class);

	private static final Set<QName> isIgnoredAspect = new HashSet<>();

	@Autowired
	private NodeService nodeService;

	@Autowired
	@Qualifier("mlAwareNodeService")
	private NodeService mlNodeService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private FormulationService<FormulatedEntity> formulationService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityService entityService;

	@Autowired
	private RuntimeRuleService ruleService;

	@Autowired
	private EntityTplPlugin[] entityTplPlugins;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	BeCPGMailService beCPGMailService;

	private ReentrantLock lock = new ReentrantLock();

	static {
		isIgnoredAspect.add(ContentModel.ASPECT_VERSIONABLE);
		isIgnoredAspect.add(ContentModel.ASPECT_TEMPORARY);
		isIgnoredAspect.add(ContentModel.ASPECT_WORKING_COPY);
		isIgnoredAspect.add(ContentModel.ASPECT_COPIEDFROM);
		isIgnoredAspect.add(TransferModel.ASPECT_TRANSFERRED);
		isIgnoredAspect.add(RuleModel.ASPECT_RULES);
		isIgnoredAspect.add(BeCPGModel.ASPECT_ENTITY_TPL);
	};

	private boolean ignoreAspect(QName aspect) {
		return (aspect.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI) || isIgnoredAspect.contains(aspect));
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createEntityTpl(NodeRef parentNodeRef, QName entityType, String entityTplName, boolean enabled, boolean isDefault,
			Set<QName> entityLists, Set<String> subFolders) {

		TypeDefinition typeDef = entityDictionaryService.getType(entityType);
		if (entityTplName == null) {
			entityTplName = typeDef.getTitle(entityDictionaryService);
		}
		// entityTpl
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, entityTplName);
		properties.put(BeCPGModel.PROP_ENTITY_TPL_ENABLED, enabled);
		properties.put(BeCPGModel.PROP_ENTITY_TPL_IS_DEFAULT, isDefault);

		NodeRef entityTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, entityTplName);
		// #1911 do not update existing templates
		if (entityTplNodeRef == null) {
			logger.debug("Creating a new entity template: " + entityTplName);
			entityTplNodeRef = nodeService
					.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, entityType.getLocalName()), entityType, properties)
					.getChildRef();

			// entityLists
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityTplNodeRef);
			if (listContainerNodeRef == null) {
				listContainerNodeRef = entityListDAO.createListContainer(entityTplNodeRef);
			}

			if (entityLists != null) {
				for (QName entityList : entityLists) {

					NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, entityList);
					if (listNodeRef == null) {
						entityListDAO.createList(listContainerNodeRef, entityList);
					}
				}
			}

			// subFolders
			if (subFolders != null) {
				for (String subFolder : subFolders) {

					properties.clear();
					properties.put(ContentModel.PROP_NAME, TranslateHelper.getTranslatedPath(subFolder));
					NodeRef documentsFolderNodeRef = nodeService.getChildByName(entityTplNodeRef, ContentModel.ASSOC_CONTAINS,
							(String) properties.get(ContentModel.PROP_NAME));
					if (documentsFolderNodeRef == null) {
						nodeService.createNode(entityTplNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(subFolder)),
								ContentModel.TYPE_FOLDER, properties).getChildRef();
					}
				}
			}
		} else {
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityTplNodeRef);
			if (listContainerNodeRef == null) {
				listContainerNodeRef = entityListDAO.createListContainer(entityTplNodeRef);
			}

			for (QName entityList : entityLists) {

				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, entityList);

				if (listNodeRef != null) {

					ClassDefinition classDef = entityDictionaryService.getClass(entityList);

					MLText title = (MLText) mlNodeService.getProperty(listNodeRef, ContentModel.PROP_TITLE);
					MLText description = (MLText) mlNodeService.getProperty(listNodeRef, ContentModel.PROP_DESCRIPTION);

					MLText classTitleMLText = TranslateHelper.getTemplateTitleMLText(classDef.getName());
					MLText classDescritptionMLText = TranslateHelper.getTemplateDescriptionMLText(classDef.getName());

					if ((title != null) && (classTitleMLText != null)) {
						mlNodeService.setProperty(listNodeRef, ContentModel.PROP_TITLE, MLTextHelper.merge(title, classTitleMLText));
					}
					if ((description != null) && (classDescritptionMLText != null)) {
						mlNodeService.setProperty(listNodeRef, ContentModel.PROP_DESCRIPTION,
								MLTextHelper.merge(description, classDescritptionMLText));
					}

				}
			}

		}
		return entityTplNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createWUsedList(NodeRef entityTplNodeRef, QName typeQName, QName assocQName) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityTplNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entityTplNodeRef);
		}

		String name = RepoConsts.WUSED_PREFIX;
		if (assocQName != null) {
			name += RepoConsts.WUSED_SEPARATOR + assocQName.toPrefixString(namespaceService).replace(":", "_");
		}

		NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, name);
		if (listNodeRef == null) {

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name);
			properties.put(ContentModel.PROP_TITLE, TranslateHelper.getTranslatedKey("entity-datalist-wused-title"));
			properties.put(ContentModel.PROP_DESCRIPTION, TranslateHelper.getTranslatedKey("entity-datalist-wused-description"));
			properties.put(DataListModel.PROP_DATALISTITEMTYPE, typeQName.toPrefixString(namespaceService));

			listNodeRef = nodeService.createNode(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
					DataListModel.TYPE_DATALIST, properties).getChildRef();

		}
		return listNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createView(NodeRef entityTplNodeRef, QName typeQName, String name) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityTplNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entityTplNodeRef);
		}

		NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, name);
		if (listNodeRef == null) {

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name);
			properties.put(ContentModel.PROP_TITLE, TranslateHelper.getTranslatedKey(ENTITY_DATALIST_KEY_PREFIX + name.toLowerCase() + "-title"));
			properties.put(ContentModel.PROP_DESCRIPTION,
					TranslateHelper.getTranslatedKey(ENTITY_DATALIST_KEY_PREFIX + name.toLowerCase() + "-description"));
			properties.put(DataListModel.PROP_DATALISTITEMTYPE, typeQName.toPrefixString(namespaceService));

			listNodeRef = nodeService.createNode(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
					DataListModel.TYPE_DATALIST, properties).getChildRef();

		} else {

			MLText title = (MLText) mlNodeService.getProperty(listNodeRef, ContentModel.PROP_TITLE);
			MLText description = (MLText) mlNodeService.getProperty(listNodeRef, ContentModel.PROP_DESCRIPTION);

			MLText classTitleMLText = TranslateHelper.getTranslatedKey(ENTITY_DATALIST_KEY_PREFIX + name.toLowerCase() + "-title");
			MLText classDescritptionMLText = TranslateHelper.getTranslatedKey(ENTITY_DATALIST_KEY_PREFIX + name.toLowerCase() + "-description");

			mlNodeService.setProperty(listNodeRef, ContentModel.PROP_TITLE, MLTextHelper.merge(title, classTitleMLText));
			mlNodeService.setProperty(listNodeRef, ContentModel.PROP_DESCRIPTION, MLTextHelper.merge(description, classDescritptionMLText));

		}
		return listNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEntityTpl(QName nodeType) {

		if (nodeType == null) {
			return null;
		}

		List<NodeRef> tplsNodeRef = BeCPGQueryBuilder.createQuery().ofType(nodeType).withAspect(BeCPGModel.ASPECT_ENTITY_TPL).inDB().list();

		for (NodeRef tpl : tplsNodeRef) {

			try {
				if (!nodeService.hasAspect(tpl, BeCPGModel.ASPECT_COMPOSITE_VERSION)
						&& (Boolean) nodeService.getProperty(tpl, BeCPGModel.PROP_ENTITY_TPL_ENABLED)
						&& (Boolean) nodeService.getProperty(tpl, BeCPGModel.PROP_ENTITY_TPL_IS_DEFAULT)) {
					return tpl;
				}
			} catch (InvalidNodeRefException | InvalidAspectException e) {
				logger.error(e, e);
			}

		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public void synchronizeEntities(NodeRef tplNodeRef) {

		boolean runWithSuccess = false;
		StopWatch watch = new StopWatch();
		watch.start();

		if (lock.tryLock()) {
			try {

				RepositoryEntity entityTpl = alfrescoRepository.findOne(tplNodeRef);

				List<NodeRef> entityNodeRefs = getEntitiesToUpdate(tplNodeRef);
				if (logger.isInfoEnabled()) {
					logger.info("Launch synchronizeEntities, size " + entityNodeRefs.size());
				}

				runWithSuccess = doInBatch(entityNodeRefs, 10, entityNodeRef -> {

					final Map<QName, List<? extends RepositoryEntity>> datalistsTpl = repositoryEntityDefReader.getDataLists(entityTpl);

					Map<QName, ?> datalistViews = repositoryEntityDefReader.getDataListViews(entityTpl);

					for (Map.Entry<QName, ?> dataListViewEntry : datalistViews.entrySet()) {
						Map<QName, List<? extends RepositoryEntity>> tmp = repositoryEntityDefReader.getDataLists(dataListViewEntry.getValue());
						datalistsTpl.putAll(tmp);

					}

					if ((datalistsTpl != null) && !datalistsTpl.isEmpty()) {

						RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
						Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);
						Map<QName, ?> datalistViews1 = repositoryEntityDefReader.getDataListViews(entity);
						for (Map.Entry<QName, ?> dataListViewEntry : datalistViews1.entrySet()) {
							Map<QName, List<? extends RepositoryEntity>> tmp = repositoryEntityDefReader.getDataLists(dataListViewEntry.getValue());
							datalists.putAll(tmp);
						}

						NodeRef listContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(entity);

						for (Map.Entry<QName, List<? extends RepositoryEntity>> entry : datalistsTpl.entrySet()) {
							QName dataListQName = entry.getKey();

							List<BeCPGDataObject> dataListItems = (List<BeCPGDataObject>) datalists.get(dataListQName);

							boolean update = false;

							for (EntityTplPlugin entityTplPlugin : entityTplPlugins) {
								if (entityTplPlugin.shouldSynchronizeDataList(entity, dataListQName)) {
									entityTplPlugin.synchronizeDataList(entity, dataListItems,
											(List<BeCPGDataObject>) datalistsTpl.get(dataListQName));
									update = true;
								}
							}

							if (!update) {

								for (RepositoryEntity dataListItemTpl : entry.getValue()) {
									Map<QName, Serializable> identAttrTpl = repositoryEntityDefReader.getIdentifierAttributes(dataListItemTpl);

									if (!identAttrTpl.isEmpty()) {
										boolean isFound = false;
										// look on instance
										for (RepositoryEntity dataListItem : dataListItems) {

											Map<QName, Serializable> identAttr = repositoryEntityDefReader.getIdentifierAttributes(dataListItem);
											if (identAttrTpl.equals(identAttr)) {

												isFound = true;
												break;
											}
										}

										if (!isFound) {
											dataListItemTpl.setName(null);
											dataListItemTpl.setNodeRef(null);
											dataListItemTpl.setParentNodeRef(null);

											if ((dataListItemTpl instanceof CompositeDataItem)
													&& (((CompositeDataItem<RepositoryEntity>) dataListItemTpl).getParent() != null)) {
												((CompositeDataItem<RepositoryEntity>) dataListItemTpl).setParent(findCompositeParent(
														((CompositeDataItem<RepositoryEntity>) dataListItemTpl).getParent(), dataListItems));
											}

											if (dataListItemTpl instanceof Synchronisable) {
												if (((Synchronisable) dataListItemTpl).isSynchronisable()) {
													dataListItems.add((BeCPGDataObject) dataListItemTpl);
													update = true;
												}
											} else {
												// Synchronize always
												dataListItems.add((BeCPGDataObject) dataListItemTpl);
												update = true;
											}
										}
									}

								}
							}

							if (update) {
								alfrescoRepository.saveDataList(listContainerNodeRef, dataListQName, dataListQName, dataListItems);
							}

						}

						synchronizeTitle(entityTpl, entity);

					}

					// synchronize folders
					// remove empty folders that are not in the template
					for (FileInfo folder : fileFolderService.listFolders(entityNodeRef)) {
						if ((!"DataLists".equals(folder.getName())) && folder.getType().equals(ContentModel.TYPE_FOLDER)
								&& (nodeService.getChildByName(tplNodeRef, ContentModel.ASSOC_CONTAINS, folder.getName()) == null)
								&& (fileFolderService.list(folder.getNodeRef()).isEmpty())) {

							if (logger.isDebugEnabled()) {
								logger.debug("Remove folder " + folder.getName() + " of node " + entityNodeRef);
							}
							fileFolderService.delete(folder.getNodeRef());
						}
					}

					// copy folders of template that are not in the entity
					for (FileInfo folder : fileFolderService.listFolders(tplNodeRef)) {
						if ((!"DataLists".equals(folder.getName())) && folder.getType().equals(ContentModel.TYPE_FOLDER)
								&& (nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS, folder.getName()) == null)) {

							if (logger.isDebugEnabled()) {
								logger.debug("Copying folder " + folder.getName() + " to node " + entityNodeRef);
							}

							try {
								fileFolderService.copy(folder.getNodeRef(), entityNodeRef, null);
							} catch (FileExistsException | FileNotFoundException e) {
								logger.warn("Unable to synchronize folder " + folder.getName() + " of node " + entityNodeRef + ": " + e.getMessage());
							}
						}
					}

					// synchronize aspects
					// copy missing aspects
					Set<QName> aspects = nodeService.getAspects(tplNodeRef);
					for (QName aspect : aspects) {
						if (!nodeService.hasAspect(entityNodeRef, aspect) && !ignoreAspect(aspect)) {
							nodeService.addAspect(entityNodeRef, aspect, null);
						}
					}

				});

			} finally {
				lock.unlock();
				watch.stop();
				beCPGMailService.sendMailOnAsyncAction(AuthenticationUtil.getFullyAuthenticatedUser(), "entitiesTemplate.synchronize",
						ASYNC_ACTION_URL_PREFIX + tplNodeRef, runWithSuccess, watch.getTotalTimeSeconds());
			}
		} else {
			logger.error("Only one massive operation at a time");
		}
	}

	private void synchronizeTitle(RepositoryEntity entityTpl, RepositoryEntity entity) {

		/*-- copy source datalists--*/
		logger.debug("copy source datalists");
		NodeRef sourceListContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(entityTpl);
		NodeRef targetListContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(entity);

		if ((sourceListContainerNodeRef != null) && (targetListContainerNodeRef != null)) {

			List<NodeRef> sourceListsNodeRef = entityListDAO.getExistingListsNodeRef(sourceListContainerNodeRef);
			for (NodeRef sourceListNodeRef : sourceListsNodeRef) {

				String dataListType = (String) nodeService.getProperty(sourceListNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
				String name = (String) nodeService.getProperty(sourceListNodeRef, ContentModel.PROP_NAME);
				QName listQName = QName.createQName(dataListType, namespaceService);

				NodeRef existingListNodeRef;

				if (name.startsWith(RepoConsts.WUSED_PREFIX) || name.startsWith(RepoConsts.CUSTOM_VIEW_PREFIX)
						|| name.startsWith(RepoConsts.SMART_CONTENT_PREFIX) || name.contains("@")) {
					existingListNodeRef = entityListDAO.getList(targetListContainerNodeRef, name);
				} else {
					existingListNodeRef = entityListDAO.getList(targetListContainerNodeRef, listQName);
				}

				if (existingListNodeRef != null) {
					MLText title = (MLText) mlNodeService.getProperty(sourceListNodeRef, ContentModel.PROP_TITLE);
					MLText description = (MLText) mlNodeService.getProperty(sourceListNodeRef, ContentModel.PROP_DESCRIPTION);

					mlNodeService.setProperty(existingListNodeRef, ContentModel.PROP_TITLE, title);
					mlNodeService.setProperty(existingListNodeRef, ContentModel.PROP_DESCRIPTION, description);
				}

			}
		}

	}

	private RepositoryEntity findCompositeParent(RepositoryEntity parent, List<BeCPGDataObject> dataListItems) {

		for (RepositoryEntity dataListItem : dataListItems) {

			Map<QName, Serializable> identAttrTpl = repositoryEntityDefReader.getIdentifierAttributes(dataListItem);

			if (!identAttrTpl.isEmpty()) {

				Map<QName, Serializable> identAttr = repositoryEntityDefReader.getIdentifierAttributes(parent);
				if (identAttrTpl.equals(identAttr)) {
					return dataListItem;
				}
			}

		}

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void formulateEntities(NodeRef tplNodeRef) {
		boolean runWithSuccess = false;
		StopWatch watch = new StopWatch();
		watch.start();

		if (lock.tryLock()) {
			try {

				List<NodeRef> entityNodeRefs = getEntitiesToUpdate(tplNodeRef);

				if (logger.isInfoEnabled()) {
					logger.info("Launch formulateEntities, size " + entityNodeRefs.size());
				}

				runWithSuccess = doInBatch(entityNodeRefs, 5, entityNodeRef -> {
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("Formulate : " + entityNodeRef);
						}
						formulationService.formulate(entityNodeRef);
					} catch (FormulateException e) {

						logger.error(e, e);
					}

				});

			} finally {
				lock.unlock();
				watch.stop();
				beCPGMailService.sendMailOnAsyncAction(AuthenticationUtil.getFullyAuthenticatedUser(), "entitiesTemplate.formulate",
						ASYNC_ACTION_URL_PREFIX + tplNodeRef, runWithSuccess, watch.getTotalTimeSeconds());
			}
		} else {
			logger.error("Only one massive operation at a time");
		}

	}

	private interface BatchCallBack {
		void run(NodeRef entityNodeRef);
	}

	private boolean doInBatch(final List<NodeRef> entityNodeRefs, final int batchSize, final BatchCallBack batchCallBack) {

		StopWatch watch = null;
		if (logger.isInfoEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		AtomicInteger batchCount = new AtomicInteger(0);
		AtomicInteger errorCount = new AtomicInteger(0);
		int total = entityNodeRefs.size() / batchSize;

		L2CacheSupport.doInCacheContext(() -> {

			for (final List<NodeRef> subList : Lists.partition(entityNodeRefs, batchSize)) {

				RunAsWork<Object> actionRunAs = () -> {
					RetryingTransactionCallback<Object> actionCallback = () -> {

						for (final NodeRef entityNodeRef : subList) {
							try {

								policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
								policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
								policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
								policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

								batchCallBack.run(entityNodeRef);

							} finally {
								policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
								policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
								policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
								policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
							}
						}
						return null;

					};
					return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
				};

				try {

					logger.info(" - Run batch:" + (batchCount.getAndIncrement()) + " over " + total);
					AuthenticationUtil.runAsSystem(actionRunAs);
				} catch (Exception e) {
					errorCount.incrementAndGet();
					logger.error("Error running batch: " + batchCount.get(), e);

				}
			}

		}, false, true);

		if (logger.isInfoEnabled() && (watch != null)) {
			watch.stop();
			logger.info("Batch takes " + watch.getTotalTimeSeconds() + " seconds");
		}

		return errorCount.get() == 0;
	}

	private List<NodeRef> getEntitiesToUpdate(NodeRef tplNodeRef) {

		List<NodeRef> entityNodeRefs = new ArrayList<>();
		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(tplNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);

		for (AssociationRef assocRef : assocRefs) {
			if (!nodeService.hasAspect(assocRef.getSourceRef(), BeCPGModel.ASPECT_COMPOSITE_VERSION) && !tplNodeRef.equals(assocRef.getSourceRef())) {
				entityNodeRefs.add(assocRef.getSourceRef());
			}
		}

		return entityNodeRefs;
	}

	/** {@inheritDoc} */
	@Override
	public void synchronizeEntity(NodeRef entityNodeRef, NodeRef entityTplNodeRef) {
		if (entityTplNodeRef != null) {

			StopWatch watch = null;
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}

			try {
				((RuleService) ruleService).disableRules(entityNodeRef);

				// Desactivate for all the transaction (do not reactivate it)
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);

				for (EntityTplPlugin entityTplPlugin : entityTplPlugins) {
					entityTplPlugin.beforeSynchronizeEntity(entityNodeRef, entityTplNodeRef);
				}

				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

				// copy files
				entityService.copyFiles(entityTplNodeRef, entityNodeRef);

				// copy datalists
				entityListDAO.copyDataLists(entityTplNodeRef, entityNodeRef, false);

				// copy missing aspects
				Set<QName> aspects = nodeService.getAspects(entityTplNodeRef);
				for (QName aspect : aspects) {
					if (!nodeService.hasAspect(entityNodeRef, aspect) && !ignoreAspect(aspect)) {
						nodeService.addAspect(entityNodeRef, aspect, null);
					}
				}

				for (EntityTplPlugin entityTplPlugin : entityTplPlugins) {
					entityTplPlugin.synchronizeEntity(entityNodeRef, entityTplNodeRef);
				}

			} finally {
				((RuleService) ruleService).enableRules(entityNodeRef);
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			}

			// Copy rules
			if (nodeService.hasAspect(entityTplNodeRef, RuleModel.ASPECT_RULES)
					&& !((RuleService) ruleService).getRules(entityTplNodeRef, false).isEmpty()) {
				boolean hasRule = false;

				// Check whether the node already has rules or not
				if (nodeService.hasAspect(entityNodeRef, RuleModel.ASPECT_RULES)) {

					// Check for a linked to node
					NodeRef linkedToNode = ((RuleService) ruleService).getLinkedToRuleNode(entityNodeRef);
					if (linkedToNode == null) {
						// if the node has no rules we can delete the folder
						// ready to link
						List<Rule> rules = ((RuleService) ruleService).getRules(entityNodeRef, false);
						if (!rules.isEmpty()) {
							hasRule = true;
						} else {
							// Delete the rules system folder
							NodeRef ruleFolder = ruleService.getSavedRuleFolderAssoc(entityNodeRef).getChildRef();
							nodeService.deleteNode(ruleFolder);
						}
					} else {
						// Just remove the aspect and have the associated
						// data automatically removed
						nodeService.removeAspect(entityNodeRef, RuleModel.ASPECT_RULES);
					}
				}

				if (!hasRule) {

					// Create the destination folder as a secondary child of
					// the first
					NodeRef ruleSetNodeRef = ruleService.getSavedRuleFolderAssoc(entityTplNodeRef).getChildRef();
					// The required aspect will automatically be added to
					// the node
					nodeService.addChild(entityNodeRef, ruleSetNodeRef, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER);

				} else {
					logger.warn("The current folder has rules and can not be linked to another folder.");
				}

			}

			if (logger.isDebugEnabled() && (watch != null)) {
				watch.stop();
				logger.debug("Synchronize entity in " + watch.getTotalTimeSeconds() + " seconds");
			}

		}

	}

	/** {@inheritDoc} */
	@Override
	public void removeDataListOnEntities(NodeRef entityTplNodeRef, String entityListName) {

		boolean runWithSuccess = false;
		StopWatch watch = new StopWatch();
		watch.start();

		if (lock.tryLock()) {
			try {

				List<NodeRef> entities = getEntitiesToUpdate(entityTplNodeRef);

				runWithSuccess = doInBatch(entities, 10, entityNodeRef -> {

					NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
					NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, entityListName);

					if (listNodeRef != null) {
						logger.debug("Deleting list with node: " + listNodeRef + " on entity: " + entityNodeRef + " ("
								+ nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME) + ")");
						nodeService.addAspect(listNodeRef, ContentModel.ASPECT_TEMPORARY, null);
						nodeService.deleteNode(listNodeRef);
					}

				});

				NodeRef tplListContainerNodeRef = entityListDAO.getListContainer(entityTplNodeRef);
				NodeRef tplListNodeRef = entityListDAO.getList(tplListContainerNodeRef, entityListName);

				if (tplListNodeRef != null) {
					logger.debug("Deleting list with node: " + tplListNodeRef + " on template: " + entityTplNodeRef + " ("
							+ nodeService.getProperty(entityTplNodeRef, ContentModel.PROP_NAME) + ")");
					nodeService.addAspect(tplListNodeRef, ContentModel.ASPECT_TEMPORARY, null);
					nodeService.deleteNode(tplListNodeRef);
				}

			} finally {
				lock.unlock();
				watch.stop();
				beCPGMailService.sendMailOnAsyncAction(AuthenticationUtil.getFullyAuthenticatedUser(), "entitiesTemplate.removeDataList",
						ASYNC_ACTION_URL_PREFIX + entityTplNodeRef, runWithSuccess, watch.getTotalTimeSeconds());
			}
		} else {
			logger.error("Only one massive operation at a time");
		}

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createActivityList(NodeRef entityNodeRef, QName typeActivityList) {
		// entityLists
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entityNodeRef);
		}
		NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, typeActivityList);
		if (listNodeRef == null) {
			listNodeRef = entityListDAO.createList(listContainerNodeRef, typeActivityList);
		}

		return listNodeRef;
	}

}
