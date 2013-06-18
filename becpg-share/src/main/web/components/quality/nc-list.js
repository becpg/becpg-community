(function() {

   if (beCPG.module.EntityDataGridRenderers) {

      YAHOO.Bubbling
            .fire(
                  "registerDataGridRenderer",
                  {
                     propertyName : "cm:name",
                     renderer : function(oRecord, data, label, scope) {

                        var record = oRecord.getData(), url = beCPG.util
                              .entityDetailsURL(record.siteId, record.nodeRef), version = "", priorityImg = "", priority = record.itemData["prop_qa_ncPriority"].value, priorityMap = {
                           "1" : "high",
                           "2" : "medium",
                           "3" : "low"
                        }, priorityKey = priorityMap[priority + ""], title = record.itemData["prop_cm_name"].displayValue, code = record.itemData["prop_bcpg_code"].displayValue
                        , urlFolder = beCPG.util.entityDocumentsURL(record.siteId, record.path, title);
                        if (data.version && data.version !== "") {
                           version = '<span class="document-version">' + data.version + '</span>';
                        }

                        priorityImg = '<img class="priority" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/priority-' + priorityKey + '-16.png" title="' + scope
                              .msg("label.priority", scope.msg("priority." + priorityKey)) + '"/>';
                        
                        if (record.version && record.version !== "") {
                           version = '<span class="document-version">' + record.version + '</span>';
                        }

                        return '<span class="nc ' + record.itemData["prop_qa_ncType"].value + '" ><a href="' + url + '">' + priorityImg + code + "&nbsp;-&nbsp;" + Alfresco.util
                              .encodeHTML(title) + '</a><a class="folder-link" href="' + urlFolder + '" title="' + scope
                              .msg("link.title.open-folder") + '">&nbsp;</a></span>' + version;

                     }

                  });
      
      YAHOO.Bubbling
      .fire(
            "registerDataGridRenderer",
            {
               propertyName : "cm:creator",
               renderer : function(oRecord, data, label, scope) {
                  var record = oRecord.getData();
                  return  '<span class="avatar" title="' + record.createdBy.displayValue + '">'+ Alfresco.Share.userAvatar(record.createdBy.value, 32)+"&nbsp;" + record.createdBy.displayValue+"</span>";
                  
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
 

})();
