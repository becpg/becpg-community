/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
package fr.becpg.repo.designer;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.designer.data.DesignerTree;
import fr.becpg.repo.designer.data.FormControl;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface DesignerService {

	NodeRef createModelAspectNode(NodeRef parentNode, InputStream modelXml);

	void writeXml(NodeRef nodeRef);
	
	DesignerTree getDesignerTree(NodeRef nodeRef);
	
	NodeRef createModelElement(NodeRef parentNodeRef, QName typeName, QName assocName, Map<QName, Serializable> props, String modelTemplate);

	String prefixName(NodeRef elementRef, String name);

	NodeRef findModelNodeRef(NodeRef nodeRef);

	void publish(NodeRef nodeRef);

	List<FormControl> getFormControls();

	NodeRef moveElement(NodeRef from, NodeRef to);

	NodeRef findOrCreateModel(String modelName, String modelTemplate, Map<String, Object> templateContext);

	NodeRef findOrCreateConfig(String configName, String modelTemplate, Map<String, Object> templateContext);
	
	void createAndPublishConfig(NodeRef nodeRef);

	void unpublish(NodeRef parentNodeRef);
	
	void unpublish(String fileName);

	

	
}
