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
package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Return OK if entity exist or KO
 * 
 * @author matthieu
 * 
 */
public class CheckEntityWebScript extends AbstractEntityWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		String nodeRef = req.getParameter(PARAM_NODEREF);
		if (nodeRef != null && nodeRef.length() > 0) {
			NodeRef node = new NodeRef(nodeRef);
			if (nodeService.exists(node)) {
				resp.getWriter().write("OK");
				return;
			}
		}

		resp.getWriter().write("KO");

	}

}
