/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
package fr.becpg.repo.designer.web.scripts;

import java.io.IOException;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.designer.service.SharePublishService;

/**
 * Publish config files in share
 *
 * @author "Valentin Leblanc"
 * @version $Id: $Id
 */
public class ShareUnpublishWebscript extends AbstractWebScript {

	private SharePublishService sharePublishService;
	
	public void setSharePublishService(SharePublishService sharePublishService) {
		this.sharePublishService = sharePublishService;
	}
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String nodeRef = req.getParameter("nodeRef");
		String fileName = req.getParameter("fileName");
		
		sharePublishService.unpublishDocument(nodeRef, fileName);
	}

}
