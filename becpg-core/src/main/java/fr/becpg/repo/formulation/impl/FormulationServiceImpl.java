/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StopWatch;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationChain;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * 
 * @author matthieu
 * @since 1.5
 * @param <T>
 */
public class FormulationServiceImpl<T extends FormulatedEntity> implements FormulationService<T>{

	private static final String DEFAULT_CHAIN_ID = "default";

	AlfrescoRepository<T> alfrescoRepository;
	
	private final Map<Class<T>,Map<String,FormulationChain<T>> > formulationChains = new HashMap<>();

	private static final Log logger = LogFactory.getLog(FormulationServiceImpl.class);
	

	public void setAlfrescoRepository(AlfrescoRepository<T> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public void registerFormulationChain( Class<T> clazz, FormulationChain<T> chain){
		if(logger.isDebugEnabled()){
			logger.debug("Register  chain for: "+clazz.getName());
		}
		Map<String,FormulationChain<T>> chains = formulationChains.get(clazz);
		if(chains == null){
			chains = new HashMap<>();
		}
		
		if(chain.getChainId() != null){
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
		return formulate(repositoryEntity, DEFAULT_CHAIN_ID);
	}

	@Override
	public T formulate(NodeRef entityNodeRef, String chainId) throws FormulateException {
		T entity = alfrescoRepository.findOne(entityNodeRef);

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		entity = formulate(entity,chainId);
		
		if(logger.isDebugEnabled()){
			assert watch != null;
			watch.stop();
        	logger.debug("Formulate : "+this.getClass().getName()+" takes " + watch.getTotalTimeSeconds() + " seconds");
        	watch = new StopWatch();
			watch.start();
        }
		
		alfrescoRepository.save(entity);
		
		if(logger.isDebugEnabled()){
			assert watch != null;
			watch.stop();
        	logger.debug("Save : "+this.getClass().getName()+" takes " + watch.getTotalTimeSeconds() + " seconds");
        }

		return entity;
	}

	@Override
	public T formulate(T repositoryEntity, String chainId) throws FormulateException {
		try {
			
			FormulationChain<T> chain = getChain(repositoryEntity.getClass(), chainId);
		
			
			if(chain==null && repositoryEntity.getClass().getSuperclass()!=null){
				//look from superclass
				if(logger.isDebugEnabled()){
					logger.debug("Look for superClass :"+repositoryEntity.getClass().getSuperclass().getName());
				}
				chain = getChain(repositoryEntity.getClass().getSuperclass(),chainId);	
			}
			
			if(chain!=null){
				int i=0;
				do {
					if(logger.isDebugEnabled()){
						logger.debug("Execute formulation chain  - "+i+" for "+repositoryEntity.getName());
					}
					chain.executeChain(repositoryEntity);
				} while(repositoryEntity.getReformulateCount() != null && i++ < repositoryEntity.getReformulateCount()) ;
				if(chain.shouldUpdateFormulatedDate()){
					repositoryEntity.setFormulatedDate(Calendar.getInstance().getTime());
				}
				
				//Warning only on integrity check for formulation
				IntegrityChecker.setWarnInTransaction();
				
			} else {
				logger.error("No formulation chain define for :"+repositoryEntity.getClass().getName());
			}
		} catch (Exception e) {
			
			if(e instanceof FormulateException){
				throw (FormulateException)e;
			} 
			else if(e instanceof ConcurrencyFailureException){
				throw (ConcurrencyFailureException)e;
			} 
			throw new FormulateException(I18NUtil.getMessage("message.formulate.failure",repositoryEntity.getNodeRef()),e);
			
		}
		
		
		return repositoryEntity;
	}

	private FormulationChain<T> getChain(Class<?> clazz, String chainId) {
		Map<String, FormulationChain<T>> claims = formulationChains.get(clazz);
		if(claims!=null){
			return claims.get(chainId);
		}
		
		return null;
	}

}
