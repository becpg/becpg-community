/**
 * SpelEditor component.
 * 
 * @namespace beCPG
 * @class beCPG.SpelEditor
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
    * SpelEditor constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @param {String}
    *            currentValueHtmlId The HTML id of the parent element
    * @return {beCPG.SpelEditor} The new SpelEditor instance
    * @constructor
    */
   beCPG.SpelEditor = function beCPG_SpelEditor(htmlId, currentValueHtmlId) {
      beCPG.SpelEditor.superclass.constructor.call(this, "beCPG.SpelEditor", htmlId, [ "button", "menu", "container",
            "resize", "datasource", "datatable" ]);
      this.currentValueHtmlId = currentValueHtmlId;

      /**
       * Decoupled event listeners
       */
      this.eventGroup = htmlId;
      YAHOO.Bubbling.on("parentTypeChanged", this.onParentTypeChanged, this);
      YAHOO.Bubbling.on("formContainerDestroyed", this.onFormContainerDestroyed, this);
      YAHOO.Bubbling.on("refreshItemList", this.onRefreshItemList, this);

      // Initialise prototype properties
      this.editorId = htmlId + "-editor";
      this.columns = [];
      this.selectedItems = {};

      return this;
   };

   YAHOO
         .extend(
               beCPG.SpelEditor,
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
                      * The selected value to be displayed (but not yet persisted)
                      * 
                      * @property selectedParentType
                      * @type string
                      * @default null
                      */
                     selectedParentType : null,

                     /**
                      * The current value
                      * 
                      * @property currentValue
                      * @type string
                      */
                     currentValue : "",

                     /**
                      * The id of the item being edited
                      * 
                      * @property currentItem
                      * @type string
                      */
                     currentItem : null,

                     /**
                      * Number of characters required for a search
                      * 
                      * @property minSearchTermLength
                      * @type int
                      * @default 1
                      */
                     minSearchTermLength : 1,

                     /**
                      * Maximum number of items to display in the results list
                      * 
                      * @property maxSearchResults
                      * @type int
                      * @default 100
                      */
                     maxSearchResults : 100,

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
                     currentList : "compoList",

                     /**
                      * Datasource
                      */
                     dsr : Alfresco.constants.PROXY_URI + "becpg/autocomplete/speleditor"

                  },

                  /**
                   * Resizable columns
                   * 
                   * @property columns
                   * @type array
                   * @default []
                   */
                  columns : null,

                  /**
                   * Single selected item, for when in single select mode
                   * 
                   * @property singleSelectedItem
                   * @type string
                   */
                  singleSelectedItem : null,

                  /**
                   * Selected items. Keeps a list of selected items for correct Add button state.
                   * 
                   * @property selectedItems
                   * @type object
                   */
                  selectedItems : null,

                  /**
                   * Fired by YUI when parent element is available for scripting. Component initialisation, including
                   * instantiation of YUI widgets and event listener binding.
                   * 
                   * @method onReady
                   */
                  onReady : function SpelEditor_onReady() {
                     this._createItemListControls();
                     if (!this.options.disabled) {

                        this._createNavigationControls();
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

                        // force the generated buttons to have a name of "-" so
                        // it gets ignored in
                        // JSON submit. TODO: remove this when JSON submit
                        // behaviour is configurable
                        Dom.get(this.id + "-ok-button").name = "-";
                        Dom.get(this.id + "-cancel-button").name = "-";

                        this.widgets.dialog = Alfresco.util.createYUIPanel(this.editorId, {
                           width : "63em"
                        });
                        this.widgets.dialog.hideEvent.subscribe(this.onCancel, null, this);
                        Dom.addClass(this.editorId, "spel-editor");

                        var instance = this;
                        require([
                    	         "cm/lib/codemirror", "cm/addon/hint/show-hint", "cm/addon/search/searchcursor", "cm/addon/edit/matchbrackets", "cm/mode/javascript/javascript"
                    	       ], function(CodeMirror) {	
                        
                        if(typeof(instance.widgets.brush) == "undefined" || instance.widgets.brush == null){
                        	instance.widgets.brush = new CodeMirror(document.getElementById(instance.id + "-currentValueDisplay"), {
                  				lineNumbers: true,
                  				lineWrapping: true,
                        		mode: "javascript",
                        		readOnly: true
                   			});
                       	 
                        	instance.widgets.brush.setSize("100%",100);
                        }

                        if(typeof(instance.widgets.editor) == "undefined" || instance.widgets.editor == null){
                        	instance.widgets.editor = CodeMirror.fromTextArea(document.getElementById(instance.id + "-editor-textarea"),
                           			{
                           			lineNumbers: true,
   	                        		mode: "javascript",
   	                        		extraKeys: {"Ctrl-Space": "autocomplete"},
   	                        		lineWrapping: true,
   	                        		matchBrackets: true

                           			});
   	                        
                           	CodeMirror.commands.autocomplete = function(cm) {
                                   cm.showHint({hint: CodeMirror.hint.spel});
                                 };
                                 
                                CodeMirror.registerHelper("hint", "spel", function(editor, options) {
                               	 var cur = editor.getCursor(), token = editor.getTokenAt(cur);
                        			 var start = token.start, end = cur.ch;
                        			 
                        			  return {
                        			      list: [
                        			             'entity',
                        			             'dataListItem',
                        			             'dataListItemEntity', 
                        			             '#this',
                        			             '#root',
                        			             '$Value instanceof T($Type)',
                        			             '$String matches "$Regexp"',
                        			             'T($type)',
                        			             '$false ? $trueExp : $falseExp',
                        			             '$list.?[$property == $value]',
                        			             '$list.![$property]',
                        			             'sum($range,$formula)',
                        			             'avg($range,$formula)',
                        			             'children($compoListDataItem)',
                        			             '@beCPG.findOne($nodeRef)',
                        			             '@beCPG.propValue($nodeRef,"bcpg:productQty")'],
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
                   * Destroy method - deregister Bubbling event handlers
                   * 
                   * @method destroy
                   */
                  destroy : function SpelEditor_destroy() {
                     try {
                        YAHOO.Bubbling.unsubscribe("parentTypeChanged", this.onParentTypeChanged, this);
                        YAHOO.Bubbling.unsubscribe("formContainerDestroyed", this.onFormContainerDestroyed, this);
                        YAHOO.Bubbling.unsubscribe("refreshItemList", this.onRefreshItemList, this);
                     } catch (e) {
                        // Ignore
                     }
                     beCPG.SpelEditor.superclass.destroy.call(this);
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
                  onShowEditorClick : function SpelEditor_onShowEditorClick(e, p_obj) {
                     // Register the ESC key to close the dialog
                     if (!this.widgets.escapeListener) {
                        this.widgets.escapeListener = new KeyListener(this.editorId, {
                           keys : KeyListener.KEY.ESCAPE
                        }, {
                           fn : function SpelEditor_onShowEditorClick_fn(eventName, keyEvent) {
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
                  onOK : function SpelEditor_onOK(e, p_obj) {
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
                  onCancel : function SpelEditor_onCancel(e, p_obj) {
                     this.widgets.escapeListener.disable();
                     this.widgets.dialog.hide();
                     this.widgets.showEditorButton.set("disabled", false);
                     if (e) {
                        Event.preventDefault(e);
                     }
                  },

                  /**
                   * Triggers a search
                   * 
                   * @method onSearch
                   */
                  onSearch : function SpelEditor_onSearch() {
                     var searchTerm = Dom.get(this.editorId + "-searchText").value;
                     if (searchTerm.length < this.options.minSearchTermLength) {
                        searchTerm = null;
                     }

                     // execute search
                     YAHOO.Bubbling.fire("refreshItemList", {
                        eventGroup : this,
                        searchTerm : searchTerm
                     });
                  },

                  /**
                   * BUBBLING LIBRARY EVENT HANDLERS FOR PAGE EVENTS Disconnected event handlers for inter-component
                   * event notification
                   */

                  /**
                   * Parent changed event handler
                   * 
                   * @method onParentTypeChanged
                   * @param layer
                   *            {object} Event fired
                   * @param args
                   *            {array} Event parameters (depends on event type)
                   */
                  onParentTypeChanged : function SpelEditor_onParentTypeChanged(layer, args) {
                     // Check the event is directed towards this instance
                     if ($hasEventInterest(this, args)) {
                        var obj = args[1];
                        if (obj && obj.item) {
                           this.widgets.itemTypeMenu
                                 .set(
                                       "label",
                                       '<div><span class="item-icon"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/ajax_anim.gif" class="icon16" alt="' + this
                                             .msg("message.please-wait") + '"></span><span class="item-name">' + this
                                             ._formatLabel(obj.item.name) + '</span></div>');
                           this.options.selectedParentType = obj.item;
                           this._fireRefreshEvent();
                        }
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
                  onFormContainerDestroyed : function SpelEditor_onFormContainerDestroyed(layer, args) {
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
                   * PRIVATE FUNCTIONS
                   */

                  /**
                   * Gets selected or current value's metadata from the repository
                   * 
                   * @method _renderFormula
                   * @private
                   */
                  _renderFormula : function SpelEditor__renderFormula(updateTextArea) {


                     var items = [], instance = this, regexp = new RegExp("(workspace://SpacesStore/[a-z0-9A-Z\-]*)",
                           "gi");
                     
                     
                     items = this.options.currentValue.match(regexp);

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

                     if (items != null && items.length > 0) {

                        Alfresco.util.Ajax.jsonRequest({
                           url : Alfresco.constants.PROXY_URI + "api/forms/picker/items",
                           method : "POST",
                           dataObj : {
                              items : items
                           },
                           successCallback : {
                              fn : itemsCallBack,
                              scope : this
                           },
                           failureCallback : {
                              fn : function(response) {

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
                   * Creates the UI Navigation controls
                   * 
                   * @method _createNavigationControls
                   * @private
                   */
                  _createNavigationControls : function SpelEditor__createNavigationControls() {
                     var me = this;

                     // Navigation drop-down menu
                     this.widgets.itemTypeMenu = new YAHOO.widget.Button(this.editorId + "-itemType", {
                        type : "menu",
                        menu : this.editorId + "-itemTypeMenu",
                        lazyloadmenu : false
                     });

                     // force the generated buttons to have a name of "-" so it
                     // gets ignored in
                     // JSON submit. TODO: remove this when JSON submit
                     // behaviour is configurable
                     Dom.get(this.editorId + "-itemType-button").name = "-";

                     this.widgets.itemTypeMenu.getMenu().subscribe("click", function(p_sType, p_aArgs) {
                        var menuItem = p_aArgs[1];
                        if (menuItem) {
                           YAHOO.Bubbling.fire("parentTypeChanged", {
                              eventGroup : me,
                              item : menuItem.value
                           });
                        }
                     });

                     // setup search widgets
                     this.widgets.searchButton = new YAHOO.widget.Button(this.editorId + "-searchButton");
                     this.widgets.searchButton.on("click", this.onSearch, this.widgets.searchButton, this);

                     // force the generated buttons to have a name of "-" so it
                     // gets ignored in
                     // JSON submit. TODO: remove this when JSON submit
                     // behaviour is configurable
                     Dom.get(this.editorId + "-searchButton").name = "-";

                     // register the "enter" event on the search text field
                     var zinput = Dom.get(this.editorId + "-searchText");
                     new YAHOO.util.KeyListener(zinput, {
                        keys : 13
                     }, {
                        fn : me.onSearch,
                        scope : this,
                        correctScope : true
                     }, "keydown").enable();

                     var parentTypes = [
                           {
                              name : "product",
                              type : "fr.becpg.repo.product.data.ProductData",
                              template : "{item1}"
                           },
                           {
                              name : "costList",
                              type : "bcpg:cost",
                              subType : "fr.becpg.repo.product.data.productList.CostListDataItem",
                              template : "costList.^[cost.toString() == '{item1}']?.{item2}"
                           },
                           {
                              name : "nutList",
                              type : "bcpg:nut",
                              subType : "fr.becpg.repo.product.data.productList.NutListDataItem",
                              template : "nutList.^[nut.toString() == '{item1}']?.{item2}"
                           },
                           {
                              name : "allergenList",
                              type : "bcpg:allergen",
                              subType : "fr.becpg.repo.product.data.productList.AllergenListDataItem",
                              template : "allergenList.^[allergen.toString() == '{item1}']?.{item2}"
                           },
                           {
                              name : "ingList",
                              type : "bcpg:ing",
                              subType : "fr.becpg.repo.product.data.productList.IngListDataItem",
                              template : "ingList.^[ing.toString() == '{item1}']?.{item2}"
                           },
                           {
                              name : "organoList",
                              type : "bcpg:organo",
                              subType : "fr.becpg.repo.product.data.productList.OrganoListDataItem",
                              template : "organoList.^[organo.toString() == '{item1}']?.{item2}"
                           },
                           {
                              name : "physicoChemList",
                              type : "bcpg:physicoChem",
                              subType : "fr.becpg.repo.product.data.productList.PhysicoChemListDataItem",
                              template : "physicoChemList.^[physicoChem.toString() == '{item1}']?.{item2}"
                           },
                           {
                              name : "microbioList",
                              type : "bcpg:microbio",
                              subType : "fr.becpg.repo.product.data.productList.MicrobioListDataItem",
                              template : "microbioList.^[microBio.toString() == '{item1}']?.{item2}"
                           },
                           {
                              name : "compoList",
                              type : "bcpg:rawMaterial,bcpg:finishedProduct,bcpg:localSemiFinishedProduct,bcpg:semiFinishedProduct",
                              subType : "fr.becpg.repo.product.data.productList.CompoListDataItem",
                              template : "compoListView.compoList.^[product.toString() == '{item1}']?.{item2}"
                           },
                           {
                              name : "processList",
                              type : "bcpg:resourceProduct",
                              subType : "fr.becpg.repo.product.data.productList.ProcessListDataItem",
                              template : "processListView.processList.^[resource.toString() == '{item1}']?.{item2}"
                           },
                           {
                              name : "resourceParamList",
                              type : "mpm:resourceParam",
                              subType : "fr.becpg.repo.product.data.productList.ResourceParamListItem",
                              template : "resourceParamList.^[param.toString() == '{item1}']?.{item2}"
                           }, 
                           {
                               name : "packagingList",
                               type : "bcpg:packagingMaterial,bcpg:packagingKit",
                               subType : "fr.becpg.repo.product.data.productList.PackagingListDataItem",
                               template : "packagingListView.packagingList.^[product.toString() == '{item1}']?.{item2}"
                            },
                           {
                              name : "variables",
                              type : "bcpg:dynamicCharactList",
                              template : "{currentList}View.dynamicCharactList.^[title == '{name1}']?.value"
                           },
                           {
                              name : "labelClaimList",
                              type : "bcpg:labelClaim",
                              subType : "fr.becpg.repo.product.data.productList.LabelClaimListDataItem",
                              template : "labelClaimList.^[labelClaim.toString() == '{item1}']?.{item2}"
                           },
                           {
                              name : "ingLabelingList",
                              type : "bcpg:labelingRuleList",
                              subType : "fr.becpg.repo.product.data.productList.IngLabelingListDataItem",
                              template : "labelingListView.ingLabelingList.^[grp.toString() == '{item1}']?.{item2}"
                           } ];

                     var menuItem, item, label;
                     for ( var i = 0; i < parentTypes.length; i++) {
                        item = parentTypes[i];

                        label = "<span class='" + item.name + "'  >" + this._formatLabel(item.name) + "</span>";

                        menuItem = new YAHOO.widget.MenuItem(label, {
                           value : item
                        });

                        this.widgets.itemTypeMenu.getMenu().addItem(menuItem, 0);
                        if (i == 0) {
                           this.options.selectedParentType = item;
                        }
                     }

                     this.widgets.itemTypeMenu.getMenu().render();

                  },

                  /**
                   * Creates UI controls to support Selected Items
                   * 
                   * @method _createItemListControls
                   * @private
                   */
                  _createItemListControls : function SpelEditor__createItemListControls() {
                     var me = this;

                     if (this.options.disabled === false) {

                        // Setup a DataSource for the selected items list
                        this.widgets.dataSource = new YAHOO.util.DataSource(this.options.dsr, {
                           responseType : YAHOO.util.XHRDataSource.TYPE_JSON,
                           responseSchema : {
                              resultsList : "result",
                              fields : [ "value", "name", "cssClass", "metadatas" ],
                              metaFields : {
                                 page : "page",
                                 pageSize : "pageSize",
                                 fullListSize : "fullListSize"
                              }
                           }
                        });

                        // Editor DataTable definition
                        var columnDefinitions = [ {
                           key : "value",
                           label : "Value",
                           formatter : function(elCell, oRecord, oColumn, oData) {
                              var data = oRecord.getData();

                              elCell.innerHTML = "<span class='defaultIcon " + data.cssClass + "' >" + me
                                    ._formatLabel(data.name) + "</span>";

                           }
                        } ];

                        this.widgets.dataTable1 = new YAHOO.widget.DataTable(this.editorId + "-itemList1",
                              columnDefinitions, this.widgets.dataSource, {
                                 MSG_EMPTY : this.msg("form.control.spel-editor.selected-items.empty"),
                                 initialLoad : false
                              });

                        this.widgets.dataTable2 = new YAHOO.widget.DataTable(this.editorId + "-itemList2",
                              columnDefinitions, this.widgets.dataSource, {
                                 MSG_EMPTY : this.msg("form.control.spel-editor.selected-items.empty"),
                                 initialLoad : false
                              });

                        this.widgets.dataTable1.doBeforeLoadData = function(sRequest, oResponse, oPayload) {
                           if (me.options.selectedParentType) {

                              label = "<span class='" + me.options.selectedParentType.name + "' style='padding-left: 20px;' >" + me
                                    ._formatLabel(me.options.selectedParentType.name) + "</span>";

                              me.widgets.itemTypeMenu.set("label", label);
                           }
                           return true;
                        };

                        this.widgets.dataTable1.subscribe("rowClickEvent", function(oArgs) {
                           var target = oArgs.target, oRecord = this.getRecord(target);

                           var data = oRecord.getData();

                           me.options.currentItem = data;

                           // Highlight
                           me.widgets.dataTable1.onEventSelectRow(oArgs);

                           if (me.options.selectedParentType.subType != null) {
                              me.widgets.dataTable2.load({
                                 request : me._buildParamUrl(me.options.selectedParentType.subType)
                              });
                           } else {

                              var text = Lang.substitute(me.options.selectedParentType.template, {
                                 name1 : data.name,
                                 item1 : data.value,
                                 currentList : me.options.currentList
                              });

                              me.widgets.editor.replaceRange( text, me.widgets.editor.getCursor());
                              
                              var searchCursor = me.widgets.editor.getSearchCursor(me.options.currentItem.value);
                              
                              while(searchCursor.findNext()){
                           	   var span = document.createElement('span');
                           	   span.innerHTML =$html(me.options.currentItem.name);
                           	   me.widgets.editor.markText(searchCursor.from(), searchCursor.to(), {replacedWith:span } );
                              }
                              
                              me.widgets.editor.focus();

                           }
                        });

                        this.widgets.dataTable2.subscribe("rowClickEvent", function(oArgs) {
                           var target = oArgs.target, oRecord = this.getRecord(target);

                           // Highlight
                           me.widgets.dataTable2.onEventSelectRow(oArgs);

                           var data = oRecord.getData();

                           var text = Lang.substitute(me.options.selectedParentType.template, {
                              item1 : me.options.currentItem.value,
                              item2 : data.value,
                              currentList : me.options.currentList
                           });

                           me.widgets.editor.replaceRange( " " + text,  me.widgets.editor.getCursor());
                           
                           var searchCursor = me.widgets.editor.getSearchCursor(me.options.currentItem.value);
                           
                           while(searchCursor.findNext()){
                        	   var span = document.createElement('span');
                        	   span.innerHTML =$html(me.options.currentItem.name);
                        	   me.widgets.editor.markText(searchCursor.from(), searchCursor.to(), {replacedWith:span } );
                           }
                           
                           me.widgets.editor.focus();

                        });

                     }
                  },

                  /**
                   * @returns {String}
                   */
                  _buildParamUrl : function SpelEditor___buildUrl(type, query) {


                     var q = YAHOO.util.Lang
                           .substitute(
                                 "?q={query}&page={page}&pageSize={pageSize}&className={className}&entityNodeRef={entityNodeRef}",
                                 {
                                    query : (!query || query.length < 1) ? "*" : query ,
                                    page : 1,
                                    className : type,
                                    entityNodeRef : this.options.entityNodeRef,
                                    pageSize : this.options.maxSearchResults
                                 });

                     return q;

                  },

                  onRefreshItemList : function SpelEditor_onRefreshItemListfunction(layer, args) {
                     if ($hasEventInterest(this, args)) {

                        this.widgets.dataTable2.deleteRows(0, this.widgets.dataTable2.getRecordSet().getLength());
                        this.widgets.dataTable1.load({
                           request : this._buildParamUrl(this.options.selectedParentType.type, args[1].searchTerm)
                        });

                     }
                  },

                  /**
                   * Fires the refreshItemList event to refresh the contents of the editor.
                   * 
                   * @method _fireRefreshEvent
                   * @private
                   */
                  _fireRefreshEvent : function SpelEditor__fireRefreshEvent() {

                     // get the current search term
                     var searchTermInput = Dom.get(this.editorId + "-searchText");
                     var searchTerm = searchTermInput.value;
                     if (searchTerm.length >= this.options.minSearchTermLength) {
                        // refresh the previous search
                        YAHOO.Bubbling.fire("refreshItemList", {
                           eventGroup : this,
                           searchTerm : searchTerm
                        });
                     } else {
                        YAHOO.Bubbling.fire("refreshItemList", {
                           eventGroup : this
                        });

                     }
                  },
                  /**
                   * Display i18N label text
                   * 
                   * @param labelCode
                   */
                  _formatLabel : function SpelEditor__formatLabel(labelCode) {

                     var key = "form.control.spel-editor.item." + labelCode;

                     if (key.indexOf(" ") < 0 && this.msg(key) != key) {
                        return $html(this.msg(key));
                     }

                     return $html(labelCode);

                  },

                  /**
                   * Create YUI resizer widget
                   * 
                   * @method _createResizer
                   * @private
                   */
                  _createResizer : function SpelEditor__createResizer() {
                     if (!this.widgets.resizer) {
                        var size = parseInt(Dom.get(this.editorId + "-body").offsetWidth, 10) - 2, heightFix = 0;
                        this.columns[0] = Dom.get(this.editorId + "-left");
                        this.columns[1] = Dom.get(this.editorId + "-right");
                        this.widgets.resizer = new YAHOO.util.Resize(this.editorId + "-left", {
                           handles : [ "r" ],
                           minWidth : 200,
                           maxWidth : (size - 200)
                        });
                        // The resize handle doesn't quite get the element
                        // height correct, so it's saved here
                        heightFix = this.widgets.resizer.get("height");

                        this.widgets.resizer.on("resize", function(e) {
                           var w = e.width;
                           Dom.setStyle(this.columns[0], "height", "");
                           Dom.setStyle(this.columns[1], "width", (size - w - 8) + "px");
                        }, this, true);

                        this.widgets.resizer.on("endResize", function(e) {
                           // Reset the resize handle height to it's original
                           // value
                           this.set("height", heightFix);
                        });

                        this.widgets.resizer.fireEvent("resize", {
                           ev : 'resize',
                           target : this.widgets.resizer,
                           width : size / 2
                        });
                     }
                  },

                  /**
                   * Determines whether the editor is in 'authority' mode.
                   * 
                   * @method _enableActions
                   * @private
                   */
                  _enableActions : function SpelEditor__enableActions() {

                     if (this.widgets.showEditorButton) {
                        // Enable the add button
                        this.widgets.showEditorButton.set("disabled", false);
                     }
                  }
               });
   
})();
   
