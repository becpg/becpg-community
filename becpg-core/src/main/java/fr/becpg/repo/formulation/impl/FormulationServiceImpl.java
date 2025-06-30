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
package fr.becpg.repo.formulation.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.audit.helper.StopWatchSupport;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.impl.FormulationAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationChain;
import fr.becpg.repo.formulation.FormulationPlugin;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.formulation.ReportableEntity;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.jscript.BeCPGStateHelper;
import fr.becpg.repo.jscript.BeCPGStateHelper.ActionStateContext;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * <p>
 * FormulationServiceImpl class.
 * </p>
 *
 * @author matthieu
 * @since 1.5
 * @version $Id: $Id
 */
public class FormulationServiceImpl<T extends FormulatedEntity> implements FormulationService<T>, FormulationPlugin {

	private static final String MESSAGE_FORMULATE_FAILURE_LOOP = "message.formulate.failure.loop";

	private static final String MESSAGE_FORMULATE_FAILURE = "message.formulate.failure";

	private AlfrescoRepository<T> alfrescoRepository;

	private NodeService nodeService;
	
	private final Map<Class<T>, Map<String, FormulationChain<T>>> formulationChains = new HashMap<>();

	private static final Log logger = LogFactory.getLog(FormulationServiceImpl.class);


	/**
	 * <p>
	 * Setter for the field <code>alfrescoRepository</code>.
	 * </p>
	 *
	 * @param alfrescoRepository
	 *            a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<T> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	private BeCPGAuditService beCPGAuditService;
	
	/**
	 * <p>Setter for the field <code>beCPGAuditService</code>.</p>
	 *
	 * @param beCPGAuditService a {@link fr.becpg.repo.audit.service.BeCPGAuditService} object
	 */
	public void setBeCPGAuditService(BeCPGAuditService beCPGAuditService) {
		this.beCPGAuditService = beCPGAuditService;
	}

	/** {@inheritDoc} */
	@Override
	public void registerFormulationChain(Class<T> clazz, FormulationChain<T> chain) {
		if (logger.isDebugEnabled()) {
			logger.debug("Register  chain for: " + clazz.getName());
		}
		
		Map<String, FormulationChain<T>> chains = formulationChains.get(clazz);
		if (chains == null) {
			chains = new HashMap<>();
		}

		if (chain.getChainId() != null) {
			chains.put(chain.getChainId(), chain);
		} else {
			chains.put(DEFAULT_CHAIN_ID, chain);
		}

		formulationChains.put(clazz, chains);
	}

	/** {@inheritDoc} */
	@Override
	public T formulate(NodeRef entityNodeRef)  {
		return formulate(entityNodeRef, DEFAULT_CHAIN_ID);
	}

	/** {@inheritDoc} */
	@Override
	public T formulate(T repositoryEntity)  {
		return formulate(repositoryEntity, DEFAULT_CHAIN_ID);
	}
	
	/** {@inheritDoc} */
	@Override
	public T formulate(T repositoryEntity, String chainId) {
		Locale currentLocal = I18NUtil.getLocale();
		Locale currentContentLocal = I18NUtil.getContentLocale();
		try (ActionStateContext state = BeCPGStateHelper.onFormulateEntity(repositoryEntity.getNodeRef())){
			I18NUtil.setLocale(Locale.getDefault());
			I18NUtil.setContentLocale(null);
			return formulateInternal(repositoryEntity, chainId);
		} finally {
			I18NUtil.setLocale(currentLocal);
			I18NUtil.setContentLocale(currentContentLocal);
		}
		
	}

	/** {@inheritDoc} */
	@Override
	public T formulate(NodeRef entityNodeRef, String chainId)  {
		Locale currentLocal = I18NUtil.getLocale();
		Locale currentContentLocal = I18NUtil.getContentLocale();
		try (ActionStateContext state = BeCPGStateHelper.onFormulateEntity(entityNodeRef)){
		
			return StopWatchSupport.build().scopeName(entityNodeRef.toString()).logger(logger).run(() -> {
				I18NUtil.setLocale(Locale.getDefault());
				I18NUtil.setContentLocale(null);
				
				T entity = alfrescoRepository.findOne(entityNodeRef);
				StopWatchSupport.addCheckpoint("findOne");
				
				entity = formulateInternal(entity, chainId);
				StopWatchSupport.addCheckpoint("formulateInternal");
				
				alfrescoRepository.save(entity);
				StopWatchSupport.addCheckpoint("save");
				
				return entity;
			});
			
			
		} finally {
			I18NUtil.setLocale(currentLocal);
			I18NUtil.setContentLocale(currentContentLocal);
		}
	}

	private T formulateInternal(T repositoryEntity, String chainId) {
		
		FormulationChain<T> chain = getChain(repositoryEntity.getClass(), chainId);

		try (AuditScope auditScope = beCPGAuditService.startAudit(AuditType.FORMULATION)) {

			auditScope.putAttribute(FormulationAuditPlugin.CHAIN_ID, chainId);
			auditScope.putAttribute(FormulationAuditPlugin.ENTITY_NAME, repositoryEntity.getName());
			auditScope.putAttribute(FormulationAuditPlugin.ENTITY_NODE_REF, repositoryEntity.getNodeRef());

			if ((chain == null) && (repositoryEntity.getClass().getSuperclass() != null)) {
				// look from superclass
				if (logger.isDebugEnabled()) {
					logger.debug("Look for superClass :" + repositoryEntity.getClass().getSuperclass().getName());
				}
				chain = getChain(repositoryEntity.getClass().getSuperclass(), chainId);

			}

			if (chain != null) {
				int i = 0;
				do {
					
					if (logger.isDebugEnabled()) {
						logger.debug("Execute formulation chain  - " + i + " for " + repositoryEntity.getName());
					}
					
					repositoryEntity.setCurrentReformulateCount(i);
					repositoryEntity.setFormulationChainId(chainId);
					chain.executeChain(repositoryEntity);
				} while ((repositoryEntity.getReformulateCount() != null) && (i++ < repositoryEntity.getReformulateCount()));
				if (chain.shouldUpdateFormulatedDate() && repositoryEntity.shouldUpdateFormulatedDate()) {
					repositoryEntity.setFormulatedDate(Calendar.getInstance().getTime());
				}

				// Warning only on integrity check for formulation
				IntegrityChecker.setWarnInTransaction();

			} else {
				logger.error("No formulation chain define for :" + repositoryEntity.getClass().getName());
			}

			// Warning only on integrity check for formulation
			IntegrityChecker.setWarnInTransaction();
			
			auditScope.addCheckpoint("formulateInternal");

		} catch (Throwable e) {

			if (RetryingTransactionHelper.extractRetryCause(e) != null) {
				if(logger.isDebugEnabled()) {
					logger.debug("Retrying the formulation due to exception "+e.getMessage());
				}
				throw e;
			}
			
			MLText message = null;
			
			if (e instanceof StackOverflowError) {
				message = MLTextHelper.getI18NMessage(MESSAGE_FORMULATE_FAILURE_LOOP, repositoryEntity.getName(), repositoryEntity.getNodeRef());
			} else {
				message = MLTextHelper.getI18NMessage(MESSAGE_FORMULATE_FAILURE, repositoryEntity != null ? repositoryEntity.getNodeRef() : null);
			}
			
			if (L2CacheSupport.isSilentModeEnable() && repositoryEntity instanceof ReportableEntity && chain != null) {
				((ReportableEntity) repositoryEntity).addError(message, chainId, Collections.emptyList());
				chain.onError(repositoryEntity);
			} else {
				if (e instanceof FormulateException) {
					throw e;
				}
				
				throw new FormulateException(message.getDefaultValue(), e);
			}
		}

		return repositoryEntity;
	}

	/** {@inheritDoc} */
	@Override
	public boolean shouldFormulate(NodeRef entityNodeRef) {

		if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_FORMULATED_ENTITY)) {

			Date modified = (Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIED);
			Date formulated = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_FORMULATED_DATE);

			if ((modified == null) || (formulated == null) || (modified.getTime() > formulated.getTime())) {
				return true;
			}
		}
		return false;
	}

	private FormulationChain<T> getChain(Class<?> clazz, String chainId) {
		Map<String, FormulationChain<T>> claims = formulationChains.get(clazz);
		if (claims != null) {
			if (claims.containsKey(chainId)) {
				return claims.get(chainId);
			}
			return claims.get(DEFAULT_CHAIN_ID);
		}

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public FormulationPluginPriority getMatchPriority(QName type) {
		return FormulationPluginPriority.NORMAL;
	}

	/** {@inheritDoc} */
	@Override
	public void runFormulation(NodeRef entityNodeRef, String chainId) {
		formulate(entityNodeRef, chainId);
	}

}

