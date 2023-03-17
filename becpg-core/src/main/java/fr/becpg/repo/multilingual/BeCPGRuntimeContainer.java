package fr.becpg.repo.multilingual;

import java.io.IOException;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.web.scripts.TenantRepositoryContainer;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.helper.MLTextHelper;

/**
 * <p>BeCPGRuntimeContainer class.</p>
 *
 * @author matthieu Override TenantRepositoryContainer
 * @version $Id: $Id
 */
public class BeCPGRuntimeContainer extends TenantRepositoryContainer implements TenantDeployer {

	private static final Log logger = LogFactory.getLog(BeCPGRuntimeContainer.class);

	private NodeService nodeService;

	private PersonService personService;


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

	/** {@inheritDoc} */
	@Override
	protected void transactionedExecute(final WebScript script, final WebScriptRequest scriptReq, final WebScriptResponse scriptRes)
			throws IOException {
		
		
		String userId = AuthenticationUtil.getFullyAuthenticatedUser();
		
		if ((userId != null) && !userId.isEmpty() && !AuthenticationUtil.getGuestUserName().equals(userId)  && personService.personExists(userId)) {
			NodeRef personNodeRef = personService.getPerson(userId);
			if ((personNodeRef != null) && nodeService.exists(personNodeRef)) {

				if (logger.isDebugEnabled()) {
					logger.debug("Set content locale:" + MLTextHelper.getUserContentLocale(nodeService, personNodeRef));
				}

				I18NUtil.setLocale(MLTextHelper.getUserLocale(nodeService,personNodeRef));
				I18NUtil.setContentLocale(MLTextHelper.getUserContentLocale(nodeService,personNodeRef));
			}
		}

		try {
			super.transactionedExecute(script, scriptReq, scriptRes);

		} finally {
			I18NUtil.setContentLocale(null);
		}
	}

	

}
