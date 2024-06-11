package fr.becpg.repo.web.scripts.supplier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.supplier.SupplierPortalService;

/**
 * <p>SupplierAccountWebScript class.</p>
 *
 * @author rabah, matthieu
 * @version $Id: $Id
 */
public class SupplierAccountWebScript extends AbstractWebScript {

	private static final String PARAM_ENTITY_NODEREF = "nodeRef";
	private static final String PARAM_EMAIL_ADDRESS = "emailAddress";
	private static final String PARAM_NOTIFY_SUPPLIER = "notifySupplier";
	private static final String SUPPLIER_PREFIX = "supplier";
	private static final String PARAM_FIRST_NAME = "firstName";
	private static final String PARAM_LAST_NAME = "lastName";

	NodeService nodeService;

	AssociationService associationService;

	SupplierPortalService supplierPortalService;
	
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
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		NodeRef nodeRef = new NodeRef(req.getParameter(PARAM_ENTITY_NODEREF));
		Boolean notifySupplier = Boolean.parseBoolean(req.getParameter(PARAM_NOTIFY_SUPPLIER));
		String supplierEmail = req.getParameter(PARAM_EMAIL_ADDRESS);

		String supplierFirstName = req.getParameter(PARAM_FIRST_NAME);
		String supplierLastName = req.getParameter(PARAM_LAST_NAME);

		if (supplierFirstName == null || supplierFirstName.isEmpty()) {
			supplierFirstName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
		}

		if (supplierLastName == null || supplierLastName.isEmpty()) {
			supplierLastName = SUPPLIER_PREFIX + "-" + (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE);
		}

		try {
			
			NodeRef personNodeRef = supplierPortalService.createExternalUser(supplierEmail, supplierFirstName, supplierLastName, notifySupplier, null);

			final List<NodeRef> associations = new ArrayList<>();
			associations.addAll(associationService.getTargetAssocs(nodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS));

			associations.add(personNodeRef);
			associationService.update(nodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS, associations);

			JSONObject ret = new JSONObject();
			ret.put("login", supplierEmail);
			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}

}
