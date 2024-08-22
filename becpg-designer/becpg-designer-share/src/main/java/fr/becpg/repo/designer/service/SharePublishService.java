package fr.becpg.repo.designer.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
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
			List<File> files = new ArrayList<>(Arrays.asList(configDir.listFiles()));
			List<File> rootFiles = files.stream().filter(f -> !f.getName().contains("_")).toList();
			
			for (File rootFile : rootFiles) {
				try {
					Long bundleId = new Date().getTime();
					logger.info("Write classpath messages for " + rootFile.getName());
					writeClassPathMessages(rootFile.getName(), Files.readString(rootFile.toPath()), bundleId);
					files.remove(rootFile);
					String baseName = rootFile.getName().replace(PROPERTIES, "");
					String bundleName = baseName + "-" + bundleId;
					String defaultLocaleFileName = baseName + "_" + Locale.getDefault().getCountry() + PROPERTIES;
					File defaultLocaleFile = files.stream().filter(f -> f.getName().equalsIgnoreCase(defaultLocaleFileName)).findFirst().orElse(null);
					if (defaultLocaleFile != null) {
						logger.info("Write classpath messages for " + defaultLocaleFile.getName());
						writeClassPathMessages(defaultLocaleFile.getName(), Files.readString(defaultLocaleFile.toPath()), bundleId);
						files.remove(defaultLocaleFile);
					}
					List<File> otherFiles = files.stream().filter(f -> f.getName().contains("_") && f.getName().split("_")[0].equals(baseName)).toList();
					for (File otherFile : otherFiles) {
						logger.info("Write classpath messages for " + otherFile.getName());
						writeClassPathMessages(otherFile.getName(), Files.readString(otherFile.toPath()), bundleId);
						files.remove(otherFile);
					}
					I18NUtil.registerResourceBundle("alfresco.messages.custom." + bundleName);
				} catch (IOException e) {
					logger.error("Error while reading properties file: " + rootFile.getName());
				}
			}
			for (File file : files) {
				Long bundleId = new Date().getTime();
				try {
					logger.info("Write classpath messages for " + file.getName());
					writeClassPathMessages(file.getName(), Files.readString(file.toPath()), bundleId);
				} catch (IOException e) {
					logger.error("Error while reading properties file: " + file.getName());
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
		if (fileName.endsWith(XML)) {
			publishXml(nodeRef, fileName, writeXml);
		} else if (fileName.endsWith(PROPERTIES)) {
			String bundleClasspathName = publishProperties(nodeRef, fileName, null);
			if (bundleClasspathName != null) {
				I18NUtil.getAllMessages();
				I18NUtil.getAllMessages(Locale.FRENCH);
				I18NUtil.getAllMessages(Locale.ENGLISH);
				if (logger.isDebugEnabled()) {
					logger.debug("Register bundle: " + bundleClasspathName);
				}
				I18NUtil.registerResourceBundle(bundleClasspathName);
				I18NUtil.getAllMessages();
				I18NUtil.getAllMessages(Locale.FRENCH);
				I18NUtil.getAllMessages(Locale.ENGLISH);
			}
		}
	}

	private String publishProperties(String nodeRef, String fileName, Long bundleId) {
		JSONObject jsonResponse = alfrescoRequest(HttpMethod.POST, "/becpg/designer/model/publish?nodeRef=" + nodeRef + "&writeXml=false");
		if (jsonResponse.has("type") && jsonResponse.getString("type").equals("config")) {
			if (bundleId == null) {
				bundleId = jsonResponse.getLong("modifiedDate");
			}
			JSONObject alfrescoConfigList = alfrescoRequest(HttpMethod.GET, "/becpg/designer/config/list");
			JSONArray alfrescoConfigFiles = alfrescoConfigList.getJSONArray("items");
			writeFileOnDisk(configPath + FILE_SEPARATOR + "messages", fileName, jsonResponse.getString("content"));
			writeClassPathMessages(fileName, jsonResponse.getString("content"), bundleId);
			String locale = fileName.contains("_") ? fileName.split("_")[1].replace(PROPERTIES, "") : "";
			if (locale.isEmpty() || locale.equalsIgnoreCase(Locale.getDefault().getCountry())) {
				for (int i = 0; i < alfrescoConfigFiles.length(); i++) {
					JSONObject alfrescoConfigFile = alfrescoConfigFiles.getJSONObject(i);
					String otherFileName = alfrescoConfigFile.getString("displayName");
					if (otherFileName.contains("_") && (otherFileName.split("_")[0] + PROPERTIES).equals(fileName)) {
						publishProperties(alfrescoConfigFile.getString("nodeRef"), otherFileName, bundleId);
					}
				}
			}
			String fileBaseName = fileName.split("_")[0].replace(PROPERTIES, "");
			String bundleName = fileBaseName + "-" + bundleId;
			return "alfresco.messages.custom." + bundleName;
		}
		return null;
	}

	private void publishXml(String nodeRef, String fileName, Boolean writeXml) {
		JSONObject jsonResponse = alfrescoRequest(HttpMethod.POST, "/becpg/designer/model/publish?nodeRef=" + nodeRef + "&writeXml=" + writeXml);
		if (jsonResponse.has("type") && jsonResponse.getString("type").equals("config")) {
			if (fileName.endsWith(XML)) {
				if (jsonResponse.has(PUBLISHED_CONFIG_NAME)) {
					deleteFileOnDisk(configPath, jsonResponse.getString(PUBLISHED_CONFIG_NAME));
				}
				writeFileOnDisk(configPath, fileName, jsonResponse.getString("content"));
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
		cleanConfigFromPath(configPath);
		cleanConfigFromPath(configPath + FILE_SEPARATOR + "messages");
		configService.reset();
	}

	@SuppressWarnings("unchecked")
	private void cleanConfigFromPath(String path) {
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
			if (logger.isDebugEnabled()) {
				logger.debug("Deleting file at path " + path + ", exists ? " + file.exists());
			}
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
		if (logger.isDebugEnabled()) {
			logger.debug("Write designer file: " + path);
		}
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

	public void writeClassPathMessages(String fileName, String content, Long modifiedDate) {
		if (logger.isDebugEnabled()) {
			logger.debug("Write classpath messages for " + fileName);
		}
		String fileBaseName = fileName.split("_")[0].replace(PROPERTIES, "");
		String locale = fileName.contains("_") ? fileName.split("_")[1].replace(PROPERTIES, "") : "";
		String bundleName = fileBaseName + "-" + modifiedDate;
		try {
			if (locale.isEmpty() || locale.equalsIgnoreCase(Locale.getDefault().getCountry())) {
				List<String> uiLocales = configService.getConfig("Languages").getConfigElement("ui-languages").getChildren().stream()
						.map(c -> c.getAttribute("locale")).toList();
				for (String uiLocale : uiLocales) {
					if (locale.isEmpty()) {
						writeClassPathFile(content, bundleName, uiLocale);
					} else {
						writeClassPathFile("", bundleName, uiLocale);
					}
				}
			}
			writeClassPathFile("", bundleName, "");
			writeClassPathFile(content, bundleName, locale);
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	private void writeClassPathFile(String contentString, String fileName, String locale) throws IOException {
		String pathName = System.getProperty("catalina.base") + "/shared/classes/alfresco/messages/custom";
		File messageDir = new File(pathName);
		if (!messageDir.exists()) {
			messageDir.mkdirs();
		}
		String localeSuffix = locale.isBlank() ? "" : "_" + locale;
		String pathname = pathName + FILE_SEPARATOR + fileName + localeSuffix + PROPERTIES;
		File file = new File(pathname);
		if (file.exists() && contentString.isBlank()) {
			return;
		}
		if (file.exists() || file.createNewFile()) {
			if (logger.isDebugEnabled()) {
				if (contentString.isBlank()) {
					logger.debug("Write empty classpath file: " + pathname);
				} else {
					logger.debug("Write classpath file: " + pathname);
				}
			}
			try (InputStream in = new ByteArrayInputStream(contentString.getBytes()); OutputStream out = new FileOutputStream(file)) {
				IOUtils.copy(in, out);
			}
		}
	}

}
