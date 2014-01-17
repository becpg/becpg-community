/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.icu.util.Calendar;

import fr.becpg.report.client.ReportException;
import fr.becpg.report.services.TemplateCacheService;
/**
 * Basic inMemoryTemplateCache
 * Limit Number of entry for security and size
 * @author matthieu
 *
 */
public class TemplateCacheServiceImpl implements TemplateCacheService {

	private static int NUMBER_IN_MEMORY = 10000;
	
	private static Log logger = LogFactory.getLog(TemplateCacheServiceImpl.class);
	
	
    private static final Map<String, TemplateCacheEl> cache = new ConcurrentHashMap<String, TemplateCacheServiceImpl.TemplateCacheEl>();
	

	private class TemplateCacheEl {
		
		private final static int BUFFER_SIZE = 2048;
		
		private int MAX_SIZE = 5 * 1000 * BUFFER_SIZE; //10 Mo 
		
		private long timeStamp = Calendar.getInstance().getTimeInMillis();
		private byte[] content;
		
		
		public long getTimeStamp() {
			return timeStamp;
		}
		
		public InputStream getContent(){
			return new ByteArrayInputStream(content);
		}

		public void writeContent(InputStream in) throws IOException, ReportException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			try {
				byte[] buffer = new byte[BUFFER_SIZE];
				int l,i = 0;
				// consume until EOF
				while ((l = in.read(buffer)) != -1) {
					if(i>MAX_SIZE/BUFFER_SIZE){
						throw new ReportException("Template file to big!");
					}
					out.write(buffer, 0, l);
					i++;
				}
				content  = out.toByteArray();
				
				
			} finally {
				in.close();
				out.close();
			}
			
			
		}
		
		
		
	}
	
	@Override
	public Long getTemplateTimeStamp(String templateId) {
		logger.debug("getTemplateTimeStamp for "+templateId);
		TemplateCacheEl el = cache.get(templateId);
		if(el!=null){
			return el.getTimeStamp();
		}
		
		return -1L;
	}

	@Override
	public Long saveTemplate(String templateId, InputStream in) throws ReportException, IOException {
		 if(cache.size()>NUMBER_IN_MEMORY){
				throw new ReportException("Max cache entries reached");
		 }
		 TemplateCacheEl el = new TemplateCacheEl();
			
		 el.writeContent(in);
		 cache.put(templateId, el);

	     logger.debug("saveTemplate for "+templateId);
		return  el.getTimeStamp();
		
	}

	@Override
	public InputStream getTemplate(String templateId) throws ReportException {
		logger.debug("getTemplate for "+templateId);
		TemplateCacheEl el = cache.get(templateId);
		if(el!=null){
			return el.getContent();
		}
		
		throw new ReportException("No template found in cache for: " +templateId);
	}

}
