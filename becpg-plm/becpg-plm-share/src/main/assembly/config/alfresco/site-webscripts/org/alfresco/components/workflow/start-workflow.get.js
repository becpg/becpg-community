<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/workflow/workflow.lib.js">


function main() {

	var uri = "/becpg/workflow/start-process?defs=true&nodeRefs=" +( (page.url.args.selectedItems != null) ? page.url.args.selectedItems : "");
	var connector = remote.connect("alfresco");
    var result = connector.get(uri);
    if (result.status.code == status.STATUS_OK && result != "{}")
     {
        var defs = eval('(' + result.response + ')');
        model.workflowDefinitions = defs.processDefinitions;
     }
	

	// Widget instantiation metadata...
	var startWorkflow = {
		id: "StartWorkflow",
		name: "beCPG.component.StartProcess",
		options: {
			failureMessage: "message.failure",
			submitButtonMessageKey: "button.startWorkflow",
			defaultUrl: getSiteUrl("my-tasks"),
			siteId: (page.url.templateArgs.site != null) ? page.url.templateArgs.site : ((args.site != null) ? args.site : ""),
			selectedItems: (page.url.args.selectedItems != null) ? page.url.args.selectedItems : "",
			destination: (page.url.args.destination != null) ? page.url.args.destination : "",
			workflowDefinitions: model.workflowDefinitions
		}
	};
	model.widgets = [startWorkflow];
}

main();
