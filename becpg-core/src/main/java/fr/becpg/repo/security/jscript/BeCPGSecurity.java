package fr.becpg.repo.security.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.security.SecurityService;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class BeCPGSecurity extends BaseScopableProcessorExtension{

	private static Log logger = LogFactory.getLog(BeCPGSecurity.class);
	
    protected ServiceRegistry services;
    
	private SecurityService securityService;
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
		
	/**
	 * 
	 * @param entityNode
	 * @param dataListType
	 * @return
	 */
	public boolean hasWriteAccess(ScriptNode entityNode,String dataListType){
		logger.debug(" jscript - hasWriteAccess");
		return securityService.computeAccessMode(services.getNodeService().getType(entityNode.getNodeRef()), dataListType) 
				== SecurityService.WRITE_ACCESS;
		
	}
	

}
