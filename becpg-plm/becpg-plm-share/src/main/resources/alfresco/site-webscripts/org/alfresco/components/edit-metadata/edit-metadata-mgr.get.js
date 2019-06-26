<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
	// Need to know what type of node this is - document or folder
	var nodeRef = page.url.args.nodeRef,
	nodeType = "document",
	fileName = "",
	connector = remote.connect("alfresco"),
	result = connector.get("/slingshot/edit-metadata/node/" + nodeRef.replace(":/", "")),
	hasScore=false;

	var entityCatalog = {
			id : "EntityCatalog", 
			name : "beCPG.component.EntityCatalog",
			options : {
				entityNodeRef : nodeRef
			}
	};
	
	if (result.status == 200)
	{
		var metadata = JSON.parse(result);
		nodeType = metadata.node.isContainer ? "folder" : "document";
		fileName = metadata.node.fileName;
		var nodeDetails = AlfrescoUtil.getNodeDetails(nodeRef, null);
		hasScore = nodeDetails.item.node.aspects.indexOf("bcpg:entityScoreAspect") > 0;		
	}

	model.nodeRef = nodeRef;
	model.nodeType = nodeType;
	model.fileName = fileName;
	model.hasScore = hasScore;
	if(hasScore){
		model.widgets = [entityCatalog];
	}
}

main();