
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
	   
	   var groupName = groups[i].properties["cm:authorityName"];
       if(groupName == "GROUP_ExternalUser"){
    	   model.capabilities["isbeCPGExternalUser"] = true;
       }
       if(groupName == "GROUP_LanguageMgr"){
    	   model.capabilities["isbeCPGLanguageMgr"] = true;
       }
       if(groupName == "GROUP_SystemMgr"){
    	   model.capabilities["isbeCPGSystemManager"] = true;
       }
       if(groupName == "GROUP_OlapUser"){
    	   isOlapUser = true;
       }
       if(groupName == "GROUP_AiUser"){
    	  model.capabilities["isAIUser"] = true;
       }
       if(groupName == "GROUP_ALFRESCO_ADMINISTRATORS"){
    	   isAdmin = true;
       }

       if(groupName == "GROUP_LicenseWriteNamed"
       	|| groupName == "GROUP_LicenseReadNamed"
       	|| groupName == "GROUP_LicenseWriteConcurrent"
       	|| groupName == "GROUP_LicenseReadConcurrent"
       	|| groupName == "GROUP_LicenseSupplierConcurrent"){
    	   isMemberOfAllowedGroup = true;
       }
   }
   
   var olapSSOUrl = bcpg.getOlapSSOUrl();
   if(olapSSOUrl!=null && isOlapUser){
       model.capabilities["olapSSOUrl_"+olapSSOUrl] = true;
   }
   
    var authTocken = bcpg.getBeCPGAuthTocken();
   if(authTocken!=null){
       model.capabilities["beCPGAuthTocken_"+authTocken] = true;
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
	model.capabilities["floatingLicensesExceeded"] = bcpg.isShowUnauthorizedWarning() && userId != "admin" && !userId.endsWith("@becpg.fr") && bcpg.floatingLicensesExceeded(session.getId());
	
	
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