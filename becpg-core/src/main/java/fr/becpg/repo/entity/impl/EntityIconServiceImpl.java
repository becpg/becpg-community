package fr.becpg.repo.entity.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.EntityIconService;
import fr.becpg.repo.entity.EntityService;

@Service("entityIconService")
public class EntityIconServiceImpl implements EntityIconService {

	@Autowired
	private EntityService entityService;

	@Autowired
	private ContentService contentService;

	private static final String DATA_IMAGE_PREFIX = "data:image/";
	private static final String DATA_BASE64_ENCODING = ";base64,";
	private static final String URL_OPEN_TARGET_PATTERN = "url('";
	private static final String URL_CLOSE_TARGET_PATTERN = "')";
	private static final String OPEN_CURLY_BRACKET = "{";
	private static final String CLOSE_CURLY_BRACKET = "}";
	private static final String DOUBLE_DASH = "--";
	private static final String COLON = ":";
	private static final String DOT = ".";

	@Override
	public void writeIconCSS(OutputStream out) {

		StringBuilder builder = new StringBuilder();
		
		for (Map.Entry<String, NodeRef> icon : entityService.getEntityIcons().entrySet()) {
			String name = icon.getKey();
			NodeRef iconNodeRef = icon.getValue();

			if (name.matches("generic-.*\\.png")) {
				ContentReader reader = contentService.getReader(iconNodeRef, ContentModel.PROP_CONTENT);
				if ((reader == null) || !reader.exists()) {
					throw beCPGException("Unable to locate content for node ref " + iconNodeRef, null);
				}

				try {
					String encodedImage = encodeImage(reader.getContentInputStream());
					String cssClassName = extractCSSClassName(name);
					String cssVarName = cssClassName.indexOf('-') != -1 ? cssClassName.substring(0, cssClassName.indexOf('-')): cssClassName;
					builder.append("span" + DOT + cssClassName + OPEN_CURLY_BRACKET + DOUBLE_DASH + cssVarName + "-icon" + COLON + URL_OPEN_TARGET_PATTERN 
					+ DATA_IMAGE_PREFIX + reader.getMimetype() + DATA_BASE64_ENCODING + encodedImage + URL_CLOSE_TARGET_PATTERN + ";" + CLOSE_CURLY_BRACKET);
				} catch (IOException e) {
					throw beCPGException("Failed to encode image for node ref " + iconNodeRef, e);
				}
			}
		}

		try {
			out.write(builder.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw beCPGException("Failed to write CSS to output stream.", e);
		}
	}

	public String extractCSSClassName(String fileName) {
		String withoutPrefix = fileName.replace("generic-", "");
		return withoutPrefix.lastIndexOf('-') != -1 ? withoutPrefix.substring(0, withoutPrefix.lastIndexOf('-')): withoutPrefix;
	}

	public static String encodeImage(InputStream inputStream) throws IOException {

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			byte[] buffer = new byte[1024];
			int bytesRead;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			byte[] bytes = outputStream.toByteArray();

			return Base64.getEncoder().encodeToString(bytes);
		}
	}

	public BeCPGException beCPGException(String message, Exception cause) {
		return cause != null ? new BeCPGException(message, cause) : new BeCPGException(message);
	}
}