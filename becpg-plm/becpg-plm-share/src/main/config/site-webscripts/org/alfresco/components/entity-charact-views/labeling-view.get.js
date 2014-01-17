<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/entity-charact-views/include/dashlet-view.lib.js">




function main()
{

model.widgets = [];
model.widgets = model.widgets.concat(createDashlet("labelingRuleList-"+args.htmlid, "labelingRuleListDashlet",msg.get("dashlet.labelingRuleList.title"),"bcpg:labelingRuleList"));
model.widgets = model.widgets.concat(createDashlet("ingLabelingList-"+args.htmlid, "ingLabelingListDashlet"));

}

parseActions(page.url.args.list!=null ?page.url.args.list : null);   
main();
