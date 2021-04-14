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
package fr.becpg.repo.formulation.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationChain;
import fr.becpg.repo.formulation.FormulationPlugin;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.repository.AlfrescoRepository;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

/**
 * <p>
 * FormulationServiceImpl class.
 * </p>
 *
 * @author matthieu
 * @since 1.5
 * @param <T>
 * @version $Id: $Id
 */
public class FormulationServiceImpl<T extends FormulatedEntity> implements FormulationService<T>, FormulationPlugin {

	private AlfrescoRepository<T> alfrescoRepository;

	private NodeService nodeService;

	private final Map<Class<T>, Map<String, FormulationChain<T>>> formulationChains = new HashMap<>();

	private static final Log logger = LogFactory.getLog(FormulationServiceImpl.class);

	private static final Tracer tracer = Tracing.getTracer();

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
		Locale currentLocal = I18NUtil.getLocale();
		Locale currentContentLocal = I18NUtil.getContentLocale();
		try {
			I18NUtil.setLocale(Locale.getDefault());
			I18NUtil.setContentLocale(null);
			return formulate(repositoryEntity, DEFAULT_CHAIN_ID);
		} finally {
			I18NUtil.setLocale(currentLocal);
			I18NUtil.setContentLocale(currentContentLocal);
		}
	}

	/** {@inheritDoc} */
	@Override
	public T formulate(NodeRef entityNodeRef, String chainId)  {
		Locale currentLocal = I18NUtil.getLocale();
		try(Scope scope = tracer.spanBuilder("formulateEntity").startScopedSpan()) {
			tracer.getCurrentSpan().addAnnotation("findOne");
			
			I18NUtil.setLocale(Locale.getDefault());
			T entity = alfrescoRepository.findOne(entityNodeRef);

			StopWatch watch = null;
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}

			entity = formulate(entity, chainId);

			if (logger.isDebugEnabled() && (watch != null)) {
				watch.stop();
				logger.debug("Formulate : " + this.getClass().getName() + " takes " + watch.getTotalTimeSeconds() + " seconds");
				watch = new StopWatch();
				watch.start();
			}

			tracer.getCurrentSpan().addAnnotation("save");
			alfrescoRepository.save(entity);

			if (logger.isDebugEnabled() && (watch != null)) {
				watch.stop();
				logger.debug("Save : " + this.getClass().getName() + " takes " + watch.getTotalTimeSeconds() + " seconds");
			}

			return entity;
		} finally {
			I18NUtil.setLocale(currentLocal);
		}
	}

	/** {@inheritDoc} */
	@Override
	public T formulate(T repositoryEntity, String chainId) {
		try(Scope scope = tracer.spanBuilder("formulate").startScopedSpan()) {

			FormulationChain<T> chain = getChain(repositoryEntity.getClass(), chainId);

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
		} catch (Throwable e) {

			 if (RetryingTransactionHelper.extractRetryCause(e) != null) {
				 throw e;
              }

			if (e instanceof FormulateException) {
				throw (FormulateException) e;
			}

			if (e instanceof StackOverflowError) {
				throw new FormulateException(
						I18NUtil.getMessage("message.formulate.failure.loop", repositoryEntity.getName(), repositoryEntity.getNodeRef()), e);
			}

			throw new FormulateException(
					I18NUtil.getMessage("message.formulate.failure", repositoryEntity != null ? repositoryEntity.getNodeRef() : null), e);

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
	public void runFormulation(NodeRef entityNodeRef) {
		formulate(entityNodeRef);
	}

}
