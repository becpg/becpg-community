<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getActivityParameters(nodeRef, defaultValue)
{
   var cm = "{http://www.alfresco.org/model/content/1.0}",
      metadata = AlfrescoUtil.getMetaData(nodeRef, {});
   if (metadata.properties)
   {
     
     if (model.activityType == "entity")
      {
         return (
         {
            itemTitle: metadata.properties[cm + 'name'],
            page: 'entity-details',
            pageParams:
            {
               nodeRef: metadata.nodeRef
            }
         });
      } else if (model.activityType == "datalist")
      {
         return (
         {
            itemTitle: metadata.properties[cm + 'name'],
            page: 'entity-details',
            pageParams:
            {
               nodeRef: metadata.nodeRef
            }
         });
      } else if (model.activityType == "task")
      {
         return (
         {
            itemTitle: metadata.properties[cm + 'name'],
            page: 'entity-details',
            pageParams:
            {
               nodeRef: metadata.nodeRef
            }
         });
      }
      
   }
   return defaultValue;
}

function main()
{
   AlfrescoUtil.param('nodeRef', null);
   AlfrescoUtil.param('entityNodeRef', null);
   AlfrescoUtil.param('site', null);
   AlfrescoUtil.param('maxItems', 10);
   AlfrescoUtil.param('activityType', null);

   if (!model.nodeRef)
   {
      // Handle urls that doesn't use nodeRef
      AlfrescoUtil.param('postId', null);
      if (model.postId)
      {
         // translate blog post "postId" to a nodeRef
         AlfrescoUtil.param('container', 'blog');
         model.nodeRef = AlfrescoUtil.getBlogPostDetailsByPostId(model.site, model.container, model.postId, {}).nodeRef;
      }
      else
      {
         AlfrescoUtil.param('linkId', null);
         if (model.linkId)
         {
            // translate link's "linkId" to a nodeRef
            AlfrescoUtil.param('container', 'links');
            model.nodeRef = AlfrescoUtil.getLinkDetailsByPostId(model.site, model.container, model.linkId, {}).nodeRef;
         }
      }
   }

   var documentDetails = AlfrescoUtil.getNodeDetails(model.entityNodeRef? model.entityNodeRef : model.nodeRef, model.site);
   if (documentDetails)
   {
      model.activityParameters = getActivityParameters(model.entityNodeRef? model.entityNodeRef : model.nodeRef, null);
   }
   else
   {
      // Signal to the template that the node doesn't exist and that comments therefore shouldn't be displayed.
      model.nodeRef = null;
   }
   
   // Widget instantiation metadata...
  
   model.localeString = this.locale.substring(0, 2);
   
}

main();


