package fr.becpg.repo.entity.remote.extractor;

import java.io.OutputStream;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.entity.remote.RemoteParams;

/**
 * <p>RemoteEntityVisitor interface.</p>
 *
 * @author matthieu
 */
public interface RemoteEntityVisitor {

	/**
	 * <p>setParams.</p>
	 *
	 * @param params a {@link fr.becpg.repo.entity.remote.RemoteParams} object
	 */
	public void setParams(RemoteParams params);

	/**
	 * <p>visit.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @throws java.lang.Exception if any.
	 */
	public void visit(NodeRef entityNodeRef, OutputStream result) throws Exception;

	/**
	 * <p>visit.</p>
	 *
	 * @param entities a {@link java.util.List} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @throws java.lang.Exception if any.
	 */
	public void visit(PagingResults<NodeRef> entities, OutputStream result) throws Exception;

	/**
	 * <p>visitData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @throws java.lang.Exception if any.
	 */
	public void visitData(NodeRef entityNodeRef, OutputStream result) throws Exception;

}
