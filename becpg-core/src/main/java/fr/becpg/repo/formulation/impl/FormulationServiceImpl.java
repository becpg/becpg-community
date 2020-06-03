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
package fr.becpg.repo.formulation.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationChain;
import fr.becpg.repo.formulation.FormulationPlugin;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 *
 * @author matthieu
 * @since 1.5
 * @param <T>
 */
public class FormulationServiceImpl<T extends FormulatedEntity> implements FormulationService<T>, FormulationPlugin {

	

	private AlfrescoRepository<T> alfrescoRepository;

	private NodeService nodeService;

	private final Map<Class<T>, Map<String, FormulationChain<T>>> formulationChains = new HashMap<>();

	private static final Log logger = LogFactory.getLog(FormulationServiceImpl.class);

	public void setAlfrescoRepository(AlfrescoRepository<T> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

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

	@Override
	public T formulate(NodeRef entityNodeRef) throws FormulateException {
		return formulate(entityNodeRef, DEFAULT_CHAIN_ID);
	}

	@Override
	public T formulate(T repositoryEntity) throws FormulateException {
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

	@Override
	public T formulate(NodeRef entityNodeRef, String chainId) throws FormulateException {
		Locale currentLocal = I18NUtil.getLocale();
		try {
			I18NUtil.setLocale(Locale.getDefault());
			T entity = alfrescoRepository.findOne(entityNodeRef);

			StopWatch watch = null;
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}

			entity = formulate(entity, chainId);

			if (logger.isDebugEnabled() && watch!=null) {
				watch.stop();
				logger.debug("Formulate : " + this.getClass().getName() + " takes " + watch.getTotalTimeSeconds() + " seconds");
				watch = new StopWatch();
				watch.start();
			}

			alfrescoRepository.save(entity);

			if (logger.isDebugEnabled() && watch!=null) {
				watch.stop();
				logger.debug("Save : " + this.getClass().getName() + " takes " + watch.getTotalTimeSeconds() + " seconds");
			}

			return entity;
		} finally {
			I18NUtil.setLocale(currentLocal);
		}
	}

	@Override
	public T formulate(T repositoryEntity, String chainId) throws FormulateException {
		try {

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
		} catch (Exception e) {

			if (e instanceof FormulateException) {
				logger.error(e,e);
				throw (FormulateException) e;
			} else if (e instanceof ConcurrencyFailureException) {
				throw (ConcurrencyFailureException) e;
			}
			logger.error(e,e);
			throw new FormulateException(I18NUtil.getMessage("message.formulate.failure", repositoryEntity!=null ? repositoryEntity.getNodeRef() : null), e);

		} catch (StackOverflowError e) {
			logger.error(e,e);
			throw new FormulateException(I18NUtil.getMessage("message.formulate.failure.loop"), e);
			
		}

		return repositoryEntity;
	}

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
			if(claims.containsKey(chainId)) {
				return claims.get(chainId);
			} 
			return claims.get(DEFAULT_CHAIN_ID);
		}

		return null;
	}

	@Override
	public FormulationPluginPriority getMatchPriority(QName type) {
		return FormulationPluginPriority.NORMAL;
	}

	@Override
	public void runFormulation(NodeRef entityNodeRef) throws FormulateException {
		formulate(entityNodeRef);
	}


}
