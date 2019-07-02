<import resource="classpath:alfresco/site-webscripts/org/alfresco/callutils.js">

function getHiddenTaskTypes()
{
   var hiddenTaskTypes = [],
      hiddenTasks = config.scoped["Workflow"]["hidden-tasks"].childrenMap["task"];
   if (hiddenTasks)
   {
      for (var hi = 0, hil = hiddenTasks.size(); hi < hil; hi++)
      {
         hiddenTaskTypes.push(hiddenTasks.get(hi).attributes["type"]);
      }
   }
   return hiddenTaskTypes;
}

function getHiddenWorkflowNames()
{
   var hiddenWorkflowNames = [],
      hiddenWorkflows = config.scoped["Workflow"]["hidden-workflows"].childrenMap["workflow"];
   if (hiddenWorkflows)
   {
      for (var hi = 0, hil = hiddenWorkflows.size(); hi < hil; hi++)
      {
         hiddenWorkflowNames.push(hiddenWorkflows.get(hi).attributes["name"]);
      }
   }
   return hiddenWorkflowNames;
}

function sortByTitle(workflow1, workflow2)
{
   var title1 = (workflow1.title || workflow1.name).toUpperCase(),
      title2 = (workflow2.title || workflow2.name).toUpperCase();
   return (title1 > title2) ? 1 : (title1 < title2) ? -1 : 0;
}

function getWorkflowDefinitions()
{
   var hiddenWorkflowNames = getHiddenWorkflowNames(),
      connector = remote.connect("alfresco"),
      result = connector.get("/api/workflow-definitions?exclude=" + hiddenWorkflowNames.join(","));
   if (result.status == 200)
   {
      var workflows = JSON.parse(result).data;
      workflows.sort(sortByTitle);
      return workflows;
   }
   return [];
}

function getMaxItems()
{
   var myConfig = new XML(config.script),
      maxItems = myConfig["max-items"];
   if (maxItems)
   {
      maxItems = myConfig["max-items"].toString();
   }
   return maxItems && maxItems.length > 0 ? maxItems : null;
}

function getSiteUrl(relativeURL, siteId)
{
    var site_url = relativeURL;

   if (!siteId)
   {
      siteId = (page.url.templateArgs.site != null) ? page.url.templateArgs.site : ((args.site != null) ? args.site : "");
   }

   if (siteId.length > 0)
   {
      site_url = "site/" + siteId + "/" + site_url;
   }

   if (site_url.indexOf("/") == 0)
   {
      site_url = site_url.substring(1);
   }
   if (site_url.indexOf("page/") != 0)
   {
      site_url = "page/" + site_url;
   }
   site_url = url.context + "/" + site_url;

   return site_url;
}

function resolveAuthorities(authorities) {
    var authorityResult = {
        "user": new java.util.ArrayList(),
        "group": new java.util.ArrayList()
    };

    if (authorities.children.size() != 0)
    {
        var childAuthorities = authorities.children.iterator();
        while (childAuthorities.hasNext())
        {
            var childAuthority = childAuthorities.next();
            switch(childAuthority.attributes["type"])
            {
                case "user": authorityResult.user.add(childAuthority.value); break;
                case "group": authorityResult.group.add(childAuthority.value); break;
            }
        }
    }

    return authorityResult;
}

function isNotInPermissionDefinitions(permissionDefinitions, workflowDefinition){
	var permissionDefinitionIterator = permissionDefinitions.iterator();
    while (permissionDefinitionIterator.hasNext())
    {
    	var permissionDefinition = permissionDefinitionIterator.next();
    	if (permissionDefinition.attributes["name"] == workflowDefinition.name)
        {
    		return false;
        }
    }
    return true;
}

function isGroupExistAndHasUsers(fullName){
	var shortName =  fullName.replace("GROUP_", "");
	var json = doGetCall("/api/groups/" + shortName + "/children");
	if(json) {
		var groupChildren = json.data;
		for(var child in groupChildren){
			if(groupChildren[child].authorityType == "USER"){
				return true;
			}else if (groupChildren[child].authorityType == "GROUP"){
				return isGroupExistAndHasUsers(groupChildren[child].fullName)
			}
		}
	}
	
	return false;
}

function containsGroup(groups, group){
	for each (var element in groups){
		if (element == group){
			return true
		}
	}
	return false;
}

function getWorkflowDefinitionsOfCurrentUser()
{
    var person = doGetCall("/api/people/current");
    var workflowDefinitions = getWorkflowDefinitions();
    var workflowConfig = config.scoped["Workflow"];

    var filterCondition = {
        "true": function returnDefinitions(workfowConfig, workflowDefinitions)
        {
            return workflowDefinitions;
        },
        "false": function filterBeforeReturnDefinitions(workflowConfig, workflowDefinitions)
        {
            var permissionWorkflows = workflowConfig["permission-workflows"];
            var permissionDefinitions = permissionWorkflows.getChildren("permission-workflow"), authorities = null;
         
            var defaultAllow = (permissionWorkflows.hasAttribute("default") && permissionWorkflows.attributes["default"] == "allow") ? true : false;
            var workflowDefinitionsResult = new java.util.HashSet();

            var permissionDefinitionIterator = permissionDefinitions.iterator();
            while (permissionDefinitionIterator.hasNext())
            {
                var permissionDefinition = permissionDefinitionIterator.next();
                for each(var workflowDefinition in workflowDefinitions)
                {
                    if (permissionDefinition.attributes["name"] == workflowDefinition.name)
                    {
                        authorities = resolveAuthorities(permissionDefinition.getChild('authorities'));
                        // check authority not define
                        if (authorities.user.length == 0 && authorities.group.length == 0)
                        {
                            continue;
                        }
                        

                        if (authorities.user.contains(person.userName))
                        {
                        	
                            workflowDefinitionsResult.add(workflowDefinition);
                        }
                        else
                        {
                            
                        	for(var i = 0; i < authorities.group.size(); i++)
                            {
                            	var group = authorities.group.get(i);
                           
                                if (containsGroup(person.groups, group) || !isGroupExistAndHasUsers(group) )
                                {
                                    workflowDefinitionsResult.add(workflowDefinition);
                                    break;
                                }
                            }
                        }
                    } else {
                        if (defaultAllow && isNotInPermissionDefinitions(permissionDefinitions, workflowDefinition) ) {
                            workflowDefinitionsResult.add(workflowDefinition);
                        }
                    }
                }
            }

            return workflowDefinitionsResult.toArray().sort(sortByTitle);
        }
    };

    return filterCondition[((person.userName == 'admin') || (workflowConfig["permission-workflows"] == undefined))](workflowConfig, workflowDefinitions);
}

