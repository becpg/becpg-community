<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/include/documentlist.lib.js">

/*******************************************************************************
 *  Copyright (C) 2010-2026 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
function main()
{
    
    
    AlfrescoUtil.param('nodeRef',null);
    AlfrescoUtil.param('site', null);
    AlfrescoUtil.param('container', 'documentLibrary');

    if(model.nodeRef == null){
        var entityDataListToolbar = {
                id : "EntityDataListToolbar", 
                name : "beCPG.component.EntityDataListToolbar",
                options : {
                   siteId : (model.site != null) ? model.site : null   
                }
             };
        model.widgets = [entityDataListToolbar];

    } else {
        // The action evaluators pull the whole data dictionary (a few MB) from the repository on
        // first use. A truncated or failed answer there must not take the entire toolbar down with
        // a 500: fall back on the same reduced toolbar as when no entity is given.
        var documentDetails = null;
        try
        {
           documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site,
                {
                   actions: true
                });
        }
        catch (e)
        {
           if (logger.isLoggingEnabled())
           {
              logger.log("Unable to read the entity details, rendering the toolbar without them: " + e);
           }
        }

                if (documentDetails)
                {
                   model.documentDetails = true;
                   doclibCommon();
                }
                

                // Widget instantiation metadata...
                var entityDataListToolbar = {
                   id : "EntityDataListToolbar", 
                   name : "beCPG.component.EntityDataListToolbar",
                   options : {
                      nodeRef : model.nodeRef,
                      entityNodeRef : model.nodeRef,
                      siteId : (model.site != null) ? model.site : null,
                      containerId : model.container,
                      rootNode : model.rootNode,
                      replicationUrlMapping : (model.replicationUrlMapping != null) ? model.replicationUrlMapping : "{}",
                      documentDetails : documentDetails,
                      repositoryBrowsing : (model.rootNode != null)       
                   }
                };
                
                if (model.repositoryUrl != null)
                {
                    entityDataListToolbar.options.repositoryUrl = model.repositoryUrl;
                }  
                
                model.widgets = [entityDataListToolbar];
    }
    
   

}

main();
