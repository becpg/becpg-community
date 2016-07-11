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
 

	var prefs = "fr.becpg.formulation.dashlet.custom";

	if(page.url.args.list!=null && page.url.args.list.length>0){
	   prefs+="."+page.url.args.list;
	}

	model.preferences = AlfrescoUtil.getPreferences(prefs);
	model.customLists = getCustomLists();


	if(model.preferences.list!=null ){
		model.customListName = model.preferences.list;
	}

	for(var j in model.customLists){
		var customList = model.customLists[j];
		if(model.customListName!=null){
			if(model.customListName == customList.id ){
				model.customListType = customList.type;
				break;
			}
		} else if (customList.selected == "true"){
			model.customListName = customList.id;
			model.customListType = customList.type;
			break;
		}
		
	}
	
	
	
 
// Widget instantiation metadata...
var formulationView = {
   id : "FormulationView", 
   name : "beCPG.component.FormulationView",
   options : {
      siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
      entityNodeRef : (page.url.args.nodeRef != null) ? page.url.args.nodeRef : "",
      list : (page.url.args.list != null) ? page.url.args.list : "",
      customLists : model.customLists,
      customListName : model.customListName
   }
};



model.widgets = [formulationView];

model.widgets = model.widgets.concat(createDashlet("compoList-"+args.htmlid, "compoListDashlet"));
model.widgets = model.widgets.concat(createDashlet("dynamicCharactList-"+args.htmlid, "dynamicCharactListDashlet",msg.get("dashlet.dynamicCharactList.title"),"bcpg:dynamicCharactList",true));
model.widgets = model.widgets.concat(createDashlet("customList-"+args.htmlid, "customListDashlet",msg.get("dashlet.customList.title"),model.customListType, true , model.customListName, "datagrid", "&repo=true&guessContainer=true" ));

}

main();
