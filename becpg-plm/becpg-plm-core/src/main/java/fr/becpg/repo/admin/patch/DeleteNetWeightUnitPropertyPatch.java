package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;

public class DeleteNetWeightUnitPropertyPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(DeleteNetWeightUnitPropertyPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.deleteNetWeightPropertyPatch.result";
	
	QName PROP_NET_WEIGHT_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "netWeightUnit");

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;

	@Override
	protected String applyInternal() throws Exception {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
		
		policyBehaviourFilter.disableBehaviour();

		doForType(PLMModel.TYPE_FINISHEDPRODUCT, false);
		doForType(PLMModel.TYPE_SEMIFINISHEDPRODUCT, false);
		doForType(PLMModel.TYPE_RAWMATERIAL, false);
		doForType(PLMModel.TYPE_PACKAGINGMATERIAL, false);
		doForType(PLMModel.TYPE_PACKAGINGKIT, false);
		
		policyBehaviourFilter.enableBehaviour();
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void doForType(final QName type, boolean isAspect) {
		
		final Pair<Long, QName> val = getQnameDAO().getQName(type);
		
		if (val != null) {
			Long typeQNameId = val.getFirst();
			List<Long> nodeids = null;
		
			nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, 1L, getPatchDAO().getMaxAdmNodeID());
			
			for (Long nodeid : nodeids) {
				NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
				if (!status.isDeleted()) {
					if (nodeService.exists(status.getNodeRef())) {
						if(nodeService.getProperty(status.getNodeRef(), PROP_NET_WEIGHT_UNIT) != null) {
							logger.info("Remove netWeightUnit On :" + status.getNodeRef());
							nodeService.removeProperty(status.getNodeRef(), PROP_NET_WEIGHT_UNIT);
						}
					} else {
						logger.warn("entityNodeRef doesn't exist : " + status.getNodeRef());
					}
				}
			}
		}
	}
			

	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	
}
