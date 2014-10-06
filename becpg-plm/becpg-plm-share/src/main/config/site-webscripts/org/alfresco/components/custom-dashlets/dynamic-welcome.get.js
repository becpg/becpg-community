function override()
{
    // Get the tutorial link from the config (taking care to avoid scripting
    // errors!)
    var tutorial = config.scoped["HelpPages"];
    if (tutorial != null)
    {
        tutorial = tutorial["help-pages"];
        tutorial = (tutorial != null) ? tutorial.getChildValue("share-tutorial") : "";
    }

    function getCreatePFColumn()
    {
        return (
        {
            title : "welcome.user.product.title",
            description : "welcome.user.product.description",
            imageUrl : "/res/components/images/filetypes/generic-finishedProduct-64.png",
            actionMsg : "welcome.user.product.link",
            actionHref : "#",
            actionId : "-createProduct-button",
            actionTarget : null
        });
    }

    function getCreateProjectColumn()
    {
        return (
        {
            title : "welcome.user.project.title",
            description : "welcome.user.project.description",
            imageUrl : "/res/components/images/filetypes/generic-project-64.png",
            actionMsg : "welcome.user.project.link",
            actionHref : "#",
            actionId : "-createProject-button",
            actionTarget : null
        });
    }

    function getImportMPColumn()
    {
        return (
        {
            title : "welcome.user.rawmaterial.title",
            description : "welcome.user.rawmaterial.description",
            imageUrl : "/res/components/images/filetypes/generic-rawMaterial-64.png",
            actionMsg : "welcome.user.rawmaterial.link",
            actionHref : "#",
            actionId : "-importMP-button",
            actionTarget : null
        });
    }

    // Define a set of functions to return common column settings...
    function getTutorialColumn()
    {
        return (
        {
            title : "welcome.user.tutorial.title",
            description : "welcome.user.tutorial.description",
            imageUrl : "/res/components/images/help-tutorial-bw-64.png",
            actionMsg : "welcome.user.tutorial.link",
            actionHref : tutorial,
            actionId : null,
            actionTarget : "_blank"
        });
    }

    var columns = [];
    if (args.dashboardType == "user")
    {

        columns[0] = getCreatePFColumn();
        columns[1] = getImportMPColumn();
        columns[2] = getCreateProjectColumn();
        columns[3] = getTutorialColumn();

        model.columns = columns;

        for (var i = 0; i < model.widgets.length; i++)
        {
            if (model.widgets[i].id == "DynamicWelcome")
            {
                model.widgets[i].name = "beCPG.custom.DynamicWelcome";
                model.widgets[i].options =
                {
                    userHomeNodeRef : user.properties['userHome']
                };

            }
        }

    } else if (args.dashboardType == "site")  {
        
        var sitePreset = null,siteId = null;
        try
        {
           // Call the repository for the site profile
           var json = remote.call("/api/sites/" + page.url.templateArgs.site);
          
           if (json.status == 200)
           {
              // Create javascript objects from the repo response
              var obj = eval('(' + json + ')');
              if (obj)
              {
                  sitePreset = obj.sitePreset;
                  siteId = obj.shortName;
              }
           }

        }
        catch (e)
        {
        }
        
        if(sitePreset === "product-site-dashboard"){
            model.columns[2] = getCreatePFColumn();
            model.columns[3] = getImportMPColumn();
        } else if(sitePreset === "project-site-dashboard"){
            model.columns[3] = getCreateProjectColumn();
            
            
        } else {
            model.columns[3] = getTutorialColumn();
        }
        
        
        for (var i = 0; i < model.widgets.length; i++)
        {
            if (model.widgets[i].id == "DynamicWelcome")
            {
                model.widgets[i].name = "beCPG.custom.DynamicWelcome";
                model.widgets[i].options =
                {
                    siteId : siteId   
                };

            }
        }
        
    }

}

override();
