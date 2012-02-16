/*
 * 
 */
package fr.becpg.repo.web.scripts.admin;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

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
