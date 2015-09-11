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
package fr.becpg.repo.olap;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StopWatch;

import fr.becpg.repo.olap.data.OlapContext;
import fr.becpg.repo.olap.impl.OlapServiceImpl;

public class OlapUtils {

	private static final Log logger = LogFactory.getLog(OlapServiceImpl.class);

	public static String readJsonFromUrl(String url, OlapContext olapContext) throws IOException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {
			URIBuilder builder = new URIBuilder(url);
			builder.setParameter("ticket", olapContext.getAuthToken());

			HttpGet httpget = new HttpGet(builder.build());

			try (CloseableHttpResponse response = olapContext.getSession().execute(httpget)) {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, "UTF-8");
			}
		} catch (URISyntaxException e) {
			logger.error(e, e);
			return null;
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("Retrivied JSON String from :" + url + " in " + watch.getTotalTimeSeconds() + " seconds");
			}

		}
	}

	public static void sendCreateQueryPostRequest(OlapContext olapContext, String postUrl, String xml) throws IOException {

		if (logger.isDebugEnabled()) {
			logger.debug("Send POST request:\n" + xml + "\n to " + postUrl);
		}
		try {
			URIBuilder builder = new URIBuilder(postUrl);
			builder.setParameter("ticket", olapContext.getAuthToken());

			HttpPost httpPost = new HttpPost(builder.build());
			HttpEntity entity = new StringEntity("xml=" + xml, "UTF-8");

			httpPost.setEntity(entity);
			try (CloseableHttpResponse response = olapContext.getSession().execute(httpPost)) {
				// keep that as we should read the response
				entity = response.getEntity();

				String ret = EntityUtils.toString(entity);

				if (logger.isDebugEnabled()) {
					logger.debug("Ret: " + ret);
				}
			}
		} catch (URISyntaxException e) {
			logger.error(e, e);
		}

	}

	// TODO crappy !!!
	public static Object convert(String value) {
		if (value == null || value.isEmpty()) {
			return new Long(0);
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			try {
				return Double.parseDouble(value.replace(",", "."));
			} catch (NumberFormatException ignored) {
			}
		}
		return value;
	}

}
