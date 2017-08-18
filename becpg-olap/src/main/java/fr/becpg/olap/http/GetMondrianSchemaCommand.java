/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import fr.becpg.tools.http.AbstractHttpCommand;

public class GetMondrianSchemaCommand extends AbstractHttpCommand {

	private static final String COMMAND_URL_TEMPLATE = "/becpg/olap/schema?instance=%s&alf_ticket=%s";

	public GetMondrianSchemaCommand(String serverUr) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {

		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, encodeParams(params));
	}

	public String getSchema(CloseableHttpClient httpClient, HttpContext httpContext, Object... params) throws IOException {

		try (CloseableHttpResponse resp = runCommand(httpClient, httpContext, params)) {
			HttpEntity entity = resp.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			}

		}

		return null;
	}

}
