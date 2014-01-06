
function main()
{
   
// Widget instantiation metadata...
var projectListToolbar = {
   id : "ProjectListToolbar", 
   name : "beCPG.component.ProjectListToolbar",
   options : {
      siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
      view : (page.url.args.view != null) ? page.url.args.view : "dataTable"
   }
};

model.widgets = [projectListToolbar];
}


main();