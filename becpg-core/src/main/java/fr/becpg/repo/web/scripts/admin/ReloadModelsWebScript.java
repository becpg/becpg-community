/*
 * 
 */
package fr.becpg.repo.web.scripts.admin;

import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.dictionary.DictionaryDAO;
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

/**
 * The Class ReloadModelsWebScript.
 *
 * @author querephi
 */
public class ReloadModelsWebScript  extends AbstractWebScript
{
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ReloadModelsWebScript.class);
	
	private DictionaryDAO dictionaryDAO;
	
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}

	/* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
    {
    	logger.debug("reload models");
    	
    	dictionaryDAO.reset();
    	
    }
}
