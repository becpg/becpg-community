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

/**
 * <p>EntityIconServiceImpl class.</p>
 *
 * @author matthieu
 */
@Service("entityIconService")
public class EntityIconServiceImpl implements EntityIconService {

	@Autowired
	private EntityService entityService;

	@Autowired
	private ContentService contentService;

	private static final String DATA_IMAGE_PREFIX = "data:";
	private static final String DATA_BASE64_ENCODING = ";base64,";
	private static final String URL_OPEN_TARGET_PATTERN = "url('";
	private static final String URL_CLOSE_TARGET_PATTERN = "')";
	private static final String OPEN_CURLY_BRACKET = "{";
	private static final String CLOSE_CURLY_BRACKET = "}";
	private static final String DOUBLE_DASH = "--";
	private static final String COLON = ":";

	/** {@inheritDoc} */
	@Override
	public void writeIconCSS(OutputStream out) {

		StringBuilder builder = new StringBuilder();
		
		for (Map.Entry<String, NodeRef> icon : entityService.getEntityIcons().entrySet()) {
			String name = icon.getKey();
			NodeRef iconNodeRef = icon.getValue();

			if (name.matches("generic-.*-16\\.png") || name.matches("generic-.*-24\\.png")
					|| name.matches("generic-.*-32\\.png") || name.matches("generic-.*-48\\.png")
					|| name.matches("generic-.*-64\\.png")) {
				ContentReader reader = contentService.getReader(iconNodeRef, ContentModel.PROP_CONTENT);
				if ((reader == null) || !reader.exists()) {
					throw beCPGException("Unable to locate content for node ref " + iconNodeRef, null);
				}

				try {
			        String[] parts = name.split("-");
			        
			        String type = parts[1];
			        String resolution = parts[parts.length - 1].split("\\.")[0];
					
					String encodedImage = encodeImage(reader.getContentInputStream());
					String cssClassName = extractCSSClassName(name);
					String cssVarName = "icon-"+type+"-"+resolution ;
					
					// Original CSS class rule for entity and span elements
					builder.append(".entity."+cssClassName+",span."  + cssClassName + OPEN_CURLY_BRACKET + DOUBLE_DASH + cssVarName + COLON + URL_OPEN_TARGET_PATTERN 
					+ DATA_IMAGE_PREFIX + reader.getMimetype() + DATA_BASE64_ENCODING + encodedImage + URL_CLOSE_TARGET_PATTERN + ";" + CLOSE_CURLY_BRACKET);
					
					if(parts.length == 3) {
						builder.append("img[src*=\"-"+type+"-"+resolution+".png\"]" + OPEN_CURLY_BRACKET
							+ DOUBLE_DASH + cssVarName + COLON + URL_OPEN_TARGET_PATTERN + DATA_IMAGE_PREFIX + reader.getMimetype() + DATA_BASE64_ENCODING + encodedImage + URL_CLOSE_TARGET_PATTERN + ";"
							+ "content: var(" + DOUBLE_DASH + cssVarName + ");"
							+ "background-image: var(" + DOUBLE_DASH + cssVarName + ");"
							+ "background-size: contain;"
							+ "background-repeat: no-repeat;"
							+ "width: "+resolution+"px;"
							+ "height: "+resolution+"px;"
							+ CLOSE_CURLY_BRACKET);
						
						if("16".equals(resolution)) {
							builder.append("span."+type+"-file" + OPEN_CURLY_BRACKET
							    + DOUBLE_DASH + cssVarName + COLON + URL_OPEN_TARGET_PATTERN + DATA_IMAGE_PREFIX + reader.getMimetype() + DATA_BASE64_ENCODING + encodedImage + URL_CLOSE_TARGET_PATTERN + ";"
								+ "background-image: var(" + DOUBLE_DASH + cssVarName + ") !important;"
								+ CLOSE_CURLY_BRACKET);
						}
					}
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
	

	/**
	 * <p>extractCSSClassName.</p>
	 *
	 * @param fileName a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String extractCSSClassName(String fileName) {
		String withoutPrefix = fileName.replace("generic-", "");
		return withoutPrefix.lastIndexOf('-') != -1 ? withoutPrefix.substring(0, withoutPrefix.lastIndexOf('-')): withoutPrefix;
	}

	/**
	 * <p>encodeImage.</p>
	 *
	 * @param inputStream a {@link java.io.InputStream} object
	 * @return a {@link java.lang.String} object
	 * @throws java.io.IOException if any.
	 */
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

	/**
	 * <p>beCPGException.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 * @param cause a {@link java.lang.Exception} object
	 * @return a {@link fr.becpg.common.BeCPGException} object
	 */
	public BeCPGException beCPGException(String message, Exception cause) {
		return cause != null ? new BeCPGException(message, cause) : new BeCPGException(message);
	}
}
