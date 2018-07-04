package fr.becpg.web.scripts.forms;

import java.util.List;
import java.util.Map;

import org.alfresco.web.config.forms.FormConfigElement;
import org.alfresco.web.config.forms.Mode;
import org.alfresco.web.scripts.forms.FormUIGet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.Response;


public class BeCPGFormUIGet extends FormUIGet {

	private static final String PARAM_SITEID = "siteId";

	private static final Log logger = LogFactory.getLog(BeCPGFormUIGet.class);
	
    /**
     * Generates the model to send to the FreeMarker engine.
     * 
     * @param siteId The site ID to check if this site has a custom form 
     * @return Map
     */
    protected Map<String, Object> generateModel(String itemKind, String itemId, 
                WebScriptRequest request, Status status, Cache cache)
    {
        Map<String, Object> model = null;
        
        // get mode and optional formId
        String modeParam = getParameter(request, MODEL_MODE, DEFAULT_MODE);
        String siteIdParam = getParameter(request, PARAM_SITEID);
        String formId = getParameter(request, PARAM_FORM_ID);
        Mode mode = Mode.modeFromString(modeParam);
        
        
        
        // get the form configuration and list of fields that are visible (if any)
        FormConfigElement formConfig = null;
        if(siteIdParam != null && mode.equals(Mode.CREATE)){
        	if(getFormConfig(itemId, "create-" + siteIdParam) != null){
        		formId = "create-" + siteIdParam;
        	}
        } else if (siteIdParam != null && mode.equals(Mode.EDIT)){
        	if (getFormConfig(itemId, "edit-" + siteIdParam) != null){
        		formId = "edit-" + siteIdParam;
        	}
        } 
        	
        formConfig = getFormConfig(itemId, formId);
        
        if (logger.isDebugEnabled()){
        	logger.debug("Showing " + mode + " form (id=" + formId + ") for item: [" + itemKind + "]" + itemId);
        }
        
        List<String> visibleFields = getVisibleFields(mode, formConfig);
        
        // get the form definition from the form service
        Response formSvcResponse = retrieveFormDefinition(itemKind, itemId, visibleFields, formConfig);
        if (formSvcResponse.getStatus().getCode() == Status.STATUS_OK)
        {
            model = generateFormModel(request, mode, formSvcResponse, formConfig);
        }
        else if (formSvcResponse.getStatus().getCode() == Status.STATUS_UNAUTHORIZED)
        {
            // set status to 401 and return null model
            status.setCode(Status.STATUS_UNAUTHORIZED);
            status.setRedirect(true);
        }
        else
        {
            String errorKey = getParameter(request, PARAM_ERROR_KEY);
            model = generateErrorModel(formSvcResponse, errorKey);
        }
        
        return model;
    }


}
