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
function createDashlet(dashletId, dashletName, dashletTitle, itemType, disableResize){

   var entityDataGrid = {
         id : "entityDataGrid", 
         name : "beCPG.module.EntityDataGrid",
         initArgs : itemType ? ["\"" + dashletId + "\"", "\"true\""] : ["\"" + dashletId + "\""],
         options : {
            entityNodeRef: page.url.args.nodeRef!=null ?page.url.args.nodeRef : "",
            siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
            list: page.url.args.list!=null ?page.url.args.list : "",
            dataUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/data/node/",
            itemUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/item/node/",
            usePagination: true,
            displayBottomPagination : false,
            useFilter: true,
            sortable : true,
            sortUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/sort/node",
            itemType : itemType ? itemType : null,
            saveFieldUrl : page.url.context+"/proxy/alfresco/becpg/bulkedit/save",
            hiddenColumns : ["prop_bcpg_depthLevel"],
            initHistoryManager : false
           }
        };
         
        if(!disableResize){
              var dashletResizer = {
                 id : "DashletResizer",
                 name : "beCPG.widget.DashletResizer",
                 initArgs : ["\"" + dashletId + "\"", "\"" + dashletName + "\""],
                 useMessages: false
              };
              
              
              
              var prefs = AlfrescoUtil.getPreferences("fr.becpg.formulation.dashlet."+dashletName);
        
              if(prefs){
                 if(!model.dashletPrefs){
                    model.dashletPrefs = {};
                 }
                 model.dashletPrefs[dashletId] = prefs;
              }
              
        }
      
      var dashletTitleBarActions = {
         id : "DashletTitleBarActions",
         name : "Alfresco.widget.DashletTitleBarActions",
         initArgs : ["\"" + dashletId + "\""],
         useMessages : false,
         options : {
            actions : [
               {
                  cssClass: "help",
                  bubbleOnClick:
                  {
                     message: itemType ? msg.get("dashlet.help."+itemType.replace(":","_")) :  msg.get("dashlet.help.composition")
                  },
                  tooltip: msg.get("dashlet.help.tooltip")
               }
            ]
         }
      };
      
    
      if(!disableResize){
          return [entityDataGrid, dashletResizer, dashletTitleBarActions];
      }  
      
      return [entityDataGrid, dashletTitleBarActions];
   
}
