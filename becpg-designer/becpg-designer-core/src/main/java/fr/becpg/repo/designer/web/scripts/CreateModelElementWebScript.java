/*
 * 
 */
package fr.becpg.repo.designer.web.scripts;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;

// TODO: Auto-generated Javadoc
/**
 * The Class CreateModelElementWebScript.
 *
 * @author matthieu
 */
public class CreateModelElementWebScript extends DeclarativeWebScript  {
	

	private static final String PARAM_NODEREF = "nodeRef";

	private static final String NAME = "name";

	private static final String ASSOC_TYPE = "assocType";

	private static final String TYPE = "type";

	private static final String MODEL = "model";

	private static final String PERSISTED_OBJECT = "persistedObject";

	private static final String REDIRECT = "alf_redirect";

	private static final String TREE_NODE = "treeNode";
	
	private static final String PARAM_PALETTE_EL = "palette_el_id";

	
	/** The Constant PARAM_STORE_TYPE. */
	private static final String PARAM_STORE_TYPE = "store_type";
	
	/** The Constant PARAM_STORE_ID. */
	private static final String PARAM_STORE_ID = "store_id";
	
	/** The Constant PARAM_ID. */
	private static final String PARAM_ID = "id";
	

	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CreateModelElementWebScript.class);
	
	/** The node service. */
	private DesignerService designerService;
	
	private NamespaceService namespaceService;


	/**
	 * @param designerService the designerService to set
	 */
	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}


	/**
	 * @param namespaceService the namespaceService to set
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}


	/**
	 * Retrieve model Tree
	 * 
	 *    <url>/becpg/designer/create/element?nodeRef={nodeRef}</url>
	 *	  <url>/becpg/designer/dnd/{palette_el_id}?nodeRef={nodeRef}</url>
	 *	  <url>/becpg/designer/dnd/{store_type}/{store_id}/{id}?nodeRef={nodeRef}</url>
	 *
	 * @param req the req
	 * @param status the status
	 * @param cache the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
				
		logger.debug("CreateModelElementWebScript executeImpl()");
		

		Map<String, Object> model = new HashMap<String, Object>();

	
		Map<QName, Serializable> props = new HashMap<QName, Serializable>();
		NodeRef parentNodeRef = new NodeRef( req.getParameter(PARAM_NODEREF));	
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	
		
		NodeRef createNodeRef = null;
		
		if(templateArgs.containsKey(PARAM_STORE_TYPE)){
			    	
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
	    	
			NodeRef fromNodeRef = new NodeRef(storeType, storeId, nodeId);
			
			createNodeRef = designerService.moveElement(fromNodeRef, parentNodeRef);
			
		} else if(templateArgs.containsKey(PARAM_PALETTE_EL)){
			String paletteId = templateArgs.get(PARAM_PALETTE_EL);
			QName typeName = DesignerModel.TYPE_DSG_FORMCONTROL;
			QName assocName = DesignerModel.ASSOC_DSG_CONTROLS;
			if(paletteId.contains("formSets_")){
				typeName = DesignerModel.TYPE_DSG_FORMSET;
				assocName = DesignerModel.ASSOC_DSG_SETS;
				props.put(DesignerModel.PROP_DSG_APPEARANCE, paletteId.replace("formSets_", ""));
				props.put(DesignerModel.PROP_DSG_ID, paletteId.replace("formSets_", ""));
				paletteId = null;
			}
			
			createNodeRef = designerService.createModelElement(parentNodeRef,typeName, assocName,props,paletteId );
			
		} else {

			JsonParams jsonPostParams = parsePostParams(req);
			
			QName typeName = parseQName(jsonPostParams.getType());
			QName assocName = parseQName(jsonPostParams.getAssocType());
			
			
			if(jsonPostParams.getName()!=null && jsonPostParams.getName().length()>0){
				if(DesignerModel.TYPE_M2_NAMESPACE.equals(typeName) && (jsonPostParams.getModel()==null ||  jsonPostParams.getModel().length()<1)  ){
					props.put(DesignerModel.PROP_M2_URI, jsonPostParams.getName());
				} else if(DesignerModel.DESIGNER_URI.equals(typeName.getNamespaceURI())){
					props.put(DesignerModel.PROP_DSG_ID, jsonPostParams.getName());
				}	else {
					props.put(DesignerModel.PROP_M2_NAME, designerService.prefixName(parentNodeRef,jsonPostParams.getName()));
				}
			}
		
			createNodeRef = designerService.createModelElement(parentNodeRef, typeName, assocName, props, jsonPostParams.getModel());
			

			if(jsonPostParams.getRedirect()!=null){
				model.put("redirect",jsonPostParams.getRedirect());
			}
		}
		
		if(createNodeRef!=null){
			model.put(PERSISTED_OBJECT, createNodeRef.toString());
			model.put(TREE_NODE,designerService.getDesignerTree(parentNodeRef));
		}
		
		
		
		return model;
	}
	
	
	 private QName parseQName(String s) {
	
		 String qNamePrefix = s.split(":")[0];
         String localName = s.split(":")[1];
         return  QName.createQName(qNamePrefix, localName, namespaceService);

	}





	private JsonParams parsePostParams(WebScriptRequest req)
	    {
			
		
		
	        try
	        {
	        	
	        	
	            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
	            
	            JsonParams result = new JsonParams();
	            if (json.has(NAME))
	            {
	                result.setName(json.getString(NAME));
	            }
	            if (json.has(ASSOC_TYPE))
	            {
	                result.setAssocType(json.getString(ASSOC_TYPE));
	            }
	            if (json.has(TYPE))
	            {
	                result.setType(json.getString(TYPE));
	            }
	            if (json.has(MODEL))
	            {
	                result.setModel(json.getString(MODEL));
	            }
	            if (json.has(REDIRECT)){
	            	 result.setRedirect(json.getString(REDIRECT));
	            	
	            }
	           
	            
	            return result;
	        }
	        catch (IOException iox)
	        {
	            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", iox);
	        }
	        catch (JSONException je)
	        {
	            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
	        }
	    }
	 
	 /**
	     * A simple POJO class for the parsed JSON from the POST body.
	     */
	    class JsonParams
	    {
	        private String name;
	        private String assocType;
	        private String type; 
	        private String model;
	        private String redirect;
			/**
			 * @param name the name to set
			 */
			public void setName(String name) {
				this.name = name;
			}
			/**
			 * @param assocType the assocType to set
			 */
			public void setAssocType(String assocType) {
				this.assocType = assocType;
			}
			/**
			 * @param type the type to set
			 */
			public void setType(String type) {
				this.type = type;
			}
			/**
			 * @param model the model to set
			 */
			public void setModel(String model) {
				this.model = model;
			}
			/**
			 * @return the redirect
			 */
			public String getRedirect() {
				return redirect;
			}
			/**
			 * @param redirect the redirect to set
			 */
			public void setRedirect(String redirect) {
				this.redirect = redirect;
			}
			/**
			 * @return the name
			 */
			public String getName() {
				return name;
			}
			/**
			 * @return the assocType
			 */
			public String getAssocType() {
				return assocType;
			}
			/**
			 * @return the type
			 */
			public String getType() {
				return type;
			}
			/**
			 * @return the model
			 */
			public String getModel() {
				return model;
			}
			
			
	     
	    }

}
