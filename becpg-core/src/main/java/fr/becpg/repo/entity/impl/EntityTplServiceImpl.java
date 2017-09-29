/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.EntityTplPlugin;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.Synchronisable;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("entityTplService")
public class EntityTplServiceImpl implements EntityTplService {

	private static final Log logger = LogFactory.getLog(EntityTplServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private DictionaryService dictionaryService;

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

	private ReentrantLock lock = new ReentrantLock();

	/**
	 * Create the entityTpl
	 *
	 * @param entityTplsNodeRef
	 * @param entityType
	 */
	@Override
	public NodeRef createEntityTpl(NodeRef parentNodeRef, QName entityType, String entityTplName, boolean enabled, boolean isDefault,
			Set<QName> entityLists, Set<String> subFolders) {

		TypeDefinition typeDef = dictionaryService.getType(entityType);
		if (entityTplName == null) {
			entityTplName = typeDef.getTitle(dictionaryService);
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
						listNodeRef = entityListDAO.createList(listContainerNodeRef, entityList);
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
		}
		return entityTplNodeRef;
	}

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
			properties.put(ContentModel.PROP_TITLE, TranslateHelper.getTranslatedKey("entity-datalist-" + name.toLowerCase() + "-title"));
			properties.put(ContentModel.PROP_DESCRIPTION, TranslateHelper.getTranslatedKey("entity-datalist-" + name.toLowerCase() + "-description"));
			properties.put(DataListModel.PROP_DATALISTITEMTYPE, typeQName.toPrefixString(namespaceService));

			listNodeRef = nodeService.createNode(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
					DataListModel.TYPE_DATALIST, properties).getChildRef();

		}
		return listNodeRef;
	}

	/**
	 * Get the entityTpl
	 */
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

	@Override
	@SuppressWarnings("unchecked")
	public void synchronizeEntities(NodeRef tplNodeRef) {

		if (lock.tryLock()) {
			try {

				logger.debug("synchronizeEntities");
				RepositoryEntity entityTpl = alfrescoRepository.findOne(tplNodeRef);

				logger.debug("entityTpl" + entityTpl.toString());

				final Map<QName, List<? extends RepositoryEntity>> datalistsTpl = repositoryEntityDefReader.getDataLists(entityTpl);

				Map<QName, ?> datalistViews = repositoryEntityDefReader.getDataListViews(entityTpl);
				for (Map.Entry<QName, ?> dataListViewEntry : datalistViews.entrySet()) {

					Map<QName, List<? extends RepositoryEntity>> tmp = repositoryEntityDefReader.getDataLists(dataListViewEntry.getValue());
					datalistsTpl.putAll(tmp);

				}

				if ((datalistsTpl != null) && !datalistsTpl.isEmpty()) {

					List<NodeRef> entityNodeRefs = getEntitiesToUpdate(tplNodeRef);
					logger.debug("synchronize entityNodeRefs, size " + entityNodeRefs.size());

					doInBatch(entityNodeRefs, 10, entityNodeRef -> {

						RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
						Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);

						Map<QName, ?> datalistViews1 = repositoryEntityDefReader.getDataListViews(entity);
						for (Map.Entry<QName, ?> dataListViewEntry : datalistViews1.entrySet()) {
							Map<QName, List<? extends RepositoryEntity>> tmp = repositoryEntityDefReader.getDataLists(dataListViewEntry.getValue());
							datalists.putAll(tmp);
						}

						NodeRef listContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(entity);

						for (QName dataListQName : datalistsTpl.keySet()) {

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

								for (RepositoryEntity dataListItemTpl : datalistsTpl.get(dataListQName)) {

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

											// if we change identifier assoc
											// -> Duplicate child name not
											// allowed
											dataListItemTpl.setName(null);
											dataListItemTpl.setNodeRef(null);
											dataListItemTpl.setParentNodeRef(null);

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

						// synchronize folders
						// clean empty folders
						for (FileInfo folder : fileFolderService.listFolders(entityNodeRef)) {
							if (logger.isDebugEnabled()) {
								logger.debug("Synchro, checking empty folder " + folder.getName() + " of node "
										+ nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME) + ", template = "
										+ nodeService.getProperty(tplNodeRef, ContentModel.PROP_NAME));
							}
							if (fileFolderService.list(folder.getNodeRef()).size() == 0) {
								fileFolderService.delete(folder.getNodeRef());
							}
						}

						// copy folders of template
						List<AssociationRef> products = nodeService.getSourceAssocs(tplNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);
						boolean exists = products.stream().anyMatch(assoc -> assoc.getSourceRef().equals(entityNodeRef));

						if (exists) {
							for (FileInfo folder : fileFolderService.listFolders(tplNodeRef)) {
								if (logger.isDebugEnabled()) {
									logger.debug("Synchro, copying folder " + folder.getName() + " to node "
											+ nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME) + ", template = "
											+ nodeService.getProperty(tplNodeRef, ContentModel.PROP_NAME));
								}
								try {
									fileFolderService.copy(folder.getNodeRef(), entityNodeRef, null);
								} catch (Exception e) {
									logger.warn(
											"Unable to synchronize folder " + folder.getName() + " of node " + entityNodeRef + ": " + e.getMessage());
								}
							}
						}

					});
				}
			} finally {
				lock.unlock();
			}
		} else {
			logger.error("Only one massive operation at a time");
		}
	}

	@Override
	public void formulateEntities(NodeRef tplNodeRef) throws FormulateException {

		if (lock.tryLock()) {
			try {
				List<NodeRef> entityNodeRefs = getEntitiesToUpdate(tplNodeRef);

				doInBatch(entityNodeRefs, 5, entityNodeRef -> {
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
			}
		} else {
			logger.error("Only one massive operation at a time");
		}

	}

	private interface BatchCallBack {
		void run(NodeRef entityNodeRef);
	}

	private void doInBatch(final List<NodeRef> entityNodeRefs, final int batchSize, final BatchCallBack batchCallBack) {

		StopWatch watch = null;
		if (logger.isInfoEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
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
				AuthenticationUtil.runAsSystem(actionRunAs);

			}

		}, false, true);

		if (logger.isInfoEnabled()) {
			watch.stop();
			logger.info("Batch takes " + watch.getTotalTimeSeconds() + " seconds");
		}

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
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				// copy files
				entityService.copyFiles(entityTplNodeRef, entityNodeRef);

				// copy datalists
				entityListDAO.copyDataLists(entityTplNodeRef, entityNodeRef, false);

				// copy missing aspects
				Set<QName> aspects = nodeService.getAspects(entityTplNodeRef);
				for (QName aspect : aspects) {
					if (!nodeService.hasAspect(entityNodeRef, aspect) && !BeCPGModel.ASPECT_ENTITY_TPL.isMatch(aspect)) {
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

			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("Synchronize entity in " + watch.getTotalTimeSeconds() + " seconds");
			}

		}

	}
	
	@Override
	public void removeDataListOnEntities(NodeRef entityTplNodeRef, QName entityList){
		List<NodeRef> entities = getEntitiesToUpdate(entityTplNodeRef);		

		doInBatch(entities, 10, entityNodeRef -> {
			
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, entityList);
			
			if (listNodeRef != null) {
				logger.debug("Deleting list with node: "+listNodeRef+" on entity: "+entityNodeRef+" ("+nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME)+")");
				nodeService.deleteNode(listNodeRef);
			}

		});
		
		NodeRef tplListContainerNodeRef = entityListDAO.getListContainer(entityTplNodeRef);
		NodeRef tplListNodeRef = entityListDAO.getList(tplListContainerNodeRef, entityList);
		
		if (tplListNodeRef != null) {
			logger.debug("Deleting list with node: "+tplListNodeRef+" on template: "+entityTplNodeRef+" ("+nodeService.getProperty(entityTplNodeRef, ContentModel.PROP_NAME)+")");
			nodeService.deleteNode(tplListNodeRef);
		}
	}

	/*/
	 * TODO faire en s'inspirant du synchronizeEntities
	@Override
	public void removeDataListItemsOnEntities(NodeRef entityTplNodeRef, List<NodeRef> dataListItems) {
		List<NodeRef> entities = getEntitiesToUpdate(entityTplNodeRef);
		
		
		
	}
	*/
}
