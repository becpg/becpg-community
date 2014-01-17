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
package fr.becpg.repo.designer.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BPMN2XmlParser {

	private static Log logger = LogFactory.getLog(BPMN2XmlParser.class);
	
	private List<String> startTasks = new ArrayList<String>();
	private List<String> userTasks = new ArrayList<String>();
	private String processId = null;

	public void parse(InputStream in) throws IOException, SAXException, ParserConfigurationException {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			BPMN2XmlHandler handler = new BPMN2XmlHandler();
			saxParser.parse(in, handler);
			
			processId = handler.getId();

		} finally {
			IOUtils.closeQuietly(in);
		}

	}
	
	
	private class BPMN2XmlHandler extends DefaultHandler {
	
		private String id = null;
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
			if(qName.equals("process")){
				if(attributes.getValue("id") == null){
					logger.warn("process/@id is null");
				}
				else{
					id = attributes.getValue("id");
				}				
			}
			
			if(qName.equals("startEvent")){
				if(attributes.getValue("activiti:formKey") == null){
					logger.warn("startEvent/@activiti:formKey is null");
				}
				else{
					createStartTask(attributes.getValue("activiti:formKey"));
				}				
			}
			
			
			if(qName.equals("userTask")){
				if(attributes.getValue("activiti:formKey") == null){
					logger.warn("userTask/@activiti:formKey is null");
				}
				else{
					createUserTask(attributes.getValue("activiti:formKey"));
				}				
			}
			
		}

		public String getId() {
			return id;
		}

	}
	

	private void createUserTask(String qName) {
		userTasks.add(qName);
		
	}
	
	private void createStartTask(String qName) {
		startTasks.add(qName);
	}


	public List<String> getStartTasks() {
		return startTasks;
	}


	public List<String> getUserTasks() {
		return userTasks;
	}
	
	public boolean isActivitiWf(){
		return (!startTasks.isEmpty() || !userTasks.isEmpty());
	}


	public String getProcessId() {
		return processId;
	}

	
	
}
