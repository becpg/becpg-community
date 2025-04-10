package fr.becpg.repo.template;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;

public class BeCPGTemplateExtension extends BaseTemplateProcessorExtension {

    private AuthorityService authorityService;
    private MutableAuthenticationService authenticationService;
    private NodeService nodeService;
    private PersonService personService;
    
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setAuthenticationService(MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

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
