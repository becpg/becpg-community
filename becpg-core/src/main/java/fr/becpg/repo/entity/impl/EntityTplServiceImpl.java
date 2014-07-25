/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.L2CacheSupport.Action;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.Synchronisable;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("entityTplService")
public class EntityTplServiceImpl implements EntityTplService {

	private static Log logger = LogFactory.getLog(EntityTplServiceImpl.class);

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

	/**
	 * Create the entityTpl
	 * 
	 * @param entityTplsNodeRef
	 * @param entityType
	 */
	@Override
	public NodeRef createEntityTpl(NodeRef parentNodeRef, QName entityType, boolean enabled, Set<QName> entityLists, Set<String> subFolders) {

		TypeDefinition typeDef = dictionaryService.getType(entityType);
		String entityTplName = typeDef.getTitle(dictionaryService);

		// entityTpl
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, entityTplName);
		properties.put(BeCPGModel.PROP_ENTITY_TPL_ENABLED, enabled);
		properties.put(BeCPGModel.PROP_ENTITY_TPL_IS_DEFAULT, true);

		NodeRef entityTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, entityTplName);
		if (entityTplNodeRef == null) {
			entityTplNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, entityType.getLocalName()), entityType, properties).getChildRef();
		}

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

			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, name);
			properties.put(ContentModel.PROP_TITLE, I18NUtil.getMessage("entity-datalist-wused-title"));
			properties.put(ContentModel.PROP_DESCRIPTION, I18NUtil.getMessage("entity-datalist-wused-description"));
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

		return BeCPGQueryBuilder.createQuery().ofExactType(nodeType).withAspect(BeCPGModel.ASPECT_ENTITY_TPL)
				.andPropEquals(BeCPGModel.PROP_ENTITY_TPL_ENABLED, Boolean.TRUE.toString())
				.andPropEquals(BeCPGModel.PROP_ENTITY_TPL_IS_DEFAULT, Boolean.TRUE.toString()).excludeVersions().singleValue();
	}

	@Override
	public void synchronizeEntities(NodeRef tplNodeRef) {

		logger.debug("synchronizeEntities");
		RepositoryEntity entityTpl = alfrescoRepository.findOne(tplNodeRef);

		logger.debug("entityTpl" + entityTpl.toString());

		final Map<QName, List<? extends RepositoryEntity>> datalistsTpl = repositoryEntityDefReader.getDataLists(entityTpl);

		if (datalistsTpl != null && !datalistsTpl.isEmpty()) {

			List<NodeRef> entityNodeRefs = getEntitiesToUpdate(tplNodeRef);
			logger.debug("synchronize entityNodeRefs, size " + entityNodeRefs.size());

			doInBatch(entityNodeRefs, 10, new BatchCallBack() {

				public void run(NodeRef entityNodeRef) {
					RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
					Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);
					NodeRef listContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(entity);

					for (QName dataListQName : datalistsTpl.keySet()) {

						@SuppressWarnings("unchecked")
						List<BeCPGDataObject> dataListItems = (List<BeCPGDataObject>) datalists.get(dataListQName);

						boolean update = false;

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
						if (update) {
							alfrescoRepository.saveDataList(listContainerNodeRef, dataListQName, dataListQName, dataListItems);
						}
					}
				}
			});

		}
	}

	@Override
	public void formulateEntities(NodeRef tplNodeRef) throws FormulateException {

		List<NodeRef> entityNodeRefs = getEntitiesToUpdate(tplNodeRef);

		doInBatch(entityNodeRefs, 5, new BatchCallBack() {

			public void run(NodeRef entityNodeRef) {
				try {
					if(logger.isDebugEnabled()){
						logger.debug("Formulate : "+entityNodeRef);
					}
					formulationService.formulate(entityNodeRef);
				} catch (FormulateException e) {
					logger.error(e, e);
				}

			}

		});

	}

	private interface BatchCallBack {
		public void run(NodeRef entityNodeRef);
	}

	private void doInBatch(final List<NodeRef> entityNodeRefs, final int batchSize, final BatchCallBack batchCallBack) {

		StopWatch watch = null;
		if (logger.isInfoEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		L2CacheSupport.doInCacheContext(new Action() {

			public void run() {
				for (final List<NodeRef> subList : Lists.partition(entityNodeRefs, batchSize)) {
					RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
						@Override
						public Object doWork() throws Exception {
							RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>() {
								@Override
								public Object execute() {
									for (final NodeRef entityNodeRef : subList) {
										try {

											policyBehaviourFilter.disableBehaviour(entityNodeRef, ReportModel.ASPECT_REPORT_ENTITY);
											policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
											policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_VERSIONABLE);

											batchCallBack.run(entityNodeRef);

										} finally {
											policyBehaviourFilter.enableBehaviour(entityNodeRef, ReportModel.ASPECT_REPORT_ENTITY);
											policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
											policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_VERSIONABLE);
										}
									}
									return null;

								}
							};
							return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
						}
					};
					AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getSystemUserName());

				}

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
}
