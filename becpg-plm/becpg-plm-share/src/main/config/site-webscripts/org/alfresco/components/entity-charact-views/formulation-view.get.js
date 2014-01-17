<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/entity-charact-views/include/dashlet-view.lib.js">

parseActions(page.url.args.list!=null ?page.url.args.list : null);


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
