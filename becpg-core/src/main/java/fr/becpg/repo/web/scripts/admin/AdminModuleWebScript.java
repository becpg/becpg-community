/*
 * 
 */
package fr.becpg.repo.web.scripts.admin;

import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.InitVisitor;

// TODO: Auto-generated Javadoc
/**
 * The Class AdminModuleWebScript.
 *
 * @author querephi
 */
public class AdminModuleWebScript  extends AbstractWebScript
{
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(AdminModuleWebScript.class);
	
	// request parameter names
	/** The Constant PARAM_ACTION. */
	private static final String PARAM_ACTION = "action";
	
	/** The Constant PARAM_SITE. */
	private static final String PARAM_SITE = "site";
		
	/** The Constant ACTION_INIT_REPO. */
	private static final String ACTION_INIT_REPO = "init-repo";
	
	/** The Constant ACTION_INIT_DEMO. */
	private static final String ACTION_INIT_DEMO = "init-demo";
	
	/** The init repo visitor. */
	private InitVisitor initRepoVisitor;		
	
	/** The repository. */
	private Repository repository;
	
	/** The site service. */
	private SiteService siteService;	
	
	/** The init site visitor. */
	private InitVisitor initSiteVisitor;
	
	/** The init demo visitor. */
	private InitVisitor initDemoVisitor;
					
	/**
	 * Sets the inits the repo visitor.
	 *
	 * @param initRepoVisitor the new inits the repo visitor
	 */
	public void setInitRepoVisitor(InitVisitor initRepoVisitor) {
		this.initRepoVisitor = initRepoVisitor;
	}
	
	/**
	 * Sets the repository.
	 *
	 * @param repository the new repository
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}	
	
	/**
	 * Sets the site service.
	 *
	 * @param siteService the new site service
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	/**
	 * Sets the inits the site visitor.
	 *
	 * @param initSiteVisitor the new inits the site visitor
	 */
	public void setInitSiteVisitor(InitVisitor initSiteVisitor) {
		this.initSiteVisitor = initSiteVisitor;
	}
	
	/**
	 * Sets the inits the demo visitor.
	 *
	 * @param initDemoVisitor the new inits the demo visitor
	 */
	public void setInitDemoVisitor(InitVisitor initDemoVisitor) {
		this.initDemoVisitor = initDemoVisitor;
	}
	
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
    {
    	logger.debug("start admin webscript");
    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
    	
    	String action = templateArgs.get(PARAM_ACTION);
    	String site = templateArgs.get(PARAM_SITE);
    	
    	//Check arg    	    
    	if(action == null || action.isEmpty())
    		throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'action' argument cannot be null or empty");
    	
    	//site
    	if(site != null && !site.isEmpty()){
    		    		
    		NodeRef docLibNodeRef = siteService.getContainer(site, RepoConsts.CONTAINER_DOCUMENT_LIBRARY);
    		
    		if(docLibNodeRef != null){
    			    		    	
        		if(action.equals(ACTION_INIT_REPO)){
        			logger.debug(String.format("init repository in the site ''%s", site));
        			initSiteVisitor.visitContainer(docLibNodeRef);
        		}    		
        		else{
        			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported argument 'action'. action = " + action);
        		}
    		}

    	}
    	//repository
    	else{
    		
    		if(action.equals(ACTION_INIT_REPO)){
    			logger.debug("init repository");
    			initRepoVisitor.visitContainer(repository.getCompanyHome());
    		} 
    		else if(action.equals(ACTION_INIT_DEMO)){
    			logger.debug("init demo");
    			initDemoVisitor.visitContainer(repository.getCompanyHome());
    		} 
    		else{
    			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported argument 'action'. action = " + action);
    		}    		
    	}
    	
    	
    }
}
