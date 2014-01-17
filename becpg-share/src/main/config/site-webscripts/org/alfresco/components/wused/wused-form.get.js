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
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


function main()
{
 AlfrescoUtil.param('type', null);
 AlfrescoUtil.param('nodeRefs', "");
 AlfrescoUtil.param('assocName', null);
 
 

   model.itemTypes = [];
   
   var url = "/becpg/dictionnary/entity";
   if(model.type){
      url+="?itemType=" + model.type;
   } else if(model.assocName){
      url+="?assocName=" + model.assocName;
   }
   
     
      // Call the repository for the site profile
 var json = remote.call(url);
      if (json.status == 200)
      {
         // Create javascript objects from the repo response
         var obj = eval('(' + json + ')');
         if (obj && obj.items)
         {
           model.itemTypes = obj.items;
         } 
         if(obj.type){
            model.type = obj.type;
         }
     }
   
 //Widget instantiation metadata...
   var wUsedForm = {
    id : "wUsedForm", 
    name : "beCPG.component.WUsedForm",
    options : {
       type: model.type,
       nodeRefs : model.nodeRefs
      }
   };
    
   
   model.widgets = [wUsedForm];

}

main();


