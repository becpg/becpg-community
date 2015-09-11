package fr.becpg.repo.admin.patch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityTplService;

public class WUsedListPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(WUsedListPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.wusedListPatch.result";

	private EntityTplService entityTplService;

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	
	
	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	@Override
	protected String applyInternal() throws Exception {

		Set<QName> productTypes = new HashSet<>();
		productTypes.add(PLMModel.TYPE_RAWMATERIAL);
		productTypes.add(PLMModel.TYPE_SEMIFINISHEDPRODUCT);
		productTypes.add(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT);
		productTypes.add(PLMModel.TYPE_FINISHEDPRODUCT);
		productTypes.add(PLMModel.TYPE_PACKAGINGMATERIAL);
		productTypes.add(PLMModel.TYPE_PACKAGINGKIT);
		productTypes.add(PLMModel.TYPE_RESOURCEPRODUCT);

		Pair<Long, QName> val = qnameDAO.getQName(BeCPGModel.ASPECT_ENTITY_TPL);

		if(val!=null){
		
			for (QName productType : productTypes) {
	
				// datalists
	
				QName wusedQName = null;
	
				if (productType.equals(PLMModel.TYPE_RAWMATERIAL)) {
	
					wusedQName = PLMModel.TYPE_COMPOLIST;
	
				} else if (productType.equals(PLMModel.TYPE_PACKAGINGMATERIAL)) {
	
					wusedQName = PLMModel.TYPE_PACKAGINGLIST;
	
				} else if (productType.equals(PLMModel.TYPE_RESOURCEPRODUCT)) {
	
					wusedQName = MPMModel.TYPE_PROCESSLIST;
	
				} else if (productType.equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)) {
	
					wusedQName = PLMModel.TYPE_COMPOLIST;
	
				} else if (productType.equals(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)) {
	
					wusedQName = PLMModel.TYPE_COMPOLIST;
	
				} else if (productType.equals(PLMModel.TYPE_FINISHEDPRODUCT)) {
	
					wusedQName = PLMModel.TYPE_COMPOLIST;
	
				} else if (productType.equals(PLMModel.TYPE_PACKAGINGKIT)) {
	
					wusedQName = PLMModel.TYPE_PACKAGINGLIST;
	
				}
	
				List<Long> nodeids = patchDAO.getNodesByAspectQNameId(val.getFirst(), 1L, patchDAO.getMaxAdmNodeID());
	
				for (Long nodeid : nodeids) {
					NodeRef.Status status = nodeDAO.getNodeIdStatus(nodeid);
					if (!status.isDeleted()) {
						NodeRef entityTplNodeRef = status.getNodeRef();
						if (nodeService.exists(entityTplNodeRef) && !nodeService.hasAspect(entityTplNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
								&& productType.equals(nodeService.getType(entityTplNodeRef))) {
	
							logger.info("Adding wusedList : " + wusedQName.toPrefixString() + " to "
									+ nodeService.getProperty(entityTplNodeRef, ContentModel.PROP_NAME));
	
							entityTplService.createWUsedList(entityTplNodeRef, wusedQName, null);
						}
					}
				}
	
			}
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
