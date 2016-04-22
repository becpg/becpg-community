/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.migration.MigrationService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Service to migrate model and data
 * @author quere
 *
 */
public class MigrationServiceImpl implements MigrationService {
	
	private static final int BATCH_SIZE = 50;
	
	private static final Log logger = LogFactory.getLog(MigrationServiceImpl.class);
	
	private NodeService nodeService;
	
	private TenantAdminService tenantAdminService;
	
	private TransactionService transactionService;
	
	private DictionaryService dictionaryService;
		
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	@Override
	@Deprecated //Use patch instead
	public void addMandatoryAspectInMt(final QName type, final QName aspect) {
		PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);

		AuthenticationUtil.runAs(new RunAsWork<Object>() {
			public Object doWork() throws Exception {

				if (tenantAdminService.isEnabled()) {
					for (final Tenant tenant : tenantAdminService.getAllTenants()) {
						AuthenticationUtil.runAs(new RunAsWork<Object>() {
							public Object doWork() throws Exception {
								logger.info("addMandatoryAspectInMt for tenant: "+tenant.getTenantDomain());
								addMandatoryAspect(type, aspect);

								return null;
							}

						}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
					}

				} else {
				  logger.info("addMandatoryAspectInMt in non-tenant environment");
				  addMandatoryAspect(type, aspect);
				}
				return null;
			}
		}, AuthenticationUtil.getSystemUserName());
	}

	//TODO Use BatchProcessWorkProvider to do batching
	
	public void addMandatoryAspect(QName type, final QName aspect) {
		
		
		List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().ofType(type).excludeAspect(aspect).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();

		logger.info("Found " + nodeRefs.size() + " node of type " + type + " without mandatory aspect " + aspect);

		if (!nodeRefs.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(nodeRefs, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(
						new RetryingTransactionCallback<Boolean>() {
							public Boolean execute() throws Exception {

								for (NodeRef nodeRef : batchList) {
									if (nodeService.exists(nodeRef)) {
										if (!nodeService.hasAspect(nodeRef, aspect)) {
											nodeService.addAspect(nodeRef, aspect, null);
											//look for other mandatory aspects
											TypeDefinition typeDef = dictionaryService.getType(nodeService.getType(nodeRef));
											for(QName defaultAspect : typeDef.getDefaultAspectNames()){
												if (!nodeService.hasAspect(nodeRef, defaultAspect)) {
													logger.debug("Add other default aspect " + defaultAspect + " for node " + nodeRef);
													nodeService.addAspect(nodeRef, defaultAspect, null);
												}
											}
										}
									}
								}
								return true;
							}
						}, false, true);
			}
		}
	}
	
	@Override
	public void removeAspectInMt(final QName type, final QName aspect) {
		PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);

		AuthenticationUtil.runAs(new RunAsWork<Object>() {
			public Object doWork() throws Exception {

				if (tenantAdminService.isEnabled()) {
					for (final Tenant tenant : tenantAdminService.getAllTenants()) {
						AuthenticationUtil.runAs(new RunAsWork<Object>() {
							public Object doWork() throws Exception {
								logger.info("removeAspectInMt for tenant: "+tenant.getTenantDomain());
								removeAspect(type, aspect);

								return null;
							}

						}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
					}

				} else {
				  logger.info("removeAspectInMt in non-tenant environment");
				  removeAspect(type, aspect);
				}
				return null;
			}
		}, AuthenticationUtil.getSystemUserName());
	}

	private void removeAspect(QName type, final QName aspect) {
		
		List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().ofType(type).withAspect(aspect).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();

		logger.info("Found " + nodeRefs.size() + " node of type " + type + " with aspect " + aspect + ". Start remove aspects");

		if (!nodeRefs.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(nodeRefs, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(
						new RetryingTransactionCallback<Boolean>() {
							public Boolean execute() throws Exception {

								for (NodeRef nodeRef : batchList) {
									if (nodeService.exists(nodeRef)) {
										if (nodeService.hasAspect(nodeRef, aspect)) {
											nodeService.removeAspect(nodeRef, aspect);											
										}
									}
								}
								return true;
							}
						}, false, true);
			}
		}
	}
	
	private BeCPGQueryBuilder getLuceneQueryforClass(QName classQName){
		ClassDefinition classDef = dictionaryService.getClass(classQName);
		
		if(classDef.isAspect()){
			return BeCPGQueryBuilder.createQuery().withAspect(classQName);
		}
		else{
			return BeCPGQueryBuilder.createQuery().ofType(classQName);
		}
	}
	
	@Override
	public void migrateAssociationInMt(final QName classQName, final QName sourceAssoc, final QName targetAssoc) {
		PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);

		AuthenticationUtil.runAs(new RunAsWork<Object>() {
			public Object doWork() throws Exception {

				if (tenantAdminService.isEnabled()) {
					for (final Tenant tenant : tenantAdminService.getAllTenants()) {
						AuthenticationUtil.runAs(new RunAsWork<Object>() {
							public Object doWork() throws Exception {
								logger.info("migrateAssociationInMt for tenant: "+tenant.getTenantDomain());
								migrateAssociation(classQName, sourceAssoc, targetAssoc);

								return null;
							}

						}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
					}

				} else {
				  logger.info("migrateAssociationInMt in non-tenant environment");
				  migrateAssociation(classQName, sourceAssoc, targetAssoc);
				}
				return null;
			}
		}, AuthenticationUtil.getSystemUserName());
	}

	@Override
	public void migrateAssociation(QName classQName, final QName sourceAssoc, final QName targetAssoc) {

		List<NodeRef> nodeRefs = getLuceneQueryforClass(classQName).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();

		logger.info("Found " + nodeRefs.size() + " nodes. migrate association " + sourceAssoc + " in " + targetAssoc);

		if (!nodeRefs.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(nodeRefs, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(
						new RetryingTransactionCallback<Boolean>() {
							public Boolean execute() throws Exception {

								for (NodeRef nodeRef : batchList) {
									if (nodeService.exists(nodeRef)) {
										logger.debug("migrate association of node " + nodeRef);
										List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, sourceAssoc);
										for(AssociationRef assocRef : assocRefs){
											nodeService.createAssociation(nodeRef, assocRef.getTargetRef(), targetAssoc);
											nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), sourceAssoc);
										}
									}
								}
								return true;
							}
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
		AuthenticationUtil.runAs(new RunAsWork<Object>() {
			public Object doWork() throws Exception {

				if (tenantAdminService.isEnabled()) {
					for (final Tenant tenant : tenantAdminService.getAllTenants()) {
						AuthenticationUtil.runAs(new RunAsWork<Object>() {
							public Object doWork() throws Exception {
								logger.info("migratePropertyInMt for tenant: "+tenant.getTenantDomain());
								migrateProperty(classQName, sourceProp, targetProp);

								return null;
							}

						}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
					}

				} else {
				  logger.info("migratePropertyInMt in non-tenant environment");
				  migrateProperty(classQName, sourceProp, targetProp);
				}
				return null;
			}
		}, AuthenticationUtil.getSystemUserName());

	}

	private void migrateProperty(QName classQName, final QName sourceProp, final QName targetProp) {

		List<NodeRef> nodeRefs = getLuceneQueryforClass(classQName).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();

		logger.info("Found " + nodeRefs.size() + " nodes. migrate property " + sourceProp + " in " + targetProp);

		if (!nodeRefs.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(nodeRefs, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(
						new RetryingTransactionCallback<Boolean>() {
							public Boolean execute() throws Exception {

								for (NodeRef nodeRef : batchList) {
									if (nodeService.exists(nodeRef)) {
										logger.debug("migrate property of node " + nodeRef);										
										nodeService.setProperty(nodeRef, targetProp, nodeService.getProperty(nodeRef, sourceProp));
										nodeService.removeProperty(nodeRef, sourceProp);
									}
								}
								return true;
							}
						}, false, true);
			}
		}
	}

}
