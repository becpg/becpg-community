package fr.becpg.repo.entity.policy;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DocumentEffectivityType;
import fr.becpg.model.DocumentSupplierUpdateMode;
import fr.becpg.model.SystemGroup;
import fr.becpg.model.SystemState;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>DocumentAspectPolicy class.</p>
 *
 * @author maxime
 * @version $Id: $Id
 */
public class DocumentAspectPolicy extends AbstractBeCPGPolicy
		implements ContentServicePolicies.OnContentUpdatePolicy, NodeServicePolicies.OnCreateAssociationPolicy {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(DocumentAspectPolicy.class);

	private static final String PROJECT_URI = "http://www.bcpg.fr/model/project/1.0";

	/** <code>pjt:projectEntity</code>, read by name: the project model is not a dependency of core. */
	private static final QName ASSOC_PROJECT_ENTITY = QName.createQName(PROJECT_URI, "projectEntity");

	private static final QName PROP_PROJECT_STATE = QName.createQName(PROJECT_URI, "projectState");

	private static final Set<String> OPEN_PROJECT_STATES = Set.of("Planned", "InProgress", "OnHold");

	private static final String EXTERNAL_USER_GROUP = PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser;

	private AssociationService associationService;

	private AuthorityService authorityService;

	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, BeCPGModel.ASPECT_DOCUMENT_ASPECT,
				new JavaBehaviour(this, "onContentUpdate"));
		
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,  BeCPGModel.ASPECT_DOCUMENT_ASPECT,
				BeCPGModel.ASSOC_DOCUMENT_TYPE_REF, new JavaBehaviour(this, "onCreateAssociation"));

	}

	/** {@inheritDoc} */
	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		queueNode(nodeRef);

	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef nodeAssocRef) {
		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)) {
			queueNode(nodeAssocRef.getSourceRef());
		}
	}
		

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {
			logger.debug("onContentUpdate");

			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef)) {
				NodeRef documentTypeRef = associationService.getTargetAssoc(nodeRef, BeCPGModel.ASSOC_DOCUMENT_TYPE_REF);
				if (documentTypeRef != null) {
					String effectivityType = (String) nodeService.getProperty(documentTypeRef, BeCPGModel.PROP_DOCUMENT_TYPE_EFFECTIVITY_TYPE);
					if (DocumentEffectivityType.AUTO.toString().equals(effectivityType)
							|| DocumentEffectivityType.NONE.toString().equals(effectivityType)) {
						Date fromDate = (Date) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CM_FROM);
						if (fromDate == null) {
							fromDate = new Date();
							nodeService.setProperty(nodeRef, BeCPGModel.PROP_CM_FROM, fromDate);
						}

						if (DocumentEffectivityType.AUTO.toString().equals(effectivityType)) {
							Integer autoExpirationDelay = (Integer) nodeService.getProperty(documentTypeRef,
									BeCPGModel.PROP_DOCUMENT_TYPE_AUTO_EXPIRATION_DELAY);
							if (autoExpirationDelay != null) {
								Calendar toDateCal = Calendar.getInstance();
								toDateCal.setTime(fromDate);
								toDateCal.add(Calendar.DAY_OF_YEAR, autoExpirationDelay);
								nodeService.setProperty(nodeRef, BeCPGModel.PROP_CM_TO, toDateCal.getTime());
							}
						}
					}
				}
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_DOCUMENT_STATE, depositState(nodeRef, documentTypeRef).toString());
			}
		}

		return true;
	}

	/**
	 * The state of a new version, from the supplier update mode of its type.
	 *
	 * The mode only governs what a supplier does outside a task: an internal
	 * user, a document under a supplier project still open, or a type without a
	 * mode keep the historical behaviour, which is to wait for validation.
	 */
	private SystemState depositState(NodeRef nodeRef, NodeRef documentTypeRef) {
		DocumentSupplierUpdateMode mode = supplierUpdateMode(documentTypeRef);
		if ((mode == null) || !isExternalUser() || isUnderOpenProject(nodeRef)) {
			return SystemState.ToValidate;
		}
		switch (mode) {
		case OnRequest:
			throw new IllegalStateException(I18NUtil.getMessage("message.document.supplier-update.on-request"));
		case Free:
			return SystemState.Valid;
		case Notify:
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_DOCUMENT_SUPPLIER_DEPOSIT_DATE, new Date());
			return SystemState.Valid;
		default:
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_DOCUMENT_SUPPLIER_DEPOSIT_DATE, new Date());
			return SystemState.ToValidate;
		}
	}

	private DocumentSupplierUpdateMode supplierUpdateMode(NodeRef documentTypeRef) {
		String mode = documentTypeRef == null ? null
				: (String) nodeService.getProperty(documentTypeRef, BeCPGModel.PROP_DOCUMENT_TYPE_SUPPLIER_UPDATE_MODE);
		return (mode == null) || mode.isBlank() ? null : DocumentSupplierUpdateMode.valueOf(mode);
	}

	private boolean isExternalUser() {
		String userName = AuthenticationUtil.getRunAsUser();
		return (userName != null) && !AuthenticationUtil.isRunAsUserTheSystemUser()
				&& authorityService.getAuthoritiesForUser(userName).contains(EXTERNAL_USER_GROUP);
	}

	/**
	 * Inside a task the supplier works on a branch, or on the supplier entity
	 * itself under an open project.
	 */
	private boolean isUnderOpenProject(NodeRef nodeRef) {
		NodeRef entityNodeRef = associationService.getTargetAssoc(nodeRef, BeCPGModel.ASSOC_DOCUMENT_ENTITY_REF);
		if (entityNodeRef == null) {
			return false;
		}
		if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_BRANCH)) {
			return true;
		}
		return associationService.getSourcesAssocs(entityNodeRef, ASSOC_PROJECT_ENTITY).stream()
				.anyMatch(projectNodeRef -> OPEN_PROJECT_STATES.contains((String) nodeService.getProperty(projectNodeRef, PROP_PROJECT_STATE)));
	}

}
