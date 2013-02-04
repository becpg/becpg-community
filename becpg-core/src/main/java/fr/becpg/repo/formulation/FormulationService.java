package fr.becpg.repo.formulation;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.RepositoryEntity;


public interface FormulationService<T extends RepositoryEntity> {

    public T formulate(NodeRef entityNodeRef) throws FormulateException;
    
    public T formulate(T repositoryEntity) throws FormulateException;

	void registerFormulationChain(Class<T> clazz, FormulationChain<T> chain);
	
}
