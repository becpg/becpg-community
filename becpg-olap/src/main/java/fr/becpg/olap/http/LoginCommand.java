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
package fr.becpg.olap.http;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import fr.becpg.tools.http.AbstractHttpCommand;


public class LoginCommand  extends AbstractHttpCommand {


	private static final String COMMAND_URL_TEMPLATE = "/api/login?u=%s&pw=%s";

	
	public LoginCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, encodeParams(params) );
	}
	
	public String getAlfTicket(String login, String password) throws UsernameNotFoundException{
		DefaultHttpClient httpclient = new DefaultHttpClient();

		
		try(InputStream in  = runCommand(httpclient,login, password )){
			
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(in);
	
			NodeList nodes = doc.getElementsByTagName("ticket");
			if(nodes!=null && nodes.getLength()>0){
				return nodes.item(0).getTextContent();
			}
			
		} catch (Exception e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
		return null;
		
	}
	
	
}
