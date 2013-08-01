<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getActivityParameters(nodeRef, entityNodeRef)
{
   var cm = "{http://www.alfresco.org/model/content/1.0}";
   var pjt = "{http://www.bcpg.fr/model/project/1.0}";
  
     
     if (model.activityType == "entity" || !entityNodeRef )
      {
        metadata = AlfrescoUtil.getMetaData(nodeRef, {});
        if (metadata.properties)
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
      } else if (model.activityType == "task" || model.activityType == "datalist")
      {
         metadata = AlfrescoUtil.getMetaData(nodeRef, {});
         entityNodeRefMetadata = AlfrescoUtil.getMetaData(entityNodeRef, {});
         if (metadata.properties && entityNodeRefMetadata.properties)
         {
              if(metadata.type == pjt+"taskList"){
                return (
                {
                  
                   itemTitle: metadata.properties[pjt+"tlTaskName"]+" ["+entityNodeRefMetadata.properties[cm + 'name']+"]",
                   page: 'entity-data-lists',
                   pageParams:
                   {
                      nodeRef: entityNodeRef,
                      list: 'taskList'
                   }
                });
               
            } else  if (metadata.type == pjt+"deliverableList") {
               return (
               {
                  itemTitle: metadata.properties[pjt+"dlDescription"]+" ["+entityNodeRefMetadata.properties[cm + 'name']+"]",
                  page: 'entity-data-lists',
                  pageParams:
                  {
                     nodeRef: entityNodeRef,
                     list: 'deliverableList'
                  }
               });
            } else {
                return (
                {
                   itemTitle: entityNodeRefMetadata.properties[cm + 'name'],
                   page: 'entity-details',
                   pageParams:
                   {
                      nodeRef: entityNodeRef
                   }
                });
            } 
              
         }
      }
      
   
   return null;
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

   var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef , model.site);
   if (documentDetails)
   {
      model.activityParameters = getActivityParameters(model.nodeRef, model.entityNodeRef );
      
      if(model.site == null && documentDetails.item.location !=null && documentDetails.item.location.path !=null){
         
         if (documentDetails.item.location.path.indexOf('/Sites/')>-1)
         {
            model.site = documentDetails.item.location.path.split('/')[2];
         }

      }
      
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


