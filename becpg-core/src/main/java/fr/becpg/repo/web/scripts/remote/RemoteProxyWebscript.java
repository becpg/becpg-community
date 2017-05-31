/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Act has a remote proxy for webscript
 * @author matthieu
 *
 */
public class RemoteProxyWebscript extends AbstractWebScript {


	private static final Log logger = LogFactory.getLog(RemoteProxyWebscript.class);
	
	private static final String REMOTE_URL_PARAM = "remoteUrl";

	private String remoteServer;

	private String remoteUser;

	private char[] remotePwd;
	
	public void setRemoteServer(String remoteServer) {
		this.remoteServer = remoteServer;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public void setRemotePwd(char[] remotePwd) {
		if(remotePwd!=null){
			this.remotePwd = Arrays.copyOf(remotePwd, remotePwd.length);
		}
	}

	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		
		
		String remoteUrl = remoteServer+"/service/"+templateArgs.get(REMOTE_URL_PARAM);
		boolean first = true;
		for(String name : req.getParameterNames()){
			//ISSUE do not forward alf_ticket
			if(!"alf_ticket".equals(name)){
				remoteUrl+=(first?"?":"&")+name+"="+URLEncoder.encode(req.getParameter(name),"UTF-8");
				first = false;
			}
		}
		
		logger.debug("Forward request to :"+remoteUrl);
		
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(remoteUser, remotePwd);
			}
		});

		try {
			URL entityUrl = new URL(remoteUrl);
			InputStream input = null;
		
			try {
				input = entityUrl.openStream();
				IOUtils.copy(input, resp.getOutputStream());

			} finally {
				IOUtils.closeQuietly(input);
			}
		} catch (MalformedURLException e) {
			throw new WebScriptException(e.getMessage());
		}
		
	}
}
