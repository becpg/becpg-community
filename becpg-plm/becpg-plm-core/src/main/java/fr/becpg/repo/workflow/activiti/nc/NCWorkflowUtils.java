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
package fr.becpg.repo.workflow.activiti.nc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.util.ApplicationContextHelper;

public class NCWorkflowUtils {

	private static Log logger = LogFactory.getLog(NCWorkflowUtils.class);

	public interface NCWorkflowUtilsTask {

		Object getVariable(String name);

		Set<String> getVariableNames();
		
	}
	
	static {
	
		DefaultTypeConverter.INSTANCE.addConverter(ActivitiScriptNode.class, NodeRef.class,new TypeConverter.Converter<ActivitiScriptNode, NodeRef>()
                {
            public NodeRef convert(ActivitiScriptNode source)
            {
            	if(source!=null){
            		return source.getNodeRef();
            	} 
            	return null;
            }
        } );
	
	}

	public static void updateNC(NodeRef ncNodeRef, NCWorkflowUtilsTask task, ServiceRegistry serviceRegistry) throws FileExistsException, FileNotFoundException {

		AssociationService associationService = ApplicationContextHelper.getApplicationContext().getBean(fr.becpg.repo.helper.AssociationService.class);
		EntityService entityService = ApplicationContextHelper.getApplicationContext().getBean(fr.becpg.repo.entity.EntityService.class);
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>(2);
		if (task.getVariable("ncwf_ncState") != null) {
			properties.put(QualityModel.PROP_NC_STATE, (String) task.getVariable("ncwf_ncState"));
		}
		
		
		if (task.getVariable("bpm_comment") != null) {
			properties.put(QualityModel.PROP_NC_COMMENT, (String) task.getVariable("bpm_comment"));
		}

		for (QName aspectQname : new QName[] { QualityModel.ASPECT_BATCH, QualityModel.ASPECT_CLAIM_RESPONSE, QualityModel.ASPECT_CLAIM_TREATEMENT,
				QualityModel.ASPECT_CLAIM_CLOSING, QualityModel.ASPECT_CLAIM, PLMModel.ASPECT_CLIENTS, PLMModel.ASPECT_SUPPLIERS , PLMModel.ASPECT_MANUFACTURING}) {

			AspectDefinition aspectDef = serviceRegistry.getDictionaryService().getAspect(aspectQname);
			for (QName propQname : aspectDef.getProperties().keySet()) {
				String propName = propQname.toPrefixString(serviceRegistry.getNamespaceService()).replaceFirst(":", "_");
				
				Serializable attr = (Serializable) task.getVariable(propName);
				
				if (attr != null) {
					properties.put(propQname, attr);
				} else if(task.getVariableNames().contains(propName)){
					properties.put(propQname, null);
				}
			}

			for (QName assocQname : aspectDef.getAssociations().keySet()) {
				String assocName =assocQname.toPrefixString(serviceRegistry.getNamespaceService()).replaceFirst(":", "_");
				if (task.getVariable(assocName) instanceof ActivitiScriptNode) {
					ActivitiScriptNode node = (ActivitiScriptNode) task.getVariable(assocName);
					if (node != null) {
						associationService.update(ncNodeRef, assocQname, node.getNodeRef());
					} 
				} else {
					@SuppressWarnings("unchecked")
					List<ActivitiScriptNode> nodes = (ArrayList<ActivitiScriptNode>) task.getVariable(assocName);
					if (nodes != null) {
						associationService.update(ncNodeRef, assocQname, convertList(nodes));
					}
				}
			}

		}

		if (logger.isDebugEnabled()) {
			logger.debug("UpdateNC: " + ncNodeRef + " - " + properties);
		}
		serviceRegistry.getNodeService().addProperties(ncNodeRef, properties);

		// Move documents from pkgNodeRef
		NodeRef briefNodeRef = entityService.getOrCreateDocumentsFolder(ncNodeRef);
		
		NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

		List<ChildAssociationRef> childAssocs = serviceRegistry.getNodeService().getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
		for (ChildAssociationRef childAssoc : childAssocs) {
			String name = (String) serviceRegistry.getNodeService().getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME);
			if ( !serviceRegistry.getNodeService().getType(childAssoc.getChildRef()).equals(QualityModel.TYPE_NC)) {
				serviceRegistry.getFileFolderService().move(childAssoc.getChildRef(), briefNodeRef, name);
				serviceRegistry.getNodeService().removeChild(pkgNodeRef, childAssoc.getChildRef());
			}
		}

	}

	private static List<NodeRef> convertList(List<ActivitiScriptNode> nodes) {
		List<NodeRef> ret = new ArrayList<>();
		
		for(ActivitiScriptNode node : nodes){
			ret.add(node.getNodeRef());
		}
		
		return ret;
	}
}
