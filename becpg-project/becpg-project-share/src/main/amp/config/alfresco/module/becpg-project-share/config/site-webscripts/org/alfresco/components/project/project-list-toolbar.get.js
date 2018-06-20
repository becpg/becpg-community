<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/workflow/filter/filter.lib.js">


function main()
{
	var site = page.url.templateArgs.site;
	var prefs = "org.alfresco.share.project.list";

	if(site!=null && site.length>0){
	   prefs+="."+site;
	}

    model.preferences = AlfrescoUtil.getPreferences(prefs);
	model.filters = getFilters();
	model.view = (page.url.args.view != null) ? page.url.args.view : "dataTable";
	
	   
	// Widget instantiation metadata...
	var projectListToolbar = {
	   id : "ProjectListToolbar", 
	   name : "beCPG.component.ProjectListToolbar",
	   options : {
	      siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
	      view : (page.url.args.view != null) ? page.url.args.view : "dataTable",
	      prefsId : prefs,
	      simpleView :  model.preferences.simpleView !=null ?  model.preferences.simpleView : false
	   }
	};
	
	model.widgets = [projectListToolbar];
}


main();
