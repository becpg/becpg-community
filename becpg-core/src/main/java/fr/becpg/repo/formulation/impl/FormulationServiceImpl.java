package fr.becpg.repo.formulation.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;

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
public class FormulationServiceImpl<T extends RepositoryEntity> implements FormulationService<T>{

	AlfrescoRepository<T> alfrescoRepository;
	
	private Map<Class<T>,FormulationChain<T> > formulationChains = new HashMap<Class<T>,FormulationChain<T>>();

	private static Log logger = LogFactory.getLog(FormulationServiceImpl.class);
	

	public void setAlfrescoRepository(AlfrescoRepository<T> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public void registerFormulationChain(Class<T> clazz, FormulationChain<T> chain){
		if(logger.isDebugEnabled()){
			logger.debug("Register  chain for: "+clazz.getName());
		}
		
		formulationChains.put(clazz, chain);
	}
	
	@Override
	public void formulate(NodeRef entityNodeRef) throws FormulateException {
		 T entity = alfrescoRepository.findOne(entityNodeRef);
		
		 entity =  formulate(entity);
		 
		 alfrescoRepository.save(entity);
		 
	}

	@Override
	public T formulate(T repositoryEntity) throws FormulateException {
		
		try {
			
			FormulationChain<T> chain = formulationChains.get(repositoryEntity.getClass());
			
			if(chain==null && repositoryEntity.getClass().getSuperclass()!=null){
				//look from superclass
				if(logger.isDebugEnabled()){
					logger.debug("Look for superClass :"+repositoryEntity.getClass().getSuperclass().getName());
				}
				chain = formulationChains.get(repositoryEntity.getClass().getSuperclass());	
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
			throw new FormulateException(I18NUtil.getMessage("message.formulate.failure"),e);
			
		}
		
		
		return repositoryEntity;
	}

}
