package fr.becpg.repo.supplier;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteAssociationPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.util.transaction.TransactionSupportUtil;

import fr.becpg.model.PLMGroup;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>SupplierPortalPolicy class.</p>
 *
 * @author matthieu
 */
public class SupplierPortalPolicy extends AbstractBeCPGPolicy implements OnDeleteAssociationPolicy {

	/** Constant <code>FORCE_REFERENCING_MANAGER="forceReferencingManager"</code> */
	public static final String FORCE_REFERENCING_MANAGER = "forceReferencingManager";

	private AssociationService associationService;

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
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, PLMModel.ASPECT_SUPPLIERS_ACCOUNTREF,
				PLMModel.ASSOC_SUPPLIER_ACCOUNTS, new JavaBehaviour(this, "onDeleteAssociation"));
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef nodeAssocRef) {
		Boolean forceReferencingManager = TransactionSupportUtil.getResource(FORCE_REFERENCING_MANAGER);
		if ((forceReferencingManager == null || !forceReferencingManager.booleanValue()) && !AuthorityHelper.hasAdminAuthority()
				&& !AuthenticationUtil.isRunAsUserTheSystemUser()
				&& !AuthorityHelper.hasGroupAuthority(AuthenticationUtil.getRunAsUser(), PLMGroup.ReferencingMgr.toString())) {
			throw new IllegalStateException("You need to be Referencing Manager to perform this operation");
		}
		if (associationService.getSourcesAssocs(nodeAssocRef.getTargetRef(), PLMModel.ASSOC_SUPPLIER_ACCOUNTS).isEmpty()) {
			nodeService.addAspect(nodeAssocRef.getTargetRef(), ContentModel.ASPECT_PERSON_DISABLED, null);
		}
	}

}
