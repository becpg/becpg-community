package fr.becpg.repo.template;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;

/**
 * <p>BeCPGTemplateExtension class.</p>
 *
 * @author matthieu
 */
public class BeCPGTemplateExtension extends BaseTemplateProcessorExtension {

    private AuthorityService authorityService;
    private MutableAuthenticationService authenticationService;
    private NodeService nodeService;
    private PersonService personService;
    
	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * <p>Setter for the field <code>authenticationService</code>.</p>
	 *
	 * @param authenticationService a {@link org.alfresco.service.cmr.security.MutableAuthenticationService} object
	 */
	public void setAuthenticationService(MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>personService</code>.</p>
	 *
	 * @param personService a {@link org.alfresco.service.cmr.security.PersonService} object
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * <p>isAccountEnabled.</p>
	 *
	 * @param person a {@link org.alfresco.repo.template.TemplateNode} object
	 * @return a boolean
	 */
	@SuppressWarnings("unlikely-arg-type")
	public boolean isAccountEnabled(TemplateNode person) {
		// Only admins have rights to check authentication enablement
		if (this.authorityService.isAdminAuthority(AuthenticationUtil.getFullyAuthenticatedUser())) {
			String userName = (String) person.getProperties().get(ContentModel.PROP_USERNAME);
			if (!authenticationService.isAuthenticationMutable(userName)
					&& nodeService.hasAspect(personService.getPerson(userName), ContentModel.ASPECT_PERSON_DISABLED)) {
				return false;
			}
			return this.authenticationService.getAuthenticationEnabled(userName);
		}
		return true;
	}
}
