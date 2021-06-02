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
package fr.becpg.repo.product;

import java.util.Map;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.formulation.FormulateException;

/**
 * <p>CharactDetailsVisitorFactoryImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CharactDetailsVisitorFactoryImpl implements CharactDetailsVisitorFactory{
	
	
	Map<String,CharactDetailsVisitor> visitorRegistry;
	
	NamespaceService namespaceService;
	
	/**
	 * <p>Setter for the field <code>visitorRegistry</code>.</p>
	 *
	 * @param visitorRegistry a {@link java.util.Map} object.
	 */
	public void setVisitorRegistry(Map<String, CharactDetailsVisitor> visitorRegistry) {
		this.visitorRegistry = visitorRegistry;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}


	/** {@inheritDoc} */
	@Override
	public CharactDetailsVisitor getCharactDetailsVisitor(QName dataType, String dataListName) throws FormulateException {
		
		CharactDetailsVisitor visitor = visitorRegistry.get(dataType.toPrefixString(namespaceService));
		if(visitor!=null){
			visitor.setDataListType(dataType);
			return visitor;
		}
		
		throw new FormulateException("No visitor found");
	}

}
