package fr.becpg.repo.designer.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

public class SharePublishHelper {

	private static final Log logger = LogFactory.getLog(SharePublishHelper.class);

	private SharePublishHelper() {
		
	}
	
	public static void publishConfig(String configPath, String fileName, JSONObject jsonResponse) {
		File configDir = new File(configPath);
		if (!configDir.exists()) {
			configDir.mkdirs();
		}
		String path = configPath + System.getProperty("file.separator") + fileName;
		logger.debug("Publish config under " + path);
		try {
			File file = new File(path);

			if (file.exists() || file.createNewFile()) {
				try (InputStream in = new ByteArrayInputStream(jsonResponse.getString("xml").getBytes());
						OutputStream out = new FileOutputStream(file)) {
					IOUtils.copy(in, out);
				}
			}

		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	public static void unpublishConfig(String configPath, String fileName) {
		File configDir = new File(configPath);
		if (!configDir.exists()) {
			configDir.mkdirs();
		}
		String path = configPath + System.getProperty("file.separator") + fileName;

		File file = new File(path);
		logger.debug("Deleting file at path " + path + ", exists ? " + file.exists());
		if (file.exists()) {
			if (!file.delete()) {
				logger.error("Cannot delete file: " + file.getName());
			}
		}
	}

}
