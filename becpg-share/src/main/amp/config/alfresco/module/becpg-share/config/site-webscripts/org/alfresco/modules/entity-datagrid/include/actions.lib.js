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
function parseActions(list)
{
   // Actions
   var myConfig = new XML(config.script),
      actionSetToolbar = [],
      actionSetDataGrid = [];
   
   for each (var xmlAction in myConfig.actionSetToolbar.action)
   {
    if( !list || xmlAction.@list.toString().length < 1 || xmlAction.@list.toString() == list  
    		||	(xmlAction.@list.toString().indexOf("!")==0 &&  xmlAction.@list.toString() != ("!"+list))){
      	actionSetToolbar.push(
         {
            id: xmlAction.@id.toString(),
            type: xmlAction.@type.toString(),
            permission: xmlAction.@permission.toString(),
            asset: xmlAction.@asset.toString(),
            href: xmlAction.@href.toString(),
            label: xmlAction.@label.toString()
         });
      }
   }
   
   for each (var xmlAction in myConfig.actionSetDataGrid.action)
   {
      if( !list || xmlAction.@list.toString().length < 1 || xmlAction.@list.toString() == list
    		  || (xmlAction.@list.toString().indexOf("!")==0 &&  xmlAction.@list.toString() !=  ("!"+list))){
      	actionSetDataGrid.push(
         {
            id: xmlAction.@id.toString(),
            type: xmlAction.@type.toString(),
            permission: xmlAction.@permission.toString(),
            asset: xmlAction.@asset.toString(),
            href: xmlAction.@href.toString(),
            label: xmlAction.@label.toString()
         });
      }
   }
   
   model.actionSetToolbar = actionSetToolbar;
   model.actionSet = actionSetDataGrid;
}
