var oldUsers = search.luceneSearch("+TYPE:\"cm:person\"  +@cm\\:userName:melodie.azunlopez");
var newUsers = search.luceneSearch("+TYPE:\"cm:person\"  +@cm\\:userName:\"Anne Laure\"");
var assocName = "pjt:tlResources";
 
if(oldUsers.length==1 && newUsers.length==1){
  
  var oldUser = oldUsers[0];
  var newUser = newUsers[0];
  
  
  var sourceNodes = oldUser.sourceAssocs[assocName];
		
		if(sourceNodes != null)
		{
			for (var k = 0; k < sourceNodes.length; k++)
			{
              if(sourceNodes[k].properties["pjt:tlState"] == "InProgress" || sourceNodes[k].properties["pjt:tlState"] == "Planned"){
               	logger.log("update ressource in task " + sourceNodes[k].name + " - " + sourceNodes[k].properties["pjt:tlTaskName"]);
				sourceNodes[k].removeAssociation(oldUser, assocName);
				sourceNodes[k].createAssociation(newUser, assocName); 
              }  
              else{
                logger.log("don't update ressource in task " + sourceNodes[k].name + " - " + sourceNodes[k].properties["pjt:tlTaskName"] + " - " + sourceNodes[k].properties["pjt:tlState"]);
              }
			}
		}
}
