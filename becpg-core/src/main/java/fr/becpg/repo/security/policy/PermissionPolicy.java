package fr.becpg.repo.security.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.security.SecurityService;

/**
 * <p>PermissionPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PermissionPolicy extends AbstractBeCPGPolicy
		implements NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnCreateAssociationPolicy,
		OnCreateNodePolicy, OnDeleteNodePolicy, OnUpdatePropertiesPolicy, OnCreateChildAssociationPolicy, OnDeleteChildAssociationPolicy {
	
	private static final String KEY_UPDATE_READ_PERMISSIONS = "KEY_UPDATE_READ_PERMISSIONS";

	private PermissionService permissionService;
	
	private AssociationService associationService;
	
	private SecurityService securityService;
	
	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
	
	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "onDeleteAssociation"));
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, SecurityModel.TYPE_ACL_GROUP,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, SecurityModel.TYPE_ACL_GROUP,
				new JavaBehaviour(this, "onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, SecurityModel.TYPE_ACL_GROUP,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, SecurityModel.TYPE_ACL_GROUP,
				new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, SecurityModel.TYPE_ACL_GROUP,
				new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, SecurityModel.TYPE_ACL_GROUP,
				new JavaBehaviour(this, "onCreateChildAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteChildAssociationPolicy.QNAME, SecurityModel.TYPE_ACL_GROUP,
				new JavaBehaviour(this, "onDeleteChildAssociation"));
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, SecurityModel.TYPE_ACL_ENTRY,
				new JavaBehaviour(this, "onUpdateProperties"));
		
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef nodeAssocRef) {
		if (SecurityModel.ASSOC_READ_GROUPS.equals(nodeAssocRef.getTypeQName())) {
			queueNode(KEY_UPDATE_READ_PERMISSIONS, nodeAssocRef.getSourceRef());
		}
		securityService.refreshAcls();
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef nodeAssocRef) {
		if (SecurityModel.ASSOC_READ_GROUPS.equals(nodeAssocRef.getTypeQName())) {
			queueNode(KEY_UPDATE_READ_PERMISSIONS, nodeAssocRef.getSourceRef());
		}
		securityService.refreshAcls();
	}
	
	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		if (KEY_UPDATE_READ_PERMISSIONS.equals(key)) {
			for (NodeRef pendingNode : pendingNodes) {
				updatePermissions(pendingNode, PermissionService.READ);
			}
		}
		return true;
	}
	

	private void updatePermissions(NodeRef nodeRef, String permission) {
		if (nodeService.exists(nodeRef)) {
			List<NodeRef> readGroups = associationService.getTargetAssocs(nodeRef, SecurityModel.ASSOC_READ_GROUPS);
			
			if (readGroups.isEmpty()) {
				clearPermissions(nodeRef, true);
			} else {
				clearPermissions(nodeRef, false);
				setPermissions(nodeRef, readGroups, permission);
			}
		}
	}

	private void setPermissions(NodeRef nodeRef, List<NodeRef> groups, String permission) {
		AuthenticationUtil.runAsSystem(() -> {
			permissionService.setInheritParentPermissions(nodeRef, false);
			
			for (NodeRef group : groups) {
				String authorityName = (String) nodeService.getProperty(group, ContentModel.PROP_AUTHORITY_NAME);
				permissionService.setPermission(nodeRef, authorityName, permission, true);
			}
			
			return null;
		});
	}
	
	private boolean clearPermissions(NodeRef nodeRef, boolean inherite) {
		return AuthenticationUtil.runAsSystem(() -> {
			Set<AccessPermission> acls = permissionService.getAllSetPermissions(nodeRef);
			for (AccessPermission permission : acls) {
				if (permission.isSetDirectly()) {
				   permissionService.deletePermission(nodeRef, permission.getAuthority(), permission.getPermission());
				}
			}
			permissionService.setInheritParentPermissions(nodeRef, inherite);
			return true;
		});
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteChildAssociation(ChildAssociationRef childAssocRef) {
		securityService.refreshAcls();
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
		securityService.refreshAcls();
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		securityService.refreshAcls();
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		securityService.refreshAcls();
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		securityService.refreshAcls();
	}

}
