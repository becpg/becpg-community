<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/entity-charact-views/include/dashlet-view.lib.js">


function main()
{
	
	// Widget instantiation metadata...
	var labelingView = {
	   id : "LabelingView", 
	   name : "beCPG.component.LabelingView",
	   options : {
	      entityNodeRef : (page.url.args.nodeRef != null) ? page.url.args.nodeRef : ""
	   }
	};
	
    model.widgets = [labelingView];
    model.widgets = model.widgets.concat(createDashlet("ingLabelingList-"+args.htmlid, "ingLabelingListDashlet"));
    model.widgets = model.widgets.concat(createDashlet("labelingRuleList-"+args.htmlid, "labelingRuleListDashlet",msg.get("dashlet.labelingRuleList.title"),"bcpg:labelingRuleList",true));
    model.widgets = model.widgets.concat(createDashlet("compoList-"+args.htmlid, "compoListDashlet",msg.get("dashlet.compoList.title"),"bcpg:compoList", true ,"compoList", "labeling", "&repo=true&guessContainer=true&effectiveFilterOn=true" ));
}

parseActions(page.url.args.list!=null ?page.url.args.list : null);   
main();
