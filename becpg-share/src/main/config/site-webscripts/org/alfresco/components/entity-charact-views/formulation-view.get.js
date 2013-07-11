<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">


parseActions();

function createDashlet(dashletId, dashletName, dashletTitle, itemType){
   
   var entityDataGrid = {
         id : "entityDataGrid", 
         name : "beCPG.module.EntityDataGrid",
         initArgs : itemType ? ["\"" + dashletId + "\"", "\"true\""] : ["\"" + dashletId + "\""],
         options : {
            entityNodeRef: page.url.args.nodeRef!=null ?page.url.args.nodeRef : "",
            siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
            list: page.url.args.list!=null ?page.url.args.list : "",
            dataUrl : page.url.context+"/proxy/alfresco/" +  (args.dataUrl!=null ? args.dataUrl :"becpg/entity/datalists/data/node"),
            itemUrl : page.url.context+"/proxy/alfresco/" +  (args.itemUrl!=null ? args.itemUrl :"becpg/entity/datalists/item/node/"),
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


function main()
{
   
//TODO var component =  sitedata.getComponent("compoListDashlet");
//if( component!=null ){
//   model.height = component.properties.height;
//}
 
// Widget instantiation metadata...
var formulationView = {
   id : "FormulationView", 
   name : "beCPG.component.FormulationView",
   options : {
      siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
      entityNodeRef : (page.url.args.nodeRef != null) ? page.url.args.nodeRef : ""
   }
};

model.widgets = [formulationView];

model.widgets = model.widgets.concat(createDashlet("compoList-"+args.htmlid, "compoListDashlet"));
model.widgets = model.widgets.concat(createDashlet("dynamicCharactList-"+args.htmlid, "dynamicCharactListDashlet",msg.get("dashlet.dynamicCharactList.title"),"bcpg:dynamicCharactList"));
model.widgets = model.widgets.concat(createDashlet("constraintsList-"+args.htmlid, "constraintsListDashlet",msg.get("dashlet.constraintsList.title"),"bcpg:reqCtrlList"));

}


main();