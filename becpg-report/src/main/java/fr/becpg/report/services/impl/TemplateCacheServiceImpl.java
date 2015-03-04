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
package fr.becpg.report.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.icu.util.Calendar;

import fr.becpg.report.client.ReportException;
import fr.becpg.report.services.TemplateCacheService;

/**
 * Basic inMemoryTemplateCache Limit Number of entry for security and size
 * 
 * @author matthieu
 *
 */
public class TemplateCacheServiceImpl implements TemplateCacheService {

	private static final Log logger = LogFactory.getLog(TemplateCacheServiceImpl.class);

	private static int NUMBER_IN_MEMORY = 10000;

	private Map<String, TemplateCacheEl> cache = new ConcurrentHashMap<String, TemplateCacheServiceImpl.TemplateCacheEl>();

	private class TemplateCacheEl {

		private Path backedFile;
		
		private long timeStamp = Calendar.getInstance().getTimeInMillis();

		public TemplateCacheEl(String templateId) throws IOException {
			super();
			backedFile = Files.createTempFile(templateId.replace(":/", "-").replace("/", ""), ".bcpg");
		}
		
		public long getTimeStamp() {
			return Files.exists(backedFile) ? timeStamp : -1L;
		}

		public InputStream getContent() throws IOException {
			return Files.newInputStream(backedFile);
		}

		public void writeContent(InputStream in) throws IOException, ReportException {
			Files.copy(in, backedFile, StandardCopyOption.REPLACE_EXISTING);
		}

		public URL getURL() throws MalformedURLException {
			return backedFile.toUri().toURL();
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			Files.deleteIfExists(backedFile);
		}
	}

	@Override
	public Long getTemplateTimeStamp(String templateId) {
		logger.debug("getTemplateTimeStamp for " + templateId);
		TemplateCacheEl el = cache.get(templateId);
		if (el != null) {
			return el.getTimeStamp();
		}

		return -1L;
	}

	@Override
	public Long saveTemplate(String templateId, InputStream in) throws ReportException, IOException {
		if (cache.size() > NUMBER_IN_MEMORY) {
			throw new ReportException("Max cache entries reached");
		}
		TemplateCacheEl el = new TemplateCacheEl(templateId);

		el.writeContent(in);
		cache.put(templateId, el);

		logger.debug("saveTemplate for " + templateId);
		return el.getTimeStamp();

	}

	@Override
	public InputStream getTemplate(String templateId) throws ReportException, IOException {
		logger.debug("getTemplate for " + templateId);
		TemplateCacheEl el = cache.get(templateId);
		if (el != null) {
			return el.getContent();
		}

		throw new ReportException("No template found in cache for: " + templateId);
	}

	@Override
	public URL getTemplateURL(String templateId) throws ReportException, MalformedURLException {
		TemplateCacheEl el = cache.get(templateId);
		if (el != null) {
			return el.getURL();
		}
		throw new ReportException("No template URL found in cache for: " + templateId);
	}

}
