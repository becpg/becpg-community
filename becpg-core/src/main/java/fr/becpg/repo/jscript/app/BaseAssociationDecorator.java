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
package fr.becpg.repo.jscript.app;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public abstract class BaseAssociationDecorator implements AssociationDecorator{
	   
	    protected Set<QName> associationNames;
	    
	    protected NodeService nodeService;
	    
	    protected NamespaceService namespaceService;
	    
	    protected PermissionService permissionService;
	    
	    protected BeCPGJSONConversionComponent jsonConversionComponent;
	    
	    public void setNamespaceService(NamespaceService namespaceService)
	    {
	        this.namespaceService = namespaceService;
	    }
	    
	    public void setJsonConversionComponent(BeCPGJSONConversionComponent jsonConversionComponent)
	    {
	        this.jsonConversionComponent = jsonConversionComponent;
	    }

	    public void setNodeService(NodeService nodeService)
	    {
	        this.nodeService = nodeService;
	    }

	    public void setPermissionService(PermissionService permissionService)
	    {
	        this.permissionService = permissionService;
	    }
	    
	    public void init()
	    {
	        jsonConversionComponent.registerAssociationDecorator(this);
	    }
	    
	    @Override
	    public Set<QName> getAssociationNames()
	    {
	        return associationNames;
	    }
	    
	    public void setAssociationName(String AssociationName)
	    {
	        associationNames = new HashSet<>(1);
	        associationNames.add(QName.createQName(AssociationName, namespaceService));
	    }
	    
	    public void setAssociationNames(Set<String> associationNames)
	    {
	        this.associationNames = new HashSet<>(associationNames.size());
	        for (String associationName : associationNames)
	        {
	            this.associationNames.add(QName.createQName(associationName, namespaceService));
	        }
	    }
}
