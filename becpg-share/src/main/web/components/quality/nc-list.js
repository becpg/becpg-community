(function() {

   if (beCPG.module.EntityDataGridRenderers) {

      YAHOO.Bubbling
            .fire(
                  "registerDataGridRenderer",
                  {
                     propertyName : "cm:name",
                     renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {

                        var record = oRecord.getData(), url = beCPG.util
                              .entityDetailsURL(record.siteId, record.nodeRef), version = "", priorityImg = "", priority = record.itemData["prop_qa_ncPriority"].value, priorityMap = {
                           "1" : "high",
                           "2" : "medium",
                           "3" : "low"
                        }, priorityKey = priorityMap[priority + ""], title = record.itemData["prop_cm_name"].displayValue
                        , urlFolder = beCPG.util.entityDocumentsURL(record.siteId, record.path, title);
                        if (data.version && data.version !== "") {
                           version = '<span class="document-version">' + data.version + '</span>';
                        }

                        priorityImg = '<img class="priority" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/priority-' + priorityKey + '-16.png" title="' + scope
                              .msg("label.priority", scope.msg("priority." + priorityKey)) + '"/>';
                        
                        if (record.version && record.version !== "") {
                           version = '<span class="document-version">' + record.version + '</span>';
                        }

                        Dom.setStyle(elCell, "width", "12em");
                        Dom.setStyle(elCell.parentNode, "width", "12em");
                        
                        return '<span class="nc ' + record.itemData["prop_qa_ncType"].value + '" ><a href="' + url + '">' + priorityImg  + "&nbsp;" + Alfresco.util
                              .encodeHTML(title) + '</a>&nbsp;<a class="folder-link" href="' + urlFolder + '" title="' + scope
                              .msg("link.title.open-folder") + '">&nbsp;</a></span>' + version;

                     }

                  });
      
      YAHOO.Bubbling
      .fire(
            "registerDataGridRenderer",
            {
               propertyName : ["cm:creator","qa:claimResponseActor"],
               renderer : function(oRecord, data, label, scope) {
                  var record = oRecord.getData();
                  return  '<span class="avatar" title="' + record.createdBy.displayValue + '">'+ Alfresco.Share.userAvatar(record.createdBy.value, 32)+"&nbsp;" + record.createdBy.displayValue+"</span>";
                  
               }

            });
      
      
      YAHOO.Bubbling.fire("registerDataGridRenderer", {
         propertyName :  "qa:ncState" ,
         renderer : function(oRecord, data, label, scope) {        
           if(data.displayValue != null){
               var msgKey = "status.quality-" + data.displayValue.toLowerCase(), msgValue = scope.msg(msgKey);
               if(msgKey != msgValue){
                   return Alfresco.util.encodeHTML(msgValue);
               }           
           }
           return Alfresco.util.encodeHTML(data.displayValue);
         }
      });
      

   }

   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
      actionName : "create-nc",
      evaluate : function(asset, entity) {
         return asset.name !== null && (asset.name === "ncList");
      },
      fn : function(instance) {
         document.location.href = Alfresco.util.siteURL("start-workflow?referrer=nc-list&myWorkflowsLinkBack=true");
      }
   });
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
      actionName : "export-csv",
      right : true,
      evaluate : function(asset, entity) {
         return asset.name !== null && (asset.name === "ncList");
      },
      fn : function(instance) {
         
         //TODO pageSize
          var dt =   Alfresco.util.ComponentManager.find({name: "beCPG.module.EntityDataGrid"})[0];
         
         document.location.href = dt._getDataUrl()+"&format=csv&metadata="+encodeURIComponent(YAHOO.lang.JSON.stringify(dt._buildDataGridParams()));
      }
   });
 

})();
