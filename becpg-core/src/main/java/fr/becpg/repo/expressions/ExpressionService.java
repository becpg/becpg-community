package fr.becpg.repo.expressions;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * 
 * @author matthieu
 *
 */
public interface ExpressionService<T extends RepositoryEntity> {

	Object eval(String condition, List<NodeRef> nodeRefs);

	Object eval(String condition, T formulatedEntity);


}
