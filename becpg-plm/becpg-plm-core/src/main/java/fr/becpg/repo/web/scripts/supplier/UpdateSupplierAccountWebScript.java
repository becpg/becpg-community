package fr.becpg.repo.web.scripts.supplier;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.supplier.SupplierPortalService;

/**
 * <p>UpdateSupplierAccountWebScript class.</p>
 *
 * @author matthieu
 */
public class UpdateSupplierAccountWebScript extends AbstractWebScript {

	private static final String PARAM_ENTITY_NODEREFS = "nodeRefs";

	private NodeService nodeService;

	private SupplierPortalService supplierPortalService;
	
	private EntityService entityService;
	
	/**
	 * <p>Setter for the field <code>entityService</code>.</p>
	 *
	 * @param entityService a {@link fr.becpg.repo.entity.EntityService} object
	 */
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/**
	 * <p>Setter for the field <code>supplierPortalService</code>.</p>
	 *
	 * @param supplierPortalService a {@link fr.becpg.repo.supplier.SupplierPortalService} object
	 */
	public void setSupplierPortalService(SupplierPortalService supplierPortalService) {
		this.supplierPortalService = supplierPortalService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		JSONObject ret = new JSONObject();
		NodeRef supplierNodeRef = null;
		if (req.getParameter(PARAM_ENTITY_NODEREFS) != null) {
			for (String nodeRefString : req.getParameter(PARAM_ENTITY_NODEREFS).split(",")) {
				NodeRef contactListNodeRef = new NodeRef(nodeRefString);
				if (supplierNodeRef == null) {
					supplierNodeRef = entityService.getEntityNodeRef(contactListNodeRef, nodeService.getType(contactListNodeRef));
				}
				supplierPortalService.updateSupplierAccount(supplierNodeRef, contactListNodeRef);
			}
		}
		res.setContentType("application/json");
		res.setContentEncoding("UTF-8");
		ret.write(res.getWriter());
	}

}
