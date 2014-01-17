/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
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
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">


function main()
{
   parseActions();
   
   //Widget instantiation metadata...
   var entityDataGrid = {
    id : "entityDataGrid", 
    name : "beCPG.module.EntityDataGrid",
    options : {
       siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
       usePagination: args.pagination!=null ? args.pagination!="false" : false,
       useFilter: args.filter!=null ? args.filter!="false" : false,
       entityNodeRef: page.url.args.nodeRef!=null ?page.url.args.nodeRef : "",
       list: page.url.args.list!=null ?page.url.args.list : "",
       sortable : true,
       sortUrl : page.url.context+"/proxy/alfresco/" + "becpg/entity/datalists/sort/node",
       dataUrl : page.url.context+"/proxy/alfresco/" +  (args.dataUrl!=null ? args.dataUrl :"becpg/entity/datalists/data/node"),
       itemUrl : page.url.context+"/proxy/alfresco/" +  (args.itemUrl!=null ? args.itemUrl :"becpg/entity/datalists/item/node/"),
       saveFieldUrl : page.url.context+"/proxy/alfresco/" + "becpg/bulkedit/save"
      }
   };
    
   model.widgets = [entityDataGrid];
}


main();
