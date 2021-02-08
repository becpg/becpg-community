<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


function main()
{
	
	model.view = page.url.args.view ? page.url.args.view: "dataTable";
	
	var site = page.url.templateArgs.site;
	var prefs = "org.alfresco.share.project.list."+ model.view  +"."+ (site!= null && site.length>0 ? site : "home");
		
	model.preferences = AlfrescoUtil.getPreferences(prefs);
	 
	   
	// Widget instantiation metadata...
	var projectListToolbar = {
	   id : "ProjectListToolbar", 
	   name : "beCPG.component.ProjectListToolbar",
	   options : {
	      siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
	      view : model.view,
	      prefsId : prefs,
	      simpleView :  model.preferences.simpleView !=null ?  model.preferences.simpleView : false
	   }
	};
	
	model.widgets = [projectListToolbar];
}


main();
