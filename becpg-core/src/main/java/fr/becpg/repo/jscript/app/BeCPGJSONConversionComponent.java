package fr.becpg.repo.jscript.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.app.JSONConversionComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.simple.JSONObject;

import fr.becpg.repo.helper.AssociationService;

public class BeCPGJSONConversionComponent  extends JSONConversionComponent {
	
	private AssociationService associationService;
	
	private DictionaryService dictionaryService;
	
	
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}


	private static Log logger = LogFactory.getLog(BeCPGJSONConversionComponent.class);
	

    /** Registered decorators */
    protected Map<QName, AssociationDecorator> associationDecorators = new HashMap<QName, AssociationDecorator>(3);

    
    
	 /**
     * Register a property decorator;
     * 
     * @param propertyDecorator
     */
    public void registerAssociationDecorator(AssociationDecorator associationDecorator)
    {
        for (QName assocName : associationDecorator.getAssociationNames())
        {
        	if(logger.isDebugEnabled()){
        		logger.debug("Register decorators for assoc :"+assocName.toPrefixString(namespaceService));
        	}
        	
        	associationDecorators.put(assocName, associationDecorator);
        }        
    }
	
	 /**
     * Convert a node reference to a JSON string.  Selects the correct converter based on selection
     * implementation.
     */
    @SuppressWarnings("unchecked")
    public String toJSON(NodeRef nodeRef, boolean useShortQNames)
    {
        JSONObject json = new JSONObject();
    
        if (this.nodeService.exists(nodeRef) == true)
        {
            if (publicServiceAccessService.hasAccess(ServiceRegistry.NODE_SERVICE.getLocalName(), "getProperties", nodeRef) == AccessStatus.ALLOWED)
            {
                // Get node info
                FileInfo nodeInfo = fileFolderService.getFileInfo(nodeRef);

                // Set root values
                setRootValues(nodeInfo, json, useShortQNames);                                       

                // add permissions
                json.put("permissions", permissionsToJSON(nodeRef));

                // add properties
                json.put("properties", propertiesToJSON(nodeRef, useShortQNames));
                
                // add associations
                json.put("associations", associationsToJSON(nodeRef, useShortQNames));

                // add aspects
                json.put("aspects", apsectsToJSON(nodeRef, useShortQNames));
            }
        }    
       
        return json.toJSONString();
    }
    
    
    /**
     * 
     * @param nodeRef
     * @param useShortQNames
     * @return
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    protected JSONObject associationsToJSON(NodeRef nodeRef, boolean useShortQNames)
    {
        JSONObject assocsToJSON = new JSONObject();
        
        TypeDefinition typeDef = dictionaryService.getType(nodeService.getType(nodeRef));
        
        List<QName> assocs = new ArrayList<QName>(typeDef.getAssociations().keySet());
        for(QName aspect : nodeService.getAspects(nodeRef)){
        	AspectDefinition aspectDefinition =dictionaryService.getAspect(aspect);
        	
        	assocs.addAll(aspectDefinition.getAssociations().keySet());
        }
        
        for (QName assoc : assocs)
        {
            try
            {
            	
            	String key = nameToString(assoc, useShortQNames);
            	if(logger.isDebugEnabled()){
            		logger.debug("look for assoc decorator: "+assoc.toPrefixString(namespaceService));
            	}
            	if(associationDecorators.get(assoc)!=null){
                	assocsToJSON.put(key, associationDecorators.get(assoc).decorate(assoc, nodeRef, associationService.getTargetAssocs(nodeRef, assoc)));
            	}
            }
            catch (NamespaceException ne)
            {
                // ignore properties that do not have a registered namespace
                if (logger.isDebugEnabled())
                    logger.debug("Ignoring assoc '" + assoc + "' as its namespace is not registered");
            }
        }
        
        return assocsToJSON;
    }
    
    
    /**
     * 
     * @param qname
     * @param isShortName
     * @return
     */
    private String nameToString(QName qname, boolean isShortName)
    {
        String result = null;
        if (isShortName == true)
        {
            result = qname.toPrefixString(namespaceService);
        }
        else
        {
            result = qname.toString();
        }
        return result;
    }
  
}
