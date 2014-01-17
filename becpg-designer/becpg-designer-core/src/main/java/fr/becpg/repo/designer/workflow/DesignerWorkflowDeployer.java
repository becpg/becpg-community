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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;

/**
 * Create missing forms and type from workflow definition
 * 
 * @author matthieu
 * 
 */
public class DesignerWorkflowDeployer {

	private NodeService nodeService;

	private ContentService contentService;

	private DesignerService designerService;

	private DictionaryService dictionaryService;

	private NamespaceService namespaceService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	private static Log logger = LogFactory.getLog(DesignerWorkflowDeployer.class);

	public void createMissingFormsAndType(NodeRef nodeRef) {

		BPMN2XmlParser bpmn2Parser = new BPMN2XmlParser();
		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		InputStream in = null;
		try {
			in = reader.getContentInputStream();
			bpmn2Parser.parse(in);

			if (bpmn2Parser.isActivitiWf()) {

				nodeService.setProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEF_ENGINE_ID, ActivitiConstants.ENGINE_ID);

				String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

				Map<String,Object> templateContext = new HashMap<String, Object>();
				templateContext.put("processId", bpmn2Parser.getProcessId());
				templateContext.put("modelName", FilenameUtils.removeExtension(name));
				templateContext.put("prefix", extractPrefix(bpmn2Parser));
				templateContext.put("engineId", "activiti");
				
				NodeRef modelNodeRef = designerService.findOrCreateModel(name, "extWorkflowModel.ftl",templateContext);

				NodeRef configNodeRef = designerService.findOrCreateConfig(name, "extWorkflowForm.ftl",templateContext);

				for (String startTask : bpmn2Parser.getStartTasks()) {

					if (!checkForType(startTask, modelNodeRef)) {
						addNameSpace(startTask, modelNodeRef);
						Map<QName, Serializable> props = new HashMap<QName, Serializable>();

						props.put(DesignerModel.PROP_M2_NAME, startTask);
						NodeRef typeNodeRef = designerService.createModelElement(modelNodeRef, DesignerModel.TYPE_M2_TYPE, DesignerModel.ASSOC_M2_TYPES, props,
								"templateModel_STARTTASK");
						designerService.moveElement(typeNodeRef, configNodeRef);
					}
				}

				for (String userTask : bpmn2Parser.getUserTasks()) {
					if (!checkForType(userTask, modelNodeRef)) {
						addNameSpace( userTask, modelNodeRef);
						Map<QName, Serializable> props = new HashMap<QName, Serializable>();
						props.put(DesignerModel.PROP_M2_NAME, userTask);
						NodeRef typeNodeRef = designerService.createModelElement(modelNodeRef, DesignerModel.TYPE_M2_TYPE, DesignerModel.ASSOC_M2_TYPES, props,
								"templateModel_TASKNODE");
						designerService.moveElement(typeNodeRef, configNodeRef);
					}
				}
			}

		} catch (IOException e) {
			logger.error(e, e);
		} catch (SAXException e) {
			logger.error(e, e);
		} catch (ParserConfigurationException e) {
			logger.error(e, e);
		} finally {
			IOUtils.closeQuietly(in);
		}

	}

	private String extractPrefix(BPMN2XmlParser bpmn2Parser) {
		for (String startTask : bpmn2Parser.getStartTasks()) {
			String prefix = startTask.split(":")[0];
			String uri = namespaceService.getNamespaceURI(prefix);
			if (uri == null || uri.length() < 1) {
				return prefix;
			}
		}
		for (String userTask : bpmn2Parser.getUserTasks()) {
			String prefix = userTask.split(":")[0];
			String uri = namespaceService.getNamespaceURI(prefix);
			if (uri == null || uri.length() < 1) {
				return prefix;
			}
		}
		return "toChange";
	}

	private void addNameSpace( String qName, NodeRef modelNodeRef) {
		UUID uuid = UUID.randomUUID();
		String prefix = qName.split(":")[0];
		String uri = namespaceService.getNamespaceURI(prefix);
		Map<QName, Serializable> props = new HashMap<QName, Serializable>();

		props.put(DesignerModel.PROP_M2_PREFIX, prefix);
		QName assocQName = DesignerModel.ASSOC_M2_IMPORTS;

		if (uri == null || uri.length() < 1) {
			assocQName = DesignerModel.ASSOC_M2_NAMESPACES;
			uri = "http://www.bcpg.fr/model/wf/" + uuid.toString() + "/1.0";
		}

		props.put(DesignerModel.PROP_M2_URI, uri);
		// look current model for existing namespace or import
		for (ChildAssociationRef assoc : nodeService.getChildAssocs(modelNodeRef)) {
			if (assoc.getQName().equals(assocQName)) {
				NodeRef namespaceNodeRef = assoc.getChildRef();
				String tmpPrefix = (String) nodeService.getProperty(namespaceNodeRef, DesignerModel.PROP_M2_PREFIX);
				if (tmpPrefix != null && tmpPrefix.equals(prefix)) {
					logger.debug("Namespaces or import already added " + prefix);
					return;
				}
			}
		}
		
		logger.debug("Adding : " + uri + " " + prefix + " to " + assocQName);
		designerService.createModelElement(modelNodeRef, DesignerModel.TYPE_M2_NAMESPACE, assocQName, props, null);
	}

	private boolean checkForType(String qname, NodeRef modelNodeRef) {
		boolean exist = false;
		try {
			QName typeQName = QName.createQName(qname, namespaceService);
			exist = dictionaryService.getType(typeQName) != null;
		} catch (Exception e) {
			exist = false;
		}

		// Look into current model
		if (!exist) {
			for (ChildAssociationRef assoc : nodeService.getChildAssocs(modelNodeRef)) {
				if (assoc.getQName().equals(DesignerModel.ASSOC_M2_TYPES)) {
					NodeRef namespaceNodeRef = assoc.getChildRef();
					String name = (String) nodeService.getProperty(namespaceNodeRef, DesignerModel.PROP_M2_NAME);
					if (name != null && name.equals(qname)) {
						logger.debug("Type allready added  " + qname);
						return true;
					}
				}
			}
		}
		logger.debug("Type exist : " + qname + "(" + exist + ")");
		return exist;
	}

}
