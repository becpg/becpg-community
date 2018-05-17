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
package fr.becpg.repo.migration.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.migration.MigrationService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Service to migrate model and data
 *
 * @author quere
 *
 */
@Service("migrationService")
public class MigrationServiceImpl implements MigrationService {

	private static final int BATCH_SIZE = 50;

	private static final Log logger = LogFactory.getLog(MigrationServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private TenantAdminService tenantAdminService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	@Qualifier("mtAwareNodeService")
	private NodeService dbNodeService;

	@Override
	@Deprecated // Use patch instead
	public void addMandatoryAspectInMt(final QName type, final QName aspect) {
		PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);

		AuthenticationUtil.runAs(() -> {

			if (tenantAdminService.isEnabled()) {
				for (final Tenant tenant : tenantAdminService.getAllTenants()) {
					AuthenticationUtil.runAs(() -> {
						logger.info("addMandatoryAspectInMt for tenant: " + tenant.getTenantDomain());
						addMandatoryAspect(type, aspect);

						return null;
					}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
				}

			} else {
				logger.info("addMandatoryAspectInMt in non-tenant environment");
				addMandatoryAspect(type, aspect);
			}
			return null;
		}, AuthenticationUtil.getSystemUserName());
	}

	// TODO Use BatchProcessWorkProvider to do batching

	@Override
	public void addMandatoryAspect(QName type, final QName aspect) {

		List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().ofType(type).excludeAspect(aspect).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
				.list();

		logger.info("Found " + nodeRefs.size() + " node of type " + type + " without mandatory aspect " + aspect);

		if (!nodeRefs.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(nodeRefs, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

					for (NodeRef nodeRef : batchList) {
						if (nodeService.exists(nodeRef)) {
							if (!nodeService.hasAspect(nodeRef, aspect)) {
								nodeService.addAspect(nodeRef, aspect, null);
								// look for other mandatory aspects
								TypeDefinition typeDef = dictionaryService.getType(nodeService.getType(nodeRef));
								for (QName defaultAspect : typeDef.getDefaultAspectNames()) {
									if (!nodeService.hasAspect(nodeRef, defaultAspect)) {
										logger.debug("Add other default aspect " + defaultAspect + " for node " + nodeRef);
										nodeService.addAspect(nodeRef, defaultAspect, null);
									}
								}
							}
						}
					}
					return true;
				}, false, true);
			}
		}
	}

	@Override
	public void removeAspectInMt(final QName type, final QName aspect) {
		PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);

		AuthenticationUtil.runAs(() -> {

			if (tenantAdminService.isEnabled()) {
				for (final Tenant tenant : tenantAdminService.getAllTenants()) {
					AuthenticationUtil.runAs(() -> {
						logger.info("removeAspectInMt for tenant: " + tenant.getTenantDomain());
						removeAspect(type, aspect);

						return null;
					}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
				}

			} else {
				logger.info("removeAspectInMt in non-tenant environment");
				removeAspect(type, aspect);
			}
			return null;
		}, AuthenticationUtil.getSystemUserName());
	}

	private void removeAspect(QName type, final QName aspect) {

		List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().ofType(type).withAspect(aspect).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();

		logger.info("Found " + nodeRefs.size() + " node of type " + type + " with aspect " + aspect + ". Start remove aspects");

		if (!nodeRefs.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(nodeRefs, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

					for (NodeRef nodeRef : batchList) {
						if (nodeService.exists(nodeRef)) {
							if (nodeService.hasAspect(nodeRef, aspect)) {
								nodeService.removeAspect(nodeRef, aspect);
							}
						}
					}
					return true;
				}, false, true);
			}
		}
	}

	private BeCPGQueryBuilder getLuceneQueryforClass(QName classQName) {
		ClassDefinition classDef = dictionaryService.getClass(classQName);

		if (classDef.isAspect()) {
			return BeCPGQueryBuilder.createQuery().withAspect(classQName);
		} else {
			return BeCPGQueryBuilder.createQuery().ofType(classQName);
		}
	}

	@Override
	public void migrateAssociationInMt(final QName classQName, final QName sourceAssoc, final QName targetAssoc) {
		PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);

		AuthenticationUtil.runAs(() -> {

			if (tenantAdminService.isEnabled()) {
				for (final Tenant tenant : tenantAdminService.getAllTenants()) {
					AuthenticationUtil.runAs(() -> {
						logger.info("migrateAssociationInMt for tenant: " + tenant.getTenantDomain());
						migrateAssociation(classQName, sourceAssoc, targetAssoc);

						return null;
					}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
				}

			} else {
				logger.info("migrateAssociationInMt in non-tenant environment");
				migrateAssociation(classQName, sourceAssoc, targetAssoc);
			}
			return null;
		}, AuthenticationUtil.getSystemUserName());
	}

	@Override
	public void migrateAssociation(QName classQName, final QName sourceAssoc, final QName targetAssoc) {

		List<NodeRef> nodeRefs = getLuceneQueryforClass(classQName).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();

		logger.info("Found " + nodeRefs.size() + " nodes. migrate association " + sourceAssoc + " in " + targetAssoc);

		if (!nodeRefs.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(nodeRefs, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

					for (NodeRef nodeRef : batchList) {
						if (nodeService.exists(nodeRef)) {
							logger.debug("migrate association of node " + nodeRef);
							List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, sourceAssoc);
							for (AssociationRef assocRef : assocRefs) {
								nodeService.createAssociation(nodeRef, assocRef.getTargetRef(), targetAssoc);
								nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), sourceAssoc);
							}
						}
					}
					return true;
				}, false, true);
			}
		}
	}

	@Override
	public void migratePropertyInMt(final QName classQName, final QName sourceProp, final QName targetProp) {
		PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);
		/*
		 * Ensure transactionality and the correct authentication
		 */
		AuthenticationUtil.runAs(() -> {

			if (tenantAdminService.isEnabled()) {
				for (final Tenant tenant : tenantAdminService.getAllTenants()) {
					AuthenticationUtil.runAs(() -> {
						logger.info("migratePropertyInMt for tenant: " + tenant.getTenantDomain());
						migrateProperty(classQName, sourceProp, targetProp);

						return null;
					}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
				}

			} else {
				logger.info("migratePropertyInMt in non-tenant environment");
				migrateProperty(classQName, sourceProp, targetProp);
			}
			return null;
		}, AuthenticationUtil.getSystemUserName());

	}

	private void migrateProperty(QName classQName, final QName sourceProp, final QName targetProp) {

		List<NodeRef> nodeRefs = getLuceneQueryforClass(classQName).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();

		logger.info("Found " + nodeRefs.size() + " nodes. migrate property " + sourceProp + " in " + targetProp);

		if (!nodeRefs.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(nodeRefs, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

					for (NodeRef nodeRef : batchList) {
						if (nodeService.exists(nodeRef)) {
							logger.debug("migrate property of node " + nodeRef);
							nodeService.setProperty(nodeRef, targetProp, nodeService.getProperty(nodeRef, sourceProp));
							nodeService.removeProperty(nodeRef, sourceProp);
						}
					}
					return true;
				}, false, true);
			}
		}
	}

	@Override
	public void cleanOrphanVersion() {

		NodeRef rootNode = dbNodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID));

		List<ChildAssociationRef> childAssocs = dbNodeService.getChildAssocs(rootNode, Version2Model.CHILD_QNAME_VERSION_HISTORIES,
				RegexQNamePattern.MATCH_ALL);

		for (ChildAssociationRef childAssoc : childAssocs) {
			String name = (String) dbNodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME);
			NodeRef nodeToTest = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore", name);

			if (!dbNodeService.exists(nodeToTest)) {
				List<ChildAssociationRef> versionAssocs = dbNodeService.getChildAssocs(childAssoc.getChildRef(), Version2Model.CHILD_QNAME_VERSIONS,
						RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef versionAssoc : versionAssocs) {
					if (dictionaryService.isSubClass(nodeService.getType(versionAssoc.getChildRef()), BeCPGModel.TYPE_ENTITY_V2)) {
						logger.info("version  doesn't exist :" + nodeService.getProperty(versionAssoc.getChildRef(), BeCPGModel.PROP_CODE) + " "
								+ nodeToTest + " for :" + name);
							nodeService.deleteNode(childAssoc.getChildRef());
					}
					break;
				}

			} else if(nodeService.hasAspect(nodeToTest, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
				logger.info("Removing unneeded version for Composite version : "+nodeToTest);
				nodeService.deleteNode(childAssoc.getChildRef());
			} 

		}

	}
//
//	private Version getVersion(NodeRef versionRef) {
//		if (versionRef == null) {
//			return null;
//		}
//		Map<String, Serializable> versionProperties = new HashMap<>();
//
//		// Get the standard node details and get the meta data
//		Map<QName, Serializable> nodeProperties = dbNodeService.getProperties(versionRef);
//
//		if (logger.isTraceEnabled()) {
//			logger.trace("getVersion: " + versionRef + " nodeProperties=\n" + nodeProperties.keySet());
//		}
//
//		// TODO consolidate with VersionUtil.convertFrozenToOriginalProps
//
//		for (QName key : nodeProperties.keySet()) {
//			Serializable value = nodeProperties.get(key);
//
//			String keyName = key.getLocalName();
//			int idx = keyName.indexOf(Version2Model.PROP_METADATA_PREFIX);
//			if (idx == 0) {
//				// versioned metadata property - additional (optional) metadata,
//				// set during versioning
//				versionProperties.put(keyName.substring(Version2Model.PROP_METADATA_PREFIX.length()), value);
//			} else {
//				if (key.equals(Version2Model.PROP_QNAME_VERSION_DESCRIPTION)) {
//					versionProperties.put(Version.PROP_DESCRIPTION, value);
//				} else if (key.equals(Version2Model.PROP_QNAME_VERSION_LABEL)) {
//					versionProperties.put(VersionBaseModel.PROP_VERSION_LABEL, value);
//				} else {
//					if (keyName.equals(Version.PROP_DESCRIPTION) || keyName.equals(VersionBaseModel.PROP_VERSION_LABEL)) {
//						// ignore reserved localname (including cm:description,
//						// cm:versionLabel)
//					} else {
//						// all other properties
//						versionProperties.put(keyName, value);
//					}
//				}
//			}
//		}
//
//		// Create and return the version object
//		NodeRef newNodeRef = new NodeRef(new StoreRef(VersionBaseModel.STORE_PROTOCOL, Version2Model.STORE_ID), versionRef.getId());
//		Version result = new VersionImpl(versionProperties, newNodeRef);
//
//		if (logger.isTraceEnabled()) {
//			logger.trace("getVersion: " + versionRef + " versionProperties=\n" + versionProperties.keySet());
//		}
//
//		// done
//		return result;
//	}

}
