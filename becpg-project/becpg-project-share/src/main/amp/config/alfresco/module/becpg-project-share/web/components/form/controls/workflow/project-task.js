(function()
{
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling, TASK_EVENTCLASS = Alfresco.util.generateDomId(null, "task");
    var COMMENT_EVENTCLASS = Alfresco.util.generateDomId(null, "comment");
    var DELIVERABLE_STATE_EVENTCLASS = Alfresco.util.generateDomId(null, "deliverableState");
    /**
     * ProjectTask constructor.
     * 
     * @param htmlId
     *            {String} The HTML id of the parent element
     * @return {beCPG.component.ProjectTask} The new ProjectTask instance
     * @constructor
     */
    beCPG.component.ProjectTask = function(htmlId)
    {

        beCPG.component.ProjectTask.superclass.constructor.call(this, "beCPG.component.ProjectTask", htmlId, [
                "button", "container" ]);

        this.scopeId = htmlId;

        Bubbling.on(this.scopeId + "dataItemUpdated", this.onDataItemUpdated, this);

        Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);

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
     * Augment prototype with main class implementation, ensuring overwrite is
     * enabled
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
                        options :
                        {
                            taskNodeRef : null,
                            readOnly : false,
                            transitionField : "prop_pjt_worflowTransition"
                        },

                        /**
                         * Fired by YUI when parent element is available for
                         * scripting.
                         * 
                         * @method onReady
                         */
                        onReady : function ProjectTask_onReady()
                        {
                            this.onDataItemUpdated();
                        },

                        onDataItemUpdated : function DT_onDataItemUpdated(layer, args)
                        {
                            if (this.options.taskNodeRef && this.options.taskNodeRef.length > 0)
                            {
                                Alfresco.util.Ajax
                                        .request(
                                        {
                                            url : Alfresco.constants.PROXY_URI + "becpg/project/task?nodeRef=" + this.options.taskNodeRef,
                                            successCallback :
                                            {
                                                fn : this.showTaskInfos,
                                                scope : this
                                            }
                                        });
                            }
                        },

                        showTaskInfos : function(response)
                        {

                            if (response.json)
                            {

                                var task = response.json.task, deliverables = task.deliverables, me = this;

                                Dom.get(this.id + "-currentTask").innerHTML = this.getTaskTitle(task,
                                        task.entityNodeRef);
                                Dom.get(this.id + "-currentTask-description").innerHTML = '<span>' + task.description + '</span>';
                                
                                
                                if(!task.isRefusedEnabled){
                                    Dom.addClass(this.id.replace("assoc_pjt_workflowTask-cntrl",this.options.transitionField) + "-refused", "hidden");
                                }

                                if (deliverables != null && deliverables.length > 0)
                                {
                                    Dom.get(this.id + "-currentDeliverableList").innerHTML = this.getDeliverableList(
                                            deliverables, task.entityNodeRef);
                                    Dom.removeClass(this.id + "-deliverableList", "hidden");
                                }

                                if (!this.options.readOnly)
                                {
                                    var fnOnShowTaskHandler = function PL__fnOnShowTaskHandler(layer, args)
                                    {
                                        var owner = Bubbling.getOwnerByTagName(args[1].anchor, "span");
                                        if (owner !== null)
                                        {
                                            me.onActionShowTask.call(me, owner.className, owner);
                                        }
                                        return true;
                                    };
                                    Bubbling.addDefaultAction(TASK_EVENTCLASS, fnOnShowTaskHandler);
                                    
                                    var fnOnChangeDeliverableStateHandler = function PL__fnOnChangeDeliverableStateHandler(layer, args)
                                    {
                                        var owner = Bubbling.getOwnerByTagName(args[1].anchor, "span");
                                        if (owner !== null)
                                        {
                                            me.onActionChangeDeliverableState.call(me, owner.className, owner);
                                        }
                                        return true;
                                    };
                                    Bubbling.addDefaultAction(DELIVERABLE_STATE_EVENTCLASS, fnOnChangeDeliverableStateHandler);
                                }

                                var fnOnCommentTaskHandler = function PL__fnOnShowTaskHandler(layer, args)
                                {
                                    var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
                                    if (owner !== null)
                                    {

                                        me.onActionCommentTask.call(me, owner.className, owner);
                                    }
                                    return true;
                                };
                               Bubbling.addDefaultAction(COMMENT_EVENTCLASS, fnOnCommentTaskHandler);
                            }
                        },

                        getDeliverableList : function(deliverables, entityNodeRef)
                        {
                            var deliverableHtlm = "<ul>";

                            for (j in deliverables)
                            {
                                deliverableHtlm += "<li>" + this.getDeliverableTitle(deliverables[j], entityNodeRef) + "</li>";
                            }
                            deliverableHtlm += "</ul>";

                            return deliverableHtlm;

                        },
                        getTaskTitle : function PL_getTaskTitle(task, entityNodeRef)
                        {
                            // var ret = '<span class="node-' + task.nodeRef +
                            // '|' + entityNodeRef + '"><a class="theme-color-1
                            // ' + TASK_EVENTCLASS + '" title="' + this
                            // .msg("form.control.project-task.link.title.task-edit")
                            // + '" >' + task.name + ' (' +
                            // task.completionPercent + '%)</a></span>';
                            // ret += "</span>";

                            var ret = '<span class="node-' + task.nodeRef + '|' + entityNodeRef + '">' + task.name + ' (' + task.completionPercent + '%)';
                            ret += '<a class="task-comments ' + COMMENT_EVENTCLASS +(task.commentCount?" active-comments":"")+ '" title="' + this
                                    .msg("link.title.comment-task") + '" href="#" >';

                            if (task.commentCount)
                            {
                                ret += task.commentCount;
                            }
                            else
                            {
                                ret += "&nbsp;";
                            }
                            ret += "</a></span>";

                            return ret;

                        },
                        getDeliverableTitle : function PT_getDeliverableTitle(deliverable, entityNodeRef)
                        {

                            var ret = "", url = deliverable.url;

                            ret = '<div class="delivrable delivrable-status-' + deliverable.state + '">';
                            ret += '<div class="delivrable-status delivrable-status-' + deliverable.state + '"></div>';
                            
                            /*
                            ret += '<span class="node-' + deliverable.nodeRef + '|' + entityNodeRef + '"><a class="task-comments ' + COMMENT_EVENTCLASS + '" title="' + this
                                    .msg("link.title.comment-task") + '" href="#" >';
                            if (deliverable.commentCount)
                            {
                                ret += deliverable.commentCount;
                            }*/
                            
                            ret += '<div class="delivrable-container">';

                            if (url != null && url.length > 0 && url.indexOf("wizard") > 0)
                            {

                                ret += '<a title="' + this.msg("form.control.project-task.link.title.open-link") + '" href="' + url + '">';
                                ret += '<span class="wizard" ><h2>';
                                ret += this.msg("form.control.project-task.wizard");
                                ret += '</h2>' + deliverable.name + '</span>';
                                ret += '</a>';

                            }
                            else
                            {
                                ret += '<span class="node-' + deliverable.nodeRef + '|' + deliverable.state + '">';

                                ret += '<a class="'+DELIVERABLE_STATE_EVENTCLASS+'" title="' + this
                                .msg("form.control.project-task.change-state") + '" href="#"><span class="delivrable-status delivrable-status-' + deliverable.state + '">&nbsp;</span></a>';

                                var contents = deliverable.contents;

                                var contentUrl =  "";
                                
                                if (url != null && url.length > 0){
                                    contentUrl = url;
                                    
                                    ret += '<span class="doc-url"><a title="' + this
                                    .msg("form.control.project-task.link.title.open-link") + '" href="' + url + '">' + '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/link-16.png" /></a></span>';
                                } else  if (contents.length > 0) {
                                
                                    if( contents[0].isContainer ){
                                       contentUrl = this._getBrowseUrlForFolderPath(contents[0].path, contents[0].siteId, contents[0].name);
                                    } else {
                                       var context =  null;
                                     //TODO Only work in fr or en
                                       if (contents[0].path.indexOf("/User Homes") > 0 || contents[0].path.indexOf("/Espaces Utilisateurs") > 0)
                                       {
                                           context = "mine";
                                       } else if(contents[0].path.indexOf("/Shared") > 0 || contents[0].path.indexOf("/Partagé") > 0) {
                                           context = "shared";
                                       }

                                       contentUrl = beCPG.util.entityURL(contents[0].siteId, contents[0].nodeRef, "document",context);
                                    }
                                        
                                    ret += '<span class="doc-file"><a title="' + this
                                            .msg("form.control.project-task.link.title.open-document") + '" href="' +contentUrl + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
                                            .getFileIcon(contents[0].name, contents[0].isContainer ? "cm:folder" : "cm:content", 16) + '" /></a></span>';
                                }

                               
                                if (contentUrl.length > 0)
                                {
                                    ret += '<a class="theme-color-1 ' + '" title="' + this
                                            .msg("form.control.project-task.link.title.open-document") + '" href="' +contentUrl + '" >';
                                }
                                ret += deliverable.name;
                                if (contentUrl.length > 0)
                                {
                                    ret += '</a>';
                                }
                                ret += '</span>';



                            }

                            ret += '</div></div>';

                            return ret;
                        },

                        
                        onActionChangeDeliverableState : function PL_onActionShowTask(className) {

                            var me = this;
                            
                            var nodes = className.replace("node-", "").split("|");
                            
                            
                             Alfresco.util.Ajax.jsonPost({
                                    url :Alfresco.constants.PROXY_URI + "becpg/bulkedit/save",
                                    dataObj : {
                                          field : "prop_pjt_dlState",    
                                          isMultiple: false,
                                          nodeRef: nodes[0],
                                          value : nodes[1] == "Completed" ? "InProgress" :"Completed"
                                     }, 
                                successCallback : {
                                   fn : function(response) {
                                       
                                      Bubbling.fire(me.scopeId+"dataItemUpdated");
                                   },
                                 scope : this
                                }
                            });


                         },
                        /**
                         * Helper to get folder URL
                         */
                        _getBrowseUrlForFolderPath: function (path, siteId, name)
                        {
                           var url = null;
                           if (siteId)
                           {
                              url = Alfresco.constants.URL_PAGECONTEXT + "site/" + siteId + "/documentlibrary?path=" + encodeURIComponent('/' + path.split('/').slice(5).join('/')+ '/' + name);
                           }
                           else
                           {
                               //TODO Only work in fr or en
                               if (path.indexOf("/User Homes") > 0 || path.indexOf("/Espaces Utilisateurs") > 0)
                               {
                                   url = Alfresco.constants.URL_PAGECONTEXT + "context/mine/myfiles?path=" + encodeURIComponent('/' + path.split('/').slice(4).join('/')+ '/' + name);
                               } else if(path.indexOf("/Shared") > 0 || path.indexOf("/Partagé") > 0) {
                                   url = Alfresco.constants.URL_PAGECONTEXT + "context/shared/sharedfiles?path=" + encodeURIComponent('/' + path.split('/').slice(2).join('/')+ '/' + name);
                               } else {
                                   url = Alfresco.constants.URL_PAGECONTEXT + "repository?path=" + encodeURIComponent('/' + path.split('/').slice(2).join('/')+ '/' + name);
                               }
                           }
                           return url;
                        },

                        /**
                         * Event handler called when the "beforeFormRuntimeInit"
                         * event is received
                         */
                        onBeforeFormRuntimeInit : function FormManager_onBeforeFormRuntimeInit(layer, args)
                        {
                            args[1].runtime.setAJAXSubmit(true,
                            {
                                successCallback :
                                {
                                    fn : this.onFormSubmitSuccess,
                                    scope : this
                                }
                            });
                        },

                        /**
                         * Handler called when the form was submitted
                         * successfully
                         * 
                         * @method onFormSubmitSuccess
                         * @param response
                         *            The response from the submission
                         */
                        onFormSubmitSuccess : function FormManager_onFormSubmitSuccess(response)
                        {

                            document.location.href = Alfresco.constants.URL_CONTEXT;
                        },

                        // TODO Duplicate Method
                        _showPanel : function EntityDataGrid__showPanel(panelUrl, htmlid, itemNodeRef)
                        {

                            var me = this;
                            Alfresco.util.Ajax.request(
                            {
                                url : panelUrl,
                                dataObj :
                                {
                                    htmlid : htmlid
                                },
                                successCallback :
                                {
                                    fn : function(response)
                                    {
                                        // Inject the template from the XHR
                                        // request into a new DIV
                                        // element
                                        var containerDiv = document.createElement("div");
                                        containerDiv.innerHTML = response.serverResponse.responseText;

                                        // The panel is created from the HTML
                                        // returned in the XHR
                                        // request, not the container
                                        var panelDiv = Dom.getFirstChild(containerDiv);
                                        this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv,
                                        {
                                            draggable : true,
                                            width : "50em"
                                        });

                                        this.widgets.panel.subscribe("hide", function()
                                        {
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
