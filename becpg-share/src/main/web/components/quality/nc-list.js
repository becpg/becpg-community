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
                        }, priorityKey = priorityMap[priority + ""], title = record.itemData["prop_cm_name"].displayValue, urlFolder = beCPG.util
                              .entityDocumentsURL(record.siteId, record.path, title);
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

                        return '<span class="nc ' + record.itemData["prop_qa_ncType"].value + '" ><a href="' + url + '">' + priorityImg + "&nbsp;" + Alfresco.util
                              .encodeHTML(title) + '</a>&nbsp;<a class="folder-link" href="' + urlFolder + '" title="' + scope
                              .msg("link.title.open-folder") + '">&nbsp;</a></span>' + version;

                     }

                  });

      YAHOO.Bubbling.fire("registerDataGridRenderer", {
         propertyName : [ "cm:creator", "qa:claimResponseActor" ],
         renderer : function(oRecord, data, label, scope) {
            var record = oRecord.getData();
            return '<span class="avatar" title="' + record.createdBy.displayValue + '">' + Alfresco.Share.userAvatar(
                  record.createdBy.value, 32) + "&nbsp;" + record.createdBy.displayValue + "</span>";

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

   YAHOO.Bubbling
         .fire(
               "registerToolbarButtonAction",
               {
                  actionName : "export-csv",
                  right : true,
                  evaluate : function(asset, entity) {
                     return asset.name !== null && (asset.name === "ncList");
                  },
                  fn : function(instance) {
                     var dt = Alfresco.util.ComponentManager.find({
                        name : "beCPG.module.EntityDataGrid"
                     })[0];

                     Alfresco.util.Ajax
                           .jsonGet({
                              url : dt._getColumnUrl("export"),
                              successCallback : {
                                 fn : function(response) {
                                    

                                    var requestParams = {
                                       fields : [],
                                       filter : dt.currentFilter,
                                       page : 1
                                    };
                                    
                                    requestParams.filter.filterParams = dt._createFilterURLParameters(dt.currentFilter, dt.options.filterParameters);

                                    for ( var i = 0, ii = response.json.columns.length; i < ii; i++) {
                                       var column = response.json.columns[i], columnName = column.name.replace(":", "_");
                                       if (column.dataType == "nested" && column.columns) {
                                          for ( var j = 0; j < column.columns.length; j++) {                                             
                                             var col = column.columns[j];                                            
                                             columnName += "|" + col.name.replace(":", "_");                                             
                                          }
                                       }

                                       requestParams.fields.push(columnName);
                                    }

                                    var PAGE_SIZE = 5000;
                                    
                                    var url = dt._getDataUrl(PAGE_SIZE).replace("/node?","/node.csv?") + "&format=csv&metadata=" + encodeURIComponent(YAHOO.lang.JSON
                                            .stringify(requestParams));
                                    
                                    if (YAHOO.env.ua.ie > 0 && url.length > 2048) {
                                    	alert("GET URL size to large for IE. Try with firefox or chrome");
                                    } else {
                                    	document.location.href = url;
                                    }

                                 },
                                 scope : this
                              }
                           });

                  }
               });

})();
