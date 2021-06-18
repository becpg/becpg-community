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
 * <p>DesignerService interface.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public interface DesignerService {

	/**
	 * <p>createModelAspectNode.</p>
	 *
	 * @param parentNode a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param modelXml a {@link java.io.InputStream} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createModelAspectNode(NodeRef parentNode, InputStream modelXml);

	/**
	 * <p>writeXml.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void writeXml(NodeRef nodeRef);
	
	/**
	 * <p>getDesignerTree.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.designer.data.DesignerTree} object.
	 */
	DesignerTree getDesignerTree(NodeRef nodeRef);
	
	/**
	 * <p>createModelElement.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param typeName a {@link org.alfresco.service.namespace.QName} object.
	 * @param assocName a {@link org.alfresco.service.namespace.QName} object.
	 * @param props a {@link java.util.Map} object.
	 * @param modelTemplate a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createModelElement(NodeRef parentNodeRef, QName typeName, QName assocName, Map<QName, Serializable> props, String modelTemplate);

	/**
	 * <p>prefixName.</p>
	 *
	 * @param elementRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	String prefixName(NodeRef elementRef, String name);

	/**
	 * <p>findModelNodeRef.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef findModelNodeRef(NodeRef nodeRef);

	/**
	 * <p>publish.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void publish(NodeRef nodeRef);

	/**
	 * <p>getFormControls.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	List<FormControl> getFormControls();

	/**
	 * <p>moveElement.</p>
	 *
	 * @param from a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param to a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef moveElement(NodeRef from, NodeRef to);

	/**
	 * <p>findOrCreateModel.</p>
	 *
	 * @param modelName a {@link java.lang.String} object.
	 * @param modelTemplate a {@link java.lang.String} object.
	 * @param templateContext a {@link java.util.Map} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef findOrCreateModel(String modelName, String modelTemplate, Map<String, Object> templateContext);

	/**
	 * <p>findOrCreateConfig.</p>
	 *
	 * @param configName a {@link java.lang.String} object.
	 * @param modelTemplate a {@link java.lang.String} object.
	 * @param templateContext a {@link java.util.Map} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef findOrCreateConfig(String configName, String modelTemplate, Map<String, Object> templateContext);
	
	/**
	 * <p>createAndPublishConfig.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void createAndPublishConfig(NodeRef nodeRef);

	/**
	 * <p>unpublish.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void unpublish(NodeRef parentNodeRef);
	
	/**
	 * <p>unpublish.</p>
	 *
	 * @param fileName a {@link java.lang.String} object.
	 */
	void unpublish(String fileName);


	/**
	 * <p>export.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	String export(NodeRef nodeRef) ;
	
}
