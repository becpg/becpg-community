(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, 
   			Bubbling = YAHOO.Bubbling, 
   			TASK_EVENTCLASS = Alfresco.util.generateDomId(null, "task");
   var COMMENT_EVENTCLASS = Alfresco.util.generateDomId(null, "comment");
   /**
    * ProjectTask constructor.
    * 
    * @param htmlId
    *            {String} The HTML id of the parent element
    * @return {beCPG.component.ProjectTask} The new ProjectTask instance
    * @constructor
    */
   beCPG.component.ProjectTask = function(htmlId) {

      beCPG.component.ProjectTask.superclass.constructor.call(this, "beCPG.component.ProjectTask", htmlId, [ "button",
            "container" ]);

      this.scopeId = htmlId;

      Bubbling.on(this.scopeId + "dataItemUpdated", this.onDataItemUpdated, this);

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.ProjectTask, Alfresco.component.Base);

   /**
    * Augment prototype with Actions module
    */
   YAHOO.lang.augmentProto(beCPG.component.ProjectTask, beCPG.component.ProjectCommons);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang
         .augmentObject(
               beCPG.component.ProjectTask.prototype,
               {
                  /**
                   * Object container for initialization options
                   * 
                   * @property options
                   * @type object
                   */
                  options : {
                     taskNodeRef : null,
                     readOnly : false
                  },

                  /**
                   * Fired by YUI when parent element is available for scripting.
                   * 
                   * @method onReady
                   */
                  onReady : function ProjectTask_onReady() {
                     this.onDataItemUpdated();
                  },

                  onDataItemUpdated : function DT_onDataItemUpdated(layer, args) {
                     if (this.options.taskNodeRef && this.options.taskNodeRef.length > 0) {
                        Alfresco.util.Ajax
                              .request({
                                 url : Alfresco.constants.PROXY_URI + "becpg/project/task?nodeRef=" + this.options.taskNodeRef,
                                 successCallback : {
                                    fn : this.showTaskInfos,
                                    scope : this
                                 }
                              });
                     }
                  },

                  showTaskInfos : function(response) {

                     if (response.json) {

                        var task = response.json.task, deliverables = task.deliverables, me = this;

                        Dom.get(this.id + "-currentTask").innerHTML = this.getTaskTitle(task, task.entityNodeRef);
                        Dom.get(this.id + "-currentTask-description").innerHTML = '<span>' + task.description + '</span>';
                        Dom.get(this.id + "-currentDeliverableList").innerHTML = this.getDeliverableList(deliverables,
                              task.entityNodeRef);
                        Dom.get(this.id + "-prevDeliverableList").innerHTML = this.getDeliverableList(task.prevDeliverables,
                              task.entityNodeRef);
                        
                        if (!this.options.readOnly) {
                           var fnOnShowTaskHandler = function PL__fnOnShowTaskHandler(layer, args) {
                              var owner = Bubbling.getOwnerByTagName(args[1].anchor, "span");
                              if (owner !== null) {
                                 me.onActionShowTask.call(me, owner.className, owner);
                              }
                              return true;
                           };
                           Bubbling.addDefaultAction(TASK_EVENTCLASS, fnOnShowTaskHandler);
                        }
                        
                        var fnOnCommentTaskHandler = function PL__fnOnShowTaskHandler(layer, args) {
                           var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
                           if (owner !== null) {
                             
                              me.onActionCommentTask.call(me, owner.className, owner);
                           }
                           return true;
                        };
                        YAHOO.Bubbling.addDefaultAction(COMMENT_EVENTCLASS, fnOnCommentTaskHandler);
                     }
                  },

                  getDeliverableList : function(deliverables, entityNodeRef) {
                     var deliverableHtlm = "<ul>";

                     for (j in deliverables) {
                        deliverableHtlm += "<li>" + this.getDeliverableTitle(deliverables[j], entityNodeRef) + "</li>";
                     }
                     deliverableHtlm += "</ul>";

                     return deliverableHtlm;

                  },
                  getTaskTitle : function PL_getTaskTitle(task, entityNodeRef) {
                     // var ret = '<span class="node-' + task.nodeRef + '|' + entityNodeRef + '"><a class="theme-color-1
                     // ' + TASK_EVENTCLASS + '" title="' + this
                     // .msg("form.control.project-task.link.title.task-edit") + '" >' + task.name + ' (' +
                     // task.completionPercent + '%)</a></span>';
                     // ret += "</span>";

                     
                     var ret ='<span class="node-' + task.nodeRef + '|' + entityNodeRef + '">' + task.name + ' (' + task.completionPercent + '%)';
                     ret += '<a class="task-comments '+COMMENT_EVENTCLASS+'" title="' + this.msg("link.title.comment-task") + '" href="#" >';
                     
                     if (task.commentCount) {
                        ret += task.commentCount;
                     } else {
                        ret +="&nbsp;";
                     }
                     ret += "</a></span>";
                     
                     return ret;
                     
                  },                  
                  getDeliverableTitle : function PT_getDeliverableTitle(deliverable, entityNodeRef) {

                     var ret = '<span class="delivrable-status delivrable-status-' + deliverable.state + '">';

                     var contents = deliverable.contents;

                     if (contents.length > 0) {
                        ret += '<span class="doc-file"><a title="' + this
                              .msg("form.control.project-task.link.title.open-document") + '" href="' + 
                              beCPG.util.entityDetailsURL(contents[0].siteId,contents[0].nodeRef, "document") 
                              + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
                              .getFileIcon(contents[0].name, "cm:content", 16) + '" /></a></span>';
                     }

                     ret += '<span class="node-' + deliverable.nodeRef + '|' + entityNodeRef + '">';
                     if (!this.options.readOnly) {
                        ret += '<a class="theme-color-1 ' + TASK_EVENTCLASS + '" title="' + this
                              .msg("form.control.project-task.link.title.deliverable-edit") + '" >';
                     }
                     ret += deliverable.name;
                     if (!this.options.readOnly) {
                        ret += '</a>';
                     }
                     ret += '<a class="task-comments '+COMMENT_EVENTCLASS+'" title="' + this.msg("link.title.comment-task") + '" href="#" >';
                     if (deliverable.commentCount) {
                        ret += deliverable.commentCount;
                     } else {
                        ret +="&nbsp;";
                     }
                     ret += "</a>";
                     ret += '</span>';

                     return ret;
                  },

                  _buildCellUrl : function PT__buildCellUrl(content) {
                     var cellUrl;
                     if (content.siteId) {
                        cellUrl = Alfresco.constants.URL_PAGECONTEXT + "site/" + data.siteId + "/" + 'document-details?nodeRef=' + content.nodeRef;
                     } else {
                        cellUrl = Alfresco.constants.URL_PAGECONTEXT + 'document-details?nodeRef=' + content.nodeRef;
                     }
                     return cellUrl;

                  },
                  //TODO Duplicate Method
                  _showPanel : function EntityDataGrid__showPanel(panelUrl, htmlid, itemNodeRef) {
                     
                     var me = this;
                     Alfresco.util.Ajax.request({
                        url : panelUrl,
                        dataObj : {
                           htmlid : htmlid
                        },
                        successCallback : {
                           fn : function(response) {
                              // Inject the template from the XHR request into a new DIV
                              // element
                              var containerDiv = document.createElement("div");
                              containerDiv.innerHTML = response.serverResponse.responseText;

                              // The panel is created from the HTML returned in the XHR
                              // request, not the container
                              var panelDiv = Dom.getFirstChild(containerDiv);
                              this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, {
                                 draggable : true,
                                 width : "50em"
                              });

                              this.widgets.panel.subscribe("hide", function (){
                                 me.onDataItemUpdated();
                              });
                              
                              this.widgets.panel.show();
                              

                           },
                           scope : this
                        },
                        failureMessage : "Could not load dialog template from '" + panelUrl + "'.",
                        scope : this,
                        execScripts : true
                     });
                  }

               }, true);

})();
