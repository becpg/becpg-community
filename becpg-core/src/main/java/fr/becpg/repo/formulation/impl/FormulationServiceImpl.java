package fr.becpg.repo.formulation.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationChain;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * 
 * @author matthieu
 * @since 1.5
 * @param <T>
 */
@Service
public class FormulationServiceImpl<T extends RepositoryEntity> implements FormulationService<T>{

	private static final String DEFAULT_CHAIN_ID = "default";

	AlfrescoRepository<T> alfrescoRepository;
	
	private Map<Class<T>,Map<String,FormulationChain<T>> > formulationChains = new HashMap<>();

	private static Log logger = LogFactory.getLog(FormulationServiceImpl.class);
	

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
        	watch.stop();
        	logger.debug("Formulate : "+this.getClass().getName()+" takes " + watch.getTotalTimeSeconds() + " seconds");
        	watch = new StopWatch();
			watch.start();
        }

		alfrescoRepository.save(entity);
		
		if(logger.isDebugEnabled()){
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
				chain.executeChain(repositoryEntity);
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
