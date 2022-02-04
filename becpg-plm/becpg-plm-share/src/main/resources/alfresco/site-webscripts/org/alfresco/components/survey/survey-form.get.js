<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


function main() {


	AlfrescoUtil.param('nodeRef');
	AlfrescoUtil.param('list');
	AlfrescoUtil.param('itemType', null);
	AlfrescoUtil.param('title', null);


	var result = remote.connect("alfresco").get('/becpg/survey?entityNodeRef=' + model.nodeRef + "&dataListName="+model.list);
	if (result.status != 200) {
		AlfrescoUtil.error(result.status, 'Could not load survey' + model.nodeRef);
	}
	result = JSON.parse(result);

    model.currentValue=result.data.toString();


	// Widget instantiation metadata...
	var widget = {
		id: "decisionTree",
		name: "beCPG.component.DecisionTree",
		initArgs : ["\"" + args.htmlid + "-control\"","\"" + args.htmlid + "-survey\""],
		options: {
			disabled: false,
			prefix: "survey",
			data: result.def,
			currentValue: result.data
		}
	};
	model.widgets = [widget];


}

main();
