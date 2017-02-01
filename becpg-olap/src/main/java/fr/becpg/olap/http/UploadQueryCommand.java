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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import fr.becpg.tools.http.AbstractHttpCommand;

public class UploadQueryCommand extends AbstractHttpCommand {

	private static final String COMMAND_URL_TEMPLATE = "/api/upload";
	private static final String FILEBODY_CHARSET = "UTF-8";
	
	public UploadQueryCommand(String serverUrl) {
		super(serverUrl);
		setHttpMethod(HttpCommandMethod.METHOD_POST);
	}

	@Override
	protected HttpUriRequest buildHttpPost(String url, final Object[] params) {

		HttpPost postRequest = new HttpPost(url);

		MultipartEntityBuilder entity = MultipartEntityBuilder.create();
		entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.setCharset(Charset.forName(FILEBODY_CHARSET));

		if (params.length > 2) {
			// Case create
			entity.addPart("destination", new StringBody((String) params[0], ContentType.MULTIPART_FORM_DATA));
			entity.addPart("filename", new StringBody((String) params[1], ContentType.MULTIPART_FORM_DATA));
			entity.addPart("filedata", getFileBody((String) params[1], (String) params[2]));

			if (logger.isDebugEnabled()) {
				logger.debug("Create upload post request");
				logger.debug("destination=" + params[0]);
				logger.debug("filename=" + params[1]);
				logger.debug("filedata=" + params[2]);
			}

		} else {

			// Case update

			entity.addPart("updatenoderef", new StringBody((String) params[0], ContentType.MULTIPART_FORM_DATA));
			entity.addPart("filedata", getFileBody(null, (String) params[1]));

			if (logger.isDebugEnabled()) {
				logger.debug("Update upload post request");
				logger.debug("updatenoderef=" + params[0]);
				logger.debug("filedata=" + params[1]);
			}
		}

		postRequest.setEntity(entity.build());

		return postRequest;
	}

	private ContentBody getFileBody(final String filename, final String filecontent) {

		return new ContentBody() {

			@Override
			public String getTransferEncoding() {
				return FILEBODY_CHARSET;
			}

			@Override
			public String getCharset() {
				return FILEBODY_CHARSET;
			}

			@Override
			public String getSubType() {
				return "octet-stream";
			}

			@Override
			public String getMimeType() {
				return "application/octet-stream";
			}

			@Override
			public String getMediaType() {
				return "application";
			}

			private InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(filecontent.getBytes(FILEBODY_CHARSET));
			}

			@Override
			public void writeTo(OutputStream out) throws IOException {
				if (out == null) {
					throw new IllegalArgumentException("Output stream may not be null");
				}
				try (InputStream in = getInputStream()) {
					byte[] tmp = new byte[4096];
					int l;
					while ((l = in.read(tmp)) != -1) {
						out.write(tmp, 0, l);
					}
					out.flush();
				}
			}

			@Override
			public long getContentLength() {
				try {
					return filecontent.getBytes(FILEBODY_CHARSET).length;
				} catch (UnsupportedEncodingException e) {
					throw new IllegalArgumentException("Unsupported stream encoding",e);
				}
			}

			@Override
			public String getFilename() {
				return filename;
			}
		};

	}

	@Override
	public String getHttpUrl(Object... params) {

		return getServerUrl() + COMMAND_URL_TEMPLATE;
	}

}
