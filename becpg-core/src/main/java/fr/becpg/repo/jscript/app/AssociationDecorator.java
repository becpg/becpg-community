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
package fr.becpg.repo.jscript.app;

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONAware;

/**
 * <p>AssociationDecorator interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface AssociationDecorator {

	/**
	 * <p>getAssociationNames.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	Set<QName> getAssociationNames();

	/**
	 * <p>decorate.</p>
	 *
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param targetAssocs a {@link java.util.List} object.
	 * @return a {@link org.json.simple.JSONAware} object.
	 */
	JSONAware decorate(QName qName, NodeRef nodeRef, List<NodeRef> targetAssocs);

	/**
	 * <p>getAspect.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName getAspect();

}
