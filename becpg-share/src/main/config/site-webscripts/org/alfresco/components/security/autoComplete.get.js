/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Retrieves the value of the given named argument from the URL arguments
 *
 * @method getArgument
 * @param argName The name of the argument to locate
 * @param defValue The default value to use if the argument could not be found
 * @return The value or null if not found
 */
function getArgument(argName, defValue)
{
   var result = args[argName];
   
   // if we don't have a result and a default has been defined, return that instead
   if (result === null && typeof defValue !== "undefined")
   {
      result = defValue;
   }
   
   return result;
}

/**
 * Finds the configuration for the given item id, if
 * there isn't any configuration for the item null is
 * returned.
 *
 * @method getFormConfig
 * @param itemId The id of the item to retrieve for config for
 * @param formId The id of the specific form to lookup or null
 *               to get the default form
 * @return Object representing the configuration or null
 */
function getFormConfig(itemId, formId)
{
   var formConfig = null;
   
   // query for configuration for item
   var nodeConfig = config.scoped[itemId];
   
   if (nodeConfig !== null)
   {
      // get the forms configuration
      var formsConfig = nodeConfig.forms;

      if (formsConfig !== null)
      {
         if (formId !== null && formId.length > 0)
         {
            // look up the specific form
            formConfig = formsConfig.getForm(formId);
         } 
         
         if (formConfig === null)
         {
        	  formConfig = formsConfig.getForm("create");
         }
         
         
         if (formConfig === null)
         {
            // look up the default form
            formConfig = formsConfig.defaultForm;
         }
    	  
      }
   }
   
   return formConfig;
}

/**
 * Returns the list of fields configured to be visible for the 
 * given mode. If this method returns null or an empty list the
 * component should attempt to display ALL known data for the item, 
 * unless there are fields configured to be hidden.
 *
 * @method getVisibleFields
 * @param mode The mode the form is rendering, 'view', 'edit' or 'create'
 * @param formConfig The form configuration, maybe null
 * @return Array of field names or null
 */
function getVisibleFields(mode, formConfig)
{
   var visibleFields = null;
   
   if (formConfig != null)
   {
      // get visible fields for the current mode
      switch (mode)
      {
         case "view":
            visibleFields = formConfig.visibleViewFieldNames;
            break;
         case "edit":
            visibleFields = formConfig.visibleEditFieldNames;
            break;
         case "create":
            visibleFields = formConfig.visibleCreateFieldNames;
            break;
         default:
            visibleFields = formConfig.visibleViewFieldNames;
            break;
      }
   }
   
   if (logger.isLoggingEnabled())
   {
      logger.log("Fields configured to be visible for " + mode + " mode = " + visibleFields);
   }
         
   return visibleFields;
}

/**
 * Creates an Object to represent the body of the POST request
 * to send to the form service.
 *
 * @method createPostBody
 * @param itemKind The kind of item
 * @param itemId The id of the item
 * @param visibleFields List of fields to get data for
 * @param formConfig The form configuration object
 * @return Object representing the POST body
 */
function createPostBody(itemKind, itemId, visibleFields, formConfig)
{
   var postBody = {};
   
   postBody.itemKind = itemKind;
   postBody.itemId = itemId.replace(":/", "");
   
   if (visibleFields !== null)
   {
      // create list of fields to show and a list of
      // those fields to 'force'
      var postBodyFields = [];
      var postBodyForcedFields = [];
      var fieldId = null;
      for (var f = 0; f < visibleFields.length; f++)
      {
         fieldId = visibleFields[f];
         postBodyFields.push(fieldId);
         if (formConfig.isFieldForced(fieldId))
         {
            postBodyForcedFields.push(fieldId);
         }
      }
      
      postBody.fields = postBodyFields;
      if (postBodyForcedFields.length > 0)
      {
         postBody.force = postBodyForcedFields;
      }
   }
   
   if (logger.isLoggingEnabled())
   {
      logger.log("postBody = " + jsonUtils.toJSONString(postBody));
   }
      
   return postBody;
}



function getACLGroupInfo(entityNodeRef){
	
	 var result = remote.call("/becpg/entitylists/node/"+entityNodeRef.replace("://", "/")+"?aclMode=true");
	   
	   if (result.status == 200)
	   { 
		   var aclInfo = eval('(' + result + ')');
      
          return aclInfo;
      
	   }
	
	
	return {aclType:"bcpg:finishedProduct",datalists: []}; 
}

/**
 * Main entrypoint for component webscript logic
 *
 * @method main
 */
function main()
{
   var entityNodeRef = getArgument("entityNodeRef");
   
   //ACL Group
   var aclInfo = getACLGroupInfo(entityNodeRef),
   	itemType = aclInfo.aclType,
    columns = [];
   
   
   if (itemType !== null && itemType.length > 0)
   {

      var formConfig = getFormConfig(itemType,"security");
      
      // get the configured visible fields
      var visibleFields = getVisibleFields("edit", formConfig);
      
      // build the JSON object to send to the server
      var postBody = createPostBody("type", itemType, visibleFields, formConfig);
         
      // make remote call to service
      var connector = remote.connect("alfresco");
      var json = connector.post("/api/formdefinitions", jsonUtils.toJSONString(postBody), "application/json");
      
      if (logger.isLoggingEnabled())
      {
         logger.log("json = " + json);
      }
      
      if (json.status == 401)
      {
         status.setCode(json.status, "Not authenticated");
         return;
      }
   
         var formModel = eval('(' + json + ')');
         
         // if we got a successful response attempt to render the form
         if (json.status == 200)
         {
            columns = formModel.data.definition.fields;
         }
         else
         {
            model.error = formModel.message;
         }
      }
   
   
   var q = getArgument("q");
   var page = getArgument("page");
   if(page==null || page == undefined){
	   page=1;
   }
   
   var pageSize = 10;
  
   
   
   var results = [];
   
   for(var i in columns){
	   if(q==null || q==undefined || q=="*" || columns[i].label.toLowerCase().indexOf(q.toLowerCase())!=-1){
		   columns[i].type ="aclEntry";
		   results.push(columns[i]);
	   }
   }
   
   
   for(var i in aclInfo.datalists){
	   if(q==null || q==undefined || q=="*" || aclInfo.datalists[i].title.toLowerCase().indexOf(q.toLowerCase())!=-1){
		   results.push({name :  aclInfo.datalists[i].itemType,label : aclInfo.datalists[i].title, type :"dataList" });
	   }
   }
   
   model.pageSize = pageSize;
   model.pageNumber = page;
   model.fullListSize = results.length;
   model.columns = results.slice(Math.max((page-1) * pageSize,0), Math.min(page * pageSize, results.length));
   
}

main();
