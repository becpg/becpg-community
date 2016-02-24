<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


function getActivityParameters(nodeRef, defaultValue)
{
   var cm = "{http://www.alfresco.org/model/content/1.0}",
      metadata = AlfrescoUtil.getMetaData(nodeRef, {});
   if (metadata.properties)
   {
         return (
         {
            itemTitle: metadata.properties[cm + 'name'],
            page: 'entity-data-lists',
            pageParams:
            {
               nodeRef: metadata.nodeRef,
               list: "View-properties"
            }
         });
   }
   return defaultValue;
}


function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);
   AlfrescoUtil.param('formId', null);
   AlfrescoUtil.param('maxItems', 10);
   AlfrescoUtil.param('activityType', null);
   AlfrescoUtil.param('currVersionNodeRef',null);
   
   var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (documentDetails)
   {
      model.document = documentDetails;
      model.item = documentDetails.item;
      model.node = documentDetails.item.node;
      model.allowMetaDataUpdate = (!documentDetails.item.node.isLocked && documentDetails.item.node.permissions.user["Write"]) || false;
      model.thumbnailUrl= "/share/proxy/alfresco/api/node/" + model.nodeRef.replace(':/','') + "/content/thumbnails/doclib?c=queue&ph=true";
      model.displayName =  (model.item.displayName != null) ? model.item.displayName : model.item.fileName;
      activityParameters = getActivityParameters(model.nodeRef, null);
      var count = documentDetails.item.node.properties["fm:commentCount"];
      model.commentCount = (count != undefined ? count : null);
   // Widget instantiation 
      
      var entityCatalog = {
    	         id : "EntityCatalog", 
    	         name : "beCPG.component.EntityCatalog",
    	         options : {
    	        	 entityNodeRef : model.nodeRef
    	         }
    	   };
      
      var propertiesView = {
         id : "Properties", 
         name : "beCPG.component.Properties",
         options : {
            nodeRef : model.nodeRef,
            siteId : model.site,
            formId : model.formId,
            currVersionNodeRef : model.currVersionNodeRef,
            maxItems : parseInt(model.maxItems),
            activity :  activityParameters,
            isFavourite : (model.item.isFavourite || false),
            editorConfig : {
               menu: {},
               toolbar: "bold italic underline | bullist numlist | forecolor backcolor | undo redo removeformat",
               language: locale,
               statusbar: false
            }
         }
      };
      
      model.widgets = [propertiesView, entityCatalog];
      
   }
  
}

main();