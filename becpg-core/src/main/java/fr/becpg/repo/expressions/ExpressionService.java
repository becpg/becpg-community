package fr.becpg.repo.expressions;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * 
 * @author matthieu
 *
 */
public interface ExpressionService {

	Object eval(String condition, List<NodeRef> nodeRefs);

	<T extends RepositoryEntity> Object eval(String condition, T formulatedEntity);

	String extractExpr(NodeRef nodeRef, String exprFormat);

	String extractExpr(NodeRef nodeRef, NodeRef docNodeRef, String exprFormat);
	
	String extractExpr(JSONObject object, String exprFormat);


}
