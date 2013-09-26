package fr.becpg.repo.importer.impl;

import java.io.Serializable;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.CommentsService;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImporterException;

/**
 * 
 * @author matthieu
 * Used to add comments to existing products
 * Usage : 
 *  IMPORT_TYPE	Comments	
 *	TYPE	bcpg:finishedProduct	
 *	COLUMNS	bcpg:code	bcpg:productComments
 * 
 */
public class ImportCommentsVisitor  extends AbstractImportVisitor {

	private static Log logger = LogFactory.getLog(ImportCommentsVisitor.class);
	
	private CommentsService commentsService;
	
	
	
	public void setCommentsService(CommentsService commentsService) {
		this.commentsService = commentsService;
	}



	@Override
	public NodeRef importNode(ImportContext importContext, List<String> values) throws ParseException, ImporterException {
	
		
		
		// import properties
		Map<QName, Serializable> properties = getNodePropertiesToImport(importContext, values);


		NodeRef nodeRef = findNode(importContext, importContext.getType(), properties);
		
		if(nodeRef !=null){
			String comment = (String) properties.get(BeCPGModel.PROP_PRODUCT_COMMENTS);
			logger.debug("Import comments :"+comment +" for product :"+nodeRef);
			if(comment!=null){
				commentsService.createComment(nodeRef, "", comment, false);
			}
		} else {
			logger.info("Cannot add comments to new node");
		}

		return nodeRef;
	}
}
