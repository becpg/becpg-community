/**
 * ConfigEditor component.
 * 
 * @namespace beCPG
 * @class beCPG.ConfigEditor
 */
(function(){
	
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, KeyListener = YAHOO.util.KeyListener, Lang = YAHOO.util.Lang;
   
   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML, $hasEventInterest = Alfresco.util.hasEventInterest;

   /**
    * ConfigEditor constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @param {String}
    *            currentValueHtmlId The HTML id of the parent element
    * @return {beCPG.ConfigEditor} The new ConfigEditor instance
    * @constructor
    */
   beCPG.ConfigEditor = function beCPG_ConfigEditor(htmlId, currentValueHtmlId) {
      beCPG.ConfigEditor.superclass.constructor.call(this, "beCPG.ConfigEditor", htmlId, [ "button", "menu", "container",
            "resize", "datasource", "datatable" ]);
      this.currentValueHtmlId = currentValueHtmlId;

      /**
       * Decoupled event listeners
       */
      this.eventGroup = htmlId;
      YAHOO.Bubbling.on("formContainerDestroyed", this.onFormContainerDestroyed, this);

      // Initialise prototype properties
      this.editorId = htmlId + "-editor";
      this.columns = [];
      this.selectedItems = {};

      return this;
   };

   YAHOO
         .extend(
               beCPG.ConfigEditor,
               Alfresco.component.Base,
               {
                  /**
                   * Object container for initialization options
                   * 
                   * @property options
                   * @type object
                   */
                  options : {

                     /**
                      * The current value
                      * 
                      * @property currentValue
                      * @type string
                      */
                     currentValue : "",

                     /**
                      * Flag to determine whether the editor is in disabled mode
                      * 
                      * @property disabled
                      * @type boolean
                      * @default false
                      */
                     disabled : false,

                     /**
                      * Flag to indicate whether the field is mandatory
                      * 
                      * @property mandatory
                      * @type boolean
                      * @default false
                      */
                     mandatory : false,

                     /**
                      * Current entityNodeRef
                      */
                     entityNodeRef : null,

                     /**
                      * Current dataList
                      */
                     currentList : "compoList"

                  },


                  /**
                   * Fired by YUI when parent element is available for scripting. Component initialisation, including
                   * instantiation of YUI widgets and event listener binding.
                   * 
                   * @method onReady
                   */
                  onReady : function ConfigEditor_onReady() {
                     if (!this.options.disabled) {

                        var showEditorActionContainerEl = Dom.get(this.id + "-showEditorAction");
                        if (showEditorActionContainerEl) {

                           var showEditorButtonEl = document.createElement("button");
                           showEditorActionContainerEl.appendChild(showEditorButtonEl);

                           this.widgets.showEditorButton = Alfresco.util.createYUIButton(this, null,
                                 this.onShowEditorClick, {
                                    label : this.msg("form.control.spel-editor.show"),
                                    disabled : true
                                 }, showEditorButtonEl);

                        }

                        this.widgets.ok = Alfresco.util.createYUIButton(this, "ok", this.onOK);
                        this.widgets.cancel = Alfresco.util.createYUIButton(this, "cancel", this.onCancel);



                        Dom.get(this.id + "-ok-button").name = "-";
                        Dom.get(this.id + "-cancel-button").name = "-";

                        this.widgets.dialog = Alfresco.util.createYUIPanel(this.editorId, {
                           width : "63em"
                        });
                        this.widgets.dialog.hideEvent.subscribe(this.onCancel, null, this);
                        Dom.addClass(this.editorId, "spel-editor");

                        var instance = this;
                        require([
                    	         "spel/lib/codemirror", "spel/addon/hint/show-hint", "spel/addon/search/searchcursor", "spel/addon/edit/matchbrackets", "spel/mode/javascript/javascript"
                    	       ], function(CodeMirror) {	
                        
                        if(typeof(instance.widgets.brush) == "undefined" || instance.widgets.brush == null){
                        	instance.widgets.brush = new CodeMirror(document.getElementById(instance.id + "-currentValueDisplay"), {
                  				 matchBrackets: true,
						         autoCloseBrackets: true,
						         mode: "application/ld+json",
						         lineWrapping: true
                   			});
                       	 
                        	instance.widgets.brush.setSize("100%",100);
                        }

                        if(typeof(instance.widgets.editor) == "undefined" || instance.widgets.editor == null){
                        	instance.widgets.editor = CodeMirror.fromTextArea(document.getElementById(instance.id + "-editor-textarea"),
                           			{
                           			lineNumbers: true,
   	                        		mode: "application/ld+json",
   	                        		extraKeys: {"Ctrl-Space": "autocomplete"},
   	                        		lineWrapping: true,
   	                        		matchBrackets: true,
						            autoCloseBrackets: true

                           			});
   	                        
                           	CodeMirror.commands.autocomplete = function(cm) {
                                   cm.showHint({hint: CodeMirror.hint.json});
                                 };
                                 //TODO en fonction de la liste charger des hints differents
                                 
                                CodeMirror.registerHelper("hint", "json", function(editor, options) {
                               	 var cur = editor.getCursor(), token = editor.getTokenAt(cur);
                        			 var start = token.start, end = cur.ch;
                        			 
                        			  return {
                        			      list: [
											//Report
											  'componentDatalistsToExtract',
											  'assocsToExtract',
											  'assocsToExtractWithImage',
											  'assocsToExtractWithDataList',
											  'assocsToExtractInDataList',
											  'entityDatalistsToExtract',
											  'multilineProperties',
											  'extractPriceBreaks',
											  'extractRawMaterial',
											  'extractInMultiLevel',
											  'maxCompoListLevelToExtract',
											  'extraImagePaths',
											  'nutLocalesToExtract',
											  'mlTextFields',
											  'mlTextLocales',
											  //Notification, Channel
											  "query",
											  "entityFilter",
											  "entityType",
											  "nodeFilter",
											  "nodeType",
											  "nodePath",
											  "criteria",
											  "dateFilter",
											  "dateField",
											  "dateFilterType",
											  "dateFilterDelay",
											  "dateFilterDelayUnit",
											  "versionFilter",
											  "versionFilterType"
										  ],
                        			             
                        			      from: CodeMirror.Pos(cur.line, start),
                        			      to: CodeMirror.Pos(cur.line, end)
                        			    };
                        			    
                               	  });
                               	  
                               	  
                             
                       	 
                        }
                        
                        instance._renderFormula(true);
                        });
                       
                     }
                  },
                  
                  
                  /**
                   * Gets selected or current value's metadata from the repository
                   * 
                   * @method _renderFormula
                   * @private
                   */
                  _renderFormula : function SpelEditor__renderFormula(updateTextArea) {


                     var nodeRefs = null, instance = this, regexp = new RegExp("(workspace://SpacesStore/[a-z0-9A-Z\-]*)",
                           "gi");
                     
                     
                     nodeRefs = this.options.currentValue.match(regexp);


					function removeDups(names) {
					  var unique = {};
					  names.forEach(function(i) {
					    if(!unique[i]) {
					      unique[i] = true;
					    }
					  });
					  return Object.keys(unique);
					}


					var uniqueNodeRefs = [];
					if(nodeRefs!=null){
						uniqueNodeRefs = removeDups(nodeRefs);
					}

                     function itemsCallBack(response) {
                        var items = null,item,span;
                        if (response != null) {
                           items = response.json.data.items;
                        }
                        
                        instance.widgets.brush.setValue(instance.options.currentValue);
                        instance.widgets.editor.setValue(instance.options.currentValue);
                        
                        if (items != null) {
                           for ( var i = 0, il = items.length; i < il; i++) {
                              item = items[i];
                              
                              var searchCursor = instance.widgets.brush.getSearchCursor(item.nodeRef);
                              var searchCursor2 = instance.widgets.editor.getSearchCursor(item.nodeRef);
   
                              while(searchCursor.findNext()){
                           	     span = document.createElement('span');
                           	     span.innerHTML =$html(item.name);
                           	     span.className = "spel-editor-nodeRef";
                           	     instance.widgets.brush.markText(searchCursor.from(), searchCursor.to(), {replacedWith:span } );
                              }
                              
                              while(searchCursor2.findNext()){
                              	 span = document.createElement('span');
                              	 span.innerHTML =$html(item.name);
                              	 span.className = "spel-editor-nodeRef";
                              	 instance.widgets.editor.markText(searchCursor2.from(), searchCursor2.to(), {replacedWith:span } );
                               }
                              
                           }
                        }
                        if(updateTextArea){
                         instance._enableActions();
                        }

                     }

                     if (uniqueNodeRefs != null && uniqueNodeRefs.length > 0) {

                        Alfresco.util.Ajax.jsonRequest({
                           url : Alfresco.constants.PROXY_URI + "api/forms/picker/items",
                           method : "POST",
                           dataObj : {
                              items : uniqueNodeRefs
                           },
                           successCallback : {
                              fn : itemsCallBack,
                              scope : this
                           },
                           failureCallback : {
                              fn : function() {

                                 Alfresco.util.PopupManager.displayMessage({
                                    text : this.msg("message.spel-editor.failure")
                                 });
                              },
                              scope : this
                           }
                        });

                     } else {
                        itemsCallBack();
                     }

                  },


                  /**
                   * Destroy method - deregister Bubbling event handlers
                   * 
                   * @method destroy
                   */
                  destroy : function ConfigEditor_destroy() {
                     try {
                        YAHOO.Bubbling.unsubscribe("formContainerDestroyed", this.onFormContainerDestroyed, this);
                     } catch (e) {
                        // Ignore
                     }
                     beCPG.ConfigEditor.superclass.destroy.call(this);
                  },

                  /**
                   * Add button click handler, shows editor
                   * 
                   * @method onShowEditorClick
                   * @param e
                   *            {object} DomEvent
                   * @param p_obj
                   *            {object} Object passed back from addListener method
                   */
                  onShowEditorClick : function ConfigEditor_onShowEditorClick(e, p_obj) {
                     // Register the ESC key to close the dialog
                     if (!this.widgets.escapeListener) {
                        this.widgets.escapeListener = new KeyListener(this.editorId, {
                           keys : KeyListener.KEY.ESCAPE
                        }, {
                           fn : function ConfigEditor_onShowEditorClick_fn(eventName, keyEvent) {
                              this.onCancel();
                              Event.stopEvent(keyEvent[1]);
                           },
                           scope : this,
                           correctScope : true
                        });
                     }
                     this.widgets.escapeListener.enable();
                     this.widgets.editor.focus();
                     this.widgets.dialog.show();

                     this._createResizer();
                     this._fireRefreshEvent();

                     p_obj.set("disabled", true);
                     Event.preventDefault(e);
                  },

                  /**
                   * Editor OK button click handler
                   * 
                   * @method onOK
                   * @param e
                   *            {object} DomEvent
                   * @param p_obj
                   *            {object} Object passed back from addListener method
                   */
                  onOK : function ConfigEditor_onOK(e, p_obj) {
                     this.widgets.escapeListener.disable();
                     this.widgets.dialog.hide();
                     this.widgets.showEditorButton.set("disabled", false);
                     if (e) {
                        Event.preventDefault(e);
                     }

                     this.options.currentValue = this.widgets.editor.getValue(); 
                     
                     
                     
                     Dom.get(this.currentValueHtmlId).value = this.options.currentValue;
                     YAHOO.Bubbling.fire("mandatoryControlValueUpdated", Dom.get(this.currentValueHtmlId));

                     this._renderFormula(false);
                  },


                  /**
                   * Editor Cancel button click handler
                   * 
                   * @method onCancel
                   * @param e
                   *            {object} DomEvent
                   * @param p_obj
                   *            {object} Object passed back from addListener method
                   */
                  onCancel : function ConfigEditor_onCancel(e, p_obj) {
                     this.widgets.escapeListener.disable();
                     this.widgets.dialog.hide();
                     this.widgets.showEditorButton.set("disabled", false);
                     if (e) {
                        Event.preventDefault(e);
                     }
                  },

                  /**
                   * Notification that form is being destroyed.
                   * 
                   * @method onFormContainerDestroyed
                   * @param layer
                   *            {object} Event fired (unused)
                   * @param args
                   *            {array} Event parameters
                   */
                  onFormContainerDestroyed : function ConfigEditor_onFormContainerDestroyed(layer, args) {
                     if (this.widgets.dialog) {
                        this.widgets.dialog.destroy();
                        delete this.widgets.dialog;
                     }
                     if (this.widgets.resizer) {
                        this.widgets.resizer.destroy();
                        delete this.widgets.resizer;
                     }
                     if (this.widgets.editor) {
                        this.widgets.editor.toTextArea();
                        delete this.widgets.editor;
                     }
                     
                     if (this.widgets.brush) {
                    	 delete this.widgets.brush;
                     }
                  },

                  /**
                   * Determines whether the editor is in 'authority' mode.
                   * 
                   * @method _enableActions
                   * @private
                   */
                  _enableActions : function ConfigEditor__enableActions() {

                     if (this.widgets.showEditorButton) {
                        // Enable the add button
                        this.widgets.showEditorButton.set("disabled", false);
                     }
                  }
               });
   
})();
   
