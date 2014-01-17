/* beCPG : Get the export search templates */
//TODO Move that to ASYNC see project-list-toolbar.js
//Add search widget
function getExportSearchTpls(datatype)
{
   var reportTpls = [];
	  if(datatype!=null && datatype.length>0){
		   try
		   {            
			  var uri = "/becpg/report/exportsearch/templates/" + datatype;
			  var connector = remote.connect("alfresco");
			  var result = connector.get(uri);
		      if (result.status.code == status.STATUS_OK && result != "{}")
		      {
		         var tpls = eval('(' + result.response + ')');
					reportTpls = tpls.reportTpls;
		      }
		   }
		   catch (e)
		   {
		   }
	  }

   return reportTpls;
}
function customSearchMain(){
	//datatype
	var datatype = null;
	if (model.searchQuery !== null && model.searchQuery.length !== 0)
	{
	  var formJson = jsonUtils.toObject(model.searchQuery);
	        
	  if(formJson.length !== 0)
	  {
		  datatype = formJson.datatype;
	  }
	}	
	
	model.exportSearchTpls = getExportSearchTpls(datatype);
	
	var metadatas = config.scoped["Search"]["metadata"].childrenMap["show"];
	var metadataFields = "";
	for (var i = 0, metadata; i < metadatas.size(); i++)
	{
	   if(metadataFields.length>0){
		   metadataFields+=",";
	   }
	   metadata = metadatas.get(i);
	   metadataFields+=metadata.attributes["id"].replace(":","_");
	}
	
	for (var i=0; i<model.widgets.length; i++)
	{
	  if (model.widgets[i].id == "Search")
	  {
	    model.widgets[i].name = "beCPG.custom.Search";
	    model.widgets[i].options.metadataFields = metadataFields;
	  }
	}
}

customSearchMain();
