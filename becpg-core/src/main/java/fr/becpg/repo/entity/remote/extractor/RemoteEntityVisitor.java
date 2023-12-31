package fr.becpg.repo.entity.remote.extractor;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.entity.remote.RemoteParams;

public interface RemoteEntityVisitor {


	public void setParams(RemoteParams params);
	
	
	/**
	 * <p>visit.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @throws java.lang.Exception if any.
	 */
	public  void visit(NodeRef entityNodeRef, OutputStream result) throws Exception ;

	/**
	 * <p>visit.</p>
	 *
	 * @param entities a {@link java.util.List} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @throws Exception 
	 * @throws java.lang.Exception if any.
	 */
	public  void visit(List<NodeRef> entities, OutputStream result) throws Exception ;

	/**
	 * <p>visitData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @throws java.lang.Exception if any.
	 */
	public  void visitData(NodeRef entityNodeRef, OutputStream result)throws Exception ;


	
}
