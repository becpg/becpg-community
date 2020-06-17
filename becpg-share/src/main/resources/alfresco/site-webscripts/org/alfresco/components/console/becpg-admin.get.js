/*******************************************************************************
 *  Copyright (C) 2010-2020 beCPG. 
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
/**
 * beCPG console
 */

function main()
{
    
   var json = remote.call("/becpg/admin/repository/system-entities");
   if (json.status == 200) {
       var obj = eval('(' + json + ')');
       if (obj) {
    	   
    	   model.systemFolders = new Array();
    	   
    	   if( obj.systemFolders){
	    	   for(var i = 0;i< obj.systemFolders.length; i++){
	    		   var systemFolder = obj.systemFolders[i];
	    		   systemFolder.urlPath = systemFolder.path.split("/").slice(2).join("/")+"/"+systemFolder.name;
	    		   model.systemFolders.push(systemFolder);
	    	   }
    	   }
    	   
    	   if(obj.systemInfo.becpgSchema){
    		   var splitted = obj.systemInfo.becpgSchema.split('-');
	    		if(splitted.length>2){
	    			model.schema = splitted[0];
	    			model.version = splitted[1];
	    			var d = new Date();
	    			d.setTime(splitted[2]);
	    			model.versionDate = new Date(d);
	    		}
    	   }
    	   
    	   
          model.systemEntities = obj.systemEntities;
          model.systemFolders = obj.systemFolders;
          model.systemInfo = obj.systemInfo;
          var adminConsole = {
                id : "AdminConsole", 
                name : "beCPG.component.AdminConsole",
                options : { memory :Math.round((model.systemInfo.totalMemory-model.systemInfo.freeMemory)/model.systemInfo.totalMemory*100)}
             };
          
          model.widgets = [adminConsole];
       }
   }
   
  

}

main();
