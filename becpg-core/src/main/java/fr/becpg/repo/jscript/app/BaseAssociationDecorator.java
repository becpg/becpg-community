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
	        associationNames = new HashSet<QName>(1);        
	        associationNames.add(QName.createQName(AssociationName, namespaceService));
	    }
	    
	    public void setAssociationNames(Set<String> associationNames)
	    {
	        this.associationNames = new HashSet<QName>(associationNames.size());
	        for (String associationName : associationNames)
	        {
	            this.associationNames.add(QName.createQName(associationName, namespaceService));
	        }
	    }
}
