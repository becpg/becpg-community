package fr.becpg.repo.multilingual;

import java.io.IOException;
import java.util.Locale;

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

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.MLTextHelper;

/**
 * <p>BeCPGRuntimeContainer class.</p>
 *
 * @author matthieu Override TenantRepositoryContainer
 * @version $Id: $Id
 */
public class BeCPGRuntimeContainer extends TenantRepositoryContainer implements TenantDeployer {

	private final static Log logger = LogFactory.getLog(BeCPGRuntimeContainer.class);

	private NodeService nodeService;

	private PersonService personService;

	private boolean useBrowserLocale = false;

	/**
	 * <p>Setter for the field <code>useBrowserLocale</code>.</p>
	 *
	 * @param useBrowserLocale a boolean.
	 */
	public void setUseBrowserLocale(boolean useBrowserLocale) {
		this.useBrowserLocale = useBrowserLocale;
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
					logger.debug("Set content locale:" + getUserContentLocale(personNodeRef));
				}

				I18NUtil.setLocale(getUserLocale(personNodeRef));
				I18NUtil.setContentLocale(getUserContentLocale(personNodeRef));
			}
		}

		try {
			super.transactionedExecute(script, scriptReq, scriptRes);

		} finally {
			I18NUtil.setContentLocale(null);
		}
	}

	private Locale getUserLocale(NodeRef personNodeRef) {
		String loc = (String) nodeService.getProperty(personNodeRef, BeCPGModel.PROP_USER_LOCALE);
		if ((loc == null) || loc.isEmpty()) {
			Locale currentLocale = Locale.getDefault();

			if (useBrowserLocale) {
				currentLocale = I18NUtil.getLocale();
			}
			if (!Locale.FRENCH.getLanguage().equals(currentLocale.getLanguage())) {
				if (Locale.US.getCountry().equals(currentLocale.getCountry())) {
					return Locale.US;
				}
				return Locale.ENGLISH;
			}
			return Locale.FRENCH;

		}
		return MLTextHelper.parseLocale(loc);
	}

	/**
	 * <p>getUserContentLocale.</p>
	 *
	 * @param personNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.Locale} object.
	 */
	public Locale getUserContentLocale(NodeRef personNodeRef) {
		String loc = (String) nodeService.getProperty(personNodeRef, BeCPGModel.PROP_USER_CONTENT_LOCAL);
		if ((loc == null) || loc.isEmpty()) {
			if (useBrowserLocale) {
				return MLTextHelper.getNearestLocale(I18NUtil.getContentLocale());
			} else {
				return MLTextHelper.getNearestLocale(Locale.getDefault());
			}
		}
		return MLTextHelper.parseLocale(loc);
	}

}
