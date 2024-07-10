/*
 * 
 */
package fr.becpg.repo.designer.web.scripts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;


/**
 * The Class PublishWebScript.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PublishWebScript extends AbstractWebScript  {
	

	private static final String PARAM_NODEREF = "nodeRef";

	
	/** The logger. */
	private static final Log logger = LogFactory.getLog(PublishWebScript.class);
	
	/** The node service. */
	private DesignerService designerService;
	
	private ContentService contentService;
	
	private NodeService nodeService;
	

	/**
	 * <p>Setter for the field <code>designerService</code>.</p>
	 *
	 * @param designerService the designerService to set
	 */
	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	/**
	 * {@inheritDoc}
	 *
	 * Publish
	 *
	 * url : /becpg/designer/model/publish?nodeRef={nodeRef}.
	 * url : /becpg/designer/form/publish?nodeRef={nodeRef}.
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		logger.debug("PublishWebScript executeImpl()");
			
		NodeRef parentNodeRef = new NodeRef(req.getParameter(PARAM_NODEREF));
		
		if (Boolean.parseBoolean(req.getParameter("writeXml"))) {
			designerService.writeXml(parentNodeRef);
		}
		
		ContentReader reader = contentService.getReader(parentNodeRef, ContentModel.PROP_CONTENT);
		
		String content = IOUtils.toString(reader.getContentInputStream(), StandardCharsets.UTF_8);
		
		JSONObject jsonResponse = new JSONObject();
		
		jsonResponse.put("content", content);
		jsonResponse.put("modifiedDate", ((Date) nodeService.getProperty(parentNodeRef, ContentModel.PROP_MODIFIED)).getTime());
		
		String publishedConfigName = (String) nodeService.getProperty(parentNodeRef, DesignerModel.PROP_PUBLISHED_CONFIG_NAME);
		
		if (publishedConfigName != null) {
			jsonResponse.put("publishedConfigName", publishedConfigName);
		} else {
			jsonResponse.put("publishedConfigName", nodeService.getProperty(parentNodeRef, ContentModel.PROP_NAME));
		}
		
		designerService.publish(parentNodeRef);
		
		if (nodeService.hasAspect(parentNodeRef, DesignerModel.ASPECT_MODEL)) {
			jsonResponse.put("type", "model");
		} else if (nodeService.hasAspect(parentNodeRef, DesignerModel.ASPECT_CONFIG)) {
			jsonResponse.put("type", "config");
		}
		
		try (InputStream in = new ByteArrayInputStream(jsonResponse.toString().getBytes())){
			IOUtils.copy(in, res.getOutputStream());
		} catch (IOException e) {
			logger.error(e, e);
		}
	}
}
