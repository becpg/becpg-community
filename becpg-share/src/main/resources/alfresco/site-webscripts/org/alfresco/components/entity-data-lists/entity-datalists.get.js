<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 * 
 * This file is part of beCPG
 * 
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/* Sort Options */
function getSortOptions()
{
    var sortOptions = [], sortingConfig = config.scoped["EntityDataLists"]["lists"];

    if (sortingConfig !== null)
    {
        var configs = sortingConfig.getChildren(), configItem, listId, sortIndex;

        if (configs)
        {
            for (var i = 0; i < configs.size(); i++)
            {
                configItem = configs.get(i);
                listId = String(configItem.attributes["id"]);
                sortIndex = String(configItem.attributes["index"]),
                view = String(configItem.attributes["view"]);
                if (listId && sortIndex)
                {
                    sortOptions.push(
                    {
                        id : listId,
                        sortIndex : parseInt(sortIndex),
                        isView : view == "true"
                    });
                }
            }
        }
    }
    return sortOptions;
}

function main()
{
    
    AlfrescoUtil.param("nodeRef");
    var nodeDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
    
    if(nodeDetails){
        model.item = nodeDetails.item;
        model.itemType = model.item.node.type;
     
        model.showCreate = false;
        if( model.item.node.aspects.indexOf("bcpg:entityTplAspect")>0 ||  model.itemType == "bcpg:systemEntity"){
            model.showCreate = true;
        }
    
        // Widget instantiation metadata...
        var entityDataLists =
        {
            id : "EntityDataLists",
            name : "beCPG.component.EntityDataLists",
            options :
            {
                listId : (page.url.args.list != null) ? page.url.args.list : "",
                entityNodeRef : (page.url.args.nodeRef != null) ? page.url.args.nodeRef : "",
                sortOptions : getSortOptions(),
                showCreate : model.showCreate
            }
        };
    
        model.widgets = [ entityDataLists ];
    
    }

}

main();
