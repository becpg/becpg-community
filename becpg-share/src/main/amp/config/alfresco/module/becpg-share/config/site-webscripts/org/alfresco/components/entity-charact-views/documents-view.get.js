<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/include/toolbar.lib.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/upload/uploadable.lib.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/include/documentlist.lib.js">

doclibCommon();

function widgets()
{
 
   var docListToolbar = {
      id: "DocListToolbar", 
      name: "beCPG.custom.DocListToolbar",
      assignTo: "docListToolbar",
      options: {
         siteId: (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
    	 rootNode : page.url.args.nodeRef!=null ?page.url.args.nodeRef : "null",
    	 disableSiteMode : true,
         hideNavBar: false,
         repositoryBrowsing: false,
         useTitle: false,
         syncMode: model.syncMode != null ? model.syncMode : "",
         createContentByTemplateEnabled: model.createContentByTemplateEnabled,
         createContentActions: model.createContent
      }
   };
   
   var documentList = {
      id : "DocumentList", 
      name : "beCPG.custom.DocumentList",
      options : {
         syncMode : model.syncMode != null ? model.syncMode : "",        
         disableSiteMode : true,		 
         siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
         containerId : template.properties.container != null ? template.properties.container : "documentLibrary",
         rootNode : page.url.args.nodeRef!=null ?page.url.args.nodeRef : "null",
         usePagination : true,
         sortAscending : (model.preferences.sortAscending != null ? model.preferences.sortAscending : true),
         sortField : model.preferences.sortField != null ? model.preferences.sortField : "cm:name",
         showFolders : (model.preferences.showFolders != null ? model.preferences.showFolders : true),
         hideNavBar: (model.preferences.hideNavBar != null ? model.preferences.hideNavBar : false),
         simpleView : model.preferences.simpleView != null ? model.preferences.simpleView : "null",
         viewRenderers: model.viewRenderers,
         viewRendererName : model.preferences.viewRendererName != null ? model.preferences.viewRendererName : "detailed",
         viewRendererNames : model.viewRendererNames != null ? model.viewRendererNames : ["simple", "detailed", "gallery"],
         highlightFile : page.url.args["file"] != null ? page.url.args["file"] : "",
         replicationUrlMapping : model.replicationUrlMapping != null ? model.replicationUrlMapping : "{}",
         repositoryBrowsing : false, 
         useTitle : false,
         userIsSiteManager : model.userIsSiteManager,
         associatedToolbar: { _alfValue: "docListToolbar", _alfType: "REFERENCE" }
      }
   };
   if (model.repositoryUrl != null)
   {
      documentList.options.repositoryUrl = model.repositoryUrl;
   }
   
   var documentsView = {
		id : "DocumentsView",
		name : "beCPG.component.DocumentsView",
		options: {
			currVersionNodeRef : page.url.args.nodeRef!=null ?page.url.args.nodeRef : null
		}   
   };
   
   model.widgets = [documentsView, docListToolbar, documentList];
}

widgets();
