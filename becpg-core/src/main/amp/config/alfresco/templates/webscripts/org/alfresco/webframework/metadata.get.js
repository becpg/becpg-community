
//beCPG
function isbeCPGSystemMember(user){
    var groups = people.getContainerGroups(user);
    for (var i=0;i<groups.length;i++) {
        if("GROUP_SystemMgr" == groups[i].properties["cm:authorityName"]){
            return true;
        }
    }
    return false;
}

function isbeCPGExternalUser(user){
    var groups = people.getContainerGroups(user);
    for (var i=0;i<groups.length;i++) {
        if("GROUP_ExternalUser" == groups[i].properties["cm:authorityName"]){
            return true;
        }
    }
    return false;
}


model.includeChildren = true;
model.includeContent = false;
model.isUser = false;

model.code = "ERROR";

var object = null;

// allow for content to be loaded from id
if (args["id"] != null)
{
	var id = args["id"];
	object = search.findNode(id);
}

// if not by id, then allow for user id
else if (args["user"] != null)
{
   var userId = args["user"];
   object = people.getPerson(userId);
   model.isUser = true; 
   model.includeChildren = false;
   model.capabilities = people.getCapabilities(object);
   
   model.capabilities["isbeCPGSystemManager"] = isbeCPGSystemMember(object);
   model.capabilities["isbeCPGExternalUser"] = isbeCPGExternalUser(object);
   
   var olapSSOUrl = bcpg.getOlapSSOUrl();
   if(olapSSOUrl!=null){
       model.capabilities["olapSSOUrl_"+olapSSOUrl] = true;
   }
   
   
   model.immutableProperties = people.getImmutableProperties(userId);
}

// load content by relative path
else
{
	var path = args["path"];
	if (path == null || path == "" || path == "/")
	{
		path = "/Company Home";
	}
	
	// look up the content by path
	object = roothome.childByNamePath(path);
}

if (object != null)
{
	model.code = "OK";
}

model.object = object;