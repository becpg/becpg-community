package fr.becpg.repo.expressions;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>ExpressionService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ExpressionService {

	/**
	 * <p>eval.</p>
	 *
	 * @param condition a {@link java.lang.String} object
	 * @param nodeRefs a {@link java.util.List} object
	 * @return a {@link java.lang.Object} object
	 */
	Object eval(String condition, List<NodeRef> nodeRefs);

	/**
	 * <p>eval.</p>
	 *
	 * @param condition a {@link java.lang.String} object
	 * @param formulatedEntity a T object
	 * @param <T> a T class
	 * @return a {@link java.lang.Object} object
	 */
	<T extends RepositoryEntity> Object eval(String condition, T formulatedEntity);

	/**
	 * <p>extractExpr.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param exprFormat a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	String extractExpr(NodeRef nodeRef, String exprFormat);

	/**
	 * <p>extractExpr.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param docNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param exprFormat a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	String extractExpr(NodeRef nodeRef, NodeRef docNodeRef, String exprFormat);
	
	/**
	 * <p>extractExpr.</p>
	 *
	 * @param object a {@link org.json.JSONObject} object
	 * @param exprFormat a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	String extractExpr(JSONObject object, String exprFormat);


}
