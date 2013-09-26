/**
 * beCPG console
 */

function main()
{
    
  
   
   var json = remote.call("/becpg/admin/repository/system-entities");
   if (json.status == 200) {
       var obj = eval('(' + json + ')');
       if (obj) {
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
