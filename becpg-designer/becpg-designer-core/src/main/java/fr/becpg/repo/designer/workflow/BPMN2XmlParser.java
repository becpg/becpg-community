/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>BPMN2XmlParser class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BPMN2XmlParser {

	private static final Log logger = LogFactory.getLog(BPMN2XmlParser.class);
	
	private final List<String> startTasks = new ArrayList<>();
	private final List<String> userTasks = new ArrayList<>();
	private String processId = null;

	/**
	 * <p>parse.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @throws java.io.IOException if any.
	 * @throws org.xml.sax.SAXException if any.
	 * @throws javax.xml.parsers.ParserConfigurationException if any.
	 */
	public void parse(InputStream in) throws IOException, SAXException, ParserConfigurationException {

		try (in) {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

			SAXParser saxParser = factory.newSAXParser();

			BPMN2XmlHandler handler = new BPMN2XmlHandler();
			saxParser.parse(in, handler);
			
			processId = handler.getId();

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


	/**
	 * <p>Getter for the field <code>startTasks</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getStartTasks() {
		return startTasks;
	}


	/**
	 * <p>Getter for the field <code>userTasks</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getUserTasks() {
		return userTasks;
	}
	
	/**
	 * <p>isActivitiWf.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isActivitiWf(){
		return (!startTasks.isEmpty() || !userTasks.isEmpty());
	}


	/**
	 * <p>Getter for the field <code>processId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getProcessId() {
		return processId;
	}

	
	
}
