/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.importer.impl;

import java.io.Serializable;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImporterException;
import org.alfresco.repo.forum.CommentService;

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
	
	private CommentService commentService;
	

	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}


	@Override
	public NodeRef importNode(ImportContext importContext, List<String> values) throws ParseException, ImporterException {
	
		
		
		// import properties
		Map<QName, Serializable> properties = getNodePropertiesToImport(importContext, values);


		NodeRef nodeRef = findNode(importContext, importContext.getType(), properties);
		
		if(nodeRef !=null){
			String comment = (String) properties.get(PLMModel.PROP_PRODUCT_COMMENTS);
			logger.debug("Import comments :"+comment +" for product :"+nodeRef);
			if(comment!=null){
				commentService.createComment(nodeRef, "", comment, false);
			}
		} else {
			logger.info("Cannot add comments to new node");
		}

		return nodeRef;
	}
}
