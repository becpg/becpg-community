package fr.becpg.repo.web.scripts.supplier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.BasicPasswordGenerator;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.authentication.BeCPGUserAccount;
import fr.becpg.repo.authentication.BeCPGUserAccountService;
import fr.becpg.repo.helper.AssociationService;

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

	AuthorityService authorityService;

	AssociationService associationService;

	BeCPGUserAccountService beCPGUserAccountService;

	public void setBeCPGUserAccountService(BeCPGUserAccountService beCPGUserAccountService) {
		this.beCPGUserAccountService = beCPGUserAccountService;
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
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object.
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
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

		if (supplierEmail == null || supplierEmail.isBlank()) {
			throw new IllegalStateException(I18NUtil.getMessage("message.supplier.missing-email"));
		}

		String supplierFirstName = req.getParameter(PARAM_FIRST_NAME);
		String supplierLastName = req.getParameter(PARAM_LAST_NAME);

		if (supplierFirstName == null || supplierFirstName.isEmpty()) {
			supplierFirstName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
		}

		if (supplierLastName == null || supplierLastName.isEmpty()) {
			supplierLastName = SUPPLIER_PREFIX + "-" + (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE);
		}

		try {

			boolean hasAccess = AuthenticationUtil.runAsSystem(() -> {
				return authorityService.hasAdminAuthority() || authorityService.getAuthoritiesForUser(AuthenticationUtil.getFullyAuthenticatedUser())
						.contains(PermissionService.GROUP_PREFIX + SystemGroup.ExternalUserMgr.toString());
			});

			if (hasAccess) {

				BasicPasswordGenerator pwdGen = new BasicPasswordGenerator();
				pwdGen.setPasswordLength(10);

				BeCPGUserAccount userAccount = new BeCPGUserAccount();
				userAccount.setEmail(supplierEmail);
				userAccount.setUserName(supplierEmail);
				userAccount.setFirstName(supplierFirstName);
				userAccount.setLastName(supplierLastName);
				userAccount.setPassword(pwdGen.generatePassword());
				userAccount.setNotify(notifySupplier);
				userAccount.getAuthorities().add(SystemGroup.ExternalUser.toString());

				NodeRef personNodeRef = beCPGUserAccountService.getOrCreateUser(userAccount);

				final List<NodeRef> associations = new ArrayList<>();
				associations.addAll(associationService.getTargetAssocs(nodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS));

				associations.add(personNodeRef);
				associationService.update(nodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS, associations);

				JSONObject ret = new JSONObject();
				ret.put("login", supplierEmail);
				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				ret.write(res.getWriter());

			} else {
				throw new IllegalAccessError("You should be member of ExternalUserMgr");
			}
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}

}
