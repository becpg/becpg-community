<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/document-details/beCPG.lib.js">


function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);
   var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (documentDetails)
   {
	  //beCPG
	  if(!isEntity(documentDetails)){
	      model.document = documentDetails.item;
	      model.repositoryUrl = AlfrescoUtil.getRepositoryUrl();
	  }
   }
}

main();