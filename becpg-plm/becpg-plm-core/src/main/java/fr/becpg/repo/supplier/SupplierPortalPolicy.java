package fr.becpg.repo.supplier;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteAssociationPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.transaction.TransactionSupportUtil;

import fr.becpg.model.PLMGroup;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityService;
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
	
	private EntityService entityService;
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
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
			throw new IllegalStateException("You need to be Referencing Manager to delete an association of type 'bcpg:supplierAccountRefAspect'");
		}
		NodeRef supplierAccountNodeRef = nodeAssocRef.getTargetRef();
		List<NodeRef> sourcesAssocs = new ArrayList<>(associationService.getSourcesAssocs(supplierAccountNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS));
		if (nodeService.getType(nodeAssocRef.getSourceRef()).equals(PLMModel.TYPE_CONTACTLIST)) {
			NodeRef supplierNodeRef = entityService.getEntityNodeRef(nodeAssocRef.getSourceRef(), PLMModel.TYPE_CONTACTLIST);
			if (supplierNodeRef != null) {
				sourcesAssocs.remove(supplierNodeRef);
			}
		}
		sourcesAssocs.removeIf(n -> nodeService.getType(n).equals(ProjectModel.TYPE_PROJECT));
		if (sourcesAssocs.isEmpty()) {
			String supplierUserName = (String) nodeService.getProperty(supplierAccountNodeRef, ContentModel.PROP_USERNAME);
			AuthorityHelper.disableAccount(supplierUserName);
		}
	}

}
