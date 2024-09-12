package fr.becpg.repo.supplier;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>SupplierPortalService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SupplierPortalService {

	/**
	 * <p>createSupplierProject.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param projectTemplateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param supplierAccountNodeRefs a {@link java.util.List} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef createSupplierProject(NodeRef entityNodeRef, NodeRef projectTemplateNodeRef, List<NodeRef> supplierAccountNodeRefs);
	
	/**
	 * <p>getSupplierNodeRef.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef getSupplierNodeRef(NodeRef entityNodeRef);
	
	/**
	 * <p>getOrCreateSupplierDestFolder.</p>
	 *
	 * @param supplierNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param resources a {@link java.util.List} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef getOrCreateSupplierDestFolder(NodeRef supplierNodeRef, List<NodeRef> resources);
	
	/**
	 * <p>createName.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param supplierNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param nameFormat a {@link java.lang.String} object
	 * @param currentDate a {@link java.util.Date} object
	 * @return a {@link java.lang.String} object
	 */
	String createName(NodeRef entityNodeRef, NodeRef supplierNodeRef, String nameFormat, Date currentDate);
	
	/**
	 * <p>getProjectNameTpl.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getProjectNameTpl();
	
	/**
	 * <p>getEntityNameTpl.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getEntityNameTpl();
	
	/**
	 * <p>createExternalUser.</p>
	 *
	 * @param email a {@link java.lang.String} object
	 * @param firstName a {@link java.lang.String} object
	 * @param lastName a {@link java.lang.String} object
	 * @param notify a boolean
	 * @param extraProps a {@link java.util.Map} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef createExternalUser(String email, String firstName, String lastName, boolean notify, Map<QName, Serializable> extraProps);

	/**
	 * <p>extractSupplierAccountRefs.</p>
	 *
	 * @param document a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.List} object
	 */
	List<NodeRef> extractSupplierAccountRefs(NodeRef document);

	/**
	 * <p>getOrCreateSupplierDocumentsFolder.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef getOrCreateSupplierDocumentsFolder(NodeRef entityNodeRef);
}
