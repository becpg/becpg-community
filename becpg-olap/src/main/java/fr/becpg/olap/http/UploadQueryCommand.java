package fr.becpg.olap.http;

import java.io.ByteArrayInputStream;
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
import org.apache.http.entity.mime.content.StringBody;

public class UploadQueryCommand extends AbstractHttpCommand {

	private static String COMMAND_URL_TEMPLATE = "/api/upload";
	private static String FILEBODY_CHARSET = "UTF-8";
	private static String FILEBODY_MIMETYPE = "text/plain";

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
				entity.addPart("destination", new StringBody((String) params[0], FILEBODY_MIMETYPE, Charset.forName(FILEBODY_CHARSET)));
				entity.addPart("filename", new StringBody((String) params[1], Charset.forName(FILEBODY_CHARSET)));
				entity.addPart("filedata", getFileBody((String) params[1], (String) params[2]));

				if (logger.isDebugEnabled()) {
					logger.debug("Create upload post request");
					logger.debug("destination=" + (String) params[0]);
					logger.debug("filename=" + (String) params[1]);
					logger.debug("filedata=" + (String) params[2]);
				}

			} else {

				// Case update

				entity.addPart("updatenoderef", new StringBody((String) params[0], FILEBODY_MIMETYPE, Charset.forName(FILEBODY_CHARSET)));
				entity.addPart("filedata", getFileBody(null, (String) params[1]));

				if (logger.isDebugEnabled()) {
					logger.debug("Update upload post request");
					logger.debug("updatenoderef=" + (String) params[0]);
					logger.debug("filedata=" + (String) params[1]);
				}
			}
			
			logger.debug("MultipartEntity: " + entity + " length: " + entity.getContentLength());				

			postRequest.setEntity(entity);		

		} catch (UnsupportedEncodingException e) {
			logger.error(e, e);
		}
		
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
				try {
					return filecontent.getBytes(FILEBODY_CHARSET).length;
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return -1;
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
