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
	 * Is a supplier project still open (Planned, InProgress, OnHold) on this
	 * entity, on the entity it was branched from, or on one of its branches?
	 *
	 * @param entityNodeRef an entity, an entity branch or a supplier
	 * @return the name of the first open project found, or null when there is none
	 */
	String findOpenSupplierProject(NodeRef entityNodeRef);

	/**
	 * A supplier asks for the update of one of its validated sheets, or of its
	 * own supplier record: starts a supplier project on it, exactly as « Send to
	 * supplier » does, with the reason written on the project. The whole
	 * workflow runs again: the supplier goes back through the wizard.
	 *
	 * The caller must be one of the supplier's accounts, the template one of
	 * {@link #findSupplierProjectTemplates()}, and no supplier project may already
	 * be open on the entity.
	 *
	 * @param entityNodeRef the validated entity, or the supplier itself
	 * @param templateNodeRef the project template the supplier picked
	 * @param reason why the supplier asks, shown to the quality team
	 * @return the project created
	 * @throws org.alfresco.repo.security.permissions.AccessDeniedException when the caller is not an account of the supplier, or the template is not open to suppliers
	 * @throws IllegalStateException when a supplier project is already open on the entity
	 * @throws IllegalArgumentException when the entity is a working copy or the reason is blank
	 */
	NodeRef requestSupplierUpdate(NodeRef entityNodeRef, NodeRef templateNodeRef, String reason);

	/**
	 * The supplier project templates a supplier may start itself: enabled
	 * templates carrying {@code bcpg:suppliersAspect} on which
	 * {@code GROUP_ExternalUser} holds a read permission. That permission is the
	 * whole configuration — no property, no extra field.
	 *
	 * @return the templates, possibly empty
	 */
	List<NodeRef> findSupplierProjectTemplates();
	
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

	/**
	 * <p>updateSupplierAccount.</p>
	 *
	 * @param supplierNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param contactListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	void updateSupplierAccount(NodeRef supplierNodeRef, NodeRef contactListNodeRef);

	/**
	 * <p>deleteExternalUser.</p>
	 *
	 * @param userNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param supplierNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	void deleteExternalUser(NodeRef userNodeRef, NodeRef supplierNodeRef);

	/**
	 * <p>getOrCreateSupplierGroup.</p>
	 *
	 * @param supplierNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param resources a {@link java.util.List} object
	 * @return a {@link java.lang.String} object
	 */
	String getOrCreateSupplierGroup(NodeRef supplierNodeRef, List<NodeRef> resources);

	/**
	 * <p>isCurrentUserInSupplierGroup.</p>
	 *
	 * @param supplierNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	boolean isCurrentUserInSupplierGroup(NodeRef supplierNodeRef);
}
