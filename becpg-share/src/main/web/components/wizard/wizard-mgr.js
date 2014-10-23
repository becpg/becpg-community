(function()
{
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling;

    /**
     * Alfresco Slingshot aliases
     */
    var $siteURL = Alfresco.util.siteURL;

    /**
     * beCPG.component.WizardMgr constructor.
     * 
     * @param {String}
     *            htmlId The HTML id of the parent element
     * @return {beCPG.component.WizardMgr} The new WizardMgr instance
     * @constructor
     */
    beCPG.component.WizardMgr = function WizardMgr_constructor(htmlId)
    {
        beCPG.component.WizardMgr.superclass.constructor.call(this, "beCPG.component.WizardMgr", htmlId);

        Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);
        return this;
    };

    YAHOO
            .extend(
                    beCPG.component.WizardMgr,
                    Alfresco.component.Base,
                    {
                        
                        currentIndex : 0,
                        
                        /**
                         * Object container for initialization options
                         * 
                         * @property options
                         * @type object
                         */
                        options :
                        {
                            siteId : "",
                            
                            nodeRef : "",

                            destination : "",

                            wizardStruct : []
                        },

                        /**
                         * Fired by YUI when parent element is available for
                         * scripting.
                         * 
                         * @method onReady
                         */
                        onReady : function WizardMgr_onReady()
                        {
                            var me = this;

                            // Initialize wizard
                            this.widgets.wizard = jQuery("#" + this.id + "-wizard")
                                    .steps(
                                            {
                                                stepsOrientation : "vertical",
                                                enableCancelButton : true,
                                                labels: {
                                                    cancel: me.msg("wizard.cancel.button"),
                                                    current:  me.msg("wizard.current.step"),
                                                    pagination: me.msg("wizard.pagination"),
                                                    finish:  me.msg("wizard.finish.button"),
                                                    next: me.msg("wizard.next.button"),
                                                    previous: me.msg("wizard.previous.button"),
                                                    loading: me.msg("wizard.loading.msg")
                                                },
                                                onStepChanging : function(event, currentIndex, newIndex)
                                                {
                                                    // Always allow previous
                                                    // action even if the
                                                    // current form is not
                                                    // valid!
                                                    if (currentIndex > newIndex)
                                                    {
                                                        return true;
                                                    }

                                                    var isValid = false;

                                                    var step = me.options.wizardStruct[currentIndex];
                                                    if (step != null)
                                                    {
                                                        if (step.type == "form")
                                                        {
                                                            if (step.form != null)
                                                            {
                                                                Dom.get(me.id + "-step-" + step.id + "-form-submit")
                                                                        .click();
                                                                isValid = step.form
                                                                        .validate(Alfresco.forms.Form.NOTIFICATION_LEVEL_CONTAINER);
                                                            }
                                                        }
                                                        else
                                                        {
                                                            isValid = true;
                                                        }
                                                    }

                                                    return isValid;

                                                },
                                                onStepChanged : function(event, currentIndex, priorIndex)
                                                {
                                                    
                                                    me.currentIndex = currentIndex;
                                                    
                                                    var step = me.options.wizardStruct[priorIndex];
                                                    var nextStep = me.options.wizardStruct[currentIndex];
                                                    // Load next step
                                                    if(step!=null && nextStep!=null && (step.type != "form"  || currentIndex < priorIndex) ){
                                                        if(currentIndex > priorIndex){
                                                            nextStep.nodeRef = step.nodeRef;
                                                        }
                                                        me.loadStep(nextStep);
                                                    }
                                                    

                                                },
                                                onFinished : function (event, currentIndex) { 
                                                    
                                                    var step = me.options.wizardStruct[currentIndex];
                                                    if (step != null)
                                                    {
                                                        if (step.type == "form")
                                                        {
                                                            if (step.form != null)
                                                            {
                                                                Dom.get(me.id + "-step-" + step.id + "-form-submit")
                                                                        .click();
                                                            }
                                                        }
                                                    }
                                                    
                                                    
                                                    me._navigateForward(me.options.wizardStruct[0].nodeRef);
                                                },
                                                onCanceled : function (event, currentIndex) { 
                                                    //If first step is a creation delete project
                                                    if(me.options.nodeRef == "" && 
                                                            me.options.wizardStruct[0].nodeRef ){
                                                          Alfresco.util.Ajax.request({
                                                             method : Alfresco.util.Ajax.DELETE,
                                                             url : Alfresco.constants.PROXY_URI + "slingshot/doclib/action/file/node/"+me.options.wizardStruct[0].nodeRef.replace("://", "/"),
                                                             scope : this,
                                                             execScripts : false
                                                          });
                                                        
                                                    }
                                                    me._navigateForward();
                                                }
                                                

                                            });

                            for (var i = 0; i < this.options.wizardStruct.length; i++)
                            {
                                var step = this.options.wizardStruct[i];
                               
                                this.widgets.wizard.steps("add",
                                {
                                    title : this.msg(step.label),
                                    content : "<div id='" + this.id + "-step-" + step.id + "'>"+this.msg("wizard.loading.msg")+"</div>"
                                });

                                if (i == 0)
                                {
                                    me.loadStep(step);
                                }

                            }

                        },

                        onBeforeFormRuntimeInit : function WizardMgr_onBeforeFormRuntimeInit(layer, args)
                        {

                            var splitted = args[1].eventGroup.split("-step-"), me = this;

                            if (splitted.length == 2)
                            {
                                var stepId = splitted[1];
                                for (var i = 0; i < this.options.wizardStruct.length; i++)
                                {
                                    var step = this.options.wizardStruct[i];
                                    if (step.id + "-form" == stepId)
                                    {
                                        step.form = args[1].runtime;
                                        step.form.setAJAXSubmit(true,
                                        {
                                            successCallback :
                                            {
                                                fn : me.onFormSubmit,
                                                scope : this
                                            }
                                        });
                                    }
                                }

                            }

                        },
                        
                        onFormSubmit : function (response){
                            var me = this; 
                            
                            if(response.json.persistedObject){
                                
                                var step = me.options.wizardStruct[me.currentIndex-1]
                                
                                step.nodeRef = response.json.persistedObject;
                                
                                    var nextStep = me.options.wizardStruct[me.currentIndex];
                                    if (nextStep != null)
                                    {
                                           if(step.nextStepWebScript!=null){
                                               
                                               var url = YAHOO.lang.substitute(
                                                       Alfresco.constants.PROXY_URI + step.nextStepWebScript, {
                                                  nodeRef :   step.nodeRef
                                               });
                                               
                                               Alfresco.util.Ajax.jsonRequest({
                                                   url : url,
                                                   method : "GET",
                                                   successCallback : {
                                                      fn : function(response) {
                                                          nextStep.nodeRef = response.json.nodeRef;
                                                          me.loadStep(nextStep);
                                                          
                                                      },
                                                      scope : this
                                                   }
                                                });
                                           } else {
                                              nextStep.nodeRef = step.nodeRef;
                                              me.loadStep(nextStep);
                                           }
                                        }
                                       
                            }
                        },
                        
                        loadStep :  function WizardMgr_loadStep(step)
                        {

                            if(!step.title || step.title == null){
                                Dom.get(this.id + "-wizardTitle" ).innerHTML ="";
                                Dom.addClass(this.id + "-wizardTitle", "hidden");
                            } else {
                                Dom.get(this.id + "-wizardTitle" ).innerHTML = this.msg(step.title);
                                Dom.removeClass(this.id + "-wizardTitle", "hidden");
                            }
                            
                            
                            if (!step.nodeRef || step.nodeRef == null || step.nodeRef.length < 1)
                            {
                                step.nodeRef = this.options.nodeRef;
                            }
                            
                            if(step.nodeRefStepIndex!=null && step.nodeRefStepIndex!=""){
                                step.nodeRef = this.options.wizardStruct[step.nodeRefStepIndex].nodeRef;
                            }
                            
                            var url = null;
                            
                            if(step.type == "form"){
                                url = YAHOO.lang
                                        .substitute(
                                                Alfresco.constants.URL_SERVICECONTEXT + "components/form" + "?destination={destination}" +
                                                		"&formId={formId}" +
                                                		"&itemId={itemId}" +
                                                		"&itemKind={itemKind}" + 
                                                		"&mode={mode}&submitType=json&&showCancelButton=false&showSubmitButton=true",
                                                {
                                                    mode:  (step.nodeRef != null && step.nodeRef.length > 0) ? "edit" : "create",
                                                    itemKind : (step.nodeRef != null && step.nodeRef.length > 0) ?"node" : "type",
                                                    itemId : (step.nodeRef != null && step.nodeRef.length > 0) ? step.nodeRef : step.itemId,
                                                    destination : this.options.destination,
                                                    formId : step.formId
                                                });
                            } else if(step.type == "entityDataList"){
                                
                               
                                 url =  YAHOO.lang
                                .substitute(
                                        Alfresco.constants.URL_SERVICECONTEXT + "components/entity-charact-views/simple-view" + 
                                        "?list={list}&nodeRef={nodeRef}&itemType={itemType}&title={title}",
                                                {
                                                    nodeRef : step.nodeRef,
                                                    list : step.listId,
                                                    itemType : step.itemId,
                                                    title : encodeURIComponent(step.label)
                                        });
                            }
                            
                            
                            if(url!=null){
                                
                                if(step.type != "entityDataList" || !step.loaded  ){
                                    Alfresco.util.Ajax
                                    .request(
                                    {
                                            url : url,
                                            dataObj :
                                            {
                                                htmlid : this.id + "-step-" + step.id
                                            },
                                            successCallback :
                                            {
                                                fn : function(response)
                                                {
                                                    Dom.get(this.id + "-step-" + step.id).innerHTML = response.serverResponse.responseText;
                                                    step.loaded = true;
                                                    if(step.type == "entityDataList"){
                                                        this.loadDataList(step);
                                                    }
                                                    
                                                },
                                                scope : this
                                            },
                                            scope : this,
                                            execScripts : true
                                    });
                                }
                            }
                        },
                        
                        loadDataList :   function WizardMgr_loadDataList(step){
                            var me = this;
                            Alfresco.util.Ajax.jsonGet({
                                url : Alfresco.constants.PROXY_URI+ "becpg/entitylists/node/" + step.nodeRef.replace(":/", ""),
                                successCallback : {
                                 fn : function (response){
                                      var lists = response.json.datalists, list;
                                      for (var i = 0, ii = lists.length; i < ii; i++) {
                                            list = lists[i];
                                            if(list.name == step.listId){
                                                
                                             YAHOO.Bubbling.fire("simpleView-"+me.id + "-step-" + step.id+"scopedActiveDataListChanged", {
                                                         list : list.name,
                                                          dataList : list
                                                 });
                                                
                                            }
                                  }
                                 },
                                 scope : this
                               }
                   });
                        },
                        
                        /**
                         * Displays the corresponding details page for the current node
                         *
                         * @method _navigateForward
                         * @private
                         * @param nodeRef {Alfresco.util.NodeRef} Optional: NodeRef of just-created content item
                         */
                        _navigateForward: function WizardMgr__navigateForward(nodeRef)
                        {
                           /* Have we been given a nodeRef from the Forms Service? */
                           if (YAHOO.lang.isObject(nodeRef))
                           {
                              window.location.href = $siteURL("entity-details?nodeRef=" + nodeRef.toString());
                           }
                           else if (document.referrer)
                           {
                              /* Did we come from the document library? If so, then direct the user back there */
                              if (document.referrer.match(/documentlibrary([?]|$)/) || document.referrer.match(/repository([?]|$)/))
                              {
                                 // go back to the referrer page
                                 history.go(-1);
                              }
                              else
                              {
                                 document.location.href = document.referrer;
                              }
                           }
                           else if (this.options.siteId && this.options.siteId !== "")
                           {
                              // In a Site, so go back to the document library root
                              window.location.href = $siteURL("documentlibrary");
                           }
                           else
                           {
                              // Nowhere sensible to go other than the default page unless we're in a portal
                              if (Alfresco.constants.PORTLET)
                              {
                                 window.location.href = $siteURL("repository");
                              }
                              else
                              {
                                 window.location.href = Alfresco.constants.URL_CONTEXT;
                              }
                           }
                           
                           
                        }
                        

                    });
})();
