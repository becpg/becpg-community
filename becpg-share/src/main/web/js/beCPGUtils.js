
/**
 * Asset location helper class.
 *
 * @namespace Alfresco
 * @class Alfresco.Location
 */
(function()
{
	
   /**
    * Alfresco Slingshot aliases
    */
   var  $siteURL = Alfresco.util.siteURL;
   
   
   
   beCPG.util.entityCharactURL = function(siteId, nodeRef , type) {
   	
   	nodeRef = new Alfresco.util.NodeRef(nodeRef);
   	
   	var redirect  = $siteURL("entity-data-lists?nodeRef="+nodeRef.toString(),
      {
         site: siteId
      });
   	
   	if(type == "bcpg:finishedProduct" || type == "bcpg:semiFinishedProduct"){
   		redirect+="&list=compoList";
   	}
		else if(type == "bcpg:packagingKit"){
			redirect+="&list=packagingList";
		}
   	
   	return redirect;
   	
   };
   
   
   beCPG.util.entityDocumentsURL = function(siteId,path,name){
   	 var url= null; 
   		
   	if (siteId) {
   		url = 'documentlibrary?path=' + encodeURIComponent('/' + path + '/' + name);
        } else {
           if (path) {
              url = 'repository?path='
                    + encodeURIComponent('/' + path.split('/').slice(2).join('/') + '/' + name);
           }
        }
   	if(url!=null){
   		url = $siteURL(url,{site:siteId});
   	}
   	return url;
   	
   };
   
   beCPG.util.entityDetailsURL = function (siteId, nodeRef, type){
   	nodeRef = new Alfresco.util.NodeRef(nodeRef);
   	
   	return $siteURL("entity-details?nodeRef=" + nodeRef.toString(),{site:siteId});
   	
   };
   
//   buildSpaceNamePath = function(pathParts, name) {
//
//   	
//  	 //TODO remove when V2
//   if (me.options.list == "taskList" || me.options.list == "deliverableList") {
//  	 return (pathParts.length !== 0 ? ("/" + pathParts.join("/")) : "") + "/" + name;
//      }
//  	
//	   return (pathParts.length !== 0 ? ("/" + pathParts.join("/")) : "");
//  };
//
//  // rep:reportEntityAspect
//  if (this.options.siteId) {
//	   url = Alfresco.constants.URL_PAGECONTEXT + "site/" + this.options.siteId + "/" + "documentlibrary?path="
//	         + encodeURIComponent(buildSpaceNamePath( this.entity.path.split("/").slice(5), this.entity.name));
//  } else {
//	   url = Alfresco.constants.URL_PAGECONTEXT + "repository?path="
//	         + encodeURIComponent(buildSpaceNamePath(this.entity.path.split("/").slice(2),  this.entity.name));
//  }
   
   
})();