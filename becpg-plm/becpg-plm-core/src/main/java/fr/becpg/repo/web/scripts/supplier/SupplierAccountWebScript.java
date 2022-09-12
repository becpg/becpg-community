package fr.becpg.repo.web.scripts.supplier;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.mail.BeCPGMailService;

/**
 * <p>SupplierAccountWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SupplierAccountWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(SupplierAccountWebScript.class);

	private static final String PARAM_ENTITY_NODEREF = "nodeRef";
	private static final String PARAM_EMAIL_ADDRESS = "emailAddress";
	private static final String PARAM_NOTIFY_SUPPLIER = "notifySupplier";
	private static final String SUPPLIER_PREFIX = "supplier";

	NodeService nodeService;

	PersonService personService;

	AuthorityService authorityService;

	TransactionService transactionService;

	BeCPGMailService mailService;

	MutableAuthenticationService authenticationService;

	AssociationService associationService;
	
	TenantAdminService tenantAdminService;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>personService</code>.</p>
	 *
	 * @param personService a {@link org.alfresco.service.cmr.security.PersonService} object.
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
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
	 * <p>Setter for the field <code>transactionService</code>.</p>
	 *
	 * @param transactionService a {@link org.alfresco.service.transaction.TransactionService} object.
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * <p>Setter for the field <code>mailService</code>.</p>
	 *
	 * @param mailService a {@link fr.becpg.repo.mail.BeCPGMailService} object.
	 */
	public void setMailService(BeCPGMailService mailService) {
		this.mailService = mailService;
	}

	/**
	 * <p>Setter for the field <code>authenticationService</code>.</p>
	 *
	 * @param authenticationService a {@link org.alfresco.service.cmr.security.MutableAuthenticationService} object.
	 */
	public void setAuthenticationService(MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		NodeRef nodeRef = new NodeRef(req.getParameter(PARAM_ENTITY_NODEREF));
		Boolean notifySupplier = Boolean.parseBoolean(req.getParameter(PARAM_NOTIFY_SUPPLIER));
		String supplierEmail = req.getParameter(PARAM_EMAIL_ADDRESS);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			String userName = SUPPLIER_PREFIX + "-" + (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE);
			if (!TenantService.DEFAULT_DOMAIN.equals(tenantAdminService.getCurrentUserDomain())) {
				userName += "@" + tenantAdminService.getCurrentUserDomain();
			}
			
			String password = UUID.randomUUID().toString();
			
			List<NodeRef> associations = associationService.getTargetAssocs(nodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
			if (associations == null) {
				associations = new ArrayList<>();
			}

			if (!personService.personExists(userName)) {

				if (logger.isDebugEnabled()) {
					logger.debug("Create external user: " + userName + " pwd: " + password);
				}

				authenticationService.createAuthentication(userName, password.toCharArray());

				Map<QName, Serializable> propMap = new HashMap<>();
				propMap.put(ContentModel.PROP_USERNAME, userName);
				propMap.put(ContentModel.PROP_LASTNAME, userName + "_" + supplierEmail);
				propMap.put(ContentModel.PROP_FIRSTNAME, nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
				propMap.put(ContentModel.PROP_EMAIL, supplierEmail);
				NodeRef userRef = personService.createPerson(propMap);
				authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, SystemGroup.ExternalUser.toString()), userName);

				associations.add(userRef);

				String creator = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR);
				mailService.sendMailNewUser(personService.getPersonOrNull(creator), userName, password, true);

				// notify supplier
				if (Boolean.TRUE.equals(notifySupplier)) {
					mailService.sendMailNewUser(userRef, userName, password, false);
				}

			} else {

				if (logger.isDebugEnabled()) {
					logger.debug("Reassign to an existing user");
				}

				associations.add(personService.getPerson(userName));

			}

			associationService.update(nodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS, associations);

			try {
				JSONObject ret = new JSONObject();
				ret.put("login", userName);
				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				ret.write(res.getWriter());
			} catch (JSONException e) {
				throw new WebScriptException("Unable to serialize JSON", e);
			}

			return null;
		}, true, false);

	}

}
