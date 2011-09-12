/*
 * 
 */
package fr.becpg.repo.web.scripts.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.security.SecurityService;

/**
 * The Class ReloadModelsWebScript.
 *
 * @author matthieu
 */
public class ReloadAclsWebScript  extends AbstractWebScript
{
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ReloadAclsWebScript.class);
	
	private SecurityService securityService;
	

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}



	/* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
    {
    	logger.debug("reload acls");
    	
    	securityService.computeAcls();
    	
    }
}
