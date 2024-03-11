
//beCPG


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
   
   //beCPG
   model.capabilities["isbeCPGSystemManager"] =false;
  
   model.capabilities["isbeCPGExternalUser"] = false;
   model.capabilities["isbeCPGLanguageMgr"] = false;
   
   var languageMgrGroup = people.getGroup("GROUP_LanguageMgr");

   var isOlapUser = false, isMemberOfAllowedGroup = false;

   var isAdmin = false;
   
   if(languageMgrGroup){
      if(people.getMembers(languageMgrGroup, false).length == 0){
    	  model.capabilities["isbeCPGLanguageMgr"] = true;
      }         
   }
   
   var groups = people.getContainerGroups(object);
   for (var i=0;i<groups.length;i++) {
       if("GROUP_ExternalUser" == groups[i].properties["cm:authorityName"]){
    	   model.capabilities["isbeCPGExternalUser"] = true;
       }
       if("GROUP_LanguageMgr" == groups[i].properties["cm:authorityName"]){
    	   model.capabilities["isbeCPGLanguageMgr"] = true;
       }
       if("GROUP_SystemMgr" == groups[i].properties["cm:authorityName"]){
    	   model.capabilities["isbeCPGSystemManager"] = true;
       }
       if("GROUP_OlapUser" == groups[i].properties["cm:authorityName"]){
    	   isOlapUser = true;
       }
       if("GROUP_AiUser" == groups[i].properties["cm:authorityName"]){
    	  model.capabilities["isAIUser"] = true;
       }
       if("GROUP_ALFRESCO_ADMINISTRATORS" == groups[i].properties["cm:authorityName"]){
    	   isAdmin = true;
       }

       if("GROUP_LicenseWriteNamed" == groups[i].properties["cm:authorityName"]
       	|| "GROUP_LicenseReadNamed" == groups[i].properties["cm:authorityName"]
       	|| "GROUP_LicenseWriteConcurrent" == groups[i].properties["cm:authorityName"]
       	|| "GROUP_LicenseReadConcurrent" == groups[i].properties["cm:authorityName"]
       	|| "GROUP_LicenseSupplierConcurrent" == groups[i].properties["cm:authorityName"]){
    	   isMemberOfAllowedGroup = true;
       }
   }
   
   var olapSSOUrl = bcpg.getOlapSSOUrl();
   if(olapSSOUrl!=null && isOlapUser){
       model.capabilities["olapSSOUrl_"+olapSSOUrl] = true;
   }
    
  var personNodeRef = object.nodeRef;
  if(personNodeRef!=null){
      model.capabilities["personNodeRef_"+personNodeRef] = true;
  }
  

  var userLocale = bcpg.getUserLocale(object);
  if(userLocale!=null){
      model.capabilities["userLocale_"+userLocale] = true;
  } 
  

  var userContentLocale = bcpg.getUserContentLocale(object);
  if(userContentLocale!=null){
      model.capabilities["userContentLocale_"+userContentLocale] = true;
  }
	
	model.capabilities["isLicenseValid"] = !bcpg.isShowLicenceWarning()  || bcpg.isLicenseValid() || !isAdmin;
	model.capabilities["isMemberOfLicenseGroup"] = !bcpg.isShowUnauthorizedWarning() || userId == "admin"|| userId.endsWith("@becpg.fr") || isMemberOfAllowedGroup;

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