<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


function main() {


	AlfrescoUtil.param('nodeRef');
	AlfrescoUtil.param('list');
	AlfrescoUtil.param('itemType', null);
	AlfrescoUtil.param('title', null);
	AlfrescoUtil.param('mode', null);


	var result = remote.connect("alfresco").get('/becpg/survey?entityNodeRef=' + model.nodeRef + "&dataListName="+model.list);
	if (result.status != 200) {
		AlfrescoUtil.error(result.status, 'Could not load survey' + model.nodeRef);
	}
	result = JSON.parse(result);

    model.currentValue=result.data.toString();

    //Ensure question are in a correct order
	var sorted  = result.def.sort(function compare(a, b) {
		  if (a.sort < b.sort)
		     return -1;
		  if (a.sort > b.sort)
		     return 1;
		  return 0;
		}
	);

	// Widget instantiation metadata...
	var widget = {
		id: "decisionTree",
		name: "beCPG.component.DecisionTree",
		initArgs : ["\"" + args.htmlid + "-control\"","\"" + args.htmlid + "-survey\""],
		options: {
			disabled: result.disabled || model.mode == "view",
			prefix: "survey",
			data: sorted,
			currentValue: result.data
		}
	};
	model.widgets = [widget];


}

main();
