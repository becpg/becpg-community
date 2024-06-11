package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityTplService;

/**
 * <p>AddActivityListPatch class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AddActivityListPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(AddActivityListPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.addActivityListPatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private RuleService ruleService;
	private EntityTplService entityTplService;

	/**
	 * <p>Setter for the field <code>entityTplService</code>.</p>
	 *
	 * @param entityTplService a {@link fr.becpg.repo.entity.EntityTplService} object.
	 */
	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		doForType(PLMModel.ASPECT_PRODUCT, true);
		doForType(PLMModel.TYPE_CLIENT, false);
		doForType(PLMModel.TYPE_SUPPLIER, false);

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void doForType(final QName type, boolean isAspect) {
		
		final Pair<Long, QName> val = getQnameDAO().getQName(type);
		
		if (val != null) {
			Long typeQNameId = val.getFirst();
			List<Long> nodeids = null;
			
			if (isAspect) {
				nodeids = getPatchDAO().getNodesByAspectQNameId(typeQNameId, 1L, getNodeDAO().getMaxNodeId());
			} else {
				nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, 1L, getNodeDAO().getMaxNodeId());
			}
			
			for (Long nodeid : nodeids) {
				NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
				if (!status.isDeleted()) {
					if (nodeService.exists(status.getNodeRef())) {
						if (nodeService.hasAspect(status.getNodeRef(), BeCPGModel.ASPECT_ENTITY_TPL)) {
							logger.debug("Add activity list to entity template " + status.getNodeRef());
							entityTplService.createOrUpdateList(status.getNodeRef(), BeCPGModel.TYPE_ACTIVITY_LIST);
						}
					} else {
						logger.warn("entityNodeRef doesn't exist : " + status.getNodeRef());
					}
				}
			}
		}
	}
			

	/**
	 * <p>Getter for the field <code>nodeDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	/**
	 * <p>Setter for the field <code>nodeDAO</code>.</p>
	 *
	 * @param nodeDAO a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	/**
	 * <p>Getter for the field <code>patchDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	/**
	 * <p>Setter for the field <code>patchDAO</code>.</p>
	 *
	 * @param patchDAO a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	/**
	 * <p>Getter for the field <code>qnameDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	/**
	 * <p>Setter for the field <code>qnameDAO</code>.</p>
	 *
	 * @param qnameDAO a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	/**
	 * <p>Getter for the field <code>ruleService</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public RuleService getRuleService() {
		return ruleService;
	}

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

}
