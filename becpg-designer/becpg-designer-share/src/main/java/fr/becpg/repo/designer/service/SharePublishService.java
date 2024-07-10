package fr.becpg.repo.designer.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;

public class SharePublishService implements ApplicationListener<ContextRefreshedEvent>  {

	private static final String XML = ".xml";
	private static final String PUBLISHED_CONFIG_NAME = "publishedConfigName";
	private static final String PROPERTIES = ".properties";
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	private static final Log logger = LogFactory.getLog(SharePublishService.class);

	private String configPath;

	private ConfigService configService;
	
	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		deleteClasspathMessages();
		loadDiskMessages();
	}

	private void loadDiskMessages() {
		File configDir = new File(configPath + FILE_SEPARATOR + "messages");
		if (configDir.exists()) {
			for (File configFile : configDir.listFiles()) {
				if (configFile.isFile() && configFile.getName().endsWith(PROPERTIES)) {
					try {
						loadMessages(configFile.getName(), Files.readString(configFile.toPath()), new Date().getTime());
					} catch (IOException e) {
						logger.error("Error while reading properties file: " + configFile.getName());
					}
				}
			}
		}
	}

	private void deleteClasspathMessages() {
		String pathName = System.getProperty("catalina.base") + "/shared/classes/alfresco/messages/custom";
		File messageDir = new File(pathName);
		if (!messageDir.exists()) {
			messageDir.mkdirs();
		}
		for (File messageFile : messageDir.listFiles()) {
			if (messageFile.isFile() && messageFile.getName().endsWith(PROPERTIES)) {
				messageFile.delete();
			}
		}
	}

	public void publishDocument(String nodeRef, String fileName, Boolean writeXml) {
		JSONObject jsonResponse = alfrescoRequest(HttpMethod.POST, "/becpg/designer/model/publish?nodeRef=" + nodeRef + "&writeXml=" + writeXml);
		if (jsonResponse.has("type") && jsonResponse.getString("type").equals("config")) {
			if (fileName.endsWith(XML)) {
				if (jsonResponse.has(PUBLISHED_CONFIG_NAME)) {
					deleteFileOnDisk(configPath, jsonResponse.getString(PUBLISHED_CONFIG_NAME));
				}
				writeFileOnDisk(configPath, fileName, jsonResponse.getString("content"));
			} else if (fileName.endsWith(PROPERTIES)) {
				writeFileOnDisk(configPath + FILE_SEPARATOR + "messages", fileName, jsonResponse.getString("content"));
				loadMessages(fileName, jsonResponse.getString("content"), jsonResponse.getLong("modifiedDate"));
			}
		}
	}

	public void unpublishDocument(String nodeRef, String fileName) {
		JSONObject jsonResponse = alfrescoRequest(HttpMethod.POST, "/becpg/designer/model/unpublish?nodeRef=" + nodeRef);
		if (jsonResponse.has("type") && jsonResponse.getString("type").equals("config")) {
			if (fileName.endsWith(XML)) {
				deleteFileOnDisk(configPath, fileName);
			} else if (fileName.endsWith(PROPERTIES)) {
				deleteFileOnDisk(configPath + FILE_SEPARATOR + "messages", fileName);
			}
		}
	}

	public void cleanConfig() {
		cleanConfigForPath(configPath);
		cleanConfigForPath(configPath + FILE_SEPARATOR + "messages");
		configService.reset();
	}

	@SuppressWarnings("unchecked")
	private void cleanConfigForPath(String path) {
		File configDir = new File(path);
		if (configDir.exists()) {
			File[] shareConfigFiles = configDir.listFiles();
			if (shareConfigFiles.length > 0) {
				JSONObject jsonResponse = alfrescoRequest(HttpMethod.GET, "/becpg/designer/config/list");
				JSONArray alfrescoConfigFiles = jsonResponse.getJSONArray("items");
				for (File shareConfigFile : shareConfigFiles) {
					if (shareConfigFile.isFile()) {
						String shareConfigFileName = shareConfigFile.getName();
						if (alfrescoConfigFiles.toList().stream().noneMatch(i -> ((Map<String, Object>) i).get("displayName").equals(shareConfigFileName))) {
							deleteFileOnDisk(path, shareConfigFileName);
						}
					}
				}
			}
		}
	}

	private void deleteFileOnDisk(String path, String fileName) {
		File configDir = new File(path);
		if (configDir.exists()) {
			path = path + FILE_SEPARATOR + fileName;
			File file = new File(path);
			logger.debug("Deleting file at path " + path + ", exists ? " + file.exists());
			if (file.exists() && !file.delete()) {
				logger.error("Cannot delete file: " + file.getName());
			}
		}
	}

	private JSONObject alfrescoRequest(HttpMethod method, String url) {
		try {
			RequestContext rc = ThreadLocalRequestContext.getRequestContext();
			Connector conn = rc.getServiceRegistry().getConnectorService().getConnector("alfresco", rc.getUserId(), ServletUtil.getSession());
			ConnectorContext ctx = new ConnectorContext();
			ctx.setMethod(method);
			Response response = conn.call(url, ctx);
			if (response.getStatus().getCode() == Status.STATUS_OK) {
				return new JSONObject(response.getResponse());
			}
			throw new WebScriptException("Response status is not OK for url: " + url);
		} catch (ConnectorServiceException e) {
			logger.error(e.getMessage(), e);
			throw new WebScriptException(e.getMessage());
		}
	}

	private void writeFileOnDisk(String path, String fileName, String content) {
		File configDir = new File(path);
		if (!configDir.exists()) {
			configDir.mkdirs();
		}
		path = path + FILE_SEPARATOR + fileName;
		logger.debug("Publish config under " + path);
		try {
			File file = new File(path);
			if (file.exists() || file.createNewFile()) {
				try (InputStream in = new ByteArrayInputStream(content.getBytes());
						OutputStream out = new FileOutputStream(file)) {
					IOUtils.copy(in, out);
				}
			}
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	public void loadMessages(String fileName, String content, Long modifiedDate) {
		String fileBaseName = fileName.split("_")[0].replace(PROPERTIES, "");
		String locale = fileName.contains("_") ? fileName.split("_")[1].replace(PROPERTIES, "") : "";
		String bundleName = fileBaseName + "-" + modifiedDate;
		try {
			String bundleClasspathName = "alfresco.messages.custom." + bundleName;
			if (locale.isEmpty() || locale.equalsIgnoreCase(Locale.getDefault().getCountry())) {
				List<String> uiLocales = configService.getConfig("Languages").getConfigElement("ui-languages").getChildren().stream()
						.map(c -> c.getAttribute("locale")).toList();
				for (String uiLocale : uiLocales) {
					writeMessageDiskFile("", bundleName, uiLocale);
				}
			}
			writeMessageDiskFile("", bundleName, "");
			writeMessageDiskFile(content, bundleName, locale);
			I18NUtil.getAllMessages();
			I18NUtil.getAllMessages(Locale.FRENCH);
			I18NUtil.getAllMessages(Locale.ENGLISH);
			I18NUtil.registerResourceBundle(bundleClasspathName);
			I18NUtil.getAllMessages();
			I18NUtil.getAllMessages(Locale.FRENCH);
			I18NUtil.getAllMessages(Locale.ENGLISH);
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	private void writeMessageDiskFile(String contentString, String fileName, String locale) throws IOException {
		String pathName = System.getProperty("catalina.base") + "/shared/classes/alfresco/messages/custom";
		File messageDir = new File(pathName);
		if (!messageDir.exists()) {
			messageDir.mkdirs();
		}
		String localeSuffix = locale.isBlank() ? "" : "_" + locale;
		File file = new File(pathName + FILE_SEPARATOR + fileName + localeSuffix + PROPERTIES);
		if (file.exists() || file.createNewFile()) {
			try (InputStream in = new ByteArrayInputStream(contentString.getBytes()); OutputStream out = new FileOutputStream(file)) {
				IOUtils.copy(in, out);
			}
		}
	}

}
