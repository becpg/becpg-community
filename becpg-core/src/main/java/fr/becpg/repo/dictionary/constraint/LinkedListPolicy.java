/*
 * 
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>LinkedListPolicy class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
public class LinkedListPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnDeleteNodePolicy, NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnCreateAssociationPolicy {

	private static final String KEY_UPDATE_READ_PERMISSIONS = "KEY_UPDATE_READ_PERMISSIONS";
	
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private PermissionService permissionService;
	
	private AssociationService associationService;
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
	
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}
	
	/**
	 * <p>doInit.</p>
	 */
	public void doInit() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.TYPE_LINKED_VALUE, new JavaBehaviour(this,
				"onDeleteNode"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.TYPE_LINKED_VALUE,
				new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, BeCPGModel.TYPE_LINKED_VALUE,
				new JavaBehaviour(this, "onDeleteAssociation"));
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		queueNode(childAssocRef.getChildRef());
	}
	
	@Override
	public void onCreateAssociation(AssociationRef nodeAssocRef) {
		if (SecurityModel.ASSOC_READ_GROUPS.equals(nodeAssocRef.getTypeQName())) {
			queueNode(KEY_UPDATE_READ_PERMISSIONS, nodeAssocRef.getSourceRef());
		}
	}
	
	@Override
	public void onDeleteAssociation(AssociationRef nodeAssocRef) {
		if (SecurityModel.ASSOC_READ_GROUPS.equals(nodeAssocRef.getTypeQName())) {
			queueNode(KEY_UPDATE_READ_PERMISSIONS, nodeAssocRef.getSourceRef());
		}
	}
	
	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		if(!pendingNodes.isEmpty()){
			alfrescoRepository.clearCaches(LinkedListPolicy.class.getName());
		}
		
		if (KEY_UPDATE_READ_PERMISSIONS.equals(key)) {
			for (NodeRef pendingNode : pendingNodes) {
				updatePermissions(pendingNode, PermissionService.READ);
			}
		}
		
		return true;
	}

	private void updatePermissions(NodeRef nodeRef, String permission) {
		List<NodeRef> readGroups = associationService.getTargetAssocs(nodeRef, SecurityModel.ASSOC_READ_GROUPS);
	
		if (readGroups.isEmpty()) {
			clearPermissions(nodeRef, true);
		} else {
			clearPermissions(nodeRef, false);
			setPermissions(nodeRef, readGroups, permission);
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
	
	public boolean clearPermissions(NodeRef nodeRef, boolean inherite) {
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
	
}
