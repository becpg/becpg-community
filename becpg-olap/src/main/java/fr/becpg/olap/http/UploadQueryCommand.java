package fr.becpg.olap.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

public class UploadQueryCommand extends AbstractHttpCommand {

	private static String COMMAND_URL_TEMPLATE = "/api/upload";

	public UploadQueryCommand(String serverUrl) {
		super(serverUrl);
		setHttpMethod(HttpCommandMethod.METHOD_POST);
	}

	@Override
	protected HttpUriRequest buildHttpPost(String url, final Object[] params) {

		HttpPost postRequest = new HttpPost(url);

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			if (params.length > 2) {
				// Case create
				entity.addPart("destination", new StringBody((String) params[0], "text/plain", Charset.forName("UTF-8")));
				entity.addPart("filename", new StringBody((String) params[1], Charset.forName("UTF-8")));
				entity.addPart("filedata", getFileBody((String) params[1], (String) params[2]));

				if (logger.isDebugEnabled()) {
					logger.debug("Create upload post request");
					logger.debug("destination=" + (String) params[0]);
					logger.debug("filename=" + (String) params[1]);
					logger.debug("filedata=" + (String) params[2]);
				}

			} else {

				// Case update

				entity.addPart("updatenoderef", new StringBody((String) params[0], "text/plain", Charset.forName("UTF-8")));
				entity.addPart("filedata", getFileBody(null, (String) params[1]));

				if (logger.isDebugEnabled()) {
					logger.debug("Update upload post request");
					logger.debug("updatenoderef=" + (String) params[0]);
					logger.debug("filedata=" + (String) params[1]);
				}
			}

			postRequest.setEntity(entity);

		} catch (UnsupportedEncodingException e) {
			logger.error(e, e);
		}
		return postRequest;
	}

	private ContentBody getFileBody(String filename, final String filecontent) {
		File temp = null;

		try {
			temp = File.createTempFile("temp", ".saiku");

			return new FileBody(temp, filename, "text/plain", "UTF-8") {

				@Override
				public InputStream getInputStream() throws IOException {
					return new ByteArrayInputStream(filecontent.getBytes("UTF-8"));
				}

				@Override
				public void writeTo(OutputStream out) throws IOException {
					if (out == null) {
						throw new IllegalArgumentException("Output stream may not be null");
					}
					InputStream in = getInputStream();
					try {
						byte[] tmp = new byte[4096];
						int l;
						while ((l = in.read(tmp)) != -1) {
							out.write(tmp, 0, l);
						}
						out.flush();
					} finally {
						in.close();
					}
				}

				@Override
				public long getContentLength() {
					return filecontent.length();
				}

			};
		} catch (IOException e) {
			logger.error(e, e);
		} finally {
			temp.delete();
		}

		return null;
	}

	@Override
	public String getHttpUrl(Object... params) {

		return getServerUrl() + COMMAND_URL_TEMPLATE;
	}

}
