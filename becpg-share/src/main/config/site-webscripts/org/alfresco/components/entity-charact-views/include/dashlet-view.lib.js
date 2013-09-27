function createDashlet(dashletId, dashletName, dashletTitle, itemType){

   var entityDataGrid = {
         id : "entityDataGrid", 
         name : "beCPG.module.EntityDataGrid",
         initArgs : itemType ? ["\"" + dashletId + "\"", "\"true\""] : ["\"" + dashletId + "\""],
         options : {
            entityNodeRef: page.url.args.nodeRef!=null ?page.url.args.nodeRef : "",
            siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
            list: page.url.args.list!=null ?page.url.args.list : "",
            dataUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/data/node/",
            itemUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/item/node/",
            usePagination: true,
            displayBottomPagination : false,
            useFilter: true,
            sortable : true,
            sortUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/sort/node",
            itemType : itemType ? itemType : null,
            saveFieldUrl : page.url.context+"/proxy/alfresco/becpg/bulkedit/save",
            hiddenColumns : ["prop_bcpg_depthLevel"],
            initHistoryManager : false
           }
        };
         

      var dashletResizer = {
         id : "DashletResizer",
         name : "Alfresco.widget.DashletResizer",
         initArgs : ["\"" + dashletId + "\"", "\"" + dashletName + "\""],
         useMessages: false
      };

      var dashletTitleBarActions = {
         id : "DashletTitleBarActions",
         name : "Alfresco.widget.DashletTitleBarActions",
         initArgs : ["\"" + dashletId + "\""],
         useMessages : false,
         options : {
            actions : [
               {
                  cssClass: "help",
                  bubbleOnClick:
                  {
                     message: itemType ? msg.get("dashlet.help."+itemType.replace(":","_")) :  msg.get("dashlet.help.composition")
                  },
                  tooltip: msg.get("dashlet.help.tooltip")
               }
            ]
         }
      };
      
      return [entityDataGrid, dashletResizer, dashletTitleBarActions];
   
}