<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/entity-charact-views/include/dashlet-view.lib.js">

parseActions(page.url.args.list!=null ?page.url.args.list : null);


function getCustomLists()
{
   var myConfig = new XML(config.script),
   customLists = [];

   for each (var xmlCustomList in myConfig..customList)
   {
	   customLists.push(
      {
    	 id: xmlCustomList.@id.toString(),
         type: xmlCustomList.@type.toString(),
         selected: xmlCustomList.@selected.toString(),
         formId: xmlCustomList.@formId.toString()
      });
   }
   return customLists;
}


function main()
{
   
 
// Widget instantiation metadata...
var formulationView = {
   id : "FormulationView", 
   name : "beCPG.component.FormulationView",
   options : {
      siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
      entityNodeRef : (page.url.args.nodeRef != null) ? page.url.args.nodeRef : "",
      list : (page.url.args.list != null) ? page.url.args.list : ""
   }
};


var customListPrefs = AlfrescoUtil.getPreferences("fr.becpg.formulation.dashlet.custom."+(page.url.args.list != null) ? page.url.args.list : "");

model.customLists = getCustomLists();

model.customListName = "costList";
model.customListType = "bcpg:costList";

//
//if(customListPrefs){
//   if(!model.customListPrefs){
//      model.customListPrefs = {};
//   }
//   model.customListPrefs[dashletId] = prefs;
//}


model.widgets = [formulationView];

model.widgets = model.widgets.concat(createDashlet("compoList-"+args.htmlid, "compoListDashlet"));
model.widgets = model.widgets.concat(createDashlet("dynamicCharactList-"+args.htmlid, "dynamicCharactListDashlet",msg.get("dashlet.dynamicCharactList.title"),"bcpg:dynamicCharactList",true));
model.widgets = model.widgets.concat(createDashlet("customList-"+args.htmlid, "customListDashlet",msg.get("dashlet."+model.customListName+".title"),model.customListType, true , model.customListName, "formulation", "&repo=true&guessContainer=true" ));

}

main();
